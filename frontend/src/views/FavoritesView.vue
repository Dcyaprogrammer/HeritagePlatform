<template>
  <div class="favorites-page">
    <section class="favorites-hero public-panel">
      <div class="favorites-hero__copy">
        <p class="public-eyebrow">Saved Collection</p>
        <div class="page-header">
          <div class="header-content">
            <h1 class="page-title">My Favorites</h1>
          </div>
          <RouterLink to="/" class="public-btn public-btn--primary back-link">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M19 12H5M12 19l-7-7 7-7"/>
            </svg>
            Back to Discover
          </RouterLink>
        </div>
      </div>

      <div class="favorites-hero__aside">
        <div class="shelf-card">
          <span class="shelf-card__label">Saved items</span>
          <strong class="shelf-card__value">{{ favorites.length }}</strong>
        </div>
      </div>
    </section>

    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p>Loading your favorites...</p>
    </div>

    <div v-else-if="!isLoggedIn" class="auth-prompt public-panel public-panel--soft">
      <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
        <path d="M17 3H7c-1.1 0-2 .9-2 2v16l7-3 7 3V5c0-1.1-.9-2-2-2z"/>
      </svg>
      <h2>Sign in to view your favorites</h2>
      <RouterLink to="/login" class="public-btn public-btn--primary">Sign In</RouterLink>
    </div>

    <div v-else-if="favorites.length === 0" class="empty-state public-panel public-panel--soft">
      <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1">
        <path d="M17 3H7c-1.1 0-2 .9-2 2v16l7-3 7 3V5c0-1.1-.9-2-2-2z"/>
      </svg>
      <h2>No favorites yet</h2>
      <RouterLink to="/" class="public-btn public-btn--primary">Discover Resources</RouterLink>
    </div>

    <section v-else class="favorites-shelf">
      <div class="favorites-grid">
        <HeritageCard v-for="item in favorites" :key="item.id" :item="item" />
      </div>
    </section>

    <div v-if="hasMore && !loading" class="load-more">
      <button class="public-btn" @click="loadMore" :disabled="loadingMore">
        {{ loadingMore ? 'Loading...' : 'Load more' }}
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { RouterLink } from 'vue-router'
import HeritageCard from '../components/HeritageCard.vue'
import { getMyFavorites } from '../api/resource.js'
import { getToken } from '../api/auth.js'

const favorites = ref([])
const loading = ref(true)
const loadingMore = ref(false)
const page = ref(0)
const pageSize = 12
const hasMore = ref(true)

const isLoggedIn = computed(() => !!getToken())

async function loadFavorites(reset = false) {
  if (reset) {
    page.value = 0
    favorites.value = []
    hasMore.value = true
  }

  if (!hasMore.value) return

  if (reset) {
    loading.value = true
  } else {
    loadingMore.value = true
  }

  try {
    const res = await getMyFavorites({ page: page.value, size: pageSize })
    if (res.data.code === 200) {
      const data = res.data.data
      if (reset) {
        favorites.value = data.items || []
      } else {
        favorites.value = [...favorites.value, ...(data.items || [])]
      }
      hasMore.value = (page.value + 1) * pageSize < data.total
      page.value++
    }
  } catch (error) {
    console.error('Failed to load favorites:', error)
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

function loadMore() {
  loadFavorites(false)
}

onMounted(() => {
  loadFavorites(true)
})
</script>

<style scoped>
.favorites-page {
  width: 100%;
  padding: 1.25rem 0 4rem;
}

.favorites-hero {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 240px;
  gap: 1.2rem;
  align-items: stretch;
  padding: 1.15rem 1.2rem;
  margin-bottom: 1.35rem;
}

.favorites-hero__copy,
.favorites-hero__aside {
  min-width: 0;
}

.page-header {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  align-items: start;
  margin-bottom: 0;
  gap: 1rem;
}

.page-title {
  margin: 0;
  font-family: var(--font-serif);
  font-size: clamp(1.9rem, 2.8vw, 2.55rem);
  font-weight: 700;
  color: var(--ink);
}

.back-link {
  display: inline-flex;
  align-items: center;
  justify-self: end;
  gap: 6px;
  font-size: 0.875rem;
  font-weight: 700;
  color: #fff;
  text-decoration: none;
}

.shelf-card {
  min-height: 100%;
  padding: 1rem 1.05rem;
  border-radius: 20px;
  border: 1px solid color-mix(in srgb, var(--border-strong) 58%, white 42%);
  background: linear-gradient(180deg, color-mix(in srgb, var(--surface-raised) 92%, white 8%), var(--surface));
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
}

.shelf-card__label {
  color: var(--muted);
  font-size: 0.72rem;
  font-weight: 800;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.shelf-card__value {
  margin-top: 0.55rem;
  color: var(--ink);
  font-family: var(--font-serif);
  font-size: 2.4rem;
  font-weight: 700;
  line-height: 1;
}

.loading-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 2rem;
  gap: 1rem;
  color: var(--muted);
}

.spinner {
  width: 32px;
  height: 32px;
  border: 3px solid var(--border);
  border-top-color: var(--accent);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.auth-prompt,
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 2rem;
  text-align: center;
}

.auth-prompt {
  color: var(--muted);
}

.auth-prompt svg,
.empty-state svg {
  opacity: 0.35;
  margin-bottom: 1rem;
  color: var(--muted);
}

.auth-prompt h2,
.empty-state h2 {
  margin: 0 0 1.5rem;
  font-size: 1.25rem;
  color: var(--ink);
}

.favorites-shelf {
  width: 100%;
}

.favorites-grid {
  display: grid;
  width: 100%;
  grid-template-columns: repeat(3, minmax(0, 340px));
  gap: 1.5rem;
  justify-content: space-between;
}

.load-more {
  display: flex;
  justify-content: center;
  margin-top: 2rem;
}

@media (max-width: 1120px) {
  .favorites-grid {
    grid-template-columns: repeat(2, minmax(0, 340px));
    justify-content: start;
  }
}

@media (max-width: 920px) {
  .favorites-hero {
    grid-template-columns: 1fr;
  }

  .page-header {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .favorites-page {
    padding: 0.9rem 0 3rem;
  }

  .page-header {
    grid-template-columns: 1fr;
  }

  .favorites-grid {
    grid-template-columns: 1fr;
    gap: 1rem;
  }
}
</style>
