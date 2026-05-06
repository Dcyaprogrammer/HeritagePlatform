<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import ResourceImageCarousel from '../components/ResourceImageCarousel.vue'
import CommentSection from '../components/CommentSection.vue'
import { getPublicResourceDetail, getComments } from '../api/resource.js'
import { getToken } from '../api/auth.js'
import { getAttachmentType, getAttachmentSrc, filterByType, getTypeMeta } from '../utils/attachmentUtils.js'
import CustomAudioPlayer from '../components/CustomAudioPlayer.vue'

const route = useRoute()
const loading = ref(true)
const resource = ref(null)
const comments = ref([])
const isLoggedIn = ref(false)
const currentUserName = ref('')

const id = computed(() => Number(route.params.id))

const audioAttachments = computed(() => filterByType(resource.value?.attachments, 'audio'))
const fileAttachments = computed(() =>
  (resource.value?.attachments || [])
    .filter(a => !['image', 'video', 'audio'].includes(getAttachmentType(a)))
    .map(a => ({ ...a, meta: getTypeMeta(getAttachmentType(a)) }))
)

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
  <div v-else-if="resource" class="page detail-page">
    <div class="detail-container">

      <nav class="crumb" aria-label="Breadcrumb">
        <RouterLink to="/">Discover</RouterLink>
        <span class="sep" aria-hidden="true">/</span>
        <span class="current">{{ resource.title }}</span>
      </nav>

      <!-- Hero image -->
      <section class="hero-card">
        <ResourceImageCarousel :attachments="resource.attachments || []" />
      </section>

      <!-- Main content sections -->
      <main class="main-content">
        <div class="section-block">
          <h1 class="title">{{ resource.title }}</h1>
          <div class="meta">
            <span v-if="resource.categoryName" class="pill">{{ resource.categoryName }}</span>
            <span v-if="resource.locationName" class="loc">{{ resource.locationName }}</span>
          </div>
          <ul v-if="resource.tags && resource.tags.length" class="tags" aria-label="Tags">
            <li v-for="t in resource.tags" :key="t.id" class="tag">{{ t.name }}</li>
          </ul>
          <dl class="meta-info">
            <div v-if="resource.contributorName" class="meta-item">
              <dt>Contributor</dt>
              <dd>{{ resource.contributorName }}</dd>
            </div>
            <div class="meta-item">
              <dt>Published</dt>
              <dd>{{ formatDate(resource.createdAt) }}</dd>
            </div>
            <div class="meta-item">
              <dt>Last updated</dt>
              <dd>{{ formatDate(resource.updatedAt) }}</dd>
            </div>
          </dl>
        </div>

        <div class="section-divider" />

        <div class="section-block">
          <h2 class="section-title">Description</h2>
          <p class="desc">{{ resource.description }}</p>
          <p v-if="resource.copyrightDeclaration" class="legal">
            <strong>Copyright &amp; usage</strong><br />
            {{ resource.copyrightDeclaration }}
          </p>
        </div>

        <template v-if="audioAttachments.length || fileAttachments.length">
          <div class="section-divider" />

          <div class="section-block">
            <section v-if="audioAttachments.length" class="media-section" aria-label="Audio">
              <h3 class="media-heading">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><path d="M12 3v10.55c-.59-.34-1.27-.55-2-.55-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4V7h4V3h-6z"/></svg>
                Audio
              </h3>
              <ul class="audio-list">
                <li v-for="audio in audioAttachments" :key="audio.id" class="audio-item">
                  <CustomAudioPlayer :src="getAttachmentSrc(audio)" :name="audio.display_name || audio.displayName || 'Audio file'" />
                  <span class="audio-name">{{ audio.display_name || audio.displayName || 'Audio file' }}</span>
                </li>
              </ul>
            </section>

            <section v-if="fileAttachments.length" class="media-section" aria-label="Files">
              <h3 class="media-heading">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><path d="M6 2c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V8l-6-6H6zm7 7V3.5L18.5 9H13zM8 12h8v2H8v-2zm0 4h8v2H8v-2z"/></svg>
                Documents &amp; Files
              </h3>
              <ul class="file-list">
                <li v-for="file in fileAttachments" :key="file.id" class="file-item">
                  <span class="file-type-badge" :style="{ color: file.meta.color, background: file.meta.bg }">
                    {{ file.meta.label }}
                  </span>
                  <a :href="getAttachmentSrc(file)" :download="file.display_name || file.displayName" class="file-name" target="_blank" rel="noopener">
                    {{ file.display_name || file.displayName || 'Download file' }}
                  </a>
                </li>
              </ul>
            </section>
          </div>
        </template>

        <div class="section-divider" />

        <CommentSection
          :resource-id="resource.id"
          :initial-comments="comments"
          :is-logged-in="isLoggedIn"
          :current-user-id="999"
          :current-user-name="currentUserName"
          :initial-like-count="resource.likeCount || 0"
          :initial-favorite-count="resource.favoriteCount || 0"
        />
      </main>
    </div>
  </div>

  <div v-else class="page inner notfound">
    <h1>Resource not found</h1>
    <p>This entry does not exist, is not approved, or has been archived and is hidden from visitors.</p>
    <RouterLink to="/">Back to Discover</RouterLink>
  </div>
