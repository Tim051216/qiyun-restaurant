package com.qiyun.restaurant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qiyun.restaurant.entity.DishCategory;
import java.util.List;

/**
 * 菜品分类Service
 */
public interface DishCategoryService extends IService<DishCategory> {
    
    /**
     * 获取所有分类列表
     */
    List<DishCategory> getAllCategories();
    
    /**
     * 添加分类
     */
    void addCategory(DishCategory category);
    
    /**
     * 更新分类
     */
    void updateCategory(DishCategory category);
    
    /**
     * 删除分类
     */
    void deleteCategory(Long id);
}
