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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 缓存同步更新属性测试
 * 
 * 使用jqwik进行属性测试，验证数据更新时本地缓存和Redis缓存都被删除
 * 
 * **Validates: Requirements 7.4**
 * 
 * @author qiyun
 * @since 2026-02-08
 */
class CacheSyncUpdatePropertiesTest {
    
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
     * Property 30: 缓存同步更新
     * 
     * For any 数据更新操作，应该同时更新或删除本地缓存和Redis缓存中的对应数据
     * 
     * 验证策略：
     * 1. 数据更新时，应该先更新数据库
     * 2. 然后删除本地缓存（L1）
     * 3. 同时删除Redis缓存（L2）
     * 4. 更新后再次查询，应该从数据库重新加载最新数据
     * 
     * **Validates: Requirements 7.4**
     */
    @Property(tries = 100)
    @Label("Feature: restaurant-tech-stack-upgrade, Property 30: 缓存同步更新")
    void cacheSynchronousUpdate(
            @ForAll @Positive Long dishId,
            @ForAll("dishNames") String originalName,
            @ForAll("dishNames") String updatedName,
            @ForAll("prices") BigDecimal originalPrice,
            @ForAll("prices") BigDecimal updatedPrice) {
        
        // 确保更新前后的数据不同
        Assume.that(!originalName.equals(updatedName) || !originalPrice.equals(updatedPrice));
        
        // 准备原始数据
        Dish originalDish = createTestDish(dishId, originalName, originalPrice);
        
        // 将测试ID添加到布隆过滤器，避免被拦截
        dishCacheService.getBloomFilter().put(dishId);
        
        // 清空本地缓存
        clearLocalCache();
        
        // 步骤1：先查询一次，让数据进入本地缓存和Redis缓存
        when(valueOperations.get("dish:" + dishId)).thenReturn(originalDish);
        Dish cachedDish = dishCacheService.getDishById(dishId);
        assertNotNull(cachedDish, "数据应该被缓存");
        assertEquals(originalName, cachedDish.getName());
        assertEquals(originalPrice, cachedDish.getPrice());
        
        // 验证数据已在本地缓存中（再次查询不会查Redis）
        clearInvocations(valueOperations, dishMapper);
        Dish localCachedDish = dishCacheService.getDishById(dishId);
        assertNotNull(localCachedDish, "数据应该在本地缓存中");
        verify(valueOperations, never()).get(anyString());
        verify(dishMapper, never()).selectById(any());
        
        // 步骤2：更新数据
        Dish updatedDish = createTestDish(dishId, updatedName, updatedPrice);
        clearInvocations(valueOperations, dishMapper, redisTemplate);
        
        dishCacheService.updateDish(updatedDish);
        
        // 验证：应该更新数据库
        verify(dishMapper, times(1)).updateById(updatedDish);
        
        // 验证：应该删除Redis缓存
        verify(redisTemplate, times(1)).delete("dish:" + dishId);
        
        // 步骤3：验证本地缓存被清除（再次查询会查Redis或数据库）
        clearInvocations(valueOperations, dishMapper, redisTemplate);
        
        // 模拟从数据库返回更新后的数据
        when(valueOperations.get("dish:" + dishId)).thenReturn(null);
        when(dishMapper.selectById(dishId)).thenReturn(updatedDish);
        
        Dish reloadedDish = dishCacheService.getDishById(dishId);
        
        // 验证：应该从数据库重新加载数据（因为缓存已被清除）
        assertNotNull(reloadedDish, "应该重新加载数据");
        assertEquals(updatedName, reloadedDish.getName(), "应该返回更新后的名称");
        assertEquals(updatedPrice, reloadedDish.getPrice(), "应该返回更新后的价格");
        
        // 验证：应该查询了Redis和数据库（因为缓存已被清除）
        verify(valueOperations, times(1)).get("dish:" + dishId);
        verify(dishMapper, times(1)).selectById(dishId);
    }
    
