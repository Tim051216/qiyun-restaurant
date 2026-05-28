package com.qiyun.restaurant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qiyun.restaurant.entity.Coupon;
import com.qiyun.restaurant.mapper.CouponMapper;
import com.qiyun.restaurant.service.CouponService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 优惠券Service实现
 */
@Service
@Slf4j
public class CouponServiceImpl implements CouponService {
    
    @Autowired
    private CouponMapper couponMapper;
    
    @Override
    public List<Coupon> getAllCoupons() {
        LambdaQueryWrapper<Coupon> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Coupon::getCreateTime);
        return couponMapper.selectList(wrapper);
    }
    
    @Override
    public Coupon getCouponById(Long id) {
        Coupon coupon = couponMapper.selectById(id);
        if (coupon == null) {
            throw new RuntimeException("优惠券不存在");
        }
        return coupon;
    }
    
    @Override
    public void addCoupon(Coupon coupon) {
        couponMapper.insert(coupon);
    }
    
    @Override
    public void updateCoupon(Coupon coupon) {
        couponMapper.updateById(coupon);
    }
    
    @Override
    public void deleteCoupon(Long id) {
        couponMapper.deleteById(id);
    }
}
