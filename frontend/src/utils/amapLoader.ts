let amapLoaderPromise: Promise<AMapNamespace> | null = null;

const AMAP_SDK_URL = 'https://webapi.amap.com/maps';

function getAmapApiKey(): string {
  return String(import.meta.env.VITE_AMAP_API_KEY ?? '').trim();
}

function getAmapSecurityJsCode(): string {
  return String(import.meta.env.VITE_AMAP_SECURITY_JSCODE ?? '').trim();
}

function createSdkUrl(key: string): string {
  const searchParams = new URLSearchParams({
    v: '2.0',
    key
  });
  return `${AMAP_SDK_URL}?${searchParams.toString()}`;
}

export function getAmapApiKeyConfigured(): boolean {
  return !!getAmapApiKey();
}

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
