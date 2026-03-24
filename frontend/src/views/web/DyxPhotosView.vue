<template>
  <section class="dyx-page-shell">
    <div class="dyx-glass-panel rounded-[32px] p-8">
      <p class="text-sm uppercase tracking-[0.35em] text-slate-500">个人照片</p>
      <h1 class="mt-4 text-4xl font-semibold text-slate-900">用影像延伸内容表达。</h1>
      <div class="mt-8 grid gap-5 sm:grid-cols-2 xl:grid-cols-3">
        <article v-for="item in photos" :key="item.id" class="overflow-hidden rounded-[28px] border border-slate-200/70 bg-white/80">
          <img v-if="item.imageUrl" :src="item.imageUrl" :alt="item.title" class="h-56 w-full object-cover" />
          <div v-else class="h-56 bg-gradient-to-br from-slate-200 to-slate-100" />
          <div class="p-5">
            <h2 class="text-lg font-semibold">{{ item.title }}</h2>
            <p class="mt-2 text-sm leading-7 text-slate-600">{{ item.description || '暂无图片描述' }}</p>
          </div>
        </article>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { getPhotos, type PhotoData } from '@/api/modules/site';

const photos = ref<PhotoData[]>([]);

/**
 * 获取照片列表数据。
 */
async function loadPhotos(): Promise<void> {
  const response = await getPhotos();
  photos.value = response.data ?? [];
}

onMounted(() => {
  void loadPhotos();
});
</script>
