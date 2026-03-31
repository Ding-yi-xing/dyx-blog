import { adminHttp, publicHttp } from '@/api/http';
import type {
  FootprintData,
  GuestbookData,
  GuestbookMessageData,
  HonorData,
  MomentData,
  PostData,
  ProfileData,
  ProjectData,
  WorkData
} from '@/api/modules/site';

/**
 * 后台登录用户信息。
 */
export interface AdminUserData {
  id: number;
  username: string;
  displayName: string;
  role: string;
}

/**
 * 后台登录响应数据。
 */
export interface AdminLoginData {
  token: string;
  user: AdminUserData;
}

export interface VisitTrendPoint {
  date: string;
  label: string;
  visitCount: number;
}

export interface DeviceTypeStat {
  deviceType: string;
  label: string;
  visitCount: number;
}

export interface PageVisitStat {
  pageKey: string;
  label: string;
  visitCount: number;
  lastVisitAt?: string;
}

export interface RecentVisitRecord {
  id: number;
  pageKey: string;
  pageLabel: string;
  ipAddress: string;
  userAgent: string;
  deviceType: string;
  deviceTypeLabel: string;
  deviceName: string;
  createdAt?: string;
}

export type AdminGuestbookData = GuestbookData;

/**
 * 后台仪表盘摘要数据。
 */
export interface DashboardSummaryData {
  postCount?: number;
  momentCount?: number;
  honorCount?: number;
  userCount?: number;
  totalPostViews?: number;
  totalSiteVisits?: number;
  dailySiteVisits?: VisitTrendPoint[];
  deviceTypeDistribution?: DeviceTypeStat[];
  topVisitedPages?: PageVisitStat[];
}

export interface AdminVisitLogQuery {
  startTime?: string;
  endTime?: string;
  pageKey?: string;
  deviceType?: string;
  ipAddress?: string;
  page?: number;
  pageSize?: number;
}

export interface SystemConfigData {
  id?: number;
  storageType?: 'local' | 'oss' | string;
  ossEndpoint?: string;
  ossRegion?: string;
  ossBucketName?: string;
  ossPublicUrlPrefix?: string;
  ossBaseDir?: string;
  footprintEyebrow?: string;
  footprintTitle?: string;
  footprintSubtitle?: string;
  footprintDescription?: string;
  copyrightText?: string;
  techSupportText?: string;
  updatedAt?: string;
  ossEndpointConfigured?: boolean;
  ossRegionConfigured?: boolean;
  ossBucketNameConfigured?: boolean;
  ossPublicUrlPrefixConfigured?: boolean;
}

/**
 * 获取后台访问日志分页列表。
 *
 * @param params 访问日志筛选与分页参数。
 * @returns 返回包含日志列表与分页信息的 Promise 结果。
 * @throws 该函数不会主动抛出同步异常；接口失败时会以 Promise reject 形式返回。
 * @author Dyx
 */
export function getAdminVisitLogs(params?: AdminVisitLogQuery) {
  return adminHttp.get('/dyx-manager/visit-logs', { params });
}

/**
 * 删除单条后台访问日志。
 *
 * @param id 访问日志主键。
 * @returns 返回删除接口的 Promise 结果。
 * @throws 该函数不会主动抛出同步异常；删除失败或权限不足时会以 Promise reject 形式返回。
 * @author Dyx
 */
export function deleteAdminVisitLog(id: number) {
  return adminHttp.delete(`/dyx-manager/visit-logs/${id}`);
}

/**
 * 批量删除后台访问日志。
 *
 * @param ids 访问日志主键列表。
 * @returns 返回批量删除接口的 Promise 结果。
 * @throws 该函数不会主动抛出同步异常；删除失败或权限不足时会以 Promise reject 形式返回。
 * @author Dyx
 */
export function deleteAdminVisitLogs(ids: number[]) {
  return adminHttp.post('/dyx-manager/visit-logs/batch-delete', ids);
}

/**
 * 获取后台留言管理页数据。
 *
 * @returns 返回留言介绍文案与留言列表等后台管理数据。
 * @throws 该函数不会主动抛出同步异常；接口失败时会以 Promise reject 形式返回。
 * @author Dyx
 */
export function getAdminGuestbook() {
  return adminHttp.get('/dyx-manager/guestbook');
}

/**
 * 更新留言页介绍文案。
 *
 * @param guestbookIntro 最新留言页介绍文案。
 * @returns 返回更新后的留言页介绍相关数据。
 * @throws 该函数不会主动抛出同步异常；保存失败时会以 Promise reject 形式返回。
 * @author Dyx
 */
export function updateAdminGuestbookIntro(guestbookIntro: string) {
  return adminHttp.put('/dyx-manager/guestbook/intro', { guestbookIntro });
}

