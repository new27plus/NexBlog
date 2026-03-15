<script setup lang="ts">
import DefaultLayout from '@/layouts/DefaultLayout.vue'
</script>

<template>
  <DefaultLayout>
    <section class="py-16 md:py-20">
      <div class="mx-auto w-full max-w-4xl px-4 sm:px-6 lg:px-8">
        <header class="mb-8 md:mb-10">
          <h1 class="text-3xl font-bold tracking-tight text-slate-900 dark:text-white md:text-4xl">
            关于作者
          </h1>
          <p class="mt-3 text-base text-slate-600 dark:text-slate-400">
            关于页基础骨架已完成，你可以在这里补充个人介绍、技术栈和项目经历。
          </p>
        </header>

        <section class="space-y-4 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm dark:border-slate-700 dark:bg-slate-800">
          <article class="rounded-xl bg-slate-50 p-4 dark:bg-slate-900">
            <h2 class="text-lg font-semibold text-slate-900 dark:text-white">个人介绍</h2>
            <p class="mt-2 text-sm text-slate-600 dark:text-slate-400">可放你的背景、方向和目标岗位。</p>
          </article>

          <article class="rounded-xl bg-slate-50 p-4 dark:bg-slate-900">
            <h2 class="text-lg font-semibold text-slate-900 dark:text-white">技术栈</h2>
            <p class="mt-2 text-sm text-slate-600 dark:text-slate-400">可放你熟悉的前后端框架与工程化能力。</p>
          </article>

          <article class="rounded-xl bg-slate-50 p-4 dark:bg-slate-900">
            <h2 class="text-lg font-semibold text-slate-900 dark:text-white">联系与主页</h2>
            <p class="mt-2 text-sm text-slate-600 dark:text-slate-400">可放 GitHub、邮箱和社交链接。</p>
          </article>
        </section>
      </div>
    </section>
  </DefaultLayout>
</template>
