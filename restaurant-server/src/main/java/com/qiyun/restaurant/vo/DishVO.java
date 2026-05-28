package com.qiyun.restaurant.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 菜品VO
 */
@Data
public class DishVO {
    
    private Long id;
    
    private String name;
    
    private Long categoryId;
    
    private String categoryName;
    
    private BigDecimal price;
    
    private String image;
    
    private String description;
    
    private Integer sales;
    
    private Integer stock;
    
    private Integer status;
    
    private LocalDateTime createTime;
}
