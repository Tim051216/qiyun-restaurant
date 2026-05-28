package com.qiyun.dish.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiyun.dish.entity.Dish;
import com.qiyun.dish.mapper.DishMapper;
import com.qiyun.dish.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 菜品服务实现
 *
 * @author qiyun
 * @since 2026-02-08
 */
@Slf4j
@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    @Cacheable(value = "dish", key = "#id")
    public Dish getDishByIdWithCache(Long id) {
        log.info("查询菜品: dishId={}", id);

        String key = "dish:" + id;
        Object obj = redisTemplate.opsForValue().get(key);
        Dish dish = obj != null ? (Dish) obj : null;

        if (dish != null) {
            log.info("从 Redis 缓存获取菜品: dishId={}", id);
            return dish;
        }

        dish = this.getById(id);

        if (dish != null) {
            redisTemplate.opsForValue().set(key, dish, 5, TimeUnit.MINUTES);
            log.info("菜品写入 Redis 缓存: dishId={}", id);
        }

        return dish;
    }

    @Override
    public List<Dish> getDishByIds(List<Long> ids) {
        log.info("批量查询菜品: dishIds={}", ids);
        return this.listByIds(ids);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deductStockForSeataDemo(Long dishId, Integer count) {
        if (count == null || count <= 0) {
            throw new IllegalArgumentException("数量必须大于 0");
        }

        Dish dish = this.getById(dishId);
        if (dish == null) {
            throw new IllegalArgumentException("菜品不存在: " + dishId);
        }
        if (dish.getStock() == null || dish.getStock() < count) {
            throw new IllegalStateException("菜品库存不足: " + dishId);
        }

        LambdaUpdateWrapper<Dish> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Dish::getId, dishId)
            .ge(Dish::getStock, count)
            .setSql("stock = stock - " + count + ", sales = sales + " + count);

        boolean updated = this.update(updateWrapper);
        if (!updated) {
            throw new IllegalStateException("扣减库存失败: " + dishId);
        }

        return true;
    }
}
