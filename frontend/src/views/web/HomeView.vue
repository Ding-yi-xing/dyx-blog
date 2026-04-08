<template>
  <section class="relative h-full w-full px-0">
    <div
      ref="scrollContainer"
      class="relative h-full overflow-y-auto snap-y snap-mandatory scroll-smooth scrollbar-none"
    >
      <section
        class="relative flex h-full w-full snap-start items-stretch overflow-hidden"
      >
        <div
          v-if="heroBackgroundStyle"
          class="home-hero-bg home-hero-bg--image pointer-events-none absolute inset-0"
          :style="heroBackgroundStyle"
        ></div>
        <div
          v-else
          class="home-hero-bg home-hero-bg--fallback pointer-events-none absolute inset-0"
        ></div>

        <div
          class="relative z-10 grid h-full w-full min-w-0 px-4 sm:px-5 lg:px-8 xl:px-10"
          :class="heroViewportClass"
        >
          <div
            class="grid h-full w-full min-w-0 overflow-hidden px-5 sm:px-7 lg:gap-12 lg:px-10 xl:gap-16 xl:px-14"
            :class="heroInnerClass"
          >
            <div
              class="home-hero-copy flex min-w-0 flex-col"
              :class="[heroCopyClass, !hasHeroImage ? 'lg:col-span-2' : '']"
            >
              <div
                class="flex min-w-0 flex-1 flex-col"
                :class="heroContentClass"
              >
                <template v-for="block in heroContentBlocks" :key="block.id">
                  <p
                    v-if="block.type === 'eyebrow'"
                    class="home-meta mb-4 text-[11px] font-semibold uppercase tracking-[0.42em] sm:text-xs lg:mb-5 lg:text-sm"
                  >
                    {{ block.text || "HELLO THERE!" }}
                  </p>
                  <h1
                    v-else-if="block.type === 'title'"
                    class="home-section-title mb-5 max-w-4xl whitespace-nowrap text-[2.05rem] font-semibold leading-[1.04] tracking-[-0.05em] sm:text-[2.65rem] lg:mb-6 lg:text-[3.55rem] xl:text-[4.1rem]"
                  >
                    {{ block.text || "写代码的人，也写点文字。" }}
                  </h1>
                  <p
                    v-else-if="block.type === 'subtitle'"
                    class="home-section-text mb-5 max-w-[34rem] text-[15px] leading-8 sm:text-[1.02rem] sm:leading-8 lg:mb-6 lg:text-[1.2rem] lg:leading-9"
                  >
                    {{
                      block.text ||
                      "这里有后端开发、安全研究、折腾小工具的记录，也有一些不那么严肃的碎碎念。这个博客更像是一个公开的笔记本，欢迎随便翻翻。"
                    }}
                  </p>
                  <template v-else-if="isTagsBlock(block)">
                    <div
                      class="mb-5 flex flex-wrap gap-2.5 text-xs font-medium sm:text-sm lg:mb-7"
                    >
                      <span
                        v-for="item in block.items"
                        :key="item"
                        class="inline-flex items-center rounded-full bg-[rgb(var(--dyx-accent-soft-rgb)/0.92)] px-4 py-2 text-[rgb(var(--dyx-accent-strong-rgb))] lg:px-5 lg:py-2.5"
                      >
                        {{ item }}
                      </span>
                    </div>
                    <div class="flex flex-wrap items-center gap-4">
                      <RouterLink
                        to="/blog"
                        class="inline-flex items-center rounded-full bg-[rgb(var(--dyx-text-main-rgb))] px-6 py-3.5 text-sm font-medium text-[rgb(var(--dyx-text-inverse-rgb))] transition hover:opacity-90 lg:px-7 lg:py-4 lg:text-[15px]"
                      >
                        阅读博客
                      </RouterLink>
                      <RouterLink
                        to="/about"
                        class="dyx-ghost-pill inline-flex items-center px-6 py-3.5 lg:px-7 lg:py-4 lg:text-[15px]"
                      >
                        查看关于我
                      </RouterLink>
                    </div>
                  </template>
                </template>
              </div>
            </div>

            <div
              v-if="hasHeroImage"
              class="home-hero-photo-stage relative flex min-h-0 w-full items-center justify-center overflow-hidden px-0 py-0 sm:px-0 lg:min-h-[74vh] lg:justify-end lg:pl-6 lg:py-0 xl:pl-10"
            >
              <div
                class="relative flex h-full w-full items-center justify-center overflow-hidden lg:justify-end"
              >
                <img
                  :src="heroImageBlock?.imageUrl"
                  :alt="heroImageBlock?.alt || 'avatar'"
                  class="home-hero-photo h-full max-h-[74vh] w-full object-contain object-center lg:max-h-[86vh] lg:object-right"
                />
              </div>
            </div>
          </div>
        </div>
      </section>

      <section
        class="relative h-full w-full snap-start overflow-hidden"
        :class="footprintSectionClass"
      >
        <div class="absolute inset-y-0 left-0 z-[2] w-[15%] touch-pan-y"></div>
        <div class="absolute inset-y-0 right-0 z-[2] w-[15%] touch-pan-y"></div>
        <div class="absolute inset-0 z-0" :class="footprintBackdropClass"></div>
        <div class="absolute inset-0 z-[1] overflow-hidden">
          <div
            v-if="!hasInitLoadedOnce"
            class="flex h-full w-full items-center justify-center text-sm"
            :class="footprintEmptyClass"
          >
            <el-skeleton animated :rows="4" class="w-[min(480px,90%)] rounded-2xl bg-black/10 px-6 py-5" />
          </div>
          <HomeFootprintMap
            v-else-if="footprints.length"
            class="h-full w-full"
            :items="footprintMapData"
            :visited-province-names="visitedProvinceNames"
            :theme="activeTheme"
            :visible="shouldActivateMap"
          />
          <div
            v-else
            class="flex h-full w-full items-center justify-center text-sm"
            :class="footprintEmptyClass"
          >
            暂时还没有公开足迹，后续会逐步补充。
          </div>
        </div>

        <div
          class="pointer-events-none absolute inset-x-0 top-0 z-10 px-4 pt-[20px] sm:px-6 sm:pt-[72px] lg:px-8 lg:pt-[86px]"
        >
          <div class="max-w-[34rem] space-y-2 sm:space-y-2.5">
            <p
              class="home-meta text-[11px] font-medium uppercase tracking-[0.3em]"
              :class="footprintMetaClass"
            >
              {{ footprintEyebrow }}
            </p>
            <h2
              class="home-section-title text-2xl font-semibold tracking-tight sm:text-3xl"
              :class="footprintTitleClass"
            >
              {{ footprintTitle }}
            </h2>
            <p
              class="max-w-[30rem] text-sm leading-7 sm:text-[15px]"
              :class="footprintSubtitleClass"
            >
              {{ footprintSubtitle }}
            </p>
            <p
              class="hidden max-w-[32rem] text-sm leading-7 sm:block sm:text-[15px]"
              :class="footprintDescriptionClass"
            >
              {{ footprintDescription }}
            </p>
          </div>
        </div>

        <div
          v-if="!hasInitLoadedOnce"
          class="pointer-events-none absolute inset-x-0 bottom-0 z-10 px-3 pb-3 sm:px-6 sm:pb-5 lg:px-8 lg:pb-6"
        >
          <div
            class="pointer-events-auto mx-auto w-full rounded-[26px] border px-4 py-4 backdrop-blur md:ml-auto md:mr-0 md:max-w-[18rem] md:rounded-[22px] md:px-4 md:py-3 lg:max-w-[20rem] xl:max-w-[22rem] xl:rounded-[24px] xl:px-4 xl:py-3"
            :class="footprintStatsClass"
          >
            <el-skeleton animated :rows="2" />
          </div>
        </div>

        <div
          v-else-if="footprints.length"
          class="pointer-events-none absolute inset-x-0 bottom-0 z-10 px-3 pb-3 sm:px-6 sm:pb-5 lg:px-8 lg:pb-6"
        >
          <div
            class="pointer-events-auto mx-auto w-full rounded-[26px] border px-3 py-3 backdrop-blur md:ml-auto md:mr-0 md:max-w-[18rem] md:rounded-[22px] md:px-3 md:py-2 lg:max-w-[20rem] xl:max-w-[22rem] xl:rounded-[24px] xl:px-4 xl:py-3"
            :class="footprintStatsClass"
          >
            <div class="grid grid-cols-4 gap-2 md:grid-cols-2 md:gap-2">
              <div
                v-for="item in footprintStats"
                :key="item.label"
                class="min-w-0 rounded-[18px] px-2.5 py-3 md:px-3 md:py-2 md:w-32 lg:w-36 xl:px-4 xl:py-2.5 xl:w-40"
                :class="footprintStatsItemClass"
              >
                <p
                  class="truncate text-[10px] font-medium uppercase tracking-[0.14em] md:text-[10px] md:tracking-[0.14em] xl:text-[11px] xl:tracking-[0.18em]"
                  :class="footprintMetaClass"
                >
                  {{ item.label }}
                </p>
                <p
                  class="mt-1.5 text-center text-sm font-semibold leading-5 sm:text-[15px] md:mt-1.5 md:text-left md:text-base xl:mt-2 xl:text-lg"
                  :class="footprintTitleClass"
                >
                  {{ item.value }}
                </p>
              </div>
            </div>
          </div>
        </div>
      </section>

      <section
        class="relative h-full w-full snap-start overflow-hidden"
        :class="activitySectionClass"
      >
        <div class="absolute inset-0" :class="activityBackdropClass"></div>
        <div class="relative z-10 flex h-full items-center justify-center px-4">
          <div class="flex w-full max-w-6xl flex-col gap-8 lg:flex-row lg:items-stretch">
            <div class="flex-1 space-y-4 text-left">
              <p class="home-meta text-[11px] font-medium uppercase tracking-[0.3em]" :class="activityMetaClass">
                能力与精选项目
              </p>
              <h2
                class="home-section-title text-2xl font-semibold tracking-tight sm:text-3xl"
                :class="activityTitleClass"
              >
                我擅长什么，以及这些能力落在了哪些具体项目里。
              </h2>
              <p
                class="max-w-xl text-sm leading-7 sm:text-[15px]"
                :class="activityTextClass"
              >
                这里挑了一小部分代表性的经历和作品，用来补充首页第一屏里那几行文案背后的具体内容。更多细节可以在“项目经历”和“关于我 / 简历”里展开看。
              </p>
            </div>

            <div class="flex-1 min-w-0">
              <div v-if="!hasInitLoadedOnce" class="grid gap-4 sm:grid-cols-2">
                <el-skeleton
                  v-for="n in 2"
                  :key="`activity-skeleton-${n}`"
                  animated
                  :rows="4"
                  class="dyx-page-card rounded-2xl p-4 shadow-dyx-soft/70"
                />
              </div>

              <div v-else class="grid gap-4 sm:grid-cols-2">
                <article
                  v-for="item in featuredProjects"
                  :key="`project-${item.id}`"
                  class="dyx-page-card rounded-2xl p-4 shadow-dyx-soft/70"
                >
                  <p class="text-[11px] font-medium tracking-[0.22em] uppercase" :class="activityMetaClass">
                    项目
                  </p>
                  <h3 class="mt-2 text-base font-semibold" :class="activityTitleClass">
                    {{ item.name }}
                  </h3>
                  <p class="mt-2 line-clamp-3 text-xs leading-6" :class="activityTextClass">
                    {{ item.description || '这个项目还在整理描述。' }}
                  </p>
                  <p v-if="item.techStack" class="mt-2 text-[11px] leading-5 opacity-80" :class="activityTextClass">
                    {{ item.techStack }}
                  </p>
                </article>

                <article
                  v-for="item in featuredWorks"
                  :key="`work-${item.id}`"
                  class="dyx-page-card rounded-2xl p-4 shadow-dyx-soft/70"
                >
                  <p class="text-[11px] font-medium tracking-[0.22em] uppercase" :class="activityMetaClass">
                    作品
                  </p>
                  <h3 class="mt-2 text-base font-semibold" :class="activityTitleClass">
                    {{ item.title }}
                  </h3>
                  <p class="mt-2 line-clamp-3 text-xs leading-6" :class="activityTextClass">
                    {{ item.summary || '这个作品的说明还在补充中。' }}
                  </p>
                </article>
              </div>
            </div>
          </div>
        </div>

        <div
          v-if="copyrightText || techSupportText"
          class="absolute bottom-6 right-6 z-50 text-right text-[11px] leading-5 sm:bottom-8 sm:right-8 lg:bottom-10 lg:right-10"
          :class="activeTheme === 'dark' ? 'text-slate-500' : 'text-slate-400'"
        >
          <p v-if="copyrightText" class="font-medium tracking-wide">
            {{ copyrightText }}
          </p>
          <p v-if="techSupportText" class="mt-0.5 opacity-80">
            {{ techSupportText }}
          </p>
        </div>
      </section>

    </div>

    <GlobalInitOverlay :visible="isInitLoading" :theme="activeTheme" />
  </section>
