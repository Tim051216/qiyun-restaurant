package com.qiyun.order.client;

import com.qiyun.order.common.Result;
import com.qiyun.order.dto.SeataStockDeductRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Seata 演示使用的菜品服务 Feign 客户端。
 */
@FeignClient(
    contextId = "dishSeataDemoClient",
    name = "restaurant-dish-service",
    path = "/dish/seata/demo",
    fallback = DishSeataDemoClientFallback.class
)
public interface DishSeataDemoClient {

    @PostMapping("/deduct")
    Result<Boolean> deduct(@RequestBody SeataStockDeductRequest request);
}
