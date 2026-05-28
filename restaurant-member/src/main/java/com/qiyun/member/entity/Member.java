package com.qiyun.member.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("member")
public class Member implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String openid;
    private String nickname;
    private String avatar;
    private String phone;
    private String level;
    private Integer points;
    private BigDecimal balance;
    private BigDecimal totalConsume;
    private Integer orderCount;
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    private Integer deleted;
}
