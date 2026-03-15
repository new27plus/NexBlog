<script setup lang="ts">
// 复用后台统一外壳，提供页面标题和左侧主导航
import StudioLayout from '@/layouts/StudioLayout.vue'
// 使用 Lucide 图标作为按钮图标，避免文字按钮过于密集
import { Plus, Save, Pencil, Trash2, ChevronUp, ChevronDown, X } from 'lucide-vue-next'
// Vue 组合式 API：onMounted 处理初始化，reactive/ref 管理响应式状态
import { onMounted, reactive, ref } from 'vue'

import {
  getStudioPersonalConfig,
  updatePersonalConfig,
  type PersonalItem,
  type SectionKey,
} from '@/api/personal'

// 浏览器本地草稿存储 key
const STORAGE_KEY = 'nexblog:personal:content:draft'

// 分区显示顺序，模板渲染和恢复数据都依赖它
const sectionOrder: SectionKey[] = ['intro', 'links', 'topics', 'updates']

// 分区元信息：导航标题和说明文案
const sectionMeta: Record<SectionKey, { title: string; description: string }> = {
  intro: { title: '页面开场', description: '用于首页顶部的自我介绍语气和身份定位' },
  links: { title: '常用链接', description: '展示你希望访客优先访问的入口' },
  topics: { title: '关注方向', description: '告诉读者你长期关注和持续输出的主题' },
  updates: { title: '最近动态', description: '发布你近期在做的内容或计划中的更新' },
}

// 生成本地唯一 ID（时间戳 + 随机串）
function createId() {
  return `${Date.now()}-${Math.random().toString(36).slice(2, 9)}`
}

// 按分区生成默认模板，点击“新增”时直接得到可编辑草稿
function createDefaultItem(section: SectionKey): PersonalItem {
  // 基础空模板，下面会按分区覆盖具体文案
  const base = {
    id: createId(),
    title: '',
    subtitle: '',
    content: '',
    tag: '',
    linkLabel: '',
    linkUrl: '',
  }

  // 页面开场模板
  if (section === 'intro') {
    return {
      ...base,
      title: '你好，我是你的名字',
      subtitle: '独立开发者 / 内容创作者',
      content: '这里写一段你想让访客第一眼看到的介绍。',
      tag: '开场',
    }
  }

  // 常用链接模板
  if (section === 'links') {
    return {
      ...base,
      title: '我的 GitHub',
      subtitle: '代码与项目',
      content: '这里一句话说明这个链接为什么值得点击。',
      tag: '链接',
      linkLabel: '访问 GitHub',
      linkUrl: 'https://github.com/your-name',
    }
  }

  // 关注方向模板
  if (section === 'topics') {
    return {
      ...base,
      title: '我正在研究的方向',
      subtitle: '例如：AI 应用 / 前端工程化',
      content: '写你关注这个方向的原因，以及你想做出的内容。',
      tag: '主题',
    }
  }

  // 默认走“最近动态”模板
  return {
    ...base,
    title: '最近更新',
    subtitle: '2026.03',
    content: '写最近发布了什么、完成了什么，或下个阶段打算做什么。',
    tag: '动态',
  }
}

// 创建条目副本，避免引用同一对象导致编辑时直接污染展示态
function cloneItem(item: PersonalItem): PersonalItem {
  return {
    id: item.id,
    title: item.title,
    subtitle: item.subtitle,
    content: item.content,
    tag: item.tag,
    linkLabel: item.linkLabel,
    linkUrl: item.linkUrl,
  }
}

// 所有分区的响应式数据源
const sections = reactive<Record<SectionKey, PersonalItem[]>>({
  intro: [createDefaultItem('intro')],
  links: [createDefaultItem('links')],
  topics: [createDefaultItem('topics')],
  updates: [createDefaultItem('updates')],
})

// 当前高亮的分区（用于右侧导航样式）
const activeSection = ref<SectionKey>('intro')
// 当前正在编辑的分区
const editingSection = ref<SectionKey | ''>('')
// 当前正在编辑的条目 ID
const editingItemId = ref('')
// 编辑草稿对象：输入框都绑定到它，点“保存当前项”后再落回列表
const editingDraft = reactive<PersonalItem>({
  id: '',
  title: '',
  subtitle: '',
  content: '',
  tag: '',
  linkLabel: '',
  linkUrl: '',
})
// 草稿保存成功提示
const saveMessage = ref('')
// 草稿保存失败提示
const saveError = ref('')
const isLoadingConfig = ref(false)
const isSavingConfig = ref(false)

