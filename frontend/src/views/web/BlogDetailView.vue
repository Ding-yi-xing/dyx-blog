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
      <div v-if="post.updatedAt" class="mt-4 flex flex-wrap items-center gap-3 text-[12px] dyx-text-meta">
        <span>{{ formatDateYmd(post.updatedAt) }}</span>
      </div>
    </article>

    <article class="dyx-page-card rounded-[36px] px-6 py-8 md:px-8 md:py-10">
      <el-image
        v-if="post.coverImage"
        :src="post.coverImage"
        :preview-src-list="[post.coverImage]"
        fit="cover"
        preview-teleported
        class="mb-8 block max-h-[460px] w-full overflow-hidden rounded-[28px]"
      />
      <div class="max-w-none">
        <p class="whitespace-pre-line text-[15px] leading-8 dyx-text-muted">
          {{ post.content || post.summary || '暂无正文内容。' }}
        </p>
      </div>
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

const route = useRoute();
const post = ref<Partial<PostData>>({});

async function loadPostDetail(): Promise<void> {
  const response = await getPostDetail(String(route.params.id));
  post.value = response.data ?? {};
}

onMounted(() => {
  void recordSiteVisit('blog-detail');
  void loadPostDetail();
});
</script>
