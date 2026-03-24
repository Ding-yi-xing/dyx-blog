<template>
  <section class="grid gap-6 xl:grid-cols-[1fr_1.1fr]">
    <article class="rounded-[28px] bg-white p-6 shadow-sm">
      <h2 class="text-xl font-semibold text-slate-900">个人资料管理</h2>
      <p class="mt-2 text-sm text-slate-500">维护网站标题、简介、头像和联系方式。</p>
      <el-form class="mt-6" label-position="top">
        <el-form-item label="头像">
          <div class="flex flex-col gap-4 sm:flex-row sm:items-center">
            <div class="flex h-24 w-24 items-center justify-center overflow-hidden rounded-full bg-slate-100">
              <img v-if="form.avatarUrl" :src="form.avatarUrl" alt="avatar" class="h-full w-full object-cover" />
              <span v-else class="text-xs text-slate-400">暂无头像</span>
            </div>
            <div class="flex flex-1 flex-col gap-3">
              <div class="flex flex-wrap gap-3">
                <el-upload :show-file-list="false" :http-request="handleAvatarUpload">
                  <el-button type="primary" :loading="uploadingAvatar">上传头像</el-button>
                </el-upload>
                <el-button v-if="form.avatarUrl" @click="form.avatarUrl = ''">清空头像</el-button>
              </div>
              <el-input v-model="form.avatarUrl" placeholder="可直接粘贴头像地址，或上传图片自动回填" />
            </div>
          </div>
        </el-form-item>
        <el-form-item label="站点标题">
          <el-input v-model="form.siteTitle" />
        </el-form-item>
        <el-form-item label="首页主标题">
          <el-input v-model="form.heroTitle" />
        </el-form-item>
        <el-form-item label="首页副标题">
          <el-input v-model="form.heroSubtitle" type="textarea" :rows="3" />
        </el-form-item>
        <el-form-item label="关于我">
          <el-input v-model="form.aboutContent" type="textarea" :rows="4" />
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
        本页已接入个人资料查询与更新接口，保存后可同步驱动前台首页、个人信息页与个人经历页展示。
      </p>
    </article>
  </section>
</template>

<script setup lang="ts">
import { ElMessage, type UploadRequestOptions } from 'element-plus';
import { onMounted, reactive, ref } from 'vue';
import { getAdminProfile, updateAdminProfile, uploadAdminMedia } from '@/api/modules/admin';

const saving = ref(false);
const uploadingAvatar = ref(false);
const form = reactive({
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
  avatarUrl: ''
});

/**
 * 加载后台个人资料数据。
 */
async function loadProfile(): Promise<void> {
  const response = await getAdminProfile();
  Object.assign(form, response.data ?? {});
}

/**
 * 上传头像并回填头像地址。
 * @param options Element Plus 上传请求参数。
 */
async function handleAvatarUpload(options: UploadRequestOptions): Promise<void> {
  uploadingAvatar.value = true;
  try {
    const response = await uploadAdminMedia(options.file as File);
    form.avatarUrl = response.data?.fileUrl ?? '';
    ElMessage.success('头像上传成功');
  } catch (error) {
    ElMessage.error('头像上传失败');
  } finally {
    uploadingAvatar.value = false;
  }
}

/**
 * 保存后台个人资料数据。
 */
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
