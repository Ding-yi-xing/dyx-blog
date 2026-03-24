<template>
  <div class="min-h-screen">
    <header class="sticky top-0 z-30 border-b border-slate-200/60 bg-white/70 backdrop-blur-xl">
      <div class="dyx-page-shell flex items-center justify-between py-4">
        <RouterLink to="/" class="text-xl font-semibold tracking-tight text-slate-900">dyx-blog</RouterLink>
        <nav class="hidden items-center gap-6 text-sm text-slate-600 md:flex">
          <RouterLink v-for="item in dyxNavItems" :key="item.path" :to="item.path" class="transition hover:text-slate-900">
            {{ item.label }}
          </RouterLink>
          <RouterLink to="/admin/login" class="rounded-full bg-slate-900 px-4 py-2 text-white">后台管理</RouterLink>
        </nav>
      </div>
    </header>

    <main class="pb-16 pt-8">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
const dyxNavItems = [
  { path: '/moments', label: '最新动态' },
  { path: '/projects', label: '项目经历' },
  { path: '/photos', label: '个人照片' },
  { path: '/experience', label: '个人经历' },
  { path: '/profile', label: '个人信息' },
  { path: '/blog', label: '博客文章' }
];
</script>
