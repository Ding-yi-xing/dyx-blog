<template>
  <section class="dyx-page-shell">
    <div class="dyx-glass-panel rounded-[32px] p-8">
      <p class="text-sm uppercase tracking-[0.35em] text-slate-500">个人经历</p>
      <h1 class="mt-4 text-4xl font-semibold text-slate-900">按时间线沉淀教育与工作历程。</h1>
      <div class="mt-8 space-y-5">
        <article v-for="item in experiences" :key="item.period" class="rounded-[24px] border border-slate-200/70 bg-white/80 p-6">
          <p class="text-sm text-slate-500">{{ item.period }}</p>
          <h2 class="mt-2 text-xl font-semibold">{{ item.title }}</h2>
          <p class="mt-3 text-sm leading-7 text-slate-600">{{ item.content }}</p>
        </article>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { getProfile, type ProfileData } from '@/api/modules/site';

const profile = ref<ProfileData>({});

const experiences = computed(() => [
  {
    period: '教育经历',
    title: '教育阶段展示区',
    content: profile.value.educationExperience || '后续可通过后台维护学校、专业、时间区间和主要收获。'
  },
  {
    period: '工作经历',
    title: '工作阶段展示区',
    content: profile.value.workExperience || '后续可通过后台维护公司、岗位职责、项目成果与成长总结。'
  }
]);

/**
 * 获取个人经历相关资料。
 */
async function loadProfile(): Promise<void> {
  const response = await getProfile();
  profile.value = response.data ?? {};
}

onMounted(() => {
  void loadProfile();
});
</script>
