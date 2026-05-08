<template>
  <div class="reset-container">
    <el-card class="reset-card">
      <template #header>
        <div class="card-header">
          <h2>Reset Your Password</h2>
        </div>
      </template>

      <el-form ref="resetFormRef" :model="resetForm" :rules="rules" label-position="top">
        <el-form-item label="New Password" prop="newPassword">
          <el-input 
            v-model="resetForm.newPassword" 
            type="password" 
            placeholder="Enter new password" 
            prefix-icon="Lock" 
            size="large" 
            show-password 
          />
        </el-form-item>

        <el-form-item label="Confirm New Password" prop="confirmPassword">
          <el-input 
            v-model="resetForm.confirmPassword" 
            type="password" 
            placeholder="Confirm new password" 
            prefix-icon="Lock" 
            size="large" 
            show-password 
            @keyup.enter="handleResetPassword"
          />
        </el-form-item>

        <el-form-item>
          <el-button 
            type="primary" 
            size="large" 
            style="width: 100%" 
            :loading="loading" 
            @click="handleResetPassword"
          >
            Reset Password
          </el-button>
        </el-form-item>

        <div class="form-footer">
          <router-link to="/login">← Back to Login</router-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { resetPassword } from '../../api/auth.js'
import { validatePasswordPolicy } from '../../utils/passwordPolicy.js'

const router = useRouter()
const route = useRoute()

const resetForm = reactive({
  newPassword: '',
  confirmPassword: ''
})

const rules = {
  newPassword: [
    { required: true, message: 'Please enter new password', trigger: 'blur' },
    { validator: validatePasswordPolicy, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: 'Please confirm your password', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const resetFormRef = ref(null)
const loading = ref(false)
const token = ref('')

onMounted(() => {
  token.value = route.query.token
  
  if (!token.value) {
    ElMessage.error('Invalid or missing reset token')
    setTimeout(() => {
      router.push('/forgot-password')
    }, 2000)
  }
})

function validateConfirmPassword(rule, value, callback) {
  if (value !== resetForm.newPassword) {
    callback(new Error('Passwords do not match'))
  } else {
    callback()
  }
}

const handleResetPassword = async () => {
  if (!resetFormRef.value || !token.value) return

  await resetFormRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true

    try {
      const res = await resetPassword({
        token: token.value,
        newPassword: resetForm.newPassword
      })

      if (res.data.code === 200) {
        ElMessage.success('Password has been reset successfully!')
        setTimeout(() => {
          router.push('/login')
        }, 1500)
      } else {
        ElMessage.error(res.data.message || 'Failed to reset password')
      }
    } catch (error) {
      console.error(error)
      ElMessage.error(error.response?.data?.message || 'Failed to reset password')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.wrap {
  max-width: 1120px;
  margin: 0 auto;
  padding: 0 1.25rem;
}
.top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem 0;
  border-bottom: 1px solid var(--border, #eaeaea);
}
.top h1 {
  margin: 0;
  font-family: var(--font-serif, 'Georgia', serif);
  font-size: 1.5rem;
  color: var(--text, #333);
}
.btn {
  padding: 0.5rem 1rem;
  border: 1px solid var(--border, #eaeaea);
  border-radius: 4px;
  background: white;
  cursor: pointer;
  font-size: 0.875rem;
  color: var(--text, #333);
}
.btn:hover {
  background: #f9f9f9;
}
.login-container {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding-top: 4rem;
  min-height: calc(100vh - 100px);
}
.login-card {
  width: 100%;
  max-width: 420px;
  border-radius: 12px;
  border: 1px solid var(--border, #eaeaea);
}
.card-header {
  text-align: center;
}
.card-header h2 {
  margin: 0;
  color: var(--text, #333);
  font-family: var(--font-serif, 'Georgia', serif);
  font-size: 1.75rem;
}
.subtitle {
  margin: 0.5rem 0 0;
  color: var(--muted, #666);
  font-size: 0.875rem;
}
.form-footer {
  text-align: center;
  margin-top: 1.5rem;
  font-size: 0.875rem;
  color: var(--muted, #666);
}

.reset-container {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding-top: 4rem;
  min-height: calc(100vh - 100px);
}

.reset-card {
  width: 100%;
  max-width: 420px;
  border-radius: 12px;
  border: 1px solid var(--border, #eaeaea);
}
</style>
