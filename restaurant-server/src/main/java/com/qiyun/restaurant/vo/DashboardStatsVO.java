package com.qiyun.restaurant.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 仪表盘统计数据VO
 */
@Data
public class DashboardStatsVO {
    
    private Integer todayOrders;
    
    private BigDecimal todaySales;
    
    private Integer newMembers;
    
    private BigDecimal monthSales;
}
