package com.qiyun.dish;

import com.github.benmanes.caffeine.cache.Cache;
import com.qiyun.dish.entity.Dish;
import com.qiyun.dish.mapper.DishMapper;
import com.qiyun.dish.service.DishCacheService;
import net.jqwik.api.*;
import net.jqwik.api.constraints.Positive;
import net.jqwik.api.lifecycle.BeforeTry;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 缓存雪崩保护属性测试
 * 
 * 测试策略：
 * 1. 验证缓存过期时间的随机性
 * 2. 验证过期时间在合理范围内
 * 3. 验证多个缓存的过期时间分布
 * 
 * **Validates: Property 33 - 缓存过期时间随机化**
 * 
 * @author qiyun
 * @since 2026-02-09
 */
class CacheAvalanchePropertiesTest {
    
    private DishCacheService dishCacheService;
    private DishMapper dishMapper;
    private RedisTemplate<String, Dish> redisTemplate;
    private ValueOperations<String, Dish> valueOperations;
    private RedissonClient redissonClient;
    private RLock rLock;
    
    @BeforeTry
    void setUp() throws Exception {
        // 创建mocks
        redisTemplate = mock(RedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        dishMapper = mock(DishMapper.class);
        redissonClient = mock(RedissonClient.class);
        rLock = mock(RLock.class);
        
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
        
        // Mock RedisTemplate的opsForValue方法
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        
        // Mock Redisson锁
        lenient().when(redissonClient.getLock(anyString())).thenReturn(rLock);
        lenient().when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        lenient().when(rLock.isHeldByCurrentThread()).thenReturn(true);
    }
    
    /**
     * Property 33: 缓存过期时间随机化
     * 
     * 测试策略：
     * 1. 生成多个不同的菜品ID
     * 2. Mock数据库查询返回菜品数据
     * 3. 通过DishCacheService查询菜品（触发缓存写入）
     * 4. 捕获Redis set操作的过期时间参数
     * 5. 验证过期时间在合理范围内（基础时间 + 随机值）
     * 6. 验证多个缓存的过期时间不完全相同（随机性）
     * 
     * 预期结果：
     * - 所有缓存的过期时间都在 [基础时间, 基础时间 + 最大随机值] 范围内
     * - 至少有50%的缓存过期时间不相同（证明随机性）
     */
    @Property(tries = 10)
    @Label("Property 33: 缓存过期时间随机化")
    void cacheExpirationTimeShouldBeRandomized(
        @ForAll @Positive long baseDishId,
        @ForAll("dishCount") int count
    ) {
        // 准备测试数据
        List<Long> dishIds = new ArrayList<>();
        List<Long> capturedExpireTimes = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            long dishId = baseDishId + i;
            dishIds.add(dishId);
            
            // 添加到布隆过滤器
            dishCacheService.getBloomFilter().put(dishId);
            
            // Mock数据库查询返回菜品
            Dish dish = createTestDish(dishId);
            when(dishMapper.selectById(dishId)).thenReturn(dish);
            
            // Mock Redis缓存未命中
            when(valueOperations.get("dish:" + dishId)).thenReturn(null);
            
            // 捕获Redis set操作的过期时间
            doAnswer(invocation -> {
                Long expireTime = invocation.getArgument(2);
                capturedExpireTimes.add(expireTime);
                return null;
            }).when(valueOperations).set(eq("dish:" + dishId), any(Dish.class), anyLong(), eq(TimeUnit.SECONDS));
        }
        
        // 查询菜品，触发缓存写入
        for (Long dishId : dishIds) {
            Dish dish = dishCacheService.getDishById(dishId);
            assertNotNull(dish, "查询结果不应该为null");
        }
        
        // 验证过期时间在合理范围内
        // 根据DishCacheService实现：基础时间300秒，随机值0-59秒
        long minExpireTime = 300L;
        long maxExpireTime = 300L + 60L;
        
        for (Long expireTime : capturedExpireTimes) {
            assertTrue(expireTime >= minExpireTime && expireTime < maxExpireTime,
                String.format("缓存过期时间应该在 [%d, %d) 范围内，实际值: %d", 
                    minExpireTime, maxExpireTime, expireTime));
        }
        
        // 验证过期时间的随机性
        // 至少有50%的缓存过期时间不相同
        Set<Long> uniqueExpireTimes = new HashSet<>(capturedExpireTimes);
        double uniqueRatio = (double) uniqueExpireTimes.size() / capturedExpireTimes.size();
        
        assertTrue(uniqueRatio >= 0.5,
            String.format("至少有50%%的缓存过期时间应该不相同，实际比例: %.2f%%", uniqueRatio * 100));
    }
    
