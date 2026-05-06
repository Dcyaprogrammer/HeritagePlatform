<template>
  <div class="admin-page user-management">
    <!-- Page Header -->
    <div class="admin-page-header page-header">
      <div class="admin-page-copy">
        <h2>User Management</h2>
        <p class="admin-page-subtitle subtitle">Manage roles, contributor status, and profile visibility from one place.</p>
      </div>
      <el-button type="primary" @click="fetchUserList" :loading="loading">
        Refresh
      </el-button>
    </div>

    <!-- Search and Filter Section -->
    <el-card class="admin-filter-card filter-card">
      <el-form :inline="true" :model="searchForm" class="admin-filter-form filter-form">
        <el-form-item label="Role">
          <el-select v-model="searchForm.role" placeholder="All Roles" clearable @change="handleRoleChange" class="role-select">
            <el-option label="All" value="" />
            <el-option label="Admin" value="ADMIN" />
            <el-option label="Contributor" value="CONTRIBUTOR" />
            <el-option label="Viewer" value="VIEWER" />
          </el-select>
        </el-form-item>

        <el-form-item label="Search">
          <el-input v-model="searchForm.keyword" placeholder="Username" clearable @keyup.enter="handleSearch" @clear="handleSearch" class="search-input">
            <template #prefix>
              <el-icon>
                <Search />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon>
              <Search />
            </el-icon>
            Search
          </el-button>
          <el-button @click="handleReset">
            <el-icon>
              <RefreshRight />
            </el-icon>
            Reset
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- Statistics Cards -->
    <el-row :gutter="20" class="admin-stats-row stats-row">
      <el-col :span="6">
        <el-card class="admin-stat-card stat-card" @click="filterByRole('')">
          <div class="admin-stat-value stat-value">{{ stats.total }}</div>
          <div class="admin-stat-label stat-label">Total Users</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="admin-stat-card stat-card" @click="filterByRole('ADMIN')">
          <div class="admin-stat-value stat-value">{{ stats.admin }}</div>
          <div class="admin-stat-label stat-label">Admins</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="admin-stat-card stat-card" @click="filterByRole('CONTRIBUTOR')">
          <div class="admin-stat-value stat-value">{{ stats.contributor }}</div>
          <div class="admin-stat-label stat-label">Contributors</div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="admin-stat-card stat-card" @click="filterByRole('VIEWER')">
          <div class="admin-stat-value stat-value">{{ stats.viewer }}</div>
          <div class="admin-stat-label stat-label">Viewers</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- User Table -->
    <el-card class="admin-table-card table-card">
      <el-table :data="userList" v-loading="loading" stripe style="width: 100%" class="admin-table user-table">
        <el-table-column type="index" width="50" />

        <el-table-column label="User" min-width="200">
          <template #default="{ row }">
            <div class="user-info">
              <el-avatar :size="40" :src="row.avatar || defaultAvatar" @error="handleAvatarError" />
              <div class="user-details">
                <div class="username">{{ row.username }}</div>
                <div class="display-name">{{ row.displayName }}</div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="Roles" width="150">
          <template #default="{ row }">
            <div class="roles-container">
              <el-tag v-for="role in row.roles" :key="role" :type="getRoleType(role)" size="small" class="role-tag">
                {{ role }}
              </el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="Contributor Status" width="150">
          <template #default="{ row }">
            <el-tag v-if="row.contributorStatus && row.contributorStatus !== 'NONE'"
              :type="getStatusType(row.contributorStatus)" size="small">
              {{ row.contributorStatus }}
            </el-tag>
            <span v-else class="no-status">-</span>
          </template>
        </el-table-column>

        <el-table-column label="Application Reason" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.contributorReason || '-' }}
          </template>
        </el-table-column>

        <el-table-column label="Created" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>

        <el-table-column label="Actions" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="handleView(row)">
              <el-icon>
                <View />
              </el-icon>
              View
            </el-button>
            <el-button size="small" type="primary" @click="handleEditRole(row)">
              <el-icon>
                <Edit />
              </el-icon>
              Edit
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- Pagination -->
      <div class="admin-pagination pagination-container">
        <el-pagination v-model:current-page="pagination.page" v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]" :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper" @size-change="handleSizeChange"
          @current-change="handlePageChange" />
      </div>
    </el-card>

    <!-- View User Detail Drawer -->
    <el-drawer v-model="drawerVisible" title="User Details" size="400px">
      <div v-if="selectedUser" class="user-detail">
        <div class="detail-header">
          <el-avatar :size="80" :src="selectedUser.avatar || defaultAvatar" />
          <h3>{{ selectedUser.username }}</h3>
          <div class="roles-container">
            <el-tag v-for="role in selectedUser.roles" :key="role" :type="getRoleType(role)">
              {{ role }}
            </el-tag>
          </div>
        </div>

        <el-descriptions :column="1" border>
          <el-descriptions-item label="Display Name">
            {{ selectedUser.displayName }}
          </el-descriptions-item>
          <el-descriptions-item label="Email">
            {{ selectedUser.email || 'Not set' }}
          </el-descriptions-item>
          <el-descriptions-item label="Bio">
            {{ selectedUser.bio || 'No bio' }}
          </el-descriptions-item>
          <el-descriptions-item label="Contributor Status">
            <el-tag v-if="selectedUser.contributorStatus && selectedUser.contributorStatus !== 'NONE'"
              :type="getStatusType(selectedUser.contributorStatus)">
              {{ selectedUser.contributorStatus }}
            </el-tag>
            <span v-else>-</span>
          </el-descriptions-item>
          <el-descriptions-item label="Application Reason" v-if="selectedUser.contributorReason">
            {{ selectedUser.contributorReason }}
          </el-descriptions-item>
          <el-descriptions-item label="Created At">
            {{ formatDate(selectedUser.createdAt) }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-drawer>

    <!-- Edit Role Dialog -->
    <el-dialog v-model="roleDialogVisible" title="Edit User Role" width="400px">
      <div v-if="editingUser" class="role-edit-content">
        <p>Change role for user: <strong>{{ editingUser.username }}</strong></p>
        <p>Current roles:
          <el-tag v-for="role in editingUser.roles" :key="role" :type="getRoleType(role)" size="small">
            {{ role }}
          </el-tag>
        </p>

        <el-form :model="roleForm" label-width="100px">
          <el-form-item label="New Role">
            <el-select v-model="roleForm.role" placeholder="Select role">
              <el-option label="Admin" value="ADMIN" />
              <el-option label="Contributor" value="CONTRIBUTOR" />
              <el-option label="Viewer" value="VIEWER" />
            </el-select>
          </el-form-item>
        </el-form>
      </div>

      <template #footer>
        <el-button @click="roleDialogVisible = false">Cancel</el-button>
        <el-button type="primary" @click="confirmUpdateRole" :loading="updatingRole">
          Confirm
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  RefreshRight,
  View,
  Edit
} from '@element-plus/icons-vue'
import { getUserPage, getUserById, updateUserRole } from '../../api/user.js'

