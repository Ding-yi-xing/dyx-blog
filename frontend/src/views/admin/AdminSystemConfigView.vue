<template>
  <section class="rounded-[28px] bg-white p-6 shadow-sm">
    <div class="flex flex-wrap items-start justify-between gap-4">
      <div>
        <h2 class="text-xl font-semibold text-slate-900">系统配置</h2>
        <p class="mt-2 text-sm leading-6 text-slate-500">
          这里统一维护媒体存储、IP 查询与首页第三屏聚合规则。
        </p>
      </div>
      <el-button type="primary" :loading="saving" @click="handleSave"
        >保存配置</el-button
      >
    </div>

    <div class="mt-6 grid gap-6 xl:grid-cols-[0.9fr_1.1fr]">
      <article class="rounded-[24px] border border-slate-200 bg-slate-50 p-5">
        <h3 class="text-base font-semibold text-slate-900">存储方式</h3>
        <el-radio-group
          v-model="form.storageType"
          class="mt-4 flex flex-col gap-3"
        >
          <el-radio
            value="local"
            border
            class="!mr-0 !h-auto rounded-2xl !py-3 !pl-4 !pr-5"
          >
            <div>
              <p class="font-medium text-slate-900">本地存储</p>
              <p class="text-xs text-slate-500">
                上传到 backend/uploads，并通过 /media/ 访问。
              </p>
            </div>
          </el-radio>
          <el-radio
            value="oss"
            border
            class="!mr-0 !h-auto rounded-2xl !py-3 !pl-4 !pr-5"
          >
            <div>
              <p class="font-medium text-slate-900">阿里云 OSS</p>
              <p class="text-xs text-slate-500">
                上传到 OSS Bucket，媒体链接切换为 OSS 地址。
              </p>
            </div>
          </el-radio>
        </el-radio-group>
      </article>

      <article class="rounded-[24px] border border-slate-200 bg-slate-50 p-5">
        <div class="flex items-center justify-between gap-3">
          <div>
            <h3 class="text-base font-semibold text-slate-900">OSS 配置</h3>
            <p class="text-xs text-slate-500">仅在存储方式为 OSS 时生效。</p>
          </div>
          <el-tag :type="form.storageType === 'oss' ? 'success' : 'info'">{{
            form.storageType === "oss" ? "已启用" : "未启用"
          }}</el-tag>
        </div>

        <el-form label-position="top" class="mt-5 grid gap-4 md:grid-cols-2">
          <el-form-item label="Endpoint">
            <el-input
              v-model="form.ossEndpoint"
              placeholder="未修改则保留当前值；留空保存可清空"
            />
            <div class="mt-2 text-xs text-slate-500">
              当前状态：{{ form.ossEndpointConfigured ? '已配置' : '未配置' }}
            </div>
          </el-form-item>
          <el-form-item label="Region">
            <el-input
              v-model="form.ossRegion"
              placeholder="未修改则保留当前值；留空保存可清空"
            />
            <div class="mt-2 text-xs text-slate-500">
              当前状态：{{ form.ossRegionConfigured ? '已配置' : '未配置' }}
            </div>
          </el-form-item>
          <el-form-item label="Bucket" class="md:col-span-2">
            <el-input
              v-model="form.ossBucketName"
              placeholder="未修改则保留当前值；留空保存可清空"
            />
            <div class="mt-2 text-xs text-slate-500">
              当前状态：{{ form.ossBucketNameConfigured ? '已配置' : '未配置' }}
            </div>
          </el-form-item>
          <el-form-item label="公开访问前缀" class="md:col-span-2">
            <el-input
              v-model="form.ossPublicUrlPrefix"
              placeholder="未修改则保留当前值；留空保存可清空"
            />
            <div class="mt-2 text-xs text-slate-500">
              当前状态：{{ form.ossPublicUrlPrefixConfigured ? '已配置' : '未配置' }}
            </div>
          </el-form-item>
          <el-form-item label="OSS 目录" class="md:col-span-2">
            <el-input
              v-model="form.ossBaseDir"
              placeholder="例如 dyx-blog/media"
            />
          </el-form-item>
        </el-form>
      </article>
    </div>

    <article class="mt-6 rounded-[24px] border border-slate-200 bg-slate-50 p-5">
      <div class="flex items-center justify-between gap-3">
        <div>
          <h3 class="text-base font-semibold text-slate-900">IP 查询配置</h3>
          <p class="text-xs text-slate-500">启用后会调用接口并将返回的 data.address 写入访问日志实际地址字段。</p>
        </div>
        <el-tag :type="form.ipLookupEnabled ? 'success' : 'info'">{{ form.ipLookupEnabled ? '已启用' : '未启用' }}</el-tag>
      </div>

      <el-form label-position="top" class="mt-5 grid gap-4 md:grid-cols-2">
        <el-form-item label="启用 IP 查询" class="md:col-span-2">
          <el-switch v-model="form.ipLookupEnabled" />
        </el-form-item>
        <el-form-item label="接口地址" class="md:col-span-2">
          <el-input
            v-model="form.ipLookupApiUrl"
            placeholder="例如 https://v2.xxapi.cn/api/ip"
          />
        </el-form-item>
      </el-form>
    </article>

    <article
      class="mt-6 rounded-[24px] border border-slate-200 bg-slate-50 p-5"
    >
      <div class="flex items-center justify-between gap-3">
        <div>
          <h3 class="text-base font-semibold text-slate-900">第三屏聚合配置</h3>
          <p class="text-xs text-slate-500">
            控制首页第三屏的版权文案、参与类型与展示数量。
          </p>
        </div>
        <el-tag type="warning">首页第三屏</el-tag>
      </div>

      <el-form label-position="top" class="mt-5 grid gap-4 md:grid-cols-2">
        <el-form-item label="版权文案" class="md:col-span-2">
          <el-input
            v-model="form.copyrightText"
            maxlength="255"
            show-word-limit
            placeholder="如：© 2026 DYX. All rights reserved."
          />
        </el-form-item>
        <el-form-item label="技术支持文案" class="md:col-span-2">
          <el-input
            v-model="form.techSupportText"
            maxlength="255"
            show-word-limit
            placeholder="如：技术支持 · Vue 3 + Spring Boot 3"
          />
        </el-form-item>
        <el-form-item label="首页第三屏参与类型" class="md:col-span-2">
          <div class="flex flex-wrap gap-3">
            <el-checkbox v-model="form.homeActivityEnablePosts">博客</el-checkbox>
            <el-checkbox v-model="form.homeActivityEnableMoments">动态</el-checkbox>
            <el-checkbox v-model="form.homeActivityEnableProjects">项目</el-checkbox>
            <el-checkbox v-model="form.homeActivityEnableWorks">作品</el-checkbox>
            <el-checkbox v-model="form.homeActivityEnableHonors">荣誉</el-checkbox>
          </div>
        </el-form-item>
        <el-form-item label="第三屏最多展示条数">
          <el-input-number
            v-model="form.homeActivityMaxItems"
            :min="1"
            :max="20"
            class="!w-full"
          />
        </el-form-item>
        <el-form-item label="单类型最多展示条数">
          <el-input-number
            v-model="form.homeActivityMaxItemsPerType"
            :min="1"
            :max="10"
            class="!w-full"
          />
        </el-form-item>
      </el-form>
    </article>

    <div
      class="mt-6 rounded-[24px] border border-dashed border-slate-300 bg-slate-50 px-5 py-4 text-sm leading-7 text-slate-500"
    >
      <p>
        1. 切到 OSS 前，请先在服务端环境变量中配置
        <code>OSS_ACCESS_KEY_ID</code> 与 <code>OSS_ACCESS_KEY_SECRET</code>。
      </p>
      <p>
        2. Endpoint 推荐填写不带协议头的域名（如
        <code>oss-cn-hangzhou.aliyuncs.com</code>），若填写完整
        URL，后端会自动规范化。
      </p>
      <p>
        3. 公开访问前缀用于自定义 CDN / Bucket 访问域名；未填写时会按 Bucket +
        Endpoint 自动生成公开地址。
      </p>
      <p>4. 启用 IP 查询后，后端会调用接口返回的 <code>data.address</code> 填充访问日志中的“实际地址”。</p>
      <p>5. 首页第二屏文案已迁移到“足迹管理”页编辑，这里只保留第三屏聚合与存储配置。</p>
      <p>6. 本地模式下“导入 uploads 文件”仍可用，OSS 模式下会自动禁用。</p>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus';
