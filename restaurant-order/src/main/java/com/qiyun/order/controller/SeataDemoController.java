package com.qiyun.order.controller;

import com.qiyun.order.common.Result;
import com.qiyun.order.dto.SeataDemoOrderRequest;
import com.qiyun.order.entity.SeataDemoOrder;
import com.qiyun.order.service.SeataDemoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Seata 分布式事务演示接口。
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/order/seata/demo")
public class SeataDemoController {

    private final SeataDemoService seataDemoService;

    @PostMapping("/place")
    public Result<SeataDemoOrder> place(@RequestBody SeataDemoOrderRequest request) {
        log.info("Seata 演示下单请求: dishId={}, count={}, failAfterDeduct={}",
            request.getDishId(), request.getCount(), request.getFailAfterDeduct());
        return Result.success(seataDemoService.placeOrder(request));
    }

    @GetMapping("/orders")
    public Result<List<SeataDemoOrder>> orders() {
        return Result.success(seataDemoService.listOrders());
    }
}
