<script setup lang="ts">
import { computed, ref } from 'vue'
import ResourceCard from '@/components/ResourceCard.vue'
import { getApprovedResources } from '@/api/mockData'
import type { ResourceDetail } from '@/types/resource'

const all = ref<ResourceDetail[]>(getApprovedResources())
const q = ref('')
const categoryId = ref<number | null>(null)

const categories = computed(() => {
  const map = new Map<number, string>()
  for (const r of all.value) {
    if (r.category) map.set(r.category.id, r.category.name)
  }
  return [...map.entries()].map(([id, name]) => ({ id, name }))
})

const filtered = computed(() => {
  const text = q.value.trim().toLowerCase()
  return all.value.filter((r) => {
    if (categoryId.value != null && r.category?.id !== categoryId.value) return false
    if (!text) return true
    const inTitle = r.title.toLowerCase().includes(text)
    const inDesc = r.description.toLowerCase().includes(text)
    const inTags = r.tags.some((t) => t.name.toLowerCase().includes(text))
    const inLoc = (r.location_name ?? '').toLowerCase().includes(text)
    return inTitle || inDesc || inTags || inLoc
  })
})
</script>

<template>
  <div class="page inner">
    <header class="hero">
      <h1 class="page-title">发现社区遗产</h1>
      <p class="lead">浏览已发布的社区遗产资源（当前为演示数据）。</p>
    </header>

    <div class="toolbar">
      <label class="field">
        <span class="label">搜索</span>
        <input v-model="q" type="search" class="control" placeholder="标题、地点、标签、描述" />
      </label>
      <label class="field">
        <span class="label">类别</span>
        <select v-model="categoryId" class="control">
          <option :value="null">全部</option>
          <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</option>
        </select>
      </label>
    </div>

    <p v-if="!filtered.length" class="none">没有匹配的资源。</p>

    <div v-else class="grid">
      <ResourceCard v-for="item in filtered" :key="item.id" :item="item" />
    </div>
  </div>
</template>

<style scoped>
.inner {
  max-width: 1120px;
  margin: 0 auto;
  padding: 0 1.25rem;
}
.hero {
  margin-bottom: 1.5rem;
}
.page-title {
  margin: 0 0 0.5rem;
  font-family: var(--font-serif);
  font-size: clamp(1.75rem, 3vw, 2.25rem);
  font-weight: 700;
}
.lead {
  margin: 0;
  max-width: 62ch;
  color: var(--muted);
  font-size: 0.975rem;
}
.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem;
  margin-bottom: 1.75rem;
  align-items: flex-end;
}
.field {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  min-width: min(100%, 220px);
  flex: 1;
}
.label {
  font-size: 0.8125rem;
  font-weight: 600;
  color: var(--muted);
}
.control {
  padding: 0.55rem 0.75rem;
  border-radius: 8px;
  border: 1px solid var(--border);
  font-family: inherit;
  font-size: 0.9375rem;
  background: var(--surface);
}
.grid {
  display: grid;
  grid-template-columns: repeat(1, 1fr);
  gap: 1.25rem;
}
@media (min-width: 640px) {
  .grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
@media (min-width: 960px) {
  .grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
.none {
  color: var(--muted);
  font-size: 0.9375rem;
}
</style>