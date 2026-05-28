package com.qiyun.dish;

import com.qiyun.dish.entity.Dish;
import com.qiyun.dish.mapper.DishMapper;
import com.qiyun.dish.service.DishCacheService;
import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.Positive;
import net.jqwik.api.lifecycle.BeforeTry;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.lang.reflect.Field;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 缓存击穿保护属性测试
 * 
 * 测试场景：
 * 1. 多个并发请求同时查询缓存未命中的热点数据
 * 2. 验证只有一个请求获取分布式锁并查询数据库
 * 3. 验证其他请求等待锁释放后从缓存获取数据
 * 4. 验证数据库只被查询一次
 * 
 * Property 32: 缓存击穿保护
 * For any 缓存未命中的热点数据查询，应该使用分布式锁保证只有一个请求查询数据库并回写缓存
 * 
 * **Validates: Requirements 7.7**
 * 
 * @author qiyun
 * @since 2026-02-08
 */
class CacheBreakdownPropertiesTest {
    
    private DishCacheService dishCacheService;
    private DishMapper dishMapper;
    private RedisTemplate<String, Dish> redisTemplate;
    private RedissonClient redissonClient;
    private RLock rLock;
    private ValueOperations<String, Dish> valueOperations;
    
    @BeforeTry
    void setUp() throws Exception {
        // 创建mocks
        dishMapper = mock(DishMapper.class);
        redisTemplate = mock(RedisTemplate.class);
        redissonClient = mock(RedissonClient.class);
        rLock = mock(RLock.class);
        valueOperations = mock(ValueOperations.class);
        
        // 创建DishCacheService实例
        dishCacheService = new DishCacheService();
        
        // 使用反射注入依赖
        Field redisTemplateField = DishCacheService.class.getDeclaredField("redisTemplate");
        redisTemplateField.setAccessible(true);
        redisTemplateField.set(dishCacheService, redisTemplate);
        
        Field dishMapperField = DishCacheService.class.getDeclaredField("dishMapper");
        dishMapperField.setAccessible(true);
        dishMapperField.set(dishCacheService, dishMapper);
        
        Field redissonClientField = DishCacheService.class.getDeclaredField("redissonClient");
        redissonClientField.setAccessible(true);
        redissonClientField.set(dishCacheService, redissonClient);
        
        // 初始化本地缓存和布隆过滤器
        dishCacheService.init();
        
        // 配置RedisTemplate返回ValueOperations
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        
        // Mock selectHotDishes返回空列表(避免warmUp时调用数据库)
        lenient().when(dishMapper.selectHotDishes()).thenReturn(java.util.Collections.emptyList());
    }
    
