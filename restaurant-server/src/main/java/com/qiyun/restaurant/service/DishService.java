package com.qiyun.restaurant.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.qiyun.restaurant.dto.DishDTO;
import com.qiyun.restaurant.entity.Dish;
import com.qiyun.restaurant.vo.DishVO;

/**
 * 菜品Service
 */
public interface DishService extends IService<Dish> {
    
    /**
     * 分页查询菜品
     */
    Page<DishVO> getDishPage(Integer page, Integer size, String name, Long categoryId, Integer status);
    
    /**
     * 根据ID获取菜品详情
     */
    DishVO getDishById(Long id);
    
    /**
     * 添加菜品
     */
    void addDish(DishDTO dishDTO);
    
    /**
     * 更新菜品
     */
    void updateDish(DishDTO dishDTO);
    
    /**
     * 删除菜品
     */
    void deleteDish(Long id);
    
    /**
     * 批量删除菜品
     */
    void batchDeleteDish(Long[] ids);
    
    /**
     * 更新菜品状态
     */
    void updateStatus(Long id, Integer status);
}
