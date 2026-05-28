package com.qiyun.restaurant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qiyun.restaurant.entity.Activity;
import com.qiyun.restaurant.mapper.ActivityMapper;
import com.qiyun.restaurant.service.ActivityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 活动Service实现
 */
@Service
@Slf4j
public class ActivityServiceImpl implements ActivityService {
    
    @Autowired
    private ActivityMapper activityMapper;
    
    @Override
    public List<Activity> getAllActivities() {
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Activity::getCreateTime);
        return activityMapper.selectList(wrapper);
    }
    
    @Override
    public Activity getActivityById(Long id) {
        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new RuntimeException("活动不存在");
        }
        return activity;
    }
    
    @Override
    public void addActivity(Activity activity) {
        activityMapper.insert(activity);
    }
    
    @Override
    public void updateActivity(Activity activity) {
        activityMapper.updateById(activity);
    }
    
    @Override
    public void deleteActivity(Long id) {
        activityMapper.deleteById(id);
    }
}
