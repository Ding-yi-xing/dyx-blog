import chinaGeoJson from 'china-map-data/china.js';
import worldGeoJson from 'china-map-data/world.js';
import provinceGeoCollection from 'china-map-data/province/index.js';
import countryList from 'province-city-china/dist/country.json';
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

export interface WorldCountryLabelItem {
  name: string;
  position: [number, number];
}

export interface WorldCountryBoundaryItem {
  name: string;
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

import worldCitiesGeoJson from '@/assets/world-cities.geojson';

function unwrapModuleExport<T>(value: T | { default?: T }): T {
  if (value && typeof value === 'object' && 'default' in value) {
    return (value as { default?: T }).default ?? (value as T);
  }
  return value as T;
}

const normalizedChinaGeoJson = normalizeGeoJsonCollection(
  unwrapModuleExport(chinaGeoJson as GeoJsonCollection | { default?: GeoJsonCollection })
);
const normalizedWorldGeoJson = normalizeGeoJsonCollection(
  unwrapModuleExport(worldGeoJson as GeoJsonCollection | { default?: GeoJsonCollection })
);
const normalizedWorldCitiesGeoJson = unwrapModuleExport(worldCitiesGeoJson);
const normalizedProvinceGeoCollection = unwrapModuleExport(
  provinceGeoCollection as Record<string, GeoJsonCollection | undefined> | { default?: Record<string, GeoJsonCollection | undefined> }
);

// 缓存已解码和处理过的地理数据，提升重复查询和缩放时的性能
const normalizedProvinceCache = new Map<string, GeoJsonCollection>();
const boundaryCache = new Map<string, GeoJsonFeature[]>();

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

const WORLD_COUNTRY_NAME_MAP = new Map<string, string>();

function normalizeCountryLookupKey(value?: string): string {
  return String(value ?? '')
    .trim()
    .toLowerCase()
    .replace(/&/g, 'and')
    .replace(/[.'’()\-]/g, ' ')
    .replace(/,/g, ' ')
    .replace(/\bthe\b/g, ' ')
    .replace(/\bof\b/g, ' ')
    .replace(/\brepublic\b/g, ' ')
    .replace(/\bdemocratic\b/g, ' ')
    .replace(/\bfederal\b/g, ' ')
    .replace(/\bislamic\b/g, ' ')
    .replace(/\bkingdom\b/g, ' ')
    .replace(/\bstate\b/g, ' ')
    .replace(/\bstates\b/g, ' ')
    .replace(/\bunion\b/g, ' ')
    .replace(/\s+/g, ' ')
    .trim();
}

function registerWorldCountryName(sourceName: string | undefined, targetName: string | undefined): void {
  const key = normalizeCountryLookupKey(sourceName);
  const value = String(targetName ?? '').trim();
  if (!key || !value) {
    return;
  }
  WORLD_COUNTRY_NAME_MAP.set(key, value);
}

countryList.forEach((country) => {
  registerWorldCountryName(country.name, country.cnname);
  registerWorldCountryName(country.fullname, country.cnname || country.name);
  registerWorldCountryName(country.cnname, country.cnname);
  registerWorldCountryName(country.alpha2, country.cnname || country.name);
  registerWorldCountryName(country.alpha3, country.cnname || country.name);
});

[
  ['United States', '美国'],
  ['United States of America', '美国'],
  ['Russia', '俄罗斯'],
  ['Russian Federation', '俄罗斯'],
  ['South Korea', '韩国'],
  ['Korea', '韩国'],
  ['North Korea', '朝鲜'],
  ['Dem. Rep. Korea', '朝鲜'],
  ['Democratic Republic of the Congo', '刚果（金）'],
  ['Dem. Rep. Congo', '刚果（金）'],
  ['Republic of the Congo', '刚果（布）'],
  ['Congo', '刚果（布）'],
  ['United Kingdom', '英国'],
  ['Britain', '英国'],
  ['Czech Republic', '捷克'],
  ['Czechia', '捷克'],
  ['Dominican Rep.', '多米尼加'],
  ['Central African Rep.', '中非'],
  ['Bosnia and Herz.', '波黑'],
  ['Eq. Guinea', '赤道几内亚'],
  ['eSwatini', '斯威士兰'],
  ['Swaziland', '斯威士兰'],
  ['Timor-Leste', '东帝汶'],
  ['Solomon Is.', '所罗门群岛'],
  ['N. Cyprus', '北塞浦路斯'],
  ['Macedonia', '北马其顿'],
  ['North Macedonia', '北马其顿'],
  ['S. Sudan', '南苏丹'],
  ['Laos', '老挝'],
  ['Syria', '叙利亚'],
  ['Vatican', '梵蒂冈'],
  ['Brunei', '文莱'],
  ['Moldova', '摩尔多瓦'],
  ['Iran', '伊朗'],
  ['Tanzania', '坦桑尼亚'],
  ['Palestine', '巴勒斯坦']
].forEach(([sourceName, targetName]) => {
  registerWorldCountryName(sourceName, targetName);
});

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
  if (normalizedProvinceCache.has(moduleName)) {
    return normalizedProvinceCache.get(moduleName)!;
  }
  const provinceModule = normalizedProvinceGeoCollection[moduleName];
  if (!provinceModule) {
    return null;
  }
  const collection = normalizeGeoJsonCollection(
    unwrapModuleExport(
      provinceModule as GeoJsonCollection | { default?: GeoJsonCollection }
    )
  );
  normalizedProvinceCache.set(moduleName, collection);
  return collection;
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
  const normalizedProvinceName = normalizeProvinceName(provinceName);
  if (!normalizedProvinceName) return [];
  if (boundaryCache.has(normalizedProvinceName)) {
    return boundaryCache.get(normalizedProvinceName)!;
  }
  const provinceCollection = getProvinceGeoCollection(provinceName);
  if (!provinceCollection?.features?.length) {
    return [];
  }
  const features = provinceCollection.features
    .filter((feature) => feature?.geometry && feature.properties?.name)
    .filter((feature) => {
      if (DIRECT_ADMIN_PROVINCE_SET.has(normalizedProvinceName)) {
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
  boundaryCache.set(normalizedProvinceName, features);
  return features;
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

function resolveStoredFootprintGeoPoint(item: Pick<FootprintData, 'positionX' | 'positionY'>): GeoPoint | null {
  const longitude = Number(item.positionX);
  const latitude = Number(item.positionY);
  if (!Number.isFinite(longitude) || !Number.isFinite(latitude)) {
    return null;
  }
  if (longitude < -180 || longitude > 180 || latitude < -90 || latitude > 90) {
    return null;
  }
  return {
    longitude,
    latitude
  };
}

function shouldPreferStoredFootprintGeoPoint(item: Pick<FootprintData, 'countryName' | 'regionName'>): boolean {
  const countryName = String(item.countryName ?? '').trim();
  if (countryName && countryName !== '中国') {
    return true;
  }
  return !normalizeProvinceName(item.regionName);
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
      const geoPoint = shouldPreferStoredFootprintGeoPoint(item)
        ? resolveStoredFootprintGeoPoint(item) ?? resolveFootprintGeoPoint(item)
        : resolveFootprintGeoPoint(item) ?? resolveStoredFootprintGeoPoint(item);
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

function getPolygonCenter(paths: [number, number][][]): [number, number] | null {
  let totalLongitude = 0;
  let totalLatitude = 0;
  let totalPoints = 0;

  paths.forEach((path) => {
    path.forEach(([longitude, latitude]) => {
      totalLongitude += longitude;
      totalLatitude += latitude;
      totalPoints += 1;
    });
  });

  if (!totalPoints) {
    return null;
  }

  return [totalLongitude / totalPoints, totalLatitude / totalPoints];
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
  const requestedProvinceNames = provinceNames.length
    ? provinceNames
    : getAllProvinceFeatures().map(
        (feature) => String(feature.properties?.name ?? '').trim()
      );
  const seen = new Set<string>();
  const result: MapLabelItem[] = [];

  requestedProvinceNames.forEach((provinceName) => {
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
  const requestedProvinceNames = provinceNames.length
    ? provinceNames
    : getAllProvinceFeatures().map(
        (feature) => String(feature.properties?.name ?? '').trim()
      );
  const seen = new Set<string>();
  const result: MapBoundaryItem[] = [];

  requestedProvinceNames.forEach((provinceName) => {
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

export function getWorldCountryBoundaryItems(): WorldCountryBoundaryItem[] {
  const seen = new Set<string>();
  const result: WorldCountryBoundaryItem[] = [];

  normalizedWorldGeoJson.features?.forEach((feature) => {
    const featureName = String(feature.properties?.name ?? '').trim();
    const paths = featureToPolygonPaths(feature);
    if (!featureName || !paths.length) {
      return;
    }
    const key = normalizeCountryLookupKey(featureName) || featureName;
    if (seen.has(key)) {
      return;
    }
    seen.add(key);
    result.push({
      name: WORLD_COUNTRY_NAME_MAP.get(key) ?? featureName,
      paths,
    });
  });

  return result;
}

export interface WorldCityLabelItem {
  name: string;
  position: [number, number];
}

export function getWorldCityLabelItems(): WorldCityLabelItem[] {
  const result: WorldCityLabelItem[] = [];
  
  if (!normalizedWorldCitiesGeoJson || !Array.isArray(normalizedWorldCitiesGeoJson.features)) {
    return result;
  }

  normalizedWorldCitiesGeoJson.features.forEach((feature: any) => {
    const name = feature.properties?.NAME;
    const coordinates = feature.geometry?.coordinates;
    
    if (name && Array.isArray(coordinates) && coordinates.length === 2) {
      result.push({
        name,
        position: [Number(coordinates[0]), Number(coordinates[1])]
      });
    }
  });

  return result;
}

export function getWorldCountryLabelItems(): WorldCountryLabelItem[] {
  const seen = new Set<string>();
  const result: WorldCountryLabelItem[] = [];

  normalizedWorldGeoJson.features?.forEach((feature) => {
    const featureName = String(feature.properties?.name ?? '').trim();
    if (!featureName) {
      return;
    }

    const cp = feature.properties?.cp;
    const position = Array.isArray(cp) && cp.length === 2
      ? [Number(cp[0]), Number(cp[1])] as [number, number]
      : getPolygonCenter(featureToPolygonPaths(feature));

    if (!position) {
      return;
    }

    const [longitude, latitude] = position;
    if (!Number.isFinite(longitude) || !Number.isFinite(latitude)) {
      return;
    }

    const key = normalizeCountryLookupKey(featureName);
    const labelName = WORLD_COUNTRY_NAME_MAP.get(key) ?? featureName;
    if (!labelName || labelName === '中国' || labelName === 'China') {
      return;
    }

    if (Math.abs(longitude) > 180 || Math.abs(latitude) > 90) {
      return;
    }

    const dedupeKey = normalizeCountryLookupKey(labelName) || `${labelName}::${longitude.toFixed(3)}::${latitude.toFixed(3)}`;
    if (seen.has(dedupeKey)) {
      return;
    }
    seen.add(dedupeKey);
    result.push({
      name: labelName,
      position: [longitude, latitude]
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
