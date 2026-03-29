<template>
  <div class="dyx-footprint-map relative h-full w-full overflow-hidden">
    <div
      class="pointer-events-none absolute inset-0 z-0"
      :class="mapGlowClass"
    ></div>
    <div
      ref="mapRoot"
      class="dyx-footprint-map--interactive relative z-[1] h-full w-full"
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
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from "vue";
import type { FootprintMapItem } from "@/utils/footprintGeo";
import {
  featureToPolygonPaths,
  getAllProvinceFeatures,
  getCityFeatureByName,
  getMapBoundaryItems,
  getMapLabelItems,
} from "@/utils/footprintGeo";
import { getAmapApiKeyConfigured, loadAmapSdk } from "@/utils/amapLoader";

const props = defineProps<{
  items: FootprintMapItem[];
  visitedProvinceNames: string[];
  theme: "light" | "dark";
}>();

const mapRoot = ref<HTMLElement | null>(null);
const statusMessage = ref("");

const overlayPanelClass = computed(() =>
  props.theme === "dark"
    ? "border-white/10 bg-slate-950/72 text-slate-200 shadow-[0_18px_56px_rgba(2,6,23,0.42)]"
    : "border-slate-200/80 bg-white/88 text-slate-700 shadow-[0_18px_56px_rgba(148,163,184,0.22)]"
);
const overlayTextClass = computed(() =>
  props.theme === "dark" ? "text-slate-200" : "text-slate-700"
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
let hasUserMoved = false;
let provinceOverlays: AMapPolygon[] = [];
let cityBoundaryOverlays: AMapPolygon[] = [];
let cityOverlays: AMapPolygon[] = [];
let markerOverlays: AMapMarker[] = [];
let labelOverlays: AMapText[] = [];
let latestRenderToken = 0;
let renderTimer: ReturnType<typeof setTimeout> | null = null;
let isOverlaysInitialized = false; // 记录是否已经初始化了持久化覆盖物
let moveHandler: (() => void) | null = null;
let zoomEndHandler: (() => void) | null = null;
let rootClickHandler: ((event: MouseEvent) => void) | null = null;
let rootContextMenuHandler: ((event: MouseEvent) => void) | null = null;

const INITIAL_CENTER: [number, number] = [104.195397, 35.86166];
const INITIAL_ZOOM = 4.2;
const MIN_ZOOM = 3;
const MAX_ZOOM = 9;
const LABEL_ZOOM_THRESHOLD = 4.8;
const CITY_NAME_ZOOM_THRESHOLD = 5.05;
const CITY_BOUNDARY_ZOOM_THRESHOLD = 5.05;
const allProvinceNames = getAllProvinceFeatures()
  .map((feature) => String(feature.properties?.name ?? "").trim())
  .filter((name): name is string => !!name);

function escapeHtml(value: string): string {
  return value
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#39;");
}

function buildMarkerContent(item: FootprintMapItem): string {
  const dotSize = item.isLatest ? 10 : 7;
  const labelText = escapeHtml(item.cityName || item.name);
  const labelDisplay = shouldShowLabels() ? "block" : "none";

  if (props.theme === "dark") {
    return `
      <div style="position:relative;display:flex;flex-direction:column;align-items:center;pointer-events:none;">
        <span style="display:block;width:${dotSize}px;height:${dotSize}px;border-radius:9999px;background:#ffffff;border:1.5px solid rgba(34,211,238,0.92);box-sizing:border-box;"></span>
        <span style="display:${labelDisplay};margin-top:8px;white-space:nowrap;font-size:12px;line-height:1;color:rgba(255,255,255,0.94);text-shadow:0 2px 12px rgba(0,0,0,0.48);letter-spacing:0.02em;">${labelText}</span>
      </div>
    `;
  }

  return `
    <div style="position:relative;display:flex;flex-direction:column;align-items:center;pointer-events:none;">
      <span style="display:block;width:${dotSize}px;height:${dotSize}px;border-radius:9999px;background:#2563eb;border:1.5px solid rgba(255,255,255,0.96);box-sizing:border-box;"></span>
      <span style="display:${labelDisplay};margin-top:8px;white-space:nowrap;font-size:12px;line-height:1;color:#0f172a;text-shadow:0 1px 8px rgba(255,255,255,0.82);letter-spacing:0.02em;">${labelText}</span>
    </div>
  `;
}

function getProvincePolygonStyle(visited: boolean) {
  if (props.theme === "dark") {
    return visited
      ? {
          strokeColor: "rgba(203, 213, 225, 0.5)",
          strokeOpacity: 1,
          strokeWeight: 1,
          fillColor: "rgba(8, 145, 178, 0.3)",
          fillOpacity: 0.3,
        }
      : {
          strokeColor: "rgba(148, 163, 184, 0.42)",
          strokeOpacity: 1,
          strokeWeight: 1,
          fillColor: "rgba(17, 24, 39, 0.88)",
          fillOpacity: 0.96,
        };
  }
  return visited
    ? {
        strokeColor: "rgba(203, 213, 225, 0.72)",
        strokeOpacity: 1,
        strokeWeight: 1,
        fillColor: "rgba(56, 189, 248, 0.3)",
        fillOpacity: 0.3,
      }
    : {
        strokeColor: "rgba(148, 163, 184, 0.58)",
        strokeOpacity: 1,
        strokeWeight: 1,
        fillColor: "rgba(203, 213, 225, 0.72)",
        fillOpacity: 0.9,
      };
}

function getCityPolygonStyle() {
  if (props.theme === "dark") {
    return {
      strokeColor: "rgba(255, 255, 255, 0.92)",
      strokeOpacity: 1,
      strokeWeight: 1,
      fillColor: "rgba(34, 211, 238, 0.3)",
      fillOpacity: 0.3,
    };
  }
  return {
    strokeColor: "rgba(255, 255, 255, 0.96)",
    strokeOpacity: 1,
    strokeWeight: 1,
    fillColor: "rgba(14, 165, 233, 0.3)",
    fillOpacity: 0.3,
  };
}

function getCityBoundaryStyle() {
  return props.theme === "dark"
    ? {
        strokeColor: "rgba(203, 213, 225, 0.34)",
        strokeOpacity: 1,
        strokeWeight: 1,
        fillOpacity: 0,
        fillColor: "rgba(0,0,0,0)",
      }
    : {
        strokeColor: "rgba(148, 163, 184, 0.46)",
        strokeOpacity: 1,
        strokeWeight: 1,
        fillOpacity: 0,
        fillColor: "rgba(0,0,0,0)",
      };
}

function getMapStyle(): string {
  return props.theme === "dark"
    ? "amap://styles/darkblue"
    : "amap://styles/whitesmoke";
}

function shouldShowLabels(): boolean {
  return (mapInstance?.getZoom() ?? INITIAL_ZOOM) >= LABEL_ZOOM_THRESHOLD;
}

function shouldShowCityNameLabels(): boolean {
  return (mapInstance?.getZoom() ?? INITIAL_ZOOM) >= CITY_NAME_ZOOM_THRESHOLD;
}

function shouldShowCityBoundaries(): boolean {
  return (
    (mapInstance?.getZoom() ?? INITIAL_ZOOM) >= CITY_BOUNDARY_ZOOM_THRESHOLD
  );
}

function getCityNameCollisionDistance(zoom: number): number {
  if (zoom >= 8.2) return 0;
  if (zoom >= 7.4) return 0.1;
  if (zoom >= 6.6) return 0.18;
  if (zoom >= 5.9) return 0.28;
  if (zoom >= 5.3) return 0.4;
  if (zoom >= 5.05) return 0.52;
  return 0.68;
}

function getLabelTextStyle(): AMapTextStyle {
  return props.theme === "dark"
    ? {
        "font-size": "12px",
        "font-weight": "500",
        color: "rgba(255,255,255,0.88)",
        "text-shadow": "0 2px 10px rgba(0,0,0,0.5)",
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

function clearProvinceOverlays(): void {
  if (!provinceOverlays.length || !mapInstance) {
    provinceOverlays = [];
    return;
  }
  mapInstance.remove(provinceOverlays);
  provinceOverlays.forEach((overlay) => overlay.setMap(null));
  provinceOverlays = [];
}

function clearCityBoundaryOverlays(): void {
  if (!cityBoundaryOverlays.length || !mapInstance) {
    cityBoundaryOverlays = [];
    return;
  }
  mapInstance.remove(cityBoundaryOverlays);
  cityBoundaryOverlays.forEach((overlay) => overlay.setMap(null));
  cityBoundaryOverlays = [];
}

function clearCityOverlays(): void {
  if (!cityOverlays.length || !mapInstance) {
    cityOverlays = [];
    return;
  }
  mapInstance.remove(cityOverlays);
  cityOverlays.forEach((overlay) => overlay.setMap(null));
  cityOverlays = [];
}

function clearLabelOverlays(): void {
  if (!labelOverlays.length || !mapInstance) {
    labelOverlays = [];
    return;
  }
  mapInstance.remove(labelOverlays);
  labelOverlays.forEach((overlay) => overlay.setMap(null));
  labelOverlays = [];
}

function renderProvinceOverlays(AMap: AMapNamespace): void {
  if (!mapInstance) {
    return;
  }
  // 如果已经初始化，不再重新创建，除非数据变更
  if (isOverlaysInitialized && provinceOverlays.length > 0) {
    return;
  }
  clearProvinceOverlays();
  const provinceNamesWithCityHighlights = new Set(
    props.items
      .map((item) => item.provinceName)
      .filter((provinceName): provinceName is string => !!provinceName)
  );
  const visitedProvinceNameSet = new Set(
    props.visitedProvinceNames.filter(
      (provinceName) => !provinceNamesWithCityHighlights.has(provinceName)
    )
  );
  const overlays: AMapPolygon[] = [];
  getAllProvinceFeatures().forEach((feature) => {
    const provinceName = String(feature.properties?.name ?? "").trim();
    const polygonStyle = getProvincePolygonStyle(
      visitedProvinceNameSet.has(provinceName)
    );
    const paths = featureToPolygonPaths(feature);
    paths.forEach((path) => {
      overlays.push(
        new AMap.Polygon({
          path,
          ...polygonStyle,
          bubble: false,
          zIndex: visitedProvinceNameSet.has(provinceName) ? 22 : 12,
        })
      );
    });
  });
  provinceOverlays = overlays;
  if (provinceOverlays.length) {
    mapInstance.add(provinceOverlays);
  }
}

function clearMarkerOverlays(): void {
  if (!markerOverlays.length || !mapInstance) {
    markerOverlays = [];
    return;
  }
  mapInstance.remove(markerOverlays);
  markerOverlays.forEach((overlay) => overlay.setMap(null));
  markerOverlays = [];
}

function renderCityBoundaryOverlays(AMap: AMapNamespace): void {
  if (!mapInstance) {
    return;
  }

  const shouldShow = shouldShowCityBoundaries();

  // 如果已经初始化过，只需要显示或隐藏，不再重新创建数千个对象
  if (isOverlaysInitialized && cityBoundaryOverlays.length > 0) {
    cityBoundaryOverlays.forEach((overlay) => {
      if (shouldShow) overlay.show();
      else overlay.hide();
    });
    return;
  }

  clearCityBoundaryOverlays();
  const boundaryStyle = getCityBoundaryStyle();
  cityBoundaryOverlays = getMapBoundaryItems(allProvinceNames).flatMap((item) =>
    item.paths.map(
      (path) =>
        new AMap.Polygon({
          path,
          ...boundaryStyle,
          bubble: false,
          zIndex: 20,
          visible: shouldShow, // 初始可见性
        })
    )
  );
  if (cityBoundaryOverlays.length) {
    mapInstance.add(cityBoundaryOverlays);
  }
}

function renderCityOverlays(AMap: AMapNamespace): void {
  if (!mapInstance) {
    return;
  }
  // 如果已经初始化过，不再重新创建，因为数据没变
  if (isOverlaysInitialized && cityOverlays.length > 0) {
    return;
  }
  clearCityOverlays();
  const cityPolygonStyle = getCityPolygonStyle();
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
            ...cityPolygonStyle,
            bubble: false,
            zIndex: 28,
          })
      )
    );
  });
  cityOverlays = Array.from(overlayMap.values()).flat();
  if (cityOverlays.length) {
    mapInstance.add(cityOverlays);
  }
}

