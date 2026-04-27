<template>
  <div class="create-resource-page">
    <div class="header">
      <h1>Create New Heritage Resource</h1>
      <p>Share local culture, places, traditions, stories, or objects.</p>
    </div>

    <form @submit.prevent="handleSaveDraft" class="resource-form">
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
          <optgroup v-for="group in heritageTypeGroups" :key="group.code" :label="group.name">
            <option v-for="type in group.children" :key="type.code" :value="type.code">
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
        <FileUploader ref="uploaderRef" />
        <p class="help-text">Please upload your files before saving the draft.</p>
      </div>

      <div class="actions">
        <button type="button" class="btn btn-secondary" @click="goBack">Cancel</button>
        <button type="submit" class="btn btn-primary" :disabled="loading">
          <span v-if="loading">Saving...</span>
          <span v-else>Save as Draft</span>
        </button>
      </div>
      
      <div v-if="errorMsg" class="error-msg">{{ errorMsg }}</div>
      <div v-if="successMsg" class="success-msg">{{ successMsg }}</div>
    </form>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { createDraft, getCategories, getTags, getHeritageTypeGroups } from '@/api/resource.js';
import FileUploader from '@/components/FileUploader.vue';

const router = useRouter();

const form = reactive({
  title: '',
  description: '',
  locationName: '',
  heritageTypeCode: '',
  categoryId: '',
  copyrightDeclaration: '',
  tagIds: [],
  attachmentIds: []
});

const categories = ref([]);
const availableTags = ref([]);
const heritageTypeGroups = ref([]);

const uploaderRef = ref(null);
const loading = ref(false);
const errorMsg = ref('');
const successMsg = ref('');

onMounted(async () => {
  try {
    const [catRes, tagsRes, typeRes] = await Promise.all([
      getCategories(),
      getTags(),
      getHeritageTypeGroups()
    ]);
    
    if (catRes.data.success) categories.value = catRes.data.data;
    if (tagsRes.data.success) availableTags.value = tagsRes.data.data;
    if (typeRes.data.success) heritageTypeGroups.value = typeRes.data.data;
  } catch (err) {
    console.error("Failed to load form options", err);
  }
});

const handleSaveDraft = async () => {
  loading.value = true;
  errorMsg.value = '';
  successMsg.value = '';

  try {
    if (uploaderRef.value) {
      const uploadedFiles = uploaderRef.value.uploadedFiles || [];
      form.attachmentIds = uploadedFiles.map(f => f.id).filter(id => id != null);
    }

    if (form.categoryId) {
      const cat = categories.value.find(c => c.id == form.categoryId);
      if (cat) form.category = cat.name;
    }

    const response = await createDraft(form);
    
    if (response.data.success) {
      successMsg.value = 'Draft saved successfully!';
      setTimeout(() => {
        router.push('/resources/submissions');
      }, 1500);
    } else {
      errorMsg.value = response.data.message || 'Failed to save draft.';
    }
  } catch (err) {
    errorMsg.value = err.response?.data?.message || err.message || 'An error occurred.';
  } finally {
    loading.value = false;
  }
};

const goBack = () => {
  router.push('/');
};
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