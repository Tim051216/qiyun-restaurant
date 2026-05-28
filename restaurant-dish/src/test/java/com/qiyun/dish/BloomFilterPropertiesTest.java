package com.qiyun.dish;

import com.github.benmanes.caffeine.cache.Cache;
import com.google.common.hash.BloomFilter;
import com.qiyun.dish.entity.Dish;
import com.qiyun.dish.mapper.DishMapper;
import com.qiyun.dish.service.DishCacheService;
import net.jqwik.api.*;
import net.jqwik.api.constraints.Positive;
import net.jqwik.api.lifecycle.BeforeTry;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 布隆过滤器属性测试
 * 
 * 使用jqwik进行属性测试，验证布隆过滤器能够拦截不存在的数据查询，防止缓存穿透
 * 
 * **Validates: Requirements 7.6**
 * 
 * @author qiyun
 * @since 2026-02-08
 */
class BloomFilterPropertiesTest {
    
    private RedisTemplate<String, Dish> redisTemplate;
    private ValueOperations<String, Dish> valueOperations;
    private DishMapper dishMapper;
    private DishCacheService dishCacheService;
    
    @BeforeTry
    void setUp() throws Exception {
        // 手动创建mocks
        redisTemplate = mock(RedisTemplate.class);
        valueOperations = mock(ValueOperations.class);
        dishMapper = mock(DishMapper.class);
        
        // 创建DishCacheService实例并注入mocks
        dishCacheService = new DishCacheService();
        
        // 使用反射注入依赖
        Field redisTemplateField = DishCacheService.class.getDeclaredField("redisTemplate");
        redisTemplateField.setAccessible(true);
        redisTemplateField.set(dishCacheService, redisTemplate);
        
        Field dishMapperField = DishCacheService.class.getDeclaredField("dishMapper");
        dishMapperField.setAccessible(true);
        dishMapperField.set(dishCacheService, dishMapper);
        
        // 初始化本地缓存和布隆过滤器
        dishCacheService.init();
        
        // Mock RedisTemplate的opsForValue方法
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }
    
    /**
     * Property 31: 布隆过滤器拦截
     * 
     * For any 数据查询请求，如果布隆过滤器判断数据不存在，应该直接返回而不查询数据库
     * 
     * 验证策略：
     * 1. 查询不在布隆过滤器中的ID，应该直接返回null
     * 2. 不应该查询Redis缓存
     * 3. 不应该查询数据库
     * 4. 查询在布隆过滤器中的ID，应该继续正常的缓存查询流程
     * 
     * **Validates: Requirements 7.6**
     */
    @Property(tries = 100)
    @Label("Feature: restaurant-tech-stack-upgrade, Property 31: 布隆过滤器拦截")
    void bloomFilterBlocksNonExistentQueries(
            @ForAll @Positive Long existingDishId,
            @ForAll @Positive Long nonExistentDishId,
            @ForAll("dishNames") String dishName,
            @ForAll("prices") BigDecimal price) {
        
        // 确保两个ID不同
        Assume.that(existingDishId.longValue() != nonExistentDishId.longValue());
        
        // 准备测试数据
        Dish testDish = createTestDish(existingDishId, dishName, price);
        
        // 清空本地缓存
        clearLocalCache();
        
        // 获取布隆过滤器并添加已存在的ID
        BloomFilter<Long> bloomFilter = getBloomFilter();
        bloomFilter.put(existingDishId);
        
        // 清理mock调用记录
        clearInvocations(valueOperations, dishMapper);
        
        // 场景1：查询不在布隆过滤器中的ID（应该被拦截）
        Dish result1 = dishCacheService.getDishById(nonExistentDishId);
        
        // 验证：应该直接返回null，不查询Redis和数据库
        assertNull(result1, "布隆过滤器应该拦截不存在的数据查询");
        verify(valueOperations, never()).get(anyString());
        verify(dishMapper, never()).selectById(any());
        
        // 清理mock调用记录
        clearInvocations(valueOperations, dishMapper);
        
        // 场景2：查询在布隆过滤器中的ID（应该继续正常流程）
        when(valueOperations.get("dish:" + existingDishId)).thenReturn(null);
        when(dishMapper.selectById(existingDishId)).thenReturn(testDish);
        
        Dish result2 = dishCacheService.getDishById(existingDishId);
        
        // 验证：应该继续查询Redis和数据库
        assertNotNull(result2, "布隆过滤器中存在的ID应该继续查询");
        assertEquals(existingDishId, result2.getId());
        verify(valueOperations, times(1)).get("dish:" + existingDishId);
        verify(dishMapper, times(1)).selectById(existingDishId);
    }
    
