/**
 * 解析 API 请求错误并返回可读的错误消息。
 *
 * @param error 异常对象，通常为 Axios 请求失败时抛出的错误。
 * @param defaultMsg 默认错误消息。
 * @returns 优先返回后端响应中的 message 或字符串 data，缺失时回退到默认消息。
 * @throws 该函数不会主动抛出业务异常；当异常对象结构不完整时会直接回退默认消息。
 * @author Dyx
 */
export function resolveErrorMessage(error: any, defaultMsg = '操作失败'): string {
  const payload = error?.response?.data;
  if (payload && typeof payload === 'object') {
    if (typeof payload.message === 'string') {
      return payload.message;
    }
    if (typeof payload.data === 'string') {
      return payload.data;
    }
  }
  return defaultMsg;
}
