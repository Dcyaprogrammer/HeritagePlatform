<template>
  <div class="admin-layout">
    <el-container class="layout-container">
      <!-- Sidebar -->
      <el-aside width="220px" class="sidebar">
        <div class="sidebar-header">
          <h3>Admin Panel</h3>
        </div>
        <el-menu :default-active="$route.path" router class="admin-menu" background-color="var(--surface)"
          text-color="var(--muted)" active-text-color="var(--accent)">
          <el-menu-item index="/admin/users">
            <el-icon>
              <UserFilled />
            </el-icon>
            <span>User Management</span>
          </el-menu-item>
          <el-menu-item index="/admin/review">
            <el-icon>
              <Stamp />
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
              <FolderOpened />
            </el-icon>
            <span>All Resources</span>
          </el-menu-item>
          <el-menu-item index="/admin/master-data">
            <el-icon>
              <Setting />
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
            <el-button type="primary" class="home-btn" @click="router.push('/')">
              Home
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
import {
  UserFilled,
  DocumentChecked,
  ArrowDown,
  Stamp,
  FolderOpened,
  Setting,
} from '@element-plus/icons-vue'
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
  background-color: var(--surface);
  border-right: 1px solid var(--border);
}

.sidebar-header {
  padding: 24px 20px;
  text-align: center;
  border-bottom: 1px solid var(--border);
}

.sidebar-header h3 {
  margin: 0;
  color: var(--accent);
  font-size: 20px;
  font-weight: 700;
  letter-spacing: 0.3px;
  font-family: var(--font-serif);
}

.admin-menu {
  border-right: none;
  padding: 8px;
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
  background-color: var(--accent);
  border-color: var(--accent);
  color: white;
  border-radius: 8px;
  padding: 8px 16px;
  font-weight: 500;
}

.home-btn:hover {
  background-color: var(--accent-soft);
  border-color: var(--accent-soft);
  color: white;
  transform: translateY(-1px);
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
  color: var(--ink-soft);
  font-weight: 600;
}

.main-content {
  padding: 32px;
  overflow-y: auto;
  width: 100%;
  max-width: 1680px;
  margin: 0 auto;
}

/* Menu item styling */
.admin-menu :deep(.el-menu-item) {
  margin-bottom: 4px;
  border-radius: 8px;
  font-weight: 600;
  transition: all 0.2s ease;
}

.admin-menu :deep(.el-menu-item:hover) {
  background-color: color-mix(in srgb, var(--accent) 8%, transparent);
}

.admin-menu :deep(.el-menu-item.is-active) {
  background-color: color-mix(in srgb, var(--accent) 8%, transparent);
  color: var(--accent);
}
</style>
