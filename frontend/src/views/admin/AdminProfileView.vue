<template>
  <section class="grid gap-6 xl:grid-cols-[1fr_1.1fr]">
    <article class="rounded-[28px] bg-white p-6 shadow-sm">
      <h2 class="text-xl font-semibold text-slate-900">关于我管理</h2>
      <p class="mt-2 text-sm text-slate-500">维护头像、关于我内容和联系方式。</p>
      <el-form class="mt-6" label-position="top">
        <el-form-item label="头像">
          <div class="flex flex-col gap-4 sm:flex-row sm:items-center">
            <div class="flex h-24 w-24 items-center justify-center overflow-hidden rounded-full bg-slate-100">
              <img v-if="form.avatarUrl" :src="form.avatarUrl" alt="avatar" class="h-full w-full object-cover" />
              <span v-else class="text-xs text-slate-400">暂无头像</span>
            </div>
            <div class="flex flex-1 flex-col gap-3">
              <AdminMediaPicker v-model="form.avatarUrl" button-text="选择头像" empty-text="暂未选择头像" />
              <el-input v-model="form.avatarUrl" placeholder="可直接粘贴头像地址，或从媒体库选择" />
            </div>
          </div>
        </el-form-item>
        <el-form-item label="关于我">
          <el-input v-model="form.aboutContent" type="textarea" :rows="6" />
        </el-form-item>
        <el-form-item label="联系邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="微信">
          <el-input v-model="form.wechat" />
        </el-form-item>
        <el-form-item label="GitHub">
          <el-input v-model="form.githubUrl" />
        </el-form-item>
        <el-button type="primary" :loading="saving" @click="handleSave">保存资料</el-button>
      </el-form>
    </article>

    <article class="rounded-[28px] bg-white p-6 shadow-sm">
      <h2 class="text-xl font-semibold text-slate-900">资料说明</h2>
      <p class="mt-4 text-sm leading-8 text-slate-600">
        本页仅维护关于我、联系方式与头像。首页横幅内容请前往“首页横幅管理”单独调整。
      </p>
      <div class="mt-6 space-y-3 text-sm text-slate-600">
        <p>邮箱：{{ form.email || '-' }}</p>
        <p>电话：{{ form.phone || '-' }}</p>
        <p>微信：{{ form.wechat || '-' }}</p>
        <p>GitHub：{{ form.githubUrl || '-' }}</p>
      </div>
    </article>
  </section>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus';
import { onMounted, reactive, ref } from 'vue';
import AdminMediaPicker from '@/views/admin/AdminMediaPicker.vue';
import { getAdminProfile, updateAdminProfile } from '@/api/modules/admin';
import { type ProfileData } from '@/api/modules/site';

const saving = ref(false);
const form = reactive<ProfileData>({
  id: 1,
  siteTitle: '',
  heroTitle: '',
  heroSubtitle: '',
  heroConfig: '',
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

async function loadProfile(): Promise<void> {
  const response = await getAdminProfile();
  Object.assign(form, response.data ?? {});
}

async function handleSave(): Promise<void> {
  if (saving.value) {
    return;
  }
  saving.value = true;
  try {
    const response = await updateAdminProfile({ ...form });
    Object.assign(form, response.data ?? {});
    ElMessage.success('资料保存成功');
  } finally {
    saving.value = false;
  }
}

onMounted(() => {
  void loadProfile();
});
</script>
