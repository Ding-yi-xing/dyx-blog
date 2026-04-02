<template>
  <el-dialog
    :model-value="visible"
    :title="config.title"
    width="1120px"
    destroy-on-close
    append-to-body
    @closed="handleClosed"
    @update:model-value="emit('update:visible', $event)"
  >
    <div class="grid gap-6 lg:grid-cols-[minmax(0,1fr)_320px]">
      <div
        class="overflow-hidden rounded-[24px] border border-slate-200 bg-slate-950/95 p-4"
      >
        <div class="rounded-[20px] bg-slate-900 p-4">
          <div
            class="h-[460px] overflow-hidden rounded-[16px] bg-slate-950/90"
            @wheel="markEdited"
          >
            <VueCropper
              v-if="visible && cropSourceUrl"
              :key="cropperKey"
              ref="cropperRef"
              :img="cropSourceUrl"
              :output-size="1"
              output-type="png"
              :auto-crop="true"
              :auto-crop-width="config.autoCropWidth"
              :auto-crop-height="config.autoCropHeight"
              :fixed="false"
              :fixed-box="false"
              :can-scale="true"
              :can-move="true"
              :can-move-box="true"
              :center-box="true"
              :info="true"
              :full="true"
              :high="true"
              :original="false"
              :enlarge="1"
              mode="contain"
              class="h-full w-full"
              @realTime="handleRealtime"
              @imgLoad="handleImgLoad"
              @imgMoving="markEdited"
              @cropMoving="markEdited"
            />
            <div
              v-else
              class="flex h-full items-center justify-center text-sm text-slate-400"
            >
              暂无可裁剪图片
            </div>
          </div>
        </div>
      </div>

      <div
        class="space-y-4 rounded-[24px] border border-slate-200 bg-slate-50 p-5"
      >
        <div>
          <p class="text-sm font-semibold text-slate-900">裁剪说明</p>
          <p class="mt-2 text-sm leading-6 text-slate-500">
            {{ config.description }}
          </p>
        </div>

        <div class="rounded-[18px] border border-slate-200 bg-white p-4">
          <p class="text-xs uppercase tracking-[0.22em] text-slate-400">
            业务预览
          </p>

          <div
            v-if="mode === 'avatar'"
            class="mt-4 flex flex-col items-center gap-3"
          >
            <div
              class="flex h-28 w-28 items-center justify-center overflow-hidden rounded-full border border-slate-200 bg-slate-100 shadow-sm"
            >
              <div
                v-if="hasPreview"
                :style="previewViewportStyle"
                class="preview-frame"
              >
                <div :style="previewContentStyle">
                  <div :style="preview.div">
                    <img
                      :src="preview.url || cropSourceUrl"
                      :style="preview.img"
                      alt="avatar-preview"
                    />
                  </div>
                </div>
              </div>
              <span v-else class="text-xs text-slate-400">等待预览</span>
            </div>
            <p class="text-xs text-slate-400">头像将以圆形样式展示。</p>
          </div>

          <div v-else-if="mode === 'hero-background'" class="mt-4 space-y-3">
            <div
              class="overflow-hidden rounded-[20px] border border-slate-200 bg-slate-950 shadow-sm"
            >
              <div class="relative h-32 w-full bg-slate-900">
                <div
                  v-if="hasPreview"
                  :style="previewViewportStyle"
                  class="preview-frame absolute inset-0"
                >
                  <div :style="previewContentStyle">
                    <div :style="preview.div">
                      <img
                        :src="preview.url || cropSourceUrl"
                        :style="preview.img"
                        alt="hero-background-preview"
                      />
                    </div>
                  </div>
                </div>
                <div
                  class="absolute inset-0 bg-gradient-to-r from-slate-950/55 via-slate-950/20 to-transparent"
                ></div>
                <div
                  class="relative z-10 flex h-full flex-col justify-end px-4 py-4 text-white"
                >
                  <p
                    class="text-[11px] uppercase tracking-[0.28em] text-slate-200/90"
                  >
                    HERO BACKGROUND
                  </p>
                  <p class="mt-2 text-base font-semibold leading-6">
                    阅读博客 / 查看关于我
                  </p>
                </div>
              </div>
            </div>
            <p class="text-xs leading-5 text-slate-400">
              用于首页首屏背景，建议保留主体区域与文字安全区。
            </p>
          </div>

          <div v-else class="mt-4 flex flex-col items-center gap-3">
            <div
              class="overflow-hidden rounded-[28px] border border-slate-200 bg-slate-100 shadow-sm"
            >
              <div
                class="flex h-56 w-44 items-center justify-center overflow-hidden bg-slate-100"
              >
                <div
                  v-if="hasPreview"
                  :style="previewViewportStyle"
                  class="preview-frame"
                >
                  <div :style="previewContentStyle">
                    <div :style="preview.div">
                      <img
                        :src="preview.url || cropSourceUrl"
                        :style="preview.img"
                        alt="hero-portrait-preview"
                      />
                    </div>
                  </div>
                </div>
                <span v-else class="px-4 text-center text-xs text-slate-400"
                  >等待预览</span
                >
              </div>
            </div>
            <p class="text-xs text-slate-400">用于首页右侧人物图展示。</p>
          </div>
        </div>

        <div class="grid gap-3 sm:grid-cols-2 lg:grid-cols-1 xl:grid-cols-2">
          <el-button plain @click="handleZoom(1)">放大</el-button>
          <el-button plain @click="handleZoom(-1)">缩小</el-button>
          <el-button plain @click="handleRotate(-90)">左转 90°</el-button>
          <el-button plain @click="handleRotate(90)">右转 90°</el-button>
          <el-button plain @click="handleReset">重置</el-button>
        </div>
      </div>
    </div>

    <template #footer>
      <div class="flex flex-wrap items-center justify-between gap-3">
        <p class="text-sm text-slate-500">
          调整完成后确认生成裁剪图片；未调整时会直接沿用原图。
        </p>
        <div class="flex gap-3">
          <el-button @click="emit('update:visible', false)">取消</el-button>
          <el-button type="primary" :loading="exporting" @click="handleConfirm"
            >确认裁剪</el-button
          >
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import "vue-cropper/dist/index.css";
import { ElMessage } from "element-plus";
import { computed, nextTick, ref, watch } from "vue";
import { VueCropper } from "vue-cropper";
import { getAdminMediaContentUrl } from "@/api/modules/admin";
import { extractFileName } from "@/utils/media";
import type { CropConfirmPayload, CropMode } from "@/types/media";

