<template>
  <section class="dyx-page-shell space-y-8 px-2 sm:px-0">
    <div class="dyx-page-card-elevated rounded-[38px] p-8 lg:p-10">
      <div class="grid gap-10 lg:grid-cols-[0.72fr_1.28fr] lg:items-start">
        <div class="dyx-page-card rounded-[32px] p-7 text-center shadow-dyx-soft">
          <el-image
            v-if="profile.avatarUrl"
            :src="profile.avatarUrl"
            :preview-src-list="[profile.avatarUrl]"
            fit="cover"
            preview-teleported
            class="mx-auto h-32 w-32 overflow-hidden rounded-full border border-[rgb(var(--dyx-border-subtle-rgb)/0.6)]"
          />
          <div v-else class="mx-auto h-32 w-32 rounded-full border border-[rgb(var(--dyx-border-subtle-rgb)/0.6)] bg-[rgb(var(--dyx-bg-surface-muted-rgb)/0.7)]" />
          <p class="mt-6 text-sm uppercase tracking-[0.35em] dyx-text-meta">About</p>
          <h1 class="mt-4 text-3xl font-semibold dyx-text-main">关于我</h1>
          <p class="mt-3 text-sm leading-7 dyx-text-muted">这里展示个人介绍、联系方式、作品与荣誉时间线。</p>
          <div class="mt-6 flex flex-wrap justify-center gap-3">
            <RouterLink
              to="/resume"
              class="inline-flex rounded-full bg-[rgb(var(--dyx-text-main-rgb))] px-5 py-3 text-sm text-[rgb(var(--dyx-text-inverse-rgb))]"
            >
              查看简历
            </RouterLink>
            <a
              v-if="profile.resumePdfUrl"
              :href="profile.resumePdfUrl"
              download
              target="_blank"
              rel="noreferrer"
              class="dyx-ghost-pill inline-flex"
            >
              下载 PDF 简历
            </a>
          </div>
        </div>

        <div class="space-y-5">
          <article class="dyx-page-card rounded-[30px] p-6 shadow-dyx-soft">
            <p class="text-sm uppercase tracking-[0.3em] dyx-text-meta">个人介绍</p>
            <p class="mt-5 text-sm leading-8 dyx-text-muted">
              {{ profile.aboutContent || '暂无个人介绍。' }}
            </p>
          </article>

          <article class="dyx-page-card rounded-[30px] p-6 shadow-dyx-soft">
            <p class="text-sm uppercase tracking-[0.3em] dyx-text-meta">联系方式</p>
            <div class="mt-5 grid gap-4 text-sm sm:grid-cols-2">
              <div class="rounded-[22px] bg-[rgb(var(--dyx-bg-surface-muted-rgb)/0.72)] px-4 py-4">
                <p class="text-xs uppercase tracking-[0.24em] dyx-text-meta">Email</p>
                <p class="mt-2 break-all text-sm dyx-text-main">{{ profile.email || '-' }}</p>
              </div>
              <div class="rounded-[22px] bg-[rgb(var(--dyx-bg-surface-muted-rgb)/0.72)] px-4 py-4">
                <p class="text-xs uppercase tracking-[0.24em] dyx-text-meta">Phone</p>
                <p class="mt-2 text-sm dyx-text-main">{{ profile.phone || '-' }}</p>
              </div>
              <div class="rounded-[22px] bg-[rgb(var(--dyx-bg-surface-muted-rgb)/0.72)] px-4 py-4">
                <p class="text-xs uppercase tracking-[0.24em] dyx-text-meta">WeChat</p>
                <p class="mt-2 text-sm dyx-text-main">{{ profile.wechat || '-' }}</p>
              </div>
              <div class="rounded-[22px] bg-[rgb(var(--dyx-bg-surface-muted-rgb)/0.72)] px-4 py-4">
                <p class="text-xs uppercase tracking-[0.24em] dyx-text-meta">GitHub</p>
                <p class="mt-2 break-all text-sm dyx-text-main">{{ profile.githubUrl || '-' }}</p>
              </div>
            </div>
          </article>
        </div>
      </div>
    </div>

    <div class="dyx-page-card rounded-[38px] p-8 lg:p-10">
      <div class="flex flex-wrap items-end justify-between gap-4">
        <p class="text-sm uppercase tracking-[0.35em] dyx-text-meta">个人作品</p>
      </div>

      <div class="mt-8 grid gap-5 xl:grid-cols-3">
        <article
          v-for="item in works"
          :key="item.id"
          class="dyx-page-card rounded-[28px] overflow-hidden shadow-dyx-soft transition hover:-translate-y-1"
        >
          <el-image
            v-if="resolveWorkCover(item)"
            :src="resolveWorkCover(item)"
            :preview-src-list="resolveWorkImages(item)"
            fit="cover"
            preview-teleported
            class="block h-48 w-full"
          />
          <div class="p-6">
            <div class="flex flex-wrap items-center gap-3 text-xs dyx-text-meta">
              <span class="rounded-full bg-[rgb(var(--dyx-bg-surface-muted-rgb)/0.8)] px-3 py-1.5 dyx-text-main">{{ item.videoUrl ? '视频作品' : '图文作品' }}</span>
              <span>{{ resolveWorkImages(item).length }} 张素材</span>
            </div>
            <h3 class="mt-4 text-2xl font-semibold dyx-text-main">{{ item.title }}</h3>
            <p class="mt-4 text-sm leading-7 dyx-text-muted">{{ item.summary || '暂无作品说明。' }}</p>
            <a
              v-if="item.workLink"
              :href="item.workLink"
              target="_blank"
              rel="noreferrer"
              class="dyx-ghost-pill mt-5 inline-flex"
            >
              查看作品链接
            </a>
          </div>
        </article>

        <article v-if="!works.length" class="rounded-[28px] border border-dashed border-[rgb(var(--dyx-border-subtle-rgb)/0.72)] bg-[rgb(var(--dyx-bg-surface-rgb)/0.36)] p-8 text-sm dyx-text-meta xl:col-span-3">
          暂无已发布作品内容。
        </article>
      </div>
    </div>

    <div class="dyx-page-card rounded-[38px] p-8 lg:p-10">
      <div class="flex flex-wrap items-end justify-between gap-4">
        <p class="text-sm uppercase tracking-[0.35em] dyx-text-meta">荣誉时间线</p>
      </div>

      <div ref="honorsScroller" class="honors-scroll mt-10 overflow-x-auto pb-4" @wheel="handleHonorsWheel">
        <div v-if="honors.length" class="relative flex min-w-max items-start gap-6 px-1 py-4">
          <div class="absolute left-0 right-0 top-[74px] h-px bg-[rgb(var(--dyx-border-subtle-rgb)/0.8)]"></div>

          <article
            v-for="item in honors"
            :key="item.id"
            class="relative w-[340px] flex-none snap-start pt-24 first:ml-1 last:mr-1 md:w-[380px]"
          >
            <p class="absolute left-0 top-0 text-sm font-medium tracking-[0.08em] dyx-text-meta">{{ formatAwardDate(item.awardAt) }}</p>
            <div class="absolute left-0 top-[66px] h-4 w-4 rounded-full border-4 border-[rgb(var(--dyx-bg-surface-rgb))] bg-[rgb(var(--dyx-text-main-rgb))] shadow-sm"></div>

            <div class="dyx-page-card rounded-[28px] p-6 shadow-dyx-soft transition hover:-translate-y-1">
              <div class="flex flex-wrap items-center gap-3">
                <h3 class="text-2xl font-semibold dyx-text-main">{{ item.title }}</h3>
                <span v-if="item.issuer" class="rounded-full bg-[rgb(var(--dyx-bg-surface-muted-rgb)/0.8)] px-3 py-1 text-xs dyx-text-meta">{{ item.issuer }}</span>
              </div>
              <p class="mt-4 text-sm leading-7 dyx-text-muted">{{ item.description || '暂无荣誉说明。' }}</p>
              <div v-if="resolveHonorImages(item).length" class="mt-6 grid gap-3 sm:grid-cols-2">
                <el-image
                  v-for="(url, index) in resolveHonorImages(item)"
                  :key="`${item.id}-${index}`"
                  :src="url"
                  :preview-src-list="resolveHonorImages(item)"
                  :initial-index="index"
                  fit="cover"
                  preview-teleported
                  class="block h-40 w-full overflow-hidden rounded-2xl"
                />
              </div>
              <a
                v-if="item.attachmentUrl"
                :href="item.attachmentUrl"
                target="_blank"
                rel="noreferrer"
                class="dyx-ghost-pill mt-5 inline-flex"
              >
                查看证书附件
              </a>
            </div>
          </article>
        </div>

        <article v-else class="rounded-[28px] border border-dashed border-[rgb(var(--dyx-border-subtle-rgb)/0.72)] bg-[rgb(var(--dyx-bg-surface-rgb)/0.36)] p-8 text-sm dyx-text-meta">
          暂无已发布荣誉内容。
        </article>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { getHonors, getProfile, getWorks, recordSiteVisit, type HonorData, type ProfileData, type WorkData } from '@/api/modules/site';
