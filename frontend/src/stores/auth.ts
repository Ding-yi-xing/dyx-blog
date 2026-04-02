import { defineStore } from 'pinia';

interface AdminUser {
  id: string | number;
  username: string;
  displayName: string;
  role: string;
}

interface AuthState {
  token: string;
  user: AdminUser | null;
}

const TOKEN_KEY = 'dyx-admin-token';
const USER_KEY = 'dyx-admin-user';

/**
 * 从会话存储中读取当前后台登录用户信息。
 *
 * @returns 返回已缓存的后台用户信息；若当前会话不存在登录用户，则返回 null。
 * @throws 该函数不会主动处理 JSON 解析异常；若会话存储中的用户数据被破坏，将由运行时抛出异常。
 * @author Dyx
 */
function readSessionUser(): AdminUser | null {
  const raw = sessionStorage.getItem(USER_KEY);
  return raw ? (JSON.parse(raw) as AdminUser) : null;
}

/**
 * 后台认证状态仓库。
 * 负责维护 token、用户信息和登录退出行为。
 */
export const useAuthStore = defineStore('dyx-auth', {
  state: (): AuthState => ({
    token: sessionStorage.getItem(TOKEN_KEY) ?? '',
    user: readSessionUser()
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.token)
  },
  actions: {
    /**
     * 保存登录成功后的认证令牌与用户信息。
     *
     * @param token 登录成功后返回的 JWT 认证令牌。
     * @param user 当前登录用户信息，用于页面鉴权与展示。
     * @returns 无返回值。
     * @throws 该方法不会主动抛出业务异常；若浏览器存储不可用，则会由运行时环境抛出存储相关异常。
     * @author Dyx
     */
    setAuth(token: string, user: AdminUser): void {
      this.token = token;
      this.user = user;
      sessionStorage.setItem(TOKEN_KEY, token);
      sessionStorage.setItem(USER_KEY, JSON.stringify(user));
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USER_KEY);
    },

    /**
     * 清理当前登录态并移除浏览器中的认证缓存。
     *
     * @returns 无返回值。
     * @throws 该方法不会主动抛出业务异常；若浏览器存储不可用，则会由运行时环境抛出存储相关异常。
     * @author Dyx
     */
    clearAuth(): void {
      this.token = '';
      this.user = null;
      sessionStorage.removeItem(TOKEN_KEY);
      sessionStorage.removeItem(USER_KEY);
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USER_KEY);
    }
  }
});
