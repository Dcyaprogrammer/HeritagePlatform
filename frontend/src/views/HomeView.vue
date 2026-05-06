<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import HeritageCard from '../components/HeritageCard.vue'
import { getPublicResources } from '../api/resource.js'

const allResources = ref([])
const provinces = ref([])
const heritageGroups = ref([])
const ALL_OPTION = '__ALL__'
const list = ref([])
const total = ref(0)
const page = ref(0)
const size = ref(8)
const loading = ref(false)
const err = ref('')

const q = ref('')
const selectedProvinceCodes = ref(new Set())
const provincePickerValue = ref(ALL_OPTION)
const selectedHeritageTypeCodes = ref(new Set())
const heritageTypePickerValue = ref(ALL_OPTION)
const showFilterPanel = ref(false)
const autoSearchEnabled = ref(false)

const categories = computed(() => {
  const map = new Map()
  for (const r of allResources.value) {
    if (r.category) map.set(r.category.id, r.category.name)
  }
  return [...map.entries()].map(([id, name]) => ({ id, name }))
})
const sortedProvinces = computed(() => {
  return [...provinces.value].sort((a, b) => (a.name || '').localeCompare(b.name || ''))
})
const groupedHeritageTypeOptions = computed(() => {
  return heritageGroups.value.map((group) => ({
    groupCode: group.groupCode,
    groupName: group.groupName,
    types: [...(group.types || [])].sort((a, b) => (a.name || '').localeCompare(b.name || '')),
  }))
})

const categoryId = ref(null)

const filteredResources = computed(() => {
  return list.value
})

/** 与后端 TaxonomyCatalog 一致；接口未返回时兜底（例如未重启的旧后端） */
const HERITAGE_OTHER_GROUP_FALLBACK = Object.freeze({
  groupCode: 'HTG_OTHER',
  groupName: 'Others',
  types: Object.freeze([Object.freeze({ id: 1014, code: 'HERITAGE_OTHER', name: 'Others' })]),
})

function ensureHeritageOtherGroup(groups) {
  if (!Array.isArray(groups)) {
    return []
  }
  const base = groups.filter((g) => g && g.groupCode !== 'HTG_OTHER')
  const existingOther = groups.find((g) => g && g.groupCode === 'HTG_OTHER')
  const otherGroup = existingOther
    ? existingOther
    : { ...HERITAGE_OTHER_GROUP_FALLBACK, types: [...HERITAGE_OTHER_GROUP_FALLBACK.types] }
  return [...base, otherGroup]
}

const totalPages = computed(() => {
  const n = Math.ceil(total.value / size.value)
  return n > 0 ? n : 1
})

const activeFilterCount = computed(() => {
  let n = 0
  if (selectedProvinceCodes.value.size > 0) {
    n += 1
  }
  if (selectedHeritageTypeCodes.value.size > 0) {
    n += 1
  }
  return n
})

const hasListFilters = computed(() => {
  return !!(
    (q.value && q.value.trim()) ||
    categoryId.value != null ||
    activeFilterCount.value > 0
  )
})

let autoSearchTimer = null

function syncPickerSelections() {
  selectedProvinceCodes.value =
    provincePickerValue.value === ALL_OPTION ? new Set() : new Set([provincePickerValue.value])
  selectedHeritageTypeCodes.value =
    heritageTypePickerValue.value === ALL_OPTION
      ? new Set()
      : new Set([heritageTypePickerValue.value])
}

function applyAdvancedFilters() {
  syncPickerSelections()
  page.value = 0
  search()
}

function clearAdvancedFilters() {
  provincePickerValue.value = ALL_OPTION
  heritageTypePickerValue.value = ALL_OPTION
  syncPickerSelections()
  page.value = 0
  search()
}

function clearAllListFilters() {
  const hadQOrCat = !!(q.value?.trim() || categoryId.value != null)
  q.value = ''
  categoryId.value = null
  provincePickerValue.value = ALL_OPTION
  heritageTypePickerValue.value = ALL_OPTION
  syncPickerSelections()
  page.value = 0
  if (!hadQOrCat) {
    search()
  }
}

function goPrevPage() {
  if (page.value <= 0) return
  page.value -= 1
  search()
}

function goNextPage() {
  if (page.value >= totalPages.value - 1) return
  page.value += 1
  search()
}

async function loadMeta() {
  const [p, h, c] = await Promise.all([
    fetch('/api/public/provinces'),
    fetch('/api/public/heritage-type-groups'),
    fetch('/api/public/categories'),
  ])
  const pj = await p.json()
  const hj = await h.json()
  const cj = await c.json()
  if (pj.code === 200) provinces.value = pj.data
  if (hj.code === 200) heritageGroups.value = ensureHeritageOtherGroup(hj.data)
  if (cj.code === 200) {
    allResources.value = cj.data.map((cat) => ({ category: { id: cat.id, name: cat.name } }))
  }
}

