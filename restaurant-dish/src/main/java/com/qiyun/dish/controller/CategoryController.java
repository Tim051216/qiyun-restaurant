package com.qiyun.dish.controller;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiyun.dish.common.Result;
import com.qiyun.dish.entity.DishCategory;
import com.qiyun.dish.mapper.DishCategoryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 菜品分类控制器
 * 
 * @author qiyun
 * @since 2026-02-09
 */
@Slf4j
@RestController
@RequestMapping("/dish/category")
public class CategoryController {
    
    @Autowired
    private CategoryService categoryService;
    
    @GetMapping("/list")
    public Result<List<DishCategory>> listCategories() {
        log.info("查询菜品分类列表");
        List<DishCategory> categories = categoryService.lambdaQuery()
                .eq(DishCategory::getDeleted, 0)
                .eq(DishCategory::getStatus, 1)
                .orderByAsc(DishCategory::getSort)
                .list();
        return Result.success(categories);
    }
    
    @GetMapping("/{id}")
    public Result<DishCategory> getCategoryById(@PathVariable Long id) {
        log.info("查询分类详情: categoryId={}", id);
        DishCategory category = categoryService.getById(id);
        return Result.success(category);
    }
    
    @PostMapping
    public Result<Void> addCategory(@RequestBody DishCategory category) {
        log.info("添加分类: {}", category);
        categoryService.save(category);
        return Result.success(null);
    }
    
    @PutMapping
    public Result<Void> updateCategory(@RequestBody DishCategory category) {
        log.info("更新分类: {}", category);
        categoryService.updateById(category);
        return Result.success(null);
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        log.info("删除分类: categoryId={}", id);
        categoryService.removeById(id);
        return Result.success(null);
    }
}

/**
 * 分类服务
 */
@Service
class CategoryService extends ServiceImpl<DishCategoryMapper, DishCategory> {
}
