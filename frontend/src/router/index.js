import { createRouter, createWebHistory } from 'vue-router'
import ReviewCenter from '../views/ReviewCenter.vue'
import ReviewDetail from '../views/ReviewDetail.vue'

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/review' },
    { path: '/review', component: ReviewCenter },
    { path: '/review/:id', component: ReviewDetail },
  ],
})