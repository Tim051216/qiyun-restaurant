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
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 多级缓存属性测试
 * 
 * 使用jqwik进行属性测试，验证多级缓存的查询顺序正确性
 * 
 * **Validates: Requirements 7.1, 7.2, 7.3**
 * 
 * @author qiyun
 * @since 2026-02-08
 */
class MultiLevelCachePropertiesTest {
    
    private RedisTemplate<String, Dish> redisTemplate;
    private ValueOperations<String, Dish> valueOperations;
    private DishMapper dishMapper;
    private RedissonClient redissonClient;
    private RLock rLock;
    private DishCacheService dishCacheService;
    
    @BeforeTry
    void setUp() throws Exception {
        // 手动创建mocks
        redisTemplate = mock(RedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        dishMapper = mock(DishMapper.class);
        redissonClient = mock(RedissonClient.class);
        rLock = mock(RLock.class);
        
        // 创建DishCacheService实例并注入mocks
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
        
        // 初始化本地缓存
        dishCacheService.init();
        
        // Mock RedisTemplate的opsForValue方法
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        
        // Mock RedissonClient的getLock方法
        lenient().when(redissonClient.getLock(anyString())).thenReturn(rLock);
        
        // Mock RLock的tryLock方法（默认返回true）
        lenient().when(rLock.tryLock(anyLong(), anyLong(), any())).thenReturn(true);
        lenient().when(rLock.isHeldByCurrentThread()).thenReturn(true);
    }
    
    /**
     * Property 29: 多级缓存查询顺序
     * 
     * For any 数据查询请求，应该按照本地缓存 → Redis缓存 → 数据库的顺序查找数据
     * 
     * 验证策略：
     * 1. L1本地缓存命中时，不查询L2和L3
     * 2. L1未命中但L2 Redis命中时，不查询L3，并回写L1
     * 3. L1和L2都未命中时，查询L3数据库，并回写L1和L2
     * 
     * **Validates: Requirements 7.1, 7.2, 7.3**
     */
    @Property(tries = 100)
    @Label("Feature: restaurant-tech-stack-upgrade, Property 29: 多级缓存查询顺序")
    void multiLevelCacheQueryOrder(
            @ForAll @Positive Long dishId,
            @ForAll("dishNames") String dishName,
            @ForAll("prices") BigDecimal price) {
        
        // 准备测试数据
        Dish testDish = createTestDish(dishId, dishName, price);
        
        // 将测试ID添加到布隆过滤器，避免被拦截
        dishCacheService.getBloomFilter().put(dishId);
        
        // 清理之前的mock调用记录
        clearInvocations(valueOperations, dishMapper);
        
        // 清空本地缓存，确保测试从干净状态开始
        clearLocalCache();
        
        // 场景1：L1、L2、L3都未命中（数据不存在）
        when(valueOperations.get("dish:" + dishId)).thenReturn(null);
        when(dishMapper.selectById(dishId)).thenReturn(null);
        
        Dish result1 = dishCacheService.getDishById(dishId);
        
        // 验证：应该查询Redis和数据库
        assertNull(result1, "数据不存在时应返回null");
        verify(valueOperations, times(1)).get("dish:" + dishId);
        verify(dishMapper, times(1)).selectById(dishId);
        verify(valueOperations, never()).set(anyString(), any(), anyLong(), any());
        
        // 清理mock调用记录
        clearInvocations(valueOperations, dishMapper);
        clearLocalCache();
        
        // 场景2：L1和L2未命中，L3数据库命中
        when(valueOperations.get("dish:" + dishId)).thenReturn(null);
        when(dishMapper.selectById(dishId)).thenReturn(testDish);
        
        Dish result2 = dishCacheService.getDishById(dishId);
        
        // 验证：应该查询Redis和数据库，并回写到Redis和本地缓存
        assertNotNull(result2, "数据库中存在的数据应返回");
        assertEquals(dishId, result2.getId());
        assertEquals(dishName, result2.getName());
        verify(valueOperations, times(1)).get("dish:" + dishId);
        verify(dishMapper, times(1)).selectById(dishId);
        verify(valueOperations, times(1)).set(
            eq("dish:" + dishId),
            eq(testDish),
            longThat(expire -> expire >= 300 && expire < 360),
            eq(TimeUnit.SECONDS)
        );
        
        // 验证数据已在本地缓存中（再次查询不会查Redis和数据库）
        clearInvocations(valueOperations, dishMapper);
        
        Dish result3 = dishCacheService.getDishById(dishId);
        assertNotNull(result3, "本地缓存中的数据应返回");
        verify(valueOperations, never()).get(anyString());
        verify(dishMapper, never()).selectById(any());
        
        // 清理mock调用记录和本地缓存
        clearInvocations(valueOperations, dishMapper);
        clearLocalCache();
        
        // 场景3：L1未命中，L2 Redis命中
        when(valueOperations.get("dish:" + dishId)).thenReturn(testDish);
        
        Dish result4 = dishCacheService.getDishById(dishId);
        
        // 验证：应该查询Redis，不查询数据库，并回写到本地缓存
        assertNotNull(result4, "Redis中存在的数据应返回");
        assertEquals(dishId, result4.getId());
        verify(valueOperations, times(1)).get("dish:" + dishId);
        verify(dishMapper, never()).selectById(any());
        
        // 验证数据已在本地缓存中（再次查询不会查Redis）
        clearInvocations(valueOperations, dishMapper);
        
        Dish result5 = dishCacheService.getDishById(dishId);
        assertNotNull(result5, "本地缓存中的数据应返回");
        verify(valueOperations, never()).get(anyString());
        verify(dishMapper, never()).selectById(any());
    }
    
    /**
     * Property 29.1: L1本地缓存优先级最高
     * 
     * For any 已在本地缓存中的数据，查询时应该直接从本地缓存返回，不查询Redis和数据库
     * 
     * **Validates: Requirements 7.1**
     */
    @Property(tries = 100)
    @Label("Feature: restaurant-tech-stack-upgrade, Property 29: 多级缓存查询顺序")
    void l1LocalCacheHasHighestPriority(
            @ForAll @Positive Long dishId,
            @ForAll("dishNames") String dishName,
            @ForAll("prices") BigDecimal price) {
        
        // 准备测试数据
        Dish testDish = createTestDish(dishId, dishName, price);
        
        // 将测试ID添加到布隆过滤器，避免被拦截
        dishCacheService.getBloomFilter().put(dishId);
        
        // 清空本地缓存
        clearLocalCache();
        
        // 先查询一次，让数据进入本地缓存
        when(valueOperations.get("dish:" + dishId)).thenReturn(testDish);
        Dish firstResult = dishCacheService.getDishById(dishId);
        assertNotNull(firstResult);
        
        // 清理mock调用记录
        clearInvocations(valueOperations, dishMapper);
        
        // 再次查询相同数据（此时数据已在本地缓存中）
        Dish secondResult = dishCacheService.getDishById(dishId);
        
        // 验证：应该从本地缓存返回，不查询Redis和数据库
        assertNotNull(secondResult, "本地缓存中的数据应返回");
        assertEquals(dishId, secondResult.getId());
        assertEquals(dishName, secondResult.getName());
        verify(valueOperations, never()).get(anyString());
        verify(dishMapper, never()).selectById(any());
    }
    
    /**
     * Property 29.2: L2 Redis缓存次优先级
     * 
     * For any 不在本地缓存但在Redis缓存中的数据，查询时应该从Redis返回，不查询数据库，并回写到本地缓存
     * 
     * **Validates: Requirements 7.2**
     */
    @Property(tries = 100)
    @Label("Feature: restaurant-tech-stack-upgrade, Property 29: 多级缓存查询顺序")
    void l2RedisCacheHasSecondPriority(
            @ForAll @Positive Long dishId,
            @ForAll("dishNames") String dishName,
            @ForAll("prices") BigDecimal price) {
        
        // 准备测试数据
        Dish testDish = createTestDish(dishId, dishName, price);
        
        // 将测试ID添加到布隆过滤器，避免被拦截
        dishCacheService.getBloomFilter().put(dishId);
        
        // 清空本地缓存
        clearLocalCache();
        
        // 数据在Redis中
        when(valueOperations.get("dish:" + dishId)).thenReturn(testDish);
        
        // 查询数据
        Dish result = dishCacheService.getDishById(dishId);
        
        // 验证：应该从Redis返回，不查询数据库
        assertNotNull(result, "Redis中存在的数据应返回");
        assertEquals(dishId, result.getId());
        assertEquals(dishName, result.getName());
        verify(valueOperations, times(1)).get("dish:" + dishId);
        verify(dishMapper, never()).selectById(any());
        
        // 验证数据被回写到本地缓存（再次查询时不会查Redis）
        clearInvocations(valueOperations, dishMapper);
        
        Dish secondResult = dishCacheService.getDishById(dishId);
        assertNotNull(secondResult, "本地缓存中的数据应返回");
        verify(valueOperations, never()).get(anyString());
        verify(dishMapper, never()).selectById(any());
    }
    
    /**
     * Property 29.3: L3数据库作为最终数据源
     * 
     * For any 不在本地缓存和Redis缓存中的数据，查询时应该从数据库查询，并回写到Redis和本地缓存
     * 
     * **Validates: Requirements 7.3**
     */
    @Property(tries = 100)
    @Label("Feature: restaurant-tech-stack-upgrade, Property 29: 多级缓存查询顺序")
    void l3DatabaseIsUltimateDatasource(
            @ForAll @Positive Long dishId,
            @ForAll("dishNames") String dishName,
            @ForAll("prices") BigDecimal price) {
        
        // 准备测试数据
        Dish testDish = createTestDish(dishId, dishName, price);
        
        // 将测试ID添加到布隆过滤器，避免被拦截
        dishCacheService.getBloomFilter().put(dishId);
        
        // 清空本地缓存
        clearLocalCache();
        
        // 数据不在缓存中，需要查询数据库
        when(valueOperations.get("dish:" + dishId)).thenReturn(null);
        when(dishMapper.selectById(dishId)).thenReturn(testDish);
        
        // 查询数据
        Dish result = dishCacheService.getDishById(dishId);
        
        // 验证：应该从数据库返回
        assertNotNull(result, "数据库中存在的数据应返回");
        assertEquals(dishId, result.getId());
        assertEquals(dishName, result.getName());
        verify(valueOperations, times(1)).get("dish:" + dishId);
        verify(dishMapper, times(1)).selectById(dishId);
        
        // 验证数据被写入Redis（带随机过期时间）
        verify(valueOperations, times(1)).set(
            eq("dish:" + dishId),
            eq(testDish),
            longThat(expire -> expire >= 300 && expire < 360),
            eq(TimeUnit.SECONDS)
        );
        
        // 验证数据被写入本地缓存（再次查询不会查Redis和数据库）
        clearInvocations(valueOperations, dishMapper);
        
        Dish secondResult = dishCacheService.getDishById(dishId);
        assertNotNull(secondResult, "本地缓存中的数据应返回");
        verify(valueOperations, never()).get(anyString());
        verify(dishMapper, never()).selectById(any());
    }
    
    /**
     * Property 29.4: 缓存未命中时的完整查询链路
     * 
     * For any 不存在的数据，查询时应该依次查询本地缓存、Redis缓存、数据库，都未命中时返回null
     * 
     * **Validates: Requirements 7.1, 7.2, 7.3**
     */
    @Property(tries = 100)
    @Label("Feature: restaurant-tech-stack-upgrade, Property 29: 多级缓存查询顺序")
    void cacheMissFollowsCompleteQueryChain(@ForAll @Positive Long dishId) {
        
        // 将测试ID添加到布隆过滤器，避免被拦截
        dishCacheService.getBloomFilter().put(dishId);
        
        // 清空本地缓存
        clearLocalCache();
        
        // 数据在所有层级都不存在
        when(valueOperations.get("dish:" + dishId)).thenReturn(null);
        when(dishMapper.selectById(dishId)).thenReturn(null);
        
        // 查询数据
        Dish result = dishCacheService.getDishById(dishId);
        
        // 验证：应该返回null
        assertNull(result, "不存在的数据应返回null");
        
        // 验证：应该依次查询Redis和数据库
        verify(valueOperations, times(1)).get("dish:" + dishId);
        verify(dishMapper, times(1)).selectById(dishId);
        
        // 验证：不应该写入缓存
        verify(valueOperations, never()).set(anyString(), any(), anyLong(), any());
    }
    
    /**
     * 提供菜品名称的生成器
     */
    @Provide
    Arbitrary<String> dishNames() {
        return Arbitraries.of(
            "宫保鸡丁", "鱼香肉丝", "麻婆豆腐", "回锅肉", "水煮鱼",
            "糖醋里脊", "红烧肉", "东坡肉", "佛跳墙", "清蒸鲈鱼",
            "小炒肉", "酸菜鱼", "毛血旺", "辣子鸡", "口水鸡"
        );
    }
    
    /**
     * 提供价格的生成器
     */
    @Provide
    Arbitrary<BigDecimal> prices() {
        return Arbitraries.bigDecimals()
            .between(BigDecimal.valueOf(10.00), BigDecimal.valueOf(200.00))
            .ofScale(2);
    }
    
    /**
     * 创建测试菜品
     */
    private Dish createTestDish(Long id, String name, BigDecimal price) {
        Dish dish = new Dish();
        dish.setId(id);
        dish.setName(name);
        dish.setPrice(price);
        dish.setCategoryId(1L);
        dish.setDescription("测试菜品");
        dish.setStatus(1);
        dish.setCreateTime(LocalDateTime.now());
        dish.setUpdateTime(LocalDateTime.now());
        return dish;
    }
    
    /**
     * 清空本地缓存
     * 
     * 使用反射访问私有字段来清空缓存
     */
    private void clearLocalCache() {
        try {
            Field localCacheField = DishCacheService.class.getDeclaredField("localCache");
            localCacheField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Cache<Long, Dish> localCache = (Cache<Long, Dish>) localCacheField.get(dishCacheService);
            if (localCache != null) {
                localCache.invalidateAll();
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to clear local cache", e);
        }
    }
}
