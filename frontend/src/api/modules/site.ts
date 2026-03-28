import http from '@/api/http';

export type HeroBlockType = 'eyebrow' | 'title' | 'subtitle' | 'tags' | 'image';
export type HeroBlockColumn = 'left' | 'right';

export interface ContactMethodData {
  type?: string;
  label?: string;
  value?: string;
}

export interface HeroBaseBlock {
  id: string;
  type: HeroBlockType;
  column: HeroBlockColumn;
}

export interface HeroTextBlock extends HeroBaseBlock {
  type: 'eyebrow' | 'title' | 'subtitle';
  text: string;
}

export interface HeroTagsBlock extends HeroBaseBlock {
  type: 'tags';
  items: string[];
}

export interface HeroImageBlock extends HeroBaseBlock {
  type: 'image';
  imageUrl?: string;
  backgroundImageUrl?: string;
  alt?: string;
}

export type HeroBlock = HeroTextBlock | HeroTagsBlock | HeroImageBlock;

export interface HeroConfigData {
  version: number;
  blocks: HeroBlock[];
}

/**
 * 个人资料数据结构。
 */
export interface ProfileData {
  id?: number;
  siteTitle?: string;
  heroTitle?: string;
  heroSubtitle?: string;
  heroConfig?: string;
  aboutContent?: string;
  educationExperience?: string;
  workExperience?: string;
  email?: string;
  phone?: string;
  wechat?: string;
  githubUrl?: string;
  avatarUrl?: string;
  resumePdfUrl?: string;
  guestbookIntro?: string;
  contactMethods?: string;
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
  viewCount?: number;
}

/**
 * 动态数据结构。
 */
