<template>
  <div id="app-container">
    <NavBar />
    <main class="main-content">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import NavBar from './components/NavBar.vue'
</script>

<style>
* { margin: 0; padding: 0; box-sizing: border-box; }
.main-content { min-height: calc(100vh - 64px); padding: 24px; }
@media (max-width: 768px) {
  .main-content { padding: 16px; }
}
</style>