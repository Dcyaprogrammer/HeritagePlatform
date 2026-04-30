<template>
  <AuthPageFrame
    eyebrow="Join the community"
    aside-title="Create your contributor profile"
    aside-lead="Register to save work in progress, apply for contributor access, and help grow a shared catalog of heritage resources."
    :points="asidePoints"
    quote="New voices and local knowledge make the archive stronger for everyone."
  >
    <div class="register-container">
      <el-card class="register-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <h2>Create Account</h2>
            <p class="subtitle">Join the Heritage Platform community</p>
          </div>
        </template>

        <el-form ref="registerFormRef" :model="registerForm" :rules="rules" label-position="top">
          <el-form-item label="Username" prop="username">
            <el-input v-model="registerForm.username" placeholder="Enter username" prefix-icon="User" size="large" />
          </el-form-item>

          <el-form-item label="Email" prop="email">
            <el-input v-model="registerForm.email" placeholder="Enter email" prefix-icon="Message" size="large" />
          </el-form-item>

          <el-form-item label="Password" prop="password">
            <el-input v-model="registerForm.password" type="password" placeholder="Enter password" prefix-icon="Lock" size="large"
              show-password />
          </el-form-item>

          <el-form-item label="Confirm Password" prop="confirmPassword">
            <el-input v-model="registerForm.confirmPassword" type="password" placeholder="Confirm password" prefix-icon="Lock"
              size="large" show-password @keyup.enter="handleRegister" />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" size="large" :loading="loading" class="submit-btn" @click="handleRegister">
              Register
            </el-button>
          </el-form-item>

          <div class="form-footer">
            <span>Already have an account? </span>
            <router-link to="/login" class="link">Login</router-link>
          </div>
        </el-form>
      </el-card>
    </div>
  </AuthPageFrame>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register, login, setToken, setUserInfo } from '../../api/auth.js'
import AuthPageFrame from '../../components/auth/AuthPageFrame.vue'

const asidePoints = [
  'One account for browsing and optional submissions',
  'Secure sign-in with email and password',
  'Start exploring before you apply to contribute',
]

const router = useRouter()
const registerFormRef = ref(null)
const loading = ref(false)

const registerForm = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('Passwords do not match'))
  } else {
    callback()
  }
}

const rules = {
  username: [
    { required: true, message: 'Please enter username', trigger: 'blur' },
    { min: 3, max: 20, message: 'Username must be 3-20 characters', trigger: 'blur' }
  ],
  email: [
    { required: true, message: 'Please enter email', trigger: 'blur' },
    { type: 'email', message: 'Please enter valid email', trigger: 'blur' }
  ],
  password: [
    { required: true, message: 'Please enter password', trigger: 'blur' },
    { min: 6, message: 'Password must be at least 6 characters', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: 'Please confirm password', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!registerFormRef.value) return

  await registerFormRef.value.validate(async (valid) => {
    if (!valid) return

    loading.value = true
    try {
      const res = await register({
        username: registerForm.username,
        password: registerForm.password,
        email: registerForm.email
      })

      if (res.data.code === 200) {
        ElMessage.success('Registration successful, logging in...')
        
        const loginRes = await login({
          username: registerForm.username,
          password: registerForm.password
        })

        if (loginRes.data.code === 200) {
          const { token, username, roles } = loginRes.data.data
          setToken(token)
          setUserInfo(username, roles)
          router.push('/')
        }
      } else {
        ElMessage.error(res.data.message || 'Registration failed')
      }
    } catch (error) {
      console.error('Registration error:', error)
      ElMessage.error(error.response?.data?.message || 'Registration failed')
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  width: 100%;
}

.register-card {
  width: 100%;
  max-width: 420px;
  border-radius: var(--radius);
  border: 1px solid var(--border);
  box-shadow: var(--card-shadow);
}

.card-header {
  text-align: center;
}

.card-header h2 {
  margin: 0;
  color: var(--ink);
  font-family: var(--font-serif, 'Georgia', serif);
  font-size: 1.75rem;
}

.subtitle {
  margin: 0.5rem 0 0;
  color: var(--muted);
  font-size: 0.875rem;
}

.submit-btn {
  width: 100%;
  background: var(--accent);
  border-color: var(--accent);
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
  color: var(--muted);
}

.link {
  color: var(--accent);
  text-decoration: none;
  font-weight: 500;
}

.link:hover {
  text-decoration: underline;
}
</style>
