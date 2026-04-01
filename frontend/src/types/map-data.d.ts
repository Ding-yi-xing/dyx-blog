/**
 * 地图数据静态资源模块声明。
 * 用于为 Vite 中直接导入的地理数据文件提供基础类型。
 */
declare module '@/assets/map-data/china.js' {
  const value: unknown;
  export default value;
}

declare module '@/assets/map-data/world.js' {
  const value: unknown;
  export default value;
}

declare module '@/assets/map-data/province/index.js' {
  const value: Record<string, unknown>;
  export default value;
}

declare module '@/assets/map-data/world-city-boundaries.js' {
  const value: unknown;
  export default value;
}
