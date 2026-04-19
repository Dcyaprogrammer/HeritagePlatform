<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { RouterLink, useRouter } from 'vue-router'

const dynasties = ref([])
const provinces = ref([])
const heritageGroups = ref([])
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

const isAdmin = computed(() => {
  return localStorage.getItem('role') === 'ADMIN'
})

const goToAdmin = () => {
  router.push('/admin/users')
}

const goToProfile = () => {
  router.push('/profile')
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
  const [d, p, h] = await Promise.all([
    fetch('/api/public/dynasties'),
    fetch('/api/public/provinces'),
    fetch('/api/public/heritage-type-groups'),
  ])
  const dj = await d.json()
  const pj = await p.json()
  const hj = await h.json()
  if (dj.code === 200) {
    dynasties.value = dj.data
  }
  if (pj.code === 200) {
    provinces.value = pj.data
  }
  if (hj.code === 200) {
    heritageGroups.value = ensureHeritageOtherGroup(hj.data)
  }
}

/** 将 ISO 日期 (yyyy-MM-dd) 格式化为中文年月日展示 */
function formatIsoToCnYmd(iso) {
  if (!iso) {
    return ''
  }
  const parts = String(iso).trim().split('-')
  if (parts.length !== 3) {
    return ''
  }
  const [y, m, d] = parts
  return `${y}-${m}-${d}`
}

function openNativeDatePicker(which) {
  const el = which === 'from' ? eraFromInputRef.value : eraToInputRef.value
  if (!el) {
    return
  }
  try {
    if (typeof el.showPicker === 'function') {
      el.showPicker().catch(() => el.click())
    } else {
      el.click()
    }
  } catch {
    el.click()
  }
}

function onEraDateShellKeydown(e, which) {
  if (e.key === 'Enter' || e.key === ' ') {
    e.preventDefault()
    openNativeDatePicker(which)
  }
}

function onDynastyPick() {
  const picked = dynastyPickerValue.value
  if (!picked) {
    selectedDynastyCodes.value = new Set()
    return
  }
  const next = new Set(selectedDynastyCodes.value)
  next.add(picked)
  selectedDynastyCodes.value = next
  dynastyPickerValue.value = ''
}

function onProvincePick() {
  const picked = provincePickerValue.value
  if (!picked) {
    selectedProvinceCodes.value = new Set()
    return
  }
  const next = new Set(selectedProvinceCodes.value)
  next.add(picked)
  selectedProvinceCodes.value = next
  provincePickerValue.value = ''
}

function onTypePick() {
  const picked = heritageTypePickerValue.value
  if (!picked) {
    selectedHeritageTypeCodes.value = new Set()
    return
  }
  const next = new Set(selectedHeritageTypeCodes.value)
  next.add(picked)
  selectedHeritageTypeCodes.value = next
  heritageTypePickerValue.value = ''
}

function resetFilters() {
  selectedDynastyCodes.value = new Set()
  dynastyPickerValue.value = ''
  eraFrom.value = ''
  eraTo.value = ''
  selectedProvinceCodes.value = new Set()
  provincePickerValue.value = ''
  selectedHeritageTypeCodes.value = new Set()
  heritageTypePickerValue.value = ''
  search(true)
}

function toggleFilterPanel() {
  showFilterPanel.value = !showFilterPanel.value
}

function applyFilters() {
  search(true)
  showFilterPanel.value = false
}

function removeFilterChip(chip) {
  if (chip.type === 'dynasty') {
    const next = new Set(selectedDynastyCodes.value)
    next.delete(chip.value)
    selectedDynastyCodes.value = next
  } else if (chip.type === 'dateRange') {
    eraFrom.value = ''
    eraTo.value = ''
  } else if (chip.type === 'province') {
    const next = new Set(selectedProvinceCodes.value)
    next.delete(chip.value)
    selectedProvinceCodes.value = next
  } else if (chip.type === 'heritageType') {
    const next = new Set(selectedHeritageTypeCodes.value)
    next.delete(chip.value)
    selectedHeritageTypeCodes.value = next
  }
}

/** 强制起始 ≤ 截止：改起始则推后截止，改截止则前移起始 */
function clampEraRange(source) {
  const a = eraFrom.value
  const b = eraTo.value
  if (!a || !b) {
    return
  }
  if (a <= b) {
    return
  }
  if (source === 'from') {
    eraTo.value = a
  } else {
    eraFrom.value = b
  }
}

