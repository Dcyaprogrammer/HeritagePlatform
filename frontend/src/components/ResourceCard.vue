<script setup>
import { computed } from 'vue'
import { RouterLink } from 'vue-router'

const props = defineProps({ item: Object })
const detailHref = computed(() => `/resources/${props.item.id}`)
</script>

<template>
  <article class="card">
    <RouterLink :to="detailHref" class="media-link" :aria-label="`View: ${item.title}`">
      <div class="media">
        <img v-if="item.coverUrl" :src="item.coverUrl" :alt="`${item.title} cover`" loading="lazy" />
        <div v-else class="placeholder" role="img" aria-label="No cover image">
          <span>No image</span>
        </div>
      </div>
    </RouterLink>
    <div class="body">
      <RouterLink :to="detailHref" class="title-link">
        <h2 class="title">{{ item.title }}</h2>
      </RouterLink>
      <p v-if="item.locationName" class="location">{{ item.locationName }}</p>
      <ul v-if="item.tags.length" class="tags" aria-label="Tags">
        <li v-for="t in item.tags" :key="t.id" class="tag">{{ t.name }}</li>
      </ul>
      <RouterLink :to="detailHref" class="cta">View details</RouterLink>
    </div>
  </article>
</template>

<style scoped>
.card {
  background: var(--surface);
  border-radius: var(--radius);
  overflow: hidden;
  border: 1px solid var(--border);
  box-shadow: var(--card-shadow);
  display: flex;
  flex-direction: column;
  height: 100%;
  transition: box-shadow 0.2s ease, transform 0.2s ease;
}
.card:hover {
  box-shadow: 0 4px 12px rgba(28, 25, 23, 0.08), 0 16px 40px rgba(28, 25, 23, 0.08);
  transform: translateY(-2px);
}
.media-link {
  display: block;
  color: inherit;
  text-decoration: none;
}
.media-link:hover {
  text-decoration: none;
}
.media {
  aspect-ratio: 4 / 3;
  background: #e7e5e4;
  overflow: hidden;
}
.media img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--muted);
  font-size: 0.875rem;
  background: linear-gradient(145deg, #f5f5f4, #e7e5e4);
}
.body {
  padding: 1rem 1.1rem 1.15rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
  flex: 1;
}
.title-link {
  color: inherit;
  text-decoration: none;
}
.title-link:hover .title {
  color: var(--accent);
}
.title {
  margin: 0;
  font-family: var(--font-serif);
  font-size: 1.125rem;
  font-weight: 600;
  line-height: 1.35;
  transition: color 0.15s ease;
}
.location {
  margin: 0;
  font-size: 0.8125rem;
  color: var(--muted);
}
.tags {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-wrap: wrap;
  gap: 0.35rem;
}
.tag {
  font-size: 0.75rem;
  padding: 0.25rem 0.6rem;
  border-radius: 999px;
  background: #1d4ed8;
  color: #ffffff;
  border: none;
  font-weight: 500;
}
.cta {
  margin-top: auto;
  padding-top: 0.35rem;
  font-size: 0.875rem;
  font-weight: 600;
  color: var(--accent);
  text-decoration: none;
}
.cta:hover {
  text-decoration: underline;
}
</style>