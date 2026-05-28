package com.qiyun.admin.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qiyun.admin.dto.AiChatRequest;
import com.qiyun.admin.dto.AiChatResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class AiChatService {
    
    // API Key 安全存储在配置文件中
    @Value("${ai.dashscope.api-key:sk-7daca2a765c440c39ada2566908488eb}")
    private String apiKey;
    
    @Value("${ai.dashscope.api-url:https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation}")
    private String apiUrl;
    
    @Value("${ai.dashscope.model:qwen-turbo}")
    private String model;
    
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 频率限制
    private final Map<String, RateLimitInfo> rateLimitMap = new HashMap<>();
    private static final int RATE_LIMIT = 10; // 每分钟最多10次
    private static final int MAX_HISTORY = 5; // 最多保留5条历史
    
    public AiChatResponse chat(AiChatRequest request) throws Exception {
        // 频率限制检查
        checkRateLimit(request);
        
        // 限制历史记录数量
        List<AiChatRequest.ChatMessage> history = request.getHistory();
        if (history != null && history.size() > MAX_HISTORY) {
            history = history.subList(history.size() - MAX_HISTORY, history.size());
        }
        
        // 构建消息列表
        List<Map<String, String>> messages = new ArrayList<>();
        
        // 系统提示
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "你是七云菜馆的智能点餐助手，你可以帮助用户推荐菜品、解答问题、协助点餐。请用友好、专业的语气回答用户问题。");
        messages.add(systemMessage);
        
        // 历史消息
        if (history != null) {
            for (AiChatRequest.ChatMessage msg : history) {
                Map<String, String> historyMsg = new HashMap<>();
                historyMsg.put("role", msg.getRole());
                historyMsg.put("content", msg.getContent());
                messages.add(historyMsg);
            }
        }
        
        // 当前消息
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", request.getMessage());
        messages.add(userMessage);
        
        // 构建请求体
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        
        Map<String, Object> input = new HashMap<>();
        input.put("messages", messages);
        requestBody.put("input", input);
        
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("result_format", "message");
        parameters.put("max_tokens", 500);
        parameters.put("temperature", 0.7);
        requestBody.put("parameters", parameters);
        
        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        log.info("调用通义千问API: {}", apiUrl);
        
        // 发送请求
        ResponseEntity<String> response = restTemplate.exchange(
            apiUrl,
            HttpMethod.POST,
            entity,
            String.class
        );
        
        // 解析响应
        if (response.getStatusCode() == HttpStatus.OK) {
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            String reply = jsonNode.path("output").path("choices").get(0)
                .path("message").path("content").asText();
            JsonNode usage = jsonNode.path("usage");
            
            log.info("AI回复成功: {}", reply);
            return new AiChatResponse(reply, usage);
        } else {
            log.error("AI调用失败: {}", response.getBody());
            throw new RuntimeException("AI服务调用失败");
        }
    }
    
    private void checkRateLimit(AiChatRequest request) {
        String userId = "default"; // 可以根据实际情况获取用户ID
        long now = System.currentTimeMillis();
        
        RateLimitInfo info = rateLimitMap.computeIfAbsent(userId, k -> new RateLimitInfo());
        
        if (now - info.timestamp < 60000) { // 1分钟内
            if (info.count >= RATE_LIMIT) {
                throw new RuntimeException("请求过于频繁，请稍后再试");
            }
            info.count++;
        } else {
            info.count = 1;
            info.timestamp = now;
        }
    }
    
    private static class RateLimitInfo {
        int count = 0;
        long timestamp = System.currentTimeMillis();
    }
}
