<template>
  <div class="master-data">
    <div class="page-header">
      <div>
        <h2>Master Data Management</h2>
        <p class="subtitle">Manage categories and tags available to contributors</p>
      </div>
      <el-button type="primary" @click="refreshAll" :loading="loadingAll">
        <el-icon><Refresh /></el-icon>
        Refresh
      </el-button>
    </div>

    <el-card class="card">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="Categories" name="categories">
          <div class="toolbar">
            <el-button type="primary" @click="openCreateCategory">New Category</el-button>
          </div>
          <el-table :data="categoryRows" v-loading="loadingCategories" stripe style="width: 100%">
            <el-table-column type="index" width="50" />
            <el-table-column prop="name" label="Name" min-width="200" />
            <el-table-column prop="description" label="Description" min-width="260" show-overflow-tooltip />
            <el-table-column label="Actions" width="220" fixed="right">
              <template #default="{ row }">
                <el-button size="small" @click="openEditCategory(row)">Edit</el-button>
                <el-button size="small" type="danger" @click="confirmDeleteCategory(row)">Delete</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>

        <el-tab-pane label="Tags" name="tags">
          <div class="toolbar">
            <el-button type="primary" @click="openCreateTag">New Tag</el-button>
          </div>
          <el-table :data="tagRows" v-loading="loadingTags" stripe style="width: 100%">
            <el-table-column type="index" width="50" />
            <el-table-column prop="name" label="Name" min-width="240" />
            <el-table-column label="Actions" width="220" fixed="right">
              <template #default="{ row }">
                <el-button size="small" @click="openEditTag(row)">Edit</el-button>
                <el-button size="small" type="danger" @click="confirmDeleteTag(row)">Delete</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <!-- Category dialog -->
    <el-dialog v-model="categoryDialog.visible" :title="categoryDialog.mode === 'create' ? 'New Category' : 'Edit Category'" width="520px">
      <el-form label-position="top">
        <el-form-item label="Name (required)">
          <el-input v-model="categoryDialog.form.name" maxlength="100" />
        </el-form-item>
        <el-form-item label="Description">
          <el-input v-model="categoryDialog.form.description" maxlength="255" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="categoryDialog.visible = false">Cancel</el-button>
        <el-button type="primary" :loading="categoryDialog.saving" @click="saveCategory">Save</el-button>
      </template>
    </el-dialog>

    <!-- Tag dialog -->
    <el-dialog v-model="tagDialog.visible" :title="tagDialog.mode === 'create' ? 'New Tag' : 'Edit Tag'" width="520px">
      <el-form label-position="top">
        <el-form-item label="Name (required)">
          <el-input v-model="tagDialog.form.name" maxlength="50" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="tagDialog.visible = false">Cancel</el-button>
        <el-button type="primary" :loading="tagDialog.saving" @click="saveTag">Save</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import {
  createCategory,
  createTag,
  deleteCategory,
  deleteTag,
  getCategoriesAdmin,
  getTagsAdmin,
  updateCategory,
  updateTag
} from '../../api/user.js'

const activeTab = ref('categories')

const loadingCategories = ref(false)
const loadingTags = ref(false)
const loadingAll = computed(() => loadingCategories.value || loadingTags.value)

const categoryRows = ref([])
const tagRows = ref([])

const categoryDialog = reactive({
  visible: false,
  mode: 'create', // create | edit
  saving: false,
  id: null,
  form: { name: '', description: '' }
})

const tagDialog = reactive({
  visible: false,
  mode: 'create', // create | edit
  saving: false,
  id: null,
  form: { name: '' }
})

const fetchCategories = async () => {
  loadingCategories.value = true
  try {
    const res = await getCategoriesAdmin()
    categoryRows.value = res.data?.data || []
  } catch (e) {
    ElMessage.error(e.response?.data?.message || 'Failed to load categories')
  } finally {
    loadingCategories.value = false
  }
}

const fetchTags = async () => {
  loadingTags.value = true
  try {
    const res = await getTagsAdmin()
    tagRows.value = res.data?.data || []
  } catch (e) {
    ElMessage.error(e.response?.data?.message || 'Failed to load tags')
  } finally {
    loadingTags.value = false
  }
}

const refreshAll = async () => {
  await Promise.all([fetchCategories(), fetchTags()])
}

const handleTabChange = () => {
  // no-op; keep data hot for demo
}

const openCreateCategory = () => {
  categoryDialog.mode = 'create'
  categoryDialog.id = null
  categoryDialog.form.name = ''
  categoryDialog.form.description = ''
  categoryDialog.visible = true
}

const openEditCategory = (row) => {
  categoryDialog.mode = 'edit'
  categoryDialog.id = row.id
  categoryDialog.form.name = row.name || ''
  categoryDialog.form.description = row.description || ''
  categoryDialog.visible = true
}

const saveCategory = async () => {
  const name = categoryDialog.form.name?.trim()
  if (!name) {
    ElMessage.warning('Name is required')
    return
  }
  categoryDialog.saving = true
  try {
    const payload = { name, description: categoryDialog.form.description || '' }
    if (categoryDialog.mode === 'create') {
      await createCategory(payload)
      ElMessage.success('Category created')
    } else {
      await updateCategory(categoryDialog.id, payload)
      ElMessage.success('Category updated')
    }
    categoryDialog.visible = false
    fetchCategories()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || 'Save failed')
  } finally {
    categoryDialog.saving = false
  }
}

const confirmDeleteCategory = async (row) => {
  try {
    await ElMessageBox.confirm(`Delete category "${row.name}"?`, 'Confirm Delete', {
      type: 'warning',
      confirmButtonText: 'Delete',
      cancelButtonText: 'Cancel'
    })
  } catch {
    return
  }
  try {
    await deleteCategory(row.id)
    ElMessage.success('Category deleted')
    fetchCategories()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || 'Delete failed')
  }
}

const openCreateTag = () => {
  tagDialog.mode = 'create'
  tagDialog.id = null
  tagDialog.form.name = ''
  tagDialog.visible = true
}

const openEditTag = (row) => {
  tagDialog.mode = 'edit'
  tagDialog.id = row.id
  tagDialog.form.name = row.name || ''
  tagDialog.visible = true
}

const saveTag = async () => {
  const name = tagDialog.form.name?.trim()
  if (!name) {
    ElMessage.warning('Name is required')
    return
  }
  tagDialog.saving = true
  try {
    const payload = { name }
    if (tagDialog.mode === 'create') {
      await createTag(payload)
      ElMessage.success('Tag created')
    } else {
      await updateTag(tagDialog.id, payload)
      ElMessage.success('Tag updated')
    }
    tagDialog.visible = false
    fetchTags()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || 'Save failed')
  } finally {
    tagDialog.saving = false
  }
}

const confirmDeleteTag = async (row) => {
  try {
    await ElMessageBox.confirm(`Delete tag "${row.name}"?`, 'Confirm Delete', {
      type: 'warning',
      confirmButtonText: 'Delete',
      cancelButtonText: 'Cancel'
    })
  } catch {
    return
  }
  try {
    await deleteTag(row.id)
    ElMessage.success('Tag deleted')
    fetchTags()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || 'Delete failed')
  }
}

onMounted(refreshAll)
</script>

<style scoped>
.master-data {
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

.card {
  border-radius: 10px;
}

.toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
}
</style>

