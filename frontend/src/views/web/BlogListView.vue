<template>
  <section class="dyx-page-shell space-y-8 px-2 sm:px-0">
    <div
      class="dyx-page-card-elevated rounded-[36px] px-6 py-7 md:px-8 md:py-8"
    >
      <div
        class="flex flex-col gap-4 border-b border-[rgb(var(--dyx-border-subtle-rgb)/0.62)] pb-5 md:flex-row md:items-end md:justify-between"
      >
        <div>
          <p
            class="text-[11px] font-medium uppercase tracking-[0.32em] dyx-text-meta"
          >
            Blog
          </p>
          <h1
            class="mt-2 text-2xl font-semibold tracking-tight dyx-text-main md:text-3xl"
          >
            所有文章
          </h1>
        </div>
      </div>
    </div>

    <div class="space-y-3">
      <div v-if="loading" class="space-y-3">
        <el-skeleton
          v-for="n in 4"
          :key="`post-skeleton-${n}`"
          animated
          :rows="3"
          class="dyx-page-card rounded-[28px] px-5 py-5"
        />
      </div>

      <template v-else>
        <RouterLink
          v-for="item in posts"
          :key="item.id"
          :to="`/blog/${item.id}`"
          class="dyx-page-card group block overflow-hidden rounded-[28px] px-5 py-5 transition-all duration-200 hover:-translate-y-0.5"
        >
          <div class="flex flex-col gap-4 sm:flex-row sm:items-start">
            <div class="flex-1">
              <h2
                class="text-base font-semibold dyx-text-main transition-opacity duration-150 group-hover:opacity-80 sm:text-lg"
              >
                {{ item.title }}
              </h2>
              <p
                class="mt-2 max-w-3xl text-[13px] leading-7 dyx-text-muted line-clamp-4"
              >
                {{ item.summary || "暂无摘要" }}
              </p>
              <div
                class="mt-4 flex flex-wrap items-center gap-2 text-[12px] dyx-text-meta"
              >
                <span
                  v-if="resolvePostDate(item)"
                  >{{ formatDateYmd(resolvePostDate(item) as string) }}</span
                >
              </div>
            </div>

            <div v-if="item.coverImage" class="shrink-0 sm:w-48">
              <div
                class="flex aspect-[4/3] items-center justify-center overflow-hidden rounded-[24px]"
              >
                <el-image
                  :src="item.coverImage"
                  :preview-src-list="[item.coverImage]"
                  fit="contain"
                  preview-teleported
                  class="h-full w-full"
                  @click.stop
                />
              </div>
            </div>
          </div>
        </RouterLink>

        <p
          v-if="!posts.length"
          class="dyx-page-card rounded-[24px] border border-dashed border-[rgb(var(--dyx-border-subtle-rgb)/0.72)] px-4 py-6 text-center text-sm dyx-text-meta"
        >
          暂无文章。
        </p>
      </template>
    </div>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { getPosts, recordSiteVisit, type PostData } from "@/api/modules/site";
import { formatDateYmd } from "@/utils/date";

const posts = ref<PostData[]>([]);
const loading = ref(false);

function resolvePostDate(post: PostData): string | undefined {
  return post.publishedAt || post.updatedAt;
}

/**
 * 获取文章列表并刷新页面数据源。
 *
 * @returns 返回异步加载结果；成功后会更新文章列表。
 * @throws 该函数不会主动抛出同步异常；接口失败时会以 Promise reject 形式返回。
 * @author Dyx
 */
async function loadPosts(): Promise<void> {
  loading.value = true;
  try {
    const response = await getPosts();
    posts.value = response.data ?? [];
  } finally {
    loading.value = false;
  }
}
onMounted(() => {
  void recordSiteVisit("blog");
  void loadPosts();
});
</script>
