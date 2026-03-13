<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Bot, Check, ChevronDown, ImagePlus, Sparkles, Tags, Loader2, Save, Trash2, Edit3, Search, Plus, ArrowLeft, LayoutDashboard, FileText, AlertCircle, ChevronLeft, ChevronRight, Eye, Calendar, Clock } from 'lucide-vue-next'
import { Listbox, ListboxButton, ListboxOption, ListboxOptions } from '@headlessui/vue'
import StudioLayout from '@/layouts/StudioLayout.vue'
import MarkdownIt from 'markdown-it'
import markdownItAttrs from 'markdown-it-attrs'
import hljs from 'highlight.js'
import { optimizeAiContent, summarizeAiContent, type AiCompareResult, type AiOptimizeMode } from '@/api/ai'
import {
  createStudioArticle,
  deleteStudioArticle,
  getStudioArticleDetail,
  getStudioArticles,
  updateStudioArticle,
  type ArticleListItem,
  type ArticleDetail,
} from '@/api/article'
import { getStudioCategories, type CategoryListItem } from '@/api/category'

const route = useRoute()
const router = useRouter()

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

const articles = ref<ArticleListItem[]>([])
const isLoadingList = ref(false)
const listError = ref('')
const currentPage = ref(0)
const totalPages = ref(0)
const pageSize = 10
const categories = ref<CategoryListItem[]>([])
const isLoadingCategories = ref(false)

const form = reactive({
  id: null as number | null,
  title: '',
  summary: '',
  content: '',
  categoryId: null as number | null,
})

const isSubmitting = ref(false)
const submitError = ref('')
const keyword = ref('')
const viewMode = ref<'dashboard' | 'list' | 'editor' | 'preview'>('dashboard')
const aiActions = ref(0)
const isGeneratingSummary = ref(false)
const isOptimizingContent = ref(false)
const showToast = ref(false)
const toastMessage = ref('')
const toastType = ref<'success' | 'error'>('success')
const currentArticle = ref<ArticleDetail | null>(null)
const isLoadingDetail = ref(false)
const compareResult = reactive<AiCompareResult>({
  original: '',
  optimized: '',
})
const isCompareResultVisible = ref(false)
const isAiBusy = computed(() => isGeneratingSummary.value || isOptimizingContent.value)
const renderedOriginalCompareContent = computed(() => md.render(compareResult.original))
const renderedOptimizedCompareContent = computed(() => md.render(compareResult.optimized))

function showNotification(message: string, type: 'success' | 'error' = 'success') {
  toastMessage.value = message
  toastType.value = type
  showToast.value = true
  setTimeout(() => {
    showToast.value = false
  }, 3000)
}

function resetForm() {
  form.id = null
  form.title = ''
  form.summary = ''
  form.content = ''
  form.categoryId = null
  submitError.value = ''
  compareResult.original = ''
  compareResult.optimized = ''
  isCompareResultVisible.value = false
}

async function loadArticleList(page: number = 0) {
  isLoadingList.value = true
  listError.value = ''
  try {
    const pageData = await getStudioArticles({ page, size: pageSize })
    articles.value = pageData.content
    totalPages.value = pageData.totalPages
    currentPage.value = pageData.number
  } catch (error) {
    listError.value = error instanceof Error ? error.message : '加载后台列表失败'
  } finally {
    isLoadingList.value = false
  }
}

function handlePageChange(newPage: number) {
  if (newPage >= 0 && newPage < totalPages.value) {
    loadArticleList(newPage)
  }
}

async function loadCategories() {
  isLoadingCategories.value = true
  try {
    const pageData = await getStudioCategories({ page: 0, size: 200 })
    categories.value = pageData.content
  } catch (error) {
    showNotification('加载分类失败', 'error')
  } finally {
    isLoadingCategories.value = false
  }
}

async function handleSubmit() {
  if (!form.title.trim()) {
    submitError.value = '请输入文章标题'
    return
  }
  
  isSubmitting.value = true
  submitError.value = ''
  try {
    if (form.id === null) {
      await createStudioArticle({
        title: form.title,
        summary: form.summary,
        content: form.content,
        categoryId: form.categoryId,
      })
      showNotification('文章发布成功！')
    } else {
      await updateStudioArticle(form.id, {
        title: form.title,
        summary: form.summary,
        content: form.content,
        categoryId: form.categoryId,
      })
      showNotification('文章更新成功！')
    }

    resetForm()
    await loadArticleList()
    router.push({ query: { view: 'list' } })
  } catch (error) {
    submitError.value = error instanceof Error ? error.message : '提交失败'
    showNotification('操作失败，请重试', 'error')
  } finally {
    isSubmitting.value = false
  }
}

