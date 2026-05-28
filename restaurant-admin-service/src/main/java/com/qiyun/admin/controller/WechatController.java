package com.qiyun.admin.controller;

import com.qiyun.admin.common.Result;
import com.qiyun.admin.dto.WechatLoginRequest;
import com.qiyun.admin.dto.WechatLoginResponse;
import com.qiyun.admin.service.WechatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/wechat")
@RequiredArgsConstructor
public class WechatController {
    
    private final WechatService wechatService;
    
    @PostMapping("/login")
    public Result<WechatLoginResponse> login(@RequestBody WechatLoginRequest request) {
        log.info("微信登录请求");
        try {
            WechatLoginResponse response = wechatService.login(request);
            return Result.success(response);
        } catch (Exception e) {
            log.error("微信登录失败: {}", e.getMessage(), e);
            return Result.error("登录失败: " + e.getMessage());
        }
    }
}
