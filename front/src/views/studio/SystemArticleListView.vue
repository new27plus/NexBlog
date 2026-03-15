<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { ChevronLeft, ChevronRight, Loader2, BookOpen } from 'lucide-vue-next'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import { getPublicArticles, type ArticleListItem } from '@/api/article'

/*
 * 页面职责：系统区文章列表页（用户真实使用入口）
 * 与门户页分离，避免“品牌展示”和“业务操作”混在同一个页面。
 */
const route = useRoute()
const router = useRouter()

const articles = ref<ArticleListItem[]>([])
const isLoading = ref(false)
const errorMessage = ref('')

// Pagination State
const currentPage = ref(0)
const totalPages = ref(0)
const pageSize = 12

const hasArticles = computed(() => articles.value.length > 0)

function formatDate(value: string) {
  return new Date(value).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

async function loadSystemArticles(page: number = 0) {
  isLoading.value = true
  errorMessage.value = ''
  try {
    // Scroll to top when changing pages
    if (page !== 0) {
      window.scrollTo({ top: 0, behavior: 'smooth' })
    }
    
    // Simulate slight delay for skeleton demo
    await new Promise(resolve => setTimeout(resolve, 300))
    
    const pageData = await getPublicArticles({ page, size: pageSize })
    articles.value = pageData.content
    totalPages.value = pageData.totalPages
    currentPage.value = pageData.number
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '加载文章列表失败'
  } finally {
    isLoading.value = false
  }
}

function handlePageChange(newPage: number) {
  if (newPage >= 0 && newPage < totalPages.value) {
    loadSystemArticles(newPage)
  }
}

onMounted(() => {
  loadSystemArticles()
})
</script>

<template>
  <DefaultLayout>
    <section class="min-h-screen bg-slate-50 py-12 sm:py-16 dark:bg-slate-900 transition-colors duration-300">
      <div class="container mx-auto px-4 sm:px-6 lg:px-8">
        <div class="mb-10 text-center sm:text-left">
          <h1 class="text-3xl font-extrabold tracking-tight text-slate-900 sm:text-4xl dark:text-white flex items-center justify-center sm:justify-start gap-3">
            <BookOpen class="w-8 h-8 text-indigo-600 dark:text-indigo-400" />
            系统 · 文章中心
          </h1>
          <p class="mt-3 text-lg text-slate-600 dark:text-slate-400 max-w-2xl">
            探索最新的技术见解、教程和行业动态。
          </p>
        </div>

        <!-- Loading State (Skeleton) -->
        <div v-if="isLoading" class="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-3">
          <div v-for="i in 6" :key="i" class="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm dark:bg-slate-800 dark:border-slate-700">
            <div class="animate-pulse space-y-4">
              <div class="h-4 bg-slate-200 rounded w-1/3 dark:bg-slate-700"></div>
              <div class="h-6 bg-slate-200 rounded w-3/4 dark:bg-slate-700"></div>
              <div class="space-y-2">
                <div class="h-3 bg-slate-200 rounded dark:bg-slate-700"></div>
                <div class="h-3 bg-slate-200 rounded dark:bg-slate-700"></div>
                <div class="h-3 bg-slate-200 rounded w-5/6 dark:bg-slate-700"></div>
              </div>
              <div class="h-3 bg-slate-200 rounded w-1/4 pt-4 dark:bg-slate-700"></div>
            </div>
          </div>
        </div>

        <!-- Error State -->
        <div v-else-if="errorMessage" class="rounded-2xl border border-red-200 bg-red-50 p-8 text-center dark:bg-red-900/20 dark:border-red-900/50">
          <div class="inline-flex items-center justify-center w-12 h-12 rounded-full bg-red-100 mb-4 dark:bg-red-900/50">
            <svg class="w-6 h-6 text-red-600 dark:text-red-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
          </div>
          <h3 class="text-lg font-medium text-red-800 dark:text-red-300">加载失败</h3>
          <p class="mt-2 text-sm text-red-600 dark:text-red-400">{{ errorMessage }}</p>
          <button 
            @click="loadSystemArticles(currentPage)" 
            class="mt-4 px-4 py-2 bg-white border border-red-200 rounded-lg text-sm font-medium text-red-600 hover:bg-red-50 transition-colors dark:bg-slate-800 dark:border-slate-700 dark:text-red-400 dark:hover:bg-slate-700"
          >
            重试
          </button>
        </div>

        <!-- Empty State -->
        <div v-else-if="!hasArticles" class="text-center py-20 rounded-2xl border border-dashed border-slate-300 bg-slate-50/50 dark:border-slate-700 dark:bg-slate-800/50">
          <div class="inline-flex items-center justify-center w-16 h-16 rounded-full bg-slate-100 mb-4 dark:bg-slate-700">
            <BookOpen class="w-8 h-8 text-slate-400" />
          </div>
          <h3 class="text-lg font-medium text-slate-900 dark:text-white">暂无文章</h3>
          <p class="mt-1 text-slate-500 dark:text-slate-400">系统暂时没有发布任何文章。</p>
        </div>

        <!-- Article Grid -->
        <div v-else>
          <div class="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-3">
            <RouterLink
              v-for="article in articles"
              :key="article.id"
              :to="`/app/articles/${article.id}`"
              class="group flex flex-col h-full rounded-2xl border border-slate-200 bg-white p-6 shadow-sm transition-all duration-300 hover:-translate-y-1 hover:shadow-lg hover:border-indigo-100 dark:bg-slate-800 dark:border-slate-700 dark:hover:border-indigo-900/50"
            >
              <div class="flex items-center justify-between mb-4">
                <span class="inline-flex items-center rounded-full bg-blue-50 px-2.5 py-0.5 text-xs font-medium text-blue-700 dark:bg-blue-900/30 dark:text-blue-300">
                  Article
                </span>
                <span class="text-xs text-slate-400 dark:text-slate-500">
                  {{ formatDate(article.createdAt) }}
                </span>
              </div>
              
              <h3 class="text-xl font-bold text-slate-900 mb-3 group-hover:text-indigo-600 transition-colors line-clamp-2 dark:text-white dark:group-hover:text-indigo-400">
                {{ article.title }}
              </h3>
              
              <p class="text-sm text-slate-600 leading-relaxed line-clamp-3 mb-6 flex-grow dark:text-slate-400">
                {{ article.summary || '这篇文章暂未填写摘要，点击查看完整内容以获取更多信息。' }}
              </p>
              
              <div class="pt-4 mt-auto border-t border-slate-100 flex items-center text-sm font-medium text-indigo-600 group-hover:gap-2 transition-all dark:border-slate-700 dark:text-indigo-400">
                阅读全文
                <span class="ml-1 transition-transform group-hover:translate-x-1">→</span>
              </div>
            </RouterLink>
          </div>

          <!-- Pagination -->
          <div v-if="totalPages > 1" class="mt-12 flex items-center justify-center gap-4">
            <button
              @click="handlePageChange(currentPage - 1)"
              :disabled="currentPage === 0"
              class="flex items-center gap-2 px-4 py-2 text-sm font-medium rounded-lg border border-slate-200 bg-white text-slate-700 hover:bg-slate-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors dark:bg-slate-800 dark:border-slate-700 dark:text-slate-300 dark:hover:bg-slate-700"
            >
              <ChevronLeft class="w-4 h-4" />
              上一页
            </button>
            
            <span class="text-sm text-slate-600 font-medium dark:text-slate-400">
              第 {{ currentPage + 1 }} 页 / 共 {{ totalPages }} 页
            </span>
            
            <button
              @click="handlePageChange(currentPage + 1)"
              :disabled="currentPage >= totalPages - 1"
              class="flex items-center gap-2 px-4 py-2 text-sm font-medium rounded-lg border border-slate-200 bg-white text-slate-700 hover:bg-slate-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors dark:bg-slate-800 dark:border-slate-700 dark:text-slate-300 dark:hover:bg-slate-700"
            >
              下一页
              <ChevronRight class="w-4 h-4" />
            </button>
          </div>
        </div>
      </div>
    </section>
  </DefaultLayout>
</template>
