import axios, { type AxiosError, type AxiosInstance } from 'axios';
import { ElMessage } from 'element-plus';
import { useAuthStore } from '@/stores/auth';

/**
 * 创建基础 Axios 客户端实例。
 *
 * @returns 返回统一配置了基础路径和超时时间的 Axios 实例。
 * @throws 该函数不会主动抛出业务异常；若 Axios 初始化参数非法，则会由运行时抛出对应错误。
 * @author Dyx
 */
function createBaseClient(): AxiosInstance {
  return axios.create({
    baseURL: '/api'
    // 上传等特殊场景在具体请求上单独配置超时时间
  });
}

const publicHttp: AxiosInstance = createBaseClient();
const adminHttp: AxiosInstance = createBaseClient();

let authRedirecting = false;

/**
 * 清理后台登录态并重定向到登录页。
 *
 * @returns 无返回值。
 * @throws 该函数不会主动抛出业务异常；若浏览器跳转或存储访问受限，则由运行时环境处理。
 * @author Dyx
 */
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

/**
 * 为指定客户端挂载统一返回体解析拦截器。
 *
 * @param client 需要挂载统一响应解析逻辑的 Axios 实例。
 * @returns 无返回值。
 * @throws 该函数不会主动抛出同步异常；当响应体中的业务状态码非成功时，会以 Promise reject 形式向调用方透出。
 * @author Dyx
 */
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