    /**
     * Property 30.1: 更新时必须删除本地缓存
     * 
     * For any 数据更新操作，本地缓存中的对应数据必须被删除
     * 
     * **Validates: Requirements 7.4**
     */
    @Property(tries = 100)
    @Label("Feature: restaurant-tech-stack-upgrade, Property 30: 缓存同步更新")
    void updateMustInvalidateLocalCache(
            @ForAll @Positive Long dishId,
            @ForAll("dishNames") String originalName,
            @ForAll("dishNames") String updatedName,
            @ForAll("prices") BigDecimal price) {
        
        // 确保名称不同
        Assume.that(!originalName.equals(updatedName));
        
        // 准备原始数据
        Dish originalDish = createTestDish(dishId, originalName, price);
        
        // 将测试ID添加到布隆过滤器，避免被拦截
        dishCacheService.getBloomFilter().put(dishId);
        
        // 清空本地缓存
        clearLocalCache();
        
        // 先查询一次，让数据进入本地缓存
        when(valueOperations.get("dish:" + dishId)).thenReturn(originalDish);
        Dish cachedDish = dishCacheService.getDishById(dishId);
        assertNotNull(cachedDish);
        
        // 验证数据在本地缓存中
        clearInvocations(valueOperations, dishMapper);
        Dish localCachedDish = dishCacheService.getDishById(dishId);
        assertNotNull(localCachedDish);
        assertEquals(originalName, localCachedDish.getName());
        verify(valueOperations, never()).get(anyString()); // 没有查Redis，说明在本地缓存中
        
        // 更新数据
        Dish updatedDish = createTestDish(dishId, updatedName, price);
        dishCacheService.updateDish(updatedDish);
        
        // 验证本地缓存被清除：再次查询会查Redis或数据库
        clearInvocations(valueOperations, dishMapper);
        when(valueOperations.get("dish:" + dishId)).thenReturn(updatedDish);
        
        Dish reloadedDish = dishCacheService.getDishById(dishId);
        
        // 验证：应该查询了Redis（说明本地缓存已被清除）
        assertNotNull(reloadedDish);
        assertEquals(updatedName, reloadedDish.getName());
        verify(valueOperations, times(1)).get("dish:" + dishId);
    }
    
    /**
     * Property 30.2: 更新时必须删除Redis缓存
     * 
     * For any 数据更新操作，Redis缓存中的对应数据必须被删除
     * 
     * **Validates: Requirements 7.4**
     */
    @Property(tries = 100)
    @Label("Feature: restaurant-tech-stack-upgrade, Property 30: 缓存同步更新")
    void updateMustInvalidateRedisCache(
            @ForAll @Positive Long dishId,
            @ForAll("dishNames") String name,
            @ForAll("prices") BigDecimal price) {
        
        // 准备测试数据
        Dish dish = createTestDish(dishId, name, price);
        
        // 清空本地缓存
        clearLocalCache();
        
        // 更新数据
        dishCacheService.updateDish(dish);
        
        // 验证：应该删除Redis缓存
        verify(redisTemplate, times(1)).delete("dish:" + dishId);
    }
    
    /**
     * Property 30.3: 更新时必须先更新数据库
     * 
     * For any 数据更新操作，必须先更新数据库，然后再删除缓存
     * 
     * **Validates: Requirements 7.4**
     */
    @Property(tries = 100)
    @Label("Feature: restaurant-tech-stack-upgrade, Property 30: 缓存同步更新")
    void updateMustUpdateDatabaseFirst(
            @ForAll @Positive Long dishId,
            @ForAll("dishNames") String name,
            @ForAll("prices") BigDecimal price) {
        
        // 准备测试数据
        Dish dish = createTestDish(dishId, name, price);
        
        // 清空本地缓存
        clearLocalCache();
        
        // 更新数据
        dishCacheService.updateDish(dish);
        
        // 验证：应该更新数据库
        verify(dishMapper, times(1)).updateById(dish);
        
        // 验证：应该删除Redis缓存
        verify(redisTemplate, times(1)).delete("dish:" + dishId);
    }
    
    /**
     * Property 30.4: 缓存删除后数据一致性
     * 
     * For any 数据更新操作，缓存删除后再次查询应该返回最新的数据
     * 
     * **Validates: Requirements 7.4**
     */
    @Property(tries = 100)
    @Label("Feature: restaurant-tech-stack-upgrade, Property 30: 缓存同步更新")
    void cacheInvalidationEnsuresDataConsistency(
            @ForAll @Positive Long dishId,
            @ForAll("dishNames") String originalName,
            @ForAll("dishNames") String updatedName,
            @ForAll("prices") BigDecimal originalPrice,
            @ForAll("prices") BigDecimal updatedPrice) {
        
        // 确保更新前后的数据不同
        Assume.that(!originalName.equals(updatedName) || !originalPrice.equals(updatedPrice));
        
        // 准备原始数据和更新后的数据
        Dish originalDish = createTestDish(dishId, originalName, originalPrice);
        Dish updatedDish = createTestDish(dishId, updatedName, updatedPrice);
        
        // 将测试ID添加到布隆过滤器，避免被拦截
        dishCacheService.getBloomFilter().put(dishId);
        
        // 清空本地缓存
        clearLocalCache();
        
        // 先查询一次，让数据进入缓存
        when(valueOperations.get("dish:" + dishId)).thenReturn(originalDish);
        Dish cachedDish = dishCacheService.getDishById(dishId);
        assertNotNull(cachedDish);
        assertEquals(originalName, cachedDish.getName());
        assertEquals(originalPrice, cachedDish.getPrice());
        
        // 更新数据
        dishCacheService.updateDish(updatedDish);
        
        // 模拟从数据库返回更新后的数据
        clearInvocations(valueOperations, dishMapper);
        when(valueOperations.get("dish:" + dishId)).thenReturn(null);
        when(dishMapper.selectById(dishId)).thenReturn(updatedDish);
        
        // 再次查询
        Dish reloadedDish = dishCacheService.getDishById(dishId);
        
        // 验证：应该返回更新后的数据
        assertNotNull(reloadedDish, "应该返回数据");
        assertEquals(updatedName, reloadedDish.getName(), "应该返回更新后的名称");
        assertEquals(updatedPrice, reloadedDish.getPrice(), "应该返回更新后的价格");
        
        // 只有当数据确实不同时才验证不相等
        if (!originalName.equals(updatedName)) {
            assertNotEquals(originalName, reloadedDish.getName(), "不应该返回旧的名称");
        }
        if (!originalPrice.equals(updatedPrice)) {
            assertNotEquals(originalPrice, reloadedDish.getPrice(), "不应该返回旧的价格");
        }
    }
    
