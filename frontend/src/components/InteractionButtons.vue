<script setup>
import { ref, computed, onMounted } from 'vue'
import { getInteractions, toggleLike, toggleFavorite } from '../api/resource.js'

const props = defineProps({
  resourceId: {
    type: Number,
    required: true
  },
  initialLikeCount: {
    type: Number,
    default: 0
  },
  initialFavoriteCount: {
    type: Number,
    default: 0
  },
  initialLiked: {
    type: Boolean,
    default: false
  },
  initialFavorited: {
    type: Boolean,
    default: false
  },
  isLoggedIn: {
    type: Boolean,
    default: false
  }
})

const likeCount = ref(props.initialLikeCount)
const favoriteCount = ref(props.initialFavoriteCount)
const liked = ref(props.initialLiked)
const favorited = ref(props.initialFavorited)
const likeLoading = ref(false)
const favoriteLoading = ref(false)

const likeLabel = computed(() => {
  if (likeCount.value === 0) return 'Like'
  if (likeCount.value === 1) return '1 like'
  return `${likeCount.value} likes`
})

const favoriteLabel = computed(() => {
  if (favoriteCount.value === 0) return 'Save'
  if (favoriteCount.value === 1) return '1 save'
  return `${favoriteCount.value} saves`
})

async function handleLike() {
  if (!props.isLoggedIn) {
    alert('Please sign in to like this resource.')
    return
  }
  if (likeLoading.value) return
  likeLoading.value = true

  // Optimistic update
  const wasLiked = liked.value
  liked.value = !liked.value
  likeCount.value += liked.value ? 1 : -1

  try {
    const res = await toggleLike(props.resourceId)
    if (res.data.code === 200) {
      likeCount.value = res.data.data.likeCount
      liked.value = res.data.data.liked
    } else {
      // Revert on error
      liked.value = wasLiked
      likeCount.value += wasLiked ? -1 : 1
    }
  } catch (error) {
    console.error('Failed to toggle like:', error)
    liked.value = wasLiked
    likeCount.value += wasLiked ? -1 : 1
  } finally {
    likeLoading.value = false
  }
}

async function handleFavorite() {
  if (!props.isLoggedIn) {
    alert('Please sign in to save this resource.')
    return
  }
  if (favoriteLoading.value) return
  favoriteLoading.value = true

  // Optimistic update
  const wasFavorited = favorited.value
  favorited.value = !favorited.value
  favoriteCount.value += favorited.value ? 1 : -1

  try {
    const res = await toggleFavorite(props.resourceId)
    if (res.data.code === 200) {
      favoriteCount.value = res.data.data.favoriteCount
      favorited.value = res.data.data.favorited
    } else {
      // Revert on error
      favorited.value = wasFavorited
      favoriteCount.value += wasFavorited ? -1 : 1
    }
  } catch (error) {
    console.error('Failed to toggle favorite:', error)
    favorited.value = wasFavorited
    favoriteCount.value += wasFavorited ? -1 : 1
  } finally {
    favoriteLoading.value = false
  }
}

onMounted(async () => {
  if (props.isLoggedIn) {
    try {
      const res = await getInteractions(props.resourceId)
      if (res.data.code === 200) {
        const data = res.data.data
        if (data.likeCount !== undefined) likeCount.value = data.likeCount
        if (data.favoriteCount !== undefined) favoriteCount.value = data.favoriteCount
        if (data.liked !== undefined) liked.value = data.liked
        if (data.favorited !== undefined) favorited.value = data.favorited
      }
    } catch (error) {
      console.error('Failed to fetch interactions:', error)
    }
  }
})
</script>

<template>
  <div class="interaction-bar">
    <!-- Like button -->
    <button
      class="interaction-btn"
      :class="{ active: liked, loading: likeLoading }"
      @click="handleLike"
      :aria-label="liked ? 'Unlike' : 'Like'"
      :aria-pressed="liked"
    >
      <svg v-if="liked" class="icon" width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
        <path d="M12 21.35l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.54L12 21.35z"/>
      </svg>
      <svg v-else class="icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
      </svg>
      <span class="label">{{ likeLabel }}</span>
    </button>

    <!-- Save button -->
    <button
      class="interaction-btn"
      :class="{ active: favorited, loading: favoriteLoading }"
      @click="handleFavorite"
      :aria-label="favorited ? 'Remove from saved' : 'Save'"
      :aria-pressed="favorited"
    >
      <svg v-if="favorited" class="icon" width="18" height="18" viewBox="0 0 24 24" fill="currentColor">
        <path d="M17 3H7c-1.1 0-2 .9-2 2v16l7-3 7 3V5c0-1.1-.9-2-2-2z"/>
      </svg>
      <svg v-else class="icon" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M17 3H7c-1.1 0-2 .9-2 2v16l7-3 7 3V5c0-1.1-.9-2-2-2z"/>
      </svg>
      <span class="label">{{ favoriteLabel }}</span>
    </button>
  </div>
</template>

<style scoped>
.interaction-bar {
  display: flex;
  align-items: center;
  gap: 1rem;
}

.interaction-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: var(--surface);
  color: var(--muted);
  font-size: 0.875rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease;
}

.interaction-btn:hover:not(.loading) {
  border-color: var(--accent-soft);
  color: var(--accent);
  background: color-mix(in srgb, var(--surface) 95%, var(--accent) 5%);
}

.interaction-btn.active {
  border-color: var(--accent);
  color: var(--accent);
  background: color-mix(in srgb, var(--accent) 10%, var(--surface) 90%);
}

.interaction-btn.active:hover:not(.loading) {
  background: color-mix(in srgb, var(--accent) 15%, var(--surface) 85%);
}

.interaction-btn.loading {
  opacity: 0.7;
  cursor: wait;
}

.interaction-btn .icon {
  flex-shrink: 0;
  transition: transform 0.2s ease;
}

.interaction-btn:hover:not(.loading) .icon {
  transform: scale(1.1);
}

.interaction-btn.active .icon {
  transform: scale(1.05);
}

.interaction-btn .label {
  white-space: nowrap;
}

@media (max-width: 480px) {
  .interaction-btn {
    padding: 6px 10px;
    font-size: 0.8rem;
  }

  .interaction-btn .icon {
    width: 16px;
    height: 16px;
  }
}
</style>
