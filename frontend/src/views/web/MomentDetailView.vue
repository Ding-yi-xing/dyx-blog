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
      <div v-if="!errorMessage" class="mt-4 flex flex-wrap items-center gap-3 text-[12px] dyx-text-meta">
        <span>{{ formatDateYmd(moment.happenedAt) || '未设置时间' }}</span>
      </div>
    </article>

    <article class="dyx-page-card rounded-[36px] px-6 py-8 md:px-8 md:py-10">
      <div v-if="loading" class="space-y-4">
        <el-skeleton animated :rows="4" />
      </div>
      <p v-else-if="errorMessage" class="text-[15px] leading-8 text-rose-500">
        {{ errorMessage }}
      </p>
      <template v-else>
        <p class="whitespace-pre-line text-[15px] leading-8 dyx-text-muted">
          {{ moment.content || '暂无正文内容。' }}
        </p>

        <div v-if="mediaItems.length" class="mt-8 grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
          <template v-for="(url, index) in mediaItems" :key="`${url}-${index}`">
            <div
              v-if="isImageUrl(url)"
              class="flex aspect-[4/3] items-center justify-center overflow-hidden rounded-[24px]"
            >
              <el-image
                :src="url"
                :preview-src-list="imageItems"
                :initial-index="imageItems.indexOf(url)"
                fit="contain"
                preview-teleported
                class="h-full w-full"
              />
            </div>
            <video
              v-else-if="isVideoUrl(url)"
              :src="url"
              controls
              preload="metadata"
              class="block aspect-[4/3] w-full rounded-[24px] bg-black object-contain"
            ></video>
          </template>
        </div>
      </template>
    </article>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import { getMomentDetail, recordSiteVisit, type MomentData } from '@/api/modules/site';
import { formatDateYmd } from '@/utils/date';
import { resolveErrorMessage } from '@/utils/error';
import { isImageUrl, isVideoUrl, parseImageUrls } from '@/utils/media';

const route = useRoute();
const moment = ref<Partial<MomentData>>({});
const loading = ref(false);
const errorMessage = ref('');

const mediaItems = computed(() => {
  const mediaUrls = parseImageUrls(moment.value.imageUrls);
  if (moment.value.coverImage && !mediaUrls.includes(moment.value.coverImage)) {
    return [moment.value.coverImage, ...mediaUrls];
  }
  return mediaUrls;
});

const imageItems = computed(() => mediaItems.value.filter((url) => isImageUrl(url)));

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

async function loadMomentDetail(rawId: unknown): Promise<void> {
  const momentId = resolveMomentId(rawId);
  if (!momentId) {
    moment.value = {};
    errorMessage.value = '动态地址无效。';
    loading.value = false;
    return;
  }

  loading.value = true;
  errorMessage.value = '';
  try {
    const response = await getMomentDetail(momentId);
    const nextMoment = response.data ?? {};
    if (!nextMoment.id) {
      moment.value = {};
      errorMessage.value = '动态不存在或未发布。';
      return;
    }
    moment.value = nextMoment;
  } catch (error) {
    moment.value = {};
    errorMessage.value = resolveErrorMessage(error, '动态加载失败，请稍后重试。');
  } finally {
    loading.value = false;
  }
}

watch(
  () => route.params.id,
  (nextId) => {
    void loadMomentDetail(nextId);
  },
  { immediate: true }
);

onMounted(() => {
  void recordSiteVisit('moment-detail');
});
</script>