function renderCityNameLabels(AMap: AMapNamespace): void {
  if (!mapInstance) {
    return;
  }
  clearLabelOverlays();
  if (!shouldShowCityNameLabels()) {
    return;
  }
  const zoom = mapInstance.getZoom();
  const collisionDistance = getCityNameCollisionDistance(zoom);
  const visibleItems: { position: [number, number] }[] = [];
  const labels = getMapLabelItems(allProvinceNames)
    .sort((left, right) => left.name.localeCompare(right.name, "zh-CN"))
    .filter((item) => {
      if (collisionDistance <= 0) {
        visibleItems.push(item);
        return true;
      }
      const [longitude, latitude] = item.position;
      const isTooClose = visibleItems.some((current) => {
        const [currentLongitude, currentLatitude] = current.position;
        return (
          Math.abs(currentLongitude - longitude) < collisionDistance &&
          Math.abs(currentLatitude - latitude) < collisionDistance
        );
      });
      if (isTooClose) {
        return false;
      }
      visibleItems.push(item);
      return true;
    })
    .map(
      (item) =>
        new AMap.Text({
          text: item.name,
          position: item.position,
          anchor: "center",
          offset: new AMap.Pixel(0, 0),
          style: getLabelTextStyle(),
          zIndex: 24,
        })
    );
  labelOverlays = labels;
  if (labelOverlays.length) {
    mapInstance.add(labelOverlays);
  }
}

