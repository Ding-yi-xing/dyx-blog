<template>
  <section class="relative h-full w-full px-0">
    <div
      ref="scrollContainer"
      class="relative h-full overflow-y-auto snap-y snap-mandatory scroll-smooth scrollbar-none"
    >
      <section class="relative flex h-full w-full snap-start items-stretch overflow-hidden">
        <div
          v-if="heroBackgroundStyle"
          class="home-hero-bg home-hero-bg--image pointer-events-none absolute inset-0"
          :style="heroBackgroundStyle"
        ></div>
        <div
          v-else
          class="home-hero-bg home-hero-bg--fallback pointer-events-none absolute inset-0"
        ></div>

        <div class="relative z-10 grid h-full w-full min-w-0 px-3 pb-2 pt-[90px] sm:px-4 sm:pb-3 sm:pt-[104px] lg:px-5 lg:pt-[112px]">
          <div class="grid h-full w-full min-w-0 overflow-hidden p-3 sm:p-5 lg:gap-6 lg:p-6 xl:p-8" :class="heroGridClass">
            <div class="home-hero-copy flex min-w-0 flex-col justify-between gap-8" :class="!hasHeroImage ? 'lg:col-span-2' : ''">
              <div class="flex min-w-0 flex-1 flex-col justify-center gap-6 lg:gap-7 lg:pr-6">
                <template v-for="block in heroContentBlocks" :key="block.id">
                  <p
                    v-if="block.type === 'eyebrow'"
                    class="home-meta text-xs font-semibold uppercase tracking-[0.42em] lg:text-sm"
                  >
                    {{ block.text || 'HELLO THERE!' }}
                  </p>
                  <h1
                    v-else-if="block.type === 'title'"
                    class="home-section-title max-w-4xl text-4xl font-semibold leading-[1.02] tracking-[-0.04em] sm:text-5xl lg:text-[4.2rem] xl:text-[4.9rem]"
                  >
                    {{ block.text || '写代码的人，也写点文字。' }}
                  </h1>
                  <p
                    v-else-if="block.type === 'subtitle'"
                    class="home-section-text max-w-2xl text-[15px] leading-8 sm:text-lg lg:text-[1.15rem] lg:leading-9"
                  >
                    {{
                      block.text ||
                      '这里有后端开发、安全研究、折腾小工具的记录，也有一些不那么严肃的碎碎念。这个博客更像是一个公开的笔记本，欢迎随便翻翻。'
                    }}
                  </p>
                  <template v-else-if="isTagsBlock(block)">
                    <div class="flex flex-wrap gap-3 text-xs font-medium sm:text-sm">
                      <span
                        v-for="item in block.items"
                        :key="item"
                        class="inline-flex items-center rounded-full bg-[rgb(var(--dyx-accent-soft-rgb)/0.92)] px-4 py-2 text-[rgb(var(--dyx-accent-strong-rgb))]"
                      >
                        {{ item }}
                      </span>
                    </div>
                  </template>
                </template>
              </div>

              <div class="flex flex-wrap items-center gap-3">
                <RouterLink
                  to="/blog"
                  class="inline-flex items-center rounded-full bg-[rgb(var(--dyx-text-main-rgb))] px-5 py-3 text-sm font-medium text-[rgb(var(--dyx-text-inverse-rgb))] transition hover:opacity-90"
                >
                  阅读博客
                </RouterLink>
                <RouterLink
                  to="/about"
                  class="dyx-ghost-pill inline-flex items-center"
                >
                  查看关于我
                </RouterLink>
              </div>
            </div>

            <div v-if="hasHeroImage" class="home-hero-photo-stage relative flex min-h-0 w-full items-center justify-center overflow-hidden px-0 py-0 sm:px-0 lg:min-h-[70vh] lg:justify-end lg:py-0">
              <div class="relative flex h-full w-full items-center justify-center overflow-hidden lg:justify-end">
                <img
                  :src="heroImageBlock?.imageUrl"
                  :alt="heroImageBlock?.alt || 'avatar'"
                  class="home-hero-photo h-full max-h-[72vh] w-full object-contain object-center lg:max-h-[78vh] lg:object-right"
                />
              </div>
            </div>
          </div>
        </div>
      </section>

      <section class="min-h-full w-full snap-start px-3 py-3 sm:px-4 sm:py-4 lg:px-5">
        <div class="dyx-page-card flex min-h-full flex-col justify-center rounded-[36px] px-5 py-6 sm:px-7 sm:py-7 lg:px-9 lg:py-8">
          <div class="flex items-baseline justify-between gap-4 border-b border-[rgb(var(--dyx-border-subtle-rgb)/0.6)] pb-5">
            <div>
              <p class="home-meta text-[11px] font-medium uppercase tracking-[0.3em]">Currently</p>
              <h2 class="home-section-title mt-2 text-xl font-semibold tracking-tight sm:text-2xl">这段时间我在做什么</h2>
            </div>
            <RouterLink to="/about" class="home-section-link text-[12px] font-medium">
              继续了解我 →
            </RouterLink>
          </div>

          <div class="mt-6 grid gap-5 lg:grid-cols-[minmax(0,1.12fr)_minmax(0,0.88fr)]">
            <article class="rounded-[30px] border border-[rgb(var(--dyx-border-subtle-rgb)/0.58)] bg-[rgb(var(--dyx-bg-surface-rgb)/0.46)] px-6 py-6">
              <p class="home-meta text-[11px] font-medium uppercase tracking-[0.24em]">Notebook</p>
              <div class="mt-4 space-y-4">
                <p
                  v-for="(paragraph, index) in storyParagraphs"
                  :key="`story-${index}`"
                  class="home-section-text text-[14px] leading-8 sm:text-[15px]"
                >
                  {{ paragraph }}
                </p>
              </div>
              <div class="mt-6 flex flex-wrap gap-3">
                <RouterLink to="/about" class="dyx-ghost-pill inline-flex items-center">
                  关于我
                </RouterLink>
                <RouterLink to="/resume" class="dyx-ghost-pill inline-flex items-center">
                  查看简历
                </RouterLink>
              </div>
            </article>

            <div class="grid gap-4">
              <article class="rounded-[28px] border border-[rgb(var(--dyx-border-subtle-rgb)/0.58)] bg-[rgb(var(--dyx-bg-surface-rgb)/0.46)] px-5 py-5">
                <p class="home-meta text-[11px] font-medium uppercase tracking-[0.24em]">Current focus</p>
                <ul v-if="currentFocusItems.length" class="mt-4 space-y-3 text-[14px] leading-7 home-section-text">
                  <li v-for="(item, index) in currentFocusItems" :key="`focus-${index}`" class="flex gap-3">
                    <span class="home-section-link">•</span>
                    <span>{{ item }}</span>
                  </li>
                </ul>
                <p v-else class="home-section-text mt-4 text-[14px] leading-7">
                  最近仍在持续整理写作、项目经验和一些阶段性的成果记录。
                </p>
              </article>

              <article class="rounded-[28px] border border-[rgb(var(--dyx-border-subtle-rgb)/0.58)] bg-[rgb(var(--dyx-bg-surface-rgb)/0.46)] px-5 py-5">
                <p class="home-meta text-[11px] font-medium uppercase tracking-[0.24em]">Where to next</p>
                <div class="mt-4 space-y-4 text-[13px] leading-7">
                  <RouterLink to="/blog" class="group block">
                    <p class="home-section-title text-base font-semibold transition group-hover:opacity-80">
                      {{ latestPost?.title || '去博客看看最近写了什么' }}
                    </p>
                    <p class="home-section-text mt-1">
                      {{ latestPost?.summary || '文章会放更完整的技术记录、阶段总结和长一点的思考。' }}
                    </p>
                  </RouterLink>
                  <RouterLink to="/moments" class="group block">
                    <p class="home-section-title text-base font-semibold transition group-hover:opacity-80">
                      {{ latestMoment?.title || '去动态看看最近在忙什么' }}
                    </p>
                    <p class="home-section-text mt-1">
                      {{ latestMoment?.content || '动态页会保留更轻量、更及时的一些更新节奏。' }}
                    </p>
                  </RouterLink>
                </div>
              </article>
            </div>
          </div>
        </div>
      </section>

      <section class="min-h-full w-full snap-start px-3 py-3 sm:px-4 sm:py-4 lg:px-5">
        <div class="dyx-page-card flex min-h-full flex-col justify-center rounded-[36px] px-5 py-6 sm:px-7 sm:py-7 lg:px-9 lg:py-8">
          <div class="flex items-baseline justify-between gap-4 border-b border-[rgb(var(--dyx-border-subtle-rgb)/0.6)] pb-5">
            <div>
              <p class="home-meta text-[11px] font-medium uppercase tracking-[0.3em]">Recent activity</p>
              <h2 class="home-section-title mt-2 text-xl font-semibold tracking-tight sm:text-2xl">最近更新节奏</h2>
            </div>
            <div class="flex flex-wrap items-center gap-3 text-[12px] font-medium">
              <RouterLink to="/blog" class="home-section-link">博客 →</RouterLink>
              <RouterLink to="/moments" class="home-section-link">动态 →</RouterLink>
            </div>
          </div>

          <div class="mt-6 grid gap-6 lg:grid-cols-[minmax(0,0.82fr)_minmax(0,1.18fr)]">
            <article class="rounded-[30px] border border-[rgb(var(--dyx-border-subtle-rgb)/0.58)] bg-[rgb(var(--dyx-bg-surface-rgb)/0.46)] px-6 py-6">
              <p class="home-meta text-[11px] font-medium uppercase tracking-[0.24em]">Why this rhythm</p>
              <h3 class="home-section-title mt-4 text-2xl font-semibold">把写作和动态放在同一条时间线上</h3>
              <p class="home-section-text mt-4 text-[14px] leading-8 sm:text-[15px]">
                这里不会把博客和动态拆成两块彼此孤立的内容，而是更直接地展示最近的更新节奏：什么时候写完了一篇文章，什么时候推进了一个阶段，什么时候留下了新的记录。
              </p>
              <p class="home-section-text mt-4 text-[14px] leading-8 sm:text-[15px]">
                {{ timelineLeadText }}
              </p>
            </article>

            <div class="relative pl-6 sm:pl-8">
              <div class="absolute left-[7px] top-1 bottom-1 w-px bg-[rgb(var(--dyx-border-subtle-rgb)/0.68)] sm:left-[11px]"></div>
              <div v-if="recentActivityItems.length" class="space-y-5">
                <RouterLink
                  v-for="item in recentActivityItems"
                  :key="item.id"
                  :to="item.to"
                  class="group relative block"
                >
                  <span class="absolute left-[-2px] top-7 h-4 w-4 rounded-full border-4 border-[rgb(var(--dyx-bg-page-rgb))] bg-[rgb(var(--dyx-text-main-rgb))] sm:left-[2px]"></span>
                  <article class="ml-6 rounded-[28px] border border-[rgb(var(--dyx-border-subtle-rgb)/0.58)] bg-[rgb(var(--dyx-bg-surface-rgb)/0.46)] px-5 py-5 transition group-hover:-translate-y-0.5 sm:ml-10">
                    <div class="flex flex-wrap items-center gap-3">
                      <span class="dyx-meta-pill">{{ item.label }}</span>
                      <span class="home-meta text-[12px]">{{ formatDateYmd(item.date) || '近期' }}</span>
                    </div>
                    <h3 class="home-section-title mt-4 text-lg font-semibold transition group-hover:opacity-80">{{ item.title }}</h3>
                    <p class="home-section-text mt-3 text-[13px] leading-7">{{ item.summary }}</p>
                    <div class="mt-4 text-[12px] home-section-link">继续阅读 →</div>
                  </article>
                </RouterLink>
              </div>
              <article v-else class="ml-6 rounded-[28px] border border-dashed border-[rgb(var(--dyx-border-subtle-rgb)/0.72)] px-5 py-5 text-sm home-meta sm:ml-10">
                最近还没有公开更新，稍后再来看看。
              </article>
            </div>
          </div>
        </div>
      </section>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import {
  getHomeData,
  parseHeroConfig,
  recordSiteVisit,
  type HeroImageBlock,
  type HeroTagsBlock,
  type HeroTextBlock,
  type HomeData,
  type MomentData,
  type PostData
} from '@/api/modules/site';
import { formatDateYmd } from '@/utils/date';

