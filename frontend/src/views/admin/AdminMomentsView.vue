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
      <el-table-column prop="mediaCount" label="媒体数" width="100" />
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑动态' : '新建动态'" width="760px">
      <el-form label-position="top">
        <el-form-item label="标题">
          <el-input v-model="form.title" placeholder="请输入动态标题" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="form.content" type="textarea" :rows="5" placeholder="请输入动态内容" />
        </el-form-item>
        <el-form-item label="封面媒体">
          <AdminMediaPicker v-model="form.coverImage" button-text="选择封面媒体" empty-text="暂未选择动态封面媒体" />
        </el-form-item>
        <el-form-item label="动态媒体">
          <AdminMediaPicker
            v-model="selectedMediaUrls"
            multiple
            button-text="选择多项媒体"
            empty-text="暂未选择动态媒体"
          />
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
import AdminMediaPicker from '@/views/admin/AdminMediaPicker.vue';
import { deleteAdminMoment, getAdminMoments, saveAdminMoment } from '@/api/modules/admin';
import type { MomentData } from '@/api/modules/site';
import { parseImageUrls, stringifyImageUrls } from '@/utils/media';

/**
 * 后台动态管理页。
 * 负责展示动态列表，并提供动态的新建、编辑与删除流程。
 */
const rawList = ref<MomentData[]>([]);
const dialogVisible = ref(false);
const saving = ref(false);
const selectedMediaUrls = ref<string[]>([]);

const form = reactive<Partial<MomentData>>({
  id: undefined,
  title: '',
  content: '',
  coverImage: '',
  imageUrls: '',
  happenedAt: '',
  sortOrder: 0,
  published: 1
});

/**
 * 将后台原始动态列表转换为表格展示所需的衍生字段。
 */
const moments = computed(() =>
  rawList.value.map((item) => ({
    ...item,
    mediaCount: [item.coverImage, ...parseImageUrls(item.imageUrls)].filter((url, index, list): url is string => !!url && list.indexOf(url) === index).length,
    statusText: item.published === 1 ? '已发布' : '草稿',
    raw: item
  }))
);

/**
 * 重置动态表单与已选媒体列表，供新建和编辑前统一复用。
 *
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；仅重置本地表单状态。
 * @author Dyx
 */
function resetForm(): void {
  Object.assign(form, {
    id: undefined,
    title: '',
    content: '',
    coverImage: '',
    imageUrls: '',
    happenedAt: '',
    sortOrder: 0,
    published: 1
  });
  selectedMediaUrls.value = [];
}

/**
 * 获取后台动态列表并刷新表格数据源。
 *
 * @returns 返回异步加载结果；成功后会更新页面表格数据。
 * @throws 该函数不会主动抛出同步异常；接口失败时会以 Promise reject 形式返回。
 * @author Dyx
 */
async function loadMoments(): Promise<void> {
  const response = await getAdminMoments();
  rawList.value = response.data ?? [];
}

/**
 * 打开新建动态弹窗，并初始化为空表单。
 *
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；仅重置表单并展示弹窗。
 * @author Dyx
 */
function openCreateDialog(): void {
  resetForm();
  dialogVisible.value = true;
}

/**
 * 打开编辑动态弹窗，并将当前动态数据回填到表单中。
 *
 * @param item 待编辑的动态数据。
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；仅执行表单回填。
 * @author Dyx
 */
function openEditDialog(item: MomentData): void {
  resetForm();
  Object.assign(form, item);
  selectedMediaUrls.value = parseImageUrls(item.imageUrls);
  dialogVisible.value = true;
}

/**
 * 保存当前动态表单。
 * 新建与编辑共用同一套提交逻辑，成功后会刷新列表并关闭弹窗。
 *
 * @returns 返回异步保存结果。
 * @throws 该函数不会主动向外抛出异常；保存失败时会通过页面提示反馈。
 * @author Dyx
 */
async function handleSave(): Promise<void> {
  if (saving.value) {
    return;
  }
  saving.value = true;
  try {
    await saveAdminMoment({
      ...form,
      imageUrls: stringifyImageUrls(selectedMediaUrls.value)
    });
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
 * 删除指定动态，并在用户确认后刷新当前列表。
 *
 * @param item 待删除的动态数据。
 * @returns 返回异步删除结果。
 * @throws 该函数不会主动向外抛出异常；取消删除时会静默结束，失败时通过页面提示反馈。
 * @author Dyx
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
