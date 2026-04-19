<script setup>
import { computed, onMounted, ref } from 'vue'
import { getPending } from '../api'
import { useRouter } from 'vue-router'

const router = useRouter()
const items = ref([])
const error = ref('')

const staleItems = computed(() => items.value.filter(i => i.stale))
const normalItems = computed(() => items.value.filter(i => !i.stale))

onMounted(async () =>{
    try{
        items.value = await getPending()
    }catch(e){
        error.value = String(e.message || e)
    }
})

function openItem(item){
    router.push(`/review/${item.id}`)
}
</script>

<template>
    <main class="container">
        <h1>Review Center</h1>

        <p v-if="error" style="color:#b91c1c;">{{ error }}</p>

        <section v-if="staleItems.length">
            <h2>Waiting > 3 days</h2>
            <ul>
                <li v-for="i in staleItems" :key="i.id" class="card" @click="openItem(i)">
                    <div class="title">{{ i.title }}</div>
                    <div class="meta">{{ i.submitterName }} · {{ i.category }} · {{ i.submittedAt }}</div>
                </li>
            </ul>
        </section>
        <section>
            <h2>Pending</h2>
            <ul>
                <li v-for="i in normalItems" :key="i.id" class="card" @click="openItem(i)">
                <div class="title">{{ i.title }}</div>
                <div class="meta">{{ i.submitterName }} · {{ i.category }} · {{ i.submittedAt }}</div>
                </li>
            </ul>
        </section>
    </main>
</template>

<style scoped>
h2 { font-size: 16px; margin: 16px 0 8px; }
ul { list-style: none; padding: 0; margin: 0; }
.card { padding: 12px; background: white; border: 1px solid #e5e7eb; border-radius: 10px; cursor: pointer; }
.card.stale { border-left: 6px solid #f59e0b; }
.title { font-weight: 600; margin-bottom: 4px; }
.meta { font-size: 13px; color: #6b7280; }
</style>