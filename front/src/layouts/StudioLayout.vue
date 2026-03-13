<script setup lang="ts">
import { computed, ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { FileText, LayoutDashboard, LogOut, PenTool, Rocket, Settings, Tags } from 'lucide-vue-next'
import { clearAuthSession, getAuthUsername } from '@/api/auth'

const props = defineProps<{
  title: string
  active: 'articles' | 'categories' | 'settings'
}>()

const route = useRoute()
const router = useRouter()
const isPublishing = ref(false)
const authUsername = computed(() => getAuthUsername())
const avatarLetter = computed(() => authUsername.value.slice(0, 1).toUpperCase() || 'A')

const navItems = computed(() => [
  {
    key: 'dashboard' as const,
    label: '仪表盘',
    to: '/studio/articles?view=dashboard',
    icon: LayoutDashboard,
    active: props.active === 'articles' && (!route.query.view || route.query.view === 'dashboard'),
  },
  {
            key: 'articles' as const,
            label: '文章管理',
            to: '/studio/articles?view=list',
            icon: FileText,
            active: props.active === 'articles' && (route.query.view === 'list' || route.query.view === 'preview'),
          },
  {
    key: 'articles' as const,
    label: '撰写文章',
    to: '/studio/articles?view=editor',
    icon: PenTool,
    active: props.active === 'articles' && route.query.view === 'editor',
  },
  {
    key: 'categories' as const,
    label: '分类管理',
    to: '/studio/categories',
    icon: Tags,
    active: props.active === 'categories',
  },
])

function backToBlog() {
  router.push('/')
}

function logout() {
  clearAuthSession()
  router.push('/studio/login')
}

function handlePublish() {
  if (isPublishing.value) return
  isPublishing.value = true
  
  // Mock publish process
  setTimeout(() => {
    isPublishing.value = false
    alert('已触发 GitHub Actions 构建发布！')
  }, 2000)
}
</script>

<template>
  <div class="h-screen w-full flex bg-slate-50 overflow-hidden">
    <aside class="w-64 bg-slate-900 text-slate-300 flex flex-col flex-shrink-0 transition-all duration-300 shadow-xl z-20">
      <div class="h-16 flex items-center px-6 border-b border-slate-800 bg-slate-900/50 backdrop-blur-sm">
        <span class="text-xl font-bold text-white tracking-tight flex items-center gap-2">
          <div class="w-8 h-8 rounded-lg bg-indigo-600 flex items-center justify-center text-white font-black">N</div>
          <span>Nex<span class="text-indigo-400">Studio</span></span>
        </span>
      </div>
      
      <nav class="flex-1 p-4 space-y-1 overflow-y-auto">
        <div class="mb-4 px-3 text-xs font-semibold text-slate-500 uppercase tracking-wider">主要功能</div>
        
        <RouterLink
          to="/studio/articles?view=dashboard"
          class="flex items-center gap-3 px-3 py-2.5 rounded-lg font-medium transition-all duration-200 group"
          :class="(props.active === 'articles' && (!route.query.view || route.query.view === 'dashboard')) ? 'bg-indigo-600 text-white shadow-lg shadow-indigo-900/20' : 'hover:bg-slate-800 hover:text-white'"
        >
          <LayoutDashboard class="w-5 h-5 transition-transform group-hover:scale-110" :class="(props.active === 'articles' && (!route.query.view || route.query.view === 'dashboard')) ? 'text-indigo-200' : 'text-slate-400 group-hover:text-white'" />
          仪表盘
        </RouterLink>

        <RouterLink
          to="/studio/articles?view=editor"
          class="flex items-center gap-3 px-3 py-2.5 rounded-lg font-medium transition-all duration-200 group"
          :class="(props.active === 'articles' && route.query.view === 'editor') ? 'bg-indigo-600 text-white shadow-lg shadow-indigo-900/20' : 'hover:bg-slate-800 hover:text-white'"
        >
          <PenTool class="w-5 h-5 transition-transform group-hover:scale-110" :class="(props.active === 'articles' && route.query.view === 'editor') ? 'text-indigo-200' : 'text-slate-400 group-hover:text-white'" />
          撰写文章
        </RouterLink>

        <div class="mt-6 mb-2 px-3 text-xs font-semibold text-slate-500 uppercase tracking-wider">内容管理</div>

        <RouterLink
          to="/studio/articles?view=list"
          class="flex items-center gap-3 px-3 py-2.5 rounded-lg font-medium transition-all duration-200 group"
          :class="(props.active === 'articles' && (route.query.view === 'list' || route.query.view === 'preview')) ? 'bg-indigo-600 text-white shadow-lg shadow-indigo-900/20' : 'hover:bg-slate-800 hover:text-white'"
        >
          <FileText class="w-5 h-5 transition-transform group-hover:scale-110" :class="(props.active === 'articles' && (route.query.view === 'list' || route.query.view === 'preview')) ? 'text-indigo-200' : 'text-slate-400 group-hover:text-white'" />
          文章列表
        </RouterLink>
        
        <RouterLink
          to="/studio/categories"
          class="flex items-center gap-3 px-3 py-2.5 rounded-lg font-medium transition-all duration-200 group"
          :class="props.active === 'categories' ? 'bg-indigo-600 text-white shadow-lg shadow-indigo-900/20' : 'hover:bg-slate-800 hover:text-white'"
        >
          <Tags class="w-5 h-5 transition-transform group-hover:scale-110" :class="props.active === 'categories' ? 'text-indigo-200' : 'text-slate-400 group-hover:text-white'" />
          分类专栏
        </RouterLink>

        <div class="mt-6 mb-2 px-3 text-xs font-semibold text-slate-500 uppercase tracking-wider">系统</div>

        <RouterLink
          to="/studio/system-config"
          class="flex items-center gap-3 px-3 py-2.5 rounded-lg font-medium transition-all duration-200 group"
          :class="props.active === 'settings' ? 'bg-indigo-600 text-white shadow-lg shadow-indigo-900/20' : 'hover:bg-slate-800 hover:text-white'"
        >
          <Settings class="w-5 h-5 transition-transform group-hover:scale-110" :class="props.active === 'settings' ? 'text-indigo-200' : 'text-slate-400 group-hover:text-white'" />
          全局设置
        </RouterLink>
      </nav>

      <div class="p-4 border-t border-slate-800 space-y-2 bg-slate-900/50">
        <button
          class="w-full flex items-center gap-2 text-slate-400 hover:text-white transition-colors px-2 py-1.5 rounded-md hover:bg-slate-800"
          type="button"
          @click="backToBlog"
        >
          <LogOut class="w-4 h-4" />
          <span class="text-sm">返回前台首页</span>
        </button>
        <button
          class="w-full flex items-center gap-2 text-slate-400 hover:text-red-400 transition-colors px-2 py-1.5 rounded-md hover:bg-slate-800"
          type="button"
          @click="logout"
        >
          <LogOut class="w-4 h-4" />
          <span class="text-sm">退出登录</span>
        </button>
      </div>
    </aside>

    <div class="flex-1 flex flex-col overflow-hidden relative bg-slate-50">
      <header class="h-16 bg-white/80 backdrop-blur-md border-b border-slate-200 flex items-center justify-between px-6 flex-shrink-0 z-10 sticky top-0">
        <div class="flex items-center gap-4">
          <h2 class="text-lg font-bold text-slate-800 tracking-tight">{{ title }}</h2>
        </div>
        
        <div class="flex items-center gap-4">
          <button
            class="group relative overflow-hidden bg-slate-900 text-white text-sm font-medium px-4 py-2 rounded-lg hover:bg-slate-800 transition-all duration-200 flex items-center gap-2 shadow-sm border border-slate-700 disabled:opacity-70 disabled:cursor-not-allowed"
            type="button"
            :disabled="isPublishing"
            @click="handlePublish"
          >
            <div class="absolute inset-0 bg-gradient-to-r from-indigo-500 via-purple-500 to-indigo-500 opacity-0 group-hover:opacity-20 transition-opacity duration-500 bg-[length:200%_auto] animate-gradient"></div>
            <Rocket class="w-4 h-4 text-green-400 group-hover:-translate-y-0.5 group-hover:translate-x-0.5 transition-transform" :class="{ 'animate-bounce': isPublishing }" />
            <span>{{ isPublishing ? '正在发布...' : '一键发布至 GitHub' }}</span>
          </button>
          
          <div class="h-6 w-px bg-slate-200 mx-1"></div>
          
          <div class="flex items-center gap-3 pl-2 cursor-pointer hover:bg-slate-100 rounded-full pr-4 py-1 transition-colors">
            <div class="w-8 h-8 rounded-full bg-gradient-to-br from-indigo-500 to-purple-600 flex items-center justify-center text-white font-bold text-sm shadow-sm ring-2 ring-white">
              {{ avatarLetter }}
            </div>
            <div class="hidden md:block">
              <p class="text-sm font-semibold text-slate-700 leading-none">{{ authUsername }}</p>
              <p class="text-[10px] text-slate-500 font-medium mt-0.5 uppercase tracking-wide">Administrator</p>
            </div>
          </div>
        </div>
      </header>
      
      <main class="flex-1 overflow-y-auto p-6 scroll-smooth">
        <div class="w-full max-w-[96%] mx-auto">
          <slot />
        </div>
      </main>
    </div>
  </div>
</template>

<style scoped>
.animate-gradient {
  animation: gradient 3s linear infinite;
}
@keyframes gradient {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}
</style>
