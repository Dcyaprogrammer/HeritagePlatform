<template>
  <div class="not-found">
    <el-result icon="error" title="404" sub-title="Page Not Found">
      <template #extra>
        <el-button type="primary" @click="$router.push('/')">
          Back Home
        </el-button>
      </template>
    </el-result>
  </div>
</template>

<style scoped>
.not-found {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
}
</style>
