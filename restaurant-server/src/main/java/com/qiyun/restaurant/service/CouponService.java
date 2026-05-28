package com.qiyun.restaurant.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiyun.restaurant.entity.Coupon;

import java.util.List;

/**
 * 优惠券Service
 */
public interface CouponService {
    
    /**
     * 获取所有优惠券
     */
    List<Coupon> getAllCoupons();
    
    /**
     * 根据ID获取优惠券详情
     */
    Coupon getCouponById(Long id);
    
    /**
     * 添加优惠券
     */
    void addCoupon(Coupon coupon);
    
    /**
     * 更新优惠券
     */
    void updateCoupon(Coupon coupon);
    
    /**
     * 删除优惠券
     */
    void deleteCoupon(Long id);
}
