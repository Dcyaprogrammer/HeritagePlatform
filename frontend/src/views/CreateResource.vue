<template>
  <div class="create-resource-page">
    <div class="header">
      <h1>{{ isEditMode ? 'Edit Heritage Resource' : 'Create New Heritage Resource' }}</h1>
      <p>
        {{
          isEditMode
            ? 'Update your draft before saving it again or submitting it later.'
            : 'Share local culture, places, traditions, stories, or objects.'
        }}
      </p>
    </div>

    <div v-if="pageLoading" class="page-state">Loading draft...</div>

    <form v-else @submit.prevent="handleSaveDraft" class="resource-form">
      <div class="form-group">
        <label for="title">Title *</label>
        <input v-model="form.title" type="text" id="title" required placeholder="Enter an engaging title" />
      </div>

      <div class="form-group">
        <label for="description">Description *</label>
        <textarea v-model="form.description" id="description" rows="5" required placeholder="Describe the heritage resource in detail..."></textarea>
      </div>

      <div class="form-group row-group">
        <div class="col">
          <label for="locationName">Location / Place</label>
          <input v-model="form.locationName" type="text" id="locationName" placeholder="E.g., Beijing, China" />
        </div>
        <div class="col">
          <label for="category">Category</label>
          <select v-model="form.categoryId" id="category">
            <option value="">Select a Category</option>
            <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
          </select>
        </div>
      </div>

      <div class="form-group">
        <label for="heritageType">Heritage Type</label>
        <select v-model="form.heritageTypeCode" id="heritageType">
          <option value="">Select a Heritage Type</option>
          <optgroup v-for="group in heritageTypeGroups" :key="group.groupCode" :label="group.groupName">
            <option v-for="type in group.types" :key="type.code" :value="type.code">
              {{ type.name }}
            </option>
          </optgroup>
        </select>
      </div>

      <div class="form-group">
        <label>Tags</label>
        <div class="tags-container">
          <label v-for="tag in availableTags" :key="tag.id" class="tag-checkbox">
            <input type="checkbox" :value="tag.id" v-model="form.tagIds" />
            <span class="tag-label">{{ tag.name }}</span>
          </label>
        </div>
      </div>

      <div class="form-group">
        <label for="copyright">Copyright & Usage Declaration</label>
        <input v-model="form.copyrightDeclaration" type="text" id="copyright" placeholder="E.g., CC BY-NC 4.0 or All Rights Reserved" />
      </div>

      <div class="form-group">
        <label>Media Attachments (Images, Videos, Documents)</label>
        <FileUploader ref="uploaderRef" :initial-files="initialUploaderFiles" />
        <p class="help-text">
          Please wait for each file upload to finish before saving or submitting.
        </p>
      </div>

      <div class="actions">
        <button type="button" class="btn btn-secondary" @click="goBack">Cancel</button>
        <button type="submit" class="btn btn-primary" :disabled="loading || submitting || hasPendingUploads">
          <span v-if="loading">{{ isEditMode ? 'Updating...' : 'Saving...' }}</span>
          <span v-else>{{ isEditMode ? 'Update Draft' : 'Save as Draft' }}</span>
        </button>
        <button
          type="button"
          class="btn btn-submit"
          :disabled="loading || submitting || hasPendingUploads"
          @click="handleSubmitForReview"
        >
          <span v-if="submitting">Submitting...</span>
          <span v-else>Submit for Review</span>
        </button>
      </div>

      <div v-if="errorMsg" class="error-msg">{{ errorMsg }}</div>
      <div v-if="successMsg" class="success-msg">{{ successMsg }}</div>
    </form>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  createDraft,
  getCategories,
  getHeritageTypeGroups,
  getOwnedResource,
  getTags,
  submitForReview,
  updateDraft,
} from '../api/resource.js'
import FileUploader from '../components/FileUploader.vue'

const route = useRoute()
const router = useRouter()

const form = reactive({
  title: '',
  description: '',
  locationName: '',
  heritageTypeCode: '',
  category: '',
  categoryId: '',
  copyrightDeclaration: '',
  version: null,
  tagIds: [],
  attachmentIds: [],
})

const categories = ref([])
const availableTags = ref([])
const heritageTypeGroups = ref([])
const initialUploaderFiles = ref([])

const uploaderRef = ref(null)
const loading = ref(false)
const submitting = ref(false)
const pageLoading = ref(false)
const errorMsg = ref('')
const successMsg = ref('')
const resourceStatus = ref('DRAFT')

const resourceId = computed(() => {
  const rawId = route.params.id
  return rawId ? Number(rawId) : null
})

const isEditMode = computed(() => Number.isFinite(resourceId.value))

