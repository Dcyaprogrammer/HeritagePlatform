<script setup>
import { computed, onMounted, ref } from 'vue'
import { RouterLink, useRoute } from 'vue-router'

const route = useRoute()
const loading = ref(true)
const err = ref('')
const detail = ref(null)

const resourceId = computed(() => route.params.id)

function fmtDate(value) {
  if (!value) {
    return '—'
  }
  const date = new Date(value)
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString()
}

async function loadDetail() {
  loading.value = true
  err.value = ''
  detail.value = null
  try {
    const res = await fetch(`/api/public/resources/${resourceId.value}`)
    const body = await res.json()
    if (body.code !== 200) {
      err.value = body.message || 'Failed to load resource'
      return
    }
    if (!body.data) {
      err.value = 'Resource does not exist or has not been approved'
      return
    }
    detail.value = body.data
  } catch (e) {
    err.value = 'Network or backend error, please ensure Spring Boot is running'
  } finally {
    loading.value = false
  }
}

onMounted(loadDetail)
</script>

<template>
  <div class="wrap">
    <p class="back"><RouterLink to="/">← Back to Home</RouterLink></p>
    <section class="panel">
      <p v-if="loading" class="muted">Loading...</p>
      <p v-else-if="err" class="err">{{ err }}</p>
      <template v-else-if="detail">
        <h1 class="title">{{ detail.title }}</h1>
        <p class="meta">
          <span v-if="detail.dynastyName">Dynasty (inferred from record date): {{ detail.dynastyName }}</span>
          <span v-if="detail.provinceName"> · Region (matched from location text): {{ detail.provinceName }}</span>
        </p>
        <p v-if="detail.heritageTypeLabel" class="meta">Type: {{ detail.heritageTypeLabel }}</p>
        <p class="meta">
          <span>Category: {{ detail.categoryName || 'Uncategorized' }}</span>
          <span> · Location: {{ detail.locationName || 'Not provided' }}</span>
        </p>
        <p v-if="detail.eraStart || detail.eraEnd" class="meta">
          Record Date (for era display): {{ detail.eraStart || '—' }} ～ {{ detail.eraEnd || '—' }}
        </p>
        <p class="meta">Last Updated: {{ fmtDate(detail.updatedAt) }}</p>
        <div class="tags">
          <span v-for="tag in detail.tags || []" :key="tag.id" class="tag">{{ tag.name }}</span>
          <span v-if="!(detail.tags || []).length" class="muted">No tags</span>
        </div>
        <article class="desc">{{ detail.description || '(No description)' }}</article>
      </template>
    </section>
  </div>
</template>

<style scoped>
.wrap {
  max-width: 880px;
  margin: 0 auto;
  padding: 32px 20px 64px;
}

.back {
  margin-bottom: 12px;
}

.back a {
  color: #1d4ed8;
  text-decoration: none;
}

.panel {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 20px;
}

.title {
  margin: 0 0 10px;
}

.meta {
  color: #64748b;
  font-size: 14px;
  margin: 0 0 8px;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: 12px 0 16px;
}

.tag {
  border: 1px solid #cbd5e1;
  border-radius: 999px;
  padding: 4px 10px;
  font-size: 13px;
  color: #334155;
}

.desc {
  margin: 0;
  white-space: pre-wrap;
  line-height: 1.7;
  color: #334155;
}

.muted {
  color: #64748b;
}

.err {
  color: #b91c1c;
}
</style>
