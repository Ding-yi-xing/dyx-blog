<template>
  <section class="rounded-[28px] bg-white p-6 shadow-sm">
    <div class="mb-6 flex items-center justify-between gap-4">
      <div>
        <h2 class="text-xl font-semibold text-slate-900">照片管理</h2>
        <p class="mt-2 text-sm text-slate-500">维护个人照片集与展示顺序。</p>
      </div>
      <el-button type="primary" @click="openCreateDialog">新建照片</el-button>
    </div>

    <el-table :data="photos" border>
      <el-table-column prop="title" label="标题" min-width="220" />
      <el-table-column prop="imageUrl" label="图片地址" min-width="220" show-overflow-tooltip />
      <el-table-column prop="shotAt" label="拍摄时间" width="180" />
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑照片' : '新建照片'" width="720px">
      <el-form label-position="top">
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="请输入照片标题" />
        </el-form-item>
        <el-form-item label="图片地址">
          <el-input v-model="form.imageUrl" placeholder="请输入图片地址，可先从媒体资源页上传后复制" />
        </el-form-item>
        <el-form-item label="图片描述">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="请输入照片描述" />
        </el-form-item>
        <div class="grid gap-4 sm:grid-cols-3">
          <el-form-item label="拍摄时间">
            <el-date-picker v-model="form.shotAt" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择时间" class="!w-full" />
          </el-form-item>
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
import { deleteAdminPhoto, getAdminPhotos, saveAdminPhoto } from '@/api/modules/admin';
import type { PhotoData } from '@/api/modules/site';

const rawList = ref<PhotoData[]>([]);
const dialogVisible = ref(false);
const saving = ref(false);

const form = reactive<Partial<PhotoData>>({
  id: undefined,
  title: '',
  imageUrl: '',
  description: '',
  shotAt: '',
  sortOrder: 0,
  published: 1
});

const photos = computed(() =>
  rawList.value.map((item) => ({
    ...item,
    statusText: item.published === 1 ? '已发布' : '草稿',
    raw: item
  }))
);

function resetForm(): void {
  Object.assign(form, {
    id: undefined,
    title: '',
    imageUrl: '',
    description: '',
    shotAt: '',
    sortOrder: 0,
    published: 1
  });
}

/**
 * 获取后台照片列表。
 */
async function loadPhotos(): Promise<void> {
  const response = await getAdminPhotos();
  rawList.value = response.data ?? [];
}

/**
 * 打开新建弹窗。
 */
function openCreateDialog(): void {
  resetForm();
  dialogVisible.value = true;
}

/**
 * 打开编辑弹窗。
 * @param item 当前照片数据。
 */
function openEditDialog(item: PhotoData): void {
  resetForm();
  Object.assign(form, item);
  dialogVisible.value = true;
}

/**
 * 保存照片数据。
 */
async function handleSave(): Promise<void> {
  if (saving.value) {
    return;
  }
  saving.value = true;
  try {
    await saveAdminPhoto({ ...form });
    ElMessage.success(form.id ? '照片更新成功' : '照片创建成功');
    dialogVisible.value = false;
    await loadPhotos();
  } catch (error) {
    ElMessage.error('照片保存失败');
  } finally {
    saving.value = false;
  }
}

/**
 * 删除照片数据。
 * @param item 当前照片数据。
 */
async function handleDelete(item: PhotoData): Promise<void> {
  try {
    await ElMessageBox.confirm(`确认删除照片“${item.title}”吗？`, '删除确认', {
      type: 'warning'
    });
    await deleteAdminPhoto(item.id);
    ElMessage.success('照片删除成功');
    await loadPhotos();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('照片删除失败');
    }
  }
}

onMounted(() => {
  void loadPhotos();
});
</script>
