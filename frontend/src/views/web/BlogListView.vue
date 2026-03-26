<template>
  <section class="dyx-page-shell space-y-8 px-2 sm:px-0">
    <div class="dyx-page-card-elevated rounded-[36px] px-6 py-7 md:px-8 md:py-8">
      <div class="flex flex-col gap-4 border-b border-[rgb(var(--dyx-border-subtle-rgb)/0.62)] pb-5 md:flex-row md:items-end md:justify-between">
        <div>
          <p class="text-[11px] font-medium uppercase tracking-[0.32em] dyx-text-meta">Blog</p>
          <h1 class="mt-2 text-2xl font-semibold tracking-tight dyx-text-main md:text-3xl">所有文章</h1>
          <p class="mt-2 max-w-2xl text-sm leading-7 dyx-text-muted">
            参考 SpaceX 那种克制的大留白布局，把内容退回到文章本身。这里按更新时间从近到远展开。
          </p>
        </div>
      </div>
    </div>

    <div class="space-y-3">
      <RouterLink
        v-for="item in posts"
        :key="item.id"
        :to="`/blog/${item.id}`"
        class="dyx-page-card group block overflow-hidden rounded-[28px] px-5 py-5 transition-all duration-200 hover:-translate-y-0.5"
      >
        <div class="flex flex-col gap-4 sm:flex-row sm:items-start">
          <div class="flex-1">
            <h2 class="text-base font-semibold dyx-text-main transition-opacity duration-150 group-hover:opacity-80 sm:text-lg">
              {{ item.title }}
            </h2>
            <p class="mt-2 max-w-3xl text-[13px] leading-7 dyx-text-muted line-clamp-2">
              {{ item.summary || '暂无摘要' }}
            </p>
            <div class="mt-4 flex flex-wrap items-center gap-2 text-[12px] dyx-text-meta">
              <span v-if="item.updatedAt">{{ formatDateYmd(item.updatedAt) }}</span>
            </div>
          </div>

          <div v-if="item.coverImage" class="shrink-0 sm:w-44">
            <el-image
              :src="item.coverImage"
              :preview-src-list="[item.coverImage]"
              fit="cover"
              preview-teleported
              class="h-28 w-full rounded-[24px] object-cover"
              @click.stop
            />
          </div>
        </div>
      </RouterLink>

      <p v-if="!posts.length" class="dyx-page-card rounded-[24px] border border-dashed border-[rgb(var(--dyx-border-subtle-rgb)/0.72)] px-4 py-6 text-center text-sm dyx-text-meta">
        暂无文章。
      </p>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { getPosts, recordSiteVisit, type PostData } from '@/api/modules/site';
import { formatDateYmd } from '@/utils/date';

const posts = ref<PostData[]>([]);

async function loadPosts(): Promise<void> {
  const response = await getPosts();
  posts.value = response.data ?? [];
}

onMounted(() => {
  void recordSiteVisit('blog');
  void loadPosts();
});
</script>
