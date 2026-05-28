package com.qiyun.order.config;

import feign.RequestInterceptor;
import io.seata.core.context.RootContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 确保 Seata 的 XID 能通过 Feign 在演示链路中透传。
 */
@Configuration
public class SeataFeignConfig {

    @Bean
    public RequestInterceptor seataRequestInterceptor() {
        return template -> {
            String xid = RootContext.getXID();
            if (xid != null && !xid.isBlank()) {
                template.header(RootContext.KEY_XID, xid);
            }
        };
    }
}
