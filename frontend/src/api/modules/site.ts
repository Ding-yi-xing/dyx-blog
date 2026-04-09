import { publicHttp } from '@/api/http';

export type HeroBlockType = 'eyebrow' | 'title' | 'subtitle' | 'tags' | 'image';
export type HeroBlockColumn = 'left' | 'right';

export interface ContactMethodData {
  type?: string;
  label?: string;
  value?: string;
  href?: string;
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
  publishedAt?: string;
  updatedAt?: string;
  viewCount?: number;
  homeFeatured?: number;
  homeFeaturedOrder?: number;
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
  homeFeatured?: number;
  homeFeaturedOrder?: number;
}

/**
 * 项目经历数据结构。
 */
export interface ProjectData {
  id: string | number;
  name: string;
  roleName?: string;
  description?: string;
  techStack?: string;
  projectLink?: string;
  coverImage?: string;
  sortOrder?: number;
  published?: number;
  updatedAt?: string;
  homeFeatured?: number;
  homeFeaturedOrder?: number;
}

/**
 * 个人作品数据结构。
 */
export interface WorkData {
  id: string | number;
  title: string;
  summary?: string;
  coverImage?: string;
  imageUrls?: string;
  videoUrl?: string;
  videoPoster?: string;
  workLink?: string;
  awardAt?: string;
  sortOrder?: number;
  published?: number;
  updatedAt?: string;
  homeFeatured?: number;
  homeFeaturedOrder?: number;
}

/**
 * 荣誉数据结构。
 */
export interface HonorData {
  id: string | number;
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
  homeFeatured?: number;
  homeFeaturedOrder?: number;
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
  homeActivityEnablePosts?: boolean;
  homeActivityEnableMoments?: boolean;
  homeActivityEnableProjects?: boolean;
  homeActivityEnableWorks?: boolean;
  homeActivityEnableHonors?: boolean;
  homeActivityMaxItems?: number;
  homeActivityMaxItemsPerType?: number;
}

export interface HomeActivityItemData {
  type?: string;
  refId?: number | string;
  title?: string;
  summary?: string;
  coverImage?: string;
  highlightTime?: string;
}

/**
 * 首页聚合数据结构。
 * 仅保留首页当前实际使用的资料、足迹与首页系统配置。
 */
export interface HomeData {
  profile?: ProfileData;
  footprints?: FootprintData[];
  systemConfig?: HomeSystemConfigData;
  featuredItems?: HomeActivityItemData[];
}
export interface GuestbookMessageData {
  id?: number;
  content?: string;
  published?: number;
  createdAt?: string;
  updatedAt?: string;
}

export interface GuestbookData {
  guestbookIntro?: string;
  messages?: GuestbookMessageData[];
}

/**
 * 根据当前资料生成首页 Hero 的默认区块配置。
 *
 * @param profile 可选的资料子集，用于优先填充站点标题、主副标题和头像等默认展示内容。
 * @returns 返回可直接用于首页 Hero 渲染和后台初始化的默认配置对象。
 * @throws 该函数不会主动抛出异常；当传入资料缺失时会回退到内置默认文案。
 * @author Dyx
 */
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

/**
 * 解析资料中的 Hero 配置 JSON，并在解析失败时回退到默认配置。
 *
 * @param profile 可选的资料子集，既可能提供序列化后的 Hero 配置，也可能仅提供默认回退所需的基础字段。
 * @returns 返回可安全用于页面渲染的 Hero 配置对象。
 * @throws 该函数不会向外抛出 JSON 解析异常；解析失败时会捕获异常并返回默认配置。
 * @author Dyx
 */
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

const heroConfigCache = new Map<string, HeroConfigData>();

export function resolveHeroConfig(profile?: Pick<ProfileData, 'heroConfig' | 'siteTitle' | 'heroTitle' | 'heroSubtitle' | 'avatarUrl'>): HeroConfigData {
  const rawHeroConfig = profile?.heroConfig?.trim();
  if (!rawHeroConfig) {
    return createDefaultHeroConfig(profile);
  }
  const cached = heroConfigCache.get(rawHeroConfig);
  if (cached) {
    return cached;
  }
  const resolved = parseHeroConfig(profile);
  heroConfigCache.set(rawHeroConfig, resolved);
  return resolved;
}

