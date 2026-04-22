<script setup>
import { onMounted, ref } from 'vue'
import { getReviewDetail } from '../../api'
import { useRoute, useRouter } from 'vue-router'
const route = useRoute()
const router = useRouter()
const detail = ref(null)
const error = ref('')
onMounted(async () => {
  try {
    detail.value = await getReviewDetail(route.params.id)
  } catch (e) {
    error.value = String(e.message || e)
  }
})
</script>

<template>
    <main class="container">
        <button @click="router.push('/admin/resource-review')">Back</button>

        <p v-if="error" style="color:#b91c1c;">{{ error }}</p>

        <div v-if="detail" class="card">
            <h1 style="margin: 0 0 8px;">{{ detail.title }}</h1>
            <p>{{ detail.submitterName }} · {{ detail.category }}</p>
            <p>Status: {{ detail.status }}</p>
            <p>Submitted: {{ detail.submittedAt }}</p>
            <p>Version: {{ detail.version }}</p>
            <p v-if="detail.rejectionReason">Rejection: {{ detail.rejectionReason }}</p>
        </div>
    </main>
</template>

<style scoped>
.card { padding: 12px; background: white; border: 1px solid #e5e7eb; border-radius: 10px; margin-top: 12px; }
button { border: 1px solid #e5e7eb; background: white; padding: 8px 10px; border-radius: 8px; cursor: pointer; }
</style>

