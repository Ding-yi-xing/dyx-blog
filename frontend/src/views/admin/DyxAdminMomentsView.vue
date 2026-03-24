<template>
  <section class="rounded-[28px] bg-white p-6 shadow-sm">
    <div class="mb-6 flex items-center justify-between gap-4">
      <div>
        <h2 class="text-xl font-semibold text-slate-900">动态管理</h2>
        <p class="mt-2 text-sm text-slate-500">维护首页与动态页展示的近期更新内容。</p>
      </div>
      <el-button type="primary" @click="openCreateDialog">新建动态</el-button>
    </div>

    <el-table :data="moments" border>
      <el-table-column prop="title" label="标题" min-width="220" />
      <el-table-column prop="happenedAt" label="时间" width="180" />
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑动态' : '新建动态'" width="680px">
      <el-form label-position="top">
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="请输入动态标题" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="5" placeholder="请输入动态内容" />
        </el-form-item>
        <el-form-item label="封面图">
          <el-input v-model="form.coverImage" placeholder="请输入封面图片地址" />
        </el-form-item>
        <div class="grid gap-4 sm:grid-cols-3">
          <el-form-item label="发生时间">
            <el-date-picker v-model="form.happenedAt" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择时间" class="!w-full" />
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
import { deleteAdminMoment, getAdminMoments, saveAdminMoment } from '@/api/modules/admin';
import type { MomentData } from '@/api/modules/site';

const rawList = ref<MomentData[]>([]);
const dialogVisible = ref(false);
const saving = ref(false);

const form = reactive<Partial<MomentData>>({
  id: undefined,
  title: '',
  content: '',
  coverImage: '',
  happenedAt: '',
  sortOrder: 0,
  published: 1
});

const moments = computed(() =>
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
    content: '',
    coverImage: '',
    happenedAt: '',
    sortOrder: 0,
    published: 1
  });
}

/**
 * 获取后台动态列表。
 */
async function loadMoments(): Promise<void> {
  const response = await getAdminMoments();
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
 * @param item 当前动态数据。
 */
function openEditDialog(item: MomentData): void {
  resetForm();
  Object.assign(form, item);
  dialogVisible.value = true;
}

/**
 * 保存动态数据。
 */
async function handleSave(): Promise<void> {
  if (saving.value) {
    return;
  }
  saving.value = true;
  try {
    await saveAdminMoment({ ...form });
    ElMessage.success(form.id ? '动态更新成功' : '动态创建成功');
    dialogVisible.value = false;
    await loadMoments();
  } catch (error) {
    ElMessage.error('动态保存失败');
  } finally {
    saving.value = false;
  }
}

/**
 * 删除动态数据。
 * @param item 当前动态数据。
 */
async function handleDelete(item: MomentData): Promise<void> {
  try {
    await ElMessageBox.confirm(`确认删除动态“${item.title}”吗？`, '删除确认', {
      type: 'warning'
    });
    await deleteAdminMoment(item.id);
    ElMessage.success('动态删除成功');
    await loadMoments();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('动态删除失败');
    }
  }
}

onMounted(() => {
  void loadMoments();
});
</script>