interface RecentActivityItem {
  id: string;
  label: '博客' | '动态';
  title: string;
  summary: string;
  to: string;
  date?: string;
  sortValue: number;
}

const homeData = ref<HomeData>({});

const profile = computed(() => homeData.value.profile ?? {});
const posts = computed(() => homeData.value.latestPosts ?? []);
const moments = computed(() => homeData.value.latestMoments ?? []);
const latestPost = computed(() => posts.value[0] ?? null);
const latestMoment = computed(() => moments.value[0] ?? null);
const featuredProject = computed(() => homeData.value.featuredProjects?.[0] ?? null);
const latestHonor = computed(() => homeData.value.latestHonors?.[0] ?? null);
const heroConfig = computed(() => parseHeroConfig(profile.value));
const heroContentBlocks = computed(() => heroConfig.value.blocks.filter((block): block is HeroTextBlock | HeroTagsBlock => block.type !== 'image'));
const heroImageBlock = computed(() => heroConfig.value.blocks.find((block): block is HeroImageBlock => block.type === 'image'));
const hasHeroImage = computed(() => !!heroImageBlock.value?.imageUrl);
const heroGridClass = computed(() =>
  hasHeroImage.value
    ? 'lg:grid-cols-[minmax(0,0.92fr)_minmax(0,1.08fr)] xl:grid-cols-[minmax(0,0.86fr)_minmax(0,1.14fr)]'
    : 'lg:grid-cols-1'
);
const heroBackgroundStyle = computed(() => {
  const backgroundImageUrl = heroImageBlock.value?.backgroundImageUrl?.trim();
  if (!backgroundImageUrl) {
    return undefined;
  }
  return {
    backgroundImage: `url(${backgroundImageUrl})`
  };
});
const storyParagraphs = computed(() => {
  const paragraphs = (profile.value.aboutContent ?? '')
    .split(/\n+/)
    .map((item) => item.trim())
    .filter(Boolean);
  if (paragraphs.length) {
    return paragraphs.slice(0, 2);
  }
  return [
    '这里记录我最近在写什么、做什么，以及一些还在成形中的想法。比起把内容排成一张信息面板，我更想把它整理成一个可持续更新的公开笔记本。',
    '技术实践、阶段性总结、项目推进和偶尔的碎片化动态会交织出现，你可以顺着时间线继续往下看，也可以直接跳去博客、动态或关于页。'
  ];
});
const currentFocusItems = computed(() => {
  const items: string[] = [];
  if (latestMoment.value?.title) {
    items.push(`最近在推进「${latestMoment.value.title}」这条线索。`);
  }
  if (latestPost.value?.title) {
    items.push(`刚写完或正在整理和「${latestPost.value.title}」相关的内容。`);
  }
  if (featuredProject.value?.name) {
    items.push(`项目侧的代表性内容目前是「${featuredProject.value.name}」。`);
  }
  if (latestHonor.value?.title) {
    items.push(`阶段节点则落在「${latestHonor.value.title}」这一项。`);
  }
  return items.slice(0, 3);
});
const timelineLeadText = computed(() => {
  if (recentActivityItems.value.length >= 2) {
    return `当前首页会把最近 ${recentActivityItems.value.length} 条公开更新压缩成一条轻量时间线，方便快速感知这段时间的输出节奏。`;
  }
  return '最近的更新还不算多，但后续会继续沿着这条时间线把新的文章和动态补上。';
});
const recentActivityItems = computed<RecentActivityItem[]>(() => {
  const postItems = posts.value.map((item) => createPostActivity(item));
  const momentItems = moments.value.map((item) => createMomentActivity(item));
  return [...momentItems, ...postItems]
    .sort((left, right) => right.sortValue - left.sortValue)
    .slice(0, 6);
});

