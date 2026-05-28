package com.qiyun.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Seata 演示中发送给菜品服务的扣库存参数。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeataStockDeductRequest {

    private Long dishId;

    private Integer count;
}