</template>

<script setup lang="ts">
import {
  computed,
  inject,
  nextTick,
  onBeforeUnmount,
  onMounted,
  ref,
  type Ref,
} from "vue";
import HomeFootprintMap from "@/components/web/HomeFootprintMap.vue";
import {
  createDefaultHeroConfig,
  getHomeData,
  getProjects,
  getWorks,
  recordSiteVisit,
  resolveHeroConfig,
  type HeroConfigData,
  type HeroImageBlock,
  type HeroTagsBlock,
  type HeroTextBlock,
  type HomeData,
  type ProjectData,
  type WorkData,
} from "@/api/modules/site";
import { getCurrentYear } from "@/utils/date";
import { buildFootprintMapItems } from "@/utils/footprintGeo";

type ThemeMode = "light" | "dark";

const PROVINCE_AREA_MAP: Record<string, number> = {
  北京市: 16410,
  天津市: 11966,
  河北省: 188800,
  山西省: 156700,
  内蒙古自治区: 1183000,
  辽宁省: 148000,
  吉林省: 187400,
  黑龙江省: 473000,
  上海市: 6340,
  江苏省: 107200,
  浙江省: 105500,
  安徽省: 140100,
  福建省: 124000,
  江西省: 166900,
  山东省: 157900,
  河南省: 167000,
  湖北省: 185900,
  湖南省: 211800,
  广东省: 179800,
  广西壮族自治区: 236700,
  海南省: 35400,
  重庆市: 82400,
  四川省: 486000,
  贵州省: 176200,
  云南省: 394100,
  西藏自治区: 1228400,
  陕西省: 205600,
  甘肃省: 425800,
  青海省: 722300,
  宁夏回族自治区: 66400,
  新疆维吾尔自治区: 1664900,
  香港特别行政区: 1110,
  澳门特别行政区: 33,
  台湾省: 36000,
};

