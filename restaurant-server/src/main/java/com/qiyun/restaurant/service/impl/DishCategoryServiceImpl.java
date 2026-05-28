package com.qiyun.restaurant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiyun.restaurant.entity.Dish;
import com.qiyun.restaurant.entity.DishCategory;
import com.qiyun.restaurant.mapper.DishCategoryMapper;
import com.qiyun.restaurant.mapper.DishMapper;
import com.qiyun.restaurant.service.DishCategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 菜品分类Service实现
 */
@Service
@Slf4j
public class DishCategoryServiceImpl extends ServiceImpl<DishCategoryMapper, DishCategory> implements DishCategoryService {
    
    @Autowired
    private DishMapper dishMapper;
    
    @Override
    public List<DishCategory> getAllCategories() {
        LambdaQueryWrapper<DishCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(DishCategory::getSort);
        return list(queryWrapper);
    }
    
    @Override
    public void addCategory(DishCategory category) {
        save(category);
    }
    
    @Override
    public void updateCategory(DishCategory category) {
        updateById(category);
    }
    
    @Override
    public void deleteCategory(Long id) {
        // 检查该分类下是否有菜品
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dish::getCategoryId, id);
        long count = dishMapper.selectCount(queryWrapper);
        
        if (count > 0) {
            throw new RuntimeException("该分类下还有菜品，无法删除");
        }
        
        removeById(id);
    }
}
