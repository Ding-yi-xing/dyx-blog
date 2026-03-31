<template>
  <div class="dyx-footprint-map relative h-full w-full overflow-hidden">
    <div
      class="pointer-events-none absolute inset-0 z-0"
      :class="mapBaseUnderlayClass"
    ></div>
    <div
      class="pointer-events-none absolute inset-0 z-[1]"
      :class="mapGlowClass"
    ></div>
    <div
      v-if="showMapBackdrop"
      class="pointer-events-none absolute inset-0 z-[2]"
      :style="getMapContainerStyle()"
    ></div>
    <div
      ref="mapRoot"
      class="dyx-footprint-map--interactive relative z-[3] h-full w-full"
      :style="getMapContainerStyle()"
    ></div>
    <div
      v-if="statusMessage"
      class="absolute inset-0 z-20 flex items-center justify-center px-6 text-center text-sm backdrop-blur-sm"
      :class="overlayTextClass"
    >
      <div
        class="max-w-md rounded-3xl border px-5 py-4"
        :class="overlayPanelClass"
      >
        {{ statusMessage }}
      </div>
    </div>
    <div
      class="pointer-events-none absolute inset-x-0 bottom-0 z-10 h-28"
      :class="mapBottomFadeClass"
    ></div>

    <!-- 地图操作提示 -->
    <div
      v-if="!statusMessage"
      class="pointer-events-none absolute left-1/2 z-20 -translate-x-1/2 opacity-0 transition-opacity duration-1000"
      :class="[
        isMobile ? 'top-28' : 'bottom-12',
        { 'opacity-100': showInteractionTip },
      ]"
    >
      <!-- 移动端提示 -->
      <div
        v-if="isMobile"
        class="flex items-center gap-3 rounded-full border px-4 py-2.5 text-[11px] font-medium backdrop-blur-md sm:gap-4 sm:px-6 sm:text-xs"
        :class="overlayPanelClass"
      >
        <span class="flex items-center gap-1.5">
          <kbd class="rounded border border-current/20 px-1 py-0.5 font-sans"
            >双指</kbd
          >
          <span>缩放</span>
        </span>
        <span class="h-2.5 w-[1px] bg-current/20"></span>
        <span class="flex items-center gap-1.5">
          <kbd class="rounded border border-current/20 px-1 py-0.5 font-sans"
            >单指</kbd
          >
          <span>移动</span>
        </span>
        <span class="h-2.5 w-[1px] bg-current/20"></span>
        <span class="flex items-center gap-1.5">
          <kbd class="rounded border border-current/20 px-1 py-0.5 font-sans"
            >双击</kbd
          >
          <span>放大</span>
        </span>
        <span class="h-2.5 w-[1px] bg-current/20"></span>
        <span class="flex items-center gap-1.5">
          <kbd class="rounded border border-current/20 px-1 py-0.5 font-sans"
            >侧边</kbd
          >
          <span>翻页</span>
        </span>
      </div>

      <!-- 桌面端提示 -->
      <div
        v-else
        class="flex items-center gap-4 rounded-full border px-6 py-2.5 text-xs font-medium backdrop-blur-md"
        :class="overlayPanelClass"
      >
        <span class="flex items-center gap-1.5">
          <kbd class="rounded border border-current/20 px-1.5 py-0.5 font-sans"
            >双击</kbd
          >
          <span>放大</span>
        </span>
        <span class="h-3 w-[1px] bg-current/20"></span>
        <span class="flex items-center gap-1.5">
          <kbd class="rounded border border-current/20 px-1.5 py-0.5 font-sans"
            >右键</kbd
          >
          <span>缩小</span>
        </span>
        <span class="h-3 w-[1px] bg-current/20"></span>
        <span class="flex items-center gap-1.5">
          <kbd class="rounded border border-current/20 px-1.5 py-0.5 font-sans"
            >拖拽</kbd
          >
          <span>移动</span>
        </span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from "vue";
import {
  featureToPolygonPaths,
  getAllProvinceFeatures,
  getCachedMapBoundaryItems,
  getCachedMapLabelItems,
  getCityFeatureByName,
  getWorldCityLabelItems,
  getWorldCityLabelZoomBucket,
  getWorldCountryBoundaryItems,
  getWorldCountryLabelItems,
  type FootprintMapItem,
  type MapBoundaryItem,
  type MapLabelItem,
  type WorldCountryBoundaryItem,
  type WorldCountryLabelItem,
} from "@/utils/footprintGeo";
import { getAmapApiKeyConfigured, loadAmapSdk } from "@/utils/amapLoader";

const props = defineProps<{
  items: FootprintMapItem[];
  visitedProvinceNames: string[];
  theme: "light" | "dark";
  visible?: boolean;
}>();

/**
 * 首页足迹地图组件。
 * 负责初始化高德地图、管理多层覆盖物，并根据主题、缩放级别和足迹数据动态切换显示内容。
 */
const mapRoot = ref<HTMLElement | null>(null);
const statusMessage = ref("");
const showInteractionTip = ref(false);
const isMobile = ref(false);
const showMapBackdrop = ref(true);

/**
 * 检测当前是否处于移动端触控环境，用于切换交互提示文案与展示位置。
 */
const checkIsMobile = (): void => {
  isMobile.value = window.innerWidth < 768 || "ontouchstart" in window;
};

/**
 * 根据当前主题生成遮罩面板样式，用于状态提示与交互提示气泡。
 */
const overlayPanelClass = computed(() =>
  props.theme === "dark"
    ? "border-white/10 bg-slate-950/72 text-slate-200 shadow-[0_18px_56px_rgba(2,6,23,0.42)]"
    : "border-slate-200/80 bg-white/88 text-slate-700 shadow-[0_18px_56px_rgba(148,163,184,0.22)]"
);
const overlayTextClass = computed(() =>
  props.theme === "dark" ? "text-slate-200" : "text-slate-700"
);
const mapBaseUnderlayClass = computed(() =>
  props.theme === "dark"
    ? "bg-[radial-gradient(circle_at_50%_42%,rgba(15,23,42,0.96),rgba(2,6,23,0.98)_52%,rgba(1,4,18,1)_100%)]"
    : "bg-[radial-gradient(circle_at_50%_42%,rgba(255,255,255,0.98),rgba(248,250,252,0.98)_56%,rgba(241,245,249,1)_100%)]"
);
const mapGlowClass = computed(() =>
  props.theme === "dark"
    ? "bg-[radial-gradient(circle_at_50%_42%,rgba(34,211,238,0.24),rgba(15,23,42,0.08)_34%,rgba(2,6,23,0)_66%),radial-gradient(circle_at_50%_88%,rgba(8,145,178,0.26),rgba(2,6,23,0)_52%)]"
    : "bg-[radial-gradient(circle_at_50%_42%,rgba(56,189,248,0.14),rgba(255,255,255,0)_42%),radial-gradient(circle_at_50%_92%,rgba(125,211,252,0.22),rgba(255,255,255,0)_54%)]"
);
const mapBottomFadeClass = computed(() =>
  props.theme === "dark"
    ? "bg-[linear-gradient(180deg,rgba(2,6,23,0),rgba(2,6,23,0.16)_28%,rgba(2,6,23,0.58)_100%)]"
    : "bg-[linear-gradient(180deg,rgba(255,255,255,0),rgba(241,245,249,0.18)_28%,rgba(226,232,240,0.56)_100%)]"
);

