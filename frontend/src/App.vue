<script setup>
import { onMounted, ref } from 'vue'

const status = ref('加载中...')

onMounted(async () => {
  try {
    const response = await fetch('/api/health')
    if (!response.ok) {
      throw new Error('请求失败')
    }
    const data = await response.json()
    status.value = `后端状态：${data.status}`
  } catch (error) {
    status.value = '后端未连接，请先启动 Spring Boot'
  }
})
</script>

<template>
  <main class="container">
    <h1>Heritage Platform</h1>
    <p>Vue 前端已接入。</p>
    <p>{{ status }}</p>
  </main>
</template>
