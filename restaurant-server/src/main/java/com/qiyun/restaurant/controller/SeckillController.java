package com.qiyun.restaurant.controller;

import com.qiyun.restaurant.common.Result;
import com.qiyun.restaurant.service.SeckillService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 秒杀Controller
 * 
 * 用于演示Redis解决高并发问�?
 */
@RestController
@RequestMapping("/seckill")
@Tag(name = "秒杀管理")
@Slf4j
public class SeckillController {

    @Autowired
    private SeckillService seckillService;

    /**
     * 初始化秒杀库存
     */
    @PostMapping("/init")
    @Operation(summary = "初始化秒杀库存")
    public Result<String> initStock(@RequestParam Long activityId, @RequestParam Integer stock) {
        seckillService.initSeckillStock(activityId, stock);
        return Result.success("初始化成�?);
    }

    /**
     * 秒杀下单
     */
    @PostMapping("/order")
    @Operation(summary = "秒杀下单")
    public Result<String> seckillOrder(@RequestParam Long activityId, @RequestParam Long memberId) {
        boolean success = seckillService.seckillOrder(activityId, memberId);
        if (success) {
            return Result.success("秒杀成功");
        } else {
            return Result.error("秒杀失败");
        }
    }

    /**
     * 查询秒杀库存
     */
    @GetMapping("/stock")
    @Operation(summary = "查询秒杀库存")
    public Result<Integer> getStock(@RequestParam Long activityId) {
        Integer stock = seckillService.getSeckillStock(activityId);
        return Result.success(stock);
    }
}
