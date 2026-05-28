package com.qiyun.dish.dto;

import lombok.Data;

/**
 * Seata 演示的库存扣减请求。
 */
@Data
public class SeataStockDeductRequest {

    private Long dishId;

    private Integer count;
}