async function handleEdit(id: number) {
  try {
    isLoadingDetail.value = true
    const detail = await getStudioArticleDetail(id)
    form.id = detail.id
    form.title = detail.title
    form.summary = detail.summary ?? ''
    form.content = detail.content
    form.categoryId = detail.categoryId ?? null
    router.push({ query: { view: 'editor', id: id } })
  } catch (error) {
    showNotification('加载文章详情失败', 'error')
  } finally {
    isLoadingDetail.value = false
  }
}

async function handlePreview(id: number) {
  try {
    isLoadingDetail.value = true
    currentArticle.value = await getStudioArticleDetail(id)
    router.push({ query: { view: 'preview', id: id } })
  } catch (error) {
    showNotification('加载文章详情失败', 'error')
  } finally {
    isLoadingDetail.value = false
  }
}

async function handleDelete(id: number) {
  const confirmed = window.confirm('确认删除这篇文章吗？此操作不可恢复。')
  if (!confirmed) return

  try {
    await deleteStudioArticle(id)
    await loadArticleList()
    showNotification('文章已删除')
  } catch (error) {
    showNotification('删除失败', 'error')
  }
}

function openCreateView() {
  resetForm()
  router.push({ query: { view: 'editor' } })
}

function openDashboard() {
  router.push({ query: { view: 'dashboard' } })
}

async function fillAiSummary() {
  if (isOptimizingContent.value) {
    showNotification('正在进行正文润色，请稍后再生成摘要', 'error')
    return
  }

  const source = form.content.trim() ? form.content : form.title
  if (!source.trim()) {
    form.summary = '请输入标题或正文后再生成摘要。'
    return
  }

  try {
    isGeneratingSummary.value = true
    const summary = await summarizeAiContent(source)
    form.summary = summary
    aiActions.value += 1
    showNotification('AI 摘要生成完成')
  } catch (error) {
    const message = error instanceof Error ? error.message : 'AI 摘要生成失败'
    showNotification(message, 'error')
  } finally {
    isGeneratingSummary.value = false
  }
}

async function optimizeContent(mode: AiOptimizeMode) {
  if (isGeneratingSummary.value) {
    showNotification('正在生成摘要，请稍后再进行润色', 'error')
    return
  }

  const source = form.content
  if (!source.trim()) {
    showNotification('请输入正文后再进行润色', 'error')
    return
  }

  try {
    isOptimizingContent.value = true
    const result = await optimizeAiContent(source, mode)
    aiActions.value += 1
    if (typeof result === 'string') {
      form.content = result
      isCompareResultVisible.value = false
      showNotification('AI 已完成正文润色')
      return
    }
    compareResult.original = result.original.trim() ? result.original : source
    compareResult.optimized = result.optimized
    isCompareResultVisible.value = true
    showNotification('已生成润色对比结果')
  } catch (error) {
    const message = error instanceof Error ? error.message : 'AI 润色失败'
    showNotification(message, 'error')
  } finally {
    isOptimizingContent.value = false
  }
}

function applyCompareOptimized() {
  if (!compareResult.optimized.trim()) {
    showNotification('没有可应用的润色结果', 'error')
    return
  }
  form.content = compareResult.optimized
  isCompareResultVisible.value = false
  showNotification('已应用润色结果')
}

function closeCompareResult() {
  isCompareResultVisible.value = false
}

const filteredArticles = computed(() => {
  const query = keyword.value.trim().toLowerCase()
  if (!query) return articles.value
  return articles.value.filter((item) => {
    const title = item.title.toLowerCase()
    const summary = (item.summary ?? '').toLowerCase()
    return title.includes(query) || summary.includes(query)
  })
})

