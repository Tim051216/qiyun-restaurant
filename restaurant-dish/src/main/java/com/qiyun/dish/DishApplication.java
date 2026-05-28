package com.qiyun.dish;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 菜品服务启动类
 * 
 * @author qiyun
 * @since 2026-02-08
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableCaching
@MapperScan("com.qiyun.dish.mapper")
public class DishApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(DishApplication.class, args);
    }
}
