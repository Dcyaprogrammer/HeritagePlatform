<template>
  <div class="admin-resources">
    <div class="page-header">
      <div>
        <h2>All Resources</h2>
        <p class="subtitle">Archive / restore approved resources</p>
      </div>
      <el-button type="primary" @click="fetchList" :loading="loading">
        <el-icon><Refresh /></el-icon>
        Refresh
      </el-button>
    </div>

    <el-card class="filter-card">
      <el-form :inline="true" class="filter-form">
        <el-form-item label="Status">
          <el-select v-model="filters.status" placeholder="All" clearable @change="handleFilterChange">
            <el-option label="Approved" value="APPROVED" />
            <el-option label="Archived" value="ARCHIVED" />
            <el-option label="Pending review" value="PENDING_REVIEW" />
            <el-option label="Rejected" value="REJECTED" />
            <el-option label="Draft" value="DRAFT" />
          </el-select>
        </el-form-item>

        <el-form-item label="Category">
          <el-select v-model="filters.categoryId" placeholder="All" clearable filterable @change="handleFilterChange">
            <el-option
              v-for="c in categories"
              :key="c.id"
              :label="c.name"
              :value="c.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="Keyword">
          <el-input
            v-model="filters.q"
            placeholder="Title keyword"
            clearable
            @keyup.enter="handleFilterChange"
          />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleFilterChange">Search</el-button>
          <el-button @click="handleReset">Reset</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card">
      <el-table :data="items" v-loading="loading" stripe style="width: 100%">
        <el-table-column type="index" width="50" />
        <el-table-column prop="title" label="Title" min-width="260" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="Category" width="160" show-overflow-tooltip />
        <el-table-column label="Status" width="140">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="Updated At" width="200">
          <template #default="{ row }">
            {{ formatDate(row.updatedAt) }}
          </template>
        </el-table-column>
        <el-table-column label="Actions" width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              v-if="row.status === 'APPROVED'"
              size="small"
              type="warning"
              :loading="actingId === row.id"
              @click.stop="confirmArchive(row)"
            >
              Archive
            </el-button>
            <el-button
              v-else-if="row.status === 'ARCHIVED'"
              size="small"
              type="success"
              :loading="actingId === row.id"
              @click.stop="confirmRestore(row)"
            >
              Restore
            </el-button>
            <span v-else class="muted">-</span>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handlePageSize"
          @current-change="handlePage"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import api, { archiveResource, getAdminResources, restoreResource } from '../../api/user.js'

const loading = ref(false)
const items = ref([])
const actingId = ref(null)

const filters = reactive({
  status: 'APPROVED',
  categoryId: null,
  q: ''
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

const categories = ref([])

const fetchCategories = async () => {
  try {
    const res = await api.get('/public/categories')
    categories.value = res.data?.data || []
  } catch {
    categories.value = []
  }
}

const fetchList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page - 1,
      size: pagination.size
    }
    if (filters.status) params.status = filters.status
    if (filters.q) params.q = filters.q
    if (filters.categoryId != null) params.categoryId = filters.categoryId

    const res = await getAdminResources(params)
    const data = res.data?.data
    items.value = data?.items || []
    pagination.total = data?.total || 0
  } catch (error) {
    ElMessage.error(error.response?.data?.message || 'Failed to load resources')
  } finally {
    loading.value = false
  }
}

const handleFilterChange = () => {
  pagination.page = 1
  fetchList()
}

const handleReset = () => {
  filters.status = 'APPROVED'
  filters.categoryId = null
  filters.q = ''
  pagination.page = 1
  fetchList()
}

const handlePageSize = (size) => {
  pagination.size = size
  pagination.page = 1
  fetchList()
}

const handlePage = (page) => {
  pagination.page = page
  fetchList()
}

const confirmArchive = async (row) => {
  try {
    await ElMessageBox.confirm(
      `Archive resource \"${row.title}\"? It will be hidden from the public hall.`,
      'Confirm Archive',
      { type: 'warning', confirmButtonText: 'Archive', cancelButtonText: 'Cancel' }
    )
  } catch {
    return
  }

  actingId.value = row.id
  try {
    await archiveResource(row.id, row.version)
    ElMessage.success('Archived')
    fetchList()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || 'Archive failed')
  } finally {
    actingId.value = null
  }
}

const confirmRestore = async (row) => {
  try {
    await ElMessageBox.confirm(
      `Restore resource \"${row.title}\"? It will be visible in the public hall.`,
      'Confirm Restore',
      { type: 'info', confirmButtonText: 'Restore', cancelButtonText: 'Cancel' }
    )
  } catch {
    return
  }

  actingId.value = row.id
  try {
    await restoreResource(row.id, row.version)
    ElMessage.success('Restored')
    fetchList()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || 'Restore failed')
  } finally {
    actingId.value = null
  }
}

const statusTagType = (status) => {
  if (status === 'APPROVED') return 'success'
  if (status === 'ARCHIVED') return 'warning'
  if (status === 'PENDING_REVIEW') return 'info'
  if (status === 'REJECTED') return 'danger'
  return ''
}

const formatDate = (value) => {
  if (!value) return '-'
  return new Date(value).toLocaleString()
}

onMounted(async () => {
  await fetchCategories()
  await fetchList()
})
</script>

<style scoped>
.admin-resources {
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

.filter-card {
  border-radius: 10px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.table-card {
  border-radius: 10px;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.muted {
  color: #c0c4cc;
}
</style>

