<template>
  <div class="admin-layout">
    <el-container class="layout-container">
      <!-- Sidebar -->
      <el-aside width="220px" class="sidebar">
        <div class="sidebar-header">
          <h3>Admin Panel</h3>
        </div>
        <el-menu :default-active="$route.path" router class="admin-menu" background-color="var(--ink)"
          text-color="#e2ddd4" active-text-color="#faf8f3">
          <el-menu-item index="/admin/users">
            <el-icon>
              <UserFilled />
            </el-icon>
            <span>User Management</span>
          </el-menu-item>
          <el-menu-item index="/admin/review">
            <el-icon>
              <DocumentChecked />
            </el-icon>
            <span>Contributor Review</span>
          </el-menu-item>
          <el-menu-item index="/admin/resource-review">
            <el-icon>
            <DocumentChecked />
            </el-icon>
            <span>Resource Review</span>
          </el-menu-item>
          <el-menu-item index="/admin/resources">
            <el-icon>
              <DocumentChecked />
            </el-icon>
            <span>All Resources</span>
          </el-menu-item>
          <el-menu-item index="/admin/master-data">
            <el-icon>
              <DocumentChecked />
            </el-icon>
            <span>Master Data</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- Main Content -->
      <el-container class="right-container">
        <!-- Top Header -->
        <el-header class="top-header">
          <div class="header-right">
            <el-button type="primary" plain class="home-btn" @click="router.push('/')">
              <el-icon><House /></el-icon>
            </el-button>
            <el-dropdown @command="handleCommand">
              <span class="user-info">
                <el-avatar :size="32" :src="defaultAvatar" />
                <span class="username">{{ username || 'Admin' }}</span>
                <el-icon><ArrowDown /></el-icon>
              </span>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">Profile</el-dropdown-item>
                  <el-dropdown-item divided command="logout">Logout</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>

        <el-main class="main-content">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { UserFilled, DocumentChecked, ArrowDown, House } from '@element-plus/icons-vue'
import { logout } from '../../api/auth.js'

const router = useRouter()
const username = ref(localStorage.getItem('username') || '')
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png'

const handleCommand = (command) => {
  if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'logout') {
    logout()
    ElMessage.success('Logged out')
    router.push('/login')
  }
}
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}

.layout-container {
  height: 100%;
}

.sidebar {
  background-color: var(--ink);
  color: var(--surface);
}

.sidebar-header {
  padding: 20px;
  text-align: center;
  border-bottom: 1px solid color-mix(in srgb, var(--ink) 75%, #fff 25%);
}

.sidebar-header h3 {
  margin: 0;
  color: var(--surface);
  font-size: 18px;
  font-family: var(--font-serif);
}

.admin-menu {
  border-right: none;
}

.right-container {
  background-color: var(--bg);
}

.top-header {
  background-color: var(--surface);
  display: flex;
  align-items: center;
  justify-content: flex-end;
  box-shadow: var(--card-shadow);
  z-index: 10;
  border-bottom: 1px solid var(--border);
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.home-btn {
  border-radius: 50%;
  padding: 8px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.user-info:hover {
  background-color: color-mix(in srgb, var(--surface) 85%, var(--accent) 15%);
}

.username {
  font-size: 14px;
  color: var(--muted);
}

.main-content {
  padding: 20px;
  overflow-y: auto;
}
</style>
