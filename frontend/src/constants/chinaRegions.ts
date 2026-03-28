import provinceData from 'province-city-china/dist/province.json';
import cityData from 'province-city-china/dist/city.json';
import areaData from 'province-city-china/dist/area.json';

interface ChinaProvinceRecord {
  code: string;
  name: string;
  province: string;
}

interface ChinaCityRecord {
  code: string;
  name: string;
  province: string;
  city: string;
}

interface ChinaAreaRecord {
  code: string;
  name: string;
  province: string;
  city: string;
  area: string;
}

export interface ChinaRegionOption {
  value: string;
  label: string;
  code?: string;
  children?: ChinaRegionOption[];
}

const directAdminProvinceNameSet = new Set(['北京市', '天津市', '上海市', '重庆市']);

function sortByChineseName<T extends { name: string }>(items: T[]): T[] {
  return [...items].sort((left, right) => left.name.localeCompare(right.name, 'zh-CN'));
}

function createCountyOptions(items: ChinaAreaRecord[]): ChinaRegionOption[] {
  return sortByChineseName(items).map((item) => ({
    value: item.name,
    label: item.name,
    code: item.code
  }));
}

function createCityOptions(province: ChinaProvinceRecord, cities: ChinaCityRecord[], areaMap: Map<string, ChinaAreaRecord[]>): ChinaRegionOption[] {
  if (directAdminProvinceNameSet.has(province.name)) {
    const districts = createCountyOptions(areaMap.get(`${province.province}:01`) ?? []);
    return [
      {
        value: province.name,
        label: province.name,
        code: `${province.province}0100`,
        children: districts.length ? districts : undefined
      }
    ];
  }

  return sortByChineseName(cities).map((city) => {
    const districts = createCountyOptions(areaMap.get(`${city.province}:${city.city}`) ?? []);
    return {
      value: city.name,
      label: city.name,
      code: city.code,
      children: districts.length ? districts : undefined
    };
  });
}

function buildChinaRegionOptions(): ChinaRegionOption[] {
  const provinces = provinceData as ChinaProvinceRecord[];
  const cities = cityData as ChinaCityRecord[];
  const areas = areaData as ChinaAreaRecord[];

  const cityMap = new Map<string, ChinaCityRecord[]>();
  cities.forEach((city) => {
    const list = cityMap.get(city.province) ?? [];
    list.push(city);
    cityMap.set(city.province, list);
  });

  const areaMap = new Map<string, ChinaAreaRecord[]>();
  areas.forEach((area) => {
    const key = `${area.province}:${area.city}`;
    const list = areaMap.get(key) ?? [];
    list.push(area);
    areaMap.set(key, list);
  });

  return sortByChineseName(provinces).map((province) => ({
    value: province.name,
    label: province.name,
    code: province.code,
    children: createCityOptions(province, cityMap.get(province.province) ?? [], areaMap)
  }));
}

function findDistrictPath(provinceOption: ChinaRegionOption, districtName: string): string[] {
  for (const cityOption of provinceOption.children ?? []) {
    const districtOption = cityOption.children?.find((option) => option.value === districtName);
    if (districtOption) {
      return [provinceOption.value, cityOption.value, districtOption.value];
    }
  }
  return [];
}

export function findChinaRegionSelection(regionName?: string, cityName?: string): string[] {
  const provinceName = regionName?.trim();
  const normalizedCityName = cityName?.trim();
  if (!provinceName || !normalizedCityName) {
    return [];
  }

  const provinceOption = chinaRegionOptions.find((option) => option.value === provinceName);
  if (!provinceOption) {
    return [];
  }

  const cityOption = provinceOption.children?.find((option) => option.value === normalizedCityName);
  if (cityOption) {
    return [provinceName, cityOption.value];
  }

  return findDistrictPath(provinceOption, normalizedCityName);
}

export function resolveChinaRegionDistrictName(regionName?: string, cityName?: string): string {
  const selection = findChinaRegionSelection(regionName, cityName);
  return selection[2] ?? '-';
}

export const chinaRegionOptions: ChinaRegionOption[] = buildChinaRegionOptions();
