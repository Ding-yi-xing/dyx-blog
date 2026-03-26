import axios, { type AxiosInstance } from 'axios';
import { useAuthStore } from '@/stores/auth';

/**
 * 创建 Axios 实例。
 * 用于统一处理基础地址、超时和请求头注入。
 */
const http: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 10000
});

/**
 * 请求拦截器。
 * 在后台登录后自动为请求附带 Bearer Token。
 */
http.interceptors.request.use((config) => {
  const authStore = useAuthStore();
  if (authStore.token) {
    config.headers.Authorization = `Bearer ${authStore.token}`;
  }
  return config;
});

/**
 * 响应拦截器。
 * 统一返回后端 data 结构，简化页面层调用。
 */
http.interceptors.response.use((response) => {
  const payload = response.data as { code?: number } | undefined;
  if (payload && typeof payload === 'object' && typeof payload.code === 'number' && payload.code !== 200) {
    return Promise.reject({ response: { ...response, data: payload } });
  }
  return response.data;
});

export default http;