function onEraFromChange() {
  clampEraRange('from')
}

function onEraToChange() {
  clampEraRange('to')
}

function validateEraRange() {
  const a = eraFrom.value
  const b = eraTo.value
  if ((a && !b) || (!a && b)) {
    err.value = 'Start and end dates must both be filled or both be empty.'
    return false
  }
  if (a && b && a > b) {
    clampEraRange('from')
  }
  return true
}

async function search(resetPage) {
  if (!validateEraRange()) {
    return
  }
  if (resetPage) {
    page.value = 0
  }
  loading.value = true
  err.value = ''
  const params = new URLSearchParams()
  if (q.value.trim()) {
    params.set('q', q.value.trim())
  }
  if (selectedDynastyCodes.value.size > 0) {
    params.set('dynastyCode', [...selectedDynastyCodes.value].sort().join(','))
  }
  if (eraFrom.value && eraTo.value) {
    params.set('eraFrom', eraFrom.value)
    params.set('eraTo', eraTo.value)
  }
  if (selectedProvinceCodes.value.size > 0) {
    params.set('provinceCode', [...selectedProvinceCodes.value].sort().join(','))
  }
  if (selectedHeritageTypeCodes.value.size > 0) {
    params.set('heritageTypeCode', [...selectedHeritageTypeCodes.value].sort().join(','))
  }
  params.set('page', String(page.value))
  params.set('size', String(size.value))
  try {
    const res = await fetch(`/api/public/resources?${params}`)
    const body = await res.json()
    if (body.code !== 200) {
      err.value = body.message || 'Failed to load resources.'
      return
    }
    list.value = body.data.items
    total.value = body.data.total
  } catch {
    err.value = 'Network or backend error. Please ensure Spring Boot is running.'
  } finally {
    loading.value = false
  }
}

function summaryLine(item) {
  const parts = []
  if (item.heritageTypeLabel) {
    parts.push(item.heritageTypeLabel)
  }
  if (item.dynastyName) {
    parts.push(item.dynastyName)
  }
  if (item.provinceName) {
    parts.push(item.provinceName)
  }
  if (item.categoryName) {
    parts.push(item.categoryName)
  }
  if (item.locationName) {
    parts.push(item.locationName)
  }
  return parts.length ? parts.join(' · ') : '—'
}

onMounted(async () => {
  await loadMeta()
  await search(true)
  autoSearchEnabled.value = true
})

watch(filterWatchKey, () => {
  if (!autoSearchEnabled.value) {
    return
  }
  if (autoSearchTimer) {
    clearTimeout(autoSearchTimer)
  }
  autoSearchTimer = setTimeout(() => {
    search(true)
  }, 250)
})
</script>

