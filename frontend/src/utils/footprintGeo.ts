import chinaGeoJson from 'china-map-data/china.js';
import provinceGeoCollection from 'china-map-data/province/index.js';
import type { FootprintData } from '@/api/modules/site';

export interface GeoPoint {
  longitude: number;
  latitude: number;
}

export interface FootprintMapItem {
  id?: number;
  name: string;
  cityName: string;
  provinceName: string | null;
  position: [number, number];
  importance: number;
  isLatest: boolean;
  raw: FootprintData;
}

export interface MapLabelItem {
  name: string;
  provinceName: string | null;
  position: [number, number];
}

export interface MapBoundaryItem {
  name: string;
  provinceName: string | null;
  paths: [number, number][][];
}

type GeoJsonFeatureProperties = {
  name?: string;
  adcode?: string | number;
  id?: string | number;
  cp?: [number, number];
  [key: string]: unknown;
};

export interface GeoJsonFeature {
  type: string;
  properties?: GeoJsonFeatureProperties;
  geometry?: {
    type?: string;
    coordinates?: unknown;
    encodeOffsets?: unknown;
  } | null;
}

interface GeoJsonCollection {
  type: string;
  features?: GeoJsonFeature[];
  UTF8Encoding?: boolean;
}

type EncodedRing = string;
type EncodedPolygon = EncodedRing[];
type EncodedMultiPolygon = EncodedPolygon[];
type EncodeOffset = [number, number];
type EncodeOffsetPolygon = EncodeOffset[];
type EncodeOffsetMultiPolygon = EncodeOffsetPolygon[];

function unwrapModuleExport<T>(value: T | { default?: T }): T {
  if (value && typeof value === 'object' && 'default' in value) {
    return (value as { default?: T }).default ?? (value as T);
  }
  return value as T;
}

function decodePolygonRing(encoded: EncodedRing, encodeOffsets: EncodeOffset): [number, number][] {
  const result: [number, number][] = [];
  let prevX = encodeOffsets[0];
  let prevY = encodeOffsets[1];

  for (let index = 0; index < encoded.length; index += 2) {
    let x = encoded.charCodeAt(index) - 64;
    let y = encoded.charCodeAt(index + 1) - 64;
    x = (x >> 1) ^ -(x & 1);
    y = (y >> 1) ^ -(y & 1);
    x += prevX;
    y += prevY;
    prevX = x;
    prevY = y;
    result.push([x / 1024, y / 1024]);
  }

  return result;
}

function decodeFeatureGeometry(feature: GeoJsonFeature): void {
  const geometry = feature.geometry;
  if (!geometry?.coordinates || !geometry.encodeOffsets || !geometry.type) {
    return;
  }
  if (geometry.type === 'Polygon') {
    const encodedPolygon = geometry.coordinates as EncodedPolygon;
    const offsets = geometry.encodeOffsets as EncodeOffsetPolygon;
    geometry.coordinates = encodedPolygon.map((ring, index) => decodePolygonRing(ring, offsets[index]));
    return;
  }
  if (geometry.type === 'MultiPolygon') {
    const encodedMultiPolygon = geometry.coordinates as EncodedMultiPolygon;
    const offsets = geometry.encodeOffsets as EncodeOffsetMultiPolygon;
    geometry.coordinates = encodedMultiPolygon.map((polygon, polygonIndex) =>
      polygon.map((ring, ringIndex) => decodePolygonRing(ring, offsets[polygonIndex][ringIndex]))
    );
  }
}

function normalizeGeoJsonCollection(collection: GeoJsonCollection): GeoJsonCollection {
  if (!collection.UTF8Encoding) {
    return collection;
  }
  collection.features?.forEach((feature) => decodeFeatureGeometry(feature));
  collection.UTF8Encoding = false;
  return collection;
}

const normalizedChinaGeoJson = normalizeGeoJsonCollection(
  unwrapModuleExport(chinaGeoJson as GeoJsonCollection | { default?: GeoJsonCollection })
);
const normalizedProvinceGeoCollection = unwrapModuleExport(
  provinceGeoCollection as Record<string, GeoJsonCollection | undefined> | { default?: Record<string, GeoJsonCollection | undefined> }
);

export const DIRECT_ADMIN_PROVINCE_SET = new Set([
  '北京市',
  '天津市',
  '上海市',
  '重庆市',
  '香港特别行政区',
  '澳门特别行政区',
  '台湾省'
]);

