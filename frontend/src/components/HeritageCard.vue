<script setup>
import { computed } from 'vue'
import { RouterLink } from 'vue-router'

const props = defineProps({
  item: Object
})

const detailHref = computed(() => `/resources/${props.item.id}`)
const hasCover = computed(() => Boolean(props.item?.coverUrl))
const hasVideo = computed(() => props.item?.hasVideo || false)
const commentCount = computed(() => props.item?.commentCount || props.item?.comment_count || 0)
const likeCount = computed(() => props.item?.likeCount || props.item?.like_count || 0)
const favoriteCount = computed(() => props.item?.favoriteCount || props.item?.favorite_count || 0)
const categoryName = computed(() => props.item?.categoryName || props.item?.category?.name || 'Heritage Resource')
const locationName = computed(() => props.item?.locationName?.trim() || 'Location pending')
const descriptionText = computed(() => props.item?.description?.trim() || 'No summary available yet.')
const allTags = computed(() => Array.isArray(props.item?.tags) ? props.item.tags : [])
const visibleTags = computed(() => allTags.value.slice(0, 2))
const hiddenTagCount = computed(() => Math.max(0, allTags.value.length - visibleTags.value.length))
const placeholderMark = computed(() => {
  const title = props.item?.title?.trim()
  return title ? title.charAt(0).toUpperCase() : 'H'
})
</script>

<template>
  <article class="public-card heritage-card">
    <RouterLink :to="detailHref" class="public-card-media-link" :aria-label="`View: ${item.title}`">
      <div class="public-card-media heritage-card__media" :class="{ 'heritage-card__media--no-cover': !hasCover }">
        <img
          v-if="hasCover"
          :src="item.coverUrl"
          :alt="`${item.title} cover`"
          loading="lazy"
          class="public-card-image"
        />
        <div v-else class="public-card-placeholder heritage-card__placeholder" role="img" aria-label="No cover image">
          <div class="heritage-card__placeholder-inner">
            <span class="heritage-card__placeholder-kicker">Archive Resource</span>
            <strong class="heritage-card__placeholder-title">{{ categoryName }}</strong>
            <p class="heritage-card__placeholder-meta">{{ locationName }}</p>
            <span class="heritage-card__placeholder-mark" aria-hidden="true">{{ placeholderMark }}</span>
          </div>
        </div>

        <div class="gradient-overlay" />

        <span v-if="hasVideo" class="public-badge public-badge--danger heritage-card__badge" aria-label="Contains video">
          <svg width="11" height="11" viewBox="0 0 24 24" fill="currentColor"><path d="M8 5v14l11-7z"/></svg>
          Video
        </span>
      </div>
    </RouterLink>

    <div class="public-card-body heritage-card__body">
      <div class="heritage-card__taxonomy">
        <div class="heritage-card__category-block" aria-label="Category">
          <span class="heritage-card__field-label">Category</span>
          <span class="heritage-card__category">{{ categoryName }}</span>
        </div>
      </div>

      <RouterLink :to="detailHref" class="public-card-title-link">
        <h2 class="public-card-title heritage-card__title">{{ item.title }}</h2>
      </RouterLink>

      <p class="public-card-meta heritage-card__location" :class="{ 'heritage-card__location--fallback': !item.locationName }">
        <svg class="loc-icon" width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M12 2C8.13 2 5 5.13 5 9c0 5.25 7 13 7 13s7-7.75 7-13c0-3.87-3.13-7-7-7z"/>
          <circle cx="12" cy="9" r="2.5"/>
        </svg>
        {{ locationName }}
      </p>

      <p class="public-card-description heritage-card__description" :class="{ 'heritage-card__description--fallback': !item.description }">
        {{ descriptionText }}
      </p>

      <div class="heritage-card__tag-section">
        <span class="heritage-card__field-label">Tags</span>
        <ul class="public-card-tags heritage-card__tags" aria-label="Tags">
          <li v-for="t in visibleTags" :key="t.id" class="public-tag heritage-card__tag">{{ t.name }}</li>
          <li v-if="hiddenTagCount" class="heritage-card__more-tag">+{{ hiddenTagCount }}</li>
          <li v-if="!visibleTags.length" class="heritage-card__empty-tag">Unlabeled</li>
        </ul>
      </div>

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
  width: 100%;
  max-width: 340px;
}

.heritage-card__media {
  height: 184px;
}

.heritage-card__media--no-cover {
  background:
    radial-gradient(circle at top right, color-mix(in srgb, var(--accent) 8%, transparent), transparent 32%),
    linear-gradient(145deg, color-mix(in srgb, var(--surface) 84%, white 16%), color-mix(in srgb, var(--bg-accent) 72%, white 28%));
}

.gradient-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    to bottom,
    transparent 40%,
    rgba(28, 25, 23, 0.08) 100%
  );
  opacity: 0;
  transition: opacity 0.3s ease;
}

.heritage-card:hover .gradient-overlay {
  opacity: 1;
}

.heritage-card__placeholder {
  padding: 1rem 1.1rem;
  align-items: stretch;
  justify-content: stretch;
}

