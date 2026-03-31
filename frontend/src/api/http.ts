import axios, { type AxiosError, type AxiosInstance } from 'axios';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '@/stores/auth';

/**
 * 创建 Axios 实例。
 * 用于统一处理基础地址、超时和请求头注入。
 */
function createBaseClient(): AxiosInstance {
  return axios.create({
    baseURL: '/api',
    timeout: 10000
  });
}

const publicHttp: AxiosInstance = createBaseClient();
const adminHttp: AxiosInstance = createBaseClient();

let authRedirecting = false;

function redirectToLogin(): void {
  if (authRedirecting) {
    return;
  }
  authRedirecting = true;
  const authStore = useAuthStore();
  authStore.clearAuth();
  const currentPath = `${window.location.pathname}${window.location.search}`;
  const redirectQuery = currentPath.startsWith('/dyx-manager')
    ? `?redirect=${encodeURIComponent(currentPath)}`
    : '';
  window.location.href = `/dyx-manager/login${redirectQuery}`;
}

function applyResultInterceptor(client: AxiosInstance): void {
  client.interceptors.response.use(
    (response) => {
      const payload = response.data as { code?: number } | undefined;
      if (payload && typeof payload === 'object' && typeof payload.code === 'number' && payload.code !== 200) {
        return Promise.reject({ response: { ...response, data: payload } });
      }
      return response.data;
    }
  );
}

applyResultInterceptor(publicHttp);
applyResultInterceptor(adminHttp);

/**
 * 请求拦截器。
 * 在后台登录后自动为后台请求附带 Bearer Token。
 */
adminHttp.interceptors.request.use((config) => {
  const authStore = useAuthStore();
  if (authStore.token) {
    config.headers.Authorization = `Bearer ${authStore.token}`;
  }
  return config;
});

/**
 * 后台响应拦截器。
 * 仅后台受保护接口处理 401/403 登录态与权限提示。
 */
adminHttp.interceptors.response.use(
  undefined,
  (error: AxiosError<{ code?: number; message?: string }>) => {
    const status = error.response?.status;
    if (status === 401) {
      ElMessage.error(error.response?.data?.message || '登录状态已失效，请重新登录');
      redirectToLogin();
    } else if (status === 403) {
      ElMessage.error(error.response?.data?.message || '当前账号无权限访问该功能');
    }
    return Promise.reject(error);
  }
);

export { publicHttp, adminHttp };
export default publicHttp;
