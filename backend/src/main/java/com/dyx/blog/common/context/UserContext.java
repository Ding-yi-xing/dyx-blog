package com.dyx.blog.common.context;

/**
 * 当前登录用户上下文。
 * 使用 ThreadLocal 暂存当前请求中的用户 ID。
 */
public final class UserContext {

    private static final ThreadLocal<Long> USER_ID_HOLDER = new ThreadLocal<>();
    private static final ThreadLocal<String> USER_ROLE_HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    /**
     * 设置当前登录用户 ID。
     *
     * @param userId 用户主键。
     */
    public static void setUserId(Long userId) {
        USER_ID_HOLDER.set(userId);
    }

    /**
     * 获取当前登录用户 ID。
     *
     * @return 用户主键。
     */
    public static Long getUserId() {
        return USER_ID_HOLDER.get();
    }

    /**
     * 设置当前登录用户角色。
     *
     * @param role 角色标识。
     */
    public static void setUserRole(String role) {
        USER_ROLE_HOLDER.set(role);
    }

    /**
     * 获取当前登录用户角色。
     *
     * @return 角色标识。
     */
    public static String getUserRole() {
        return USER_ROLE_HOLDER.get();
    }

    /**
     * 判断当前用户是否为超级管理员。
     *
     * @return 是否为管理员。
     */
    public static boolean isAdmin() {
        return "ADMIN".equalsIgnoreCase(getUserRole());
    }

    /**
     * 清理线程变量，避免线程复用带来的脏数据问题。
     */
    public static void clear() {
        USER_ID_HOLDER.remove();
        USER_ROLE_HOLDER.remove();
    }
}
