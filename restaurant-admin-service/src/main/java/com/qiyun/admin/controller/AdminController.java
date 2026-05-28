package com.qiyun.admin.controller;

import com.qiyun.admin.common.Result;
import com.qiyun.admin.dto.LoginRequest;
import com.qiyun.admin.dto.LoginResponse;
import com.qiyun.admin.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {
    
    private final AdminService adminService;
    
    @GetMapping("/health")
    public Result<String> health() {
        log.info("管理服务健康检查");
        return Result.success("Admin Service is running");
    }
    
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        log.info("管理员登录请求: {}", request.getUsername());
        try {
            LoginResponse response = adminService.login(request);
            return Result.success(response);
        } catch (Exception e) {
            log.error("登录失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }
    
    @PostMapping("/logout")
    public Result<String> logout() {
        log.info("管理员退出登录");
        return Result.success("退出成功");
    }
}
