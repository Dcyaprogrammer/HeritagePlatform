<template>
  <div class="admin-layout">
    <el-container class="layout-container">
      <!-- Sidebar -->
      <el-aside width="220px" class="sidebar">
        <div class="sidebar-header">
          <h3>Admin Panel</h3>
        </div>
        <el-menu :default-active="$route.path" router class="admin-menu" background-color="#304156"
          text-color="#bfcbd9" active-text-color="#409EFF">
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
        </el-menu>
      </el-aside>

      <!-- Main Content -->
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { UserFilled, DocumentChecked } from '@element-plus/icons-vue'
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}

.layout-container {
  height: 100%;
}

.sidebar {
  background-color: #304156;
  color: #fff;
}

.sidebar-header {
  padding: 20px;
  text-align: center;
  border-bottom: 1px solid #1f2d3d;
}

.sidebar-header h3 {
  margin: 0;
  color: #fff;
  font-size: 18px;
}

.admin-menu {
  border-right: none;
}

.main-content {
  padding: 0;
  background-color: #f0f2f5;
  overflow-y: auto;
}
</style>
