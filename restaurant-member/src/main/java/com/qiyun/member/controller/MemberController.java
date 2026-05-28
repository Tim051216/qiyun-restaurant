package com.qiyun.member.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiyun.member.common.Result;
import com.qiyun.member.entity.Member;
import com.qiyun.member.service.MemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 会员控制器
 * 
 * @author qiyun
 * @since 2026-02-09
 */
@Slf4j
@RestController
@RequestMapping("/member")
public class MemberController {
    
    @Autowired
    private MemberService memberService;
    
    @GetMapping("/page")
    public Result<Page<Member>> pageMember(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String level) {
        log.info("分页查询会员: page={}, pageSize={}, nickname={}, phone={}, level={}", 
                page, pageSize, nickname, phone, level);
        
        Page<Member> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Member> queryWrapper = new LambdaQueryWrapper<>();
        
        // 条件查询
        if (nickname != null && !nickname.isEmpty()) {
            queryWrapper.like(Member::getNickname, nickname);
        }
        if (phone != null && !phone.isEmpty()) {
            queryWrapper.like(Member::getPhone, phone);
        }
        if (level != null) {
            queryWrapper.eq(Member::getLevel, level);
        }
        
        // 只查询未删除的记录
        queryWrapper.eq(Member::getDeleted, 0);
        
        // 按创建时间降序
        queryWrapper.orderByDesc(Member::getCreateTime);
        
        Page<Member> result = memberService.page(pageInfo, queryWrapper);
        return Result.success(result);
    }
    
    @GetMapping("/{id}")
    public Result<Member> getMemberById(@PathVariable Long id) {
        log.info("查询会员详情: memberId={}", id);
        Member member = memberService.getById(id);
        return Result.success(member);
    }
    
    @PutMapping
    public Result<Void> updateMember(@RequestBody Member member) {
        log.info("更新会员信息: {}", member);
        memberService.updateById(member);
        return Result.success(null);
    }
}
