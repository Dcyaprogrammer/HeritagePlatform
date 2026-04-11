<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import ResourceImageCarousel from '@/components/ResourceImageCarousel.vue'
import CommentSection from '@/components/CommentSection.vue'
import { getComments, getResourceById } from '@/api/mockData'

const route = useRoute()

const id = computed(() => Number(route.params.id))

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
  } catch {
    return iso
  }
}
</script>

<template>
  <div v-if="resource" class="page inner">
    <nav class="crumb" aria-label="面包屑">
      <RouterLink to="/">发现资源</RouterLink>
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
        <ul v-if="resource.tags.length" class="tags" aria-label="标签">
          <li v-for="t in resource.tags" :key="t.id" class="tag">{{ t.name }}</li>
        </ul>
      </header>

      <div class="prose">
        <p class="desc">{{ resource.description }}</p>
        <p v-if="resource.copyright_declaration" class="legal">
          <strong>版权与使用声明</strong><br />
          {{ resource.copyright_declaration }}
        </p>
        <dl class="facts">
          <div v-if="resource.contributorName" class="fact">
            <dt>贡献者</dt>
            <dd>{{ resource.contributorName }}</dd>
          </div>
          <div class="fact">
            <dt>发布日期</dt>
            <dd>{{ formatDate(resource.created_at) }}</dd>
          </div>
          <div class="fact">
            <dt>最近更新</dt>
            <dd>{{ formatDate(resource.updated_at) }}</dd>
          </div>
        </dl>
      </div>
    </article>

    <CommentSection
      :resource-id="resource.id"
      :initial-comments="comments"
      current-user-name="演示用户"
    />
  </div>

  <div v-else class="page inner notfound">
    <h1>未找到资源</h1>
    <p>该条目不存在、未通过审核或已归档（对访客不可见）。</p>
    <RouterLink to="/">返回发现页</RouterLink>
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