const CHINA_TOTAL_AREA = 9600000;
const HOME_SECTION_COUNT = 3;
const currentYear = getCurrentYear();

const homeData = ref<HomeData>({});
const heroConfigState = ref<HeroConfigData>(createDefaultHeroConfig());
const projects = ref<ProjectData[]>([]);
const works = ref<WorkData[]>([]);
const featuredProjects = computed(() => projects.value.slice(0, 2));
const featuredWorks = computed(() => works.value.slice(0, 2));
const isInitLoading = ref(true);
const isHeroReady = ref(false);
const isAboveFoldReady = ref(false);
const hasInitLoadedOnce = ref(false);
const setTopNavVisible = inject<((visible: boolean) => void) | undefined>(
  "dyx-set-top-nav-visible"
);
const currentTheme = inject<Ref<ThemeMode> | undefined>("dyx-theme");

const footprints = computed(() =>
  (homeData.value.footprints ?? []).filter((item) => item.published !== 0)
);
const activeTheme = computed<ThemeMode>(() => currentTheme?.value ?? "dark");
const footprintConfig = computed(() => homeData.value.systemConfig ?? {});
const footprintEyebrow = computed(
  () => footprintConfig.value.footprintEyebrow?.trim() || "Footprints"
);
const footprintTitle = computed(
  () => footprintConfig.value.footprintTitle?.trim() || "我去过的地方"
);
const footprintSubtitle = computed(
  () =>
    footprintConfig.value.footprintSubtitle?.trim() ||
    "从沿海到高原，慢慢点亮地图上的每一站。"
);
const footprintDescription = computed(
  () =>
    footprintConfig.value.footprintDescription?.trim() ||
    "这些城市和区域构成了最近几年的出发方向，也让首页第二屏变成一张正在持续扩展的旅行轨迹。"
);
const copyrightText = computed(
  () =>
    footprintConfig.value.copyrightText?.trim() ||
    `© ${currentYear} DYX BLOG. All rights reserved.`
);
const techSupportText = computed(
  () =>
    footprintConfig.value.techSupportText?.trim() ||
    "Powered by Vue 3 & Spring Boot 3"
);
const footprintSectionClass = computed(() =>
  activeTheme.value === "dark"
    ? "bg-[#0a0a0f]"
    : "bg-[radial-gradient(circle_at_20%_18%,rgba(59,130,246,0.12),transparent_28%),radial-gradient(circle_at_78%_22%,rgba(56,189,248,0.10),transparent_26%),linear-gradient(180deg,rgba(248,250,252,0.98),rgba(239,246,255,0.98))]"
);
const footprintBackdropClass = computed(() =>
  activeTheme.value === "dark"
    ? "bg-[radial-gradient(circle_at_18%_14%,rgba(0,200,255,0.16),transparent_24%),radial-gradient(circle_at_82%_18%,rgba(0,150,200,0.14),transparent_22%),radial-gradient(circle_at_50%_78%,rgba(0,120,160,0.10),transparent_32%),linear-gradient(180deg,rgba(10,10,15,1),rgba(11,14,20,0.98)_48%,rgba(7,9,14,1))]"
    : "bg-[radial-gradient(circle_at_18%_16%,rgba(59,130,246,0.14),transparent_30%),radial-gradient(circle_at_82%_18%,rgba(56,189,248,0.12),transparent_28%),linear-gradient(180deg,rgba(248,250,252,0.99),rgba(239,246,255,0.99)_48%,rgba(226,232,240,1))]"
);
const footprintTitleClass = computed(() =>
  activeTheme.value === "dark" ? "text-white" : "text-slate-900"
);
const footprintMetaClass = computed(() =>
  activeTheme.value === "dark" ? "text-cyan-200/70" : "text-slate-500"
);
const footprintSubtitleClass = computed(() =>
  activeTheme.value === "dark" ? "text-cyan-50/92" : "text-slate-600"
);
const footprintDescriptionClass = computed(() =>
  activeTheme.value === "dark" ? "text-slate-300/88" : "text-slate-500"
);
const footprintStatsClass = computed(() =>
  activeTheme.value === "dark"
    ? "border-cyan-300/15 bg-[linear-gradient(180deg,rgba(2,6,23,0.72),rgba(8,20,35,0.82))] shadow-[0_24px_72px_rgba(2,6,23,0.42)] md:border-white/10 md:bg-slate-950/24 md:shadow-[0_14px_42px_rgba(2,6,23,0.22)]"
    : "border-sky-200/80 bg-[linear-gradient(180deg,rgba(255,255,255,0.9),rgba(239,246,255,0.94))] shadow-[0_18px_56px_rgba(148,163,184,0.2)] md:border-slate-200/70 md:bg-white/72 md:shadow-[0_16px_40px_rgba(148,163,184,0.12)]"
);
const footprintStatsItemClass = computed(() =>
  activeTheme.value === "dark"
    ? "bg-[linear-gradient(180deg,rgba(255,255,255,0.06),rgba(34,211,238,0.08))] ring-1 ring-cyan-300/10 md:bg-white/0 md:ring-white/0"
    : "bg-[linear-gradient(180deg,rgba(255,255,255,0.95),rgba(224,242,254,0.85))] ring-1 ring-white/80 md:bg-white/0 md:ring-transparent"
);
const footprintEmptyClass = computed(() =>
  activeTheme.value === "dark" ? "text-slate-400" : "text-slate-500"
);
const activitySectionClass = computed(() =>
  activeTheme.value === "dark" ? "bg-[#08101b]" : "bg-slate-100"
);
const activityBackdropClass = computed(() =>
  activeTheme.value === "dark"
    ? "bg-[radial-gradient(circle_at_16%_20%,rgba(56,189,248,0.16),transparent_32%),radial-gradient(circle_at_82%_16%,rgba(96,165,250,0.14),transparent_28%),linear-gradient(180deg,rgba(6,10,18,0.98),rgba(10,17,30,0.98)_48%,rgba(5,8,15,1))]"
    : "bg-[radial-gradient(circle_at_16%_20%,rgba(59,130,246,0.12),transparent_30%),radial-gradient(circle_at_82%_16%,rgba(125,211,252,0.16),transparent_28%),linear-gradient(180deg,rgba(248,250,252,0.99),rgba(239,246,255,1)_48%,rgba(226,232,240,1))]"
);
const activityTitleClass = computed(() =>
  activeTheme.value === "dark" ? "text-white" : "text-slate-900"
);
const activityTextClass = computed(() =>
  activeTheme.value === "dark" ? "text-slate-300" : "text-slate-600"
);
const activityMetaClass = computed(() =>
  activeTheme.value === "dark" ? "text-slate-400" : "text-slate-500"
);
const footprintMapData = computed(() =>
  buildFootprintMapItems(footprints.value)
);
const visitedProvinceNames = computed(() => {
  const uniqueProvinceNames = new Set(
    footprintMapData.value
      .map((item) => item.provinceName)
      .filter((item): item is string => !!item)
  );
  return Array.from(uniqueProvinceNames);
});
const footprintStats = computed(() => {
  const provinceCount = visitedProvinceNames.value.length;
  const cityCount = footprintMapData.value.length;
  const unlockedArea = visitedProvinceNames.value.reduce(
    (total, item) => total + (PROVINCE_AREA_MAP[item] ?? 0),
    0
  );
  const unlockedRate = Math.min(
    100,
    Number(((unlockedArea / CHINA_TOTAL_AREA) * 100).toFixed(1))
  );
  return [
    { label: "去过省份", value: `${provinceCount} 个` },
    { label: "去过城市", value: `${cityCount} 个` },
    { label: "点亮国土", value: `${unlockedRate}%` },
    { label: "足迹总数", value: `${footprints.value.length} 条` },
  ];
});
const heroConfig = computed(() => heroConfigState.value);
const heroContentBlocks = computed(() =>
  heroConfig.value.blocks.filter(
    (block): block is HeroTextBlock | HeroTagsBlock => block.type !== "image"
  )
);
const heroImageBlock = computed(() =>
  heroConfig.value.blocks.find(
    (block): block is HeroImageBlock => block.type === "image"
  )
);
const hasHeroImage = computed(() => !!heroImageBlock.value?.imageUrl);
const heroGridClass = computed(() =>
  hasHeroImage.value
    ? "lg:grid-cols-[minmax(0,0.92fr)_minmax(0,1.08fr)] xl:grid-cols-[minmax(0,0.9fr)_minmax(0,1.1fr)]"
    : "lg:grid-cols-1"
);
const heroInnerClass = computed(() =>
  hasHeroBackground.value
    ? `${heroGridClass.value} py-0`
    : `${heroGridClass.value} py-5 sm:py-6 lg:py-8 xl:py-10`
);
const heroViewportClass = computed(() => {
  if (hasHeroBackground.value) {
    return "home-hero-viewport home-hero-viewport--with-bg h-full pt-[64px] pb-0 sm:pb-0 sm:pt-[92px] lg:pt-[140px]";
  }
  return "home-hero-viewport h-full pt-[76px] pb-2 sm:pb-3 sm:pt-[96px] lg:pt-[88px]";
});
const heroCopyClass = computed(() =>
  hasHeroBackground.value
    ? "justify-start gap-4 pt-6 sm:justify-center sm:gap-5 sm:pt-0 lg:justify-start lg:gap-6 lg:pt-0 xl:pt-0"
    : "justify-start gap-5 pt-6 sm:justify-center sm:pt-0 lg:justify-start lg:gap-6 lg:pt-0 xl:pt-0"
);
const heroContentClass = computed(() =>
  hasHeroBackground.value
    ? "justify-start gap-4 sm:justify-center sm:gap-5 lg:justify-start lg:gap-6 lg:pr-12 xl:pr-16"
    : "justify-start gap-5 sm:justify-center lg:justify-start lg:gap-6 lg:pr-12 xl:pr-16"
);
const heroBackgroundStyle = computed(() => {
  const backgroundImageUrl = heroImageBlock.value?.backgroundImageUrl?.trim();
  if (!backgroundImageUrl) {
    return undefined;
  }
  return {
    backgroundImage: `url(${backgroundImageUrl})`,
  };
});
const hasHeroBackground = computed(
  () => !!heroImageBlock.value?.backgroundImageUrl?.trim()
);
const shouldActivateMap = computed(
  () => hasInitLoadedOnce.value && currentSectionIndex.value >= 1
);

