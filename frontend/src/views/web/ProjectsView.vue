<template>
  <section class="dyx-page-shell px-2 sm:px-0">
    <div class="dyx-page-card rounded-[34px] p-7 lg:p-8">
      <p class="text-sm uppercase tracking-[0.35em] dyx-text-meta">项目经历</p>
      <h1 class="mt-4 text-4xl font-semibold dyx-text-main">以项目为线索展示能力与成果。</h1>
      <div class="mt-8 grid gap-5 lg:grid-cols-2">
        <article v-for="item in projects" :key="item.id" class="dyx-page-card rounded-[26px] p-6 shadow-dyx-soft">
          <p class="text-sm dyx-text-meta">{{ item.techStack || '未设置技术栈' }}</p>
          <h2 class="mt-2 text-2xl font-semibold dyx-text-main">{{ item.name }}</h2>
          <p class="mt-3 text-sm leading-7 dyx-text-muted">{{ item.description || '暂无项目描述' }}</p>
        </article>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { getProjects, type ProjectData } from '@/api/modules/site';

const projects = ref<ProjectData[]>([]);

async function loadProjects(): Promise<void> {
  const response = await getProjects();
  projects.value = response.data ?? [];
}

onMounted(() => {
  void loadProjects();
});
</script>
