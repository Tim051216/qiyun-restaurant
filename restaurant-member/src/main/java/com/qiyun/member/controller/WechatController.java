package com.qiyun.member.controller;

import com.qiyun.member.common.Result;
import com.qiyun.member.dto.WechatLoginRequest;
import com.qiyun.member.dto.WechatLoginResponse;
import com.qiyun.member.service.WechatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 微信登录控制器
 * 
 * @author qiyun
 * @since 2026-02-09
 */
@Slf4j
@RestController
@RequestMapping("")  // 空路径，因为 Gateway 已经处理了 /wechat 前缀
public class WechatController {
    
    @Autowired
    private WechatService wechatService;
    
    /**
     * 微信小程序登录
     * @param request 登录请求（包含code和用户信息）
     * @return 登录响应（包含token和用户信息）
     */
    @PostMapping("/login")
    public Result<WechatLoginResponse> login(@RequestBody WechatLoginRequest request) {
        log.info("微信登录请求: code={}", request.getCode());
        
        try {
            WechatLoginResponse response = wechatService.login(request);
            return Result.success(response);
        } catch (Exception e) {
            log.error("微信登录失败", e);
            return Result.error("登录失败: " + e.getMessage());
        }
    }
}
