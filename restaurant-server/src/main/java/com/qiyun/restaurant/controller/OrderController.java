package com.qiyun.restaurant.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiyun.restaurant.common.Result;
import com.qiyun.restaurant.service.OrderService;
import com.qiyun.restaurant.vo.OrderVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 订单Controller
 */
@RestController
@RequestMapping("/order")
@Tag(name = "订单接口")
@Slf4j
public class OrderController {
    
    @Autowired
    private OrderService orderService;
    
    /**
     * 分页查询订单
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询订单")
    public Result<Page<OrderVO>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String tableNo,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate
    ) {
        Page<OrderVO> orderPage = orderService.getOrderPage(page, size, orderNo, tableNo, status, startDate, endDate);
        return Result.success(orderPage);
    }
    
    /**
     * 根据ID获取订单详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取订单详情")
    public Result<OrderVO> getById(@PathVariable Long id) {
        OrderVO order = orderService.getOrderById(id);
        return Result.success(order);
    }
    
    /**
     * 更新订单状态
     */
    @PutMapping("/status/{id}")
    @Operation(summary = "更新订单状态")
    public Result<String> updateStatus(@PathVariable Long id, @RequestParam String status) {
        orderService.updateOrderStatus(id, status);
        return Result.success("更新成功");
    }
    
    /**
     * 获取实时订单
     */
    @GetMapping("/realtime")
    @Operation(summary = "获取实时订单")
    public Result<Page<OrderVO>> realtime(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size
    ) {
        Page<OrderVO> orderPage = orderService.getRealtimeOrders(page, size);
        return Result.success(orderPage);
    }
}
