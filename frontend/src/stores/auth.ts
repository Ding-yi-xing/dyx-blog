import { defineStore } from 'pinia';

interface AdminUser {
  id: number;
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
 * 后台认证状态仓库。
 * 负责维护 token、用户信息和登录退出行为。
 */
export const useAuthStore = defineStore('dyx-auth', {
  state: (): AuthState => ({
    token: localStorage.getItem(TOKEN_KEY) ?? '',
    user: localStorage.getItem(USER_KEY)
      ? (JSON.parse(localStorage.getItem(USER_KEY) as string) as AdminUser)
      : null
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.token)
  },
  actions: {
    /**
     * 保存登录后的 token 和用户信息。
     * @param token 登录成功后返回的认证令牌。
     * @param user 当前登录用户信息。
     */
    setAuth(token: string, user: AdminUser): void {
      this.token = token;
      this.user = user;
      localStorage.setItem(TOKEN_KEY, token);
      localStorage.setItem(USER_KEY, JSON.stringify(user));
    },

    /**
     * 清理当前登录态。
     */
    clearAuth(): void {
      this.token = '';
      this.user = null;
      localStorage.removeItem(TOKEN_KEY);
      localStorage.removeItem(USER_KEY);
    }
  }
});
