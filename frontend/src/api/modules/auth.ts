import { publicHttp } from '@/api/http';

/**
 * 登录请求参数。
 */
export interface DyxLoginPayload {
  username: string;
  password: string;
}

/**
 * 调用后台登录接口并获取后台登录态数据。
 *
 * @param payload 登录表单数据，包含用户名与密码。
 * @returns 返回包含 JWT 与当前登录用户信息的 Promise 结果。
 * @throws 该函数不会直接抛出同步异常；接口失败时会以 Promise reject 形式返回，并由调用方决定提示方式。
 * @author Dyx
 */
export function login(payload: DyxLoginPayload) {
  return publicHttp.post('/auth/login', payload);
}
