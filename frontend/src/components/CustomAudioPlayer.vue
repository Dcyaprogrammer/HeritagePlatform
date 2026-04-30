<script setup>
import { computed, ref } from 'vue'

const props = defineProps({
  src: String,
  name: { type: String, default: 'Audio file' }
})

const audio = ref(null)
const playing = ref(false)
const currentTime = ref(0)
const duration = ref(0)
const volume = ref(1)
const muted = ref(false)
const speed = ref(1)
const playbackRates = [0.5, 0.75, 1, 1.25, 1.5, 2]
const showSpeedMenu = ref(false)

const progress = computed(() =>
  duration.value > 0 ? (currentTime.value / duration.value) * 100 : 0
)

function formatTime(secs) {
  if (!secs || isNaN(secs)) return '0:00'
  const m = Math.floor(secs / 60)
  const s = Math.floor(secs % 60)
  return `${m}:${s.toString().padStart(2, '0')}`
}

function togglePlay() {
  if (!audio.value) return
  if (playing.value) {
    audio.value.pause()
  } else {
    audio.value.play()
  }
}

function onTimeUpdate() {
  currentTime.value = audio.value?.currentTime ?? 0
}
function onLoadedMetadata() {
  duration.value = audio.value?.duration ?? 0
}
function onPlay() { playing.value = true }
function onPause() { playing.value = false }
function onEnded() { playing.value = false; currentTime.value = 0 }

function seek(e) {
  if (!audio.value || !duration.value) return
  const rect = e.currentTarget.getBoundingClientRect()
  const ratio = Math.max(0, Math.min(1, (e.clientX - rect.left) / rect.width))
  audio.value.currentTime = ratio * duration.value
}

function setSpeed(r) {
  speed.value = r
  if (audio.value) audio.value.playbackRate = r
  showSpeedMenu.value = false
}

function toggleMute() {
  muted.value = !muted.value
  if (audio.value) audio.value.muted = muted.value
}

function onVolumeChange(e) {
  volume.value = parseFloat(e.target.value)
  if (audio.value) audio.value.volume = volume.value
}
</script>

<template>
  <div class="custom-audio">
    <audio
      ref="audio"
      :src="src"
      preload="metadata"
      @timeupdate="onTimeUpdate"
      @loadedmetadata="onLoadedMetadata"
      @play="onPlay"
      @pause="onPause"
      @ended="onEnded"
    />

    <!-- Play/Pause -->
    <button type="button" class="btn-play" :aria-label="playing ? 'Pause' : 'Play'" @click="togglePlay">
      <svg v-if="!playing" width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><path d="M8 5v14l11-7z"/></svg>
      <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><path d="M6 19h4V5H6v14zm8-14v14h4V5h-4z"/></svg>
    </button>

    <!-- Progress bar -->
    <div class="progress-wrap" role="slider" :aria-valuenow="Math.round(currentTime)" :aria-valuemax="Math.round(duration)" aria-label="Playback progress" @click="seek">
      <div class="progress-track">
        <div class="progress-fill" :style="{ width: progress + '%' }" />
      </div>
    </div>

    <!-- Time -->
    <span class="time">{{ formatTime(currentTime) }} / {{ formatTime(duration) }}</span>

    <!-- Speed -->
    <div class="speed-wrap">
      <button type="button" class="btn-speed" :aria-label="`Playback speed: ${speed}x`" @click="showSpeedMenu = !showSpeedMenu">
        {{ speed }}x
      </button>
      <div v-if="showSpeedMenu" class="speed-menu">
        <button
          v-for="r in playbackRates"
          :key="r"
          type="button"
          class="speed-option"
          :class="{ active: speed === r }"
          @click="setSpeed(r)"
        >
          {{ r }}x
        </button>
      </div>
    </div>

    <!-- Volume -->
    <button type="button" class="btn-vol" :aria-label="muted ? 'Unmute' : 'Mute'" @click="toggleMute">
      <svg v-if="muted || volume === 0" width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><path d="M16.5 12c0-1.77-1.02-3.29-2.5-4.03v2.21l2.45 2.45c.03-.2.05-.41.05-.63zm2.5 0c0 .94-.2 1.82-.54 2.64l1.51 1.51C20.63 14.91 21 13.5 21 12c0-4.28-2.99-7.86-7-8.77v2.06c2.89.86 5 3.54 5 6.71zM4.27 3L3 4.27 7.73 9H3v6h4l5 5v-6.73l4.25 4.25c-.67.52-1.42.93-2.25 1.18v2.06c1.38-.31 2.63-.95 3.69-1.81L19.73 21 21 19.73l-9-9L4.27 3zM12 4L9.91 6.09 12 8.18V4z"/></svg>
      <svg v-else width="16" height="16" viewBox="0 0 24 24" fill="currentColor"><path d="M3 9v6h4l5 5V4L7 9H3zm13.5 3c0-1.77-1.02-3.29-2.5-4.03v8.05c1.48-.73 2.5-2.25 2.5-4.02zM14 3.23v2.06c2.89.86 5 3.54 5 6.71s-2.11 5.85-5 6.71v2.06c4.01-.91 7-4.49 7-8.77s-2.99-7.86-7-8.77z"/></svg>
    </button>
    <input type="range" class="vol-slider" min="0" max="1" step="0.05" :value="volume" aria-label="Volume" @input="onVolumeChange" />
  </div>
</template>

<style scoped>
.custom-audio {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: #f4f4f5;
  border: 1px solid #e4e4e7;
  border-radius: 999px;
  padding: 0.35rem 0.6rem 0.35rem 0.5rem;
  width: 100%;
}

.btn-play {
  flex: 0 0 auto;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  border: none;
  background: var(--accent);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: background 0.15s;
}
.btn-play:hover { background: #1d4ed8; }

.progress-wrap {
  flex: 1;
  min-width: 0;
  padding: 8px 0;
  cursor: pointer;
}
.progress-track {
  height: 4px;
  background: #d4d4d8;
  border-radius: 999px;
  overflow: hidden;
}
.progress-fill {
  height: 100%;
  background: var(--accent);
  border-radius: 999px;
  transition: width 0.1s linear;
}

.time {
  flex: 0 0 auto;
  font-size: 0.75rem;
  color: #71717a;
  white-space: nowrap;
  font-variant-numeric: tabular-nums;
}

.speed-wrap {
  position: relative;
  flex: 0 0 auto;
}
.btn-speed {
  border: none;
  background: none;
  font-size: 0.75rem;
  font-weight: 600;
  color: var(--accent);
  cursor: pointer;
  padding: 2px 4px;
  border-radius: 4px;
}
.btn-speed:hover { background: #eff6ff; }

.speed-menu {
  position: absolute;
  bottom: calc(100% + 6px);
  right: 0;
  background: #fff;
  border: 1px solid #e4e4e7;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  z-index: 100;
  min-width: 60px;
}
.speed-option {
  display: block;
  width: 100%;
  border: none;
  background: none;
  padding: 6px 12px;
  font-size: 0.8125rem;
  text-align: left;
  cursor: pointer;
  color: #3f3f46;
}
.speed-option:hover { background: #f4f4f5; }
.speed-option.active { color: var(--accent); font-weight: 700; }

.btn-vol {
  flex: 0 0 auto;
  border: none;
  background: none;
  color: #71717a;
  cursor: pointer;
  display: flex;
  align-items: center;
  padding: 2px;
}
.btn-vol:hover { color: var(--accent); }

.vol-slider {
  flex: 0 0 auto;
  width: 60px;
  accent-color: var(--accent);
}
</style>
