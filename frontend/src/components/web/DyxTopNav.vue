<template>
  <header
    class="fixed inset-x-0 top-0 z-50 w-full bg-transparent px-0 transition duration-300"
    :class="props.visible === false ? 'dyx-topbar-hidden' : 'dyx-topbar-visible'"
    style="background-color: transparent !important"
  >
    <div class="w-full px-3 sm:px-4 lg:px-5">
      <div class="w-full px-4 py-3 sm:px-5 lg:px-6">
        <div
          class="grid grid-cols-[minmax(0,auto)_1fr_auto] items-center gap-3 lg:gap-4"
        >
          <button
            type="button"
            class="flex min-w-0 items-center text-left"
            @click="handleBrandClick"
          >
            <span class="min-w-0">
              <span
                class="block truncate text-[12px] font-semibold tracking-[0.18em] dyx-text-main sm:text-[13px]"
                >dyx-blog</span
              >
            </span>
          </button>

          <nav
            class="hidden items-center justify-self-start gap-1 md:flex lg:gap-2"
          >
            <RouterLink
              v-for="item in dyxNavItems"
              :key="item.path"
              :to="item.path"
              class="dyx-topbar-link px-2 py-2 text-[13px] font-semibold tracking-[0.1em] lg:px-3 lg:text-[14px]"
              :class="route.path === item.path ? 'dyx-topbar-link--active' : ''"
            >
              {{ item.label }}
            </RouterLink>
          </nav>

          <div class="flex items-center justify-self-end gap-2">
            <button
              type="button"
              class="hidden items-center gap-2 px-2 py-2 text-[11px] font-medium uppercase tracking-[0.22em] dyx-text-main sm:inline-flex"
              @click="handleToggleTheme"
            >
              <span
                class="h-2 w-2 rounded-full bg-[rgb(var(--dyx-accent-rgb))]"
              ></span>
              <span>{{ theme === "dark" ? "Dark" : "Light" }}</span>
            </button>
            <button
              type="button"
              class="inline-flex h-10 w-10 items-center justify-center dyx-text-main md:hidden"
              aria-label="打开菜单"
              @click="mobileNavOpen = true"
            >
              <svg
                viewBox="0 0 24 24"
                class="h-4 w-4"
                fill="none"
                stroke="currentColor"
                stroke-width="1.8"
                stroke-linecap="round"
              >
                <path d="M4 7h16" />
                <path d="M4 12h16" />
                <path d="M4 17h16" />
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>

    <transition name="mobile-drawer">
      <div
        v-if="mobileNavOpen"
        class="fixed inset-0 z-50 px-4 py-4 md:hidden sm:px-6 sm:py-5"
      >
        <button
          type="button"
          class="absolute inset-0 bg-slate-950/18 backdrop-blur-sm"
          @click="mobileNavOpen = false"
        ></button>
        <div
          class="dyx-page-card-elevated relative ml-auto w-full max-w-[320px] rounded-[32px] p-5"
        >
          <div class="flex items-center justify-between gap-3">
            <div>
              <p
                class="text-[11px] font-medium uppercase tracking-[0.28em] dyx-text-meta"
              >
                menu
              </p>
              <p class="mt-1 text-lg font-semibold dyx-text-main">导航</p>
            </div>
            <button
              type="button"
              class="dyx-topbar-control inline-flex rounded-full px-3 py-2 text-[11px] font-medium uppercase tracking-[0.2em] dyx-text-main"
              @click="mobileNavOpen = false"
            >
              关闭
            </button>
          </div>

          <nav class="mt-6 flex flex-col gap-2">
            <RouterLink
              v-for="item in dyxNavItems"
              :key="item.path"
              :to="item.path"
              class="rounded-[22px] px-4 py-3 text-sm font-medium dyx-text-main transition hover:bg-[rgb(var(--dyx-bg-surface-rgb)/0.45)]"
              @click="mobileNavOpen = false"
            >
              {{ item.label }}
            </RouterLink>
          </nav>

          <button
            type="button"
            class="dyx-topbar-control mt-6 inline-flex w-full items-center justify-center gap-2 rounded-full px-4 py-3 text-[11px] font-medium uppercase tracking-[0.22em] dyx-text-main"
            @click="handleToggleTheme"
          >
            <span
              class="h-2 w-2 rounded-full bg-[rgb(var(--dyx-accent-rgb))]"
            ></span>
            <span>切换到{{ theme === "dark" ? "浅色" : "深色" }}模式</span>
          </button>
        </div>
      </div>
    </transition>
  </header>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { useRoute, useRouter } from "vue-router";

const props = defineProps<{
  theme: "light" | "dark";
  toggleTheme: () => void;
  visible?: boolean;
}>();

const route = useRoute();
const router = useRouter();
const mobileNavOpen = ref(false);

const dyxNavItems = [
  { path: "/blog", label: "博客" },
  { path: "/moments", label: "动态" },
  { path: "/guestbook", label: "留言" },
  { path: "/about", label: "关于我" },
  { path: "/resume", label: "简历" },
];

function handleBrandClick(): void {
  void router.push("/");
}

function handleToggleTheme(): void {
  props.toggleTheme();
}
</script>

<style scoped>
.mobile-drawer-enter-active,
.mobile-drawer-leave-active {
  transition: opacity 0.22s ease;
}

.mobile-drawer-enter-from,
.mobile-drawer-leave-to {
  opacity: 0;
}

.dyx-topbar-visible {
  opacity: 1;
  transform: translateY(0);
  pointer-events: auto;
}

.dyx-topbar-hidden {
  opacity: 0;
  transform: translateY(-16px);
  pointer-events: none;
}
</style>
