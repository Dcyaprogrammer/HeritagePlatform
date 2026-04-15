<script setup lang="ts">
<<<<<<< HEAD
import { computed } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import ResourceImageCarousel from '@/components/ResourceImageCarousel.vue'
import CommentSection from '@/components/CommentSection.vue'
import { getComments, getResourceById } from '@/api/mockData'
=======
import { computed, onMounted, ref, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import ResourceImageCarousel from '@/components/ResourceImageCarousel.vue'
import CommentSection from '@/components/CommentSection.vue'
import { getComments, getResourceById } from '@/api/resource'
import { devAuth } from '@/config/auth'
import type { Comment, ResourceDetail } from '@/types/resource'
>>>>>>> 31a296c (feat: update View function)

const route = useRoute()

const id = computed(() => Number(route.params.id))

<<<<<<< HEAD
const resource = computed(() => {
  const n = id.value
  if (Number.isNaN(n)) return undefined
  return getResourceById(n)
})

const comments = computed(() => {
  const n = id.value
  if (Number.isNaN(n)) return []
  return getComments(n)
})

function formatDate(iso: string) {
  try {
    return new Intl.DateTimeFormat('zh-CN', { dateStyle: 'long' }).format(new Date(iso))
=======
const resource = ref<ResourceDetail>()
const comments = ref<Comment[]>([])
const loading = ref(false)

async function load() {
  const n = id.value
  if (Number.isNaN(n)) return
  loading.value = true
  try {
    resource.value = await getResourceById(n)
    comments.value = await getComments(n)
  } catch {
    resource.value = undefined
    comments.value = []
  } finally {
    loading.value = false
  }
}

onMounted(load)
watch(id, load)

function formatDate(iso: string) {
  try {
    return new Intl.DateTimeFormat('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
    }).format(new Date(iso))
>>>>>>> 31a296c (feat: update View function)
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

    <ResourceImageCarousel :attachments="resource.attachments" />

    <article class="article">
      <header class="head">
        <h1 class="title">{{ resource.title }}</h1>
        <div class="meta">
          <span v-if="resource.category" class="pill">{{ resource.category.name }}</span>
          <span v-if="resource.location_name" class="loc">{{ resource.location_name }}</span>
        </div>
        <ul v-if="resource.tags.length" class="tags" aria-label="Tags">
          <li v-for="t in resource.tags" :key="t.id" class="tag">{{ t.name }}</li>
        </ul>
      </header>

      <div class="prose">
        <p class="desc">{{ resource.description }}</p>
        <p v-if="resource.copyright_declaration" class="legal">
          <strong>Copyright &amp; usage</strong><br />
          {{ resource.copyright_declaration }}
        </p>
        <dl class="facts">
          <div v-if="resource.contributorName" class="fact">
            <dt>Contributor</dt>
            <dd>{{ resource.contributorName }}</dd>
          </div>
          <div class="fact">
            <dt>Published</dt>
            <dd>{{ formatDate(resource.created_at) }}</dd>
          </div>
          <div class="fact">
            <dt>Last updated</dt>
            <dd>{{ formatDate(resource.updated_at) }}</dd>
          </div>
        </dl>
      </div>
    </article>

    <CommentSection
      :resource-id="resource.id"
      :initial-comments="comments"
      :is-logged-in="devAuth.isLoggedIn"
      :current-user-id="devAuth.userId"
      :current-user-name="devAuth.userName"
    />
  </div>

  <div v-else class="page inner notfound">
    <h1>Resource not found</h1>
    <p>This entry does not exist, is not approved, or has been archived and is hidden from visitors.</p>
    <RouterLink to="/">Back to Discover</RouterLink>
  </div>
</template>

<style scoped>
.inner {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 1.25rem;
}
.crumb {
  font-size: 0.875rem;
  color: var(--muted);
  margin-bottom: 1.25rem;
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
  padding: 0.2rem 0.5rem;
  border-radius: 999px;
  background: color-mix(in srgb, var(--accent) 8%, var(--surface));
  color: var(--muted);
  border: 1px solid var(--border);
}
.prose {
  font-size: 1rem;
  color: var(--ink);
}
.desc {
  margin: 0 0 1.25rem;
  white-space: pre-wrap;
  word-break: break-word;
}
.legal {
  margin: 0 0 1.5rem;
  padding: 1rem 1.1rem;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 10px;
  font-size: 0.9rem;
  color: var(--muted);
}
.legal strong {
  color: var(--ink);
}
.facts {
  margin: 0;
  display: grid;
  gap: 0.75rem;
}
.fact {
  display: grid;
  grid-template-columns: 7rem 1fr;
  gap: 0.5rem;
  font-size: 0.875rem;
}
@media (max-width: 520px) {
  .fact {
    grid-template-columns: 1fr;
  }
}
.fact dt {
  margin: 0;
  color: var(--muted);
  font-weight: 600;
}
.fact dd {
  margin: 0;
}
.notfound {
  padding-top: 2rem;
}
.notfound h1 {
  font-family: var(--font-serif);
  font-size: 1.5rem;
}
</style>