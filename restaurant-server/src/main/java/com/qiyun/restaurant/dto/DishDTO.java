package com.qiyun.restaurant.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 菜品DTO
 */
@Data
public class DishDTO {
    
    private Long id;
    
    @NotBlank(message = "菜品名称不能为空")
    private String name;
    
    @NotNull(message = "分类ID不能为空")
    private Long categoryId;
    
    @NotNull(message = "价格不能为空")
    private BigDecimal price;
    
    private String image;
    
    private String description;
    
    private Integer stock;
    
    private Integer status;
}
