package com.dyx.blog.controller.auth;

import com.dyx.blog.common.dto.LoginRequest;
import com.dyx.blog.common.response.Result;
import com.dyx.blog.common.util.ClientIpUtil;
import com.dyx.blog.service.AuthService;
import com.dyx.blog.vo.LoginResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证控制器。
 * 提供后台登录认证接口。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService dyxAuthService;

    /**
     * 后台登录接口。
     *
     * @param request 登录请求参数。
     * @param httpRequest HTTP 请求。
     * @return 登录结果。
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return Result.success(dyxAuthService.login(request, ClientIpUtil.resolveClientIp(httpRequest)));
    }
}
