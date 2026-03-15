<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import type { Component } from 'vue'
import {
  BookOpen,
  CodeXml,
  Flame,
  Github,
  Globe,
  Linkedin,
  Link2,
  Mail,
  Rss,
  Tv,
} from 'lucide-vue-next'
import DefaultLayout from '@/layouts/DefaultLayout.vue'
import { getPersonalConfig, type PersonalItem } from '@/api/personal'

import markdownit from 'markdown-it'

const isLoading = ref(true)
const errorMessage = ref('')
const introItems = ref<PersonalItem[]>([])
const storyItems = ref<PersonalItem[]>([])
const linksItems = ref<PersonalItem[]>([])
const topicsItems = ref<PersonalItem[]>([])
const tagsItems = ref<PersonalItem[]>([])
const updatesItems = ref<PersonalItem[]>([])

const heroItem = computed(() => introItems.value[0] ?? null)
const pageTitle = computed(() => heroItem.value?.title || '关于作者')
const pageDescription = computed(() => heroItem.value?.content || '暂无个人介绍')
const storyItem = computed(() => storyItems.value[0] ?? null)
const timelineItems = computed(() => updatesItems.value.slice(0, 3))

const md = markdownit({
  html: true,
  linkify: true,
  typographer: true
})

const storyHtml = computed(() => {
  if (!storyItem.value?.content) return ''
  return md.render(storyItem.value.content)
})

const hasAnyContent = computed(() => {
  return (
    introItems.value.length > 0 ||
    storyItems.value.length > 0 ||
    linksItems.value.length > 0 ||
    topicsItems.value.length > 0 ||
    tagsItems.value.length > 0 ||
    updatesItems.value.length > 0
  )
})

const linkIconMap: Record<string, Component> = {
  github: Github,
  linkedin: Linkedin,
  mail: Mail,
  email: Mail,
  globe: Globe,
  website: Globe,
  rss: Rss,
  book: BookOpen,
  article: BookOpen,
  code: CodeXml,
  link: Link2,
  weibo: Flame,
  bilibili: Tv,
}

function resolveLinkIcon(item: PersonalItem) {
  const key = item.linkIcon?.trim().toLowerCase()
  if (key && linkIconMap[key]) {
    return linkIconMap[key]
  }
  return Link2
}

function toParagraphs(text: string) {
  return text
    .split('\n')
    .map(value => value.trim())
    .filter(Boolean)
}

async function loadPersonalConfig() {
  isLoading.value = true
  errorMessage.value = ''
  try {
    const config = await getPersonalConfig()
    introItems.value = config.sections.intro || []
    storyItems.value = config.sections.story || []
    linksItems.value = config.sections.links || []
    topicsItems.value = config.sections.topics || []
    tagsItems.value = config.sections.tags || []
    updatesItems.value = config.sections.updates || []
  } catch (error) {
    errorMessage.value = error instanceof Error ? error.message : '加载个人配置失败'
  } finally {
    isLoading.value = false
  }
}

onMounted(loadPersonalConfig)
</script>

