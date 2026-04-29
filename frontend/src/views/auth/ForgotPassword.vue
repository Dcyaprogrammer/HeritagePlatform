<template>
  <div class="forgot-container">
    <el-card class="forgot-card">
      <template #header>
        <div class="card-header">
          <h2>Forgot Password</h2>
        </div>
      </template>

      <el-form ref="forgotFormRef" :model="forgotForm" :rules="rules" label-position="top">
        <el-form-item label="Email Address" prop="email">
          <el-input 
            v-model="forgotForm.email" 
            type="email" 
            placeholder="Enter your registered email" 
            prefix-icon="Message" 
            size="large" 
            @keyup.enter="handleForgotPassword"
          />
        </el-form-item>

        <el-form-item>
          <el-button 
            type="primary" 
            size="large" 
            style="width: 100%" 
            :loading="loading" 
            @click="handleForgotPassword"
          >
            Send Reset Link
          </el-button>
        </el-form-item>

        <div class="form-footer">
          <router-link to="/login">← Back to Login</router-link>
        </div>
      </el-form>

      <p v-if="message" class="message" :class="{ success: isSuccess }">
        {{ message }}
      </p>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { forgotPassword } from '../../api/auth.js'

const forgotForm = reactive({
  email: ''
})

const rules = {
  email: [
    { required: true, message: 'Please enter your email', trigger: 'blur' },
    { type: 'email', message: 'Please enter a valid email address', trigger: 'blur' }
  ]
}

const forgotFormRef = ref(null)
const loading = ref(false)
const message = ref('')
const isSuccess = ref(false)

const handleForgotPassword = async () => {
  if (!forgotFormRef.value) return

  await forgotFormRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    message.value = ''

    try {
      const res = await forgotPassword(forgotForm.email)

      if (res.data.code === 200) {
        isSuccess.value = true
        message.value = 'If your email is registered, we have sent a password reset link to your inbox.'
        ElMessage.success('Reset link sent successfully')
      } else {
        ElMessage.error(res.data.message || 'Failed to send reset link')
      }
    } catch (error) {
      console.error(error)
      ElMessage.error(error.response?.data?.message || 'Failed to send reset link')
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



.forgot-container {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding-top: 4rem;
  min-height: calc(100vh - 100px);
}

.forgot-card {
  width: 100%;
  max-width: 420px;
  border-radius: 12px;
  border: 1px solid var(--border, #eaeaea);
}
</style>