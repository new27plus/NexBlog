# NexBlog

一个 **AI 驱动的双端博客系统**：前台面向读者阅读，后台面向作者/管理员写作与发布。  
当前版本已完成内容管理、AI 写作辅助、单用户鉴权、个人主页配置、静态导出与 GitHub Pages 发布闭环。

---

## 项目介绍

NexBlog 目标是提供一套“能快速上线、可持续演进”的博客基础设施，核心思路：

- 前台：聚焦阅读体验，提供文章列表、详情与关于页展示。
- 后台：聚焦创作与运营效率，支持文章/分类管理、个人配置、系统配置。
- AI 能力：支持正文润色（替换/对比）与摘要生成。
- 发布能力：支持静态包生成、本地预览、发布到 GitHub Pages。
- 安全能力：单用户模式下的首次初始化、登录鉴权、受保护接口访问控制。

---

## 打算做什么（路线方向）

项目计划逐步演进到以下方向：

1. **写作生产力增强**
   - 更丰富的 AI 指令模板（风格改写、SEO 优化、标题建议等）
   - 版本快照与内容回滚
2. **发布链路增强**
   - 发布历史与回滚能力
   - 增量构建与发布效率优化
3. **系统能力增强**
   - 可观测性与日志治理
   - 配置审计与安全策略强化
4. **多人协作（中长期）**
   - 从单用户扩展到多角色权限模型

---

## 当前开发进度（MVP）

> 状态：**MVP 核心可用（含发布链路）**

- ✅ 前台文章列表 / 详情浏览 / 关于页
- ✅ 无封面图时自动隐藏图片区域，有图时按封面展示
- ✅ 后台文章 CRUD + 分类 CRUD
- ✅ 个人信息配置（分区化内容）与前台关于页联动
- ✅ 全局设置（GitHub Pages 配置）与一键发布（生成/预览/发布）
- ✅ 可复用最近一次静态包（刷新后可继续发布）
- ✅ AI 润色（直接替换 / 左右对比）
- ✅ AI 摘要（单句纯文本）
- ✅ 单用户首次初始化账号 + Bearer Token 鉴权

---

## 已实现功能清单

### 1) 前台（Reader）

- 文章分页列表（支持关键字检索）
- 文章详情页 Markdown 渲染
- 文章封面按内容自动解析（无图不占位）
- 关于页动态渲染（个人介绍、链接、主题、标签、更新轨迹）

### 2) 后台（Studio）

- 文章管理：创建、编辑、删除、列表查询
- 分类管理：创建、编辑、删除、列表查询
- 个人配置管理：分区编辑（intro/story/links/topics/tags/updates）
- 全局设置管理：GitHub Owner/Repo/Branch/Token/Base Path
- 路由守卫：未登录重定向登录页

### 3) 发布能力（Static Export + GitHub Pages）

- 生成静态包：导出文章数据与个人配置数据，构建前台静态站点
- 本地预览：按发布任务 ID 提供预览路径
- 一键发布：将静态包推送到目标仓库分支
- 发布恢复：自动加载最近一次可用静态包用于继续发布

### 4) AI 写作辅助

- 文本润色：
  - `replace`：直接替换编辑器正文
  - `compare`：左右栏对比（原文 vs 润色后）
- 摘要生成：
  - 输出限制为一句话纯文本
  - 去除 Markdown/换行等噪声内容

### 5) 登录与鉴权（单用户模式）

- 首次运行自动进入初始化流程（创建管理员账号）
- 初始化后关闭重复创建入口
- 登录成功签发 Token（带过期时间）
- 前端统一携带 `Authorization: Bearer <token>`
- 后端拦截器保护后台、AI 与配置/发布接口

---

## 技术栈

### 前端

- Vue 3 + TypeScript
- Vite
- Vue Router
- Pinia
- Tailwind CSS
- Markdown-It + Highlight.js

### 后端

- Java 21
- Spring Boot（Web MVC / Validation / Data JPA）
- H2（文件模式持久化）
- Spring AI（Ollama）
- Gradle

---

## 项目结构

