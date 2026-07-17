# 小红书 AI 内容自动化系统 V1.0

面向“小红书内容创作者”的安全内容生产系统。当前内置人设为“程序员 AI 副业博主”，目标用户是想做副业的人，核心方向覆盖 AI 副业、AI 工具、AI Coding、程序员副业和个人成长。

系统完成：

```text
AI 选题 → 完整内容包 → AI 审核 → 人工确认
→ 一键复制 → 人工发布 → 数据回填 → AI 复盘
```

V1.0 **不使用非官方小红书接口、不保存小红书账号凭证、不自动发布**。发布动作始终由创作者人工完成。

## 已实现功能

- 每日定时/手动生成 3～10 个选题，包含匹配度、爆款潜力、综合评分和优先级。
- 根据选题生成 5 个标题、正文、封面主副标题、6 张卡片、标签和互动引导语。
- AI 审核自然度、标题吸引力、真实性、收益夸大、敏感表达、营销感和平台适配度。
- 草稿、待审核、待发布、已发布、已归档状态机；高风险内容禁止直接确认。
- 人工发布链接和曝光/赞/藏/评/涨粉数据回填，自动计算收藏率和互动率。
- AI 数据复盘、TOP 内容和下一批选题建议。
- OpenAI-compatible 模型抽象，支持 OpenAI、DeepSeek、OpenRouter 和其他兼容服务。
- 默认 Mock AI，无 API Key 也可以完整启动和联调。

## 技术栈

- 后端：Java 21、Spring Boot 3.5、Maven、MyBatis-Plus、MySQL 8、Redis、Lombok、Hutool。
- 前端：Vue 3、Vite 8、TypeScript、Element Plus、Axios。
- 测试：JUnit 5、Spring Boot Test、MockMvc、H2。

## 项目结构

```text
├── backend/       Spring Boot 后端
├── frontend/      Vue 3 管理后台
├── sql/init.sql   MySQL 完整初始化脚本
├── docs/          技术设计和 API 文档
├── .env.example   环境变量示例
└── docker-compose.yml
```

## 快速启动

### 1. 环境要求

- JDK 21
- Maven 3.6.3+
- Node.js 20.19+ 或 22.12+
- Docker Desktop（推荐，用于 MySQL/Redis）或本地 MySQL 8 + Redis

### 2. 启动 MySQL 和 Redis

先复制环境变量模板，将数据库密码占位值替换为本机专用强密码：

```powershell
Copy-Item .env.example .env
```

然后在根目录执行：

```bash
docker compose up -d
```

本地连接信息：

- MySQL：`127.0.0.1:3306/xhs_ai_content`
- 用户：`xhs_app`
- 密码：使用 `.env` 中的 `DB_PASSWORD`
- Redis：`127.0.0.1:6379`

Compose 在缺少 `DB_PASSWORD` 或 `MYSQL_ROOT_PASSWORD` 时会拒绝启动，避免误用示例密码。正式环境应使用最小权限数据库账号。已有 MySQL 实例可以直接执行 [sql/init.sql](sql/init.sql)。

### 3. 启动后端

```bash
cd backend
mvn spring-boot:run
```

启动前请通过终端、IDE 或部署平台注入 `DB_PASSWORD`，其值应与数据库配置一致。默认使用 `mock` AI Provider，不需要 AI 密钥。后端地址为 `http://localhost:8080`，健康检查为 `GET /actuator/health`。

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

打开 `http://localhost:5173`。Vite 会把 `/api` 代理到后端 `8080` 端口。

## 接入真实 AI 模型

所有配置均由环境变量提供，禁止把密钥写入仓库。PowerShell 示例：

```powershell
$env:XHS_AI_PROVIDER = "deepseek"
$env:XHS_AI_BASE_URL = "https://api.deepseek.com/v1"
$env:XHS_AI_API_KEY = "your-api-key"
$env:XHS_AI_MODEL = "deepseek-chat"
mvn spring-boot:run
```

其他兼容服务可将 Provider 设置为 `openai`、`openrouter` 或 `compatible`，并配置对应 Base URL 和模型名。业务代码只依赖 `AiModelService`，不会绑定某个厂商。

## 常用配置

| 环境变量 | 默认值 | 说明 |
| --- | --- | --- |
| `DB_HOST/DB_PORT/DB_NAME` | `127.0.0.1/3306/xhs_ai_content` | MySQL |
| `DB_USERNAME/DB_PASSWORD` | `xhs_app/空` | 数据库凭据；密码必须由环境注入 |
| `REDIS_HOST/REDIS_PORT` | `127.0.0.1/6379` | Redis |
| `XHS_AI_PROVIDER` | `mock` | AI Provider |
| `XHS_AI_BASE_URL` | OpenAI v1 地址 | 兼容 API 根地址 |
| `XHS_AI_API_KEY` | 空 | API Key |
| `XHS_AI_MODEL` | `gpt-4.1-mini` | 模型名 |
| `XHS_TOPIC_SCHEDULE_ENABLED` | `true` | 每日选题定时任务 |
| `XHS_TOPIC_SCHEDULE_CRON` | `0 0 8 * * ?` | 每日任务 Cron |
| `XHS_CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | 前端来源，逗号分隔 |

完整示例见 [.env.example](.env.example)。Spring Boot 直接运行时需由终端、IDE 或部署平台注入环境变量；Docker Compose 会自动读取根目录 `.env`。

## 构建与测试

```bash
cd backend
mvn test
mvn package

cd ../frontend
npm run build
```

后端集成测试使用 H2 和 Mock AI，不依赖 MySQL、Redis 或外部 AI，实际覆盖完整安全工作流。

## 文档

- [总体技术设计](docs/technical-design.md)
- [API 接口文档](docs/api.md)
- [MySQL 初始化脚本](sql/init.sql)

## 生产部署提醒

- V1.0 是单账号个人后台，未内置登录/RBAC。不要直接暴露到公网；应由内网、VPN 或认证网关保护。
- 生成内容必须人工核实真实性，尤其是个人经历、产品体验和收益数据。
- API Key、数据库密码、Token 不得进入代码、日志或 Git。
- 不要通过脚本模拟登录或接入非官方小红书发布接口。
- 生产数据库执行脚本前先备份并评估；`sql/init.sql` 只创建不存在的库表，不包含 DROP/TRUNCATE。
