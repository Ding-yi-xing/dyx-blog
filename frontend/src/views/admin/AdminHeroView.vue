<template>
  <section class="rounded-[28px] bg-white p-6 shadow-sm">
    <div class="grid gap-6 xl:grid-cols-[1.2fr_0.8fr]">
      <article class="space-y-6">
        <div>
          <h2 class="text-xl font-semibold text-slate-900">首页横幅管理</h2>
          <p class="mt-2 text-sm leading-6 text-slate-500">
            拖拽调整首页横幅文案顺序，右侧图片区可直接从媒体库替换。
          </p>
        </div>

        <section class="rounded-[24px] border border-slate-200 bg-slate-50 p-5">
          <div class="flex flex-col gap-1 sm:flex-row sm:items-end sm:justify-between">
            <div>
              <h3 class="text-lg font-semibold text-slate-900">横幅内容编排</h3>
              <p class="text-sm leading-6 text-slate-500">拖动组件卡片即可调整首页横幅左侧文字顺序，图片区域单独维护。</p>
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
                    />
                    <el-input
                      v-else
                      v-model="block.text"
                      type="textarea"
                      :rows="4"
                      :placeholder="blockPlaceholderMap[block.type]"
                    />
                  </template>

                  <template v-else-if="isTagsBlock(block)">
                    <el-input
                      :model-value="formatTagItems(block.items)"
                      type="textarea"
                      :rows="4"
                      placeholder="每行一个标签，或用逗号分隔"
                      @update:model-value="handleTagItemsInput(block, $event)"
                    />
                    <p class="text-xs text-slate-400">会在首页显示为标签胶囊。</p>
                  </template>
                </div>
              </div>
            </div>

            <div class="rounded-[20px] border border-slate-200 bg-white p-4 shadow-sm">
              <div class="flex items-center justify-between gap-3">
                <div>
                  <p class="text-sm font-semibold text-slate-900">右侧图片</p>
                  <p class="text-xs text-slate-400">用于首页横幅右侧展示</p>
                </div>
              </div>

              <div class="mt-4 space-y-4" v-if="imageBlock">
                <div class="flex justify-center">
                  <div class="flex h-56 w-44 items-center justify-center overflow-hidden rounded-[28px] bg-slate-100">
                    <img v-if="imageBlock.imageUrl" :src="imageBlock.imageUrl" :alt="imageBlock.alt || 'hero-image'" class="h-full w-full object-cover" />
                    <span v-else class="px-4 text-center text-xs text-slate-400">暂未选择横幅图片</span>
                  </div>
                </div>
                <AdminMediaPicker
                  :model-value="imageBlock.imageUrl"
                  button-text="选择横幅图片"
                  empty-text="暂未选择横幅图片"
                  @update:model-value="updateImageBlockUrl"
                />
                <el-input v-model="imageBlock.alt" placeholder="图片说明（可选）" />
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
        <div class="mt-4 overflow-hidden rounded-[28px] bg-slate-950 px-6 py-8 text-white shadow-[0_26px_70px_rgba(15,23,42,0.18)]">
          <div class="grid gap-8 lg:grid-cols-[minmax(0,1fr)_minmax(0,1fr)] lg:items-stretch">
            <div class="space-y-5 lg:self-center lg:py-4">
              <template v-for="block in previewLeftBlocks" :key="block.id">
                <p
                  v-if="block.type === 'eyebrow'"
                  class="text-xs font-semibold uppercase tracking-[0.42em] text-slate-300 lg:text-sm"
                >
                  {{ block.text || 'HELLO THERE!' }}
                </p>
                <h3
                  v-else-if="block.type === 'title'"
                  class="text-4xl font-semibold leading-tight tracking-tight text-white lg:text-[3.25rem]"
                >
                  {{ block.text || '写代码的人，也写点文字。' }}
                </h3>
                <p v-else-if="block.type === 'subtitle'" class="max-w-2xl text-base leading-8 text-slate-300 lg:text-lg lg:leading-9">
                  {{ block.text || '这里有后端开发、安全研究、折腾小工具的记录，也有一些不那么严肃的碎碎念。' }}
                </p>
                <template v-else-if="isTagsBlock(block)">
                  <div class="flex flex-wrap gap-3 text-xs font-medium lg:text-sm">
                    <span
                      v-for="item in block.items"
                      :key="item"
                      class="inline-flex items-center rounded-full bg-slate-800/80 px-4 py-1.5 text-slate-100"
                    >
                      {{ item }}
                    </span>
                  </div>
                </template>
              </template>
            </div>

            <div class="mx-auto flex w-full items-stretch">
              <div class="flex h-[420px] w-full items-center justify-center overflow-hidden lg:h-full lg:min-h-[520px]">
                <img
                  v-if="previewImageBlock?.imageUrl"
                  :src="previewImageBlock.imageUrl"
                  :alt="previewImageBlock.alt || 'hero-image'"
                  class="h-full w-full object-cover"
                />
                <span v-else class="px-4 text-center text-xs text-slate-400">右侧横幅图片会显示在这里</span>
              </div>
            </div>
          </div>
        </div>
      </article>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus';