async function search() {
  loading.value = true
  err.value = ''
  try {
    const params = {
      page: page.value,
      size: size.value,
    }
    if (q.value) params.q = q.value
    if (categoryId.value) params.categoryId = categoryId.value

    if (selectedProvinceCodes.value.size > 0) {
      params.provinceCode = Array.from(selectedProvinceCodes.value).join(',')
    }
    if (selectedHeritageTypeCodes.value.size > 0) {
      params.heritageTypeCode = Array.from(selectedHeritageTypeCodes.value).join(',')
    }

    const res = await getPublicResources(params)
    if (res.data.code === 200) {
      list.value = res.data.data.items || []
      total.value = res.data.data.total || 0
    }
  } catch (e) {
    err.value = e.message || 'Failed to load resources'
    console.error(e)
  } finally {
    loading.value = false
  }
}

watch([q, categoryId], () => {
  page.value = 0
  search()
})

watch([provincePickerValue, heritageTypePickerValue], () => {
  syncPickerSelections()
  if (!autoSearchEnabled.value) return
  if (autoSearchTimer) clearTimeout(autoSearchTimer)
  autoSearchTimer = setTimeout(() => {
    page.value = 0
    search()
  }, 250)
})

onMounted(() => {
  loadMeta()
  search()
})

onUnmounted(() => {
  if (autoSearchTimer) {
    clearTimeout(autoSearchTimer)
    autoSearchTimer = null
  }
})
</script>

<template>
  <div class="home">
    <div class="wrap">
    <div class="hero public-page-intro">
      <p class="public-eyebrow">Public Archive</p>
      <h2 class="public-page-title">Discover community heritage</h2>
      <p class="public-lead">
        Browse published resources, narrow the archive with focused filters, and move quickly between stories,
        places, and cultural records.
      </p>
    </div>

    <section class="public-panel public-panel--soft search-panel">
      <div class="toolbar">
      <label class="public-field field--grow">
        <span class="public-label">Search</span>
        <input
          v-model="q"
          type="search"
          class="public-input home-control home-search-control"
          placeholder="Title, place, tags, description"
        />
      </label>
      <label class="public-field field">
        <span class="public-label">Category</span>
        <select v-model="categoryId" class="public-select home-control">
          <option :value="null">All</option>
          <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</option>
        </select>
      </label>
      </div>
    </section>

    <section class="public-panel advanced">
      <div class="advanced-head">
        <button type="button" class="public-btn public-btn--ghost" @click="showFilterPanel = !showFilterPanel">
          {{ showFilterPanel ? 'Hide Advanced Filters' : 'Show Advanced Filters' }}
        </button>
        <label class="auto">
          <input v-model="autoSearchEnabled" type="checkbox" class="home-control" />
          <span>Auto apply</span>
        </label>
      </div>

      <div v-if="showFilterPanel" class="advanced-grid">
        <label class="public-field field">
          <span class="public-label">Province</span>
          <select v-model="provincePickerValue" class="public-select home-control">
            <option :value="ALL_OPTION">All</option>
            <option v-for="p in sortedProvinces" :key="p.code" :value="p.code">{{ p.name }}</option>
          </select>
        </label>

        <label class="public-field field">
          <span class="public-label">Heritage Type</span>
          <select v-model="heritageTypePickerValue" class="public-select home-control">
            <option :value="ALL_OPTION">All</option>
            <optgroup
              v-for="group in groupedHeritageTypeOptions"
              :key="group.groupCode"
              :label="group.groupName"
            >
              <option v-for="t in group.types" :key="t.code" :value="t.code">{{ t.name }}</option>
            </optgroup>
          </select>
        </label>
      </div>

      <div v-if="showFilterPanel" class="advanced-actions">
        <button type="button" class="public-btn public-btn--primary" @click="applyAdvancedFilters">Apply</button>
        <button type="button" class="public-btn" @click="clearAdvancedFilters">Clear</button>
      </div>
    </section>

    <div v-if="err" class="panel-error public-callout public-callout--error" role="alert">
      <p>{{ err }}</p>
      <button type="button" class="public-btn public-btn--primary" @click="search">Retry</button>
    </div>

    <div v-if="loading && !filteredResources.length && !err" class="skeleton-grid" aria-busy="true" aria-label="Loading">
      <div v-for="i in 8" :key="i" class="skeleton-card">
        <el-skeleton animated>
          <template #template>
            <el-skeleton-item variant="image" style="width: 100%; height: 160px" />
            <div style="padding: 12px 0 0">
              <el-skeleton-item variant="h3" style="width: 55%; margin-bottom: 8px" />
              <el-skeleton-item variant="text" style="width: 100%" />
              <el-skeleton-item variant="text" style="width: 40%" />
            </div>
          </template>
        </el-skeleton>
      </div>
    </div>

    <div v-else-if="!filteredResources.length && !loading && !err" class="empty-panel public-panel public-panel--soft">
      <template v-if="hasListFilters">
        <p class="empty-title">No resources match your filters.</p>
        <p class="empty-hint">Try widening your search or clearing some criteria.</p>
        <button type="button" class="public-btn public-btn--primary" @click="clearAllListFilters">
          Clear all filters
        </button>
      </template>
      <template v-else>
        <p class="empty-title">No published resources yet.</p>
        <p class="empty-hint">Check back later as contributors add entries.</p>
      </template>
    </div>

    <div v-else class="grid-wrap" :class="{ 'grid-wrap--dim': loading }">
      <div class="grid">
        <HeritageCard v-for="item in filteredResources" :key="item.id" :item="item" />
      </div>
    </div>

    <nav v-if="totalPages > 1 && filteredResources.length && !err" class="pager" aria-label="Pagination">
      <button type="button" class="public-btn" :disabled="page <= 0" @click="goPrevPage">Previous</button>
      <span class="pager-meta">Page {{ page + 1 }} of {{ totalPages }}</span>
      <button type="button" class="public-btn" :disabled="page >= totalPages - 1" @click="goNextPage">
        Next
      </button>
    </nav>
    </div>
  </div>
