<template>
  <div>
    <div v-if="selectedUrls.length" class="grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
      <div
        v-for="(url, index) in selectedUrls"
        :key="`${url}-${index}`"
        class="group relative overflow-hidden rounded-2xl border border-slate-200 bg-slate-50"
      >
        <img v-if="isImageUrl(url)" :src="url" alt="selected media" class="h-32 w-full object-cover" />
        <video v-else-if="isVideoUrl(url)" :src="url" controls preload="metadata" class="h-32 w-full bg-black object-cover"></video>
        <div v-else class="flex h-32 flex-col justify-between bg-slate-950 px-4 py-4 text-white">
          <span class="w-fit rounded-full bg-white/15 px-3 py-1 text-[11px] uppercase tracking-[0.24em]">
            {{ isPdfUrl(url) ? 'PDF' : 'FILE' }}
          </span>
          <p class="line-clamp-2 text-sm font-medium leading-6">{{ extractFileName(url) || '已选择文件' }}</p>
        </div>
        <button
          type="button"
          class="absolute right-3 top-3 rounded-full bg-black/65 px-3 py-1 text-xs text-white opacity-0 transition group-hover:opacity-100"
          @click="removeAt(index)"
        >
          移除
        </button>
      </div>
    </div>
    <div v-else class="rounded-2xl border border-dashed border-slate-300 bg-slate-50 px-4 py-6 text-sm text-slate-400">
      {{ emptyText }}
    </div>

    <div class="mt-4 flex flex-wrap gap-3">
      <el-button type="primary" plain @click="openDialog">{{ buttonText }}</el-button>
      <el-button v-if="selectedUrls.length" @click="clearSelection">清空已选</el-button>
    </div>
    <p class="mt-2 text-xs leading-6 text-slate-400">
      {{ multiple ? '支持多选图片、视频、PDF 与其他媒体文件。' : '支持从媒体库选择单个媒体文件，也可在弹窗内直接上传。' }}
    </p>

    <el-dialog v-model="dialogVisible" :title="title" width="960px">
      <div class="flex flex-wrap items-center justify-between gap-3 border-b border-slate-200 pb-4">
        <p class="text-sm text-slate-500">点击卡片即可{{ multiple ? '多选' : '选择' }}，上传成功后会自动加入当前选择。</p>
        <el-upload :show-file-list="false" :http-request="handleUpload">
          <el-button type="primary" :loading="uploading">上传文件</el-button>
        </el-upload>
      </div>

      <div v-if="loading" class="py-12 text-center text-sm text-slate-500">媒体资源加载中...</div>
      <div v-else-if="!mediaList.length" class="py-8">
        <el-empty description="暂无媒体资源" />
      </div>
      <div v-else class="mt-6 grid max-h-[60vh] gap-4 overflow-y-auto sm:grid-cols-2 xl:grid-cols-3">
        <button
          v-for="item in mediaList"
          :key="item.id"
          type="button"
          class="group overflow-hidden rounded-2xl border text-left transition"
          :class="isSelected(item.fileUrl)
            ? 'border-slate-900 bg-slate-900/5 shadow-sm'
            : 'border-slate-200 bg-white hover:border-slate-300 hover:-translate-y-0.5'"
          @click="toggleMedia(item.fileUrl)"
        >
          <div class="relative h-40 overflow-hidden bg-slate-100">
            <img
              v-if="isImageUrl(item.fileUrl)"
              :src="item.fileUrl"
              :alt="item.originalName || item.fileName || 'media'"
              class="h-full w-full object-cover"
            />
            <video
              v-else-if="isVideoUrl(item.fileUrl)"
              :src="item.fileUrl"
              preload="metadata"
              muted
              playsinline
              class="h-full w-full bg-black object-cover"
            ></video>
            <div v-else class="flex h-full flex-col justify-between bg-slate-950 px-4 py-4 text-white">
              <span class="w-fit rounded-full bg-white/15 px-3 py-1 text-[11px] uppercase tracking-[0.24em]">
                {{ isPdfUrl(item.fileUrl) ? 'PDF' : 'FILE' }}
              </span>
              <p class="line-clamp-3 text-sm font-medium leading-6">
                {{ item.originalName || item.fileName || '未命名文件' }}
              </p>
            </div>
            <span
              v-if="isVideoUrl(item.fileUrl)"
              class="absolute left-3 top-3 rounded-full bg-black/65 px-3 py-1 text-[11px] uppercase tracking-[0.24em] text-white"
            >
              Video
            </span>
            <span
              v-if="isSelected(item.fileUrl)"
              class="absolute right-3 top-3 rounded-full bg-slate-900 px-3 py-1 text-xs text-white"
            >
              已选
            </span>
          </div>
          <div class="space-y-2 p-4">
            <p class="truncate text-sm font-medium text-slate-900">{{ item.originalName || item.fileName || '未命名文件' }}</p>
            <p class="text-xs text-slate-500">{{ item.mediaType || '未知类型' }}</p>
            <p class="text-xs text-slate-400">{{ item.createdAt || '暂无时间' }}</p>
          </div>
        </button>
      </div>

      <template #footer>
        <div class="flex flex-wrap items-center justify-between gap-3">
          <p class="text-sm text-slate-500">当前已选 {{ draftUrls.length }} {{ multiple ? '项资源' : '个文件' }}</p>
          <div class="flex gap-3">
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" @click="confirmSelection">确认选择</el-button>
          </div>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ElMessage, type UploadRequestOptions } from 'element-plus';
