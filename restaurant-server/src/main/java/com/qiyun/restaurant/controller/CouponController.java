package com.qiyun.restaurant.controller;

import com.qiyun.restaurant.common.Result;
import com.qiyun.restaurant.entity.Coupon;
import com.qiyun.restaurant.service.CouponService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 优惠券Controller
 */
@RestController
@RequestMapping("/coupon")
@Api(tags = "优惠券接�?)
@Slf4j
public class CouponController {
    
    @Autowired
    private CouponService couponService;
    
    /**
     * 获取所有优惠券
     */
    @GetMapping("/list")
    @Operation(summary = "获取所有优惠券")
    public Result<List<Coupon>> list() {
        List<Coupon> coupons = couponService.getAllCoupons();
        return Result.success(coupons);
    }
    
    /**
     * 根据ID获取优惠券详�?
     */
    @GetMapping("/{id}")
    @ApiOperation("获取优惠券详�?)
    public Result<Coupon> getById(@PathVariable Long id) {
        Coupon coupon = couponService.getCouponById(id);
        return Result.success(coupon);
    }
    
    /**
     * 添加优惠�?
     */
    @PostMapping
    @ApiOperation("添加优惠�?)
    public Result<String> add(@RequestBody Coupon coupon) {
        couponService.addCoupon(coupon);
        return Result.success("添加成功");
    }
    
    /**
     * 更新优惠�?
     */
    @PutMapping
    @ApiOperation("更新优惠�?)
    public Result<String> update(@RequestBody Coupon coupon) {
        couponService.updateCoupon(coupon);
        return Result.success("更新成功");
    }
    
    /**
     * 删除优惠�?
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除优惠�?)
    public Result<String> delete(@PathVariable Long id) {
        couponService.deleteCoupon(id);
        return Result.success("删除成功");
    }
}
