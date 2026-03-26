<template>
  <div class="dyx-page-root" :class="[pageBgClass, layoutClass]">
    <DyxTopNav :theme="theme" :toggle-theme="toggleTheme" />

    <main :class="mainClass">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { useRoute } from 'vue-router';
import DyxTopNav from '@/components/web/DyxTopNav.vue';

type ThemeMode = 'light' | 'dark';

const THEME_STORAGE_KEY = 'dyx-theme';

const route = useRoute();
const theme = ref<ThemeMode>('dark');

const pageBgClass = computed(() =>
  typeof route.meta.pageBgClass === 'string' ? route.meta.pageBgClass : 'dyx-page-bg-default'
);

const isHomeRoute = computed(() => route.name === 'dyx-home');

const layoutClass = computed(() =>
  isHomeRoute.value ? 'h-screen overflow-hidden' : 'min-h-screen overflow-x-hidden'
);

const mainClass = computed(() =>
  isHomeRoute.value
    ? 'relative z-10 -mt-[82px] h-screen overflow-hidden sm:-mt-[94px]'
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

onBeforeUnmount(() => {
  clearScheduledThemeTimer();
});
</script>
