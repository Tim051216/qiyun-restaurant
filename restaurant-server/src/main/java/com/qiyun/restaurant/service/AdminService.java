package com.qiyun.restaurant.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qiyun.restaurant.dto.AdminLoginDTO;
import com.qiyun.restaurant.entity.Admin;
import com.qiyun.restaurant.vo.AdminLoginVO;

/**
 * 管理员Service
 */
public interface AdminService extends IService<Admin> {

    /**
     * 管理员登录
     */
    AdminLoginVO login(AdminLoginDTO adminLoginDTO);
}
