<template>
  <section class="space-y-6">
    <article class="rounded-[28px] bg-white p-6 shadow-sm">
      <div class="flex flex-wrap items-start justify-between gap-4">
        <div>
          <h2 class="text-xl font-semibold text-slate-900">首页管理 / 足迹</h2>
          <p class="mt-2 text-sm text-slate-500">维护首页第二屏的足迹点位、展示顺序和文案内容。</p>
        </div>
        <el-button type="primary" @click="openCreateDialog">新建足迹</el-button>
      </div>

      <article class="mt-6 rounded-[24px] border border-slate-200 bg-slate-50 p-5">
        <div class="flex items-center justify-between gap-3">
          <div>
            <h3 class="text-base font-semibold text-slate-900">第二屏文案配置</h3>
            <p class="text-xs text-slate-500">控制首页地图屏左上角的标题和说明文字。</p>
          </div>
          <el-button type="primary" plain :loading="copySaving" @click="handleSaveCopy">保存文案</el-button>
        </div>

        <el-form label-position="top" class="mt-5 grid gap-4 md:grid-cols-2">
          <el-form-item label="眉标题">
            <el-input v-model="systemConfigForm.footprintEyebrow" maxlength="120" show-word-limit placeholder="如：Footprints" />
          </el-form-item>
          <el-form-item label="主标题">
            <el-input v-model="systemConfigForm.footprintTitle" maxlength="200" show-word-limit placeholder="如：我去过的地方" />
          </el-form-item>
          <el-form-item label="副标题" class="md:col-span-2">
            <el-input v-model="systemConfigForm.footprintSubtitle" maxlength="255" show-word-limit placeholder="补充一行短说明" />
          </el-form-item>
          <el-form-item label="描述文案" class="md:col-span-2">
            <el-input
              v-model="systemConfigForm.footprintDescription"
              type="textarea"
              :rows="3"
              maxlength="500"
              show-word-limit
              placeholder="补充第二行说明文案"
            />
          </el-form-item>
        </el-form>
      </article>
    </article>

    <article class="rounded-[28px] bg-white p-6 shadow-sm">
      <el-table :data="footprints" border>
        <el-table-column prop="cityName" label="城市" min-width="140" />
        <el-table-column prop="regionLabel" label="地区" min-width="180" />
        <el-table-column prop="districtName" label="区县" min-width="140" />
        <el-table-column prop="visitedAt" label="到访时间" width="180" />
        <el-table-column prop="importance" label="权重" width="80" />
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column prop="statusText" label="状态" width="100" />
        <el-table-column prop="updatedAt" label="更新时间" width="180" />
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="openEditDialog(scope.row.raw)">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(scope.row.raw)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </article>

    <el-dialog v-model="dialogVisible" :title="form.id ? '编辑足迹' : '新建足迹'" width="760px">
      <el-form label-position="top">
        <el-form-item label="省 / 市 / 区县">
          <el-cascader
            v-model="regionSelection"
            :options="chinaRegionOptions"
            :props="{ emitPath: true, checkStrictly: false, value: 'value', label: 'label' }"
            class="!w-full"
            placeholder="请选择省市区"
            clearable
            filterable
          />
        </el-form-item>

        <div class="grid gap-4 sm:grid-cols-2">
          <el-form-item label="到访时间">
            <el-date-picker v-model="form.visitedAt" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="选择时间" class="!w-full" />
          </el-form-item>
          <el-form-item label="发布状态">
            <el-select v-model="form.published" class="!w-full">
              <el-option label="草稿" :value="0" />
              <el-option label="已发布" :value="1" />
            </el-select>
          </el-form-item>
        </div>

        <el-form-item label="足迹说明">
          <el-input v-model="form.description" type="textarea" :rows="4" placeholder="记录这次到访的简短说明" />
        </el-form-item>

        <div class="grid gap-4 sm:grid-cols-2">
          <el-form-item label="重要程度">
            <el-input-number v-model="form.importance" :min="1" :max="5" class="!w-full" />
          </el-form-item>
          <el-form-item label="排序">
            <el-input-number v-model="form.sortOrder" :min="0" class="!w-full" />
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
import {
  deleteAdminFootprint,
  getAdminFootprints,
  getAdminSystemConfig,
  saveAdminFootprint,
  updateAdminSystemConfig,
  type SystemConfigData
} from '@/api/modules/admin';
import type { FootprintData } from '@/api/modules/site';
import { resolveErrorMessage } from '@/utils/error';
import { chinaRegionOptions, findChinaRegionSelection, resolveChinaRegionDistrictName } from '@/constants/chinaRegions';

