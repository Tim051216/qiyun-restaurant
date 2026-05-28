package com.qiyun.restaurant.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 订单明细VO
 */
@Data
public class OrderDetailVO {
    
    private Long id;
    
    private Long orderId;
    
    private Long dishId;
    
    private String dishName;
    
    private BigDecimal price;
    
    private Integer quantity;
    
    private BigDecimal amount;
}
