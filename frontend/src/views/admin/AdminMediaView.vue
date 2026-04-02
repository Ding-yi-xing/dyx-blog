<template>
  <section class="rounded-[28px] bg-white p-6 shadow-sm">
    <div class="flex flex-wrap items-start justify-between gap-4">
      <h2 class="text-xl font-semibold text-slate-900">媒体资源管理</h2>
      <div class="flex flex-wrap gap-3">
        <el-button type="danger" plain :disabled="!selectedIds.length" @click="handleBatchDelete">批量删除</el-button>
        <el-button :loading="importing" plain @click="handleImportExisting">导入 uploads 文件</el-button>
        <el-upload :show-file-list="false" :http-request="handleUpload">
          <el-button type="primary" :loading="uploading">选择文件</el-button>
        </el-upload>
      </div>
    </div>

    <div class="mt-6 text-sm text-slate-500">已收录 {{ mediaList.length }} 个媒体文件。</div>

    <el-table ref="tableRef" class="mt-6" :data="mediaList" border @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="52" align="center" />
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
            <button
              v-else
              type="button"
              class="flex h-16 w-24 flex-col justify-between rounded-2xl bg-slate-950 px-3 py-3 text-left text-white"
              @click="openPreview(scope.row.fileUrl)"
            >
              <span class="w-fit rounded-full bg-white/15 px-2 py-0.5 text-[10px] uppercase tracking-[0.2em]">
                {{ isVideoUrl(scope.row.fileUrl) ? 'VIDEO' : isPdfUrl(scope.row.fileUrl) ? 'PDF' : 'FILE' }}
              </span>
              <span class="line-clamp-2 text-[11px] leading-4">{{ extractFileName(scope.row.fileUrl) || '打开文件' }}</span>
            </button>
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
            :href="getPreviewUrl(scope.row.fileUrl)"
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

    <el-dialog
      v-model="previewVisible"
      :title="previewType === 'video' ? '视频预览' : '图片预览'"
      width="90vw"
      top="5vh"
      destroy-on-close
      append-to-body
    >
      <div class="flex min-h-[60vh] items-center justify-center rounded-2xl bg-slate-950/95 p-4">
        <img
          v-if="previewType === 'image' && previewUrl"
          :src="previewUrl"
          alt="preview"
          class="max-h-[80vh] max-w-full rounded-2xl object-contain"
        />
        <video
          v-else-if="previewType === 'video' && previewUrl"
          :src="previewUrl"
          controls
          preload="metadata"
          playsinline
          class="max-h-[80vh] w-full rounded-2xl bg-black"
        ></video>
      </div>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { ElMessage, ElMessageBox, type UploadRequestOptions } from 'element-plus';
import { computed, h, onMounted, ref } from 'vue';
import { deleteAdminMedia, deleteAdminMediaBatch, getAdminMedia, getAdminMediaContentUrl, importExistingAdminMedia, uploadAdminMedia } from '@/api/modules/admin';
import type { MediaData } from '@/api/modules/admin';
import { extractFileName, isImageUrl, isPdfUrl, isVideoUrl } from '@/utils/media';
import { resolveErrorMessage } from '@/utils/error';

/**
 * 后台媒体资源管理页。
 * 负责展示媒体库列表，并提供导入 uploads、上传新文件、预览和删除能力。
 */
const importing = ref(false);
const uploading = ref(false);
const mediaRawList = ref<MediaData[]>([]);
const selectedIds = ref<number[]>([]);
const tableRef = ref<{ clearSelection: () => void } | null>(null);
const previewVisible = ref(false);
const previewUrl = ref('');
const previewType = ref<'image' | 'video'>('image');

/**
 * 将后台媒体列表转换为表格展示所需的衍生字段。
 */
const mediaList = computed(() =>
  mediaRawList.value.map((item) => ({
    ...item,
    sizeText: formatFileSize(item.fileSize),
    raw: item
  }))
);

function resetSelection(): void {
  tableRef.value?.clearSelection();
  selectedIds.value = [];
}

function handleSelectionChange(selection: Array<MediaData & { raw?: MediaData }>): void {
  selectedIds.value = selection.map((item) => Number(item.id)).filter((id) => !Number.isNaN(id));
}

/**
 * 将字节大小格式化为便于后台表格阅读的文本。
 *
 * @param size 文件大小，单位为字节。
 * @returns 返回 KB 或 MB 文本；缺失时返回 0KB。
 * @throws 该函数不会主动抛出异常；仅执行本地格式化。
 * @author Dyx
 */
