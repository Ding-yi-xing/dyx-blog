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

        <div class="dyx-page-card rounded-[30px] p-6 shadow-dyx-soft">
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

const guestbookData = ref<{ guestbookIntro?: string; messages?: GuestbookMessageData[] }>({});
const submitting = ref(false);
const form = reactive({
  content: '',
  publishChecked: true
});

const guestbookIntro = computed(
  () =>
    guestbookData.value.guestbookIntro ||
    '这里会保留一些来自访客的短留言。你可以只写正文，并自行选择这条留言是否公开展示。'
);
const messages = computed(() => guestbookData.value.messages ?? []);

function resetForm(): void {
  form.content = '';
  form.publishChecked = true;
}

async function loadGuestbookData(): Promise<void> {
  const response = await getGuestbookData();
  guestbookData.value = response.data ?? {};
}

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