const scrollContainer = ref<HTMLElement | null>(null);
const currentSectionIndex = ref(0);
let wheelHandler: ((event: WheelEvent) => void) | null = null;
let scrollSyncHandler: (() => void) | null = null;
let scrollSnapTimer: number | null = null;
let scrollFrameId: number | null = null;
let wheelNavigationLocked = false;

function updateTopNavVisibility(): void {
  setTopNavVisible?.(currentSectionIndex.value === 0);
}

function unlockWheelNavigation(): void {
  wheelNavigationLocked = false;
}

function scrollToSection(
  index: number,
  behavior: ScrollBehavior = "smooth"
): void {
  const el = scrollContainer.value;
  if (!el) {
    return;
  }
  const maxIndex = HOME_SECTION_COUNT - 1;
  const nextIndex = Math.max(0, Math.min(index, maxIndex));
  if (nextIndex !== currentSectionIndex.value) {
    currentSectionIndex.value = nextIndex;
    updateTopNavVisibility();
  }
  el.scrollTo({
    top: nextIndex * el.clientHeight,
    behavior,
  });
}

function syncCurrentSectionFromScroll(): void {
  const el = scrollContainer.value;
  if (!el || el.clientHeight <= 0) {
    return;
  }
  const nextIndex = Math.max(
    0,
    Math.min(HOME_SECTION_COUNT - 1, Math.round(el.scrollTop / el.clientHeight))
  );
  if (nextIndex !== currentSectionIndex.value) {
    currentSectionIndex.value = nextIndex;
    updateTopNavVisibility();
  }
}