function normalizeContactType(item?: ContactMethodData | null): ContactMethodData['type'] {
  const type = item?.type?.trim().toLowerCase() || '';
  const label = item?.label?.trim().toLowerCase() || '';
  const value = item?.value?.trim() || '';
  const href = item?.href?.trim() || '';
  const normalized = `${type} ${label}`;
  if (type === 'text' || type === 'link' || type === 'email' || type === 'phone' || type === 'wechat' || type === 'github') {
    return type;
  }
  if (normalized.includes('email') || normalized.includes('邮箱') || /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(href || value)) {
    return 'email';
  }
  if (normalized.includes('phone') || normalized.includes('mobile') || normalized.includes('电话') || normalized.includes('手机')) {
    return 'phone';
  }
  if (normalized.includes('wechat') || normalized.includes('微信')) {
    return 'wechat';
  }
  if (normalized.includes('github') || /^https?:\/\/github\.com\//i.test(href || value) || /^github\.com\//i.test(href || value) || /^@[\w-]+$/.test(href || value)) {
    return 'github';
  }
  if (/^[a-z][a-z\d+.-]*:/i.test(href || value) || /^(https?:)?\/\//i.test(href || value) || /^www\./i.test(href || value) || /^[\w.-]+\.[a-z]{2,}(?:[/:?#].*)?$/i.test(href || value)) {
    return 'link';
  }
  return 'text';
}

function resolveDefaultContactLabel(type?: ContactMethodData['type']): string {
  switch (type) {
    case 'email':
      return '邮箱';
    case 'phone':
      return '电话';
    case 'wechat':
      return '微信';
    case 'github':
      return 'GitHub';
    case 'link':
      return '链接';
    default:
      return '联系方式';
  }
}

/**
 * 解析资料中的联系方式配置并过滤无效项。
 *
 * @param value 联系方式原始值，既可能是 JSON 字符串，也可能已经是对象数组。
 * @returns 返回可用于页面渲染的联系方式数组；解析失败或值为空时返回空数组。
 * @throws 该函数不会向外抛出 JSON 解析异常；解析失败时会捕获异常并返回空数组。
 * @author Dyx
 */
export function parseContactMethods(value?: string | ContactMethodData[] | null): ContactMethodData[] {
  if (Array.isArray(value)) {
    return value.filter((item) => item?.value || item?.href);
  }
  if (!value) {
    return [];
  }
  try {
    const parsed = JSON.parse(value) as ContactMethodData[];
    return Array.isArray(parsed) ? parsed.filter((item) => item?.value || item?.href) : [];
  } catch {
    return [];
  }
}

/**
 * 将联系方式数组序列化为可持久化保存的 JSON 字符串。
 *
 * @param items 联系方式数组，通常来源于后台编辑表单。
 * @returns 返回去除首尾空白并过滤无效值后的 JSON 字符串。
 * @throws 该函数不会主动抛出业务异常；若序列化过程中出现运行时异常，将由 JavaScript 引擎抛出。
 * @author Dyx
 */
export function stringifyContactMethods(items: ContactMethodData[]): string {
  return JSON.stringify(
    items
      .map((item) => ({
        type: normalizeContactType(item),
        label: item.label?.trim(),
        value: item.value?.trim(),
        href: item.href?.trim()
      }))
      .filter((item) => item.value || item.href)
  );
}

/**
 * 根据资料对象计算页面实际可展示的联系方式列表。
 *
 * @param profile 资料对象，优先使用结构化 contactMethods，缺失时再回退到 email/phone/wechat/githubUrl 等旧字段。
 * @returns 返回已标准化标签与类型的联系方式数组。
 * @throws 该函数不会主动抛出业务异常；内部若结构化联系方式解析失败，会回退到兼容字段方案。
 * @author Dyx
 */
