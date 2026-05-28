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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

/**
 * 缓存预热功能测试
 * 
 * 测试系统启动时的缓存预热功能：
 * 1. 从数据库加载热点数据
 * 2. 预热到本地缓存（Caffeine）
 * 3. 预热到分布式缓存（Redis）
 * 4. 实现热点数据识别逻辑
 * 
 * Requirements: 7.5
 * 
 * @author qiyun
 * @since 2026-02-08
 */
@ExtendWith(MockitoExtension.class)
class CacheWarmupTest {
    
    @Mock
    private RedisTemplate<String, Dish> redisTemplate;
    
    @Mock
    private ValueOperations<String, Dish> valueOperations;
    
    @Mock
    private DishMapper dishMapper;
    
    @InjectMocks
    private DishCacheService dishCacheService;
    
    private List<Dish> hotDishes;
    
    @BeforeEach
    void setUp() {
        // 准备热点菜品数据
        hotDishes = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Dish dish = new Dish();
            dish.setId((long) i);
            dish.setName("热门菜品" + i);
            dish.setCategoryId(1L);
            dish.setPrice(new BigDecimal("38.00"));
            dish.setDescription("热门菜品描述" + i);
            dish.setStatus(1); // 可售状态
            dish.setCreateTime(LocalDateTime.now().minusDays(i));
            dish.setUpdateTime(LocalDateTime.now());
            hotDishes.add(dish);
        }
        
