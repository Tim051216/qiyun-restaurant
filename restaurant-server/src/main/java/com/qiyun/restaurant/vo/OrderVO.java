package com.qiyun.restaurant.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单VO
 */
@Data
public class OrderVO {
    
    private Long id;
    
    private String orderNo;
    
    private Long memberId;
    
    private String memberNickname;
    
    private Long tableId;
    
    private String tableNo;
    
    private Integer dinerCount;
    
    private BigDecimal amount;
    
    private String status;
    
    private String remark;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    
    private List<OrderDetailVO> details;
}
