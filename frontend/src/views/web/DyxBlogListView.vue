<template>
  <section class="dyx-page-shell">
    <div class="dyx-glass-panel rounded-[32px] p-8">
      <p class="text-sm uppercase tracking-[0.35em] text-slate-500">博客文章</p>
      <h1 class="mt-4 text-4xl font-semibold text-slate-900">发布技术、思考与实践复盘。</h1>
      <div class="mt-8 grid gap-5">
        <RouterLink
          v-for="item in posts"
          :key="item.id"
          :to="`/blog/${item.id}`"
          class="rounded-[28px] border border-slate-200/70 bg-white/80 p-6 transition hover:-translate-y-1 hover:shadow-dyx-soft"
        >
          <p class="text-sm text-slate-500">{{ item.category || '未分类' }}</p>
          <h2 class="mt-3 text-2xl font-semibold text-slate-900">{{ item.title }}</h2>
          <p class="mt-4 text-sm leading-7 text-slate-600">{{ item.summary || '暂无摘要' }}</p>
        </RouterLink>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { getPosts, type PostData } from '@/api/modules/site';

const posts = ref<PostData[]>([]);

/**
 * 获取博客列表数据。
 */
async function loadPosts(): Promise<void> {
  const response = await getPosts();
  posts.value = response.data ?? [];
}

onMounted(() => {
  void loadPosts();
});
</script>
