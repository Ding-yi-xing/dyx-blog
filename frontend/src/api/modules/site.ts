import http from '@/api/http';

/**
 * 个人资料数据结构。
 */
export interface ProfileData {
  id?: number;
  siteTitle?: string;
  heroTitle?: string;
  heroSubtitle?: string;
  aboutContent?: string;
  educationExperience?: string;
  workExperience?: string;
  email?: string;
  phone?: string;
  wechat?: string;
  githubUrl?: string;
  avatarUrl?: string;
}

/**
 * 文章数据结构。
 */
export interface PostData {
  id: number;
  title: string;
  summary?: string;
  content?: string;
  coverImage?: string;
  category?: string;
  tags?: string;
  published?: number;
  updatedAt?: string;
}

/**
 * 动态数据结构。
 */
export interface MomentData {
  id: number;
  title: string;
  content?: string;
  coverImage?: string;
  happenedAt?: string;
  sortOrder?: number;
  published?: number;
  updatedAt?: string;
}

/**
 * 项目经历数据结构。
 */
export interface ProjectData {
  id: number;
  name: string;
  roleName?: string;
  description?: string;
  techStack?: string;
  projectLink?: string;
  coverImage?: string;
  sortOrder?: number;
  published?: number;
  updatedAt?: string;
}

/**
 * 照片数据结构。
 */
export interface PhotoData {
  id: number;
  title: string;
  imageUrl?: string;
  description?: string;
  shotAt?: string;
  sortOrder?: number;
  published?: number;
  updatedAt?: string;
}

/**
 * 首页聚合数据结构。
 */
export interface HomeData {
  profile?: ProfileData;
  latestPosts?: PostData[];
  latestMoments?: MomentData[];
  featuredProjects?: ProjectData[];
}

/**
 * 获取前台首页聚合数据。
 */
export function getHomeData() {
  return http.get('/site/home');
}

/**
 * 获取个人资料信息。
 */
export function getProfile() {
  return http.get('/site/profile');
}

/**
 * 获取动态列表。
 */
export function getMoments() {
  return http.get('/site/moments');
}

/**
 * 获取项目经历列表。
 */
export function getProjects() {
  return http.get('/site/projects');
}

/**
 * 获取照片列表。
 */
export function getPhotos() {
  return http.get('/site/photos');
}

/**
 * 获取博客列表。
 */
export function getPosts() {
  return http.get('/posts');
}

/**
 * 获取博客详情。
 * @param id 文章主键。
 */
export function getPostDetail(id: string | number) {
  return http.get(`/posts/${id}`);
}
