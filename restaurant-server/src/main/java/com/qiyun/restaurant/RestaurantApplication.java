package com.qiyun.restaurant;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 七云菜馆应用启动类
 */
@SpringBootApplication
@EnableTransactionManagement  // 开启事务管理
@EnableCaching  // 开启缓存
@MapperScan("com.qiyun.restaurant.mapper")
public class RestaurantApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestaurantApplication.class, args);
        System.out.println("========================================");
        System.out.println("七云菜馆后端服务启动成功！");
        System.out.println("接口文档地址: http://localhost:8080/doc.html");
        System.out.println("========================================");
    }
}