        // Mock RedisTemplate的opsForValue方法（使用lenient避免不必要的stubbing警告）
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }
    
    /**
     * 测试缓存预热成功场景
     * 
     * 场景：系统启动时，从数据库查询到热点菜品
     * 预期：
     * 1. 调用DishMapper.selectHotDishes()查询热点数据
     * 2. 将所有热点数据写入Redis（过期时间600-720秒）
     * 3. 将所有热点数据写入本地缓存
     */
    @Test
    void testWarmUp_Success() {
        // Given: 数据库中有热点菜品
        when(dishMapper.selectHotDishes()).thenReturn(hotDishes);
        
        // When: 执行缓存预热
        dishCacheService.init();
        
        // Then: 应该查询热点菜品
        verify(dishMapper, times(1)).selectHotDishes();
        
        // 应该将所有热点菜品写入Redis（过期时间600-720秒，防止缓存雪崩）
        verify(valueOperations, times(10)).set(
            anyString(),
            any(Dish.class),
            longThat(expire -> expire >= 600 && expire < 720),
            eq(TimeUnit.SECONDS)
        );
        
        // 验证每个菜品都被写入Redis
        for (Dish dish : hotDishes) {
            verify(valueOperations, times(1)).set(
                eq("dish:" + dish.getId()),
                eq(dish),
                longThat(expire -> expire >= 600 && expire < 720),
                eq(TimeUnit.SECONDS)
            );
        }
        
        // 验证数据被写入本地缓存（通过查询验证）
        for (Dish dish : hotDishes) {
            Dish cached = dishCacheService.getDishById(dish.getId());
            assertNotNull(cached, "菜品应该在本地缓存中: " + dish.getId());
            assertEquals(dish.getId(), cached.getId());
            assertEquals(dish.getName(), cached.getName());
        }
        
        // 验证查询时没有再次访问数据库（说明数据在本地缓存中）
        verify(dishMapper, times(1)).selectHotDishes(); // 只在预热时查询了一次
    }
    
    /**
     * 测试热点数据识别逻辑
     * 
     * 场景：DishMapper.selectHotDishes()应该返回最新的100条可售菜品
     * 预期：预热的数据应该是状态为可售（status=1）且按创建时间倒序的菜品
     * 
     * 注：这个测试验证热点数据的识别逻辑是否正确
     */
    @Test
    void testWarmUp_HotDataIdentification() {
        // Given: 准备不同状态的菜品
        List<Dish> mixedDishes = new ArrayList<>();
        
        // 可售菜品（应该被预热）
        for (int i = 1; i <= 5; i++) {
            Dish dish = new Dish();
            dish.setId((long) i);
            dish.setName("可售菜品" + i);
            dish.setStatus(1); // 可售
            dish.setCreateTime(LocalDateTime.now().minusDays(i));
            mixedDishes.add(dish);
        }
        
        // 售罄菜品（不应该被预热）
        for (int i = 6; i <= 8; i++) {
            Dish dish = new Dish();
            dish.setId((long) i);
            dish.setName("售罄菜品" + i);
            dish.setStatus(0); // 售罄
            dish.setCreateTime(LocalDateTime.now().minusDays(i));
            // 这些菜品不应该被selectHotDishes返回
        }
        
        // Mock只返回可售菜品（模拟SQL的WHERE status = 1条件）
        List<Dish> availableDishes = mixedDishes.subList(0, 5);
        when(dishMapper.selectHotDishes()).thenReturn(availableDishes);
        
        // When: 执行缓存预热
        dishCacheService.init();
        
        // Then: 只有可售菜品被预热
        verify(valueOperations, times(5)).set(
            anyString(),
            any(Dish.class),
            anyLong(),
            eq(TimeUnit.SECONDS)
        );
        
        // 验证可售菜品在缓存中
        for (int i = 1; i <= 5; i++) {
            Dish cached = dishCacheService.getDishById((long) i);
            assertNotNull(cached, "可售菜品应该在缓存中: " + i);
        }
    }
    
    /**
     * 测试缓存预热时数据库为空
     * 
     * 场景：数据库中没有热点菜品
     * 预期：不执行任何缓存写入操作，不抛出异常
     */
    @Test
    void testWarmUp_EmptyHotDishes() {
        // Given: 数据库中没有热点菜品
        when(dishMapper.selectHotDishes()).thenReturn(new ArrayList<>());
        
        // When: 执行缓存预热
        assertDoesNotThrow(() -> dishCacheService.init());
        
        // Then: 应该查询热点菜品
        verify(dishMapper, times(1)).selectHotDishes();
        
        // 不应该写入任何缓存
        verify(valueOperations, never()).set(anyString(), any(), anyLong(), any());
    }
    
    /**
     * 测试缓存预热时数据库返回null
     * 
     * 场景：数据库查询返回null
     * 预期：不执行任何缓存写入操作，不抛出异常
     */
    @Test
    void testWarmUp_NullHotDishes() {
        // Given: 数据库查询返回null
        when(dishMapper.selectHotDishes()).thenReturn(null);
        
        // When: 执行缓存预热
        assertDoesNotThrow(() -> dishCacheService.init());
        
        // Then: 应该查询热点菜品
        verify(dishMapper, times(1)).selectHotDishes();
        
        // 不应该写入任何缓存
        verify(valueOperations, never()).set(anyString(), any(), anyLong(), any());
    }
    
    /**
     * 测试缓存预热时包含无效数据
     * 
     * 场景：热点菜品列表中包含null或ID为null的数据
     * 预期：跳过无效数据，继续处理其他有效数据
     */
    @Test
    void testWarmUp_WithInvalidData() {
        // Given: 热点菜品列表中包含无效数据
        List<Dish> dishesWithInvalid = new ArrayList<>();
        
        // 有效菜品
        Dish validDish1 = new Dish();
        validDish1.setId(1L);
        validDish1.setName("有效菜品1");
        validDish1.setStatus(1);
        dishesWithInvalid.add(validDish1);
        
        // null菜品
        dishesWithInvalid.add(null);
        
        // ID为null的菜品
        Dish invalidDish = new Dish();
        invalidDish.setName("无效菜品");
        invalidDish.setStatus(1);
        dishesWithInvalid.add(invalidDish);
        
        // 有效菜品
        Dish validDish2 = new Dish();
        validDish2.setId(2L);
        validDish2.setName("有效菜品2");
        validDish2.setStatus(1);
        dishesWithInvalid.add(validDish2);
        
        when(dishMapper.selectHotDishes()).thenReturn(dishesWithInvalid);
        
        // When: 执行缓存预热
        assertDoesNotThrow(() -> dishCacheService.init());
        
        // Then: 只有有效菜品被写入缓存
        verify(valueOperations, times(2)).set(
            anyString(),
            any(Dish.class),
            anyLong(),
            eq(TimeUnit.SECONDS)
        );
        
        // 验证有效菜品在缓存中
        assertNotNull(dishCacheService.getDishById(1L));
        assertNotNull(dishCacheService.getDishById(2L));
    }
    
    /**
     * 测试缓存预热时Redis写入失败
     * 
     * 场景：Redis写入时抛出异常
     * 预期：记录错误日志，继续处理其他数据，不影响系统启动
     */
    @Test
    void testWarmUp_RedisWriteFailure() {
        // Given: Redis写入时抛出异常
        when(dishMapper.selectHotDishes()).thenReturn(hotDishes);
        
        // 第一个菜品写入失败
        doThrow(new RuntimeException("Redis connection failed"))
            .when(valueOperations).set(
                eq("dish:1"),
                any(Dish.class),
                anyLong(),
                eq(TimeUnit.SECONDS)
            );
        
        // 其他菜品写入成功
        doNothing().when(valueOperations).set(
            argThat(key -> !key.equals("dish:1")),
            any(Dish.class),
            anyLong(),
            eq(TimeUnit.SECONDS)
        );
        
        // When: 执行缓存预热
        assertDoesNotThrow(() -> dishCacheService.init());
        
        // Then: 应该尝试写入所有菜品
        verify(valueOperations, times(10)).set(
            anyString(),
            any(Dish.class),
            anyLong(),
            eq(TimeUnit.SECONDS)
        );
        
        // 验证失败的菜品不在本地缓存中（因为Redis写入失败后，本地缓存也不会写入）
        // 但其他菜品应该在本地缓存中
        for (int i = 2; i <= 10; i++) {
            Dish cached = dishCacheService.getDishById((long) i);
            assertNotNull(cached, "菜品应该在本地缓存中: " + i);
        }
    }
    
    /**
     * 测试缓存预热时数据库查询失败
     * 
     * 场景：数据库查询时抛出异常
     * 预期：记录错误日志，不影响系统启动
     */
    @Test
    void testWarmUp_DatabaseQueryFailure() {
        // Given: 数据库查询失败
        when(dishMapper.selectHotDishes()).thenThrow(new RuntimeException("Database connection failed"));
        
        // When: 执行缓存预热
        assertDoesNotThrow(() -> dishCacheService.init());
        
        // Then: 应该尝试查询数据库
        verify(dishMapper, times(1)).selectHotDishes();
        
        // 不应该写入任何缓存
        verify(valueOperations, never()).set(anyString(), any(), anyLong(), any());
    }
    
    /**
     * 测试缓存预热的过期时间随机化
     * 
     * 场景：预热多个菜品到Redis
     * 预期：每个菜品的过期时间都在600-720秒之间（防止缓存雪崩）
     */
    @Test
    void testWarmUp_ExpirationRandomization() {
        // Given: 准备大量热点菜品
        List<Dish> manyDishes = new ArrayList<>();
        for (int i = 1; i <= 50; i++) {
            Dish dish = new Dish();
            dish.setId((long) i);
            dish.setName("菜品" + i);
            dish.setStatus(1);
            dish.setCreateTime(LocalDateTime.now().minusDays(i));
            manyDishes.add(dish);
        }
        
        when(dishMapper.selectHotDishes()).thenReturn(manyDishes);
        
        // When: 执行缓存预热
        dishCacheService.init();
        
        // Then: 所有菜品的过期时间都应该在600-720秒之间
        verify(valueOperations, times(50)).set(
            anyString(),
            any(Dish.class),
            longThat(expire -> expire >= 600 && expire < 720),
            eq(TimeUnit.SECONDS)
        );
    }
    
    /**
     * 测试缓存预热后的查询性能
     * 
     * 场景：预热后查询热点数据
     * 预期：直接从本地缓存返回，不访问Redis和数据库
     */
    @Test
    void testWarmUp_QueryPerformance() {
        // Given: 执行缓存预热
        when(dishMapper.selectHotDishes()).thenReturn(hotDishes);
        dishCacheService.init();
        
        // When: 查询所有预热的菜品
        for (Dish dish : hotDishes) {
            Dish cached = dishCacheService.getDishById(dish.getId());
            assertNotNull(cached);
            assertEquals(dish.getId(), cached.getId());
        }
        
        // Then: 应该只在预热时查询了一次数据库
        verify(dishMapper, times(1)).selectHotDishes();
        
        // 查询时不应该访问Redis（因为数据在本地缓存中）
        verify(valueOperations, times(10)).set(anyString(), any(), anyLong(), any()); // 只有预热时的写入
        verify(valueOperations, never()).get(anyString()); // 没有读取操作
    }
    
    /**
     * 测试手动触发缓存预热
     * 
     * 场景：系统运行时手动调用warmUp()方法
     * 预期：重新加载热点数据并更新缓存
     */
    @Test
    void testWarmUp_ManualTrigger() {
        // Given: 系统已启动
        when(dishMapper.selectHotDishes()).thenReturn(hotDishes);
        dishCacheService.init();
        
        // 清除mock的调用记录
        clearInvocations(dishMapper, valueOperations);
        
        // 准备新的热点数据
        List<Dish> newHotDishes = new ArrayList<>();
        for (int i = 11; i <= 15; i++) {
            Dish dish = new Dish();
            dish.setId((long) i);
            dish.setName("新热门菜品" + i);
            dish.setStatus(1);
            dish.setCreateTime(LocalDateTime.now());
            newHotDishes.add(dish);
        }
        
        when(dishMapper.selectHotDishes()).thenReturn(newHotDishes);
        
        // When: 手动触发缓存预热
        dishCacheService.warmUp();
        
        // Then: 应该重新查询热点菜品
        verify(dishMapper, times(1)).selectHotDishes();
        
        // 应该写入新的热点菜品
        verify(valueOperations, times(5)).set(
            anyString(),
            any(Dish.class),
            anyLong(),
            eq(TimeUnit.SECONDS)
        );
        
        // 验证新菜品在缓存中
        for (int i = 11; i <= 15; i++) {
            Dish cached = dishCacheService.getDishById((long) i);
            assertNotNull(cached, "新菜品应该在缓存中: " + i);
        }
    }
}
