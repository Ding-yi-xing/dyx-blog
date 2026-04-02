<template>
  <section class="space-y-6 rounded-[28px] bg-white p-6 shadow-sm">
    <div class="flex items-center justify-between gap-4">
      <div>
        <h2 class="text-xl font-semibold text-slate-900">作品管理</h2>
        <p class="mt-2 text-sm text-slate-500">
          维护关于我页面展示的个人作品、图集、视频与外部作品链接。
        </p>
      </div>
      <div class="flex items-center gap-3">
        <el-button type="danger" plain :disabled="!selectedIds.length" @click="handleBatchDelete">批量删除</el-button>
        <el-button type="primary" @click="openCreateDialog">新建作品</el-button>
      </div>
    </div>

    <el-table ref="tableRef" :data="works" border @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="52" align="center" />
      <el-table-column prop="title" label="作品标题" min-width="220" />
      <el-table-column prop="awardAt" label="获得时间" width="180" />
      <el-table-column prop="mediaTypeText" label="媒体类型" width="120" />
      <el-table-column prop="imageCount" label="图片数" width="100" />
      <el-table-column prop="hasLinkText" label="外链" width="100" />
      <el-table-column prop="sortOrder" label="排序" width="100" />
      <el-table-column prop="statusText" label="状态" width="120" />
      <el-table-column prop="updatedAt" label="更新时间" width="180" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="openEditDialog(scope.row.raw)"
            >编辑</el-button
          >
          <el-button link type="danger" @click="handleDelete(scope.row.raw)"
            >删除</el-button
          >
        </template>
      </el-table-column>
    </el-table>

    <el-dialog
      v-model="dialogVisible"
      :title="form.id ? '编辑作品' : '新建作品'"
      width="780px"
    >
      <el-form label-position="top">
        <el-form-item label="作品标题">
          <el-input v-model="form.title" placeholder="请输入作品标题" />
        </el-form-item>
        <el-form-item label="作品简介">
          <el-input
            v-model="form.summary"
            type="textarea"
            :rows="4"
            placeholder="请输入作品简介"
          />
        </el-form-item>
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
          <AdminMediaPicker
            v-model="form.coverImage"
            button-text="选择封面图"
            empty-text="暂未选择作品封面"
          />
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
            <AdminMediaPicker
              v-model="form.videoUrl"
              button-text="选择视频文件"
              empty-text="暂未选择视频文件"
            />
          </el-form-item>
          <el-form-item label="视频封面">
            <AdminMediaPicker
              v-model="form.videoPoster"
              button-text="选择视频封面"
              empty-text="暂未选择视频封面"
            />
          </el-form-item>
        </div>
        <el-form-item label="作品链接">
          <el-input v-model="form.workLink" placeholder="请输入作品链接" />
        </el-form-item>
        <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          <el-form-item label="排序">
            <el-input-number
              v-model="form.sortOrder"
              :min="0"
              class="!w-full"
            />
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
        <el-button type="primary" :loading="saving" @click="handleSave"
          >保存</el-button
        >
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { ElMessage, ElMessageBox } from "element-plus";
import { computed, onMounted, reactive, ref } from "vue";
import {
  deleteAdminWork,
  deleteAdminWorks,
  getAdminWorks,
  saveAdminWork,
} from "@/api/modules/admin";
import type { WorkData } from "@/api/modules/site";
import { isImageUrl, parseImageUrls, stringifyImageUrls } from "@/utils/media";
import AdminMediaPicker from "@/views/admin/AdminMediaPicker.vue";

/**
 * 后台作品管理页。
 * 负责加载作品列表，并提供作品的新建、编辑与删除能力。
 */
const rawList = ref<WorkData[]>([]);
const dialogVisible = ref(false);
const saving = ref(false);
const selectedImageUrls = ref<string[]>([]);
const selectedIds = ref<number[]>([]);
const tableRef = ref<{ clearSelection: () => void } | null>(null);

const form = reactive<Partial<WorkData>>({
  id: undefined,
  title: "",
  summary: "",
  coverImage: "",
  imageUrls: "",
  videoUrl: "",
  videoPoster: "",
  workLink: "",
  awardAt: "",
  sortOrder: 0,
  published: 1,
});

/**
 * 将后台原始作品列表转换为表格展示所需的衍生字段。
 */
const works = computed(() =>
  rawList.value.map((item) => ({
    ...item,
    imageCount: resolveImageCount(item),
    hasLinkText: item.workLink ? "已配置" : "无",
    mediaTypeText: item.videoUrl ? "视频" : "图文",
    statusText: item.published === 1 ? "已发布" : "草稿",
    raw: item,
  }))
);

