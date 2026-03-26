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
const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');

const pageBgClass = computed(() =>
  typeof route.meta.pageBgClass === 'string' ? route.meta.pageBgClass : 'dyx-page-bg-default'
);

const isHomeRoute = computed(() => route.name === 'dyx-home');

const layoutClass = computed(() =>
  isHomeRoute.value ? 'h-screen overflow-hidden' : 'min-h-screen overflow-x-hidden'
);

const mainClass = computed(() =>
  isHomeRoute.value
    ? 'relative z-10 h-[calc(100vh-82px)] overflow-hidden pb-3 pt-0 sm:h-[calc(100vh-94px)] sm:pb-4'
    : 'relative z-10 pb-16 pt-4 sm:pb-20 sm:pt-6'
);

function syncDocumentTheme(nextTheme: ThemeMode): void {
  theme.value = nextTheme;
  document.documentElement.classList.toggle('dark', nextTheme === 'dark');
  document.documentElement.style.colorScheme = nextTheme;
}

function getStoredTheme(): ThemeMode | null {
  const stored = window.localStorage.getItem(THEME_STORAGE_KEY);
  return stored === 'light' || stored === 'dark' ? stored : null;
}

function applyThemeFromSystem(): void {
  syncDocumentTheme(mediaQuery.matches ? 'dark' : 'light');
}

function toggleTheme(): void {
  const nextTheme: ThemeMode = theme.value === 'dark' ? 'light' : 'dark';
  syncDocumentTheme(nextTheme);
  window.localStorage.setItem(THEME_STORAGE_KEY, nextTheme);
}

function handleSystemThemeChange(event: MediaQueryListEvent): void {
  if (getStoredTheme()) {
    return;
  }
  syncDocumentTheme(event.matches ? 'dark' : 'light');
}

onMounted(() => {
  const storedTheme = getStoredTheme();
  if (storedTheme) {
    syncDocumentTheme(storedTheme);
  } else {
    applyThemeFromSystem();
  }
  mediaQuery.addEventListener('change', handleSystemThemeChange);
});

onBeforeUnmount(() => {
  mediaQuery.removeEventListener('change', handleSystemThemeChange);
});
</script>
