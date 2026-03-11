<script setup lang="ts">
import { RouterLink } from 'vue-router'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import FeatureBento from '@/components/FeatureBento.vue'
import HeroSection from '@/components/HeroSection.vue'
</script>

<template>
  <DefaultLayout>
    <HeroSection />
    <FeatureBento />
    <section class="py-16 sm:py-20">
      <div class="container mx-auto px-4 sm:px-6 lg:px-8">
        <h2 class="text-3xl font-bold tracking-tight text-slate-900 sm:text-4xl">Portal · 门户与系统分离</h2>
        <p class="mt-4 max-w-3xl text-slate-600 leading-7">
          当前首页定位为门户页：用于展示产品价值、能力介绍和入口导航。真正的业务使用（文章浏览、管理）统一进入系统区。
        </p>
        <div class="mt-8 flex flex-wrap gap-3">
          <RouterLink
            to="/app/articles"
            class="inline-flex items-center rounded-lg bg-blue-600 px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-blue-700"
          >
            进入系统文章中心
          </RouterLink>
          <RouterLink
            to="/app/studio/articles"
            class="inline-flex items-center rounded-lg border border-slate-300 px-5 py-2.5 text-sm font-semibold text-slate-700 transition hover:bg-slate-50"
          >
            登录写作后台
          </RouterLink>
        </div>
      </div>
    </section>
    <section id="about" class="border-t border-slate-200 py-16 sm:py-20">
      <div class="container mx-auto px-4 sm:px-6 lg:px-8">
        <h2 class="text-3xl font-bold tracking-tight text-slate-900 sm:text-4xl">About NexBlog</h2>
        <p class="mt-4 max-w-3xl text-slate-600 leading-7">
          NexBlog 旨在构建一个面向开发者的 AI 原生博客系统，前后端解耦，支持动态站点与静态站点双模式部署。
        </p>
      </div>
    </section>
  </DefaultLayout>
</template>
