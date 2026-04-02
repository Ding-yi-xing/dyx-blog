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
          </div>
        </div>

        <div class="space-y-5">
          <article class="dyx-page-card rounded-[30px] p-6 shadow-dyx-soft">
            <p class="text-sm uppercase tracking-[0.3em] dyx-text-meta">个人介绍</p>
            <p class="mt-5 whitespace-pre-line text-sm leading-8 dyx-text-muted">
              {{ profile.aboutContent || '暂无个人介绍。' }}
            </p>
          </article>

          <article class="dyx-page-card rounded-[30px] p-6 shadow-dyx-soft">
            <p class="text-sm uppercase tracking-[0.3em] dyx-text-meta">联系方式</p>
            <div v-if="linkedContactMethods.length" class="mt-5 grid gap-4 text-sm sm:grid-cols-2">
              <div
                v-for="(item, index) in linkedContactMethods"
                :key="`${item.label || item.type}-${index}`"
                class="rounded-[22px] bg-[rgb(var(--dyx-bg-surface-muted-rgb)/0.72)] px-4 py-4"
              >
                <p class="text-xs uppercase tracking-[0.24em] dyx-text-meta">{{ item.label || item.type || '联系' }}</p>
                <a
                  v-if="item.href"
                  :href="item.href"
                  :target="item.external ? '_blank' : undefined"
                  :rel="item.external ? 'noreferrer' : undefined"
                  class="mt-2 block break-all text-sm dyx-text-main underline-offset-4 transition hover:underline"
                >
                  {{ item.value || '-' }}
                </a>
                <p v-else class="mt-2 break-all text-sm dyx-text-main">{{ item.value || '-' }}</p>
              </div>
            </div>
            <p v-else class="mt-5 text-sm dyx-text-meta">暂无联系方式。</p>
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
          class="dyx-page-card overflow-hidden rounded-[28px] shadow-dyx-soft transition hover:-translate-y-1"
        >
          <div v-if="resolveWorkMediaCount(item)" class="relative">
            <button
              type="button"
              class="work-media-shell group relative block h-48 w-full overflow-hidden bg-black text-left"
              @click="openWorkViewer(item)"
            >
              <video
                v-if="shouldRenderWorkVideoCover(item)"
                :src="resolveVideoPosterAtSecond(item.videoUrl)"
                muted
                playsinline
                webkit-playsinline="true"
                x5-playsinline="true"
                x5-video-player-type="h5"
                preload="metadata"
                class="block h-48 w-full object-cover"
              ></video>
              <el-image
                v-else-if="resolveWorkCover(item)"
                :src="resolveWorkCover(item)"
                fit="cover"
                class="block h-48 w-full transition duration-300 group-hover:scale-[1.03]"
              />
              <div v-else class="flex h-48 w-full items-center justify-center bg-[rgb(var(--dyx-bg-surface-muted-rgb)/0.18)] text-sm text-white/72">
                暂无预览素材
              </div>

              <div class="work-media-overlay">
                <span>{{ resolveWorkMediaCount(item) > 1 ? `查看全部 ${resolveWorkMediaCount(item)} 项素材` : '查看素材' }}</span>
              </div>

              <div v-if="resolveWorkHasVideo(item)" class="work-media-play">
                <span class="work-media-play-icon"></span>
              </div>

              <div v-if="resolveWorkMediaCount(item) > 1" class="work-media-badge">
                <span class="work-media-badge-dot"></span>
                <span>{{ resolveWorkMediaCount(item) }} 项素材</span>
              </div>

              <div v-if="resolveWorkPreviewStack(item).length" class="work-media-stack" aria-hidden="true">
                <button
                  v-for="preview in resolveWorkPreviewStack(item)"
                  :key="preview.key"
                  type="button"
                  class="work-media-stack-item"
                  :style="{
                    backgroundImage: `url(${preview.thumb})`,
                    transform: `translateX(${preview.offset * 12}px) rotate(${preview.offset === 0 ? '-4deg' : preview.offset === 1 ? '0deg' : '4deg'})`,
                    zIndex: String(6 - preview.offset)
                  }"
                  :aria-label="`查看素材 ${preview.offset + 1}`"
                  @click.stop="openWorkViewer(item, preview.index)"
                ></button>
              </div>
            </button>
          </div>
          <div class="p-6">
            <div class="flex flex-wrap items-center gap-3 text-xs dyx-text-meta">
              <span class="rounded-full bg-[rgb(var(--dyx-bg-surface-muted-rgb)/0.8)] px-3 py-1.5 dyx-text-main">{{ resolveWorkTypeText(item) }}</span>
              <span>{{ resolveWorkMediaCount(item) }} 项素材</span>
              <span v-if="item.workLink">含作品链接</span>
            </div>
            <h3 class="mt-4 text-2xl font-semibold dyx-text-main">{{ item.title }}</h3>
            <p class="mt-4 text-sm leading-7 dyx-text-muted">{{ item.summary || '暂无作品说明。' }}</p>
            <div class="mt-5 flex flex-wrap gap-3">
              <button type="button" class="dyx-ghost-pill inline-flex" @click="openWorkViewer(item)">
                查看素材
              </button>
              <a
                v-if="item.workLink"
                :href="item.workLink"
                target="_blank"
                rel="noreferrer"
                class="dyx-ghost-pill inline-flex"
              >
                查看作品链接
              </a>
            </div>
          </div>
        </article>

        <article v-if="!works.length" class="xl:col-span-3 rounded-[28px] border border-dashed border-[rgb(var(--dyx-border-subtle-rgb)/0.72)] bg-[rgb(var(--dyx-bg-surface-rgb)/0.36)] p-8 text-sm dyx-text-meta">
          暂无已发布作品内容。
        </article>
      </div>
    </div>

    <el-dialog
      v-model="workViewerVisible"
      width="min(1080px, calc(100vw - 24px))"
      top="108px"
      append-to-body
      :modal-class="activeTheme === 'dark' ? 'work-viewer-overlay work-viewer-overlay--dark' : 'work-viewer-overlay work-viewer-overlay--light'"
      :class="activeTheme === 'dark' ? 'work-viewer-dialog work-viewer-dialog--dark' : 'work-viewer-dialog work-viewer-dialog--light'"
      destroy-on-close
    >
      <template #header>
        <div class="work-viewer-header" :class="activeTheme === 'dark' ? 'work-viewer-header--dark' : 'work-viewer-header--light'">
          <div class="work-viewer-header-main">
            <div>
              <p class="text-sm uppercase tracking-[0.28em] dyx-text-meta">作品素材</p>
              <h3 class="mt-2 text-2xl font-semibold dyx-text-main">{{ activeWork?.title || '作品预览' }}</h3>
              <p class="mt-2 text-sm dyx-text-muted">
                {{ activeWorkMediaItems.length }} 项素材 · {{ resolveWorkTypeText(activeWork) }}
              </p>
            </div>
            <a
              v-if="activeWork?.workLink"
              :href="activeWork.workLink"
              target="_blank"
              rel="noreferrer"
              class="dyx-ghost-pill inline-flex"
            >
              打开作品链接
            </a>
          </div>
        </div>
      </template>

      <div v-if="activeWorkMedia" class="space-y-5">
        <div class="work-viewer-stage">
          <el-image
            v-if="activeWorkMedia.type === 'image'"
            :src="activeWorkMedia.src"
            :preview-src-list="activeWorkImageSources"
            :initial-index="activeWorkImageIndex"
            fit="contain"
            preview-teleported
            class="block h-full w-full"
          />
          <video
            v-else
            :src="resolveVideoPlaybackSrc(activeWorkMedia.src)"
            :poster="activeWorkMedia.thumb || undefined"
            controls
            playsinline
            webkit-playsinline="true"
            x5-playsinline="true"
            x5-video-player-type="h5"
            preload="metadata"
            class="block h-full w-full bg-black object-contain"
          ></video>
        </div>

        <div v-if="activeWorkMediaItems.length > 1" class="grid gap-3 sm:grid-cols-4 lg:grid-cols-6">
          <button
            v-for="(media, index) in activeWorkMediaItems"
            :key="media.key"
            type="button"
            class="work-viewer-thumb"
            :class="{ 'work-viewer-thumb-active': index === activeWorkMediaIndex }"
            @click="activeWorkMediaIndex = index"
          >
            <video
              v-if="media.type === 'video'"
              :src="resolveVideoPosterAtSecond(media.src)"
              :poster="media.thumb || undefined"
              muted
              playsinline
              webkit-playsinline="true"
              x5-playsinline="true"
              x5-video-player-type="h5"
              preload="metadata"
              class="block h-20 w-full rounded-[18px] bg-black object-cover"
            ></video>
            <el-image
              v-else-if="media.thumb"
              :src="media.thumb"
              fit="cover"
              class="block h-20 w-full rounded-[18px]"
            />
            <div v-else class="flex h-20 w-full items-center justify-center rounded-[18px] bg-[rgb(var(--dyx-bg-surface-muted-rgb)/0.7)] text-xs dyx-text-meta">
              无预览
            </div>
            <span class="work-viewer-thumb-tag">
              {{ media.type === 'video' ? '视频' : '图片' }}
            </span>
          </button>
        </div>
      </div>
      <p v-else class="text-sm dyx-text-meta">暂无可查看素材。</p>
    </el-dialog>

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
            <p class="absolute left-0 top-0 text-sm font-medium tracking-[0.08em] dyx-text-meta">{{ formatDateYmd(item.awardAt) || '未设置时间' }}</p>
            <div class="absolute left-0 top-[66px] h-4 w-4 rounded-full border-4 border-[rgb(var(--dyx-bg-surface-rgb))] bg-[rgb(var(--dyx-text-main-rgb))] shadow-sm"></div>

            <div class="dyx-page-card rounded-[28px] p-6 shadow-dyx-soft transition hover:-translate-y-1">
              <div class="flex flex-wrap items-center gap-3">
                <h3 class="text-2xl font-semibold dyx-text-main">{{ item.title }}</h3>
                <span v-if="item.issuer" class="rounded-full bg-[rgb(var(--dyx-bg-surface-muted-rgb)/0.8)] px-3 py-1 text-xs dyx-text-meta">{{ item.issuer }}</span>
              </div>
              <p class="mt-4 text-sm leading-7 dyx-text-muted">{{ item.description || '暂无荣誉说明。' }}</p>
              <div v-if="resolveHonorMedia(item).length" class="mt-6 grid gap-3 sm:grid-cols-2">
                <template v-for="(url, index) in resolveHonorMedia(item)" :key="`${item.id}-${index}`">
                  <div v-if="isImageUrl(url)" class="honor-media-shell flex h-40 w-full items-center justify-center overflow-hidden rounded-2xl bg-black">
                    <el-image
                      :src="url"
                      :preview-src-list="resolveHonorImages(item)"
                      :initial-index="resolveHonorImages(item).indexOf(url)"
                      fit="cover"
                      preview-teleported
                      class="block h-40 w-full"
                    />
                  </div>
                  <div v-else-if="isVideoUrl(url)" class="honor-media-shell flex h-40 w-full items-center justify-center overflow-hidden rounded-2xl bg-black">
                    <video
                      :src="url"
                      controls
                      preload="metadata"
                      class="block h-40 w-full rounded-2xl bg-black object-cover"
                    ></video>
                  </div>
                </template>
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
import { computed, inject, onMounted, ref, type Ref } from 'vue';
import {
  getHonors,
  getProfile,
  getWorks,
  isExternalContactHref,
  recordSiteVisit,
  resolveContactHref,
  resolveProfileContactMethods,
  type HonorData,
  type ProfileData,
  type WorkData
} from '@/api/modules/site';
import { formatDateYmd } from '@/utils/date';
import { isImageUrl, isVideoUrl, parseImageUrls } from '@/utils/media';