function resetSelection(): void {
  tableRef.value?.clearSelection();
  selectedIds.value = [];
}

function handleSelectionChange(selection: Array<WorkData & { raw?: WorkData }>): void {
  selectedIds.value = selection.map((item) => Number(item.id)).filter((id) => !Number.isNaN(id));
}

/**
 * 统计作品关联的有效图片数量。
 * 会合并封面图、视频封面与图集中的图片地址，并自动去重。
 *
 * @param item 作品数据对象。
 * @returns 返回当前作品可展示的图片数量。
 * @throws 该函数不会主动抛出异常；无效地址会在过滤阶段被忽略。
 * @author Dyx
 */
function resolveImageCount(item: WorkData): number {
  return new Set(
    [
      item.coverImage,
      item.videoPoster,
      ...parseImageUrls(item.imageUrls).filter((url) => isImageUrl(url)),
    ].filter((url): url is string => !!url && isImageUrl(url))
  ).size;
}

/**
 * 重置作品表单与已选图集，供新建和编辑前统一复用。
 *
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；仅重置本地表单状态。
 * @author Dyx
 */
function resetForm(): void {
  Object.assign(form, {
    id: undefined,
    title: "",
    summary: "",
    coverImage: "",
    imageUrls: "",
    videoUrl: "",
    videoPoster: "",
    workLink: "",
    awardAt: "",
    sortOrder: 0,
    published: 1,
  });
  selectedImageUrls.value = [];
}

/**
 * 获取后台作品列表并刷新表格数据源。
 *
 * @returns 返回异步加载结果；成功后会更新页面表格数据。
 * @throws 该函数不会主动抛出同步异常；接口失败时会以 Promise reject 形式返回。
 * @author Dyx
 */
async function loadWorks(): Promise<void> {
  const result = await getAdminWorks();
  // 响应拦截器返回的是 Result 对象 {code, message, data}，需要提取 data 字段
  const worksData = (result as { data?: WorkData[] })?.data;
  rawList.value = Array.isArray(worksData) ? worksData : [];
}

/**
 * 打开新建作品弹窗，并初始化为空表单。
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
 * 打开编辑作品弹窗，并将当前作品数据回填到表单中。
 *
 * @param item 待编辑的作品数据。
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；图集地址会在回填前完成过滤。
 * @author Dyx
 */
function openEditDialog(item: WorkData): void {
  resetForm();
  Object.assign(form, item);
  selectedImageUrls.value = parseImageUrls(item.imageUrls).filter((url) =>
    isImageUrl(url)
  );
  dialogVisible.value = true;
}

/**
 * 保存当前作品表单。
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
    const payload: Partial<WorkData> = {
      ...form,
      imageUrls: stringifyImageUrls(selectedImageUrls.value),
    };
    if (!payload.awardAt) {
      delete payload.awardAt;
    }
    if (payload.id === undefined || payload.id === null) {
      delete payload.id;
    }
    await saveAdminWork(payload);
    ElMessage.success(form.id ? "作品更新成功" : "作品创建成功");
    dialogVisible.value = false;
    await loadWorks();
  } catch (error) {
    ElMessage.error("作品保存失败");
  } finally {
    saving.value = false;
  }
}

/**
 * 删除指定作品，并在用户确认后刷新当前列表。
 *
 * @param item 待删除的作品数据。
 * @returns 返回异步删除结果。
 * @throws 该函数不会主动向外抛出异常；取消删除时会静默结束，失败时通过页面提示反馈。
 * @author Dyx
 */
async function handleDelete(item: WorkData): Promise<void> {
  try {
    await ElMessageBox.confirm(`确认删除作品"${item.title}"吗？`, "删除确认", {
      type: "warning",
    });
    await deleteAdminWork(item.id);
    ElMessage.success("作品删除成功");
    await loadWorks();
    resetSelection();
  } catch (error) {
    if (error === "cancel" || error === "close") {
      return;
    }
    ElMessage.error("作品删除失败");
  }
}

async function handleBatchDelete(): Promise<void> {
  if (!selectedIds.value.length) {
    return;
  }
  try {
    await ElMessageBox.confirm(`确认删除选中的 ${selectedIds.value.length} 个作品吗？`, "批量删除确认", {
      type: "warning",
    });
    await deleteAdminWorks(selectedIds.value);
    ElMessage.success("作品批量删除成功");
    await loadWorks();
    resetSelection();
  } catch (error) {
    if (error === "cancel" || error === "close") {
      return;
    }
    ElMessage.error("作品批量删除失败");
  }
}

onMounted(() => {
  void loadWorks();
});
</script>
