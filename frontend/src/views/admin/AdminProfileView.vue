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
              <AdminMediaPicker :model-value="form.avatarUrl" button-text="选择头像" empty-text="暂未选择头像" @update:model-value="handleAvatarSelect" />
              <el-input v-model="form.avatarUrl" placeholder="可直接粘贴头像地址，或从媒体库选择" />
            </div>
          </div>
        </el-form-item>
        <el-form-item label="关于我">
          <el-input v-model="form.aboutContent" type="textarea" :rows="6" />
        </el-form-item>
        <el-form-item label="联系方式">
          <div class="space-y-4">
            <div
                  v-for="(item, index) in contactMethods"
                  :key="index"
                  class="rounded-[20px] border border-slate-200 bg-white p-4 shadow-sm"
                >
                  <div class="grid gap-4 md:grid-cols-[0.9fr_1fr_1.5fr_auto] md:items-end">
                    <el-input v-model="item.type" placeholder="类型，如 email / github / 微信" />
                    <el-input v-model="item.label" placeholder="显示名称，如 邮箱 / GitHub" />
                    <el-input v-model="item.value" placeholder="联系方式内容" />
                    <el-button type="danger" plain @click="removeContact(index)">删除</el-button>
                  </div>
                </div>
            <el-button plain @click="addContact">新增联系方式</el-button>
          </div>
        </el-form-item>
        <el-button type="primary" :loading="saving" @click="handleSave">保存资料</el-button>
      </el-form>
    </article>

    <article class="rounded-[28px] bg-white p-6 shadow-sm">
      <h2 class="text-xl font-semibold text-slate-900">资料说明</h2>
      <p class="mt-4 text-sm leading-8 text-slate-600">
        本页仅维护关于我、联系方式与头像。首页首屏内容请前往“首页管理”单独调整。
      </p>
      <div class="mt-6 space-y-3 text-sm text-slate-600">
        <p v-if="contactMethods.length" v-for="(item, index) in contactMethods" :key="`${item.label || item.type}-${index}`">
          {{ item.label || item.type || '联系方式' }}：{{ item.value || '-' }}
        </p>
        <p v-else>暂无联系方式</p>
      </div>
    </article>

    <BusinessImageCropper
      :visible="avatarCropperVisible"
      :image-url="pendingAvatarUrl"
      mode="avatar"
      :source-name="pendingAvatarName"
      @update:visible="handleAvatarCropperVisibleChange"
      @confirm="handleAvatarCropConfirm"
    />
  </section>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus';
import { onMounted, reactive, ref } from 'vue';
import BusinessImageCropper from '@/components/admin/BusinessImageCropper.vue';
import AdminMediaPicker from '@/views/admin/AdminMediaPicker.vue';
import { getAdminProfile, updateAdminProfile, uploadAdminMedia } from '@/api/modules/admin';
import { resolveProfileContactMethods, stringifyContactMethods, type ContactMethodData, type ProfileData } from '@/api/modules/site';
import { extractFileName } from '@/utils/media';

interface CropConfirmPayload {
  edited: boolean;
  file?: File;
  originalUrl?: string;
}

/**
 * 后台“关于我”资料管理页。
 * 负责维护头像、关于我内容和联系方式，并处理头像裁剪上传流程。
 */
const saving = ref(false);
const avatarCropperVisible = ref(false);
const pendingAvatarUrl = ref('');
const pendingAvatarName = ref('');
const contactMethods = ref<ContactMethodData[]>([]);
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
  resumePdfUrl: '',
  contactMethods: ''
});

/**
 * 创建一条空的联系方式记录，供列表新增时复用。
 *
 * @returns 返回空白联系方式对象。
 * @throws 该函数不会主动抛出异常；仅返回本地默认值。
 * @author Dyx
 */
function createEmptyContact(): ContactMethodData {
  return {
    type: '',
    label: '',
    value: ''
  };
}

/**
 * 在联系方式列表末尾新增一条空记录。
 *
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；仅更新本地列表。
 * @author Dyx
 */
function addContact(): void {
  contactMethods.value = [...contactMethods.value, createEmptyContact()];
}

/**
 * 删除指定下标的联系方式记录。
 *
 * @param index 待删除项的下标。
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；越界下标会自然得到过滤后的原列表。
 * @author Dyx
 */
function removeContact(index: number): void {
  contactMethods.value = contactMethods.value.filter((_, currentIndex) => currentIndex !== index);
}

