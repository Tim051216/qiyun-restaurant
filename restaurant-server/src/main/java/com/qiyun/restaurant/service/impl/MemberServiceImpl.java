package com.qiyun.restaurant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiyun.restaurant.entity.Member;
import com.qiyun.restaurant.mapper.MemberMapper;
import com.qiyun.restaurant.service.MemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 会员Service实现
 */
@Service
@Slf4j
public class MemberServiceImpl implements MemberService {
    
    @Autowired
    private MemberMapper memberMapper;
    
    @Override
    public Page<Member> getMemberPage(Integer page, Integer size, String nickname, String level) {
        Page<Member> memberPage = new Page<>(page, size);
        
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(nickname), Member::getNickname, nickname)
               .eq(StringUtils.hasText(level), Member::getLevel, level)
               .orderByDesc(Member::getCreateTime);
        
        memberMapper.selectPage(memberPage, wrapper);
        
        return memberPage;
    }
    
    @Override
    public Member getMemberById(Long id) {
        Member member = memberMapper.selectById(id);
        if (member == null) {
            throw new RuntimeException("会员不存在");
        }
        return member;
    }
    
    @Override
    public void updateMember(Member member) {
        memberMapper.updateById(member);
    }
    
    @Override
    public Member getOrCreateByOpenid(String openid) {
        // 查询会员是否存在
        LambdaQueryWrapper<Member> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Member::getOpenid, openid);
        Member member = memberMapper.selectOne(wrapper);
        
        // 如果不存在则创建新会员
        if (member == null) {
            member = new Member();
            member.setOpenid(openid);
            member.setNickname("微信用户");
            member.setLevel("V1");
            member.setPoints(0);
            member.setBalance(java.math.BigDecimal.ZERO);
            member.setTotalConsume(java.math.BigDecimal.ZERO);
            member.setOrderCount(0);
            member.setStatus(1);
            memberMapper.insert(member);
        }
        
        return member;
    }
}