export function resolveProfileContactMethods(
  profile?: Pick<ProfileData, 'contactMethods' | 'email' | 'phone' | 'wechat' | 'githubUrl'> | null
): ContactMethodData[] {
  const parsed = parseContactMethods(profile?.contactMethods);
  if (parsed.length) {
    return parsed
      .map((item) => {
        const value = item.value?.trim();
        const href = item.href?.trim();
        const type = normalizeContactType(item);
        return {
          type,
          label: item.label?.trim() || resolveDefaultContactLabel(type),
          value: value || href || '',
          href: href || ''
        };
      })
      .filter((item) => !!item.value || !!item.href);
  }
  const fallbackContacts: Array<ContactMethodData | null> = [
    profile?.email ? { type: 'email', label: '邮箱', value: profile.email, href: profile.email } : null,
    profile?.phone ? { type: 'phone', label: '电话', value: profile.phone, href: profile.phone } : null,
    profile?.wechat ? { type: 'wechat', label: '微信', value: profile.wechat, href: profile.wechat } : null,
    profile?.githubUrl ? { type: 'github', label: 'GitHub', value: profile.githubUrl, href: profile.githubUrl } : null
  ];
  return fallbackContacts.filter((item): item is ContactMethodData => !!item?.value || !!item?.href);
}

/**
 * 根据联系方式项推导可点击跳转的链接地址。
 *
 * @param item 单条联系方式配置。
 * @returns 返回 mailto、tel、GitHub 或普通外链地址；若无法生成有效链接则返回空字符串。
 * @throws 该函数不会主动抛出业务异常；无法识别的联系方式类型会按空链接处理。
 * @author Dyx
 */
