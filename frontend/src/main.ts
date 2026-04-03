import { createApp } from 'vue';
import { createPinia } from 'pinia';
import ElementPlus from 'element-plus';
import 'element-plus/dist/index.css';
import App from './App.vue';
import GlobalInitOverlay from './components/common/GlobalInitOverlay.vue';
import router from './router';
import './styles/index.scss';

const SITE_NAME = 'dyx-blog';
const SITE_URL = 'https://www.dyx.company';
const DEFAULT_TITLE = `${SITE_NAME} | 个人主页、博客与简历`;
const DEFAULT_DESCRIPTION = 'dyx-blog 是一个集个人主页、博客文章、动态记录、简历展示与留言互动于一体的个人站点。';
const DEFAULT_ROBOTS = 'index,follow,max-image-preview:large';

type SeoMeta = {
  title?: string;
  description?: string;
  canonicalPath?: string;
  ogType?: string;
  robots?: string;
};

function getSiteOrigin(): string {
  if (typeof window === 'undefined') {
    return SITE_URL;
  }
  const currentOrigin = window.location.origin;
  if (/^https?:\/\/(localhost|127\.0\.0\.1)(:\d+)?$/i.test(currentOrigin)) {
    return currentOrigin;
  }
  return SITE_URL;
}

function upsertMeta(selector: string, attributes: Record<string, string>, content: string): void {
  let element = document.head.querySelector<HTMLMetaElement>(selector);
  if (!element) {
    element = document.createElement('meta');
    Object.entries(attributes).forEach(([key, value]) => element?.setAttribute(key, value));
    document.head.appendChild(element);
  }
  element.setAttribute('content', content);
}

function upsertLink(selector: string, attributes: Record<string, string>, href: string): void {
  let element = document.head.querySelector<HTMLLinkElement>(selector);
  if (!element) {
    element = document.createElement('link');
    Object.entries(attributes).forEach(([key, value]) => element?.setAttribute(key, value));
    document.head.appendChild(element);
  }
  element.setAttribute('href', href);
}

function removeMeta(selector: string): void {
  document.head.querySelector(selector)?.remove();
}

function removeElementById(id: string): void {
  document.getElementById(id)?.remove();
}

function applyRouteSeo(): void {
  const currentRoute = router.currentRoute.value;
  const meta = (currentRoute.meta ?? {}) as SeoMeta;
  const origin = getSiteOrigin();
  const title = meta.title || DEFAULT_TITLE;
  const description = meta.description || DEFAULT_DESCRIPTION;
  const canonicalUrl = new URL(meta.canonicalPath || currentRoute.path || '/', origin).toString();
  const ogType = meta.ogType || 'website';
  const robots = meta.robots || DEFAULT_ROBOTS;

  document.title = title;
  upsertMeta('meta[name="description"]', { name: 'description' }, description);
  upsertMeta('meta[name="robots"]', { name: 'robots' }, robots);
  upsertMeta('meta[property="og:site_name"]', { property: 'og:site_name' }, SITE_NAME);
  upsertMeta('meta[property="og:locale"]', { property: 'og:locale' }, 'zh_CN');
  upsertMeta('meta[property="og:title"]', { property: 'og:title' }, title);
  upsertMeta('meta[property="og:description"]', { property: 'og:description' }, description);
  upsertMeta('meta[property="og:type"]', { property: 'og:type' }, ogType);
  upsertMeta('meta[property="og:url"]', { property: 'og:url' }, canonicalUrl);
  upsertMeta('meta[name="twitter:card"]', { name: 'twitter:card' }, 'summary');
  upsertMeta('meta[name="twitter:title"]', { name: 'twitter:title' }, title);
  upsertMeta('meta[name="twitter:description"]', { name: 'twitter:description' }, description);
  upsertLink('link[rel="canonical"]', { rel: 'canonical' }, canonicalUrl);
  removeMeta('meta[property="og:image"]');
  removeMeta('meta[name="twitter:image"]');
  removeElementById('dyx-blog-post-jsonld');
}

/**
 * 前端应用入口。
 * <p>
 * 该入口负责创建 Vue 应用实例，按顺序挂载 Pinia、路由系统与 Element Plus，
 * 最终将整站应用挂载到页面根节点。
 * </p>
 *
 * @author Dyx
 */
const dyxApp = createApp(App);
const dyxPinia = createPinia();

dyxApp.component('GlobalInitOverlay', GlobalInitOverlay);
dyxApp.use(dyxPinia);
dyxApp.use(router);
dyxApp.use(ElementPlus);

router.isReady().then(() => {
  applyRouteSeo();
});

router.afterEach(() => {
  applyRouteSeo();
});

dyxApp.mount('#app');
