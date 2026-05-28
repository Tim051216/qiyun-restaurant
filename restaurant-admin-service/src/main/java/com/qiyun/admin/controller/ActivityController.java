package com.qiyun.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiyun.admin.common.Result;
import com.qiyun.admin.entity.Activity;
import com.qiyun.admin.service.ActivityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 活动管理控制器
 * 
 * @author qiyun
 * @since 2026-02-09
 */
@Slf4j
@RestController
@RequestMapping("/activity")
public class ActivityController {
    
    @Autowired
    private ActivityService activityService;
    
    @GetMapping("/list")
    public Result<List<Activity>> listActivities() {
        log.info("查询活动列表");
        List<Activity> activities = activityService.lambdaQuery()
                .eq(Activity::getDeleted, 0)
                .orderByDesc(Activity::getCreateTime)
                .list();
        return Result.success(activities);
    }
    
    @GetMapping("/page")
    public Result<Page<Activity>> pageActivities(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String status) {
        log.info("分页查询活动: page={}, pageSize={}, title={}, status={}", 
                page, pageSize, title, status);
        
        Page<Activity> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Activity> queryWrapper = new LambdaQueryWrapper<>();
        
        if (title != null && !title.isEmpty()) {
            queryWrapper.like(Activity::getTitle, title);
        }
        if (status != null && !status.isEmpty()) {
            queryWrapper.eq(Activity::getStatus, status);
        }
        
        queryWrapper.eq(Activity::getDeleted, 0);
        queryWrapper.orderByDesc(Activity::getCreateTime);
        
        Page<Activity> result = activityService.page(pageInfo, queryWrapper);
        return Result.success(result);
    }
    
    @GetMapping("/{id}")
    public Result<Activity> getActivityById(@PathVariable Long id) {
        log.info("查询活动详情: activityId={}", id);
        Activity activity = activityService.getById(id);
        return Result.success(activity);
    }
    
    @PostMapping
    public Result<Void> addActivity(@RequestBody Activity activity) {
        log.info("添加活动: {}", activity);
        activityService.save(activity);
        return Result.success(null);
    }
    
    @PutMapping
    public Result<Void> updateActivity(@RequestBody Activity activity) {
        log.info("更新活动: {}", activity);
        activityService.updateById(activity);
        return Result.success(null);
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> deleteActivity(@PathVariable Long id) {
        log.info("删除活动: activityId={}", id);
        activityService.removeById(id);
        return Result.success(null);
    }
}
