<template>
  <Teleport to="body">
    <Transition name="global-init-overlay-fade">
      <div
        v-if="visible"
        class="global-init-overlay"
        :class="[
          fullscreen ? 'global-init-overlay--fullscreen' : 'global-init-overlay--contained',
          themeClass,
        ]"
        aria-live="polite"
        aria-busy="true"
      >
        <div class="global-init-overlay__panel" aria-hidden="true">
          <div class="global-init-overlay__halo"></div>
          <div class="global-init-overlay__ring-wrap">
            <span class="global-init-overlay__ring global-init-overlay__ring--outer"></span>
            <span class="global-init-overlay__ring global-init-overlay__ring--middle"></span>
            <span class="global-init-overlay__ring global-init-overlay__ring--inner"></span>
            <span class="global-init-overlay__core"></span>
          </div>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { computed } from 'vue';

const props = withDefaults(
  defineProps<{
    visible: boolean;
    fullscreen?: boolean;
    theme?: 'light' | 'dark';
  }>(),
  {
    fullscreen: true,
    theme: 'dark',
  }
);

const themeClass = computed(() =>
  props.theme === 'dark' ? 'global-init-overlay--dark' : 'global-init-overlay--light'
);
</script>

<style scoped>
.global-init-overlay {
  pointer-events: auto;
  display: flex;
  align-items: center;
  justify-content: center;
  inset: 0;
}

.global-init-overlay--fullscreen {
  position: fixed;
  z-index: 160;
}

.global-init-overlay--contained {
  position: absolute;
}

.global-init-overlay--dark {
  background:
    radial-gradient(circle at 50% 38%, rgba(34, 211, 238, 0.14), transparent 24%),
    linear-gradient(180deg, rgba(2, 6, 23, 0.82), rgba(2, 6, 23, 0.92));
}

.global-init-overlay--light {
  background:
    radial-gradient(circle at 50% 38%, rgba(56, 189, 248, 0.16), transparent 26%),
    linear-gradient(180deg, rgba(255, 255, 255, 0.82), rgba(241, 245, 249, 0.92));
}

.global-init-overlay__panel {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 132px;
  height: 132px;
}

.global-init-overlay__halo {
  position: absolute;
  inset: 12px;
  border-radius: 9999px;
  filter: blur(18px);
  animation: global-init-overlay-pulse 2.6s ease-in-out infinite;
}

.global-init-overlay__ring-wrap {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 92px;
  height: 92px;
}

.global-init-overlay__ring {
  position: absolute;
  inset: 0;
  border-radius: 9999px;
  border-style: solid;
  border-color: transparent;
}

.global-init-overlay__ring--outer {
  border-width: 3px;
  animation: global-init-overlay-spin 1.5s linear infinite;
}

.global-init-overlay__ring--middle {
  inset: 10px;
  border-width: 2px;
  animation: global-init-overlay-spin-reverse 1.15s linear infinite;
}

.global-init-overlay__ring--inner {
  inset: 21px;
  border-width: 2px;
  animation: global-init-overlay-spin 0.95s linear infinite;
}

.global-init-overlay__core {
  width: 12px;
  height: 12px;
  border-radius: 9999px;
  animation: global-init-overlay-core 1.6s ease-in-out infinite;
}

.global-init-overlay--dark .global-init-overlay__halo {
  background: radial-gradient(circle, rgba(34, 211, 238, 0.28), rgba(34, 211, 238, 0));
}

.global-init-overlay--light .global-init-overlay__halo {
  background: radial-gradient(circle, rgba(14, 165, 233, 0.22), rgba(14, 165, 233, 0));
}

.global-init-overlay--dark .global-init-overlay__ring--outer {
  border-top-color: rgba(255, 255, 255, 0.95);
  border-right-color: rgba(34, 211, 238, 0.75);
}

.global-init-overlay--dark .global-init-overlay__ring--middle {
  border-left-color: rgba(125, 211, 252, 0.9);
  border-bottom-color: rgba(255, 255, 255, 0.34);
}

.global-init-overlay--dark .global-init-overlay__ring--inner {
  border-top-color: rgba(255, 255, 255, 0.82);
  border-right-color: rgba(34, 211, 238, 0.52);
}

.global-init-overlay--dark .global-init-overlay__core {
  background: rgba(255, 255, 255, 0.96);
  box-shadow: 0 0 24px rgba(34, 211, 238, 0.42);
}

.global-init-overlay--light .global-init-overlay__ring--outer {
  border-top-color: rgba(15, 23, 42, 0.9);
  border-right-color: rgba(14, 165, 233, 0.7);
}

.global-init-overlay--light .global-init-overlay__ring--middle {
  border-left-color: rgba(2, 132, 199, 0.78);
  border-bottom-color: rgba(15, 23, 42, 0.22);
}

.global-init-overlay--light .global-init-overlay__ring--inner {
  border-top-color: rgba(15, 23, 42, 0.72);
  border-right-color: rgba(56, 189, 248, 0.56);
}

.global-init-overlay--light .global-init-overlay__core {
  background: rgb(15, 23, 42);
  box-shadow: 0 0 20px rgba(56, 189, 248, 0.32);
}

.global-init-overlay-fade-enter-active,
.global-init-overlay-fade-leave-active {
  transition: opacity 0.32s ease;
}

.global-init-overlay-fade-enter-from,
.global-init-overlay-fade-leave-to {
  opacity: 0;
}

@keyframes global-init-overlay-spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

@keyframes global-init-overlay-spin-reverse {
  from {
    transform: rotate(360deg);
  }
  to {
    transform: rotate(0deg);
  }
}

@keyframes global-init-overlay-pulse {
  0%,
  100% {
    transform: scale(0.92);
    opacity: 0.52;
  }
  50% {
    transform: scale(1.08);
    opacity: 0.9;
  }
}

@keyframes global-init-overlay-core {
  0%,
  100% {
    transform: scale(0.82);
    opacity: 0.76;
  }
  50% {
    transform: scale(1.18);
    opacity: 1;
  }
}
</style>
