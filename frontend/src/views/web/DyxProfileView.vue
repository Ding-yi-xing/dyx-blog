<template>
  <section class="dyx-page-shell">
    <div class="dyx-glass-panel rounded-[32px] p-8 lg:p-10">
      <p class="text-sm uppercase tracking-[0.35em] text-slate-500">个人信息</p>
      <div class="mt-6 grid gap-10 lg:grid-cols-[0.7fr_1.3fr] lg:items-start">
        <div class="rounded-[28px] bg-gradient-to-br from-slate-200 to-slate-100 p-8 text-center">
          <img v-if="profile.avatarUrl" :src="profile.avatarUrl" alt="avatar" class="mx-auto h-28 w-28 rounded-full object-cover" />
          <div v-else class="mx-auto h-28 w-28 rounded-full bg-white/70" />
          <h2 class="mt-5 text-2xl font-semibold text-slate-900">{{ profile.siteTitle || 'dyx-blog' }}</h2>
          <p class="mt-2 text-sm text-slate-500">{{ profile.heroTitle || '全栈开发 / 内容创作 / 项目实践' }}</p>
        </div>
        <div class="space-y-5">
          <article class="rounded-[24px] border border-slate-200/70 bg-white/80 p-6">
            <h3 class="text-lg font-semibold">关于我</h3>
            <p class="mt-3 text-sm leading-8 text-slate-600">{{ profile.aboutContent || '这里用于介绍你的核心方向、个人定位、擅长领域和长期关注内容。' }}</p>
          </article>
          <article class="rounded-[24px] border border-slate-200/70 bg-white/80 p-6">
            <h3 class="text-lg font-semibold">联系方式</h3>
            <div class="mt-4 grid gap-3 text-sm text-slate-600 sm:grid-cols-2">
              <p>邮箱：{{ profile.email || '-' }}</p>
              <p>电话：{{ profile.phone || '-' }}</p>
              <p>微信：{{ profile.wechat || '-' }}</p>
              <p>GitHub：{{ profile.githubUrl || '-' }}</p>
            </div>
          </article>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { getProfile, type ProfileData } from '@/api/modules/site';

const profile = ref<ProfileData>({});

/**
 * 获取个人资料数据。
 */
async function loadProfile(): Promise<void> {
  const response = await getProfile();
  profile.value = response.data ?? {};
}

onMounted(() => {
  void loadProfile();
});
</script>