const hasPendingUploads = computed(() => {
  const files = uploaderRef.value?.uploadedFiles || []
  return files.some((file) => !file._removed && (file.uploading || (file.rawFile && !file.attachmentId && !file.uploadError)))
})

function resetMessages() {
  errorMsg.value = ''
  successMsg.value = ''
}

function resetForm() {
  form.title = ''
  form.description = ''
  form.locationName = ''
  form.heritageTypeCode = ''
  form.category = ''
  form.categoryId = ''
  form.copyrightDeclaration = ''
  form.version = null
  form.tagIds = []
  form.attachmentIds = []
  initialUploaderFiles.value = []
  resourceStatus.value = 'DRAFT'
}

async function loadOptions() {
  const [catRes, tagsRes, typeRes] = await Promise.all([
    getCategories(),
    getTags(),
    getHeritageTypeGroups(),
  ])

  if (catRes.data.code === 200) categories.value = catRes.data.data
  if (tagsRes.data.code === 200) availableTags.value = tagsRes.data.data
  if (typeRes.data.code === 200) heritageTypeGroups.value = typeRes.data.data
}

async function loadOwnedDraft() {
  if (!isEditMode.value) {
    return
  }

  const response = await getOwnedResource(resourceId.value)
  const data = response.data?.data
  if (!data) {
    throw new Error('Draft not found')
  }

  form.title = data.title || ''
  form.description = data.description || ''
  form.locationName = data.locationName || ''
  form.heritageTypeCode = data.heritageTypeCode || ''
  form.categoryId = data.categoryId || ''
  form.copyrightDeclaration = data.copyrightDeclaration || ''
  form.version = data.version ?? null
  form.tagIds = Array.isArray(data.tags) ? data.tags.map((tag) => tag.id) : []
  form.attachmentIds = Array.isArray(data.attachments)
    ? data.attachments.map((attachment) => attachment.id)
    : []
  resourceStatus.value = data.status || 'DRAFT'
  initialUploaderFiles.value = Array.isArray(data.attachments) ? data.attachments : []
}

async function initializePage() {
  pageLoading.value = isEditMode.value
  resetMessages()
  resetForm()

  try {
    await loadOptions()
    await loadOwnedDraft()
  } catch (err) {
    console.error('Failed to initialize resource form', err)
    errorMsg.value = err.response?.data?.message || err.message || 'Failed to load draft.'
  } finally {
    pageLoading.value = false
  }
}

function syncAttachmentIdsFromUploader() {
  if (!uploaderRef.value) {
    form.attachmentIds = []
    return
  }

  const uploadedFiles = uploaderRef.value.uploadedFiles || []
  form.attachmentIds = uploadedFiles
    .map((file) => file.attachmentId ?? file.id)
    .filter((id) => id != null)
}

async function ensureUploadsReady() {
  const currentFiles = uploaderRef.value?.uploadedFiles || []
  const pendingUploads = currentFiles
    .filter((file) => !file._removed && file.uploadPromise && (file.uploading || (file.rawFile && !file.attachmentId && !file.uploadError)))
    .map((file) => file.uploadPromise)

  if (pendingUploads.length > 0) {
    await Promise.allSettled(pendingUploads)
  }

  const files = uploaderRef.value?.uploadedFiles || []
  const failedUploads = files.filter((file) => !file._removed && file.uploadError)
  if (failedUploads.length > 0) {
    throw new Error('One or more attachments failed to upload. Remove or re-upload them before continuing.')
  }

  const unfinishedUploads = files.filter((file) => !file._removed && file.rawFile && !file.attachmentId)
  if (unfinishedUploads.length > 0) {
    throw new Error('Please wait for all attachments to finish uploading before saving or submitting.')
  }
}

function syncCategoryName() {
  if (!form.categoryId) {
    form.category = ''
    return
  }

  const category = categories.value.find((item) => item.id == form.categoryId)
  form.category = category ? category.name : ''
}

async function saveDraft() {
  await ensureUploadsReady()
  syncAttachmentIdsFromUploader()
  syncCategoryName()

  const payload = {
    title: form.title,
    description: form.description,
    locationName: form.locationName,
    heritageTypeCode: form.heritageTypeCode,
    category: form.category,
    categoryId: form.categoryId ? Number(form.categoryId) : null,
    copyrightDeclaration: form.copyrightDeclaration,
    version: form.version,
    tagIds: form.tagIds,
    attachmentIds: form.attachmentIds,
  }

  const response = isEditMode.value
    ? await updateDraft(resourceId.value, payload)
    : await createDraft(payload)

  const data = response.data?.data
  if (data) {
    form.version = data.version ?? form.version
    resourceStatus.value = data.status || resourceStatus.value
  }

  return response
}

