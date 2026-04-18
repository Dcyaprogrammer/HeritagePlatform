import { createRouter, createWebHistory } from 'vue-router'

import AdminLayout from '../views/admin/AdminLayout.vue'
import AdminUserList from '../views/admin/AdminUserList.vue'
import ContributorReview from '../views/admin/ContributorReview.vue'
import Login from '../views/auth/Login.vue'
import Register from '../views/auth/Register.vue'
import HomeView from '../views/HomeView.vue'
import ResourceDetailView from '../views/ResourceDetailView.vue'
import SubmissionsView from '../views/SubmissionsView.vue'
import FeedbackView from '../views/FeedbackView.vue'
import { getStoredRoles, getToken } from '../api/auth.js'

// Route configuration
const routes = [
  {
    path: '/',
    name: 'Home',
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
    meta: { title: 'Register' }
  },

  {
    path: '/resources/:id',
    name: 'ResourceDetail',
    component: ResourceDetailView,
    meta: { title: 'Resource Detail' }
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
          title: 'User Management'
        }
      },
      {
        path: 'review',
        name: 'ContributorReview',
        component: ContributorReview,
        meta: {
          title: 'Contributor Review'
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

// Create router instance
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
