<template>
  <section class="dyx-page-shell space-y-6 px-2 sm:px-0">
    <div class="dyx-page-card-elevated overflow-hidden rounded-[34px] print:rounded-none print:border-0 print:shadow-none">
      <div class="border-b border-[rgb(var(--dyx-border-subtle-rgb)/0.62)] px-6 py-6 sm:px-8 print:px-0">
        <div class="flex flex-col gap-5 sm:flex-row sm:items-start sm:justify-between">
          <div class="space-y-3">
            <p class="text-[11px] font-medium uppercase tracking-[0.32em] dyx-text-meta">Resume</p>
            <h1 class="text-3xl font-semibold dyx-text-main">个人简历</h1>
            <p class="text-base dyx-text-muted">教育经历、工作经历、项目经历与荣誉摘要。</p>
            <div class="flex flex-wrap gap-x-5 gap-y-2 text-sm dyx-text-muted">
              <span v-if="profile.email">{{ profile.email }}</span>
              <span v-if="profile.phone">{{ profile.phone }}</span>
              <span v-if="profile.wechat">{{ profile.wechat }}</span>
              <span v-if="profile.githubUrl">{{ profile.githubUrl }}</span>
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
            <button type="button" class="dyx-ghost-pill inline-flex" @click="handlePrint">
              打印当前页
            </button>
          </div>
        </div>
      </div>

      <div class="grid gap-8 px-6 py-6 sm:px-8 print:px-0 print:py-4">
        <section class="space-y-4">
          <h2 class="text-lg font-semibold dyx-text-main">教育经历</h2>
          <article v-for="(item, index) in educationItems" :key="`${item}-${index}`" class="dyx-page-card rounded-[24px] px-5 py-4 print:border-[rgb(var(--dyx-border-subtle-rgb)/0.72)] print:bg-white">
            <p class="whitespace-pre-line text-sm leading-7 dyx-text-muted">{{ item }}</p>
          </article>
          <article v-if="!educationItems.length" class="rounded-[24px] border border-dashed border-[rgb(var(--dyx-border-subtle-rgb)/0.72)] px-5 py-4 text-sm dyx-text-meta">
            暂无教育经历。
          </article>
        </section>

        <section class="space-y-4">
          <h2 class="text-lg font-semibold dyx-text-main">工作经历</h2>
          <article v-for="(item, index) in workItems" :key="`${item}-${index}`" class="dyx-page-card rounded-[24px] px-5 py-4 print:border-[rgb(var(--dyx-border-subtle-rgb)/0.72)] print:bg-white">
            <p class="whitespace-pre-line text-sm leading-7 dyx-text-muted">{{ item }}</p>
          </article>
          <article v-if="!workItems.length" class="rounded-[24px] border border-dashed border-[rgb(var(--dyx-border-subtle-rgb)/0.72)] px-5 py-4 text-sm dyx-text-meta">
            暂无工作经历。
          </article>
        </section>

        <section class="space-y-4">
          <div class="flex items-center justify-between gap-4">
            <h2 class="text-lg font-semibold dyx-text-main">项目经历</h2>
            <span class="text-sm dyx-text-meta">{{ projects.length }} 项</span>
          </div>
          <div class="grid gap-4 lg:grid-cols-2">
            <article v-for="item in projects" :key="item.id" class="dyx-page-card rounded-[24px] px-5 py-5 print:break-inside-avoid print:border-[rgb(var(--dyx-border-subtle-rgb)/0.72)] print:bg-white">
              <div class="flex flex-wrap items-center gap-3 text-xs dyx-text-meta">
                <span v-if="item.roleName" class="rounded-full bg-[rgb(var(--dyx-bg-surface-rgb)/0.72)] px-3 py-1 ring-1 ring-[rgb(var(--dyx-border-subtle-rgb)/0.72)] print:ring-[rgb(var(--dyx-border-subtle-rgb)/0.72)]">{{ item.roleName }}</span>
                <span v-if="item.techStack">{{ item.techStack }}</span>
              </div>
              <h3 class="mt-3 text-xl font-semibold dyx-text-main">{{ item.name }}</h3>
              <p class="mt-3 text-sm leading-7 dyx-text-muted">{{ item.description || '暂无项目描述。' }}</p>
              <a
                v-if="item.projectLink"
                :href="item.projectLink"
                target="_blank"
                rel="noreferrer"
                class="mt-4 inline-flex text-sm dyx-text-main underline-offset-4 hover:underline print:dyx-text-main"
              >
                {{ item.projectLink }}
              </a>
            </article>
            <article v-if="!projects.length" class="rounded-[24px] border border-dashed border-[rgb(var(--dyx-border-subtle-rgb)/0.72)] px-5 py-4 text-sm dyx-text-meta lg:col-span-2">
              暂无已发布项目内容。
            </article>
          </div>
        </section>

        <section class="space-y-4">
          <div class="flex items-center justify-between gap-4">
            <h2 class="text-lg font-semibold dyx-text-main">荣誉摘要</h2>
            <RouterLink to="/about" class="text-sm dyx-text-main print:hidden">查看完整荣誉</RouterLink>
          </div>
          <div class="grid gap-4">
            <article v-for="item in honors.slice(0, 3)" :key="item.id" class="dyx-page-card rounded-[24px] px-5 py-4 print:border-[rgb(var(--dyx-border-subtle-rgb)/0.72)] print:bg-white">
              <div class="flex flex-wrap items-center gap-3">
                <h3 class="text-base font-semibold dyx-text-main">{{ item.title }}</h3>
                <span v-if="item.awardAt" class="text-sm dyx-text-meta">{{ item.awardAt.slice(0, 10) }}</span>
              </div>
              <p class="mt-3 text-sm leading-7 dyx-text-muted">{{ item.description || '暂无荣誉说明。' }}</p>
            </article>
            <article v-if="!honors.length" class="rounded-[24px] border border-dashed border-[rgb(var(--dyx-border-subtle-rgb)/0.72)] px-5 py-4 text-sm dyx-text-meta">
              暂无已发布荣誉内容。
            </article>
          </div>
        </section>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { getHonors, getProfile, getProjects, recordSiteVisit, type HonorData, type ProfileData, type ProjectData } from '@/api/modules/site';

const profile = ref<ProfileData>({});
const projects = ref<ProjectData[]>([]);
const honors = ref<HonorData[]>([]);

const educationItems = computed(() => splitParagraphs(profile.value.educationExperience));
const workItems = computed(() => splitParagraphs(profile.value.workExperience));

function splitParagraphs(value?: string): string[] {
  return (value ?? '')
    .split(/\n+/)
    .map((item) => item.trim())
    .filter(Boolean);
}

async function loadResumeData(): Promise<void> {
  const [profileResponse, projectResponse, honorResponse] = await Promise.allSettled([getProfile(), getProjects(), getHonors()]);
  profile.value = profileResponse.status === 'fulfilled' ? (profileResponse.value.data ?? {}) : {};
  projects.value = projectResponse.status === 'fulfilled' ? (projectResponse.value.data ?? []) : [];
  honors.value = honorResponse.status === 'fulfilled' ? (honorResponse.value.data ?? []) : [];
}

function handlePrint(): void {
  window.print();
}

onMounted(() => {
  void recordSiteVisit('resume');
  void loadResumeData();
});
</script>

<style scoped>
@media print {
  :global(body) {
    background: #fff;
  }

  :global(header) {
    display: none !important;
  }
}
</style>
