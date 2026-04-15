<template>
  <div class="login-container">
    <el-card class="login-card">
      <template #header>
        <div class="card-header">
          <h2>Heritage Platform Login</h2>
        </div>
      </template>

      <el-form ref="loginFormRef" :model="loginForm" :rules="rules" label-position="top">
        <el-form-item label="Username" prop="username">
          <el-input v-model="loginForm.username" placeholder="Enter username" prefix-icon="User" size="large" />
        </el-form-item>

        <el-form-item label="Password" prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="Enter password" prefix-icon="Lock" size="large"
            show-password @keyup.enter="handleLogin" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" size="large" :loading="loading" style="width: 100%" @click="handleLogin">
            Login
          </el-button>
        </el-form-item>

        <div class="form-footer">
          <span>Don't have an account? </span>
          <router-link to="/register">Register</router-link>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login, setToken, setUserInfo } from '../../api/auth.js'

const router = useRouter()
const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: 'Please enter username', trigger: 'blur' }],
  password: [{ required: true, message: 'Please enter password', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const res = await login(loginForm)
      
      if (res.data.code === 200) {
        const { token, username, role } = res.data.data
        setToken(token)
        setUserInfo(username, role)
        
        ElMessage.success('Login successful')
        router.push('/admin/users')
      } else {
        ElMessage.error(res.data.message || 'Login failed')
      }
    } catch (error) {
      console.error('Login error:', error)
      ElMessage.error(error.response?.data?.message || 'Login failed, please check your credentials')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-card {
  width: 400px;
}

.card-header h2 {
  margin: 0;
  text-align: center;
  color: #333;
}

.form-footer {
  text-align: center;
  margin-top: 10px;
}

.form-footer a {
  color: #409eff;
  text-decoration: none;
}

.form-footer a:hover {
  text-decoration: underline;
}
</style>