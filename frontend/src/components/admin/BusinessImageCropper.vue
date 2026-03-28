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
      <div class="overflow-hidden rounded-[24px] border border-slate-200 bg-slate-950/95 p-4">
        <div class="rounded-[20px] bg-slate-900 p-4">
          <div
            class="h-[460px] overflow-hidden rounded-[16px] bg-slate-950/90"
            @pointerdown.capture="handlePointerDown"
            @pointermove.capture="handlePointerMove"
            @pointerup.capture="handlePointerUp"
            @pointercancel.capture="handlePointerUp"
            @pointerleave.capture="handlePointerUp"
            @wheel.capture="markEdited"
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
              :fixed="true"
              :fixed-number="config.fixedNumber"
              :fixed-box="false"
              :can-scale="true"
              :can-move="true"
              :can-move-box="true"
              :center-box="true"
              :info="true"
              :full="true"
              :high="true"
              :original="true"
              :enlarge="1"
              mode="contain"
              class="h-full w-full"
              @realTime="handleRealtime"
            />
            <div v-else class="flex h-full items-center justify-center text-sm text-slate-400">暂无可裁剪图片</div>
          </div>
        </div>
      </div>

      <div class="space-y-4 rounded-[24px] border border-slate-200 bg-slate-50 p-5">
        <div>
          <p class="text-sm font-semibold text-slate-900">裁剪说明</p>
          <p class="mt-2 text-sm leading-6 text-slate-500">{{ config.description }}</p>
        </div>

        <div class="rounded-[18px] border border-slate-200 bg-white p-4">
          <p class="text-xs uppercase tracking-[0.22em] text-slate-400">业务预览</p>

          <div v-if="mode === 'avatar'" class="mt-4 flex flex-col items-center gap-3">
            <div class="flex h-28 w-28 items-center justify-center overflow-hidden rounded-full border border-slate-200 bg-slate-100 shadow-sm">
              <div v-if="hasPreview" :style="previewFrameStyle" class="preview-frame">
                <div :style="preview.div">
                  <img :src="preview.url || cropSourceUrl" :style="preview.img" alt="avatar-preview" />
                </div>
              </div>
              <span v-else class="text-xs text-slate-400">等待预览</span>
            </div>
            <p class="text-xs text-slate-400">头像将以圆形样式展示。</p>
          </div>

          <div v-else-if="mode === 'hero-background'" class="mt-4 space-y-3">
            <div class="overflow-hidden rounded-[20px] border border-slate-200 bg-slate-950 shadow-sm">
              <div class="relative h-32 w-full bg-slate-900">
                <div v-if="hasPreview" :style="previewFrameStyle" class="preview-frame absolute inset-0">
                  <div :style="preview.div">
                    <img :src="preview.url || cropSourceUrl" :style="preview.img" alt="hero-background-preview" />
                  </div>
                </div>
                <div class="absolute inset-0 bg-gradient-to-r from-slate-950/55 via-slate-950/20 to-transparent"></div>
                <div class="relative z-10 flex h-full flex-col justify-end px-4 py-4 text-white">
                  <p class="text-[11px] uppercase tracking-[0.28em] text-slate-200/90">HERO BACKGROUND</p>
                  <p class="mt-2 text-base font-semibold leading-6">阅读博客 / 查看关于我</p>
                </div>
              </div>
            </div>
            <p class="text-xs leading-5 text-slate-400">用于首页首屏背景，建议保留主体区域与文字安全区。</p>
          </div>

          <div v-else class="mt-4 flex flex-col items-center gap-3">
            <div class="overflow-hidden rounded-[28px] border border-slate-200 bg-slate-100 shadow-sm">
              <div class="flex h-56 w-44 items-center justify-center overflow-hidden bg-slate-100">
                <div v-if="hasPreview" :style="previewFrameStyle" class="preview-frame">
                  <div :style="preview.div">
                    <img :src="preview.url || cropSourceUrl" :style="preview.img" alt="hero-portrait-preview" />
                  </div>
                </div>
                <span v-else class="px-4 text-center text-xs text-slate-400">等待预览</span>
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
        <p class="text-sm text-slate-500">未调整裁剪区域时会直接使用原图，只有实际编辑后才重新上传。</p>
        <div class="flex gap-3">
          <el-button @click="emit('update:visible', false)">取消</el-button>
          <el-button type="primary" :loading="exporting" @click="handleConfirm">确认裁剪并上传</el-button>
        </div>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import 'vue-cropper/dist/index.css';
import { ElMessage } from 'element-plus';
import { computed, nextTick, ref, watch } from 'vue';
import { VueCropper } from 'vue-cropper';
import { getAdminMediaContentUrl } from '@/api/modules/admin';
import { extractFileName } from '@/utils/media';

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

