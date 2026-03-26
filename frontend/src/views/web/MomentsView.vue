<template>
  <section class="dyx-page-shell space-y-6 px-2 sm:px-0">
    <div class="dyx-page-card rounded-[34px] p-7 lg:p-8">
      <p class="text-sm uppercase tracking-[0.35em] dyx-text-meta">最新动态</p>
      <h1 class="mt-4 text-4xl font-semibold dyx-text-main">最近动态</h1>
      <div class="mt-8 grid gap-4">
        <RouterLink
          v-for="item in moments"
          :key="item.id"
          :to="`/moments/${item.id}`"
          class="dyx-page-card group block rounded-[26px] p-5 shadow-dyx-soft transition hover:-translate-y-0.5"
        >
          <div class="flex items-start justify-between gap-4">
            <div>
              <p class="text-sm dyx-text-meta">{{ formatDateYmd(item.happenedAt) || '未设置时间' }}</p>
              <h2 class="mt-2 text-xl font-semibold dyx-text-main transition-opacity group-hover:opacity-80">{{ item.title }}</h2>
            </div>
            <span class="text-xs dyx-text-meta">查看详情 →</span>
          </div>
          <p class="mt-3 text-sm leading-7 dyx-text-muted">{{ item.content || '暂无内容描述' }}</p>
          <div v-if="resolveMedia(item).length" class="mt-5 grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
            <template v-for="(url, index) in resolveMedia(item)" :key="`${item.id}-${index}`">
              <el-image
                v-if="isImageUrl(url)"
                :src="url"
                :preview-src-list="resolveImageMedia(item)"
                :initial-index="resolveImageMedia(item).indexOf(url)"
                fit="cover"
                preview-teleported
                class="block h-40 w-full overflow-hidden rounded-2xl"
              />
              <video
                v-else-if="isVideoUrl(url)"
                :src="url"
                controls
                preload="metadata"
                class="block h-40 w-full rounded-2xl bg-black object-cover"
              ></video>
            </template>
          </div>
        </RouterLink>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { getMoments, recordSiteVisit, type MomentData } from '@/api/modules/site';
import { formatDateYmd } from '@/utils/date';
import { isImageUrl, isVideoUrl, parseImageUrls } from '@/utils/media';

const moments = ref<MomentData[]>([]);

async function loadMoments(): Promise<void> {
  const response = await getMoments();
  moments.value = response.data ?? [];
}

function resolveMedia(item: MomentData): string[] {
  const mediaUrls = parseImageUrls(item.imageUrls);
  if (item.coverImage && !mediaUrls.includes(item.coverImage)) {
    return [item.coverImage, ...mediaUrls];
  }
  return mediaUrls;
}

function resolveImageMedia(item: MomentData): string[] {
  return resolveMedia(item).filter((url) => isImageUrl(url));
}

onMounted(() => {
  void recordSiteVisit('moments');
  void loadMoments();
});
</script>
