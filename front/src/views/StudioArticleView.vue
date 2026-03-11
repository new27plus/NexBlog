<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import {
  createStudioArticle,
  deleteStudioArticle,
  getStudioArticleDetail,
  getStudioArticles,
  updateStudioArticle,
  type ArticleListItem,
} from '@/api/article'

/*
 * 页面职责：MVP 文章管理页
 * 覆盖功能：
 * - 列表（Read）
 * - 新增（Create）
 * - 编辑（Update）
 * - 删除（Delete）
 *
 * 为什么先做成“单页管理”：
 * - 对新手更友好：所有 CRUD 流程集中在一个文件里观察
 * - 先跑通业务闭环，后续再拆成多个组件
 */

const articles = ref<ArticleListItem[]>([])
const isLoadingList = ref(false)
const listError = ref('')

/*
 * 表单模型：
 * - id 为 null 表示创建模式
 * - id 有值表示编辑模式
 */
const form = reactive({
  id: null as number | null,
  title: '',
  summary: '',
  content: '',
})

const isSubmitting = ref(false)
const submitError = ref('')

function resetForm() {
  form.id = null
  form.title = ''
  form.summary = ''
  form.content = ''
  submitError.value = ''
}

async function loadArticleList() {
  isLoadingList.value = true
  listError.value = ''
  try {
    const pageData = await getStudioArticles({ page: 0, size: 20 })
    articles.value = pageData.content
  } catch (error) {
    listError.value = error instanceof Error ? error.message : '加载后台列表失败'
  } finally {
    isLoadingList.value = false
  }
}

async function handleSubmit() {
  /*
   * 提交逻辑设计：
   * - create 与 update 共用一个表单
   * - 通过 form.id 是否为空来判断当前模式
   */
  isSubmitting.value = true
  submitError.value = ''
  try {
    if (form.id === null) {
      await createStudioArticle({
        title: form.title,
        summary: form.summary,
        content: form.content,
      })
    } else {
      await updateStudioArticle(form.id, {
        title: form.title,
        summary: form.summary,
        content: form.content,
      })
    }

    resetForm()
    await loadArticleList()
  } catch (error) {
    submitError.value = error instanceof Error ? error.message : '提交失败'
  } finally {
    isSubmitting.value = false
  }
}

async function handleEdit(id: number) {
  /*
   * 编辑时先拉详情，再回填表单。
   * 这样可以确保你拿到的是数据库最新数据。
   */
  try {
    const detail = await getStudioArticleDetail(id)
    form.id = detail.id
    form.title = detail.title
    form.summary = detail.summary ?? ''
    form.content = detail.content
  } catch (error) {
    submitError.value = error instanceof Error ? error.message : '加载编辑数据失败'
  }
}

async function handleDelete(id: number) {
  /*
   * 交互设计：
   * - 删除前二次确认，避免误操作
   * - 删除后刷新列表，保证页面状态与后端一致
   */
  const confirmed = window.confirm('确认删除这篇文章吗？此操作不可恢复。')
  if (!confirmed) return

  try {
    await deleteStudioArticle(id)
    await loadArticleList()
  } catch (error) {
    listError.value = error instanceof Error ? error.message : '删除失败'
  }
}

onMounted(loadArticleList)
</script>

<template>
  <DefaultLayout>
    <section class="py-12 sm:py-16">
      <div class="container mx-auto px-4 sm:px-6 lg:px-8">
        <h1 class="text-3xl font-bold tracking-tight text-slate-900 sm:text-4xl">Studio · Personal Blog Editor</h1>
        <p class="mt-3 text-sm text-slate-600">
          这是个人博客写作后台：在一个页面里完成新增、编辑、删除和列表查看。
        </p>

        <div class="mt-8 grid grid-cols-1 gap-6 lg:grid-cols-2">
          <section class="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
            <h2 class="text-xl font-semibold text-slate-900">
              {{ form.id === null ? '创建文章' : `编辑文章 #${form.id}` }}
            </h2>

            <form class="mt-5 space-y-4" @submit.prevent="handleSubmit">
              <div>
                <label class="mb-2 block text-sm font-medium text-slate-700">标题（title）</label>
                <input
                  v-model="form.title"
                  type="text"
                  required
                  maxlength="100"
                  class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm outline-none ring-blue-500 focus:ring-2"
                />
              </div>

              <div>
                <label class="mb-2 block text-sm font-medium text-slate-700">摘要（summary）</label>
                <textarea
                  v-model="form.summary"
                  rows="3"
                  maxlength="300"
                  class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm outline-none ring-blue-500 focus:ring-2"
                />
              </div>

              <div>
                <label class="mb-2 block text-sm font-medium text-slate-700">正文（content）</label>
                <textarea
                  v-model="form.content"
                  rows="10"
                  required
                  class="w-full rounded-lg border border-slate-300 px-3 py-2 text-sm outline-none ring-blue-500 focus:ring-2"
                />
              </div>

              <p v-if="submitError" class="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
                {{ submitError }}
              </p>

              <div class="flex items-center gap-3">
                <button
                  type="submit"
                  :disabled="isSubmitting"
                  class="rounded-lg bg-slate-900 px-4 py-2 text-sm font-medium text-white disabled:opacity-60"
                >
                  {{ isSubmitting ? '提交中...' : form.id === null ? '创建文章' : '保存更新' }}
                </button>

                <button
                  type="button"
                  class="rounded-lg border border-slate-300 px-4 py-2 text-sm font-medium text-slate-700"
                  @click="resetForm"
                >
                  重置表单
                </button>
              </div>
            </form>
          </section>

          <section class="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
            <h2 class="text-xl font-semibold text-slate-900">文章列表</h2>

            <p v-if="isLoadingList" class="mt-4 text-sm text-slate-500">加载中...</p>
            <p v-else-if="listError" class="mt-4 rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
              {{ listError }}
            </p>
            <ul v-else class="mt-4 space-y-3">
              <li
                v-for="item in articles"
                :key="item.id"
                class="rounded-xl border border-slate-200 p-4"
              >
                <h3 class="font-semibold text-slate-900">{{ item.title }}</h3>
                <p class="mt-1 text-xs text-slate-500">ID: {{ item.id }}</p>
                <p class="mt-2 text-sm text-slate-600">{{ item.summary || '暂无摘要' }}</p>

                <div class="mt-3 flex items-center gap-3">
                  <button
                    class="rounded-md bg-blue-600 px-3 py-1.5 text-xs font-medium text-white"
                    @click="handleEdit(item.id)"
                  >
                    编辑
                  </button>
                  <button
                    class="rounded-md bg-red-600 px-3 py-1.5 text-xs font-medium text-white"
                    @click="handleDelete(item.id)"
                  >
                    删除
                  </button>
                </div>
              </li>
            </ul>
          </section>
        </div>
      </div>
    </section>
  </DefaultLayout>
</template>
