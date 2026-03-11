<script setup lang="ts">
import { Bot, Code2, BarChart3, Rocket } from 'lucide-vue-next'
</script>

<template>
  <section id="features" class="py-16 sm:py-20">
    <div class="container mx-auto px-4 sm:px-6 lg:px-8">
      <div class="mb-10">
        <h2 class="text-3xl font-bold tracking-tight text-slate-900 sm:text-4xl">Core Features</h2>
        <p class="mt-3 text-slate-600">基于设计规范的 Bento Grid 首页特性区。</p>
      </div>
      <div class="grid grid-cols-1 gap-4 md:grid-cols-3 md:grid-rows-2">
        <article class="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm md:col-span-2 md:row-span-2">
          <div class="mb-5 inline-flex rounded-lg bg-indigo-50 p-2 text-indigo-600">
            <Bot class="h-5 w-5" />
          </div>
          <h3 class="text-xl font-semibold text-slate-900">AI 写作助手</h3>
          <p class="mt-2 text-sm leading-6 text-slate-600">
            提供摘要、标签建议、语言优化和风格改写，提升写作效率与内容质量。
          </p>
          <div class="mt-6 space-y-3 rounded-xl bg-slate-50 p-4">
            <div class="w-3/4 rounded-lg bg-white px-3 py-2 text-sm text-slate-500 shadow-sm">请帮我生成这篇文章的摘要</div>
            <div class="ml-auto w-4/5 rounded-lg bg-blue-600 px-3 py-2 text-sm text-white shadow-sm">好的，已为你生成 3 种摘要版本。</div>
          </div>
        </article>

        <article class="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm md:col-span-1">
          <div class="mb-4 inline-flex rounded-lg bg-emerald-50 p-2 text-emerald-600">
            <Rocket class="h-5 w-5" />
          </div>
          <h3 class="text-lg font-semibold text-slate-900">一键部署</h3>
          <p class="mt-2 text-sm text-slate-600">支持推送至 GitHub Pages，自动化发布流程。</p>
          <div class="mt-4 h-2 w-full rounded bg-slate-100">
            <div class="h-2 w-3/4 rounded bg-emerald-500"></div>
          </div>
        </article>

        <article class="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
          <div class="mb-4 inline-flex rounded-lg bg-sky-50 p-2 text-sky-600">
            <Code2 class="h-5 w-5" />
          </div>
          <h3 class="text-lg font-semibold text-slate-900">Markdown 支持</h3>
          <p class="mt-2 text-sm text-slate-600">代码高亮、数学公式、目录导航一体化支持。</p>
        </article>

        <article class="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
          <div class="mb-4 inline-flex rounded-lg bg-violet-50 p-2 text-violet-600">
            <BarChart3 class="h-5 w-5" />
          </div>
          <h3 class="text-lg font-semibold text-slate-900">SEO 分析</h3>
          <p class="mt-2 text-sm text-slate-600">自动生成结构化数据和内容优化建议。</p>
        </article>
      </div>
    </div>
  </section>
</template>
