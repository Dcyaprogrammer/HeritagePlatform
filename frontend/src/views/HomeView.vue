<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { getToken, logout, getStoredRoles } from '../api/auth.js'
import ResourceCard from '../components/ResourceCard.vue'
import { getPublicResources } from '../api/resource.js'

const allResources = ref([])
const provinces = ref([])
const heritageGroups = ref([])
const dynasties = ref([])
const list = ref([])
const total = ref(0)
const page = ref(0)
const size = ref(8)
const loading = ref(false)
const err = ref('')

const q = ref('')
const selectedDynastyCodes = ref(new Set())
const dynastyPickerValue = ref('')
const eraFrom = ref('')
const eraTo = ref('')
const selectedProvinceCodes = ref(new Set())
const provincePickerValue = ref('')
const selectedHeritageTypeCodes = ref(new Set())
const heritageTypePickerValue = ref('')
const showFilterPanel = ref(false)
const autoSearchEnabled = ref(false)

const eraFromInputRef = ref(null)
const eraToInputRef = ref(null)
const router = useRouter()

const categories = computed(() => {
  const map = new Map()
  for (const r of allResources.value) {
    if (r.category) map.set(r.category.id, r.category.name)
  }
  return [...map.entries()].map(([id, name]) => ({ id, name }))
})

const categoryId = ref(null)

const filteredResources = computed(() => {
  return list.value
})

const isLoggedIn = ref(!!getToken())
const initialRoles = getStoredRoles()
const isAdmin = ref(initialRoles.includes('ADMIN'))
const isContributor = ref(initialRoles.includes('CONTRIBUTOR') || initialRoles.includes('ADMIN'))

// Update on route change or when storage changes
onMounted(() => {
  isLoggedIn.value = !!getToken()
  const r = getStoredRoles()
  isAdmin.value = r.includes('ADMIN')
  isContributor.value = r.includes('CONTRIBUTOR') || r.includes('ADMIN')
})

const goToAdmin = () => {
  router.push('/admin/resource-review')
}

const goToCreateResource = () => {
  router.push('/resources/create')
}

const goToProfile = () => {
  router.push('/profile')
}

const goToLogin = () => {
  router.push('/login')
}

const goToRegister = () => {
  router.push('/register')
}

const handleLogout = () => {
  logout()
  isLoggedIn.value = false
  isAdmin.value = false
  router.push('/login')
}

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
  if (selectedDynastyCodes.value.size > 0) {
    n += 1
  }
  if (eraFrom.value && eraTo.value) {
    n += 1
  }
  if (selectedProvinceCodes.value.size > 0) {
    n += 1
  }
  if (selectedHeritageTypeCodes.value.size > 0) {
    n += 1
  }
  return n
})

const dynastyNameByCode = computed(() => {
  const m = new Map()
  for (const d of dynasties.value) {
    m.set(d.code, d.name)
  }
  return m
})

const provinceNameByCode = computed(() => {
  const m = new Map()
  for (const p of provinces.value) {
    m.set(p.code, p.name)
  }
  return m
})

const heritageNameByCode = computed(() => {
  const m = new Map()
  for (const g of heritageGroups.value) {
    m.set(g.groupCode, g.groupName)
    for (const t of g.types || []) {
      m.set(t.code, t.name)
    }
  }
  return m
})

const selectedFilterChips = computed(() => {
  const chips = []
  const selectedDynastySorted = [...selectedDynastyCodes.value].sort()
  for (const code of selectedDynastySorted) {
    chips.push({
      key: `dynasty:${code}`,
      type: 'dynasty',
      value: code,
      label: `Dynasty: ${dynastyNameByCode.value.get(code) || code}`,
    })
  }
  if (eraFrom.value && eraTo.value) {
    chips.push({
      key: `date:${eraFrom.value}-${eraTo.value}`,
      type: 'dateRange',
      label: `Date: ${formatIsoToCnYmd(eraFrom.value)} to ${formatIsoToCnYmd(eraTo.value)}`,
    })
  }
  const selectedProvinceSorted = [...selectedProvinceCodes.value].sort()
  for (const code of selectedProvinceSorted) {
    chips.push({
      key: `province:${code}`,
      type: 'province',
      value: code,
      label: `Region: ${provinceNameByCode.value.get(code) || code}`,
    })
  }

  const selectedTypeSorted = [...selectedHeritageTypeCodes.value].sort()
  for (const code of selectedTypeSorted) {
    chips.push({
      key: `type:${code}`,
      type: 'heritageType',
      value: code,
      label: `Type: ${heritageNameByCode.value.get(code) || code}`,
    })
  }
  return chips
})

