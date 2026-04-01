/**
 * 解析图片地址字段，兼容 JSON 字符串、逗号分隔字符串与数组输入。
 *
 * @param value 原始图片地址值。
 * @returns 返回过滤空值后的图片地址数组；解析失败时会回退到逗号分隔解析。
 * @throws 该函数不会主动抛出业务异常；JSON 解析失败时会自动降级为字符串拆分。
 * @author Dyx
 */
export function parseImageUrls(value?: string | string[] | null): string[] {
  if (Array.isArray(value)) {
    return value.filter(Boolean);
  }
  if (!value) {
    return [];
  }
  try {
    const parsed = JSON.parse(value);
    return Array.isArray(parsed) ? parsed.filter(Boolean) : [];
  } catch (error) {
    return value
      .split(',')
      .map((item) => item.trim())
      .filter(Boolean);
  }
}

/**
 * 将图片地址数组序列化为可保存的 JSON 字符串。
 *
 * @param urls 图片地址数组。
 * @returns 返回过滤空值后的 JSON 字符串。
 * @throws 该函数不会主动抛出业务异常；若序列化出现运行时异常，将由 JavaScript 引擎抛出。
 * @author Dyx
 */
export function stringifyImageUrls(urls: string[]): string {
  return JSON.stringify(urls.filter(Boolean));
}

/**
 * 判断给定地址是否为 PDF 资源。
 *
 * @param value 资源地址。
 * @returns 当地址存在且扩展名为 `.pdf` 时返回 true。
 * @throws 该函数不会主动抛出异常。
 * @author Dyx
 */
export function isPdfUrl(value?: string | null): boolean {
  if (!value) {
    return false;
  }
  return /\.pdf(?:$|\?)/i.test(value);
}

/**
 * 判断给定地址是否为图片资源。
 *
 * @param value 资源地址。
 * @returns 当地址存在且扩展名符合常见图片格式时返回 true。
 * @throws 该函数不会主动抛出异常。
 * @author Dyx
 */
export function isImageUrl(value?: string | null): boolean {
  if (!value) {
    return false;
  }
  return /\.(png|jpe?g|gif|webp|bmp|svg)(?:$|\?)/i.test(value);
}

/**
 * 判断上传文件对象是否为图片文件。
 *
 * @param file 浏览器 File 对象。
 * @returns 当 MIME 类型或文件名扩展名匹配图片格式时返回 true。
 * @throws 该函数不会主动抛出异常。
 * @author Dyx
 */
export function isImageFile(file?: File | null): boolean {
  if (!file) {
    return false;
  }
  if (file.type.startsWith('image/')) {
    return true;
  }
  return isImageUrl(file.name);
}

/**
 * 判断给定地址是否为视频资源。
 *
 * @param value 资源地址。
 * @returns 当地址存在且扩展名符合常见视频格式时返回 true。
 * @throws 该函数不会主动抛出异常。
 * @author Dyx
 */
export function isVideoUrl(value?: string | null): boolean {
  if (!value) {
    return false;
  }
  return /\.(mp4|webm|mov|m4v)(?:$|\?)/i.test(value);
}

/**
 * 从资源地址中提取文件名。
 *
 * @param value 资源地址或文件路径。
 * @returns 返回解码后的文件名；值为空时返回空字符串。
 * @throws 该函数不会主动抛出业务异常；若 URL 解码失败，将由运行时抛出异常。
 * @author Dyx
 */
export function extractFileName(value?: string | null): string {
  if (!value) {
    return '';
  }
  const normalized = value.split('?')[0] ?? value;
  const segments = normalized.split('/');
  return decodeURIComponent(segments[segments.length - 1] ?? '');
}