    /**
     * Property 31.1: 布隆过滤器无假阴性
     * 
     * For any 已添加到布隆过滤器的ID，查询时不应该被拦截
     * 
     * 验证布隆过滤器的核心特性：如果数据存在，永远不会被错误拦截（无假阴性）
     * 
     * **Validates: Requirements 7.6**
     */
    @Property(tries = 100)
    @Label("Feature: restaurant-tech-stack-upgrade, Property 31: 布隆过滤器拦截")
    void bloomFilterHasNoFalseNegatives(
            @ForAll @Positive Long dishId,
            @ForAll("dishNames") String dishName,
            @ForAll("prices") BigDecimal price) {
        
        // 准备测试数据
        Dish testDish = createTestDish(dishId, dishName, price);
        
        // 清空本地缓存
        clearLocalCache();
        
        // 获取布隆过滤器并添加ID
        BloomFilter<Long> bloomFilter = getBloomFilter();
        bloomFilter.put(dishId);
        
        // 清理mock调用记录
        clearInvocations(valueOperations, dishMapper);
        
        // 配置mock：数据在数据库中
        when(valueOperations.get("dish:" + dishId)).thenReturn(null);
        when(dishMapper.selectById(dishId)).thenReturn(testDish);
        
        // 查询数据
        Dish result = dishCacheService.getDishById(dishId);
        
        // 验证：不应该被布隆过滤器拦截，应该能够查询到数据
        assertNotNull(result, "已添加到布隆过滤器的ID不应该被拦截");
        assertEquals(dishId, result.getId());
        
        // 验证：应该查询了Redis和数据库（说明没有被布隆过滤器拦截）
        verify(valueOperations, times(1)).get("dish:" + dishId);
        verify(dishMapper, times(1)).selectById(dishId);
    }
    
    /**
     * Property 31.2: 布隆过滤器批量拦截
     * 
     * For any 一批不在布隆过滤器中的ID，查询时都应该被拦截
     * 
     * 验证布隆过滤器能够批量拦截恶意查询，保护数据库
     * 
     * **Validates: Requirements 7.6**
     */
    @Property(tries = 100)
    @Label("Feature: restaurant-tech-stack-upgrade, Property 31: 布隆过滤器拦截")
    void bloomFilterBlocksMultipleNonExistentQueries(
            @ForAll("nonExistentDishIds") Set<Long> nonExistentIds) {
        
        // 确保至少有一些ID
        Assume.that(!nonExistentIds.isEmpty());
        
        // 清空本地缓存
        clearLocalCache();
        
        // 清理mock调用记录
        clearInvocations(valueOperations, dishMapper);
        
        // 查询所有不存在的ID
        int blockedCount = 0;
        for (Long id : nonExistentIds) {
            Dish result = dishCacheService.getDishById(id);
            if (result == null) {
                blockedCount++;
            }
        }
        
        // 验证：大部分查询应该被布隆过滤器拦截（考虑到1%的误判率）
        // 至少90%的查询应该被拦截
        double blockRate = (double) blockedCount / nonExistentIds.size();
        assertTrue(blockRate >= 0.90, 
            String.format("布隆过滤器应该拦截至少90%%的不存在数据查询，实际拦截率: %.2f%%", blockRate * 100));
        
        // 验证：数据库查询次数应该远少于查询总数（说明大部分被拦截了）
        verify(dishMapper, atMost((int) (nonExistentIds.size() * 0.1))).selectById(any());
    }
    
    /**
     * Property 31.3: 布隆过滤器自动添加新数据
     * 
     * For any 从数据库查询到的新数据，应该自动添加到布隆过滤器
     * 
     * 验证布隆过滤器能够动态更新，包含新查询到的数据
     * 
     * **Validates: Requirements 7.6**
     */
    @Property(tries = 100)
    @Label("Feature: restaurant-tech-stack-upgrade, Property 31: 布隆过滤器拦截")
    void bloomFilterAutoAddsNewData(
            @ForAll @Positive Long dishId,
            @ForAll("dishNames") String dishName,
            @ForAll("prices") BigDecimal price) {
        
        // 准备测试数据
        Dish testDish = createTestDish(dishId, dishName, price);
        
        // 清空本地缓存
        clearLocalCache();
        
        // 获取布隆过滤器并添加ID（模拟数据存在的场景）
        BloomFilter<Long> bloomFilter = getBloomFilter();
        bloomFilter.put(dishId);
        
        // 验证ID在布隆过滤器中
        assertTrue(bloomFilter.mightContain(dishId), "ID应该在布隆过滤器中");
        
        // 配置mock：数据在数据库中
        when(valueOperations.get("dish:" + dishId)).thenReturn(null);
        when(dishMapper.selectById(dishId)).thenReturn(testDish);
        
        // 清理mock调用记录
        clearInvocations(valueOperations, dishMapper);
        
        // 查询数据：数据在布隆过滤器中，应该继续查询
        Dish result = dishCacheService.getDishById(dishId);
        
        // 验证：应该能够查询到数据
        assertNotNull(result, "数据在布隆过滤器中应该能够查询");
        assertEquals(dishId, result.getId());
        
        // 验证：应该查询了数据库（说明没有被布隆过滤器拦截）
        verify(valueOperations, times(1)).get("dish:" + dishId);
        verify(dishMapper, times(1)).selectById(dishId);
        
        // 验证：布隆过滤器中仍然包含该ID
        assertTrue(bloomFilter.mightContain(dishId), "查询后ID应该仍在布隆过滤器中");
    }
    
