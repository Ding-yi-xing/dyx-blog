import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import DyxWebLayout from '@/layouts/DyxWebLayout.vue';
import DyxAdminLayout from '@/layouts/DyxAdminLayout.vue';

/**
 * 前台路由配置。
 * 负责承载个人站点相关页面。
 */
const webRoutes: RouteRecordRaw[] = [
  {
    path: '/',
    component: DyxWebLayout,
    children: [
      { path: '', name: 'dyx-home', component: () => import('@/views/web/DyxHomeView.vue') },
      { path: 'moments', name: 'dyx-moments', component: () => import('@/views/web/DyxMomentsView.vue') },
      { path: 'projects', name: 'dyx-projects', component: () => import('@/views/web/DyxProjectsView.vue') },
      { path: 'photos', name: 'dyx-photos', component: () => import('@/views/web/DyxPhotosView.vue') },
      { path: 'experience', name: 'dyx-experience', component: () => import('@/views/web/DyxExperienceView.vue') },
      { path: 'profile', name: 'dyx-profile', component: () => import('@/views/web/DyxProfileView.vue') },
      { path: 'blog', name: 'dyx-blog-list', component: () => import('@/views/web/DyxBlogListView.vue') },
      { path: 'blog/:id', name: 'dyx-blog-detail', component: () => import('@/views/web/DyxBlogDetailView.vue') }
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
    component: () => import('@/views/admin/DyxAdminLoginView.vue')
  },
  {
    path: '/admin',
    component: DyxAdminLayout,
    meta: { requiresAuth: true },
    children: [
      { path: '', redirect: '/admin/dashboard' },
      { path: 'dashboard', name: 'dyx-admin-dashboard', component: () => import('@/views/admin/DyxAdminDashboardView.vue') },
      { path: 'posts', name: 'dyx-admin-posts', component: () => import('@/views/admin/DyxAdminPostsView.vue') },
      { path: 'moments', name: 'dyx-admin-moments', component: () => import('@/views/admin/DyxAdminMomentsView.vue') },
      { path: 'projects', name: 'dyx-admin-projects', component: () => import('@/views/admin/DyxAdminProjectsView.vue') },
      { path: 'photos', name: 'dyx-admin-photos', component: () => import('@/views/admin/DyxAdminPhotosView.vue') },
      { path: 'profile', name: 'dyx-admin-profile', component: () => import('@/views/admin/DyxAdminProfileView.vue') },
      { path: 'media', name: 'dyx-admin-media', component: () => import('@/views/admin/DyxAdminMediaView.vue') },
      { path: 'users', name: 'dyx-admin-users', component: () => import('@/views/admin/DyxAdminUsersView.vue') }
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
