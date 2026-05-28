package com.qiyun.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单明细实体
 * 
 * @author qiyun
 * @since 2026-02-08
 */
@Data
@TableName("t_order_detail")
public class OrderDetail implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    
    private Long orderId;
    
    private Long dishId;
    
    private String dishName;
    
    private BigDecimal price;
    
    private Integer quantity;
    
    private BigDecimal amount;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
