<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { AlertCircle, CheckCircle2, ExternalLink, Loader2, Rocket, Save, ShieldCheck, Upload } from 'lucide-vue-next'
import StudioLayout from '@/layouts/StudioLayout.vue'
import { API_BASE_URL, getLatestPublish, getSystemConfig, preparePublish, releasePublish, upsertSystemConfig, type PublishPrepareResponse } from '@/api/config'

const PUBLISH_SESSION_KEY = 'nexblog:publish:session'

const form = reactive({
  githubOwner: '',
  githubRepo: '',
  publishBranch: 'gh-pages',
  githubToken: '',
  siteBasePath: '/',
})

const isLoading = ref(false)
const isSubmitting = ref(false)
const loadError = ref('')
const submitError = ref('')
const successMessage = ref('')
const tokenConfigured = ref(false)
const isPreparing = ref(false)
const prepareError = ref('')
const prepareMessage = ref('')
const preparedJobId = ref('')
const preparedPreviewPath = ref('')
const preparedArticleCount = ref(0)
const preparedDistPath = ref('')
const isReleasing = ref(false)
const releaseError = ref('')
const releaseErrorDetail = ref('')
const releaseErrorReason = ref('')
const releaseErrorActions = ref<string[]>([])
const releaseMessage = ref('')
const publishedUrl = ref('')

const canSubmit = computed(() => {
  return (
    form.githubOwner.trim().length > 0 &&
    form.githubRepo.trim().length > 0 &&
    form.publishBranch.trim().length > 0 &&
    form.siteBasePath.trim().length > 0 &&
    !isSubmitting.value
  )
})

const manualPublishCommandText = computed(() => {
  const staticPath = preparedDistPath.value || '<静态包目录>'
  const branch = form.publishBranch.trim() || 'gh-pages'
  const owner = form.githubOwner.trim() || '<owner>'
  const repo = form.githubRepo.trim() || '<repo>'
  const repoUrl = `https://github.com/${owner}/${repo}.git`
  return [
    `cd "${staticPath}"`,
    'git init',
    `git checkout -B ${branch}`,
    'git add -A',
    'git commit -m "deploy: manual publish"',
    'git remote remove origin',
    `git remote add origin ${repoUrl}`,
    `git push --force origin ${branch}`,
  ].join('\n')
})

function applyConfig(data: {
  githubOwner: string | null
  githubRepo: string | null
  publishBranch: string | null
  siteBasePath: string | null
  tokenConfigured: boolean
}) {
  form.githubOwner = data.githubOwner ?? ''
  form.githubRepo = data.githubRepo ?? ''
  form.publishBranch = data.publishBranch ?? 'gh-pages'
  form.siteBasePath = data.siteBasePath ?? '/'
  form.githubToken = ''
  tokenConfigured.value = data.tokenConfigured
}

function restorePublishSession() {
  const raw = sessionStorage.getItem(PUBLISH_SESSION_KEY)
  if (!raw) return
  try {
    const parsed = JSON.parse(raw) as {
      jobId?: string
      previewPath?: string
      articleCount?: number
      distPath?: string
      publishedUrl?: string
    }
    preparedJobId.value = parsed.jobId ?? ''
    preparedPreviewPath.value = parsed.previewPath ?? ''
    preparedArticleCount.value = parsed.articleCount ?? 0
    preparedDistPath.value = parsed.distPath ?? ''
    publishedUrl.value = parsed.publishedUrl ?? ''
    if (preparedJobId.value) {
      prepareMessage.value = `静态包已就绪，共导出 ${preparedArticleCount.value} 篇文章`
    }
  } catch {
  }
}

function persistPublishSession() {
  sessionStorage.setItem(
    PUBLISH_SESSION_KEY,
    JSON.stringify({
      jobId: preparedJobId.value,
      previewPath: preparedPreviewPath.value,
      articleCount: preparedArticleCount.value,
      distPath: preparedDistPath.value,
      publishedUrl: publishedUrl.value,
    })
  )
}

