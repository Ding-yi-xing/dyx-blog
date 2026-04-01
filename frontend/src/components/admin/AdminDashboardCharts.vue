<template>
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
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { use } from 'echarts/core';
import { CanvasRenderer } from 'echarts/renderers';
import { LineChart, PieChart } from 'echarts/charts';
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components';
import VChart from 'vue-echarts';
import { Monitor } from '@element-plus/icons-vue';
import type { DeviceTypeStat, VisitTrendPoint } from '@/api/modules/admin';

use([CanvasRenderer, LineChart, PieChart, GridComponent, TooltipComponent, LegendComponent]);

/**
 * 后台仪表盘图表组件。
 * 负责根据摘要接口返回的数据渲染访问趋势折线图与设备分布饼图。
 */
const props = defineProps<{
  trendPoints: VisitTrendPoint[];
  deviceStats: DeviceTypeStat[];
}>();

/**
 * 判断是否存在可展示的趋势图数据。
 */
const hasTrendData = computed(() => props.trendPoints.some((item) => Number(item.visitCount) > 0));

/**
 * 判断是否存在可展示的设备分布数据。
 */
const hasDeviceData = computed(() => props.deviceStats.some((item) => Number(item.visitCount) > 0));

/**
 * 汇总最近七天总访问量，用于卡片头部展示。
 */
const recentSevenDaysVisits = computed(() =>
  props.trendPoints.reduce((total, item) => total + Number(item.visitCount ?? 0), 0)
);

/**
 * 生成访问趋势折线图配置。
 */
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
    data: props.trendPoints.map((item) => item.label),
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
      data: props.trendPoints.map((item) => Number(item.visitCount ?? 0)),
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

/**
 * 生成设备分布饼图配置。
 */
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
      data: props.deviceStats.map((item, index) => ({
        name: item.label,
        value: Number(item.visitCount ?? 0),
        itemStyle: {
          color: ['#0f172a', '#06b6d4', '#f59e0b', '#8b5cf6', '#94a3b8'][index % 5]
        }
      }))
    }
  ]
}));
</script>
