import chinaGeoJson from '@/assets/map-data/china.js';
import worldGeoJson from '@/assets/map-data/world.js';
import provinceGeoCollection from '@/assets/map-data/province/index.js';
import worldCityBoundariesGeoJson from '@/assets/map-data/world-city-boundaries.js';
import countryList from 'province-city-china/dist/country.json';
import type { FootprintData } from '@/api/modules/site';

/**
 * 地图经纬度坐标。
 */
export interface GeoPoint {
  longitude: number;
  latitude: number;
}

/**
 * 足迹地图渲染项。
 */
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

/**
 * 地图文字标签项。
 */
export interface MapLabelItem {
  name: string;
  provinceName: string | null;
  position: [number, number];
  priority: 'capital' | 'important' | 'normal';
}

/**
 * 地图边界路径项。
 */
export interface MapBoundaryItem {
  name: string;
  provinceName: string | null;
  paths: [number, number][][];
}

/**
 * 世界国家标签项。
 */
export interface WorldCountryLabelItem {
  name: string;
  position: [number, number];
}

/**
 * 世界国家边界项。
 */
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

import worldCitiesGeoJsonText from '@/assets/world-cities.geojson?raw';

function decodePolygonRing(encodedRing: EncodedRing, encodeOffset: EncodeOffset): [number, number][] {
  if (!encodedRing || !Array.isArray(encodeOffset) || encodeOffset.length < 2) {
    return [];
  }

  const result: [number, number][] = [];
  let prevX = Number(encodeOffset[0]);
  let prevY = Number(encodeOffset[1]);

  for (let index = 0; index < encodedRing.length; index += 2) {
    let x = encodedRing.charCodeAt(index) - 64;
    let y = encodedRing.charCodeAt(index + 1) - 64;

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

function decodeGeometry(
  geometry?: GeoJsonFeature['geometry']
): GeoJsonFeature['geometry'] {
  if (!geometry?.type || !geometry.coordinates || !geometry.encodeOffsets) {
    return geometry;
  }

  if (geometry.type === 'Polygon') {
    const coordinates = geometry.coordinates as EncodedPolygon;
    const encodeOffsets = geometry.encodeOffsets as EncodeOffsetPolygon;
    return {
      ...geometry,
      coordinates: coordinates.map((ring, index) =>
        decodePolygonRing(ring, encodeOffsets[index] ?? [0, 0])
      )
    };
  }

  if (geometry.type === 'MultiPolygon') {
    const coordinates = geometry.coordinates as EncodedMultiPolygon;
    const encodeOffsets = geometry.encodeOffsets as EncodeOffsetMultiPolygon;
    return {
      ...geometry,
      coordinates: coordinates.map((polygon, polygonIndex) =>
        polygon.map((ring, ringIndex) =>
          decodePolygonRing(
            ring,
            encodeOffsets[polygonIndex]?.[ringIndex] ?? [0, 0]
          )
        )
      )
    };
  }

  return geometry;
}

function normalizeGeoJsonCollection(collection: GeoJsonCollection): GeoJsonCollection {
  if (!collection?.features?.length) {
    return collection;
  }

  if (!collection.UTF8Encoding) {
    return collection;
  }

  return {
    ...collection,
    UTF8Encoding: false,
    features: collection.features.map((feature) => ({
      ...feature,
      geometry: decodeGeometry(feature.geometry)
    }))
  };
}

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
const normalizedWorldCitiesGeoJson = JSON.parse(
  unwrapModuleExport(worldCitiesGeoJsonText)
) as { features?: Array<any> };
const normalizedProvinceGeoCollection = unwrapModuleExport(
  provinceGeoCollection as Record<string, GeoJsonCollection | undefined> | { default?: Record<string, GeoJsonCollection | undefined> }
);
const normalizedWorldCityBoundariesGeoJson = unwrapModuleExport(
  worldCityBoundariesGeoJson as GeoJsonCollection | { default?: GeoJsonCollection }
);

// 缓存已解码和处理过的地理数据，提升重复查询和缩放时的性能
const normalizedProvinceCache = new Map<string, GeoJsonCollection>();
const boundaryCache = new Map<string, GeoJsonFeature[]>();
let allProvinceFeaturesCache: GeoJsonFeature[] | null = null;
let allProvinceNamesCache: string[] | null = null;
let worldCountryBoundaryItemsCache: WorldCountryBoundaryItem[] | null = null;
let worldCountryLabelItemsCache: WorldCountryLabelItem[] | null = null;
let worldCityLabelSourceCache: WorldCityLabelItem[] | null = null;
let worldCityBoundaryItemsCache: MapBoundaryItem[] | null = null;
let allCityBoundaryItemsCache: MapBoundaryItem[] | null = null;
let allCityLabelItemsCache: MapLabelItem[] | null = null;

function getAllProvinceNames(): string[] {
  if (allProvinceNamesCache) {
    return allProvinceNamesCache;
  }
  allProvinceNamesCache = getAllProvinceFeatures()
    .map((feature) => normalizeProvinceName(String(feature.properties?.name ?? '')))
    .filter((provinceName): provinceName is string => !!provinceName);
  return allProvinceNamesCache;
}

function getAllCachedCityBoundaryItems(): MapBoundaryItem[] {
  if (allCityBoundaryItemsCache) {
    return allCityBoundaryItemsCache;
  }
  allCityBoundaryItemsCache = [
    ...getMapBoundaryItems(getAllProvinceNames()),
    ...getWorldCityBoundaryItems(),
  ];
  return allCityBoundaryItemsCache;
}

function getAllCachedCityLabelItems(): MapLabelItem[] {
  if (allCityLabelItemsCache) {
    return allCityLabelItemsCache;
  }
  allCityLabelItemsCache = getMapLabelItems(getAllProvinceNames());
  return allCityLabelItemsCache;
}

/**
 * 返回所有省份范围内的城市边界缓存结果。
 *
 * @returns 返回已缓存的城市边界列表；首次调用时会基于全部省份数据构建。
 * @throws 该函数不会主动抛出业务异常；内部地理数据缺失时会返回空数组。
 * @author Dyx
 */
export function getCachedMapBoundaryItems(): MapBoundaryItem[] {
  return getAllCachedCityBoundaryItems();
}

/**
 * 返回所有省份范围内的城市标签缓存结果。
 *
 * @returns 返回已缓存的城市标签列表；首次调用时会基于全部省份数据构建。
 * @throws 该函数不会主动抛出业务异常；内部地理数据缺失时会返回空数组。
 * @author Dyx
 */
export function getCachedMapLabelItems(): MapLabelItem[] {
  return getAllCachedCityLabelItems();
}

/**
 * 将世界地图缩放等级转换为标签缓存桶编号。
 *
 * @param zoomLevel 当前地图缩放等级。
 * @returns 返回用于缓存世界城市标签结果的整数桶值。
 * @throws 该函数不会主动抛出异常。
 * @author Dyx
 */
export function getWorldCityLabelZoomBucket(zoomLevel = 5.2): number {
  return Math.round(zoomLevel * 10);
}

/**
 * 获取世界城市标签的原始数据源列表。
 *
 * @returns 返回已标准化并缓存的世界城市标签源数据。
 * @throws 该函数不会主动抛出业务异常；GeoJSON 数据缺失时会返回空数组。
 * @author Dyx
 */
export function getWorldCityLabelSourceItems(): WorldCityLabelItem[] {
  if (worldCityLabelSourceCache) {
    return worldCityLabelSourceCache;
  }
  worldCityLabelSourceCache = buildWorldCityLabelItems();
  return worldCityLabelSourceCache;
}

/**
 * 获取世界国家边界缓存数据。
 *
 * @returns 返回世界国家边界列表；首次调用时会从世界 GeoJSON 中构建。
 * @throws 该函数不会主动抛出业务异常；GeoJSON 数据缺失时会返回空数组。
 * @author Dyx
 */
export function getWorldCountryBoundaryCacheItems(): WorldCountryBoundaryItem[] {
  if (worldCountryBoundaryItemsCache) {
    return worldCountryBoundaryItemsCache;
  }
  worldCountryBoundaryItemsCache = buildWorldCountryBoundaryItems();
  return worldCountryBoundaryItemsCache;
}

/**
 * 获取世界国家名称标签缓存数据。
 *
 * @returns 返回世界国家标签列表；首次调用时会从世界 GeoJSON 中构建。
 * @throws 该函数不会主动抛出业务异常；GeoJSON 数据缺失时会返回空数组。
 * @author Dyx
 */
export function getWorldCountryLabelCacheItems(): WorldCountryLabelItem[] {
  if (worldCountryLabelItemsCache) {
    return worldCountryLabelItemsCache;
  }
  worldCountryLabelItemsCache = buildWorldCountryLabelItems();
  return worldCountryLabelItemsCache;
}

function getRequestedProvinceNames(provinceNames: string[]): string[] {
  return provinceNames.length
    ? provinceNames
    : getAllProvinceNames();
}

function buildWorldCountryBoundaryItems(): WorldCountryBoundaryItem[] {
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

function buildWorldCountryLabelItems(): WorldCountryLabelItem[] {
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

function buildAllProvinceFeatures(): GeoJsonFeature[] {
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

function resetDerivedGeoCaches(): void {
  allProvinceFeaturesCache = null;
  allProvinceNamesCache = null;
  worldCountryBoundaryItemsCache = null;
  worldCountryLabelItemsCache = null;
  worldCityLabelSourceCache = null;
  worldCityBoundaryItemsCache = null;
  allCityBoundaryItemsCache = null;
  allCityLabelItemsCache = null;
}

resetDerivedGeoCaches();

// end derived caches

/**
 * 中国地图中需要按省级行政区直接处理的直辖市与特别行政区集合。
 */
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

const CAPITAL_CITY_NAME_SET = new Set([
  '北京市',
  '天津市',
  '上海市',
  '重庆市',
  '石家庄市',
  '太原市',
  '呼和浩特市',
  '沈阳市',
  '长春市',
  '哈尔滨市',
  '南京市',
  '杭州市',
  '合肥市',
  '福州市',
  '南昌市',
  '济南市',
  '郑州市',
  '武汉市',
  '长沙市',
  '广州市',
  '南宁市',
  '海口市',
  '成都市',
  '贵阳市',
  '昆明市',
  '拉萨市',
  '西安市',
  '兰州市',
  '西宁市',
  '银川市',
  '乌鲁木齐市',
  '台北市',
  '香港特别行政区',
  '澳门特别行政区'
]);

const IMPORTANT_CITY_NAME_SET = new Set([
  '深圳市',
  '苏州市',
  '宁波市',
  '青岛市',
  '无锡市',
  '厦门市',
  '泉州市',
  '温州市',
  '佛山市',
  '东莞市',
  '珠海市',
  '绍兴市',
  '嘉兴市',
  '金华市',
  '台州市',
  '烟台市',
  '潍坊市',
  '济宁市',
  '徐州市',
  '常州市',
  '南通市',
  '扬州市',
  '盐城市',
  '镇江市',
  '泰州市',
  '连云港市',
  '淮安市',
  '宿迁市',
  '芜湖市',
  '铜陵市',
  '襄阳市',
  '宜昌市',
  '洛阳市',
  '南阳市',
  '大连市',
  '三亚市',
  '桂林市',
  '柳州市',
  '遵义市',
  '大理市',
  '丽江市',
  '延安市',
  '咸阳市'
]);

function getMapLabelPriority(name: string): 'capital' | 'important' | 'normal' {
  if (CAPITAL_CITY_NAME_SET.has(name)) {
    return 'capital';
  }
  if (IMPORTANT_CITY_NAME_SET.has(name)) {
    return 'important';
  }
  return 'normal';
}

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

/**
 * 将城市名称标准化为地图数据可识别的行政区名称。
 *
 * @param value 原始城市名。
 * @returns 返回补齐后缀或别名纠正后的城市名；为空时返回 null。
 * @throws 该函数不会主动抛出异常。
 * @author Dyx
 */
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

/**
 * 将省级行政区名称标准化为地图数据可识别的完整名称。
 *
 * @param value 原始省份名称。
 * @returns 返回补齐后缀或别名纠正后的省份名称；为空时返回 null。
 * @throws 该函数不会主动抛出异常。
 * @author Dyx
 */
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

/**
 * 生成地图展示用的区域短标签。
 *
 * @param name 原始行政区名称。
 * @returns 返回去除“市”“地区”“自治州”等后缀后的展示文案。
 * @throws 该函数不会主动抛出异常。
 * @author Dyx
 */
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

/**
 * 收集指定省份下可用于地图边界绘制的城市级 GeoJSON 特征。
 *
 * @param provinceName 省份名称。
 * @returns 返回标准化后的城市边界特征列表；无匹配省份时返回空数组。
 * @throws 该函数不会主动抛出业务异常；地图数据缺失时会返回空数组。
 * @author Dyx
 */
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

/**
 * 按城市名与省份名查找对应的地图特征。
 *
 * @param cityName 城市名称。
 * @param provinceName 所属省份名称。
 * @returns 返回匹配到的城市或直辖市省级特征；找不到时返回 null。
 * @throws 该函数不会主动抛出业务异常；地图数据缺失时会返回 null。
 * @author Dyx
 */
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

/**
 * 按省份名称查找中国省级边界特征。
 *
 * @param provinceName 省份名称。
 * @returns 返回匹配到的省级特征；找不到时返回 null。
 * @throws 该函数不会主动抛出业务异常；地图数据缺失时会返回 null。
 * @author Dyx
 */
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

/**
 * 获取中国全部省级边界特征。
 *
 * @returns 返回已标准化并缓存的全部省级 GeoJSON 特征。
 * @throws 该函数不会主动抛出业务异常；地图数据缺失时会返回空数组。
 * @author Dyx
 */
export function getAllProvinceFeatures(): GeoJsonFeature[] {
  if (allProvinceFeaturesCache) {
    return allProvinceFeaturesCache;
  }
  allProvinceFeaturesCache = buildAllProvinceFeatures();
  return allProvinceFeaturesCache;
}

/**
 * 解析足迹所在城市的地图中心点坐标。
 *
 * @param item 足迹数据中的城市与省份信息。
 * @returns 返回城市特征中心点；无法解析时返回 null。
 * @throws 该函数不会主动抛出业务异常；地图数据缺失时会返回 null。
 * @author Dyx
 */
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

/**
 * 解析足迹在地图上的最终经纬度坐标。
 *
 * @param item 足迹数据，至少包含城市名与省份名。
 * @returns 返回优先使用覆盖坐标、城市特征中心或内置城市坐标后的结果；无法解析时返回 null。
 * @throws 该函数不会主动抛出业务异常；地图数据缺失时会返回 null。
 * @author Dyx
 */
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

/**
 * 将足迹数据转换为首页地图渲染所需的数据结构。
 *
 * @param footprints 足迹列表。
 * @returns 返回过滤掉无有效坐标项后的地图渲染列表，并标记最新足迹。
 * @throws 该函数不会主动抛出业务异常；单条足迹无法解析坐标时会被忽略。
 * @author Dyx
 */
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

/**
 * 将 GeoJSON 特征转换为地图可消费的多边形路径数组。
 *
 * @param feature GeoJSON 特征对象。
 * @returns 返回 Polygon 或 MultiPolygon 的首层边界路径；不支持或无效数据时返回空数组。
 * @throws 该函数不会主动抛出业务异常；坐标结构异常时会返回空数组。
 * @author Dyx
 */
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

/**
 * 获取指定省份集合下的地图标签点位。
 *
 * @param provinceNames 省份名称数组；为空时默认返回全部省份的标签。
 * @returns 返回已去重的地图标签列表。
 * @throws 该函数不会主动抛出业务异常；地图数据缺失时会返回空数组。
 * @author Dyx
 */
export function getMapLabelItems(provinceNames: string[]): MapLabelItem[] {
  const requestedProvinceNames = getRequestedProvinceNames(provinceNames);
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
            position: [Number(cp[0]), Number(cp[1])],
            priority: 'capital'
          });
        }
      }
      return;
    }

    collectProvinceBoundaryFeatures(normalizedProvinceName).forEach((feature) => {
      const featureName = String(feature.properties?.name ?? '').trim();
      const normalizedName = normalizeCityName(featureName) ?? featureName;
      const cp = feature.properties?.cp;
      if (
        !normalizedName ||
        !normalizedName.endsWith('市') ||
        !Array.isArray(cp) ||
        cp.length !== 2
      ) {
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
        position: [Number(cp[0]), Number(cp[1])],
        priority: getMapLabelPriority(normalizedName)
      });
    });
  });

  return result;
}