function applyPreparedResult(result: PublishPrepareResponse, message: string) {
  preparedJobId.value = result.jobId
  preparedPreviewPath.value = result.previewPath
  preparedArticleCount.value = result.articleCount
  preparedDistPath.value = result.distPath
  prepareMessage.value = message
  persistPublishSession()
}

async function hydrateLatestPrepared() {
  try {
    const latest = await getLatestPublish()
    if (!latest) return
    if (!preparedJobId.value || !preparedPreviewPath.value) {
      applyPreparedResult(latest, `已加载最近一次静态包，共导出 ${latest.articleCount} 篇文章`)
    }
  } catch {
  }
}

async function loadConfig() {
  isLoading.value = true
  loadError.value = ''
  try {
    const data = await getSystemConfig()
    applyConfig(data)
  } catch (error) {
    loadError.value = error instanceof Error ? error.message : '加载系统配置失败'
  } finally {
    isLoading.value = false
  }
}

async function handleSubmit() {
  if (!canSubmit.value) return
  isSubmitting.value = true
  submitError.value = ''
  successMessage.value = ''

  try {
    const saved = await upsertSystemConfig({
      githubOwner: form.githubOwner.trim(),
      githubRepo: form.githubRepo.trim(),
      publishBranch: form.publishBranch.trim(),
      githubToken: form.githubToken.trim(),
      siteBasePath: form.siteBasePath.trim(),
    })
    applyConfig(saved)
    successMessage.value = '配置已保存'
  } catch (error) {
    submitError.value = error instanceof Error ? error.message : '保存配置失败'
  } finally {
    isSubmitting.value = false
  }
}

async function handlePreparePublish() {
  isPreparing.value = true
  prepareError.value = ''
  prepareMessage.value = ''
  releaseError.value = ''
  releaseErrorDetail.value = ''
  releaseErrorReason.value = ''
  releaseErrorActions.value = []
  releaseMessage.value = ''
  publishedUrl.value = ''
  try {
    const result = await preparePublish()
    applyPreparedResult(result, `静态包生成完成，共导出 ${result.articleCount} 篇文章`)
  } catch (error) {
    prepareError.value = error instanceof Error ? error.message : '静态包生成失败'
  } finally {
    isPreparing.value = false
  }
}

function openPreview() {
  if (!preparedPreviewPath.value) return
  const previewPath = preparedPreviewPath.value.endsWith('/index.html')
    ? `${preparedPreviewPath.value.slice(0, -'index.html'.length)}`
    : preparedPreviewPath.value
  window.open(`${API_BASE_URL}${previewPath}`, '_blank')
}

function resolveReleaseErrorGuidance(errorText: string) {
  const normalized = errorText.toLowerCase()
  if (
    normalized.includes('could not connect to server') ||
    normalized.includes('failed to connect') ||
    normalized.includes('unable to access') ||
    normalized.includes('timed out')
  ) {
    return {
      reason: '服务器到 GitHub 的网络连接失败（443 无法连通）',
      actions: [
        '检查后端服务所在机器是否能访问 https://github.com',
        '若在公司网络，配置可用代理并放行 github.com:443',
        '在服务器终端执行 git ls-remote https://github.com/<owner>/<repo>.git 验证连通性',
      ],
    }
  }
  if (normalized.includes('authentication failed') || normalized.includes('bad credentials')) {
    return {
      reason: 'GitHub Token 无效或已过期',
      actions: [
        '重新生成具有 repo 权限的 Personal Access Token',
        '在“全局设置”里重新保存 GitHub Token 后再次发布',
      ],
    }
  }
  if (normalized.includes('repository not found')) {
    return {
      reason: '仓库不存在，或 Token 没有该仓库访问权限',
      actions: [
        '确认 GitHub Owner 和 Repo 名称拼写完全正确',
        '确认目标仓库存在且 Token 对该仓库有读写权限',
      ],
    }
  }
  if (normalized.includes('src refspec') || normalized.includes('remote rejected')) {
    return {
      reason: '推送分支被远端拒绝',
      actions: [
        '检查发布分支名称是否正确（如 gh-pages）',
        '确认仓库规则未阻止该分支的强制推送',
      ],
    }
  }
  return {
    reason: '发布命令执行失败',
    actions: [
      '先查看上方详细日志中的第一条 fatal 报错',
      '根据报错关键字检查网络、Token 权限、仓库名和分支配置',
    ],
  }
}