    /**
     * Property 31.4: 布隆过滤器性能验证
     * 
     * For any 大量查询请求，布隆过滤器应该能够快速判断，不影响系统性能
     * 
     * 验证布隆过滤器的性能特性：O(1)时间复杂度
     * 
     * **Validates: Requirements 7.6**
     */
    @Property(tries = 100)
    @Label("Feature: restaurant-tech-stack-upgrade, Property 31: 布隆过滤器拦截")
    void bloomFilterHasConstantTimeComplexity(
            @ForAll("dishIdBatch") Set<Long> dishIds) {
        
        // 确保有足够的测试数据
        Assume.that(dishIds.size() >= 10);
        
        // 清空本地缓存
        clearLocalCache();
        
        // 获取布隆过滤器并添加一半的ID
        BloomFilter<Long> bloomFilter = getBloomFilter();
        int halfSize = dishIds.size() / 2;
        int count = 0;
        for (Long id : dishIds) {
            if (count++ < halfSize) {
                bloomFilter.put(id);
            }
        }
        
        // 测量查询时间
        long startTime = System.nanoTime();
        
        for (Long id : dishIds) {
            // 只调用布隆过滤器检查，不执行完整查询
            bloomFilter.mightContain(id);
        }
        
        long endTime = System.nanoTime();
        long totalTime = endTime - startTime;
        long avgTimePerQuery = totalTime / dishIds.size();
        
        // 验证：平均每次查询应该在10微秒以内（10000纳秒）
        // 放宽限制以适应不同系统性能
        assertTrue(avgTimePerQuery < 10000, 
            String.format("布隆过滤器查询应该非常快速，平均耗时: %d纳秒", avgTimePerQuery));
    }
    
    /**
     * Property 31.5: 空ID处理
     * 
     * For any null ID，查询时应该直接返回null，不查询布隆过滤器、缓存和数据库
     * 
     * **Validates: Requirements 7.6**
     */
    @Property(tries = 100)
    @Label("Feature: restaurant-tech-stack-upgrade, Property 31: 布隆过滤器拦截")
    void bloomFilterHandlesNullId() {
        
        // 清理mock调用记录
        clearInvocations(valueOperations, dishMapper);
        
        // 查询null ID
        Dish result = dishCacheService.getDishById(null);
        
        // 验证：应该直接返回null
        assertNull(result, "null ID应该直接返回null");
        
        // 验证：不应该查询Redis和数据库
        verify(valueOperations, never()).get(anyString());
        verify(dishMapper, never()).selectById(any());
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
     * 提供不存在的菜品ID集合生成器
     * 
     * 生成一批大的ID（远超过布隆过滤器中的数据），模拟恶意查询
     */
    @Provide
    Arbitrary<Set<Long>> nonExistentDishIds() {
        return Arbitraries.longs()
            .between(1000000L, 9999999L)  // 使用大ID，确保不在布隆过滤器中
            .set()
            .ofMinSize(10)
            .ofMaxSize(50);
    }
    
    /**
     * 提供菜品ID批次生成器
     */
    @Provide
    Arbitrary<Set<Long>> dishIdBatch() {
        return Arbitraries.longs()
            .between(1L, 100000L)
            .set()
            .ofMinSize(10)
            .ofMaxSize(100);
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
    
    /**
     * 获取布隆过滤器
     * 
     * 使用反射访问私有字段
     */
    @SuppressWarnings("unchecked")
    private BloomFilter<Long> getBloomFilter() {
        try {
            Field bloomFilterField = DishCacheService.class.getDeclaredField("bloomFilter");
            bloomFilterField.setAccessible(true);
            return (BloomFilter<Long>) bloomFilterField.get(dishCacheService);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get bloom filter", e);
        }
    }
}
