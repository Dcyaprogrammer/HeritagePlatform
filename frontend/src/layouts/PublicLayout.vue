<template>
  <div class="public-page">
    <header class="public-header">
      <div class="public-header-inner public-shell">
        <RouterLink to="/" class="public-brand">
          <h1 class="public-title">Heritage Resource Hall</h1>
        </RouterLink>
        <div class="public-actions">
          <template v-if="isLoggedIn">
            <button
              v-if="isContributor"
              type="button"
              class="public-btn public-btn--primary"
              @click="goToCreateResource"
            >
              + Create Draft
            </button>
            <button
              v-if="isContributor"
              type="button"
              class="public-btn"
              @click="goToSubmissions"
            >
              My Submissions
            </button>
            <button type="button" class="public-btn public-btn--ghost" @click="goToFavorites">My Favorites</button>
            <button
              v-if="isAdmin"
              type="button"
              class="public-btn"
              @click="goToAdmin"
            >
              Admin Panel
            </button>
            <button type="button" class="public-btn public-btn--ghost" @click="goToProfile">Profile</button>
            <button type="button" class="public-btn public-btn--ghost" @click="handleLogout">Logout</button>
          </template>
          <template v-else>
            <button type="button" class="public-btn public-btn--ghost" @click="goToLogin">Login</button>
            <button type="button" class="public-btn public-btn--primary" @click="goToRegister">
              Register
            </button>
          </template>
        </div>
      </div>
    </header>
    <main class="public-shell public-main" :class="shellClass">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { usePublicHeader } from '../composables/usePublicHeader.js'

const router = useRouter()
const route = useRoute()
const {
  isLoggedIn,
  isAdmin,
  isContributor,
  refreshAuth,
  goToAdmin,
  goToCreateResource,
  goToSubmissions,
  goToFavorites,
  goToProfile,
  goToLogin,
  goToRegister,
  handleLogout,
} = usePublicHeader()

const shellClass = computed(() => {
  return route.meta?.shellWidth === 'wide' ? 'public-shell--wide' : ''
})

onMounted(() => {
  refreshAuth()
})

const stopAfterEach = router.afterEach(() => {
  refreshAuth()
})

onUnmounted(() => {
  stopAfterEach()
})
</script>

<style scoped>
.public-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
.public-header {
  position: relative;
  z-index: 1;
  border-bottom: 1px solid var(--border);
  background: color-mix(in srgb, var(--surface) 92%, white 8%);
  box-shadow: 0 1px 0 rgba(255, 255, 255, 0.6);
}
.public-header::before {
  content: '';
  position: absolute;
  inset: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100vw;
  background: color-mix(in srgb, var(--surface) 92%, white 8%);
  z-index: -1;
}
.public-header-inner {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 1rem;
  padding: 1.25rem 0;
  flex-wrap: wrap;
}
.public-brand {
  color: inherit;
  text-decoration: none;
}
.public-brand:hover {
  text-decoration: none;
}
.public-title {
  margin: 0;
  font-family: var(--font-serif);
  font-size: clamp(1.18rem, 2.5vw, 1.55rem);
  font-weight: 700;
  color: var(--ink);
  letter-spacing: 0.01em;
}
.public-actions {
  display: flex;
  flex-wrap: wrap;
  gap: 0.55rem;
  align-items: center;
}
.public-main {
  flex: 1;
  min-height: 0;
  display: flex;
  flex-direction: column;
  padding-bottom: 2rem;
}

.public-shell--wide {
  max-width: 1480px;
}
</style>