    /**
     * 测试缓存预热时的过期时间随机化
     * 
     * 测试策略：
     * 1. Mock数据库查询返回多个热点菜品
     * 2. 执行缓存预热
     * 3. 捕获Redis set操作的过期时间
     * 4. 验证过期时间的随机性
     */
    @Property(tries = 5)
    @Label("缓存预热过期时间随机化")
    void warmUpShouldSetRandomizedExpirationTime(
        @ForAll @Positive long baseDishId,
        @ForAll("warmUpDishCount") int count
    ) {
        // 准备测试数据
        List<Dish> hotDishes = new ArrayList<>();
        List<Long> capturedExpireTimes = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            long dishId = baseDishId + i;
            Dish dish = createTestDish(dishId);
            dish.setStatus(1); // 设置为可售状态
            hotDishes.add(dish);
        }
        
        // Mock数据库查询返回热点菜品
        when(dishMapper.selectHotDishes()).thenReturn(hotDishes);
        
        // 捕获Redis set操作的过期时间
        doAnswer(invocation -> {
            Long expireTime = invocation.getArgument(2);
            capturedExpireTimes.add(expireTime);
            return null;
        }).when(valueOperations).set(anyString(), any(Dish.class), anyLong(), eq(TimeUnit.SECONDS));
        
        // 执行缓存预热
        dishCacheService.warmUp();
        
        // 验证至少有一些缓存被预热
        assertTrue(capturedExpireTimes.size() > 0, "应该有缓存被预热");
        
        // 验证过期时间在合理范围内
        // 根据DishCacheService实现：基础时间600秒，随机值0-119秒
        long minExpireTime = 600L;
        long maxExpireTime = 600L + 120L;
        
        for (Long expireTime : capturedExpireTimes) {
            assertTrue(expireTime >= minExpireTime && expireTime < maxExpireTime,
                String.format("预热缓存的过期时间应该在 [%d, %d) 范围内，实际值: %d", 
                    minExpireTime, maxExpireTime, expireTime));
        }
        
        // 验证过期时间的随机性
        if (capturedExpireTimes.size() >= 3) {
            Set<Long> uniqueExpireTimes = new HashSet<>(capturedExpireTimes);
            double uniqueRatio = (double) uniqueExpireTimes.size() / capturedExpireTimes.size();
            
            assertTrue(uniqueRatio >= 0.3,
                String.format("预热缓存的过期时间应该有一定的随机性，实际比例: %.2f%%", uniqueRatio * 100));
        }
    }
    
    /**
     * 生成菜品数量
     */
    @Provide
    Arbitrary<Integer> dishCount() {
        return Arbitraries.integers().between(5, 15);
    }
    
    /**
     * 生成预热菜品数量
     */
    @Provide
    Arbitrary<Integer> warmUpDishCount() {
        return Arbitraries.integers().between(3, 10);
    }
    
    /**
     * 创建测试菜品
     */
    private Dish createTestDish(Long id) {
        Dish dish = new Dish();
        dish.setId(id);
        dish.setName("测试菜品" + id);
        dish.setPrice(new BigDecimal("29.99"));
        dish.setCategoryId(1L);
        dish.setDescription("测试描述");
        dish.setImage("test.jpg");
        dish.setStatus(1);
        return dish;
    }
}