<template>
  <DefaultLayout>
    <section class="pb-20 pt-14 md:pb-24 md:pt-20">
      <div class="mx-auto w-full max-w-5xl space-y-8 px-4 sm:px-6 lg:px-8">
        <header class="overflow-hidden rounded-3xl border border-slate-200 bg-linear-to-br from-white via-slate-50 to-indigo-50 p-8 shadow-sm dark:border-slate-700 dark:from-slate-800 dark:via-slate-800 dark:to-slate-900 md:p-10">
          <p v-if="heroItem?.tag && heroItem.tag !== '开场'" class="inline-flex rounded-full bg-slate-900 px-3 py-1 text-xs font-semibold tracking-wide text-white dark:bg-white dark:text-slate-900">
            {{ heroItem.tag }}
          </p>
          <h1 class="mt-4 text-4xl font-extrabold tracking-tight text-slate-900 dark:text-white md:text-5xl">
            {{ pageTitle }}
          </h1>
          <p class="mt-4 max-w-3xl text-lg leading-8 text-slate-600 dark:text-slate-300">
            {{ pageDescription }}
          </p>
        </header>

        <section v-if="isLoading" class="space-y-4 rounded-2xl border border-slate-200 bg-white p-6 shadow-sm dark:border-slate-700 dark:bg-slate-800">
          <div v-for="item in 4" :key="item" class="h-24 animate-pulse rounded-xl bg-slate-100 dark:bg-slate-700"></div>
        </section>

        <section v-else-if="errorMessage" class="rounded-2xl border border-red-200 bg-red-50 p-6 text-red-700 dark:border-red-900/40 dark:bg-red-950/20 dark:text-red-300">
          <p class="text-base font-semibold">关于页加载失败</p>
          <p class="mt-2 text-sm">{{ errorMessage }}</p>
          <button
            class="mt-4 rounded-lg bg-white px-4 py-2 text-sm font-medium text-red-700 shadow-sm transition hover:bg-red-100 dark:bg-red-900/30 dark:text-red-200 dark:hover:bg-red-900/50"
            @click="loadPersonalConfig"
          >
            重试
          </button>
        </section>

        <section v-else-if="!hasAnyContent" class="rounded-2xl border border-slate-200 bg-white p-6 text-slate-600 shadow-sm dark:border-slate-700 dark:bg-slate-800 dark:text-slate-300">
          暂无可展示的个人信息
        </section>

        <section v-else class="grid gap-6 lg:grid-cols-[minmax(0,1fr)_18rem]">
          <article class="rounded-2xl border border-slate-200 bg-white p-7 shadow-sm dark:border-slate-700 dark:bg-slate-800 md:p-8">
            <section class="prose prose-slate max-w-none dark:prose-invert">
              <h2>我的故事</h2>
              <div v-if="storyHtml" v-html="storyHtml" class="mt-6 space-y-4"></div>
              <p v-else class="text-slate-500">暂无个人故事内容。</p>
            </section>

            <section v-if="topicsItems.length > 0" class="mt-12 pt-8 border-t border-slate-100 dark:border-slate-700/50">
              <h2 class="text-2xl font-bold text-slate-900 dark:text-white mb-6">关注主题</h2>
              <div class="grid gap-4 sm:grid-cols-2">
                <div
                  v-for="item in topicsItems"
                  :key="item.id"
                  class="rounded-2xl bg-slate-50 p-5 border border-slate-100 dark:bg-slate-900 dark:border-slate-700/50"
                >
                  <h3 class="text-lg font-semibold text-slate-900 dark:text-white">
                    <a
                      v-if="item.linkUrl"
                      :href="item.linkUrl"
                      target="_blank"
                      rel="noopener noreferrer"
                      class="hover:text-indigo-600 dark:hover:text-indigo-400 transition-colors"
                    >
                      {{ item.title || '技术主题' }}
                    </a>
                    <span v-else>{{ item.title || '技术主题' }}</span>
                  </h3>
                  <p v-if="item.content" class="mt-2 text-sm leading-relaxed text-slate-600 dark:text-slate-400">
                    {{ item.content }}
                  </p>
                </div>
              </div>
            </section>
          </article>

          <aside class="space-y-6">
            <section v-if="linksItems.length > 0" class="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm dark:border-slate-700 dark:bg-slate-800">
              <h2 class="text-lg font-bold text-slate-900 dark:text-white">在别处找到我</h2>
              <div class="mt-5 flex flex-wrap gap-4">
                <a
                  v-for="item in linksItems"
                  :key="item.id"
                  :href="item.linkUrl || '#'"
                  :target="item.linkUrl ? '_blank' : undefined"
                  rel="noopener noreferrer"
                  :title="item.subtitle || '链接'"
                  class="flex h-11 w-11 items-center justify-center rounded-full bg-slate-50 text-slate-600 transition-all hover:bg-indigo-50 hover:text-indigo-600 hover:scale-110 dark:bg-slate-900 dark:text-slate-400 dark:hover:bg-indigo-900/30 dark:hover:text-indigo-400"
                >
                  <component :is="resolveLinkIcon(item)" class="h-5 w-5" />
                </a>
              </div>
            </section>

            <section v-if="tagsItems.length > 0" class="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm dark:border-slate-700 dark:bg-slate-800">
              <h2 class="text-lg font-bold text-slate-900 dark:text-white">关键词</h2>
              <div class="mt-5 flex flex-wrap gap-2">
                <span
                  v-for="item in tagsItems"
                  :key="`tag-${item.id}`"
                  class="rounded-full border border-slate-200 bg-white px-3.5 py-1.5 text-sm font-medium text-slate-700 shadow-sm dark:border-slate-600 dark:bg-slate-800 dark:text-slate-300"
                >
                  #{{ item.title || '标签' }}
                </span>
              </div>
            </section>

            <section v-if="timelineItems.length > 0" class="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm dark:border-slate-700 dark:bg-slate-800">
              <h2 class="text-lg font-bold text-slate-900 dark:text-white">更新轨迹</h2>
              <div class="mt-6 relative border-l-2 border-slate-100 dark:border-slate-700/50 ml-3 space-y-6">
                <article
                  v-for="item in timelineItems"
                  :key="item.id"
                  class="relative pl-5"
                >
                  <div class="absolute -left-[21px] top-1.5 h-3.5 w-3.5 rounded-full border-[3px] border-indigo-500 bg-white dark:bg-slate-900"></div>
                  <h3 class="text-sm font-semibold text-slate-900 dark:text-white">
                    {{ item.title || '近期更新' }}
                  </h3>
                  <p v-if="item.content" class="mt-1.5 text-sm leading-6 text-slate-600 dark:text-slate-400">
                    {{ item.content }}
                  </p>
                </article>
              </div>
            </section>
          </aside>
        </section>
      </div>
    </section>
  </DefaultLayout>
</template>
