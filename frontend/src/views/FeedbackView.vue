<script setup>
import { onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getFeedback, getReviewHistory, resubmitResource } from '../api/resource.js'

const route = useRoute()
const router = useRouter()

const loading = ref(true)
const resubmitting = ref(false)
const feedback = ref(null)
const history = ref([])

async function loadPage() {
  loading.value = true
  try {
    const [feedbackRes, historyRes] = await Promise.all([
      getFeedback(route.params.id),
      getReviewHistory(route.params.id)
    ])
    feedback.value = feedbackRes.data.data
    history.value = historyRes.data.data || []
  } catch (error) {
    console.error('Failed to load feedback page:', error)
    ElMessage.error(error.response?.data?.message || 'Failed to load feedback details')
  } finally {
    loading.value = false
  }
}

async function handleResubmit() {
  resubmitting.value = true
  try {
    await resubmitResource(route.params.id)
    ElMessage.success('Resource resubmitted successfully')
    router.push('/resources/submissions')
  } catch (error) {
    console.error('Failed to resubmit from feedback page:', error)
    ElMessage.error(error.response?.data?.message || 'Failed to resubmit resource')
  } finally {
    resubmitting.value = false
  }
}

onMounted(() => {
  loadPage()
})
</script>

<template>
  <div class="page" v-loading="loading">
    <div class="page-header">
      <div>
        <h1>Feedback Detail</h1>
      </div>
      <div class="header-actions">
        <el-button @click="router.push('/resources/submissions')">Back to Submissions</el-button>
        <el-button type="warning" :loading="resubmitting" @click="handleResubmit">Resubmit</el-button>
      </div>
    </div>

    <el-card shadow="never" class="card section">
      <h2>Latest Rejection Feedback</h2>
      <template v-if="feedback">
        <p class="reason">{{ feedback.reason || 'No rejection reason provided.' }}</p>
        <p class="meta"><strong>Reviewed At:</strong> {{ feedback.operatedAt }}</p>
      </template>
      <el-empty v-else description="No feedback found." />
    </el-card>

    <el-card shadow="never" class="card section">
      <h2>Review History</h2>
      <el-table v-if="history.length" :data="history" stripe>
        <el-table-column prop="action" label="Action" width="140">
          <template #default="{ row }">
            <span class="action-text" :class="row.action">{{ row.action }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="reason" label="Comment" min-width="320" />
        <el-table-column prop="operatedAt" label="Operated At" min-width="220" />
      </el-table>
      <el-empty v-else description="No review history found." />
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

.header-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.card {
  border: 1px solid #e5e7eb;
}

.section {
  margin-bottom: 20px;
}

.section h2 {
  margin: 0 0 16px;
  font-size: 18px;
  color: #1f2937;
}

.reason {
  margin: 0 0 12px;
  color: #1f2937;
  line-height: 1.8;
  white-space: pre-wrap;
}

.meta {
  margin: 0;
  color: #64748b;
}

.action-text {
  font-weight: 600;
}

.action-text.REJECTED {
  color: #b91c1c;
}

.action-text.APPROVED {
  color: #15803d;
}
</style>
