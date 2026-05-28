package com.qiyun.dish.integration;

import com.qiyun.dish.DishApplication;
import com.qiyun.dish.entity.Dish;
import com.qiyun.dish.mapper.DishMapper;
import com.qiyun.dish.service.DishCacheService;
import com.qiyun.dish.service.DishService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 菜品服务集成测试
 * 
 * 测试覆盖：
 * - 菜品服务与数据库的集成
 * - 菜品服务与Redis缓存的集成
 * - 多级缓存的集成
 * 
 * @author qiyun
 * @since 2026-02-09
 */
@SpringBootTest(classes = DishApplication.class)
@ActiveProfiles("test")
@Transactional
@DisplayName("菜品服务集成测试")
class DishServiceIntegrationTest {
    
    @Autowired
    private DishService dishService;
    
    @Autowired
    private DishCacheService dishCacheService;
    
    @Autowired
    private DishMapper dishMapper;
    
    @Autowired
    private RedisTemplate<String, Dish> redisTemplate;
    
    private Dish testDish;
    
    @BeforeEach
    void setUp() {
        // 清理测试数据
        dishMapper.delete(null);
        
        // 清理Redis缓存
        redisTemplate.getConnectionFactory().getConnection().flushDb();
        
        testDish = new Dish();
        testDish.setName("宫保鸡丁");
        testDish.setPrice(new BigDecimal("38.00"));
        testDish.setCategoryId(1L);
        testDish.setDescription("经典川菜");
        testDish.setStatus(1);
    }
    
    @Test
    @DisplayName("集成测试 - 创建菜品并保存到数据库")
    void testCreateDishIntegration() {
        // When
        boolean saved = dishService.save(testDish);
        
        // Then
        assertTrue(saved);
        assertNotNull(testDish.getId());
        
        // 验证数据库中存在该菜品
        Dish dbDish = dishMapper.selectById(testDish.getId());
        assertNotNull(dbDish);
        assertEquals(testDish.getName(), dbDish.getName());
        assertEquals(testDish.getPrice(), dbDish.getPrice());
    }
    
    @Test
    @DisplayName("集成测试 - 查询菜品")
    void testGetDishIntegration() {
        // Given - 先创建菜品
        dishService.save(testDish);
        
        // When
        Dish foundDish = dishService.getById(testDish.getId());
        
        // Then
        assertNotNull(foundDish);
        assertEquals(testDish.getId(), foundDish.getId());
        assertEquals(testDish.getName(), foundDish.getName());
    }
    
    @Test
    @DisplayName("集成测试 - 更新菜品")
    void testUpdateDishIntegration() {
        // Given
        dishService.save(testDish);
        
        // When - 更新菜品价格
        testDish.setPrice(new BigDecimal("42.00"));
        boolean updated = dishService.updateById(testDish);
        
        // Then
        assertTrue(updated);
        
        // 验证数据库中的价格已更新
        Dish dbDish = dishMapper.selectById(testDish.getId());
        assertEquals(new BigDecimal("42.00"), dbDish.getPrice());
    }
    
    @Test
    @DisplayName("集成测试 - 删除菜品")
    void testDeleteDishIntegration() {
        // Given
        dishService.save(testDish);
        Long dishId = testDish.getId();
        
        // When
        boolean deleted = dishService.removeById(dishId);
        
        // Then
        assertTrue(deleted);
        
        // 验证数据库中已删除
        Dish dbDish = dishMapper.selectById(dishId);
        assertNull(dbDish);
    }
    