<template>
  <div class="wrap">
    <header class="top">
      <h1>Heritage Resource Hall</h1>
      <div class="header-actions">
        <button v-if="isAdmin" type="button" class="btn primary" @click="goToAdmin">Admin Panel</button>
        <button type="button" class="btn" @click="goToProfile">Profile</button>
      </div>
    </header>

    <section class="panel home-search">
      <div class="search-row">
        <input v-model="q" type="search" class="inp search-input" placeholder="Enter title keywords" @keyup.enter="search(true)" />
        <button type="button" class="btn primary" :disabled="loading" @click="search(true)">
          {{ loading ? 'Searching...' : 'Search' }}
        </button>
        <button type="button" class="btn" :disabled="loading" @click="toggleFilterPanel">
          Filter<span v-if="activeFilterCount"> ({{ activeFilterCount }})</span>
        </button>
      </div>
      <div v-if="selectedFilterChips.length" class="chips-row">
        <span class="chips-label">Selected Filters:</span>
        <button v-for="chip in selectedFilterChips" :key="chip.key" type="button" class="chip" @click="removeFilterChip(chip)">
          <span>{{ chip.label }}</span>
          <span class="chip-close" aria-hidden="true">×</span>
        </button>
      </div>

      <div v-if="showFilterPanel" class="filter-panel">
        <div class="row">
          <label class="lbl">Dynasty</label>
          <select v-model="dynastyPickerValue" class="inp" @change="onDynastyPick">
            <option value="">All</option>
            <option v-for="d in dynasties" :key="d.code" :value="d.code">
              {{ selectedDynastyCodes.has(d.code) ? `Selected: ${d.name}` : d.name }}
            </option>
          </select>
        </div>
        <div class="row">
          <label class="lbl">Date Range</label>
          <div class="era-range">
            <div class="era-date-wrap inp inp-date">
              <div
                class="era-date-shell"
                role="button"
                tabindex="0"
                :aria-label="'Start date, ' + (eraFrom ? formatIsoToCnYmd(eraFrom) : 'not selected, click to choose')"
                @click="openNativeDatePicker('from')"
                @keydown="onEraDateShellKeydown($event, 'from')"
              >
                <span class="era-date-text" :class="{ 'is-empty': !eraFrom }">
                  {{ eraFrom ? formatIsoToCnYmd(eraFrom) : 'Select start date' }}
                </span>
              </div>
              <input
                ref="eraFromInputRef"
                v-model="eraFrom"
                type="date"
                class="era-date-sr-only"
                tabindex="-1"
                aria-hidden="true"
                :max="eraTo || undefined"
                @change="onEraFromChange"
              />
            </div>
            <span class="era-sep">to</span>
            <div class="era-date-wrap inp inp-date">
              <div
                class="era-date-shell"
                role="button"
                tabindex="0"
                :aria-label="'End date, ' + (eraTo ? formatIsoToCnYmd(eraTo) : 'not selected, click to choose')"
                @click="openNativeDatePicker('to')"
                @keydown="onEraDateShellKeydown($event, 'to')"
              >
                <span class="era-date-text" :class="{ 'is-empty': !eraTo }">
                  {{ eraTo ? formatIsoToCnYmd(eraTo) : 'Select end date' }}
                </span>
              </div>
              <input
                ref="eraToInputRef"
                v-model="eraTo"
                type="date"
                class="era-date-sr-only"
                tabindex="-1"
                aria-hidden="true"
                :min="eraFrom || undefined"
                @change="onEraToChange"
              />
            </div>
          </div>
          <p class="hint muted">
            Optional. Start and end dates must be filled together. Start will always be <= end (auto-aligned on conflict).
            Filtering uses the resource <strong>record creation time</strong> (compared with the date part of `created_at`), not
            the artifact's historical era.
          </p>
        </div>
        <div class="row">
          <label class="lbl">Region (Province / Municipality)</label>
          <select v-model="provincePickerValue" class="inp" @change="onProvincePick">
            <option value="">All</option>
            <option v-for="p in provinces" :key="p.code" :value="p.code">
              {{ selectedProvinceCodes.has(p.code) ? `Selected: ${p.name}` : p.name }}
            </option>
          </select>
        </div>
        <div class="row">
          <label class="lbl">Type</label>
          <div class="type-col">
            <select v-model="heritageTypePickerValue" class="inp" @change="onTypePick">
              <option value="">All</option>
              <template v-for="g in heritageGroups" :key="g.groupCode">
                <option :value="g.groupCode" class="type-group-option">
                  {{ selectedHeritageTypeCodes.has(g.groupCode) ? `Selected: ${g.groupName}` : g.groupName }}
                </option>
                <option v-for="t in g.types" :key="t.code" :value="t.code">
                  {{ selectedHeritageTypeCodes.has(t.code) ? `Selected: ${'\u3000' + t.name}` : '\u3000' + t.name }}
                </option>
              </template>
            </select>
            <p class="hint muted type-hint">
              You can choose a major category (including all subtypes) or a single subtype to filter resources.
            </p>
          </div>
        </div>
        <div class="actions">
          <button type="button" class="btn primary" :disabled="loading" @click="applyFilters">Apply Filters</button>
          <button type="button" class="btn" :disabled="loading" @click="resetFilters">Reset Filters</button>
        </div>
      </div>
      <p v-if="err" class="err">{{ err }}</p>
    </section>

    <section class="results">
      <h2 class="h2">Latest Uploaded Heritage Resources</h2>
      <p class="muted count">Total {{ total }} items · Page {{ page + 1 }} / {{ totalPages }}</p>
      <ul class="cards">
        <li v-for="item in list" :key="item.id" class="card">
          <h3 class="title">
            <RouterLink :to="`/resources/${item.id}`" class="title-link">{{ item.title }}</RouterLink>
          </h3>
          <p class="meta">{{ summaryLine(item) }}</p>
          <p class="desc">{{ item.description || '(No description)' }}</p>
        </li>
      </ul>
      <p v-if="!list.length && !loading" class="muted empty">No approved resources match the current criteria.</p>

      <nav v-if="total > size" class="pager">
        <button type="button" class="btn" :disabled="page <= 0 || loading" @click="page--; search(false)">Previous</button>
        <button type="button" class="btn" :disabled="page >= totalPages - 1 || loading" @click="page++; search(false)">
          Next
        </button>
      </nav>
    </section>
  </div>
