<template>
  <div class="admin-page contributor-review">
    <!-- Page Header -->
    <div class="admin-page-header page-header">
      <div class="admin-page-copy">
        <h2>Contributor Applications</h2>
        <p class="admin-page-subtitle subtitle">Approve or reject contributor access with clear status cues and fast triage.</p>
      </div>
      <el-button type="primary" @click="fetchApplications" :loading="loading">
        Refresh
      </el-button>
    </div>

    <!-- Statistics Cards -->
    <el-row :gutter="20" class="admin-stats-row stats-row">
      <el-col :span="8">
        <el-card class="admin-stat-card stat-card" :class="{ 'is-active': filterStatus === 'PENDING' }" @click="setFilter('PENDING')">
          <div class="admin-stat-value stat-value">{{ stats.pending }}</div>
          <div class="admin-stat-label stat-label">Pending Review</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="admin-stat-card stat-card" :class="{ 'is-active': filterStatus === 'APPROVED' }" @click="setFilter('APPROVED')">
          <div class="admin-stat-value stat-value">{{ stats.approved }}</div>
          <div class="admin-stat-label stat-label">Approved</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="admin-stat-card stat-card" :class="{ 'is-active': filterStatus === 'REJECTED' }" @click="setFilter('REJECTED')">
          <div class="admin-stat-value stat-value">{{ stats.rejected }}</div>
          <div class="admin-stat-label stat-label">Rejected</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Applications Table -->
    <el-card class="admin-table-card table-card">
      <template #header>
        <div class="table-header">
          <span class="title">
            {{ filterStatus === 'ALL' ? 'All Applications' : filterStatus + ' Applications' }}
          </span>
          <el-radio-group v-model="filterStatus" size="small" @change="handleFilterChange">
            <el-radio-button label="ALL">All</el-radio-button>
            <el-radio-button label="PENDING">Pending</el-radio-button>
            <el-radio-button label="APPROVED">Approved</el-radio-button>
            <el-radio-button label="REJECTED">Rejected</el-radio-button>
          </el-radio-group>
        </div>
      </template>

      <el-table :data="filteredApplications" v-loading="loading" stripe style="width: 100%" empty-text="No applications found" class="admin-table">
        <el-table-column type="index" width="50" />

        <el-table-column label="Applicant" min-width="180">
          <template #default="{ row }">
            <div class="applicant-info">
              <el-avatar :size="40" :src="row.avatar || defaultAvatar" />
              <div class="applicant-details">
                <div class="username">{{ row.username }}</div>
                <div class="display-name">{{ row.displayName }}</div>
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="Application Reason" min-width="300">
          <template #default="{ row }">
            <div class="reason-text" :title="row.contributorReason">
              {{ row.contributorReason || 'No reason provided' }}
            </div>
          </template>
        </el-table-column>

        <el-table-column label="Current Roles" width="150">
          <template #default="{ row }">
            <div class="roles-container">
              <el-tag v-for="role in row.roles" :key="role" :type="getRoleType(role)" size="small">
                {{ role }}
              </el-tag>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="Status" width="120">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.contributorStatus)">
              {{ row.contributorStatus }}
            </el-tag>
          </template>
        </el-table-column>

        <el-table-column label="Applied At" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>

        <el-table-column label="Actions" width="200" fixed="right">
          <template #default="{ row }">
            <template v-if="row.contributorStatus === 'PENDING'">
              <el-button size="small" type="success" @click="handleApprove(row)"
                :loading="processing[row.username] === 'approve'">
                <el-icon>
                  <Check />
                </el-icon>
                Approve
              </el-button>
              <el-button size="small" type="danger" @click="handleReject(row)"
                :loading="processing[row.username] === 'reject'">
                <el-icon>
                  <Close />
                </el-icon>
                Reject
              </el-button>
            </template>

            <template v-else>
              <el-button size="small" @click="handleViewDetails(row)">
                <el-icon>
                  <View />
                </el-icon>
                View
              </el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- Application Detail Dialog -->
    <el-dialog v-model="detailDialogVisible" title="Application Details" width="500px">
      <div v-if="selectedApplication" class="application-detail">
        <div class="applicant-header">
          <el-avatar :size="60" :src="selectedApplication.avatar || defaultAvatar" />
          <div class="applicant-summary">
            <h3>{{ selectedApplication.username }}</h3>
            <p>{{ selectedApplication.displayName }}</p>
            <el-tag :type="getStatusType(selectedApplication.contributorStatus)">
              {{ selectedApplication.contributorStatus }}
            </el-tag>
          </div>
        </div>

        <el-divider />

        <div class="detail-section">
          <h4>Application Reason</h4>
          <p class="reason-content">
            {{ selectedApplication.contributorReason || 'No reason provided' }}
          </p>
        </div>

        <el-divider />

        <div class="detail-section">
          <h4>Additional Information</h4>
          <el-descriptions :column="1" border>
            <el-descriptions-item label="Current Roles">
              <div class="roles-container">
                <el-tag v-for="role in selectedApplication.roles" :key="role" :type="getRoleType(role)">
                  {{ role }}
                </el-tag>
              </div>
            </el-descriptions-item>
            <el-descriptions-item label="Email">
              {{ selectedApplication.email || 'Not set' }}
            </el-descriptions-item>
            <el-descriptions-item label="Bio">
              {{ selectedApplication.bio || 'No bio' }}
            </el-descriptions-item>
            <el-descriptions-item label="Member Since">
              {{ formatDate(selectedApplication.createdAt) }}
            </el-descriptions-item>
          </el-descriptions>
        </div>
      </div>

      <template #footer v-if="selectedApplication?.contributorStatus === 'PENDING'">
        <el-button @click="detailDialogVisible = false">Close</el-button>
        <el-button type="danger" @click="confirmReject(selectedApplication)"
          :loading="processing[selectedApplication.username] === 'reject'">
          Reject
        </el-button>
        <el-button type="success" @click="confirmApprove(selectedApplication)"
          :loading="processing[selectedApplication.username] === 'approve'">
          Approve
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Check,
  Close,
  View
} from '@element-plus/icons-vue'
import {
  getAllUsers,
  approveContributor,
  rejectContributor
} from '../../api/user.js'

