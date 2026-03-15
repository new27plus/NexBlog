<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { RouterLink } from 'vue-router'
import { Menu, X, Github } from 'lucide-vue-next'

const isMobileMenuOpen = ref(false)
const isScrolled = ref(false)
// 导航栏的路由链接
const navRouteLinks = [
  { name: '博客首页', to: '/' },
  { name: '技术文章', to: '/articles' },
  { name: '关于作者', to: '/about' },
]

function handleScroll() {
  isScrolled.value = window.scrollY > 20
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
})

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<template>
  <header 
    class="fixed top-0 w-full z-50 transition-all duration-300 border-b"
    :class="[
      isScrolled || isMobileMenuOpen 
        ? 'bg-white/80 backdrop-blur-md border-slate-200 dark:bg-slate-900/80 dark:border-slate-800 shadow-sm' 
        : 'bg-transparent border-transparent'
    ]"
  >
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
      <div class="flex items-center justify-between h-16">
        <!-- Logo -->
        <RouterLink to="/" class="shrink-0 flex items-center gap-2.5 group">
          <div class="w-8 h-8 rounded-xl bg-linear-to-br from-indigo-500 to-blue-600 flex items-center justify-center text-white shadow-lg shadow-indigo-500/20 group-hover:scale-110 transition-transform duration-300">
            <span class="font-bold text-lg">N</span>
          </div>
          <span class="text-xl font-bold bg-clip-text text-transparent bg-linear-to-r from-slate-900 to-slate-700 dark:from-white dark:to-slate-300">
            NexBlog
          </span>
        </RouterLink>

        <!-- Desktop Navigation -->
        <nav class="hidden md:flex items-center space-x-1">
          <RouterLink
            v-for="link in navRouteLinks"
            :key="link.name" 
            :to="link.to"
            class="px-4 py-2 rounded-lg text-sm font-medium text-slate-600 hover:text-indigo-600 hover:bg-indigo-50 transition-all duration-200 dark:text-slate-300 dark:hover:text-white dark:hover:bg-slate-800"
            active-class="text-indigo-600 bg-indigo-50 dark:bg-slate-800 dark:text-white"
          >
            {{ link.name }}
          </RouterLink>
        </nav>

        <!-- Right Side Actions -->
        <div class="hidden md:flex items-center gap-3">
          <a 
            href="https://github.com" 
            target="_blank" 
            class="p-2 text-slate-500 hover:text-slate-900 hover:bg-slate-100 rounded-lg transition-colors dark:text-slate-400 dark:hover:text-white dark:hover:bg-slate-800"
            title="GitHub"
          >
            <Github class="w-5 h-5" />
          </a>
          
          <div class="h-4 w-px bg-slate-200 dark:bg-slate-700 mx-1"></div>

        </div>

        <!-- Mobile Menu Button -->
        <div class="md:hidden flex items-center">
          <button 
            @click="isMobileMenuOpen = !isMobileMenuOpen" 
            class="p-2 rounded-lg text-slate-600 hover:bg-slate-100 dark:text-slate-300 dark:hover:bg-slate-800 transition-colors"
          >
            <component :is="isMobileMenuOpen ? X : Menu" class="w-6 h-6" />
          </button>
        </div>
      </div>
    </div>

    <!-- Mobile Menu -->
    <Transition
      enter-active-class="transition duration-200 ease-out"
      enter-from-class="opacity-0 -translate-y-2"
      enter-to-class="opacity-100 translate-y-0"
      leave-active-class="transition duration-150 ease-in"
      leave-from-class="opacity-100 translate-y-0"
      leave-to-class="opacity-0 -translate-y-2"
    >
      <div v-show="isMobileMenuOpen" class="md:hidden border-t border-slate-200 dark:border-slate-800 bg-white/95 backdrop-blur-md dark:bg-slate-900/95 shadow-xl">
        <div class="px-4 pt-3 pb-6 space-y-2">
          <RouterLink
            v-for="link in navRouteLinks"
            :key="link.name"
            :to="link.to"
            class="block px-4 py-3 rounded-xl text-base font-medium text-slate-600 hover:text-indigo-600 hover:bg-indigo-50 dark:text-slate-300 dark:hover:bg-slate-800 dark:hover:text-white transition-all"
            @click="isMobileMenuOpen = false"
          >
            {{ link.name }}
          </RouterLink>
          
          <div class="h-px bg-slate-100 dark:bg-slate-800 my-2"></div>
        </div>
      </div>
    </Transition>
  </header>
</template>
