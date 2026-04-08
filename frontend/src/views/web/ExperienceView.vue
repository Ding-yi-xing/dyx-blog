<template>
  <section class="dyx-page-shell space-y-6 px-2 sm:px-0">
    <div
      class="dyx-page-card-elevated overflow-hidden rounded-[34px] print:rounded-none print:border-0 print:shadow-none"
    >
      <div
        class="border-b border-[rgb(var(--dyx-border-subtle-rgb)/0.62)] px-6 py-6 sm:px-8 print:px-0"
      >
        <div class="flex flex-col gap-5 sm:flex-row sm:items-start sm:justify-between">
          <div class="space-y-3">
            <p
              class="text-[11px] font-medium uppercase tracking-[0.32em] dyx-text-meta"
            >
              Resume
            </p>
            <h1 class="text-3xl font-semibold dyx-text-main">个人简历</h1>
            <p class="text-base dyx-text-muted">
              教育经历、工作经历、项目经历与荣誉摘要。
            </p>
            <div class="flex flex-wrap gap-x-5 gap-y-2 text-sm dyx-text-muted">
              <template
                v-for="(item, index) in linkedContactMethods"
                :key="`${item.label || item.type}-${index}`"
              >
                <a
                  v-if="item.href"
                  :href="item.href"
                  :target="item.external ? '_blank' : undefined"
                  :rel="item.external ? 'noreferrer' : undefined"
                  class="break-all underline-offset-4 transition hover:underline"
                >
                  {{ item.value }}
                </a>
                <span v-else>{{ item.value }}</span>
              </template>
            </div>
          </div>
          <div class="flex flex-wrap gap-3 print:hidden">
            <a
              v-if="profile.resumePdfUrl"
              :href="profile.resumePdfUrl"
              download
              target="_blank"
              rel="noreferrer"
              class="inline-flex rounded-full bg-[rgb(var(--dyx-text-main-rgb))] px-5 py-3 text-sm text-[rgb(var(--dyx-text-inverse-rgb))]"
            >
              下载 PDF 简历
            </a>
            <button
              type="button"
              class="dyx-ghost-pill inline-flex"
              @click="handleResumePrint"
            >
              打印简历
            </button>
          </div>
        </div>
      </div>

      <div class="grid gap-8 px-6 py-6 sm:px-8 print:px-0 print:py-4">
        <div v-if="loading" class="space-y-6">
          <el-skeleton animated :rows="3" />
          <el-skeleton animated :rows="3" />
          <el-skeleton animated :rows="3" />
        </div>

        <template v-else>
          <section class="space-y-4">
            <h2 class="text-lg font-semibold dyx-text-main">教育经历</h2>
            <article
              v-for="(item, index) in educationItems"
              :key="`${item}-${index}`"
              class="dyx-page-card rounded-[24px] px-5 py-4 print:border-[rgb(var(--dyx-border-subtle-rgb)/0.72)] print:bg-white"
            >
              <p class="whitespace-pre-line text-sm leading-7 dyx-text-muted">
                {{ item }}
              </p>
            </article>
            <article
              v-if="!educationItems.length"
              class="rounded-[24px] border border-dashed border-[rgb(var(--dyx-border-subtle-rgb)/0.72)] px-5 py-4 text-sm dyx-text-meta"
            >
              暂无教育经历。
            </article>
          </section>

          <section class="space-y-4">
            <h2 class="text-lg font-semibold dyx-text-main">工作经历</h2>
            <article
              v-for="(item, index) in workItems"
              :key="`${item}-${index}`"
              class="dyx-page-card rounded-[24px] px-5 py-4 print:border-[rgb(var(--dyx-border-subtle-rgb)/0.72)] print:bg-white"
            >
              <p class="whitespace-pre-line text-sm leading-7 dyx-text-muted">
                {{ item }}
              </p>
            </article>
            <article
              v-if="!workItems.length"
              class="rounded-[24px] border border-dashed border-[rgb(var(--dyx-border-subtle-rgb)/0.72)] px-5 py-4 text-sm dyx-text-meta"
            >
              暂无工作经历。
            </article>
          </section>

          <section class="space-y-4">
            <div class="flex items-center justify-between gap-4">
              <h2 class="text-lg font-semibold dyx-text-main">项目经历</h2>
              <span class="text-sm dyx-text-meta">{{ projects.length }} 项</span>
            </div>
            <div class="grid gap-4 lg:grid-cols-2">
              <article
                v-for="item in projects"
                :key="item.id"
                class="dyx-page-card rounded-[24px] px-5 py-5 print:break-inside-avoid print:border-[rgb(var(--dyx-border-subtle-rgb)/0.72)] print:bg-white"
              >
                <div class="flex flex-wrap items-center gap-3 text-xs dyx-text-meta">
                  <span
                    v-if="item.roleName"
                    class="rounded-full bg-[rgb(var(--dyx-bg-surface-rgb)/0.72)] px-3 py-1 ring-1 ring-[rgb(var(--dyx-border-subtle-rgb)/0.72)] print:ring-[rgb(var(--dyx-border-subtle-rgb)/0.72)]"
                    >{{ item.roleName }}</span
                  >
                  <span v-if="item.techStack">{{ item.techStack }}</span>
                </div>
                <h3 class="mt-3 text-xl font-semibold dyx-text-main">
                  {{ item.name }}
                </h3>
                <p class="mt-3 text-sm leading-7 dyx-text-muted">
                  {{ item.description || '暂无项目描述。' }}
                </p>
                <div class="mt-4 flex flex-wrap gap-3">
                  <button
                    v-if="item.projectLink && isVideoUrl(item.projectLink)"
                    type="button"
                    class="dyx-ghost-pill inline-flex print:hidden"
                    @click="openProjectVideo(item.projectLink)"
                  >
                    播放项目视频
                  </button>
                  <a
                    v-else-if="item.projectLink"
                    :href="item.projectLink"
                    target="_blank"
                    rel="noreferrer"
                    class="inline-flex text-sm dyx-text-main underline-offset-4 hover:underline print:dyx-text-main"
                  >
                    {{ item.projectLink }}
                  </a>
                </div>
              </article>
              <article
                v-if="!projects.length"
                class="rounded-[24px] border border-dashed border-[rgb(var(--dyx-border-subtle-rgb)/0.72)] px-5 py-4 text-sm dyx-text-meta lg:col-span-2"
              >
                暂无已发布项目内容。
              </article>
            </div>
          </section>

          <section class="space-y-4">
            <div class="flex items-center justify-between gap-4">
              <h2 class="text-lg font-semibold dyx-text-main">荣誉摘要</h2>
              <RouterLink to="/about" class="text-sm dyx-text-main print:hidden">
                查看完整荣誉
              </RouterLink>
            </div>
            <div class="grid gap-4">
              <article
                v-for="item in honors.slice(0, 3)"
                :key="item.id"
                class="dyx-page-card rounded-[24px] px-5 py-4 print:border-[rgb(var(--dyx-border-subtle-rgb)/0.72)] print:bg-white"
              >
                <div class="flex flex-wrap items-center gap-3">
                  <h3 class="text-base font-semibold dyx-text-main">
                    {{ item.title }}
                  </h3>
                  <span
                    v-if="item.awardAt"
                    class="text-sm dyx-text-meta"
                    >{{ formatDateYmd(item.awardAt) }}</span
                  >
                </div>
                <p class="mt-3 text-sm leading-7 dyx-text-muted">
                  {{ item.description || '暂无荣誉说明。' }}
                </p>
              </article>
              <article
                v-if="!honors.length"
                class="rounded-[24px] border border-dashed border-[rgb(var(--dyx-border-subtle-rgb)/0.72)] px-5 py-4 text-sm dyx-text-meta"
              >
                暂无已发布荣誉内容。
              </article>
            </div>
          </section>
        </template>
      </div>
    </div>

    <el-dialog
      v-model="projectVideoDialogVisible"
      width="min(960px, calc(100vw - 32px))"
      top="96px"
      append-to-body
      destroy-on-close
      :class="activeTheme === 'dark' ? 'resume-video-dialog resume-video-dialog--dark' : 'resume-video-dialog resume-video-dialog--light'"
      :modal-class="'resume-video-overlay'"
    >
      <template #header>
        <div class="flex items-center justify-between gap-3 pr-8">
          <div>
            <p class="text-xs uppercase tracking-[0.26em] dyx-text-meta">项目视频</p>
            <p class="mt-1 text-sm dyx-text-muted">
              该项目链接为视频地址，已在站内以播放器形式打开。
            </p>
          </div>
        </div>
      </template>

      <div class="space-y-4">
        <div class="resume-video-shell flex min-h-[320px] items-center justify-center overflow-hidden rounded-2xl">
          <video
            v-if="projectVideoDialogVisible && activeProjectVideoUrl"
            :src="activeProjectVideoUrl"
            controls
            playsinline
            preload="metadata"
            class="block h-full max-h-[70vh] w-full max-w-full object-contain"
          ></video>
        </div>
        <p class="break-all text-xs dyx-text-meta">
          源地址：{{ activeProjectVideoUrl }}
        </p>
      </div>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus';
