<template>
  <section class="rounded-[28px] bg-white p-6 shadow-sm">
    <div class="grid gap-6 xl:grid-cols-[1.2fr_0.8fr]">
      <article class="space-y-6">
        <div>
          <h2 class="text-xl font-semibold text-slate-900">首页管理 / 首屏</h2>
          <p class="mt-2 text-sm leading-6 text-slate-500">
            拖拽调整首页首屏文案顺序，右侧图片区和背景图都可直接从媒体库替换。
          </p>
        </div>

        <section class="rounded-[24px] border border-slate-200 bg-slate-50 p-5">
          <div class="flex flex-col gap-1 sm:flex-row sm:items-end sm:justify-between">
            <div>
              <h3 class="text-lg font-semibold text-slate-900">首屏内容编排</h3>
              <p class="text-sm leading-6 text-slate-500">拖动组件卡片即可调整首页首屏左侧文字顺序，视觉素材区域单独维护。</p>
            </div>
          </div>

          <div class="mt-5 grid gap-6 xl:grid-cols-[1.1fr_0.9fr]">
            <div class="space-y-3">
              <div
                v-for="block in leftHeroBlocks"
                :key="block.id"
                draggable="true"
                class="rounded-[20px] border border-slate-200 bg-white p-4 transition"
                :class="draggingBlockId === block.id ? 'opacity-50' : 'shadow-sm'"
                @dragstart="handleBlockDragStart(block.id)"
                @dragover.prevent
                @drop="handleBlockDrop(block.id)"
                @dragend="handleDragEnd"
              >
                <div class="flex items-center justify-between gap-3">
                  <div>
                    <p class="text-sm font-semibold text-slate-900">{{ blockLabelMap[block.type] }}</p>
                    <p class="text-xs text-slate-400">拖拽排序</p>
                  </div>
                  <div class="flex items-center gap-2">
                    <el-button text size="small" :disabled="isFirstBlock(block.id)" @click="moveBlock(block.id, -1)">
                      上移
                    </el-button>
                    <el-button text size="small" :disabled="isLastBlock(block.id)" @click="moveBlock(block.id, 1)">
                      下移
                    </el-button>
                  </div>
                </div>

                <div class="mt-4 space-y-3">
                  <template v-if="isTextBlock(block)">
                    <el-input
                      v-if="block.type !== 'subtitle'"
                      v-model="block.text"
                      :placeholder="blockPlaceholderMap[block.type]"
                      :maxlength="textBlockMaxLengthMap[block.type]"
                      show-word-limit
                    />
                    <el-input
                      v-else
                      v-model="block.text"
                      type="textarea"
                      :rows="4"
                      :placeholder="blockPlaceholderMap[block.type]"
                      :maxlength="textBlockMaxLengthMap[block.type]"
                      show-word-limit
                    />
                  </template>

                  <template v-else-if="isTagsBlock(block)">
                    <el-input
                      :model-value="formatTagItems(block.items)"
                      type="textarea"
                      :rows="4"
                      maxlength="239"
                      show-word-limit
                      placeholder="每行一个标签，或用逗号分隔"
                      @update:model-value="handleTagItemsInput(block, $event)"
                    />
                    <p class="text-xs text-slate-400">最多 8 个标签，每个标签不超过 24 个字，会在首页显示为标签胶囊。</p>
                  </template>
                </div>
              </div>
            </div>

            <div class="rounded-[20px] border border-slate-200 bg-white p-4 shadow-sm">
              <div class="flex items-center justify-between gap-3">
                <div>
                  <p class="text-sm font-semibold text-slate-900">横幅视觉素材</p>
                  <p class="text-xs text-slate-400">分别配置背景图和右侧人物图，均可为空。</p>
                </div>
              </div>

              <div class="mt-4 space-y-5" v-if="imageBlock">
                <div class="space-y-3 rounded-[18px] border border-slate-200 bg-slate-50 p-4">
                  <div>
                    <p class="text-sm font-semibold text-slate-900">背景图</p>
                    <p class="text-xs text-slate-400">未设置时首页仍使用当前渐变背景。</p>
                  </div>
                  <div class="flex h-28 w-full items-center justify-center overflow-hidden rounded-[20px] bg-slate-100">
                    <img v-if="imageBlock.backgroundImageUrl" :src="imageBlock.backgroundImageUrl" alt="hero-background" class="h-full w-full object-cover" />
                    <span v-else class="px-4 text-center text-xs text-slate-400">暂未选择背景图</span>
                  </div>
                  <AdminMediaPicker
                    :model-value="imageBlock.backgroundImageUrl"
                    button-text="选择背景图"
                    empty-text="暂未选择背景图"
                    crop-mode="hero-background"
                    @update:model-value="handleBackgroundSelect"
                  />
                  <div class="flex flex-wrap gap-3">
                    <el-button plain :disabled="!imageBlock.backgroundImageUrl" @click="openBackgroundCropper">裁剪当前背景图</el-button>
                  </div>
                </div>

                <div class="space-y-3 rounded-[18px] border border-slate-200 bg-slate-50 p-4">
                  <div>
                    <p class="text-sm font-semibold text-slate-900">右侧人物图</p>
                    <p class="text-xs text-slate-400">未设置时首页右侧图片区会自动隐藏。</p>
                  </div>
                  <div class="flex justify-center">
                    <div class="flex h-56 w-44 items-center justify-center overflow-hidden rounded-[28px] bg-slate-100">
                      <img v-if="imageBlock.imageUrl" :src="imageBlock.imageUrl" :alt="imageBlock.alt || 'hero-image'" class="h-full w-full object-cover" />
                      <span v-else class="px-4 text-center text-xs text-slate-400">暂未选择右侧人物图</span>
                    </div>
                  </div>
                  <AdminMediaPicker
                    :model-value="imageBlock.imageUrl"
                    button-text="选择右侧人物图"
                    empty-text="暂未选择右侧人物图"
                    crop-mode="hero-portrait"
                    @update:model-value="handlePortraitSelect"
                  />
                  <div class="flex flex-wrap gap-3">
                    <el-button plain :disabled="!imageBlock.imageUrl" @click="openPortraitCropper">裁剪当前人物图</el-button>
                  </div>
                  <el-input v-model="imageBlock.alt" placeholder="图片说明（可选）" />
                </div>
              </div>
            </div>
          </div>
        </section>

        <div>
          <el-button type="primary" :loading="saving" @click="handleSave">保存横幅</el-button>
        </div>
      </article>

      <article class="rounded-[24px] border border-slate-200 bg-slate-50 p-6">
        <h3 class="text-lg font-semibold text-slate-900">横幅预览</h3>
        <div class="relative mt-4 overflow-hidden rounded-[28px] bg-slate-950 px-6 py-8 text-white shadow-[0_26px_70px_rgba(15,23,42,0.18)]">
          <div v-if="previewBackgroundStyle" class="home-hero-bg pointer-events-none absolute inset-0" :style="previewBackgroundStyle"></div>
          <div class="home-hero-copy relative grid gap-8" :class="previewGridClass">
            <div class="space-y-5 lg:self-center lg:py-4">
              <template v-for="block in previewLeftBlocks" :key="block.id">
                <p
                  v-if="block.type === 'eyebrow'"
                  class="line-clamp-2 max-w-full break-words text-xs font-semibold uppercase tracking-[0.42em] text-slate-300 lg:text-sm"
                  :title="block.text || 'HELLO THERE!'"
                >
                  {{ block.text || 'HELLO THERE!' }}
                </p>
                <h3
                  v-else-if="block.type === 'title'"
                  class="line-clamp-3 max-w-full break-words text-4xl font-semibold leading-tight tracking-tight text-white lg:text-[3.25rem]"
                  :title="block.text || '写代码的人，也写点文字。'"
                >
                  {{ block.text || '写代码的人，也写点文字。' }}
                </h3>
                <p v-else-if="block.type === 'subtitle'" class="line-clamp-4 max-w-2xl break-words text-base leading-8 text-slate-300 lg:text-lg lg:leading-9" :title="block.text || '这里有后端开发、安全研究、折腾小工具的记录，也有一些不那么严肃的碎碎念。'">
                  {{ block.text || '这里有后端开发、安全研究、折腾小工具的记录，也有一些不那么严肃的碎碎念。' }}
                </p>
                <template v-else-if="isTagsBlock(block)">
                  <div class="flex flex-wrap gap-3 text-xs font-medium lg:text-sm">
                    <span
                      v-for="item in block.items"
                      :key="item"
                      class="inline-flex max-w-full items-center truncate rounded-full bg-slate-800/80 px-4 py-1.5 text-slate-100"
                      :title="item"
                    >
                      {{ item }}
                    </span>
                  </div>
                </template>
              </template>
            </div>

            <div v-if="previewImageBlock?.imageUrl" class="mx-auto flex w-full items-stretch">
              <div class="flex h-[420px] w-full items-center justify-center overflow-hidden lg:h-full lg:min-h-[520px]">
                <img
                  :src="previewImageBlock.imageUrl"
                  :alt="previewImageBlock.alt || 'hero-image'"
                  class="h-full w-full object-cover opacity-95"
                />
              </div>
            </div>
          </div>
        </div>
      </article>
    </div>

    <BusinessImageCropper
      :visible="backgroundCropperVisible"
      :image-url="pendingBackgroundUrl"
      mode="hero-background"
      :source-name="pendingBackgroundName"
      @update:visible="handleBackgroundCropperVisibleChange"
      @confirm="handleBackgroundCropConfirm"
    />

    <BusinessImageCropper
      :visible="portraitCropperVisible"
      :image-url="pendingPortraitUrl"
      mode="hero-portrait"
      :source-name="pendingPortraitName"
      @update:visible="handlePortraitCropperVisibleChange"
      @confirm="handlePortraitCropConfirm"
    />
  </section>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus';
