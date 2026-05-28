package com.qiyun.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.qiyun.admin.dto.LoginRequest;
import com.qiyun.admin.dto.LoginResponse;
import com.qiyun.admin.entity.Admin;
import com.qiyun.admin.mapper.AdminMapper;
import com.qiyun.admin.service.AdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService {
    
    private final AdminMapper adminMapper;
    
    @Override
    public LoginResponse login(LoginRequest request) {
        log.info("管理员登录: {}", request.getUsername());
        
        // 查询管理员
        LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Admin::getUsername, request.getUsername());
        Admin admin = adminMapper.selectOne(wrapper);
        
        if (admin == null) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 验证密码（MD5）
        String md5Password = DigestUtils.md5DigestAsHex(request.getPassword().getBytes());
        if (!md5Password.equals(admin.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }
        
        // 检查状态
        if (admin.getStatus() != 1) {
            throw new RuntimeException("账号已被禁用");
        }
        
        // 生成token（简单实现，实际应该使用JWT）
        String token = UUID.randomUUID().toString().replace("-", "");
        
        log.info("管理员登录成功: {}", admin.getUsername());
        
        return new LoginResponse(token, admin.getId(), admin.getUsername(), admin.getName());
    }
}
