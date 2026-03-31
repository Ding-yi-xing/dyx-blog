package com.dyx.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dyx.blog.common.constant.SystemConstant;
import com.dyx.blog.common.dto.LoginRequest;
import com.dyx.blog.common.exception.BusinessException;
import com.dyx.blog.common.util.JwtUtil;
import com.dyx.blog.common.util.LoginAttemptLimiter;
import com.dyx.blog.entity.User;
import com.dyx.blog.mapper.UserMapper;
import com.dyx.blog.service.AuthService;
import com.dyx.blog.vo.LoginResponse;
import com.dyx.blog.vo.UserVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 认证服务实现类。
 * 负责管理员登录校验与 JWT 生成。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserMapper dyxUserMapper;
    private final JwtUtil dyxJwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final LoginAttemptLimiter loginAttemptLimiter;

    /**
     * 执行后台用户登录校验并生成 JWT。
     * <p>
     * 实现流程包括：清洗用户名输入、校验登录限流、按用户名查询用户、校验密码、校验账号启用状态、校验后台角色权限，
     * 最终在校验通过后生成携带用户身份与角色信息的 Token。登录失败会记录失败日志并累计限流计数。
     * </p>
     *
     * @param request 登录请求参数，包含用户名和密码。
     * @param ipAddress 当前登录请求的客户端 IP。
     * @return 包含 JWT 与当前登录用户信息的登录结果。
     * @throws BusinessException 当登录频率超限、用户名或密码错误、账号已禁用或账号无后台登录权限时抛出。
     * @author Dyx
     */
    @Override
    public LoginResponse login(LoginRequest request, String ipAddress) {
        String username = request.getUsername() == null ? "" : request.getUsername().trim();
        String rawPassword = request.getPassword() == null ? "" : request.getPassword();
        log.info("用户登录尝试: username={}, ip={}", username, ipAddress);
        try {
            loginAttemptLimiter.check(username, ipAddress);
        } catch (IllegalStateException exception) {
            log.warn("用户登录受限: username={}, ip={}", username, ipAddress);
            throw new BusinessException(429, exception.getMessage());
        }

        User user = dyxUserMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .last("limit 1"));
        if (user == null) {
            loginAttemptLimiter.recordFailure(username, ipAddress);
            log.warn("用户登录失败: username={} (用户不存在), ip={}", username, ipAddress);
            throw new BusinessException("用户名或密码错误");
        }
        if (!matchesPassword(rawPassword, user)) {
            loginAttemptLimiter.recordFailure(username, ipAddress);
            log.warn("用户登录失败: username={} (密码错误), ip={}", username, ipAddress);
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getEnabled() == null || user.getEnabled() != 1) {
            loginAttemptLimiter.recordFailure(username, ipAddress);
            log.warn("用户登录失败: username={} (账号已被禁用), ip={}", username, ipAddress);
            throw new BusinessException("当前账号已被禁用");
        }
        if (!SystemConstant.ROLE_ADMIN.equals(user.getRole())) {
            loginAttemptLimiter.recordFailure(username, ipAddress);
            log.warn("用户登录失败: username={} (无后台权限), ip={}", username, ipAddress);
            throw new BusinessException("当前账号无后台登录权限");
        }

        loginAttemptLimiter.recordSuccess(username, ipAddress);
        log.info("用户登录成功: username={}, role={}, ip={}", user.getUsername(), user.getRole(), ipAddress);
        String token = dyxJwtUtil.generateToken(user.getId(), user.getRole());
        UserVo userVo = new UserVo();
        userVo.setId(user.getId());
        userVo.setUsername(user.getUsername());
        userVo.setDisplayName(user.getDisplayName());
        userVo.setRole(user.getRole());
        return new LoginResponse(token, userVo);
    }

    private boolean matchesPassword(String rawPassword, User user) {
        String storedPassword = user.getPassword();
        if (storedPassword == null) {
            return false;
        }
        if (looksLikeBcrypt(storedPassword)) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }
        boolean matches = storedPassword.equals(rawPassword);
        if (matches) {
            user.setPassword(passwordEncoder.encode(rawPassword));
            dyxUserMapper.updateById(user);
            log.info("用户密码已自动升级为 BCrypt: username={}", user.getUsername());
        }
        return matches;
    }

    private boolean looksLikeBcrypt(String value) {
        return value != null && value.matches("^\\$2[aby]\\$\\d{2}\\$[./A-Za-z0-9]{53}$");
    }
}