function formatFileSize(size?: number): string {
  if (!size) {
    return '0KB';
  }
  if (size >= 1024 * 1024) {
    return `${(size / (1024 * 1024)).toFixed(1)}MB`;
  }
  return `${Math.max(1, Math.round(size / 1024))}KB`;
}

function getPreviewUrl(url: string): string {
  if (isVideoUrl(url)) {
    return url;
  }
  return getAdminMediaContentUrl(url);
}

/**
 * 获取后台媒体库列表并刷新表格数据源。
 *
 * @returns 返回异步加载结果；成功后会更新页面媒体列表。
 * @throws 该函数不会主动抛出同步异常；接口失败时会以 Promise reject 形式返回。
 * @author Dyx
 */
async function loadMediaList(): Promise<void> {
  const result = await getAdminMedia();
  const mediaData = (result as { data?: MediaData[] })?.data;
  mediaRawList.value = Array.isArray(mediaData) ? mediaData : [];
}

/**
 * 导入 uploads 目录中尚未登记到媒体库的历史文件。
 *
 * @returns 返回异步导入结果；成功后会提示导入数量并刷新列表。
 * @throws 该函数不会主动向外抛出异常；导入失败时会通过页面提示反馈。
 * @author Dyx
 */
async function handleImportExisting(): Promise<void> {
  if (importing.value) {
    return;
  }
  importing.value = true;
  try {
    const result = await importExistingAdminMedia();
    const importedCount = Number((result as { data?: number })?.data ?? 0);
    ElMessage.success(importedCount > 0 ? `已导入 ${importedCount} 个已有文件` : 'uploads 中暂无可新增导入文件');
    await loadMediaList();
  } catch (error) {
    ElMessage.error('导入已有文件失败');
  } finally {
    importing.value = false;
  }
}

/**
 * 上传用户选中的新媒体文件。
 *
 * @param options Element Plus 上传组件传入的请求上下文。
 * @returns 返回异步上传结果；成功后会刷新媒体列表。
 * @throws 该函数不会主动向外抛出异常；上传失败时会通过页面提示反馈。
 * @author Dyx
 */
async function handleUpload(options: UploadRequestOptions): Promise<void> {
  if (uploading.value) {
    return;
  }
  uploading.value = true;
  try {
    await uploadAdminMedia(options.file as File);
    ElMessage.success('上传成功');
    await loadMediaList();
  } catch (error) {
    ElMessage.error(resolveErrorMessage(error, '上传失败'));
  } finally {
    uploading.value = false;
  }
}

/**
 * 根据媒体类型打开预览。
 * 图片与视频使用大尺寸弹窗，其他文件交由新窗口打开。
 *
 * @param url 待预览的媒体地址。
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；仅执行前端预览逻辑。
 * @author Dyx
 */
function openPreview(url: string): void {
  const resolvedPreviewUrl = getPreviewUrl(url);
  if (isImageUrl(url)) {
    previewType.value = 'image';
    previewUrl.value = resolvedPreviewUrl;
    previewVisible.value = true;
    return;
  }
  if (isVideoUrl(url)) {
    previewType.value = 'video';
    previewUrl.value = resolvedPreviewUrl;
    previewVisible.value = true;
    return;
  }
  void window.open(resolvedPreviewUrl, '_blank', 'noopener,noreferrer');
}

/**
 * 删除指定媒体，并在确认后刷新当前列表。
 *
 * @param item 待删除的媒体数据。
 * @returns 返回异步删除结果。
 * @throws 该函数不会主动向外抛出异常；取消删除时会静默结束，失败时通过页面提示反馈。
 * @author Dyx
 */
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
    resetSelection();
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      return;
    }
    ElMessage.error(resolveErrorMessage(error, '媒体删除失败'));
  }
}

async function handleBatchDelete(): Promise<void> {
  if (!selectedIds.value.length) {
    return;
  }
  try {
    await ElMessageBox.confirm(
      h('div', { class: 'space-y-2' }, [
        h('p', `确认删除选中的 ${selectedIds.value.length} 个媒体文件吗？`),
        h('p', { class: 'text-sm text-slate-500' }, '若所选媒体中存在仍被引用的文件，系统会阻止删除。')
      ]),
      '批量删除确认',
      {
        type: 'warning'
      }
    );
    await deleteAdminMediaBatch(selectedIds.value);
    ElMessage.success('媒体批量删除成功');
    await loadMediaList();
    resetSelection();
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      return;
    }
    ElMessage.error(resolveErrorMessage(error, '媒体批量删除失败'));
  }
}

onMounted(() => {
  void loadMediaList();
});
</script>
