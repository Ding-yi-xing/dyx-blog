<template>
  <section class="rounded-[28px] bg-white p-6 shadow-sm">
    <div class="mb-6 flex items-center justify-between gap-4">
      <div>
        <h2 class="text-xl font-semibold text-slate-900">荣誉管理</h2>
        <p class="mt-2 text-sm text-slate-500">维护荣誉时间、授予机构、说明、配图与证书附件。</p>
      </div>
      <div class="flex items-center gap-3">
        <el-button type="danger" plain :disabled="!selectedIds.length" @click="handleBatchDelete">批量删除</el-button>
        <el-button type="primary" @click="openCreateDialog">新建荣誉</el-button>
      </div>
    </div>

    <el-table ref="tableRef" :data="honors" border @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="52" align="center" />
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
import { deleteAdminHonor, deleteAdminHonors, getAdminHonors, saveAdminHonor } from '@/api/modules/admin';
import type { HonorData } from '@/api/modules/site';
import { isImageUrl, parseImageUrls, stringifyImageUrls } from '@/utils/media';

/**
 * 后台荣誉管理页。
 * 负责展示荣誉列表，并提供荣誉的新建、编辑与删除流程。
 */
const rawList = ref<HonorData[]>([]);
const dialogVisible = ref(false);
const saving = ref(false);
const selectedImageUrls = ref<string[]>([]);
const selectedIds = ref<number[]>([]);
const tableRef = ref<{ clearSelection: () => void } | null>(null);

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

/**
 * 将后台原始荣誉列表转换为表格展示所需的衍生字段。
 */
const honors = computed(() =>
  rawList.value.map((item) => ({
    ...item,
    imageCount: resolveImageCount(item),
    attachmentText: item.attachmentUrl ? '已配置' : '无',
    statusText: item.published === 1 ? '已发布' : '草稿',
    raw: item
  }))
);

function resetSelection(): void {
  tableRef.value?.clearSelection();
  selectedIds.value = [];
}

function handleSelectionChange(selection: Array<HonorData & { raw?: HonorData }>): void {
  selectedIds.value = selection.map((item) => Number(item.id)).filter((id) => !Number.isNaN(id));
}

/**
 * 统计荣誉关联的有效图片数量。
 *
 * @param item 荣誉数据对象。
 * @returns 返回当前荣誉可展示的图片数量。
 * @throws 该函数不会主动抛出异常；无效地址会在过滤阶段被忽略。
 * @author Dyx
 */
function resolveImageCount(item: HonorData): number {
  return new Set([item.coverImage, ...parseImageUrls(item.imageUrls)].filter((url): url is string => !!url && isImageUrl(url))).size;
}

/**
 * 重置荣誉表单与已选图集，供新建和编辑前统一复用。
 *
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；仅重置本地表单状态。
 * @author Dyx
 */
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

/**
 * 获取后台荣誉列表并刷新表格数据源。
 *
 * @returns 返回异步加载结果；成功后会更新页面表格数据。
 * @throws 该函数不会主动抛出同步异常；接口失败时会以 Promise reject 形式返回。
 * @author Dyx
 */
async function loadHonors(): Promise<void> {
  const result = await getAdminHonors();
  const honorsData = (result as { data?: HonorData[] })?.data;
  rawList.value = Array.isArray(honorsData) ? honorsData : [];
}

/**
 * 打开新建荣誉弹窗，并初始化为空表单。
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
 * 打开编辑荣誉弹窗，并将当前荣誉数据回填到表单中。
 *
 * @param item 待编辑的荣誉数据。
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；图集地址会在回填前完成过滤。
 * @author Dyx
 */
function openEditDialog(item: HonorData): void {
  resetForm();
  Object.assign(form, item);
  selectedImageUrls.value = parseImageUrls(item.imageUrls).filter((url) => isImageUrl(url));
  dialogVisible.value = true;
}

/**
 * 保存当前荣誉表单。
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
    const isEditing = form.id !== undefined && form.id !== null;
    const payload: Partial<HonorData> = {
      ...form,
      id: isEditing ? form.id : undefined,
      imageUrls: stringifyImageUrls(selectedImageUrls.value)
    };
    if (!payload.awardAt) {
      delete payload.awardAt;
    }
    if (!isEditing) {
      delete payload.id;
    }
    await saveAdminHonor(payload);
    ElMessage.success(isEditing ? '荣誉更新成功' : '荣誉创建成功');
    dialogVisible.value = false;
    await loadHonors();
  } catch (error) {
    ElMessage.error('荣誉保存失败');
  } finally {
    saving.value = false;
  }
}

/**
 * 删除指定荣誉，并在用户确认后刷新当前列表。
 *
 * @param item 待删除的荣誉数据。
 * @returns 返回异步删除结果。
 * @throws 该函数不会主动向外抛出异常；取消删除时会静默结束，失败时通过页面提示反馈。
 * @author Dyx
 */
async function handleDelete(item: HonorData): Promise<void> {
  try {
    await ElMessageBox.confirm(`确认删除荣誉“${item.title}”吗？`, '删除确认', {
      type: 'warning'
    });
    await deleteAdminHonor(item.id);
    ElMessage.success('荣誉删除成功');
    await loadHonors();
    resetSelection();
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      return;
    }
    ElMessage.error('荣誉删除失败');
  }
}

async function handleBatchDelete(): Promise<void> {
  if (!selectedIds.value.length) {
    return;
  }
  try {
    await ElMessageBox.confirm(`确认删除选中的 ${selectedIds.value.length} 条荣誉吗？`, '批量删除确认', {
      type: 'warning'
    });
    await deleteAdminHonors(selectedIds.value);
    ElMessage.success('荣誉批量删除成功');
    await loadHonors();
    resetSelection();
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      return;
    }
    ElMessage.error('荣誉批量删除失败');
  }
}

onMounted(() => {
  void loadHonors();
});
</script>
