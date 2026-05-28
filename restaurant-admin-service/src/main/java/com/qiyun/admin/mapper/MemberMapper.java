package com.qiyun.admin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qiyun.admin.entity.Member;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MemberMapper extends BaseMapper<Member> {
    
    /**
     * 统计今日新增会员数
     */
    @Select("SELECT COUNT(*) FROM member WHERE DATE(create_time) = CURDATE() AND deleted = 0")
    Integer countTodayMembers();
}
