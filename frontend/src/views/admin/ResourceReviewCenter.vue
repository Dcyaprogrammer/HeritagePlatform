<template>
  <div class="admin-page admin-page--wide resource-review-center">
    <div class="admin-page-header page-header">
      <div class="admin-page-copy">
        <h2>Resource Review Center</h2>
        <p class="admin-page-subtitle subtitle">Review pending heritage resources with a single queue and direct jump to full detail.</p>
      </div>
      <el-button type="primary" @click="fetchPending" :loading="loading">
        Refresh
      </el-button>
    </div>


    <el-row :gutter="20" class="admin-stats-row stats-row">
      <el-col :span="24">
        <el-card class="admin-stat-card stat-card">
          <div class="admin-stat-value stat-value">{{ pendingCount }}</div>
          <div class="admin-stat-label stat-label">Pending Resources</div>
        </el-card>
      </el-col>
    </el-row>

    
    <el-card class="admin-table-card table-card">
      <template #header>
        <div class="table-header">
          <span class="title">Pending List</span>
        </div>
      </template>

      <el-table
        :data="items"
        v-loading="loading"
        stripe
        style="width: 100%"
        empty-text="No pending resources"
        @row-click="openDetail"
        class="admin-table clickable-table"
      >
        <el-table-column type="index" width="50" />

        <el-table-column prop="title" label="Title" min-width="220" />

        <el-table-column label="Submitter" min-width="140">
          <template #default="{ row }">
            {{ row.submitterName || 'Unknown' }}
          </template>
        </el-table-column>

        <el-table-column prop="category" label="Category" width="140" />

        <el-table-column label="Submitted At" min-width="180">
          <template #default="{ row }">
            {{ formatDate(row.submittedAt) }}
          </template>
        </el-table-column>

        <el-table-column label="Status" width="170" align="center">
          <template #default="{ row }">
            <div class="status-cell">
              <span class="status-badge" :class="row.stale ? 'status-badge--warning' : 'status-badge--info'">
                <span class="status-badge__title">Pending</span>
                <span v-if="row.stale" class="status-badge__meta">3+ days</span>
              </span>
            </div>
          </template>
        </el-table-column>

      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getPendingResources } from '../../api/user.js'
import { useRouter } from 'vue-router'

const loading = ref(false)
const items = ref([])
const router = useRouter()

const pendingCount = computed(() => items.value.length)

const fetchPending = async () => {
  loading.value = true
  try {
    const res = await getPendingResources()
    //{ code, message, data }
    items.value = res.data?.data || []
  } catch (error) {
    ElMessage.error(error.response?.data?.message || 'Failed to load pending resources')
  } finally {
    loading.value = false
  }
}

const formatDate = (value) => {
  if (!value) return '-'
  return new Date(value).toLocaleString()
}

const openDetail = (row) => {
  if (!row?.id) return
  router.push(`/admin/resource-review/${row.id}`)
}

onMounted(fetchPending)
</script>

<style scoped>
.resource-review-center {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.page-header {
  margin-bottom: 0.25rem;
}

.stats-row {
  margin-bottom: 0;
}

.stat-card {
  cursor: default;
}

.table-card {
  overflow: hidden;
}

.table-header .title {
  font-weight: 700;
  font-size: 1rem;
  color: var(--ink);
}

.status-cell {
  display: flex;
  justify-content: center;
}

.status-badge {
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-width: 96px;
  min-height: 44px;
  padding: 0.35rem 0.7rem;
  border-radius: 12px;
  border: 1px solid var(--border);
  line-height: 1.1;
}

.status-badge__title {
  color: var(--ink);
  font-size: var(--text-xs);
  font-weight: 700;
}

.status-badge__meta {
  margin-top: 0.18rem;
  color: var(--ink-soft);
  font-size: 0.72rem;
  font-weight: 700;
  letter-spacing: 0.04em;
  text-transform: uppercase;
}

.status-badge--info {
  background: color-mix(in srgb, var(--surface-raised) 86%, var(--bg-accent) 14%);
  border-color: color-mix(in srgb, var(--border-strong) 70%, white 30%);
}

.status-badge--warning {
  background: color-mix(in srgb, #f59e0b 10%, var(--surface-raised) 90%);
  border-color: color-mix(in srgb, #f59e0b 30%, var(--border) 70%);
}

/* Table styling */
.clickable-table :deep(.el-table__row) {
  cursor: pointer;
  transition: background-color 0.15s ease;
}

.clickable-table :deep(.el-table__row:hover) {
  background-color: color-mix(in srgb, var(--accent) 5%, transparent);
}

</style>
