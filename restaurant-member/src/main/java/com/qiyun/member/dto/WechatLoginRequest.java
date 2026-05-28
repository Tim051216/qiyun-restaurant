package com.qiyun.member.dto;

import lombok.Data;

/**
 * 微信登录请求
 * 
 * @author qiyun
 * @since 2026-02-09
 */
@Data
public class WechatLoginRequest {
    
    /**
     * 微信登录凭证code
     */
    private String code;
    
    /**
     * 用户信息（可选）
     */
    private UserInfo userInfo;
    
    @Data
    public static class UserInfo {
        /**
         * 用户昵称
         */
        private String nickName;
        
        /**
         * 用户头像URL
         */
        private String avatarUrl;
        
        /**
         * 用户性别 0-未知 1-男 2-女
         */
        private Integer gender;
    }
}
