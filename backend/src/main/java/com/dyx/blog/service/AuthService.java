package com.dyx.blog.service;

import com.dyx.blog.common.dto.LoginRequest;
import com.dyx.blog.vo.LoginResponse;

/**
 * 认证服务接口。
 */
public interface AuthService {

    /**
     * 管理员登录。
     *
     * @param request 登录请求参数。
     * @return 登录响应数据。
     */
    LoginResponse login(LoginRequest request);
}
