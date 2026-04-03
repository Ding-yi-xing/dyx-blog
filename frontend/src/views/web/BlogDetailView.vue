<template>
  <section class="dyx-page-shell space-y-6 px-2 sm:px-0">
    <article class="dyx-page-card-elevated rounded-[36px] px-6 py-8 md:px-8 md:py-9">
      <RouterLink to="/blog" class="inline-flex items-center text-[12px] font-medium dyx-text-meta transition-opacity hover:opacity-80">
        <span class="mr-1 text-base">←</span>
        返回列表
      </RouterLink>
      <p class="mt-4 text-[11px] font-semibold uppercase tracking-[0.32em] dyx-text-meta">博客详情</p>
      <h1 class="mt-3 text-3xl font-semibold tracking-tight dyx-text-main md:text-4xl">
        {{ post.title || `文章 ${route.params.id}` }}
      </h1>
      <div v-if="resolvePostDate(post) && !errorMessage" class="mt-4 flex flex-wrap items-center gap-3 text-[12px] dyx-text-meta">
        <span>{{ formatDateYmd(resolvePostDate(post) as string) }}</span>
      </div>
    </article>

    <article class="dyx-page-card rounded-[36px] px-6 py-8 md:px-8 md:py-10">
      <p v-if="loading" class="text-[15px] leading-8 dyx-text-muted">
        正在加载文章内容...
      </p>
      <p v-else-if="errorMessage" class="text-[15px] leading-8 text-rose-500">
        {{ errorMessage }}
      </p>
      <template v-else>
        <div v-if="post.coverImage" class="mb-8 flex justify-center">
          <img
            :src="post.coverImage"
            :alt="post.title || '博客封面'"
            class="block max-h-[520px] w-auto max-w-full rounded-[30px] object-contain"
          />
        </div>
        <div v-if="renderedContent" class="blog-content max-w-none text-[15px] leading-8 dyx-text-muted" v-html="renderedContent"></div>
        <p v-else class="whitespace-pre-line text-[15px] leading-8 dyx-text-muted">
          {{ post.summary || '暂无正文内容。' }}
        </p>
      </template>
    </article>

    <div class="flex flex-wrap items-center justify-between gap-3 text-[12px] dyx-text-meta">
      <RouterLink to="/blog" class="dyx-ghost-pill inline-flex items-center">
        <span class="mr-1 text-base">←</span>
        返回列表
      </RouterLink>
      <span class="text-[11px] dyx-text-meta">感谢阅读。</span>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import { getPostDetail, recordSiteVisit, type PostData } from '@/api/modules/site';
import { formatDateYmd } from '@/utils/date';
import { resolveErrorMessage } from '@/utils/error';

const SITE_NAME = 'dyx-blog';
const SITE_URL = 'https://www.dyx.company';
const DEFAULT_TITLE = `博客详情 | ${SITE_NAME}`;
const DEFAULT_DESCRIPTION = '查看 dyx-blog 的博客详情内容与文章摘要。';
const DEFAULT_IMAGE = '';
const JSON_LD_ID = 'dyx-blog-post-jsonld';

const route = useRoute();
const post = ref<Partial<PostData>>({});
const loading = ref(false);
const errorMessage = ref('');
const htmlEntityDecoder = typeof document === 'undefined' ? null : document.createElement('textarea');

const renderedContent = computed(() => decodeHtmlEntities(post.value.content));

function resolvePostDate(currentPost: Partial<PostData>): string | undefined {
  return currentPost.publishedAt || currentPost.updatedAt;
}

function decodeHtmlEntities(content?: string): string {
  if (!content) {
    return '';
  }
  if (!htmlEntityDecoder || !content.includes('&lt;')) {
    return content;
  }
  htmlEntityDecoder.innerHTML = content;
  return htmlEntityDecoder.value;
}

function stripHtml(content?: string): string {
  if (!content) {
    return '';
  }
  return content.replace(/<[^>]*>/g, ' ').replace(/\s+/g, ' ').trim();
}

function buildDescription(currentPost: Partial<PostData>): string {
  const summary = currentPost.summary?.trim();
  if (summary) {
    return summary.slice(0, 120);
  }
  const plainTextContent = stripHtml(decodeHtmlEntities(currentPost.content));
  if (plainTextContent) {
    return plainTextContent.slice(0, 120);
  }
  return DEFAULT_DESCRIPTION;
}

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

function removeJsonLd(): void {
  document.getElementById(JSON_LD_ID)?.remove();
}

