<script setup lang="ts">
import DefaultLayout from '@/layouts/DefaultLayout.vue'
</script>

<template>
  <DefaultLayout>
    <section class="py-16 md:py-20">
      <div class="mx-auto w-full max-w-6xl px-4 sm:px-6 lg:px-8">
        <header class="mb-8 md:mb-10">
          <h1 class="text-3xl font-bold tracking-tight text-slate-900 dark:text-white md:text-4xl">
            技术文章
          </h1>
          <p class="mt-3 text-base text-slate-600 dark:text-slate-400">
            文章列表页基础骨架已完成，你可以从这里开始接入筛选、分页和数据请求。
          </p>
        </header>

        <section class="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm dark:border-slate-700 dark:bg-slate-800">
          <div class="grid gap-4 md:grid-cols-2">
            <div class="rounded-xl bg-slate-50 p-4 dark:bg-slate-900">
              <p class="text-sm font-medium text-slate-700 dark:text-slate-300">筛选区域</p>
              <p class="mt-2 text-sm text-slate-500 dark:text-slate-400">可放搜索、分类和排序控件</p>
            </div>
            <div class="rounded-xl bg-slate-50 p-4 dark:bg-slate-900">
              <p class="text-sm font-medium text-slate-700 dark:text-slate-300">列表区域</p>
              <p class="mt-2 text-sm text-slate-500 dark:text-slate-400">可放文章卡片或表格列表</p>
            </div>
          </div>
        </section>
      </div>
    </section>
  </DefaultLayout>
</template>