let mapInstance: AMapMap | null = null;
let worldLayer: AMapDistrictLayerInstance | null = null;
let provinceOutlineOverlays: AMapPolygon[] = [];
let worldCountryBoundaryOverlays: AMapPolygon[] = [];
let markerOverlays: AMapMarker[] = [];
let markerCache = new Map<string, AMapMarker>();
let visitedCityOverlays: AMapPolygon[] = [];
let cityBoundaryOverlays: AMapPolygon[] = [];
let worldCountryLabelOverlays: AMapText[] = [];
let worldCityLabelOverlays: AMapText[] = [];
let cityNameLabelOverlays: AMapText[] = [];
let hasUserMoved = false;
let latestRenderToken = 0;
let tipTimer: ReturnType<typeof setTimeout> | null = null;
let moveHandler: (() => void) | null = null;
let zoomEndHandler: (() => void) | null = null;
let rootClickHandler: ((event: MouseEvent) => void) | null = null;
let rootContextMenuHandler: ((event: MouseEvent) => void) | null = null;
let worldCountryBoundariesReady = false;
let provinceOutlinesReady = false;
let visitedCityOverlaysKey = "";
let markerRenderKey = "";
let cityBoundariesReady = false;
let worldCountryLabelsReady = false;
let worldCityLabelsReady = false;
let cityNameLabelsReady = false;
let lastWorldCityLabelBucket: number | null = null;
let lastRenderTheme: "light" | "dark" | null = null;
let lastCityBoundaryVisible: boolean | null = null;
let lastCountryLabelVisible: boolean | null = null;
let lastCityLabelVisible: boolean | null = null;
let lastWorldCityVisible: boolean | null = null;
let lastMarkerZoomBucket: number | null = null;
const ALL_PROVINCE_FEATURES = getAllProvinceFeatures();
const ALL_CITY_BOUNDARY_ITEMS = getCachedMapBoundaryItems();
const ALL_CITY_LABEL_ITEMS = getCachedMapLabelItems();
const WORLD_COUNTRY_BOUNDARY_ITEMS = getWorldCountryBoundaryItems();
const WORLD_COUNTRY_LABEL_ITEMS = getWorldCountryLabelItems();
const INITIAL_CENTER: [number, number] = [104.195397, 35.86166];
const INITIAL_ZOOM = 4.2;
const MIN_ZOOM = 4.2;
const MAX_ZOOM = 9;
const COUNTRY_LABEL_MAX_ZOOM = 4.45;
const WORLD_CITY_LABEL_ZOOM_THRESHOLD = 5.2;
const CITY_BOUNDARY_ZOOM_THRESHOLD = 5.2;
const CITY_NAME_ZOOM_THRESHOLD = 5.2;
const WORLD_CITY_DETAIL_ZOOM_THRESHOLD = 6.2;
const WORLD_CITY_IMPORTANT_ZOOM_THRESHOLD = 6.2;
const WORLD_CITY_CAPITAL_ZOOM_THRESHOLD = 5.2;
const CITY_LABEL_IMPORTANT_ZOOM_THRESHOLD = 6.2;
const CITY_LABEL_CAPITAL_ZOOM_THRESHOLD = 5.2;
const WORLD_CITY_LABEL_Z_INDEX = 31;
const CITY_NAME_LABEL_Z_INDEX = 32;
const WORLD_COUNTRY_LABEL_Z_INDEX = 16;
const WORLD_COUNTRY_BOUNDARY_Z_INDEX = 8;
const PROVINCE_OUTLINE_Z_INDEX = 12;
const CITY_BOUNDARY_Z_INDEX = 36;
const VISITED_CITY_Z_INDEX = 34;
const MARKER_LATEST_Z_INDEX = 130;
const MARKER_NORMAL_Z_INDEX = 120;
const EMPTY_OVERLAY_KEY = "";
let hasInitializedMap = false;
let mapInitPromise: Promise<void> | null = null;

function getMarkerZoomBucket(): number {
  return Math.round((mapInstance?.getZoom() ?? INITIAL_ZOOM) * 10);
}

function isThemeChanged(): boolean {
  return lastRenderTheme !== props.theme;
}

function syncRenderTheme(): void {
  lastRenderTheme = props.theme;
}

function shouldRefreshMarkers(): boolean {
  return lastMarkerZoomBucket !== getMarkerZoomBucket();
}

function syncMarkerZoomBucket(): void {
  lastMarkerZoomBucket = getMarkerZoomBucket();
}

function resetRenderSnapshots(): void {
  lastWorldCityLabelBucket = null;
  lastRenderTheme = null;
  lastCityBoundaryVisible = null;
  lastCountryLabelVisible = null;
  lastCityLabelVisible = null;
  lastWorldCityVisible = null;
  lastMarkerZoomBucket = null;
  markerRenderKey = "";
}

/**
 * 生成当前地图点位集合的渲染快照键。
 * 用于判断点位覆盖物是否需要整体重建。
 */
function buildMarkerRenderKey(items: FootprintMapItem[]): string {
  return `${props.theme}::${getMarkerZoomBucket()}::${items
    .map((item) => `${item.id || item.name}::${item.position[0]}::${item.position[1]}::${Number(item.isLatest)}::${item.importance}`)
    .join("||")}`;
}

function shouldRefreshWorldCityLabels(): boolean {
  if (!shouldShowWorldCityLabels()) {
    return false;
  }
  const nextBucket = getWorldCityLabelZoomBucket(getWorldCityLabelZoomLevel());
  return isThemeChanged() || lastWorldCityLabelBucket !== nextBucket;
}

function syncWorldCityLabelBucket(): void {
  lastWorldCityLabelBucket = getWorldCityLabelZoomBucket(getWorldCityLabelZoomLevel());
}

function shouldUpdateVisibility(nextValue: boolean, previousValue: boolean | null): boolean {
  return previousValue === null || previousValue !== nextValue || isThemeChanged();
}

function buildVisitedCityOverlayKey(items: FootprintMapItem[]): string {
  return items
    .map((item) => `${item.provinceName || ""}::${item.name || item.cityName}`)
    .sort()
    .join("||");
}

function shouldRefreshVisitedCityOverlays(nextKey: string): boolean {
  return visitedCityOverlaysKey !== nextKey || isThemeChanged();
}

function shouldRefreshMarkerSet(nextKey: string): boolean {
  return markerRenderKey !== nextKey || shouldRefreshMarkers() || isThemeChanged();
}

function syncMarkerRenderState(nextKey: string): void {
  markerRenderKey = nextKey;
  syncMarkerZoomBucket();
}

resetRenderSnapshots();

/**
 * 统一切换文本和多边形覆盖物的显隐状态。
 */
