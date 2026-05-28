package com.qiyun.order.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 菜品DTO
 * 
 * @author qiyun
 * @since 2026-02-08
 */
@Data
public class DishDTO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long id;
    
    private String name;
    
    private BigDecimal price;
    
    private Integer status;
    
    private String image;
}
