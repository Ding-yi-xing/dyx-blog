<template>
  <div class="min-h-screen bg-slate-100">
    <div class="flex min-h-screen">
      <aside class="hidden w-64 flex-col border-r border-slate-200 bg-slate-950 px-5 py-8 text-slate-200 lg:flex">
        <div>
          <div class="text-lg font-semibold">dyx-blog Admin</div>
        </div>
        <nav class="mt-8 flex flex-1 flex-col gap-2 text-sm">
          <RouterLink v-for="item in adminNavItems" :key="item.path" :to="item.path" class="rounded-xl px-4 py-3 transition hover:bg-slate-800">
            {{ item.label }}
          </RouterLink>
        </nav>
      </aside>

      <div class="flex min-h-screen flex-1 flex-col">
        <header class="flex items-center justify-between border-b border-slate-200 bg-white px-4 py-4 sm:px-6">
          <div class="flex items-center gap-3">
            <button
              type="button"
              class="inline-flex h-10 w-10 items-center justify-center rounded-full border border-slate-200 text-slate-700 lg:hidden"
              @click="mobileNavOpen = true"
            >
              ☰
            </button>
            <div>
              <h1 class="text-lg font-semibold text-slate-900">后台管理</h1>
            </div>
          </div>
          <button class="rounded-full bg-slate-900 px-4 py-2 text-sm text-white" @click="handleLogout">退出登录</button>
        </header>

        <main class="flex-1 p-4 sm:p-6">
          <router-view />
        </main>
      </div>
    </div>

    <transition name="mobile-drawer">
      <div v-if="mobileNavOpen" class="fixed inset-0 z-50 lg:hidden">
        <button type="button" class="absolute inset-0 bg-slate-950/45" @click="mobileNavOpen = false"></button>
        <aside class="relative h-full w-72 max-w-[85vw] bg-slate-950 px-5 py-8 text-slate-200 shadow-2xl">
          <div class="flex items-center justify-between">
            <div class="text-lg font-semibold">dyx-blog Admin</div>
            <button type="button" class="rounded-full border border-white/10 px-3 py-1 text-sm" @click="mobileNavOpen = false">关闭</button>
          </div>
          <nav class="mt-8 flex flex-col gap-2 text-sm">
            <RouterLink
              v-for="item in adminNavItems"
              :key="item.path"
              :to="item.path"
              class="rounded-xl px-4 py-3 transition hover:bg-slate-800"
              @click="mobileNavOpen = false"
            >
              {{ item.label }}
            </RouterLink>
          </nav>
        </aside>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const authStore = useAuthStore();
const mobileNavOpen = ref(false);

const adminNavItems = [
  { path: '/admin/dashboard', label: '仪表盘' },
  { path: '/admin/visit-logs', label: '访问日志' },
  { path: '/admin/posts', label: '博客管理' },
  { path: '/admin/moments', label: '动态管理' },
  { path: '/admin/honors', label: '荣誉管理' },
  { path: '/admin/hero', label: '首页横幅管理' },
  { path: '/admin/about', label: '关于我管理' },
  { path: '/admin/resume', label: '简历管理' },
  { path: '/admin/works', label: '作品管理' },
  { path: '/admin/media', label: '媒体资源' },
  { path: '/admin/users', label: '用户管理' }
];

function handleLogout(): void {
  authStore.clearAuth();
  void router.push('/admin/login');
}
</script>

<style scoped>
.mobile-drawer-enter-active,
.mobile-drawer-leave-active {
  transition: opacity 0.2s ease;
}

.mobile-drawer-enter-from,
.mobile-drawer-leave-to {
  opacity: 0;
}
</style>