function applyOverlayVisibility(overlays: Array<AMapText | AMapPolygon>, visible: boolean): void {
  overlays.forEach((overlay) => {
    if (visible) {
      overlay.show();
      return;
    }
    overlay.hide();
  });
}

/**
 * 批量创建文本覆盖物。
 * 用于世界国家、世界城市和国内城市名称等标签层。
 */
function createTextOverlays<T extends { name: string; position: [number, number] }>(
  AMap: AMapNamespace,
  items: T[],
  style: AMapTextStyle,
  zIndex: number,
  visible = true
): AMapText[] {
  return items.map(
    (item) =>
      new AMap.Text({
        text: item.name,
        position: item.position,
        anchor: "center",
        offset: new AMap.Pixel(0, 0),
        style,
        zIndex,
        visible,
      })
  );
}

function rebuildTextOverlays<T extends { name: string; position: [number, number] }>(
  AMap: AMapNamespace,
  overlays: AMapText[],
  items: T[],
  style: AMapTextStyle,
  zIndex: number,
  visible = true
): AMapText[] {
  if (mapInstance && overlays.length) {
    mapInstance.remove(overlays);
  }
  overlays.forEach((overlay) => overlay.setMap(null));
  const nextOverlays = createTextOverlays(AMap, items, style, zIndex, visible);
  if (mapInstance && nextOverlays.length) {
    mapInstance.add(nextOverlays);
  }
  return nextOverlays;
}

function updateTextOverlayStyle(overlays: AMapText[], style: AMapTextStyle): void {
  overlays.forEach((overlay) => {
    overlay.setOptions({ style });
  });
}

function getVisitedCityItemsKey(items: FootprintMapItem[]): string {
  return buildVisitedCityOverlayKey(items);
}

function getWorldCityItemsForCurrentZoom(): ReturnType<typeof getWorldCityLabelItems> {
  return getWorldCityLabelItems(getWorldCityLabelZoomLevel());
}

function shouldRefreshWorldCityOverlaySet(): boolean {
  if (!shouldShowWorldCityLabels()) {
    return false;
  }
  const nextBucket = getWorldCityLabelZoomBucket(getWorldCityLabelZoomLevel());
  return lastWorldCityLabelBucket !== nextBucket;
}

function syncVisibilitySnapshots(): void {
  lastCityBoundaryVisible = shouldShowCityBoundaries();
  lastCountryLabelVisible = shouldShowCountryLabels();
  lastCityLabelVisible = shouldShowCityNameLabels();
  lastWorldCityVisible = shouldShowWorldCityLabels();
}

function setVisitedCityOverlaysKey(nextKey: string): void {
  visitedCityOverlaysKey = nextKey;
}

function setWorldCityLabelBucket(): void {
  syncWorldCityLabelBucket();
}

function setThemeSnapshot(): void {
  syncRenderTheme();
}

function setMarkerSnapshot(nextKey: string): void {
  syncMarkerRenderState(nextKey);
}

function shouldRefreshCountryLabels(): boolean {
  return !worldCountryLabelsReady || isThemeChanged();
}

function shouldRefreshCityLabels(): boolean {
  return !cityNameLabelsReady || isThemeChanged();
}

function shouldRefreshCityBoundaries(): boolean {
  return !cityBoundariesReady || isThemeChanged();
}

function shouldRefreshVisitedFill(nextKey: string): boolean {
  return !visitedCityOverlays.length || shouldRefreshVisitedCityOverlays(nextKey);
}

function shouldRefreshMarkerLayer(nextKey: string): boolean {
  return !markerOverlays.length || shouldRefreshMarkerSet(nextKey);
}

function getMarkerZIndex(item: FootprintMapItem): number {
  return item.isLatest ? MARKER_LATEST_Z_INDEX : MARKER_NORMAL_Z_INDEX;
}

function getCityBoundaryItems(): MapBoundaryItem[] {
  return ALL_CITY_BOUNDARY_ITEMS;
}

function getCityLabelItems(): MapLabelItem[] {
  return ALL_CITY_LABEL_ITEMS;
}

function getCountryBoundaryItems(): WorldCountryBoundaryItem[] {
  return WORLD_COUNTRY_BOUNDARY_ITEMS;
}

function getCountryLabelItems(): WorldCountryLabelItem[] {
  return WORLD_COUNTRY_LABEL_ITEMS;
}

function createVisitedCityOverlays(AMap: AMapNamespace): AMapPolygon[] {
  const overlayMap = new Map<string, AMapPolygon[]>();
  props.items.forEach((item) => {
    const cityName = item.name || item.cityName;
    const provinceName = item.provinceName ?? undefined;
    const key = `${provinceName || ""}::${cityName}`;
    if (overlayMap.has(key)) {
      return;
    }
    const feature = getCityFeatureByName(cityName, provinceName);
    const paths = featureToPolygonPaths(feature);
    if (!paths.length) {
      return;
    }
    overlayMap.set(
      key,
      paths.map(
        (path) =>
          new AMap.Polygon({
            path,
            ...getVisitedCityPolygonStyle(),
            bubble: false,
            zIndex: VISITED_CITY_Z_INDEX,
          })
      )
    );
  });
  return Array.from(overlayMap.values()).flat();
}

function createMarkerKey(item: FootprintMapItem): string {
  return `${item.id || item.name}::${item.position[0]}::${item.position[1]}::${props.theme}`;
}

function shouldUpdateMarkerContent(): boolean {
  return shouldShowLabels() || shouldShowVisitedCityLabels() || isThemeChanged() || shouldRefreshMarkers();
}

function escapeHtml(value: string): string {
  return value
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}

function shouldShowVisitedCityLabels(): boolean {
  return true;
}

function getVisitedCityLabelStyle(isLatest: boolean): string {
  return isLatest
    ? "margin-top:8px;white-space:nowrap;font-size:12px;line-height:1;color:rgba(255,255,255,1);text-shadow:0 2px 12px rgba(0,0,0,0.56);letter-spacing:0.02em;font-weight:600;"
    : "margin-top:8px;white-space:nowrap;font-size:12px;line-height:1;color:rgba(255,255,255,0.98);text-shadow:0 2px 12px rgba(0,0,0,0.56);letter-spacing:0.02em;";
}

function buildMarkerContent(item: FootprintMapItem): string {
  const dotSize = item.isLatest ? 10 : 7;
  const labelText = escapeHtml(item.cityName || item.name);
  const labelDisplay = shouldShowVisitedCityLabels() ? "block" : "none";

  if (props.theme === "dark") {
    return `
      <div style="position:relative;display:flex;flex-direction:column;align-items:center;pointer-events:none;">
        <span style="display:block;width:${dotSize}px;height:${dotSize}px;border-radius:9999px;background:#ffffff;border:1.5px solid rgba(34,211,238,0.92);box-sizing:border-box;"></span>
        <span style="display:${labelDisplay};${getVisitedCityLabelStyle(
      item.isLatest
    )}">${labelText}</span>
      </div>
    `;
  }

  return `
    <div style="position:relative;display:flex;flex-direction:column;align-items:center;pointer-events:none;">
      <span style="display:block;width:${dotSize}px;height:${dotSize}px;border-radius:9999px;background:#2563eb;border:1.5px solid rgba(255,255,255,0.96);box-sizing:border-box;"></span>
      <span style="display:${labelDisplay};${getVisitedCityLabelStyle(
    item.isLatest
  )}">${labelText}</span>
    </div>
  `;
}