    @Test
    @DisplayName("集成测试 - 批量查询菜品")
    void testBatchQueryDishesIntegration() {
        // Given - 创建多个菜品
        Dish dish1 = new Dish();
        dish1.setName("宫保鸡丁");
        dish1.setPrice(new BigDecimal("38.00"));
        dish1.setCategoryId(1L);
        dish1.setStatus(1);
        dishService.save(dish1);
        
        Dish dish2 = new Dish();
        dish2.setName("鱼香肉丝");
        dish2.setPrice(new BigDecimal("32.00"));
        dish2.setCategoryId(1L);
        dish2.setStatus(1);
        dishService.save(dish2);
        
        List<Long> ids = Arrays.asList(dish1.getId(), dish2.getId());
        
        // When
        List<Dish> dishes = dishService.listByIds(ids);
        
        // Then
        assertNotNull(dishes);
        assertEquals(2, dishes.size());
    }
    
    @Test
    @DisplayName("集成测试 - 多级缓存查询（L1本地缓存）")
    void testMultiLevelCacheL1Integration() {
        // Given - 创建菜品
        dishService.save(testDish);
        Long dishId = testDish.getId();
        
        // When - 第一次查询（从数据库）
        Dish dish1 = dishCacheService.getDishById(dishId);
        assertNotNull(dish1);
        
        // When - 第二次查询（从本地缓存）
        Dish dish2 = dishCacheService.getDishById(dishId);
        assertNotNull(dish2);
        
        // Then - 两次查询结果相同
        assertEquals(dish1.getId(), dish2.getId());
        assertEquals(dish1.getName(), dish2.getName());
    }
    
    @Test
    @DisplayName("集成测试 - 多级缓存查询（L2 Redis缓存）")
    void testMultiLevelCacheL2Integration() {
        // Given - 创建菜品并写入Redis
        dishService.save(testDish);
        Long dishId = testDish.getId();
        
        String key = "dish:" + dishId;
        redisTemplate.opsForValue().set(key, testDish);
        
        // When - 查询（从Redis缓存）
        Dish cachedDish = dishCacheService.getDishById(dishId);
        
        // Then
        assertNotNull(cachedDish);
        assertEquals(testDish.getId(), cachedDish.getId());
        assertEquals(testDish.getName(), cachedDish.getName());
    }
    
    @Test
    @DisplayName("集成测试 - 缓存更新同步")
    void testCacheUpdateSyncIntegration() {
        // Given - 创建菜品
        dishService.save(testDish);
        Long dishId = testDish.getId();
        
        // 先查询一次，写入缓存
        dishCacheService.getDishById(dishId);
        
        // When - 更新菜品
        testDish.setPrice(new BigDecimal("45.00"));
        dishCacheService.updateDish(testDish);
        
        // Then - 缓存应该被清除，查询应该返回新价格
        Dish updatedDish = dishCacheService.getDishById(dishId);
        assertEquals(new BigDecimal("45.00"), updatedDish.getPrice());
    }
    
    @Test
    @DisplayName("集成测试 - 根据分类查询菜品")
    void testGetDishesByCategoryIntegration() {
        // Given - 创建同一分类的多个菜品
        Dish dish1 = new Dish();
        dish1.setName("宫保鸡丁");
        dish1.setPrice(new BigDecimal("38.00"));
        dish1.setCategoryId(1L);
        dish1.setStatus(1);
        dishService.save(dish1);
        
        Dish dish2 = new Dish();
        dish2.setName("鱼香肉丝");
        dish2.setPrice(new BigDecimal("32.00"));
        dish2.setCategoryId(1L);
        dish2.setStatus(1);
        dishService.save(dish2);
        
        Dish dish3 = new Dish();
        dish3.setName("麻婆豆腐");
        dish3.setPrice(new BigDecimal("28.00"));
        dish3.setCategoryId(2L);
        dish3.setStatus(1);
        dishService.save(dish3);
        
        // When
        List<Dish> category1Dishes = dishMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Dish>()
                .eq(Dish::getCategoryId, 1L)
        );
        
        // Then
        assertNotNull(category1Dishes);
        assertEquals(2, category1Dishes.size());
        assertTrue(category1Dishes.stream().allMatch(d -> d.getCategoryId().equals(1L)));
    }
}
