<template>
  <section class="rounded-[28px] bg-white p-6 shadow-sm">
    <div class="mb-6 flex items-center justify-between gap-4">
      <div>
        <h2 class="text-xl font-semibold text-slate-900">博客管理</h2>
        <p class="mt-2 text-sm text-slate-500">集中维护文章标题、摘要、分类与发布状态。</p>
      </div>
      <el-button type="primary" @click="openCreateDialog">新建文章</el-button>
    </div>

    <el-table :data="posts" border>
      <el-table-column prop="title" label="标题" min-width="220" />
      <el-table-column prop="category" label="分类" width="140" />
      <el-table-column prop="tags" label="标签" min-width="180" show-overflow-tooltip />
      <el-table-column prop="statusText" label="状态" width="120" />
      <el-table-column prop="updatedAt" label="更新时间" width="180" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="openEditDialog(scope.row.raw)">编辑</el-button>
          <el-button link type="danger" @click="handleDelete(scope.row.raw)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑文章' : '新建文章'" width="760px">
      <el-form label-position="top">
        <el-form-item label="文章标题">
          <el-input v-model="form.title" placeholder="请输入文章标题" />
        </el-form-item>
        <div class="grid gap-4 sm:grid-cols-2">
          <el-form-item label="分类">
            <el-input v-model="form.category" placeholder="请输入文章分类" />
          </el-form-item>
          <el-form-item label="标签">
            <el-input v-model="form.tags" placeholder="多个标签可用逗号分隔" />
          </el-form-item>
        </div>
        <el-form-item label="摘要">
          <el-input v-model="form.summary" type="textarea" :rows="3" placeholder="请输入文章摘要" />
        </el-form-item>
        <el-form-item label="封面图">
          <el-input v-model="form.coverImage" placeholder="请输入封面图片地址" />
        </el-form-item>
        <el-form-item label="正文内容">
          <el-input v-model="form.content" type="textarea" :rows="8" placeholder="请输入文章正文" />
        </el-form-item>
        <el-form-item label="发布状态">
          <el-select v-model="form.published" class="!w-full">
            <el-option label="草稿" :value="0" />
            <el-option label="已发布" :value="1" />
          </el-select>
        </el-form-item>
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
import { deleteAdminPost, getAdminPosts, saveAdminPost } from '@/api/modules/admin';
import type { PostData } from '@/api/modules/site';

const rawList = ref<PostData[]>([]);
const dialogVisible = ref(false);
const saving = ref(false);

const form = reactive<Partial<PostData>>({
  id: undefined,
  title: '',
  summary: '',
  content: '',
  coverImage: '',
  category: '',
  tags: '',
  published: 1
});

const posts = computed(() =>
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
    summary: '',
    content: '',
    coverImage: '',
    category: '',
    tags: '',
    published: 1
  });
}

/**
 * 获取后台文章列表。
 */
async function loadAdminPosts(): Promise<void> {
  const response = await getAdminPosts();
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
 * @param item 当前文章数据。
 */
function openEditDialog(item: PostData): void {
  resetForm();
  Object.assign(form, item);
  dialogVisible.value = true;
}

/**
 * 保存文章数据。
 */
async function handleSave(): Promise<void> {
  if (saving.value) {
    return;
  }
  saving.value = true;
  try {
    await saveAdminPost({ ...form });
    ElMessage.success(form.id ? '文章更新成功' : '文章创建成功');
    dialogVisible.value = false;
    await loadAdminPosts();
  } catch (error) {
    ElMessage.error('文章保存失败');
  } finally {
    saving.value = false;
  }
}

/**
 * 删除文章数据。
 * @param item 当前文章数据。
 */
async function handleDelete(item: PostData): Promise<void> {
  try {
    await ElMessageBox.confirm(`确认删除文章“${item.title}”吗？`, '删除确认', {
      type: 'warning'
    });
    await deleteAdminPost(item.id);
    ElMessage.success('文章删除成功');
    await loadAdminPosts();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('文章删除失败');
    }
  }
}

onMounted(() => {
  void loadAdminPosts();
});
</script>
