<template>
  <div class="auth-page">
    <div class="auth-backdrop" aria-hidden="true" />
    <div class="auth-inner public-shell">
      <div class="auth-grid">
        <aside class="auth-aside">
          <p class="auth-eyebrow">{{ eyebrow }}</p>
          <h1 class="auth-aside-title">{{ asideTitle }}</h1>
          <p class="auth-aside-lead">{{ asideLead }}</p>
          <ul class="auth-points">
            <li v-for="(item, i) in points" :key="i">{{ item }}</li>
          </ul>
          <blockquote v-if="quote" class="auth-quote">
            <p>{{ quote }}</p>
          </blockquote>
          <div class="auth-ornament" aria-hidden="true" />
        </aside>
        <div class="auth-form-column">
          <slot />
        </div>
      </div>
      <footer class="auth-foot">
        <RouterLink class="auth-foot-link" to="/">Browse published resources</RouterLink>
        <span class="auth-foot-dot" aria-hidden="true">·</span>
        <span class="auth-foot-note">Heritage Platform</span>
      </footer>
    </div>
  </div>
</template>

<script setup>
import { RouterLink } from 'vue-router'

defineProps({
  eyebrow: { type: String, required: true },
  asideTitle: { type: String, required: true },
  asideLead: { type: String, required: true },
  points: { type: Array, default: () => [] },
  quote: { type: String, default: '' },
})
</script>

<style scoped>
.auth-page {
  position: relative;
  /* Stretch within PublicLayout main so vertical margins can center content */
  flex: 1;
  display: flex;
  flex-direction: column;
  padding: 1rem 0;
  overflow-x: hidden;
  min-height: 0;
}
.auth-backdrop {
  position: absolute;
  inset: 0;
  z-index: 0;
  background:
    radial-gradient(ellipse 120% 80% at 0% 0%, color-mix(in srgb, var(--accent) 14%, transparent), transparent 55%),
    radial-gradient(ellipse 100% 70% at 100% 20%, color-mix(in srgb, var(--accent-soft) 10%, transparent), transparent 50%),
    radial-gradient(circle at 80% 85%, color-mix(in srgb, var(--accent) 6%, transparent), transparent 45%),
    var(--bg);
  pointer-events: none;
}
.auth-backdrop::after {
  content: '';
  position: absolute;
  inset: 0;
  opacity: 0.35;
  background-image: repeating-linear-gradient(
    -12deg,
    transparent,
    transparent 11px,
    color-mix(in srgb, var(--border) 45%, transparent) 11px,
    color-mix(in srgb, var(--border) 45%, transparent) 12px
  );
  mask-image: linear-gradient(to bottom, black 0%, transparent 55%, transparent 100%);
}
.auth-inner {
  position: relative;
  z-index: 1;
  width: 100%;
  margin-top: auto;
  margin-bottom: auto;
}
.auth-grid {
  display: grid;
  gap: 2.5rem 3rem;
  align-items: start;
}
@media (min-width: 900px) {
  .auth-grid {
    grid-template-columns: 1fr minmax(320px, 420px);
    gap: 3rem 4rem;
    align-items: center;
  }
}
.auth-aside {
  padding: 0.5rem 0 1rem;
}
@media (min-width: 900px) {
  .auth-aside {
    padding: 2rem 0;
    max-width: 36rem;
  }
}
.auth-eyebrow {
  margin: 0 0 0.75rem;
  font-size: 0.75rem;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--accent-soft);
}
.auth-aside-title {
  margin: 0 0 1rem;
  font-family: var(--font-serif);
  font-size: clamp(1.65rem, 2.8vw, 2.35rem);
  font-weight: 700;
  line-height: 1.2;
  color: var(--ink);
}
.auth-aside-lead {
  margin: 0 0 1.25rem;
  font-size: 1.02rem;
  line-height: 1.6;
  color: var(--muted);
  max-width: 42ch;
}
.auth-points {
  margin: 0 0 1.5rem;
  padding: 0 0 0 1.15rem;
  color: var(--ink);
  font-size: 0.9375rem;
  line-height: 1.55;
}
.auth-points li {
  margin-bottom: 0.5rem;
}
.auth-points li::marker {
  color: var(--accent);
}
.auth-quote {
  margin: 0;
  padding: 1rem 0 0 1rem;
  border-left: 3px solid color-mix(in srgb, var(--accent) 55%, var(--border));
  font-family: var(--font-serif);
  font-size: 1.05rem;
  font-style: italic;
  line-height: 1.5;
  color: var(--muted);
}
.auth-quote p {
  margin: 0;
}
.auth-ornament {
  margin-top: 2rem;
  height: 3px;
  max-width: 8rem;
  border-radius: 2px;
  background: linear-gradient(90deg, var(--accent), color-mix(in srgb, var(--accent) 25%, var(--border)));
  opacity: 0.85;
}
.auth-form-column {
  width: 100%;
  justify-self: stretch;
}
.auth-foot {
  margin-top: 2.25rem;
  padding-top: 1.25rem;
  border-top: 1px solid var(--border);
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: center;
  gap: 0.5rem 0.75rem;
  font-size: 0.8125rem;
  color: var(--muted);
}
.auth-foot-link {
  color: var(--accent);
  text-decoration: none;
  font-weight: 500;
}
.auth-foot-link:hover {
  text-decoration: underline;
}
.auth-foot-dot {
  opacity: 0.5;
}
.auth-foot-note {
  opacity: 0.85;
}
</style>
