import { createRouter, createWebHistory } from 'vue-router'

import AdminLayout from '../views/admin/AdminLayout.vue'
import AdminUserList from '../views/admin/AdminUserList.vue'
import ContributorReview from '../views/admin/ContributorReview.vue'
import ResourceReviewCenter from '../views/admin/ResourceReviewCenter.vue'
import ResourceReviewDetail from '../views/admin/ResourceReviewDetail.vue'
import AdminResourceList from '../views/admin/AdminResourceList.vue'
import MasterDataManagement from '../views/admin/MasterDataManagement.vue'
import HomeView from '../views/HomeView.vue'
import Login from '../views/auth/Login.vue'
import Profile from '../views/Profile.vue'
import Register from '../views/auth/Register.vue'
import ResourceDetailView from '../views/ResourceDetailView.vue'
import SubmissionsView from '../views/SubmissionsView.vue'
import FeedbackView from '../views/FeedbackView.vue'
import CreateResource from '../views/CreateResource.vue'
import { getStoredRoles, getToken } from '../api/auth.js'

// Route configuration
const routes = [
  {
    path: '/',
    name: 'Landing',
    component: HomeView,
    meta: { title: 'Heritage Resource Hall' }
  },
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { title: 'Login' }
  },
  {
    path: '/register',
    name: 'Register',
    component: Register,
    meta:{ title: 'Register' }
  },
  // Home page for viewers
  {
    path: '/home',
    redirect: '/'
  },

  // Profile page
  {
    path: '/profile',
    name: 'Profile',
    component: Profile,
    meta: { title: 'Profile', requiresAuth: true }
  },
  
  {
    path: '/resources/:id',
    name: 'ResourceDetail',
    component: ResourceDetailView,
    meta: { title: 'Resource Detail' }
  },

  {
    path: '/resources/create',
    name: 'CreateResource',
    component: CreateResource,
    meta: {
      title: 'Create Resource',
      requiresAuth: true,
      roles: ['CONTRIBUTOR', 'ADMIN']
    }
  },
  {
    path: '/resources/:id/edit',
    name: 'EditResource',
    component: CreateResource,
    meta: {
      title: 'Edit Resource',
      requiresAuth: true,
      roles: ['CONTRIBUTOR', 'ADMIN']
    }
  },

  {
    path: '/resources/submissions',
    name: 'Submissions',
    component: SubmissionsView,
    meta: {
      title: 'My Submissions',
      requiresAuth: true
    }
  },
  {
    path: '/resources/:id/feedback',
    name: 'Feedback',
    component: FeedbackView,
    meta: {
      title: 'Feedback Detail',
      requiresAuth: true
    }
  },
  // Redirect root to home page
  {
    path: '/',
    redirect: '/home'

  },

  // Admin routes with nested layout
  {
    path: '/admin',
    component: AdminLayout,
    meta: {
      title: 'Administration',
      requiresAuth: true,
      roles: ['ADMIN']
    },
    children: [
      {
        path: '',
        redirect: '/admin/users'
      },
      {
        path: 'users',
        name: 'AdminUserList',
        component: AdminUserList,
        meta: {
          title: 'User Management',
          requiresAuth: true,
          roles: ['ADMIN']
        }
      },
      {
        path: 'review',
        name: 'ContributorReview',
        component: ContributorReview,
        meta: {
          title: 'Contributor Review',
          requiresAuth: true,
          roles: ['ADMIN']
        }
      },
      {
        path: 'resource-review',
        name: 'ResourceReviewCenter',
        component: ResourceReviewCenter,
        meta: {
          title: 'Resource Review',
          requiresAuth: true,
          roles: ['ADMIN']
        }
      },
      {
        path: 'resource-review/:id',
        name: 'ResourceReviewDetail',
        component: ResourceReviewDetail,
        meta: {
          title: 'Resource Review Detail',
          requiresAuth: true,
          roles: ['ADMIN']
        }
      },
      {
        path: 'resources',
        name: 'AdminResourceList',
        component: AdminResourceList,
        meta: {
          title: 'All Resources',
          requiresAuth: true,
          roles: ['ADMIN']
        }
      },
      {
        path: 'master-data',
        name: 'MasterDataManagement',
        component: MasterDataManagement,
        meta: {
          title: 'Master Data Management',
          requiresAuth: true,
          roles: ['ADMIN']
        }
      }
    ]
  },

  // 404 Not Found page
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFound.vue'),
    meta: {
      title: 'Page Not Found'
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

// Navigation guards
router.beforeEach((to, from, next) => {
  document.title = to.meta.title
    ? `${to.meta.title} - Heritage Platform`
    : 'Heritage Platform'

  const token = getToken()
  const roles = getStoredRoles()

  if (to.meta.requiresAuth && !token) {
    return next({ name: 'Login' })
  }
  if (to.meta.roles && !roles.some(role => to.meta.roles.includes(role))) {
    return next({ name: 'Login' })
  }

  next()
})

router.afterEach((to, from) => {
  console.log(`Navigated from ${from.path} to ${to.path}`)
})

export default router
