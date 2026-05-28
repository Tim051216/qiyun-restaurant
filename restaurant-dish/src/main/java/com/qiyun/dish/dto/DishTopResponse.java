package com.qiyun.dish.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DishTopResponse {
    private Long id;
    private String name;
    private Integer sales;  // 销量
}
