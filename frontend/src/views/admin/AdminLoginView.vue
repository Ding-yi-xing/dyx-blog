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
    </div>
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus';
import { reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { adminLogin } from '@/api/modules/admin';
import { useAuthStore } from '@/stores/auth';
import { resolveErrorMessage } from '@/utils/error';

/**
 * 后台登录页。
 * 负责处理管理员账号登录、登录态写入以及登录后的安全跳转。
 */
const router = useRouter();
const route = useRoute();
const authStore = useAuthStore();
const submitting = ref(false);
const DEFAULT_REDIRECT_PATH = '/dyx-manager/dashboard';

const form = reactive({
  username: '',
  password: ''
});

/**
 * 规范化登录后的跳转地址，只允许站内后台路径。
 *
 * @param value 路由查询参数中的 redirect 值。
 * @returns 返回安全可用的后台跳转路径；不合法时回退到默认仪表盘。
 * @throws 该函数不会主动抛出异常；非法路径会直接回退到默认值。
 * @author Dyx
 */
function resolveRedirectPath(value: unknown): string {
  if (typeof value !== 'string') {
    return DEFAULT_REDIRECT_PATH;
  }
  const normalized = value.trim();
  if (!normalized.startsWith('/')) {
    return DEFAULT_REDIRECT_PATH;
  }
  if (normalized.startsWith('//') || !normalized.startsWith('/dyx-manager')) {
    return DEFAULT_REDIRECT_PATH;
  }
  return normalized;
}

/**
 * 提交后台登录请求，并在成功后写入登录态与跳转到目标后台页面。
 *
 * @returns 返回异步登录结果。
 * @throws 该函数不会主动向外抛出异常；登录失败时会通过页面提示反馈。
 * @author Dyx
 */
async function handleLogin(): Promise<void> {
  if (submitting.value) {
    return;
  }
  submitting.value = true;
  try {
    const result = await adminLogin(form);
    const loginData = (result as { data?: { token: string; user: any } })?.data;
    if (loginData) {
      authStore.setAuth(loginData.token, loginData.user);
    }
    ElMessage.success('登录成功');
    await router.push(resolveRedirectPath(route.query.redirect));
  } catch (error) {
    ElMessage.error(resolveErrorMessage(error, '登录失败，请检查账号或密码'));
  } finally {
    submitting.value = false;
  }
}
</script>
