import { createRouter, createWebHistory } from 'vue-router'

import AdminLayout from '../views/admin/AdminLayout.vue'
import AdminUserList from '../views/admin/AdminUserList.vue'
import ContributorReview from '../views/admin/ContributorReview.vue'
import ResourceReviewCenter from '../views/admin/ResourceReviewCenter.vue'
import ResourceReviewDetail from '../views/admin/ResourceReviewDetail.vue'
import AdminResourceList from '../views/admin/AdminResourceList.vue'
import MasterDataManagement from '../views/admin/MasterDataManagement.vue'
import PublicLayout from '../layouts/PublicLayout.vue'
import HomeView from '../views/HomeView.vue'
import Login from '../views/auth/Login.vue'
import Profile from '../views/Profile.vue'
import Register from '../views/auth/Register.vue'
import FavoritesView from '../views/FavoritesView.vue'
import ResourceDetailView from '../views/ResourceDetailView.vue'
import SubmissionsView from '../views/SubmissionsView.vue'
import FeedbackView from '../views/FeedbackView.vue'
import CreateResource from '../views/CreateResource.vue'
import { getStoredRoles, getToken } from '../api/auth.js'
import ResetPassword from '../views/auth/ResetPassword.vue'


const routes = [
  {
    path: '/',
    component: PublicLayout,
    children: [
      {
        path: '',
        name: 'Landing',
        component: HomeView,
        meta: { title: 'Heritage Resource Hall' },
      },
      {
        path: 'login',
        name: 'Login',
        component: Login,
        meta: { title: 'Login' },
      },
      {
        path: 'register',
        name: 'Register',
        component: Register,
        meta: { title: 'Register' },
      },
      {
        path: 'profile',
        name: 'Profile',
        component: Profile,
        meta: { title: 'Profile', requiresAuth: true },
      },
      {
        path: 'favorites',
        name: 'Favorites',
        component: FavoritesView,
        meta: {
          title: 'My Favorites',
          requiresAuth: true,
        },
      },
      {
        path: 'resources/create',
        name: 'CreateResource',
        component: CreateResource,
        meta: {
          title: 'Create Resource',
          requiresAuth: true,
          roles: ['CONTRIBUTOR', 'ADMIN'],
        },
      },
      {
        path: 'resources/submissions',
        name: 'Submissions',
        component: SubmissionsView,
        meta: {
          title: 'My Submissions',
          requiresAuth: true,
        },
      },
      {
        path: 'resources/:id/feedback',
        name: 'Feedback',
        component: FeedbackView,
        meta: {
          title: 'Feedback Detail',
          requiresAuth: true,
        },
      },
      {
        path: 'resources/:id/edit',
        name: 'EditResource',
        component: CreateResource,
        meta: {
          title: 'Edit Resource',
          requiresAuth: true,
          roles: ['CONTRIBUTOR', 'ADMIN'],
        },
      },
      {
        path: 'resources/:id',
        name: 'ResourceDetail',
        component: ResourceDetailView,
        meta: { title: 'Resource Detail' },
      },
    ],
  },


  //忘记/重置
    {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('../views/auth/ForgotPassword.vue'),
    meta: { title: 'Forgot Password' }
  },
    {
    path: '/reset-password',
    name: 'ResetPassword',
    component: ResetPassword,     //
    props: true,
    meta: { title: 'Reset Password' }
  },




  //多会话
    {
    path: '/sessions',
    name: 'Sessions',
    component: () => import('../views/auth/Sessions.vue'),
    meta: {
      title: 'Active Sessions',
      requiresAuth: true
    }
  },






  // Home page for viewers
  {
    path: '/home',
    redirect: '/',
  },
  {
    path: '/admin',
    component: AdminLayout,
    meta: {
      title: 'Administration',
      requiresAuth: true,
      roles: ['ADMIN'],
    },
    children: [
      {
        path: '',
        redirect: '/admin/users',
      },
      {
        path: 'users',
        name: 'AdminUserList',
        component: AdminUserList,
        meta: {
          title: 'User Management',
          requiresAuth: true,
          roles: ['ADMIN'],
        },
      },
      {
        path: 'review',
        name: 'ContributorReview',
        component: ContributorReview,
        meta: {
          title: 'Contributor Review',
          requiresAuth: true,
          roles: ['ADMIN'],
        },
      },
      {
        path: 'resource-review',
        name: 'ResourceReviewCenter',
        component: ResourceReviewCenter,
        meta: {
          title: 'Resource Review',
          requiresAuth: true,
          roles: ['ADMIN'],
        },
      },
      {
        path: 'resource-review/:id',
        name: 'ResourceReviewDetail',
        component: ResourceReviewDetail,
        meta: {
          title: 'Resource Review Detail',
          requiresAuth: true,
          roles: ['ADMIN'],
        },
      },
      {
        path: 'resources',
        name: 'AdminResourceList',
        component: AdminResourceList,
        meta: {
          title: 'All Resources',
          requiresAuth: true,
          roles: ['ADMIN'],
        },
      },
      {
        path: 'master-data',
        name: 'MasterDataManagement',
        component: MasterDataManagement,
        meta: {
          title: 'Master Data Management',
          requiresAuth: true,
          roles: ['ADMIN'],
        },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFound.vue'),
    meta: {
      title: 'Page Not Found',
    },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  },
})

router.beforeEach((to, from, next) => {
  document.title = to.meta.title
    ? `${to.meta.title} - Heritage Platform`
    : 'Heritage Platform'

  const token = getToken()
  const roles = getStoredRoles()

  if (to.meta.requiresAuth && !token) {
    return next({ name: 'Login' })
  }
  if (to.meta.roles && !roles.some((role) => to.meta.roles.includes(role))) {
    return next({ name: 'Login' })
  }

  next()
})

export default router
