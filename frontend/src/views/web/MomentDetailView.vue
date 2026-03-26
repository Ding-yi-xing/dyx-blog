<template>
  <section class="dyx-page-shell space-y-6 px-2 sm:px-0">
    <article class="dyx-page-card-elevated rounded-[36px] px-6 py-8 md:px-8 md:py-9">
      <RouterLink to="/moments" class="inline-flex items-center text-[12px] font-medium dyx-text-meta transition-opacity hover:opacity-80">
        <span class="mr-1 text-base">←</span>
        返回动态列表
      </RouterLink>
      <p class="mt-4 text-[11px] font-semibold uppercase tracking-[0.32em] dyx-text-meta">Moment detail</p>
      <h1 class="mt-3 text-3xl font-semibold tracking-tight dyx-text-main md:text-4xl">
        {{ moment.title || `动态 ${route.params.id}` }}
      </h1>
      <div class="mt-4 flex flex-wrap items-center gap-3 text-[12px] dyx-text-meta">
        <span>{{ formatDateYmd(moment.happenedAt) || '未设置时间' }}</span>
      </div>
    </article>

    <article class="dyx-page-card rounded-[36px] px-6 py-8 md:px-8 md:py-10">
      <p class="whitespace-pre-line text-[15px] leading-8 dyx-text-muted">
        {{ moment.content || '暂无正文内容。' }}
      </p>

      <div v-if="mediaItems.length" class="mt-8 grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
        <template v-for="(url, index) in mediaItems" :key="`${url}-${index}`">
          <el-image
            v-if="isImageUrl(url)"
            :src="url"
            :preview-src-list="imageItems"
            :initial-index="imageItems.indexOf(url)"
            fit="cover"
            preview-teleported
            class="block h-56 w-full overflow-hidden rounded-[24px]"
          />
          <video
            v-else-if="isVideoUrl(url)"
            :src="url"
            controls
            preload="metadata"
            class="block h-56 w-full rounded-[24px] bg-black object-cover"
          ></video>
        </template>
      </div>
    </article>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import { getMomentDetail, recordSiteVisit, type MomentData } from '@/api/modules/site';
import { formatDateYmd } from '@/utils/date';
import { isImageUrl, isVideoUrl, parseImageUrls } from '@/utils/media';

const route = useRoute();
const moment = ref<Partial<MomentData>>({});

const mediaItems = computed(() => {
  const mediaUrls = parseImageUrls(moment.value.imageUrls);
  if (moment.value.coverImage && !mediaUrls.includes(moment.value.coverImage)) {
    return [moment.value.coverImage, ...mediaUrls];
  }
  return mediaUrls;
});

const imageItems = computed(() => mediaItems.value.filter((url) => isImageUrl(url)));

async function loadMomentDetail(): Promise<void> {
  const response = await getMomentDetail(String(route.params.id));
  moment.value = response.data ?? {};
}

onMounted(() => {
  void recordSiteVisit('moment-detail');
  void loadMomentDetail();
});
</script>
