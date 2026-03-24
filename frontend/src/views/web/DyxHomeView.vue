<template>
  <section class="dyx-page-shell space-y-8">
    <div class="dyx-glass-panel rounded-[32px] px-8 py-14">
      <p class="text-sm uppercase tracking-[0.4em] text-slate-500">{{ homeData.profile?.siteTitle || 'dyx-blog' }}</p>
      <div class="mt-6 grid gap-10 lg:grid-cols-[1.3fr_0.7fr] lg:items-center">
        <div>
          <h1 class="text-4xl font-semibold leading-tight text-slate-900 md:text-6xl">
            {{ homeData.profile?.heroTitle || '构建一个兼具内容表达与个人展示的现代博客网站。' }}
          </h1>
          <p class="mt-6 max-w-2xl text-base leading-8 text-slate-600 md:text-lg">
            {{
              homeData.profile?.heroSubtitle ||
              '这里用于承载博客文章、个人动态、荣誉成果、照片集以及教育、工作与项目经历，整体视觉方向强调简洁、通透与高级留白。'
            }}
          </p>
          <div class="mt-8 flex flex-wrap gap-4">
            <RouterLink to="/blog" class="rounded-full bg-slate-900 px-6 py-3 text-sm text-white">查看博客</RouterLink>
            <RouterLink to="/experience" class="rounded-full border border-slate-300 px-6 py-3 text-sm text-slate-700">查看简历</RouterLink>
          </div>
        </div>
        <div class="grid gap-4">
          <article class="dyx-glass-panel rounded-[28px] p-6">
            <p class="text-sm text-slate-500">最新文章</p>
            <p class="mt-3 text-2xl font-semibold">{{ homeData.latestPosts?.length ?? 0 }} 篇</p>
          </article>
          <article class="dyx-glass-panel rounded-[28px] p-6">
            <p class="text-sm text-slate-500">荣誉成果</p>
            <p class="mt-3 text-2xl font-semibold">{{ homeData.latestHonors?.length ?? 0 }} 项</p>
          </article>
        </div>
      </div>
    </div>

    <div class="grid gap-6 md:grid-cols-2 xl:grid-cols-3">
      <article v-for="item in featureItems" :key="item.title" class="dyx-glass-panel rounded-[28px] p-6">
        <p class="text-sm text-slate-500">{{ item.caption }}</p>
        <h2 class="mt-3 text-2xl font-semibold text-slate-900">{{ item.title }}</h2>
        <p class="mt-4 text-sm leading-7 text-slate-600">{{ item.description }}</p>
      </article>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { getHomeData, type HomeData } from '@/api/modules/site';

const homeData = ref<HomeData>({});

const featureItems = computed(() => [
  {
    caption: '01',
    title: '最新动态',
    description: `当前已发布 ${homeData.value.latestMoments?.length ?? 0} 条近期更新。`
  },
  {
    caption: '02',
    title: '简历项目',
    description: `当前已整合 ${homeData.value.featuredProjects?.length ?? 0} 个项目案例进入简历模块。`
  },
  {
    caption: '03',
    title: '荣誉时间线',
    description: `当前已沉淀 ${homeData.value.latestHonors?.length ?? 0} 项阶段性荣誉成果。`
  },
  {
    caption: '04',
    title: '个人照片',
    description: '通过图像叙事补充个人风格与生活化表达。'
  },
  {
    caption: '05',
    title: '博客文章',
    description: `当前已沉淀 ${homeData.value.latestPosts?.length ?? 0} 篇最新内容。`
  },
  {
    caption: '06',
    title: '后台系统',
    description: '支持内容发布、编辑、删除、媒体上传与权限控制。'
  }
]);

/**
 * 加载首页聚合数据。
 */
async function loadHomeData(): Promise<void> {
  const response = await getHomeData();
  homeData.value = response.data ?? {};
}

onMounted(() => {
  void loadHomeData();
});
</script>
