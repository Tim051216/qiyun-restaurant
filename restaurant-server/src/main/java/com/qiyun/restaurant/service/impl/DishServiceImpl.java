package com.qiyun.restaurant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiyun.restaurant.dto.DishDTO;
import com.qiyun.restaurant.entity.Dish;
import com.qiyun.restaurant.entity.DishCategory;
import com.qiyun.restaurant.mapper.DishCategoryMapper;
import com.qiyun.restaurant.mapper.DishMapper;
import com.qiyun.restaurant.service.DishService;
import com.qiyun.restaurant.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜品Service实现
 */
@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    
    @Autowired
    private DishCategoryMapper dishCategoryMapper;
    
    @Override
    public Page<DishVO> getDishPage(Integer page, Integer size, String name, Long categoryId, Integer status) {
        Page<Dish> dishPage = new Page<>(page, size);
        
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(name != null && !name.isEmpty(), Dish::getName, name);
        queryWrapper.eq(categoryId != null, Dish::getCategoryId, categoryId);
        queryWrapper.eq(status != null, Dish::getStatus, status);
        queryWrapper.orderByDesc(Dish::getCreateTime);
        
        page(dishPage, queryWrapper);
        
        // 转换为VO
        Page<DishVO> voPage = new Page<>(page, size, dishPage.getTotal());
        List<DishVO> voList = dishPage.getRecords().stream().map(dish -> {
            DishVO vo = new DishVO();
            BeanUtils.copyProperties(dish, vo);
            
            // 查询分类名称
            DishCategory category = dishCategoryMapper.selectById(dish.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
            
            return vo;
        }).collect(Collectors.toList());
        
        voPage.setRecords(voList);
        return voPage;
    }
    
    @Override
    public DishVO getDishById(Long id) {
        Dish dish = getById(id);
        if (dish == null) {
            throw new RuntimeException("菜品不存在");
        }
        
        DishVO vo = new DishVO();
        BeanUtils.copyProperties(dish, vo);
        
        // 查询分类名称
        DishCategory category = dishCategoryMapper.selectById(dish.getCategoryId());
        if (category != null) {
            vo.setCategoryName(category.getName());
        }
        
        return vo;
    }
    
    @Override
    public void addDish(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dish.setSales(0);
        dish.setStatus(1);
        save(dish);
    }
    
    @Override
    public void updateDish(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        updateById(dish);
    }
    
    @Override
    public void deleteDish(Long id) {
        removeById(id);
    }
    
    @Override
    public void batchDeleteDish(Long[] ids) {
        removeByIds(Arrays.asList(ids));
    }
    
    @Override
    public void updateStatus(Long id, Integer status) {
        Dish dish = new Dish();
        dish.setId(id);
        dish.setStatus(status);
        updateById(dish);
    }
}
