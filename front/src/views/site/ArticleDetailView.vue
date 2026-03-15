<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, RouterLink } from 'vue-router'
import { ArrowLeft, Calendar, Clock, Tag, User, Share2, Check } from 'lucide-vue-next'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import { getPublicArticleDetail, type ArticleDetail } from '@/api/article'
import MarkdownIt from 'markdown-it'
import markdownItAttrs from 'markdown-it-attrs'
import hljs from 'highlight.js'

const route = useRoute()
const article = ref<ArticleDetail | null>(null)
const isLoading = ref(true)
const errorMessage = ref('')
const isCopied = ref(false)

const md = new MarkdownIt({
  html: true,
  linkify: true,
  typographer: true,
  breaks: true,
  highlight(code: string, language: string): string {
    if (language && hljs.getLanguage(language)) {
      return `<pre class="hljs"><code>${hljs.highlight(code, { language, ignoreIllegals: true }).value}</code></pre>`
    }
    return `<pre class="hljs"><code>${hljs.highlightAuto(code).value}</code></pre>`
  },
}).use(markdownItAttrs)

const articleId = computed(() => Number(route.params.id))

function formatDate(value: string) {
  return new Date(value).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

function calculateReadingTime(content: string) {
  const wordsPerMinute = 300
  const words = content.length
  return Math.ceil(words / wordsPerMinute)
}

async function handleShare() {
  try {
    await navigator.clipboard.writeText(window.location.href)
    isCopied.value = true
    setTimeout(() => {
      isCopied.value = false
    }, 2000)
  } catch (err) {
    console.error('Failed to copy link', err)
  }
}

async function loadArticleDetail() {
  if (Number.isNaN(articleId.value)) {
    errorMessage.value = '文章 ID 非法，请返回首页重试。'
    isLoading.value = false
    return
  }

  isLoading.value = true
  errorMessage.value = ''
  try {
    // Simulate slight delay for skeleton demo
    await new Promise(resolve => setTimeout(resolve, 600))
    const data = await getPublicArticleDetail(articleId.value)
    article.value = data
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '加载文章详情失败'
  } finally {
    isLoading.value = false
  }
}

onMounted(loadArticleDetail)
</script>

<template>
  <DefaultLayout>
    <article class="min-h-screen bg-white dark:bg-slate-900 pb-20">
      <!-- Header / Hero -->
      <div class="relative bg-slate-50 border-b border-slate-200 dark:bg-slate-800 dark:border-slate-700">
        <div class="absolute inset-0 bg-[radial-gradient(#e5e7eb_1px,transparent_1px)] [background-size:16px_16px] opacity-50 dark:opacity-10"></div>
        
        <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 pt-24 pb-12 relative z-10">
          <RouterLink 
            to="/" 
            class="inline-flex items-center gap-2 text-sm font-medium text-slate-500 hover:text-indigo-600 transition-colors mb-8 group dark:text-slate-400 dark:hover:text-indigo-400"
          >
            <ArrowLeft class="w-4 h-4 group-hover:-translate-x-1 transition-transform" />
            返回首页
          </RouterLink>

          <div v-if="isLoading" class="animate-pulse space-y-4">
            <div class="h-4 bg-slate-200 rounded w-32 dark:bg-slate-700"></div>
            <div class="h-10 bg-slate-200 rounded w-3/4 dark:bg-slate-700"></div>
            <div class="h-4 bg-slate-200 rounded w-1/2 dark:bg-slate-700"></div>
          </div>

          <div v-else-if="article">
            <div class="flex flex-wrap items-center gap-3 text-sm text-indigo-600 font-medium mb-4 dark:text-indigo-400">
              <span class="px-3 py-1 rounded-full bg-indigo-50 border border-indigo-100 dark:bg-indigo-900/30 dark:border-indigo-800">
                Technical
              </span>
              <span v-if="article.categoryName" class="px-3 py-1 rounded-full bg-blue-50 border border-blue-100 dark:bg-blue-900/30 dark:border-blue-800">
                {{ article.categoryName }}
              </span>
            </div>

            <h1 class="text-3xl sm:text-4xl md:text-5xl font-extrabold tracking-tight text-slate-900 mb-6 leading-tight dark:text-white">
              {{ article.title }}
            </h1>

            <div class="flex flex-wrap items-center gap-6 text-sm text-slate-500 dark:text-slate-400">
              <div class="flex items-center gap-2">
                <div class="w-8 h-8 rounded-full bg-indigo-100 flex items-center justify-center text-indigo-600 font-bold dark:bg-indigo-900 dark:text-indigo-300">
                  <User class="w-4 h-4" />
                </div>
                <span class="font-medium text-slate-900 dark:text-slate-200">NexBlog Author</span>
              </div>
              <div class="flex items-center gap-1.5">
                <Calendar class="w-4 h-4" />
                {{ formatDate(article.createdAt) }}
              </div>
              <div class="flex items-center gap-1.5">
                <Clock class="w-4 h-4" />
                {{ calculateReadingTime(article.content) }} min read
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Content -->
      <div class="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div v-if="isLoading" class="space-y-6 animate-pulse">
          <div class="h-4 bg-slate-100 rounded w-full dark:bg-slate-800"></div>
          <div class="h-4 bg-slate-100 rounded w-full dark:bg-slate-800"></div>
          <div class="h-4 bg-slate-100 rounded w-3/4 dark:bg-slate-800"></div>
          <div class="h-64 bg-slate-100 rounded w-full dark:bg-slate-800"></div>
          <div class="h-4 bg-slate-100 rounded w-full dark:bg-slate-800"></div>
        </div>

        <div v-else-if="errorMessage" class="rounded-2xl border border-red-100 bg-red-50 p-8 text-center text-red-600 dark:bg-red-900/20 dark:border-red-900/50 dark:text-red-400">
          <p class="font-medium mb-2">加载失败</p>
          <p class="text-sm opacity-80">{{ errorMessage }}</p>
          <RouterLink to="/" class="inline-block mt-4 px-4 py-2 bg-white rounded-lg text-sm font-medium shadow-sm hover:bg-slate-50 text-red-600 transition-colors">返回首页</RouterLink>
        </div>

        <div v-else-if="article">
          <!-- Summary Card -->
          <div v-if="article.summary" class="bg-indigo-50/50 border border-indigo-100 rounded-2xl p-6 mb-10 dark:bg-indigo-900/20 dark:border-indigo-900/50">
            <h3 class="flex items-center gap-2 text-indigo-900 font-bold mb-3 dark:text-indigo-300">
              <Tag class="w-4 h-4" />
              文章摘要
            </h3>
            <p class="text-indigo-800/80 leading-relaxed dark:text-indigo-300/80">
              {{ article.summary }}
            </p>
          </div>

          <!-- Main Content -->
          <article class="prose prose-slate prose-lg max-w-none dark:prose-invert 
            prose-headings:font-bold prose-headings:tracking-tight 
            prose-a:text-indigo-600 prose-a:no-underline hover:prose-a:underline
            prose-img:rounded-2xl prose-img:shadow-lg
            prose-pre:bg-slate-900 prose-pre:shadow-lg prose-pre:rounded-xl
          " v-html="md.render(article.content)">
          </article>

          <!-- Footer Actions -->
          <div class="mt-16 pt-8 border-t border-slate-200 flex justify-between items-center dark:border-slate-800">
            <div class="text-sm text-slate-500 dark:text-slate-400">
              最后更新于 {{ formatDate(article.updatedAt) }}
            </div>
            <button 
              @click="handleShare"
              class="flex items-center gap-2 px-4 py-2 rounded-lg border transition-all duration-200"
              :class="isCopied 
                ? 'bg-green-50 border-green-200 text-green-600 dark:bg-green-900/30 dark:border-green-800 dark:text-green-400' 
                : 'border-slate-200 text-slate-600 hover:bg-slate-50 hover:text-slate-900 dark:border-slate-700 dark:text-slate-400 dark:hover:bg-slate-800 dark:hover:text-white'"
            >
              <Check v-if="isCopied" class="w-4 h-4" />
              <Share2 v-else class="w-4 h-4" />
              {{ isCopied ? '链接已复制' : '分享文章' }}
            </button>
          </div>
        </div>
      </div>
    </article>
  </DefaultLayout>
</template>
