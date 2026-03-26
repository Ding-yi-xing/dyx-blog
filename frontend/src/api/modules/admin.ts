import http from '@/api/http';
import type { HonorData, MomentData, PostData, ProfileData, ProjectData, WorkData } from '@/api/modules/site';

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

/**
 * 调用后台访问日志列表接口。
 */
export function getAdminVisitLogs(params?: AdminVisitLogQuery) {
  return http.get('/admin/visit-logs', { params });
}

/**
 * 调用后台访问日志删除接口。
 * @param id 访问日志主键。
 */
export function deleteAdminVisitLog(id: number) {
  return http.delete(`/admin/visit-logs/${id}`);
}

/**
 * 调用后台访问日志批量删除接口。
 * @param ids 访问日志主键列表。
 */
export function deleteAdminVisitLogs(ids: number[]) {
  return http.post('/admin/visit-logs/batch-delete', ids);
}

/**
 * 媒体资源数据。
 */
export interface MediaData {
  id: number;
  originalName?: string;
  fileName?: string;
  fileUrl?: string;
  mediaType?: string;
  fileSize?: number;
  createdAt?: string;
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
  return http.get('/admin/dashboard/summary');
}

/**
 * 调用后台文章列表接口。
 */
export function getAdminPosts() {
  return http.get('/admin/posts');
}

/**
 * 调用后台文章保存接口。
 * @param payload 文章表单数据。
 */
export function saveAdminPost(payload: Partial<PostData>) {
  if (payload.id) {
    return http.put(`/admin/posts/${payload.id}`, payload);
  }
  return http.post('/admin/posts', payload);
}

/**
 * 调用后台文章删除接口。
 * @param id 文章主键。
 */
export function deleteAdminPost(id: number) {
  return http.delete(`/admin/posts/${id}`);
}

/**
 * 调用后台动态列表接口。
 */
export function getAdminMoments() {
  return http.get('/admin/moments');
}

/**
 * 调用后台动态保存接口。
 * @param payload 动态表单数据。
 */
export function saveAdminMoment(payload: Partial<MomentData>) {
  if (payload.id) {
    return http.put(`/admin/moments/${payload.id}`, payload);
  }
  return http.post('/admin/moments', payload);
}

/**
 * 调用后台动态删除接口。
 * @param id 动态主键。
 */
export function deleteAdminMoment(id: number) {
  return http.delete(`/admin/moments/${id}`);
}

/**
 * 调用后台项目列表接口。
 */
export function getAdminProjects() {
  return http.get('/admin/projects');
}

/**
 * 调用后台项目保存接口。
 * @param payload 项目表单数据。
 */
export function saveAdminProject(payload: Partial<ProjectData>) {
  if (payload.id) {
    return http.put(`/admin/projects/${payload.id}`, payload);
  }
  return http.post('/admin/projects', payload);
}

/**
 * 调用后台项目删除接口。
 * @param id 项目主键。
 */
export function deleteAdminProject(id: number) {
  return http.delete(`/admin/projects/${id}`);
}

/**
 * 调用后台作品列表接口。
 */
export function getAdminWorks() {
  return http.get('/admin/works');
}

/**
 * 调用后台作品保存接口。
 * @param payload 作品表单数据。
 */
export function saveAdminWork(payload: Partial<WorkData>) {
  if (payload.id) {
    return http.put(`/admin/works/${payload.id}`, payload);
  }
  return http.post('/admin/works', payload);
}

/**
 * 调用后台作品删除接口。
 * @param id 作品主键。
 */
export function deleteAdminWork(id: number) {
  return http.delete(`/admin/works/${id}`);
}

/**
 * 调用后台荣誉列表接口。
 */
export function getAdminHonors() {
  return http.get('/admin/honors');
}

/**
 * 调用后台荣誉保存接口。
 * @param payload 荣誉表单数据。
 */
export function saveAdminHonor(payload: Partial<HonorData>) {
  if (payload.id) {
    return http.put(`/admin/honors/${payload.id}`, payload);
  }
  return http.post('/admin/honors', payload);
}

/**
 * 调用后台荣誉删除接口。
 * @param id 荣誉主键。
 */
export function deleteAdminHonor(id: number) {
  return http.delete(`/admin/honors/${id}`);
}

/**
 * 调用后台首页横幅查询接口。
 */
export function getAdminHeroProfile() {
  return http.get('/admin/profile/hero');
}

/**
 * 调用后台首页横幅更新接口。
 * @param payload 首页横幅表单数据。
 */
export function updateAdminHeroProfile(payload: ProfileData) {
  return http.put('/admin/profile/hero', payload);
}

/**
 * 调用后台个人资料查询接口。
 */
export function getAdminProfile() {
  return http.get('/admin/profile');
}

/**
 * 调用后台个人资料更新接口。
 * @param payload 个人资料表单数据。
 */
export function updateAdminProfile(payload: ProfileData) {
  return http.put('/admin/profile', payload);
}

/**
 * 调用后台媒体列表接口。
 */
export function getAdminMedia() {
  return http.get('/admin/media');
}

/**
 * 调用后台媒体删除接口。
 * @param id 媒体主键。
 */
export function deleteAdminMedia(id: number) {
  return http.delete(`/admin/media/${id}`);
}

/**
 * 导入 uploads 目录下已存在文件。
 */
export function importExistingAdminMedia() {
  return http.post('/admin/media/import-existing');
}

/**
 * 调用后台用户列表接口。
 */
export function getAdminUsers() {
  return http.get('/admin/users');
}

/**
 * 调用后台用户保存接口。
 * @param payload 用户表单数据。
 */
export function saveAdminUser(payload: Partial<AdminListUserData>) {
  if (payload.id) {
    return http.put(`/admin/users/${payload.id}`, payload);
  }
  return http.post('/admin/users', payload);
}

/**
 * 调用后台用户删除接口。
 * @param id 用户主键。
 */
export function deleteAdminUser(id: number) {
  return http.delete(`/admin/users/${id}`);
}

/**
 * 调用后台媒体上传接口。
 * @param file 待上传文件。
 */
export function uploadAdminMedia(file: File) {
  const formData = new FormData();
  formData.append('file', file);
  return http.post('/admin/media/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  });
}

/**
 * 调用后台登录接口。
 * @param payload 登录表单数据。
 */
export function adminLogin(payload: { username: string; password: string }) {
  return http.post('/auth/login', payload);
}
