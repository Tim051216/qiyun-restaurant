package com.qiyun.dish;

import com.qiyun.dish.entity.Dish;
import com.qiyun.dish.mapper.DishMapper;
import com.qiyun.dish.service.DishCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * DishCacheService单元测试
 * 
 * 测试多级缓存的查询顺序和缓存更新逻辑
 * 
 * @author qiyun
 * @since 2026-02-08
 */
@ExtendWith(MockitoExtension.class)
class DishCacheServiceTest {
    
    @Mock
    private RedisTemplate<String, Dish> redisTemplate;
    
    @Mock
    private ValueOperations<String, Dish> valueOperations;
    
    @Mock
    private DishMapper dishMapper;
    
    @Mock
    private RedissonClient redissonClient;
    
    @Mock
    private RLock rLock;
    
    @InjectMocks
    private DishCacheService dishCacheService;
    
    private Dish testDish;
    
    @BeforeEach
    void setUp() throws Exception {
        // 初始化本地缓存
        dishCacheService.init();
        
        // 准备测试数据
        testDish = new Dish();
        testDish.setId(1L);
        testDish.setName("宫保鸡丁");
        testDish.setCategoryId(1L);
        testDish.setPrice(new BigDecimal("38.00"));
        testDish.setDescription("经典川菜");
        testDish.setStatus(1);
        testDish.setCreateTime(LocalDateTime.now());
        testDish.setUpdateTime(LocalDateTime.now());
        
        // Mock RedisTemplate的opsForValue方法（使用lenient避免不必要的stubbing警告）
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        
        // Mock RedissonClient和RLock（使用lenient避免不必要的stubbing警告）
        lenient().when(redissonClient.getLock(anyString())).thenReturn(rLock);
        lenient().when(rLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(true);
        lenient().when(rLock.isHeldByCurrentThread()).thenReturn(true);
    }
    
    /**
     * 测试L1本地缓存命中
     * 
     * 场景：数据在本地缓存中存在
     * 预期：直接从本地缓存返回，不查询Redis和数据库
     */
    @Test
    void testGetDishById_L1CacheHit() throws Exception {
        // Given: 先查询一次，让数据进入本地缓存
        Long dishId = 1L;
        
        // 添加到布隆过滤器（模拟数据存在）
        dishCacheService.getBloomFilter().put(dishId);
        
        // Mock分布式锁行为
        when(redissonClient.getLock("lock:dish:" + dishId)).thenReturn(rLock);
        when(rLock.tryLock(5, 10, TimeUnit.SECONDS)).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);
        
        when(valueOperations.get("dish:" + dishId)).thenReturn(null);
        when(dishMapper.selectById(dishId)).thenReturn(testDish);
        
        // 第一次查询，数据会从数据库加载并缓存
        Dish firstResult = dishCacheService.getDishById(dishId);
        assertNotNull(firstResult);
        
        // When: 再次查询相同数据（此时数据已在本地缓存中）
        Dish secondResult = dishCacheService.getDishById(dishId);
        
        // Then: 应该从本地缓存返回，不再查询Redis和数据库
        assertNotNull(secondResult);
        assertEquals(testDish.getId(), secondResult.getId());
        assertEquals(testDish.getName(), secondResult.getName());
        
        // 验证只查询了一次数据库（第一次查询时）
        verify(dishMapper, times(1)).selectById(dishId);
        // 验证Redis查询次数：第一次L2查询 + 第一次获取锁后的双重检查 = 2次
        verify(valueOperations, times(2)).get("dish:" + dishId);
    }
    