type ThemeMode = 'light' | 'dark';

interface WorkMediaItem {
  key: string;
  src: string;
  thumb: string;
  type: 'image' | 'video';
}

interface WorkPreviewItem {
  key: string;
  thumb: string;
  index: number;
  offset: number;
}

const profile = ref<ProfileData>({});
const honors = ref<HonorData[]>([]);
const works = ref<WorkData[]>([]);
const honorsScroller = ref<HTMLElement>();
const workViewerVisible = ref(false);
const activeWork = ref<WorkData | null>(null);
const activeWorkMediaIndex = ref(0);
const currentTheme = inject<Ref<ThemeMode> | undefined>('dyx-theme');

const activeTheme = computed<ThemeMode>(() => currentTheme?.value ?? 'dark');
const contactMethods = computed(() => resolveProfileContactMethods(profile.value));
const linkedContactMethods = computed(() =>
  contactMethods.value.map((item) => {
    const href = resolveContactHref(item);
    return {
      ...item,
      href,
      external: isExternalContactHref(href)
    };
  })
);
const activeWorkMediaItems = computed(() => (activeWork.value ? resolveWorkMediaItems(activeWork.value) : []));
const activeWorkMedia = computed(() => activeWorkMediaItems.value[activeWorkMediaIndex.value] ?? null);
const activeWorkImageSources = computed(() => activeWorkMediaItems.value.filter((item) => item.type === 'image').map((item) => item.src));
const activeWorkImageIndex = computed(() => {
  if (!activeWorkMedia.value || activeWorkMedia.value.type !== 'image') {
    return 0;
  }
  const index = activeWorkImageSources.value.indexOf(activeWorkMedia.value.src);
  return index >= 0 ? index : 0;
});

