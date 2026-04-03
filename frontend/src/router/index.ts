import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import WebLayout from '@/layouts/WebLayout.vue';
import AdminLayout from '@/layouts/AdminLayout.vue';

const SITE_NAME = 'dyx-blog';

interface SeoMeta {
  title?: string;
  description?: string;
  canonicalPath?: string;
  ogType?: 'website' | 'article';
  robots?: string;
  pageBgClass?: string;
}

/**
 * 前台路由配置。
 * 负责承载个人站点相关页面。
 */
const webRoutes: RouteRecordRaw[] = [
  {
    path: '/',
    component: WebLayout,
    meta: { pageBgClass: 'dyx-page-bg-default' },
    children: [
      {
        path: '',
        name: 'dyx-home',
        component: () => import('@/views/web/HomeView.vue'),
        meta: {
          pageBgClass: 'dyx-page-bg-home-hero',
          title: `${SITE_NAME} | 个人主页、博客与简历`,
          description: '浏览 dyx-blog 的个人主页、博客文章、项目经历、简历展示、动态记录与留言互动内容。',
          canonicalPath: '/',
          ogType: 'website'
        } satisfies SeoMeta
      },
      {
        path: 'moments',
        name: 'dyx-moments',
        component: () => import('@/views/web/MomentsView.vue'),
        meta: {
          title: `动态记录 | ${SITE_NAME}`,
          description: '查看 dyx-blog 的动态记录，了解日常思考、项目进展与碎片化内容更新。',
          canonicalPath: '/moments',
          ogType: 'website'
        } satisfies SeoMeta
      },
      {
        path: 'moments/:id',
        name: 'dyx-moment-detail',
        component: () => import('@/views/web/MomentDetailView.vue'),
        meta: {
          title: `动态详情 | ${SITE_NAME}`,
          description: '查看 dyx-blog 的动态详情内容。',
          canonicalPath: '/moments',
          ogType: 'article'
        } satisfies SeoMeta
      },
      { path: 'projects', redirect: '/resume' },
      { path: 'photos', redirect: '/about' },
      { path: 'experience', redirect: '/resume' },
      {
        path: 'resume',
        name: 'dyx-resume',
        component: () => import('@/views/web/ExperienceView.vue'),
        meta: {
          title: `个人简历 | ${SITE_NAME}`,
          description: '查看 dyx-blog 的个人简历、教育经历、工作经历、项目经历与 PDF 简历信息。',
          canonicalPath: '/resume',
          ogType: 'website'
        } satisfies SeoMeta
      },
      {
        path: 'guestbook',
        name: 'dyx-guestbook',
        component: () => import('@/views/web/GuestbookView.vue'),
        meta: {
          title: `留言板 | ${SITE_NAME}`,
          description: '在 dyx-blog 留言板中查看访客留言并留下你的建议与想法。',
          canonicalPath: '/guestbook',
          ogType: 'website'
        } satisfies SeoMeta
      },
      { path: 'profile', redirect: '/about' },
      {
        path: 'about',
        name: 'dyx-about',
        component: () => import('@/views/web/ProfileView.vue'),
        meta: {
          title: `关于我 | ${SITE_NAME}`,
          description: '了解 dyx-blog 站点作者的个人介绍、联系方式、作品列表与荣誉经历。',
          canonicalPath: '/about',
          ogType: 'website'
        } satisfies SeoMeta
      },
      {
        path: 'blog',
        name: 'dyx-blog-list',
        component: () => import('@/views/web/BlogListView.vue'),
        meta: {
          title: `博客文章 | ${SITE_NAME}`,
          description: '阅读 dyx-blog 发布的博客文章，涵盖技术总结、项目记录、学习笔记与个人思考。',
          canonicalPath: '/blog',
          ogType: 'website'
        } satisfies SeoMeta
      },
      {
        path: 'blog/:id',
        name: 'dyx-blog-detail',
        component: () => import('@/views/web/BlogDetailView.vue'),
        meta: {
          title: `博客详情 | ${SITE_NAME}`,
          description: '查看 dyx-blog 的博客详情内容与文章摘要。',
          canonicalPath: '/blog',
          ogType: 'article'
        } satisfies SeoMeta
      }
    ]
  }
];