function shouldShowCityLabelItem(priority: "capital" | "important" | "normal"): boolean {
  const zoom = getCurrentZoom();
  if (priority === "capital") {
    return zoom >= CITY_LABEL_CAPITAL_ZOOM_THRESHOLD;
  }
  if (priority === "important") {
    return zoom >= CITY_LABEL_IMPORTANT_ZOOM_THRESHOLD;
  }
  return false;
}

function shouldShowWorldCityLabelItem(priority: "capital" | "important" | "normal"): boolean {
  const zoom = getCurrentZoom();
  if (priority === "capital") {
    return zoom >= WORLD_CITY_CAPITAL_ZOOM_THRESHOLD;
  }
  if (priority === "important") {
    return zoom >= WORLD_CITY_IMPORTANT_ZOOM_THRESHOLD;
  }
  return false;
}

function filterVisibleWorldCityLabelItems(items: ReturnType<typeof getWorldCityLabelItems>) {
  return items.filter((item) => shouldShowWorldCityLabelItem(item.priority));
}

function filterVisibleCityLabelItems(items: ReturnType<typeof getCityLabelItems>) {
  const visitedCityNames = new Set(
    props.items.map((item) => `${item.provinceName || ""}::${item.cityName || item.name}`)
  );
  return items.filter((item) => {
    if (!shouldShowCityLabelItem(item.priority)) {
      return false;
    }
    return !visitedCityNames.has(`${item.provinceName || ""}::${item.name}`);
  });
}

function getLabelTextStyle(priority: "capital" | "important" | "normal" = "important"): AMapTextStyle {
  if (props.theme === "dark") {
    return {
      "font-size": priority === "capital" ? "12px" : "11px",
      "font-weight": priority === "capital" ? "600" : "500",
      color: priority === "capital" ? "rgba(226,232,240,0.9)" : "rgba(148,163,184,0.86)",
      "text-shadow": "0 2px 10px rgba(2,6,23,0.9)",
      "background-color": "transparent",
      border: "none",
    };
  }
  return {
    "font-size": priority === "capital" ? "12px" : "11px",
    "font-weight": priority === "capital" ? "600" : "500",
    color: priority === "capital" ? "#1e293b" : "#475569",
    "background-color": "transparent",
    border: "none",
  };
}

function getMapStyle(): string {
  return props.theme === "dark" ? "amap://styles/dark" : "amap://styles/light";
}

function getMapFeatures(): string[] {
  return ["bg"];
}

function getMapContainerStyle() {
  return props.theme === "dark"
    ? { backgroundColor: "#0a0a0f" }
    : { backgroundColor: "#f8fafc" };
}

function getWorldLayerStyles(): AMapDistrictLayerStyleOptions {
  return props.theme === "dark"
    ? {
        "stroke-width": 0,
        "stroke-color": "rgba(0,0,0,0)",
        "nation-stroke": "rgba(0,0,0,0)",
        "coastline-stroke": "rgba(0,0,0,0)",
        "province-stroke": "rgba(0,0,0,0)",
        "city-stroke": "rgba(0,0,0,0)",
        "county-stroke": "rgba(0,0,0,0)",
        fill: "rgba(15, 23, 42, 0.95)",
      }
    : {
        "stroke-width": 0,
        "stroke-color": "rgba(0,0,0,0)",
        "nation-stroke": "rgba(0,0,0,0)",
        "coastline-stroke": "rgba(0,0,0,0)",
        "province-stroke": "rgba(0,0,0,0)",
        fill: "rgba(241, 245, 249, 0.98)",
      };
}

function getProvinceOutlineStyle() {
  return props.theme === "dark"
    ? {
        strokeColor: "rgba(71, 85, 105, 0.15)",
        strokeOpacity: 0.15,
        strokeWeight: 0.8,
        fillOpacity: 0,
        fillColor: "rgba(0,0,0,0)",
      }
    : {
        strokeColor: "rgba(148, 163, 184, 0.12)",
        strokeOpacity: 0.12,
        strokeWeight: 0.7,
        fillOpacity: 0,
        fillColor: "rgba(0,0,0,0)",
      };
}

function ensureWorldLayer(AMap: AMapNamespace): void {
  if (!mapInstance || !AMap.DistrictLayer?.World) {
    return;
  }
  if (!worldLayer) {
    worldLayer = new AMap.DistrictLayer.World({
      zIndex: 1,
      zooms: [MIN_ZOOM, MAX_ZOOM],
    });
    worldLayer.setMap(mapInstance);
  }
  worldLayer.setStyles(getWorldLayerStyles());
}

function disableBaseMapNoise(): void {
  if (!mapRoot.value) {
    return;
  }
  mapRoot.value.style.filter =
    props.theme === "dark"
      ? "contrast(1.02) brightness(0.92) saturate(0.88)"
      : "";
}

function shouldShowCityBoundaries(): boolean {
  return (
    (mapInstance?.getZoom() ?? INITIAL_ZOOM) >= CITY_BOUNDARY_ZOOM_THRESHOLD
  );
}

function shouldShowCountryLabels(): boolean {
  return (mapInstance?.getZoom() ?? INITIAL_ZOOM) <= COUNTRY_LABEL_MAX_ZOOM;
}

function getCurrentZoom(): number {
  return mapInstance?.getZoom() ?? INITIAL_ZOOM;
}

function shouldShowWorldCityLabels(): boolean {
  return getCurrentZoom() >= WORLD_CITY_LABEL_ZOOM_THRESHOLD;
}

function getWorldCityLabelZoomLevel(): number {
  const zoom = getCurrentZoom();
  if (zoom >= WORLD_CITY_DETAIL_ZOOM_THRESHOLD) {
    return zoom;
  }
  return WORLD_CITY_LABEL_ZOOM_THRESHOLD;
}

function getCountryLabelTextStyle(): AMapTextStyle {
  return props.theme === "dark"
    ? {
        "font-size": "11px",
        "font-weight": "500",
        color: "rgba(226,232,240,0.96)",
        "text-shadow": "0 2px 10px rgba(2,6,23,0.96)",
        "background-color": "transparent",
        border: "none",
      }
    : {
        "font-size": "11px",
        "font-weight": "500",
        color: "rgba(71,85,105,0.86)",
        "background-color": "transparent",
        border: "none",
      };
}

function shouldShowCityNameLabels(): boolean {
  return (mapInstance?.getZoom() ?? INITIAL_ZOOM) >= CITY_NAME_ZOOM_THRESHOLD;
}

