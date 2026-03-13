# NexBlog

一个 **AI 驱动的双端博客系统**：前台面向读者阅读，后台面向作者/管理员写作与管理。  
项目当前定位为 MVP 阶段，重点完成了内容管理、AI 写作辅助、单用户初始化登录与鉴权链路。

---

## 项目介绍

NexBlog 目标是提供一套“能快速上线、可持续演进”的博客基础设施，核心思路：

- 前台：聚焦阅读体验，提供文章列表与详情浏览。
- 后台：聚焦创作效率，支持文章/分类管理与 AI 辅助写作。
- AI 能力：支持正文润色（替换/对比）与摘要生成。
- 安全能力：单用户模式下的首次初始化、登录鉴权、受保护接口访问控制。

---

## 打算做什么（路线方向）

项目计划逐步演进到以下方向：

1. **写作生产力增强**
   - 更丰富的 AI 指令模板（风格改写、SEO 优化、标题建议等）
   - 版本快照与内容回滚
2. **发布链路完善**
   - 一键发布流程打通（构建、部署、回滚）
3. **系统能力增强**
   - 全局设置页落地
   - 可观测性与日志治理
4. **多人协作（中长期）**
   - 从单用户扩展到多角色权限模型

---

## 当前开发进度（MVP）

> 状态：**MVP 核心可用**

- ✅ 前台文章列表 / 详情浏览
- ✅ 后台文章 CRUD + 分类 CRUD
- ✅ AI 润色（直接替换 / 左右对比）
- ✅ AI 摘要（单句纯文本）
- ✅ Markdown 写作与渲染
- ✅ 单用户首次初始化账号
- ✅ 登录鉴权（Bearer Token）
- ✅ 受保护接口拦截（studio/ai/auth me）

---

## 已实现功能清单

### 1) 前台（Reader）

- 文章分页列表（支持关键字检索）
- 文章详情页渲染

### 2) 后台（Studio）

- 仪表盘与写作入口
- 文章管理：创建、编辑、删除、列表查询
- 分类管理：创建、编辑、删除、列表查询
- 路由守卫：未登录重定向登录页

### 3) AI 写作辅助

- 文本润色：
  - `replace`：直接替换编辑器正文
  - `compare`：左右栏对比（原文 vs 润色后）
- 摘要生成：
  - 输出限制为一句话纯文本
  - 去除 Markdown/换行等噪声内容

### 4) 登录与鉴权（单用户模式）

- 首次运行自动进入初始化流程（创建管理员账号）
- 初始化后关闭重复创建入口
- 登录成功签发 Token（带过期时间）
- 前端统一携带 `Authorization: Bearer <token>`
- 后端拦截器保护后台与 AI 接口

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
│  ├─ src/api/                # 前端 API 层（article/category/ai/auth）
│  ├─ src/views/              # 页面（前台 + 后台）
│  ├─ src/layouts/            # 布局
│  └─ src/router/             # 路由与鉴权守卫
└─ backend/                   # Spring Boot 后端
   ├─ controller/             # 接口层（public/studio/ai/auth）
   ├─ service/                # 业务层
   ├─ repository/             # 数据访问层
   ├─ entity/                 # 实体
   └─ config/                 # CORS、鉴权拦截、配置绑定
```

---

## 安装与运行

## 1. 环境要求

- Node.js：`^20.19.0 || >=22.12.0`
- npm：建议随 Node LTS
- JDK：21
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

建议在生产环境覆盖以下配置：

- `NEXBLOG_AUTH_TOKEN_SECRET`（必须替换）
- Ollama 模型与地址按部署环境调整

---

## 常用命令

### 前端

```bash
cd front
npm run dev
npm run type-check
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
- Studio（需登录）:
  - `GET/POST/PUT/DELETE /api/studio/articles`
  - `GET/POST/PUT/DELETE /api/studio/categories`
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

- 当前版本强调 MVP 闭环：内容管理 + AI 辅助 + 登录鉴权。
- 后续迭代建议优先级：
  1. 全局设置页与配置管理界面
  2. 发布链路与部署流程自动化
  3. 监控告警与结构化日志
  4. 从单用户平滑升级到多用户权限体系

---

## License