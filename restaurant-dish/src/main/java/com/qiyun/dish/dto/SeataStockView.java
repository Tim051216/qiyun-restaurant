package com.qiyun.dish.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Seata 演示的库存快照。
 */
@Data
@AllArgsConstructor
public class SeataStockView {

    private Long dishId;

    private String dishName;

    private Integer stock;

    private Integer sales;
}
