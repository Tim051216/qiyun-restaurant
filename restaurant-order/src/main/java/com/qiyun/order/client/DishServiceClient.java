package com.qiyun.order.client;

import com.qiyun.order.common.Result;
import com.qiyun.order.dto.DishDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * 菜品服务Feign客户端
 * 
 * @author qiyun
 * @since 2026-02-08
 */
@FeignClient(
    name = "restaurant-dish-service",
    fallback = DishServiceClientFallback.class
)
public interface DishServiceClient {
    
    /**
     * 根据ID查询菜品
     */
    @GetMapping("/dish/{id}")
    Result<DishDTO> getDishById(@PathVariable("id") Long id);
    
    /**
     * 批量查询菜品
     */
    @PostMapping("/dish/batch")
    Result<List<DishDTO>> getDishByIds(@RequestBody List<Long> ids);
}
