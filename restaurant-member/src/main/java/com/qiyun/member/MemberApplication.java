package com.qiyun.member;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 会员服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("com.qiyun.member.mapper")
public class MemberApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(MemberApplication.class, args);
    }
}