async function handleReleasePublish() {
  if (!preparedJobId.value) {
    await hydrateLatestPrepared()
    if (!preparedJobId.value) {
      releaseError.value = '请先生成静态包'
      return
    }
  }
  isReleasing.value = true
  releaseError.value = ''
  releaseErrorDetail.value = ''
  releaseErrorReason.value = ''
  releaseErrorActions.value = []
  releaseMessage.value = ''
  try {
    const result = await releasePublish({ jobId: preparedJobId.value })
    publishedUrl.value = result.publishedUrl
    releaseMessage.value = `发布完成，提交 ${result.commitId.slice(0, 8)}，分支 ${result.branch}`
    persistPublishSession()
  } catch (error) {
    const rawMessage = error instanceof Error ? error.message : '发布到 GitHub 失败'
    const [title, ...detailLines] = rawMessage.split('\n')
    releaseError.value = title || '发布到 GitHub 失败'
    releaseErrorDetail.value = detailLines.join('\n').trim()
    const guidance = resolveReleaseErrorGuidance(rawMessage)
    releaseErrorReason.value = guidance.reason
    releaseErrorActions.value = guidance.actions
  } finally {
    isReleasing.value = false
  }
}

onMounted(async () => {
  restorePublishSession()
  await Promise.all([loadConfig(), hydrateLatestPrepared()])
})
</script>