function renderMarkers(AMap: AMapNamespace): void {
  if (!mapInstance) {
    return;
  }
  clearMarkerOverlays();
  const visibleItems = getVisibleItems(props.items);
  markerOverlays = visibleItems.map(
    (item) =>
      new AMap.Marker({
        position: item.position,
        anchor: "center",
        offset: new AMap.Pixel(0, 0),
        content: buildMarkerContent(item),
        zIndex: item.isLatest ? 130 : 120,
        extData: item,
      })
  );
  if (markerOverlays.length) {
    mapInstance.add(markerOverlays);
  }
}

function renderMapOverlays(): void {
  const AMap = window.AMap;
  if (!mapInstance || !AMap) {
    return;
  }

  // 防抖处理，避免频繁渲染
  if (renderTimer) {
    clearTimeout(renderTimer);
  }

  const performRender = (): void => {
    const renderToken = ++latestRenderToken;

    // 分批异步渲染，避免阻塞主线程
    requestAnimationFrame(() => {
      if (renderToken !== latestRenderToken) return;
      renderProvinceOverlays(AMap);

      requestAnimationFrame(() => {
        if (renderToken !== latestRenderToken) return;
        renderCityBoundaryOverlays(AMap);

        requestAnimationFrame(() => {
          if (renderToken !== latestRenderToken) return;
          renderCityOverlays(AMap);

          requestAnimationFrame(() => {
            if (renderToken !== latestRenderToken) return;
            renderCityNameLabels(AMap);
            renderMarkers(AMap);
            isOverlaysInitialized = true; // 首次全量渲染完成后标记
          });
        });
      });
    });
  };

  // 如果是第一次渲染或已经移动，则增加一点防抖
  // 如果在缩放/拖拽中，会由 zoomend/moveend 触发
  renderTimer = setTimeout(performRender, 60);
}

