package com.dyx.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dyx.blog.common.constant.SystemConstant;
import com.dyx.blog.common.dto.LoginRequest;
import com.dyx.blog.common.exception.BusinessException;
import com.dyx.blog.common.util.JwtUtil;
import com.dyx.blog.entity.User;
import com.dyx.blog.mapper.UserMapper;
import com.dyx.blog.service.AuthService;
import com.dyx.blog.vo.LoginResponse;
import com.dyx.blog.vo.UserVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 认证服务实现类。
 * 负责管理员登录校验与 JWT 生成。
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserMapper dyxUserMapper;
    private final JwtUtil dyxJwtUtil;

    /**
     * 执行登录校验。
     *
     * @param request 登录请求参数。
     * @return 登录结果。
     */
    @Override
    public LoginResponse login(LoginRequest request) {
        User user = dyxUserMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, request.getUsername())
                .last("limit 1"));
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (!request.getPassword().equals(user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.getEnabled() == null || user.getEnabled() != 1) {
            throw new BusinessException("当前账号已被禁用");
        }
        if (!SystemConstant.ROLE_ADMIN.equals(user.getRole())) {
            throw new BusinessException("当前账号无后台登录权限");
        }

        String token = dyxJwtUtil.generateToken(user.getId());
        UserVo userVo = new UserVo();
        userVo.setId(user.getId());
        userVo.setUsername(user.getUsername());
        userVo.setDisplayName(user.getDisplayName());
        userVo.setRole(user.getRole());
        return new LoginResponse(token, userVo);
    }
}