const scrollContainer = ref<HTMLElement | null>(null);
let wheelHandler: ((event: WheelEvent) => void) | null = null;

function isTagsBlock(block: HeroTextBlock | HeroTagsBlock): block is HeroTagsBlock {
  return block.type === 'tags';
}

function summarizeText(value: string | undefined, fallback: string): string {
  const normalized = (value ?? '').replace(/\s+/g, ' ').trim();
  if (!normalized) {
    return fallback;
  }
  return normalized.length > 110 ? `${normalized.slice(0, 110).trim()}…` : normalized;
}

function parseSortValue(value?: string): number {
  if (!value) {
    return 0;
  }
  const normalized = value.includes(' ') ? value.replace(' ', 'T') : value;
  const timestamp = Date.parse(normalized);
  if (!Number.isNaN(timestamp)) {
    return timestamp;
  }
  const matched = value.match(/^(\d{4})-(\d{2})-(\d{2})/);
  if (!matched) {
    return 0;
  }
  return new Date(Number(matched[1]), Number(matched[2]) - 1, Number(matched[3])).getTime();
}

function createPostActivity(item: PostData): RecentActivityItem {
  return {
    id: `post-${item.id}`,
    label: '博客',
    title: item.title,
    summary: summarizeText(item.summary, '最近更新了一篇新的博客文章。'),
    to: `/blog/${item.id}`,
    date: item.updatedAt,
    sortValue: parseSortValue(item.updatedAt)
  };
}

