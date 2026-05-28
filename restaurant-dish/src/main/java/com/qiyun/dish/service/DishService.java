package com.qiyun.dish.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qiyun.dish.entity.Dish;

import java.util.List;

/**
 * 菜品服务接口
 *
 * @author qiyun
 * @since 2026-02-08
 */
public interface DishService extends IService<Dish> {

    /**
     * 根据 ID 查询菜品，并带缓存
     */
    Dish getDishByIdWithCache(Long id);

    /**
     * 批量查询菜品
     */
    List<Dish> getDishByIds(List<Long> ids);

    /**
     * Seata 演示：扣减库存并增加销量
     */
    boolean deductStockForSeataDemo(Long dishId, Integer count);
}
