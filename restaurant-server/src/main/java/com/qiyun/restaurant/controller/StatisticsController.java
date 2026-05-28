package com.qiyun.restaurant.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qiyun.restaurant.common.Result;
import com.qiyun.restaurant.entity.Dish;
import com.qiyun.restaurant.entity.Member;
import com.qiyun.restaurant.entity.Order;
import com.qiyun.restaurant.mapper.DishMapper;
import com.qiyun.restaurant.mapper.MemberMapper;
import com.qiyun.restaurant.mapper.OrderMapper;
import com.qiyun.restaurant.vo.DashboardStatsVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计Controller
 */
@RestController
@RequestMapping("/statistics")
@Tag(name = "统计接口")
@Slf4j
public class StatisticsController {
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private MemberMapper memberMapper;
    
    @Autowired
    private DishMapper dishMapper;
    
    /**
     * 获取仪表盘统计数�?
     */
    @GetMapping("/dashboard")
    @ApiOperation("获取仪表盘统计数�?)
    public Result<DashboardStatsVO> getDashboardStats() {
        DashboardStatsVO stats = new DashboardStatsVO();
        
        // 今日订单�?
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        LambdaQueryWrapper<Order> todayOrderWrapper = new LambdaQueryWrapper<>();
        todayOrderWrapper.between(Order::getCreateTime, todayStart, todayEnd);
        Long todayOrderCount = orderMapper.selectCount(todayOrderWrapper);
        stats.setTodayOrders(todayOrderCount.intValue());
        
        // 今日销售额
        List<Order> todayOrders = orderMapper.selectList(todayOrderWrapper);
        BigDecimal todaySales = todayOrders.stream()
                .map(Order::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setTodaySales(todaySales);
        
        // 今日新增会员
        LambdaQueryWrapper<Member> todayMemberWrapper = new LambdaQueryWrapper<>();
        todayMemberWrapper.between(Member::getCreateTime, todayStart, todayEnd);
        Long newMemberCount = memberMapper.selectCount(todayMemberWrapper);
        stats.setNewMembers(newMemberCount.intValue());
        
        // 本月销售额
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime monthStart = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime monthEnd = currentMonth.atEndOfMonth().atTime(LocalTime.MAX);
        LambdaQueryWrapper<Order> monthOrderWrapper = new LambdaQueryWrapper<>();
        monthOrderWrapper.between(Order::getCreateTime, monthStart, monthEnd);
        List<Order> monthOrders = orderMapper.selectList(monthOrderWrapper);
        BigDecimal monthSales = monthOrders.stream()
                .map(Order::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        stats.setMonthSales(monthSales);
        
        return Result.success(stats);
    }
    
    /**
     * 获取菜品销量排�?
     */
    @GetMapping("/dish/top")
    @ApiOperation("获取菜品销量排�?)
    public Result<List<Dish>> getDishTop(@RequestParam(defaultValue = "5") Integer limit) {
        LambdaQueryWrapper<Dish> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(Dish::getSales).last("LIMIT " + limit);
        List<Dish> topDishes = dishMapper.selectList(wrapper);
        return Result.success(topDishes);
    }
    
    /**
     * 获取销售趋势数�?
     */
    @GetMapping("/sales/trend")
    @ApiOperation("获取销售趋势数�?)
    public Result<Map<String, Object>> getSalesTrend(@RequestParam(defaultValue = "7") Integer days) {
        Map<String, Object> result = new HashMap<>();
        
        // 这里简化处理，实际应该按日期分组统�?
        // 返回模拟数据结构
        result.put("dates", new String[]{"周一", "周二", "周三", "周四", "周五", "周六", "周日"});
        result.put("sales", new Integer[]{8200, 9500, 11200, 10800, 13500, 15600, 14200});
        result.put("orders", new Integer[]{85, 98, 115, 108, 135, 156, 142});
        
        return Result.success(result);
    }
}
