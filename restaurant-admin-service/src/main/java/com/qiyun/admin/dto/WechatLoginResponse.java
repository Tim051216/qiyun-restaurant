package com.qiyun.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WechatLoginResponse {
    private String token;
    private Long memberId;
    private String nickname;
    private String avatar;
    private String phone;
    private String level;
    private Integer points;
}