/**
 * 从联系方式扩展列表中回填旧版兼容字段值。
 *
 * @param types 需要匹配的类型关键词列表。
 * @returns 返回首个匹配项的联系方式值；未命中时返回空字符串。
 * @throws 该函数不会主动抛出异常；仅执行本地查找。
 * @author Dyx
 */
function findLegacyContactValue(types: string[]): string {
  return contactMethods.value.find((item) => {
    const normalized = `${item.type || ''} ${item.label || ''}`.toLowerCase();
    return types.some((type) => normalized.includes(type));
  })?.value?.trim() || '';
}

/**
 * 处理头像地址选择。
 * 当新头像与当前头像不同步时，会先打开裁剪弹窗确认。
 *
 * @param value 媒体选择器返回的头像地址或地址数组。
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；仅更新本地头像裁剪上下文。
 * @author Dyx
 */
function handleAvatarSelect(value: string | string[]): void {
  const nextUrl = typeof value === 'string' ? value.trim() : value[0]?.trim() || '';
  if (!nextUrl) {
    form.avatarUrl = '';
    return;
  }
  if (nextUrl === form.avatarUrl) {
    form.avatarUrl = nextUrl;
    return;
  }
  pendingAvatarUrl.value = nextUrl;
  pendingAvatarName.value = extractFileName(nextUrl);
  avatarCropperVisible.value = true;
}

/**
 * 同步头像裁剪弹窗显隐状态，并在关闭时清理待处理头像上下文。
 *
 * @param value 裁剪弹窗新的显示状态。
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；仅更新本地状态。
 * @author Dyx
 */
function handleAvatarCropperVisibleChange(value: boolean): void {
  avatarCropperVisible.value = value;
  if (!value) {
    pendingAvatarUrl.value = '';
    pendingAvatarName.value = '';
  }
}

/**
 * 处理头像裁剪确认结果。
 * 未裁剪时直接使用原图，裁剪后则上传新文件并回填头像地址。
 *
 * @param payload 裁剪组件返回的确认结果。
 * @returns 返回异步处理结果。
 * @throws 该函数不会主动向外抛出异常；上传失败时会通过页面提示反馈。
 * @author Dyx
 */
async function handleAvatarCropConfirm(payload: CropConfirmPayload): Promise<void> {
  if (!payload.edited) {
    form.avatarUrl = payload.originalUrl ?? pendingAvatarUrl.value ?? form.avatarUrl;
    ElMessage.success('头像已更新');
    return;
  }
  if (!payload.file) {
    ElMessage.warning('未获取到裁剪后的头像文件');
    return;
  }
  try {
    const response = await uploadAdminMedia(payload.file);
    form.avatarUrl = response.data?.fileUrl ?? form.avatarUrl;
    ElMessage.success('头像裁剪并上传成功');
  } catch (error) {
    ElMessage.error('头像上传失败');
  }
}

/**
 * 获取个人资料并同步联系方式扩展字段。
 *
 * @returns 返回异步加载结果；成功后会回填资料表单和联系方式列表。
 * @throws 该函数不会主动抛出同步异常；接口失败时会以 Promise reject 形式返回。
 * @author Dyx
 */
async function loadProfile(): Promise<void> {
  const response = await getAdminProfile();
  Object.assign(form, response.data ?? {});
  contactMethods.value = resolveProfileContactMethods(response.data ?? {});
}

/**
 * 保存当前个人资料表单。
 * 提交前会把联系方式列表同步回兼容字段和 contactMethods 字符串字段。
 *
 * @returns 返回异步保存结果；成功后会回填接口返回的最新资料。
 * @throws 该函数不会主动向外抛出异常；保存失败时加载状态会在 finally 中恢复。
 * @author Dyx
 */
async function handleSave(): Promise<void> {
  if (saving.value) {
    return;
  }
  saving.value = true;
  try {
    const payload: ProfileData = {
      ...form,
      contactMethods: stringifyContactMethods(contactMethods.value),
      email: findLegacyContactValue(['email', '邮箱']),
      phone: findLegacyContactValue(['phone', '电话', 'mobile', '手机']),
      wechat: findLegacyContactValue(['wechat', '微信']),
      githubUrl: findLegacyContactValue(['github'])
    };
    const response = await updateAdminProfile(payload);
    Object.assign(form, response.data ?? {});
    contactMethods.value = resolveProfileContactMethods(response.data ?? payload);
    ElMessage.success('资料保存成功');
  } finally {
    saving.value = false;
  }
}

onMounted(() => {
  void loadProfile();
});
</script>
