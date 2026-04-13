<template>
  <section class="dyx-page-shell space-y-6 px-2 sm:px-0">
    <div class="dyx-page-card rounded-[34px] p-7 lg:p-8">
      <p class="text-sm uppercase tracking-[0.35em] dyx-text-meta">最新动态</p>
      <h1 class="mt-4 text-4xl font-semibold dyx-text-main">最近动态</h1>
      <div class="mt-8">
        <div v-if="loading" class="grid gap-4">
          <el-skeleton
            v-for="n in 4"
            :key="`moment-skeleton-${n}`"
            animated
            :rows="3"
            class="dyx-page-card rounded-[26px] p-5 shadow-dyx-soft"
          />
        </div>

        <div v-else-if="moments.length" class="grid gap-4">
          <component
            :is="item.detailRoute ? 'RouterLink' : 'article'"
            v-for="item in momentCards"
            :key="item.id"
            :to="item.detailRoute || undefined"
            class="dyx-page-card group block rounded-[26px] p-5 shadow-dyx-soft transition hover:-translate-y-0.5"
          >
            <div class="flex items-start justify-between gap-4">
              <div>
                <p class="text-sm dyx-text-meta">{{ formatDateYmd(item.happenedAt) || '未设置时间' }}</p>
                <h2 class="mt-2 text-xl font-semibold dyx-text-main transition-opacity group-hover:opacity-80">{{ item.title }}</h2>
              </div>
              <span class="text-xs dyx-text-meta">{{ item.detailRoute ? '查看详情 →' : '详情不可用' }}</span>
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
          </component>
        </div>

        <p
          v-else
          class="mt-6 rounded-[24px] border border-dashed border-[rgb(var(--dyx-border-subtle-rgb)/0.72)] bg-[rgb(var(--dyx-bg-surface-rgb)/0.34)] px-5 py-6 text-center text-sm dyx-text-meta"
        >
          暂无最近动态。
        </p>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { getMoments, recordSiteVisit, type MomentData } from '@/api/modules/site';
import { formatDateYmd } from '@/utils/date';
import { isImageUrl, isVideoUrl, parseImageUrls } from '@/utils/media';

const moments = ref<MomentData[]>([]);
const loading = ref(false);

async function loadMoments(): Promise<void> {
  loading.value = true;
  try {
    const response = await getMoments();
    moments.value = response.data ?? [];
  } finally {
    loading.value = false;
  }
}

function resolveMomentId(rawId: unknown): string | null {
  if (Array.isArray(rawId)) {
    return null;
  }
  if (typeof rawId !== 'string' && typeof rawId !== 'number') {
    return null;
  }
  const normalized = String(rawId).trim();
  if (!/^\d+$/.test(normalized)) {
    return null;
  }
  return Number(normalized) > 0 ? normalized : null;
}

function resolveMomentDetailRoute(rawId: unknown): string | null {
  const momentId = resolveMomentId(rawId);
  return momentId ? `/moments/${momentId}` : null;
}

const momentCards = computed(() =>
  moments.value.map((item) => ({
    ...item,
    detailRoute: resolveMomentDetailRoute(item.id)
  }))
);

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