interface FootprintTableRow extends FootprintData {
  regionLabel: string;
  districtName: string;
  statusText: string;
  raw: FootprintData;
}

/**
 * 后台足迹管理页。
 * 负责维护首页足迹点位列表，以及首页第二屏足迹文案配置。
 */
const rawList = ref<FootprintData[]>([]);
const dialogVisible = ref(false);
const saving = ref(false);
const copySaving = ref(false);
const regionSelection = ref<string[]>([]);
const districtMap = ref<Record<number, string>>({});

const form = reactive<Partial<FootprintData>>({
  id: undefined,
  cityName: '',
  countryName: '中国',
  regionName: '',
  visitedAt: '',
  description: '',
  importance: 3,
  sortOrder: 0,
  published: 1
});

const systemConfigForm = reactive<SystemConfigData>({
  id: 1,
  storageType: 'local',
  ossEndpoint: '',
  ossRegion: '',
  ossBucketName: '',
  ossPublicUrlPrefix: '',
  ossBaseDir: '',
  footprintEyebrow: '',
  footprintTitle: '',
  footprintSubtitle: '',
  footprintDescription: '',
  copyrightText: '',
  techSupportText: ''
});

/**
 * 将后台原始足迹列表转换为表格展示所需的衍生字段。
 */
const footprints = computed<FootprintTableRow[]>(() =>
  rawList.value.map((item) => ({
    ...item,
    regionLabel: [item.countryName, item.regionName].filter(Boolean).join(' / ') || '-',
    districtName: item.id ? districtMap.value[item.id] || '-' : '-',
    statusText: item.published === 1 ? '已发布' : '草稿',
    raw: item
  }))
);

/**
 * 将系统配置接口返回的数据回填到足迹文案表单。
 *
 * @param data 系统配置数据。
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；缺失字段会回退到默认值。
 * @author Dyx
 */
function applySystemConfig(data?: SystemConfigData): void {
  systemConfigForm.id = data?.id ?? 1;
  systemConfigForm.storageType = data?.storageType ?? 'local';
  systemConfigForm.ossEndpoint = data?.ossEndpoint ?? '';
  systemConfigForm.ossRegion = data?.ossRegion ?? '';
  systemConfigForm.ossBucketName = data?.ossBucketName ?? '';
  systemConfigForm.ossPublicUrlPrefix = data?.ossPublicUrlPrefix ?? '';
  systemConfigForm.ossBaseDir = data?.ossBaseDir ?? '';
  systemConfigForm.footprintEyebrow = data?.footprintEyebrow ?? '';
  systemConfigForm.footprintTitle = data?.footprintTitle ?? '';
  systemConfigForm.footprintSubtitle = data?.footprintSubtitle ?? '';
  systemConfigForm.footprintDescription = data?.footprintDescription ?? '';
  systemConfigForm.copyrightText = data?.copyrightText ?? '';
  systemConfigForm.techSupportText = data?.techSupportText ?? '';
}

/**
 * 重置足迹表单与地区选择状态，供新建和编辑前统一复用。
 *
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；仅重置本地表单状态。
 * @author Dyx
 */
function resetForm(): void {
  Object.assign(form, {
    id: undefined,
    cityName: '',
    countryName: '中国',
    regionName: '',
    visitedAt: '',
    description: '',
    importance: 3,
    sortOrder: 0,
    published: 1
  });
  regionSelection.value = [];
}

/**
 * 根据足迹已有地区信息反推出级联选择器的选中路径。
 *
 * @param item 足迹数据对象。
 * @returns 返回级联选择器需要的地区路径数组。
 * @throws 该函数不会主动抛出异常；无法命中时返回空数组。
 * @author Dyx
 */
function resolveSelectionFromFootprint(item: FootprintData): string[] {
  return findChinaRegionSelection(item.regionName, item.cityName);
}

/**
 * 构建足迹 ID 到区县名称的映射表，供表格快速展示。
 *
 * @param list 足迹列表。
 * @returns 返回按足迹 ID 建立的区县名称映射。
 * @throws 该函数不会主动抛出异常；缺失主键的记录会被忽略。
 * @author Dyx
 */
