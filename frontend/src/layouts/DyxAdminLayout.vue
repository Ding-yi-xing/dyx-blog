<template>
  <div class="min-h-screen bg-slate-100">
    <div class="flex min-h-screen">
      <aside class="hidden w-64 flex-col border-r border-slate-200 bg-slate-950 px-5 py-8 text-slate-200 lg:flex">
        <div>
          <div class="text-lg font-semibold">dyx-blog Admin</div>
          <p class="mt-2 text-sm text-slate-400">个人博客内容管理后台</p>
        </div>
        <nav class="mt-8 flex flex-1 flex-col gap-2 text-sm">
          <RouterLink v-for="item in adminNavItems" :key="item.path" :to="item.path" class="rounded-xl px-4 py-3 transition hover:bg-slate-800">
            {{ item.label }}
          </RouterLink>
        </nav>
      </aside>

      <div class="flex min-h-screen flex-1 flex-col">
        <header class="flex items-center justify-between border-b border-slate-200 bg-white px-6 py-4">
          <div>
            <h1 class="text-lg font-semibold text-slate-900">后台管理</h1>
            <p class="text-sm text-slate-500">统一管理博客、简历、荣誉与媒体资源</p>
          </div>
          <button class="rounded-full bg-slate-900 px-4 py-2 text-sm text-white" @click="handleLogout">退出登录</button>
        </header>

        <main class="flex-1 p-6">
          <router-view />
        </main>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const authStore = useAuthStore();

const adminNavItems = [
  { path: '/admin/dashboard', label: '仪表盘' },
  { path: '/admin/posts', label: '博客管理' },
  { path: '/admin/moments', label: '动态管理' },
  { path: '/admin/honors', label: '荣誉管理' },
  { path: '/admin/photos', label: '照片管理' },
  { path: '/admin/profile', label: '简历管理' },
  { path: '/admin/media', label: '媒体资源' },
  { path: '/admin/users', label: '用户管理' }
];

/**
 * 退出当前后台登录状态。
 */
function handleLogout(): void {
  authStore.clearAuth();
  void router.push('/admin/login');
}
</script>
