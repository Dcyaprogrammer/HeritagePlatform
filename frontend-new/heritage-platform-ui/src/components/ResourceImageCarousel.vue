<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type { Attachment } from '@/types/resource'

const props = defineProps<{ attachments: Attachment[] }>()

const imageAttachments = computed(() =>
  props.attachments.filter((a) => a.file_type.startsWith('image/')),
)

const index = ref(0)

watch(
  () => props.attachments,
  () => {
    index.value = 0
  },
  { deep: true },
)

const current = computed(() => imageAttachments.value[index.value] ?? null)
const hasMany = computed(() => imageAttachments.value.length > 1)

function prev() {
  const n = imageAttachments.value.length
  if (n === 0) return
  index.value = (index.value - 1 + n) % n
}

function next() {
  const n = imageAttachments.value.length
  if (n === 0) return
  index.value = (index.value + 1) % n
}

function go(i: number) {
  index.value = i
}
</script>

<template>
  <div class="carousel" role="region" aria-roledescription="carousel" aria-label="资源配图">
    <template v-if="current">
      <div class="stage">
        <img :src="current.file_path" :alt="`第 ${index + 1} 张`" class="main-img" />
        <template v-if="hasMany">
          <button type="button" class="nav prev" aria-label="上一张" @click="prev">上一张</button>
          <button type="button" class="nav next" aria-label="下一张" @click="next">下一张</button>
        </template>
      </div>
      <div v-if="hasMany" class="dots" role="tablist" aria-label="选择图片">
        <button
          v-for="(img, i) in imageAttachments"
          :key="img.id"
          type="button"
          role="tab"
          :aria-selected="i === index"
          class="dot"
          :class="{ active: i === index }"
          :aria-label="`第 ${i + 1} 张`"
          @click="go(i)"
        />
      </div>
      <ul class="thumbs" aria-label="缩略图">
        <li v-for="(img, i) in imageAttachments" :key="img.id">
          <button type="button" class="thumb" :class="{ active: i === index }" @click="go(i)">
            <img :src="img.file_path" alt="" loading="lazy" />
          </button>
        </li>
      </ul>
    </template>
    <div v-else class="empty">暂无图片附件</div>
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
.nav {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  padding: 0.4rem 0.65rem;
  border: none;
  border-radius: 8px;
  background: rgba(255, 252, 247, 0.92);
  color: var(--ink);
  font-size: 0.8rem;
  font-weight: 600;
  cursor: pointer;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}
.nav:hover {
  background: #fff;
}
.prev {
  left: 0.75rem;
}
.next {
  right: 0.75rem;
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
}
.dot.active {
  background: var(--accent);
}
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
}
.thumb.active {
  border-color: var(--accent);
}
.thumb img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
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