import { computed, inject, onMounted, ref, type Ref } from 'vue';
import {
  getHonors,
  getProfile,
  getProjects,
  isExternalContactHref,
  recordSiteVisit,
  resolveContactHref,
  resolveProfileContactMethods,
  type HonorData,
  type ProfileData,
  type ProjectData
} from '@/api/modules/site';
import { formatDateYmd } from '@/utils/date';
import { isVideoUrl } from '@/utils/media';

type ThemeMode = 'light' | 'dark';

const profile = ref<ProfileData>({});
const projects = ref<ProjectData[]>([]);
const honors = ref<HonorData[]>([]);
const loading = ref(false);

const projectVideoDialogVisible = ref(false);
const activeProjectVideoUrl = ref('');

const currentTheme = inject<Ref<ThemeMode> | undefined>('dyx-theme');
const activeTheme = computed<ThemeMode>(() => currentTheme?.value ?? 'dark');

const educationItems = computed(() => splitParagraphs(profile.value.educationExperience));
const workItems = computed(() => splitParagraphs(profile.value.workExperience));
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

function splitParagraphs(value?: string): string[] {
  return (value ?? '')
    .split(/\n+/)
    .map((item) => item.trim())
    .filter(Boolean);
}

async function loadResumeData(): Promise<void> {
  loading.value = true;
  try {
    const [profileResponse, projectResponse, honorResponse] = await Promise.allSettled([
      getProfile(),
      getProjects(),
      getHonors()
    ]);
    profile.value =
      profileResponse.status === 'fulfilled' ? (profileResponse.value.data ?? {}) : {};
    projects.value =
      projectResponse.status === 'fulfilled' ? (projectResponse.value.data ?? []) : [];
    honors.value =
      honorResponse.status === 'fulfilled' ? (honorResponse.value.data ?? []) : [];
  } finally {
    loading.value = false;
  }
}