function getCityBoundaryStyle(item?: MapBoundaryItem) {
  const isOverseas = !!item?.provinceName && !item.provinceName.endsWith("省") && !item.provinceName.endsWith("市") && !item.provinceName.endsWith("自治区") && !item.provinceName.endsWith("特别行政区");
  if (props.theme === "dark") {
    return isOverseas
      ? {
          strokeColor: "rgba(186, 230, 253, 1)",
          strokeOpacity: 1,
          strokeWeight: 2.8,
          fillOpacity: 0,
          fillColor: "rgba(0,0,0,0)",
        }
      : {
          strokeColor: "rgba(100, 116, 139, 0.22)",
          strokeOpacity: 1,
          strokeWeight: 0.8,
          fillOpacity: 0,
          fillColor: "rgba(0,0,0,0)",
        };
  }
  return isOverseas
    ? {
        strokeColor: "rgba(37, 99, 235, 0.82)",
        strokeOpacity: 1,
        strokeWeight: 2.2,
        fillOpacity: 0,
        fillColor: "rgba(0,0,0,0)",
      }
    : {
        strokeColor: "rgba(148, 163, 184, 0.18)",
        strokeOpacity: 1,
        strokeWeight: 0.8,
        fillOpacity: 0,
        fillColor: "rgba(0,0,0,0)",
      };
}

function getVisitedCityPolygonStyle() {
  return props.theme === "dark"
    ? {
        strokeColor: "rgba(34,211,238,0.88)",
        strokeOpacity: 1,
        strokeWeight: 1,
        fillColor: "rgba(34,211,238,0.3)",
        fillOpacity: 0.3,
      }
    : {
        strokeColor: "rgba(14,165,233,0.72)",
        strokeOpacity: 1,
        strokeWeight: 1,
        fillColor: "rgba(56,189,248,0.24)",
        fillOpacity: 0.24,
      };
}

function getWorldCountryBoundaryStyle() {
  return props.theme === "dark"
    ? {
        strokeColor: "rgba(100, 116, 139, 0.3)",
        strokeOpacity: 0.32,
        strokeWeight: 1.3,
      }
    : {
        strokeColor: "rgba(148, 163, 184, 0.26)",
        strokeOpacity: 0.3,
        strokeWeight: 1.15,
      };
}

function clearWorldCountryBoundaryOverlays(): void {
  if (!worldCountryBoundaryOverlays.length || !mapInstance) {
    worldCountryBoundaryOverlays = [];
    worldCountryBoundariesReady = false;
    return;
  }
  mapInstance.remove(worldCountryBoundaryOverlays);
  worldCountryBoundaryOverlays.forEach((overlay) => overlay.setMap(null));
  worldCountryBoundaryOverlays = [];
  worldCountryBoundariesReady = false;
}

function renderWorldCountryBoundaryOverlays(AMap: AMapNamespace): void {
  if (!mapInstance) {
    return;
  }
  const boundaryStyle = getWorldCountryBoundaryStyle();
  if (!worldCountryBoundariesReady) {
    worldCountryBoundaryOverlays = getCountryBoundaryItems().flatMap((item) =>
      item.paths.map(
        (path) =>
          new AMap.Polygon({
            path,
            ...boundaryStyle,
            fillColor: "rgba(0,0,0,0)",
            fillOpacity: 0,
            bubble: false,
            zIndex: WORLD_COUNTRY_BOUNDARY_Z_INDEX,
          })
      )
    );
    if (worldCountryBoundaryOverlays.length) {
      mapInstance.add(worldCountryBoundaryOverlays);
    }
    worldCountryBoundariesReady = true;
    return;
  }
  worldCountryBoundaryOverlays.forEach((overlay) => {
    overlay.setOptions(boundaryStyle);
    overlay.show();
  });
}

function clearProvinceOutlineOverlays(): void {
  if (!provinceOutlineOverlays.length || !mapInstance) {
    provinceOutlineOverlays = [];
    provinceOutlinesReady = false;
    return;
  }
  mapInstance.remove(provinceOutlineOverlays);
  provinceOutlineOverlays.forEach((overlay) => overlay.setMap(null));
  provinceOutlineOverlays = [];
  provinceOutlinesReady = false;
}

function renderProvinceOutlineOverlays(AMap: AMapNamespace): void {
  if (!mapInstance) {
    return;
  }
  if (!provinceOutlinesReady) {
    provinceOutlineOverlays = ALL_PROVINCE_FEATURES.flatMap((feature) =>
      featureToPolygonPaths(feature).map(
        (path) =>
          new AMap.Polygon({
            path,
            ...getProvinceOutlineStyle(),
            bubble: false,
            zIndex: PROVINCE_OUTLINE_Z_INDEX,
          })
      )
    );
    if (provinceOutlineOverlays.length) {
      mapInstance.add(provinceOutlineOverlays);
    }
    provinceOutlinesReady = true;
    return;
  }
  const nextStyle = getProvinceOutlineStyle();
  provinceOutlineOverlays.forEach((overlay) => {
    overlay.setOptions(nextStyle);
    overlay.show();
  });
}

function clearVisitedCityOverlays(): void {
  if (!visitedCityOverlays.length || !mapInstance) {
    visitedCityOverlays = [];
    visitedCityOverlaysKey = "";
    return;
  }
  mapInstance.remove(visitedCityOverlays);
  visitedCityOverlays.forEach((overlay) => overlay.setMap(null));
  visitedCityOverlays = [];
  visitedCityOverlaysKey = "";
}

function renderVisitedCityOverlays(AMap: AMapNamespace): void {
  if (!mapInstance) {
    return;
  }
  const nextKey = getVisitedCityItemsKey(props.items);
  if (shouldRefreshVisitedFill(nextKey)) {
    clearVisitedCityOverlays();
    visitedCityOverlays = createVisitedCityOverlays(AMap);
    if (visitedCityOverlays.length) {
      mapInstance.add(visitedCityOverlays);
    }
    setVisitedCityOverlaysKey(nextKey);
    return;
  }
  const nextStyle = getVisitedCityPolygonStyle();
  visitedCityOverlays.forEach((overlay) => {
    overlay.setOptions(nextStyle);
    overlay.show();
  });
}


function clearCityBoundaryOverlays(): void {
  if (!cityBoundaryOverlays.length || !mapInstance) {
    cityBoundaryOverlays = [];
    cityBoundariesReady = false;
    return;
  }
  mapInstance.remove(cityBoundaryOverlays);
  cityBoundaryOverlays.forEach((overlay) => overlay.setMap(null));
  cityBoundaryOverlays = [];
  cityBoundariesReady = false;
}

function clearLabelOverlays(): void {
  if (mapInstance) {
    if (worldCountryLabelOverlays.length) {
      mapInstance.remove(worldCountryLabelOverlays);
    }
    if (worldCityLabelOverlays.length) {
      mapInstance.remove(worldCityLabelOverlays);
    }
    if (cityNameLabelOverlays.length) {
      mapInstance.remove(cityNameLabelOverlays);
    }
  }
  worldCountryLabelOverlays.forEach((overlay) => overlay.setMap(null));
  worldCityLabelOverlays.forEach((overlay) => overlay.setMap(null));
  cityNameLabelOverlays.forEach((overlay) => overlay.setMap(null));
  worldCountryLabelOverlays = [];
  worldCityLabelOverlays = [];
  cityNameLabelOverlays = [];
  worldCountryLabelsReady = false;
  worldCityLabelsReady = false;
  cityNameLabelsReady = false;
}

