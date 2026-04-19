import { createRouter, createWebHistory } from 'vue-router'

import AdminLayout from '../views/admin/AdminLayout.vue'
import AdminUserList from '../views/admin/AdminUserList.vue'
import ContributorReview from '../views/admin/ContributorReview.vue'
import HomeView from '../views/HomeView.vue'
import Login from '../views/auth/Login.vue'
import Profile from '../views/Profile.vue'
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

  // Home page for viewers
  {
    path: '/home',
    name: 'Home',
    component: HomeView,
    meta: { title: 'Home', requiresAuth: true }
  },

  // Profile page
  {
    path: '/profile',
    name: 'Profile',
    component: Profile,
    meta: { title: 'Profile', requiresAuth: true }
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

  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')

  if (to.meta.requiresAuth && !token) {
    return next({ name: 'Login' })
  }
  if (to.meta.roles && !to.meta.roles.includes(role)) {
    return next({ name: 'Login' })
  }

  next()
})

router.afterEach((to, from) => {
  console.log(`Navigated from ${from.path} to ${to.path}`)
})

export default router
