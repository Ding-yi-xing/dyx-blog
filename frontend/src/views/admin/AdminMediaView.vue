<template>
  <section class="rounded-[28px] bg-white p-6 shadow-sm">
    <div class="flex flex-wrap items-start justify-between gap-4">
      <h2 class="text-xl font-semibold text-slate-900">媒体资源管理</h2>
      <div class="flex flex-wrap gap-3">
        <el-button :loading="importing" plain @click="handleImportExisting">导入 uploads 文件</el-button>
        <el-upload :show-file-list="false" :http-request="handleUpload">
          <el-button type="primary">选择文件</el-button>
        </el-upload>
      </div>
    </div>

    <div class="mt-6 text-sm text-slate-500">已收录 {{ mediaList.length }} 个媒体文件。</div>

    <el-table class="mt-6" :data="mediaList" border>
      <el-table-column label="预览" width="140">
        <template #default="scope">
          <div v-if="scope.row.fileUrl" class="flex items-center justify-center">
            <el-image
              v-if="isImageUrl(scope.row.fileUrl)"
              :src="scope.row.fileUrl"
              :preview-src-list="[scope.row.fileUrl]"
              fit="cover"
              preview-teleported
              class="h-16 w-24 overflow-hidden rounded-2xl"
            />
            <a
              v-else
              :href="scope.row.fileUrl"
              target="_blank"
              rel="noreferrer"
              class="flex h-16 w-24 flex-col justify-between rounded-2xl bg-slate-950 px-3 py-3 text-white"
            >
              <span class="w-fit rounded-full bg-white/15 px-2 py-0.5 text-[10px] uppercase tracking-[0.2em]">
                {{ isPdfUrl(scope.row.fileUrl) ? 'PDF' : 'FILE' }}
              </span>
              <span class="line-clamp-2 text-[11px] leading-4">{{ extractFileName(scope.row.fileUrl) || '打开文件' }}</span>
            </a>
          </div>
          <span v-else class="text-sm text-slate-400">暂无预览</span>
        </template>
      </el-table-column>
      <el-table-column prop="originalName" label="文件名" min-width="240" />
      <el-table-column prop="mediaType" label="类型" min-width="180" />
      <el-table-column prop="sizeText" label="大小" width="120" />
      <el-table-column label="链接" min-width="220">
        <template #default="scope">
          <a
            v-if="scope.row.fileUrl"
            :href="scope.row.fileUrl"
            target="_blank"
            rel="noreferrer"
            class="text-sm text-slate-700 transition hover:text-slate-950"
          >
            打开文件
          </a>
          <span v-else class="text-sm text-slate-400">暂无链接</span>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="上传时间" width="180" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="scope">
          <el-button
            v-if="scope.row.fileUrl"
            link
            type="primary"
            @click="openPreview(scope.row.fileUrl)"
          >
            预览
          </el-button>
          <el-button link type="danger" @click="handleDelete(scope.row.raw)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </section>
</template>

<script setup lang="ts">
import { ElMessage, ElMessageBox, type UploadRequestOptions } from 'element-plus';
import { computed, h, onMounted, ref } from 'vue';
import { deleteAdminMedia, getAdminMedia, importExistingAdminMedia, uploadAdminMedia } from '@/api/modules/admin';
import type { MediaData } from '@/api/modules/admin';
import { extractFileName, isImageUrl, isPdfUrl } from '@/utils/media';

const importing = ref(false);
const mediaRawList = ref<MediaData[]>([]);

const mediaList = computed(() =>
  mediaRawList.value.map((item) => ({
    ...item,
    sizeText: formatFileSize(item.fileSize),
    raw: item
  }))
);

function formatFileSize(size?: number): string {
  if (!size) {
    return '0KB';
  }
  if (size >= 1024 * 1024) {
    return `${(size / (1024 * 1024)).toFixed(1)}MB`;
  }
  return `${Math.max(1, Math.round(size / 1024))}KB`;
}

async function loadMediaList(): Promise<void> {
  const response = await getAdminMedia();
  mediaRawList.value = response.data ?? [];
}

async function handleImportExisting(): Promise<void> {
  if (importing.value) {
    return;
  }
  importing.value = true;
  try {
    const response = await importExistingAdminMedia();
    const importedCount = Number(response.data ?? 0);
    ElMessage.success(importedCount > 0 ? `已导入 ${importedCount} 个已有文件` : 'uploads 中暂无可新增导入文件');
    await loadMediaList();
  } catch (error) {
    ElMessage.error('导入已有文件失败');
  } finally {
    importing.value = false;
  }
}

async function handleUpload(options: UploadRequestOptions): Promise<void> {
  try {
    await uploadAdminMedia(options.file as File);
    ElMessage.success('上传成功');
    await loadMediaList();
  } catch (error) {
    ElMessage.error('上传失败');
  }
}

function openPreview(url: string): void {
  if (isImageUrl(url)) {
    void ElMessageBox.alert(h('img', {
      src: url,
      alt: 'preview',
      style: {
        display: 'block',
        maxWidth: '100%',
        maxHeight: '70vh',
        margin: '0 auto',
        borderRadius: '20px',
        objectFit: 'contain'
      }
    }), '图片预览', {
      confirmButtonText: '关闭'
    });
    return;
  }
  void window.open(url, '_blank', 'noopener,noreferrer');
}

function resolveErrorMessage(error: unknown, fallback: string): string {
  if (typeof error === 'object' && error && 'response' in error) {
    const response = (error as { response?: { data?: { message?: string } } }).response;
    if (response?.data?.message) {
      return response.data.message;
    }
  }
  return fallback;
}

async function handleDelete(item: MediaData): Promise<void> {
  const displayName = item.originalName || item.fileName || '该媒体';
  try {
    await ElMessageBox.confirm(
      h('div', { class: 'space-y-2' }, [
        h('p', `确认删除“${displayName}”吗？`),
        h('p', { class: 'text-sm text-slate-500' }, '若该媒体仍被个人资料、项目、动态、荣誉或文章引用，系统会阻止删除。')
      ]),
      '删除确认',
      {
        type: 'warning'
      }
    );
    await deleteAdminMedia(item.id);
    ElMessage.success('媒体删除成功');
    await loadMediaList();
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      return;
    }
    ElMessage.error(resolveErrorMessage(error, '媒体删除失败'));
  }
}

onMounted(() => {
  void loadMediaList();
});
</script>
