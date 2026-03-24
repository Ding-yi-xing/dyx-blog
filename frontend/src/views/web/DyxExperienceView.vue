<template>
  <section class="dyx-page-shell space-y-8">
    <div class="dyx-glass-panel rounded-[32px] px-8 py-14">
      <p class="text-sm uppercase tracking-[0.4em] text-slate-500">简历</p>
      <div class="mt-6 grid gap-10 lg:grid-cols-[1.15fr_0.85fr] lg:items-center">
        <div>
          <h1 class="text-4xl font-semibold leading-tight text-slate-900 md:text-6xl">用教育、工作、项目与荣誉构成完整履历。</h1>
          <p class="mt-6 max-w-2xl text-base leading-8 text-slate-600 md:text-lg">
            围绕个人成长路径沉淀学习经历、工作实践、项目成果与荣誉时间线，让简历既能承载信息，也能保留表达质感。
          </p>
          <div class="mt-8 flex flex-wrap gap-4 text-sm text-slate-500">
            <span class="rounded-full border border-slate-200 px-4 py-2">教育与工作双主线</span>
            <span class="rounded-full border border-slate-200 px-4 py-2">结构化项目经历</span>
            <span class="rounded-full border border-slate-200 px-4 py-2">荣誉时间线展示</span>
          </div>
        </div>
        <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-1">
          <article class="dyx-glass-panel rounded-[28px] p-6">
            <p class="text-sm text-slate-500">项目经历</p>
            <p class="mt-3 text-2xl font-semibold text-slate-900">{{ projects.length }} 项</p>
          </article>
          <article class="dyx-glass-panel rounded-[28px] p-6">
            <p class="text-sm text-slate-500">荣誉成果</p>
            <p class="mt-3 text-2xl font-semibold text-slate-900">{{ honors.length }} 项</p>
          </article>
        </div>
      </div>
    </div>

    <div class="grid gap-6 xl:grid-cols-2">
      <article v-for="item in experienceSections" :key="item.title" class="dyx-glass-panel rounded-[28px] p-7">
        <p class="text-sm uppercase tracking-[0.3em] text-slate-500">{{ item.label }}</p>
        <h2 class="mt-4 text-2xl font-semibold text-slate-900">{{ item.title }}</h2>
        <p class="mt-5 whitespace-pre-line text-sm leading-8 text-slate-600">{{ item.content }}</p>
      </article>
    </div>

    <div class="dyx-glass-panel rounded-[32px] p-8">
      <div class="flex flex-wrap items-end justify-between gap-4">
        <div>
          <p class="text-sm uppercase tracking-[0.35em] text-slate-500">项目经历</p>
          <h2 class="mt-3 text-3xl font-semibold text-slate-900">聚焦代表性项目与实战成果。</h2>
        </div>
        <p class="max-w-xl text-sm leading-7 text-slate-500">项目经历已整合进简历模块中，继续保留结构化数据与排序能力。</p>
      </div>
      <div class="mt-8 grid gap-5 lg:grid-cols-2">
        <article v-for="item in projects" :key="item.id" class="rounded-[24px] border border-slate-200/70 bg-white/75 p-6 transition hover:-translate-y-1 hover:shadow-sm">
          <img v-if="item.coverImage" :src="item.coverImage" :alt="item.name" class="mb-5 h-44 w-full rounded-2xl object-cover" />
          <div class="flex flex-wrap items-center gap-3 text-sm text-slate-500">
            <span class="rounded-full bg-slate-100 px-3 py-1">{{ item.roleName || '项目实践' }}</span>
            <span>{{ item.techStack || '未设置技术栈' }}</span>
          </div>
          <h3 class="mt-4 text-2xl font-semibold text-slate-900">{{ item.name }}</h3>
          <p class="mt-4 text-sm leading-7 text-slate-600">{{ item.description || '暂无项目描述。' }}</p>
          <a
            v-if="item.projectLink"
            :href="item.projectLink"
            target="_blank"
            rel="noreferrer"
            class="mt-5 inline-flex text-sm font-medium text-slate-900 transition hover:text-slate-600"
          >
            查看项目链接
          </a>
        </article>
        <article v-if="!projects.length" class="rounded-[24px] border border-dashed border-slate-300 bg-white/70 p-8 text-sm text-slate-400 lg:col-span-2">
          暂无已发布项目内容。
        </article>
      </div>
    </div>

    <div class="dyx-glass-panel rounded-[32px] p-8">
      <div>
        <p class="text-sm uppercase tracking-[0.35em] text-slate-500">荣誉时间线</p>
        <h2 class="mt-3 text-3xl font-semibold text-slate-900">按时间沉淀阶段性成果。</h2>
      </div>
      <div class="mt-8 space-y-6">
        <article v-for="item in honors" :key="item.id" class="grid gap-4 md:grid-cols-[140px_1fr] md:gap-8">
          <div class="relative pl-6 md:pl-0">
            <div class="absolute left-0 top-2 h-full w-px bg-slate-200 md:left-auto md:right-0 md:top-12"></div>
            <div class="absolute left-[-4px] top-2 h-3 w-3 rounded-full bg-slate-900 md:left-auto md:right-[-6px]"></div>
            <p class="text-sm font-medium text-slate-500">{{ formatAwardDate(item.awardAt) }}</p>
          </div>
          <div class="rounded-[24px] border border-slate-200/70 bg-white/80 p-6 transition hover:-translate-y-1 hover:shadow-sm">
            <div class="flex flex-wrap items-center gap-3">
              <h3 class="text-2xl font-semibold text-slate-900">{{ item.title }}</h3>
              <span v-if="item.issuer" class="rounded-full bg-slate-100 px-3 py-1 text-xs text-slate-500">{{ item.issuer }}</span>
            </div>
            <p class="mt-4 text-sm leading-7 text-slate-600">{{ item.description || '暂无荣誉说明。' }}</p>
            <div v-if="parseImageUrls(item.imageUrls).length || item.coverImage" class="mt-5 grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
              <img
                v-for="(url, index) in resolveHonorImages(item)"
                :key="`${item.id}-${index}`"
                :src="url"
                :alt="item.title"
                class="h-36 w-full rounded-2xl object-cover"
              />
            </div>
          </div>
        </article>
        <article v-if="!honors.length" class="rounded-[24px] border border-dashed border-slate-300 bg-white/70 p-8 text-sm text-slate-400">
          暂无已发布荣誉内容。
        </article>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { getHonors, getProfile, getProjects, type HonorData, type ProfileData, type ProjectData } from '@/api/modules/site';
import { parseImageUrls } from '@/utils/media';

const profile = ref<ProfileData>({});
const projects = ref<ProjectData[]>([]);
const honors = ref<HonorData[]>([]);

const experienceSections = computed(() => [
  {
    label: 'Education',
    title: '教育经历',
    content: profile.value.educationExperience || '后续可通过后台继续维护学校、专业、时间区间与主要收获。'
  },
  {
    label: 'Work',
    title: '工作经历',
    content: profile.value.workExperience || '后续可通过后台继续维护公司、岗位职责、项目成果与阶段总结。'
  }
]);

async function loadResumeData(): Promise<void> {
  const [profileResponse, projectResponse, honorResponse] = await Promise.all([getProfile(), getProjects(), getHonors()]);
  profile.value = profileResponse.data ?? {};
  projects.value = projectResponse.data ?? [];
  honors.value = honorResponse.data ?? [];
}

function resolveHonorImages(item: HonorData): string[] {
  const imageUrls = parseImageUrls(item.imageUrls);
  if (item.coverImage && !imageUrls.includes(item.coverImage)) {
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

onMounted(() => {
  void loadResumeData();
});
</script>