export interface MomentData {
  id: number;
  title: string;
  content?: string;
  coverImage?: string;
  imageUrls?: string;
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
 * 个人作品数据结构。
 */
export interface WorkData {
  id: number;
  title: string;
  summary?: string;
  coverImage?: string;
  imageUrls?: string;
  videoUrl?: string;
  videoPoster?: string;
  workLink?: string;
  sortOrder?: number;
  published?: number;
  updatedAt?: string;
}

/**
 * 荣誉数据结构。
 */
export interface HonorData {
  id: number;
  title: string;
  issuer?: string;
  description?: string;
  coverImage?: string;
  imageUrls?: string;
  attachmentUrl?: string;
  awardAt?: string;
  sortOrder?: number;
  published?: number;
  updatedAt?: string;
}

export interface FootprintData {
  id?: number;
  cityName: string;
  countryName?: string;
  regionName?: string;
  positionX?: number;
  positionY?: number;
  visitedAt?: string;
  description?: string;
  importance?: number;
  sortOrder?: number;
  published?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface HomeSystemConfigData {
  footprintEyebrow?: string;
  footprintTitle?: string;
  footprintSubtitle?: string;
  footprintDescription?: string;
  copyrightText?: string;
  techSupportText?: string;
}

/**
 * 首页聚合数据结构。
 */
export interface HomeData {
  profile?: ProfileData;
  latestPosts?: PostData[];
  latestMoments?: MomentData[];
  featuredProjects?: ProjectData[];
  latestHonors?: HonorData[];
  footprints?: FootprintData[];
  systemConfig?: HomeSystemConfigData;
}

export interface GuestbookMessageData {
  id?: number;
  content?: string;
  published?: number;
  ipAddress?: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface GuestbookData {
  guestbookIntro?: string;
  messages?: GuestbookMessageData[];
}

export function createDefaultHeroConfig(profile?: Pick<ProfileData, 'siteTitle' | 'heroTitle' | 'heroSubtitle' | 'avatarUrl'>): HeroConfigData {
  return {
    version: 1,
    blocks: [
      {
        id: 'eyebrow-default',
        type: 'eyebrow',
        column: 'left',
        text: profile?.siteTitle || 'HELLO THERE!'
      },
      {
        id: 'title-default',
        type: 'title',
        column: 'left',
        text: profile?.heroTitle || '写代码的人，也写点文字。'
      },
      {
        id: 'subtitle-default',
        type: 'subtitle',
        column: 'left',
        text:
          profile?.heroSubtitle ||
          '这里有后端开发、安全研究、折腾小工具的记录，也有一些不那么严肃的碎碎念。这个博客更像是一个公开的笔记本，欢迎随便翻翻。'
      },
      {
        id: 'tags-default',
        type: 'tags',
        column: 'left',
        items: ['后端 · Java', '安全 / 基础设施', '随笔与长文']
      },
      {
        id: 'image-default',
        type: 'image',
        column: 'right',
        imageUrl: profile?.avatarUrl,
        backgroundImageUrl: '',
        alt: 'avatar'
      }
    ]
  };
}

export function parseHeroConfig(profile?: Pick<ProfileData, 'heroConfig' | 'siteTitle' | 'heroTitle' | 'heroSubtitle' | 'avatarUrl'>): HeroConfigData {
  const fallback = createDefaultHeroConfig(profile);
  if (!profile?.heroConfig) {
    return fallback;
  }
  try {
    const parsed = JSON.parse(profile.heroConfig) as Partial<HeroConfigData>;
    if (!parsed || !Array.isArray(parsed.blocks)) {
      return fallback;
    }
    return {
      version: typeof parsed.version === 'number' ? parsed.version : 1,
      blocks: parsed.blocks as HeroBlock[]
    };
  } catch {
    return fallback;
  }
}

export function parseContactMethods(value?: string | ContactMethodData[] | null): ContactMethodData[] {
  if (Array.isArray(value)) {
    return value.filter((item) => item?.value);
  }
  if (!value) {
    return [];
  }
  try {
    const parsed = JSON.parse(value) as ContactMethodData[];
    return Array.isArray(parsed) ? parsed.filter((item) => item?.value) : [];
  } catch {
    return [];
  }
}

export function stringifyContactMethods(items: ContactMethodData[]): string {
  return JSON.stringify(
    items
      .map((item) => ({
        type: item.type?.trim(),
        label: item.label?.trim(),
        value: item.value?.trim()
      }))
      .filter((item) => item.value)
  );
}

export function resolveProfileContactMethods(
  profile?: Pick<ProfileData, 'contactMethods' | 'email' | 'phone' | 'wechat' | 'githubUrl'> | null
): ContactMethodData[] {
  const parsed = parseContactMethods(profile?.contactMethods);
  if (parsed.length) {
    return parsed
      .map((item) => ({
        type: item.type || item.label || 'contact',
        label: item.label || item.type || '联系方式',
        value: item.value?.trim()
      }))
      .filter((item) => !!item.value);
  }
  const fallbackContacts: Array<ContactMethodData | null> = [
    profile?.email ? { type: 'email', label: '邮箱', value: profile.email } : null,
    profile?.phone ? { type: 'phone', label: '电话', value: profile.phone } : null,
    profile?.wechat ? { type: 'wechat', label: '微信', value: profile.wechat } : null,
    profile?.githubUrl ? { type: 'github', label: 'GitHub', value: profile.githubUrl } : null
  ];
  return fallbackContacts.filter((item): item is ContactMethodData => !!item?.value);
}

export function resolveContactHref(item?: ContactMethodData | null): string {
  const value = item?.value?.trim();
  if (!value) {
    return '';
  }
  const normalizedType = `${item?.type || ''} ${item?.label || ''}`.toLowerCase();
  if (normalizedType.includes('email') || normalizedType.includes('邮箱') || /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
    return `mailto:${value}`;
  }
  if (normalizedType.includes('phone') || normalizedType.includes('mobile') || normalizedType.includes('电话') || normalizedType.includes('手机')) {
    const phoneValue = value.replace(/[^\d+]/g, '');
    return phoneValue ? `tel:${phoneValue}` : '';
  }
  if (normalizedType.includes('github')) {
    if (/^https?:\/\//i.test(value)) {
      return value;
    }
    const githubPath = value
      .replace(/^@/, '')
      .replace(/^https?:\/\/github\.com\//i, '')
      .replace(/^github\.com\//i, '');
    return githubPath ? `https://github.com/${githubPath}` : '';
  }
  if (/^(https?:)?\/\//i.test(value)) {
    return value.startsWith('//') ? `https:${value}` : value;
  }
  if (/^www\./i.test(value) || /^[\w.-]+\.[a-z]{2,}(?:[/:?#].*)?$/i.test(value)) {
    return `https://${value}`;
  }
  return '';
}

export function isExternalContactHref(href?: string): boolean {
  return !!href && !href.startsWith('mailto:') && !href.startsWith('tel:');
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
 * 获取留言页数据。
 */
export function getGuestbookData() {
  return http.get('/site/guestbook');
}

/**
 * 提交留言。
 */
export function createGuestbookMessage(payload: Partial<GuestbookMessageData>) {
  return http.post('/site/guestbook/messages', payload);
}

/**
 * 获取动态列表。
 */
export function getMoments() {
  return http.get('/site/moments');
}

export function getMomentDetail(id: string | number) {
  return http.get(`/site/moments/${id}`);
}

/**
 * 获取项目经历列表。
 */
export function getProjects() {
  return http.get('/site/projects');
}

/**
 * 获取个人作品列表。
 */
export function getWorks() {
  return http.get('/site/works');
}

/**
 * 获取荣誉列表。
 */
export function getHonors() {
  return http.get('/site/honors');
}

/**
 * 获取已发布足迹列表。
 */
export function getFootprints() {
  return http.get('/site/footprints');
}

/**
 * 记录页面访问。
 */
export function recordSiteVisit(pageKey: string) {
  return http.post(`/site/visit/${pageKey}`);
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