</template>

<style scoped>
.wrap {
  max-width: 880px;
  margin: 0 auto;
  padding: 32px 20px 64px;
}

.top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
}

.top h1 {
  margin: 0;
  font-size: 26px;
  font-weight: 650;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.muted {
  color: #64748b;
  font-size: 14px;
}

.h2 {
  margin: 0 0 16px;
  font-size: 17px;
}

.panel {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 20px 20px 12px;
  margin-bottom: 28px;
}

.home-search {
  padding-bottom: 18px;
}

.search-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

.search-input {
  min-width: 0;
}

.chips-row {
  margin-top: 12px;
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.chips-label {
  font-size: 13px;
  color: #64748b;
}

.chip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 4px 8px;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  background: #fff;
  color: #334155;
  font-size: 13px;
  cursor: pointer;
}

.chip:hover {
  border-color: #94a3b8;
  background: #f8fafc;
}

.chip-close {
  font-size: 14px;
  line-height: 1;
  color: #64748b;
}

.filter-panel {
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px dashed #d6dde8;
}

.row {
  display: flex;
  flex-wrap: wrap;
  align-items: flex-start;
  gap: 10px 16px;
  margin-bottom: 14px;
}

.lbl {
  min-width: 120px;
  padding-top: 8px;
  font-size: 14px;
  color: #334155;
}

.inp {
  flex: 1;
  min-width: 200px;
  padding: 8px 10px;
  border: 1px solid #cbd5e1;
  border-radius: 6px;
  font-size: 15px;
}

.era-range {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 200px;
}

.inp-date {
  min-width: 160px;
  flex: 0 1 auto;
}

.era-date-wrap {
  position: relative;
  display: inline-flex;
  flex: 0 1 auto;
  align-items: stretch;
  box-sizing: border-box;
}

.era-date-shell {
  flex: 1;
  display: flex;
  align-items: center;
  min-width: 0;
  cursor: pointer;
  outline: none;
}

.era-date-shell:focus-visible {
  box-shadow: 0 0 0 2px #fff, 0 0 0 4px #1d4ed8;
  border-radius: 4px;
}

.era-date-text {
  white-space: nowrap;
  padding-right: 6px;
  user-select: none;
}

.era-date-text.is-empty {
  color: #94a3b8;
}

.era-date-sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
  opacity: 0;
  pointer-events: none;
}

.era-sep {
  color: #64748b;
  font-size: 14px;
}

.hint {
  width: 100%;
  margin: 0;
  font-size: 13px;
  line-height: 1.45;
}

.type-col {
  flex: 1;
  min-width: 200px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.type-hint {
  margin: 0;
}

.type-group-option {
  font-weight: 650;
}

.actions {
  margin-top: 8px;
  display: flex;
  gap: 10px;
}

.btn {
  padding: 8px 16px;
  border-radius: 6px;
  border: 1px solid #94a3b8;
  background: #fff;
  font-size: 14px;
  cursor: pointer;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn.primary {
  background: #1d4ed8;
  border-color: #1d4ed8;
  color: #fff;
}

.err {
  color: #b91c1c;
  font-size: 14px;
  margin-top: 10px;
}

.count {
  margin: 0 0 12px;
}

.cards {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.card {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 16px 18px;
}

.title {
  margin: 0 0 6px;
  font-size: 18px;
  line-height: 1.35;
}

.title-link {
  color: #1d4ed8;
  text-decoration: none;
}

.title-link:hover {
  text-decoration: underline;
}

.meta {
  margin: 0 0 8px;
  font-size: 13px;
  color: #64748b;
}

.desc {
  margin: 0;
  font-size: 14px;
  line-height: 1.55;
  color: #334155;
  display: -webkit-box;
  -webkit-line-clamp: 4;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.empty {
  padding: 24px 0;
}

.pager {
  display: flex;
  gap: 12px;
  margin-top: 20px;
}
</style>
