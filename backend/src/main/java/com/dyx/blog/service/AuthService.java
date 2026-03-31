package com.dyx.blog.service;

import com.dyx.blog.common.dto.LoginRequest;
import com.dyx.blog.vo.LoginResponse;

/**
 * 认证服务接口。
 */
public interface AuthService {

    /**
     * 校验后台登录请求并生成登录结果。
     *
     * @param request 登录请求参数，包含用户名和密码。
     * @param ipAddress 当前登录请求的客户端 IP，用于审计日志与登录限流。
     * @return 包含 JWT 与用户展示信息的登录响应数据。
     * @throws BusinessException 当登录参数非法、账号状态异常、权限不足或登录频率超限时抛出。
     * @author Dyx
     */
    LoginResponse login(LoginRequest request, String ipAddress);
}
