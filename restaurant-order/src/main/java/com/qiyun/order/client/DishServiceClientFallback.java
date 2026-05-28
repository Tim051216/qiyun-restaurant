package com.qiyun.order.client;

import com.qiyun.order.common.Result;
import com.qiyun.order.dto.DishDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 菜品服务降级处理
 * 
 * @author qiyun
 * @since 2026-02-08
 */
@Slf4j
@Component
public class DishServiceClientFallback implements DishServiceClient {
    
    @Override
    public Result<DishDTO> getDishById(Long id) {
        log.warn("菜品服务调用失败，执行降级逻辑: dishId={}", id);
        return Result.error("菜品服务暂时不可用，请稍后再试");
    }
    
    @Override
    public Result<List<DishDTO>> getDishByIds(List<Long> ids) {
        log.warn("菜品服务批量查询失败，执行降级逻辑: dishIds={}", ids);
        return Result.error("菜品服务暂时不可用，请稍后再试");
    }
}
