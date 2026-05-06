<script setup>
import { computed } from 'vue'
import { RouterLink } from 'vue-router'

const props = defineProps({ item: Object })
const detailHref = computed(() => `/resources/${props.item.id}`)

const hasVideo = computed(() => props.item?.hasVideo || false)
</script>

<template>
  <article class="public-card resource-card">
    <RouterLink :to="detailHref" class="public-card-media-link" :aria-label="`View: ${item.title}`">
      <div class="public-card-media resource-card__media">
        <img
          v-if="item.coverUrl"
          :src="item.coverUrl"
          :alt="`${item.title} cover`"
          loading="lazy"
          class="public-card-image"
        />
        <div v-else class="public-card-placeholder" role="img" aria-label="No cover image">
          <span>No image</span>
        </div>
        <span v-if="hasVideo" class="public-badge public-badge--danger resource-card__badge" aria-label="Contains video">
          <svg width="12" height="12" viewBox="0 0 24 24" fill="currentColor"><path d="M8 5v14l11-7z"/></svg>
          Video
        </span>
      </div>
    </RouterLink>

    <div class="public-card-body">
      <RouterLink :to="detailHref" class="public-card-title-link">
        <h2 class="public-card-title resource-card__title">{{ item.title }}</h2>
      </RouterLink>
      <p v-if="item.locationName" class="public-card-meta">{{ item.locationName }}</p>
      <ul v-if="item.tags && item.tags.length" class="public-card-tags" aria-label="Tags">
        <li v-for="t in item.tags" :key="t.id" class="public-tag">{{ t.name }}</li>
      </ul>
      <RouterLink :to="detailHref" class="public-link-inline resource-card__cta">View details</RouterLink>
    </div>
  </article>
</template>

<style scoped>
.resource-card {
  height: 100%;
}

.resource-card__media {
  aspect-ratio: 4 / 3;
}

.resource-card__badge {
  position: absolute;
  top: 0.8rem;
  right: 0.8rem;
}

.resource-card__title {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.resource-card__cta {
  margin-top: auto;
}
</style>
