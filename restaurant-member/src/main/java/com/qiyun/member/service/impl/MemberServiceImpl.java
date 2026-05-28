package com.qiyun.member.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiyun.member.entity.Member;
import com.qiyun.member.mapper.MemberMapper;
import com.qiyun.member.service.MemberService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MemberServiceImpl extends ServiceImpl<MemberMapper, Member> implements MemberService {
}
