package com.qiyun.member.service;

import com.qiyun.member.dto.WechatLoginRequest;
import com.qiyun.member.dto.WechatLoginResponse;

/**
 * 微信服务接口
 * 
 * @author qiyun
 * @since 2026-02-09
 */
public interface WechatService {
    
    /**
     * 微信小程序登录
     * @param request 登录请求
     * @return 登录响应
     */
    WechatLoginResponse login(WechatLoginRequest request);
}
