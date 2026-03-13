<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import { getPublicArticles, type ArticleListItem } from '@/api/article'

const articles = ref<ArticleListItem[]>([])
const isLoading = ref(false)
const errorMessage = ref('')

const hasArticles = computed(() => articles.value.length > 0)

function formatDate(value: string) {
  return new Date(value).toLocaleDateString('zh-CN')
}

async function loadArticles() {
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

onMounted(loadArticles)
</script>

<template>
  <DefaultLayout>
    <section class="py-12 sm:py-16">
      <div class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8">
        <div class="text-center max-w-2xl mx-auto mb-12 mt-4">
          <img
            src="https://api.dicebear.com/7.x/notionists/svg?seed=Felix&backgroundColor=e0e7ff"
            alt="Author Avatar"
            class="w-24 h-24 rounded-full mx-auto mb-6 shadow-md border-4 border-white"
          />
          <h1 class="text-4xl sm:text-5xl font-extrabold text-slate-900 tracking-tight mb-4">探索代码与 AI 的边界</h1>
          <p class="text-lg text-slate-600">在这里记录技术折腾日常，以及与大模型共创的灵感火花。</p>
        </div>

        <p v-if="isLoading" class="text-sm text-slate-500">正在加载文章...</p>
        <p v-else-if="errorMessage" class="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
          加载失败：{{ errorMessage }}
        </p>
        <p v-else-if="!hasArticles" class="text-sm text-slate-500">暂无文章内容。</p>

        <div v-else id="categories" class="grid grid-cols-1 md:grid-cols-2 gap-8">
          <RouterLink
            v-for="article in articles"
            :key="article.id"
            :to="`/articles/${article.id}`"
            class="bg-white rounded-2xl border border-slate-200 overflow-hidden hover:shadow-xl transition-all duration-300 cursor-pointer group flex flex-col"
          >
            <div class="h-48 bg-slate-100 overflow-hidden relative">
              <img
                src="https://images.unsplash.com/photo-1618401471353-b98afee0b2eb?auto=format&fit=crop&q=80&w=800"
                alt="Article Cover"
                class="w-full h-full object-cover group-hover:scale-105 transition duration-500"
              />
              <div class="absolute top-4 left-4 bg-white/90 backdrop-blur text-xs font-bold px-3 py-1 rounded-full text-slate-800 shadow-sm">
                Article
              </div>
            </div>
            <div class="p-6 flex flex-col flex-1">
              <div class="text-xs text-slate-500 mb-2">{{ formatDate(article.createdAt) }}</div>
              <h2 class="text-2xl font-bold text-slate-900 mb-3 group-hover:text-indigo-600 transition">{{ article.title }}</h2>
              <div class="mt-auto bg-indigo-50 border border-indigo-100 rounded-xl p-4">
                <p class="text-sm text-slate-700 leading-relaxed">
                  {{ article.summary || '这篇文章暂未填写摘要，点击查看完整内容。' }}
                </p>
              </div>
            </div>
          </RouterLink>
        </div>

        <section id="about" class="mt-14 border-t border-slate-200 pt-10">
          <h2 class="text-2xl font-bold tracking-tight text-slate-900 sm:text-3xl">关于 NexBlog</h2>
          <p class="mt-3 max-w-3xl text-slate-600 leading-7">
            NexBlog 是一个专注技术写作的个人博客，采用前后端分离架构，面向读者提供简洁阅读体验。
          </p>
        </section>
      </div>
    </section>
  </DefaultLayout>
</template>