/**
 * 图片裁剪器实例的最小类型声明。
 * 用于约束当前组件会访问到的 vue-cropper 内部方法与尺寸字段。
 */
interface CropperInstance {
  changeScale: (delta: number) => void;
  rotateLeft: () => void;
  rotateRight: () => void;
  getCropBlob: (callback: (blob: Blob | null) => void) => void;
}

interface CropPreviewData {
  url?: string;
  img?: Record<string, string | number>;
  div?: Record<string, string | number>;
  w?: number;
  h?: number;
}

/**
 * 图片裁剪弹窗组件。
 * 负责根据不同业务裁剪模式渲染裁剪器、预览面板以及导出确认结果。
 */
const props = defineProps<{
  visible: boolean;
  imageUrl?: string;
  mode: CropMode;
  sourceName?: string;
}>();

const emit = defineEmits<{
  (event: "update:visible", value: boolean): void;
  (event: "confirm", value: CropConfirmPayload): void;
}>();

/**
 * 裁剪器运行时状态。
 * 包括实例引用、导出状态、实时预览以及编辑行为标记。
 */
const cropperRef = ref<CropperInstance | null>(null);
const exporting = ref(false);
const cropperKey = ref(0);
const preview = ref<CropPreviewData>({});
const edited = ref(false);


/**
 * 根据裁剪模式返回弹窗标题、说明文案与固定裁剪比例。
 */
const config = computed(() => {
  if (props.mode === "avatar") {
    return {
      title: "裁剪头像",
      description: "左侧调整头像取景，右侧实时查看圆形头像效果。",
      fixedNumber: [1, 1],
      autoCropWidth: 260,
      autoCropHeight: 260,
    };
  }
  if (props.mode === "hero-background") {
    return {
      title: "裁剪横幅背景图",
      description: "左侧调整横幅背景取景，右侧实时查看首页首屏背景比例效果。",
      fixedNumber: [16, 9],
      autoCropWidth: 360,
      autoCropHeight: 202,
    };
  }
  return {
    title: "裁剪横幅人物图",
    description: "左侧调整人物图取景，右侧实时查看首页右侧人物卡片效果。",
    fixedNumber: [11, 14],
    autoCropWidth: 240,
    autoCropHeight: 305,
  };
});

/**
 * 将后台存储路径转换为可预览的媒体访问地址。
 */
const cropSourceUrl = computed(() => {
  const imageUrl = props.imageUrl?.trim();
  return imageUrl ? getAdminMediaContentUrl(imageUrl) : "";
});
const hasPreview = computed(() => !!preview.value.div && !!preview.value.img);
const previewViewportStyle = computed(() => ({
  width: "100%",
  height: "100%",
  overflow: "hidden",
}));
const previewContentStyle = computed(() => {
  const width = preview.value.w ?? 0;
  const height = preview.value.h ?? 0;
  if (!width || !height) {
    return {};
  }
  return {
    width: `${width}px`,
    height: `${height}px`,
    margin: "0 auto",
  };
});

/**
 * 监听弹窗显示状态与图片来源变化，重置裁剪器内部状态。
 */
