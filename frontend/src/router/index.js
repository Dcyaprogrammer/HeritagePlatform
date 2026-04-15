import { createRouter, createWebHistory } from 'vue-router'

import AdminLayout from '../views/admin/AdminLayout.vue'
import AdminUserList from '../views/admin/AdminUserList.vue'
import ContributorReview from '../views/admin/ContributorReview.vue'
import Login from '../views/auth/Login.vue'
import Register from '../views/auth/Register.vue'

// Route configuration
const routes = [
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

  // Redirect root to admin users page
  {
    path: '/',
    redirect: '/admin/users'
  },

  // Admin routes with nested layout
  {
    path: '/admin',
    component: AdminLayout,
    meta: {
      title: 'Administration'
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
  // Set page title
  document.title = to.meta.title
    ? `${to.meta.title} - Heritage Platform`
    : 'Heritage Platform'

  // TODO: Add authentication check after JWT is implemented
  // For now, allow all access during development

  next()
})

router.afterEach((to, from) => {
  console.log(`Navigated from ${from.path} to ${to.path}`)
})

export default router
