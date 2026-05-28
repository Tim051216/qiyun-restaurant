package com.qiyun.order.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RealtimeOrderResponse {
    private Long id;
    private String orderNo;
    private String tableNo;
    private BigDecimal amount;
    private String status;  // 改为String类型
    private LocalDateTime createTime;
    private List<OrderDetailDTO> details;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderDetailDTO {
        private String dishName;
        private Integer quantity;
        private BigDecimal price;
    }
}
