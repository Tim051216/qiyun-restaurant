package com.qiyun.restaurant.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 微信工具类
 */
@Component
@Slf4j
public class WeChatUtil {
    
    @Value("${wechat.miniapp.app-id}")
    private String appId;
    
    @Value("${wechat.miniapp.app-secret}")
    private String appSecret;
    
    private static final String CODE_TO_SESSION_URL = "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code";
    
    /**
     * 微信登录，获取openid
     */
    public String getOpenId(String code) {
        String url = String.format(CODE_TO_SESSION_URL, appId, appSecret, code);
        
        try {
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(url, String.class);
            
            log.info("微信登录响应: {}", response);
            
            JSONObject jsonObject = JSON.parseObject(response);
            
            if (jsonObject.containsKey("errcode")) {
                log.error("微信登录失败: {}", jsonObject.getString("errmsg"));
                return null;
            }
            
            return jsonObject.getString("openid");
        } catch (Exception e) {
            log.error("微信登录异常", e);
            return null;
        }
    }
}