export function resolveContactHref(item?: ContactMethodData | null): string {
  const value = item?.value?.trim() || '';
  const rawHref = item?.href?.trim() || '';
  const source = rawHref || value;
  if (!source) {
    return '';
  }
  const type = normalizeContactType(item);
  if (type === 'text') {
    return '';
  }
  if (type === 'email') {
    const emailValue = source.replace(/^mailto:/i, '');
    return emailValue ? `mailto:${emailValue}` : '';
  }
  if (type === 'phone') {
    const phoneValue = source.replace(/^tel:/i, '').replace(/[^\d+]/g, '');
    return phoneValue ? `tel:${phoneValue}` : '';
  }
  if (type === 'github') {
    if (/^https?:\/\//i.test(source)) {
      return source;
    }
    const githubPath = source
      .replace(/^@/, '')
      .replace(/^https?:\/\/github\.com\//i, '')
      .replace(/^github\.com\//i, '');
    return githubPath ? `https://github.com/${githubPath}` : '';
  }
  if (type === 'wechat' && !/^[a-z][a-z\d+.-]*:/i.test(source)) {
    return '';
  }
  if (/^[a-z][a-z\d+.-]*:/i.test(source)) {
    return source;
  }
  if (/^(https?:)?\/\//i.test(source)) {
    return source.startsWith('//') ? `https:${source}` : source;
  }
  if (/^www\./i.test(source) || /^[\w.-]+\.[a-z]{2,}(?:[/:?#].*)?$/i.test(source)) {
    return `https://${source}`;
  }
  return '';
}

/**
 * 判断联系方式链接是否属于浏览器外部跳转地址。
 *
 * @param href 待判断的联系方式链接。
 * @returns 当链接既存在且不是 mailto/tel/weixin 协议时返回 true。
 * @throws 该函数不会主动抛出异常。
 * @author Dyx
 */
export function isExternalContactHref(href?: string): boolean {
  return !!href && !href.startsWith('mailto:') && !href.startsWith('tel:') && !href.startsWith('weixin:');
}

/**
 * 获取首页聚合展示数据。
 *
 * @returns 返回首页所需的资料、足迹、系统配置与精选内容。
 * @throws 该函数不会主动抛出同步异常；接口失败时会以 Promise reject 形式返回。
 * @author Dyx
 */
export function getHomeData() {
  return publicHttp.get('/site/home');
}

/**
 * 获取公开展示所需的个人资料信息。
 *
 * @returns 返回 About、Resume 等页面复用的资料数据。
 * @throws 该函数不会主动抛出同步异常；接口失败时会以 Promise reject 形式返回。
 * @author Dyx
 */
export function getProfile() {
  return publicHttp.get('/site/profile');
}

/**
 * 获取留言页展示数据。
 *
 * @returns 返回留言页介绍文案与已发布留言列表。
 * @throws 该函数不会主动抛出同步异常；接口失败时会以 Promise reject 形式返回。
 * @author Dyx
 */
export function getGuestbookData() {
  return publicHttp.get('/site/guestbook');
}

/**
 * 提交公开留言。
 *
 * @param payload 留言请求体，通常只包含留言内容等可提交字段。
 * @returns 返回保存后的留言数据。
 * @throws 该函数不会主动抛出同步异常；留言校验失败或接口调用失败时会以 Promise reject 形式返回。
 * @author Dyx
 */
export function createGuestbookMessage(payload: Partial<GuestbookMessageData>) {
  return publicHttp.post('/site/guestbook/messages', payload);
}

/**
 * 获取公开动态列表。
 *
 * @returns 返回已发布动态列表。
 * @throws 该函数不会主动抛出同步异常；接口失败时会以 Promise reject 形式返回。
 * @author Dyx
 */
export function getMoments() {
  return publicHttp.get('/site/moments');
}

/**
 * 获取单条动态详情。
 *
 * @param id 动态主键。
 * @returns 返回指定动态详情数据。
 * @throws 该函数不会主动抛出同步异常；目标不存在或接口失败时会以 Promise reject 形式返回。
 * @author Dyx
 */
export function getMomentDetail(id: string | number) {
  return publicHttp.get(`/site/moments/${id}`);
}

/**
 * 获取公开项目经历列表。
 *
 * @returns 返回已发布项目经历列表。
 * @throws 该函数不会主动抛出同步异常；接口失败时会以 Promise reject 形式返回。
 * @author Dyx
 */
export function getProjects() {
  return publicHttp.get('/site/projects');
}

/**
 * 获取公开个人作品列表。
 *
 * @returns 返回已发布作品列表。
 * @throws 该函数不会主动抛出同步异常；接口失败时会以 Promise reject 形式返回。
 * @author Dyx
 */
export function getWorks() {
  return publicHttp.get('/site/works');
}

/**
 * 获取公开荣誉列表。
 *
 * @returns 返回已发布荣誉列表。
 * @throws 该函数不会主动抛出同步异常；接口失败时会以 Promise reject 形式返回。
 * @author Dyx
 */
export function getHonors() {
  return publicHttp.get('/site/honors');
}

/**
 * 获取已发布首页足迹列表。
 *
 * @returns 返回已发布足迹列表数据。
 * @throws 该函数不会主动抛出同步异常；接口失败时会以 Promise reject 形式返回。
 * @author Dyx
 */
export function getFootprints() {
  return publicHttp.get('/site/footprints');
}

/**
 * 主动上报公开页面访问事件。
 *
 * @param pageKey 页面标识，用于区分首页、关于我、简历、博客详情等统计维度。
 * @returns 返回访问记录接口的调用结果。
 * @throws 该函数不会主动抛出同步异常；统计接口失败时会以 Promise reject 形式返回。
 * @author Dyx
 */
export function recordSiteVisit(pageKey: string) {
  return publicHttp.post(`/site/visit/${pageKey}`);
}

/**
 * 获取公开博客列表。
 *
 * @returns 返回已发布博客列表数据。
 * @throws 该函数不会主动抛出同步异常；接口失败时会以 Promise reject 形式返回。
 * @author Dyx
 */
export function getPosts() {
  return publicHttp.get('/site/posts');
}

/**
 * 获取公开博客详情。
 *
 * @param id 文章主键。
 * @returns 返回指定文章详情数据。
 * @throws 该函数不会主动抛出同步异常；目标不存在或接口失败时会以 Promise reject 形式返回。
 * @author Dyx
 */
export function getPostDetail(id: string | number) {
  return publicHttp.get(`/site/posts/${id}`);
}
