<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  deleteResource,
  getMySubmissions,
  submitForReview,
} from '../api/resource.js'

const router = useRouter()
const loading = ref(false)
const actionLoadingId = ref(null)
const submissions = ref([])

const draftCount = computed(() =>
  submissions.value.filter((item) => item.status === 'DRAFT').length,
)

const statusLabelMap = {
  APPROVED: 'Approved',
  REJECTED: 'Rejected',
  PENDING: 'Pending',
  PENDING_REVIEW: 'Pending',
  DRAFT: 'Draft',
  ARCHIVED: 'Archived'
}

async function loadSubmissions() {
  loading.value = true
  try {
    const res = await getMySubmissions()
    submissions.value = res.data.data || []
  } catch (error) {
    console.error('Failed to load submissions:', error)
    ElMessage.error(error.response?.data?.message || 'Failed to load submissions')
  } finally {
    loading.value = false
  }
}

function openFeedback(id) {
  router.push(`/resources/${id}/feedback`)
}

function isDraft(row) {
  return row.status === 'DRAFT'
}

function isRejected(row) {
  return row.status === 'REJECTED'
}

function canEdit(row) {
  return isDraft(row) || isRejected(row)
}

function canDelete(row) {
  return row.status !== 'APPROVED' && row.status !== 'PENDING_REVIEW'
}

function handleEdit(row) {
  if (!canEdit(row)) {
    return
  }
  router.push(`/resources/${row.id}/edit`)
}

async function handleSubmit(row) {
  if (!isDraft(row)) {
    return
  }

  try {
    await ElMessageBox.confirm(
      'This will submit the draft for admin review. Continue?',
      'Submit Draft',
      {
        confirmButtonText: 'Submit',
        cancelButtonText: 'Cancel',
        type: 'warning',
      },
    )
  } catch {
    return
  }

  actionLoadingId.value = row.id
  try {
    const res = await submitForReview(row.id)
    ElMessage.success(res.data?.message || 'Submitted successfully')
    await loadSubmissions()
  } catch (error) {
    console.error('Failed to submit draft:', error)
    ElMessage.error(error.response?.data?.message || 'Failed to submit draft')
  } finally {
    actionLoadingId.value = null
  }
}

async function handleDelete(row) {
  if (!canDelete(row)) {
    return
  }

  try {
    await ElMessageBox.confirm(
      'This will permanently delete the resource and its attachments. Continue?',
      'Delete Resource',
      {
        confirmButtonText: 'Delete',
        cancelButtonText: 'Cancel',
        type: 'warning',
      },
    )
  } catch {
    return
  }

  actionLoadingId.value = row.id
  try {
    const res = await deleteResource(row.id)
    ElMessage.success(res.data?.message || 'Deleted successfully')
    await loadSubmissions()
  } catch (error) {
    console.error('Failed to delete resource:', error)
    ElMessage.error(error.response?.data?.message || 'Failed to delete resource')
  } finally {
    actionLoadingId.value = null
  }
}

onMounted(() => {
  loadSubmissions()
})
</script>

<template>
  <div class="page">
    <div class="page-header">
      <div>
        <h1>My Submissions</h1>
      </div>
      <el-button @click="router.push('/')">Back to Hall</el-button>
    </div>

    <el-card shadow="never" class="card">
      <div class="summary">
        <span>Total submissions: {{ submissions.length }}</span>
        <span class="summary-divider">|</span>
        <span>Drafts awaiting action: {{ draftCount }}</span>
      </div>

      <el-table
        v-loading="loading"
        :data="submissions"
        empty-text="No submissions found yet."
        stripe
      >
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="title" label="Title" min-width="240" />
        <el-table-column prop="category" label="Category" min-width="130" />
        <el-table-column label="Status" width="150">
          <template #default="{ row }">
            <span class="status-text" :class="row.status">
              {{ statusLabelMap[row.status] || row.status }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="submittedAt" label="Updated At" min-width="210" />
        <el-table-column label="Actions" min-width="240">
          <template #default="{ row }">
            <div class="actions">
              <button
                v-if="canEdit(row)"
                class="link-button"
                @click="handleEdit(row)"
              >
                Edit
              </button>
              <button
                v-if="isDraft(row)"
                class="link-button"
                :disabled="actionLoadingId === row.id"
                @click="handleSubmit(row)"
              >
                {{ actionLoadingId === row.id ? 'Submitting...' : 'Submit' }}
              </button>
              <button
                v-if="row.canViewFeedback"
                class="link-button"
                @click="openFeedback(row.id)"
              >
                View Feedback
              </button>
              <button
                v-if="canDelete(row)"
                class="link-button danger-link"
                :disabled="actionLoadingId === row.id"
                @click="handleDelete(row)"
              >
                {{ actionLoadingId === row.id ? 'Deleting...' : 'Delete' }}
              </button>
              <span
                v-if="!canEdit(row) && !row.canViewFeedback && !canDelete(row)"
                class="muted-action"
              >
                No action available
              </span>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<style scoped>
.page {
  max-width: 1080px;
  margin: 0 auto;
  padding: 32px 20px 48px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
  margin-bottom: 20px;
}

.page-header h1 {
  margin: 0;
  font-size: 30px;
  color: #1f2937;
}

.card {
  border: 1px solid #e5e7eb;
}

.summary {
  margin-bottom: 16px;
  font-size: 14px;
  color: #475569;
}

.summary-divider {
  margin: 0 8px;
  color: #cbd5e1;
}

.status-text {
  font-weight: 600;
}

.actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.link-button {
  border: none;
  background: none;
  padding: 0;
  color: #2563eb;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}

.link-button:disabled {
  color: #94a3b8;
  cursor: not-allowed;
}

.danger-link {
  color: #dc2626;
}

.muted-action {
  color: #94a3b8;
  font-size: 13px;
}

.status-text.REJECTED,
.status-text.Rejected {
  color: #b91c1c;
}

.status-text.PENDING,
.status-text.PENDING_REVIEW,
.status-text.Pending {
  color: #b45309;
}

.status-text.APPROVED,
.status-text.Approved {
  color: #15803d;
}

.status-text.DRAFT {
  color: #4338ca;
}

.status-text.ARCHIVED {
  color: #475569;
}
</style>
