import { createRouter, createWebHistory } from 'vue-router'
import HomeView from './views/HomeView.vue'
import ResourceDetailView from './views/ResourceDetailView.vue'

const routes = [
  { path: '/', name: 'home', component: HomeView },
  { path: '/resources/:id', name: 'resourceDetail', component: ResourceDetailView },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
