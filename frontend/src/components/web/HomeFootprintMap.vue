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
  getCityFeatureByName,
  getMapBoundaryItems,
  getMapLabelItems,
  getWorldCountryBoundaryItems,
  getWorldCountryLabelItems,
  getWorldCityLabelItems,
  normalizeProvinceName,
  type FootprintMapItem,
} from "@/utils/footprintGeo";
import { getAmapApiKeyConfigured, loadAmapSdk } from "@/utils/amapLoader";

const props = defineProps<{
  items: FootprintMapItem[];
  visitedProvinceNames: string[];
  theme: "light" | "dark";
  visible?: boolean;
}>();

const mapRoot = ref<HTMLElement | null>(null);
const statusMessage = ref("");
const showInteractionTip = ref(false);
const isMobile = ref(false);
const showMapBackdrop = ref(true);

const checkIsMobile = (): void => {
  isMobile.value = window.innerWidth < 768 || "ontouchstart" in window;
};

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
let labelOverlays: AMapText[] = [];
let hasUserMoved = false;
let latestRenderToken = 0;
let renderTimer: ReturnType<typeof setTimeout> | null = null;
let tipTimer: ReturnType<typeof setTimeout> | null = null;
let moveHandler: (() => void) | null = null;
let zoomEndHandler: (() => void) | null = null;
let rootClickHandler: ((event: MouseEvent) => void) | null = null;
let rootContextMenuHandler: ((event: MouseEvent) => void) | null = null;
let worldCountryBoundariesReady = false;
let provinceOutlinesReady = false;
let visitedCityOverlaysKey = "";
let cityBoundariesReady = false;
let worldCountryLabelsReady = false;
let worldCityLabelsReady = false;
let cityNameLabelsReady = false;

const INITIAL_CENTER: [number, number] = [104.195397, 35.86166];
const INITIAL_ZOOM = 4.2;
const INITIAL_FIT_MAX_ZOOM = 5.6;
const MIN_ZOOM = 4.2;
const MAX_ZOOM = 9;
const LABEL_ZOOM_THRESHOLD = 4.8;
const COUNTRY_LABEL_MAX_ZOOM = 4.3;
const WORLD_CITY_LABEL_ZOOM_THRESHOLD = 4.5;
const CITY_BOUNDARY_ZOOM_THRESHOLD = 5.05;
const CITY_NAME_ZOOM_THRESHOLD = 4.8;
const ALL_PROVINCE_NAMES = getAllProvinceFeatures()
  .map((feature) =>
    normalizeProvinceName(String(feature.properties?.name ?? ""))
  )
  .filter((provinceName): provinceName is string => !!provinceName);

function escapeHtml(value: string): string {
  return value
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}

function shouldShowVisitedCityLabels(): boolean {
  return (mapInstance?.getZoom() ?? INITIAL_ZOOM) < CITY_NAME_ZOOM_THRESHOLD;
}

function getVisitedCityLabelStyle(isLatest: boolean): string {
  if (props.theme === "dark") {
    return isLatest
      ? "margin-top:8px;white-space:nowrap;font-size:12px;line-height:1;color:rgba(255,255,255,0.98);text-shadow:0 2px 12px rgba(0,0,0,0.48);letter-spacing:0.02em;font-weight:600;"
      : "margin-top:8px;white-space:nowrap;font-size:12px;line-height:1;color:rgba(226,232,240,0.94);text-shadow:0 2px 12px rgba(0,0,0,0.48);letter-spacing:0.02em;";
  }

  return isLatest
    ? "margin-top:8px;white-space:nowrap;font-size:12px;line-height:1;color:#0f172a;text-shadow:0 1px 8px rgba(255,255,255,0.82);letter-spacing:0.02em;font-weight:600;"
    : "margin-top:8px;white-space:nowrap;font-size:12px;line-height:1;color:#334155;text-shadow:0 1px 8px rgba(255,255,255,0.82);letter-spacing:0.02em;";
}