</template>

<style scoped>
.home {
  padding-top: 0.25rem;
}
.search-panel {
  margin-bottom: 1rem;
  padding: 1rem 1rem 1.05rem;
  width: 100%;
}
.toolbar {
  display: grid;
  width: 100%;
  grid-template-columns: minmax(0, 1fr) 340px;
  gap: 1rem;
  align-items: flex-end;
}
.field--grow {
  min-width: 0;
  width: 100%;
}
.home-search-control {
  min-height: 50px;
  font-size: 1rem;
}
.advanced {
  margin-bottom: 1.75rem;
  padding: 1rem;
  width: 100%;
}
.advanced-head {
  display: flex;
  width: 100%;
  align-items: center;
  gap: 0.75rem;
  flex-wrap: wrap;
}
.auto {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  color: var(--ink-soft);
  font-size: var(--text-sm);
  font-weight: 600;
}
.advanced-grid {
  margin-top: 1rem;
  display: grid;
  width: 100%;
  grid-template-columns: repeat(auto-fit, minmax(280px, 340px));
  gap: 1rem;
  justify-content: start;
}
.advanced-actions {
  margin-top: 1rem;
  display: flex;
  width: 100%;
  gap: 0.6rem;
  flex-wrap: wrap;
}
.field {
  min-width: 0;
  width: 100%;
  max-width: 340px;
}
.home :deep(.public-select) optgroup {
  font-weight: 700;
}
.panel-error {
  margin-bottom: 1.25rem;
}
.panel-error p {
  margin: 0 0 0.75rem;
  color: var(--ink);
}
.skeleton-grid {
  display: grid;
  grid-template-columns: repeat(1, minmax(0, 1fr));
  gap: 1.35rem;
  align-items: start;
}
@media (min-width: 640px) {
  .skeleton-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
@media (min-width: 960px) {
  .skeleton-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}
.skeleton-card {
  border-radius: var(--radius);
  overflow: hidden;
  border: 1px solid var(--border);
  background: var(--surface);
  padding: 0 0 0.75rem;
}
.empty-panel {
  width: 100%;
  padding: 2.5rem 1rem;
  text-align: center;
  margin-bottom: 1.5rem;
}
.empty-title {
  margin: 0 0 0.5rem;
  font-family: var(--font-serif);
  font-size: 1.2rem;
  font-weight: 700;
  color: var(--ink);
}
.empty-hint {
  margin: 0 0 1.25rem;
  color: var(--muted);
  font-size: var(--text-sm);
}
.grid-wrap {
  width: 100%;
  transition: opacity 0.18s ease;
}
.grid-wrap--dim {
  opacity: 0.48;
  pointer-events: none;
}
.grid {
  display: grid;
  width: 100%;
  grid-template-columns: repeat(1, minmax(0, 340px));
  gap: 1.35rem;
  align-items: stretch;
  justify-content: center;
}
@media (min-width: 640px) {
  .grid {
    grid-template-columns: repeat(2, minmax(0, 340px));
  }
}
@media (min-width: 960px) {
  .grid {
    grid-template-columns: repeat(3, minmax(0, 340px));
  }
}
.grid > * {
  min-width: 0;
  width: 340px;
}
.pager {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  flex-wrap: wrap;
  margin-top: 2rem;
  padding-top: 1rem;
  border-top: 1px solid var(--border);
}
.pager-meta {
  font-size: var(--text-sm);
  color: var(--muted);
}

@media (max-width: 760px) {
  .toolbar {
    grid-template-columns: 1fr;
  }

  .field,
  .field--grow {
    max-width: none;
  }

  .advanced-grid {
    grid-template-columns: 1fr;
  }
}
</style>