// Default avatar for users without avatar
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// Loading states
const loading = ref(false)
const updatingRole = ref(false)

// User list data
const userList = ref([])

// Search form
const searchForm = reactive({
  role: '',
  keyword: ''
})

// Pagination
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// Statistics
const stats = reactive({
  total: 0,
  admin: 0,
  contributor: 0,
  viewer: 0
})

// Drawer for viewing user details
const drawerVisible = ref(false)
const selectedUser = ref(null)

// Dialog for editing role
const roleDialogVisible = ref(false)
const editingUser = ref(null)
const roleForm = reactive({
  role: ''
})

// Fetch user list from API
const fetchUserList = async () => {
  loading.value = true
  try {
    const params = {
      page: pagination.page - 1,
      size: pagination.size
    }

    if (searchForm.role) {
      params.role = searchForm.role
    }

    if (searchForm.keyword) {
      params.keyword = searchForm.keyword
    }

    const res = await getUserPage(params)

    userList.value = res.data.data.content || []
    pagination.total = res.data.data.totalElements || 0

    updateStats()
  } catch (error) {
    console.error('Failed to fetch user list:', error)
    ElMessage.error('Failed to load user list')
  } finally {
    loading.value = false
  }
}

// Update statistics
const updateStats = () => {
  stats.total = pagination.total
  stats.admin = userList.value.filter(u => u.roles?.includes('ADMIN')).length
  stats.contributor = userList.value.filter(u => u.roles?.includes('CONTRIBUTOR')).length
  stats.viewer = userList.value.filter(u => u.roles?.includes('VIEWER')).length
}