function renderCityBoundaryOverlays(AMap: AMapNamespace): void {
  if (!mapInstance) {
    return;
  }
  const visible = shouldShowCityBoundaries();
  if (!cityBoundariesReady) {
    cityBoundaryOverlays = getCityBoundaryItems().flatMap((item) =>
      item.paths.map(
        (path) =>
          new AMap.Polygon({
            path,
            ...getCityBoundaryStyle(item),
            bubble: false,
            zIndex: CITY_BOUNDARY_Z_INDEX,
            visible,
          })
      )
    );
    if (cityBoundaryOverlays.length) {
      mapInstance.add(cityBoundaryOverlays);
    }
    cityBoundariesReady = true;
    lastCityBoundaryVisible = visible;
    return;
  }
  if (!shouldRefreshCityBoundaries() && !shouldUpdateVisibility(visible, lastCityBoundaryVisible)) {
    return;
  }
  const nextItems = getCityBoundaryItems();
  let overlayIndex = 0;
  nextItems.forEach((item) => {
    const nextStyle = getCityBoundaryStyle(item);
    item.paths.forEach(() => {
      cityBoundaryOverlays[overlayIndex]?.setOptions(nextStyle);
      overlayIndex += 1;
    });
  });
  applyOverlayVisibility(cityBoundaryOverlays, visible);
  lastCityBoundaryVisible = visible;
}

function renderCityNameLabels(AMap: AMapNamespace): void {
  if (!mapInstance) {
    return;
  }

  const showCountry = shouldShowCountryLabels();
  const countryStyle = getCountryLabelTextStyle();
  if (!worldCountryLabelsReady) {
    worldCountryLabelOverlays = createTextOverlays(
      AMap,
      getCountryLabelItems(),
      countryStyle,
      WORLD_COUNTRY_LABEL_Z_INDEX,
      showCountry
    );
    if (worldCountryLabelOverlays.length) {
      mapInstance.add(worldCountryLabelOverlays);
    }
    worldCountryLabelsReady = true;
  } else if (shouldRefreshCountryLabels() || shouldUpdateVisibility(showCountry, lastCountryLabelVisible)) {
    updateTextOverlayStyle(worldCountryLabelOverlays, countryStyle);
    applyOverlayVisibility(worldCountryLabelOverlays, showCountry);
  }
  lastCountryLabelVisible = showCountry;

  const showWorldCity = shouldShowWorldCityLabels();
  const nextWorldCityItems = filterVisibleWorldCityLabelItems(getWorldCityItemsForCurrentZoom());
  const worldCityStyle = getLabelTextStyle("important");
  if (showWorldCity && nextWorldCityItems.length) {
    if (!worldCityLabelsReady) {
      worldCityLabelOverlays = createTextOverlays(
        AMap,
        nextWorldCityItems,
        worldCityStyle,
        WORLD_CITY_LABEL_Z_INDEX,
        true
      );
      if (worldCityLabelOverlays.length) {
        mapInstance.add(worldCityLabelOverlays);
      }
      worldCityLabelsReady = true;
      setWorldCityLabelBucket();
    } else if (shouldRefreshWorldCityLabels() || shouldRefreshWorldCityOverlaySet()) {
      worldCityLabelOverlays = rebuildTextOverlays(
        AMap,
        worldCityLabelOverlays,
        nextWorldCityItems,
        worldCityStyle,
        WORLD_CITY_LABEL_Z_INDEX,
        true
      );
      setWorldCityLabelBucket();
    } else if (isThemeChanged() || shouldUpdateVisibility(true, lastWorldCityVisible)) {
      updateTextOverlayStyle(worldCityLabelOverlays, worldCityStyle);
      applyOverlayVisibility(worldCityLabelOverlays, true);
    }
  } else if (worldCityLabelsReady && shouldUpdateVisibility(false, lastWorldCityVisible)) {
    applyOverlayVisibility(worldCityLabelOverlays, false);
  }
  lastWorldCityVisible = showWorldCity && nextWorldCityItems.length > 0;

  const showCity = shouldShowCityNameLabels();
  const nextCityLabelItems = filterVisibleCityLabelItems(getCityLabelItems());
  const cityLabelStyle = getLabelTextStyle("important");
  if (!cityNameLabelsReady) {
    cityNameLabelOverlays = createTextOverlays(
      AMap,
      nextCityLabelItems,
      cityLabelStyle,
      CITY_NAME_LABEL_Z_INDEX,
      showCity && nextCityLabelItems.length > 0
    );
    if (cityNameLabelOverlays.length) {
      mapInstance.add(cityNameLabelOverlays);
    }
    cityNameLabelsReady = true;
  } else if (shouldRefreshCityLabels() || shouldUpdateVisibility(showCity && nextCityLabelItems.length > 0, lastCityLabelVisible)) {
    cityNameLabelOverlays = rebuildTextOverlays(
      AMap,
      cityNameLabelOverlays,
      nextCityLabelItems,
      cityLabelStyle,
      CITY_NAME_LABEL_Z_INDEX,
      showCity && nextCityLabelItems.length > 0
    );
  }
  lastCityLabelVisible = showCity && nextCityLabelItems.length > 0;
}

function shouldShowLabels(): boolean {
  return false;
}

/**
 * 根据当前缩放级别筛选可见足迹点，避免标签与点位过度重叠。
 */
function getVisibleItems(items: FootprintMapItem[]): FootprintMapItem[] {
  const zoom = mapInstance?.getZoom() ?? INITIAL_ZOOM;
  const collisionDistance = getCollisionDistance(zoom);
  const sorted = [...items].sort((left, right) => {
    const latestDiff = Number(right.isLatest) - Number(left.isLatest);
    if (latestDiff !== 0) {
      return latestDiff;
    }
    return right.importance - left.importance;
  });
  const visible: FootprintMapItem[] = [];
  sorted.forEach((item) => {
    if (collisionDistance <= 0) {
      visible.push(item);
      return;
    }
    const [longitude, latitude] = item.position;
    const isTooClose = visible.some((current) => {
      const [currentLongitude, currentLatitude] = current.position;
      return (
        Math.abs(currentLongitude - longitude) < collisionDistance &&
        Math.abs(currentLatitude - latitude) < collisionDistance
      );
    });
    if (!isTooClose) {
      visible.push(item);
    }
  });
  return visible;
}

/**
 * 根据缩放级别返回点位碰撞距离。
 * 缩放越小，距离越大，以降低地图缩小时的标记遮挡。
 */
function getCollisionDistance(zoom: number): number {
  if (zoom >= 6.2) return 0;
  if (zoom >= 5.4) return 0.45;
  if (zoom >= 4.8) return 0.75;
  if (zoom >= 4.2) return 1.15;
  return 1.75;
}

