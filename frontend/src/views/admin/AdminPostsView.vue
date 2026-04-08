<template>
  <section class="rounded-[28px] bg-white p-6 shadow-sm">
    <div class="mb-6 flex items-center justify-between gap-4">
      <div>
        <h2 class="text-xl font-semibold text-slate-900">博客管理</h2>
        <p class="mt-2 text-sm text-slate-500">集中维护文章标题、摘要、分类、发布时间与发布状态。</p>
      </div>
      <div class="flex items-center gap-3">
        <el-button type="danger" plain :disabled="!selectedIds.length" @click="handleBatchDelete">批量删除</el-button>
        <el-button type="primary" @click="openCreateDialog">新建文章</el-button>
      </div>
    </div>

    <el-table ref="tableRef" :data="posts" border @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="52" align="center" />
      <el-table-column prop="title" label="标题" min-width="220" />
      <el-table-column prop="category" label="分类" width="140" />
      <el-table-column prop="tags" label="标签" min-width="180" show-overflow-tooltip />
      <el-table-column prop="homeFeaturedText" label="首页第三屏" width="120" />
      <el-table-column prop="homeFeaturedOrder" label="首页排序" width="110" />
      <el-table-column prop="statusText" label="状态" width="120" />
      <el-table-column prop="publishedAt" label="发布时间" width="180" />
      <el-table-column prop="updatedAt" label="更新时间" width="180" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="openEditDialog(scope.row.raw)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(scope.row.raw)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑文章' : '新建文章'" width="960px" destroy-on-close>
      <el-form label-position="top">
        <el-form-item label="文章标题">
          <el-input v-model="form.title" maxlength="120" show-word-limit placeholder="请输入文章标题" />
        </el-form-item>
        <div class="grid gap-4 sm:grid-cols-2">
          <el-form-item label="分类">
            <el-input v-model="form.category" maxlength="60" show-word-limit placeholder="请输入文章分类" />
          </el-form-item>
          <el-form-item label="标签">
            <el-input v-model="form.tags" maxlength="120" show-word-limit placeholder="多个标签可用逗号分隔" />
          </el-form-item>
        </div>
        <el-form-item label="摘要">
          <el-input v-model="form.summary" type="textarea" :rows="3" maxlength="300" show-word-limit placeholder="请输入文章摘要" />
        </el-form-item>
        <el-form-item label="封面图">
          <AdminMediaPicker v-model="form.coverImage" button-text="选择文章封面" empty-text="暂未选择文章封面" />
        </el-form-item>
        <el-form-item label="正文内容">
          <AdminRichTextEditor
            v-if="dialogVisible"
            v-model="form.content"
            placeholder="请输入文章正文，支持标题、列表、引用、链接和图片 URL。"
          />
          <p class="mt-2 text-xs leading-6 text-slate-400">
            正文将以富文本 HTML 保存；后端会在入库前自动进行 XSS 白名单清洗。
          </p>
        </el-form-item>
        <div class="grid gap-4 sm:grid-cols-2">
          <el-form-item label="发布状态">
            <el-select v-model="form.published" class="!w-full">
              <el-option label="草稿" :value="0" />
              <el-option label="已发布" :value="1" />
            </el-select>
          </el-form-item>
          <el-form-item label="发布时间">
            <el-date-picker
              v-model="form.publishedAt"
              type="datetime"
              value-format="YYYY-MM-DD HH:mm:ss"
              placeholder="选择发布时间"
              class="!w-full"
            />
          </el-form-item>
        </div>
        <div class="mt-2 grid gap-4 sm:grid-cols-2">
          <el-form-item label="首页第三屏候选">
            <el-switch v-model="form.homeFeatured" :active-value="1" :inactive-value="0" />
          </el-form-item>
          <el-form-item label="首页排序">
            <el-input-number v-model="form.homeFeaturedOrder" :min="0" class="!w-full" />
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
import AdminRichTextEditor from '@/components/admin/AdminRichTextEditor.vue';
import { deleteAdminPost, deleteAdminPosts, getAdminPostDetail, getAdminPosts, saveAdminPost } from '@/api/modules/admin';
import type { PostData } from '@/api/modules/site';
import { resolveErrorMessage } from '@/utils/error';

