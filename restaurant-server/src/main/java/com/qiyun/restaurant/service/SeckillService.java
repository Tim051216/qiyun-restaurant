package com.qiyun.restaurant.service;

/**
 * 秒杀Service接口
 */
public interface SeckillService {
    
    /**
     * 秒杀下单（解决超卖问题）
     * 
     * @param activityId 活动ID
     * @param memberId 会员ID
     * @return 是否成功
     */
    boolean seckillOrder(Long activityId, Long memberId);
    
    /**
     * 初始化秒杀库存到Redis
     * 
     * @param activityId 活动ID
     * @param stock 库存数量
     */
    void initSeckillStock(Long activityId, Integer stock);
    
    /**
     * 获取秒杀库存
     * 
     * @param activityId 活动ID
     * @return 剩余库存
     */
    Integer getSeckillStock(Long activityId);
}