import { isImageUrl, parseImageUrls } from '@/utils/media';

const profile = ref<ProfileData>({});
const honors = ref<HonorData[]>([]);
const works = ref<WorkData[]>([]);
const honorsScroller = ref<HTMLElement>();

async function loadAboutData(): Promise<void> {
  const [profileResponse, honorResponse, workResponse] = await Promise.allSettled([getProfile(), getHonors(), getWorks()]);
  profile.value = profileResponse.status === 'fulfilled' ? (profileResponse.value.data ?? {}) : {};
  honors.value = honorResponse.status === 'fulfilled' ? (honorResponse.value.data ?? []) : [];
  works.value = workResponse.status === 'fulfilled' ? (workResponse.value.data ?? []) : [];
}

function resolveWorkImages(item: WorkData): string[] {
  const imageUrls = parseImageUrls(item.imageUrls).filter((url) => isImageUrl(url));
  const firstImage = item.coverImage && isImageUrl(item.coverImage) ? item.coverImage : item.videoPoster && isImageUrl(item.videoPoster) ? item.videoPoster : '';
  return firstImage && !imageUrls.includes(firstImage) ? [firstImage, ...imageUrls] : imageUrls;
}

function resolveWorkCover(item: WorkData): string {
  return resolveWorkImages(item)[0] ?? '';
}