    /**
     * 测试L2 Redis缓存命中
     * 
     * 场景：数据不在本地缓存，但在Redis缓存中
     * 预期：从Redis返回，并回写到本地缓存，不查询数据库
     */
    @Test
    void testGetDishById_L2CacheHit() {
        // Given: 数据在Redis中
        Long dishId = 2L;
        
        // 添加到布隆过滤器（模拟数据存在）
        dishCacheService.getBloomFilter().put(dishId);
        
        when(valueOperations.get("dish:" + dishId)).thenReturn(testDish);
        
        // When: 查询数据
        Dish result = dishCacheService.getDishById(dishId);
        
        // Then: 应该从Redis返回
        assertNotNull(result);
        assertEquals(testDish.getId(), result.getId());
        
        // 验证查询了Redis
        verify(valueOperations, times(1)).get("dish:" + dishId);
        
        // 验证没有查询数据库
        verify(dishMapper, never()).selectById(dishId);
        
        // 验证数据被回写到本地缓存（再次查询时不会查Redis）
        Dish secondResult = dishCacheService.getDishById(dishId);
        assertNotNull(secondResult);
        verify(valueOperations, times(1)).get("dish:" + dishId); // 仍然只查询了一次Redis
    }
    
    /**
     * 测试L3数据库查询
     * 
     * 场景：数据不在本地缓存和Redis缓存中
     * 预期：从数据库查询，并回写到Redis和本地缓存
     */
    @Test
    void testGetDishById_L3DatabaseQuery() throws Exception {
        // Given: 数据不在缓存中，需要查询数据库
        Long dishId = 3L;
        
        // 添加到布隆过滤器（模拟数据存在）
        dishCacheService.getBloomFilter().put(dishId);
        
        // Mock分布式锁行为
        when(redissonClient.getLock("lock:dish:" + dishId)).thenReturn(rLock);
        when(rLock.tryLock(5, 10, TimeUnit.SECONDS)).thenReturn(true);
        when(rLock.isHeldByCurrentThread()).thenReturn(true);
        
        when(valueOperations.get("dish:" + dishId)).thenReturn(null);
        when(dishMapper.selectById(dishId)).thenReturn(testDish);
        
        // When: 查询数据
        Dish result = dishCacheService.getDishById(dishId);
        
        // Then: 应该从数据库返回
        assertNotNull(result);
        assertEquals(testDish.getId(), result.getId());
        
        // 验证查询了数据库
        verify(dishMapper, times(1)).selectById(dishId);
        
        // 验证数据被写入Redis（带随机过期时间）
        verify(valueOperations, times(1)).set(
            eq("dish:" + dishId),
            eq(testDish),
            longThat(expire -> expire >= 300 && expire < 360),
            eq(TimeUnit.SECONDS)
        );
        
        // 验证获取了分布式锁
        verify(redissonClient, times(1)).getLock("lock:dish:" + dishId);
        verify(rLock, times(1)).tryLock(5, 10, TimeUnit.SECONDS);
        verify(rLock, times(1)).unlock();
    }
    
    /**
     * 测试查询不存在的数据（布隆过滤器拦截）
     * 
     * 场景：数据在数据库中不存在，且未添加到布隆过滤器
     * 预期：布隆过滤器直接拦截，返回null，不查询数据库
     */
    @Test
    void testGetDishById_NotFound() {
        // Given: 数据不存在，且未添加到布隆过滤器
        Long dishId = 999L;
        // 注意：不添加到布隆过滤器，模拟不存在的数据
        
        // When: 查询数据
        Dish result = dishCacheService.getDishById(dishId);
        
        // Then: 应该返回null（被布隆过滤器拦截）
        assertNull(result);
        
        // 验证没有查询数据库（被布隆过滤器拦截）
        verify(dishMapper, never()).selectById(dishId);
        
        // 验证没有查询Redis
        verify(valueOperations, never()).get(anyString());
    }
    
    /**
     * 测试查询null ID
     * 
     * 场景：传入null ID
     * 预期：直接返回null，不查询任何层级
     */
    @Test
    void testGetDishById_NullId() {
        // When: 查询null ID
        Dish result = dishCacheService.getDishById(null);
        
        // Then: 应该返回null
        assertNull(result);
        
        // 验证没有查询Redis和数据库
        verify(valueOperations, never()).get(anyString());
        verify(dishMapper, never()).selectById(any());
    }
    