import { computed, onMounted, reactive, ref, watch } from 'vue';
import BusinessImageCropper from '@/components/admin/BusinessImageCropper.vue';
import AdminMediaPicker from '@/views/admin/AdminMediaPicker.vue';
import { getAdminHeroProfile, updateAdminHeroProfile, uploadAdminMedia } from '@/api/modules/admin';
import {
  createDefaultHeroConfig,
  parseHeroConfig,
  type HeroBlock,
  type HeroImageBlock,
  type HeroTagsBlock,
  type HeroTextBlock,
  type ProfileData
} from '@/api/modules/site';
import { extractFileName } from '@/utils/media';

interface CropConfirmPayload {
  edited: boolean;
  file?: File;
  originalUrl?: string;
}

/**
 * 后台首页首屏管理页。
 * 负责维护首屏文字区块顺序、标签组内容，以及背景图和人物图的裁剪上传流程。
 */
const contentBlockTypes = ['eyebrow', 'title', 'subtitle', 'tags'] as const;
const blockLabelMap: Record<HeroBlock['type'], string> = {
  eyebrow: '小标题',
  title: '主标题',
  subtitle: '副标题',
  tags: '标签组',
  image: '图片'
};
const blockPlaceholderMap: Record<'eyebrow' | 'title' | 'subtitle', string> = {
  eyebrow: '请输入小标题',
  title: '请输入主标题',
  subtitle: '请输入副标题'
};
const textBlockMaxLengthMap: Record<'eyebrow' | 'title' | 'subtitle', number> = {
  eyebrow: 48,
  title: 80,
  subtitle: 160
};
const TAG_ITEM_MAX_LENGTH = 24;
const TAG_ITEM_MAX_COUNT = 8;

