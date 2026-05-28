package com.qiyun.admin.controller;

import com.qiyun.admin.common.Result;
import com.qiyun.admin.dto.TodayStatsResponse;
import com.qiyun.admin.entity.Order;
import com.qiyun.admin.mapper.MemberMapper;
import com.qiyun.admin.mapper.OrderMapper;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private static final DateTimeFormatter LABEL_FORMATTER = DateTimeFormatter.ofPattern("M/d");

    private final OrderMapper orderMapper;
    private final MemberMapper memberMapper;

    @GetMapping("/dashboard")
    public Result<TodayStatsResponse> getDashboardStats() {
        log.info("Loading dashboard statistics");

        Integer todayOrders = orderMapper.countTodayOrders();
        BigDecimal todaySales = orderMapper.sumTodaySales();
        Integer newMembers = memberMapper.countTodayMembers();

        YearMonth currentMonth = YearMonth.now();
        LocalDateTime monthStart = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime monthEnd = currentMonth.atEndOfMonth().atTime(LocalTime.MAX);
        BigDecimal monthSales = sumOrdersBetween(monthStart, monthEnd);

        TodayStatsResponse stats = new TodayStatsResponse(
                defaultInt(todayOrders),
                defaultDecimal(todaySales),
                defaultInt(newMembers),
                defaultDecimal(monthSales)
        );

        return Result.success(stats);
    }

    @GetMapping("/sales/trend")
    public Result<Map<String, Object>> getSalesTrend(@RequestParam(defaultValue = "7") Integer days) {
        int safeDays = (days == null || days <= 0) ? 7 : days;
        log.info("Loading sales trend, days={}", safeDays);

        List<String> dates = new ArrayList<>(safeDays);
        List<BigDecimal> sales = new ArrayList<>(safeDays);
        List<Integer> orders = new ArrayList<>(safeDays);

        for (int offset = safeDays - 1; offset >= 0; offset--) {
            LocalDate date = LocalDate.now().minusDays(offset);
            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.atTime(LocalTime.MAX);

            dates.add(date.format(LABEL_FORMATTER));
            orders.add(defaultInt(orderMapper.countOrdersBetween(dayStart, dayEnd)));
            sales.add(defaultDecimal(sumOrdersBetween(dayStart, dayEnd)));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("dates", dates);
        result.put("sales", sales);
        result.put("orders", orders);
        return Result.success(result);
    }

    private BigDecimal sumOrdersBetween(LocalDateTime start, LocalDateTime end) {
        List<Order> orders = orderMapper.selectOrdersBetween(start, end);
        return orders.stream()
                .filter(order -> order.getAmount() != null)
                .filter(order -> !"cancelled".equalsIgnoreCase(order.getStatus()))
                .map(Order::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Integer defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    private BigDecimal defaultDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
