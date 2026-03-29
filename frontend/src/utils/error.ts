/**
 * 解析 API 请求错误并返回可读的错误消息。
 * 
 * @param error 异常对象。
 * @param defaultMsg 默认错误消息。
 * @returns 解析后的错误字符串。
 */
export function resolveErrorMessage(error: any, defaultMsg = "操作失败"): string {
  const payload = error?.response?.data;
  if (payload && typeof payload === "object") {
    if (typeof payload.message === "string") {
      return payload.message;
    }
    if (typeof payload.data === "string") {
      return payload.data;
    }
  }
  return defaultMsg;
}