/**
 * 后台文章管理页。
 * 负责展示文章列表，并提供文章的新建、编辑与删除流程。
 */
const rawList = ref<PostData[]>([]);
const dialogVisible = ref(false);
const saving = ref(false);
const selectedIds = ref<number[]>([]);
const tableRef = ref<{ clearSelection: () => void } | null>(null);

const form = reactive<Partial<PostData>>({
  id: undefined,
  title: '',
  summary: '',
  content: '',
  coverImage: '',
  category: '',
  tags: '',
  published: 1,
  publishedAt: '',
  homeFeatured: 0,
  homeFeaturedOrder: 0
});

/**
 * 将后台原始文章列表转换为表格展示所需的衍生字段。
 */
const posts = computed(() =>
  rawList.value.map((item) => ({
    ...item,
    statusText: item.published === 1 ? '已发布' : '草稿',
    homeFeaturedText: item.homeFeatured === 1 ? '首页精选' : '未入选',
    raw: item
  }))
);

function resetForm(): void {
  Object.assign(form, {
    id: undefined,
    title: '',
    summary: '',
    content: '',
    coverImage: '',
    category: '',
    tags: '',
    published: 1,
    publishedAt: '',
    homeFeatured: 0,
    homeFeaturedOrder: 0
  });
}

async function loadAdminPosts(): Promise<void> {
  const result = await getAdminPosts();
  const postsData = (result as { data?: PostData[] })?.data;
  rawList.value = Array.isArray(postsData) ? postsData : [];
}

function resetSelection(): void {
  tableRef.value?.clearSelection();
  selectedIds.value = [];
}

function handleSelectionChange(selection: Array<PostData & { raw?: PostData }>): void {
  selectedIds.value = selection.map((item) => Number(item.id)).filter((id) => !Number.isNaN(id));
}

function openCreateDialog(): void {
  resetForm();
  dialogVisible.value = true;
}

async function openEditDialog(item: PostData): Promise<void> {
  resetForm();
  dialogVisible.value = true;
  try {
    const result = await getAdminPostDetail(item.id);
    const detail = (result as { data?: PostData })?.data;
    Object.assign(form, detail || item, {
      publishedAt: (detail?.publishedAt || item.publishedAt) || ''
    });
  } catch (error) {
    dialogVisible.value = false;
    ElMessage.error(resolveErrorMessage(error, '文章详情加载失败'));
  }
}

async function handleSave(): Promise<void> {
  if (saving.value) {
    return;
  }
  saving.value = true;
  try {
    await saveAdminPost({
      ...form,
      publishedAt: form.publishedAt || undefined
    });
    ElMessage.success(form.id ? '文章更新成功' : '文章创建成功');
    dialogVisible.value = false;
    await loadAdminPosts();
  } catch (error) {
    ElMessage.error(resolveErrorMessage(error, '文章保存失败'));
  } finally {
    saving.value = false;
  }
}

async function handleDelete(item: PostData): Promise<void> {
  try {
    await ElMessageBox.confirm(`确认删除文章“${item.title}”吗？`, '删除确认', {
      type: 'warning'
    });
    await deleteAdminPost(item.id);
    ElMessage.success('文章删除成功');
    await loadAdminPosts();
    resetSelection();
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      return;
    }
    ElMessage.error('文章删除失败');
  }
}

async function handleBatchDelete(): Promise<void> {
  if (!selectedIds.value.length) {
    return;
  }
  try {
    await ElMessageBox.confirm(`确认删除选中的 ${selectedIds.value.length} 篇文章吗？`, '批量删除确认', {
      type: 'warning'
    });
    await deleteAdminPosts(selectedIds.value);
    ElMessage.success('文章批量删除成功');
    await loadAdminPosts();
    resetSelection();
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      return;
    }
    ElMessage.error(resolveErrorMessage(error, '文章批量删除失败'));
  }
}

onMounted(() => {
  void loadAdminPosts();
});
</script>
