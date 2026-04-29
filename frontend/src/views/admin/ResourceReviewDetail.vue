<template>
  <div v-if="loading" class="page inner notfound">
    <p>Loading resource...</p>
  </div>

  <div v-else-if="detail" class="page inner resource-review-detail">
    <nav class="crumb" aria-label="Breadcrumb">
      <RouterLink to="/admin/resource-review">Resource Review Center</RouterLink>
      <span class="sep" aria-hidden="true">/</span>
      <span class="current">{{ detail.title }}</span>
    </nav>

    <ResourceImageCarousel :attachments="detail.attachments || []" />

    <!-- Audio player section -->
    <section v-if="audioAttachments.length" class="media-section audio-section" aria-label="Audio">
      <h3 class="media-heading">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M12 3v10.55c-.59-.34-1.27-.55-2-.55-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4V7h4V3h-6z"/></svg>
        Audio
      </h3>
      <ul class="audio-list">
        <li v-for="audio in audioAttachments" :key="audio.id" class="audio-item">
          <span class="audio-name">{{ audio.display_name || audio.displayName || 'Audio file' }}</span>
          <CustomAudioPlayer :src="getAttachmentSrc(audio)" :name="audio.display_name || audio.displayName || 'Audio file'" />
        </li>
      </ul>
    </section>

    <!-- File download section -->
    <section v-if="fileAttachments.length" class="media-section files-section" aria-label="Files">
      <h3 class="media-heading">
        <svg width="18" height="18" viewBox="0 0 24 24" fill="currentColor"><path d="M6 2c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V8l-6-6H6zm7 7V3.5L18.5 9H13zM8 12h8v2H8v-2zm0 4h8v2H8v-2z"/></svg>
        Documents &amp; Files
      </h3>
      <ul class="file-list">
        <li v-for="file in fileAttachments" :key="file.id" class="file-item">
          <span class="file-type-badge" :style="{ color: file.meta.color, background: file.meta.bg }">
            {{ file.meta.label }}
          </span>
          <a :href="getAttachmentSrc(file)" :download="file.display_name || file.displayName" class="file-name" target="_blank" rel="noopener">
            {{ file.display_name || file.displayName || 'Download file' }}
          </a>
        </li>
      </ul>
    </section>

    <article class="article">
      <header class="head">
        <h1 class="title">{{ detail.title }}</h1>
        <div class="meta">
          <span v-if="detail.category" class="pill">{{ detail.category }}</span>
          <span v-if="detail.locationName" class="loc">{{ detail.locationName }}</span>
        </div>
        <ul v-if="detail.tags && detail.tags.length" class="tags">
          <li v-for="tag in detail.tags" :key="tag.id" class="tag">{{ tag.name }}</li>
        </ul>
      </header>

      <div class="prose">
        <p class="desc">{{ detail.description || '-' }}</p>
        <p v-if="detail.copyrightDeclaration" class="legal">
          <strong>Copyright &amp; usage</strong><br />
          {{ detail.copyrightDeclaration }}
        </p>
        <dl class="facts">
          <div class="fact">
            <dt>Submitter</dt>
            <dd>{{ detail.submitterName || '-' }}</dd>
          </div>
          <div class="fact">
            <dt>Status</dt>
            <dd>{{ detail.status || '-' }}</dd>
          </div>
          <div class="fact">
            <dt>Submitted</dt>
            <dd>{{ formatDate(detail.submittedAt) }}</dd>
          </div>
          <div class="fact">
            <dt>Version</dt>
            <dd>{{ detail.version ?? '-' }}</dd>
          </div>
        </dl>
      </div>
    </article>

    <el-card class="action-card">
      <template #header>
        <span class="card-title">Review Actions</span>
      </template>

      <div class="actions">
        <el-button @click="goBack">Back to List</el-button>
        <el-button type="success" :loading="approving" :disabled="!canReview" @click="handleApprove">
          Approve
        </el-button>
        <el-button type="danger" :loading="rejecting" :disabled="!canReview" @click="openRejectDialog">
          Reject
        </el-button>
      </div>
      <p v-if="detail.rejectionReason" class="reject-tip">
        Previous rejection reason: {{ detail.rejectionReason }}
      </p>
    </el-card>

    <el-dialog v-model="rejectDialogVisible" title="Reject Resource" width="500px">
      <el-form label-position="top">
        <el-form-item label="Rejection Reason (required)">
          <el-input
            v-model="rejectionReason"
            type="textarea"
            :rows="4"
            maxlength="4000"
            show-word-limit
            placeholder="Please explain why this resource is rejected"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="rejectDialogVisible = false">Cancel</el-button>
        <el-button type="danger" :loading="rejecting" @click="handleReject">Confirm Reject</el-button>
      </template>
    </el-dialog>
  </div>

  <div v-else class="page inner notfound">
    <h1>Resource not found</h1>
    <p>This pending resource does not exist or is no longer available.</p>
    <RouterLink to="/admin/resource-review">Back to Review Center</RouterLink>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { approveResource, getResourceForReview, rejectResource } from '../../api/user.js'
import ResourceImageCarousel from '../../components/ResourceImageCarousel.vue'
import { getAttachmentType, getAttachmentSrc, filterByType, getTypeMeta } from '../../utils/attachmentUtils.js'
import CustomAudioPlayer from '../../components/CustomAudioPlayer.vue'

const route = useRoute()
const router = useRouter()

const detail = ref(null)
const loading = ref(false)
const approving = ref(false)
const rejecting = ref(false)
const rejectDialogVisible = ref(false)
const rejectionReason = ref('')

