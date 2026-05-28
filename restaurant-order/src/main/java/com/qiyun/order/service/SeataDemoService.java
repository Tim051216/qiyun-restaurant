package com.qiyun.order.service;

import com.qiyun.order.dto.SeataDemoOrderRequest;
import com.qiyun.order.entity.SeataDemoOrder;

import java.util.List;

/**
 * Seata 分布式事务演示服务。
 */
public interface SeataDemoService {

    SeataDemoOrder placeOrder(SeataDemoOrderRequest request);

    List<SeataDemoOrder> listOrders();
}
