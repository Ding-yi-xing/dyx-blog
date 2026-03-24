import http from '@/api/http';
import type { MomentData, PhotoData, PostData, ProfileData, ProjectData } from '@/api/modules/site';

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

/**
 * 后台仪表盘摘要数据。
 */
export interface DashboardSummaryData {
  postCount?: number;
  momentCount?: number;
  projectCount?: number;
  photoCount?: number;
  userCount?: number;
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
  id: number;
  username: string;
  displayName?: string;
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
 * 调用后台照片列表接口。
 */
export function getAdminPhotos() {
  return http.get('/admin/photos');
}

/**
 * 调用后台照片保存接口。
 * @param payload 照片表单数据。
 */
export function saveAdminPhoto(payload: Partial<PhotoData>) {
  if (payload.id) {
    return http.put(`/admin/photos/${payload.id}`, payload);
  }
  return http.post('/admin/photos', payload);
}

/**
 * 调用后台照片删除接口。
 * @param id 照片主键。
 */
export function deleteAdminPhoto(id: number) {
  return http.delete(`/admin/photos/${id}`);
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
 * 调用后台用户列表接口。
 */
export function getAdminUsers() {
  return http.get('/admin/users');
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
