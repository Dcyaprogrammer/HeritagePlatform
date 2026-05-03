<script setup>
import { computed, ref, watch } from 'vue'
import { getAttachmentType, getAttachmentSrc, TYPE_META } from '../utils/attachmentUtils.js'

const props = defineProps({ attachments: Array })

const imageAttachments = computed(() => (props.attachments || []).filter(a => getAttachmentType(a) === 'image'))
const videoAttachments = computed(() => (props.attachments || []).filter(a => getAttachmentType(a) === 'video'))

const index = ref(0)

watch(
  () => props.attachments,
  () => { index.value = 0 },
  { deep: true },
)

const allMedia = computed(() => [
  ...imageAttachments.value.map(a => ({ ...a, _type: 'image' })),
  ...videoAttachments.value.map(a => ({ ...a, _type: 'video' })),
])

const current = computed(() => allMedia.value[index.value] ?? null)
const hasMany = computed(() => allMedia.value.length > 1)
const isVideo = computed(() => current.value?._type === 'video')

function prev() {
  const n = allMedia.value.length
  if (n === 0) return
  index.value = (index.value - 1 + n) % n
}

function next() {
  const n = allMedia.value.length
  if (n === 0) return
  index.value = (index.value + 1) % n
}

function go(i) {
  index.value = i
}

function thumbnailType(media) {
  return media._type
}

// Get thumbnail URL for video attachments
function getVideoThumbnail(media) {
  // Prefer thumbnail_path from server, fallback to attachment preview (first frame)
  return media.thumbnailUrl || media.thumbnail_path ||
    (media.id ? `/api/attachments/${media.id}/thumbnail` : null) ||
    getAttachmentSrc(media)
}
</script>

<template>
  <div class="carousel" role="region" aria-roledescription="carousel" aria-label="Resource media">
    <template v-if="current">
      <div class="stage">
        <!-- Video player -->
        <video
          v-if="isVideo"
          :src="getAttachmentSrc(current)"
          controls
          class="main-video"
          :key="current.id"
        >
          Your browser does not support the video tag.
        </video>
        <!-- Image -->
        <img
          v-else
          :src="getAttachmentSrc(current)"
          :alt="`Image ${index + 1} of ${allMedia.length}`"
          class="main-img"
        />
        <!-- Navigation arrows -->
        <template v-if="hasMany">
          <button type="button" class="nav prev" aria-label="Previous media" @click="prev">‹</button>
          <button type="button" class="nav next" aria-label="Next media" @click="next">›</button>
        </template>
        <!-- Type badge on main media -->
        <span v-if="isVideo" class="type-badge video-badge">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><path d="M8 5v14l11-7z"/></svg>
          Video
        </span>
      </div>

      <!-- Dots -->
      <div v-if="hasMany" class="dots" role="tablist" aria-label="Select media">
        <button
          v-for="(media, i) in allMedia"
          :key="media.id"
          type="button"
          role="tab"
          :aria-selected="i === index"
          class="dot"
          :class="{ active: i === index, [`dot-${thumbnailType(media)}`]: true }"
          :aria-label="`${thumbnailType(media) === 'video' ? 'Video' : 'Image'} ${i + 1}`"
          @click="go(i)"
        />
      </div>

      <!-- Thumbnails -->
      <ul class="thumbs" aria-label="Media thumbnails">
        <li v-for="(media, i) in allMedia" :key="media.id">
          <button type="button" class="thumb" :class="{ active: i === index, [`thumb-${thumbnailType(media)}`]: true }" @click="go(i)">
            <img v-if="thumbnailType(media) === 'image'" :src="getAttachmentSrc(media)" alt="" loading="lazy" />
            <div v-else class="thumb-video-placeholder">
              <img v-if="getVideoThumbnail(media)" :src="getVideoThumbnail(media)" alt="" loading="lazy" class="thumb-video-thumb" />
              <div v-else class="thumb-video-empty" />
              <span class="play-overlay">
                <svg width="22" height="22" viewBox="0 0 24 24" fill="white"><path d="M8 5v14l11-7z"/></svg>
              </span>
            </div>
          </button>
        </li>
      </ul>
    </template>
    <div v-else class="empty">No media attachments</div>
  </div>
</template>

<style scoped>
.carousel {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}
.stage {
  position: relative;
  border-radius: var(--radius);
  overflow: hidden;
  background: #1c1917;
  aspect-ratio: 16 / 9;
}
.main-img {
  width: 100%;
  height: 100%;
  object-fit: contain;
  display: block;
  background: #0c0a09;
}
.main-video {
  width: 100%;
  height: 100%;
  object-fit: contain;
  display: block;
  background: #000;
}
.nav {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 50%;
  background: rgba(255, 252, 247, 0.9);
  color: var(--ink);
  font-size: 1.4rem;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
  transition: background 0.15s;
}
.nav:hover { background: #fff; }
.prev { left: 0.75rem; }
.next { right: 0.75rem; }
.type-badge {
  position: absolute;
  bottom: 0.75rem;
  right: 0.75rem;
  display: inline-flex;
  align-items: center;
  gap: 0.3rem;
  padding: 0.3rem 0.6rem;
  border-radius: 6px;
  font-size: 0.75rem;
  font-weight: 700;
  backdrop-filter: blur(4px);
}
.video-badge {
  background: rgba(220, 38, 38, 0.85);
  color: #fff;
}
.dots {
  display: flex;
  justify-content: center;
  gap: 0.4rem;
}
.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  border: none;
  padding: 0;
  background: #d6d3d1;
  cursor: pointer;
  transition: background 0.15s, transform 0.15s;
}
.dot.active { background: var(--accent); transform: scale(1.3); }
.dot-video { background: #fca5a5; }
.dot-video.active { background: #dc2626; }
.thumbs {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  gap: 0.5rem;
  overflow-x: auto;
  padding-bottom: 0.25rem;
}
.thumb {
  flex: 0 0 auto;
  width: 72px;
  height: 48px;
  border-radius: 6px;
  overflow: hidden;
  border: 2px solid transparent;
  padding: 0;
  cursor: pointer;
  background: #e7e5e4;
  transition: border-color 0.15s;
}
.thumb.active { border-color: var(--accent); }
.thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
.thumb-video-placeholder {
  width: 100%;
  height: 100%;
  position: relative;
  background: #1c1917;
  display: flex;
  align-items: center;
  justify-content: center;
}
.thumb-video-thumb {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.thumb-video-empty {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #374151, #1f2937);
}
.play-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.35);
}
.empty {
  border-radius: var(--radius);
  background: #e7e5e4;
  color: var(--muted);
  padding: 3rem 1rem;
  text-align: center;
  font-size: 0.9375rem;
}
</style>
