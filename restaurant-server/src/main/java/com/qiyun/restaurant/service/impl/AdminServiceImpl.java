package com.qiyun.restaurant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qiyun.restaurant.dto.AdminLoginDTO;
import com.qiyun.restaurant.entity.Admin;
import com.qiyun.restaurant.mapper.AdminMapper;
import com.qiyun.restaurant.service.AdminService;
import com.qiyun.restaurant.utils.JwtUtil;
import com.qiyun.restaurant.vo.AdminLoginVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员Service实现类
 */
@Service
@Slf4j
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements AdminService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.admin-expiration}")
    private Long jwtExpiration;

    @Override
    public AdminLoginVO login(AdminLoginDTO adminLoginDTO) {
        String username = adminLoginDTO.getUsername();
        String password = adminLoginDTO.getPassword();

        // 查询管理员
        LambdaQueryWrapper<Admin> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Admin::getUsername, username);
        Admin admin = this.getOne(queryWrapper);

        // 验证用户名和密码
        if (admin == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 密码加密后比对（这里简化处理，实际应该使用BCrypt）
        String md5Password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!admin.getPassword().equals(md5Password)) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 检查账号状态
        if (admin.getStatus() == 0) {
            throw new RuntimeException("账号已被禁用");
        }

        // 生成JWT令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", admin.getId());
        claims.put("username", admin.getUsername());
        String token = JwtUtil.createJWT(jwtSecret, jwtExpiration * 1000, claims);

        // 返回登录信息
        return AdminLoginVO.builder()
                .id(admin.getId())
                .username(admin.getUsername())
                .name(admin.getName())
                .token(token)
                .build();
    }
}
