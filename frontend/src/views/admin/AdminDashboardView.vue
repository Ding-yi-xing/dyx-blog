<template>
  <div class="space-y-6">
    <section class="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
      <article
        v-for="item in summaryCards"
        :key="item.label"
        class="group relative overflow-hidden rounded-[28px] border border-white/70 bg-white p-6 shadow-sm shadow-slate-200/70 transition duration-300 hover:-translate-y-1 hover:shadow-xl hover:shadow-slate-200/80"
      >
        <div class="absolute inset-x-0 top-0 h-1 bg-gradient-to-r" :class="item.accent"></div>
        <div class="flex items-start justify-between gap-4">
          <div>
            <p class="text-xs font-medium uppercase tracking-[0.22em] text-slate-400">{{ item.eyebrow }}</p>
            <h2 class="mt-3 text-sm font-medium text-slate-500">{{ item.label }}</h2>
          </div>
          <div class="flex h-12 w-12 items-center justify-center rounded-2xl text-white shadow-lg" :class="item.iconBg">
            <el-icon :size="22">
              <component :is="item.icon" />
            </el-icon>
          </div>
        </div>
        <div class="mt-8 flex items-end justify-between gap-3">
          <p class="text-4xl font-semibold tracking-tight text-slate-950">{{ item.value }}</p>
          <span class="rounded-full px-3 py-1 text-xs font-medium" :class="item.badgeClass">{{ item.badge }}</span>
        </div>
        <p class="mt-3 text-sm leading-6 text-slate-500">{{ item.description }}</p>
      </article>
    </section>

    <section class="grid gap-6 xl:grid-cols-[1.65fr_1fr]">
      <article class="rounded-[32px] border border-white/70 bg-white p-6 shadow-sm shadow-slate-200/70">
        <div class="flex flex-wrap items-start justify-between gap-4">
          <div>
            <p class="text-xs font-medium uppercase tracking-[0.26em] text-slate-400">Traffic trend</p>
            <h2 class="mt-3 text-2xl font-semibold tracking-tight text-slate-950">最近 7 天访问趋势</h2>
            <p class="mt-2 text-sm text-slate-500">观察最近一周访问热度变化，快速判断站点内容曝光节奏。</p>
          </div>
          <div class="rounded-2xl bg-slate-100 px-4 py-3 text-right">
            <p class="text-xs uppercase tracking-[0.22em] text-slate-400">最近 7 天</p>
            <p class="mt-2 text-2xl font-semibold text-slate-950">{{ recentSevenDaysVisits }}</p>
          </div>
        </div>
        <div class="mt-6 overflow-hidden rounded-[28px] border border-slate-100 bg-slate-50/80 p-4">
          <v-chart v-if="hasTrendData" class="h-[320px] w-full" :option="trendChartOption" autoresize />
          <div v-else class="flex h-[320px] items-center justify-center rounded-[24px] border border-dashed border-slate-200 bg-white text-sm text-slate-400">
            暂无访问数据
          </div>
        </div>
      </article>

      <article class="rounded-[32px] border border-white/70 bg-white p-6 shadow-sm shadow-slate-200/70">
        <div class="flex items-start justify-between gap-4">
          <div>
            <p class="text-xs font-medium uppercase tracking-[0.26em] text-slate-400">Device share</p>
            <h2 class="mt-3 text-2xl font-semibold tracking-tight text-slate-950">设备分布</h2>
            <p class="mt-2 text-sm text-slate-500">识别访问来源以优化桌面端与移动端内容体验。</p>
          </div>
          <div class="flex h-12 w-12 items-center justify-center rounded-2xl bg-slate-900 text-white shadow-lg shadow-slate-300/60">
            <el-icon :size="22"><Monitor /></el-icon>
          </div>
        </div>
        <div class="mt-6 overflow-hidden rounded-[28px] border border-slate-100 bg-slate-50/80 p-4">
          <v-chart v-if="hasDeviceData" class="h-[320px] w-full" :option="deviceChartOption" autoresize />
          <div v-else class="flex h-[320px] items-center justify-center rounded-[24px] border border-dashed border-slate-200 bg-white text-sm text-slate-400">
            暂无设备分布数据
          </div>
        </div>
      </article>
    </section>

    <section>
      <article class="rounded-[32px] border border-white/70 bg-white p-6 shadow-sm shadow-slate-200/70">
        <div class="flex items-start justify-between gap-4">
          <div>
            <p class="text-xs font-medium uppercase tracking-[0.26em] text-slate-400">Top pages</p>
            <h2 class="mt-3 text-2xl font-semibold tracking-tight text-slate-950">热门访问页面</h2>
            <p class="mt-2 text-sm text-slate-500">按访问次数排序，定位当前最受关注的内容入口。</p>
          </div>
          <div class="flex h-12 w-12 items-center justify-center rounded-2xl bg-amber-500 text-white shadow-lg shadow-amber-200/70">
            <el-icon :size="22"><TrophyBase /></el-icon>
          </div>
        </div>

        <div v-if="topPages.length" class="mt-6 space-y-4">
          <article
            v-for="(item, index) in topPages"
            :key="`${item.pageKey}-${index}`"
            class="rounded-[24px] border border-slate-100 bg-slate-50/80 p-4"
          >
            <div class="flex items-center gap-4">
              <div class="flex h-11 w-11 shrink-0 items-center justify-center rounded-2xl text-sm font-semibold text-white" :class="rankBadgeClass(index)">
                {{ index + 1 }}
              </div>
              <div class="min-w-0 flex-1">
                <div class="flex items-center justify-between gap-3">
                  <h3 class="truncate text-base font-medium text-slate-900">{{ item.label }}</h3>
                  <span class="text-sm font-semibold text-slate-950">{{ item.visitCount }}</span>
                </div>
                <div class="mt-3 h-2 overflow-hidden rounded-full bg-white">
                  <div class="h-full rounded-full bg-gradient-to-r from-slate-900 via-slate-700 to-slate-400" :style="{ width: `${resolvePageBarWidth(item.visitCount)}%` }"></div>
                </div>
                <p class="mt-3 text-xs text-slate-400">最近访问：{{ formatDateTime(item.lastVisitAt) }}</p>
              </div>
            </div>
          </article>
        </div>

        <div v-else class="mt-6 flex min-h-[320px] items-center justify-center rounded-[28px] border border-dashed border-slate-200 bg-slate-50 text-sm text-slate-400">
          暂无页面排行数据
        </div>
      </article>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { use } from 'echarts/core';
