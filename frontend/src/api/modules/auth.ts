import dyxHttp from '@/api/http';

/**
 * 登录请求参数。
 */
export interface DyxLoginPayload {
  username: string;
  password: string;
}

/**
 * 调用后台登录接口。
 * @param payload 登录表单数据。
 * @returns 登录后的 token 与用户信息。
 */
export function login(payload: DyxLoginPayload) {
  return dyxHttp.post('/auth/login', payload);
}
