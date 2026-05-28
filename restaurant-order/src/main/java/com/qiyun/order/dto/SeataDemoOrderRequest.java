package com.qiyun.order.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * Seata 分布式事务演示下单请求。
 */
@Data
public class SeataDemoOrderRequest {

    private Long userId;

    private Long dishId;

    private Integer count;

    private BigDecimal amount;

    private Boolean failAfterDeduct;

    private String remark;
}
