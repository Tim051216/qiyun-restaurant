package com.qiyun.restaurant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qiyun.restaurant.entity.Member;
import org.apache.ibatis.annotations.Mapper;

/**
 * 会员Mapper
 */
@Mapper
public interface MemberMapper extends BaseMapper<Member> {
}