function resolveHonorImages(item: HonorData): string[] {
  const imageUrls = parseImageUrls(item.imageUrls).filter((url) => isImageUrl(url));
  if (item.coverImage && isImageUrl(item.coverImage) && !imageUrls.includes(item.coverImage)) {
    return [item.coverImage, ...imageUrls];
  }
  return imageUrls;
}

function formatAwardDate(value?: string): string {
  if (!value) {
    return '未设置时间';
  }
  return value.slice(0, 10);
}

function handleHonorsWheel(event: WheelEvent): void {
  const container = honorsScroller.value;
  if (!container) {
    return;
  }
  if (Math.abs(event.deltaY) <= Math.abs(event.deltaX)) {
    return;
  }
  const maxScrollLeft = container.scrollWidth - container.clientWidth;
  if (maxScrollLeft <= 0) {
    return;
  }
  const nextScrollLeft = container.scrollLeft + event.deltaY;
  const clampedScrollLeft = Math.min(maxScrollLeft, Math.max(0, nextScrollLeft));
  if (clampedScrollLeft === container.scrollLeft) {
    return;
  }
  event.preventDefault();
  container.scrollLeft = clampedScrollLeft;
}

onMounted(() => {
  void recordSiteVisit('profile');
  void loadAboutData();
});
</script>

<style scoped>
.honors-scroll {
  -ms-overflow-style: none;
  scrollbar-width: none;
}

.honors-scroll::-webkit-scrollbar {
  display: none;
}
</style>