// Handle search
const handleSearch = () => {
  pagination.page = 1
  fetchUserList()
}

// Handle reset
const handleReset = () => {
  searchForm.role = ''
  searchForm.keyword = ''
  pagination.page = 1
  fetchUserList()
}

// Handle role filter change
const handleRoleChange = () => {
  pagination.page = 1
  fetchUserList()
}

// Filter by role from stats cards
const filterByRole = (role) => {
  searchForm.role = role
  pagination.page = 1
  fetchUserList()
}

// Handle page size change
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  fetchUserList()
}

// Handle page change
const handlePageChange = (page) => {
  pagination.page = page
  fetchUserList()
}

// View user details
const handleView = async (row) => {
  try {
    const res = await getUserById(row.id)
    selectedUser.value = res.data.data
    drawerVisible.value = true
  } catch (error) {
    ElMessage.error('Failed to load user details')
  }
}

// Edit user role
const handleEditRole = (row) => {
  editingUser.value = row
  // Determine current primary role
  if (row.roles?.includes('ADMIN')) {
    roleForm.role = 'ADMIN'
  } else if (row.roles?.includes('CONTRIBUTOR')) {
    roleForm.role = 'CONTRIBUTOR'
  } else {
    roleForm.role = 'VIEWER'
  }
  roleDialogVisible.value = true
}

// Confirm update role
const confirmUpdateRole = async () => {
  if (!roleForm.role) {
    ElMessage.warning('Please select a role')
    return
  }

  const currentRole = editingUser.value.roles?.includes('ADMIN') ? 'ADMIN' :
    editingUser.value.roles?.includes('CONTRIBUTOR') ? 'CONTRIBUTOR' : 'VIEWER'

  if (roleForm.role === currentRole) {
    ElMessage.info('No changes made')
    roleDialogVisible.value = false
    return
  }

  try {
    await ElMessageBox.confirm(
      `Change role from ${currentRole} to ${roleForm.role}?`,
      'Confirm Role Change',
      {
        confirmButtonText: 'Confirm',
        cancelButtonText: 'Cancel',
        type: 'warning'
      }
    )

    updatingRole.value = true
    await updateUserRole(editingUser.value.id, roleForm.role)

    ElMessage.success('Role updated successfully')
    roleDialogVisible.value = false

    fetchUserList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Failed to update role:', error)
      ElMessage.error(error.response?.data?.message || 'Failed to update role')
    }
  } finally {
    updatingRole.value = false
  }
}

// Get tag type for role
const getRoleType = (role) => {
  const typeMap = {
    'ADMIN': 'danger',
    'CONTRIBUTOR': 'success',
    'VIEWER': 'info'
  }
  return typeMap[role] || 'info'
}

// Get tag type for contributor status
const getStatusType = (status) => {
  const typeMap = {
    'NONE': 'info',
    'PENDING': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger'
  }
  return typeMap[status] || 'info'
}

// Format date
const formatDate = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// Handle avatar load error
const handleAvatarError = () => {
  // Avatar component has default fallback
}

// Initialize
onMounted(() => {
  fetchUserList()
})
</script>

<style scoped>
.user-management {
}

.filter-card {
  margin-bottom: 24px;
}

/* Set width for select and search */
.role-select {
  width: 200px;
  min-width: 200px;
}

.search-input {
  width: 240px;
  min-width: 240px;
}

.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  cursor: pointer;
}

.table-card {
  overflow: hidden;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-details {
  display: flex;
  flex-direction: column;
}

.username {
  font-weight: 600;
  color: var(--ink);
}

.display-name {
  font-size: var(--text-xs);
  color: var(--ink-soft);
  font-weight: 500;
}

.roles-container {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.role-tag {
  margin-right: 4px;
}

.no-status {
  color: var(--ink-soft);
  font-weight: 500;
}

.user-detail {
  padding: 20px;
}

.detail-header {
  text-align: center;
  margin-bottom: 30px;
}

.detail-header h3 {
  margin: 15px 0 10px;
  color: var(--ink);
}

.role-edit-content {
  padding: 10px;
}

.role-edit-content p {
  margin-bottom: 15px;
  color: var(--ink-soft);
  font-size: var(--text-sm);
  line-height: 1.6;
}
</style>
