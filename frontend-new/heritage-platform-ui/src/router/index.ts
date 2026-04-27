import { createRouter, createWebHistory } from 'vue-router'
import DiscoverView from '@/views/DiscoverView.vue'
import ResourceDetailView from '@/views/ResourceDetailView.vue'

export default createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    { path: '/', name: 'discover', component: DiscoverView },
    {
      path: '/resources/:id',
      name: 'resource-detail',
      component: ResourceDetailView,
      props: true,
    },
  ],
  scrollBehavior() {
    return { top: 0 }
  },
})