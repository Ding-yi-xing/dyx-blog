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
     * 处理后台登录请求并返回登录态数据。
     * <p>
     * 该方法负责接收前端提交的用户名与密码，解析客户端 IP 后委托认证服务完成账号校验、限流校验与 JWT 生成。
     * 登录失败时会由认证服务抛出业务异常，并交由全局异常处理器统一转换为响应结果。
     * </p>
     *
     * @param request 登录请求参数，包含用户名和密码。
     * @param httpRequest 当前 HTTP 请求，用于解析客户端来源 IP。
     * @return 包含 Token 与当前登录用户信息的统一响应结果。
     * @throws BusinessException 当账号密码错误、账号被禁用、无后台权限或触发登录限流时抛出。
     * @author Dyx
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        return Result.success(dyxAuthService.login(request, ClientIpUtil.resolveClientIp(httpRequest)));
    }
}
