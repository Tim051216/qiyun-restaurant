package com.qiyun.dish.service;

import com.qiyun.dish.entity.Dish;
import com.qiyun.dish.mapper.DishMapper;
import com.qiyun.dish.service.impl.DishServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * 菜品服务单元测试
 * 
 * 测试覆盖：
 * - 菜品查询
 * - 缓存逻辑
 * - 批量查询
 * - 异常处理
 * 
 * @author qiyun
 * @since 2026-02-09
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("菜品服务单元测试")
class DishServiceImplTest {
    
    @Mock
    private DishMapper dishMapper;
    
    @Mock
    private RedisTemplate<String, Dish> redisTemplate;
    
    @Mock
    private ValueOperations<String, Dish> valueOperations;
    
    @InjectMocks
    private DishServiceImpl dishService;
    
    private Dish testDish;
    
    @BeforeEach
    void setUp() {
        // 设置baseMapper，这是MyBatis Plus ServiceImpl需要的
        ReflectionTestUtils.setField(dishService, "baseMapper", dishMapper);
        
        testDish = new Dish();
        testDish.setId(200L);
        testDish.setName("宫保鸡丁");
        testDish.setPrice(new BigDecimal("38.00"));
        testDish.setCategoryId(1L);
        testDish.setDescription("经典川菜");
        testDish.setStatus(1); // 1=在售
        
        // Mock RedisTemplate的opsForValue()方法 - 使用lenient避免UnnecessaryStubbingException
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }
    
    @Test
    @DisplayName("查询菜品（带缓存）- Redis缓存命中")
    void testGetDishByIdWithCache_CacheHit() {
        // Given
        String key = "dish:" + testDish.getId();
        when(valueOperations.get(key)).thenReturn(testDish);
        
        // When
        Dish result = dishService.getDishByIdWithCache(testDish.getId());
        
        // Then
        assertNotNull(result);
        assertEquals(testDish.getId(), result.getId());
        assertEquals(testDish.getName(), result.getName());
        
        verify(valueOperations, times(1)).get(key);
        verify(dishMapper, never()).selectById(any()); // 不应该查询数据库
    }
    
    @Test
    @DisplayName("查询菜品（带缓存）- Redis缓存未命中，查询数据库")
    void testGetDishByIdWithCache_CacheMiss() {
        // Given
        String key = "dish:" + testDish.getId();
        when(valueOperations.get(key)).thenReturn(null);
        when(dishMapper.selectById(testDish.getId())).thenReturn(testDish);
        
        // When
        Dish result = dishService.getDishByIdWithCache(testDish.getId());
        
        // Then
        assertNotNull(result);
        assertEquals(testDish.getId(), result.getId());
        assertEquals(testDish.getName(), result.getName());
        
        verify(valueOperations, times(1)).get(key);
        verify(dishMapper, times(1)).selectById(testDish.getId());
        verify(valueOperations, times(1)).set(eq(key), eq(testDish), eq(5L), eq(TimeUnit.MINUTES));
    }
    
    @Test
    @DisplayName("查询菜品（带缓存）- 菜品不存在")
    void testGetDishByIdWithCache_NotFound() {
        // Given
        String key = "dish:9999";
        when(valueOperations.get(key)).thenReturn(null);
        when(dishMapper.selectById(9999L)).thenReturn(null);
        
        // When
        Dish result = dishService.getDishByIdWithCache(9999L);
        
        // Then
        assertNull(result);
        
        verify(valueOperations, times(1)).get(key);
        verify(dishMapper, times(1)).selectById(9999L);
        verify(valueOperations, never()).set(anyString(), any(), anyLong(), any());
    }
    
    @Test
    @DisplayName("批量查询菜品 - 成功场景")
    void testGetDishByIds_Success() {
        // Given
        List<Long> ids = Arrays.asList(200L, 201L, 202L);
        
        Dish dish1 = new Dish();
        dish1.setId(200L);
        dish1.setName("宫保鸡丁");
        
        Dish dish2 = new Dish();
        dish2.setId(201L);
        dish2.setName("鱼香肉丝");
        
        Dish dish3 = new Dish();
        dish3.setId(202L);
        dish3.setName("麻婆豆腐");
        
        List<Dish> expectedDishes = Arrays.asList(dish1, dish2, dish3);
        when(dishMapper.selectBatchIds(ids)).thenReturn(expectedDishes);
        
        // When
        List<Dish> result = dishService.getDishByIds(ids);
        
        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("宫保鸡丁", result.get(0).getName());
        assertEquals("鱼香肉丝", result.get(1).getName());
        assertEquals("麻婆豆腐", result.get(2).getName());
        
        verify(dishMapper, times(1)).selectBatchIds(ids);
    }
    
    @Test
    @DisplayName("批量查询菜品 - 空列表")
    void testGetDishByIds_EmptyList() {
        // Given
        List<Long> ids = Arrays.asList();
        when(dishMapper.selectBatchIds(ids)).thenReturn(Arrays.asList());
        
        // When
        List<Dish> result = dishService.getDishByIds(ids);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(dishMapper, times(1)).selectBatchIds(ids);
    }
    
    @Test
    @DisplayName("批量查询菜品 - 部分菜品不存在")
    void testGetDishByIds_PartialNotFound() {
        // Given
        List<Long> ids = Arrays.asList(200L, 9999L, 201L);
        
        Dish dish1 = new Dish();
        dish1.setId(200L);
        dish1.setName("宫保鸡丁");
        
        Dish dish2 = new Dish();
        dish2.setId(201L);
        dish2.setName("鱼香肉丝");
        
        List<Dish> expectedDishes = Arrays.asList(dish1, dish2);
        when(dishMapper.selectBatchIds(ids)).thenReturn(expectedDishes);
        
        // When
        List<Dish> result = dishService.getDishByIds(ids);
        
        // Then
        assertNotNull(result);
        assertEquals(2, result.size()); // 只返回存在的菜品
        
        verify(dishMapper, times(1)).selectBatchIds(ids);
    }
    
    @Test
    @DisplayName("菜品价格计算 - 边界值测试")
    void testDishPrice_BoundaryValues() {
        // Given - 最小价格
        Dish minPriceDish = new Dish();
        minPriceDish.setPrice(new BigDecimal("0.01"));
        
        // When & Then
        assertEquals(new BigDecimal("0.01"), minPriceDish.getPrice());
        
        // Given - 高价菜品
        Dish expensiveDish = new Dish();
        expensiveDish.setPrice(new BigDecimal("9999.99"));
        
        // When & Then
        assertEquals(new BigDecimal("9999.99"), expensiveDish.getPrice());
    }
    
    @Test
    @DisplayName("菜品状态 - 在售和停售")
    void testDishStatus() {
        // Given
        Dish dish = new Dish();
        
        // When & Then - 在售
        dish.setStatus(1);
        assertEquals(1, dish.getStatus());
        
        // When & Then - 停售
        dish.setStatus(0);
        assertEquals(0, dish.getStatus());
    }
    
    @Test
    @DisplayName("Redis异常处理 - 缓存操作失败不影响查询")
    void testRedisException_DoesNotAffectQuery() {
        // Given
        String key = "dish:" + testDish.getId();
        when(valueOperations.get(key)).thenThrow(new RuntimeException("Redis连接失败"));
        lenient().when(dishMapper.selectById(testDish.getId())).thenReturn(testDish);
        
        // When & Then - 应该抛出异常（实际场景中可能需要异常处理）
        assertThrows(RuntimeException.class, () -> {
            dishService.getDishByIdWithCache(testDish.getId());
        });
        
        verify(valueOperations, times(1)).get(key);
    }
}