const FOOTPRINT_CITY_COORDINATE_OVERRIDE_MAP: Record<string, GeoPoint> = {
  山南市: { longitude: 91.95, latitude: 29.2 }
};

const CITY_GEO_MAP: Record<string, GeoPoint> = {
  厦门市: { longitude: 118.11022, latitude: 24.49047 },
  大连市: { longitude: 121.61468, latitude: 38.91459 },
  淮安市: { longitude: 119.02127, latitude: 33.59751 },
  舟山市: { longitude: 122.20778, latitude: 29.98528 },
  南京市: { longitude: 118.79688, latitude: 32.06026 },
  成都市: { longitude: 104.06654, latitude: 30.57227 },
  山南市: { longitude: 91.76653, latitude: 29.23602 },
  拉萨市: { longitude: 91.13221, latitude: 29.66036 },
  咸阳市: { longitude: 108.70899, latitude: 34.32932 },
  襄阳市: { longitude: 112.12255, latitude: 32.00899 },
  南昌市: { longitude: 115.8582, latitude: 28.68289 },
  铜陵市: { longitude: 117.81154, latitude: 30.94552 },
  上饶市: { longitude: 117.94357, latitude: 28.45563 },
  上海市: { longitude: 121.4737, latitude: 31.23037 },
  宁波市: { longitude: 121.55027, latitude: 29.87456 },
  温州市: { longitude: 120.69939, latitude: 27.99492 },
  杭州市: { longitude: 120.15507, latitude: 30.27408 },
  泉州市: { longitude: 118.67587, latitude: 24.87413 },
  宁德市: { longitude: 119.54793, latitude: 26.66571 },
  南平市: { longitude: 118.17838, latitude: 26.63563 },
  福州市: { longitude: 119.29647, latitude: 26.07451 },
  芜湖市: { longitude: 118.43294, latitude: 31.35246 },
  济宁市: { longitude: 116.58724, latitude: 35.41539 },
  枣庄市: { longitude: 117.323, latitude: 34.81071 },
  徐州市: { longitude: 117.28412, latitude: 34.20577 },
  宿州市: { longitude: 116.96417, latitude: 33.64765 },
  南阳市: { longitude: 112.52832, latitude: 32.99073 }
};

const PROVINCE_MODULE_ALIAS_MAP: Record<string, string> = {
  北京市: 'beijing',
  天津市: 'tianjin',
  河北省: 'hebei',
  山西省: 'shanxi',
  内蒙古自治区: 'neimenggu',
  辽宁省: 'liaoning',
  吉林省: 'jilin',
  黑龙江省: 'heilongjiang',
  上海市: 'shanghai',
  江苏省: 'jiangsu',
  浙江省: 'zhejiang',
  安徽省: 'anhui',
  福建省: 'fujian',
  江西省: 'jiangxi',
  山东省: 'shandong',
  河南省: 'henan',
  湖北省: 'hubei',
  湖南省: 'hunan',
  广东省: 'guangdong',
  广西壮族自治区: 'guangxi',
  海南省: 'hainan',
  重庆市: 'chongqing',
  四川省: 'sichuan',
  贵州省: 'guizhou',
  云南省: 'yunnan',
  西藏自治区: 'xizang',
  陕西省: 'shanxi1',
  甘肃省: 'gansu',
  青海省: 'qinghai',
  宁夏回族自治区: 'ningxia',
  新疆维吾尔自治区: 'xinjiang',
  香港特别行政区: 'xianggang',
  澳门特别行政区: 'aomen',
  台湾省: 'taiwan'
};

export function normalizeCityName(value?: string): string | null {
  const normalized = value?.trim();
  if (!normalized) {
    return null;
  }
  const aliases: Record<string, string> = {
    山南: '山南市',
    山南地区: '山南市'
  };
  if (aliases[normalized]) {
    return aliases[normalized];
  }
  if (normalized.endsWith('市') || normalized.endsWith('地区') || normalized.endsWith('自治州') || normalized.endsWith('盟')) {
    return normalized;
  }
  return `${normalized}市`;
}

