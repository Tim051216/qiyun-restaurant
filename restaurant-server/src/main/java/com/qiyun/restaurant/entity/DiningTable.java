package com.qiyun.restaurant.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 桌台实体
 */
@Data
@TableName("dining_table")
public class DiningTable implements Serializable {
    
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String tableNo;
    
    private Integer capacity;
    
    private String status;
    
    private String qrCode;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    @TableLogic
    private Integer deleted;
}