async function loadAboutData(): Promise<void> {
  const [profileResponse, honorResponse, workResponse] = await Promise.allSettled([getProfile(), getHonors(), getWorks()]);
  profile.value = profileResponse.status === 'fulfilled' ? (profileResponse.value.data ?? {}) : {};
  honors.value = honorResponse.status === 'fulfilled' ? (honorResponse.value.data ?? []) : [];
  works.value = workResponse.status === 'fulfilled' ? (workResponse.value.data ?? []) : [];
}

function resolveWorkMediaItems(item: WorkData): WorkMediaItem[] {
  const items: WorkMediaItem[] = [];
  const imageUrls = parseImageUrls(item.imageUrls).filter((url) => isImageUrl(url));
  const imageSet = new Set(imageUrls);
  if (item.videoUrl && isVideoUrl(item.videoUrl)) {
    items.push({
      key: `${item.id}-video`,
      src: item.videoUrl,
      thumb: item.videoPoster && isImageUrl(item.videoPoster) ? resolveVideoPosterAtSecond(item.videoPoster) : '',
      type: 'video'
    });
  }
  if (item.coverImage && isImageUrl(item.coverImage)) {
    imageSet.delete(item.coverImage);
    items.push({
      key: `${item.id}-cover-image`,
      src: item.coverImage,
      thumb: item.coverImage,
      type: 'image'
    });
  }
  Array.from(imageSet).forEach((url, index) => {
    items.push({
      key: `${item.id}-image-${index}`,
      src: url,
      thumb: url,
      type: 'image'
    });
  });
  return items.filter((media, index, list) => !!media.src && list.findIndex((entry) => entry.src === media.src && entry.type === media.type) === index);
}

