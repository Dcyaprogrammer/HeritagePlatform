<script setup>
import { computed, ref, watch } from 'vue'
import { addComment } from '../api/resource.js'
import InteractionButtons from './InteractionButtons.vue'

const props = defineProps({
  resourceId: Number,
  initialComments: Array,
  isLoggedIn: Boolean,
  currentUserId: Number,
  currentUserName: String,
  initialLikeCount: { type: Number, default: 0 },
  initialFavoriteCount: { type: Number, default: 0 },
})

const comments = ref([...(props.initialComments || [])])

watch(() => props.initialComments, (newVal) => {
  comments.value = [...(newVal || [])]
}, { deep: true })

const draft = ref('')
const submitting = ref(false)
const replyingTo = ref(null)
const replyDraft = ref('')
const replySubmitting = ref(false)

const sorted = computed(() =>
  [...comments.value].sort(
    (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime(),
  ),
)

function formatTime(iso) {
  try {
    return new Intl.DateTimeFormat('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
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
    const res = await addComment(props.resourceId, text)
    if (res.data.code === 200) {
      comments.value = [res.data.data, ...comments.value]
      draft.value = ''
    }
  } catch (error) {
    console.error('Failed to post comment:', error)
  } finally {
    submitting.value = false
  }
}

function startReply(commentId) {
  replyingTo.value = replyingTo.value === commentId ? null : commentId
  replyDraft.value = ''
}

function cancelReply() {
  replyingTo.value = null
  replyDraft.value = ''
}

async function submitReply(parentCommentId) {
  if (!props.isLoggedIn) return
  const text = replyDraft.value.trim()
  if (!text) return
  replySubmitting.value = true
  try {
    const res = await addComment(props.resourceId, text, parentCommentId)
    if (res.data.code === 200) {
      const newComment = res.data.data
      comments.value = comments.value.map(c => {
        if (c.id === parentCommentId) {
          return { ...c, replies: [...(c.replies || []), newComment] }
        }
        return c
      })
      replyDraft.value = ''
      replyingTo.value = null
    }
  } catch (error) {
    console.error('Failed to post reply:', error)
  } finally {
    replySubmitting.value = false
  }
}
</script>

<template>
  <section class="comments" aria-labelledby="comments-heading">
    <h2 id="comments-heading" class="heading">Comments &amp; feedback</h2>

    <InteractionButtons
      :resource-id="props.resourceId"
      :initial-like-count="props.initialLikeCount"
      :initial-favorite-count="props.initialFavoriteCount"
      :is-logged-in="props.isLoggedIn"
    />

    <div class="composer-divider" />

    <form v-if="props.isLoggedIn" class="composer" @submit.prevent="submit">
      <label class="sr-only" for="comment-body">Comment</label>
      <textarea
        id="comment-body"
        v-model="draft"
        rows="3"
        class="public-textarea input"
        maxlength="2000"
        placeholder="Share your thoughts on this resource..."
      />
      <div class="composer-footer">
        <button type="submit" class="public-btn public-btn--compact" :disabled="submitting || !draft.trim()">
          {{ submitting ? 'Posting…' : 'Post comment' }}
        </button>
      </div>
    </form>
    <p v-else class="login-hint">
      Please <a href="/login">sign in</a> to leave a comment.
    </p>

    <div v-if="sorted.length" class="comment-thread">
      <article v-for="c in sorted" :key="c.id" class="comment">
        <header class="comment-header">
          <span class="author">{{ c.authorName }}</span>
          <time class="time" :datetime="c.createdAt">{{ formatTime(c.createdAt) }}</time>
        </header>
        <p class="comment-content">{{ c.content }}</p>
        <div class="comment-actions">
          <button
            v-if="props.isLoggedIn"
            class="action-btn"
            @click="startReply(c.id)"
          >
            {{ replyingTo === c.id ? 'Cancel' : 'Reply' }}
          </button>
        </div>

        <!-- Reply form -->
        <div v-if="replyingTo === c.id" class="reply-form">
          <textarea
            v-model="replyDraft"
            rows="2"
            class="public-textarea input"
            maxlength="2000"
            :placeholder="`Reply to ${c.authorName}...`"
          />
          <div class="reply-actions">
            <button
              type="button"
              class="public-btn public-btn--ghost public-btn--compact"
              @click="cancelReply"
            >
              Cancel
            </button>
            <button
              type="button"
              class="public-btn public-btn--compact"
              @click="submitReply(c.id)"
              :disabled="replySubmitting || !replyDraft.trim()"
            >
              {{ replySubmitting ? 'Posting…' : 'Post reply' }}
            </button>
          </div>
        </div>

        <!-- Replies -->
        <div v-if="c.replies && c.replies.length" class="replies">
          <article v-for="reply in c.replies" :key="reply.id" class="reply">
            <header class="comment-header">
              <span class="author">{{ reply.authorName }}</span>
              <time class="time" :datetime="reply.createdAt">{{ formatTime(reply.createdAt) }}</time>
            </header>
            <p class="comment-content">{{ reply.content }}</p>
          </article>
        </div>
      </article>
    </div>
    <p v-else class="empty">No comments yet. Be the first to share your thoughts.</p>
  </section>
</template>

<style scoped>
.comments {
  padding-top: 0;
  padding-bottom: 1rem;
}

.heading {
  margin: 0 0 1.5rem;
  font-family: var(--font-serif);
  font-size: 1.25rem;
  font-weight: 600;
  color: var(--ink);
}

.login-hint {
  margin: 0 0 1.5rem;
  font-size: 0.875rem;
  color: var(--muted);
}

.login-hint a {
  color: var(--accent);
  text-decoration: none;
}

.login-hint a:hover {
  text-decoration: underline;
}

/* ===== Composer ===== */
.composer-divider {
  border: none;
  border-top: 1.5px dashed var(--border);
  margin: 1.25rem 0;
}
.composer {
  margin-bottom: 2rem;
}

.input {
  min-height: 5rem;
  line-height: 1.6;
}

.composer-footer {
  margin-top: 0.75rem;
  display: flex;
  justify-content: flex-end;
}

/* ===== Comment Thread ===== */
.comment-thread {
  display: flex;
  flex-direction: column;
}

.comment {
  padding: 1.25rem 0;
  border-bottom: 1px dashed var(--border);
}

.comment:first-child {
  border-top: 1px dashed var(--border);
}

.comment-header {
  display: flex;
  align-items: baseline;
  gap: 0.75rem;
  margin-bottom: 0.5rem;
}

.author {
  font-weight: 600;
  font-size: 0.9rem;
  color: var(--ink);
}

.time {
  font-size: 0.75rem;
  color: var(--muted);
}

.comment-content {
  margin: 0;
  font-size: 0.9375rem;
  line-height: 1.7;
  color: var(--ink);
  white-space: pre-wrap;
  word-break: break-word;
}

.comment-actions {
  margin-top: 0.5rem;
}

.action-btn {
  padding: 0.2rem 0.5rem;
  font-size: 0.78rem;
  color: var(--muted);
  background: transparent;
  border: none;
  cursor: pointer;
  transition: color 0.15s ease;
}

.action-btn:hover {
  color: var(--accent);
}

/* ===== Reply Form ===== */
.reply-form {
  margin-top: 1rem;
  padding: 1rem;
  background: color-mix(in srgb, var(--surface) 84%, white 16%);
  border-radius: 14px;
  border: 1px solid var(--border);
}

.reply-form .input {
  min-height: 3.5rem;
}

.reply-actions {
  margin-top: 0.6rem;
  display: flex;
  justify-content: flex-end;
  gap: 0.5rem;
}

/* ===== Replies ===== */
.replies {
  margin-top: 1rem;
  padding-left: 1.25rem;
  border-left: 2px solid var(--border);
  display: flex;
  flex-direction: column;
}

.reply {
  padding: 1rem 0;
  border-bottom: 1px solid color-mix(in srgb, var(--border) 50%, transparent);
}

.reply:last-child {
  border-bottom: none;
  padding-bottom: 0;
}

/* ===== Empty State ===== */
.empty {
  margin: 0;
  padding: 2rem 0;
  font-size: 0.9375rem;
  color: var(--muted);
  text-align: center;
}

/* ===== Accessibility ===== */
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

/* ===== Responsive ===== */
@media (max-width: 480px) {
  .replies {
    padding-left: 1rem;
  }

  .comment-header {
    flex-direction: column;
    gap: 0.25rem;
  }
}
</style>
