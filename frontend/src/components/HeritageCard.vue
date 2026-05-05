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
  <article class="card">
    <!-- Image section - 60% height -->
    <RouterLink :to="detailHref" class="media-link" :aria-label="`View: ${item.title}`">
      <div class="media">
        <img v-if="item.coverUrl" :src="item.coverUrl" :alt="`${item.title} cover`" loading="lazy" />
        <div v-else class="placeholder" role="img" aria-label="No cover image">
          <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <rect x="3" y="3" width="18" height="18" rx="2"/>
            <circle cx="8.5" cy="8.5" r="1.5"/>
            <path d="M21 15l-5-5L5 21"/>
          </svg>
        </div>

        <!-- Gradient overlay -->
        <div class="gradient-overlay"></div>

        <!-- Video badge -->
        <span v-if="hasVideo" class="video-badge" aria-label="Contains video">
          <svg width="11" height="11" viewBox="0 0 24 24" fill="currentColor"><path d="M8 5v14l11-7z"/></svg>
          Video
        </span>
      </div>
    </RouterLink>

    <!-- Content section - 40% -->
    <div class="body">
      <!-- Tags -->
      <ul v-if="item.tags && item.tags.length" class="tags" aria-label="Tags">
        <li v-for="t in item.tags" :key="t.id" class="tag">{{ t.name }}</li>
      </ul>

      <!-- Title -->
      <RouterLink :to="detailHref" class="title-link">
        <h2 class="title">{{ item.title }}</h2>
      </RouterLink>

      <!-- Location -->
      <p v-if="item.locationName" class="location">
        <svg class="loc-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7z"/>
          <circle cx="12" cy="9" r="2.5"/>
        </svg>
        {{ item.locationName }}
      </p>

      <!-- Description -->
      <p v-if="item.description" class="description">{{ item.description }}</p>

      <!-- Footer: Stats + View link -->
      <div class="footer">
        <div class="stats">
          <span class="stat">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
            </svg>
            {{ commentCount }}
          </span>
          <span class="stat">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
            </svg>
            {{ likeCount }}
          </span>
          <span class="stat">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M17 3H7c-1.1 0-2 .9-2 2v16l7-3 7 3V5c0-1.1-.9-2-2-2z"/>
            </svg>
            {{ favoriteCount }}
          </span>
        </div>

        <RouterLink :to="detailHref" class="view-link">View Details</RouterLink>
      </div>
    </div>
  </article>
</template>

<style scoped>
/* ===== Card Container ===== */
.card {
  background: var(--surface);
  border-radius: 12px;
  overflow: hidden;
  border: 1px solid var(--border);
  height: 100%;
  display: flex;
  flex-direction: column;
  box-shadow: 0 1px 3px rgba(28, 25, 23, 0.06), 0 4px 12px rgba(28, 25, 23, 0.06);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(28, 25, 23, 0.08), 0 12px 32px rgba(28, 25, 23, 0.12);
}

/* ===== Media Section - 60% ===== */
.media-link {
  display: block;
  color: inherit;
  text-decoration: none;
}

.media-link:hover {
  text-decoration: none;
}

.media {
  position: relative;
  height: 220px;
  overflow: hidden;
  background: color-mix(in srgb, var(--bg) 60%, var(--surface) 40%);
}

.media img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.3s ease;
}

.card:hover .media img {
  transform: scale(1.04);
}

/* Gradient overlay for text readability on hover */
.gradient-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    to bottom,
    transparent 50%,
    rgba(28, 25, 23, 0.05) 100%
  );
  opacity: 0;
  transition: opacity 0.3s ease;
}

.card:hover .gradient-overlay {
  opacity: 1;
}

/* Placeholder */
.placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--muted);
  background: linear-gradient(
    145deg,
    color-mix(in srgb, var(--surface) 80%, var(--bg) 20%),
    color-mix(in srgb, var(--bg) 85%, var(--ink) 15%)
  );
}

.placeholder svg {
  opacity: 0.4;
}

/* Video badge */
.video-badge {
  position: absolute;
  bottom: 10px;
  right: 10px;
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  font-size: 0.72rem;
  font-weight: 600;
  background: rgba(220, 38, 38, 0.9);
  color: #fff;
  border-radius: 4px;
  backdrop-filter: blur(4px);
}

/* ===== Body Section - 40% ===== */
.body {
  flex: 1;
  height: 210px;
  padding: 16px 18px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  overflow: hidden;
}

/* ===== Tags ===== */
.tags {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.tag {
  font-size: 0.72rem;
  padding: 3px 10px;
  border-radius: 999px;
  background: color-mix(in srgb, var(--accent) 12%, var(--surface) 88%);
  color: var(--accent);
  font-weight: 500;
}

/* ===== Title ===== */
.title-link {
  color: inherit;
  text-decoration: none;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.title-link:hover .title {
  color: var(--accent);
}

.title {
  margin: 0;
  font-family: var(--font-serif);
  font-size: 1.15rem;
  font-weight: 600;
  line-height: 1.35;
  color: var(--ink);
  transition: color 0.15s ease;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* ===== Location ===== */
.location {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 0.82rem;
  color: var(--muted);
}

.loc-icon {
  flex-shrink: 0;
  opacity: 0.7;
}

/* ===== Description ===== */
.description {
  margin: 0;
  font-size: 0.82rem;
  line-height: 1.6;
  color: var(--muted);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* ===== Footer ===== */
.footer {
  margin-top: auto;
  padding-top: 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-top: 1px solid var(--border);
}

.stats {
  display: flex;
  align-items: center;
  gap: 12px;
}

.stat {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 0.78rem;
  color: var(--muted);
}

.stat svg {
  opacity: 0.7;
}

.view-link {
  font-size: 0.8rem;
  font-weight: 600;
  color: var(--accent);
  text-decoration: none;
  transition: opacity 0.15s ease;
}

.view-link:hover {
  opacity: 0.8;
  text-decoration: none;
}

/* ===== Responsive ===== */
@media (max-width: 480px) {
  .media {
    height: 180px;
  }

  .title {
    font-size: 1.05rem;
  }

  .description {
    -webkit-line-clamp: 2;
  }
}
</style>
