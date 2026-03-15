<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Lock, User, ArrowRight, Loader2 } from 'lucide-vue-next'
import {
  bootstrapStudio,
  fetchBootstrapStatus,
  loginStudio,
  saveAuthSession,
} from '@/api/auth'

const router = useRouter()
const route = useRoute()

const form = reactive({
  username: '',
  password: '',
  confirmPassword: '',
})

const isCheckingBootstrap = ref(true)
const isBootstrapMode = ref(false)
const isSubmitting = ref(false)
const errorMessage = ref('')

onMounted(async () => {
  try {
    const status = await fetchBootstrapStatus()
    isBootstrapMode.value = status.bootstrapRequired
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '无法获取系统初始化状态'
  } finally {
    isCheckingBootstrap.value = false
  }
})

async function handleSubmit() {
  if (isCheckingBootstrap.value) {
    return
  }
  isSubmitting.value = true
  errorMessage.value = ''
  try {
    if (!form.username.trim() || !form.password.trim()) {
      errorMessage.value = '请输入用户名和密码'
      return
    }

    if (isBootstrapMode.value && form.password !== form.confirmPassword) {
      errorMessage.value = '两次输入的密码不一致'
      return
    }

    const session = isBootstrapMode.value
      ? await bootstrapStudio(form.username.trim(), form.password, form.confirmPassword)
      : await loginStudio(form.username.trim(), form.password)
    saveAuthSession(session)

    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/studio/articles'
    await router.push(redirect)
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '登录失败'
  } finally {
    isSubmitting.value = false
  }
}
</script>

<template>
  <div class="min-h-screen w-full flex items-center justify-center bg-slate-900 relative overflow-hidden">
    <!-- Background Effects -->
    <div class="absolute top-0 left-0 w-full h-full overflow-hidden z-0">
      <div class="absolute top-[-10%] left-[-10%] w-[40%] h-[40%] rounded-full bg-indigo-600/20 blur-[120px]"></div>
      <div class="absolute bottom-[-10%] right-[-10%] w-[40%] h-[40%] rounded-full bg-purple-600/20 blur-[120px]"></div>
    </div>

    <div class="w-full max-w-md p-8 relative z-10">
      <div class="bg-white/5 backdrop-blur-xl border border-white/10 rounded-2xl shadow-2xl p-8">
        <div class="text-center mb-8">
          <div class="w-16 h-16 bg-gradient-to-br from-indigo-500 to-purple-600 rounded-xl mx-auto flex items-center justify-center shadow-lg mb-4">
            <span class="text-3xl font-bold text-white">N</span>
          </div>
          <h1 class="text-2xl font-bold text-white tracking-tight">NexBlog Studio</h1>
          <p class="mt-2 text-sm text-slate-400">
            {{ isBootstrapMode ? '首次运行：请初始化管理员账号' : '请使用管理员账号登录后台系统' }}
          </p>
        </div>

        <form class="space-y-5" @submit.prevent="handleSubmit">
          <div class="space-y-1">
            <label class="block text-xs font-medium text-slate-400 uppercase tracking-wider pl-1">用户名</label>
            <div class="relative group">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-500 group-focus-within:text-indigo-400 transition-colors">
                <User class="w-5 h-5" />
              </div>
              <input
                v-model="form.username"
                type="text"
                autocomplete="username"
                required
                class="w-full bg-slate-800/50 border border-slate-700 text-white rounded-xl pl-10 pr-4 py-3 outline-none focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500 transition-all placeholder:text-slate-600"
                placeholder="请输入用户名"
              />
            </div>
          </div>

          <div v-if="isBootstrapMode" class="space-y-1">
            <label class="block text-xs font-medium text-slate-400 uppercase tracking-wider pl-1">确认密码</label>
            <div class="relative group">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-500 group-focus-within:text-indigo-400 transition-colors">
                <Lock class="w-5 h-5" />
              </div>
              <input
                v-model="form.confirmPassword"
                type="password"
                autocomplete="new-password"
                required
                class="w-full bg-slate-800/50 border border-slate-700 text-white rounded-xl pl-10 pr-4 py-3 outline-none focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500 transition-all placeholder:text-slate-600"
                placeholder="请再次输入密码"
              />
            </div>
          </div>

          <div class="space-y-1">
            <label class="block text-xs font-medium text-slate-400 uppercase tracking-wider pl-1">密码</label>
            <div class="relative group">
              <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-500 group-focus-within:text-indigo-400 transition-colors">
                <Lock class="w-5 h-5" />
              </div>
              <input
                v-model="form.password"
                type="password"
                autocomplete="current-password"
                required
                class="w-full bg-slate-800/50 border border-slate-700 text-white rounded-xl pl-10 pr-4 py-3 outline-none focus:ring-2 focus:ring-indigo-500/50 focus:border-indigo-500 transition-all placeholder:text-slate-600"
                placeholder="请输入密码"
              />
            </div>
          </div>

          <div v-if="errorMessage" class="rounded-lg bg-red-500/10 border border-red-500/20 px-4 py-3 flex items-start gap-3">
            <div class="text-red-400 mt-0.5">
              <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7 4a1 1 0 11-2 0 1 1 0 012 0zm-1-9a1 1 0 00-1 1v4a1 1 0 102 0V6a1 1 0 00-1-1z" clip-rule="evenodd" />
              </svg>
            </div>
            <p class="text-sm text-red-400">{{ errorMessage }}</p>
          </div>

          <button
            type="submit"
            :disabled="isSubmitting || isCheckingBootstrap"
            class="w-full bg-indigo-600 hover:bg-indigo-500 text-white rounded-xl px-4 py-3.5 font-semibold transition-all duration-200 flex items-center justify-center gap-2 shadow-lg shadow-indigo-900/30 hover:shadow-indigo-900/50 hover:-translate-y-0.5 disabled:opacity-70 disabled:cursor-not-allowed disabled:hover:translate-y-0"
          >
            <Loader2 v-if="isSubmitting" class="w-5 h-5 animate-spin" />
            <Loader2 v-else-if="isCheckingBootstrap" class="w-5 h-5 animate-spin" />
            <span v-else>{{ isBootstrapMode ? '初始化并进入后台' : '登录后台' }}</span>
            <ArrowRight v-if="!isSubmitting && !isCheckingBootstrap" class="w-4 h-4" />
          </button>
        </form>
        
        <div class="mt-6 text-center">
          <RouterLink to="/" class="text-sm text-slate-500 hover:text-indigo-400 transition-colors">
            ← 返回博客首页
          </RouterLink>
        </div>
      </div>
      
      <p class="text-center text-slate-600 text-xs mt-8">
        &copy; {{ new Date().getFullYear() }} NexBlog Studio. All rights reserved.
      </p>
    </div>
  </div>
</template>