function resolveWorkImages(item: WorkData): string[] {
  return resolveWorkMediaItems(item)
    .filter((media) => media.type === 'image')
    .map((media) => media.src);
}

function resolveWorkCover(item: WorkData): string {
  if (resolveWorkHasVideo(item)) {
    return item.videoPoster && isImageUrl(item.videoPoster) ? resolveVideoPosterAtSecond(item.videoPoster) : '';
  }
  if (item.coverImage && isImageUrl(item.coverImage)) {
    return item.coverImage;
  }
  return resolveWorkImages(item)[0] ?? '';
}

function resolveWorkMediaCount(item: WorkData): number {
  return resolveWorkMediaItems(item).length;
}

function resolveWorkHasVideo(item?: WorkData | null): boolean {
  return !!item?.videoUrl && isVideoUrl(item.videoUrl);
}

function shouldRenderWorkVideoCover(item?: WorkData | null): boolean {
  return !!item && resolveWorkHasVideo(item) && !(item.videoPoster && isImageUrl(item.videoPoster));
}

function resolveWorkPreviewStack(item: WorkData): WorkPreviewItem[] {
  const mediaItems = resolveWorkMediaItems(item);
  return mediaItems
    .filter((media) => media.type === 'image' && !!media.thumb)
    .slice(0, 3)
    .map((media, index) => ({
      key: media.key,
      thumb: media.thumb,
      index: mediaItems.findIndex((entry) => entry.key === media.key),
      offset: index
    }));
}

function resolveVideoPosterAtSecond(url?: string | null): string {
  if (!url) {
    return '';
  }
  if (url.includes('#t=')) {
    return url;
  }
  return `${url}#t=1`;
}

function resolveVideoPlaybackSrc(url?: string | null): string {
  if (!url) {
    return '';
  }
  return url.includes('#t=') ? url : `${url}#t=1`;
}


function resolveWorkTypeText(item?: WorkData | null): string {
  if (!item) {
    return '图文作品';
  }
  const hasVideo = resolveWorkHasVideo(item);
  const hasImages = resolveWorkImages(item).length > 0;
  if (hasVideo && hasImages) {
    return '图文 + 视频';
  }
  if (hasVideo) {
    return '视频作品';
  }
  return '图文作品';
}

function openWorkViewer(item: WorkData, index = 0): void {
  activeWork.value = item;
  const mediaItems = resolveWorkMediaItems(item);
  activeWorkMediaIndex.value = Math.min(Math.max(index, 0), Math.max(mediaItems.length - 1, 0));
  workViewerVisible.value = true;
}