const saving = ref(false);
const draggingBlockId = ref<string | null>(null);
const heroBlocks = ref<HeroBlock[]>([]);
const backgroundCropperVisible = ref(false);
const portraitCropperVisible = ref(false);
const pendingBackgroundUrl = ref('');
const pendingBackgroundName = ref('');
const pendingPortraitUrl = ref('');
const pendingPortraitName = ref('');
const form = reactive<ProfileData>({
  id: 1,
  siteTitle: '',
  heroTitle: '',
  heroSubtitle: '',
  heroConfig: '',
  aboutContent: '',
  educationExperience: '',
  workExperience: '',
  email: '',
  phone: '',
  wechat: '',
  githubUrl: '',
  avatarUrl: '',
  resumePdfUrl: ''
});

/**
 * 拆分出左侧可排序的文本与标签区块。
 */
const leftHeroBlocks = computed(() => heroBlocks.value.filter((block): block is HeroTextBlock | HeroTagsBlock => block.type !== 'image'));

/**
 * 获取首屏右侧图片区块配置。
 */
const imageBlock = computed(() => heroBlocks.value.find((block): block is HeroImageBlock => block.type === 'image'));

/**
 * 解析当前表单中的首屏配置，供右侧预览区域直接消费。
 */
const previewHeroConfig = computed(() => parseHeroConfig(form));
const previewLeftBlocks = computed(() => previewHeroConfig.value.blocks.filter((block): block is HeroTextBlock | HeroTagsBlock => block.type !== 'image'));
const previewImageBlock = computed(() => previewHeroConfig.value.blocks.find((block): block is HeroImageBlock => block.type === 'image'));

