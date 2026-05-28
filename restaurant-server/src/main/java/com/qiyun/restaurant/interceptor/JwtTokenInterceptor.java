package com.qiyun.restaurant.interceptor;

import com.qiyun.restaurant.common.BaseContext;
import com.qiyun.restaurant.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * JWT令牌校验拦截器
 */
@Component
@Slf4j
public class JwtTokenInterceptor implements HandlerInterceptor {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 处理OPTIONS请求
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        // 从请求头中获取令牌
        String token = request.getHeader("Authorization");
        
        try {
            log.info("JWT校验: {}", token);
            Claims claims = JwtUtil.parseJWT(jwtSecret, token);
            Long userId = Long.valueOf(claims.get("userId").toString());
            log.info("当前用户ID: {}", userId);
            
            // 将用户ID存入ThreadLocal
            BaseContext.setCurrentId(userId);
            
            return true;
        } catch (Exception ex) {
            log.error("JWT校验失败: {}", ex.getMessage());
            response.setStatus(401);
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 清理ThreadLocal
        BaseContext.removeCurrentId();
    }
}
