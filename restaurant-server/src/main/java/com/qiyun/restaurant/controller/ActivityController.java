package com.qiyun.restaurant.controller;

import com.qiyun.restaurant.common.Result;
import com.qiyun.restaurant.entity.Activity;
import com.qiyun.restaurant.service.ActivityService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 活动Controller
 */
@RestController
@RequestMapping("/activity")
@Tag(name = "活动接口")
@Slf4j
public class ActivityController {
    
    @Autowired
    private ActivityService activityService;
    
    /**
     * 获取所有活�?
     */
    @GetMapping("/list")
    @ApiOperation("获取所有活�?)
    public Result<List<Activity>> list() {
        List<Activity> activities = activityService.getAllActivities();
        return Result.success(activities);
    }
    
    /**
     * 根据ID获取活动详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取活动详情")
    public Result<Activity> getById(@PathVariable Long id) {
        Activity activity = activityService.getActivityById(id);
        return Result.success(activity);
    }
    
    /**
     * 添加活动
     */
    @PostMapping
    @Operation(summary = "添加活动")
    public Result<String> add(@RequestBody Activity activity) {
        activityService.addActivity(activity);
        return Result.success("添加成功");
    }
    
    /**
     * 更新活动
     */
    @PutMapping
    @Operation(summary = "更新活动")
    public Result<String> update(@RequestBody Activity activity) {
        activityService.updateActivity(activity);
        return Result.success("更新成功");
    }
    
    /**
     * 删除活动
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除活动")
    public Result<String> delete(@PathVariable Long id) {
        activityService.deleteActivity(id);
        return Result.success("删除成功");
    }
}
