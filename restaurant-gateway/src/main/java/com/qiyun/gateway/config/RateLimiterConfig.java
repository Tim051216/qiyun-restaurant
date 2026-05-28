package com.qiyun.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import reactor.core.publisher.Mono;

/**
 * 限流配置类
 * 
 * 配置限流Key解析器，用于识别限流对象
 */
@Configuration
public class RateLimiterConfig {

    /**
     * 基于IP的限流Key解析器
     * 
     * 根据请求的IP地址进行限流
     */
    @Bean
    @Primary
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(
            exchange.getRequest()
                .getRemoteAddress()
                .getAddress()
                .getHostAddress()
        );
    }

    /**
     * 基于用户的限流Key解析器
     * 
     * 根据用户ID进行限流（从请求头或参数中获取）
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> Mono.just(
            exchange.getRequest()
                .getQueryParams()
                .getFirst("userId") != null 
                    ? exchange.getRequest().getQueryParams().getFirst("userId")
                    : "anonymous"
        );
    }

    /**
     * 基于API路径的限流Key解析器
     * 
     * 根据请求的API路径进行限流
     */
    @Bean
    public KeyResolver apiKeyResolver() {
        return exchange -> Mono.just(
            exchange.getRequest().getPath().value()
        );
    }
}
