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
    path: '/admin/login',
    name: 'dyx-admin-login',
    component: () => import('@/views/admin/AdminLoginView.vue')
  },
  {
    path: '/admin',
    component: AdminLayout,
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: '/admin/dashboard' },
      { path: 'dashboard', name: 'dyx-admin-dashboard', component: () => import('@/views/admin/AdminDashboardView.vue') },
      { path: 'visit-logs', name: 'dyx-admin-visit-logs', component: () => import('@/views/admin/AdminVisitLogsView.vue') },
      { path: 'guestbook', name: 'dyx-admin-guestbook', component: () => import('@/views/admin/AdminGuestbookView.vue') },
      { path: 'posts', name: 'dyx-admin-posts', component: () => import('@/views/admin/AdminPostsView.vue') },
      { path: 'moments', name: 'dyx-admin-moments', component: () => import('@/views/admin/AdminMomentsView.vue') },
      { path: 'projects', redirect: '/admin/resume' },
      { path: 'honors', name: 'dyx-admin-honors', component: () => import('@/views/admin/AdminHonorsView.vue') },
      { path: 'hero', redirect: '/admin/home/hero' },
      { path: 'home', redirect: '/admin/home/hero' },
      { path: 'home/hero', name: 'dyx-admin-home-hero', component: () => import('@/views/admin/AdminHeroView.vue') },
      { path: 'home/footprints', name: 'dyx-admin-home-footprints', component: () => import('@/views/admin/AdminFootprintsView.vue') },
      { path: 'home/activity', name: 'dyx-admin-home-activity', component: () => import('@/views/admin/AdminHomeActivityView.vue') },
      { path: 'photos', redirect: '/admin/media' },
      { path: 'profile', redirect: '/admin/about' },
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
 * 创建全局路由实例。
 */
const router = createRouter({
  history: createWebHistory(),
  routes: [...webRoutes, ...adminRoutes]
});

/**
 * 路由守卫。
 * 若访问后台受保护路由且未登录，则跳转到登录页。
 */
router.beforeEach((to) => {
  const authStore = useAuthStore();
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    return {
      path: '/admin/login',
      query: {
        redirect: to.fullPath
      }
    };
  }
  if (to.path === '/admin/login' && authStore.isAuthenticated) {
    return '/admin/dashboard';
  }
  return true;
});

export default router;
