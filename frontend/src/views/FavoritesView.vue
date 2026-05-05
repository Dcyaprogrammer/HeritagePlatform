<template>
  <div class="favorites-page">
    <div class="page-header">
      <div class="header-content">
        <h1 class="page-title">My Favorites</h1>
        <p class="page-subtitle">Resources you've saved for later</p>
      </div>
      <RouterLink to="/" class="back-link">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M19 12H5M12 19l-7-7 7-7"/>
        </svg>
        Back to Discover
      </RouterLink>
    </div>

    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p>Loading your favorites...</p>
    </div>

    <div v-else-if="!isLoggedIn" class="auth-prompt">
      <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
        <path d="M17 3H7c-1.1 0-2 .9-2 2v16l7-3 7 3V5c0-1.1-.9-2-2-2z"/>
      </svg>
      <h2>Sign in to view your favorites</h2>
      <p>Save resources you love and access them anytime</p>
      <RouterLink to="/login" class="btn-primary">Sign In</RouterLink>
    </div>

    <div v-else-if="favorites.length === 0" class="empty-state">
      <svg width="64" height="64" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1">
        <path d="M17 3H7c-1.1 0-2 .9-2 2v16l7-3 7 3V5c0-1.1-.9-2-2-2z"/>
      </svg>
      <h2>No favorites yet</h2>
      <p>Start exploring and save resources you love</p>
      <RouterLink to="/" class="btn-primary">Discover Resources</RouterLink>
    </div>

    <div v-else class="favorites-grid">
      <HeritageCard v-for="item in favorites" :key="item.id" :item="item" />
    </div>

    <div v-if="hasMore && !loading" class="load-more">
      <button class="btn-secondary" @click="loadMore" :disabled="loadingMore">
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
  max-width: 1200px;
  margin: 0 auto;
  padding: 2rem 1.5rem 4rem;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 2rem;
  gap: 1rem;
  flex-wrap: wrap;
}

.page-title {
  margin: 0;
  font-family: var(--font-serif);
  font-size: 1.75rem;
  font-weight: 600;
  color: var(--ink);
}

.page-subtitle {
  margin: 0.35rem 0 0;
  font-size: 0.9375rem;
  color: var(--muted);
}

.back-link {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--accent);
  text-decoration: none;
  padding: 8px 14px;
  border-radius: 8px;
  background: var(--surface);
  border: 1px solid var(--border);
  transition: all 0.2s ease;
}

.back-link:hover {
  background: color-mix(in srgb, var(--surface) 95%, var(--accent) 5%);
  border-color: var(--accent-soft);
}

/* Loading state */
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

/* Auth prompt */
.auth-prompt {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 2rem;
  text-align: center;
  color: var(--muted);
}

.auth-prompt svg {
  opacity: 0.4;
  margin-bottom: 1rem;
}

.auth-prompt h2 {
  margin: 0 0 0.5rem;
  font-size: 1.25rem;
  color: var(--ink);
}

.auth-prompt p {
  margin: 0 0 1.5rem;
  font-size: 0.9375rem;
}

/* Empty state */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 4rem 2rem;
  text-align: center;
}

.empty-state svg {
  opacity: 0.3;
  margin-bottom: 1rem;
  color: var(--muted);
}

.empty-state h2 {
  margin: 0 0 0.5rem;
  font-size: 1.25rem;
  color: var(--ink);
}

.empty-state p {
  margin: 0 0 1.5rem;
  font-size: 0.9375rem;
  color: var(--muted);
}

/* Favorites grid */
.favorites-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 1.5rem;
}

/* Buttons */
.btn-primary {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0.625rem 1.25rem;
  background: var(--accent);
  color: #fff;
  font-size: 0.9375rem;
  font-weight: 500;
  text-decoration: none;
  border-radius: 8px;
  border: none;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.btn-primary:hover {
  background: var(--accent-soft);
}

.btn-secondary {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 0.625rem 1.5rem;
  background: var(--surface);
  color: var(--ink);
  font-size: 0.9375rem;
  font-weight: 500;
  border: 1px solid var(--border);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-secondary:hover:not(:disabled) {
  border-color: var(--accent-soft);
  color: var(--accent);
}

.btn-secondary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Load more */
.load-more {
  display: flex;
  justify-content: center;
  margin-top: 2rem;
}

/* Responsive */
@media (max-width: 640px) {
  .favorites-page {
    padding: 1.5rem 1rem 3rem;
  }

  .page-header {
    flex-direction: column;
    align-items: stretch;
  }

  .back-link {
    align-self: flex-start;
  }

  .favorites-grid {
    grid-template-columns: 1fr;
    gap: 1rem;
  }
}
</style>
