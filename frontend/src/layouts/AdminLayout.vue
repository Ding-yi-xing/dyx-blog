<template>
  <div class="min-h-screen bg-slate-100">
    <div class="flex min-h-screen">
      <aside
        class="hidden w-64 shrink-0 flex-col border-r border-slate-200 bg-slate-950 px-5 py-8 text-slate-200 lg:flex overflow-y-auto max-h-screen sticky top-0"
      >
        <div>
          <div class="text-lg font-semibold">dyx-blog Admin</div>
        </div>
        <el-menu
          :default-active="activeMenu"
          background-color="transparent"
          text-color="#94a3b8"
          active-text-color="#ffffff"
          class="admin-menu mt-8 flex-1 border-none"
          router
        >
          <el-sub-menu
            v-for="group in adminNavGroups"
            :key="group.label"
            :index="group.label"
          >
            <template #title>
              <span class="text-xs font-medium uppercase tracking-[0.22em]">{{
                group.label
              }}</span>
            </template>
            <el-menu-item
              v-for="item in group.items"
              :key="item.path"
              :index="item.path"
              class="rounded-xl"
            >
              {{ item.label }}
            </el-menu-item>
          </el-sub-menu>
        </el-menu>
      </aside>

      <div class="flex min-h-screen min-w-0 flex-1 flex-col">
        <header
          class="flex items-center justify-between border-b border-slate-200 bg-white px-4 py-4 sm:px-6"
        >
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
          <button
            class="rounded-full bg-slate-900 px-4 py-2 text-sm text-white"
            @click="handleLogout"
          >
            退出登录
          </button>
        </header>

        <main class="flex-1 p-4 sm:p-6">
          <router-view />
        </main>
      </div>
    </div>

    <transition name="mobile-drawer">
      <div v-if="mobileNavOpen" class="fixed inset-0 z-50 lg:hidden">
        <button
          type="button"
          class="absolute inset-0 bg-slate-950/45"
          @click="mobileNavOpen = false"
        ></button>
        <aside
          class="relative h-full w-72 max-w-[85vw] bg-slate-950 px-5 py-8 text-slate-200 shadow-2xl overflow-y-auto flex flex-col"
        >
          <div class="flex items-center justify-between shrink-0">
            <div class="text-lg font-semibold">dyx-blog Admin</div>
            <button
              type="button"
              class="rounded-full border border-white/10 px-3 py-1 text-sm"
              @click="mobileNavOpen = false"
            >
              关闭
            </button>
          </div>
          <el-menu
            :default-active="activeMenu"
            background-color="transparent"
            text-color="#94a3b8"
            active-text-color="#ffffff"
            class="admin-menu mt-8 flex-1 border-none"
            router
            @select="mobileNavOpen = false"
          >
            <el-sub-menu
              v-for="group in adminNavGroups"
              :key="group.label"
              :index="group.label"
            >
              <template #title>
                <span class="text-xs font-medium uppercase tracking-[0.22em]">{{
                  group.label
                }}</span>
              </template>
              <el-menu-item
                v-for="item in group.items"
                :key="item.path"
                :index="item.path"
                class="rounded-xl"
              >
                {{ item.label }}
              </el-menu-item>
            </el-sub-menu>
          </el-menu>
        </aside>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

/**
 * 后台管理布局组件。
 * 负责渲染后台导航、移动端抽屉菜单，以及统一处理退出登录跳转。
 */
const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();
const mobileNavOpen = ref(false);

/**
 * 根据当前路由高亮对应菜单项。
 */
const activeMenu = computed(() => route.path);

/**
 * 后台导航分组配置。
 */
const adminNavGroups = [
  {
    label: '概览',
    items: [
      { path: '/dyx-manager/dashboard', label: '仪表盘' },
      { path: '/dyx-manager/visit-logs', label: '访问日志' }
    ]
  },
  {
    label: '首页管理',
    items: [
      { path: '/dyx-manager/home/hero', label: '首屏管理' },
      { path: '/dyx-manager/home/footprints', label: '足迹管理' },
      { path: '/dyx-manager/home/activity', label: '第三屏说明' }
    ]
  },
  {
    label: '内容管理',
    items: [
      { path: '/dyx-manager/guestbook', label: '留言管理' },
      { path: '/dyx-manager/posts', label: '博客管理' },
      { path: '/dyx-manager/moments', label: '动态管理' },
      { path: '/dyx-manager/honors', label: '荣誉管理' },
      { path: '/dyx-manager/works', label: '作品管理' }
    ]
  },
  {
    label: '个人资料',
    items: [
      { path: '/dyx-manager/about', label: '关于我管理' },
      { path: '/dyx-manager/resume', label: '简历管理' }
    ]
  },
  {
    label: '系统',
    items: [
      { path: '/dyx-manager/media', label: '媒体资源' },
      { path: '/dyx-manager/system-config', label: '系统配置' },
      { path: '/dyx-manager/users', label: '用户管理' }
    ]
  }
];

/**
 * 清理登录态并跳转回后台登录页。
 *
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；导航失败会由路由自身处理。
 * @author Dyx
 */
function handleLogout(): void {
  authStore.clearAuth();
  void router.push('/dyx-manager/login');
}
</script>

<style scoped>
.mobile-drawer-enter-active,
.mobile-drawer-leave-active {
  transition: all 0.3s ease-in-out;
}

.mobile-drawer-enter-active aside,
.mobile-drawer-leave-active aside {
  transition: transform 0.3s ease-in-out;
}

.mobile-drawer-enter-from,
.mobile-drawer-leave-to {
  opacity: 0;
}

.mobile-drawer-enter-from aside,
.mobile-drawer-leave-to aside {
  transform: translateX(-100%);
}

:deep(.admin-menu) {
  border-right: none;
}

:deep(.el-sub-menu__title) {
  border-radius: 0.75rem;
  margin-bottom: 0.25rem;
}

:deep(.el-sub-menu__title:hover) {
  background-color: #1e293b !important; /* slate-800 */
}

:deep(.el-menu-item) {
  border-radius: 0.75rem;
  margin-bottom: 0.25rem;
  height: 44px;
  line-height: 44px;
}

:deep(.el-menu-item:hover) {
  background-color: #1e293b !important;
}

:deep(.el-menu-item.is-active) {
  background-color: #1e293b !important;
  color: white !important;
}
</style>