async function handleSaveDraft() {
  loading.value = true
  resetMessages()

  try {
    const response = await saveDraft()

    if (response.data.success || response.data.code === 200) {
      successMsg.value = isEditMode.value
        ? 'Draft updated successfully!'
        : 'Draft saved successfully!'
      setTimeout(() => {
        router.push('/resources/submissions')
      }, 1000)
    } else {
      errorMsg.value = response.data.message || 'Failed to save draft.'
    }
  } catch (err) {
    errorMsg.value = err.response?.data?.message || err.message || 'An error occurred.'
  } finally {
    loading.value = false
  }
}

async function handleSubmitForReview() {
  submitting.value = true
  resetMessages()

  try {
    const saveResponse = await saveDraft()
    if (!(saveResponse.data.success || saveResponse.data.code === 200)) {
      throw new Error(saveResponse.data.message || 'Failed to save draft before submission.')
    }

    const savedResource = saveResponse.data?.data
    const targetResourceId = isEditMode.value
      ? resourceId.value
      : savedResource?.id

    if (!targetResourceId) {
      throw new Error('Saved draft id is missing, cannot submit for review.')
    }

    const submitResponse = await submitForReview(targetResourceId)
    if (submitResponse.data.success || submitResponse.data.code === 200) {
      const data = submitResponse.data?.data
      form.version = data?.version ?? form.version
      resourceStatus.value = data?.status || 'PENDING_REVIEW'
      successMsg.value = 'Draft submitted for review successfully!'
      setTimeout(() => {
        router.push('/resources/submissions')
      }, 1000)
      return
    }

    throw new Error(submitResponse.data.message || 'Failed to submit draft for review.')
  } catch (err) {
    errorMsg.value = err.response?.data?.message || err.message || 'An error occurred.'
  } finally {
    submitting.value = false
  }
}

function goBack() {
  if (isEditMode.value) {
    router.push('/resources/submissions')
    return
  }
  router.push('/')
}

onMounted(() => {
  initializePage()
})

watch(() => route.params.id, () => {
  initializePage()
})
</script>

<style scoped>
.create-resource-page {
  max-width: 800px;
  margin: 2rem auto;
  padding: 2rem;
  background: var(--surface, #fff);
  border-radius: 12px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.05);
}

.header h1 {
  font-family: var(--font-serif);
  color: var(--primary-dark, #2c3e50);
  margin-bottom: 0.5rem;
}

.header p {
  color: var(--muted, #7f8c8d);
  margin-bottom: 2rem;
}

.page-state {
  padding: 2rem 0;
  color: var(--muted, #7f8c8d);
}

.form-group {
  margin-bottom: 1.5rem;
}

.form-group label {
  display: block;
  font-weight: 600;
  margin-bottom: 0.5rem;
  color: var(--text, #333);
}

.form-group input[type="text"],
.form-group textarea,
.form-group select {
  width: 100%;
  padding: 0.75rem;
  border: 1px solid var(--border, #ccc);
  border-radius: 6px;
  font-family: inherit;
  font-size: 1rem;
}

.form-group input[type="text"]:focus,
.form-group textarea:focus,
.form-group select:focus {
  outline: none;
  border-color: var(--primary, #3498db);
  box-shadow: 0 0 0 2px rgba(52, 152, 219, 0.2);
}

.row-group {
  display: flex;
  gap: 1.5rem;
}

.row-group .col {
  flex: 1;
}

.tags-container {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
  padding: 1rem;
  background: #f8f9fa;
  border-radius: 6px;
  border: 1px solid var(--border, #ccc);
}

.tag-checkbox {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  cursor: pointer;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 1rem;
  margin-top: 2rem;
  padding-top: 1.5rem;
  border-top: 1px solid var(--border, #ccc);
}

.btn {
  padding: 0.75rem 1.5rem;
  border: none;
  border-radius: 6px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-primary {
  background: var(--primary, #3498db);
  color: white;
}

.btn-primary:hover:not(:disabled) {
  background: #2980b9;
}

.btn-primary:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.btn-submit {
  background: #16a34a;
  color: white;
}

.btn-submit:hover:not(:disabled) {
  background: #15803d;
}

.btn-submit:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.btn-secondary {
  background: #ecf0f1;
  color: #333;
}

.btn-secondary:hover {
  background: #bdc3c7;
}

.help-text {
  font-size: 0.85rem;
  color: var(--muted, #7f8c8d);
  margin-top: 0.5rem;
}

.error-msg {
  color: #e74c3c;
  margin-top: 1rem;
  font-weight: 600;
}

.success-msg {
  color: #27ae60;
  margin-top: 1rem;
  font-weight: 600;
}
</style>
