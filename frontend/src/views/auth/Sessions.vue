<template>
  <div class="wrap">
    <header class="top">
      <h1>Heritage Resource Hall</h1>
      <div class="header-actions">
        <button type="button" class="btn" @click="$router.push('/')">Home</button>
      </div>
    </header>

    <div class="session-container">
      <el-card class="session-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <h2>Active Sessions</h2>
            <p class="subtitle">Manage your logged-in devices and active sessions</p>
          </div>
        </template>

        <el-table :data="sessions" style="width: 100%" v-loading="loading">
          <el-table-column label="Device & IP" min-width="180">
            <template #default="scope">
              <div class="device-info">
                <strong>{{ scope.row.deviceInfo || 'Unknown Device' }}</strong>
                <div class="ip-addr">{{ scope.row.ipAddress }}</div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="Login Time" prop="loginTime" min-width="160">
            <template #default="scope">
              {{ formatTime(scope.row.loginTime) }}
            </template>
          </el-table-column>

          <el-table-column label="Status" width="120" align="center">
            <template #default="scope">
              <el-tag v-if="scope.row.current" type="success" effect="dark" size="small">
                Current
              </el-tag>
              <el-tag v-else type="info" variant="plain" size="small">
                Active
              </el-tag>
            </template>
          </el-table-column>

          <el-table-column label="Action" width="100" align="right">
            <template #default="scope">
              <el-button 
                v-if="!scope.row.current"
                type="danger" 
                link
                @click="handleLogoutSession(scope.row.jti)"
              >
                Revoke
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="form-footer">
          <p>If you see any unfamiliar activity, we recommend changing your password.</p>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getActiveSessions, logoutSession } from '../../api/auth.js'

const sessions = ref([])
const loading = ref(false)

const fetchSessions = async () => {
  loading.value = true
  try {
    const res = await getActiveSessions()
    if (res.data.code === 200) {
      sessions.value = res.data.data
    }
  } catch (error) {
    ElMessage.error('Failed to load sessions')
  } finally {
    loading.value = false
  }
}

const handleLogoutSession = (jti) => {
  ElMessageBox.confirm(
    'Are you sure you want to terminate this session? The device will be logged out immediately.',
    'Warning',
    { confirmButtonText: 'Revoke Access', cancelButtonText: 'Cancel', type: 'warning' }
  ).then(async () => {
    try {
      const res = await logoutSession(jti)
      if (res.data.code === 200) {
        ElMessage.success('Session terminated')
        fetchSessions() // 刷新列表
      }
    } catch (error) {
      ElMessage.error('Failed to terminate session')
    }
  })
}

const formatTime = (timeStr) => {
  if (!timeStr) return '-'
  return new Date(timeStr).toLocaleString()
}

onMounted(() => {
  fetchSessions()
})
</script>

<style scoped>
/* 继承自 Login/Register 的基础样式 */
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
.btn:hover { background: #f9f9f9; }

/* Sessions 专用布局 */
.session-container {
  display: flex;
  justify-content: center;
  padding-top: 4rem;
  min-height: calc(100vh - 100px);
}
.session-card {
  width: 100%;
  max-width: 800px; /* 列表页稍微宽一些 */
  border-radius: 12px;
  border: 1px solid var(--border, #eaeaea);
}
.card-header { text-align: center; }
.card-header h2 {
  margin: 0;
  font-family: var(--font-serif, 'Georgia', serif);
  font-size: 1.75rem;
  color: var(--text, #333);
}
.subtitle {
  margin: 0.5rem 0 0;
  color: var(--muted, #666);
  font-size: 0.875rem;
}
.ip-addr {
  font-size: 0.75rem;
  color: var(--muted, #999);
}
.form-footer {
  text-align: center;
  margin-top: 2rem;
  font-size: 0.85rem;
  color: var(--muted, #888);
}
</style>