<template>
  <div class="profile-page">
    <el-button @click="goBack" class="back-btn">← Back</el-button>
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <h2>My Profile</h2>
        </div>
      </template>

      <el-form :model="form" label-width="120px" v-loading="loading">
        <el-form-item label="Username">
          <el-input v-model="form.username" disabled />
        </el-form-item>

        <el-form-item label="Display Name">
          <el-input v-model="form.displayName" />
        </el-form-item>

        <el-form-item label="Email">
          <el-input v-model="form.email" />
        </el-form-item>

        <el-form-item label="Bio">
          <el-input v-model="form.bio" type="textarea" :rows="3" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleSave" :loading="saving">Save Changes</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="contributor-card">
      <template #header>
        <div class="card-header">
          <h3>Contributor Status</h3>
        </div>
      </template>

      <div class="contributor-content">
        <div class="current-roles">
          <span class="label">Current Roles:</span>
          <el-tag v-for="role in userRoles" :key="role" :type="getRoleType(role)" class="role-tag">
            {{ role }}
          </el-tag>
        </div>

        <div v-if="isContributor" class="status-message success">
          You are already a contributor. You can submit and manage resources.
        </div>

        <div v-else-if="contributorStatus === 'PENDING'" class="status-message warning">
          <el-icon><Timer /></el-icon>
          <span>Your application is pending admin approval.</span>
        </div>

        <div v-else-if="contributorStatus === 'REJECTED'" class="status-message danger">
          <el-icon><CircleClose /></el-icon>
          <span>Your previous application was rejected. You may reapply.</span>
          <el-button type="primary" @click="showApplyDialog = true" class="apply-btn">
            Reapply
          </el-button>
        </div>

        <div v-else class="apply-section">
          <p class="hint">Apply to become a contributor to submit and manage heritage resources.</p>
          <el-button type="primary" @click="showApplyDialog = true">
            Apply to become a Contributor
          </el-button>
        </div>
      </div>
    </el-card>

    <el-card class="password-card">
      <template #header>
        <div class="card-header">
          <h3>Change Password</h3>
        </div>
      </template>

      <el-form :model="pwdForm" label-width="120px" :rules="pwdRules" ref="pwdFormRef">
        <el-form-item label="Old Password" prop="oldPassword">
          <el-input v-model="pwdForm.oldPassword" type="password" show-password />
        </el-form-item>

        <el-form-item label="New Password" prop="newPassword">
          <el-input v-model="pwdForm.newPassword" type="password" show-password />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleChangePassword" :loading="changingPwd">Change Password</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- Apply Dialog -->
    <el-dialog v-model="showApplyDialog" title="Apply to be a Contributor" width="500px">
      <el-form :model="applyForm" label-width="100px">
        <el-form-item label="Reason">
          <el-input
            v-model="applyForm.reason"
            type="textarea"
            :rows="4"
            placeholder="Please tell us why you want to be a contributor..."
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showApplyDialog = false">Cancel</el-button>
        <el-button type="primary" @click="handleApply" :loading="applying">Submit Application</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Timer, CircleClose } from '@element-plus/icons-vue'
import { getCurrentUser } from '../api/auth.js'
import { getUser, updateUser, updatePassword, applyContributor } from '../api/user.js'

const router = useRouter()
const loading = ref(false)
const saving = ref(false)
const changingPwd = ref(false)
const applying = ref(false)
const pwdFormRef = ref(null)
const showApplyDialog = ref(false)

const form = reactive({
  username: '',
  displayName: '',
  email: '',
  bio: ''
})

const userRoles = ref([])
const contributorStatus = ref('NONE')

const pwdForm = reactive({
  oldPassword: '',
  newPassword: ''
})

const applyForm = reactive({
  reason: ''
})

const isContributor = computed(() => userRoles.value.includes('CONTRIBUTOR'))

const pwdRules = {
  oldPassword: [{ required: true, message: 'Please enter old password', trigger: 'blur' }],
  newPassword: [{ required: true, message: 'Please enter new password', trigger: 'blur' }]
}

const getRoleType = (role) => {
  const map = { ADMIN: 'danger', CONTRIBUTOR: 'success', VIEWER: 'info' }
  return map[role] || 'info'
}

const goBack = () => {
  const role = localStorage.getItem('role')
  if (role === 'ADMIN') {
    router.push('/admin/users')
  } else {
    router.push('/home')
  }
}

const fetchProfile = async () => {
  loading.value = true
  try {
    const username = localStorage.getItem('username')
    let res
    if (username) {
      res = await getUser(username)
    } else {
      res = await getCurrentUser()
    }
    const data = res.data.data
    form.username = data.username
    form.displayName = data.displayName || ''
    form.email = data.email || ''
    form.bio = data.bio || ''
    userRoles.value = data.roles || []
    contributorStatus.value = data.contributorStatus || 'NONE'
  } catch (error) {
    ElMessage.error('Failed to load profile')
  } finally {
    loading.value = false
  }
}

const handleSave = async () => {
  saving.value = true
  try {
    await updateUser(form.username, {
      displayName: form.displayName,
      email: form.email,
      bio: form.bio
    })
    ElMessage.success('Profile updated')
  } catch (error) {
    ElMessage.error('Failed to update profile')
  } finally {
    saving.value = false
  }
}

const handleChangePassword = async () => {
  if (!pwdFormRef.value) return
  await pwdFormRef.value.validate(async (valid) => {
    if (!valid) return
    changingPwd.value = true
    try {
      await updatePassword(form.username, {
        oldPassword: pwdForm.oldPassword,
        newPassword: pwdForm.newPassword
      })
      ElMessage.success('Password changed successfully')
      pwdForm.oldPassword = ''
      pwdForm.newPassword = ''
    } catch (error) {
      ElMessage.error(error.response?.data?.message || 'Failed to change password')
    } finally {
      changingPwd.value = false
    }
  })
}

const handleApply = async () => {
  applying.value = true
  try {
    await applyContributor(form.username, applyForm.reason)
    contributorStatus.value = 'PENDING'
    showApplyDialog.value = false
    applyForm.reason = ''
    ElMessage.success('Application submitted. Waiting for admin approval.')
  } catch (error) {
    ElMessage.error(error.response?.data?.message || 'Failed to submit application')
  } finally {
    applying.value = false
  }
}

onMounted(() => {
  fetchProfile()
})
</script>

<style scoped>
.profile-page {
  max-width: 600px;
  margin: 40px auto;
  padding: 0 20px;
}

.back-btn {
  margin-bottom: 16px;
}

.profile-card,
.contributor-card,
.password-card {
  margin-bottom: 24px;
}

.card-header h2,
.card-header h3 {
  margin: 0;
}

.contributor-content {
  padding: 8px 0;
}

.current-roles {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}

.current-roles .label {
  color: #606266;
  font-size: 14px;
}

.role-tag {
  margin-right: 4px;
}

.status-message {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  border-radius: 4px;
  font-size: 14px;
}

.status-message.success {
  background-color: #f0f9eb;
  color: #67c23a;
}

.status-message.warning {
  background-color: #fdf6ec;
  color: #e6a23c;
}

.status-message.danger {
  background-color: #fef0f0;
  color: #f56c6c;
  flex-wrap: wrap;
}

.apply-btn {
  margin-left: auto;
}

.apply-section .hint {
  color: #606266;
  font-size: 14px;
  margin: 0 0 12px 0;
}
</style>