import { onMounted, reactive, ref } from 'vue';
import {
  getAdminSystemConfig,
  updateAdminSystemConfig,
  type SystemConfigData
} from '@/api/modules/admin';
import { resolveErrorMessage } from '@/utils/error';

/**
 * 后台系统配置页。
 * 负责维护媒体存储方式、IP 查询参数以及首页第三屏聚合配置。
 */
const saving = ref(false);
const form = reactive<SystemConfigData>({
  id: 1,
  storageType: 'local',
  ossEndpoint: undefined,
  ossRegion: undefined,
  ossBucketName: undefined,
  ossPublicUrlPrefix: undefined,
  ossBaseDir: '',
  ipLookupEnabled: false,
  ipLookupApiUrl: 'https://v2.xxapi.cn/api/ip',
  copyrightText: '',
  techSupportText: '',
  ossEndpointConfigured: false,
  ossRegionConfigured: false,
  ossBucketNameConfigured: false,
  ossPublicUrlPrefixConfigured: false
});

/**
 * 将系统配置接口返回的数据回填到表单。
 * 对敏感配置项采用“显示配置状态 + 保留已有输入”的方式，避免误清空。
 *
 * @param data 系统配置数据。
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；缺失字段会回退到默认值或保留当前输入。
 * @author Dyx
 */
