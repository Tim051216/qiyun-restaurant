package com.qiyun.restaurant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qiyun.restaurant.entity.DishCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 菜品分类Mapper
 */
@Mapper
public interface DishCategoryMapper extends BaseMapper<DishCategory> {
}
