<script setup lang="ts">
import { computed, ref } from 'vue'
import type { Comment } from '@/types/resource'

const props = defineProps<{
  resourceId: number
  initialComments: Comment[]
  currentUserName?: string
}>()

const comments = ref<Comment[]>([...props.initialComments])
const draft = ref('')
const submitting = ref(false)

const sorted = computed(() =>
  [...comments.value].sort(
    (a, b) => new Date(b.created_at).getTime() - new Date(a.created_at).getTime(),
  ),
)

function formatTime(iso: string) {
  try {
    return new Intl.DateTimeFormat('zh-CN', {
      dateStyle: 'medium',
      timeStyle: 'short',
    }).format(new Date(iso))
  } catch {
    return iso
  }
}

async function submit() {
  const text = draft.value.trim()
  if (!text) return
  submitting.value = true
  try {
    await new Promise((r) => setTimeout(r, 280))
    const name = props.currentUserName ?? '访客'
    const now = new Date().toISOString()
    const next: Comment = {
      id: Date.now(),
      resource_id: props.resourceId,
      user_id: 0,
      authorName: name,
      content: text,
      created_at: now,
      updated_at: now,
    }
    comments.value = [next, ...comments.value]
    draft.value = ''
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <section class="comments" aria-labelledby="comments-heading">
    <h2 id="comments-heading" class="heading">评论与反馈</h2>
    <p class="hint">欢迎对已发布资源留言；正式上线后需登录再评论。</p>

    <form class="composer" @submit.prevent="submit">
      <label class="sr-only" for="comment-body">评论内容</label>
      <textarea
        id="comment-body"
        v-model="draft"
        rows="3"
        class="input"
        maxlength="2000"
        placeholder="分享您的想法或补充信息"
      />
      <div class="row">
        <button type="submit" class="btn" :disabled="submitting || !draft.trim()">
          {{ submitting ? '发送中' : '发表评论' }}
        </button>
      </div>
    </form>

    <ul v-if="sorted.length" class="list">
      <li v-for="c in sorted" :key="c.id" class="item">
        <div class="meta">
          <span class="author">{{ c.authorName }}</span>
          <time class="time" :datetime="c.created_at">{{ formatTime(c.created_at) }}</time>
        </div>
        <p class="content">{{ c.content }}</p>
      </li>
    </ul>
    <p v-else class="empty">还没有评论。</p>
  </section>
</template>

<style scoped>
.comments {
  margin-top: 2.5rem;
  padding-top: 2rem;
  border-top: 1px solid var(--border);
}
.heading {
  margin: 0 0 0.35rem;
  font-family: var(--font-serif);
  font-size: 1.25rem;
  font-weight: 600;
}
.hint {
  margin: 0 0 1rem;
  font-size: 0.875rem;
  color: var(--muted);
}
.composer {
  margin-bottom: 1.5rem;
}
.input {
  width: 100%;
  resize: vertical;
  min-height: 5rem;
  padding: 0.75rem 0.85rem;
  border-radius: 8px;
  border: 1px solid var(--border);
  font-family: inherit;
  font-size: 0.9375rem;
  background: var(--surface);
}
.input:focus {
  outline: 2px solid color-mix(in srgb, var(--accent-soft) 45%, transparent);
  outline-offset: 1px;
  border-color: var(--accent-soft);
}
.row {
  margin-top: 0.5rem;
  display: flex;
  justify-content: flex-end;
}
.btn {
  padding: 0.5rem 1rem;
  border-radius: 8px;
  border: none;
  background: var(--accent);
  color: #fff;
  font-weight: 600;
  font-size: 0.875rem;
  cursor: pointer;
}
.btn:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}
.list {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
.item {
  padding: 1rem 1.1rem;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: 10px;
}
.meta {
  display: flex;
  align-items: baseline;
  gap: 0.6rem;
  margin-bottom: 0.35rem;
}
.author {
  font-weight: 600;
  font-size: 0.9rem;
}
.time {
  font-size: 0.75rem;
  color: var(--muted);
}
.content {
  margin: 0;
  font-size: 0.9375rem;
  white-space: pre-wrap;
  word-break: break-word;
}
.empty {
  margin: 0;
  font-size: 0.9375rem;
  color: var(--muted);
}
.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}
</style>