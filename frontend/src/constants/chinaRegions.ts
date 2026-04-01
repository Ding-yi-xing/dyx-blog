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

/**
 * 中国省市区级联选择项。
 */
export interface ChinaRegionOption {
  value: string;
  label: string;
  code?: string;
  children?: ChinaRegionOption[];
}

/**
 * 需要按“省 -> 区县”方式直接处理的直辖市集合。
 */
const directAdminProvinceNameSet = new Set(['北京市', '天津市', '上海市', '重庆市']);

/**
 * 按中文名称对行政区数据进行排序。
 *
 * @param items 待排序的行政区数组。
 * @returns 返回新的升序数组，不修改原数组。
 * @throws 该函数不会主动抛出异常。
 * @author Dyx
 */
function sortByChineseName<T extends { name: string }>(items: T[]): T[] {
  return [...items].sort((left, right) => left.name.localeCompare(right.name, 'zh-CN'));
}

/**
 * 将区县原始数据转换为级联选择器可用的选项结构。
 *
 * @param items 区县原始数据列表。
 * @returns 返回按中文名称排序后的区县选项数组。
 * @throws 该函数不会主动抛出业务异常；输入为空时返回空数组。
 * @author Dyx
 */
function createCountyOptions(items: ChinaAreaRecord[]): ChinaRegionOption[] {
  return sortByChineseName(items).map((item) => ({
    value: item.name,
    label: item.name,
    code: item.code
  }));
}

/**
 * 根据省份和城市原始数据构造城市级联选项。
 *
 * @param province 当前省份记录。
 * @param cities 当前省份下的城市记录列表。
 * @param areaMap 区县映射表，键格式为 `省代码:市代码`。
 * @returns 返回当前省份下的城市或直辖市区县选项数组。
 * @throws 该函数不会主动抛出业务异常；缺失下级数据时会返回无 children 的选项。
 * @author Dyx
 */
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

/**
 * 构建完整的中国省市区级联选项树。
 *
 * @returns 返回按省份、城市、区县组织好的级联数据。
 * @throws 该函数不会主动抛出业务异常；静态数据为空时会返回空数组。
 * @author Dyx
 */
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

/**
 * 在指定省份选项中查找某个区县的完整级联路径。
 *
 * @param provinceOption 省份选项。
 * @param districtName 区县名称。
 * @returns 返回 `[省, 市, 区县]` 路径；找不到时返回空数组。
 * @throws 该函数不会主动抛出异常。
 * @author Dyx
 */
function findDistrictPath(provinceOption: ChinaRegionOption, districtName: string): string[] {
  for (const cityOption of provinceOption.children ?? []) {
    const districtOption = cityOption.children?.find((option) => option.value === districtName);
    if (districtOption) {
      return [provinceOption.value, cityOption.value, districtOption.value];
    }
  }
  return [];
}

/**
 * 根据省份名和城市/区县名推导级联选择组件的选中路径。
 *
 * @param regionName 省份名称。
 * @param cityName 城市或区县名称。
 * @returns 返回 `[省, 市]` 或 `[省, 市, 区县]` 形式的路径；无法匹配时返回空数组。
 * @throws 该函数不会主动抛出业务异常；未命中静态数据时会返回空数组。
 * @author Dyx
 */
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

/**
 * 根据省份名和城市/区县名解析出最终应展示的区县名称。
 *
 * @param regionName 省份名称。
 * @param cityName 城市或区县名称。
 * @returns 返回匹配到的第三级区县名称；不存在时返回 `-`。
 * @throws 该函数不会主动抛出业务异常；未命中静态数据时会返回占位符。
 * @author Dyx
 */
export function resolveChinaRegionDistrictName(regionName?: string, cityName?: string): string {
  const selection = findChinaRegionSelection(regionName, cityName);
  return selection[2] ?? '-';
}

/**
 * 中国省市区级联选择器使用的静态选项树。
 */
export const chinaRegionOptions: ChinaRegionOption[] = buildChinaRegionOptions();
