package com.qiyun.restaurant.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiyun.restaurant.common.Result;
import com.qiyun.restaurant.dto.DishDTO;
import com.qiyun.restaurant.entity.Dish;
import com.qiyun.restaurant.entity.DishCategory;
import com.qiyun.restaurant.service.DishService;
import com.qiyun.restaurant.service.DishCategoryService;
import com.qiyun.restaurant.vo.DishVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 菜品Controller
 */
@RestController
@RequestMapping("/dish")
@Tag(name = "菜品接口")
@Slf4j
public class DishController {
    
    @Autowired
    private DishService dishService;
    
    @Autowired
    private DishCategoryService dishCategoryService;
    
    /**
     * 分页查询菜品
     */
    @GetMapping("/page")
    @Operation(summary = "分页查询菜品")
    public Result<Page<DishVO>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status
    ) {
        Page<DishVO> dishPage = dishService.getDishPage(page, size, name, categoryId, status);
        return Result.success(dishPage);
    }
    
    /**
     * 获取所有菜品（按分类分组）- 用于小程序菜单页�?
     * 注意：此路由必须�?/{id} 之前，否�?grouped"会被当作id参数
     */
    @GetMapping("/grouped")
    @Operation(summary = "获取所有菜品按分类分组")
    public Result<Map<String, Object>> getGroupedDishes() {
        // 获取所有分�?
        List<DishCategory> categories = dishCategoryService.list(
            new LambdaQueryWrapper<DishCategory>()
                .eq(DishCategory::getStatus, 1)
        );
        
        // 获取所有启用的菜品
        List<Dish> allDishes = dishService.list(
            new LambdaQueryWrapper<Dish>()
                .eq(Dish::getStatus, 1)
        );
        
        // 按分类分�?
        Map<Long, List<Dish>> dishMap = allDishes.stream()
            .collect(java.util.stream.Collectors.groupingBy(Dish::getCategoryId));
        
        // 构建返回数据
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        for (DishCategory category : categories) {
            Map<String, Object> categoryData = new java.util.HashMap<>();
            categoryData.put("id", category.getId());
            categoryData.put("name", category.getName());
            categoryData.put("image", null); // 分类暂无图片字段
            categoryData.put("foods", dishMap.getOrDefault(category.getId(), new java.util.ArrayList<>()));
            result.add(categoryData);
        }
        
        Map<String, Object> response = new java.util.HashMap<>();
        response.put("categories", result);
        return Result.success(response);
    }
    
    /**
     * 根据ID获取菜品详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取菜品详情")
    public Result<DishVO> getById(@PathVariable Long id) {
        DishVO dish = dishService.getDishById(id);
        return Result.success(dish);
    }
    
    /**
     * 添加菜品
     */
    @PostMapping
    @Operation(summary = "添加菜品")
    public Result<String> add(@Valid @RequestBody DishDTO dishDTO) {
        dishService.addDish(dishDTO);
        return Result.success("添加成功");
    }
    
    /**
     * 更新菜品
     */
    @PutMapping
    @Operation(summary = "更新菜品")
    public Result<String> update(@Valid @RequestBody DishDTO dishDTO) {
        dishService.updateDish(dishDTO);
        return Result.success("更新成功");
    }
    
    /**
     * 删除菜品
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除菜品")
    public Result<String> delete(@PathVariable Long id) {
        dishService.deleteDish(id);
        return Result.success("删除成功");
    }
    
    /**
     * 批量删除菜品
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量删除菜品")
    public Result<String> batchDelete(@RequestBody Long[] ids) {
        dishService.batchDeleteDish(ids);
        return Result.success("删除成功");
    }
    
    /**
     * 更新菜品状�?
     */
    @PutMapping("/status/{id}")
    @ApiOperation("更新菜品状�?)
    public Result<String> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        dishService.updateStatus(id, status);
        return Result.success("更新成功");
    }
}