function buildMarkerContent(item: FootprintMapItem): string {
  const dotSize = item.isLatest ? 10 : 7;
  const labelText = escapeHtml(item.cityName || item.name);
  const labelDisplay =
    shouldShowLabels() || shouldShowVisitedCityLabels() ? "block" : "none";

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

function shouldShowWorldCityLabels(): boolean {
  return (
    (mapInstance?.getZoom() ?? INITIAL_ZOOM) >= WORLD_CITY_LABEL_ZOOM_THRESHOLD
  );
}

function getCountryLabelTextStyle(): AMapTextStyle {
  return props.theme === "dark"
    ? {
        "font-size": "11px",
        "font-weight": "500",
        color: "rgba(148,163,184,0.92)",
        "text-shadow": "0 2px 10px rgba(2,6,23,0.9)",
        "background-color": "transparent",
        border: "none",
      }
    : {
        "font-size": "11px",
        "font-weight": "500",
        color: "rgba(71,85,105,0.86)",
        "text-shadow": "0 1px 8px rgba(255,255,255,0.92)",
        "background-color": "transparent",
        border: "none",
      };
}

function shouldShowCityNameLabels(): boolean {
  return (mapInstance?.getZoom() ?? INITIAL_ZOOM) >= CITY_NAME_ZOOM_THRESHOLD;
}

function getCityBoundaryStyle() {
  return props.theme === "dark"
    ? {
        strokeColor: "rgba(100, 116, 139, 0.22)",
        strokeOpacity: 1,
        strokeWeight: 0.8,
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
  if (!worldCountryBoundariesReady) {
    worldCountryBoundaryOverlays = getWorldCountryBoundaryItems().flatMap(
      (item) =>
        item.paths.map(
          (path) =>
            new AMap.Polygon({
              path,
              strokeColor:
                props.theme === "dark"
                  ? "rgba(100, 116, 139, 0.15)"
                  : "rgba(148, 163, 184, 0.12)",
              strokeOpacity: 0.15,
              strokeWeight: props.theme === "dark" ? 0.8 : 0.7,
              fillColor: "rgba(0,0,0,0)",
              fillOpacity: 0,
              bubble: false,
              zIndex: 8,
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
    overlay.setOptions({
      strokeColor:
        props.theme === "dark"
          ? "rgba(100, 116, 139, 0.15)"
          : "rgba(148, 163, 184, 0.12)",
      strokeOpacity: 0.15,
      strokeWeight: props.theme === "dark" ? 0.8 : 0.7,
    });
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
    provinceOutlineOverlays = getAllProvinceFeatures().flatMap((feature) =>
      featureToPolygonPaths(feature).map(
        (path) =>
          new AMap.Polygon({
            path,
            ...getProvinceOutlineStyle(),
            bubble: false,
            zIndex: 12,
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
  const nextKey = props.items
    .map((item) => `${item.provinceName || ""}::${item.name || item.cityName}`)
    .sort()
    .join("||");
  if (visitedCityOverlaysKey !== nextKey) {
    clearVisitedCityOverlays();
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
              zIndex: 34,
            })
        )
      );
    });
    visitedCityOverlays = Array.from(overlayMap.values()).flat();
    if (visitedCityOverlays.length) {
      mapInstance.add(visitedCityOverlays);
    }
    visitedCityOverlaysKey = nextKey;
    return;
  }
  const nextStyle = getVisitedCityPolygonStyle();
  visitedCityOverlays.forEach((overlay) => {
    overlay.setOptions(nextStyle);
    overlay.show();
  });
}

function getLabelTextStyle(): AMapTextStyle {
  return props.theme === "dark"
    ? {
        "font-size": "12px",
        "font-weight": "500",
        color: "rgba(226,232,240,0.92)",
        "text-shadow": "0 2px 10px rgba(0,0,0,0.52)",
        "background-color": "transparent",
        border: "none",
      }
    : {
        "font-size": "12px",
        "font-weight": "500",
        color: "#334155",
        "text-shadow": "0 1px 8px rgba(255,255,255,0.92)",
        "background-color": "transparent",
        border: "none",
      };
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
  if (!cityBoundariesReady) {
    cityBoundaryOverlays = getMapBoundaryItems(ALL_PROVINCE_NAMES).flatMap(
      (item) =>
        item.paths.map(
          (path) =>
            new AMap.Polygon({
              path,
              ...getCityBoundaryStyle(),
              bubble: false,
              zIndex: 30,
              visible: shouldShowCityBoundaries(),
            })
        )
    );
    if (cityBoundaryOverlays.length) {
      mapInstance.add(cityBoundaryOverlays);
    }
    cityBoundariesReady = true;
    return;
  }
  const visible = shouldShowCityBoundaries();
  const nextStyle = getCityBoundaryStyle();
  cityBoundaryOverlays.forEach((overlay) => {
    overlay.setOptions(nextStyle);
    if (visible) {
      overlay.show();
      return;
    }
    overlay.hide();
  });
}

function renderCityNameLabels(AMap: AMapNamespace): void {
  if (!mapInstance) {
    return;
  }

  // 1. 处理世界国家标签
  const showCountry = shouldShowCountryLabels();
  if (!worldCountryLabelsReady) {
    worldCountryLabelOverlays = getWorldCountryLabelItems().map(
      (item) =>
        new AMap.Text({
          text: item.name,
          position: item.position,
          anchor: "center",
          offset: new AMap.Pixel(0, 0),
          style: getCountryLabelTextStyle(),
          zIndex: 16,
          visible: showCountry,
        })
    );
    if (worldCountryLabelOverlays.length) {
      mapInstance.add(worldCountryLabelOverlays);
    }
    worldCountryLabelsReady = true;
  } else {
    const nextStyle = getCountryLabelTextStyle();
    worldCountryLabelOverlays.forEach((overlay) => {
      overlay.setOptions({ style: nextStyle });
      if (showCountry) {
        overlay.show();
      } else {
        overlay.hide();
      }
    });
  }

  // 2. 处理世界城市标签
  const showWorldCity = shouldShowWorldCityLabels();
  if (!worldCityLabelsReady) {
    worldCityLabelOverlays = getWorldCityLabelItems().map(
      (item) =>
        new AMap.Text({
          text: item.name,
          position: item.position,
          anchor: "center",
          offset: new AMap.Pixel(0, 0),
          style: getLabelTextStyle(), // 复用中国城市的标签样式
          zIndex: 31, // 比中国城市低一级
          visible: showWorldCity,
        })
    );
    if (worldCityLabelOverlays.length) {
      mapInstance.add(worldCityLabelOverlays);
    }
    worldCityLabelsReady = true;
  } else {
    const nextStyle = getLabelTextStyle();
    worldCityLabelOverlays.forEach((overlay) => {
      overlay.setOptions({ style: nextStyle });
      if (showWorldCity) {
        overlay.show();
      } else {
        overlay.hide();
      }
    });
  }

  // 3. 处理中国城市标签
  const showCity = shouldShowCityNameLabels();
  if (!cityNameLabelsReady) {
    cityNameLabelOverlays = getMapLabelItems(ALL_PROVINCE_NAMES).map(
      (item) =>
        new AMap.Text({
          text: item.name,
          position: item.position,
          anchor: "center",
          offset: new AMap.Pixel(0, 0),
          style: getLabelTextStyle(),
          zIndex: 32,
          visible: showCity,
        })
    );
    if (cityNameLabelOverlays.length) {
      mapInstance.add(cityNameLabelOverlays);
    }
    cityNameLabelsReady = true;
  } else {
    const nextStyle = getLabelTextStyle();
    cityNameLabelOverlays.forEach((overlay) => {
      overlay.setOptions({ style: nextStyle });
      if (showCity) {
        overlay.show();
      } else {
        overlay.hide();
      }
    });
  }
}

function shouldShowLabels(): boolean {
  return (mapInstance?.getZoom() ?? INITIAL_ZOOM) >= LABEL_ZOOM_THRESHOLD;
}

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

function getCollisionDistance(zoom: number): number {
  if (zoom >= 6.2) return 0;
  if (zoom >= 5.4) return 0.45;
  if (zoom >= 4.8) return 0.75;
  if (zoom >= 4.2) return 1.15;
  return 1.75;
}

function clearMarkerOverlays(): void {
  if (mapInstance) {
    if (markerOverlays.length) {
      mapInstance.remove(markerOverlays);
    }
  }
  markerOverlays.forEach((overlay) => overlay.setMap(null));
  markerOverlays = [];
  markerCache.clear();
}

function renderMarkers(AMap: AMapNamespace): void {
  if (!mapInstance) {
    return;
  }

  const visibleItems = getVisibleItems(props.items);
  const nextOverlays: AMapMarker[] = [];
  const nextCache = new Map<string, AMapMarker>();

  visibleItems.forEach((item) => {
    const key = `${item.id || item.name}::${item.position[0]}::${
      item.position[1]
    }::${props.theme}`;
    let marker = markerCache.get(key);

    if (marker) {
      // 如果缩放发生变化，可能需要更新内容显隐
      marker.setContent(buildMarkerContent(item));
      nextCache.set(key, marker);
      nextOverlays.push(marker);
      markerCache.delete(key);
    } else {
      marker = new AMap.Marker({
        position: item.position,
        anchor: "center",
        offset: new AMap.Pixel(0, 0),
        content: buildMarkerContent(item),
        zIndex: item.isLatest ? 130 : 120,
        extData: item,
      });
      nextCache.set(key, marker);
      nextOverlays.push(marker);
    }
  });

  // 移除不再需要的 marker
  if (markerCache.size > 0) {
    mapInstance.remove(Array.from(markerCache.values()));
    markerCache.forEach((m) => m.setMap(null));
  }

  // 添加新的 marker
  const toAdd = nextOverlays.filter((m) => !m.getMap());
  if (toAdd.length > 0) {
    mapInstance.add(toAdd);
  }

  markerOverlays = nextOverlays;
  markerCache = nextCache;
  showMapBackdrop.value = false;
}

function renderMapOverlays(): void {
  const AMap = window.AMap;
  if (!mapInstance || !AMap) {
    return;
  }

  if (renderTimer) {
    clearTimeout(renderTimer);
    renderTimer = null;
  }

  ensureWorldLayer(AMap);
  renderWorldCountryBoundaryOverlays(AMap);
  renderProvinceOutlineOverlays(AMap);
  renderCityBoundaryOverlays(AMap);
  renderVisitedCityOverlays(AMap);

  const renderToken = ++latestRenderToken;
  if (renderToken !== latestRenderToken) {
    return;
  }
  renderCityNameLabels(AMap);
  renderMarkers(AMap);
}

function fitInitialView(): void {
  if (!mapInstance || hasUserMoved) {
    return;
  }
  showMapBackdrop.value = false;
  mapInstance.setZoomAndCenter(INITIAL_ZOOM, INITIAL_CENTER, true);
}

function bindMapEvents(): void {
  if (!mapInstance || moveHandler || zoomEndHandler) {
    return;
  }
  moveHandler = () => {
    hasUserMoved = true;
    // 移动时不重新渲染复杂的覆盖物，仅在停止后渲染
  };
  zoomEndHandler = () => {
    renderMapOverlays();
  };
  mapInstance.on("dragstart", moveHandler);
  mapInstance.on("zoomstart", moveHandler);
  mapInstance.on("zoomend", zoomEndHandler);
  mapInstance.on("moveend", zoomEndHandler); // 移动停止后触发渲染
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

async function initMap(): Promise<void> {
  if (!mapRoot.value) {
    return;
  }
  if (!getAmapApiKeyConfigured()) {
    statusMessage.value = "未配置 VITE_AMAP_API_KEY，足迹地图暂时无法加载。";
    return;
  }
  statusMessage.value = "地图加载中…";
  try {
    const AMap = await loadAmapSdk();
    if (!mapRoot.value) {
      return;
    }
    if (!mapInstance) {
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
    bindMapEvents();
    bindRootMouseEvents();
    disableBaseMapNoise();
    statusMessage.value = "";
    showInteractionTipOnce();

    // 初始渲染也使用分批异步模式，避免阻塞
    // 不渲染中国地图覆盖物，让高德地图默认显示世界地图
    renderMapOverlays();
    fitInitialView();
  } catch (error) {
    statusMessage.value =
      error instanceof Error ? error.message : "高德地图加载失败。";
  }
}

function updateMapTheme(): void {
  if (!mapInstance) {
    return;
  }
  showMapBackdrop.value = true;
  mapInstance.setMapStyle(getMapStyle());
  worldLayer?.setStyles(getWorldLayerStyles());
  disableBaseMapNoise();
  renderMapOverlays();
}

watch(
  () => props.theme,
  () => {
    updateMapTheme();
  }
);

watch(
  () => [props.items, props.visitedProvinceNames] as const,
  () => {
    const renderToken = ++latestRenderToken;
    queueMicrotask(() => {
      if (renderToken !== latestRenderToken) {
        return;
      }
      renderMapOverlays();
      fitInitialView();
    });
  },
  { deep: true }
);

watch(
  () => props.visible,
  (newVal) => {
    if (newVal) {
      showInteractionTipOnce();
    }
  }
);

onMounted(() => {
  checkIsMobile();
  window.addEventListener("resize", checkIsMobile);
  void initMap();
});

onBeforeUnmount(() => {
  window.removeEventListener("resize", checkIsMobile);
  if (renderTimer) {
    clearTimeout(renderTimer);
  }
  if (tipTimer) {
    clearTimeout(tipTimer);
  }
  unbindMapEvents();
  unbindRootMouseEvents();
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
});
</script>