import { CanvasRenderer } from 'echarts/renderers';
import { LineChart, PieChart } from 'echarts/charts';
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components';
import VChart from 'vue-echarts';
import {
  DataAnalysis,
  Document,
  Monitor,
  Reading,
  Timer,
  TrophyBase,
  User
} from '@element-plus/icons-vue';
import {
  getDashboardSummary,
  type DashboardSummaryData,
  type DeviceTypeStat,
  type PageVisitStat,
  type VisitTrendPoint
} from '@/api/modules/admin';

use([CanvasRenderer, LineChart, PieChart, GridComponent, TooltipComponent, LegendComponent]);

const summary = ref<DashboardSummaryData>({});

const trendPoints = computed<VisitTrendPoint[]>(() => summary.value.dailySiteVisits ?? []);
const deviceStats = computed<DeviceTypeStat[]>(() => summary.value.deviceTypeDistribution ?? []);
const topPages = computed<PageVisitStat[]>(() => summary.value.topVisitedPages ?? []);

const hasTrendData = computed(() => trendPoints.value.some((item) => Number(item.visitCount) > 0));
const hasDeviceData = computed(() => deviceStats.value.some((item) => Number(item.visitCount) > 0));
const recentSevenDaysVisits = computed(() => trendPoints.value.reduce((total, item) => total + Number(item.visitCount ?? 0), 0));
const maxTopPageVisits = computed(() => Math.max(...topPages.value.map((item) => Number(item.visitCount ?? 0)), 1));

const summaryCards = computed(() => [
  {
    label: '文章总数',
    eyebrow: 'Content',
    value: String(summary.value.postCount ?? 0),
    description: '当前后台可管理的博客文章总量。',
    badge: '内容池',
    badgeClass: 'bg-sky-100 text-sky-700',
    icon: Document,
    iconBg: 'bg-sky-500 shadow-sky-200/70',
    accent: 'from-sky-500 via-cyan-400 to-cyan-200'
  },
  {
    label: '动态数量',
    eyebrow: 'Moments',
    value: String(summary.value.momentCount ?? 0),
    description: '过程记录与阶段更新的沉淀数量。',
    badge: '实时更新',
    badgeClass: 'bg-violet-100 text-violet-700',
    icon: Timer,
    iconBg: 'bg-violet-500 shadow-violet-200/70',
    accent: 'from-violet-500 via-fuchsia-400 to-fuchsia-200'
  },
  {
    label: '博客总浏览量',
    eyebrow: 'Reading',
    value: String(summary.value.totalPostViews ?? 0),
    description: '博客详情页累计阅读次数，衡量内容消费热度。',
    badge: '阅读热度',
    badgeClass: 'bg-amber-100 text-amber-700',
    icon: Reading,
    iconBg: 'bg-amber-500 shadow-amber-200/70',
    accent: 'from-amber-500 via-orange-400 to-orange-200'
  },
  {
    label: '全站访问量',
    eyebrow: 'Traffic',
    value: String(summary.value.totalSiteVisits ?? 0),
    description: '公开站点页面访问累计次数与流量概览。',
    badge: '访客轨迹',
    badgeClass: 'bg-emerald-100 text-emerald-700',
    icon: DataAnalysis,
    iconBg: 'bg-emerald-500 shadow-emerald-200/70',
    accent: 'from-emerald-500 via-teal-400 to-cyan-200'
  },
  {
    label: '荣誉数量',
    eyebrow: 'Honor',
    value: String(summary.value.honorCount ?? 0),
    description: '用于展示成果证明与证书沉淀的条目数。',
    badge: '成果证明',
    badgeClass: 'bg-rose-100 text-rose-700',
    icon: TrophyBase,
    iconBg: 'bg-rose-500 shadow-rose-200/70',
    accent: 'from-rose-500 via-pink-400 to-pink-200'
  },
  {
    label: '用户数量',
    eyebrow: 'Admin',
    value: String(summary.value.userCount ?? 0),
    description: '后台当前可登录管理账号数量。',
    badge: '权限管理',
    badgeClass: 'bg-slate-200 text-slate-700',
    icon: User,
    iconBg: 'bg-slate-700 shadow-slate-200/70',
    accent: 'from-slate-900 via-slate-700 to-slate-400'
  }
]);