.heritage-card__placeholder-inner {
  position: relative;
  display: flex;
  height: 100%;
  flex-direction: column;
  justify-content: flex-end;
  border: 1px solid color-mix(in srgb, var(--border-strong) 68%, white 32%);
  border-radius: 16px;
  padding: 1rem;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.52), rgba(255, 255, 255, 0.78)),
    repeating-linear-gradient(-18deg, transparent, transparent 12px, rgba(122, 50, 39, 0.04) 12px, rgba(122, 50, 39, 0.04) 13px);
}

.heritage-card__placeholder-kicker {
  margin-bottom: 0.45rem;
  color: var(--accent-soft);
  font-size: var(--text-2xs);
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.heritage-card__placeholder-title {
  max-width: 70%;
  color: var(--ink);
  font-family: var(--font-serif);
  font-size: 1.05rem;
  font-weight: 700;
  line-height: 1.25;
}

.heritage-card__placeholder-meta {
  margin: 0.45rem 0 0;
  max-width: 70%;
  color: var(--ink-soft);
  font-size: var(--text-xs);
  font-weight: 600;
  line-height: 1.45;
}

.heritage-card__placeholder-mark {
  position: absolute;
  right: 0.9rem;
  bottom: 0.85rem;
  color: color-mix(in srgb, var(--accent) 22%, var(--border-strong));
  font-family: var(--font-serif);
  font-size: 3rem;
  font-weight: 700;
  line-height: 1;
}

.heritage-card__badge {
  position: absolute;
  top: 0.8rem;
  right: 0.8rem;
  backdrop-filter: blur(4px);
}

.heritage-card__body {
  display: grid;
  grid-template-rows: minmax(3.2rem, auto) minmax(3.25rem, auto) 1.5rem 5rem 2.25rem auto;
  min-height: 188px;
  gap: 0.72rem;
}

.heritage-card__taxonomy {
  display: flex;
  align-items: flex-start;
  min-height: 3.2rem;
  width: 100%;
}

.heritage-card__category-block,
.heritage-card__tag-section {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 0.35rem;
}

.heritage-card__field-label {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  color: var(--muted);
  font-size: 0.67rem;
  font-weight: 800;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.heritage-card__field-label::before {
  content: "";
  width: 0.45rem;
  height: 0.45rem;
  border-radius: 999px;
  background: color-mix(in srgb, var(--accent) 40%, white 60%);
  box-shadow: 0 0 0 1px color-mix(in srgb, var(--accent) 20%, var(--border));
}

.heritage-card__category {
  display: inline-flex;
  align-items: center;
  min-height: 30px;
  max-width: 100%;
  padding: 0.2rem 0.78rem 0.22rem;
  border-radius: 10px;
  background: linear-gradient(
    180deg,
    color-mix(in srgb, var(--accent) 12%, var(--surface-raised)),
    color-mix(in srgb, var(--accent) 4%, white 96%)
  );
  border: 1px solid color-mix(in srgb, var(--accent) 28%, var(--border));
  color: var(--accent-strong);
  font-size: 0.82rem;
  font-weight: 700;
  line-height: 1.3;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.5);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.heritage-card__title,
.heritage-card__description {
  display: -webkit-box;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.heritage-card__title {
  -webkit-line-clamp: 2;
  min-height: 3.25rem;
}

.heritage-card__location {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  min-height: 1.4rem;
  width: 100%;
}

.heritage-card__location--fallback {
  color: var(--muted);
}

.loc-icon {
  flex-shrink: 0;
  opacity: 0.78;
}

.heritage-card__description {
  -webkit-line-clamp: 3;
  min-height: 4.95rem;
  width: 100%;
}

.heritage-card__description--fallback {
  color: var(--muted-soft);
  font-style: italic;
}

.heritage-card__tags {
  margin-top: 0;
  min-height: 1.8rem;
  width: 100%;
  overflow: hidden;
}

.heritage-card__tag {
  background: color-mix(in srgb, var(--surface-raised) 90%, white 10%);
  border-color: color-mix(in srgb, var(--border-strong) 45%, var(--border));
  color: var(--ink-soft);
}

.heritage-card__tag::before {
  content: "#";
  color: color-mix(in srgb, var(--accent) 55%, var(--muted));
  font-weight: 800;
}

.heritage-card__more-tag {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 0.15rem 0.62rem;
  border-radius: 999px;
  border: 1px dashed color-mix(in srgb, var(--accent) 18%, var(--border));
  color: var(--ink-soft);
  font-size: var(--text-2xs);
  font-weight: 700;
  list-style: none;
}

.heritage-card__empty-tag {
  display: inline-flex;
  align-items: center;
  min-height: 28px;
  padding: 0.15rem 0.62rem;
  border-radius: 999px;
  border: 1px dashed color-mix(in srgb, var(--border-strong) 60%, var(--border));
  color: var(--muted-soft);
  font-size: var(--text-2xs);
  font-weight: 700;
  list-style: none;
}

.heritage-card .public-card-footer {
  width: 100%;
}

@media (max-width: 480px) {
  .heritage-card__media {
    height: 172px;
  }

  .heritage-card__body {
    min-height: 176px;
  }

  .heritage-card__description {
    -webkit-line-clamp: 3;
  }
}
</style>
