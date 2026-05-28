package com.qiyun.dish.controller;

import com.qiyun.dish.common.Result;
import com.qiyun.dish.dto.SeataStockDeductRequest;
import com.qiyun.dish.dto.SeataStockView;
import com.qiyun.dish.entity.Dish;
import com.qiyun.dish.service.DishService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Seata 演示专用接口，用于展示库存回滚。
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/dish/seata/demo")
public class DishSeataDemoController {

    private final DishService dishService;

    @PostMapping("/deduct")
    public Result<Boolean> deduct(@RequestBody SeataStockDeductRequest request) {
        log.info("Seata 演示扣减库存: dishId={}, count={}", request.getDishId(), request.getCount());
        return Result.success(dishService.deductStockForSeataDemo(request.getDishId(), request.getCount()));
    }

    @GetMapping("/stock/{dishId}")
    public Result<SeataStockView> stock(@PathVariable Long dishId) {
        Dish dish = dishService.getById(dishId);
        if (dish == null) {
            return Result.error("菜品不存在");
        }
        return Result.success(new SeataStockView(dish.getId(), dish.getName(), dish.getStock(), dish.getSales()));
    }
}
