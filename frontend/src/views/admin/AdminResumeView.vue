<template>
  <section class="space-y-6">
    <div class="grid gap-6 xl:grid-cols-[1fr_0.9fr]">
      <article class="rounded-[28px] bg-white p-6 shadow-sm">
        <h2 class="text-xl font-semibold text-slate-900">简历管理</h2>
        <el-form class="mt-6" label-position="top">
          <el-form-item label="PDF 简历">
            <div class="space-y-4">
              <AdminMediaPicker v-model="form.resumePdfUrl" button-text="选择 PDF 简历" empty-text="暂未选择 PDF 简历" />
              <a
                v-if="form.resumePdfUrl"
                :href="form.resumePdfUrl"
                target="_blank"
                rel="noreferrer"
                class="inline-flex text-sm font-medium text-slate-700 transition hover:text-slate-950"
              >
                预览当前简历附件
              </a>
            </div>
          </el-form-item>
          <el-form-item label="教育经历">
            <el-input v-model="form.educationExperience" type="textarea" :rows="6" placeholder="请输入教育经历内容" />
          </el-form-item>
          <el-form-item label="工作经历">
            <el-input v-model="form.workExperience" type="textarea" :rows="6" placeholder="请输入工作经历内容" />
          </el-form-item>
          <el-button type="primary" :loading="savingProfile" @click="handleSaveProfile">保存简历信息</el-button>
        </el-form>
      </article>

      <article class="rounded-[24px] border border-slate-200 bg-slate-50 p-6">
        <h3 class="text-lg font-semibold text-slate-900">附件状态</h3>
        <div class="mt-4 space-y-3 text-sm text-slate-600">
          <p>PDF：{{ form.resumePdfUrl || '未设置' }}</p>
          <p>项目数量：{{ projects.length }}</p>
        </div>
      </article>
    </div>

    <section class="rounded-[28px] bg-white p-6 shadow-sm">
      <div class="mb-6 flex items-center justify-between gap-4">
        <div>
          <h2 class="text-xl font-semibold text-slate-900">项目经历管理</h2>
        </div>
        <div class="flex items-center gap-3">
          <el-button type="danger" plain :disabled="!selectedIds.length" @click="handleBatchDeleteProjects">批量删除</el-button>
          <el-button type="primary" @click="openCreateDialog">新建项目</el-button>
        </div>
      </div>

      <el-table ref="tableRef" :data="projects" border @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="52" align="center" />
        <el-table-column prop="name" label="项目名称" min-width="220" />
        <el-table-column prop="techStack" label="技术栈" min-width="180" />
        <el-table-column prop="roleName" label="角色" width="140" />
        <el-table-column prop="sortOrder" label="排序" width="100" />
        <el-table-column prop="homeFeaturedText" label="首页第三屏" width="120" />
        <el-table-column prop="homeFeaturedOrder" label="首页排序" width="110" />
        <el-table-column prop="statusText" label="状态" width="120" />
        <el-table-column prop="updatedAt" label="更新时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="openEditDialog(scope.row.raw)">编辑</el-button>
            <el-button link type="danger" @click="handleDeleteProject(scope.row.raw)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-dialog v-model="dialogVisible" :title="projectForm.id ? '编辑项目' : '新建项目'" width="720px">
        <el-form label-position="top">
          <div class="grid gap-4 sm:grid-cols-2">
            <el-form-item label="项目名称">
              <el-input v-model="projectForm.name" placeholder="请输入项目名称" />
            </el-form-item>
            <el-form-item label="角色">
              <el-input v-model="projectForm.roleName" placeholder="请输入承担角色" />
            </el-form-item>
          </div>
          <el-form-item label="技术栈">
            <el-input v-model="projectForm.techStack" placeholder="请输入技术栈" />
          </el-form-item>
          <el-form-item label="项目链接">
            <el-input v-model="projectForm.projectLink" placeholder="请输入项目链接" />
          </el-form-item>
          <el-form-item label="封面图">
            <AdminMediaPicker v-model="projectForm.coverImage" button-text="选择项目封面" empty-text="暂未选择项目封面" />
          </el-form-item>
          <el-form-item label="项目描述">
            <el-input v-model="projectForm.description" type="textarea" :rows="5" placeholder="请输入项目描述" />
          </el-form-item>
          <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            <el-form-item label="排序">
              <el-input-number v-model="projectForm.sortOrder" :min="0" class="!w-full" />
            </el-form-item>
            <el-form-item label="发布状态">
              <el-select v-model="projectForm.published" class="!w-full">
                <el-option label="草稿" :value="0" />
                <el-option label="已发布" :value="1" />
              </el-select>
            </el-form-item>
            <el-form-item label="首页第三屏候选">
              <el-switch v-model="projectForm.homeFeatured" :active-value="1" :inactive-value="0" />
            </el-form-item>
            <el-form-item label="首页排序">
              <el-input-number v-model="projectForm.homeFeaturedOrder" :min="0" class="!w-full" />
            </el-form-item>
          </div>
        </el-form>
        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" :loading="savingProject" @click="handleSaveProject">保存</el-button>
        </template>
      </el-dialog>
    </section>
  </section>
</template>

<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus';
import { computed, onMounted, reactive, ref } from 'vue';
import AdminMediaPicker from '@/views/admin/AdminMediaPicker.vue';
import {
  deleteAdminProject,
  deleteAdminProjects,
  getAdminProfile,
  getAdminProjects,
  saveAdminProject,
  updateAdminProfile
} from '@/api/modules/admin';
import type { ProfileData, ProjectData } from '@/api/modules/site';

