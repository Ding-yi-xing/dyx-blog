/**
 * 高德地图 SDK 的单例加载 Promise。
 * 用于避免同一会话内重复注入脚本。
 */
let amapLoaderPromise: Promise<AMapNamespace> | null = null;

const AMAP_SDK_URL = 'https://webapi.amap.com/maps';

/**
 * 读取高德地图 API Key 配置。
 *
 * @returns 返回去除首尾空白后的 API Key；未配置时返回空字符串。
 * @throws 该函数不会主动抛出异常。
 * @author Dyx
 */
function getAmapApiKey(): string {
  return String(import.meta.env.VITE_AMAP_API_KEY ?? '').trim();
}

/**
 * 读取高德地图安全密钥配置。
 *
 * @returns 返回去除首尾空白后的安全密钥；未配置时返回空字符串。
 * @throws 该函数不会主动抛出异常。
 * @author Dyx
 */
function getAmapSecurityJsCode(): string {
  return String(import.meta.env.VITE_AMAP_SECURITY_JSCODE ?? '').trim();
}

/**
 * 构造高德地图 SDK 脚本地址。
 *
 * @param key 高德地图 API Key。
 * @returns 返回带版本号与 key 参数的 SDK URL。
 * @throws 该函数不会主动抛出异常。
 * @author Dyx
 */
function createSdkUrl(key: string): string {
  const searchParams = new URLSearchParams({
    v: '2.0',
    key
  });
  return `${AMAP_SDK_URL}?${searchParams.toString()}`;
}

/**
 * 判断当前前端环境是否已配置高德地图 Key。
 *
 * @returns 当 `VITE_AMAP_API_KEY` 存在且非空时返回 true。
 * @throws 该函数不会主动抛出异常。
 * @author Dyx
 */
export function getAmapApiKeyConfigured(): boolean {
  return !!getAmapApiKey();
}

/**
 * 按需加载高德地图 SDK，并复用已加载的全局实例。
 *
 * @returns 返回浏览器全局 `window.AMap` 对象。
 * @throws Error 当当前环境不是浏览器、缺少 API Key，或脚本加载失败时抛出异常。
 * @author Dyx
 */
export async function loadAmapSdk(): Promise<AMapNamespace> {
  if (typeof window === 'undefined') {
    throw new Error('高德地图仅支持在浏览器环境加载。');
  }

  if (window.AMap) {
    return window.AMap;
  }

  if (amapLoaderPromise) {
    return amapLoaderPromise;
  }

  const key = getAmapApiKey();
  if (!key) {
    throw new Error('缺少 VITE_AMAP_API_KEY，无法加载高德地图。');
  }

  const securityJsCode = getAmapSecurityJsCode();
  if (securityJsCode) {
    window._AMapSecurityConfig = {
      securityJsCode
    };
  }

  amapLoaderPromise = new Promise((resolve, reject) => {
    const existingScript = document.querySelector<HTMLScriptElement>('script[data-amap-sdk="true"]');
    if (existingScript) {
      existingScript.addEventListener('load', () => {
        if (window.AMap) {
          resolve(window.AMap);
          return;
        }
        reject(new Error('高德地图脚本已加载，但 AMap 全局对象不存在。'));
      }, { once: true });
      existingScript.addEventListener('error', () => {
        amapLoaderPromise = null;
        reject(new Error('高德地图脚本加载失败。'));
      }, { once: true });
      return;
    }

    const script = document.createElement('script');
    script.src = createSdkUrl(key);
    script.async = true;
    script.defer = true;
    script.dataset.amapSdk = 'true';
    script.onload = () => {
      if (window.AMap) {
        resolve(window.AMap);
        return;
      }
      amapLoaderPromise = null;
      reject(new Error('高德地图脚本已加载，但 AMap 全局对象不存在。'));
    };
    script.onerror = () => {
      amapLoaderPromise = null;
      reject(new Error('高德地图脚本加载失败。'));
    };
    document.head.appendChild(script);
  });

  return amapLoaderPromise;
}
