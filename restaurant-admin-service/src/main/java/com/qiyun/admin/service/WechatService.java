package com.qiyun.admin.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.qiyun.admin.dto.WechatLoginRequest;
import com.qiyun.admin.dto.WechatLoginResponse;
import com.qiyun.admin.entity.Member;
import com.qiyun.admin.mapper.MemberMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WechatService {
    
    private final MemberMapper memberMapper;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Value("${wechat.appid:wxc63d198986077877}")
    private String appId;
    
    @Value("${wechat.secret:}")
    private String appSecret;
    
    /**
     * 微信登录
     */
    public WechatLoginResponse login(WechatLoginRequest request) throws Exception {
        log.info("微信登录请求, code: {}", request.getCode());
        
        // 1. 调用微信接口获取 openid
        String openid = getOpenidFromWechat(request.getCode());
        log.info("获取到 openid: {}", openid);
        
        // 2. 根据 openid 查询或创建会员
        Member member = findOrCreateMember(openid);
        
        // 3. 生成 token
        String token = generateToken(member.getId());
        
        // 4. 返回登录响应
        return new WechatLoginResponse(
            token,
            member.getId(),
            member.getNickname(),
            member.getAvatar(),
            member.getPhone(),
            member.getLevel(),
            member.getPoints()
        );
    }
    
    /**
     * 调用微信接口获取 openid
     */
    private String getOpenidFromWechat(String code) throws Exception {
        String url = String.format(
            "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
            appId, appSecret, code
        );
        
        log.info("调用微信接口: {}", url);
        String response = restTemplate.getForObject(url, String.class);
        log.info("微信接口响应: {}", response);
        
        JsonNode jsonNode = objectMapper.readTree(response);
        
        // 检查是否有错误
        if (jsonNode.has("errcode")) {
            int errcode = jsonNode.get("errcode").asInt();
            if (errcode != 0) {
                String errmsg = jsonNode.get("errmsg").asText();
                log.error("微信接口返回错误: errcode={}, errmsg={}", errcode, errmsg);
                throw new RuntimeException("微信登录失败: " + errmsg);
            }
        }
        
        String openid = jsonNode.get("openid").asText();
        if (openid == null || openid.isEmpty()) {
            throw new RuntimeException("获取 openid 失败");
        }
        
        return openid;
    }
    
    /**
     * 根据 openid 查询或创建会员
     */
    private Member findOrCreateMember(String openid) {
        // 查询是否已存在
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Member::getOpenid, openid);
        wrapper.eq(Member::getDeleted, 0);
        Member member = memberMapper.selectOne(wrapper);
        
        if (member != null) {
            log.info("会员已存在, id: {}", member.getId());
            return member;
        }
        
        // 创建新会员
        member = new Member();
        member.setOpenid(openid);
        member.setNickname("微信用户");
        member.setAvatar("");
        member.setLevel("普通会员");
        member.setPoints(0);
        member.setStatus(1);
        member.setCreateTime(LocalDateTime.now());
        member.setUpdateTime(LocalDateTime.now());
        member.setDeleted(0);
        
        memberMapper.insert(member);
        log.info("创建新会员成功, id: {}", member.getId());
        
        return member;
    }
    
    /**
     * 生成 token
     */
    private String generateToken(Long memberId) {
        // 简单实现: 使用 UUID + memberId
        // 实际项目中应该使用 JWT 或 Redis 存储
        return UUID.randomUUID().toString().replace("-", "") + "_" + memberId;
    }
}
