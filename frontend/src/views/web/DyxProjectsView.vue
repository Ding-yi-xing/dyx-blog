<template>
  <section class="dyx-page-shell">
    <div class="dyx-glass-panel rounded-[32px] p-8">
      <p class="text-sm uppercase tracking-[0.35em] text-slate-500">项目经历</p>
      <h1 class="mt-4 text-4xl font-semibold text-slate-900">以项目为线索展示能力与成果。</h1>
      <div class="mt-8 grid gap-5 lg:grid-cols-2">
        <article v-for="item in projects" :key="item.id" class="rounded-[24px] border border-slate-200/70 bg-white/70 p-6">
          <p class="text-sm text-slate-500">{{ item.techStack || '未设置技术栈' }}</p>
          <h2 class="mt-2 text-2xl font-semibold">{{ item.name }}</h2>
          <p class="mt-3 text-sm leading-7 text-slate-600">{{ item.description || '暂无项目描述' }}</p>
        </article>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { getProjects, type ProjectData } from '@/api/modules/site';

const projects = ref<ProjectData[]>([]);

/**
 * 获取项目经历列表数据。
 */
async function loadProjects(): Promise<void> {
  const response = await getProjects();
  projects.value = response.data ?? [];
}

onMounted(() => {
  void loadProjects();
});
</script>
