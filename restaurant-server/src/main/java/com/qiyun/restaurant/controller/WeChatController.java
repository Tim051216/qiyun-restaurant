package com.qiyun.restaurant.controller;

import com.qiyun.restaurant.common.Result;
import com.qiyun.restaurant.entity.Member;
import com.qiyun.restaurant.service.MemberService;
import com.qiyun.restaurant.utils.JwtUtil;
import com.qiyun.restaurant.utils.WeChatUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 微信小程序Controller
 */
@RestController
@RequestMapping("/wechat")
@Api(tags = "微信小程序接�?)
@Slf4j
public class WeChatController {
    
    @Autowired
    private WeChatUtil weChatUtil;
    
    @Autowired
    private MemberService memberService;
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration}")
    private Long jwtExpiration;
    
    /**
     * 微信登录
     */
    @PostMapping("/login")
    @Operation(summary = "微信登录")
    public Result<Map<String, Object>> login(@RequestBody Map<String, Object> params) {
        log.info("微信登录请求: {}", params);
        
        String code = (String) params.get("code");
        
        if (code == null || code.isEmpty()) {
            return Result.error("code不能为空");
        }
        
        // 获取openid
        String openid = weChatUtil.getOpenId(code);
        
        if (openid == null) {
            return Result.error("微信登录失败");
        }
        
        log.info("获取到openid: {}", openid);
        
        // 获取用户信息（如果有�?
        @SuppressWarnings("unchecked")
        Map<String, Object> userInfo = (Map<String, Object>) params.get("userInfo");
        
        // 查询或创建会�?
        Member member = memberService.getOrCreateByOpenid(openid);
        
        // 如果传递了用户信息，更新会员信�?
        if (userInfo != null && member != null) {
            String nickname = (String) userInfo.get("nickName");
            String avatar = (String) userInfo.get("avatarUrl");
            
            if (nickname != null && !nickname.isEmpty()) {
                member.setNickname(nickname);
            }
            if (avatar != null && !avatar.isEmpty()) {
                member.setAvatar(avatar);
            }
            
            // 更新会员信息
            memberService.updateMember(member);
            log.info("更新会员信息: nickname={}, avatar={}", nickname, avatar);
        }
        
        log.info("会员信息: {}", member);
        
        // 生成token
        Map<String, Object> claims = new HashMap<>();
        claims.put("memberId", member.getId());
        claims.put("openid", member.getOpenid());
        String token = JwtUtil.createJWT(jwtSecret, jwtExpiration * 1000, claims);
        
        log.info("生成token: {}", token);
        
        // 返回数据
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("member", member);
        
        return Result.success(result);
    }
}
