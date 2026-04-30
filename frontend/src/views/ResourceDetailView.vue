<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import ResourceImageCarousel from '../components/ResourceImageCarousel.vue'
import CommentSection from '../components/CommentSection.vue'
import { getPublicResourceDetail, getComments } from '../api/resource.js'
import { getToken } from '../api/auth.js'

const route = useRoute()
const loading = ref(true)
const resource = ref(null)
const comments = ref([])
const isLoggedIn = ref(false)
const currentUserName = ref('')

const id = computed(() => Number(route.params.id))

async function load() {
  const n = id.value
  if (Number.isNaN(n)) return
  loading.value = true
  try {
    const detailRes = await getPublicResourceDetail(n)
    if (detailRes.data.code === 200 && detailRes.data.data) {
      resource.value = detailRes.data.data
    } else {
      resource.value = null
    }
    
    try {
      const commentsRes = await getComments(n)
      comments.value = commentsRes.data.data || []
    } catch (e) {
      console.error('Failed to load comments:', e)
      comments.value = []
    }
  } catch {
    resource.value = null
    comments.value = []
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  load()
  isLoggedIn.value = !!getToken()
  currentUserName.value = localStorage.getItem('username') || 'Guest'
})
watch(id, load)

function formatDate(iso) {
  try {
    return new Intl.DateTimeFormat('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    }).format(new Date(iso))
  } catch {
    return iso
  }
}
</script>

<template>
  <div v-if="loading" class="page inner notfound">
    <p>Loading resource...</p>
  </div>
  <div v-else-if="resource" class="page inner">
    <nav class="crumb" aria-label="Breadcrumb">
      <RouterLink to="/">Discover</RouterLink>
      <span class="sep" aria-hidden="true">/</span>
      <span class="current">{{ resource.title }}</span>
    </nav>

    <ResourceImageCarousel :attachments="resource.attachments || []" />

    <article class="article">
      <header class="head">
        <h1 class="title">{{ resource.title }}</h1>
        <div class="meta">
          <span v-if="resource.categoryName" class="pill">{{ resource.categoryName }}</span>
          <span v-if="resource.locationName" class="loc">{{ resource.locationName }}</span>
        </div>
        <ul v-if="resource.tags && resource.tags.length" class="tags" aria-label="Tags">
          <li v-for="t in resource.tags" :key="t.id" class="tag">{{ t.name }}</li>
        </ul>
      </header>

      <div class="prose">
        <p class="desc">{{ resource.description }}</p>
        <p v-if="resource.copyrightDeclaration" class="legal">
          <strong>Copyright &amp; usage</strong><br />
          {{ resource.copyrightDeclaration }}
        </p>
        <dl class="facts">
          <div v-if="resource.contributorName" class="fact">
            <dt>Contributor</dt>
            <dd>{{ resource.contributorName }}</dd>
          </div>
          <div class="fact">
            <dt>Published</dt>
            <dd>{{ formatDate(resource.createdAt) }}</dd>
          </div>
          <div class="fact">
            <dt>Last updated</dt>
            <dd>{{ formatDate(resource.updatedAt) }}</dd>
          </div>
        </dl>
      </div>
    </article>

    <CommentSection
      :resource-id="resource.id"
      :initial-comments="comments"
      :is-logged-in="isLoggedIn"
      :current-user-id="999"
      :current-user-name="currentUserName"
    />
  </div>

  <div v-else class="page inner notfound">
    <h1>Resource not found</h1>
    <p>This entry does not exist, is not approved, or has been archived and is hidden from visitors.</p>
    <RouterLink to="/">Back to Discover</RouterLink>
  </div>
</template>

<style scoped>
/* Match public-shell width (1120px); avoid double horizontal padding — shell already pads */
.inner {
  max-width: 1120px;
  width: 100%;
  margin: 0 auto;
  padding: 0;
}
.notfound {
  padding-top: 4rem;
  text-align: center;
}
.crumb {
  font-size: 0.875rem;
  color: var(--muted);
  margin-bottom: 1.25rem;
  margin-top: 1rem;
}
.crumb a {
  color: var(--accent);
}
.sep {
  margin: 0 0.35rem;
  opacity: 0.6;
}
.current {
  color: var(--ink);
}
.article {
  margin-top: 1.75rem;
}
.head {
  margin-bottom: 1.25rem;
}
.title {
  margin: 0 0 0.65rem;
  font-family: var(--font-serif);
  font-size: clamp(1.5rem, 3vw, 2rem);
  font-weight: 700;
  line-height: 1.3;
}
.meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.5rem 0.75rem;
  margin-bottom: 0.65rem;
}
.pill {
  font-size: 0.8125rem;
  font-weight: 600;
  padding: 0.2rem 0.55rem;
  border-radius: 6px;
  background: color-mix(in srgb, var(--accent) 12%, var(--surface));
  color: var(--accent);
  border: 1px solid color-mix(in srgb, var(--accent) 28%, var(--border));
}
.loc {
  font-size: 0.875rem;
  color: var(--muted);
}
.tags {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-wrap: wrap;
  gap: 0.35rem;
}
.tag {
  font-size: 0.75rem;
  padding: 0.25rem 0.6rem;
  border-radius: 999px;
  background: color-mix(in srgb, var(--accent) 16%, var(--surface) 84%);
  color: var(--accent);
  border: 1px solid color-mix(in srgb, var(--accent) 22%, var(--border) 78%);
  font-weight: 500;
}
.prose {
  margin-top: 1.5rem;
}
.desc {
  font-size: 1.05rem;
  line-height: 1.6;
  margin-bottom: 1.5rem;
}
.legal {
  background: var(--surface);
  padding: 1.25rem 1.5rem;
  border-left: 4px solid var(--border);
  font-size: 0.875rem;
  margin-bottom: 1.5rem;
}
.facts {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1.25rem;
  background: var(--surface);
  padding: 1.5rem;
  border-radius: var(--radius);
  border: 1px solid var(--border);
}
.fact dt {
  font-size: 0.75rem;
  color: var(--muted);
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 0.25rem;
}
.fact dd {
  margin: 0;
  font-weight: 500;
}
</style>
