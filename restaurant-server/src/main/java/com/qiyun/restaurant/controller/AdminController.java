package com.qiyun.restaurant.controller;

import com.qiyun.restaurant.common.Result;
import com.qiyun.restaurant.dto.AdminLoginDTO;
import com.qiyun.restaurant.service.AdminService;
import com.qiyun.restaurant.vo.AdminLoginVO;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * 管理员Controller
 */
@RestController
@RequestMapping("/admin")
@Tag(name = "管理员接�?)
@Slf4j
public class AdminController {

    @Autowired
    private AdminService adminService;

    /**
     * 管理员登�?
     */
    @PostMapping("/login")
    @Operation(summary = "管理员登�?)
    public Result<AdminLoginVO> login(@Valid @RequestBody AdminLoginDTO adminLoginDTO) {
        log.info("管理员登�? {}", adminLoginDTO.getUsername());
        
        try {
            AdminLoginVO adminLoginVO = adminService.login(adminLoginDTO);
            return Result.success(adminLoginVO);
        } catch (Exception e) {
            log.error("登录失败: {}", e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 退出登�?
     */
    @PostMapping("/logout")
    @Operation(summary = "退出登�?)
    public Result<String> logout() {
        return Result.success("退出成�?);
    }
}
