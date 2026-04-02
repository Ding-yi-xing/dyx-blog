<template>
  <div>
    <div
      v-if="selectedUrls.length"
      class="grid gap-3 sm:grid-cols-2 xl:grid-cols-3"
    >
      <div
        v-for="(url, index) in selectedUrls"
        :key="`${url}-${index}`"
        class="group relative overflow-hidden rounded-2xl border border-slate-200 bg-slate-50"
      >
        <img
          v-if="isImageUrl(url)"
          :src="url"
          alt="selected media"
          class="h-32 w-full object-cover"
        />
        <video
          v-else-if="isVideoUrl(url)"
          :src="url"
          controls
          preload="metadata"
          class="h-32 w-full bg-black object-cover"
        ></video>
        <div
          v-else
          class="flex h-32 flex-col justify-between bg-slate-950 px-4 py-4 text-white"
        >
          <span
            class="w-fit rounded-full bg-white/15 px-3 py-1 text-[11px] uppercase tracking-[0.24em]"
          >
            {{ isPdfUrl(url) ? "PDF" : "FILE" }}
          </span>
          <p class="line-clamp-2 text-sm font-medium leading-6">
            {{ extractFileName(url) || "已选择文件" }}
          </p>
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
    <div
      v-else
      class="rounded-2xl border border-dashed border-slate-300 bg-slate-50 px-4 py-6 text-sm text-slate-400"
    >
      {{ emptyText }}
    </div>

    <div class="mt-4 flex flex-wrap gap-3">
      <el-button type="primary" plain @click="openDialog">{{
        buttonText
      }}</el-button>
      <el-button v-if="selectedUrls.length" @click="clearSelection"
        >清空已选</el-button
      >
    </div>
    <p class="mt-2 text-xs leading-6 text-slate-400">
      {{
        multiple
          ? "支持多选图片、视频、PDF 与其他媒体文件。"
          : "支持从媒体库选择单个媒体文件，也可在弹窗内直接上传。"
      }}
    </p>

    <el-dialog v-model="dialogVisible" :title="title" width="960px">
      <div
        class="flex flex-wrap items-center justify-between gap-3 border-b border-slate-200 pb-4"
      >
        <p class="text-sm text-slate-500">
          点击卡片即可{{
            multiple ? "多选" : "选择"
          }}，图片资源支持“直接使用”或“裁剪后使用”，上传成功后会自动加入当前选择。
        </p>
        <el-upload :show-file-list="false" :http-request="handleUpload">
          <el-button type="primary" :loading="uploading">上传文件</el-button>
        </el-upload>
      </div>

      <div v-if="loading" class="py-12 text-center text-sm text-slate-500">
        媒体资源加载中...
      </div>
      <div v-else-if="!mediaList.length" class="py-8">
        <el-empty description="暂无媒体资源" />
      </div>
      <div
        v-else
        class="mt-6 grid max-h-[60vh] gap-4 overflow-y-auto sm:grid-cols-2 xl:grid-cols-3"
      >
        <div
          v-for="item in mediaList"
          :key="item.id"
          class="group relative overflow-hidden rounded-2xl border transition"
          :class="
            isSelected(item.fileUrl)
              ? 'border-slate-900 bg-slate-900/5 shadow-sm'
              : 'border-slate-200 bg-white hover:border-slate-300 hover:-translate-y-0.5'
          "
        >
          <div
            class="relative h-40 overflow-hidden bg-slate-100 cursor-pointer"
            @click="toggleMedia(item.fileUrl)"
          >
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
            <div
              v-else
              class="flex h-full flex-col justify-between bg-slate-950 px-4 py-4 text-white"
            >
              <span
                class="w-fit rounded-full bg-white/15 px-3 py-1 text-[11px] uppercase tracking-[0.24em]"
              >
                {{ isPdfUrl(item.fileUrl) ? "PDF" : "FILE" }}
              </span>
              <p class="line-clamp-3 text-sm font-medium leading-6">
                {{ item.originalName || item.fileName || "未命名文件" }}
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
            <div class="flex items-center justify-between gap-2">
              <p class="truncate text-sm font-medium text-slate-900 flex-1">
                {{ item.originalName || item.fileName || "未命名文件" }}
              </p>
              <span
                v-if="isSelected(item.fileUrl)"
                class="rounded-full bg-slate-900 px-3 py-1 text-xs text-white"
              >
                已选
              </span>
            </div>
            <p class="text-xs text-slate-500">
              {{ item.mediaType || "未知类型" }}
            </p>
            <p class="text-xs text-slate-400">
              {{ item.createdAt || "暂无时间" }}
            </p>
            <div class="flex flex-wrap gap-2 pt-1">
              <el-button
                size="small"
                @click.stop="applyMedia(item.fileUrl)"
              >
                直接使用
              </el-button>
              <el-button
                v-if="isImageUrl(item.fileUrl)"
                type="primary"
                size="small"
                plain
                @click.stop="openCropper(item)"
              >
                裁剪后使用
              </el-button>
            </div>
          </div>
        </div>
      </div>

      <template #footer>
        <div class="flex flex-wrap items-center justify-between gap-3">
          <p class="text-sm text-slate-500">
            当前已选 {{ draftUrls.length }} {{ multiple ? "项资源" : "个文件" }}
          </p>
          <div class="flex gap-3">
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" @click="confirmSelection"
              >确认选择</el-button
            >
          </div>
        </div>
      </template>
    </el-dialog>

    <BusinessImageCropper
      :visible="cropperVisible"
      :image-url="pendingCropUrl"
      :mode="cropMode"
      :source-name="pendingCropName"
      @update:visible="cropperVisible = $event"
      @confirm="handleCropConfirm"
    />
  </div>
