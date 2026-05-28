package com.qiyun.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qiyun.order.client.DishSeataDemoClient;
import com.qiyun.order.common.Result;
import com.qiyun.order.dto.SeataDemoOrderRequest;
import com.qiyun.order.dto.SeataStockDeductRequest;
import com.qiyun.order.entity.SeataDemoOrder;
import com.qiyun.order.mapper.SeataDemoOrderMapper;
import com.qiyun.order.service.SeataDemoService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 最小化的 Seata AT 模式演示：
 * 在订单服务本地插入数据，再远程调用菜品服务扣减库存。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeataDemoServiceImpl implements SeataDemoService {

    private final SeataDemoOrderMapper seataDemoOrderMapper;
    private final DishSeataDemoClient dishSeataDemoClient;

    @Override
    @GlobalTransactional(name = "seata-demo-place-order", rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public SeataDemoOrder placeOrder(SeataDemoOrderRequest request) {
        validateRequest(request);

        SeataDemoOrder order = new SeataDemoOrder();
        order.setUserId(request.getUserId());
        order.setDishId(request.getDishId());
        order.setCount(request.getCount());
        order.setAmount(request.getAmount());
        order.setStatus("CREATED");
        order.setRemark(request.getRemark());
        seataDemoOrderMapper.insert(order);

        Result<Boolean> deductResult = dishSeataDemoClient.deduct(
            new SeataStockDeductRequest(request.getDishId(), request.getCount())
        );
        if (deductResult == null || deductResult.getCode() == null || deductResult.getCode() != 200
            || !Boolean.TRUE.equals(deductResult.getData())) {
            throw new IllegalStateException(
                deductResult == null ? "菜品服务返回为空" : deductResult.getMessage()
            );
        }

        if (Boolean.TRUE.equals(request.getFailAfterDeduct())) {
            throw new IllegalStateException("演示用异常：扣减库存后主动抛错，触发 Seata 回滚");
        }

        log.info("Seata 演示下单提交成功: orderId={}, dishId={}, count={}",
            order.getId(), request.getDishId(), request.getCount());
        return order;
    }

    @Override
    public List<SeataDemoOrder> listOrders() {
        LambdaQueryWrapper<SeataDemoOrder> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(SeataDemoOrder::getCreateTime);
        return seataDemoOrderMapper.selectList(queryWrapper);
    }

    private void validateRequest(SeataDemoOrderRequest request) {
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("userId 不能为空");
        }
        if (request.getDishId() == null) {
            throw new IllegalArgumentException("dishId 不能为空");
        }
        if (request.getCount() == null || request.getCount() <= 0) {
            throw new IllegalArgumentException("count 必须大于 0");
        }
        if (request.getAmount() == null) {
            throw new IllegalArgumentException("amount 不能为空");
        }
    }
}