/**
 * 更新单条后台留言记录。
 *
 * @param id 留言主键。
 * @param payload 留言更新数据。
 * @returns 返回更新后的留言数据。
 * @throws 该函数不会主动抛出同步异常；保存失败时会以 Promise reject 形式返回。
 * @author Dyx
 */
export function updateAdminGuestbookMessage(id: number, payload: Partial<GuestbookMessageData>) {
  return adminHttp.put(`/dyx-manager/guestbook/messages/${id}`, payload);
}

/**
 * 删除单条后台留言记录。
 *
 * @param id 留言主键。
 * @returns 返回删除接口的 Promise 结果。
 * @throws 该函数不会主动抛出同步异常；删除失败时会以 Promise reject 形式返回。
 * @author Dyx
 */
export function deleteAdminGuestbookMessage(id: number) {
  return adminHttp.delete(`/dyx-manager/guestbook/messages/${id}`);
}

/**
 * 媒体资源数据。
 */
export interface MediaData {
  id: string;
  originalName?: string;
  fileName?: string;
  fileUrl?: string;
  mediaType?: string;
  fileSize?: number;
  createdAt?: string;
}

export function getAdminMediaContentUrl(fileUrl: string) {
  return `/api/dyx-manager/media/content?fileUrl=${encodeURIComponent(fileUrl)}`;
}

/**
 * 后台用户数据。
 */
export interface AdminListUserData {
  id?: number;
  username: string;
  displayName?: string;
  password?: string;
  role?: string;
  enabled?: number;
  createdAt?: string;
  updatedAt?: string;
}

/**
 * 调用后台仪表盘摘要接口。
 */
export function getDashboardSummary() {
  return adminHttp.get('/dyx-manager/dashboard/summary');
}

/**
 * 调用后台文章列表接口。
 */
export function getAdminPosts() {
  return adminHttp.get('/dyx-manager/posts');
}

/**
 * 调用后台文章保存接口。
 * @param payload 文章表单数据。
 */
export function saveAdminPost(payload: Partial<PostData>) {
  if (payload.id) {
    return adminHttp.put(`/dyx-manager/posts/${payload.id}`, payload);
  }
  return adminHttp.post('/dyx-manager/posts', payload);
}

/**
 * 调用后台文章删除接口。
 * @param id 文章主键。
 */
export function deleteAdminPost(id: number) {
  return adminHttp.delete(`/dyx-manager/posts/${id}`);
}

/**
 * 调用后台动态列表接口。
 */
export function getAdminMoments() {
  return adminHttp.get('/dyx-manager/moments');
}

/**
 * 调用后台动态保存接口。
 * @param payload 动态表单数据。
 */
export function saveAdminMoment(payload: Partial<MomentData>) {
  if (payload.id) {
    return adminHttp.put(`/dyx-manager/moments/${payload.id}`, payload);
  }
  return adminHttp.post('/dyx-manager/moments', payload);
}

/**
 * 调用后台动态删除接口。
 * @param id 动态主键。
 */
export function deleteAdminMoment(id: number) {
  return adminHttp.delete(`/dyx-manager/moments/${id}`);
}

/**
 * 调用后台项目列表接口。
 */
export function getAdminProjects() {
  return adminHttp.get('/dyx-manager/projects');
}

/**
 * 调用后台项目保存接口。
 * @param payload 项目表单数据。
 */
export function saveAdminProject(payload: Partial<ProjectData>) {
  if (payload.id) {
    return adminHttp.put(`/dyx-manager/projects/${payload.id}`, payload);
  }
  return adminHttp.post('/dyx-manager/projects', payload);
}

/**
 * 调用后台项目删除接口。
 * @param id 项目主键。
 */
export function deleteAdminProject(id: number) {
  return adminHttp.delete(`/dyx-manager/projects/${id}`);
}

/**
 * 调用后台作品列表接口。
 */
export function getAdminWorks() {
  return adminHttp.get('/dyx-manager/works');
}

/**
 * 调用后台作品保存接口。
 * @param payload 作品表单数据。
 */
export function saveAdminWork(payload: Partial<WorkData>) {
  if (payload.id) {
    return adminHttp.put(`/dyx-manager/works/${payload.id}`, payload);
  }
  return adminHttp.post('/dyx-manager/works', payload);
}

/**
 * 调用后台作品删除接口。
 * @param id 作品主键。
 */
export function deleteAdminWork(id: number) {
  return adminHttp.delete(`/dyx-manager/works/${id}`);
}

/**
 * 调用后台荣誉列表接口。
 */
export function getAdminHonors() {
  return adminHttp.get('/dyx-manager/honors');
}

/**
 * 调用后台荣誉保存接口。
 * @param payload 荣誉表单数据。
 */
export function saveAdminHonor(payload: Partial<HonorData>) {
  if (payload.id) {
    return adminHttp.put(`/dyx-manager/honors/${payload.id}`, payload);
  }
  return adminHttp.post('/dyx-manager/honors', payload);
}