// 跳转并高亮分区
function jumpToSection(section: SectionKey) {
  activeSection.value = section
  document.getElementById(`section-${section}`)?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

// 进入编辑态：记录“编辑的是哪一条”，并把该条拷贝到编辑草稿
function startEdit(section: SectionKey, item: PersonalItem) {
  editingSection.value = section
  editingItemId.value = item.id
  Object.assign(editingDraft, cloneItem(item))
}

// 取消编辑：清理编辑态和草稿缓存
function cancelEdit() {
  editingSection.value = ''
  editingItemId.value = ''
  Object.assign(editingDraft, {
    id: '',
    title: '',
    subtitle: '',
    content: '',
    tag: '',
    linkLabel: '',
    linkUrl: '',
  })
}

// 保存当前编辑项：把编辑草稿写回目标列表项
function saveEdit() {
  if (!editingSection.value || !editingItemId.value) return
  const list = sections[editingSection.value]
  const index = list.findIndex((item) => item.id === editingItemId.value)
  if (index === -1) return
  list[index] = cloneItem(editingDraft)
  cancelEdit()
}

// 新增条目：按分区注入默认模板并立即进入编辑
function addItem(section: SectionKey) {
  const next = createDefaultItem(section)
  sections[section].push(next)
  startEdit(section, next)
  jumpToSection(section)
}

// 删除条目：若删的是当前编辑项，顺带退出编辑态
function removeItem(section: SectionKey, id: string) {
  sections[section] = sections[section].filter((item) => item.id !== id)
  if (editingItemId.value === id && editingSection.value === section) {
    cancelEdit()
  }
}

// 上移/下移条目：通过 splice 重排数组顺序
function moveItem(section: SectionKey, index: number, direction: -1 | 1) {
  const target = index + direction
  if (target < 0 || target >= sections[section].length) return
  const list = sections[section]
  const current = list[index]
  if (!current) return
  list.splice(index, 1)
  list.splice(target, 0, current)
}

function normalizeItem(item: Partial<PersonalItem>): PersonalItem {
  return {
    id: item.id || createId(),
    title: item.title || '',
    subtitle: item.subtitle || '',
    content: item.content || '',
    tag: item.tag || '',
    linkLabel: item.linkLabel || '',
    linkUrl: item.linkUrl || '',
  }
}

function applySectionsFromSource(source: Partial<Record<SectionKey, PersonalItem[]>>) {
  for (const key of sectionOrder) {
    const items = source[key]
    if (!Array.isArray(items)) continue
    sections[key] = items.map((item) => normalizeItem(item))
  }
}

function toPayloadSections(): Record<SectionKey, PersonalItem[]> {
  return {
    intro: sections.intro.map((item) => cloneItem(item)),
    links: sections.links.map((item) => cloneItem(item)),
    topics: sections.topics.map((item) => cloneItem(item)),
    updates: sections.updates.map((item) => cloneItem(item)),
  }
}

// 保存全部分区草稿到 localStorage
async function saveDraft() {
  saveMessage.value = ''
  saveError.value = ''
  isSavingConfig.value = true
  try {
    const payload = { sections: toPayloadSections() }
    await updatePersonalConfig(payload)
    localStorage.setItem(STORAGE_KEY, JSON.stringify(payload.sections))
    saveMessage.value = '已保存到后端'
  } catch (error) {
    saveError.value = error instanceof Error ? error.message : '保存草稿失败'
  } finally {
    isSavingConfig.value = false
  }
}

// 页面初始化时恢复草稿
function restoreDraft() {
  const raw = localStorage.getItem(STORAGE_KEY)
  if (!raw) return
  try {
    const parsed = JSON.parse(raw) as Partial<Record<SectionKey, PersonalItem[]>>
    applySectionsFromSource(parsed)
  } catch {
    saveError.value = '历史草稿解析失败，请重新编辑并保存'
  }
}

async function loadRemoteConfig() {
  isLoadingConfig.value = true
  saveError.value = ''
  try {
    const config = await getStudioPersonalConfig()
    applySectionsFromSource(config.sections)
  } catch (error) {
    restoreDraft()
    saveError.value = error instanceof Error ? error.message : '读取后端配置失败'
  } finally {
    isLoadingConfig.value = false
  }
}

onMounted(() => {
  loadRemoteConfig()
})
</script>

<template>
  <!-- 后台页面统一布局组件 -->
  <StudioLayout title="个人配置" active="personal-config">
    <!-- 主体区域：左侧内容区 + 右侧导航区 -->
    <div class="grid gap-5 lg:grid-cols-[minmax(0,1fr)_18rem]">
      <!-- 左侧：内容分区列表 -->
      <div class="space-y-5">
        <div
          v-if="isLoadingConfig"
          class="rounded-2xl border border-slate-200 bg-white px-4 py-3 text-sm text-slate-500 shadow-sm"
        >
          正在加载后端配置...
        </div>
        <section
          v-for="section in sectionOrder"
          :id="`section-${section}`"
          :key="section"
          class="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm"
        >
          <!-- 分区头：标题说明 + 新增按钮 -->
          <div class="mb-5 flex items-start justify-between gap-4">
            <div>
              <h3 class="text-lg font-semibold text-slate-900">{{ sectionMeta[section].title }}</h3>
              <p class="mt-1 text-sm text-slate-500">{{ sectionMeta[section].description }}</p>
            </div>
            <button
              type="button"
              class="inline-flex items-center gap-2 rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm font-medium text-slate-700 transition hover:border-indigo-300 hover:text-indigo-600"
              @click="addItem(section)"
            >
              <Plus class="h-4 w-4" />
              新增
            </button>
          </div>

          <!-- 空状态：当前分区还没有条目 -->
          <div v-if="sections[section].length === 0" class="rounded-xl border border-dashed border-slate-200 bg-slate-50 p-5 text-sm text-slate-500">
            当前还没有内容，点击右上角新增第一条。
          </div>

          <!-- 非空状态：渲染分区条目 -->
          <div v-else class="space-y-4">
            <article
              v-for="(item, index) in sections[section]"
              :key="item.id"
              class="rounded-xl border border-slate-200 bg-slate-50 p-4"
            >
              <!-- 条目顶部：标签 + 操作按钮 -->
              <div class="mb-3 flex items-center justify-between gap-3">
                <span class="inline-flex items-center rounded-lg bg-indigo-50 px-2.5 py-1 text-xs font-medium text-indigo-600">
                  {{ item.tag || sectionMeta[section].title }}
                </span>
                <div class="flex items-center gap-1">
                  <!-- 上移 -->
                  <button
                    type="button"
                    class="rounded-lg p-2 text-slate-500 transition hover:bg-white hover:text-slate-800"
                    @click="moveItem(section, index, -1)"
                  >
                    <ChevronUp class="h-4 w-4" />
                  </button>
                  <!-- 下移 -->
                  <button
                    type="button"
                    class="rounded-lg p-2 text-slate-500 transition hover:bg-white hover:text-slate-800"
                    @click="moveItem(section, index, 1)"
                  >
                    <ChevronDown class="h-4 w-4" />
                  </button>
                  <!-- 编辑 -->
                  <button
                    type="button"
                    class="rounded-lg p-2 text-slate-500 transition hover:bg-white hover:text-indigo-600"
                    @click="startEdit(section, item)"
                  >
                    <Pencil class="h-4 w-4" />
                  </button>
                  <!-- 删除 -->
                  <button
                    type="button"
                    class="rounded-lg p-2 text-slate-500 transition hover:bg-white hover:text-red-600"
                    @click="removeItem(section, item.id)"
                  >
                    <Trash2 class="h-4 w-4" />
                  </button>
                </div>
              </div>

              <!-- 编辑态：显示输入框 -->
              <div v-if="editingSection === section && editingItemId === item.id" class="space-y-3">
                <input
                  v-model="editingDraft.title"
                  type="text"
                  placeholder="标题"
                  class="w-full rounded-xl border border-slate-200 bg-white px-3 py-2.5 text-sm text-slate-800 outline-none transition focus:border-indigo-500 focus:ring-4 focus:ring-indigo-500/10"
                />
                <input
                  v-model="editingDraft.subtitle"
                  type="text"
                  placeholder="副标题"
                  class="w-full rounded-xl border border-slate-200 bg-white px-3 py-2.5 text-sm text-slate-800 outline-none transition focus:border-indigo-500 focus:ring-4 focus:ring-indigo-500/10"
                />
                <textarea
                  v-model="editingDraft.content"
                  rows="3"
                  placeholder="内容描述"
                  class="w-full rounded-xl border border-slate-200 bg-white px-3 py-2.5 text-sm text-slate-800 outline-none transition focus:border-indigo-500 focus:ring-4 focus:ring-indigo-500/10"
                />
                <div class="grid grid-cols-1 gap-3 md:grid-cols-2">
                  <input
                    v-model="editingDraft.linkLabel"
                    type="text"
                    placeholder="链接文案（可选）"
                    class="w-full rounded-xl border border-slate-200 bg-white px-3 py-2.5 text-sm text-slate-800 outline-none transition focus:border-indigo-500 focus:ring-4 focus:ring-indigo-500/10"
                  />
                  <input
                    v-model="editingDraft.linkUrl"
                    type="text"
                    placeholder="链接地址（可选）"
                    class="w-full rounded-xl border border-slate-200 bg-white px-3 py-2.5 text-sm text-slate-800 outline-none transition focus:border-indigo-500 focus:ring-4 focus:ring-indigo-500/10"
                  />
                </div>
                <div class="flex items-center gap-2">
                  <button
                    type="button"
                    class="inline-flex items-center gap-2 rounded-xl bg-slate-900 px-3 py-2 text-sm font-medium text-white transition hover:bg-indigo-600"
                    @click="saveEdit"
                  >
                    <Save class="h-4 w-4" />
                    保存当前项
                  </button>
                  <button
                    type="button"
                    class="inline-flex items-center gap-2 rounded-xl border border-slate-200 bg-white px-3 py-2 text-sm font-medium text-slate-700 transition hover:border-slate-300"
                    @click="cancelEdit"
                  >
                    <X class="h-4 w-4" />
                    取消
                  </button>
                </div>
              </div>

              <!-- 展示态：只显示内容，不展示输入框 -->
              <div v-else class="space-y-2">
                <h4 class="text-base font-semibold text-slate-900">{{ item.title || '未命名条目' }}</h4>
                <p v-if="item.subtitle" class="text-sm text-slate-600">{{ item.subtitle }}</p>
                <p v-if="item.content" class="text-sm leading-6 text-slate-700">{{ item.content }}</p>
                <a
                  v-if="item.linkUrl"
                  :href="item.linkUrl"
                  target="_blank"
                  rel="noreferrer"
                  class="inline-flex items-center text-sm font-medium text-indigo-600 hover:text-indigo-500"
                >
                  {{ item.linkLabel || item.linkUrl }}
                </a>
              </div>
            </article>
          </div>
        </section>
      </div>

      <!-- 右侧：分区导航和整体保存 -->
      <aside class="h-fit rounded-2xl border border-slate-200 bg-white p-4 shadow-sm lg:sticky lg:top-0">
        <h3 class="px-1 text-sm font-semibold text-slate-900">页面导航</h3>
        <div class="mt-3 space-y-2">
          <button
            v-for="section in sectionOrder"
            :key="section"
            type="button"
            class="flex w-full items-center justify-between rounded-xl px-3 py-2 text-left text-sm transition"
            :class="
              activeSection === section
                ? 'bg-indigo-50 text-indigo-700'
                : 'text-slate-600 hover:bg-slate-50 hover:text-slate-900'
            "
            @click="jumpToSection(section)"
          >
            <span>{{ sectionMeta[section].title }}</span>
            <span class="rounded-md bg-white px-1.5 py-0.5 text-xs text-slate-500">{{ sections[section].length }}</span>
          </button>
        </div>
        <button
          type="button"
          :disabled="isSavingConfig || isLoadingConfig"
          class="mt-4 inline-flex w-full items-center justify-center gap-2 rounded-xl bg-slate-900 px-3 py-2.5 text-sm font-semibold text-white transition hover:bg-indigo-600"
          @click="saveDraft"
        >
          <Save class="h-4 w-4" />
          {{ isSavingConfig ? '保存中...' : '保存到后端' }}
        </button>
        <!-- 保存反馈 -->
        <p v-if="saveMessage" class="mt-3 text-xs text-green-600">{{ saveMessage }}</p>
        <p v-if="saveError" class="mt-3 text-xs text-red-600">{{ saveError }}</p>
      </aside>
    </div>
  </StudioLayout>
</template>

<style scoped></style>
