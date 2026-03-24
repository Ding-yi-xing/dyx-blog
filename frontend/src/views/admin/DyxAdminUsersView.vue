<template>
  <section class="rounded-[28px] bg-white p-6 shadow-sm">
    <h2 class="text-xl font-semibold text-slate-900">用户管理</h2>
    <p class="mt-2 text-sm text-slate-500">管理后台操作账号与角色权限。</p>
    <el-table class="mt-6" :data="users" border>
      <el-table-column prop="username" label="用户名" min-width="180" />
      <el-table-column prop="displayName" label="显示名称" min-width="180" />
      <el-table-column prop="role" label="角色" width="120" />
      <el-table-column prop="statusText" label="状态" width="120" />
      <el-table-column prop="updatedAt" label="更新时间" width="180" />
    </el-table>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { getAdminUsers, type AdminListUserData } from '@/api/modules/admin';

const rawList = ref<AdminListUserData[]>([]);

const users = computed(() =>
  rawList.value.map((item) => ({
    ...item,
    statusText: item.enabled === 1 ? '启用' : '停用'
  }))
);

/**
 * 获取后台用户列表。
 */
async function loadUsers(): Promise<void> {
  const response = await getAdminUsers();
  rawList.value = response.data ?? [];
}

onMounted(() => {
  void loadUsers();
});
</script>
