<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { ArrowRight, Calendar, Clock, Sparkles, Search, ChevronRight, Github, Twitter } from 'lucide-vue-next'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import { getPublicArticles, type ArticleListItem } from '@/api/article'

const articles = ref<ArticleListItem[]>([])
const isLoading = ref(true)
const errorMessage = ref('')
const searchQuery = ref('')

const hasArticles = computed(() => articles.value.length > 0)

const filteredArticles = computed(() => {
  if (!searchQuery.value) return articles.value
  const query = searchQuery.value.toLowerCase()
  return articles.value.filter(article => 
    article.title.toLowerCase().includes(query) || 
    (article.summary && article.summary.toLowerCase().includes(query))
  )
})

function formatDate(value: string) {
  return new Date(value).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric'
  })
}

async function loadArticles() {
  isLoading.value = true
  errorMessage.value = ''
  try {
    // Simulate slight delay for skeleton demo
    await new Promise(resolve => setTimeout(resolve, 800))
    const pageData = await getPublicArticles({ page: 0, size: 12 })
    articles.value = pageData.content
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '加载文章列表失败'
  } finally {
    isLoading.value = false
  }
}

onMounted(loadArticles)
</script>

<template>
  <DefaultLayout>
    <!-- Hero Section -->
    <section class="relative pt-20 pb-16 md:pt-32 md:pb-24 overflow-hidden">
      <div class="absolute inset-0 -z-10 bg-[radial-gradient(ellipse_at_top_right,_var(--tw-gradient-stops))] from-indigo-100/50 via-slate-50/50 to-white dark:from-indigo-900/20 dark:via-slate-900 dark:to-slate-900"></div>
      
      <div class="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
        <div class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-indigo-50 text-indigo-600 text-sm font-medium mb-8 border border-indigo-100 animate-fade-in-up">
          <Sparkles class="w-4 h-4" />
          <span>Exploring Code & AI</span>
        </div>
        
        <h1 class="text-5xl md:text-7xl font-extrabold tracking-tight text-slate-900 dark:text-white mb-6 animate-fade-in-up delay-100">
          探索代码与 <span class="text-transparent bg-clip-text bg-linear-to-r from-indigo-600 to-blue-500">AI 的边界</span>
        </h1>
        
        <p class="text-xl text-slate-600 dark:text-slate-400 max-w-2xl mx-auto mb-10 leading-relaxed animate-fade-in-up delay-200">
          在这里记录技术折腾日常，分享全栈开发经验，以及与大模型共创的灵感火花。
          构建下一代 AI 原生应用。
        </p>
        
        <div class="flex items-center justify-center gap-4 animate-fade-in-up delay-300">
          <a href="#articles" class="px-8 py-4 rounded-full bg-slate-900 text-white font-bold hover:bg-slate-800 transition-all hover:scale-105 active:scale-95 shadow-lg shadow-slate-900/20 flex items-center gap-2 dark:bg-white dark:text-slate-900">
            开始阅读
            <ArrowRight class="w-4 h-4" />
          </a>
          <a href="https://github.com" target="_blank" class="px-8 py-4 rounded-full bg-white text-slate-700 font-bold border border-slate-200 hover:border-slate-300 hover:bg-slate-50 transition-all hover:scale-105 active:scale-95 flex items-center gap-2 dark:bg-slate-800 dark:text-slate-300 dark:border-slate-700 dark:hover:bg-slate-700">
            <Github class="w-5 h-5" />
            GitHub
          </a>
        </div>
      </div>
    </section>

    <!-- Articles Section -->
    <section id="articles" class="py-16 bg-white dark:bg-slate-900">
      <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="flex flex-col md:flex-row md:items-center justify-between mb-12 gap-6">
          <div>
            <h2 class="text-3xl font-bold text-slate-900 dark:text-white">最新文章</h2>
            <p class="text-slate-500 mt-2 dark:text-slate-400">分享技术见解与开发心得</p>
          </div>
          
          <div class="relative group w-full md:w-72">
            <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400 group-focus-within:text-indigo-500 transition-colors" />
            <input 
              v-model="searchQuery"
              type="text" 
              placeholder="搜索文章..." 
              class="w-full pl-10 pr-4 py-2.5 rounded-xl border border-slate-200 bg-slate-50 outline-none focus:bg-white focus:border-indigo-500 focus:ring-4 focus:ring-indigo-500/10 transition-all dark:bg-slate-800 dark:border-slate-700 dark:text-white"
            >
          </div>
        </div>

        <!-- Loading State -->
        <div v-if="isLoading" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          <div v-for="i in 6" :key="i" class="rounded-2xl border border-slate-100 bg-white p-6 shadow-sm dark:bg-slate-800 dark:border-slate-700">
            <div class="h-48 bg-slate-100 rounded-xl mb-6 animate-pulse dark:bg-slate-700"></div>
            <div class="h-4 bg-slate-100 rounded w-1/3 mb-4 animate-pulse dark:bg-slate-700"></div>
            <div class="h-8 bg-slate-100 rounded w-3/4 mb-4 animate-pulse dark:bg-slate-700"></div>
            <div class="h-20 bg-slate-100 rounded w-full animate-pulse dark:bg-slate-700"></div>
          </div>
        </div>

        <!-- Error State -->
        <div v-else-if="errorMessage" class="rounded-2xl border border-red-100 bg-red-50 p-8 text-center text-red-600 dark:bg-red-900/20 dark:border-red-900/50 dark:text-red-400">
          <p class="font-medium mb-2">加载失败</p>
          <p class="text-sm opacity-80">{{ errorMessage }}</p>
          <button @click="loadArticles" class="mt-4 px-4 py-2 bg-white rounded-lg text-sm font-medium shadow-sm hover:bg-slate-50 text-red-600 transition-colors">重试</button>
        </div>

        <!-- Empty State -->
        <div v-else-if="filteredArticles.length === 0" class="text-center py-20">
          <div class="w-16 h-16 bg-slate-50 rounded-full flex items-center justify-center mx-auto mb-4 dark:bg-slate-800">
            <Search class="w-8 h-8 text-slate-300 dark:text-slate-600" />
          </div>
          <h3 class="text-lg font-medium text-slate-900 dark:text-white">未找到相关文章</h3>
          <p class="text-slate-500 mt-1 dark:text-slate-400">换个关键词试试看？</p>
        </div>

        <!-- Article Grid -->
        <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          <RouterLink
            v-for="article in filteredArticles"
            :key="article.id"
            :to="`/articles/${article.id}`"
            class="group flex flex-col bg-white rounded-2xl border border-slate-200 overflow-hidden hover:shadow-xl hover:shadow-indigo-500/10 transition-all duration-300 hover:-translate-y-1 dark:bg-slate-800 dark:border-slate-700"
          >
            <div class="aspect-video bg-slate-100 relative overflow-hidden dark:bg-slate-700">
              <div class="absolute inset-0 bg-linear-to-tr from-indigo-500/10 to-blue-500/10 group-hover:scale-105 transition-transform duration-500"></div>
              <div class="absolute top-4 left-4">
                <span class="px-3 py-1 rounded-full bg-white/90 backdrop-blur text-xs font-bold text-slate-900 shadow-sm dark:bg-slate-900/90 dark:text-white">
                  Article
                </span>
              </div>
            </div>
            
            <div class="p-6 flex flex-col flex-1">
              <div class="flex items-center gap-3 text-xs text-slate-500 mb-3 dark:text-slate-400">
                <div class="flex items-center gap-1">
                  <Calendar class="w-3.5 h-3.5" />
                  {{ formatDate(article.createdAt) }}
                </div>
                <div class="w-1 h-1 rounded-full bg-slate-300 dark:bg-slate-600"></div>
                <div class="flex items-center gap-1">
                  <Clock class="w-3.5 h-3.5" />
                  5 min read
                </div>
              </div>
              
              <h3 class="text-xl font-bold text-slate-900 mb-3 line-clamp-2 group-hover:text-indigo-600 transition-colors dark:text-white dark:group-hover:text-indigo-400">
                {{ article.title }}
              </h3>
              
              <p class="text-sm text-slate-600 leading-relaxed line-clamp-3 mb-6 dark:text-slate-400">
                {{ article.summary || '暂无摘要，点击阅读全文...' }}
              </p>
              
              <div class="mt-auto flex items-center text-indigo-600 font-medium text-sm group-hover:translate-x-1 transition-transform dark:text-indigo-400">
                阅读全文
                <ArrowRight class="w-4 h-4 ml-1" />
              </div>
            </div>
          </RouterLink>
        </div>
      </div>
    </section>

    <!-- About Section -->
    <section id="about" class="py-20 bg-slate-50 border-t border-slate-200 dark:bg-slate-800/50 dark:border-slate-800">
      <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
        <h2 class="text-3xl font-bold text-slate-900 mb-8 dark:text-white">关于 NexBlog</h2>
        <p class="text-lg text-slate-600 leading-relaxed mb-10 dark:text-slate-300">
          NexBlog 是一个专注技术写作的个人博客平台，采用现代化的前后端分离架构。
          我们致力于为开发者提供极致的写作与阅读体验，支持 Markdown 实时预览、AI 辅助写作以及一键部署到 GitHub Pages。
        </p>
        
        <div class="grid grid-cols-1 md:grid-cols-3 gap-8 text-left">
          <div class="bg-white p-6 rounded-2xl shadow-sm border border-slate-100 dark:bg-slate-800 dark:border-slate-700">
            <div class="w-10 h-10 bg-blue-50 rounded-xl flex items-center justify-center text-blue-600 mb-4 dark:bg-blue-900/30 dark:text-blue-400">
              <Sparkles class="w-5 h-5" />
            </div>
            <h3 class="font-bold text-slate-900 mb-2 dark:text-white">AI 驱动</h3>
            <p class="text-sm text-slate-600 dark:text-slate-400">内置 AI 助手，自动生成摘要、提取标签，让写作更智能。</p>
          </div>
          
          <div class="bg-white p-6 rounded-2xl shadow-sm border border-slate-100 dark:bg-slate-800 dark:border-slate-700">
            <div class="w-10 h-10 bg-indigo-50 rounded-xl flex items-center justify-center text-indigo-600 mb-4 dark:bg-indigo-900/30 dark:text-indigo-400">
              <Github class="w-5 h-5" />
            </div>
            <h3 class="font-bold text-slate-900 mb-2 dark:text-white">开源开放</h3>
            <p class="text-sm text-slate-600 dark:text-slate-400">代码完全开源，支持私有化部署，数据完全掌控在自己手中。</p>
          </div>
          
          <div class="bg-white p-6 rounded-2xl shadow-sm border border-slate-100 dark:bg-slate-800 dark:border-slate-700">
            <div class="w-10 h-10 bg-emerald-50 rounded-xl flex items-center justify-center text-emerald-600 mb-4 dark:bg-emerald-900/30 dark:text-emerald-400">
              <Clock class="w-5 h-5" />
            </div>
            <h3 class="font-bold text-slate-900 mb-2 dark:text-white">极致性能</h3>
            <p class="text-sm text-slate-600 dark:text-slate-400">基于 Vue 3 + Vite 构建，秒级加载，提供流畅的访问体验。</p>
          </div>
        </div>
      </div>
    </section>
  </DefaultLayout>
</template>

<style scoped>
.animate-fade-in-up {
  animation: fadeInUp 0.8s cubic-bezier(0.16, 1, 0.3, 1) forwards;
  opacity: 0;
  transform: translateY(20px);
}

.delay-100 {
  animation-delay: 0.1s;
}

.delay-200 {
  animation-delay: 0.2s;
}

.delay-300 {
  animation-delay: 0.3s;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
