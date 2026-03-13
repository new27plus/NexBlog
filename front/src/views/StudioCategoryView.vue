<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import {
  createStudioCategory,
  deleteStudioCategory,
  getStudioCategories,
  getStudioCategoryDetail,
  updateStudioCategory,
  type CategoryListItem,
} from '@/api/category'

const categories = ref<CategoryListItem[]>([])
const isLoadingList = ref(false)
const listError = ref('')

const form = reactive({
  id: null as number | null,
  name: '',
})

const isSubmitting = ref(false)
const submitError = ref('')

function resetForm() {
  form.id = null
  form.name = ''
  submitError.value = ''
}

function formatDate(value: string) {
  return new Date(value).toLocaleString('zh-CN')
}

async function loadCategoryList() {
  isLoadingList.value = true
  listError.value = ''
  try {
    const pageData = await getStudioCategories({ page: 0, size: 50 })
    categories.value = pageData.content
  } catch (error) {
    listError.value = error instanceof Error ? error.message : '加载分类列表失败'
  } finally {
    isLoadingList.value = false
  }
}

async function handleSubmit() {
  isSubmitting.value = true
  submitError.value = ''
  try {
    if (form.id === null) {
      await createStudioCategory({ name: form.name })
    } else {
      await updateStudioCategory(form.id, { name: form.name })
    }

    resetForm()
    await loadCategoryList()
  } catch (error) {
    submitError.value = error instanceof Error ? error.message : '提交失败'
  } finally {
    isSubmitting.value = false
  }
}

async function handleEdit(id: number) {
  try {
    const detail = await getStudioCategoryDetail(id)
    form.id = detail.id
    form.name = detail.name
  } catch (error) {
    submitError.value = error instanceof Error ? error.message : '加载编辑数据失败'
  }
}

async function handleDelete(id: number) {
  const confirmed = window.confirm('确认删除该分类吗？此操作不可恢复。')
  if (!confirmed) return

  try {
    await deleteStudioCategory(id)
    await loadCategoryList()
  } catch (error) {
    listError.value = error instanceof Error ? error.message : '删除失败'
  }
}

onMounted(loadCategoryList)
</script>

<template>
  <DefaultLayout>
    <section class="py-12 sm:py-16">
      <div class="container mx-auto px-4 sm:px-6 lg:px-8">
        <h1 class="text-3xl font-bold tracking-tight text-slate-900 sm:text-4xl">Studio · 分类管理</h1>
        <p class="mt-3 text-sm text-slate-600">在这里维护博客分类，支持新增、编辑、删除与列表查看。</p>

        <div class="mt-8 grid grid-cols-1 gap-6 lg:grid-cols-2">
          <section class="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
            <h2 class="text-xl font-semibold text-slate-900">
              {{ form.id === null ? '创建分类' : `编辑分类 #${form.id}` }}
            </h2>

            <form class="mt-5 space-y-4" @submit.prevent="handleSubmit">
              <div>
                <label class="mb-2 block text-sm font-medium text-slate-700">分类名称（name）</label>
                <input
                  v-model="form.name"
                  type="text"
                  required
                  maxlength="100"
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
                  {{ isSubmitting ? '提交中...' : form.id === null ? '创建分类' : '保存更新' }}
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
            <h2 class="text-xl font-semibold text-slate-900">分类列表</h2>

            <p v-if="isLoadingList" class="mt-4 text-sm text-slate-500">加载中...</p>
            <p v-else-if="listError" class="mt-4 rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
              {{ listError }}
            </p>
            <ul v-else class="mt-4 space-y-3">
              <li v-for="item in categories" :key="item.id" class="rounded-xl border border-slate-200 p-4">
                <h3 class="font-semibold text-slate-900">{{ item.name }}</h3>
                <p class="mt-1 text-xs text-slate-500">ID: {{ item.id }}</p>
                <p class="mt-2 text-xs text-slate-500">
                  创建：{{ formatDate(item.createdAt) }} · 更新：{{ formatDate(item.updatedAt) }}
                </p>

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
