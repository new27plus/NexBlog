<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import { getPublicArticles, type ArticleListItem } from '@/api/article'

/*
 * 页面职责：系统区文章列表页（用户真实使用入口）
 * 与门户页分离，避免“品牌展示”和“业务操作”混在同一个页面。
 */
const articles = ref<ArticleListItem[]>([])
const isLoading = ref(false)
const errorMessage = ref('')

const hasArticles = computed(() => articles.value.length > 0)

function formatDate(value: string) {
  return new Date(value).toLocaleDateString('zh-CN')
}

async function loadSystemArticles() {
  isLoading.value = true
  errorMessage.value = ''
  try {
    const pageData = await getPublicArticles({ page: 0, size: 12 })
    articles.value = pageData.content
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '加载文章列表失败'
  } finally {
    isLoading.value = false
  }
}

onMounted(loadSystemArticles)
</script>

<template>
  <DefaultLayout>
    <section class="py-12 sm:py-16">
      <div class="container mx-auto px-4 sm:px-6 lg:px-8">
        <h1 class="text-3xl font-bold tracking-tight text-slate-900 sm:text-4xl">系统 · 文章中心</h1>
        <p class="mt-3 text-sm text-slate-600">这是系统区业务页，用于浏览文章与进入详情。</p>

        <p v-if="isLoading" class="mt-6 text-sm text-slate-500">正在加载文章列表...</p>
        <p v-else-if="errorMessage" class="mt-6 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
          加载失败：{{ errorMessage }}
        </p>
        <p v-else-if="!hasArticles" class="mt-6 text-sm text-slate-500">暂无文章，先去写作后台创建第一篇吧。</p>

        <div v-else class="mt-8 grid grid-cols-1 gap-4 md:grid-cols-3">
          <RouterLink
            v-for="article in articles"
            :key="article.id"
            :to="`/app/articles/${article.id}`"
            class="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm transition hover:-translate-y-1 hover:shadow-md"
          >
            <p class="text-xs font-medium text-blue-600">NexBlog Article</p>
            <h3 class="mt-2 text-lg font-semibold text-slate-900">{{ article.title }}</h3>
            <p class="mt-2 line-clamp-3 text-sm leading-6 text-slate-600">
              {{ article.summary || '这篇文章暂未填写摘要，点击查看完整内容。' }}
            </p>
            <div class="mt-4 text-xs text-slate-500">{{ formatDate(article.createdAt) }}</div>
          </RouterLink>
        </div>
      </div>
    </section>
  </DefaultLayout>
</template>
