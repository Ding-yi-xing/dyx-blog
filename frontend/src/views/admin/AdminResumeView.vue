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
        <el-button type="primary" @click="openCreateDialog">新建项目</el-button>
      </div>

      <el-table :data="projects" border>
        <el-table-column prop="name" label="项目名称" min-width="220" />
        <el-table-column prop="techStack" label="技术栈" min-width="180" />
        <el-table-column prop="roleName" label="角色" width="140" />
        <el-table-column prop="sortOrder" label="排序" width="100" />
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
  getAdminProfile,
  getAdminProjects,
  saveAdminProject,
  updateAdminProfile
} from '@/api/modules/admin';
import type { ProfileData, ProjectData } from '@/api/modules/site';

const savingProfile = ref(false);
const savingProject = ref(false);
const dialogVisible = ref(false);
const rawList = ref<ProjectData[]>([]);

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
  published: 1
});

const projects = computed(() =>
  rawList.value.map((item) => ({
    ...item,
    statusText: item.published === 1 ? '已发布' : '草稿',
    raw: item
  }))
);

async function loadProfile(): Promise<void> {
  const response = await getAdminProfile();
  Object.assign(form, response.data ?? {});
}

async function loadProjects(): Promise<void> {
  const response = await getAdminProjects();
  rawList.value = response.data ?? [];
}

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
    published: 1
  });
}

function openCreateDialog(): void {
  resetProjectForm();
  dialogVisible.value = true;
}

function openEditDialog(item: ProjectData): void {
  resetProjectForm();
  Object.assign(projectForm, item);
  dialogVisible.value = true;
}

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

async function handleDeleteProject(item: ProjectData): Promise<void> {
  try {
    await ElMessageBox.confirm(`确认删除项目“${item.name}”吗？`, '删除确认', {
      type: 'warning'
    });
    await deleteAdminProject(item.id);
    ElMessage.success('项目删除成功');
    await loadProjects();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('项目删除失败');
    }
  }
}

onMounted(() => {
  void loadProfile();
  void loadProjects();
});
</script>
