<script setup>
import { computed, ref } from 'vue'
import { postComment } from '../api/mockData.js'

const props = defineProps({
  resourceId: Number,
  initialComments: Array,
  isLoggedIn: Boolean,
  currentUserId: Number,
  currentUserName: String
})

const comments = ref([...(props.initialComments || [])])
const draft = ref('')
const submitting = ref(false)

const sorted = computed(() =>
  [...comments.value].sort(
    (a, b) => new Date(b.created_at).getTime() - new Date(a.created_at).getTime(),
  ),
)

function formatTime(iso) {
  try {
    return new Intl.DateTimeFormat('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    }).format(new Date(iso))
  } catch {
    return iso
  }
}

async function submit() {
  if (!props.isLoggedIn) return
  const text = draft.value.trim()
  if (!text) return
  submitting.value = true
  try {
    const next = await postComment(props.resourceId, props.currentUserId, text, props.currentUserName)
    comments.value = [next, ...comments.value]
    draft.value = ''
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <section class="comments" aria-labelledby="comments-heading">
    <h2 id="comments-heading" class="heading">Comments &amp; feedback</h2>
    <p class="hint">
      {{
        props.isLoggedIn
          ? 'Share your thoughts on this resource.'
          : 'Please sign in to post comments.'
      }}
    </p>

    <form v-if="props.isLoggedIn" class="composer" @submit.prevent="submit">
      <label class="sr-only" for="comment-body">Comment</label>
      <textarea
        id="comment-body"
        v-model="draft"
        rows="3"
        class="input"
        maxlength="2000"
        placeholder="Add context, questions, or related information"
      />
      <div class="row">
        <button type="submit" class="btn" :disabled="submitting || !draft.trim()">
          {{ submitting ? 'Posting…' : 'Post comment' }}
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
    <p v-else class="empty">No comments yet.</p>
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