    /**
     * 测试更新菜品并删除缓存
     * 
     * 场景：更新菜品数据
     * 预期：更新数据库，删除本地缓存和Redis缓存
     */
    @Test
    void testUpdateDish() {
        // Given: 先查询一次，让数据进入缓存
        Long dishId = 1L;
        
        // 添加到布隆过滤器
        dishCacheService.getBloomFilter().put(dishId);
        
        when(valueOperations.get("dish:" + dishId)).thenReturn(testDish);
        dishCacheService.getDishById(dishId);
        
        // 修改菜品信息
        testDish.setPrice(new BigDecimal("48.00"));
        
        // When: 更新菜品
        dishCacheService.updateDish(testDish);
        
        // Then: 应该更新数据库
        verify(dishMapper, times(1)).updateById(testDish);
        
        // 应该删除Redis缓存
        verify(redisTemplate, times(1)).delete("dish:" + dishId);
        
        // 验证本地缓存被清除（再次查询会查Redis）
        when(valueOperations.get("dish:" + dishId)).thenReturn(testDish);
        dishCacheService.getDishById(dishId);
        verify(valueOperations, times(2)).get("dish:" + dishId); // 第一次查询 + 缓存清除后的查询
    }
    
    /**
     * 测试更新null菜品
     * 
     * 场景：传入null菜品
     * 预期：不执行任何操作
     */
    @Test
    void testUpdateDish_Null() {
        // When: 更新null菜品
        dishCacheService.updateDish(null);
        
        // Then: 不应该执行任何操作
        verify(dishMapper, never()).updateById(any());
        verify(redisTemplate, never()).delete(anyString());
    }
    
    /**
     * 测试更新ID为null的菜品
     * 
     * 场景：菜品对象不为null，但ID为null
     * 预期：不执行任何操作
     */
    @Test
    void testUpdateDish_NullId() {
        // Given: 菜品ID为null
        Dish dish = new Dish();
        dish.setName("测试菜品");
        
        // When: 更新菜品
        dishCacheService.updateDish(dish);
        
        // Then: 不应该执行任何操作
        verify(dishMapper, never()).updateById(any());
        verify(redisTemplate, never()).delete(anyString());
    }
    
    /**
     * 测试缓存统计信息
     * 
     * 场景：获取缓存统计信息
     * 预期：返回统计信息字符串
     */
    @Test
    void testGetCacheStats() {
        // When: 获取缓存统计
        String stats = dishCacheService.getCacheStats();
        
        // Then: 应该返回统计信息
        assertNotNull(stats);
        assertTrue(stats.contains("hitCount") || stats.contains("missCount"));
    }
    
    /**
     * 测试缓存过期时间随机化
     * 
     * 场景：多次查询不同的数据
     * 预期：每次设置的过期时间都在300-360秒之间（防止缓存雪崩）
     */
    @Test
    void testCacheExpirationRandomization() throws Exception {
        // Given: 准备多个不同的菜品
        for (long i = 10; i < 20; i++) {
            Long dishId = i;
            Dish dish = new Dish();
            dish.setId(dishId);
            dish.setName("菜品" + dishId);
            
            // 添加到布隆过滤器
            dishCacheService.getBloomFilter().put(dishId);
            
            // Mock分布式锁行为
            RLock mockLock = mock(RLock.class);
            when(redissonClient.getLock("lock:dish:" + dishId)).thenReturn(mockLock);
            when(mockLock.tryLock(5, 10, TimeUnit.SECONDS)).thenReturn(true);
            when(mockLock.isHeldByCurrentThread()).thenReturn(true);
            
            when(valueOperations.get("dish:" + dishId)).thenReturn(null);
            when(dishMapper.selectById(dishId)).thenReturn(dish);
            
            // When: 查询数据
            dishCacheService.getDishById(dishId);
        }
        
        // Then: 验证所有写入Redis的过期时间都在300-360秒之间
        verify(valueOperations, times(10)).set(
            anyString(),
            any(Dish.class),
            longThat(expire -> expire >= 300 && expire < 360),
            eq(TimeUnit.SECONDS)
        );
    }
}
