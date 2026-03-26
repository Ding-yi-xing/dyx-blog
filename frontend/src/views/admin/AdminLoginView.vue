<template>
  <div class="flex min-h-screen items-center justify-center bg-[radial-gradient(circle_at_top,_#dbeafe,_#f8fafc_40%,_#ffffff_100%)] px-4">
    <div class="relative w-full max-w-md rounded-[32px] border border-white/60 bg-white/80 p-8 shadow-dyx-soft backdrop-blur-xl">
      <RouterLink
        to="/"
        class="absolute right-6 top-6 inline-flex rounded-full border border-slate-200 px-3 py-1.5 text-xs text-slate-600 transition hover:border-slate-300 hover:bg-slate-50 hover:text-slate-900"
      >
        返回站点
      </RouterLink>

      <div class="text-center">
        <p class="text-sm uppercase tracking-[0.35em] text-slate-500">Admin Login</p>
        <h1 class="mt-4 text-3xl font-semibold text-slate-900">登录后台管理系统</h1>
      </div>

      <el-form class="mt-8" :model="form" label-position="top" @submit.prevent>
        <el-form-item label="用户名">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-button :loading="submitting" type="primary" class="!mt-2 !h-11 !w-full !rounded-full" @click="handleLogin">
          进入后台
        </el-button>
      </el-form>

      <p class="mt-5 text-center text-sm text-slate-500">初始化 SQL 默认账号：admin / admin123456</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus';
import { reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { adminLogin } from '@/api/modules/admin';
import { useAuthStore } from '@/stores/auth';

const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();
const submitting = ref(false);

const form = reactive({
  username: 'admin',
  password: 'admin123456'
});

async function handleLogin(): Promise<void> {
  if (submitting.value) {
    return;
  }
  submitting.value = true;
  try {
    const response = await adminLogin(form);
    authStore.setAuth(response.data.token, response.data.user);
    ElMessage.success('登录成功');
    const redirectPath = typeof route.query.redirect === 'string' ? route.query.redirect : '/admin/dashboard';
    await router.push(redirectPath);
  } catch (error) {
    ElMessage.error('登录失败，请检查账号或密码');
  } finally {
    submitting.value = false;
  }
}
</script>