function clearMarkerOverlays(): void {
  if (mapInstance && markerOverlays.length) {
    mapInstance.remove(markerOverlays);
  }
  markerOverlays.forEach((overlay) => overlay.setMap(null));
  markerOverlays = [];
  markerCache.clear();
  markerRenderKey = EMPTY_OVERLAY_KEY;
}

/**
 * 渲染足迹点标记层，并复用未失效的已有 Marker 实例。
 */
function renderMarkers(AMap: AMapNamespace): void {
  if (!mapInstance) {
    return;
  }

  const visibleItems = getVisibleItems(props.items);
  const nextRenderKey = buildMarkerRenderKey(visibleItems);
  if (!shouldRefreshMarkerLayer(nextRenderKey)) {
    showMapBackdrop.value = false;
    return;
  }

  const nextOverlays: AMapMarker[] = [];
  const nextCache = new Map<string, AMapMarker>();
  const updateContent = shouldUpdateMarkerContent();

  visibleItems.forEach((item) => {
    const key = createMarkerKey(item);
    let marker = markerCache.get(key);

    if (marker) {
      if (updateContent) {
        marker.setContent(buildMarkerContent(item));
      }
      marker.setzIndex(getMarkerZIndex(item));
      nextCache.set(key, marker);
      nextOverlays.push(marker);
      markerCache.delete(key);
      return;
    }

    marker = new AMap.Marker({
      position: item.position,
      anchor: "center",
      offset: new AMap.Pixel(0, 0),
      content: buildMarkerContent(item),
      zIndex: getMarkerZIndex(item),
      extData: item,
    });
    nextCache.set(key, marker);
    nextOverlays.push(marker);
  });

  if (markerCache.size > 0) {
    const staleMarkers = Array.from(markerCache.values());
    mapInstance.remove(staleMarkers);
    staleMarkers.forEach((marker) => marker.setMap(null));
  }

  const markersToAdd = nextOverlays.filter((marker) => !marker.getMap());
  if (markersToAdd.length) {
    mapInstance.add(markersToAdd);
  }

  markerOverlays = nextOverlays;
  markerCache = nextCache;
  setMarkerSnapshot(nextRenderKey);
  showMapBackdrop.value = false;
}

function finalizeRenderSnapshots(): void {
  syncVisibilitySnapshots();
  setThemeSnapshot();
}

/**
 * 将覆盖物渲染调度到微任务队列中，合并同一轮状态变化产生的重复刷新。
 */
function scheduleRenderMapOverlays(): void {
  const renderToken = ++latestRenderToken;
  queueMicrotask(() => {
    if (renderToken !== latestRenderToken) {
      return;
    }
    renderMapOverlays();
  });
}

/**
 * 在足迹数据变化后重新渲染覆盖物，并在用户尚未主动拖动前更新默认视角。
 */
function handleDataChange(): void {
  scheduleRenderMapOverlays();
  fitInitialView();
}

/**
 * 在主题切换后同步更新底图样式与覆盖物外观。
 */
function handleThemeChange(): void {
  updateMapTheme();
}

/**
 * 处理地图缩放或移动结束事件，并触发覆盖物刷新。
 */
function handleZoomOrMoveEnd(): void {
  scheduleRenderMapOverlays();
}

/**
 * 标记用户已经主动操作过地图，后续数据刷新时不再强制回到默认视角。
 */
function handleMapInteractionStart(): void {
  hasUserMoved = true;
}

/**
 * 根据组件显隐状态控制交互提示的展示时机。
 */
function handleVisibleChange(newVal?: boolean): void {
  if (newVal) {
    showInteractionTipOnce();
  }
}

/**
 * 释放地图实例、覆盖物、事件句柄和缓存快照。
 * 供组件卸载和地图重建前复用，避免内存泄漏或旧状态残留。
 */
function cleanupMapResources(): void {
  clearWorldCountryBoundaryOverlays();
  clearProvinceOutlineOverlays();
  clearVisitedCityOverlays();
  clearCityBoundaryOverlays();
  clearLabelOverlays();
  clearMarkerOverlays();
  worldLayer?.setMap(null);
  worldLayer = null;
  mapInstance?.destroy();
  mapInstance = null;
  hasUserMoved = false;
  resetRenderSnapshots();
}

/**
 * 创建高德地图实例，并写入当前组件约束下的基础交互配置。
 */
function initializeMapInstance(AMap: AMapNamespace): void {
  if (!mapRoot.value || mapInstance) {
    return;
  }
  mapInstance = new AMap.Map(mapRoot.value, {
    viewMode: "2D",
    zoom: INITIAL_ZOOM,
    center: INITIAL_CENTER,
    zooms: [MIN_ZOOM, MAX_ZOOM],
    showLabel: false,
    showOversea: true,
    language: "zh_cn",
    resizeEnable: true,
    dragEnable: true,
    zoomEnable: true,
    doubleClickZoom: true,
    jogEnable: false,
    keyboardEnable: false,
    scrollEnable: true,
    scrollWheel: false,
    features: getMapFeatures(),
    mapStyle: getMapStyle(),
  });
}

/**
 * 在地图实例初始化完成后绑定交互、应用样式并首次渲染覆盖物。
 */
function prepareMapAfterInit(): void {
  updateMapInteractionHandlers();
  disableBaseMapNoise();
  statusMessage.value = "";
  showInteractionTipOnce();
  renderMapOverlays();
  fitInitialView();
}

/**
 * 销毁交互提示定时器，避免组件卸载后仍保留异步回调。
 */
function destroyTimers(): void {
  if (tipTimer) {
    clearTimeout(tipTimer);
    tipTimer = null;
  }
}

/**
 * 初始化视口相关状态，并监听窗口尺寸变化以切换移动端判断。
 */
function initializeViewportState(): void {
  checkIsMobile();
  window.addEventListener("resize", checkIsMobile);
}

function teardownViewportState(): void {
  window.removeEventListener("resize", checkIsMobile);
}

function clearWorldCityLabelState(): void {
  lastWorldCityLabelBucket = null;
}

function syncThemeBeforeRender(): void {
  if (isThemeChanged()) {
    clearWorldCityLabelState();
  }
}

function renderAllOverlayLayers(AMap: AMapNamespace): void {
  renderWorldCountryBoundaryOverlays(AMap);
  renderProvinceOutlineOverlays(AMap);
  renderCityBoundaryOverlays(AMap);
  renderVisitedCityOverlays(AMap);
  renderCityNameLabels(AMap);
  renderMarkers(AMap);
}

function getMapApi(): AMapNamespace | null {
  return window.AMap ?? null;
}

function beginRenderCycle(): AMapNamespace | null {
  const AMap = getMapApi();
  if (!mapInstance || !AMap) {
    return null;
  }
  syncThemeBeforeRender();
  return AMap;
}

function finishRenderCycle(): void {
  finalizeRenderSnapshots();
}