</template>

<script setup lang="ts">
import { ElMessage, type UploadRequestOptions } from "element-plus";
import { computed, ref, watch } from "vue";
import { getAdminMedia, uploadAdminMedia } from "@/api/modules/admin";
import type {
  MediaPickerItem,
  CropConfirmPayload,
  CropMode,
} from "@/types/media";
import {
  extractFileName,
  isImageUrl,
  isPdfUrl,
  isVideoUrl,
} from "@/utils/media";
import { resolveErrorMessage } from "@/utils/error";
import BusinessImageCropper from "@/components/admin/BusinessImageCropper.vue";

/**
 * 后台媒体选择器。
 * 负责展示已选媒体、加载媒体库、处理上传，并支持单选、多选和图片裁剪流程。
 */
const props = withDefaults(
  defineProps<{
    modelValue?: string | string[];
    multiple?: boolean;
    title?: string;
    buttonText?: string;
    emptyText?: string;
    cropMode?: CropMode;
  }>(),
  {
    multiple: false,
    title: "选择媒体资源",
    buttonText: "从媒体库选择",
    emptyText: "暂未选择文件",
    cropMode: "hero-portrait",
  }
);

const emit = defineEmits<{
  (event: "update:modelValue", value: string | string[]): void;
}>();

/**
 * 媒体选择弹窗与裁剪流程的运行时状态。
 */
const dialogVisible = ref(false);
const loading = ref(false);
const uploading = ref(false);
const mediaList = ref<MediaPickerItem[]>([]);
const draftUrls = ref<string[]>([]);

const cropperVisible = ref(false);
const pendingCropUrl = ref("");
const pendingCropName = ref("");

/**
 * 打开图片裁剪弹窗，并记录当前待裁剪媒体的地址与文件名。
 *
 * @param item 当前选中的媒体项。
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；仅更新裁剪弹窗状态。
 * @author Dyx
 */
function openCropper(item: MediaPickerItem): void {
  pendingCropUrl.value = item.fileUrl || "";
  pendingCropName.value = item.originalName || item.fileName || "";
  cropperVisible.value = true;
}

/**
 * 直接应用当前媒体资源到草稿选择。
 *
 * @param url 当前媒体地址。
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；空地址会被直接忽略。
 * @author Dyx
 */
function applyMedia(url?: string): void {
  if (!url) {
    return;
  }
  draftUrls.value = props.multiple
    ? [...new Set([...draftUrls.value, url])]
    : [url];
}

/**
 * 处理图片裁剪确认结果，并在需要时上传新图片后回填当前选择。
 *
 * @param payload 图片裁剪组件返回的确认结果。
 * @returns 返回异步处理结果。
 * @throws 该函数不会主动向外抛出异常；上传失败时会通过页面提示反馈。
 * @author Dyx
 */
async function handleCropConfirm(payload: CropConfirmPayload): Promise<void> {
  if (!payload.edited) {
    cropperVisible.value = false;
    return;
  }
  if (!payload.file) {
    ElMessage.warning("未获取到裁剪后的图片文件");
    return;
  }
  try {
    const result = await uploadAdminMedia(payload.file);
    const newUrl = (result as { data?: { fileUrl: string } })?.data?.fileUrl;
    await loadMediaList();
    if (newUrl) {
      if (props.multiple) {
        draftUrls.value = [...new Set([...draftUrls.value, newUrl])];
      } else {
        draftUrls.value = [newUrl];
      }
    }
    ElMessage.success("图片裁剪并上传成功");
    cropperVisible.value = false;
  } catch (error) {
    ElMessage.error("裁剪图片上传失败");
  }
}