/**
 * 生成预览区域的背景图样式。
 */
const previewBackgroundStyle = computed(() => {
  const backgroundImageUrl = previewImageBlock.value?.backgroundImageUrl?.trim();
  if (!backgroundImageUrl) {
    return undefined;
  }
  return {
    backgroundImage: `url(${backgroundImageUrl})`
  };
});

/**
 * 根据是否存在人物图决定预览区域的栅格布局。
 */
const previewGridClass = computed(() => (previewImageBlock.value?.imageUrl ? 'lg:grid-cols-[minmax(0,1fr)_minmax(0,1fr)] lg:items-stretch' : 'lg:grid-cols-1'));

watch(
  heroBlocks,
  () => {
    syncHeroConfigToForm();
  },
  { deep: true }
);

/**
 * 深拷贝单个首屏区块，避免拖拽排序或预览编辑时污染原对象引用。
 *
 * @param block 待复制的首屏区块。
 * @returns 返回复制后的新区块对象。
 * @throws 该函数不会主动抛出异常；仅执行本地对象复制。
 * @author Dyx
 */
function cloneHeroBlock(block: HeroBlock): HeroBlock {
  if (block.type === 'image') {
    return { ...block };
  }
  if (block.type === 'tags') {
    return {
      ...block,
      items: [...block.items]
    };
  }
  return { ...block };
}

/**
 * 规范化首屏配置区块。
 * 会把解析结果与默认配置合并，确保缺失区块、重复区块和图片区块位置都能被修正。
 *
 * @param profile 个人资料与首屏配置数据。
 * @returns 返回可直接供页面编辑的标准化区块数组。
 * @throws 该函数不会主动抛出异常；异常数据会回退到默认配置。
 * @author Dyx
 */