function scheduleScrollSync(): void {
  if (scrollFrameId !== null) {
    return;
  }
  scrollFrameId = window.requestAnimationFrame(() => {
    scrollFrameId = null;
    syncCurrentSectionFromScroll();
  });
}

function clearScrollSnapTimer(): void {
  if (scrollSnapTimer !== null) {
    window.clearTimeout(scrollSnapTimer);
    scrollSnapTimer = null;
  }
}

function clearScrollFrame(): void {
  if (scrollFrameId !== null) {
    window.cancelAnimationFrame(scrollFrameId);
    scrollFrameId = null;
  }
}

function isTagsBlock(
  block: HeroTextBlock | HeroTagsBlock
): block is HeroTagsBlock {
  return block.type === "tags";
}

async function finishInitLoading(): Promise<void> {
  await nextTick();
  window.setTimeout(() => {
    isAboveFoldReady.value = true;
    isInitLoading.value = false;
    hasInitLoadedOnce.value = true;
  }, 180);
}

async function loadHomeData(): Promise<void> {
  const response = await Promise.allSettled([
    getHomeData(),
    getProjects(),
    getWorks(),
  ]);
  const [homeResponse, projectResponse, workResponse] = response;
  homeData.value =
    homeResponse.status === "fulfilled" ? homeResponse.value.data ?? {} : {};
  heroConfigState.value = resolveHeroConfig(homeData.value.profile);
  projects.value =
    projectResponse.status === "fulfilled" ? projectResponse.value.data ?? [] : [];
  works.value =
    workResponse.status === "fulfilled" ? workResponse.value.data ?? [] : [];
  isHeroReady.value = true;
  await finishInitLoading();
}

