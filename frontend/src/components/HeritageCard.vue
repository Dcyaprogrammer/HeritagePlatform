<script setup>
import { computed } from 'vue'
import { RouterLink } from 'vue-router'

const props = defineProps({
  item: Object
})

const detailHref = computed(() => `/resources/${props.item.id}`)
const hasVideo = computed(() => props.item?.hasVideo || false)
const commentCount = computed(() => props.item?.commentCount || props.item?.comment_count || 0)
const likeCount = computed(() => props.item?.likeCount || props.item?.like_count || 0)
const favoriteCount = computed(() => props.item?.favoriteCount || props.item?.favorite_count || 0)
</script>

<template>
  <article class="public-card heritage-card">
    <RouterLink :to="detailHref" class="public-card-media-link" :aria-label="`View: ${item.title}`">
      <div class="public-card-media heritage-card__media">
        <img
          v-if="item.coverUrl"
          :src="item.coverUrl"
          :alt="`${item.title} cover`"
          loading="lazy"
          class="public-card-image"
        />
        <div v-else class="public-card-placeholder" role="img" aria-label="No cover image">
          <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <rect x="3" y="3" width="18" height="18" rx="2"/>
            <circle cx="8.5" cy="8.5" r="1.5"/>
            <path d="M21 15l-5-5L5 21"/>
          </svg>
        </div>

        <div class="gradient-overlay" />

        <span v-if="hasVideo" class="public-badge public-badge--danger heritage-card__badge" aria-label="Contains video">
          <svg width="11" height="11" viewBox="0 0 24 24" fill="currentColor"><path d="M8 5v14l11-7z"/></svg>
          Video
        </span>
      </div>
    </RouterLink>

    <div class="public-card-body heritage-card__body">
      <ul v-if="item.tags && item.tags.length" class="public-card-tags" aria-label="Tags">
        <li v-for="t in item.tags" :key="t.id" class="public-tag">{{ t.name }}</li>
      </ul>

      <RouterLink :to="detailHref" class="public-card-title-link">
        <h2 class="public-card-title heritage-card__title">{{ item.title }}</h2>
      </RouterLink>

      <p v-if="item.locationName" class="public-card-meta heritage-card__location">
        <svg class="loc-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7z"/>
          <circle cx="12" cy="9" r="2.5"/>
        </svg>
        {{ item.locationName }}
      </p>

      <p v-if="item.description" class="public-card-description heritage-card__description">
        {{ item.description }}
      </p>

      <div class="public-card-footer">
        <div class="public-card-stats">
          <span class="public-stat">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
            </svg>
            {{ commentCount }}
          </span>
          <span class="public-stat">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
            </svg>
            {{ likeCount }}
          </span>
          <span class="public-stat">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M17 3H7c-1.1 0-2 .9-2 2v16l7-3 7 3V5c0-1.1-.9-2-2-2z"/>
            </svg>
            {{ favoriteCount }}
          </span>
        </div>

        <RouterLink :to="detailHref" class="public-link-inline">
          View Details
        </RouterLink>
      </div>
    </div>
  </article>
</template>

<style scoped>
.heritage-card {
  height: 100%;
}

.heritage-card__media {
  height: 220px;
}

.gradient-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    to bottom,
    transparent 45%,
    rgba(28, 25, 23, 0.06) 100%
  );
  opacity: 0;
  transition: opacity 0.3s ease;
}

.heritage-card:hover .gradient-overlay {
  opacity: 1;
}

.heritage-card__badge {
  position: absolute;
  top: 0.8rem;
  right: 0.8rem;
  backdrop-filter: blur(4px);
}

.heritage-card__body {
  min-height: 220px;
}

.heritage-card__title,
.heritage-card__description {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.heritage-card__title {
  -webkit-line-clamp: 2;
}

.heritage-card__location {
  display: flex;
  align-items: center;
  gap: 0.4rem;
}

.loc-icon {
  flex-shrink: 0;
  opacity: 0.78;
}

.heritage-card__description {
  -webkit-line-clamp: 4;
}

@media (max-width: 480px) {
  .heritage-card__media {
    height: 190px;
  }

  .heritage-card__description {
    -webkit-line-clamp: 3;
  }
}
</style>