/**
 * 后台简历管理页。
 * 负责维护简历 PDF、教育与工作经历，同时复用项目经历管理能力维护简历页项目列表。
 */
const savingProfile = ref(false);
const savingProject = ref(false);
const dialogVisible = ref(false);
const rawList = ref<ProjectData[]>([]);
const selectedIds = ref<Array<string | number>>([]);
const tableRef = ref<{ clearSelection: () => void } | null>(null);

const form = reactive<ProfileData>({
  id: 1,
  siteTitle: '',
  heroTitle: '',
  heroSubtitle: '',
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

const projectForm = reactive<Partial<ProjectData>>({
  id: undefined,
  name: '',
  roleName: '',
  description: '',
  techStack: '',
  projectLink: '',
  coverImage: '',
  sortOrder: 0,
  published: 1,
  homeFeatured: 0,
  homeFeaturedOrder: 0
});

/**
 * 将后台项目列表转换为表格展示所需的衍生字段。
 */
const projects = computed(() =>
  rawList.value.map((item) => ({
    ...item,
    homeFeaturedText: item.homeFeatured === 1 ? '首页精选' : '未入选',
    statusText: item.published === 1 ? '已发布' : '草稿',
    raw: item
  }))
);

function resetSelection(): void {
  tableRef.value?.clearSelection();
  selectedIds.value = [];
}

function handleSelectionChange(selection: Array<ProjectData & { raw?: ProjectData }>): void {
  selectedIds.value = selection.map((item) => item.id).filter((id) => id !== undefined && id !== null);
}

/**
 * 获取简历主信息并回填到页面表单。
 *
 * @returns 返回异步加载结果；成功后会刷新 PDF、教育和工作经历字段。
 * @throws 该函数不会主动抛出同步异常；接口失败时会以 Promise reject 形式返回。
 * @author Dyx
 */
async function loadProfile(): Promise<void> {
  const result = await getAdminProfile();
  const profileData = (result as { data?: ProfileData })?.data ?? {};
  Object.assign(form, profileData);
}

/**
 * 获取项目经历列表并刷新表格数据源。
 *
 * @returns 返回异步加载结果；成功后会更新项目经历表格。
 * @throws 该函数不会主动抛出同步异常；接口失败时会以 Promise reject 形式返回。
 * @author Dyx
 */
async function loadProjects(): Promise<void> {
  const result = await getAdminProjects();
  const projectsData = (result as { data?: ProjectData[] })?.data;
  rawList.value = Array.isArray(projectsData) ? projectsData : [];
}

/**
 * 重置项目经历表单，供新建和编辑前统一复用。
 *
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；仅重置本地项目表单状态。
 * @author Dyx
 */
function resetProjectForm(): void {
  Object.assign(projectForm, {
    id: undefined,
    name: '',
    roleName: '',
    description: '',
    techStack: '',
    projectLink: '',
    coverImage: '',
    sortOrder: 0,
    published: 1,
    homeFeatured: 0,
    homeFeaturedOrder: 0
  });
}

/**
 * 打开新建项目弹窗，并初始化为空表单。
 *
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；仅重置项目表单并展示弹窗。
 * @author Dyx
 */
function openCreateDialog(): void {
  resetProjectForm();
  dialogVisible.value = true;
}

/**
 * 打开编辑项目弹窗，并将当前项目数据回填到表单中。
 *
 * @param item 待编辑的项目数据。
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；仅执行项目表单回填。
 * @author Dyx
 */
function openEditDialog(item: ProjectData): void {
  resetProjectForm();
  Object.assign(projectForm, item);
  dialogVisible.value = true;
}

/**
 * 保存简历主信息。
 *
 * @returns 返回异步保存结果；成功后会回填接口返回的最新个人资料数据。
 * @throws 该函数不会主动向外抛出异常；保存失败时加载状态会在 finally 中恢复。
 * @author Dyx
 */
async function handleSaveProfile(): Promise<void> {
  if (savingProfile.value) {
    return;
  }
  savingProfile.value = true;
  try {
    const response = await updateAdminProfile({ ...form });
    Object.assign(form, response.data ?? {});
    ElMessage.success('简历信息保存成功');
  } finally {
    savingProfile.value = false;
  }
}

/**
 * 保存当前项目经历表单。
 *
 * @returns 返回异步保存结果；成功后会刷新项目列表并关闭弹窗。
 * @throws 该函数不会主动向外抛出异常；保存失败时会通过页面提示反馈。
 * @author Dyx
 */
async function handleSaveProject(): Promise<void> {
  if (savingProject.value) {
    return;
  }
  savingProject.value = true;
  try {
    await saveAdminProject({ ...projectForm });
    ElMessage.success(projectForm.id ? '项目更新成功' : '项目创建成功');
    dialogVisible.value = false;
    await loadProjects();
  } catch (error) {
    ElMessage.error('项目保存失败');
  } finally {
    savingProject.value = false;
  }
}

/**
 * 删除指定项目经历，并在确认后刷新当前列表。
 *
 * @param item 待删除的项目数据。
 * @returns 返回异步删除结果。
 * @throws 该函数不会主动向外抛出异常；取消删除时会静默结束，失败时通过页面提示反馈。
 * @author Dyx
 */
async function handleDeleteProject(item: ProjectData): Promise<void> {
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

async function handleBatchDeleteProjects(): Promise<void> {
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
  void loadProfile();
  void loadProjects();
});
</script>