interface CropConfirmPayload {
  edited: boolean;
  file?: File;
  originalUrl?: string;
}

type CropMode = 'avatar' | 'hero-background' | 'hero-portrait';

const props = defineProps<{
  visible: boolean;
  imageUrl?: string;
  mode: CropMode;
  sourceName?: string;
}>();

const emit = defineEmits<{
  (event: 'update:visible', value: boolean): void;
  (event: 'confirm', value: CropConfirmPayload): void;
}>();

const cropperRef = ref<CropperInstance | null>(null);
const exporting = ref(false);
const cropperKey = ref(0);
const preview = ref<CropPreviewData>({});
const edited = ref(false);
const pointerStart = ref<{ x: number; y: number } | null>(null);

const config = computed(() => {
  if (props.mode === 'avatar') {
    return {
      title: '裁剪头像',
      description: '左侧调整头像取景，右侧实时查看圆形头像效果。',
      fixedNumber: [1, 1],
      autoCropWidth: 260,
      autoCropHeight: 260
    };
  }
  if (props.mode === 'hero-background') {
    return {
      title: '裁剪横幅背景图',
      description: '左侧调整横幅背景取景，右侧实时查看首页首屏背景比例效果。',
      fixedNumber: [16, 9],
      autoCropWidth: 360,
      autoCropHeight: 202
    };
  }
  return {
    title: '裁剪横幅人物图',
    description: '左侧调整人物图取景，右侧实时查看首页右侧人物卡片效果。',
    fixedNumber: [11, 14],
    autoCropWidth: 240,
    autoCropHeight: 305
  };
});

const cropSourceUrl = computed(() => {
  const imageUrl = props.imageUrl?.trim();
  return imageUrl ? getAdminMediaContentUrl(imageUrl) : '';
});
const hasPreview = computed(() => !!preview.value.div && !!preview.value.img);
const previewFrameStyle = computed(() => ({
  width: `${preview.value.w ?? 0}px`,
  height: `${preview.value.h ?? 0}px`,
  overflow: 'hidden'
}));

watch(
  () => [props.visible, props.imageUrl, props.mode],
  async ([visible, imageUrl]) => {
    if (!visible || !imageUrl) {
      cropperRef.value = null;
      preview.value = {};
      edited.value = false;
      pointerStart.value = null;
      return;
    }
    preview.value = {};
    edited.value = false;
    pointerStart.value = null;
    cropperKey.value += 1;
    await nextTick();
  },
  { immediate: true }
);

function handleRealtime(data: CropPreviewData): void {
  preview.value = data;
}

function handlePointerDown(event: PointerEvent): void {
  pointerStart.value = { x: event.clientX, y: event.clientY };
}

function handlePointerMove(event: PointerEvent): void {
  if (!pointerStart.value || edited.value) {
    return;
  }
  const distanceX = Math.abs(event.clientX - pointerStart.value.x);
  const distanceY = Math.abs(event.clientY - pointerStart.value.y);
  if (distanceX > 3 || distanceY > 3) {
    edited.value = true;
  }
}

function handlePointerUp(): void {
  pointerStart.value = null;
}

function markEdited(): void {
  edited.value = true;
}

function handleZoom(delta: number): void {
  edited.value = true;
  cropperRef.value?.changeScale(delta);
}

function handleRotate(angle: number): void {
  edited.value = true;
  if (angle > 0) {
    cropperRef.value?.rotateRight();
    return;
  }
  cropperRef.value?.rotateLeft();
}

function handleReset(): void {
  cropperRef.value = null;
  preview.value = {};
  edited.value = false;
  pointerStart.value = null;
  cropperKey.value += 1;
}

async function handleConfirm(): Promise<void> {
  if (exporting.value || !props.imageUrl) {
    return;
  }
  if (!edited.value) {
    emit('confirm', {
      edited: false,
      originalUrl: props.imageUrl
    });
    emit('update:visible', false);
    return;
  }
  exporting.value = true;
  try {
    const blob = await getCropBlob();
    if (!blob) {
      ElMessage.warning('当前图片尚未完成加载，请调整后再试');
      return;
    }
    const outputName = `${removeFileExtension(props.sourceName || extractFileName(props.imageUrl) || 'image')}-cropped.png`;
    emit('confirm', {
      edited: true,
      file: new File([blob], outputName, { type: 'image/png' })
    });
    emit('update:visible', false);
  } finally {
    exporting.value = false;
  }
}

function handleClosed(): void {
  cropperRef.value = null;
  preview.value = {};
  edited.value = false;
  pointerStart.value = null;
  cropperKey.value += 1;
}

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

function removeFileExtension(fileName: string): string {
  return fileName.replace(/\.[^.]+$/, '');
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