function resolveHonorMedia(item: HonorData): string[] {
  const mediaUrls = parseImageUrls(item.imageUrls);
  if (item.coverImage && !mediaUrls.includes(item.coverImage)) {
    return [item.coverImage, ...mediaUrls];
  }
  return mediaUrls;
}

function resolveHonorImages(item: HonorData): string[] {
  return resolveHonorMedia(item).filter((url) => isImageUrl(url));
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
.work-media-shell {
  background:
    radial-gradient(circle at center, rgba(255, 255, 255, 0.08), transparent 55%),
    #000;
}

.work-media-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(180deg, rgba(15, 23, 42, 0.08), rgba(15, 23, 42, 0.56));
  color: rgba(255, 255, 255, 0.96);
  font-size: 0.82rem;
  font-weight: 500;
  letter-spacing: 0.04em;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.work-media-shell:hover .work-media-overlay,
.work-media-shell:focus-visible .work-media-overlay {
  opacity: 1;
}

.work-media-play {
  position: absolute;
  left: 50%;
  top: 50%;
  display: flex;
  height: 3.4rem;
  width: 3.4rem;
  transform: translate(-50%, -50%);
  align-items: center;
  justify-content: center;
  border-radius: 9999px;
  background: rgba(15, 23, 42, 0.72);
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.32);
  backdrop-filter: blur(8px);
}

.work-media-play-icon {
  margin-left: 0.16rem;
  display: block;
  height: 0;
  width: 0;
  border-bottom: 0.56rem solid transparent;
  border-left: 0.92rem solid rgba(255, 255, 255, 0.96);
  border-top: 0.56rem solid transparent;
}

.work-media-badge {
  position: absolute;
  right: 0.75rem;
  top: 0.75rem;
  display: inline-flex;
  align-items: center;
  gap: 0.38rem;
  border-radius: 9999px;
  background: rgba(15, 23, 42, 0.72);
  padding: 0.34rem 0.72rem;
  font-size: 0.75rem;
  line-height: 1rem;
  color: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(10px);
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.26);
}

.work-media-badge-dot {
  display: inline-block;
  height: 0.42rem;
  width: 0.42rem;
  flex-shrink: 0;
  border-radius: 9999px;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 0 0 3px rgba(255, 255, 255, 0.12);
}

.work-media-stack {
  position: absolute;
  left: 0.85rem;
  bottom: 0.85rem;
  height: 2.25rem;
  width: 6.5rem;
}

.work-media-stack-item {
  position: absolute;
  left: 0;
  top: 0;
  height: 2.25rem;
  width: 3rem;
  transform-origin: center bottom;
  border-radius: 0.95rem;
  border: 1px solid rgba(255, 255, 255, 0.3);
  background-color: rgba(15, 23, 42, 0.92);
  background-position: center;
  background-repeat: no-repeat;
  background-size: cover;
  box-shadow: 0 14px 30px rgba(15, 23, 42, 0.34);
}

.work-viewer-header {
  border-radius: 1.5rem;
  padding: 0;
}

.work-viewer-header-main {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
  padding-right: 3rem;
}

.work-viewer-stage {
  display: flex;
  min-height: min(68vh, 560px);
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border-radius: 1.75rem;
  background:
    radial-gradient(circle at center, rgba(255, 255, 255, 0.08), transparent 55%),
    #000;
}

.work-viewer-thumb {
  position: relative;
  overflow: hidden;
  border-radius: 1.1rem;
  border: 1px solid rgba(var(--dyx-border-subtle-rgb), 0.72);
  background: rgba(var(--dyx-bg-surface-muted-rgb), 0.52);
  padding: 0.3rem;
  text-align: left;
  transition: transform 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
}

.work-viewer-thumb:hover {
  transform: translateY(-1px);
}

.work-viewer-thumb-active {
  border-color: rgba(var(--dyx-text-main-rgb), 0.7);
  box-shadow: 0 12px 24px rgba(15, 23, 42, 0.12);
}

.work-viewer-thumb-tag {
  position: absolute;
  left: 0.7rem;
  top: 0.7rem;
  border-radius: 9999px;
  background: rgba(15, 23, 42, 0.72);
  padding: 0.18rem 0.52rem;
  font-size: 0.7rem;
  line-height: 1rem;
  color: rgba(255, 255, 255, 0.96);
}

:global(.work-viewer-overlay) {
  backdrop-filter: blur(14px);
}

:global(.work-viewer-overlay--dark) {
  background: rgba(2, 6, 23, 0.78);
}

