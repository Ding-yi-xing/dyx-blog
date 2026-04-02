<template>
  <section class="rounded-[28px] bg-white p-6 shadow-sm">
    <div class="mb-6 flex items-center justify-between gap-4">
      <div>
        <h2 class="text-xl font-semibold text-slate-900">项目经历管理</h2>
        <p class="mt-2 text-sm text-slate-500">维护项目名称、技术栈、角色与成果描述。</p>
      </div>
      <div class="flex items-center gap-3">
        <el-button type="danger" plain :disabled="!selectedIds.length" @click="handleBatchDelete">批量删除</el-button>
        <el-button type="primary" @click="openCreateDialog">新建项目</el-button>
      </div>
    </div>

    <el-table ref="tableRef" :data="projects" border @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="52" align="center" />
      <el-table-column prop="name" label="项目名称" min-width="220" />
      <el-table-column prop="techStack" label="技术栈" min-width="180" />
      <el-table-column prop="roleName" label="角色" width="140" />
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

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑项目' : '新建项目'" width="720px">
      <el-form label-position="top">
        <div class="grid gap-4 sm:grid-cols-2">
          <el-form-item label="项目名称">
            <el-input v-model="form.name" placeholder="请输入项目名称" />
          </el-form-item>
          <el-form-item label="角色">
            <el-input v-model="form.roleName" placeholder="请输入承担角色" />
          </el-form-item>
        </div>
        <el-form-item label="技术栈">
          <el-input v-model="form.techStack" placeholder="请输入技术栈" />
        </el-form-item>
        <el-form-item label="项目链接">
          <el-input v-model="form.projectLink" placeholder="请输入项目链接" />
        </el-form-item>
        <el-form-item label="封面图">
          <el-input v-model="form.coverImage" placeholder="请输入封面图片地址" />
        </el-form-item>
        <el-form-item label="项目描述">
          <el-input v-model="form.description" type="textarea" :rows="5" placeholder="请输入项目描述" />
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
import { deleteAdminProject, deleteAdminProjects, getAdminProjects, saveAdminProject } from '@/api/modules/admin';
import type { ProjectData } from '@/api/modules/site';

/**
 * 后台项目管理页。
 * 负责展示项目列表，并提供项目的新建、编辑与删除流程。
 */
const rawList = ref<ProjectData[]>([]);
const dialogVisible = ref(false);
const saving = ref(false);
const selectedIds = ref<number[]>([]);
const tableRef = ref<{ clearSelection: () => void } | null>(null);

const form = reactive<Partial<ProjectData>>({
  id: undefined,
  name: '',
  roleName: '',
  description: '',
  techStack: '',
  projectLink: '',
  coverImage: '',
  sortOrder: 0,
  published: 1
});

/**
 * 将后台原始项目列表转换为表格展示所需的衍生字段。
 */
const projects = computed(() =>
  rawList.value.map((item) => ({
    ...item,
    statusText: item.published === 1 ? '已发布' : '草稿',
    raw: item
  }))
);

function resetSelection(): void {
  tableRef.value?.clearSelection();
  selectedIds.value = [];
}

function handleSelectionChange(selection: Array<ProjectData & { raw?: ProjectData }>): void {
  selectedIds.value = selection.map((item) => Number(item.id)).filter((id) => !Number.isNaN(id));
}

/**
 * 重置项目表单，供新建与编辑前复用。
 *
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；仅重置本地表单状态。
 * @author Dyx
 */
function resetForm(): void {
  Object.assign(form, {
    id: undefined,
    name: '',
    roleName: '',
    description: '',
    techStack: '',
    projectLink: '',
    coverImage: '',
    sortOrder: 0,
    published: 1
  });
}

/**
 * 获取后台项目列表并刷新表格数据源。
 *
 * @returns 返回异步加载结果；成功后会更新页面表格数据。
 * @throws 该函数不会主动抛出同步异常；接口失败时会以 Promise reject 形式返回。
 * @author Dyx
 */
async function loadProjects(): Promise<void> {
  const result = await getAdminProjects();
  const projectsData = (result as { data?: ProjectData[] })?.data;
  rawList.value = Array.isArray(projectsData) ? projectsData : [];
}

/**
 * 打开新建项目弹窗，并初始化为空表单。
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
 * 打开编辑项目弹窗，并将当前项目数据回填到表单中。
 *
 * @param item 待编辑的项目数据。
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；仅执行表单回填。
 * @author Dyx
 */
function openEditDialog(item: ProjectData): void {
  resetForm();
  Object.assign(form, item);
  dialogVisible.value = true;
}

/**
 * 保存当前项目表单。
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
    await saveAdminProject({ ...form });
    ElMessage.success(form.id ? '项目更新成功' : '项目创建成功');
    dialogVisible.value = false;
    await loadProjects();
  } catch (error) {
    ElMessage.error('项目保存失败');
  } finally {
    saving.value = false;
  }
}

/**
 * 删除指定项目，并在用户确认后刷新当前列表。
 *
 * @param item 待删除的项目数据。
 * @returns 返回异步删除结果。
 * @throws 该函数不会主动向外抛出异常；取消删除时会静默结束，失败时通过页面提示反馈。
 * @author Dyx
 */
async function handleDelete(item: ProjectData): Promise<void> {
  try {
    await ElMessageBox.confirm(`确认删除项目“${item.name}”吗？`, '删除确认', {
      type: 'warning'
    });
    await deleteAdminProject(item.id);
    ElMessage.success('项目删除成功');
    await loadProjects();
    resetSelection();
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      return;
    }
    ElMessage.error('项目删除失败');
  }
}

async function handleBatchDelete(): Promise<void> {
  if (!selectedIds.value.length) {
    return;
  }
  try {
    await ElMessageBox.confirm(`确认删除选中的 ${selectedIds.value.length} 个项目吗？`, '批量删除确认', {
      type: 'warning'
    });
    await deleteAdminProjects(selectedIds.value);
    ElMessage.success('项目批量删除成功');
    await loadProjects();
    resetSelection();
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      return;
    }
    ElMessage.error('项目批量删除失败');
  }
}

onMounted(() => {
  void loadProjects();
});
</script>