function normalizeHeroBlocks(profile?: ProfileData): HeroBlock[] {
  const defaultBlocks = createDefaultHeroConfig(profile).blocks.map((block) => cloneHeroBlock(block));
  const parsedBlocks = parseHeroConfig(profile).blocks.map((block) => cloneHeroBlock(block));
  const defaultBlockMap = new Map(defaultBlocks.map((block) => [block.type, block]));
  const seenTypes = new Set<string>();
  const nextLeftBlocks: Array<HeroTextBlock | HeroTagsBlock> = [];
  let nextImageBlock: HeroImageBlock | undefined;

  for (const block of parsedBlocks) {
    if (block.type === 'image') {
      if (!nextImageBlock) {
        const fallback = defaultBlockMap.get('image') as HeroImageBlock;
        nextImageBlock = {
          ...fallback,
          ...block,
          column: 'right',
          backgroundImageUrl: typeof block.backgroundImageUrl === 'string' ? block.backgroundImageUrl : (fallback.backgroundImageUrl ?? '')
        };
      }
      continue;
    }

    if (seenTypes.has(block.type)) {
      continue;
    }
    seenTypes.add(block.type);

    const fallback = defaultBlockMap.get(block.type);
    if (!fallback || fallback.type === 'image') {
      continue;
    }

    if (block.type === 'tags') {
      nextLeftBlocks.push({
        ...(fallback as HeroTagsBlock),
        ...block,
        column: 'left',
        items: Array.isArray(block.items)
          ? block.items.map((item) => String(item).trim()).filter(Boolean)
          : [...(fallback as HeroTagsBlock).items]
      });
      continue;
    }

    nextLeftBlocks.push({
      ...(fallback as HeroTextBlock),
      ...block,
      column: 'left',
      text: typeof block.text === 'string' ? block.text : (fallback as HeroTextBlock).text
    });
  }

  for (const type of contentBlockTypes) {
    if (seenTypes.has(type)) {
      continue;
    }
    const fallback = defaultBlockMap.get(type);
    if (fallback && fallback.type !== 'image') {
      nextLeftBlocks.push({ ...(cloneHeroBlock(fallback) as HeroTextBlock | HeroTagsBlock), column: 'left' });
    }
  }

  if (!nextImageBlock) {
    const fallback = defaultBlockMap.get('image');
    if (fallback && fallback.type === 'image') {
      nextImageBlock = { ...(cloneHeroBlock(fallback) as HeroImageBlock), column: 'right' };
    }
  }

  return nextImageBlock ? [...nextLeftBlocks, nextImageBlock] : nextLeftBlocks;
}

/**
 * 按新的左侧区块顺序重建完整的首屏区块列表，并保留右侧图片区块。
 *
 * @param nextLeftBlocks 调整后的左侧文本与标签区块集合。
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；仅同步本地区块状态。
 * @author Dyx
 */
function rebuildHeroBlocks(nextLeftBlocks: Array<HeroTextBlock | HeroTagsBlock>): void {
  const nextBlocks = nextLeftBlocks.map((block) => cloneHeroBlock({ ...block, column: 'left' }));
  if (imageBlock.value) {
    nextBlocks.push(cloneHeroBlock({ ...imageBlock.value, column: 'right' }));
  }
  heroBlocks.value = nextBlocks;
}

/**
 * 把当前区块编辑结果同步回表单字段。
 * 同时兼容旧字段 heroTitle、heroSubtitle 与 siteTitle，便于后端统一保存。
 *
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；仅执行本地序列化。
 * @author Dyx
 */
function syncHeroConfigToForm(): void {
  const blocks = heroBlocks.value.map((block) => cloneHeroBlock(block));
  form.heroConfig = JSON.stringify({ version: 1, blocks });
  form.siteTitle = findTextBlockValue('eyebrow');
  form.heroTitle = findTextBlockValue('title');
  form.heroSubtitle = findTextBlockValue('subtitle');
}

/**
 * 读取指定文本区块的当前文案。
 *
 * @param type 文本区块类型。
 * @returns 返回对应区块文本；未命中时返回空字符串。
 * @throws 该函数不会主动抛出异常；仅执行本地查找。
 * @author Dyx
 */
function findTextBlockValue(type: HeroTextBlock['type']): string {
  const block = heroBlocks.value.find((item): item is HeroTextBlock => item.type === type);
  return block?.text || '';
}

/**
 * 判断当前区块是否为文本输入区块。
 */
