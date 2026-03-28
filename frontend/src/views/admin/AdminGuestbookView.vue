<template>
  <section class="space-y-6">
    <div class="rounded-[28px] border border-white/70 bg-white p-6 shadow-sm shadow-slate-200/70">
      <div class="flex flex-wrap items-start justify-between gap-4">
        <div>
          <p class="text-xs font-medium uppercase tracking-[0.24em] text-slate-400">Guestbook</p>
          <h2 class="mt-3 text-2xl font-semibold tracking-tight text-slate-950">留言管理</h2>
          <p class="mt-2 text-sm text-slate-500">维护前台留言介绍文案，并管理留言公开状态与内容。</p>
        </div>
        <div class="rounded-2xl bg-slate-100 px-4 py-3 text-right">
          <p class="text-xs uppercase tracking-[0.22em] text-slate-400">留言总数</p>
          <p class="mt-2 text-2xl font-semibold text-slate-950">{{ messages.length }}</p>
        </div>
      </div>
    </div>

    <div class="rounded-[28px] border border-white/70 bg-white p-6 shadow-sm shadow-slate-200/70">
      <div class="flex items-center justify-between gap-4">
        <div>
          <h3 class="text-lg font-semibold text-slate-950">留言页介绍文案</h3>
          <p class="mt-2 text-sm text-slate-500">保存后前台留言页会同步更新。</p>
        </div>
        <el-button type="primary" :loading="savingIntro" @click="handleSaveIntro">保存文案</el-button>
      </div>
      <el-input
        v-model="guestbookIntro"
        class="mt-5"
        type="textarea"
        :rows="5"
        maxlength="2000"
        show-word-limit
        placeholder="请输入留言页介绍文案"
      />
    </div>

    <div class="overflow-hidden rounded-[28px] border border-white/70 bg-white shadow-sm shadow-slate-200/70">
      <div class="border-b border-slate-100 px-5 py-4">
        <div class="flex flex-wrap items-center justify-between gap-3">
          <div>
            <h3 class="text-lg font-semibold text-slate-950">留言列表</h3>
            <p class="mt-1 text-sm text-slate-500">支持修改正文、切换公开状态与删除。</p>
          </div>
        </div>
      </div>

      <el-table :data="messages" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="正文" min-width="320">
          <template #default="scope">
            <div class="whitespace-pre-line break-words text-sm leading-7 text-slate-700">
              {{ scope.row.content || '-' }}
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="ipAddress" label="IP" min-width="140" />
        <el-table-column label="状态" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.published === 1 ? 'success' : 'info'">
              {{ scope.row.published === 1 ? '公开' : '不公开' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" min-width="180" />
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="openEditDialog(scope.row)">编辑</el-button>
            <el-button link type="warning" @click="handleTogglePublished(scope.row)">
              {{ scope.row.published === 1 ? '设为不公开' : '设为公开' }}
            </el-button>
            <el-button link type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div v-if="!messages.length" class="flex min-h-[220px] items-center justify-center border-t border-slate-100 bg-slate-50 text-sm text-slate-400">
        暂无留言
      </div>
    </div>

    <el-dialog v-model="dialogVisible" title="编辑留言" width="720px" @closed="resetEditForm">
      <el-form label-position="top">
        <el-form-item label="正文">
          <el-input
            v-model="editForm.content"
            type="textarea"
            :rows="6"
            maxlength="2000"
            show-word-limit
            placeholder="请输入留言正文"
          />
        </el-form-item>
        <el-form-item label="公开状态">
          <el-select v-model="editForm.published" class="!w-full">
            <el-option label="公开" :value="1" />
            <el-option label="不公开" :value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="savingMessage" @click="handleSaveMessage">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus';
import { onMounted, reactive, ref } from 'vue';
import {
  deleteAdminGuestbookMessage,
  getAdminGuestbook,
  updateAdminGuestbookIntro,
  updateAdminGuestbookMessage,
  type AdminGuestbookData
} from '@/api/modules/admin';
import type { GuestbookMessageData } from '@/api/modules/site';

const guestbookIntro = ref('');
const messages = ref<GuestbookMessageData[]>([]);
const savingIntro = ref(false);
const savingMessage = ref(false);
const dialogVisible = ref(false);
const editingId = ref<number | null>(null);
const editForm = reactive<Partial<GuestbookMessageData>>({
  content: '',
  published: 1
});

function resolveErrorMessage(error: unknown, fallback: string): string {
  if (typeof error === 'object' && error && 'response' in error) {
    const response = (error as { response?: { data?: { message?: string } } }).response;
    if (response?.data?.message) {
      return response.data.message;
    }
  }
  return fallback;
}

function resetEditForm(): void {
  editingId.value = null;
  editForm.content = '';
  editForm.published = 1;
}

async function loadGuestbook(): Promise<void> {
  const response = await getAdminGuestbook();
  const data = (response.data ?? {}) as AdminGuestbookData;
  guestbookIntro.value = data.guestbookIntro ?? '';
  messages.value = data.messages ?? [];
}

async function handleSaveIntro(): Promise<void> {
  if (savingIntro.value) {
    return;
  }
  savingIntro.value = true;
  try {
    await updateAdminGuestbookIntro(guestbookIntro.value);
    ElMessage.success('留言介绍文案保存成功');
    await loadGuestbook();
  } catch (error) {
    ElMessage.error(resolveErrorMessage(error, '留言介绍文案保存失败'));
  } finally {
    savingIntro.value = false;
  }
}

function openEditDialog(item: GuestbookMessageData): void {
  editingId.value = Number(item.id);
  editForm.content = item.content ?? '';
  editForm.published = item.published === 1 ? 1 : 0;
  dialogVisible.value = true;
}

async function handleSaveMessage(): Promise<void> {
  if (savingMessage.value || editingId.value === null) {
    return;
  }
  savingMessage.value = true;
  try {
    await updateAdminGuestbookMessage(editingId.value, {
      content: editForm.content,
      published: editForm.published
    });
    ElMessage.success('留言更新成功');
    dialogVisible.value = false;
    await loadGuestbook();
  } catch (error) {
    ElMessage.error(resolveErrorMessage(error, '留言更新失败'));
  } finally {
    savingMessage.value = false;
  }
}

async function handleTogglePublished(item: GuestbookMessageData): Promise<void> {
  try {
    await updateAdminGuestbookMessage(Number(item.id), {
      published: item.published === 1 ? 0 : 1
    });
    ElMessage.success('留言状态更新成功');
    await loadGuestbook();
  } catch (error) {
    ElMessage.error(resolveErrorMessage(error, '留言状态更新失败'));
  }
}

async function handleDelete(item: GuestbookMessageData): Promise<void> {
  try {
    await ElMessageBox.confirm('确认删除这条留言吗？', '删除确认', {
      type: 'warning'
    });
    await deleteAdminGuestbookMessage(Number(item.id));
    ElMessage.success('留言删除成功');
    await loadGuestbook();
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      return;
    }
    ElMessage.error(resolveErrorMessage(error, '留言删除失败'));
  }
}

onMounted(() => {
  void loadGuestbook();
});
</script>
