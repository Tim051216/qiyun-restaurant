package com.qiyun.admin.dto;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TodayStatsResponse {
    private Integer todayOrders;
    private BigDecimal todaySales;
    private Integer newMembers;
    private BigDecimal monthSales;
}
