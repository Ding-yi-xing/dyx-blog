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

const route = useRoute();
const theme = ref<ThemeMode>('dark');
const topNavVisible = ref(true);

function setTopNavVisible(visible: boolean): void {
  topNavVisible.value = visible;
}

function setTheme(nextTheme: ThemeMode): void {
  theme.value = nextTheme;
}

provide('dyx-set-top-nav-visible', setTopNavVisible);
provide('dyx-theme', theme);
provide('dyx-set-theme', setTheme);

const pageBgClass = computed(() =>
  typeof route.meta.pageBgClass === 'string' ? route.meta.pageBgClass : 'dyx-page-bg-default'
);

const isHomeRoute = computed(() => route.name === 'dyx-home');

const layoutClass = computed(() =>
  isHomeRoute.value ? 'h-screen overflow-hidden' : 'min-h-screen overflow-x-hidden'
);

const mainClass = computed(() =>
  isHomeRoute.value
    ? 'absolute inset-0 z-10 h-full overflow-hidden'
    : 'relative z-10 pb-16 pt-4 sm:pb-20 sm:pt-6'
);

let scheduledThemeTimer: number | null = null;

function syncDocumentTheme(nextTheme: ThemeMode): void {
  theme.value = nextTheme;
  document.documentElement.classList.toggle('dark', nextTheme === 'dark');
  document.documentElement.style.colorScheme = nextTheme;
}

function getStoredTheme(): ThemeMode | null {
  const stored = window.localStorage.getItem(THEME_STORAGE_KEY);
  return stored === 'light' || stored === 'dark' ? stored : null;
}

function getAutoTheme(now = new Date()): ThemeMode {
  const hour = now.getHours();
  return hour >= 8 && hour < 18 ? 'light' : 'dark';
}

function clearScheduledThemeTimer(): void {
  if (scheduledThemeTimer !== null) {
    window.clearTimeout(scheduledThemeTimer);
    scheduledThemeTimer = null;
  }
}

function getNextThemeBoundary(now = new Date()): Date {
  const nextBoundary = new Date(now);
  nextBoundary.setSeconds(0, 0);
  const hour = now.getHours();
  if (hour < 8) {
    nextBoundary.setHours(8, 0, 0, 0);
    return nextBoundary;
  }
  if (hour < 18) {
    nextBoundary.setHours(18, 0, 0, 0);
    return nextBoundary;
  }
  nextBoundary.setDate(nextBoundary.getDate() + 1);
  nextBoundary.setHours(8, 0, 0, 0);
  return nextBoundary;
}

function scheduleNextThemeSwitch(): void {
  clearScheduledThemeTimer();
  if (getStoredTheme()) {
    return;
  }
  const now = new Date();
  const nextBoundary = getNextThemeBoundary(now);
  const delay = Math.max(1000, nextBoundary.getTime() - now.getTime());
  scheduledThemeTimer = window.setTimeout(() => {
    if (!getStoredTheme()) {
      syncDocumentTheme(getAutoTheme());
      scheduleNextThemeSwitch();
    }
  }, delay);
}

function applyAutoTheme(): void {
  syncDocumentTheme(getAutoTheme());
  scheduleNextThemeSwitch();
}

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
      topNavVisible.value = true;
    }
  },
  { immediate: true }
);

onBeforeUnmount(() => {
  clearScheduledThemeTimer();
  topNavVisible.value = true;
});
</script>