function applyFormData(data?: SystemConfigData): void {
  form.id = data?.id ?? 1;
  form.storageType =
    (data?.storageType as 'local' | 'oss' | undefined) ?? form.storageType ?? 'local';
  form.ossEndpoint = data?.ossEndpoint ?? form.ossEndpoint;
  form.ossRegion = data?.ossRegion ?? form.ossRegion;
  form.ossBucketName = data?.ossBucketName ?? form.ossBucketName;
  form.ossPublicUrlPrefix = data?.ossPublicUrlPrefix ?? form.ossPublicUrlPrefix;
  form.ossBaseDir = data?.ossBaseDir ?? '';
  form.ipLookupEnabled = data?.ipLookupEnabled ?? false;
  form.ipLookupApiUrl = data?.ipLookupApiUrl ?? 'https://v2.xxapi.cn/api/ip';
  form.copyrightText = data?.copyrightText ?? '';
  form.techSupportText = data?.techSupportText ?? '';
  form.ossEndpointConfigured = Boolean(data?.ossEndpointConfigured);
  form.ossRegionConfigured = Boolean(data?.ossRegionConfigured);
  form.ossBucketNameConfigured = Boolean(data?.ossBucketNameConfigured);
  form.ossPublicUrlPrefixConfigured = Boolean(data?.ossPublicUrlPrefixConfigured);
  form.homeActivityEnablePosts = data?.homeActivityEnablePosts ?? false;
  form.homeActivityEnableMoments = data?.homeActivityEnableMoments ?? false;
  form.homeActivityEnableProjects = data?.homeActivityEnableProjects ?? true;
  form.homeActivityEnableWorks = data?.homeActivityEnableWorks ?? true;
  form.homeActivityEnableHonors = data?.homeActivityEnableHonors ?? false;
  form.homeActivityMaxItems = data?.homeActivityMaxItems ?? 6;
  form.homeActivityMaxItemsPerType = data?.homeActivityMaxItemsPerType ?? 3;
}
/**
 * 获取当前系统配置并回填页面表单。
 *
 * @returns 返回异步加载结果；成功后会更新存储配置和页脚文案字段。
 * @throws 该函数不会主动抛出同步异常；接口失败时会以 Promise reject 形式返回。
 * @author Dyx
 */
async function loadSystemConfig(): Promise<void> {
  const result = await getAdminSystemConfig();
  const configData = (result as { data?: SystemConfigData })?.data;
  if (configData) {
    applyFormData(configData);
  }
}

/**
 * 根据当前表单构建保存系统配置所需的提交参数。
 *
 * @returns 返回系统配置保存载荷。
 * @throws 该函数不会主动抛出异常；仅读取当前表单状态。
 * @author Dyx
 */
function buildPayload(): SystemConfigData {
  return {
    id: form.id,
    storageType: form.storageType,
    ossEndpoint: form.ossEndpoint,
    ossRegion: form.ossRegion,
    ossBucketName: form.ossBucketName,
    ossPublicUrlPrefix: form.ossPublicUrlPrefix,
    ossBaseDir: form.ossBaseDir,
    ipLookupEnabled: form.ipLookupEnabled,
    ipLookupApiUrl: form.ipLookupApiUrl,
    copyrightText: form.copyrightText,
    techSupportText: form.techSupportText,
    homeActivityEnablePosts: form.homeActivityEnablePosts,
    homeActivityEnableMoments: form.homeActivityEnableMoments,
    homeActivityEnableProjects: form.homeActivityEnableProjects,
    homeActivityEnableWorks: form.homeActivityEnableWorks,
    homeActivityEnableHonors: form.homeActivityEnableHonors,
    homeActivityMaxItems: form.homeActivityMaxItems,
    homeActivityMaxItemsPerType: form.homeActivityMaxItemsPerType
  };
}

/**
 * 保存当前系统配置。
 *
 * @returns 返回异步保存结果；成功后会回填接口返回的最新配置状态。
 * @throws 该函数不会主动向外抛出异常；保存失败时会通过页面提示反馈。
 * @author Dyx
 */
async function handleSave(): Promise<void> {
  if (saving.value) {
    return;
  }
  saving.value = true;
  try {
    const result = await updateAdminSystemConfig(buildPayload());
    const updatedConfig = (result as { data?: SystemConfigData })?.data;
    if (updatedConfig) {
      applyFormData(updatedConfig);
    }
    ElMessage.success('系统配置保存成功');
  } catch (error) {
    ElMessage.error(resolveErrorMessage(error, '系统配置保存失败'));
  } finally {
    saving.value = false;
  }
}

onMounted(() => {
  void loadSystemConfig();
});
</script>
