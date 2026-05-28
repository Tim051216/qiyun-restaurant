package com.qiyun.order.config;

import feign.Logger;
import feign.Request;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Feign配置
 * 
 * @author qiyun
 * @since 2026-02-08
 */
@Configuration
public class FeignConfig {
    
    /**
     * Feign日志级别
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }
    
    /**
     * Feign重试策略
     * 最大重试3次，初始间隔100ms，最大间隔1s
     */
    @Bean
    public Retryer feignRetryer() {
        return new Retryer.Default(100, TimeUnit.SECONDS.toMillis(1), 3);
    }
    
    /**
     * Feign超时配置
     */
    @Bean
    public Request.Options feignOptions() {
        return new Request.Options(
            5000,   // 连接超时5秒（毫秒）
            10000   // 读取超时10秒（毫秒）
        );
    }
}
