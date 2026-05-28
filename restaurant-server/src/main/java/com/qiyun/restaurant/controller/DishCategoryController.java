package com.qiyun.restaurant.controller;

import com.qiyun.restaurant.common.Result;
import com.qiyun.restaurant.entity.DishCategory;
import com.qiyun.restaurant.service.DishCategoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜品分类Controller
 */
@RestController
@RequestMapping("/dish/category")
@Tag(name = "菜品分类接口")
@Slf4j
public class DishCategoryController {
    
    @Autowired
    private DishCategoryService dishCategoryService;
    
    /**
     * 获取所有分�?
     */
    @GetMapping("/list")
    @ApiOperation("获取所有分�?)
    public Result<List<DishCategory>> list() {
        List<DishCategory> list = dishCategoryService.getAllCategories();
        return Result.success(list);
    }
    
    /**
     * 添加分类
     */
    @PostMapping
    @Operation(summary = "添加分类")
    public Result<String> add(@RequestBody DishCategory category) {
        dishCategoryService.addCategory(category);
        return Result.success("添加成功");
    }
    
    /**
     * 更新分类
     */
    @PutMapping
    @Operation(summary = "更新分类")
    public Result<String> update(@RequestBody DishCategory category) {
        dishCategoryService.updateCategory(category);
        return Result.success("更新成功");
    }
    
    /**
     * 删除分类
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除分类")
    public Result<String> delete(@PathVariable Long id) {
        try {
            dishCategoryService.deleteCategory(id);
            return Result.success("删除成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
}
