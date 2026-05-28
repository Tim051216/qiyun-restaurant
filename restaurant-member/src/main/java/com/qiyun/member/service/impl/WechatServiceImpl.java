package com.qiyun.member.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qiyun.member.dto.WechatLoginRequest;
import com.qiyun.member.dto.WechatLoginResponse;
import com.qiyun.member.entity.Member;
import com.qiyun.member.mapper.MemberMapper;
import com.qiyun.member.service.WechatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 微信服务实现
 * 
 * @author qiyun
 * @since 2026-02-09
 */
@Slf4j
@Service
public class WechatServiceImpl implements WechatService {
    
    @Autowired
    private MemberMapper memberMapper;
    
    @Override
    public WechatLoginResponse login(WechatLoginRequest request) {
        String code = request.getCode();
        
        // TODO: 实际项目中需要调用微信API获取openid和session_key
        // 这里为了演示，使用code作为openid的模拟值
        String openid = "mock_openid_" + code;
        
        log.info("微信登录: openid={}", openid);
        
        // 查询是否已存在该会员
        LambdaQueryWrapper<Member> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Member::getOpenid, openid);
        queryWrapper.eq(Member::getDeleted, 0);
        Member member = memberMapper.selectOne(queryWrapper);
        
        boolean isNewUser = false;
        
        // 如果不存在，创建新会员
        if (member == null) {
            isNewUser = true;
            member = new Member();
            member.setOpenid(openid);
            
            // 设置用户信息
            if (request.getUserInfo() != null) {
                WechatLoginRequest.UserInfo userInfo = request.getUserInfo();
                member.setNickname(userInfo.getNickName() != null ? userInfo.getNickName() : "微信用户");
                member.setAvatar(userInfo.getAvatarUrl());
                // gender字段在Member实体中不存在，暂时不设置
            } else {
                member.setNickname("微信用户");
            }
            
            // 设置默认值
            member.setLevel("普通会员");
            member.setPoints(0);
            member.setBalance(new java.math.BigDecimal("0.00"));
            member.setDeleted(0);
            member.setCreateTime(LocalDateTime.now());
            member.setUpdateTime(LocalDateTime.now());
            
            memberMapper.insert(member);
            log.info("创建新会员: memberId={}, nickname={}", member.getId(), member.getNickname());
        } else {
            // 更新用户信息（如果提供了）
            if (request.getUserInfo() != null) {
                WechatLoginRequest.UserInfo userInfo = request.getUserInfo();
                boolean needUpdate = false;
                
                if (userInfo.getNickName() != null && !userInfo.getNickName().equals(member.getNickname())) {
                    member.setNickname(userInfo.getNickName());
                    needUpdate = true;
                }
                if (userInfo.getAvatarUrl() != null && !userInfo.getAvatarUrl().equals(member.getAvatar())) {
                    member.setAvatar(userInfo.getAvatarUrl());
                    needUpdate = true;
                }
                
                if (needUpdate) {
                    member.setUpdateTime(LocalDateTime.now());
                    memberMapper.updateById(member);
                    log.info("更新会员信息: memberId={}", member.getId());
                }
            }
            
            log.info("会员登录: memberId={}, nickname={}", member.getId(), member.getNickname());
        }
        
        // 生成token（实际项目中应使用JWT）
        String token = UUID.randomUUID().toString().replace("-", "");
        
        // 构建响应
        return WechatLoginResponse.builder()
                .memberId(member.getId())
                .token(token)
                .nickname(member.getNickname())
                .avatar(member.getAvatar())
                .level(member.getLevel())
                .points(member.getPoints())
                .isNewUser(isNewUser)
                .build();
    }
}
