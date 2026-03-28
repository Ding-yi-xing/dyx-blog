interface Window {
  AMap?: AMapNamespace;
  _AMapSecurityConfig?: {
    securityJsCode?: string;
  };
}

type AMapMap = InstanceType<AMapNamespace['Map']>;
type AMapMarker = InstanceType<AMapNamespace['Marker']>;
type AMapPolygon = InstanceType<AMapNamespace['Polygon']>;
type AMapText = InstanceType<AMapNamespace['Text']>;

interface AMapPixel {
  x: number;
  y: number;
}

interface AMapTextStyle {
  'font-size'?: string;
  'font-weight'?: string;
  color?: string;
  'text-shadow'?: string;
  'background-color'?: string;
  border?: string;
}

interface AMapTextOptions {
  text?: string;
  position: [number, number];
  anchor?: string;
  offset?: AMapPixel;
  style?: AMapTextStyle;
  zIndex?: number;
}

interface AMapLngLatLike {
  lng: number;
  lat: number;
}

interface AMapEventCapable {
  on(eventName: string, handler: (...args: any[]) => void): void;
  off(eventName: string, handler: (...args: any[]) => void): void;
}

interface AMapMapOptions {
  viewMode?: '2D' | '3D';
  zoom?: number;
  center?: [number, number];
  mapStyle?: string;
  zooms?: [number, number];
  showLabel?: boolean;
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

interface AMapMarkerOptions {
  position: [number, number];
  anchor?: string;
  offset?: AMapPixel;
  content?: string;
  zIndex?: number;
  extData?: unknown;
  title?: string;
}

interface AMapPolygonOptions {
  path: [number, number][];
  strokeColor?: string;
  strokeOpacity?: number;
  strokeWeight?: number;
  fillColor?: string;
  fillOpacity?: number;
  bubble?: boolean;
  zIndex?: number;
}

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
  off(eventName: string, handler: (...args: any[]) => void): void;
}

declare class AMapMarkerClass implements AMapEventCapable {
  constructor(options?: AMapMarkerOptions);
  setMap(map: AMapMap | null): void;
  on(eventName: string, handler: (...args: any[]) => void): void;
  off(eventName: string, handler: (...args: any[]) => void): void;
}

declare class AMapPolygonClass implements AMapEventCapable {
  constructor(options?: AMapPolygonOptions);
  setMap(map: AMapMap | null): void;
  on(eventName: string, handler: (...args: any[]) => void): void;
  off(eventName: string, handler: (...args: any[]) => void): void;
}

declare class AMapTextClass implements AMapEventCapable {
  constructor(options?: AMapTextOptions);
  setMap(map: AMapMap | null): void;
  on(eventName: string, handler: (...args: any[]) => void): void;
  off(eventName: string, handler: (...args: any[]) => void): void;
}

interface AMapNamespace {
  Map: typeof AMapMapClass;
  Marker: typeof AMapMarkerClass;
  Polygon: typeof AMapPolygonClass;
  Text: typeof AMapTextClass;
  Pixel: new (x: number, y: number) => AMapPixel;
}
