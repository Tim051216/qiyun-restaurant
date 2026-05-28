package com.qiyun.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 微信登录响应
 * 
 * @author qiyun
 * @since 2026-02-09
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WechatLoginResponse {
    
    /**
     * 会员ID
     */
    private Long memberId;
    
    /**
     * 登录token
     */
    private String token;
    
    /**
     * 用户昵称
     */
    private String nickname;
    
    /**
     * 用户头像
     */
    private String avatar;
    
    /**
     * 会员等级
     */
    private String level;
    
    /**
     * 积分
     */
    private Integer points;
    
    /**
     * 是否新用户
     */
    private Boolean isNewUser;
}
