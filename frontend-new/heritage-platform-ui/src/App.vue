<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'
</script>

<template>
  <div class="layout">
    <header class="site-header">
      <div class="inner">
        <RouterLink to="/" class="brand">
          <span class="brand-mark" aria-hidden="true" />
          <span class="brand-text">Community Heritage</span>
        </RouterLink>
        <nav class="nav" aria-label="Main navigation">
          <RouterLink to="/">Discover</RouterLink>
        </nav>
      </div>
    </header>
    <main class="main">
      <RouterView />
    </main>
    <footer class="site-footer">
      <div class="inner muted">Community heritage resource sharing platform</div>
    </footer>
  </div>
</template>

<style scoped>
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
.site-header {
  background: var(--surface);
  border-bottom: 1px solid var(--border);
  position: sticky;
  top: 0;
  z-index: 20;
}
.inner {
  max-width: 1120px;
  margin: 0 auto;
  padding: 0 1.25rem;
}
.site-header .inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 3.5rem;
}
.brand {
  display: inline-flex;
  align-items: center;
  gap: 0.6rem;
  font-family: var(--font-serif);
  font-weight: 600;
  font-size: 1.05rem;
  color: var(--ink);
  text-decoration: none;
}
.brand:hover {
  text-decoration: none;
  color: var(--accent);
}
.brand-mark {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--accent-soft), var(--accent));
}
.nav {
  display: flex;
  gap: 1.25rem;
  font-size: 0.9375rem;
}
.nav a {
  color: var(--muted);
  font-weight: 500;
}
.nav a.router-link-active {
  color: var(--accent);
}
.main {
  flex: 1;
  padding: 1.75rem 0 3rem;
}
.site-footer {
  border-top: 1px solid var(--border);
  background: var(--surface);
  padding: 1rem 0;
  font-size: 0.8125rem;
}
.muted {
  color: var(--muted);
}
</style>