function fitInitialView(): void {
  if (!mapInstance || hasUserMoved) {
    return;
  }
  if (!markerOverlays.length) {
    mapInstance.setZoomAndCenter(INITIAL_ZOOM, INITIAL_CENTER, true);
    return;
  }
  mapInstance.setFitView(
    [
      ...provinceOverlays,
      ...cityBoundaryOverlays,
      ...cityOverlays,
      ...markerOverlays,
    ],
    true,
    [100, 120, 100, 120],
    5.6
  );
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
  renderMapOverlays();
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
        resizeEnable: true,
        dragEnable: true,
        zoomEnable: true,
        doubleClickZoom: true,
        jogEnable: false,
        keyboardEnable: false,
        scrollEnable: true,
        scrollWheel: false,
        features: ["bg", "road"],
        mapStyle: getMapStyle(),
      });
    }
    bindMapEvents();
    bindRootMouseEvents();
    statusMessage.value = "";

    // 初始渲染也使用分批异步模式，避免阻塞
    renderMapOverlays();
    fitInitialView();
  } catch (error) {
    statusMessage.value =
      error instanceof Error ? error.message : "高德地图加载失败。";
  }
}

function updateMapTheme(): void {
  isOverlaysInitialized = false; // 主题变更需要重新生成覆盖物
  mapInstance?.setMapStyle(getMapStyle());
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
    isOverlaysInitialized = false; // 数据变更需要重新生成覆盖物
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

onMounted(() => {
  void initMap();
});

onBeforeUnmount(() => {
  if (renderTimer) {
    clearTimeout(renderTimer);
  }
  unbindMapEvents();
  unbindRootMouseEvents();
  clearProvinceOverlays();
  clearCityBoundaryOverlays();
  clearCityOverlays();
  clearLabelOverlays();
  clearMarkerOverlays();
  mapInstance?.destroy();
  mapInstance = null;
  hasUserMoved = false;
});
</script>
