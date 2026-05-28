package com.qiyun.admin.service;

import com.qiyun.admin.dto.LoginRequest;
import com.qiyun.admin.dto.LoginResponse;

public interface AdminService {
    LoginResponse login(LoginRequest request);
}
