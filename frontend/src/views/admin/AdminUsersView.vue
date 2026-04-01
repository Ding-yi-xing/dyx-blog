<template>
  <section class="rounded-[28px] bg-white p-6 shadow-sm">
    <div class="mb-6 flex flex-wrap items-center justify-between gap-4">
      <h2 class="text-xl font-semibold text-slate-900">用户管理</h2>
      <el-button type="primary" @click="openCreateDialog">新建用户</el-button>
    </div>

    <el-table :data="users" border>
      <el-table-column prop="username" label="用户名" min-width="180" />
      <el-table-column prop="displayName" label="显示名称" min-width="180" />
      <el-table-column prop="roleText" label="角色" width="120" />
      <el-table-column prop="statusText" label="状态" width="120" />
      <el-table-column prop="updatedAt" label="更新时间" width="180" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="scope">
          <el-button link type="primary" @click="openEditDialog(scope.row.raw)">编辑</el-button>
          <el-button link type="danger" :disabled="scope.row.isCurrentUser" @click="handleDelete(scope.row.raw)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑用户' : '新建用户'" width="560px">
      <el-form label-position="top">
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="显示名称">
          <el-input v-model="form.displayName" placeholder="请输入显示名称" />
        </el-form-item>
        <el-form-item :label="form.id ? '密码（留空则不修改）' : '密码'">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <div class="grid gap-4 sm:grid-cols-2">
          <el-form-item label="角色">
            <el-select v-model="form.role" class="!w-full">
              <el-option label="管理员" value="ADMIN" />
              <el-option label="普通用户" value="USER" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="form.enabled" class="!w-full">
              <el-option label="启用" :value="1" />
              <el-option label="停用" :value="0" />
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
import { deleteAdminUser, getAdminUsers, saveAdminUser, type AdminListUserData } from '@/api/modules/admin';
import { useAuthStore } from '@/stores/auth';
import { resolveErrorMessage } from '@/utils/error';

/**
 * 后台用户管理页。
 * 负责展示用户列表，并提供用户的新建、编辑与删除流程。
 */
const authStore = useAuthStore();
const rawList = ref<AdminListUserData[]>([]);
const dialogVisible = ref(false);
const saving = ref(false);

const form = reactive<Partial<AdminListUserData>>({
  id: undefined,
  username: '',
  displayName: '',
  password: '',
  role: 'ADMIN',
  enabled: 1
});

/**
 * 将后台原始用户列表转换为表格展示所需的衍生字段。
 */
const users = computed(() =>
  rawList.value.map((item) => ({
    ...item,
    roleText: item.role === 'ADMIN' ? '管理员' : '普通用户',
    statusText: item.enabled === 1 ? '启用' : '停用',
    isCurrentUser: item.id === authStore.user?.id,
    raw: item
  }))
);

/**
 * 重置用户表单，供新建与编辑前复用。
 *
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；仅重置本地表单状态。
 * @author Dyx
 */
function resetForm(): void {
  Object.assign(form, {
    id: undefined,
    username: '',
    displayName: '',
    password: '',
    role: 'ADMIN',
    enabled: 1
  });
}

/**
 * 获取后台用户列表并刷新表格数据源。
 *
 * @returns 返回异步加载结果；成功后会更新页面表格数据。
 * @throws 该函数不会主动抛出同步异常；接口失败时会以 Promise reject 形式返回。
 * @author Dyx
 */
async function loadUsers(): Promise<void> {
  const response = await getAdminUsers();
  rawList.value = response.data ?? [];
}

/**
 * 打开新建用户弹窗，并初始化为空表单。
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
 * 打开编辑用户弹窗，并将当前用户数据回填到表单中。
 *
 * @param item 待编辑的用户数据。
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；密码字段会被重置为空以避免回显旧密码。
 * @author Dyx
 */
function openEditDialog(item: AdminListUserData): void {
  resetForm();
  Object.assign(form, item, { password: '' });
  dialogVisible.value = true;
}

/**
 * 保存当前用户表单。
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
    await saveAdminUser({ ...form });
    ElMessage.success(form.id ? '用户更新成功' : '用户创建成功');
    dialogVisible.value = false;
    await loadUsers();
  } catch (error) {
    ElMessage.error(resolveErrorMessage(error, '用户保存失败'));
  } finally {
    saving.value = false;
  }
}

/**
 * 删除指定用户，并在用户确认后刷新当前列表。
 *
 * @param item 待删除的用户数据。
 * @returns 返回异步删除结果。
 * @throws 该函数不会主动向外抛出异常；取消删除时会静默结束，失败时通过页面提示反馈。
 * @author Dyx
 */
async function handleDelete(item: AdminListUserData): Promise<void> {
  try {
    await ElMessageBox.confirm(`确认删除用户“${item.displayName || item.username}”吗？`, '删除确认', {
      type: 'warning'
    });
    await deleteAdminUser(Number(item.id));
    ElMessage.success('用户删除成功');
    await loadUsers();
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      return;
    }
    ElMessage.error(resolveErrorMessage(error, '用户删除失败'));
  }
}

onMounted(() => {
  void loadUsers();
});
</script>