<template>
  <StudioLayout title="全局设置" active="settings">
    <div class="grid grid-cols-1 gap-6 lg:grid-cols-3">
      <section class="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm lg:col-span-2">
        <div class="mb-6 flex items-center gap-3">
          <div class="flex h-10 w-10 items-center justify-center rounded-xl bg-indigo-50 text-indigo-600">
            <ShieldCheck class="h-5 w-5" />
          </div>
          <div>
            <h3 class="text-lg font-bold text-slate-900">GitHub Pages 配置</h3>
            <p class="text-sm text-slate-500">保存后用于一键发布静态博客</p>
          </div>
        </div>

        <div v-if="isLoading" class="rounded-xl border border-slate-200 bg-slate-50 p-4 text-sm text-slate-600">
          正在加载配置...
        </div>

        <div v-else-if="loadError" class="rounded-xl border border-red-200 bg-red-50 p-4 text-sm text-red-700">
          {{ loadError }}
        </div>

        <form v-else class="space-y-5" @submit.prevent="handleSubmit">
          <div>
            <label class="mb-2 block text-sm font-medium text-slate-700">GitHub Owner</label>
            <input
              v-model="form.githubOwner"
              type="text"
              required
              placeholder="例如：your-name"
              class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-indigo-500 focus:bg-white focus:ring-4 focus:ring-indigo-500/10"
            />
          </div>

          <div>
            <label class="mb-2 block text-sm font-medium text-slate-700">GitHub Repo</label>
            <input
              v-model="form.githubRepo"
              type="text"
              required
              placeholder="例如：nexblog-pages"
              class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-indigo-500 focus:bg-white focus:ring-4 focus:ring-indigo-500/10"
            />
          </div>

          <div class="grid grid-cols-1 gap-4 md:grid-cols-2">
            <div>
              <label class="mb-2 block text-sm font-medium text-slate-700">发布分支</label>
              <input
                v-model="form.publishBranch"
                type="text"
                required
                class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-indigo-500 focus:bg-white focus:ring-4 focus:ring-indigo-500/10"
              />
            </div>
            <div>
              <label class="mb-2 block text-sm font-medium text-slate-700">站点 Base Path</label>
              <input
                v-model="form.siteBasePath"
                type="text"
                required
                class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-indigo-500 focus:bg-white focus:ring-4 focus:ring-indigo-500/10"
              />
            </div>
          </div>

          <div>
            <label class="mb-2 block text-sm font-medium text-slate-700">GitHub Token</label>
            <input
              v-model="form.githubToken"
              type="password"
              placeholder="留空表示保持现有 Token 不变"
              class="w-full rounded-xl border border-slate-200 bg-slate-50 px-4 py-3 text-sm outline-none transition focus:border-indigo-500 focus:bg-white focus:ring-4 focus:ring-indigo-500/10"
            />
          </div>

          <div v-if="submitError" class="flex items-start gap-2 rounded-xl border border-red-200 bg-red-50 p-3 text-sm text-red-700">
            <AlertCircle class="mt-0.5 h-4 w-4 shrink-0" />
            <span>{{ submitError }}</span>
          </div>

          <div v-if="successMessage" class="flex items-start gap-2 rounded-xl border border-green-200 bg-green-50 p-3 text-sm text-green-700">
            <CheckCircle2 class="mt-0.5 h-4 w-4 shrink-0" />
            <span>{{ successMessage }}</span>
          </div>

          <button
            type="submit"
            :disabled="!canSubmit"
            class="inline-flex items-center gap-2 rounded-xl bg-slate-900 px-4 py-3 text-sm font-semibold text-white transition hover:bg-indigo-600 disabled:cursor-not-allowed disabled:opacity-60"
          >
            <Loader2 v-if="isSubmitting" class="h-4 w-4 animate-spin" />
            <Save v-else class="h-4 w-4" />
            {{ isSubmitting ? '保存中...' : '保存配置' }}
          </button>
        </form>
      </section>

      <aside class="space-y-4">
        <div class="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
          <p class="text-sm font-medium text-slate-500">Token 状态</p>
          <p class="mt-2 text-base font-semibold" :class="tokenConfigured ? 'text-green-600' : 'text-amber-600'">
            {{ tokenConfigured ? '已配置' : '未配置' }}
          </p>
          <p class="mt-2 text-xs leading-5 text-slate-500">
            出于安全原因，系统不会返回 token 明文。你可通过这里判断当前是否已保存 token。
          </p>
        </div>

        <div class="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
          <p class="text-sm font-medium text-slate-500">发布地址预览</p>
          <p class="mt-2 break-all text-sm text-slate-700">
            https://{{ form.githubOwner || 'your-owner' }}.github.io/{{ form.githubRepo || 'your-repo' }}
          </p>
          <p class="mt-2 text-xs text-slate-500">
            Base Path：{{ form.siteBasePath || '/' }}
          </p>
        </div>

        <div class="rounded-2xl border border-slate-200 bg-white p-5 shadow-sm">
          <p class="text-sm font-medium text-slate-500">一键发布</p>
          <div class="mt-3 space-y-3">
            <button
              type="button"
              :disabled="isPreparing"
              class="inline-flex w-full items-center justify-center gap-2 rounded-xl bg-slate-900 px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-indigo-600 disabled:cursor-not-allowed disabled:opacity-60"
              @click="handlePreparePublish"
            >
              <Loader2 v-if="isPreparing" class="h-4 w-4 animate-spin" />
              <Rocket v-else class="h-4 w-4" />
              {{ isPreparing ? '生成中...' : '1. 生成静态包' }}
            </button>

            <button
              type="button"
              :disabled="!preparedPreviewPath || isPreparing"
              class="inline-flex w-full items-center justify-center gap-2 rounded-xl border border-slate-200 bg-white px-4 py-2.5 text-sm font-semibold text-slate-700 transition hover:border-indigo-300 hover:text-indigo-600 disabled:cursor-not-allowed disabled:opacity-60"
              @click="openPreview"
            >
              <ExternalLink class="h-4 w-4" />
              2. 打开本地预览
            </button>

            <button
              type="button"
              :disabled="!preparedJobId || isReleasing"
              class="inline-flex w-full items-center justify-center gap-2 rounded-xl bg-emerald-600 px-4 py-2.5 text-sm font-semibold text-white transition hover:bg-emerald-500 disabled:cursor-not-allowed disabled:opacity-60"
              @click="handleReleasePublish"
            >
              <Loader2 v-if="isReleasing" class="h-4 w-4 animate-spin" />
              <Upload v-else class="h-4 w-4" />
              {{ isReleasing ? '发布中...' : '3. 发布到 GitHub' }}
            </button>
          </div>

          <p v-if="preparedJobId" class="mt-3 text-xs text-slate-500">
            Job ID：{{ preparedJobId }} · 文章数：{{ preparedArticleCount }}
          </p>
          <p v-if="preparedDistPath" class="mt-1 break-all text-xs text-slate-500">
            静态包目录：{{ preparedDistPath }}
          </p>

          <p v-if="prepareError" class="mt-3 text-xs text-red-600">
            {{ prepareError }}
          </p>
          <p v-else-if="prepareMessage" class="mt-3 text-xs text-emerald-600">
            {{ prepareMessage }}
          </p>

          <div v-if="releaseError" class="mt-2 rounded-xl border border-red-200 bg-red-50 p-3">
            <p class="text-xs font-medium text-red-700">{{ releaseError }}</p>
            <pre
              v-if="releaseErrorDetail"
              class="mt-2 max-h-64 overflow-auto whitespace-pre-wrap break-all rounded-lg bg-red-100/70 p-2 text-[11px] leading-5 text-red-700"
            >{{ releaseErrorDetail }}</pre>
            <div v-if="releaseErrorReason" class="mt-2 rounded-lg border border-red-200 bg-white/70 p-2">
              <p class="text-xs font-semibold text-red-700">原因判断：{{ releaseErrorReason }}</p>
              <ul v-if="releaseErrorActions.length" class="mt-1 list-disc space-y-1 pl-4 text-[11px] leading-5 text-red-700">
                <li v-for="(action, index) in releaseErrorActions" :key="`${index}-${action}`">{{ action }}</li>
              </ul>
            </div>
            <div v-if="preparedDistPath" class="mt-2 rounded-lg border border-red-200 bg-white/70 p-2">
              <p class="text-xs font-semibold text-red-700">手动发布教程（网络不稳定时）</p>
              <ol class="mt-1 list-decimal space-y-1 pl-4 text-[11px] leading-5 text-red-700">
                <li>打开终端并进入静态包目录</li>
                <li>执行下方命令初始化并切换到发布分支</li>
                <li>提交静态文件并强制推送到 GitHub Pages 分支</li>
              </ol>
              <pre class="mt-2 max-h-64 overflow-auto whitespace-pre-wrap break-all rounded-lg bg-red-100/70 p-2 text-[11px] leading-5 text-red-700">{{ manualPublishCommandText }}</pre>
            </div>
          </div>
          <p v-else-if="releaseMessage" class="mt-2 text-xs text-emerald-600">
            {{ releaseMessage }}
          </p>

          <a
            v-if="publishedUrl"
            :href="publishedUrl"
            target="_blank"
            rel="noopener noreferrer"
            class="mt-2 inline-flex items-center gap-1 text-xs font-medium text-indigo-600 hover:text-indigo-500"
          >
            查看线上地址
            <ExternalLink class="h-3.5 w-3.5" />
          </a>
        </div>
      </aside>
    </div>
  </StudioLayout>
</template>
