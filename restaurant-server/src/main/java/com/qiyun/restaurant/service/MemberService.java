package com.qiyun.restaurant.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiyun.restaurant.entity.Member;

/**
 * 会员Service
 */
public interface MemberService {
    
    /**
     * 分页查询会员
     */
    Page<Member> getMemberPage(Integer page, Integer size, String nickname, String level);
    
    /**
     * 根据ID获取会员详情
     */
    Member getMemberById(Long id);
    
    /**
     * 更新会员信息
     */
    void updateMember(Member member);
    
    /**
     * 根据openid获取或创建会员
     */
    Member getOrCreateByOpenid(String openid);
}