```text
NexBlog
├─ front/                     # Vue3 前端
│  ├─ src/api/                # 前端 API 层（article/category/ai/auth/config/personal）
│  ├─ src/views/              # 页面（前台 + 后台）
│  ├─ src/layouts/            # 布局
│  └─ src/router/             # 路由与鉴权守卫
├─ backend/                   # Spring Boot 后端
│  ├─ controller/             # 接口层（public/studio/ai/auth/publish）
│  ├─ service/                # 业务层
│  ├─ repository/             # 数据访问层
│  ├─ entity/                 # 实体
│  └─ config/                 # CORS、鉴权拦截、配置绑定
└─ work/publish/              # 发布任务工作目录（默认）
```

---

## 安装与运行

## 1. 环境要求

- Node.js：`^20.19.0 || >=22.12.0`
- npm：建议随 Node LTS
- JDK：21
- Git：用于发布阶段推送静态包
- Ollama：本地可用（用于 AI 功能）

## 2. 克隆项目

```bash
git clone <your-repo-url>.git
cd NexBlog
```

## 3. 启动后端

```bash
cd backend
./gradlew bootRun
```

Windows 可使用：

```powershell
cd backend
.\gradlew.bat bootRun
```

后端默认端口：`http://localhost:8080`

## 4. 启动前端

```bash
cd front
npm install
npm run dev
```

前端默认端口：`http://localhost:5173`

---

## 首次运行初始化（非常重要）

本项目当前是 **单用户模式**，首次使用请按以下步骤：

1. 启动前后端服务。
2. 打开 `http://localhost:5173/studio/login`。
3. 页面会自动进入“首次运行初始化管理员账号”模式。
4. 输入用户名、密码、确认密码，提交后即完成初始化并自动登录。
5. 后续再访问登录页将切换为普通登录模式。

---

## 配置说明

后端配置文件：`backend/src/main/resources/application.yaml`

关键配置项：

- `spring.datasource.*`：H2 数据库配置（文件模式）
- `spring.ai.ollama.*`：Ollama 地址与模型
- `nexblog.auth.token-secret`：Token 签名密钥
- `nexblog.auth.token-ttl-minutes`：Token 有效期（分钟）
- `nexblog.publish.workspace-root`：发布任务目录（默认 `./work/publish`）

建议在生产环境覆盖以下配置：

- `NEXBLOG_AUTH_TOKEN_SECRET`（必须替换）
- Ollama 模型与地址按部署环境调整
- 发布环境需保证服务器可访问 GitHub（443）

---

## 常用命令

### 前端

```bash
cd front
npm run dev
npm run type-check
npm run build-only
npm run build
```

### 后端

```bash
cd backend
./gradlew test
./gradlew bootRun
```

Windows:

```powershell
cd backend
.\gradlew.bat test
.\gradlew.bat bootRun
```

---

## API 概览（MVP）

- Public:
  - `GET /api/public/articles`
  - `GET /api/public/articles/{id}`
  - `GET /api/personal-config`
- Studio（需登录）:
  - `GET/POST/PUT/DELETE /api/studio/articles`
  - `GET/POST/PUT/DELETE /api/studio/categories`
  - `GET /api/studio/personal-config`
  - `PUT /api/studio/personal-config`
  - `GET /api/studio/system-config`
  - `PUT /api/studio/system-config`
  - `POST /api/studio/publish/prepare`
  - `GET /api/studio/publish/latest`
  - `POST /api/studio/publish/release`
- Preview:
  - `GET /preview/{jobId}`
  - `GET /preview/{jobId}/{*relativePath}`
- AI（需登录）:
  - `POST /api/ai/optimize`
  - `POST /api/ai/summary`
- Auth:
  - `GET /api/auth/bootstrap-status`
  - `POST /api/auth/bootstrap`
  - `POST /api/auth/login`
  - `GET /api/auth/me`

---

## 开发说明

- 当前版本强调 MVP 闭环：内容管理 + AI 辅助 + 登录鉴权 + 静态发布。
- 发布流程建议：
  1. 在“全局设置”保存 GitHub 配置
  2. 生成静态包并先本地预览
  3. 确认内容后再执行发布
---