function createMomentActivity(item: MomentData): RecentActivityItem {
  return {
    id: `moment-${item.id}`,
    label: '动态',
    title: item.title,
    summary: summarizeText(item.content, '记录了一条新的近期动态。'),
    to: `/moments/${item.id}`,
    date: item.happenedAt,
    sortValue: parseSortValue(item.happenedAt)
  };
}

async function loadHomeData(): Promise<void> {
  const response = await getHomeData();
  homeData.value = response.data ?? {};
}

onMounted(() => {
  void recordSiteVisit('home');
  void loadHomeData();

  const el = scrollContainer.value;
  if (!el) return;

  wheelHandler = (event: WheelEvent) => {
    const { deltaY } = event;
    const nearTop = el.scrollTop <= 0;
    const nearBottom = el.scrollTop + el.clientHeight >= el.scrollHeight - 10;

    if (deltaY > 0 && nearBottom) {
      event.preventDefault();
      el.scrollTo({ top: 0, behavior: 'smooth' });
      return;
    }

    if (deltaY < 0 && nearTop) {
      event.preventDefault();
      el.scrollTo({ top: el.scrollHeight, behavior: 'smooth' });
      return;
    }
  };

  el.addEventListener('wheel', wheelHandler, { passive: false });
});

onBeforeUnmount(() => {
  const el = scrollContainer.value;
  if (el && wheelHandler) {
    el.removeEventListener('wheel', wheelHandler);
  }
});
</script>