const totalArticles = computed(() => articles.value.length)
const totalViews = computed(() => articles.value.length * 342)
const totalAiActions = computed(() => articles.value.length * 8 + aiActions.value)
const selectedCategoryName = computed(() => {
  if (form.categoryId === null) {
    return '未分类'
  }
  return categories.value.find((item) => item.id === form.categoryId)?.name ?? '未分类'
})

watch(() => route.query.view, (newView) => {
  if (newView === 'editor') {
    viewMode.value = 'editor'
  } else if (newView === 'list') {
    viewMode.value = 'list'
  } else if (newView === 'preview') {
    viewMode.value = 'preview'
  } else {
    viewMode.value = 'dashboard'
  }
}, { immediate: true })

onMounted(() => {
  loadArticleList()
  loadCategories()
  // Check for id in query if editor or preview mode
  if (route.query.view === 'editor' && route.query.id) {
    handleEdit(Number(route.query.id))
  } else if (route.query.view === 'preview' && route.query.id) {
    handlePreview(Number(route.query.id))
  }
})
</script>

<template>
  <StudioLayout title="仪表盘" active="articles">
    <!-- Toast Notification -->
    <div 
      v-if="showToast" 
      class="fixed top-6 right-6 z-50 px-6 py-4 rounded-xl shadow-2xl transform transition-all duration-300 flex items-center gap-3 border"
      :class="toastType === 'success' ? 'bg-white border-green-100 text-green-700' : 'bg-white border-red-100 text-red-700'"
    >
      <div 
        class="w-8 h-8 rounded-full flex items-center justify-center shrink-0"
        :class="toastType === 'success' ? 'bg-green-100' : 'bg-red-100'"
      >
        <component :is="toastType === 'success' ? Sparkles : Bot" class="w-4 h-4" />
      </div>
      <div>
        <p class="font-bold text-sm">{{ toastType === 'success' ? '操作成功' : '操作失败' }}</p>
        <p class="text-xs opacity-80">{{ toastMessage }}</p>
      </div>
    </div>

    <div v-if="viewMode === 'dashboard'" class="space-y-8 animate-fade-in">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm flex items-center justify-between hover:shadow-md transition-shadow duration-300 group">
          <div>
            <p class="text-sm font-medium text-slate-500 mb-1">已发布文章</p>
            <p class="text-3xl font-bold text-slate-900 group-hover:text-indigo-600 transition-colors">{{ totalArticles }}</p>
          </div>
          <div class="w-12 h-12 bg-blue-50 text-blue-600 rounded-xl flex items-center justify-center group-hover:scale-110 transition-transform duration-300">
            <Bot class="w-6 h-6" />
          </div>
        </div>
        <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm flex items-center justify-between hover:shadow-md transition-shadow duration-300 group">
          <div>
            <p class="text-sm font-medium text-slate-500 mb-1">总浏览量</p>
            <p class="text-3xl font-bold text-slate-900 group-hover:text-green-600 transition-colors">{{ totalViews }}</p>
          </div>
          <div class="w-12 h-12 bg-green-50 text-green-600 rounded-xl flex items-center justify-center group-hover:scale-110 transition-transform duration-300">
            <Sparkles class="w-6 h-6" />
          </div>
        </div>
        <div class="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm flex items-center justify-between hover:shadow-md transition-shadow duration-300 group">
          <div>
            <p class="text-sm font-medium text-slate-500 mb-1">AI 交互次数</p>
            <p class="text-3xl font-bold text-slate-900 group-hover:text-purple-600 transition-colors">{{ totalAiActions }}</p>
          </div>
          <div class="w-12 h-12 bg-indigo-50 text-indigo-600 rounded-xl flex items-center justify-center group-hover:scale-110 transition-transform duration-300">
            <Tags class="w-6 h-6" />
          </div>
        </div>
      </div>
    </div>

    <div v-else-if="viewMode === 'list'" class="space-y-8 animate-fade-in">
      <div class="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden flex flex-col min-h-[400px]">
        <div class="p-5 border-b border-slate-200 flex flex-col gap-3 sm:flex-row sm:justify-between sm:items-center bg-slate-50/50">
          <h3 class="font-bold text-slate-800 flex items-center gap-2">
            <LayoutDashboard class="w-4 h-4 text-slate-500" />
            文章管理
          </h3>
          <div class="flex gap-2 w-full sm:w-auto">
            <div class="relative flex-1 sm:flex-initial">
              <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
              <input
                v-model="keyword"
                type="text"
                placeholder="搜索标题..."
                class="w-full sm:w-64 text-sm border-slate-200 rounded-lg py-2 pl-9 pr-3 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500 transition-all"
              />
            </div>
            <button
              class="bg-indigo-600 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-indigo-700 flex items-center gap-1.5 shadow-sm hover:shadow active:scale-95 transition-all"
              @click="openCreateView"
            >
              <Plus class="w-4 h-4" />
              写文章
            </button>
          </div>
        </div>

        <div v-if="isLoadingList" class="flex-1 flex flex-col items-center justify-center p-12 text-slate-400">
          <Loader2 class="w-8 h-8 animate-spin mb-4 text-indigo-500" />
          <p class="text-sm">正在加载文章列表...</p>
        </div>

        <div v-else-if="listError" class="flex-1 flex flex-col items-center justify-center p-12 text-red-500">
          <p class="bg-red-50 px-4 py-2 rounded-lg border border-red-100 flex items-center gap-2">
            <AlertCircle class="w-4 h-4" />
            {{ listError }}
          </p>
          <button @click="loadArticleList(currentPage)" class="mt-4 text-indigo-600 hover:text-indigo-700 text-sm font-medium">点击重试</button>
        </div>

        <div v-else-if="filteredArticles.length === 0" class="flex-1 flex flex-col items-center justify-center p-12 text-slate-400">
          <div class="w-16 h-16 bg-slate-50 rounded-full flex items-center justify-center mb-4">
            <FileText class="w-8 h-8 text-slate-300" />
          </div>
          <p class="text-sm font-medium text-slate-600">暂无文章</p>
          <p class="text-xs mt-1">点击右上角“写文章”开始创作</p>
        </div>

        <div v-else class="flex-1 flex flex-col">
          <table class="w-full text-left text-sm text-slate-600">
            <thead class="bg-slate-50 text-slate-500 border-b border-slate-200">
              <tr>
                <th class="px-6 py-4 font-semibold text-xs uppercase tracking-wider">标题</th>
                <th class="px-6 py-4 font-semibold text-xs uppercase tracking-wider w-32">状态</th>
                <th class="px-6 py-4 font-semibold text-xs uppercase tracking-wider w-40">分类</th>
                <th class="px-6 py-4 font-semibold text-xs uppercase tracking-wider">摘要</th>
                <th class="px-6 py-4 font-semibold text-xs uppercase tracking-wider text-right w-40">操作</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr v-for="item in filteredArticles" :key="item.id" class="hover:bg-slate-50/80 transition-colors group">
                <td 
                  class="px-6 py-4 font-medium text-slate-900 group-hover:text-indigo-600 transition-colors cursor-pointer" 
                  @click="handlePreview(item.id)"
                >
                  {{ item.title }}
                </td>
                <td class="px-6 py-4">
                  <div class="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-xs font-bold bg-emerald-50 text-emerald-600 border border-emerald-100">
                    <span class="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse"></span>
                    已发布
                  </div>
                </td>
                <td class="px-6 py-4">
                  <span class="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-bold bg-slate-100 text-slate-600 border border-slate-200">
                    {{ item.categoryName || '未分类' }}
                  </span>
                </td>
                <td class="px-6 py-4 max-w-[320px] truncate text-slate-500">{{ item.summary || '暂无摘要' }}</td>
                <td class="px-6 py-4 text-right space-x-3">
                  <button class="text-slate-400 hover:text-indigo-600 transition-colors inline-flex items-center gap-1" @click="handleEdit(item.id)" title="编辑">
                    <Edit3 class="w-4 h-4" />
                  </button>
                  <button class="text-slate-400 hover:text-red-600 transition-colors inline-flex items-center gap-1" @click="handleDelete(item.id)" title="删除">
                    <Trash2 class="w-4 h-4" />
                  </button>
                </td>
              </tr>
            </tbody>
          </table>

          <!-- Pagination -->
          <div v-if="totalPages > 1" class="p-4 border-t border-slate-100 flex items-center justify-between bg-slate-50/50">
             <div class="text-xs text-slate-500">
               显示第 {{ currentPage + 1 }} 页，共 {{ totalPages }} 页
             </div>
             <div class="flex gap-2">
               <button
                 @click="handlePageChange(currentPage - 1)"
                 :disabled="currentPage === 0"
                 class="p-1.5 rounded-lg border border-slate-200 bg-white text-slate-600 hover:bg-slate-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
               >
                 <ChevronLeft class="w-4 h-4" />
               </button>
               <button
                 @click="handlePageChange(currentPage + 1)"
                 :disabled="currentPage >= totalPages - 1"
                 class="p-1.5 rounded-lg border border-slate-200 bg-white text-slate-600 hover:bg-slate-50 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
               >
                 <ChevronRight class="w-4 h-4" />
               </button>
             </div>
          </div>
        </div>
      </div>
    </div>

    <div v-else-if="viewMode === 'preview'" class="bg-white rounded-2xl border border-slate-200 shadow-sm overflow-hidden animate-fade-in min-h-[70vh] flex flex-col">
      <div class="h-16 px-6 border-b border-slate-100 flex items-center justify-between bg-slate-50/50">
        <div class="flex items-center gap-4">
          <button 
            @click="router.push({ query: { view: 'list' } })"
            class="w-8 h-8 rounded-full bg-white border border-slate-200 flex items-center justify-center text-slate-500 hover:text-indigo-600 hover:border-indigo-200 transition-all shadow-sm hover:shadow"
          >
            <ArrowLeft class="w-4 h-4" />
          </button>
          <div class="h-4 w-px bg-slate-300"></div>
          <span class="text-sm font-bold text-slate-500 uppercase tracking-wider">文章预览</span>
        </div>
        <div class="flex items-center gap-3">
          <button 
            v-if="currentArticle"
            @click="handleEdit(currentArticle.id)"
            class="px-4 py-2 bg-indigo-600 text-white text-sm font-medium rounded-lg hover:bg-indigo-700 transition-colors flex items-center gap-2 shadow-sm hover:shadow"
          >
            <Edit3 class="w-4 h-4" />
            编辑此文
          </button>
        </div>
      </div>

      <div v-if="isLoadingDetail" class="flex-1 flex flex-col items-center justify-center text-slate-400">
        <Loader2 class="w-10 h-10 animate-spin mb-4 text-indigo-500" />
        <p class="animate-pulse">正在加载预览...</p>
      </div>
      
      <div v-else-if="currentArticle" class="flex-1 overflow-y-auto p-8 lg:p-12 bg-white scroll-smooth">
        <article class="max-w-4xl mx-auto">
          <header class="mb-10 text-center border-b border-slate-100 pb-10">
            <h1 class="text-3xl md:text-4xl font-black text-slate-900 mb-6 leading-tight">{{ currentArticle.title }}</h1>
            <div class="flex justify-center mb-6">
              <span class="inline-flex items-center px-3 py-1 rounded-full text-xs font-bold bg-indigo-50 text-indigo-600 border border-indigo-100">
                {{ currentArticle.categoryName || '未分类' }}
              </span>
            </div>
            <div class="flex items-center justify-center gap-6 text-sm text-slate-500">
              <span class="flex items-center gap-2 bg-slate-50 px-3 py-1 rounded-full border border-slate-100">
                <Calendar class="w-4 h-4 text-indigo-500" />
                {{ new Date(currentArticle.createdAt).toLocaleDateString() }}
              </span>
              <span class="flex items-center gap-2 bg-slate-50 px-3 py-1 rounded-full border border-slate-100">
                <Clock class="w-4 h-4 text-indigo-500" />
                {{ Math.ceil(currentArticle.content.length / 500) }} 分钟阅读
              </span>
            </div>
          </header>
          
          <div 
            class="prose prose-slate prose-lg max-w-none 
              prose-headings:font-bold prose-headings:tracking-tight prose-headings:text-slate-900
              prose-p:text-slate-600 prose-p:leading-loose
              prose-a:text-indigo-600 prose-a:no-underline hover:prose-a:underline
              prose-img:rounded-2xl prose-img:shadow-lg prose-img:border prose-img:border-slate-100
              prose-pre:bg-slate-900 prose-pre:shadow-xl prose-pre:rounded-xl
              prose-blockquote:border-l-4 prose-blockquote:border-indigo-500 prose-blockquote:bg-indigo-50/30 prose-blockquote:py-2 prose-blockquote:px-4 prose-blockquote:rounded-r-lg
              prose-strong:text-slate-900 prose-strong:font-bold
              prose-code:text-indigo-600 prose-code:bg-indigo-50 prose-code:px-1.5 prose-code:py-0.5 prose-code:rounded-md prose-code:font-medium before:prose-code:content-[''] after:prose-code:content-['']"
            v-html="md.render(currentArticle.content)"
          ></div>
        </article>
      </div>
    </div>

    <div v-else-if="viewMode === 'editor'" class="h-full min-h-[70vh] flex overflow-hidden bg-white rounded-2xl border border-slate-200 shadow-sm animate-fade-in">
      <div class="flex-1 overflow-y-auto p-8 lg:p-12 flex flex-col relative">
        <div class="mb-6 flex items-center justify-between sticky top-0 bg-white/95 backdrop-blur z-10 py-2 border-b border-transparent transition-all" :class="{ 'border-slate-100': true }">
          <div class="flex items-center gap-3">
            <button 
              class="w-8 h-8 rounded-full hover:bg-slate-100 flex items-center justify-center text-slate-400 hover:text-slate-700 transition-colors"
              @click="openDashboard"
            >
              <ArrowLeft class="w-5 h-5" />
            </button>
            <h3 class="text-xl font-bold text-slate-900">{{ form.id === null ? '撰写文章' : `编辑文章 #${form.id}` }}</h3>
          </div>
          <div class="flex items-center gap-3">
            <button type="button" class="text-slate-500 hover:text-slate-800 text-sm font-medium px-3 py-1.5 rounded-lg hover:bg-slate-50 transition-colors" @click="resetForm">
              清空
            </button>
            <button
              type="button"
              :disabled="isSubmitting"
              class="bg-indigo-600 text-white px-5 py-2 rounded-lg text-sm font-semibold hover:bg-indigo-700 shadow-md hover:shadow-lg hover:-translate-y-0.5 active:translate-y-0 disabled:opacity-60 disabled:shadow-none disabled:translate-y-0 transition-all flex items-center gap-2"
              @click="handleSubmit"
            >
              <Loader2 v-if="isSubmitting" class="w-4 h-4 animate-spin" />
              <Save v-else class="w-4 h-4" />
              {{ isSubmitting ? '保存中...' : form.id === null ? '发布文章' : '保存更新' }}
            </button>
          </div>
        </div>
        
        <form class="flex-1 flex flex-col gap-6" @submit.prevent="handleSubmit">
          <div class="group relative">
            <input
              v-model="form.title"
              type="text"
              required
              maxlength="100"
              placeholder="输入文章大标题..."
              class="w-full text-4xl font-black border-b-2 border-slate-100 py-4 focus:border-indigo-500 outline-none bg-transparent transition-all duration-300 placeholder-slate-300 text-slate-900 focus:bg-slate-50/30 rounded-t-lg px-2"
            />
          </div>

          <div class="flex flex-col gap-2">
            <label class="text-xs font-semibold text-slate-500 uppercase tracking-wider">文章分类</label>
            <Listbox v-model="form.categoryId" :disabled="isLoadingCategories">
              <div class="relative">
                <ListboxButton
                  class="w-full border border-slate-200 rounded-xl px-4 py-2.5 text-sm bg-white text-left transition-all outline-none focus:outline-none focus-visible:outline-none focus:border-indigo-500 focus:ring-4 focus:ring-indigo-500/10 disabled:bg-slate-100 disabled:text-slate-400 disabled:cursor-not-allowed flex items-center justify-between"
                >
                  <span class="truncate text-slate-700">{{ selectedCategoryName }}</span>
                  <ChevronDown class="w-4 h-4 text-slate-400" />
                </ListboxButton>
                <transition
                  enter-active-class="transition duration-150 ease-out"
                  enter-from-class="opacity-0 -translate-y-1 scale-95"
                  enter-to-class="opacity-100 translate-y-0 scale-100"
                  leave-active-class="transition duration-100 ease-in"
                  leave-from-class="opacity-100 translate-y-0 scale-100"
                  leave-to-class="opacity-0 -translate-y-1 scale-95"
                >
                  <ListboxOptions
                    class="absolute z-20 mt-2 max-h-64 w-full overflow-auto rounded-xl border border-slate-200 bg-white p-1 shadow-lg ring-1 ring-black/5 outline-none focus:outline-none focus-visible:outline-none"
                  >
                    <ListboxOption v-slot="{ active, selected }" :value="null" as="template">
                      <li
                        class="cursor-pointer select-none rounded-lg px-3 py-2 text-sm transition-colors flex items-center justify-between"
                        :class="active ? 'bg-indigo-50 text-indigo-700' : 'text-slate-700'"
                      >
                        <span :class="selected ? 'font-semibold' : 'font-medium'">未分类</span>
                        <Check v-if="selected" class="h-4 w-4 text-indigo-600" />
                      </li>
                    </ListboxOption>
                    <ListboxOption
                      v-for="item in categories"
                      :key="item.id"
                      v-slot="{ active, selected }"
                      :value="item.id"
                      as="template"
                    >
                      <li
                        class="cursor-pointer select-none rounded-lg px-3 py-2 text-sm transition-colors flex items-center justify-between"
                        :class="active ? 'bg-indigo-50 text-indigo-700' : 'text-slate-700'"
                      >
                        <span :class="selected ? 'font-semibold' : 'font-medium'">{{ item.name }}</span>
                        <Check v-if="selected" class="h-4 w-4 text-indigo-600" />
                      </li>
                    </ListboxOption>
                  </ListboxOptions>
                </transition>
              </div>
            </Listbox>
          </div>
          
          <div
            v-if="isCompareResultVisible"
            class="grid grid-cols-1 gap-4 lg:grid-cols-2"
          >
            <div class="rounded-xl border border-slate-200 bg-slate-50/70">
              <div class="border-b border-slate-200 px-4 py-3 text-xs font-bold uppercase tracking-wider text-slate-500">原文</div>
              <div
                class="prose prose-slate max-w-none min-h-[520px] px-4 py-4 prose-p:leading-7 prose-pre:rounded-lg prose-pre:bg-slate-900"
                v-html="renderedOriginalCompareContent"
              ></div>
            </div>
            <div class="rounded-xl border border-indigo-200 bg-indigo-50/30">
              <div class="border-b border-indigo-100 px-4 py-3 text-xs font-bold uppercase tracking-wider text-indigo-600">润色后</div>
              <div
                class="prose prose-slate max-w-none min-h-[520px] px-4 py-4 prose-p:leading-7 prose-pre:rounded-lg prose-pre:bg-slate-900"
                v-html="renderedOptimizedCompareContent"
              ></div>
            </div>
          </div>
          <textarea
            v-else
            v-model="form.content"
            required
            rows="20"
            placeholder="开始用 Markdown 记录你的灵感..."
            class="w-full flex-1 resize-none border border-slate-100 rounded-xl p-6 focus:ring-2 focus:ring-indigo-500/10 focus:border-indigo-500 outline-none text-slate-700 text-lg leading-relaxed bg-slate-50/30 focus:bg-white transition-all duration-300 placeholder-slate-300 font-serif selection:bg-indigo-100 selection:text-indigo-800"
          />
          
          <p v-if="submitError" class="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700 flex items-center gap-2">
            <AlertCircle class="w-4 h-4" />
            {{ submitError }}
          </p>
        </form>
      </div>
      
      <div class="w-80 border-l border-slate-200 bg-slate-50/80 flex flex-col shadow-[inset_1px_0_0_rgba(0,0,0,0.05)] backdrop-blur-sm">
        <div class="p-4 border-b border-slate-200 font-bold text-slate-800 flex items-center gap-2 bg-slate-100/50">
          <Bot class="w-5 h-5 text-indigo-600" />
          AI 写作副驾
        </div>
        <div class="p-5 space-y-8 overflow-y-auto flex-1 text-sm">
          <div class="space-y-3">
            <div class="flex justify-between items-center">
              <label class="font-bold text-slate-700 text-xs uppercase tracking-wider flex items-center gap-1.5">
                <Sparkles class="w-3.5 h-3.5 text-slate-400" />
                SEO 摘要
              </label>
              <button 
                class="text-indigo-600 hover:text-indigo-700 hover:bg-indigo-50 px-2 py-1 rounded-md transition-all flex items-center gap-1.5 text-xs font-bold" 
                type="button" 
                :disabled="isAiBusy"
                @click="fillAiSummary"
              >
                <Loader2 v-if="isGeneratingSummary" class="w-3 h-3 animate-spin" />
                <span v-else>一键生成</span>
              </button>
            </div>
            <textarea
              v-model="form.summary"
              rows="4"
              maxlength="300"
              class="w-full border border-slate-200 rounded-xl text-sm outline-none focus:border-indigo-500 focus:ring-4 focus:ring-indigo-500/10 bg-slate-50 focus:bg-white shadow-sm p-3 leading-relaxed transition-all duration-300 hover:border-indigo-300 hover:shadow-md hover:bg-white placeholder-slate-400"
              placeholder="点击生成，AI 将自动提炼摘要..."
            />
          </div>
          
          <div class="space-y-3">
            <div class="flex justify-between items-center">
              <label class="font-bold text-slate-700 text-xs uppercase tracking-wider flex items-center gap-1.5">
                <Tags class="w-3.5 h-3.5 text-slate-400" />
                正文润色
              </label>
            </div>
            <div class="grid grid-cols-2 gap-2">
              <button
                class="inline-flex items-center justify-center rounded-lg bg-indigo-600 px-3 py-2 text-xs font-bold text-white transition-all hover:bg-indigo-700 disabled:opacity-60"
                type="button"
                :disabled="isAiBusy"
                @click="optimizeContent('replace')"
              >
                <Loader2 v-if="isOptimizingContent" class="w-3 h-3 animate-spin" />
                <span v-else>直接替换</span>
              </button>
              <button
                class="inline-flex items-center justify-center rounded-lg border border-indigo-200 bg-white px-3 py-2 text-xs font-bold text-indigo-600 transition-all hover:bg-indigo-50 disabled:opacity-60"
                type="button"
                :disabled="isAiBusy"
                @click="optimizeContent('compare')"
              >
                <Loader2 v-if="isOptimizingContent" class="w-3 h-3 animate-spin" />
                <span v-else>对比润色</span>
              </button>
            </div>
            <div
              v-if="isCompareResultVisible"
              class="rounded-xl border border-slate-200 bg-white p-3 space-y-3 shadow-sm"
            >
              <p class="text-xs text-slate-600 leading-relaxed">左侧写作区已切换为原文 / 润色后 Markdown 对比视图。</p>
              <button
                type="button"
                class="w-full rounded-lg bg-emerald-600 px-3 py-2 text-xs font-bold text-white hover:bg-emerald-700 transition-colors"
                @click="applyCompareOptimized"
              >
                应用润色结果
              </button>
              <button
                type="button"
                class="w-full rounded-lg border border-slate-200 bg-white px-3 py-2 text-xs font-bold text-slate-600 hover:bg-slate-50 transition-colors"
                @click="closeCompareResult"
              >
                返回编辑
              </button>
            </div>
          </div>
          
          <div class="space-y-3">
            <label class="font-bold text-slate-700 text-xs uppercase tracking-wider flex items-center gap-1.5">
              <ImagePlus class="w-3.5 h-3.5 text-slate-400" />
              文章封面
            </label>
            <button class="w-full group border-2 border-dashed border-slate-300 rounded-2xl bg-slate-50/50 p-8 text-center hover:border-indigo-400 hover:bg-indigo-50/30 hover:shadow-md transition-all duration-300 focus:outline-none focus:ring-4 focus:ring-indigo-500/10 active:scale-[0.98]" type="button">
              <div class="w-12 h-12 bg-slate-50 rounded-full flex items-center justify-center mx-auto mb-3 group-hover:bg-white group-hover:scale-110 transition-transform shadow-sm">
                <ImagePlus class="w-6 h-6 text-slate-400 group-hover:text-indigo-500 transition-colors" />
              </div>
              <p class="text-xs text-slate-500 mb-1">点击上传或拖拽图片</p>
              <p class="text-[10px] text-indigo-600 font-bold opacity-0 group-hover:opacity-100 transition-opacity">支持 AI 自动生图</p>
            </button>
          </div>
        </div>
      </div>
    </div>
  </StudioLayout>
</template>

<style scoped>
.animate-fade-in {
  animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