/**
 * 获取指定省份集合下的城市边界路径数据。
 *
 * @param provinceNames 省份名称数组；为空时默认返回全部省份边界。
 * @returns 返回已去重的地图边界列表。
 * @throws 该函数不会主动抛出业务异常；地图数据缺失时会返回空数组。
 * @author Dyx
 */
export function getMapBoundaryItems(provinceNames: string[]): MapBoundaryItem[] {
  const requestedProvinceNames = getRequestedProvinceNames(provinceNames);
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

/**
 * 获取世界国家边界数据。
 *
 * @returns 返回世界国家边界列表。
 * @throws 该函数不会主动抛出业务异常；GeoJSON 数据缺失时会返回空数组。
 * @author Dyx
 */
export function getWorldCountryBoundaryItems(): WorldCountryBoundaryItem[] {
  return getWorldCountryBoundaryCacheItems();
}

export function getWorldCityBoundaryItems(): MapBoundaryItem[] {
  if (worldCityBoundaryItemsCache) {
    return worldCityBoundaryItemsCache;
  }

  const result =
    normalizedWorldCityBoundariesGeoJson.features
      ?.map((feature) => {
        const displayName = String(feature.properties?.DISPLAY_NAME ?? feature.properties?.name ?? '').trim();
        if (!displayName) {
          return null;
        }
        const paths = featureToPolygonPaths(feature);
        if (!paths.length) {
          return null;
        }
        const center = Array.isArray(feature.properties?.CENTER)
          ? feature.properties?.CENTER
          : getPolygonCenter(paths);
        return {
          name: displayName,
          provinceName: String(feature.properties?.COUNTRY ?? '').trim() || null,
          position: Array.isArray(center) && center.length === 2
            ? [Number(center[0]), Number(center[1])] as [number, number]
            : null,
          paths,
        };
      })
      .filter((item): item is MapBoundaryItem & { position: [number, number] | null } => !!item) ?? [];

  worldCityBoundaryItemsCache = result.map(({ position: _position, ...item }) => item);
  return worldCityBoundaryItemsCache;
}

/**
 * 世界城市标签项。
 */
export interface WorldCityLabelItem {
  name: string;
  countryName: string;
  level: number;
  position: [number, number];
  priority: 'capital' | 'important' | 'normal';
}

const WORLD_CITY_NAME_MAP = new Map<string, string>([
  ['Tokyo', '东京'],
  ['Osaka', '大阪'],
  ['Sapporo', '札幌'],
  ['Seoul', '首尔'],
  ['Busan', '釜山'],
  ['Bangkok', '曼谷'],
  ['Chiang Mai', '清迈'],
  ['Singapore', '新加坡'],
  ['Kuala Lumpur', '吉隆坡'],
  ['Penang', '槟城'],
  ['Jakarta', '雅加达'],
  ['Surabaya', '泗水'],
  ['Bali', '巴厘岛'],
  ['Manila', '马尼拉'],
  ['Cebu', '宿务'],
  ['Hanoi', '河内'],
  ['Ho Chi Minh City', '胡志明市'],
  ['Da Nang', '岘港'],
  ['New Delhi', '新德里'],
  ['Mumbai', '孟买'],
  ['Bengaluru', '班加罗尔'],
  ['Chennai', '金奈'],
  ['Kolkata', '加尔各答'],
  ['Dubai', '迪拜'],
  ['Abu Dhabi', '阿布扎比'],
  ['Riyadh', '利雅得'],
  ['Jeddah', '吉达'],
  ['Istanbul', '伊斯坦布尔'],
  ['Ankara', '安卡拉'],
  ['Moscow', '莫斯科'],
  ['Saint Petersburg', '圣彼得堡'],
  ['Novosibirsk', '新西伯利亚'],
  ['London', '伦敦'],
  ['Manchester', '曼彻斯特'],
  ['Birmingham', '伯明翰'],
  ['Paris', '巴黎'],
  ['Marseille', '马赛'],
  ['Lyon', '里昂'],
  ['Berlin', '柏林'],
  ['Hamburg', '汉堡'],
  ['Munich', '慕尼黑'],
  ['Madrid', '马德里'],
  ['Barcelona', '巴塞罗那'],
  ['Valencia', '瓦伦西亚'],
  ['Rome', '罗马'],
  ['Milan', '米兰'],
  ['Naples', '那不勒斯'],
  ['Amsterdam', '阿姆斯特丹'],
  ['Brussels', '布鲁塞尔'],
  ['Zurich', '苏黎世'],
  ['Vienna', '维也纳'],
  ['Prague', '布拉格'],
  ['Warsaw', '华沙'],
  ['Stockholm', '斯德哥尔摩'],
  ['Oslo', '奥斯陆'],
  ['Copenhagen', '哥本哈根'],
  ['Helsinki', '赫尔辛基'],
  ['Lisbon', '里斯本'],
  ['Dublin', '都柏林'],
  ['Athens', '雅典'],
  ['Budapest', '布达佩斯'],
  ['New York', '纽约'],
  ['Washington', '华盛顿'],
  ['Los Angeles', '洛杉矶'],
  ['Chicago', '芝加哥'],
  ['San Francisco', '旧金山'],
  ['Seattle', '西雅图'],
  ['Toronto', '多伦多'],
  ['Vancouver', '温哥华'],
  ['Montreal', '蒙特利尔'],
  ['Mexico City', '墨西哥城'],
  ['Guadalajara', '瓜达拉哈拉'],
  ['Monterrey', '蒙特雷'],
  ['Sao Paulo', '圣保罗'],
  ['Rio de Janeiro', '里约热内卢'],
  ['Brasilia', '巴西利亚'],
  ['Buenos Aires', '布宜诺斯艾利斯'],
  ['Santiago', '圣地亚哥'],
  ['Lima', '利马'],
  ['Bogota', '波哥大'],
  ['Johannesburg', '约翰内斯堡'],
  ['Cape Town', '开普敦'],
  ['Nairobi', '内罗毕'],
  ['Cairo', '开罗'],
  ['Lagos', '拉各斯'],
  ['Casablanca', '卡萨布兰卡'],
  ['Sydney', '悉尼'],
  ['Melbourne', '墨尔本'],
  ['Brisbane', '布里斯班'],
  ['Auckland', '奥克兰']
]);

const worldCityLabelCache = new Map<number, WorldCityLabelItem[]>();

function normalizeWorldCityName(name: string): string {
  return WORLD_CITY_NAME_MAP.get(name) ?? name;
}

function getWorldCityLabelPriority(level: number): 'capital' | 'important' | 'normal' {
  if (level <= 1) {
    return 'capital';
  }
  if (level <= 2) {
    return 'important';
  }
  return 'normal';
}

function getWorldCityCollisionDistance(level: number): number {
  if (level >= 7.2) return 0;
  if (level >= 6.5) return 4.2;
  if (level >= 5.8) return 7.5;
  if (level >= 5.1) return 10.5;
  return 14;
}

function buildWorldCityLabelItems(): WorldCityLabelItem[] {
  const result: WorldCityLabelItem[] = [];

  if (!normalizedWorldCitiesGeoJson || !Array.isArray(normalizedWorldCitiesGeoJson.features)) {
    return result;
  }

  normalizedWorldCitiesGeoJson.features.forEach((feature: any) => {
    const name = String(feature.properties?.NAME ?? '').trim();
    const countryName = String(feature.properties?.COUNTRY ?? '').trim();
    const level = Number(feature.properties?.LEVEL ?? 3);
    const coordinates = feature.geometry?.coordinates;

    if (!name || !countryName || !Array.isArray(coordinates) || coordinates.length !== 2) {
      return;
    }

    const longitude = Number(coordinates[0]);
    const latitude = Number(coordinates[1]);
    if (!Number.isFinite(longitude) || !Number.isFinite(latitude)) {
      return;
    }

    result.push({
      name: normalizeWorldCityName(name),
      countryName,
      level: Number.isFinite(level) ? level : 3,
      position: [longitude, latitude],
      priority: getWorldCityLabelPriority(Number.isFinite(level) ? level : 3)
    });
  });

  return result.sort((left, right) => {
    const levelDiff = left.level - right.level;
    if (levelDiff !== 0) {
      return levelDiff;
    }
    return left.name.localeCompare(right.name);
  });
}

/**
 * 按当前缩放级别返回可见的世界城市标签列表。
 *
 * @param zoomLevel 当前世界地图缩放等级。
 * @returns 返回按碰撞距离过滤后的城市标签列表，并按缩放桶缓存结果。
 * @throws 该函数不会主动抛出业务异常；城市 GeoJSON 数据缺失时会返回空数组。
 * @author Dyx
 */
export function getWorldCityLabelItems(zoomLevel = 5.2): WorldCityLabelItem[] {
  const cacheKey = getWorldCityLabelZoomBucket(zoomLevel);
  const cached = worldCityLabelCache.get(cacheKey);
  if (cached) {
    return cached;
  }

  const collisionDistance = getWorldCityCollisionDistance(zoomLevel);
  const visible: WorldCityLabelItem[] = [];

  getWorldCityLabelSourceItems().forEach((item) => {
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

  worldCityLabelCache.set(cacheKey, visible);
  return visible;
}

/**
 * 获取世界国家名称标签数据。
 *
 * @returns 返回世界国家标签列表。
 * @throws 该函数不会主动抛出业务异常；GeoJSON 数据缺失时会返回空数组。
 * @author Dyx
 */
export function getWorldCountryLabelItems(): WorldCountryLabelItem[] {
  return getWorldCountryLabelCacheItems();
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