:global(.work-viewer-overlay--light) {
  background: rgba(148, 163, 184, 0.24);
}

:global(.work-viewer-dialog) {
  margin-top: 0 !important;
}

:global(.work-viewer-dialog .el-dialog) {
  overflow: hidden;
  border-radius: 30px;
  border: 1px solid rgb(var(--dyx-border-subtle-rgb) / 0.72);
  box-shadow: var(--dyx-shadow-window);
}

:global(.work-viewer-dialog .el-dialog__header) {
  margin-right: 0;
  padding: 24px 28px 8px;
}

:global(.work-viewer-dialog .el-dialog__body) {
  padding: 12px 28px 28px;
}

:global(.work-viewer-dialog .el-dialog__headerbtn) {
  top: 18px;
  right: 18px;
  z-index: 4;
  display: inline-flex;
  height: 36px;
  width: 36px;
  align-items: center;
  justify-content: center;
  border-radius: 9999px;
  transition: background 0.2s ease, color 0.2s ease, transform 0.2s ease;
}

:global(.work-viewer-dialog .el-dialog__headerbtn:hover) {
  transform: scale(1.04);
}

:global(.work-viewer-dialog .el-dialog__close) {
  color: inherit;
}

:global(.work-viewer-dialog--dark.el-dialog),
:global(.work-viewer-dialog--dark .el-dialog) {
  background: linear-gradient(180deg, rgb(var(--dyx-bg-surface-rgb) / 0.98), rgb(var(--dyx-bg-surface-muted-rgb) / 0.94));
  color: rgb(var(--dyx-text-main-rgb));
}

:global(.work-viewer-dialog--dark.el-dialog .el-dialog__header),
:global(.work-viewer-dialog--dark .el-dialog__header),
:global(.work-viewer-dialog--dark.el-dialog .el-dialog__body),
:global(.work-viewer-dialog--dark .el-dialog__body) {
  background: transparent;
  color: rgb(var(--dyx-text-main-rgb));
}

:global(.work-viewer-dialog--dark.el-dialog .el-dialog__headerbtn),
:global(.work-viewer-dialog--dark .el-dialog__headerbtn) {
  background: rgb(var(--dyx-bg-surface-muted-rgb) / 0.88);
  color: rgb(var(--dyx-text-main-rgb));
}

:global(.work-viewer-dialog--dark.el-dialog .el-dialog__headerbtn:hover),
:global(.work-viewer-dialog--dark .el-dialog__headerbtn:hover) {
  background: rgb(var(--dyx-bg-surface-rgb) / 1);
}

:global(.work-viewer-dialog--light.el-dialog),
:global(.work-viewer-dialog--light .el-dialog) {
  background: linear-gradient(180deg, rgb(var(--dyx-bg-surface-elevated-rgb) / 0.98), rgb(var(--dyx-bg-surface-rgb) / 0.94));
  color: rgb(var(--dyx-text-main-rgb));
}

:global(.work-viewer-dialog--light.el-dialog .el-dialog__headerbtn),
:global(.work-viewer-dialog--light .el-dialog__headerbtn) {
  background: rgb(var(--dyx-bg-surface-muted-rgb) / 0.72);
  color: rgb(var(--dyx-text-main-rgb));
}

:global(.work-viewer-dialog--light.el-dialog .el-dialog__headerbtn:hover),
:global(.work-viewer-dialog--light .el-dialog__headerbtn:hover) {
  background: rgb(var(--dyx-bg-surface-rgb) / 0.88);
}

.honor-media-shell {
  background:
    radial-gradient(circle at center, rgba(255, 255, 255, 0.08), transparent 55%),
    #000;
}

.honors-scroll {
  -ms-overflow-style: none;
  scrollbar-width: none;
}

.honors-scroll::-webkit-scrollbar {
  display: none;
}

@media (max-width: 767px) {
  .work-viewer-header-main {
    padding-right: 2.5rem;
  }

  .work-viewer-stage {
    min-height: min(52vh, 420px);
    border-radius: 1.25rem;
  }

  :global(.work-viewer-dialog .el-dialog) {
    border-radius: 24px;
  }

  :global(.work-viewer-dialog .el-dialog__header) {
    padding: 18px 18px 6px;
  }

  :global(.work-viewer-dialog .el-dialog__body) {
    padding: 10px 18px 18px;
  }

  :global(.work-viewer-dialog .el-dialog__headerbtn) {
    top: 12px;
    right: 12px;
  }
}
</style>
