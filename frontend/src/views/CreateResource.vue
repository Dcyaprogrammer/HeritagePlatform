<template>
  <div class="create-resource-page">
    <header class="public-page-intro create-resource__header">
      <p class="public-eyebrow">{{ isEditMode ? 'Contributor Draft' : 'Contributor Workspace' }}</p>
      <div class="create-resource__title-row">
        <div class="create-resource__title-copy">
          <h1 class="public-page-title">{{ isEditMode ? 'Edit heritage resource' : 'Create heritage resource' }}</h1>
          <p class="public-lead">
            {{
              isEditMode
                ? 'Refine the draft, keep the description readable, and attach the material you want reviewers to evaluate.'
                : 'Capture a place, story, tradition, object, or practice with enough context for the archive and review team.'
            }}
          </p>
        </div>
        <span class="public-badge" :class="formStatusClass">{{ formStatusLabel }}</span>
      </div>
    </header>

    <div v-if="pageLoading" class="public-panel create-resource__state">Loading draft...</div>

    <form v-else @submit.prevent="handleSaveDraft" class="public-form create-resource__form">
      <section class="public-panel public-form-section">
        <h2 class="public-form-section-title">Core information</h2>
        <p class="public-form-section-lead">
          Start with the name and narrative. This is the part readers will rely on first.
        </p>

        <div class="public-form-grid">
          <label class="public-field public-field--full" for="title">
            <span class="public-label">Title *</span>
            <input
              id="title"
              v-model="form.title"
              type="text"
              class="public-input"
              required
              placeholder="Enter a clear, specific title"
            />
          </label>

          <label class="public-field public-field--full" for="description">
            <span class="public-label">Description *</span>
            <textarea
              id="description"
              v-model="form.description"
              rows="6"
              class="public-textarea"
              required
              placeholder="Describe what this heritage resource is, why it matters, where it belongs, and any historical or cultural context."
            />
            <span class="public-help">Aim for readable prose, not keywords. Reviewers need enough context to judge accuracy and relevance.</span>
          </label>
        </div>
      </section>

      <section class="public-panel public-form-section">
        <h2 class="public-form-section-title">Classification</h2>
        <p class="public-form-section-lead">
          Use structured metadata so the resource can be found, grouped, and reviewed correctly.
        </p>

        <div class="public-form-grid public-form-grid--two">
          <label class="public-field" for="locationName">
            <span class="public-label">Location / Place</span>
            <select id="locationName" v-model="form.locationName" class="public-select">
              <option value="">Select a province</option>
              <option v-for="p in locationOptions" :key="p" :value="p">{{ p }}</option>
            </select>
          </label>

          <label class="public-field" for="category">
            <span class="public-label">Category</span>
            <select id="category" v-model="form.categoryId" class="public-select">
              <option value="">Select a category</option>
              <option v-for="cat in categories" :key="cat.id" :value="cat.id">{{ cat.name }}</option>
            </select>
          </label>

          <label class="public-field public-field--full" for="heritageType">
            <span class="public-label">Heritage Type</span>
            <select id="heritageType" v-model="form.heritageTypeCode" class="public-select">
              <option value="">Select a heritage type</option>
              <optgroup v-for="group in heritageTypeGroups" :key="group.groupCode" :label="group.groupName">
                <option v-for="type in group.types" :key="type.code" :value="type.code">
                  {{ type.name }}
                </option>
              </optgroup>
            </select>
          </label>
        </div>
      </section>

      <section class="public-panel public-form-section">
        <h2 class="public-form-section-title">Tags and rights</h2>
        <p class="public-form-section-lead">
          Tags should describe content. Rights information should clarify how others may use the material.
        </p>

        <div class="public-form-grid">
          <div class="public-field public-field--full">
            <span class="public-label">Tags</span>
            <div v-if="availableTags.length" class="public-choice-grid">
              <label v-for="tag in availableTags" :key="tag.id" class="public-choice">
                <input type="checkbox" :value="tag.id" v-model="form.tagIds" />
                <span class="public-choice-label">{{ tag.name }}</span>
              </label>
            </div>
            <span v-else class="public-help">No tags available yet.</span>
          </div>

          <label class="public-field public-field--full" for="copyright">
            <span class="public-label">Copyright &amp; usage declaration</span>
            <input
              id="copyright"
              v-model="form.copyrightDeclaration"
              type="text"
              class="public-input"
              placeholder="Example: CC BY-NC 4.0, Public Domain, or All Rights Reserved"
            />
          </label>
        </div>
      </section>

      <section class="public-panel public-form-section">
        <h2 class="public-form-section-title">Attachments</h2>
        <p class="public-form-section-lead">
          Upload images, video, audio, or supporting documents. Wait until uploads finish before saving or submitting.
        </p>

        <div class="public-form-grid">
          <div class="public-field public-field--full">
            <FileUploader ref="uploaderRef" :initial-files="initialUploaderFiles" />
            <p class="public-help-text">
              Files remain attached to the draft. Removing failed uploads before submission avoids review interruptions.
            </p>
          </div>
        </div>
      </section>

      <section class="public-panel public-form-section create-resource__actions-panel">
        <div class="actions">
          <button type="button" class="public-btn public-btn--ghost" @click="goBack">Cancel</button>
          <button type="submit" class="public-btn" :disabled="loading || submitting || hasPendingUploads">
            <span v-if="loading">{{ isEditMode ? 'Updating...' : 'Saving...' }}</span>
            <span v-else>{{ isEditMode ? 'Update Draft' : 'Save Draft' }}</span>
          </button>
          <button
            type="button"
            class="public-btn public-btn--primary"
            :disabled="loading || submitting || hasPendingUploads"
            @click="handleSubmitForReview"
          >
            <span v-if="submitting">Submitting...</span>
            <span v-else>Submit for Review</span>
          </button>
        </div>

        <div v-if="errorMsg" class="public-callout public-callout--error">{{ errorMsg }}</div>
        <div v-if="successMsg" class="public-callout public-callout--success">{{ successMsg }}</div>
      </section>
    </form>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  createDraft,
  getCategories,
  getProvinces,
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
const provinces = ref([])
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
const sortedProvinces = computed(() => {
  return [...provinces.value].sort((a, b) => (a || '').localeCompare(b || ''))
})
const locationOptions = computed(() => {
  const names = sortedProvinces.value
  if (form.locationName && !names.includes(form.locationName)) {
    return [form.locationName, ...names]
  }
  return names
})
const hasPendingUploads = computed(() => {
  const files = uploaderRef.value?.uploadedFiles || []
  return files.some((file) => !file._removed && (file.uploading || (file.rawFile && !file.attachmentId && !file.uploadError)))
})
const formStatusLabel = computed(() => {
  const raw = resourceStatus.value || 'DRAFT'
  return raw
    .toLowerCase()
    .split('_')
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ')
})
const formStatusClass = computed(() => {
  if (resourceStatus.value === 'APPROVED') return 'public-badge--success'
  if (resourceStatus.value === 'REJECTED') return 'public-badge--danger'
  return 'public-badge--accent'
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
  const [catRes, tagsRes, typeRes, provinceRes] = await Promise.all([
    getCategories(),
    getTags(),
    getHeritageTypeGroups(),
    getProvinces(),
  ])

  if (catRes.data.code === 200) categories.value = catRes.data.data
  if (tagsRes.data.code === 200) availableTags.value = tagsRes.data.data
  if (typeRes.data.code === 200) heritageTypeGroups.value = typeRes.data.data
  if (provinceRes.data.code === 200) {
    provinces.value = (provinceRes.data.data || []).map((p) => p.name).filter(Boolean)
  }
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
  padding: 0.5rem 0 2.5rem;
}

.create-resource__header {
  margin-bottom: 1.1rem;
}

.create-resource__title-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
  flex-wrap: wrap;
}

.create-resource__title-copy {
  max-width: 52rem;
}

.create-resource__state {
  padding: 1.25rem;
  color: var(--muted);
  font-size: var(--text-base);
}

.create-resource__form {
  gap: 1rem;
}

.create-resource__actions-panel {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.actions {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
  flex-wrap: wrap;
}

@media (max-width: 640px) {
  .actions {
    justify-content: stretch;
  }

  .actions .public-btn {
    width: 100%;
  }
}
</style>