function updateJsonLd(currentPost: Partial<PostData>, canonicalUrl: string, description: string): void {
  removeJsonLd();
  if (!currentPost.title) {
    return;
  }
  const element = document.createElement('script');
  element.type = 'application/ld+json';
  element.id = JSON_LD_ID;
  element.text = JSON.stringify({
    '@context': 'https://schema.org',
    '@type': 'BlogPosting',
    headline: currentPost.title,
    description,
    mainEntityOfPage: canonicalUrl,
    url: canonicalUrl,
    image: currentPost.coverImage ? [currentPost.coverImage] : undefined,
    datePublished: currentPost.publishedAt || undefined,
    dateModified: currentPost.updatedAt || currentPost.publishedAt || undefined,
    author: {
      '@type': 'Person',
      name: 'Dyx'
    },
    publisher: {
      '@type': 'Person',
      name: 'Dyx'
    },
    inLanguage: 'zh-CN'
  });
  document.head.appendChild(element);
}

function applyPostSeo(currentPost: Partial<PostData>): void {
  const origin = getSiteOrigin();
  const canonicalUrl = new URL(`/blog/${route.params.id}`, origin).toString();
  const title = currentPost.title ? `${currentPost.title} | ${SITE_NAME}` : DEFAULT_TITLE;
  const description = buildDescription(currentPost);
  const image = currentPost.coverImage || DEFAULT_IMAGE;
  const robots = currentPost.id ? 'index,follow,max-image-preview:large' : 'noindex,follow';

  document.title = title;
  upsertMeta('meta[name="description"]', { name: 'description' }, description);
  upsertMeta('meta[name="robots"]', { name: 'robots' }, robots);
  upsertMeta('meta[property="og:title"]', { property: 'og:title' }, title);
  upsertMeta('meta[property="og:description"]', { property: 'og:description' }, description);
  upsertMeta('meta[property="og:type"]', { property: 'og:type' }, 'article');
  upsertMeta('meta[property="og:url"]', { property: 'og:url' }, canonicalUrl);
  upsertMeta('meta[name="twitter:title"]', { name: 'twitter:title' }, title);
  upsertMeta('meta[name="twitter:description"]', { name: 'twitter:description' }, description);
  upsertLink('link[rel="canonical"]', { rel: 'canonical' }, canonicalUrl);

  if (image) {
    upsertMeta('meta[property="og:image"]', { property: 'og:image' }, image);
    upsertMeta('meta[name="twitter:image"]', { name: 'twitter:image' }, image);
  } else {
    removeMeta('meta[property="og:image"]');
    removeMeta('meta[name="twitter:image"]');
  }

  updateJsonLd(currentPost, canonicalUrl, description);
}

async function loadPostDetail(): Promise<void> {
  loading.value = true;
  errorMessage.value = '';
  try {
    const response = await getPostDetail(String(route.params.id));
    const nextPost = response.data ?? {};
    if (!nextPost.id) {
      post.value = {};
      errorMessage.value = '文章不存在或未发布。';
      applyPostSeo({});
      return;
    }
    post.value = nextPost;
    applyPostSeo(nextPost);
  } catch (error) {
    post.value = {};
    errorMessage.value = resolveErrorMessage(error, '文章加载失败，请稍后重试。');
    applyPostSeo({});
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
  applyPostSeo(post.value);
  void recordSiteVisit('blog-detail');
  void loadPostDetail();
});
</script>

<style scoped>
.blog-content :deep(h1),
.blog-content :deep(h2),
.blog-content :deep(h3) {
  margin: 1.5em 0 0.7em;
  font-weight: 600;
  line-height: 1.3;
  color: var(--dyx-text-main);
}

.blog-content :deep(h1) {
  font-size: 1.875rem;
}

.blog-content :deep(h2) {
  font-size: 1.5rem;
}

.blog-content :deep(h3) {
  font-size: 1.25rem;
}

.blog-content :deep(p),
.blog-content :deep(ul),
.blog-content :deep(ol),
.blog-content :deep(blockquote),
.blog-content :deep(pre) {
  margin: 1em 0;
}

.blog-content :deep(ul),
.blog-content :deep(ol) {
  padding-left: 1.4em;
}

.blog-content :deep(li + li) {
  margin-top: 0.35em;
}

.blog-content :deep(a) {
  color: rgb(59 130 246);
  text-decoration: underline;
  text-underline-offset: 3px;
}

.blog-content :deep(img) {
  display: block;
  max-width: 100%;
  height: auto;
  margin: 1.5em auto;
  border-radius: 24px;
}

.blog-content :deep(blockquote) {
  border-left: 4px solid rgb(148 163 184);
  padding-left: 1em;
  color: rgb(100 116 139);
}

.blog-content :deep(pre) {
  overflow-x: auto;
  border-radius: 20px;
  padding: 1rem 1.25rem;
  background: rgb(15 23 42);
  color: rgb(226 232 240);
}

.blog-content :deep(code) {
  border-radius: 6px;
  background: rgb(241 245 249);
  padding: 0.15em 0.4em;
  font-size: 0.9em;
  color: rgb(15 23 42);
}

.blog-content :deep(pre code) {
  background: transparent;
  padding: 0;
  color: inherit;
}
</style>
