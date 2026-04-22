<template>
  <div class="resource-review-center">
    <div class="page-header">
      <div>
        <h2>Resource Review Center</h2>
        <p class="subtitle">Review pending heritage resources</p>
      </div>
      <el-button type="primary" @click="fetchPending" :loading="loading">
        <el-icon><Refresh /></el-icon>
        Refresh
      </el-button>
    </div>

    
    <el-row :gutter="20" class="stats-row">
      <el-col :span="24">
        <el-card class="stat-card pending">
          <div class="stat-icon">
            <el-icon><Timer /></el-icon>
          </div>
          <div class="stat-content">
            <div class="stat-value">{{ pendingCount }}</div>
            <div class="stat-label">Pending Resources</div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    
    <el-card class="table-card">
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
        class="clickable-table"
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

        <el-table-column label="Status" width="130">
          <template #default="{ row }">
            <el-tag :type="row.stale ? 'warning' : 'info'">
              {{ row.stale ? 'Pending > 3 days' : 'Pending' }}
            </el-tag>
          </template>
        </el-table-column>

      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Timer } from '@element-plus/icons-vue'
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
  gap: 16px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-header h2 {
  margin: 0;
  font-size: 22px;
  color: #303133;
}

.subtitle {
  margin: 6px 0 0;
  color: #909399;
  font-size: 14px;
}

.stats-row {
  margin-bottom: 4px;
}

.stat-card {
  cursor: default;
  border-radius: 10px;
}

.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px 20px;
}

.stat-icon {
  width: 44px;
  height: 44px;
  border-radius: 8px;
  background: #ecf5ff;
  color: #409eff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
}

.stat-value {
  font-size: 26px;
  font-weight: 700;
  color: #303133;
  line-height: 1;
}

.stat-label {
  margin-top: 6px;
  color: #909399;
  font-size: 13px;
}

.table-card {
  border-radius: 10px;
}

.table-header .title {
  font-weight: 600;
  color: #303133;
}

.clickable-table :deep(.el-table__row) {
  cursor: pointer;
}
</style>