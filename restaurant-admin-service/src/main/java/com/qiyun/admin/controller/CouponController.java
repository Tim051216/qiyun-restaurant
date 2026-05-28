package com.qiyun.admin.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiyun.admin.common.Result;
import com.qiyun.admin.entity.Coupon;
import com.qiyun.admin.service.CouponService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 优惠券管理控制器
 * 
 * @author qiyun
 * @since 2026-02-09
 */
@Slf4j
@RestController
@RequestMapping("/coupon")
public class CouponController {
    
    @Autowired
    private CouponService couponService;
    
    @GetMapping("/list")
    public Result<List<Coupon>> listCoupons() {
        log.info("查询优惠券列表");
        List<Coupon> coupons = couponService.lambdaQuery()
                .eq(Coupon::getDeleted, 0)
                .orderByDesc(Coupon::getCreateTime)
                .list();
        return Result.success(coupons);
    }
    
    @GetMapping("/page")
    public Result<Page<Coupon>> pageCoupons(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Integer status) {
        log.info("分页查询优惠券: page={}, pageSize={}, name={}, status={}", 
                page, pageSize, name, status);
        
        Page<Coupon> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Coupon> queryWrapper = new LambdaQueryWrapper<>();
        
        if (name != null && !name.isEmpty()) {
            queryWrapper.like(Coupon::getName, name);
        }
        if (status != null) {
            queryWrapper.eq(Coupon::getStatus, status);
        }
        
        queryWrapper.eq(Coupon::getDeleted, 0);
        queryWrapper.orderByDesc(Coupon::getCreateTime);
        
        Page<Coupon> result = couponService.page(pageInfo, queryWrapper);
        return Result.success(result);
    }
    
    @GetMapping("/{id}")
    public Result<Coupon> getCouponById(@PathVariable Long id) {
        log.info("查询优惠券详情: couponId={}", id);
        Coupon coupon = couponService.getById(id);
        return Result.success(coupon);
    }
    
    @PostMapping
    public Result<Void> addCoupon(@RequestBody Coupon coupon) {
        log.info("添加优惠券: {}", coupon);
        couponService.save(coupon);
        return Result.success(null);
    }
    
    @PutMapping
    public Result<Void> updateCoupon(@RequestBody Coupon coupon) {
        log.info("更新优惠券: {}", coupon);
        couponService.updateById(coupon);
        return Result.success(null);
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> deleteCoupon(@PathVariable Long id) {
        log.info("删除优惠券: couponId={}", id);
        couponService.removeById(id);
        return Result.success(null);
    }
}
