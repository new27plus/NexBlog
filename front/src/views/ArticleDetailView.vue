<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, RouterLink } from 'vue-router'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import { getPublicArticleDetail, type ArticleDetail } from '@/api/article'

/*
 * 页面职责：文章详情页
 * - 通过路由参数 id 拉取文章
 * - 展示标题、时间、摘要、正文
 * - 提供返回首页入口
 */

const route = useRoute()
const article = ref<ArticleDetail | null>(null)
const isLoading = ref(false)
const errorMessage = ref('')

const articleId = computed(() => Number(route.params.id))

function formatDate(value: string) {
  return new Date(value).toLocaleString('zh-CN')
}

async function loadArticleDetail() {
  /*
   * 参数说明：
   * - articleId 来自 URL：/articles/:id
   * 关键处理：
   * - 先做 Number 转换
   * - 非数字 id 直接报错，避免请求无效地址
   */
  if (Number.isNaN(articleId.value)) {
    errorMessage.value = '文章 ID 非法，请返回首页重试。'
    return
  }

  isLoading.value = true
  errorMessage.value = ''
  try {
    article.value = await getPublicArticleDetail(articleId.value)
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
    <section class="py-12 sm:py-16">
      <div class="container mx-auto max-w-4xl px-4 sm:px-6 lg:px-8">
        <RouterLink to="/app/articles" class="text-sm font-medium text-blue-600 hover:text-blue-700">← 返回文章中心</RouterLink>

        <p v-if="isLoading" class="mt-6 text-sm text-slate-500">正在加载文章详情...</p>

        <p v-else-if="errorMessage" class="mt-6 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
          加载失败：{{ errorMessage }}
        </p>

        <article v-else-if="article" class="mt-6 rounded-2xl border border-slate-200 bg-white p-8 shadow-sm">
          <h1 class="text-3xl font-bold tracking-tight text-slate-900 sm:text-4xl">
            {{ article.title }}
          </h1>

          <div class="mt-3 text-sm text-slate-500">
            创建于：{{ formatDate(article.createdAt) }} · 更新于：{{ formatDate(article.updatedAt) }}
          </div>

          <p v-if="article.summary" class="mt-6 rounded-lg border border-blue-100 bg-blue-50 px-4 py-3 text-sm text-slate-700">
            摘要：{{ article.summary }}
          </p>

          <div class="prose mt-8 max-w-none whitespace-pre-wrap text-slate-700">
            {{ article.content }}
          </div>
        </article>
      </div>
    </section>
  </DefaultLayout>
</template>
