<script setup lang="ts">
import { RouterLink } from 'vue-router'
import { ArrowRight, Sparkles } from 'lucide-vue-next'
</script>

<template>
  <section class="relative overflow-hidden">
    <div class="absolute inset-0 -z-10 bg-[radial-gradient(circle_at_top,rgba(99,102,241,0.15),transparent_55%)]"></div>
    <div class="container mx-auto px-4 sm:px-6 lg:px-8 py-20 sm:py-24">
      <div class="mx-auto max-w-4xl text-center">
        <div class="inline-flex items-center gap-2 rounded-full border border-indigo-200 bg-indigo-50 px-3 py-1 text-xs font-medium text-indigo-700">
          <Sparkles class="h-4 w-4" />
          <span>AI Native Developer Blog Platform</span>
        </div>
        <h1 class="mt-6 text-4xl font-bold tracking-tight text-slate-900 sm:text-6xl">
          Build and Publish with
          <span class="bg-linear-to-r from-blue-600 to-indigo-600 bg-clip-text text-transparent"> NexBlog</span>
        </h1>
        <p class="mx-auto mt-6 max-w-2xl text-base leading-7 text-slate-600 sm:text-lg">
          下一代 AI 原生开源博客系统，支持动态部署与 GitHub Pages 双模发布，为开发者提供完整的写作与发布链路。
        </p>
        <div class="mt-10 flex flex-col items-center justify-center gap-4 sm:flex-row">
          <RouterLink to="/app/articles" class="inline-flex items-center gap-2 rounded-lg bg-blue-600 px-6 py-3 text-sm font-semibold text-white transition hover:bg-blue-700 active:scale-95">
            进入系统
            <ArrowRight class="h-4 w-4" />
          </RouterLink>
          <RouterLink to="/app/studio/articles" class="inline-flex items-center gap-2 rounded-lg border border-slate-300 px-6 py-3 text-sm font-semibold text-slate-700 transition hover:bg-slate-50 active:scale-95">
            登录写作后台
          </RouterLink>
        </div>
      </div>
    </div>
  </section>
</template>
