<template>
  <section class="space-y-6">
    <div class="rounded-[28px] border border-white/70 bg-white p-6 shadow-sm shadow-slate-200/70">
      <div class="flex flex-wrap items-start justify-between gap-4">
        <div>
          <p class="text-xs font-medium uppercase tracking-[0.24em] text-slate-400">Visit logs</p>
          <h2 class="mt-3 text-2xl font-semibold tracking-tight text-slate-950">访问日志</h2>
          <p class="mt-2 text-sm text-slate-500">按时间倒序查看公开站点访问明细，包含页面、IP、设备类型、设备名称与 User-Agent。</p>
        </div>
        <div class="flex flex-wrap items-center justify-end gap-3">
          <div class="rounded-2xl bg-slate-100 px-4 py-3 text-right">
            <p class="text-xs uppercase tracking-[0.22em] text-slate-400">当前记录</p>
            <p class="mt-2 text-2xl font-semibold text-slate-950">{{ logs.length }}</p>
          </div>
          <el-button type="danger" plain :disabled="!selectedIds.length" @click="handleBatchDelete">批量删除</el-button>
        </div>
      </div>
    </div>

    <div class="overflow-hidden rounded-[28px] border border-white/70 bg-white shadow-sm shadow-slate-200/70">
      <div class="border-b border-slate-100 px-5 py-4">
        <div class="flex flex-wrap items-end gap-3">
          <div class="min-w-[280px] flex-1">
            <p class="mb-2 text-xs font-medium uppercase tracking-[0.18em] text-slate-400">访问时间</p>
            <el-date-picker
              v-model="filters.timeRange"
              type="datetimerange"
              range-separator="至"
              start-placeholder="开始时间"
              end-placeholder="结束时间"
              value-format="YYYY-MM-DD HH:mm:ss"
              class="!w-full"
            />
          </div>
          <div class="min-w-[160px]">
            <p class="mb-2 text-xs font-medium uppercase tracking-[0.18em] text-slate-400">页面</p>
            <el-select v-model="filters.pageKey" clearable placeholder="全部页面" class="!w-full">
              <el-option v-for="item in pageOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </div>
          <div class="min-w-[160px]">
            <p class="mb-2 text-xs font-medium uppercase tracking-[0.18em] text-slate-400">设备类型</p>
            <el-select v-model="filters.deviceType" clearable placeholder="全部设备" class="!w-full">
              <el-option v-for="item in deviceTypeOptions" :key="item.value" :label="item.label" :value="item.value" />
            </el-select>
          </div>
          <div class="min-w-[180px] flex-1">
            <p class="mb-2 text-xs font-medium uppercase tracking-[0.18em] text-slate-400">IP</p>
            <el-input v-model="filters.ipAddress" clearable placeholder="输入访问 IP" />
          </div>
          <div class="flex gap-3">
            <el-button @click="handleReset">重置</el-button>
            <el-button type="primary" @click="handleQuery">查询</el-button>
          </div>
        </div>
      </div>

      <el-table ref="tableRef" :data="logs" stripe @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="52" align="center" />
        <el-table-column label="访问时间" min-width="170">
          <template #default="scope">
            {{ formatDateTime(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="pageLabel" label="页面" min-width="120" />
        <el-table-column prop="ipAddress" label="IP" min-width="150" />
        <el-table-column prop="deviceTypeLabel" label="设备类型" min-width="120" />
        <el-table-column prop="deviceName" label="设备名称" min-width="140" />
        <el-table-column label="User-Agent" min-width="360">
          <template #default="scope">
            <div class="whitespace-normal break-all text-xs leading-6 text-slate-500">
              {{ scope.row.userAgent || 'unknown' }}
            </div>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button link type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div class="flex items-center justify-end border-t border-slate-100 bg-slate-50 px-5 py-3">
        <el-pagination
          background
          layout="prev, pager, next, sizes, total"
          :current-page="page"
          :page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          @current-change="handlePageChange"
          @size-change="handlePageSizeChange"
        />
      </div>
      <div v-if="!logs.length" class="flex min-h-[240px] items-center justify-center border-t border-slate-100 bg-slate-50 text-sm text-slate-400">
        暂无访问日志
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ElMessage, ElMessageBox } from 'element-plus';
import { onMounted, reactive, ref } from 'vue';
import {
  deleteAdminVisitLog,
  deleteAdminVisitLogs,
  getAdminVisitLogs,
  type AdminVisitLogQuery,
  type RecentVisitRecord
} from '@/api/modules/admin';
import { resolveErrorMessage } from '@/utils/error';
import { formatDateTime } from '@/utils/date';

/**
 * 后台访问日志管理页。
 * 负责按条件查询访问日志，并提供单条删除、批量删除与分页浏览能力。
 */
const logs = ref<RecentVisitRecord[]>([]);
const selectedIds = ref<number[]>([]);
const tableRef = ref<{ clearSelection: () => void } | null>(null);
const page = ref(1);
const pageSize = ref(20);
const total = ref(0);

const filters = reactive<{ timeRange: string[]; pageKey: string; deviceType: string; ipAddress: string }>({
  timeRange: [],
  pageKey: '',
  deviceType: '',
  ipAddress: ''
});

const pageOptions = [
  { label: '首页', value: 'home' },
  { label: '关于我', value: 'profile' },
  { label: '简历', value: 'resume' },
  { label: '留言', value: 'guestbook' },
  { label: '动态', value: 'moments' },
  { label: '博客', value: 'blog' },
  { label: '博客详情', value: 'blog-detail' }
];

const deviceTypeOptions = [
  { label: '桌面端', value: 'DESKTOP' },
  { label: '手机端', value: 'MOBILE' },
  { label: '平板端', value: 'TABLET' },
  { label: '爬虫 / Bot', value: 'BOT' },
  { label: '未知设备', value: 'UNKNOWN' }
];

/**
 * 根据当前筛选器和分页状态构建日志查询参数。
 *
 * @returns 返回访问日志接口需要的查询参数对象。
 * @throws 该函数不会主动抛出异常；仅执行本地参数整理。
 * @author Dyx
 */
function buildQueryParams(): AdminVisitLogQuery {
  return {
    startTime: filters.timeRange[0] || undefined,
    endTime: filters.timeRange[1] || undefined,
    pageKey: filters.pageKey || undefined,
    deviceType: filters.deviceType || undefined,
    ipAddress: filters.ipAddress.trim() || undefined,
    page: page.value,
    pageSize: pageSize.value
  };
}

/**
 * 获取访问日志列表并同步分页信息。
 *
 * @returns 返回异步加载结果；成功后会更新日志列表、总数和当前分页状态。
 * @throws 该函数不会主动抛出同步异常；接口失败时会以 Promise reject 形式返回。
 * @author Dyx
 */
async function loadVisitLogs(): Promise<void> {
  const response = await getAdminVisitLogs(buildQueryParams());
  const data = (response as {
    data?: {
      records?: RecentVisitRecord[];
      total?: number;
      page?: number;
      pageSize?: number;
    };
  }).data;

  if (data) {
    logs.value = data.records ?? [];
    total.value = data.total ?? 0;
    page.value = data.page ?? page.value;
    pageSize.value = data.pageSize ?? pageSize.value;
  } else {
    logs.value = [];
    total.value = 0;
  }
}

/**
 * 清空表格选中状态与本地已选日志 ID。
 *
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；仅重置本地选择状态。
 * @author Dyx
 */
function resetSelection(): void {
  tableRef.value?.clearSelection();
  selectedIds.value = [];
}

/**
 * 同步表格多选结果到本地 ID 列表。
 *
 * @param selection 当前表格选中的访问日志集合。
 * @returns 无返回值。
 * @throws 该函数不会主动抛出异常；非法 ID 会在转换时被过滤。
 * @author Dyx
 */
function handleSelectionChange(selection: RecentVisitRecord[]): void {
  selectedIds.value = selection.map((item) => Number(item.id)).filter((id) => !Number.isNaN(id));
}

/**
 * 删除单条访问日志，并在确认后刷新列表。
 *
 * @param item 待删除的访问日志数据。
 * @returns 返回异步删除结果。
 * @throws 该函数不会主动向外抛出异常；取消删除时会静默结束，失败时通过页面提示反馈。
 * @author Dyx
 */
async function handleDelete(item: RecentVisitRecord): Promise<void> {
  try {
    await ElMessageBox.confirm(`确认删除 ${item.pageLabel} 的这条访问日志吗？`, '删除确认', {
      type: 'warning'
    });
    await deleteAdminVisitLog(Number(item.id));
    ElMessage.success('访问日志删除成功');
    await loadVisitLogs();
    resetSelection();
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      return;
    }
    ElMessage.error(resolveErrorMessage(error, '访问日志删除失败'));
  }
}

/**
 * 批量删除当前选中的访问日志。
 *
 * @returns 返回异步删除结果；成功后会刷新列表并清空选中状态。
 * @throws 该函数不会主动向外抛出异常；取消删除时会静默结束，失败时通过页面提示反馈。
 * @author Dyx
 */
async function handleBatchDelete(): Promise<void> {
  if (!selectedIds.value.length) {
    return;
  }
  try {
    await ElMessageBox.confirm(`确认删除选中的 ${selectedIds.value.length} 条访问日志吗？`, '批量删除确认', {
      type: 'warning'
    });
    await deleteAdminVisitLogs(selectedIds.value);
    ElMessage.success('访问日志批量删除成功');
    await loadVisitLogs();
    resetSelection();
  } catch (error) {
    if (error === 'cancel' || error === 'close') {
      return;
    }
    ElMessage.error(resolveErrorMessage(error, '访问日志批量删除失败'));
  }
}

/**
 * 切换分页页码并重新加载数据。
 *
 * @param newPage 新的页码。
 * @returns 返回异步处理结果。
 * @throws 该函数不会主动向外抛出异常；接口失败时会以 Promise reject 形式返回。
 * @author Dyx
 */
async function handlePageChange(newPage: number): Promise<void> {
  page.value = newPage;
  await loadVisitLogs();
  resetSelection();
}

/**
 * 切换每页条数并回到第一页重新查询。
 *
 * @param newSize 新的分页大小。
 * @returns 返回异步处理结果。
 * @throws 该函数不会主动向外抛出异常；接口失败时会以 Promise reject 形式返回。
 * @author Dyx
 */
async function handlePageSizeChange(newSize: number): Promise<void> {
  pageSize.value = newSize;
  page.value = 1;
  await loadVisitLogs();
  resetSelection();
}

/**
 * 按当前筛选条件重新查询访问日志。
 *
 * @returns 返回异步查询结果；成功后会重置到第一页并刷新列表。
 * @throws 该函数不会主动向外抛出异常；接口失败时会以 Promise reject 形式返回。
 * @author Dyx
 */
async function handleQuery(): Promise<void> {
  page.value = 1;
  await loadVisitLogs();
  resetSelection();
}

/**
 * 重置筛选条件并重新加载访问日志。
 *
 * @returns 返回异步处理结果；成功后会恢复默认筛选状态。
 * @throws 该函数不会主动向外抛出异常；接口失败时会以 Promise reject 形式返回。
 * @author Dyx
 */
async function handleReset(): Promise<void> {
  filters.timeRange = [];
  filters.pageKey = '';
  filters.deviceType = '';
  filters.ipAddress = '';
  page.value = 1;
  await loadVisitLogs();
  resetSelection();
}

onMounted(() => {
  void loadVisitLogs();
});
</script>
