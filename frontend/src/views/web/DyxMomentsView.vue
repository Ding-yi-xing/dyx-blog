<template>
  <section class="dyx-page-shell">
    <div class="dyx-glass-panel rounded-[32px] p-8">
      <p class="text-sm uppercase tracking-[0.35em] text-slate-500">最新动态</p>
      <h1 class="mt-4 text-4xl font-semibold text-slate-900">记录最近的成长与更新。</h1>
      <div class="mt-8 grid gap-4">
        <article v-for="item in moments" :key="item.id" class="rounded-[24px] border border-slate-200/70 bg-white/70 p-5">
          <p class="text-sm text-slate-500">{{ item.happenedAt || '未设置时间' }}</p>
          <h2 class="mt-2 text-xl font-semibold">{{ item.title }}</h2>
          <p class="mt-3 text-sm leading-7 text-slate-600">{{ item.content || '暂无内容描述' }}</p>
          <div v-if="resolveImages(item).length" class="mt-5 grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
            <img
              v-for="(url, index) in resolveImages(item)"
              :key="`${item.id}-${index}`"
              :src="url"
              :alt="item.title"
              class="h-40 w-full rounded-2xl object-cover"
            />
          </div>
        </article>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { getMoments, type MomentData } from '@/api/modules/site';
import { parseImageUrls } from '@/utils/media';

const moments = ref<MomentData[]>([]);

/**
 * 获取动态列表数据。
 */
async function loadMoments(): Promise<void> {
  const response = await getMoments();
  moments.value = response.data ?? [];
}

function resolveImages(item: MomentData): string[] {
  const imageUrls = parseImageUrls(item.imageUrls);
  if (item.coverImage && !imageUrls.includes(item.coverImage)) {
    return [item.coverImage, ...imageUrls];
  }
  return imageUrls;
}

onMounted(() => {
  void loadMoments();
});
</script>
