<template>
  <section class="dyx-page-shell">
    <article class="dyx-glass-panel rounded-[32px] p-8 lg:p-10">
      <p class="text-sm uppercase tracking-[0.35em] text-slate-500">博客详情</p>
      <h1 class="mt-4 text-4xl font-semibold text-slate-900">{{ post.title || `文章 ${route.params.id}` }}</h1>
      <p class="mt-3 text-sm text-slate-500">{{ post.category || '未分类' }}</p>
      <p class="mt-6 text-base leading-8 text-slate-600">
        {{ post.content || post.summary || '这里将用于展示博客正文、封面图、标签、发布时间和相关推荐等内容。' }}
      </p>
    </article>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import { getPostDetail, type PostData } from '@/api/modules/site';

const route = useRoute();
const post = ref<Partial<PostData>>({});

/**
 * 获取博客详情数据。
 */
async function loadPostDetail(): Promise<void> {
  const response = await getPostDetail(String(route.params.id));
  post.value = response.data ?? {};
}

onMounted(() => {
  void loadPostDetail();
});
</script>
