package com.qiyun.dish.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiyun.dish.common.Result;
import com.qiyun.dish.entity.Dish;
import com.qiyun.dish.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜品控制器
 * 
 * @author qiyun
 * @since 2026-02-08
 */
@Slf4j
@RestController
@RequestMapping("/dish")
public class DishController {
    
    @Autowired
    private DishService dishService;
    
    @GetMapping("/{id}")
    public Result<Dish> getDishById(@PathVariable Long id) {
        log.info("查询菜品: dishId={}", id);
        Dish dish = dishService.getDishByIdWithCache(id);
        return Result.success(dish);
    }
    
    @PostMapping("/batch")
    public Result<List<Dish>> getDishByIds(@RequestBody List<Long> ids) {
        log.info("批量查询菜品: dishIds={}", ids);
        List<Dish> dishes = dishService.getDishByIds(ids);
        return Result.success(dishes);
    }
    
    @GetMapping("/list")
    public Result<List<Dish>> listDishes() {
        log.info("查询所有菜品");
        List<Dish> dishes = dishService.list();
        return Result.success(dishes);
    }
    
    @GetMapping("/page")
    public Result<Page<Dish>> pageDishes(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status) {
        log.info("分页查询菜品: page={}, pageSize={}, name={}, categoryId={}, status={}", 
                page, pageSize, name, categoryId, status);
        
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        
        // 条件查询
        if (name != null && !name.isEmpty()) {
            queryWrapper.like(Dish::getName, name);
        }
        if (categoryId != null) {
            queryWrapper.eq(Dish::getCategoryId, categoryId);
        }
        if (status != null) {
            queryWrapper.eq(Dish::getStatus, status);
        }
        
        // 按更新时间降序
        queryWrapper.orderByDesc(Dish::getUpdateTime);
        
        Page<Dish> result = dishService.page(pageInfo, queryWrapper);
        return Result.success(result);
    }
}
