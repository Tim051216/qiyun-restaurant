package com.qiyun.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("activity")
public class Activity implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String title;
    
    private String category;
    
    private String content;
    
    private String image;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private Integer participants;
    
    private String status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    private Integer deleted;
}