function isTextBlock(block: HeroBlock): block is HeroTextBlock {
  return block.type === 'eyebrow' || block.type === 'title' || block.type === 'subtitle';
}

/**
 * 判断当前区块是否为标签组区块。
 */
function isTagsBlock(block: HeroBlock): block is HeroTagsBlock {
  return block.type === 'tags';
}

/**
 * 将标签数组格式化为多行文本，供文本域编辑。
 *
 * @param items 标签项数组。
 * @returns 返回换行连接后的标签文本。
 * @throws 该函数不会主动抛出异常；仅执行本地格式化。
 * @author Dyx
 */
function formatTagItems(items: string[]): string {
  return items.join('\n');
}

/**
 * 处理标签文本域输入，并同步更新标签组内容。
 *
 * @param block 待更新的标签区块。
 * @param value 输入框中的最新值。
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；仅执行字符串标准化。
 * @author Dyx
 */
function handleTagItemsInput(block: HeroTagsBlock, value: string | number): void {
  updateTagItems(block, String(value ?? ''));
}

/**
 * 将原始标签输入按换行或逗号切分，并限制数量和单项长度。
 *
 * @param block 待更新的标签区块。
 * @param value 原始标签输入文本。
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；仅执行本地清洗与裁剪。
 * @author Dyx
 */
function updateTagItems(block: HeroTagsBlock, value: string): void {
  block.items = value
    .split(/\r?\n|,/)
    .map((item) => item.trim())
    .filter(Boolean)
    .slice(0, TAG_ITEM_MAX_COUNT)
    .map((item) => item.slice(0, TAG_ITEM_MAX_LENGTH));
}

/**
 * 选择并准备裁剪横幅背景图。
 *
 * @param value 媒体选择器返回的背景图地址。
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；仅更新裁剪上下文或直接清空图片。
 * @author Dyx
 */
function handleBackgroundSelect(value: string | string[] | undefined): void {
  const nextUrl = typeof value === 'string' ? value.trim() : value?.[0]?.trim() || '';
  if (!imageBlock.value) {
    return;
  }
  imageBlock.value.backgroundImageUrl = nextUrl;
}

/**
 * 打开当前背景图的裁剪弹窗。
 *
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；无图片时会直接忽略。
 * @author Dyx
 */
function openBackgroundCropper(): void {
  const currentUrl = imageBlock.value?.backgroundImageUrl?.trim();
  if (!currentUrl) {
    return;
  }
  pendingBackgroundUrl.value = currentUrl;
  pendingBackgroundName.value = extractFileName(currentUrl);
  backgroundCropperVisible.value = true;
}

/**
 * 选择并准备裁剪首屏人物图。
 *
 * @param value 媒体选择器返回的人物图地址。
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；仅更新裁剪上下文或直接清空图片。
 * @author Dyx
 */
function handlePortraitSelect(value: string | string[] | undefined): void {
  const nextUrl = typeof value === 'string' ? value.trim() : value?.[0]?.trim() || '';
  if (!imageBlock.value) {
    return;
  }
  imageBlock.value.imageUrl = nextUrl;
}

/**
 * 打开当前人物图的裁剪弹窗。
 *
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；无图片时会直接忽略。
 * @author Dyx
 */
function openPortraitCropper(): void {
  const currentUrl = imageBlock.value?.imageUrl?.trim();
  if (!currentUrl) {
    return;
  }
  pendingPortraitUrl.value = currentUrl;
  pendingPortraitName.value = extractFileName(currentUrl);
  portraitCropperVisible.value = true;
}

/**
 * 同步背景图裁剪弹窗显隐状态，并在关闭时清理待处理上下文。
 *
 * @param value 裁剪弹窗新的显示状态。
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；仅更新本地状态。
 * @author Dyx
 */
function handleBackgroundCropperVisibleChange(value: boolean): void {
  backgroundCropperVisible.value = value;
  if (!value) {
    pendingBackgroundUrl.value = '';
    pendingBackgroundName.value = '';
  }
}

