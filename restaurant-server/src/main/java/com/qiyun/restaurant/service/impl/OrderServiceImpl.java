package com.qiyun.restaurant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.qiyun.restaurant.entity.Member;
import com.qiyun.restaurant.entity.Order;
import com.qiyun.restaurant.entity.OrderDetail;
import com.qiyun.restaurant.mapper.MemberMapper;
import com.qiyun.restaurant.mapper.OrderDetailMapper;
import com.qiyun.restaurant.mapper.OrderMapper;
import com.qiyun.restaurant.service.OrderService;
import com.qiyun.restaurant.vo.OrderDetailVO;
import com.qiyun.restaurant.vo.OrderVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单Service实现
 */
@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    
    @Autowired
    private OrderMapper orderMapper;
    
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    
    @Autowired
    private MemberMapper memberMapper;
    
    @Override
    public Page<OrderVO> getOrderPage(Integer page, Integer size, String orderNo, String tableNo, String status, String startDate, String endDate) {
        Page<Order> orderPage = new Page<>(page, size);
        
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(orderNo), Order::getOrderNo, orderNo)
               .like(StringUtils.hasText(tableNo), Order::getTableNo, tableNo)
               .eq(StringUtils.hasText(status), Order::getStatus, status);
        
        // 日期范围查询
        if (StringUtils.hasText(startDate)) {
            LocalDateTime startDateTime = LocalDate.parse(startDate).atStartOfDay();
            wrapper.ge(Order::getCreateTime, startDateTime);
        }
        if (StringUtils.hasText(endDate)) {
            LocalDateTime endDateTime = LocalDate.parse(endDate).atTime(LocalTime.MAX);
            wrapper.le(Order::getCreateTime, endDateTime);
        }
        
        wrapper.orderByDesc(Order::getCreateTime);
        
        orderMapper.selectPage(orderPage, wrapper);
        
        // 转换为VO
        Page<OrderVO> voPage = new Page<>(page, size, orderPage.getTotal());
        List<OrderVO> voList = orderPage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList());
        voPage.setRecords(voList);
        
        return voPage;
    }
    
    @Override
    public OrderVO getOrderById(Long id) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        return convertToVO(order);
    }
    
    @Override
    public void updateOrderStatus(Long id, String status) {
        Order order = orderMapper.selectById(id);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }
        order.setStatus(status);
        orderMapper.updateById(order);
    }
    
    @Override
    public Page<OrderVO> getRealtimeOrders(Integer page, Integer size) {
        Page<Order> orderPage = new Page<>(page, size);
        
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Order::getStatus, "pending", "cooking")
               .orderByDesc(Order::getCreateTime);
        
        orderMapper.selectPage(orderPage, wrapper);
        
        // 转换为VO
        Page<OrderVO> voPage = new Page<>(page, size, orderPage.getTotal());
        List<OrderVO> voList = orderPage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList());
        voPage.setRecords(voList);
        
        return voPage;
    }
    
    /**
     * 转换为VO
     */
    private OrderVO convertToVO(Order order) {
        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);
        
        // 查询会员信息
        if (order.getMemberId() != null) {
            Member member = memberMapper.selectById(order.getMemberId());
            if (member != null) {
                vo.setMemberNickname(member.getNickname());
            }
        }
        
        // 查询订单明细
        LambdaQueryWrapper<OrderDetail> detailWrapper = new LambdaQueryWrapper<>();
        detailWrapper.eq(OrderDetail::getOrderId, order.getId());
        List<OrderDetail> details = orderDetailMapper.selectList(detailWrapper);
        
        List<OrderDetailVO> detailVOList = details.stream().map(detail -> {
            OrderDetailVO detailVO = new OrderDetailVO();
            BeanUtils.copyProperties(detail, detailVO);
            return detailVO;
        }).collect(Collectors.toList());
        
        vo.setDetails(detailVOList);
        
        return vo;
    }
}