/**
 * 后台路由配置。
 * 负责后台管理相关页面与鉴权分区。
 */
const adminRoutes: RouteRecordRaw[] = [
  {
    path: '/dyx-manager/login',
    name: 'dyx-admin-login',
    component: () => import('@/views/admin/AdminLoginView.vue'),
    meta: { robots: 'noindex,nofollow' } satisfies SeoMeta
  },
  {
    path: '/dyx-manager',
    component: AdminLayout,
    meta: { requiresAuth: true, robots: 'noindex,nofollow' } satisfies SeoMeta & { requiresAuth: boolean },
    children: [
      { path: '', redirect: '/dyx-manager/dashboard' },
      { path: 'dashboard', name: 'dyx-admin-dashboard', component: () => import('@/views/admin/AdminDashboardView.vue') },
      { path: 'visit-logs', name: 'dyx-admin-visit-logs', component: () => import('@/views/admin/AdminVisitLogsView.vue') },
      { path: 'guestbook', name: 'dyx-admin-guestbook', component: () => import('@/views/admin/AdminGuestbookView.vue') },
      { path: 'posts', name: 'dyx-admin-posts', component: () => import('@/views/admin/AdminPostsView.vue') },
      { path: 'moments', name: 'dyx-admin-moments', component: () => import('@/views/admin/AdminMomentsView.vue') },
      { path: 'projects', redirect: '/dyx-manager/resume' },
      { path: 'honors', name: 'dyx-admin-honors', component: () => import('@/views/admin/AdminHonorsView.vue') },
      { path: 'hero', redirect: '/dyx-manager/home/hero' },
      { path: 'home', redirect: '/dyx-manager/home/hero' },
      { path: 'home/hero', name: 'dyx-admin-home-hero', component: () => import('@/views/admin/AdminHeroView.vue') },
      { path: 'home/footprints', name: 'dyx-admin-home-footprints', component: () => import('@/views/admin/AdminFootprintsView.vue') },
      { path: 'home/activity', name: 'dyx-admin-home-activity', component: () => import('@/views/admin/AdminHomeActivityView.vue') },
      { path: 'photos', redirect: '/dyx-manager/media' },
      { path: 'profile', redirect: '/dyx-manager/about' },
      { path: 'about', name: 'dyx-admin-about', component: () => import('@/views/admin/AdminProfileView.vue') },
      { path: 'resume', name: 'dyx-admin-resume', component: () => import('@/views/admin/AdminResumeView.vue') },
      { path: 'works', name: 'dyx-admin-works', component: () => import('@/views/admin/AdminWorksView.vue') },
      { path: 'media', name: 'dyx-admin-media', component: () => import('@/views/admin/AdminMediaView.vue') },
      { path: 'system-config', name: 'dyx-admin-system-config', component: () => import('@/views/admin/AdminSystemConfigView.vue') },
      { path: 'users', name: 'dyx-admin-users', component: () => import('@/views/admin/AdminUsersView.vue') }
    ]
  }
];

/**
 * 创建前后台共用的全局路由实例。
 *
 * @returns 返回挂载了公开站点与后台管理路由树的 Vue Router 实例。
 * @throws 该初始化过程不会主动抛出业务异常；若路由配置非法，则由 Vue Router 在运行时给出错误。
 * @author Dyx
 */
const router = createRouter({
  history: createWebHistory(),
  routes: [...webRoutes, ...adminRoutes]
});

/**
 * 全局前置路由守卫。
 * <p>
 * 该守卫用于控制后台受保护路由的访问权限：未登录时跳转到后台登录页并附带原始目标地址，
 * 已登录时访问登录页则直接重定向到后台仪表盘。
 * </p>
 *
 * @param to 即将进入的目标路由对象。
 * @returns 返回允许放行、重定向地址或重定向配置对象。
 * @throws 该守卫不会主动抛出业务异常；鉴权失败时通过返回重定向结果完成处理。
 * @author Dyx
 */
router.beforeEach((to) => {
  const authStore = useAuthStore();
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    return {
      path: '/dyx-manager/login',
      query: {
        redirect: to.fullPath
      }
    };
  }
  if (to.path === '/dyx-manager/login' && authStore.isAuthenticated) {
    return '/dyx-manager/dashboard';
  }
  return true;
});

export default router;