function refreshMapVisualTheme(): void {
  if (!mapInstance) {
    return;
  }
  showMapBackdrop.value = true;
  mapInstance.setMapStyle(getMapStyle());
  worldLayer?.setStyles(getWorldLayerStyles());
  disableBaseMapNoise();
  scheduleRenderMapOverlays();
}

/**
 * 异步初始化地图 SDK 和地图实例。
 * 在缺少高德 Key 或脚本加载失败时，会通过状态文案向页面反馈原因。
 */
async function initMap(): Promise<void> {
  if (!mapRoot.value || hasInitializedMap) {
    return;
  }
  if (!getAmapApiKeyConfigured()) {
    statusMessage.value = "未配置 VITE_AMAP_API_KEY，足迹地图暂时无法加载。";
    return;
  }
  statusMessage.value = "地图加载中…";
  try {
    const AMap = await loadAmapSdk();
    if (!mapRoot.value || hasInitializedMap) {
      return;
    }
    initializeMapInstance(AMap);
    prepareMapAfterInit();
    hasInitializedMap = true;
  } catch (error) {
    statusMessage.value =
      error instanceof Error ? error.message : "高德地图加载失败。";
  }
}

async function ensureMapInitialized(): Promise<void> {
  if (hasInitializedMap) {
    return;
  }
  if (!mapInitPromise) {
    mapInitPromise = initMap().finally(() => {
      mapInitPromise = null;
    });
  }
  await mapInitPromise;
}


/**
 * 执行一次完整的覆盖物渲染流程。
 * 包括图层样式同步、边界层刷新、标签层刷新和点位层刷新。
 */
function updateRenderStateBeforeCycle(): void {
  if (!shouldShowWorldCityLabels()) {
    clearWorldCityLabelState();
  }
}

function renderMapOverlays(): void {
  const AMap = beginRenderCycle();
  if (!AMap) {
    return;
  }
  updateRenderStateBeforeCycle();
  ensureWorldLayer(AMap);
  renderAllOverlayLayers(AMap);
  finishRenderCycle();
}

function updateMapTheme(): void {
  refreshMapVisualTheme();
}

function handleThemeWatch(): void {
  handleThemeChange();
}

function handleDataWatch(): void {
  handleDataChange();
}

function handleVisibleWatch(newVal?: boolean): void {
  handleVisibleChange(newVal);
  if (newVal) {
    void ensureMapInitialized();
  }
}

/**
 * 组件挂载后初始化视口状态并启动地图加载。
 */
function handleMounted(): void {
  initializeViewportState();
  if (props.visible) {
    void ensureMapInitialized();
  }
}

/**
 * 组件卸载前清理全局监听、定时器与地图资源。
 */
function handleBeforeUnmount(): void {
  teardownViewportState();
  destroyTimers();
  teardownMapInteractionHandlers();
  cleanupMapResources();
}

function handleZoomEndEvent(): void {
  handleZoomOrMoveEnd();
}

function handleMoveStartEvent(): void {
  handleMapInteractionStart();
}

/**
 * 绑定地图缩放/拖拽事件，用于标记用户交互并在结束后刷新覆盖物。
 */
function bindMapEvents(): void {
  if (!mapInstance || moveHandler || zoomEndHandler) {
    return;
  }
  moveHandler = () => {
    handleMoveStartEvent();
  };
  zoomEndHandler = () => {
    handleZoomEndEvent();
  };
  mapInstance.on("dragstart", moveHandler);
  mapInstance.on("zoomstart", moveHandler);
  mapInstance.on("zoomend", zoomEndHandler);
  mapInstance.on("moveend", zoomEndHandler);
}

function unbindMapEvents(): void {
  if (!mapInstance) {
    moveHandler = null;
    zoomEndHandler = null;
    return;
  }
  if (moveHandler) {
    mapInstance.off("dragstart", moveHandler);
    mapInstance.off("zoomstart", moveHandler);
  }
  if (zoomEndHandler) {
    mapInstance.off("zoomend", zoomEndHandler);
    mapInstance.off("moveend", zoomEndHandler);
  }
  moveHandler = null;
  zoomEndHandler = null;
}

/**
 * 绑定地图容器的鼠标事件，为桌面端提供双击放大和右键缩小快捷操作。
 */
function bindRootMouseEvents(): void {
  if (!mapRoot.value || rootClickHandler || rootContextMenuHandler) {
    return;
  }
  rootClickHandler = (event: MouseEvent) => {
    if (event.button !== 0 || event.detail < 2) {
      return;
    }
    adjustMapZoom(1);
  };
  rootContextMenuHandler = (event: MouseEvent) => {
    event.preventDefault();
    adjustMapZoom(-1);
  };
  mapRoot.value.addEventListener("click", rootClickHandler);
  mapRoot.value.addEventListener("contextmenu", rootContextMenuHandler);
}

function unbindRootMouseEvents(): void {
  if (!mapRoot.value) {
    rootClickHandler = null;
    rootContextMenuHandler = null;
    return;
  }
  if (rootClickHandler) {
    mapRoot.value.removeEventListener("click", rootClickHandler);
  }
  if (rootContextMenuHandler) {
    mapRoot.value.removeEventListener("contextmenu", rootContextMenuHandler);
  }
  rootClickHandler = null;
  rootContextMenuHandler = null;
}

function updateMapInteractionHandlers(): void {
  bindMapEvents();
  bindRootMouseEvents();
}

function teardownMapInteractionHandlers(): void {
  unbindMapEvents();
  unbindRootMouseEvents();
}

watch(() => props.theme, handleThemeWatch);

watch(
  () => [props.items, props.visitedProvinceNames] as const,
  () => {
    handleDataWatch();
  },
  { deep: true }
);

watch(() => props.visible, handleVisibleWatch);

onMounted(() => {
  handleMounted();
});

onBeforeUnmount(() => {
  handleBeforeUnmount();
});

/**
 * 在用户尚未主动移动地图时，将视角保持在预设的全国初始中心点。
 */
function fitInitialView(): void {
  if (!mapInstance || hasUserMoved) {
    return;
  }
  showMapBackdrop.value = false;
  mapInstance.setZoomAndCenter(INITIAL_ZOOM, INITIAL_CENTER, true);
}

/**
 * 根据快捷交互指令调整地图缩放级别，并保持当前中心点不变。
 */
function adjustMapZoom(delta: 1 | -1): void {
  if (!mapInstance) {
    return;
  }
  const currentZoom = mapInstance.getZoom();
  const nextZoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, currentZoom + delta));
  if (nextZoom === currentZoom) {
    return;
  }
  hasUserMoved = true;
  mapInstance.setZoomAndCenter(
    nextZoom,
    [mapInstance.getCenter().lng, mapInstance.getCenter().lat],
    true
  );
}

/**
 * 仅在组件可见且地图已成功加载时短暂展示一次交互提示。
 */
function showInteractionTipOnce(): void {
  if (tipTimer) {
    clearTimeout(tipTimer);
  }
  showInteractionTip.value = true;
  tipTimer = setTimeout(() => {
    showInteractionTip.value = false;
    tipTimer = null;
  }, 5000);
}
</script>