import { computed, ref, watch } from 'vue';
import { getAdminMedia, uploadAdminMedia } from '@/api/modules/admin';
import type { MediaPickerItem } from '@/types/media';
import { extractFileName, isImageUrl, isPdfUrl, isVideoUrl } from '@/utils/media';

const props = withDefaults(
  defineProps<{
    modelValue?: string | string[];
    multiple?: boolean;
    title?: string;
    buttonText?: string;
    emptyText?: string;
  }>(),
  {
    multiple: false,
    title: '选择媒体资源',
    buttonText: '从媒体库选择',
    emptyText: '暂未选择文件'
  }
);

const emit = defineEmits<{
  (event: 'update:modelValue', value: string | string[]): void;
}>();

const dialogVisible = ref(false);
const loading = ref(false);
const uploading = ref(false);
const mediaList = ref<MediaPickerItem[]>([]);
const draftUrls = ref<string[]>([]);

const selectedUrls = computed(() => {
  if (Array.isArray(props.modelValue)) {
    return props.modelValue.filter(Boolean);
  }
  return props.modelValue ? [props.modelValue] : [];
});

watch(
  () => props.modelValue,
  () => {
    if (!dialogVisible.value) {
      draftUrls.value = [...selectedUrls.value];
    }
  },
  { immediate: true, deep: true }
);

function emitValue(urls: string[]): void {
  emit('update:modelValue', props.multiple ? urls : (urls[0] ?? ''));
}

function openDialog(): void {
  draftUrls.value = [...selectedUrls.value];
  dialogVisible.value = true;
  void loadMediaList();
}

async function loadMediaList(): Promise<void> {
  loading.value = true;
  try {
    const response = await getAdminMedia();
    mediaList.value = ((response.data ?? []) as MediaPickerItem[]).map((item: MediaPickerItem) => ({
      id: item.id,
      originalName: item.originalName,
      fileName: item.fileName,
      fileUrl: item.fileUrl,
      mediaType: item.mediaType,
      fileSize: item.fileSize,
      createdAt: item.createdAt
    }));
  } finally {
    loading.value = false;
  }
}

function isSelected(url?: string): boolean {
  return !!url && draftUrls.value.includes(url);
}

function toggleMedia(url?: string): void {
  if (!url) {
    return;
  }
  if (props.multiple) {
    if (draftUrls.value.includes(url)) {
      draftUrls.value = draftUrls.value.filter((item) => item !== url);
      return;
    }
    draftUrls.value = [...draftUrls.value, url];
    return;
  }
  draftUrls.value = [url];
}

function confirmSelection(): void {
  emitValue([...draftUrls.value]);
  dialogVisible.value = false;
}

function removeAt(index: number): void {
  const next = [...selectedUrls.value];
  next.splice(index, 1);
  emitValue(next);
}

function clearSelection(): void {
  emitValue([]);
}

async function handleUpload(options: UploadRequestOptions): Promise<void> {
  uploading.value = true;
  try {
    const response = await uploadAdminMedia(options.file as File);
    const fileUrl = response.data?.fileUrl;
    await loadMediaList();
    if (fileUrl) {
      draftUrls.value = props.multiple ? [...new Set([...draftUrls.value, fileUrl])] : [fileUrl];
    }
    ElMessage.success('文件上传成功');
  } catch (error) {
    ElMessage.error('文件上传失败');
  } finally {
    uploading.value = false;
  }
}
</script>