watch(
  () => [props.visible, props.imageUrl, props.mode],
  async ([visible, imageUrl]) => {
    if (!visible || !imageUrl) {
      cropperRef.value = null;
      preview.value = {};
      edited.value = false;
      return;
    }
    preview.value = {};
    edited.value = false;
    cropperKey.value += 1;
    await nextTick();
  },
  { immediate: true }
);

/**
 * 同步 vue-cropper 实时输出的预览样式与尺寸数据。
 *
 * @param data 当前裁剪区域对应的预览结构。
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；仅更新本地预览状态。
 * @author Dyx
 */
function handleRealtime(data: CropPreviewData): void {
  preview.value = data;
}

/**
 * 手动将当前图片标记为已编辑。
 * 供滚轮缩放等交互复用。
 *
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；仅更新编辑标记。
 * @author Dyx
 */
function markEdited(): void {
  edited.value = true;
}

/**
 * 调整裁剪画面的缩放比例。
 *
 * @param delta 缩放增量，正数表示放大，负数表示缩小。
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；裁剪器未就绪时会安全跳过。
 * @author Dyx
 */
function handleZoom(delta: number): void {
  edited.value = true;
  cropperRef.value?.changeScale(delta);
}

/**
 * 按指定方向旋转裁剪画面。
 *
 * @param degree 旋转角度，正数向右旋转，负数向左旋转。
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；裁剪器未就绪时会安全跳过。
 * @author Dyx
 */
function handleRotate(degree: number): void {
  edited.value = true;
  if (degree > 0) {
    cropperRef.value?.rotateRight();
  } else {
    cropperRef.value?.rotateLeft();
  }
}

/**
 * 处理图片加载完成事件。
 *
 * @param status vue-cropper 返回的图片加载状态。
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常。
 * @author Dyx
 */
function handleImgLoad(status: string): void {
  if (status === "success") {
    // 图片加载成功，无需手动设置裁剪框尺寸
    // vue-cropper 会根据配置自动处理
  }
}

/**
 * 重置裁剪器与预览状态，并通过更新 key 强制重新创建裁剪实例。
 *
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；仅重置本地状态。
 * @author Dyx
 */
function handleReset(): void {
  cropperRef.value = null;
  preview.value = {};
  edited.value = false;
  cropperKey.value += 1;
}

/**
 * 确认当前裁剪结果，并根据是否真的编辑过决定复用原图还是导出新文件。
 *
 * @returns 返回异步处理结果；完成后会向父组件发出确认事件并关闭弹窗。
 * @throws 该函数不会主动向外抛出异常；导出失败时会通过页面提示反馈。
 * @author Dyx
 */
async function handleConfirm(): Promise<void> {
  if (exporting.value || !props.imageUrl) {
    return;
  }
  if (!edited.value) {
    emit("confirm", {
      edited: false,
      originalUrl: props.imageUrl,
    });
    emit("update:visible", false);
    return;
  }
  exporting.value = true;
  try {
    const blob = await getCropBlob();
    if (!blob) {
      ElMessage.warning("当前图片尚未完成加载，请调整后再试");
      return;
    }
    const outputName = `${removeFileExtension(
      props.sourceName || extractFileName(props.imageUrl) || "image"
    )}-cropped.png`;
    emit("confirm", {
      edited: true,
      file: new File([blob], outputName, { type: "image/png" }),
    });
    emit("update:visible", false);
  } finally {
    exporting.value = false;
  }
}

/**
 * 在弹窗关闭后清理裁剪器、预览与编辑状态，避免下次打开继承上次现场。
 *
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；仅重置本地状态。
 * @author Dyx
 */
function handleClosed(): void {
  cropperRef.value = null;
  preview.value = {};
  edited.value = false;
  cropperKey.value += 1;
}

/**
 * 从裁剪器实例中异步导出当前裁剪区域的 Blob 数据。
 *
 * @returns 返回当前裁剪结果的 Blob；当裁剪器未就绪时返回 null。
 * @throws 该函数不会主动抛出异常；导出结果通过 Promise resolve 返回。
 * @author Dyx
 */
function getCropBlob(): Promise<Blob | null> {
  return new Promise((resolve) => {
    const cropper = cropperRef.value;
    if (!cropper) {
      resolve(null);
      return;
    }
    cropper.getCropBlob((blob: Blob | null) => {
      resolve(blob);
    });
  });
}

/**
 * 移除文件名末尾的扩展名，用于生成裁剪后的输出文件名。
 *
 * @param fileName 原始文件名。
 * @returns 返回去除扩展名后的文件名主体。
 * @throws 该函数不会主动抛出异常；当文件名不包含扩展名时返回原值。
 * @author Dyx
 */
function removeFileExtension(fileName: string): string {
  return fileName.replace(/\.[^.]+$/, "");
}
</script>

<style scoped>
.preview-frame {
  display: block;
}

.preview-frame :deep(img) {
  display: block;
  max-width: none;
}
</style>
