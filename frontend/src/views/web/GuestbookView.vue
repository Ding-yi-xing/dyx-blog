<template>
  <section class="dyx-page-shell space-y-8 px-2 sm:px-0">
    <div class="dyx-page-card-elevated rounded-[38px] p-8 lg:p-10">
      <div class="grid gap-8 lg:grid-cols-[0.92fr_1.08fr] lg:items-start">
        <div class="space-y-4">
          <p class="text-sm uppercase tracking-[0.35em] dyx-text-meta">Guestbook</p>
          <h1 class="text-3xl font-semibold dyx-text-main sm:text-4xl">留言</h1>
          <p class="whitespace-pre-line text-sm leading-8 dyx-text-muted">
            {{ guestbookIntro }}
          </p>
        </div>

        <div class="dyx-page-card rounded-[30px] p-6 shadow-dyx-soft dyx-guestbook-form-card">
          <p class="text-sm uppercase tracking-[0.28em] dyx-text-meta">写点什么</p>
          <el-form label-position="top" class="mt-5">
            <el-form-item label="正文">
              <el-input
                v-model="form.content"
                type="textarea"
                :rows="6"
                maxlength="2000"
                show-word-limit
                placeholder="写下你想说的话。"
              />
            </el-form-item>
            <el-form-item>
              <el-checkbox v-model="form.publishChecked">公开展示这条留言</el-checkbox>
            </el-form-item>
            <div class="flex justify-end">
              <el-button type="primary" :loading="submitting" @click="handleSubmit">提交留言</el-button>
            </div>
          </el-form>
        </div>
      </div>
    </div>

    <div class="dyx-page-card rounded-[38px] p-8 lg:p-10">
      <div class="flex items-end justify-between gap-4 border-b border-[rgb(var(--dyx-border-subtle-rgb)/0.6)] pb-5">
        <div>
          <p class="text-sm uppercase tracking-[0.35em] dyx-text-meta">Published</p>
          <h2 class="mt-2 text-2xl font-semibold dyx-text-main">公开留言</h2>
        </div>
        <p class="text-sm dyx-text-meta">{{ messages.length }} 条</p>
      </div>

      <div v-if="messages.length" class="mt-6 grid gap-4">
        <article
          v-for="item in messages"
          :key="item.id"
          class="rounded-[28px] border border-[rgb(var(--dyx-border-subtle-rgb)/0.58)] bg-[rgb(var(--dyx-bg-surface-rgb)/0.46)] px-5 py-5"
        >
          <div class="flex items-center justify-between gap-4">
            <span class="dyx-meta-pill">公开留言</span>
            <span class="text-xs dyx-text-meta">{{ formatDateTime(item.createdAt) || '刚刚' }}</span>
          </div>
          <p class="mt-4 whitespace-pre-line text-sm leading-8 dyx-text-muted">
            {{ item.content || '暂无内容。' }}
          </p>
        </article>
      </div>

      <div
        v-else
        class="mt-6 rounded-[28px] border border-dashed border-[rgb(var(--dyx-border-subtle-rgb)/0.72)] bg-[rgb(var(--dyx-bg-surface-rgb)/0.34)] px-6 py-10 text-center text-sm dyx-text-meta"
      >
        暂时还没有公开留言。
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus';
import { computed, onMounted, reactive, ref } from 'vue';
import { createGuestbookMessage, getGuestbookData, recordSiteVisit, type GuestbookMessageData } from '@/api/modules/site';
import { formatDateTime } from '@/utils/date';
import { resolveErrorMessage } from '@/utils/error';

/**
 * 前台留言页。
 * 负责展示留言介绍和公开留言列表，并提供访客提交留言能力。
 */
const guestbookData = ref<{ guestbookIntro?: string; messages?: GuestbookMessageData[] }>({});
const submitting = ref(false);
const form = reactive({
  content: '',
  publishChecked: true
});

/**
 * 解析留言页介绍文案，未配置时回退到默认提示。
 */
