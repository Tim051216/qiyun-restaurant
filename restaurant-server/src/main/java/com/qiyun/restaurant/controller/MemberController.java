package com.qiyun.restaurant.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiyun.restaurant.common.Result;
import com.qiyun.restaurant.entity.Member;
import com.qiyun.restaurant.service.MemberService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 会员Controller
 */
@RestController
@RequestMapping("/member")
@Tag(name = "会员接口")
@Slf4j
public class MemberController {
    
    @Autowired
    private MemberService memberService;
    
    /**
     * 分页查询会员
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询会员")
    public Result<Page<Member>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String nickname,
            @RequestParam(required = false) String level
    ) {
        Page<Member> memberPage = memberService.getMemberPage(page, size, nickname, level);
        return Result.success(memberPage);
    }
    
    /**
     * 根据ID获取会员详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取会员详情")
    public Result<Member> getById(@PathVariable Long id) {
        Member member = memberService.getMemberById(id);
        return Result.success(member);
    }
    
    /**
     * 更新会员信息
     */
    @PutMapping
    @Operation(summary = "更新会员信息")
    public Result<String> update(@RequestBody Member member) {
        memberService.updateMember(member);
        return Result.success("更新成功");
    }
}