/**
 * 同步人物图裁剪弹窗显隐状态，并在关闭时清理待处理上下文。
 *
 * @param value 裁剪弹窗新的显示状态。
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；仅更新本地状态。
 * @author Dyx
 */
function handlePortraitCropperVisibleChange(value: boolean): void {
  portraitCropperVisible.value = value;
  if (!value) {
    pendingPortraitUrl.value = '';
    pendingPortraitName.value = '';
  }
}

/**
 * 处理背景图裁剪确认结果。
 * 未裁剪时直接使用原图，裁剪后则上传新文件并更新背景图地址。
 *
 * @param payload 裁剪组件返回的确认结果。
 * @returns 返回异步处理结果。
 * @throws 该函数不会主动向外抛出异常；上传失败时会通过页面提示反馈。
 * @author Dyx
 */
async function handleBackgroundCropConfirm(payload: CropConfirmPayload): Promise<void> {
  if (!imageBlock.value) {
    return;
  }
  if (!payload.edited) {
    imageBlock.value.backgroundImageUrl = payload.originalUrl ?? pendingBackgroundUrl.value ?? imageBlock.value.backgroundImageUrl;
    ElMessage.success('横幅背景图已更新');
    return;
  }
  if (!payload.file) {
    ElMessage.warning('未获取到裁剪后的背景图文件');
    return;
  }
  try {
    const result = await uploadAdminMedia(payload.file);
    imageBlock.value.backgroundImageUrl = (result as { data?: { fileUrl: string } })?.data?.fileUrl ?? imageBlock.value.backgroundImageUrl;
    ElMessage.success('横幅背景图裁剪并上传成功');
  } catch (error) {
    ElMessage.error('横幅背景图上传失败');
  }
}

/**
 * 处理人物图裁剪确认结果。
 * 未裁剪时直接使用原图，裁剪后则上传新文件并更新人物图地址。
 *
 * @param payload 裁剪组件返回的确认结果。
 * @returns 返回异步处理结果。
 * @throws 该函数不会主动向外抛出异常；上传失败时会通过页面提示反馈。
 * @author Dyx
 */
async function handlePortraitCropConfirm(payload: CropConfirmPayload): Promise<void> {
  if (!imageBlock.value) {
    return;
  }
  if (!payload.edited) {
    imageBlock.value.imageUrl = payload.originalUrl ?? pendingPortraitUrl.value ?? imageBlock.value.imageUrl;
    ElMessage.success('横幅人物图已更新');
    return;
  }
  if (!payload.file) {
    ElMessage.warning('未获取到裁剪后的人物图文件');
    return;
  }
  try {
    const result = await uploadAdminMedia(payload.file);
    imageBlock.value.imageUrl = (result as { data?: { fileUrl: string } })?.data?.fileUrl ?? imageBlock.value.imageUrl;
    ElMessage.success('横幅人物图裁剪并上传成功');
  } catch (error) {
    ElMessage.error('横幅人物图上传失败');
  }
}

/**
 * 按方向移动左侧区块顺序。
 *
 * @param blockId 待移动区块 ID。
 * @param direction 移动方向，-1 表示上移，1 表示下移。
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；越界移动会被直接忽略。
 * @author Dyx
 */
function moveBlock(blockId: string, direction: number): void {
  const nextLeftBlocks = leftHeroBlocks.value.map((block) => cloneHeroBlock(block) as HeroTextBlock | HeroTagsBlock);
  const currentIndex = nextLeftBlocks.findIndex((block) => block.id === blockId);
  const nextIndex = currentIndex + direction;
  if (currentIndex < 0 || nextIndex < 0 || nextIndex >= nextLeftBlocks.length) {
    return;
  }
  const [movedBlock] = nextLeftBlocks.splice(currentIndex, 1);
  nextLeftBlocks.splice(nextIndex, 0, movedBlock);
  rebuildHeroBlocks(nextLeftBlocks);
}

