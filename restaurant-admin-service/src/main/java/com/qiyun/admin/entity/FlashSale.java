package com.qiyun.admin.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("flash_sale")
public class FlashSale implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    
    private String title;
    
    private String description;
    
    private String image;
    
    private String sessionTime;
    
    private LocalDateTime startTime;
    
    private LocalDateTime endTime;
    
    private BigDecimal flashPrice;
    
    private BigDecimal originalPrice;
    
    private Integer totalStock;
    
    private Integer soldCount;
    
    private Integer limitPerUser;
    
    private String status;
    
    private Integer hot;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    private Integer deleted;
}