// Default avatar
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

// Loading state
const loading = ref(false)

// Processing state for individual buttons
const processing = ref({})

// All applications data
const applications = ref([])

// Filter status
const filterStatus = ref('PENDING')

// Statistics
const stats = ref({
  pending: 0,
  approved: 0,
  rejected: 0
})

// Detail dialog
const detailDialogVisible = ref(false)
const selectedApplication = ref(null)

// Filtered applications based on selected status
const filteredApplications = computed(() => {
  if (filterStatus.value === 'ALL') {
    return applications.value
  }
  return applications.value.filter(app => app.contributorStatus === filterStatus.value)
})

// Fetch all applications
const fetchApplications = async () => {
  loading.value = true
  try {
    const res = await getAllUsers()
    const allUsers = res.data.data || []
    
    // Filter users who have applied (status is not null and not NONE)
    applications.value = allUsers.filter(
      user => user.contributorStatus && user.contributorStatus !== 'NONE'
    )

    updateStats()
  } catch (error) {
    console.error('Failed to fetch applications:', error)
    ElMessage.error('Failed to load applications')
  } finally {
    loading.value = false
  }
}

// Update statistics
const updateStats = () => {
  stats.value.pending = applications.value.filter(
    app => app.contributorStatus === 'PENDING'
  ).length
  stats.value.approved = applications.value.filter(
    app => app.contributorStatus === 'APPROVED'
  ).length
  stats.value.rejected = applications.value.filter(
    app => app.contributorStatus === 'REJECTED'
  ).length
}

