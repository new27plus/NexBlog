<script setup lang="ts">
import Navbar from '@/components/Navbar.vue'
</script>

<template>
  <div class="flex min-h-screen flex-col bg-white text-slate-900 dark:bg-slate-900 dark:text-slate-100">
    <Navbar />
    <main class="grow pt-16">
      <slot />
    </main>
    <footer class="border-t border-slate-200 py-8 text-center text-sm text-slate-500 dark:border-slate-800">
      © 2026 NexBlog. Built for Developers.
    </footer>
  </div>
</template>