function buildDistrictMap(list: FootprintData[]): Record<number, string> {
  const nextMap: Record<number, string> = {};
  for (const item of list) {
    if (!item.id) {
      continue;
    }
    nextMap[item.id] = resolveChinaRegionDistrictName(item.regionName, item.cityName);
  }
  return nextMap;
}

/**
 * 同时加载足迹列表与系统配置，并刷新页面所需的所有数据源。
 *
 * @returns 返回异步加载结果；成功后会更新足迹表格和足迹文案表单。
 * @throws 该函数不会主动抛出同步异常；接口失败时会以 Promise reject 形式返回。
 * @author Dyx
 */
async function loadPageData(): Promise<void> {
  const [footprintResponse, systemConfigResponse] = await Promise.all([getAdminFootprints(), getAdminSystemConfig()]);
  const nextList = footprintResponse.data ?? [];
  rawList.value = nextList;
  districtMap.value = buildDistrictMap(nextList);
  applySystemConfig(systemConfigResponse.data);
}

/**
 * 打开新建足迹弹窗，并初始化为空表单。
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
 * 打开编辑足迹弹窗，并将当前足迹数据回填到表单与地区选择器中。
 *
 * @param item 待编辑的足迹数据。
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；仅执行表单与地区回填。
 * @author Dyx
 */
function openEditDialog(item: FootprintData): void {
  resetForm();
  Object.assign(form, {
    ...item,
    countryName: item.countryName || '中国'
  });
  regionSelection.value = resolveSelectionFromFootprint(item);
  dialogVisible.value = true;
}

/**
 * 保存首页足迹区域的文案配置。
 *
 * @returns 返回异步保存结果。
 * @throws 该函数不会主动向外抛出异常；保存失败时会通过页面提示反馈。
 * @author Dyx
 */
async function handleSaveCopy(): Promise<void> {
  if (copySaving.value) {
    return;
  }
  copySaving.value = true;
  try {
    const response = await updateAdminSystemConfig({
      id: systemConfigForm.id,
      footprintEyebrow: systemConfigForm.footprintEyebrow,
      footprintTitle: systemConfigForm.footprintTitle,
      footprintSubtitle: systemConfigForm.footprintSubtitle,
      footprintDescription: systemConfigForm.footprintDescription
    });
    applySystemConfig(response.data);
    ElMessage.success('足迹文案保存成功');
  } catch (error) {
    ElMessage.error(resolveErrorMessage(error, '足迹文案保存失败'));
  } finally {
    copySaving.value = false;
  }
}

/**
 * 保存当前足迹表单。
 * 会把级联选择器中的省市结果转换为接口所需的 regionName 与 cityName 字段。
 *
 * @returns 返回异步保存结果。
 * @throws 该函数不会主动向外抛出异常；地区未选择完整或保存失败时会通过页面提示反馈。
 * @author Dyx
 */
async function handleSave(): Promise<void> {
  if (saving.value) {
    return;
  }
  if (regionSelection.value.length < 2) {
    ElMessage.warning('请先选择省市区');
    return;
  }
  const [province, city] = regionSelection.value;
  saving.value = true;
  try {
    await saveAdminFootprint({
      ...form,
      countryName: '中国',
      regionName: province,
      cityName: city,
      positionX: form.positionX ?? 0,
      positionY: form.positionY ?? 0
    });
    ElMessage.success(form.id ? '足迹更新成功' : '足迹创建成功');
    dialogVisible.value = false;
    await loadPageData();
  } catch (error) {
    ElMessage.error(resolveErrorMessage(error, '足迹保存失败'));
  } finally {
    saving.value = false;
  }
}

/**
 * 删除指定足迹，并在用户确认后刷新当前列表。
 *
 * @param item 待删除的足迹数据。
 * @returns 返回异步删除结果。
 * @throws 该函数不会主动向外抛出异常；取消删除时会静默结束，失败时通过页面提示反馈。
 * @author Dyx
 */
async function handleDelete(item: FootprintData): Promise<void> {
  try {
    await ElMessageBox.confirm(`确认删除足迹“${item.cityName}”吗？`, '删除确认', {
      type: 'warning'
    });
    await deleteAdminFootprint(item.id as number);
    ElMessage.success('足迹删除成功');
    await loadPageData();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('足迹删除失败');
    }
  }
}

onMounted(() => {
  void loadPageData();
});
</script>
