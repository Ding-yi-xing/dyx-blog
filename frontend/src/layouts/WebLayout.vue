<template>
  <div class="dyx-page-root" :class="[pageBgClass, layoutClass]">
    <DyxTopNav :theme="theme" :toggle-theme="toggleTheme" :visible="topNavVisible" />

    <main :class="mainClass">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, provide, ref, watch } from 'vue';
import { useRoute } from 'vue-router';
import DyxTopNav from '@/components/web/DyxTopNav.vue';

type ThemeMode = 'light' | 'dark';

const THEME_STORAGE_KEY = 'dyx-theme';
const DAY_THEME_START_HOUR = 8;
const NIGHT_THEME_START_HOUR = 18;

/**
 * 前台站点布局组件。
 * 负责挂载顶部导航、主题切换、首页全屏布局以及主题状态的 provide 注入。
 */
const route = useRoute();
const theme = ref<ThemeMode>('dark');
const topNavVisible = ref(true);

/**
 * 供子组件控制顶部导航显隐状态。
 *
 * @param visible 顶部导航是否显示。
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；仅更新本地状态。
 * @author Dyx
 */
function setTopNavVisible(visible: boolean): void {
  if (topNavVisible.value === visible) {
    return;
  }
  topNavVisible.value = visible;
}

/**
 * 供子组件直接设置当前主题模式。
 *
 * @param nextTheme 新的主题模式。
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；仅更新本地状态。
 * @author Dyx
 */
function setTheme(nextTheme: ThemeMode): void {
  theme.value = nextTheme;
}

provide('dyx-set-top-nav-visible', setTopNavVisible);
provide('dyx-theme', theme);
provide('dyx-set-theme', setTheme);

/**
 * 根据当前路由解析页面背景类名。
 */
const pageBgClass = computed(() =>
  typeof route.meta.pageBgClass === 'string' ? route.meta.pageBgClass : 'dyx-page-bg-default'
);

/**
 * 判断当前是否位于首页，以决定是否启用全屏滚动布局。
 */
const isHomeRoute = computed(() => route.name === 'dyx-home');

/**
 * 根据页面类型切换根容器布局模式。
 */
const layoutClass = computed(() =>
  isHomeRoute.value ? 'h-screen overflow-hidden' : 'min-h-screen overflow-x-hidden'
);

/**
 * 根据页面类型切换主内容区的定位与内边距。
 */
const mainClass = computed(() =>
  isHomeRoute.value
    ? 'absolute inset-0 z-10 h-full overflow-hidden'
    : 'relative z-10 pb-16 pt-24 sm:pb-20 sm:pt-28'
);

let scheduledThemeTimer: number | null = null;

/**
 * 同步主题到文档根节点。
 *
 * @param nextTheme 需要应用的主题模式。
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；仅更新 DOM 主题状态。
 * @author Dyx
 */
function syncDocumentTheme(nextTheme: ThemeMode): void {
  theme.value = nextTheme;
  document.documentElement.classList.toggle('dark', nextTheme === 'dark');
  document.documentElement.style.colorScheme = nextTheme;
}

/**
 * 读取本地持久化的主题偏好。
 *
 * @returns 返回 light/dark；未命中时返回 null。
 * @throws 该函数不会主动抛出异常；仅读取 localStorage。
 * @author Dyx
 */
function getStoredTheme(): ThemeMode | null {
  const stored = window.localStorage.getItem(THEME_STORAGE_KEY);
  return stored === 'light' || stored === 'dark' ? stored : null;
}

/**
 * 根据当前时间推导自动主题。
 *
 * @param now 当前时间，默认使用系统时间。
 * @returns 白天返回 light，夜间返回 dark。
 * @throws 该函数不会主动抛出异常；仅执行本地时间判断。
 * @author Dyx
 */
function getAutoTheme(now = new Date()): ThemeMode {
  const hour = now.getHours();
  return hour >= DAY_THEME_START_HOUR && hour < NIGHT_THEME_START_HOUR ? 'light' : 'dark';
}

/**
 * 清理已注册的自动主题切换定时器。
 *
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；仅在存在定时器时执行清理。
 * @author Dyx
 */
function clearScheduledThemeTimer(): void {
  if (scheduledThemeTimer !== null) {
    window.clearTimeout(scheduledThemeTimer);
    scheduledThemeTimer = null;
  }
}

/**
 * 计算下一次自动主题切换的时间边界。
 *
 * @param now 当前时间，默认使用系统时间。
 * @returns 返回下一个 08:00 或 18:00 的时间点。
 * @throws 该函数不会主动抛出异常；仅执行本地时间计算。
 * @author Dyx
 */
function getNextThemeBoundary(now = new Date()): Date {
  const nextBoundary = new Date(now);
  nextBoundary.setSeconds(0, 0);
  const hour = now.getHours();
  if (hour < DAY_THEME_START_HOUR) {
    nextBoundary.setHours(DAY_THEME_START_HOUR, 0, 0, 0);
    return nextBoundary;
  }
  if (hour < NIGHT_THEME_START_HOUR) {
    nextBoundary.setHours(NIGHT_THEME_START_HOUR, 0, 0, 0);
    return nextBoundary;
  }
  nextBoundary.setDate(nextBoundary.getDate() + 1);
  nextBoundary.setHours(DAY_THEME_START_HOUR, 0, 0, 0);
  return nextBoundary;
}

/**
 * 在未设置手动主题偏好时，安排下一次自动主题切换。
 *
 * @param now 用于计算切换边界的当前时间。
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；仅注册本地定时器。
 * @author Dyx
 */
function scheduleNextThemeSwitch(now = new Date()): void {
  clearScheduledThemeTimer();
  if (getStoredTheme()) {
    return;
  }
  const nextBoundary = getNextThemeBoundary(now);
  const delay = Math.max(1000, nextBoundary.getTime() - now.getTime());
  scheduledThemeTimer = window.setTimeout(() => {
    if (!getStoredTheme()) {
      const currentTime = new Date();
      syncDocumentTheme(getAutoTheme(currentTime));
      scheduleNextThemeSwitch(currentTime);
    }
  }, delay);
}

/**
 * 应用自动主题并安排后续自动切换。
 *
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；仅执行主题同步与定时器注册。
 * @author Dyx
 */
function applyAutoTheme(): void {
  const now = new Date();
  syncDocumentTheme(getAutoTheme(now));
  scheduleNextThemeSwitch(now);
}

/**
 * 手动切换前台主题，并写入本地持久化偏好。
 *
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；仅更新主题并写入 localStorage。
 * @author Dyx
 */
function toggleTheme(): void {
  const nextTheme: ThemeMode = theme.value === 'dark' ? 'light' : 'dark';
  syncDocumentTheme(nextTheme);
  window.localStorage.setItem(THEME_STORAGE_KEY, nextTheme);
  clearScheduledThemeTimer();
}

onMounted(() => {
  const storedTheme = getStoredTheme();
  if (storedTheme) {
    syncDocumentTheme(storedTheme);
    clearScheduledThemeTimer();
    return;
  }
  applyAutoTheme();
});

watch(
  isHomeRoute,
  (value) => {
    if (!value) {
      setTopNavVisible(true);
    }
  },
  { immediate: true }
);

onBeforeUnmount(() => {
  clearScheduledThemeTimer();
  setTopNavVisible(true);
});
</script>
