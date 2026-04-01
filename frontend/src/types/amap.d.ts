/**
 * 浏览器全局 Window 上补充的高德地图相关字段声明。
 */
interface Window {
  AMap?: AMapNamespace;
  _AMapSecurityConfig?: {
    securityJsCode?: string;
  };
}

/**
 * 高德地图实例类型别名。
 */
type AMapMap = InstanceType<AMapNamespace['Map']>;
type AMapMarker = InstanceType<AMapNamespace['Marker']>;
type AMapPolygon = InstanceType<AMapNamespace['Polygon']>;
type AMapText = InstanceType<AMapNamespace['Text']>;

/**
 * 高德地图像素坐标声明。
 */
interface AMapPixel {
  x: number;
  y: number;
}

/**
 * 高德地图文本样式声明。
 */
interface AMapTextStyle {
  'font-size'?: string;
  'font-weight'?: string;
  color?: string;
  'text-shadow'?: string;
  'background-color'?: string;
  border?: string;
}

/**
 * 高德地图文本覆盖物参数声明。
 */
interface AMapTextOptions {
  text?: string;
  position: [number, number];
  anchor?: string;
  offset?: AMapPixel;
  style?: AMapTextStyle;
  zIndex?: number;
  visible?: boolean;
}

interface AMapLngLatLike {
  lng: number;
  lat: number;
}

/**
 * 带事件监听能力的高德地图对象基础声明。
 */
interface AMapEventCapable {
  on(eventName: string, handler: (...args: any[]) => void): void;
  off(eventName: string, handler: (...args: any[]) => void): void;
}

/**
 * 高德地图实例初始化参数声明。
 */
interface AMapMapOptions {
  viewMode?: '2D' | '3D';
  zoom?: number;
  center?: [number, number];
  mapStyle?: string;
  zooms?: [number, number];
  showLabel?: boolean;
  showOversea?: boolean;
  language?: string;
  lang?: string;
  resizeEnable?: boolean;
  dragEnable?: boolean;
  zoomEnable?: boolean;
  doubleClickZoom?: boolean;
  jogEnable?: boolean;
  keyboardEnable?: boolean;
  scrollEnable?: boolean;
  scrollWheel?: boolean;
  features?: string[];
}

/**
 * 高德世界行政区图层样式参数声明。
 */
interface AMapDistrictLayerStyleOptions {
  'stroke-width'?: number;
  'stroke-color'?: string;
  'nation-stroke'?: string;
  'coastline-stroke'?: string;
  'province-stroke'?: string;
  'city-stroke'?: string;
  'county-stroke'?: string;
  fill?: string | ((properties: Record<string, unknown>) => string);
}

interface AMapDistrictLayerWorldOptions {
  zIndex?: number;
  zooms?: [number, number];
}

interface AMapDistrictLayerInstance {
  setMap(map: AMapMap | null): void;
  setStyles(styles: AMapDistrictLayerStyleOptions): void;
}

/**
 * 高德地图点标记参数声明。
 */
interface AMapMarkerOptions {
  position: [number, number];
  anchor?: string;
  offset?: AMapPixel;
  content?: string;
  zIndex?: number;
  extData?: unknown;
  title?: string;
}

/**
 * 高德地图多边形参数声明。
 */
interface AMapPolygonOptions {
  path: [number, number][];
  strokeColor?: string;
  strokeOpacity?: number;
  strokeWeight?: number;
  fillColor?: string;
  fillOpacity?: number;
  bubble?: boolean;
  zIndex?: number;
  visible?: boolean;
}

/**
 * 高德地图 Map 类型声明。
 */
declare class AMapMapClass implements AMapEventCapable {
  constructor(container: HTMLElement, options?: AMapMapOptions);
  add(overlays: unknown | unknown[]): void;
  remove(overlays: unknown | unknown[]): void;
  destroy(): void;
  clearMap(): void;
  setFitView(overlays?: unknown[], immediately?: boolean, avoid?: number[], maxZoom?: number): void;
  setMapStyle(style: string): void;
  setZoomAndCenter(zoom: number, center: [number, number], immediately?: boolean): void;
  getZoom(): number;
  getCenter(): AMapLngLatLike;
  on(eventName: string, handler: (...args: any[]) => void): void;
  on(eventName: 'complete', handler: () => void): void;
  off(eventName: string, handler: (...args: any[]) => void): void;
  off(eventName: 'complete', handler: () => void): void;
}

/**
 * 高德地图 Marker 类型声明。
 */
declare class AMapMarkerClass implements AMapEventCapable {
  constructor(options?: AMapMarkerOptions);
  setMap(map: AMapMap | null): void;
  getMap(): AMapMap | null;
  setContent(content: string): void;
  setzIndex(zIndex: number): void;
  on(eventName: string, handler: (...args: any[]) => void): void;
  off(eventName: string, handler: (...args: any[]) => void): void;
}

/**
 * 高德地图 Polygon 类型声明。
 */
declare class AMapPolygonClass implements AMapEventCapable {
  constructor(options?: AMapPolygonOptions);
  setMap(map: AMapMap | null): void;
  setOptions(options: Partial<AMapPolygonOptions>): void;
  on(eventName: string, handler: (...args: any[]) => void): void;
  off(eventName: string, handler: (...args: any[]) => void): void;
  show(): void;
  hide(): void;
}

/**
 * 高德地图世界行政区图层声明。
 */
declare class AMapDistrictLayerWorldClass implements AMapDistrictLayerInstance {
  constructor(options?: AMapDistrictLayerWorldOptions);
  setMap(map: AMapMap | null): void;
  setStyles(styles: AMapDistrictLayerStyleOptions): void;
}

interface AMapDistrictLayerNamespace {
  World: typeof AMapDistrictLayerWorldClass;
}

/**
 * 高德地图文本覆盖物声明。
 */
declare class AMapTextClass implements AMapEventCapable {
  constructor(options?: AMapTextOptions);
  setMap(map: AMapMap | null): void;
  setOptions(options: Partial<AMapTextOptions>): void;
  show(): void;
  hide(): void;
  on(eventName: string, handler: (...args: any[]) => void): void;
  off(eventName: string, handler: (...args: any[]) => void): void;
}

/**
 * 高德地图全局命名空间声明。
 */
interface AMapNamespace {
  Map: typeof AMapMapClass;
  Marker: typeof AMapMarkerClass;
  Polygon: typeof AMapPolygonClass;
  Text: typeof AMapTextClass;
  Pixel: new (x: number, y: number) => AMapPixel;
  DistrictLayer?: AMapDistrictLayerNamespace;
}