export function normalizeProvinceName(value?: string): string | null {
  const normalized = value?.trim();
  if (!normalized) {
    return null;
  }
  const aliases: Record<string, string> = {
    北京: '北京市',
    北京市: '北京市',
    天津: '天津市',
    天津市: '天津市',
    上海: '上海市',
    上海市: '上海市',
    重庆: '重庆市',
    重庆市: '重庆市',
    内蒙古: '内蒙古自治区',
    内蒙古自治区: '内蒙古自治区',
    广西: '广西壮族自治区',
    广西壮族自治区: '广西壮族自治区',
    西藏: '西藏自治区',
    西藏自治区: '西藏自治区',
    宁夏: '宁夏回族自治区',
    宁夏回族自治区: '宁夏回族自治区',
    新疆: '新疆维吾尔自治区',
    新疆维吾尔自治区: '新疆维吾尔自治区',
    香港: '香港特别行政区',
    香港特别行政区: '香港特别行政区',
    澳门: '澳门特别行政区',
    澳门特别行政区: '澳门特别行政区'
  };
  if (aliases[normalized]) {
    return aliases[normalized];
  }
  if (normalized.endsWith('省') || normalized.endsWith('市') || normalized.endsWith('自治区') || normalized.endsWith('特别行政区')) {
    return normalized;
  }
  return `${normalized}省`;
}

export function formatMapRegionLabel(name?: string): string {
  const normalized = name?.trim() || '';
  return normalized.replace(/(特别行政区|自治州|地区|盟)$/u, '').replace(/市$/u, '');
}

function resolveProvinceModuleName(provinceName?: string): string | null {
  const normalizedProvinceName = normalizeProvinceName(provinceName);
  if (!normalizedProvinceName) {
    return null;
  }
  return PROVINCE_MODULE_ALIAS_MAP[normalizedProvinceName] ?? null;
}

function getProvinceGeoCollection(provinceName?: string): GeoJsonCollection | null {
  const moduleName = resolveProvinceModuleName(provinceName);
  if (!moduleName) {
    return null;
  }
  const provinceModule = normalizedProvinceGeoCollection[moduleName];
  if (!provinceModule) {
    return null;
  }
  return normalizeGeoJsonCollection(unwrapModuleExport(provinceModule as GeoJsonCollection | { default?: GeoJsonCollection }));
}

function isPrefectureLevelCityFeature(feature: GeoJsonFeature, provinceName?: string): boolean {
  const cityName = String(feature.properties?.name ?? '').trim();
  if (!cityName) {
    return false;
  }
  const normalizedProvinceName = normalizeProvinceName(provinceName);
  if (normalizedProvinceName && DIRECT_ADMIN_PROVINCE_SET.has(normalizedProvinceName)) {
    return cityName.endsWith('区') || cityName.endsWith('县');
  }
  return cityName.endsWith('市') || cityName.endsWith('地区') || cityName.endsWith('自治州') || cityName.endsWith('盟');
}

function isMunicipalDistrictFeature(feature: GeoJsonFeature, provinceName?: string): boolean {
  const cityName = String(feature.properties?.name ?? '').trim();
  if (!cityName) {
    return false;
  }
  const normalizedProvinceName = normalizeProvinceName(provinceName);
  if (!normalizedProvinceName || !DIRECT_ADMIN_PROVINCE_SET.has(normalizedProvinceName)) {
    return false;
  }
  return cityName.endsWith('区') || cityName.endsWith('县') || cityName.endsWith('市');
}

export function collectProvinceBoundaryFeatures(provinceName?: string): GeoJsonFeature[] {
  const provinceCollection = getProvinceGeoCollection(provinceName);
  if (!provinceCollection?.features?.length) {
    return [];
  }
  return provinceCollection.features
    .filter((feature) => feature?.geometry && feature.properties?.name)
    .filter((feature) => {
      const normalizedProvinceName = normalizeProvinceName(provinceName);
      if (normalizedProvinceName && DIRECT_ADMIN_PROVINCE_SET.has(normalizedProvinceName)) {
        return isMunicipalDistrictFeature(feature, provinceName);
      }
      return isPrefectureLevelCityFeature(feature, provinceName);
    })
    .map((feature) => ({
      ...feature,
      properties: {
        ...feature.properties,
        name: normalizeCityName(String(feature.properties?.name ?? '')) ?? String(feature.properties?.name ?? '')
      }
    }));
}

