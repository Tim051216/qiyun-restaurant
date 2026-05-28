package com.qiyun.admin.controller;

import com.qiyun.admin.common.Result;
import com.qiyun.admin.dto.AiChatRequest;
import com.qiyun.admin.dto.AiChatResponse;
import com.qiyun.admin.service.AiChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/ai")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // 允许跨域，开发环境使用
public class AiChatController {
    
    private final AiChatService aiChatService;
    
    @PostMapping("/chat")
    public Result<AiChatResponse> chat(@RequestBody AiChatRequest request) {
        log.info("收到AI聊天请求: {}", request.getMessage());
        
        try {
            AiChatResponse response = aiChatService.chat(request);
            return Result.success(response);
        } catch (Exception e) {
            log.error("AI聊天失败: {}", e.getMessage(), e);
            return Result.error("AI助手暂时无法回复，请稍后重试");
        }
    }
}