const filterWatchKey = computed(() => {
  const dynKey = [...selectedDynastyCodes.value].sort().join(',')
  const provinceKey = [...selectedProvinceCodes.value].sort().join(',')
  const typeKey = [...selectedHeritageTypeCodes.value].sort().join(',')
  return [dynKey, eraFrom.value, eraTo.value, provinceKey, typeKey].join('|')
})

let autoSearchTimer = null

async function loadMeta() {
  const [d, p, h, c] = await Promise.all([
    fetch('/api/public/dynasties'),
    fetch('/api/public/provinces'),
    fetch('/api/public/heritage-type-groups'),
    fetch('/api/public/categories')
  ])
  const dj = await d.json()
  const pj = await p.json()
  const hj = await h.json()
  const cj = await c.json()
  if (dj.code === 200) {
    dynasties.value = dj.data
  }
  if (pj.code === 200) provinces.value = pj.data
  if (hj.code === 200) heritageGroups.value = ensureHeritageOtherGroup(hj.data)
  if (cj.code === 200) allResources.value = cj.data.map(cat => ({ category: { id: cat.id, name: cat.name } }))
}

async function search() {
  loading.value = true
  err.value = ''
  try {
    const params = {
      page: page.value,
      size: size.value
    }
    if (q.value) params.q = q.value
    if (categoryId.value) params.categoryId = categoryId.value
    
    if (selectedDynastyCodes.value.size > 0) {
      params.dynastyCode = Array.from(selectedDynastyCodes.value).join(',')
    }
    if (selectedProvinceCodes.value.size > 0) {
      params.provinceCode = Array.from(selectedProvinceCodes.value).join(',')
    }
    if (selectedHeritageTypeCodes.value.size > 0) {
      params.heritageTypeCode = Array.from(selectedHeritageTypeCodes.value).join(',')
    }
    if (eraFrom.value && eraTo.value) {
      params.eraFrom = eraFrom.value
      params.eraTo = eraTo.value
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

onMounted(() => {
  loadMeta()
  search()
})
</script>

<template>
  <div class="wrap">
    <header class="top">
      <h1>Heritage Resource Hall</h1>
      <div class="header-actions">
        <template v-if="isLoggedIn">
          <button v-if="isContributor" type="button" class="btn" @click="goToCreateResource">
            + Create Draft
          </button>
          <button v-if="isAdmin" type="button" class="btn primary" @click="goToAdmin">Admin Panel</button>
          <button type="button" class="btn" @click="goToProfile">Profile</button>
          <button type="button" class="btn" @click="handleLogout">Logout</button>
        </template>
        <template v-else>
          <button type="button" class="btn" @click="goToLogin">Login</button>
          <button type="button" class="btn primary" @click="goToRegister">Register</button>
        </template>
      </div>
    </header>

    <div class="hero">
      <h2 class="page-title">Discover community heritage</h2>
      <p class="lead">Browse published heritage resources.</p>
    </div>

    <div class="toolbar">
      <label class="field">
        <span class="label">Search</span>
        <input v-model="q" type="search" class="control" placeholder="Title, place, tags, description" />
      </label>
      <label class="field">
        <span class="label">Category</span>
        <select v-model="categoryId" class="control">
          <option :value="null">All</option>
          <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</option>
        </select>
      </label>
    </div>

    <p v-if="!filteredResources.length" class="none">No resources match your filters.</p>

    <div v-else class="grid">
      <ResourceCard v-for="item in filteredResources" :key="item.id" :item="item" />
    </div>

  </div>
</template>

<style scoped>
.wrap {
  max-width: 1120px;
  margin: 0 auto;
  padding: 0 1.25rem;
}
.top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem 0;
  border-bottom: 1px solid var(--border);
}
.header-actions {
  display: flex;
  gap: 0.5rem;
}
.btn {
  padding: 0.5rem 1rem;
  border: 1px solid var(--border);
  border-radius: 4px;
  background: white;
  cursor: pointer;
}
.btn.primary {
  background: var(--accent);
  color: white;
  border-color: var(--accent);
}
.hero {
  margin: 1.5rem 0;
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
