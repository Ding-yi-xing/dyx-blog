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
      <div v-if="post.updatedAt && !errorMessage" class="mt-4 flex flex-wrap items-center gap-3 text-[12px] dyx-text-meta">
        <span>{{ formatDateYmd(post.updatedAt) }}</span>
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
        <div class="max-w-none">
          <p class="whitespace-pre-line text-[15px] leading-8 dyx-text-muted">
            {{ post.content || post.summary || '暂无正文内容。' }}
          </p>
        </div>
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
import { onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import { getPostDetail, recordSiteVisit, type PostData } from '@/api/modules/site';
import { formatDateYmd } from '@/utils/date';
import { resolveErrorMessage } from '@/utils/error';

const route = useRoute();
const post = ref<Partial<PostData>>({});
const loading = ref(false);
const errorMessage = ref('');

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
