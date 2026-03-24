<template>
  <section class="grid gap-6 xl:grid-cols-5">
    <article v-for="item in summaryItems" :key="item.label" class="rounded-[28px] bg-white p-6 shadow-sm">
      <p class="text-sm text-slate-500">{{ item.label }}</p>
      <p class="mt-4 text-4xl font-semibold text-slate-900">{{ item.value }}</p>
      <p class="mt-2 text-sm text-slate-500">{{ item.tip }}</p>
    </article>
    <article class="rounded-[28px] bg-white p-6 shadow-sm xl:col-span-5">
      <h2 class="text-xl font-semibold text-slate-900">系统说明</h2>
      <p class="mt-4 max-w-3xl text-sm leading-8 text-slate-600">
        当前后台已接入真实摘要接口，可展示文章、动态、荣誉、照片和用户数量统计。
      </p>
    </article>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { getDashboardSummary } from '@/api/modules/admin';

const summary = ref<Record<string, number>>({});

const summaryItems = computed(() => [
  { label: '文章总数', value: String(summary.value.postCount ?? 0), tip: '用于展示当前博客文章存量' },
  { label: '动态数量', value: String(summary.value.momentCount ?? 0), tip: '用于展示最新动态条目数' },
  { label: '荣誉数量', value: String(summary.value.honorCount ?? 0), tip: '用于展示荣誉时间线条目数' },
  { label: '照片数量', value: String(summary.value.photoCount ?? 0), tip: '用于展示照片内容存量' },
  { label: '用户数量', value: String(summary.value.userCount ?? 0), tip: '用于展示后台可操作用户数' }
]);

/**
 * 拉取后台仪表盘摘要数据。
 */
async function loadDashboardSummary(): Promise<void> {
  const response = await getDashboardSummary();
  summary.value = response.data ?? {};
}

onMounted(() => {
  void loadDashboardSummary();
});
</script>
