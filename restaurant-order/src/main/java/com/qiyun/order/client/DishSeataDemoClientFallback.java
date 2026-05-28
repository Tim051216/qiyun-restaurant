package com.qiyun.order.client;

import com.qiyun.order.common.Result;
import com.qiyun.order.dto.SeataStockDeductRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Seata 演示中库存扣减失败时的降级处理。
 */
@Slf4j
@Component
public class DishSeataDemoClientFallback implements DishSeataDemoClient {

    @Override
    public Result<Boolean> deduct(SeataStockDeductRequest request) {
        log.warn("Seata 演示库存扣减触发降级: dishId={}", request.getDishId());
        return Result.error("Seata 演示期间菜品服务不可用");
    }
}