</template>

<style scoped>
.inner {
  max-width: 1320px;
  width: 100%;
  margin: 0 auto;
  padding: 0;
}
.detail-page {
  padding: 0;
}
.notfound {
  padding-top: 4rem;
  text-align: center;
}

/* ===== Detail Container ===== */
.detail-container {
  max-width: 1320px;
  width: 100%;
  margin: 0 auto;
  padding: 1.25rem 1.5rem 4rem;
}

/* ===== Breadcrumb ===== */
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

/* ===== Hero Card ===== */
.hero-card {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  box-shadow: var(--card-shadow);
  padding: 0.45rem;
  overflow: hidden;
  margin-bottom: 1.25rem;
}
.hero-card :deep(.carousel) {
  gap: 0.9rem;
}
.hero-card :deep(.stage) {
  aspect-ratio: 16 / 8.4;
  min-height: clamp(300px, 40vw, 640px);
  border-radius: calc(var(--radius) - 4px);
}
.hero-card :deep(.thumbs) {
  gap: 0.65rem;
  padding: 0 0.45rem;
}
.hero-card :deep(.thumb) {
  width: 92px;
  height: 60px;
  border-radius: 8px;
}

/* ===== Main Content ===== */
.main-content {
  min-width: 0;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  box-shadow: var(--card-shadow);
  padding: 1.5rem 1.75rem;
}

/* ===== Section Blocks & Dividers ===== */
.section-block {
  padding: 0;
}
.section-divider {
  border: none;
  border-top: 1.5px dashed var(--border);
  margin: 1.75rem 0;
}

/* ===== Section Titles ===== */
.section-title {
  margin: 0 0 0.75rem;
  font-size: 0.8rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  color: var(--muted);
}

/* ===== Description ===== */
.desc {
  font-size: 1.04rem;
  line-height: 1.8;
  margin: 0;
  color: var(--ink);
}
.legal {
  background: color-mix(in srgb, var(--surface) 70%, white 30%);
  padding: 1rem 1.25rem;
  border-left: 4px solid var(--border);
  font-size: 0.875rem;
  margin: 1.5rem 0 0;
  color: var(--muted);
}

/* ===== Title & Meta ===== */
.title {
  margin: 0 0 0.75rem;
  font-family: var(--font-serif);
  font-size: clamp(1.4rem, 2vw, 2rem);
  font-weight: 700;
  line-height: 1.25;
  color: var(--ink);
}
.meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 0.5rem 0.75rem;
  margin-bottom: 0.75rem;
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
  margin-bottom: 0.75rem;
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
.meta-info {
  display: flex;
  flex-wrap: wrap;
  gap: 1rem 2rem;
  margin-top: 0.5rem;
}
.meta-item {
  display: flex;
  flex-direction: column;
  gap: 0.1rem;
}
.meta-item dt {
  font-size: 0.72rem;
  color: var(--muted);
  text-transform: uppercase;
  letter-spacing: 0.06em;
}
.meta-item dd {
  margin: 0;
  font-weight: 500;
  font-size: 0.9375rem;
  color: var(--ink);
}

/* ===== Media Sections ===== */
.media-section {
  margin-bottom: 1.25rem;
}
.media-section:last-child {
  margin-bottom: 0;
}
.media-heading {
  margin: 0 0 0.75rem;
  font-size: 0.875rem;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 0.4rem;
  color: var(--ink);
}

/* Audio */
.audio-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 0.85rem;
}
.audio-item {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 0.35rem;
}
.audio-name {
  font-size: 0.8125rem;
  color: var(--muted);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  padding-left: 2px;
}

/* Files */
.file-list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 0.55rem;
}
.file-item {
  display: flex;
  align-items: center;
  gap: 0.65rem;
}
.file-type-badge {
  flex: 0 0 auto;
  font-size: 0.7rem;
  font-weight: 700;
  padding: 0.2rem 0.5rem;
  border-radius: 5px;
  letter-spacing: 0.02em;
}
.file-name {
  font-size: 0.875rem;
  font-weight: 500;
  color: var(--accent);
  text-decoration: none;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.file-name:hover { text-decoration: underline; }

/* ===== Comments ===== */
.main-content :deep(.comments) {
  padding-top: 0;
  padding-bottom: 0;
}
.main-content :deep(.comments .comment-thread .comment) {
  padding: 1.1rem 0;
}
.main-content :deep(.comments .comment-thread .comment:first-child) {
  border-top: none;
}

/* ===== Responsive ===== */
@media (max-width: 1024px) {
  .detail-grid {
    grid-template-columns: 1fr;
  }
  .sidebar {
    order: -1;
  }
  .hero-card :deep(.stage) {
    min-height: 0;
    aspect-ratio: 16 / 9;
  }
}

@media (max-width: 640px) {
  .detail-container {
    padding: 1rem 0.75rem 3rem;
  }
  .main-content {
    padding: 1.15rem 1.1rem;
    margin-top: 1rem;
  }
  .sidebar {
    padding: 1.15rem 1.1rem;
  }
}
</style>
