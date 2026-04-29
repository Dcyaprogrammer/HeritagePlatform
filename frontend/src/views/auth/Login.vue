<template>
  <div class="wrap">
    <header class="top">
      <h1>Heritage Resource Hall</h1>
      <div class="header-actions">
        <button type="button" class="btn" @click="$router.push('/')">Home</button>
      </div>
    </header>

    <div class="login-container">
      <el-card class="login-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <h2>Welcome Back</h2>
            <p class="subtitle">Login to your Heritage Platform account</p>
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
            <el-button type="primary" size="large" :loading="loading" class="submit-btn" @click="handleLogin">
              Login
            </el-button>
          </el-form-item>

          <div class="form-footer">
            <span>Don't have an account? </span>
            <router-link to="/register">Register</router-link>
            <br>
            <span>Forgot your password? </span>
            <router-link to="/forgot-password">Click here</router-link>
          </div>
        </el-form>
      </el-card>
    </div>
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
        const { token, username, roles } = res.data.data
        setToken(token)
        setUserInfo(username, roles)
        
        ElMessage.success('Login successful')
        if (Array.isArray(roles) && roles.includes('ADMIN')) {
          router.push('/admin/users')
        } else {
          router.push('/')
        }
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

.submit-btn {
  width: 100%;
  background: var(--accent, #409eff);
  border-color: var(--accent, #409eff);
  border-radius: 6px;
  font-weight: 600;
}

.submit-btn:hover {
  opacity: 0.9;
}

.form-footer {
  text-align: center;
  margin-top: 1.5rem;
  font-size: 0.875rem;
  color: var(--muted, #666);
}

.link {
  color: var(--accent, #409eff);
  text-decoration: none;
  font-weight: 500;
}

.link:hover {
  text-decoration: underline;
}
</style>
