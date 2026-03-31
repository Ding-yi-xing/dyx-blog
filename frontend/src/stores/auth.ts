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
     * 保存登录后的 token 和用户信息。
     * @param token 登录成功后返回的认证令牌。
     * @param user 当前登录用户信息。
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
     * 清理当前登录态。
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