function handleResumePrint(): void {
  if (!profile.value.resumePdfUrl) {
    ElMessage.info('还在操刀简历，稍等');
    return;
  }

  const printWindow = window.open(profile.value.resumePdfUrl, '_blank', 'noopener,noreferrer');
  printWindow?.addEventListener('load', () => {
    printWindow.print();
  });
}

function openProjectVideo(url: string): void {
  if (!url) {
    return;
  }
  activeProjectVideoUrl.value = url;
  projectVideoDialogVisible.value = true;
}

onMounted(() => {
  void recordSiteVisit('resume');
  void loadResumeData();
});
</script>

<style scoped>
.resume-video-shell {
  background:
    radial-gradient(circle at center, rgba(148, 163, 184, 0.16), transparent 60%),
    #020617;
}

.dark .resume-video-shell {
  background:
    radial-gradient(circle at center, rgba(148, 163, 184, 0.26), transparent 62%),
    #020617;
}

:root:not(.dark) .resume-video-shell {
  background:
    radial-gradient(circle at center, rgba(30, 64, 175, 0.12), transparent 60%),
    #e5e7eb;
}

:global(.resume-video-overlay) {
  backdrop-filter: blur(14px);
}

:global(.resume-video-dialog .el-dialog) {
  border-radius: 30px;
  border: 1px solid rgb(var(--dyx-border-subtle-rgb) / 0.72);
  box-shadow: var(--dyx-shadow-window);
}

:global(.resume-video-dialog .el-dialog__header) {
  margin-right: 0;
  padding: 24px 28px 8px;
}

:global(.resume-video-dialog .el-dialog__body) {
  padding: 12px 28px 28px;
}

:global(.resume-video-dialog .el-dialog__headerbtn) {
  top: 18px;
  right: 18px;
  z-index: 4;
  display: inline-flex;
  height: 36px;
  width: 36px;
  align-items: center;
  justify-content: center;
  border-radius: 9999px;
}

:global(.resume-video-dialog .el-dialog__headerbtn:hover) {
  transform: scale(1.04);
}

:global(.resume-video-dialog .el-dialog__close) {
  color: inherit;
}

:global(.resume-video-dialog--dark.el-dialog),
:global(.resume-video-dialog--dark .el-dialog) {
  background: linear-gradient(180deg, rgb(var(--dyx-bg-surface-rgb) / 0.98), rgb(var(--dyx-bg-surface-muted-rgb) / 0.94));
  color: rgb(var(--dyx-text-main-rgb));
}

:global(.resume-video-dialog--light.el-dialog),
:global(.resume-video-dialog--light .el-dialog) {
  background: linear-gradient(180deg, rgb(var(--dyx-bg-surface-elevated-rgb) / 0.98), rgb(var(--dyx-bg-surface-rgb) / 0.94));
  color: rgb(var(--dyx-text-main-rgb));
}

@media print {
  :global(body) {
    background: #fff;
  }

  :global(header) {
    display: none !important;
  }
}
</style>