export function getCityFeatureByName(cityName?: string, provinceName?: string): GeoJsonFeature | null {
  const normalizedCityName = normalizeCityName(cityName) ?? (cityName?.trim() || null);
  if (!normalizedCityName) {
    return null;
  }
  const normalizedProvinceName = normalizeProvinceName(provinceName);
  if (DIRECT_ADMIN_PROVINCE_SET.has(normalizedCityName)) {
    return getProvinceFeatureByName(normalizedCityName);
  }
  if (normalizedProvinceName && DIRECT_ADMIN_PROVINCE_SET.has(normalizedProvinceName) && normalizedCityName === normalizedProvinceName) {
    return getProvinceFeatureByName(normalizedProvinceName);
  }
  const provinceFeatures = collectProvinceBoundaryFeatures(provinceName).filter((feature) => feature.properties?.name === normalizedCityName);
  if (!provinceFeatures.length) {
    return null;
  }
  return provinceFeatures.find((feature) => featureToPolygonPaths(feature).length > 0) ?? provinceFeatures[0] ?? null;
}

export function getProvinceFeatureByName(provinceName?: string): GeoJsonFeature | null {
  const normalizedProvinceName = normalizeProvinceName(provinceName);
  if (!normalizedProvinceName) {
    return null;
  }
  return (
    normalizedChinaGeoJson.features?.find(
      (feature) => normalizeProvinceName(String(feature.properties?.name ?? '')) === normalizedProvinceName
    ) ?? null
  );
}

export function getAllProvinceFeatures(): GeoJsonFeature[] {
  return (
    normalizedChinaGeoJson.features
      ?.filter((feature) => feature?.geometry && feature.properties?.name)
      .map((feature) => ({
        ...feature,
        properties: {
          ...feature.properties,
          name: normalizeProvinceName(String(feature.properties?.name ?? '')) ?? String(feature.properties?.name ?? '')
        }
      })) ?? []
  );
}

export function resolveCityFeatureCenter(item: Pick<FootprintData, 'cityName' | 'regionName'>): GeoPoint | null {
  const feature = getCityFeatureByName(item.cityName, item.regionName);
  const cp = feature?.properties?.cp;
  if (Array.isArray(cp) && cp.length === 2) {
    return {
      longitude: Number(cp[0]),
      latitude: Number(cp[1])
    };
  }
  return null;
}

export function resolveFootprintGeoPoint(item: Pick<FootprintData, 'cityName' | 'regionName'>): GeoPoint | null {
  const normalizedCityName = normalizeCityName(item.cityName) ?? item.cityName;
  const overridePoint = FOOTPRINT_CITY_COORDINATE_OVERRIDE_MAP[normalizedCityName];
  if (overridePoint) {
    return overridePoint;
  }
  const featureCenter = resolveCityFeatureCenter(item);
  if (featureCenter) {
    return featureCenter;
  }
  const directMatch = CITY_GEO_MAP[item.cityName];
  if (directMatch) {
    return directMatch;
  }
  const trimmedName = item.cityName?.trim();
  if (!trimmedName) {
    return null;
  }
  const fuzzyMatchKey = Object.keys(CITY_GEO_MAP).find((name) => name.replace(/市|地区|自治州|盟$/u, '') === trimmedName.replace(/市|地区|自治州|盟$/u, ''));
  return fuzzyMatchKey ? CITY_GEO_MAP[fuzzyMatchKey] : null;
}

export function buildFootprintMapItems(footprints: FootprintData[]): FootprintMapItem[] {
  const latest = [...footprints].sort((left, right) => parseSortValue(right.visitedAt) - parseSortValue(left.visitedAt))[0];
  const latestCityName = normalizeCityName(latest?.cityName) ?? latest?.cityName ?? null;
  return footprints
    .map((item): FootprintMapItem | null => {
      const geoPoint = resolveFootprintGeoPoint(item);
      if (!geoPoint) {
        return null;
      }
      const normalizedCityName = normalizeCityName(item.cityName) ?? item.cityName;
      return {
        id: item.id,
        name: normalizedCityName,
        cityName: item.cityName,
        provinceName: normalizeProvinceName(item.regionName),
        position: [geoPoint.longitude, geoPoint.latitude] as [number, number],
        importance: Number(item.importance ?? 1),
        isLatest: normalizedCityName === latestCityName,
        raw: item
      };
    })
    .filter((item): item is FootprintMapItem => !!item);
}

