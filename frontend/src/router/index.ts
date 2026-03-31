import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import WebLayout from '@/layouts/WebLayout.vue';
import AdminLayout from '@/layouts/AdminLayout.vue';

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
      { path: '', name: 'dyx-home', component: () => import('@/views/web/HomeView.vue'), meta: { pageBgClass: 'dyx-page-bg-home-hero' } },
      { path: 'moments', name: 'dyx-moments', component: () => import('@/views/web/MomentsView.vue') },
      { path: 'moments/:id', name: 'dyx-moment-detail', component: () => import('@/views/web/MomentDetailView.vue') },
      { path: 'projects', redirect: '/resume' },
      { path: 'photos', redirect: '/about' },
      { path: 'experience', redirect: '/resume' },
      { path: 'resume', name: 'dyx-resume', component: () => import('@/views/web/ExperienceView.vue') },
      { path: 'guestbook', name: 'dyx-guestbook', component: () => import('@/views/web/GuestbookView.vue') },
      { path: 'profile', redirect: '/about' },
      { path: 'about', name: 'dyx-about', component: () => import('@/views/web/ProfileView.vue') },
      { path: 'blog', name: 'dyx-blog-list', component: () => import('@/views/web/BlogListView.vue') },
      { path: 'blog/:id', name: 'dyx-blog-detail', component: () => import('@/views/web/BlogDetailView.vue') }
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
    component: () => import('@/views/admin/AdminLoginView.vue')
  },
  {
    path: '/dyx-manager',
    component: AdminLayout,
    meta: { requiresAuth: true },
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