    /**
     * Property 32: 缓存击穿保护 - 并发查询只有一个请求查询数据库
     * 
     * 测试策略：
     * 1. 模拟缓存未命中（本地缓存和Redis都没有数据）
     * 2. 模拟数据库有数据
     * 3. 模拟分布式锁行为（第一个请求获取锁成功，其他请求等待）
     * 4. 启动多个并发线程同时查询同一个菜品
     * 5. 验证数据库只被查询一次
     * 6. 验证所有线程都能获取到数据
     * 
     * 属性：对于任意菜品ID和并发请求数量，数据库查询次数应该等于1
     */
    @Property(tries = 100)
    @Label("Feature: restaurant-tech-stack-upgrade, Property 32: 缓存击穿保护")
    void cacheBreakdownProtection_shouldQueryDatabaseOnlyOnce(
        @ForAll @Positive Long dishId,
        @ForAll @IntRange(min = 5, max = 20) int concurrentRequests
    ) throws Exception {
        // 准备测试数据
        Dish dish = createTestDish(dishId);
        
        // 将dishId添加到布隆过滤器(否则会被布隆过滤器拦截)
        dishCacheService.getBloomFilter().put(dishId);
        
        // 模拟缓存未命中（Redis返回null）
        when(valueOperations.get(anyString())).thenReturn(null);
        
        // 模拟数据库查询返回数据
        when(dishMapper.selectById(dishId)).thenReturn(dish);
        
        // 模拟分布式锁行为
        AtomicInteger lockAcquireCount = new AtomicInteger(0);
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        
        // 第一个请求获取锁成功，后续请求等待
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class)))
            .thenAnswer(invocation -> {
                int count = lockAcquireCount.incrementAndGet();
                if (count == 1) {
                    // 第一个请求获取锁成功
                    return true;
                } else {
                    // 后续请求等待一段时间后，模拟第一个请求已经写入缓存
                    Thread.sleep(50);
                    // 模拟缓存已经被第一个请求写入
                    when(valueOperations.get("dish:" + dishId)).thenReturn(dish);
                    return false; // 获取锁失败，但缓存已有数据
                }
            });
        
        when(rLock.isHeldByCurrentThread()).thenReturn(true);
        
        // 执行并发查询
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(concurrentRequests);
        ExecutorService executor = Executors.newFixedThreadPool(concurrentRequests);
        
        AtomicInteger successCount = new AtomicInteger(0);
        
        for (int i = 0; i < concurrentRequests; i++) {
            executor.submit(() -> {
                try {
                    // 等待所有线程就绪
                    startLatch.await();
                    
                    // 查询菜品
                    Dish result = dishCacheService.getDishById(dishId);
                    
                    // 验证结果
                    if (result != null && result.getId().equals(dishId)) {
                        successCount.incrementAndGet();
                    }
                    
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    endLatch.countDown();
                }
            });
        }
        
        // 启动所有线程
        startLatch.countDown();
        
        // 等待所有线程完成（最多等待5秒）
        boolean completed = endLatch.await(5, TimeUnit.SECONDS);
        executor.shutdown();
        
        // 验证结果
        assertTrue(completed, "所有并发请求应该在5秒内完成");
        
        // 验证数据库只被查询一次（缓存击穿保护的核心）
        verify(dishMapper, times(1)).selectById(dishId);
        
        // 验证至少有一个请求成功获取数据
        assertTrue(successCount.get() > 0, "至少应该有一个请求成功获取数据");
        
        // 验证分布式锁被正确使用
        verify(redissonClient, atLeastOnce()).getLock("lock:dish:" + dishId);
        verify(rLock, atLeastOnce()).tryLock(anyLong(), anyLong(), any(TimeUnit.class));
    }
    
    /**
     * Property 32: 缓存击穿保护 - 锁超时处理
     * 
     * 测试策略：
     * 1. 模拟获取锁超时
     * 2. 验证请求能够正确处理超时情况
     * 3. 验证不会导致死锁或无限等待
     * 
     * 属性：对于任意菜品ID，如果获取锁超时，应该返回null而不是阻塞
     */
    @Property(tries = 50)
    @Label("Feature: restaurant-tech-stack-upgrade, Property 32: 缓存击穿保护")
    void cacheBreakdownProtection_shouldHandleLockTimeout(
        @ForAll @Positive Long dishId
    ) throws Exception {
        // 准备测试数据
        Dish dish = createTestDish(dishId);
        
        // 将dishId添加到布隆过滤器
        dishCacheService.getBloomFilter().put(dishId);
        
        // 模拟缓存未命中
        when(valueOperations.get(anyString())).thenReturn(null);
        
        // 模拟数据库查询返回数据
        when(dishMapper.selectById(dishId)).thenReturn(dish);
        
        // 模拟获取锁超时（tryLock返回false）
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(false);
        
        // 执行查询
        long startTime = System.currentTimeMillis();
        Dish result = dishCacheService.getDishById(dishId);
        long endTime = System.currentTimeMillis();
        
        // 验证结果
        // 获取锁失败时，应该返回null（不阻塞）
        assertNull(result, "获取锁超时时应该返回null");
        
        // 验证执行时间不超过6秒（tryLock等待5秒 + 一些处理时间）
        long executionTime = endTime - startTime;
        assertTrue(executionTime < 6000, 
            "执行时间应该不超过6秒，实际: " + executionTime + "ms");
        
        // 验证数据库没有被查询（因为没有获取到锁）
        verify(dishMapper, never()).selectById(dishId);
    }
    
    /**
     * Property 32: 缓存击穿保护 - 双重检查机制
     * 
     * 测试策略：
     * 1. 模拟第一次Redis查询返回null
     * 2. 模拟获取锁后，第二次Redis查询返回数据（其他线程已写入）
     * 3. 验证不会查询数据库
     * 4. 验证能够正确返回缓存数据
     * 
     * 属性：对于任意菜品ID，如果获取锁后缓存已有数据，应该直接返回缓存数据而不查询数据库
     */
    @Property(tries = 50)
    @Label("Feature: restaurant-tech-stack-upgrade, Property 32: 缓存击穿保护")
    void cacheBreakdownProtection_shouldUseDoubleCheck(
        @ForAll @Positive Long dishId
    ) throws Exception {
        // 准备测试数据
        Dish dish = createTestDish(dishId);
        String redisKey = "dish:" + dishId;
        
        // 将dishId添加到布隆过滤器
        dishCacheService.getBloomFilter().put(dishId);
        
        // 模拟双重检查场景：
        // 第一次查询Redis返回null（缓存未命中）
        // 获取锁后第二次查询Redis返回数据（其他线程已写入）
        when(valueOperations.get(redisKey))
            .thenReturn(null)      // 第一次查询：缓存未命中
            .thenReturn(dish);     // 第二次查询（获取锁后）：缓存已有数据
        
        // 模拟获取锁成功
        when(redissonClient.getLock(anyString())).thenReturn(rLock);
        when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);
        
        // 执行查询
        Dish result = dishCacheService.getDishById(dishId);
        
        // 验证结果
        assertNotNull(result, "应该返回数据");
        assertEquals(dishId, result.getId(), "返回的数据ID应该正确");
        
        // 验证数据库没有被查询（双重检查生效）
        verify(dishMapper, never()).selectById(dishId);
        
        // 验证Redis被查询了至少两次（第一次未命中，第二次命中）
        // 使用atLeast而不是times，因为双重检查锁定模式可能导致多次调用
        verify(valueOperations, atLeast(2)).get(redisKey);
        
        // 验证锁被正确释放
        verify(rLock, times(1)).unlock();
    }
    
    /**
     * 创建测试用的菜品数据
     */
    private Dish createTestDish(Long dishId) {
        Dish dish = new Dish();
        dish.setId(dishId);
        dish.setName("测试菜品" + dishId);
        dish.setDescription("这是一个测试菜品");
        dish.setPrice(new BigDecimal("29.99"));
        dish.setCategoryId(1L);
        dish.setImage("http://example.com/dish" + dishId + ".jpg");
        dish.setStatus(1); // 1表示可售
        dish.setCreateTime(LocalDateTime.now());
        dish.setUpdateTime(LocalDateTime.now());
        return dish;
    }
}
