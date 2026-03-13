<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { Layers, Tags, Loader2, Check, X, AlertCircle, Search, Plus, Trash2, Edit2, Sparkles, Bot } from 'lucide-vue-next'
import StudioLayout from '@/layouts/StudioLayout.vue'
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
const keyword = ref('')

// Toast Notification State
const showToast = ref(false)
const toastMessage = ref('')
const toastType = ref<'success' | 'error'>('success')

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
  form.name = ''
  submitError.value = ''
}

function formatDate(value: string) {
  return new Date(value).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

async function loadCategoryList() {
  isLoadingList.value = true
  listError.value = ''
  try {
    const pageData = await getStudioCategories({ page: 0, size: 50 })
    categories.value = pageData.content
  } catch (error) {
    listError.value = error instanceof Error ? error.message : '加载分类列表失败'
    showNotification('加载分类列表失败', 'error')
  } finally {
    isLoadingList.value = false
  }
}

async function handleSubmit() {
  if (!form.name.trim()) {
    submitError.value = '分类名称不能为空'
    return
  }

  isSubmitting.value = true
  submitError.value = ''
  try {
    if (form.id === null) {
      await createStudioCategory({ name: form.name })
      showNotification('分类创建成功')
    } else {
      await updateStudioCategory(form.id, { name: form.name })
      showNotification('分类更新成功')
    }

    resetForm()
    await loadCategoryList()
  } catch (error) {
    const msg = error instanceof Error ? error.message : '提交失败'
    submitError.value = msg
    showNotification(msg, 'error')
  } finally {
    isSubmitting.value = false
  }
}

async function handleEdit(id: number) {
  try {
    const detail = await getStudioCategoryDetail(id)
    form.id = detail.id
    form.name = detail.name
    // Scroll to form on mobile
    window.scrollTo({ top: 0, behavior: 'smooth' })
  } catch (error) {
    const msg = error instanceof Error ? error.message : '加载编辑数据失败'
    submitError.value = msg
    showNotification(msg, 'error')
  }
}

async function handleDelete(id: number) {
  const confirmed = window.confirm('确认删除该分类吗？此操作不可恢复。')
  if (!confirmed) return

  try {
    await deleteStudioCategory(id)
    showNotification('分类删除成功')
    await loadCategoryList()
  } catch (error) {
    const msg = error instanceof Error ? error.message : '删除失败'
    listError.value = msg
    showNotification(msg, 'error')
  }
}

const filteredCategories = computed(() => {
  const query = keyword.value.trim().toLowerCase()
  if (!query) return categories.value
  return categories.value.filter((item) => item.name.toLowerCase().includes(query))
})

onMounted(loadCategoryList)
</script>

<template>
  <StudioLayout title="分类 / 标签" active="categories">
    <!-- Toast Notification -->
    <Transition
      enter-active-class="transform ease-out duration-300 transition"
      enter-from-class="translate-y-2 opacity-0 sm:translate-y-0 sm:translate-x-2"
      enter-to-class="translate-y-0 opacity-100 sm:translate-x-0"
      leave-active-class="transition ease-in duration-100"
      leave-from-class="opacity-100"
      leave-to-class="opacity-0"
    >
      <div 
        v-if="showToast" 
        class="fixed top-6 right-6 z-50 px-6 py-4 rounded-xl shadow-2xl flex items-center gap-3 border backdrop-blur-md"
        :class="toastType === 'success' ? 'bg-white/90 border-green-100 text-green-700' : 'bg-white/90 border-red-100 text-red-700'"
      >
        <div 
          class="w-8 h-8 rounded-full flex items-center justify-center shrink-0 shadow-sm"
          :class="toastType === 'success' ? 'bg-green-100' : 'bg-red-100'"
        >
          <component :is="toastType === 'success' ? Check : AlertCircle" class="w-4 h-4" />
        </div>
        <div>
          <p class="font-bold text-sm">{{ toastType === 'success' ? '操作成功' : '操作失败' }}</p>
          <p class="text-xs opacity-80">{{ toastMessage }}</p>
        </div>
      </div>
    </Transition>

    <!-- Stats Cards -->
    <div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
      <div class="group bg-white p-6 rounded-2xl border border-slate-100 shadow-sm hover:shadow-md transition-all duration-300 relative overflow-hidden">
        <div class="absolute top-0 right-0 p-4 opacity-5 group-hover:opacity-10 transition-opacity transform group-hover:scale-110 duration-500">
          <Tags class="w-24 h-24 text-indigo-600" />
        </div>
        <div class="relative z-10 flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-slate-500 mb-1">分类总数</p>
            <p class="text-3xl font-bold text-slate-900">{{ categories.length }}</p>
          </div>
          <div class="w-12 h-12 bg-indigo-50 text-indigo-600 rounded-2xl flex items-center justify-center group-hover:scale-110 transition-transform duration-300 shadow-sm">
            <Tags class="w-6 h-6" />
          </div>
        </div>
      </div>
      
      <div class="group bg-white p-6 rounded-2xl border border-slate-100 shadow-sm hover:shadow-md transition-all duration-300 relative overflow-hidden">
        <div class="absolute top-0 right-0 p-4 opacity-5 group-hover:opacity-10 transition-opacity transform group-hover:scale-110 duration-500">
          <Layers class="w-24 h-24 text-blue-600" />
        </div>
        <div class="relative z-10 flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-slate-500 mb-1">已命名分类</p>
            <p class="text-3xl font-bold text-slate-900">{{ categories.filter(item => item.name.trim().length > 0).length }}</p>
          </div>
          <div class="w-12 h-12 bg-blue-50 text-blue-600 rounded-2xl flex items-center justify-center group-hover:scale-110 transition-transform duration-300 shadow-sm">
            <Layers class="w-6 h-6" />
          </div>
        </div>
      </div>
      
      <div class="group bg-white p-6 rounded-2xl border border-slate-100 shadow-sm hover:shadow-md transition-all duration-300 relative overflow-hidden">
        <div class="absolute top-0 right-0 p-4 opacity-5 group-hover:opacity-10 transition-opacity transform group-hover:scale-110 duration-500">
          <Sparkles class="w-24 h-24 text-amber-600" />
        </div>
        <div class="relative z-10 flex items-center justify-between">
          <div>
            <p class="text-sm font-medium text-slate-500 mb-1">最近更新时间</p>
            <p class="text-base font-bold text-slate-900">{{ categories[0] ? formatDate(categories[0].updatedAt) : '-' }}</p>
          </div>
          <div class="w-12 h-12 bg-amber-50 text-amber-600 rounded-2xl flex items-center justify-center group-hover:scale-110 transition-transform duration-300 shadow-sm">
            <Sparkles class="w-6 h-6" />
          </div>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-1 gap-6 lg:grid-cols-3">
      <!-- Form Section -->
      <section class="h-fit rounded-2xl border border-slate-100 bg-white p-6 shadow-sm hover:shadow-md transition-shadow duration-300 lg:col-span-1 sticky top-6">
        <div class="flex items-center gap-3 mb-6">
          <div class="w-10 h-10 rounded-xl bg-indigo-50 flex items-center justify-center text-indigo-600">
            <component :is="form.id === null ? Plus : Edit2" class="w-5 h-5" />
          </div>
          <h3 class="text-lg font-bold text-slate-900">
            {{ form.id === null ? '创建分类' : `编辑分类 #${form.id}` }}
          </h3>
        </div>
        
        <form class="space-y-5" @submit.prevent="handleSubmit">
          <div>
            <label class="mb-2 block text-sm font-medium text-slate-700">分类名称</label>
            <div class="relative">
              <input
                v-model="form.name"
                type="text"
                required
                maxlength="100"
                placeholder="输入分类名称..."
                class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition-all focus:border-indigo-500 focus:bg-white focus:ring-4 focus:ring-indigo-500/10"
              />
            </div>
          </div>
          
          <Transition
            enter-active-class="transition-all duration-300 ease-out"
            enter-from-class="opacity-0 -translate-y-2"
            enter-to-class="opacity-100 translate-y-0"
            leave-active-class="transition-all duration-200 ease-in"
            leave-from-class="opacity-100 translate-y-0"
            leave-to-class="opacity-0 -translate-y-2"
          >
            <div v-if="submitError" class="rounded-xl border border-red-100 bg-red-50 p-4 flex items-start gap-3">
              <AlertCircle class="w-5 h-5 text-red-600 shrink-0 mt-0.5" />
              <p class="text-sm text-red-600 font-medium">{{ submitError }}</p>
            </div>
          </Transition>

          <div class="flex items-center gap-3 pt-2">
            <button
              type="submit"
              :disabled="isSubmitting"
              class="flex-1 rounded-xl bg-slate-900 hover:bg-indigo-600 px-4 py-3 text-sm font-bold text-white transition-all duration-300 shadow-lg shadow-slate-900/20 hover:shadow-indigo-600/30 hover:-translate-y-0.5 disabled:opacity-70 disabled:cursor-not-allowed disabled:hover:translate-y-0 flex items-center justify-center gap-2"
            >
              <Loader2 v-if="isSubmitting" class="w-4 h-4 animate-spin" />
              <span>{{ isSubmitting ? '提交中...' : form.id === null ? '立即创建' : '保存更新' }}</span>
            </button>
            <button
              type="button"
              class="rounded-xl border border-slate-200 hover:border-slate-300 bg-white px-4 py-3 text-sm font-bold text-slate-600 hover:text-slate-800 transition-all duration-200 hover:bg-slate-50"
              @click="resetForm"
            >
              重置
            </button>
          </div>
        </form>
      </section>

      <!-- List Section -->
      <section class="rounded-2xl border border-slate-100 bg-white shadow-sm hover:shadow-md transition-shadow duration-300 overflow-hidden lg:col-span-2 flex flex-col min-h-[500px]">
        <div class="p-5 border-b border-slate-100 flex flex-col gap-4 sm:flex-row sm:justify-between sm:items-center bg-white/50 backdrop-blur-sm sticky top-0 z-10">
          <div class="flex items-center gap-3">
            <div class="w-8 h-8 rounded-lg bg-indigo-50 flex items-center justify-center text-indigo-600">
              <Layers class="w-4 h-4" />
            </div>
            <h3 class="font-bold text-slate-800">分类列表</h3>
            <span class="px-2 py-0.5 rounded-md bg-slate-100 text-xs font-medium text-slate-500">{{ filteredCategories.length }}</span>
          </div>
          <div class="relative group">
            <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400 group-focus-within:text-indigo-500 transition-colors" />
            <input
              v-model="keyword"
              type="text"
              placeholder="搜索分类..."
              class="w-full sm:w-64 text-sm border-slate-200 bg-slate-50 rounded-xl py-2 pl-9 pr-4 outline-none focus:bg-white focus:border-indigo-500 focus:ring-4 focus:ring-indigo-500/10 transition-all"
            />
          </div>
        </div>

        <div v-if="isLoadingList" class="flex-1 flex flex-col items-center justify-center p-12 text-slate-400">
          <Loader2 class="w-8 h-8 animate-spin mb-3 text-indigo-500" />
          <p class="text-sm font-medium">正在加载分类数据...</p>
        </div>

        <div v-else-if="listError" class="flex-1 flex flex-col items-center justify-center p-12 text-red-500">
          <AlertCircle class="w-8 h-8 mb-3" />
          <p class="text-sm font-medium">{{ listError }}</p>
          <button @click="loadCategoryList" class="mt-4 text-indigo-600 hover:underline text-sm">重试</button>
        </div>

        <div v-else-if="filteredCategories.length === 0" class="flex-1 flex flex-col items-center justify-center p-12 text-slate-400">
          <div class="w-16 h-16 bg-slate-50 rounded-full flex items-center justify-center mb-4">
            <Search class="w-8 h-8 text-slate-300" />
          </div>
          <p class="text-sm font-medium text-slate-600">未找到相关分类</p>
          <p class="text-xs mt-1">尝试使用其他关键词搜索或创建一个新分类</p>
        </div>

        <div v-else class="overflow-x-auto">
          <table class="w-full text-left text-sm text-slate-600">
            <thead class="bg-slate-50/80 text-slate-500 border-b border-slate-100">
              <tr>
                <th class="px-6 py-4 font-semibold w-1/3">分类名称</th>
                <th class="px-6 py-4 font-semibold hidden sm:table-cell">创建时间</th>
                <th class="px-6 py-4 font-semibold hidden md:table-cell">更新时间</th>
                <th class="px-6 py-4 font-semibold text-right">操作</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-slate-100">
              <tr v-for="item in filteredCategories" :key="item.id" class="group hover:bg-indigo-50/30 transition-colors duration-200">
                <td class="px-6 py-4">
                  <div class="font-medium text-slate-900 group-hover:text-indigo-700 transition-colors">{{ item.name }}</div>
                  <div class="text-xs text-slate-400 mt-0.5 sm:hidden">{{ formatDate(item.updatedAt) }}</div>
                </td>
                <td class="px-6 py-4 hidden sm:table-cell font-mono text-xs text-slate-500">{{ formatDate(item.createdAt) }}</td>
                <td class="px-6 py-4 hidden md:table-cell font-mono text-xs text-slate-500">{{ formatDate(item.updatedAt) }}</td>
                <td class="px-6 py-4 text-right">
                  <div class="flex items-center justify-end gap-2 opacity-100 sm:opacity-0 sm:group-hover:opacity-100 transition-opacity">
                    <button 
                      class="p-1.5 rounded-lg text-slate-500 hover:text-indigo-600 hover:bg-indigo-50 transition-all"
                      title="编辑"
                      @click="handleEdit(item.id)"
                    >
                      <Edit2 class="w-4 h-4" />
                    </button>
                    <button 
                      class="p-1.5 rounded-lg text-slate-500 hover:text-red-600 hover:bg-red-50 transition-all"
                      title="删除"
                      @click="handleDelete(item.id)"
                    >
                      <Trash2 class="w-4 h-4" />
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </div>
  </StudioLayout>
</template>