/**
 * 调用后台荣誉删除接口。
 * @param id 荣誉主键。
 */
export function deleteAdminHonor(id: number) {
  return adminHttp.delete(`/dyx-manager/honors/${id}`);
}

/**
 * 调用后台足迹列表接口。
 */
export function getAdminFootprints() {
  return adminHttp.get('/dyx-manager/footprints');
}

/**
 * 调用后台足迹保存接口。
 * @param payload 足迹表单数据。
 */
export function saveAdminFootprint(payload: Partial<FootprintData>) {
  if (payload.id) {
    return adminHttp.put(`/dyx-manager/footprints/${payload.id}`, payload);
  }
  return adminHttp.post('/dyx-manager/footprints', payload);
}

/**
 * 调用后台足迹删除接口。
 * @param id 足迹主键。
 */
export function deleteAdminFootprint(id: number) {
  return adminHttp.delete(`/dyx-manager/footprints/${id}`);
}

/**
 * 调用后台首页横幅查询接口。
 */
export function getAdminHeroProfile() {
  return adminHttp.get('/dyx-manager/profile/hero');
}

/**
 * 调用后台首页横幅更新接口。
 * @param payload 首页横幅表单数据。
 */
export function updateAdminHeroProfile(payload: ProfileData) {
  return adminHttp.put('/dyx-manager/profile/hero', payload);
}

/**
 * 调用后台个人资料查询接口。
 */
export function getAdminProfile() {
  return adminHttp.get('/dyx-manager/profile');
}

/**
 * 调用后台个人资料更新接口。
 * @param payload 个人资料表单数据。
 */
export function updateAdminProfile(payload: ProfileData) {
  return adminHttp.put('/dyx-manager/profile', payload);
}

/**
 * 调用后台系统配置查询接口。
 */
export function getAdminSystemConfig() {
  return adminHttp.get('/dyx-manager/system-config');
}

/**
 * 调用后台系统配置更新接口。
 * @param payload 系统配置表单数据。
 */
export function updateAdminSystemConfig(payload: SystemConfigData) {
  return adminHttp.put('/dyx-manager/system-config', payload);
}

/**
 * 调用后台媒体列表接口。
 */
export function getAdminMedia() {
  return adminHttp.get('/dyx-manager/media');
}

/**
 * 调用后台媒体删除接口。
 * @param id 媒体主键。
 */
export function deleteAdminMedia(id: string | number) {
  return adminHttp.delete(`/dyx-manager/media/${id}`);
}

/**
 * 导入 uploads 目录下已存在文件。
 */
export function importExistingAdminMedia() {
  return adminHttp.post('/dyx-manager/media/import-existing');
}

/**
 * 获取后台用户列表。
 *
 * @returns 返回后台用户列表数据。
 * @throws 该函数不会主动抛出同步异常；接口失败时会以 Promise reject 形式返回。
 * @author Dyx
 */
export function getAdminUsers() {
  return adminHttp.get('/dyx-manager/users');
}

/**
 * 保存后台用户数据。
 *
 * @param payload 用户表单数据；带有 id 时更新现有用户，否则创建新用户。
 * @returns 返回保存后的用户数据。
 * @throws 该函数不会主动抛出同步异常；保存失败、校验不通过或权限不足时会以 Promise reject 形式返回。
 * @author Dyx
 */
export function saveAdminUser(payload: Partial<AdminListUserData>) {
  if (payload.id) {
    return adminHttp.put(`/dyx-manager/users/${payload.id}`, payload);
  }
  return adminHttp.post('/dyx-manager/users', payload);
}

/**
 * 删除后台用户。
 *
 * @param id 用户主键。
 * @returns 返回删除接口的 Promise 结果。
 * @throws 该函数不会主动抛出同步异常；删除失败或权限不足时会以 Promise reject 形式返回。
 * @author Dyx
 */
export function deleteAdminUser(id: number) {
  return adminHttp.delete(`/dyx-manager/users/${id}`);
}

/**
 * 上传后台媒体文件。
 *
 * @param file 待上传文件。
 * @returns 返回媒体上传接口的 Promise 结果。
 * @throws 该函数不会主动抛出同步异常；上传失败时会以 Promise reject 形式返回。
 * @author Dyx
 */
export function uploadAdminMedia(file: File) {
  const formData = new FormData();
  formData.append('file', file);
  return adminHttp.post('/dyx-manager/media/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}

/**
 * 调用后台登录接口。
 *
 * @param payload 登录表单数据，包含用户名和密码。
 * @returns 返回包含 JWT 与后台用户信息的 Promise 结果。
 * @throws 该函数不会主动抛出同步异常；登录失败时会以 Promise reject 形式返回。
 * @author Dyx
 */
export function adminLogin(payload: { username: string; password: string }) {
  return publicHttp.post('/auth/login', payload);
}
