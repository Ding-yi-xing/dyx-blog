/**
 * 将后端或前端传入的日期时间字符串标准化为可供 `Date` 解析的格式。
 *
 * @param value 原始日期字符串。
 * @returns 返回替换过空格分隔符后的日期字符串。
 */
function normalizeDateTimeString(value: string): string {
  return value.includes(' ') ? value.replace(' ', 'T') : value;
}

/**
 * 将日期时间字符串格式化为 `yyyy-MM-dd`。
 *
 * @param value 原始日期字符串，可为后端返回的 `yyyy-MM-dd HH:mm:ss`、ISO 字符串或空值。
 * @returns 返回格式化后的日期；当值为空或无法解析时返回空字符串。
 * @throws 该函数不会主动抛出业务异常；日期解析失败时会回退到正则提取或空字符串。
 * @author Dyx
 */
export function formatDateYmd(value?: string | null): string {
  if (!value) {
    return '';
  }

  const date = new Date(normalizeDateTimeString(value));
  if (!Number.isNaN(date.getTime())) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
  }

  const matched = value.match(/^(\d{4}-\d{2}-\d{2})/);
  return matched?.[1] ?? '';
}

/**
 * 将日期时间字符串格式化为 `yyyy-MM-dd HH:mm`。
 *
 * @param value 原始日期字符串，可为后端返回的 `yyyy-MM-dd HH:mm:ss`、ISO 字符串或空值。
 * @returns 返回格式化后的日期时间；无法解析时尽量回退到原始字符串中的日期与分钟部分。
 * @throws 该函数不会主动抛出业务异常；日期解析失败时会回退到正则提取或原值。
 * @author Dyx
 */
export function formatDateTime(value?: string | null): string {
  if (!value) {
    return '';
  }

  const date = new Date(normalizeDateTimeString(value));
  if (!Number.isNaN(date.getTime())) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');
    return `${year}-${month}-${day} ${hours}:${minutes}`;
  }

  const matched = value.match(/^(\d{4}-\d{2}-\d{2})(?:[ T](\d{2}:\d{2}))?/);
  if (!matched) {
    return value;
  }
  return matched[2] ? `${matched[1]} ${matched[2]}` : matched[1];
}

/**
 * 返回当前年份，用于页面内需要稳定复用年份的轻量展示场景。
 *
 * @param now 可选时间实例，默认使用当前系统时间。
 * @returns 返回四位年份数字。
 */
export function getCurrentYear(now = new Date()): number {
  return now.getFullYear();
}
