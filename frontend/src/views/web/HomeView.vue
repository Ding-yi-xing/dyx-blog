<template>
  <section class="relative h-full w-full px-0">
    <div
      ref="scrollContainer"
      class="relative h-full overflow-y-auto snap-y snap-mandatory scroll-smooth scrollbar-none"
    >
      <section class="flex h-full w-full snap-start items-stretch px-3 py-2 sm:px-4 sm:py-3 lg:px-5">
        <div class="grid h-full w-full min-w-0 overflow-hidden p-3 sm:p-5 lg:grid-cols-[minmax(0,0.92fr)_minmax(0,1.08fr)] lg:gap-6 lg:p-6 xl:grid-cols-[minmax(0,0.86fr)_minmax(0,1.14fr)] xl:p-8">
          <div class="flex min-w-0 flex-col justify-between gap-8">
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

          <div class="relative flex min-h-0 w-full items-center justify-center overflow-hidden px-0 py-0 sm:px-0 lg:min-h-[70vh] lg:justify-end lg:py-0">
            <div class="relative flex h-full w-full items-center justify-center overflow-hidden lg:justify-end">
              <img
                v-if="heroImageBlock?.imageUrl"
                :src="heroImageBlock.imageUrl"
                :alt="heroImageBlock.alt || 'avatar'"
                class="h-full max-h-[72vh] w-full object-contain object-center lg:max-h-[78vh] lg:object-right"
              />
              <span v-else class="px-4 text-center text-sm home-meta">这里以后可以放一张你的照片</span>
            </div>
          </div>
        </div>
      </section>

      <section class="min-h-full w-full snap-start px-3 py-3 sm:px-4 sm:py-4 lg:px-5">
        <div class="dyx-page-card flex min-h-full flex-col justify-center rounded-[36px] px-5 py-6 sm:px-7 sm:py-7 lg:px-9 lg:py-8">
          <div class="flex items-baseline justify-between gap-4 border-b border-[rgb(var(--dyx-border-subtle-rgb)/0.6)] pb-5">
            <div>
              <p class="home-meta text-[11px] font-medium uppercase tracking-[0.3em]">Overview</p>
              <h2 class="home-section-title mt-2 text-xl font-semibold tracking-tight sm:text-2xl">内容概览</h2>
            </div>
            <RouterLink to="/resume" class="home-section-link text-[12px] font-medium">
              查看完整履历 →
            </RouterLink>
          </div>

          <div class="mt-6 grid gap-4 md:grid-cols-2 xl:grid-cols-4">
            <RouterLink
              v-for="card in overviewCards"
              :key="card.title"
              :to="card.to"
              class="group rounded-[28px] border border-[rgb(var(--dyx-border-subtle-rgb)/0.58)] bg-[rgb(var(--dyx-bg-surface-rgb)/0.46)] px-5 py-5 transition hover:-translate-y-0.5"
            >
              <p class="home-meta text-[11px] font-medium uppercase tracking-[0.24em]">{{ card.eyebrow }}</p>
              <div class="mt-4 flex items-end justify-between gap-4">
                <div>
                  <p class="home-section-title text-2xl font-semibold">{{ card.count }}</p>
                  <h3 class="mt-1 text-base font-semibold home-section-title">{{ card.title }}</h3>
                </div>
                <span class="text-[12px] home-section-link">进入 →</span>
              </div>
              <p class="mt-4 text-[13px] leading-7 home-section-text">{{ card.description }}</p>
            </RouterLink>
          </div>
        </div>
      </section>

      <section class="min-h-full w-full snap-start px-3 py-3 sm:px-4 sm:py-4 lg:px-5">
        <div class="dyx-page-card flex min-h-full flex-col justify-center rounded-[36px] px-5 py-6 sm:px-7 sm:py-7 lg:px-9 lg:py-8">
          <div class="flex items-baseline justify-between gap-4 border-b border-[rgb(var(--dyx-border-subtle-rgb)/0.6)] pb-5">
            <div>
              <p class="home-meta text-[11px] font-medium uppercase tracking-[0.3em]">Spotlight</p>
              <h2 class="home-section-title mt-2 text-xl font-semibold tracking-tight sm:text-2xl">最近在做什么</h2>
            </div>
            <RouterLink to="/moments" class="home-section-link text-[12px] font-medium">
              查看全部动态 →
            </RouterLink>
          </div>

          <div class="mt-6 grid gap-5 lg:grid-cols-[minmax(0,1.2fr)_minmax(0,0.8fr)]">
            <RouterLink
              v-if="moments[0]"
              :to="`/moments/${moments[0].id}`"
              class="group rounded-[30px] border border-[rgb(var(--dyx-border-subtle-rgb)/0.58)] bg-[rgb(var(--dyx-bg-surface-rgb)/0.46)] px-6 py-6 transition hover:-translate-y-0.5"
            >
              <div class="flex items-center justify-between gap-3">
                <p class="home-meta text-[11px] font-medium uppercase tracking-[0.24em]">Latest moment</p>
                <span class="home-meta text-[12px]">{{ formatDateYmd(moments[0].happenedAt) || '未设置时间' }}</span>
              </div>
              <h3 class="home-section-title mt-4 text-2xl font-semibold">{{ moments[0].title || '一条动态' }}</h3>
              <p class="home-section-text mt-4 max-w-3xl text-[14px] leading-8 line-clamp-4">
                {{ moments[0].content || '点击查看最近的动态详情。' }}
              </p>
              <div class="mt-6 flex items-center gap-3 text-[12px] home-section-link">
                <span>进入详情</span>
                <span>→</span>
              </div>
            </RouterLink>
            <div v-else class="rounded-[30px] border border-dashed border-[rgb(var(--dyx-border-subtle-rgb)/0.72)] px-6 py-6 text-sm home-meta">
              暂无动态，最近可能有点忙。
            </div>

            <div class="grid gap-4">
              <RouterLink
                v-if="posts[0]"
                :to="`/blog/${posts[0].id}`"
                class="group rounded-[28px] border border-[rgb(var(--dyx-border-subtle-rgb)/0.58)] bg-[rgb(var(--dyx-bg-surface-rgb)/0.46)] px-5 py-5 transition hover:-translate-y-0.5"
              >
                <p class="home-meta text-[11px] font-medium uppercase tracking-[0.24em]">Latest post</p>
                <h3 class="home-section-title mt-3 text-lg font-semibold transition-opacity group-hover:opacity-80">{{ posts[0].title }}</h3>
                <p class="home-section-text mt-3 text-[13px] leading-7 line-clamp-3">{{ posts[0].summary || '点击进入最新文章。' }}</p>
                <p v-if="posts[0].updatedAt" class="home-meta mt-4 text-[12px]">{{ formatDateYmd(posts[0].updatedAt) }}</p>
              </RouterLink>
              <div v-else class="rounded-[28px] border border-dashed border-[rgb(var(--dyx-border-subtle-rgb)/0.72)] px-5 py-5 text-sm home-meta">
                暂无文章，等我写点东西吧。
              </div>

              <RouterLink
                to="/about"
                class="rounded-[28px] border border-[rgb(var(--dyx-border-subtle-rgb)/0.58)] bg-[rgb(var(--dyx-bg-surface-rgb)/0.46)] px-5 py-5 transition hover:-translate-y-0.5"
              >
                <p class="home-meta text-[11px] font-medium uppercase tracking-[0.24em]">Profile</p>
                <h3 class="home-section-title mt-3 text-lg font-semibold">关于我 / 简历 / 荣誉</h3>
                <p class="home-section-text mt-3 text-[13px] leading-7">
                  教育经历、项目经历、作品与联系方式都在这里，适合快速了解当前阶段的积累与方向。
                </p>
              </RouterLink>
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
  type HomeData
} from '@/api/modules/site';
import { formatDateYmd } from '@/utils/date';