const canReview = computed(() => !!detail.value && detail.value.status === 'PENDING_REVIEW')

const audioAttachments = computed(() => filterByType(detail.value?.attachments, 'audio'))
const fileAttachments = computed(() =>
  (detail.value?.attachments || [])
    .filter(a => !['image', 'video', 'audio'].includes(getAttachmentType(a)))
    .map(a => ({ ...a, meta: getTypeMeta(getAttachmentType(a)) }))
)

const fetchDetail = async () => {
  loading.value = true
  try {
    const res = await getResourceForReview(route.params.id)
    detail.value = res.data?.data || null
  } catch (error) {
    ElMessage.error(error.response?.data?.message || 'Failed to load resource detail')
    detail.value = null
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.push('/admin/resource-review')
}

const handleApprove = async () => {
  if (!detail.value) return
  approving.value = true
  try {
    await approveResource(detail.value.id, detail.value.version)
    ElMessage.success('Approved successfully')
    goBack()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || 'Approve failed')
  } finally {
    approving.value = false
  }
}

const openRejectDialog = () => {
  rejectionReason.value = ''
  rejectDialogVisible.value = true
}

const handleReject = async () => {
  if (!detail.value) return
  if (!rejectionReason.value.trim()) {
    ElMessage.warning('Rejection reason is required')
    return
  }
  rejecting.value = true
  try {
    await rejectResource(detail.value.id, detail.value.version, rejectionReason.value.trim())
    ElMessage.success('Rejected successfully')
    rejectDialogVisible.value = false
    goBack()
  } catch (error) {
    ElMessage.error(error.response?.data?.message || 'Reject failed')
  } finally {
    rejecting.value = false
  }
}

const formatDate = (value) => {
  if (!value) return '-'
  return new Date(value).toLocaleString()
}

onMounted(fetchDetail)
</script>

<style scoped>
.inner {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 1.25rem;
}
.resource-review-detail {
  padding-bottom: 2rem;
}
.notfound {
  padding-top: 4rem;
  text-align: center;
}
.crumb {
  font-size: 0.875rem;
  color: var(--muted);
  margin-bottom: 1.25rem;
  margin-top: 1rem;
}
.crumb a {
  color: var(--accent);
}
.sep {
  margin: 0 0.35rem;
  opacity: 0.6;
}
.current {
  color: var(--ink);
}
.article {
  margin-top: 1.75rem;
}
.head {
  margin-bottom: 1.25rem;
}
.title {
  margin: 0 0 0.65rem;
  font-family: var(--font-serif);
  font-size: clamp(1.5rem, 3vw, 2rem);
  font-weight: 700;
  line-height: 1.3;
}
.meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.5rem 0.75rem;
  margin-bottom: 0.65rem;
}
.pill {
  font-size: 0.8125rem;
  font-weight: 600;
  padding: 0.2rem 0.55rem;
  border-radius: 6px;
  background: color-mix(in srgb, var(--accent) 12%, var(--surface));
  color: var(--accent);
  border: 1px solid color-mix(in srgb, var(--accent) 28%, var(--border));
}
.loc {
  font-size: 0.875rem;
  color: var(--muted);
}

.action-card {
  margin-top: 1.5rem;
  border-radius: var(--radius);
}

.card-title {
  font-weight: 600;
  color: #303133;
}

.prose {
  margin-top: 1.5rem;
}
.desc {
  font-size: 1.05rem;
  line-height: 1.6;
  margin-bottom: 1.5rem;
}
.legal {
  background: var(--surface);
  padding: 1rem;
  border-left: 4px solid var(--border);
  font-size: 0.875rem;
  margin-bottom: 1.5rem;
}
.tags {
  list-style: none;
  display: flex;
  flex-wrap: wrap;
  gap: 0.35rem;
  padding: 0;
  margin: 0;
}
.tag {
  font-size: 0.75rem;
  padding: 0.25rem 0.6rem;
  border-radius: 999px;
  background: #1d4ed8;
  color: #ffffff;
  border: none;
  font-weight: 500;
}
.facts {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 1rem;
  background: var(--surface);
  padding: 1.25rem;
  border-radius: var(--radius);
  border: 1px solid var(--border);
}
.fact dt {
  font-size: 0.75rem;
  color: var(--muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 0.25rem;
}
.fact dd {
  margin: 0;
  font-weight: 500;
}

.actions {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
}

.reject-tip {
  margin: 12px 0 0;
  color: #909399;
  font-size: 13px;
}

/* Media sections */
.media-section {
  margin-top: 1.5rem;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: 1.1rem 1.25rem;
}
.media-heading {
  margin: 0 0 0.85rem;
  font-size: 0.9375rem;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 0.45rem;
  color: var(--ink);
}

/* Audio */
.audio-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 0.85rem;
}
.audio-item {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 0.4rem;
}
.audio-name {
  font-size: 0.8125rem;
  color: var(--muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  padding-left: 2px;
}

/* Files */
.file-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 0.6rem;
}
.file-item {
  display: flex;
  align-items: center;
  gap: 0.7rem;
}
.file-type-badge {
  flex: 0 0 auto;
  font-size: 0.7rem;
  font-weight: 700;
  padding: 0.2rem 0.5rem;
  border-radius: 5px;
  letter-spacing: 0.02em;
}
.file-name {
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--accent);
  text-decoration: none;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.file-name:hover { text-decoration: underline; }
</style>

