package com.qiyun.dish.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qiyun.dish.dto.DishTopResponse;
import com.qiyun.dish.entity.Dish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 菜品Mapper
 * 
 * @author qiyun
 * @since 2026-02-08
 */
@Mapper
public interface DishMapper extends BaseMapper<Dish> {
    
    /**
     * 查询热点菜品（用于缓存预热）
     * 
     * 选择标准：
     * 1. 状态为可售（status = 1）
     * 2. 按创建时间倒序，取最新的100条
     * 
     * @return 热点菜品列表
     */
    @Select("SELECT * FROM dish WHERE status = 1 ORDER BY create_time DESC LIMIT 100")
    List<Dish> selectHotDishes();
    
    /**
     * 查询菜品销量TOP
     */
    @Select("SELECT id, name, sales FROM dish WHERE deleted = 0 ORDER BY sales DESC LIMIT #{limit}")
    List<DishTopResponse> selectTopSales(Integer limit);
}