/**
 * 记录当前正在拖拽的区块 ID。
 *
 * @param blockId 正在拖拽的区块 ID。
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；仅更新本地拖拽状态。
 * @author Dyx
 */
function handleBlockDragStart(blockId: string): void {
  draggingBlockId.value = blockId;
}

/**
 * 处理区块拖拽放置，并按目标位置重排左侧区块顺序。
 *
 * @param targetBlockId 放置目标区块 ID。
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；非法拖拽目标会被直接忽略。
 * @author Dyx
 */
function handleBlockDrop(targetBlockId: string): void {
  const sourceBlockId = draggingBlockId.value;
  draggingBlockId.value = null;
  if (!sourceBlockId || sourceBlockId === targetBlockId) {
    return;
  }

  const nextLeftBlocks = leftHeroBlocks.value.map((block) => cloneHeroBlock(block) as HeroTextBlock | HeroTagsBlock);
  const sourceIndex = nextLeftBlocks.findIndex((block) => block.id === sourceBlockId);
  const targetIndex = nextLeftBlocks.findIndex((block) => block.id === targetBlockId);
  if (sourceIndex < 0 || targetIndex < 0) {
    return;
  }

  const [movedBlock] = nextLeftBlocks.splice(sourceIndex, 1);
  nextLeftBlocks.splice(targetIndex, 0, movedBlock);
  rebuildHeroBlocks(nextLeftBlocks);
}

/**
 * 拖拽结束时清理当前拖拽状态。
 *
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；仅重置本地状态。
 * @author Dyx
 */
function handleDragEnd(): void {
  draggingBlockId.value = null;
}

/**
 * 判断区块是否位于左侧列表首位。
 */
function isFirstBlock(blockId: string): boolean {
  return leftHeroBlocks.value[0]?.id === blockId;
}

/**
 * 判断区块是否位于左侧列表末位。
 */
function isLastBlock(blockId: string): boolean {
  return leftHeroBlocks.value[leftHeroBlocks.value.length - 1]?.id === blockId;
}

/**
 * 获取首页首屏配置并标准化为可编辑区块。
 *
 * @returns 返回异步加载结果；成功后会更新首屏表单与区块列表。
 * @throws 该函数不会主动向外抛出异常；失败时会通过页面提示反馈。
 * @author Dyx
 */
async function loadProfile(): Promise<void> {
  try {
    const result = await getAdminHeroProfile();
    const profileData = (result as { data?: ProfileData })?.data ?? {};
    Object.assign(form, profileData);
    heroBlocks.value = normalizeHeroBlocks(form);
  } catch {
    ElMessage.error('首页横幅加载失败，请确认后端已重启');
  }
}

/**
 * 保存当前首页首屏配置。
 * 提交前会先把区块结构同步回 heroConfig 与兼容字段，再请求后台保存。
 *
 * @returns 返回异步保存结果；成功后会回填最新首屏配置并刷新区块结构。
 * @throws 该函数不会主动向外抛出异常；保存失败时会通过页面提示反馈。
 * @author Dyx
 */
async function handleSave(): Promise<void> {
  if (saving.value) {
    return;
  }
  saving.value = true;
  try {
    syncHeroConfigToForm();
    const result = await updateAdminHeroProfile({
      id: form.id,
      siteTitle: form.siteTitle,
      heroTitle: form.heroTitle,
      heroSubtitle: form.heroSubtitle,
      heroConfig: form.heroConfig
    });
    const updatedProfile = (result as { data?: ProfileData })?.data ?? {};
    Object.assign(form, updatedProfile);
    heroBlocks.value = normalizeHeroBlocks(form);
    ElMessage.success('首页横幅保存成功');
  } catch {
    ElMessage.error('首页横幅保存失败，请确认后端已重启并使用最新代码');
  } finally {
    saving.value = false;
  }
}

onMounted(() => {
  void loadProfile();
});
</script>
