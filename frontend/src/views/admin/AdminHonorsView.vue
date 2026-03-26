<template>
  <section class="rounded-[28px] bg-white p-6 shadow-sm">
    <div class="mb-6 flex items-center justify-between gap-4">
      <div>
        <h2 class="text-xl font-semibold text-slate-900">荣誉管理</h2>
        <p class="mt-2 text-sm text-slate-500">维护荣誉时间、授予机构、说明、配图与证书附件。</p>
      </div>
      <el-button type="primary" @click="openCreateDialog">新建荣誉</el-button>
    </div>

    <el-table :data="honors" border>
      <el-table-column prop="title" label="荣誉名称" min-width="220" />
      <el-table-column prop="issuer" label="授予机构" min-width="180" />
      <el-table-column prop="awardAt" label="获得时间" width="180" />
      <el-table-column prop="imageCount" label="配图数" width="100" />
      <el-table-column prop="attachmentText" label="附件" width="110" />
      <el-table-column prop="statusText" label="状态" width="120" />
      <el-table-column prop="updatedAt" label="更新时间" width="180" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="openEditDialog(scope.row.raw)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(scope.row.raw)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑荣誉' : '新建荣誉'" width="760px">
      <el-form label-position="top">
        <div class="grid gap-4 sm:grid-cols-2">
          <el-form-item label="荣誉名称">
            <el-input v-model="form.title" placeholder="请输入荣誉名称" />
          </el-form-item>
          <el-form-item label="授予机构">
            <el-input v-model="form.issuer" placeholder="请输入授予机构" />
          </el-form-item>
        </div>
        <el-form-item label="获得时间">
          <el-date-picker
            v-model="form.awardAt"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="选择获得时间"
            class="!w-full"
          />
        </el-form-item>
        <el-form-item label="封面图">
          <AdminMediaPicker v-model="form.coverImage" button-text="选择封面图" empty-text="暂未选择荣誉封面" />
        </el-form-item>
        <el-form-item label="荣誉图集">
          <AdminMediaPicker
            v-model="selectedImageUrls"
            multiple
            button-text="选择多张图片"
            empty-text="暂未选择荣誉图片"
          />
        </el-form-item>
        <el-form-item label="证书附件 / PDF">
          <AdminMediaPicker v-model="form.attachmentUrl" button-text="选择附件文件" empty-text="暂未选择荣誉附件" />
        </el-form-item>
        <el-form-item label="荣誉说明">
          <el-input v-model="form.description" type="textarea" :rows="5" placeholder="请输入荣誉说明" />
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
import AdminMediaPicker from '@/views/admin/AdminMediaPicker.vue';
import { deleteAdminHonor, getAdminHonors, saveAdminHonor } from '@/api/modules/admin';
import type { HonorData } from '@/api/modules/site';
import { isImageUrl, parseImageUrls, stringifyImageUrls } from '@/utils/media';

const rawList = ref<HonorData[]>([]);
const dialogVisible = ref(false);
const saving = ref(false);
const selectedImageUrls = ref<string[]>([]);

const form = reactive<Partial<HonorData>>({
  id: undefined,
  title: '',
  issuer: '',
  description: '',
  coverImage: '',
  imageUrls: '',
  attachmentUrl: '',
  awardAt: '',
  sortOrder: 0,
  published: 1
});

const honors = computed(() =>
  rawList.value.map((item) => ({
    ...item,
    imageCount: resolveImageCount(item),
    attachmentText: item.attachmentUrl ? '已配置' : '无',
    statusText: item.published === 1 ? '已发布' : '草稿',
    raw: item
  }))
);

function resolveImageCount(item: HonorData): number {
  return new Set([item.coverImage, ...parseImageUrls(item.imageUrls)].filter((url): url is string => !!url && isImageUrl(url))).size;
}

function resetForm(): void {
  Object.assign(form, {
    id: undefined,
    title: '',
    issuer: '',
    description: '',
    coverImage: '',
    imageUrls: '',
    attachmentUrl: '',
    awardAt: '',
    sortOrder: 0,
    published: 1
  });
  selectedImageUrls.value = [];
}

async function loadHonors(): Promise<void> {
  const response = await getAdminHonors();
  rawList.value = response.data ?? [];
}

function openCreateDialog(): void {
  resetForm();
  dialogVisible.value = true;
}

function openEditDialog(item: HonorData): void {
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
    await saveAdminHonor({
      ...form,
      imageUrls: stringifyImageUrls(selectedImageUrls.value)
    });
    ElMessage.success(form.id ? '荣誉更新成功' : '荣誉创建成功');
    dialogVisible.value = false;
    await loadHonors();
  } catch (error) {
    ElMessage.error('荣誉保存失败');
  } finally {
    saving.value = false;
  }
}

async function handleDelete(item: HonorData): Promise<void> {
  try {
    await ElMessageBox.confirm(`确认删除荣誉“${item.title}”吗？`, '删除确认', {
      type: 'warning'
    });
    await deleteAdminHonor(item.id);
    ElMessage.success('荣誉删除成功');
    await loadHonors();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('荣誉删除失败');
    }
  }
}

onMounted(() => {
  void loadHonors();
});
</script>
