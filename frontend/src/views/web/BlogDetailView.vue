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

async function loadPostDetail(): Promise<void> {
  loading.value = true;
  errorMessage.value = '';
  try {
    const response = await getPostDetail(String(route.params.id));
    const nextPost = response.data ?? {};
    if (!nextPost.id) {
      post.value = {};
      errorMessage.value = '文章不存在或未发布。';
      return;
    }
    post.value = nextPost;
  } catch (error) {
    post.value = {};
    errorMessage.value = resolveErrorMessage(error, '文章加载失败，请稍后重试。');
  } finally {
    loading.value = false;
  }
}

onMounted(() => {
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