    /**
     * Property 30.5: 多次更新的缓存一致性
     * 
     * For any 连续多次更新操作，每次更新后缓存都应该被清除，保证数据一致性
     * 
     * **Validates: Requirements 7.4**
     */
    @Property(tries = 100)
    @Label("Feature: restaurant-tech-stack-upgrade, Property 30: 缓存同步更新")
    void multipleUpdatesKeepCacheConsistent(
            @ForAll @Positive Long dishId,
            @ForAll("dishNames") String name1,
            @ForAll("dishNames") String name2,
            @ForAll("dishNames") String name3,
            @ForAll("prices") BigDecimal price) {
        
        // 确保三个名称不同
        Assume.that(!name1.equals(name2) && !name2.equals(name3) && !name1.equals(name3));
        
        // 清空本地缓存
        clearLocalCache();
        
        // 第一次更新
        Dish dish1 = createTestDish(dishId, name1, price);
        dishCacheService.updateDish(dish1);
        
        // 验证第一次更新
        verify(dishMapper, times(1)).updateById(dish1);
        verify(redisTemplate, times(1)).delete("dish:" + dishId);
        
        // 第二次更新
        clearInvocations(dishMapper, redisTemplate);
        Dish dish2 = createTestDish(dishId, name2, price);
        dishCacheService.updateDish(dish2);
        
        // 验证第二次更新
        verify(dishMapper, times(1)).updateById(dish2);
        verify(redisTemplate, times(1)).delete("dish:" + dishId);
        
        // 第三次更新
        clearInvocations(dishMapper, redisTemplate);
        Dish dish3 = createTestDish(dishId, name3, price);
        dishCacheService.updateDish(dish3);
        
        // 验证第三次更新
        verify(dishMapper, times(1)).updateById(dish3);
        verify(redisTemplate, times(1)).delete("dish:" + dishId);
        
        // 验证最终查询返回最新数据
        clearInvocations(valueOperations, dishMapper);
        when(valueOperations.get("dish:" + dishId)).thenReturn(null);
        when(dishMapper.selectById(dishId)).thenReturn(dish3);
        
        Dish finalDish = dishCacheService.getDishById(dishId);
        assertNotNull(finalDish);
        assertEquals(name3, finalDish.getName(), "应该返回最后一次更新的名称");
    }
    
    /**
     * Property 30.6: null值更新不影响缓存
     * 
     * For any null值或null ID的更新操作，不应该执行任何缓存操作
     * 
     * **Validates: Requirements 7.4**
     */
    @Property(tries = 100)
    @Label("Feature: restaurant-tech-stack-upgrade, Property 30: 缓存同步更新")
    void nullUpdateDoesNotAffectCache(@ForAll("dishNames") String name) {
        
        // 清空本地缓存
        clearLocalCache();
        
        // 场景1：更新null对象
        dishCacheService.updateDish(null);
        
        // 验证：不应该执行任何操作
        verify(dishMapper, never()).updateById(any());
        verify(redisTemplate, never()).delete(anyString());
        
        // 场景2：更新ID为null的对象
        Dish dishWithNullId = new Dish();
        dishWithNullId.setName(name);
        dishWithNullId.setId(null);
        
        dishCacheService.updateDish(dishWithNullId);
        
        // 验证：不应该执行任何操作
        verify(dishMapper, never()).updateById(any());
        verify(redisTemplate, never()).delete(anyString());
    }
    
    /**
     * 提供菜品名称的生成器
     */
    @Provide
    Arbitrary<String> dishNames() {
        return Arbitraries.of(
            "宫保鸡丁", "鱼香肉丝", "麻婆豆腐", "回锅肉", "水煮鱼",
            "糖醋里脊", "红烧肉", "东坡肉", "佛跳墙", "清蒸鲈鱼",
            "小炒肉", "酸菜鱼", "毛血旺", "辣子鸡", "口水鸡",
            "夫妻肺片", "水煮牛肉", "干锅花菜", "剁椒鱼头", "蒜蓉西兰花"
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