const trendChartOption = computed(() => ({
  tooltip: {
    trigger: 'axis',
    backgroundColor: 'rgba(15, 23, 42, 0.9)',
    borderWidth: 0,
    textStyle: {
      color: '#fff'
    }
  },
  grid: {
    top: 28,
    left: 10,
    right: 10,
    bottom: 8,
    containLabel: true
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: trendPoints.value.map((item) => item.label),
    axisLine: {
      lineStyle: {
        color: '#cbd5e1'
      }
    },
    axisLabel: {
      color: '#64748b'
    },
    axisTick: {
      show: false
    }
  },
  yAxis: {
    type: 'value',
    minInterval: 1,
    splitLine: {
      lineStyle: {
        color: '#e2e8f0',
        type: 'dashed'
      }
    },
    axisLabel: {
      color: '#64748b'
    }
  },
  series: [
    {
      data: trendPoints.value.map((item) => Number(item.visitCount ?? 0)),
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 10,
      lineStyle: {
        width: 4,
        color: '#0f172a'
      },
      itemStyle: {
        color: '#ffffff',
        borderColor: '#0f172a',
        borderWidth: 3
      },
      areaStyle: {
        color: {
          type: 'linear',
          x: 0,
          y: 0,
          x2: 0,
          y2: 1,
          colorStops: [
            { offset: 0, color: 'rgba(15, 23, 42, 0.24)' },
            { offset: 1, color: 'rgba(15, 23, 42, 0.02)' }
          ]
        }
      }
    }
  ]
}));

const deviceChartOption = computed(() => ({
  tooltip: {
    trigger: 'item',
    backgroundColor: 'rgba(15, 23, 42, 0.9)',
    borderWidth: 0,
    textStyle: {
      color: '#fff'
    }
  },
  legend: {
    bottom: 0,
    icon: 'circle',
    textStyle: {
      color: '#475569'
    }
  },
  series: [
    {
      type: 'pie',
      radius: ['48%', '72%'],
      center: ['50%', '44%'],
      avoidLabelOverlap: true,
      itemStyle: {
        borderRadius: 14,
        borderColor: '#ffffff',
        borderWidth: 4
      },
      label: {
        formatter: '{b}\n{d}%',
        color: '#0f172a',
        fontSize: 12,
        fontWeight: 600
      },
      data: deviceStats.value.map((item, index) => ({
        name: item.label,
        value: Number(item.visitCount ?? 0),
        itemStyle: {
          color: ['#0f172a', '#06b6d4', '#f59e0b', '#8b5cf6', '#94a3b8'][index % 5]
        }
      }))
    }
  ]
}));

function resolvePageBarWidth(value: number): number {
  if (!maxTopPageVisits.value) {
    return 0;
  }
  return Math.max((Number(value ?? 0) / maxTopPageVisits.value) * 100, 8);
}

function rankBadgeClass(index: number): string {
  if (index === 0) {
    return 'bg-slate-950';
  }
  if (index === 1) {
    return 'bg-slate-700';
  }
  if (index === 2) {
    return 'bg-slate-500';
  }
  return 'bg-slate-300 text-slate-700';
}

function formatDateTime(value?: string): string {
  if (!value) {
    return '暂无时间';
  }
  return value.replace('T', ' ').slice(0, 16);
}

async function loadDashboardSummary(): Promise<void> {
  const response = await getDashboardSummary();
  summary.value = response.data ?? {};
}

onMounted(() => {
  void loadDashboardSummary();
});
</script>
