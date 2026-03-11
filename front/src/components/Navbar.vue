<script setup lang="ts">
import { computed, ref } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { Rocket, Github, Menu, X } from 'lucide-vue-next'

/*
 * 导航栏交互状态：
 * - true: 手机端菜单展开
 * - false: 手机端菜单收起
 */
const isMobileMenuOpen = ref(false)
const currentRoute = useRoute()
const isPortalPage = computed(() => !currentRoute.path.startsWith('/app'))
const navRouteLinks = computed(() =>
  isPortalPage.value
    ? [{ name: '门户首页', to: '/' }]
    : [
        { name: '门户首页', to: '/' },
        { name: '系统文章', to: '/app/articles' },
        { name: '写作后台', to: '/app/studio/articles' },
      ],
)

/*
 * 页面锚点导航：
 * - 使用普通 <a href="#...">
 */
const anchorLinks = [
  { name: 'Features', href: '#features' },
  { name: 'About', href: '#about' },
]
</script>

<template>
  <header class="fixed top-0 w-full z-50 bg-white/80 backdrop-blur-md border-b border-slate-200 dark:bg-slate-900/80 dark:border-slate-800 transition-all duration-300">
    <div class="container mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex items-center justify-between h-16">
        <RouterLink to="/" class="shrink-0 flex items-center gap-2 cursor-pointer">
          <span class="text-2xl font-bold bg-linear-to-r from-blue-600 to-indigo-600 bg-clip-text text-transparent">
            NexBlog
          </span>
          <span class="w-2 h-2 rounded-full bg-blue-500 animate-pulse"></span>
        </RouterLink>

        <nav class="hidden md:flex space-x-8">
          <RouterLink
            v-for="link in navRouteLinks"
            :key="link.name" 
            :to="link.to"
            class="text-slate-600 hover:text-blue-600 font-medium transition-colors duration-200 dark:text-slate-300 dark:hover:text-blue-400"
          >
            {{ link.name }}
          </RouterLink>
          <a
            v-if="isPortalPage"
            v-for="link in anchorLinks"
            :key="`${link.name}-anchor`"
            :href="link.href"
            class="text-slate-600 hover:text-blue-600 font-medium transition-colors duration-200 dark:text-slate-300 dark:hover:text-blue-400"
          >
            {{ link.name }}
          </a>
        </nav>

        <div class="hidden md:flex items-center space-x-4">
          <RouterLink :to="isPortalPage ? '/app/studio/articles' : '/app/articles'" class="flex items-center gap-2 px-4 py-2 bg-slate-900 text-white rounded-lg hover:bg-slate-800 transition-transform active:scale-95 dark:bg-white dark:text-slate-900 font-medium text-sm">
            <Rocket class="w-4 h-4" />
            <span>{{ isPortalPage ? '登录后台' : '进入系统' }}</span>
          </RouterLink>
          
          <a href="#" class="text-slate-500 hover:text-slate-900 dark:text-slate-400 dark:hover:text-white transition-colors">
            <Github class="w-5 h-5" />
          </a>
        </div>

        <div class="md:hidden flex items-center">
          <button @click="isMobileMenuOpen = !isMobileMenuOpen" class="text-slate-600 hover:text-slate-900 dark:text-slate-300">
            <component :is="isMobileMenuOpen ? X : Menu" class="w-6 h-6" />
          </button>
        </div>
      </div>
    </div>

    <div v-show="isMobileMenuOpen" class="md:hidden border-t border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900">
      <div class="px-2 pt-2 pb-3 space-y-1 sm:px-3">
        <RouterLink
          v-for="link in navRouteLinks"
          :key="link.name"
          :to="link.to"
          class="block px-3 py-2 rounded-md text-base font-medium text-slate-700 hover:text-blue-600 hover:bg-slate-50 dark:text-slate-300 dark:hover:bg-slate-800"
          @click="isMobileMenuOpen = false"
        >
          {{ link.name }}
        </RouterLink>
        <a
          v-if="isPortalPage"
          v-for="link in anchorLinks"
          :key="`${link.name}-mobile-anchor`"
          :href="link.href"
          class="block px-3 py-2 rounded-md text-base font-medium text-slate-700 hover:text-blue-600 hover:bg-slate-50 dark:text-slate-300 dark:hover:bg-slate-800"
          @click="isMobileMenuOpen = false"
        >
          {{ link.name }}
        </a>
      </div>
    </div>
  </header>
</template>