function normalizePolygonRing(value: unknown): [number, number][] {
  if (!Array.isArray(value)) {
    return [];
  }
  return value
    .filter((point): point is [number, number] => Array.isArray(point) && point.length >= 2)
    .map(([longitude, latitude]) => [Number(longitude), Number(latitude)] as [number, number])
    .filter(([longitude, latitude]) => Number.isFinite(longitude) && Number.isFinite(latitude));
}

export function featureToPolygonPaths(feature: GeoJsonFeature | null | undefined): [number, number][][] {
  if (!feature?.geometry?.type || !feature.geometry.coordinates) {
    return [];
  }
  if (feature.geometry.type === 'Polygon') {
    const polygon = feature.geometry.coordinates as unknown[];
    const ring = normalizePolygonRing(polygon[0]);
    return ring.length ? [ring] : [];
  }
  if (feature.geometry.type === 'MultiPolygon') {
    const polygons = feature.geometry.coordinates as unknown[];
    return polygons
      .map((polygon) => (Array.isArray(polygon) ? normalizePolygonRing(polygon[0]) : []))
      .filter((path) => path.length > 0);
  }
  return [];
}


export function getMapLabelItems(provinceNames: string[]): MapLabelItem[] {
  const seen = new Set<string>();
  const result: MapLabelItem[] = [];

  provinceNames.forEach((provinceName) => {
    const normalizedProvinceName = normalizeProvinceName(provinceName);
    if (!normalizedProvinceName) {
      return;
    }

    if (DIRECT_ADMIN_PROVINCE_SET.has(normalizedProvinceName)) {
      const provinceFeature = getProvinceFeatureByName(normalizedProvinceName);
      const cp = provinceFeature?.properties?.cp;
      if (Array.isArray(cp) && cp.length === 2) {
        const key = `${normalizedProvinceName}::${normalizedProvinceName}`;
        if (!seen.has(key)) {
          seen.add(key);
          result.push({
            name: formatMapRegionLabel(normalizedProvinceName),
            provinceName: normalizedProvinceName,
            position: [Number(cp[0]), Number(cp[1])]
          });
        }
      }
      return;
    }

    collectProvinceBoundaryFeatures(normalizedProvinceName).forEach((feature) => {
      const featureName = String(feature.properties?.name ?? '').trim();
      const normalizedName = normalizeCityName(featureName) ?? featureName;
      const cp = feature.properties?.cp;
      if (!normalizedName || !Array.isArray(cp) || cp.length !== 2) {
        return;
      }
      const labelName = formatMapRegionLabel(normalizedName);
      const key = `${normalizedProvinceName}::${featureName}`;
      if (seen.has(key)) {
        return;
      }
      seen.add(key);
      result.push({
        name: labelName,
        provinceName: normalizedProvinceName,
        position: [Number(cp[0]), Number(cp[1])]
      });
    });
  });

  return result;
}

export function getMapBoundaryItems(provinceNames: string[]): MapBoundaryItem[] {
  const seen = new Set<string>();
  const result: MapBoundaryItem[] = [];

  provinceNames.forEach((provinceName) => {
    const normalizedProvinceName = normalizeProvinceName(provinceName);
    if (!normalizedProvinceName) {
      return;
    }

    collectProvinceBoundaryFeatures(normalizedProvinceName).forEach((feature) => {
      const featureName = String(feature.properties?.name ?? '').trim();
      if (!featureName) {
        return;
      }
      const paths = featureToPolygonPaths(feature);
      if (!paths.length) {
        return;
      }
      const key = `${normalizedProvinceName}::${featureName}`;
      if (seen.has(key)) {
        return;
      }
      seen.add(key);
      result.push({
        name: featureName,
        provinceName: normalizedProvinceName,
        paths
      });
    });
  });

  return result;
}

function parseSortValue(value?: string): number {
  if (!value) {
    return 0;
  }
  const normalized = value.includes(' ') ? value.replace(' ', 'T') : value;
  const timestamp = Date.parse(normalized);
  if (!Number.isNaN(timestamp)) {
    return timestamp;
  }
  const matched = value.match(/^(\d{4})-(\d{2})-(\d{2})/);
  if (!matched) {
    return 0;
  }
  return new Date(Number(matched[1]), Number(matched[2]) - 1, Number(matched[3])).getTime();
}