const guestbookIntro = computed(
  () =>
    guestbookData.value.guestbookIntro ||
    '这里会保留一些来自访客的短留言。你可以只写正文，并自行选择这条留言是否公开展示。'
);

/**
 * 获取当前公开留言列表。
 */
const messages = computed(() => guestbookData.value.messages ?? []);

/**
 * 重置留言提交表单。
 *
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；仅恢复本地默认值。
 * @author Dyx
 */
function resetForm(): void {
  form.content = '';
  form.publishChecked = true;
}

/**
 * 获取留言页数据并刷新介绍文案与公开留言列表。
 *
 * @returns 返回异步加载结果；成功后会更新页面展示数据。
 * @throws 该函数不会主动抛出同步异常；接口失败时会以 Promise reject 形式返回。
 * @author Dyx
 */
async function loadGuestbookData(): Promise<void> {
  const response = await getGuestbookData();
  guestbookData.value = response.data ?? {};
}

/**
 * 提交新的留言内容。
 *
 * @returns 返回异步提交结果；成功后会清空表单并刷新留言数据。
 * @throws 该函数不会主动向外抛出异常；提交失败时会通过页面提示反馈。
 * @author Dyx
 */
async function handleSubmit(): Promise<void> {
  if (submitting.value) {
    return;
  }
  submitting.value = true;
  try {
    await createGuestbookMessage({
      content: form.content,
      published: form.publishChecked ? 1 : 0
    });
    ElMessage.success(form.publishChecked ? '留言提交成功' : '留言已提交，当前不会公开展示');
    resetForm();
    await loadGuestbookData();
  } catch (error) {
    ElMessage.error(resolveErrorMessage(error, '留言提交失败'));
  } finally {
    submitting.value = false;
  }
}

onMounted(() => {
  void recordSiteVisit('guestbook');
  void loadGuestbookData();
});
</script>

<style scoped>
.dyx-guestbook-form-card :deep(.el-form-item__label),
.dyx-guestbook-form-card :deep(.el-checkbox__label) {
  color: rgb(var(--dyx-text-muted-rgb));
}

.dyx-guestbook-form-card :deep(.el-input__wrapper),
.dyx-guestbook-form-card :deep(.el-textarea__inner) {
  background: rgb(var(--dyx-bg-surface-muted-rgb) / 0.72);
  border: 1px solid rgb(var(--dyx-border-subtle-rgb) / 0.72);
  box-shadow: none;
  color: rgb(var(--dyx-text-main-rgb));
}

.dyx-guestbook-form-card :deep(.el-textarea__inner) {
  min-height: 168px;
}

.dyx-guestbook-form-card :deep(.el-input__wrapper.is-focus),
.dyx-guestbook-form-card :deep(.el-textarea__inner:focus) {
  border-color: rgb(var(--dyx-accent-rgb) / 0.72);
  box-shadow: 0 0 0 1px rgb(var(--dyx-accent-rgb) / 0.22);
}

.dyx-guestbook-form-card :deep(.el-textarea__inner::placeholder) {
  color: rgb(var(--dyx-text-meta-rgb));
}

.dark .dyx-guestbook-form-card :deep(.el-input__wrapper),
.dark .dyx-guestbook-form-card :deep(.el-textarea__inner) {
  background: rgb(18 24 36 / 0.92);
  border-color: rgb(var(--dyx-border-subtle-rgb) / 0.82);
  color: rgb(var(--dyx-text-main-rgb));
}

.dark .dyx-guestbook-form-card :deep(.el-checkbox__inner) {
  background: rgb(18 24 36 / 0.92);
  border-color: rgb(var(--dyx-border-strong-rgb) / 0.9);
}

.dark .dyx-guestbook-form-card :deep(.el-input__count),
.dark .dyx-guestbook-form-card :deep(.el-input__count-inner) {
  background: transparent;
  color: rgb(var(--dyx-text-meta-rgb));
}
</style>