/**
 * 规范化当前外部传入的已选值，统一转换为字符串数组进行内部处理。
 */
const selectedUrls = computed(() => {
  if (Array.isArray(props.modelValue)) {
    return props.modelValue.filter(Boolean);
  }
  return props.modelValue ? [props.modelValue] : [];
});

/**
 * 在外部选中值发生变化时，同步刷新弹窗内部草稿选择。
 */
watch(
  () => props.modelValue,
  () => {
    if (!dialogVisible.value) {
      draftUrls.value = [...selectedUrls.value];
    }
  },
  { immediate: true, deep: true }
);

/**
 * 按组件模式向外同步当前选中的媒体地址。
 *
 * @param urls 当前选中的媒体地址数组。
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；单选模式下会仅输出第一项。
 * @author Dyx
 */
function emitValue(urls: string[]): void {
  emit("update:modelValue", props.multiple ? urls : urls[0] ?? "");
}

/**
 * 打开媒体库弹窗，并拉取最新媒体列表。
 *
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；媒体列表加载失败会在异步请求阶段体现。
 * @author Dyx
 */
function openDialog(): void {
  draftUrls.value = [...selectedUrls.value];
  dialogVisible.value = true;
  void loadMediaList();
}

/**
 * 获取后台媒体资源列表，并转换为选择器使用的轻量结构。
 *
 * @returns 返回异步加载结果；成功后会更新弹窗中的媒体卡片数据。
 * @throws 该函数不会主动抛出同步异常；接口失败时会以 Promise reject 形式返回。
 * @author Dyx
 */
async function loadMediaList(): Promise<void> {
  loading.value = true;
  try {
    const result = await getAdminMedia();
    const mediaData = (result as { data?: MediaPickerItem[] })?.data ?? [];
    mediaList.value = (mediaData as MediaPickerItem[]).map(
      (item: MediaPickerItem) => ({
        id: item.id,
        originalName: item.originalName,
        fileName: item.fileName,
        fileUrl: item.fileUrl,
        mediaType: item.mediaType,
        fileSize: item.fileSize,
        createdAt: item.createdAt,
      })
    );
  } finally {
    loading.value = false;
  }
}

/**
 * 判断指定媒体地址是否已存在于当前草稿选择中。
 *
 * @param url 待判断的媒体地址。
 * @returns 已选中时返回 true，否则返回 false。
 * @throws 该函数不会主动抛出异常；空地址会直接返回 false。
 * @author Dyx
 */
function isSelected(url?: string): boolean {
  return !!url && draftUrls.value.includes(url);
}

/**
 * 切换媒体卡片的选择状态。
 * 单选模式会直接替换当前选择，多选模式会执行增删切换。
 *
 * @param url 当前点击的媒体地址。
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；空地址会被直接忽略。
 * @author Dyx
 */
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

/**
 * 确认当前草稿选择，并同步回外层 v-model。
 *
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；仅同步当前已选媒体并关闭弹窗。
 * @author Dyx
 */
function confirmSelection(): void {
  emitValue([...draftUrls.value]);
  dialogVisible.value = false;
}

/**
 * 从当前已选结果中移除指定位置的媒体。
 *
 * @param index 待移除项的索引位置。
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；仅更新外层选中值。
 * @author Dyx
 */
function removeAt(index: number): void {
  const next = [...selectedUrls.value];
  next.splice(index, 1);
  emitValue(next);
}

/**
 * 清空当前所有已选媒体。
 *
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；仅向外同步空选择。
 * @author Dyx
 */
function clearSelection(): void {
  emitValue([]);
}

/**
 * 处理媒体上传请求，并在上传成功后刷新媒体库和当前草稿选择。
 *
 * @param options Element Plus 上传组件传入的请求参数。
 * @returns 返回异步上传结果。
 * @throws 该函数不会主动向外抛出异常；上传失败时会通过页面提示反馈。
 * @author Dyx
 */
async function handleUpload(options: UploadRequestOptions): Promise<void> {
  uploading.value = true;
  try {
    const result = await uploadAdminMedia(options.file as File);
    const fileUrl = (result as { data?: { fileUrl: string } })?.data?.fileUrl;
    await loadMediaList();
    if (fileUrl) {
      draftUrls.value = props.multiple
        ? [...new Set([...draftUrls.value, fileUrl])]
        : [fileUrl];
    }
    ElMessage.success("文件上传成功");
  } catch (error) {
    ElMessage.error(resolveErrorMessage(error, "文件上传失败"));
  } finally {
    uploading.value = false;
  }
}
</script>