onMounted(() => {
  currentSectionIndex.value = 0;
  updateTopNavVisibility();
  void recordSiteVisit("home");
  void loadHomeData();

  const el = scrollContainer.value;
  if (!el) return;

  wheelHandler = (event: WheelEvent) => {
    // 某些设备滚轮 deltaY 很小，降低阈值保证能触发翻页
    const delta = event.deltaY;
    if (Math.abs(delta) < 1) {
      return;
    }
    event.preventDefault();
    if (wheelNavigationLocked) {
      return;
    }
    const rawNextIndex = currentSectionIndex.value + (delta > 0 ? 1 : -1);
    const maxIndex = HOME_SECTION_COUNT - 1;
    const clampedIndex = Math.max(0, Math.min(rawNextIndex, maxIndex));
    // 已在边界（比如已经在最后一屏），不触发锁定，避免卡死
    if (clampedIndex === currentSectionIndex.value) {
      return;
    }
    wheelNavigationLocked = true;
    scrollToSection(clampedIndex);
  };
  scrollSyncHandler = () => {
    scheduleScrollSync();
    clearScrollSnapTimer();
    scrollSnapTimer = window.setTimeout(() => {
      scrollToSection(currentSectionIndex.value, "smooth");
      unlockWheelNavigation();
    }, 260);
  };

  el.addEventListener("wheel", wheelHandler, { passive: false });
  el.addEventListener("scroll", scrollSyncHandler, { passive: true });
});

onBeforeUnmount(() => {
  const el = scrollContainer.value;
  if (el && wheelHandler) {
    el.removeEventListener("wheel", wheelHandler);
  }
  if (el && scrollSyncHandler) {
    el.removeEventListener("scroll", scrollSyncHandler);
  }
  clearScrollSnapTimer();
  clearScrollFrame();
  unlockWheelNavigation();
  setTopNavVisible?.(true);
});
</script>
