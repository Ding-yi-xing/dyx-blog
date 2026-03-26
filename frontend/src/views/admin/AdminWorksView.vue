<template>
  <section class="space-y-6 rounded-[28px] bg-white p-6 shadow-sm">
    <div class="flex items-center justify-between gap-4">
      <div>
        <h2 class="text-xl font-semibold text-slate-900">作品管理</h2>
        <p class="mt-2 text-sm text-slate-500">维护关于我页面展示的个人作品、图集、视频与外部作品链接。</p>
      </div>
      <el-button type="primary" @click="openCreateDialog">新建作品</el-button>
    </div>

    <el-table :data="works" border>
      <el-table-column prop="title" label="作品标题" min-width="220" />
      <el-table-column prop="mediaTypeText" label="媒体类型" width="120" />
      <el-table-column prop="imageCount" label="图片数" width="100" />
      <el-table-column prop="hasLinkText" label="外链" width="100" />
      <el-table-column prop="sortOrder" label="排序" width="100" />
      <el-table-column prop="statusText" label="状态" width="120" />
      <el-table-column prop="updatedAt" label="更新时间" width="180" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="openEditDialog(scope.row.raw)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(scope.row.raw)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑作品' : '新建作品'" width="780px">
      <el-form label-position="top">
        <el-form-item label="作品标题">
          <el-input v-model="form.title" placeholder="请输入作品标题" />
        </el-form-item>
        <el-form-item label="作品简介">
          <el-input v-model="form.summary" type="textarea" :rows="4" placeholder="请输入作品简介" />
        </el-form-item>
        <el-form-item label="封面图">
          <AdminMediaPicker v-model="form.coverImage" button-text="选择封面图" empty-text="暂未选择作品封面" />
        </el-form-item>
        <el-form-item label="作品图集">
          <AdminMediaPicker
            v-model="selectedImageUrls"
            multiple
            button-text="选择多张图片"
            empty-text="暂未选择作品图片"
          />
        </el-form-item>
        <div class="grid gap-4 sm:grid-cols-2">
          <el-form-item label="视频地址">
            <AdminMediaPicker v-model="form.videoUrl" button-text="选择视频文件" empty-text="暂未选择视频文件" />
          </el-form-item>
          <el-form-item label="视频封面">
            <AdminMediaPicker v-model="form.videoPoster" button-text="选择视频封面" empty-text="暂未选择视频封面" />
          </el-form-item>
        </div>
        <el-form-item label="作品链接">
          <el-input v-model="form.workLink" placeholder="请输入作品链接" />
        </el-form-item>
        <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          <el-form-item label="排序">
            <el-input-number v-model="form.sortOrder" :min="0" class="!w-full" />
          </el-form-item>
          <el-form-item label="发布状态">
            <el-select v-model="form.published" class="!w-full">
              <el-option label="草稿" :value="0" />
              <el-option label="已发布" :value="1" />
            </el-select>
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus';
import { computed, onMounted, reactive, ref } from 'vue';
import { deleteAdminWork, getAdminWorks, saveAdminWork } from '@/api/modules/admin';
import type { WorkData } from '@/api/modules/site';
import { isImageUrl, parseImageUrls, stringifyImageUrls } from '@/utils/media';
import AdminMediaPicker from '@/views/admin/AdminMediaPicker.vue';

const rawList = ref<WorkData[]>([]);
const dialogVisible = ref(false);
const saving = ref(false);
const selectedImageUrls = ref<string[]>([]);

const form = reactive<Partial<WorkData>>({
  id: undefined,
  title: '',
  summary: '',
  coverImage: '',
  imageUrls: '',
  videoUrl: '',
  videoPoster: '',
  workLink: '',
  sortOrder: 0,
  published: 1
});

const works = computed(() =>
  rawList.value.map((item) => ({
    ...item,
    imageCount: resolveImageCount(item),
    hasLinkText: item.workLink ? '已配置' : '无',
    mediaTypeText: item.videoUrl ? '视频' : '图文',
    statusText: item.published === 1 ? '已发布' : '草稿',
    raw: item
  }))
);

function resolveImageCount(item: WorkData): number {
  return new Set([
    item.coverImage,
    item.videoPoster,
    ...parseImageUrls(item.imageUrls).filter((url) => isImageUrl(url))
  ].filter((url): url is string => !!url && isImageUrl(url))).size;
}

function resetForm(): void {
  Object.assign(form, {
    id: undefined,
    title: '',
    summary: '',
    coverImage: '',
    imageUrls: '',
    videoUrl: '',
    videoPoster: '',
    workLink: '',
    sortOrder: 0,
    published: 1
  });
  selectedImageUrls.value = [];
}

async function loadWorks(): Promise<void> {
  const response = await getAdminWorks();
  rawList.value = response.data ?? [];
}

function openCreateDialog(): void {
  resetForm();
  dialogVisible.value = true;
}

function openEditDialog(item: WorkData): void {
  resetForm();
  Object.assign(form, item);
  selectedImageUrls.value = parseImageUrls(item.imageUrls).filter((url) => isImageUrl(url));
  dialogVisible.value = true;
}

async function handleSave(): Promise<void> {
  if (saving.value) {
    return;
  }
  saving.value = true;
  try {
    await saveAdminWork({
      ...form,
      imageUrls: stringifyImageUrls(selectedImageUrls.value)
    });
    ElMessage.success(form.id ? '作品更新成功' : '作品创建成功');
    dialogVisible.value = false;
    await loadWorks();
  } catch (error) {
    ElMessage.error('作品保存失败');
  } finally {
    saving.value = false;
  }
}

async function handleDelete(item: WorkData): Promise<void> {
  try {
    await ElMessageBox.confirm(`确认删除作品“${item.title}”吗？`, '删除确认', {
      type: 'warning'
    });
    await deleteAdminWork(item.id);
    ElMessage.success('作品删除成功');
    await loadWorks();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('作品删除失败');
    }
  }
}

onMounted(() => {
  void loadWorks();
});
</script>
