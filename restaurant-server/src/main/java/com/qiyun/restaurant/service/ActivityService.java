package com.qiyun.restaurant.service;

import com.qiyun.restaurant.entity.Activity;

import java.util.List;

/**
 * 活动Service
 */
public interface ActivityService {
    
    /**
     * 获取所有活动
     */
    List<Activity> getAllActivities();
    
    /**
     * 根据ID获取活动详情
     */
    Activity getActivityById(Long id);
    
    /**
     * 添加活动
     */
    void addActivity(Activity activity);
    
    /**
     * 更新活动
     */
    void updateActivity(Activity activity);
    
    /**
     * 删除活动
     */
    void deleteActivity(Long id);
}