import { computed, onMounted, reactive, ref, watch } from 'vue';
import AdminMediaPicker from '@/views/admin/AdminMediaPicker.vue';
import { getAdminHeroProfile, updateAdminHeroProfile } from '@/api/modules/admin';
import {
  createDefaultHeroConfig,
  parseHeroConfig,
  type HeroBlock,
  type HeroImageBlock,
  type HeroTagsBlock,
  type HeroTextBlock,
  type ProfileData
} from '@/api/modules/site';

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

const saving = ref(false);
const draggingBlockId = ref<string | null>(null);
const heroBlocks = ref<HeroBlock[]>([]);
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

const leftHeroBlocks = computed(() => heroBlocks.value.filter((block): block is HeroTextBlock | HeroTagsBlock => block.type !== 'image'));
const imageBlock = computed(() => heroBlocks.value.find((block): block is HeroImageBlock => block.type === 'image'));
const previewHeroConfig = computed(() => parseHeroConfig(form));
const previewLeftBlocks = computed(() => previewHeroConfig.value.blocks.filter((block): block is HeroTextBlock | HeroTagsBlock => block.type !== 'image'));
const previewImageBlock = computed(() => previewHeroConfig.value.blocks.find((block): block is HeroImageBlock => block.type === 'image'));

watch(
  heroBlocks,
  () => {
    syncHeroConfigToForm();
  },
  { deep: true }
);

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
          column: 'right'
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

function rebuildHeroBlocks(nextLeftBlocks: Array<HeroTextBlock | HeroTagsBlock>): void {
  const nextBlocks = nextLeftBlocks.map((block) => cloneHeroBlock({ ...block, column: 'left' }));
  if (imageBlock.value) {
    nextBlocks.push(cloneHeroBlock({ ...imageBlock.value, column: 'right' }));
  }
  heroBlocks.value = nextBlocks;
}

function syncHeroConfigToForm(): void {
  const blocks = heroBlocks.value.map((block) => cloneHeroBlock(block));
  form.heroConfig = JSON.stringify({ version: 1, blocks });
  form.siteTitle = findTextBlockValue('eyebrow');
  form.heroTitle = findTextBlockValue('title');
  form.heroSubtitle = findTextBlockValue('subtitle');
}

function findTextBlockValue(type: HeroTextBlock['type']): string {
  const block = heroBlocks.value.find((item): item is HeroTextBlock => item.type === type);
  return block?.text || '';
}

function isTextBlock(block: HeroBlock): block is HeroTextBlock {
  return block.type === 'eyebrow' || block.type === 'title' || block.type === 'subtitle';
}

function isTagsBlock(block: HeroBlock): block is HeroTagsBlock {
  return block.type === 'tags';
}

function formatTagItems(items: string[]): string {
  return items.join('\n');
}

function handleTagItemsInput(block: HeroTagsBlock, value: string | number): void {
  updateTagItems(block, String(value ?? ''));
}

function updateTagItems(block: HeroTagsBlock, value: string): void {
  block.items = value
    .split(/\r?\n|,/)
    .map((item) => item.trim())
    .filter(Boolean);
}

function updateImageBlockUrl(value: string | string[] | undefined): void {
  if (!imageBlock.value) {
    return;
  }
  imageBlock.value.imageUrl = typeof value === 'string' ? value : '';
}

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

function handleBlockDragStart(blockId: string): void {
  draggingBlockId.value = blockId;
}

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

function handleDragEnd(): void {
  draggingBlockId.value = null;
}

function isFirstBlock(blockId: string): boolean {
  return leftHeroBlocks.value[0]?.id === blockId;
}

function isLastBlock(blockId: string): boolean {
  return leftHeroBlocks.value[leftHeroBlocks.value.length - 1]?.id === blockId;
}

async function loadProfile(): Promise<void> {
  try {
    const response = await getAdminHeroProfile();
    Object.assign(form, response.data ?? {});
    heroBlocks.value = normalizeHeroBlocks(form);
  } catch {
    ElMessage.error('首页横幅加载失败，请确认后端已重启');
  }
}

async function handleSave(): Promise<void> {
  if (saving.value) {
    return;
  }
  saving.value = true;
  try {
    syncHeroConfigToForm();
    const response = await updateAdminHeroProfile({
      id: form.id,
      siteTitle: form.siteTitle,
      heroTitle: form.heroTitle,
      heroSubtitle: form.heroSubtitle,
      heroConfig: form.heroConfig
    });
    Object.assign(form, response.data ?? {});
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
