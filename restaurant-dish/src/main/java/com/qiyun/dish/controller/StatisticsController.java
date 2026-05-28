package com.qiyun.dish.controller;

import com.qiyun.dish.common.Result;
import com.qiyun.dish.dto.DishTopResponse;
import com.qiyun.dish.mapper.DishMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class StatisticsController {
    
    private final DishMapper dishMapper;
    
    @GetMapping("/dish/top")
    public Result<List<DishTopResponse>> getDishTop(@RequestParam(defaultValue = "5") Integer limit) {
        log.info("获取菜品销量TOP: limit={}", limit);
        
        // 从数据库查询真实销量数据
        List<DishTopResponse> topDishes = dishMapper.selectTopSales(limit);
        
        return Result.success(topDishes);
    }
}