const homeData = ref<HomeData>({});

const profile = computed(() => homeData.value.profile ?? {});
const posts = computed(() => homeData.value.latestPosts ?? []);
const moments = computed(() => homeData.value.latestMoments ?? []);
const heroConfig = computed(() => parseHeroConfig(profile.value));
const heroContentBlocks = computed(() => heroConfig.value.blocks.filter((block): block is HeroTextBlock | HeroTagsBlock => block.type !== 'image'));
const heroImageBlock = computed(() => heroConfig.value.blocks.find((block): block is HeroImageBlock => block.type === 'image'));
const overviewCards = computed(() => [
  {
    eyebrow: 'Blog',
    title: '博客文章',
    count: `${posts.value.length}`,
    description: posts.value[0]?.title || '记录技术实践、产品思考与阶段性总结。',
    to: '/blog'
  },
  {
    eyebrow: 'Moments',
    title: '最新动态',
    count: `${moments.value.length}`,
    description: moments.value[0]?.title || '追踪近期在做的事与一些轻量更新。',
    to: '/moments'
  },
  {
    eyebrow: 'Projects',
    title: '项目经历',
    count: `${homeData.value.featuredProjects?.length ?? 0}`,
    description: homeData.value.featuredProjects?.[0]?.name || '查看做过的项目、承担的角色与技术栈。',
    to: '/resume'
  },
  {
    eyebrow: 'Honors',
    title: '荣誉摘要',
    count: `${homeData.value.latestHonors?.length ?? 0}`,
    description: homeData.value.latestHonors?.[0]?.title || '把重要节点和阶段性成果整理成时间线。',
    to: '/about'
  }
]);

const scrollContainer = ref<HTMLElement | null>(null);
let wheelHandler: ((event: WheelEvent) => void) | null = null;

function isTagsBlock(block: HeroTextBlock | HeroTagsBlock): block is HeroTagsBlock {
  return block.type === 'tags';
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