// Set filter from stat cards
const setFilter = (status) => {
  filterStatus.value = status
}

// Handle filter change
const handleFilterChange = () => {
  // Table will automatically update via computed property
}

// Handle approve
const handleApprove = async (row) => {
  try {
    await ElMessageBox.confirm(
      `Approve ${row.username} as a contributor?`,
      'Confirm Approval',
      {
        confirmButtonText: 'Approve',
        cancelButtonText: 'Cancel',
        type: 'success'
      }
    )

    await confirmApprove(row)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Approval failed:', error)
    }
  }
}

// Confirm approve
const confirmApprove = async (row) => {
  processing.value[row.username] = 'approve'
  try {
    await approveContributor(row.username)

    // Update local data
    const index = applications.value.findIndex(app => app.id === row.id)
    if (index !== -1) {
      applications.value[index].contributorStatus = 'APPROVED'
      applications.value[index].roles = ['VIEWER', 'CONTRIBUTOR']
    }

    ElMessage.success(`${row.username} has been approved as a contributor`)
    updateStats()

    if (detailDialogVisible.value) {
      detailDialogVisible.value = false
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || 'Failed to approve application')
  } finally {
    processing.value[row.username] = null
  }
}

// Handle reject
const handleReject = async (row) => {
  try {
    await ElMessageBox.confirm(
      `Reject ${row.username}'s application?`,
      'Confirm Rejection',
      {
        confirmButtonText: 'Reject',
        cancelButtonText: 'Cancel',
        type: 'warning'
      }
    )

    await confirmReject(row)
  } catch (error) {
    if (error !== 'cancel') {
      console.error('Rejection failed:', error)
    }
  }
}

// Confirm reject
const confirmReject = async (row) => {
  processing.value[row.username] = 'reject'
  try {
    await rejectContributor(row.username)

    // Update local data
    const index = applications.value.findIndex(app => app.id === row.id)
    if (index !== -1) {
      applications.value[index].contributorStatus = 'REJECTED'
    }

    ElMessage.success(`${row.username}'s application has been rejected`)
    updateStats()

    if (detailDialogVisible.value) {
      detailDialogVisible.value = false
    }
  } catch (error) {
    ElMessage.error(error.response?.data?.message || 'Failed to reject application')
  } finally {
    processing.value[row.username] = null
  }
}

// View application details
const handleViewDetails = (row) => {
  selectedApplication.value = row
  detailDialogVisible.value = true
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

// Get tag type for status
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

// Initialize
onMounted(() => {
  fetchApplications()
})
</script>

<style scoped>
.contributor-review {
}

.page-header {
  margin-bottom: 24px;
}

.stats-row {
  margin-bottom: 24px;
}

.stat-card {
  cursor: pointer;
}

.table-card {
  overflow: hidden;
  margin-bottom: 24px;
}

.table-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.table-header .title {
  font-weight: 700;
  font-size: var(--text-sm);
  color: var(--ink);
}

.applicant-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.applicant-details {
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

.reason-text {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  color: var(--ink-soft);
  font-size: var(--text-sm);
  font-weight: 500;
  line-height: 1.55;
}

.applicant-header {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 20px;
}

.applicant-summary h3 {
  margin: 0 0 5px 0;
  color: var(--ink);
}

.applicant-summary p {
  margin: 0 0 8px 0;
  color: var(--ink-soft);
  font-size: var(--text-sm);
  font-weight: 500;
}

.detail-section {
  margin-bottom: 20px;
}

.detail-section h4 {
  margin: 0 0 10px 0;
  color: var(--ink-soft);
  font-size: var(--text-xs);
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.reason-content {
  background-color: color-mix(in srgb, var(--surface) 84%, white 16%);
  padding: 15px;
  border: 1px solid var(--border);
  border-radius: 12px;
  color: var(--ink);
  line-height: 1.6;
  margin: 0;
}
</style>
