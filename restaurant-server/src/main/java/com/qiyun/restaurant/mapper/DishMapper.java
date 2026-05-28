package com.qiyun.restaurant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qiyun.restaurant.entity.Dish;
import org.apache.ibatis.annotations.Mapper;

/**
 * 菜品Mapper
 */
@Mapper
public interface DishMapper extends BaseMapper<Dish> {
}
