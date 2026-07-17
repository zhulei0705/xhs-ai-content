# 小红书 AI 内容自动化系统 V1.0 技术设计

## 1. 需求分析

### 1.1 业务目标

系统服务于“程序员 AI 副业博主”，围绕 AI 副业、AI 工具、AI Coding、程序员副业和个人成长，建立安全、可复盘的内容生产闭环：

```text
定时/人工生成选题 → 选择选题 → 生成完整内容包 → AI 审核
→ 人工修改与确认 → 复制到小红书发布 → 回填发布数据 → AI 复盘
```

V1.0 不接入非官方发布接口，不保存小红书账号凭证，不模拟登录，不自动发布。内容只有经过人工确认后才能进入“待发布”，人工发布并回填链接后才进入“已发布”。

### 1.2 角色与范围

- 当前角色：单一内容创作者/运营者。
- 当前权限：V1.0 作为个人管理后台，暂不引入用户、租户和 RBAC，部署时应限制在可信网络。
- 数据范围：选题、内容、审核结论、人工发布记录和复盘结果。
- 非目标：小红书自动登录、非官方 API 发布、自动抓取平台数据、团队协作审批、付费订阅。

### 1.3 关键业务规则

1. 每个自然日生成 3～10 个选题；定时任务使用 Redis 锁和数据库日期检查保证幂等。
2. 选题评分为 0～100，包含目标用户匹配度、爆款潜力和综合分，优先级为 1～10，数值越小越优先。
3. 一次内容生成必须返回 5 个候选标题、正文、封面主副标题、6 张图文卡片、话题标签和互动引导语。
4. AI 审核输出结构化评分、风险项、风险等级和修改建议，不自动把高风险内容推进到待发布。
5. 内容状态流转：`DRAFT → PENDING_REVIEW → READY_TO_PUBLISH → PUBLISHED → ARCHIVED`。允许审核后退回草稿；已发布内容不能物理删除，只能归档。
6. 删除采用逻辑删除。发布记录与内容一对一，重复回填执行更新，避免产生重复记录。
7. 所有 Web 业务接口均使用 POST、`@RequestBody` 和统一 `Result<T>`。
8. AI 调用失败记录任务状态与脱敏错误，不记录 API Key；业务数据事务在 AI 调用完成后落库，避免长事务占用数据库连接。

## 2. 系统总体架构

### 2.1 架构选择

V1.0 使用前后端分离的模块化单体。单体降低部署和联调复杂度；领域包隔离、AI Provider SPI 和发布网关预留，使 V2.0 以后可以按需拆分。

```text
Vue 3 Admin
    │ POST /api/v1/**
    ▼
Spring Boot 3.5 / Java 21
    ├─ common         统一响应、异常、校验、分页、配置
    ├─ topic          选题生成与管理
    ├─ content        内容生成、编辑、状态机
    ├─ review         AI 审核与人工确认
    ├─ publish        人工发布记录
    ├─ analysis       数据指标与 AI 复盘
    ├─ ai             模型抽象、Provider、提示词、任务审计
    └─ scheduler      每日选题任务
       │        │
       ▼        ▼
    MySQL 8    Redis
       │
       └──────────────► OpenAI-compatible Provider
```

### 2.2 技术选型

| 层次 | 选型 | 说明 |
| --- | --- | --- |
| 后端 | Java 21、Spring Boot 3.5.x、Maven | 采用 Spring Boot 3.x 稳定维护线 |
| 持久层 | MyBatis-Plus 3.5.x、MySQL 8.x | CRUD、分页、乐观锁、逻辑删除 |
| 缓存/锁 | Redis | 定时任务幂等锁和短期缓存；故障时由数据库兜底 |
| AI | 自研轻量 OpenAI-compatible Adapter | 不让业务绑定具体厂商，避免不必要依赖 |
| 前端 | Vue 3、Vite 8、TypeScript、Element Plus、Axios | 单页管理后台 |
| 测试 | JUnit 5、Mockito、Spring Boot Test | 核心状态机、AI JSON 解析和 Service 测试 |

版本依据：Spring Boot 3.5 兼容 Java 21；MyBatis-Plus 官方为 Spring Boot 3 提供独立 starter；Vite 8 要求 Node.js 20.19+ 或 22.12+。

### 2.3 非功能设计

- 安全：密钥只从环境变量读取；日志和 AI 任务记录不包含密钥；URL 和输入长度校验；数据库账号使用最小权限。
- 一致性：内容及其标题/卡片在同一事务保存；发布回填和状态推进在同一事务；定时任务采用 Redis 锁 + 数据库唯一约束双保险。
- 可观测性：AI 请求生成任务号，记录 provider、model、耗时、状态、token 数和错误摘要。
- 性能：管理后台以分页查询为主；发布数据聚合由 SQL 完成；列表不返回大段正文和 AI 原始结果。
- 可恢复性：AI 任务失败不会产生半成品业务记录，可从失败任务重新发起。

## 3. 项目目录结构

```text
xhs-ai-content/
├── backend/
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/xhs/ai/content/
│       │   ├── XhsAiContentApplication.java
│       │   ├── common/
│       │   ├── config/
│       │   ├── ai/
│       │   ├── topic/
│       │   ├── content/
│       │   ├── review/
│       │   ├── publish/
│       │   ├── analysis/
│       │   └── scheduler/
│       ├── main/resources/
│       └── test/
├── frontend/
│   ├── package.json
│   └── src/
│       ├── api/
│       ├── components/
│       ├── layouts/
│       ├── router/
│       ├── styles/
│       ├── types/
│       └── views/
├── sql/
│   └── init.sql
├── docs/
│   ├── technical-design.md
│   └── api.md
├── README.md
└── .gitignore
```

后端领域包内部按 `controller / service / service.impl / mapper / entity / dto / vo` 分层，Controller 只负责校验、调用和返回。

## 4. 数据库设计

所有表使用 `BIGINT` 雪花 ID、`DATETIME(3)`、`utf8mb4`，业务表包含创建/更新时间。需要删除的主数据使用 `deleted` 逻辑删除字段。

### 4.1 `xhs_topic` 选题表

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id | BIGINT PK | 选题 ID |
| topic_title | VARCHAR(200) | 选题名称 |
| core_angle | VARCHAR(500) | 核心切入角度 |
| target_audience | VARCHAR(200) | 细分目标用户 |
| content_type | VARCHAR(32) | 工具/成长/教程/复盘等 |
| keywords_json | JSON | 关键词 |
| target_match_score | TINYINT UNSIGNED | 用户匹配度 0～100 |
| viral_potential_score | TINYINT UNSIGNED | 爆款潜力 0～100 |
| overall_score | TINYINT UNSIGNED | 综合评分 0～100 |
| potential_analysis | VARCHAR(1000) | 爆款潜力分析 |
| publish_priority | TINYINT UNSIGNED | 推荐顺序 1～10 |
| source_type | VARCHAR(20) | AI/MANUAL |
| generation_date | DATE | 生成日期 |
| status | VARCHAR(20) | CANDIDATE/SELECTED/USED/ARCHIVED |
| version/deleted | INT/TINYINT | 乐观锁/逻辑删除 |

索引：`uk_topic_date_title(generation_date, topic_title, deleted)`、`idx_topic_date_score(generation_date, overall_score)`。

### 4.2 `xhs_content` 内容主表

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| id/topic_id | BIGINT | 内容 ID/来源选题 ID |
| selected_title | VARCHAR(100) | 人工选择后的标题 |
| body | TEXT | 正文 |
| cover_title | VARCHAR(50) | 封面主标题 |
| cover_subtitle | VARCHAR(80) | 封面副标题 |
| tags_json | JSON | 推荐话题标签 |
| interaction_guide | VARCHAR(500) | 评论区互动引导 |
| status | VARCHAR(32) | 内容状态 |
| current_review_id | BIGINT NULL | 最新审核记录 |
| version/deleted | INT/TINYINT | 乐观锁/逻辑删除 |

索引：`idx_content_status_updated(status, updated_at)`、`idx_content_topic(topic_id)`。

### 4.3 `xhs_content_title` 候选标题表

保存每次内容当前的 5 个候选标题：`id, content_id, title_text, sort_order, selected, attraction_score`。唯一索引 `(content_id, sort_order)`。

### 4.4 `xhs_content_card` 图文卡片表

保存 6 张卡片：`id, content_id, card_no, card_title, card_body`。唯一索引 `(content_id, card_no)`。

### 4.5 `xhs_ai_review` AI 审核表

包含 `ai_tone_score, title_attraction_score, authenticity_score, platform_fit_score`，以及夸大收益、敏感表达、明显营销三个布尔风险项，另含 `risk_level, summary, suggestions_json, provider, model_name, reviewed_at`。一个内容可保留多次审核历史。

### 4.6 `xhs_publish_record` 发布记录表

包含 `content_id, published_at, note_url, exposure_count, like_count, favorite_count, comment_count, follower_growth`。`content_id` 唯一；计数非负。收藏率与互动率查询时计算，避免冗余失真。

### 4.7 `xhs_performance_analysis` 复盘表

包含 `publish_record_id, overall_grade, performance_summary, topic_analysis, title_analysis, favorite_rate_analysis, interaction_rate_analysis, next_topic_suggestions_json, provider, model_name, analyzed_at`，支持保留多次复盘历史。

### 4.8 `xhs_ai_task` AI 任务审计表

包含 `task_no, task_type, biz_id, status, provider, model_name, prompt_version, request_hash, result摘要, prompt_tokens, completion_tokens, duration_ms, error_message, started_at, finished_at`。不保存 API Key；原始提示词默认不落库。

## 5. API 接口设计

统一前缀 `/api/v1`，业务接口全部使用 POST；统一响应：

```json
{
  "code": 0,
  "message": "success",
  "data": {},
  "timestamp": 1750000000000
}
```

### 5.1 选题

| 路径 | 请求 | 响应/用途 |
| --- | --- | --- |
| `/topics/generate` | `{count, generationDate}` | 生成 3～10 个选题 |
| `/topics/page` | `{pageNum,pageSize,status,generationDate,keyword}` | 分页查询 |
| `/topics/detail` | `{id}` | 选题详情 |
| `/topics/create` | 选题 DTO | 人工新增 |
| `/topics/update` | 选题 DTO | 修改 |
| `/topics/delete` | `{id}` | 逻辑删除未使用选题 |
| `/topics/status/update` | `{id,status}` | 状态修改 |

### 5.2 内容与审核

| 路径 | 请求 | 响应/用途 |
| --- | --- | --- |
| `/contents/generate` | `{topicId}` | 生成完整内容包并保存草稿 |
| `/contents/page` | 分页条件 | 内容分页 |
| `/contents/detail` | `{id}` | 主体、5 个标题、6 张卡片和最新审核 |
| `/contents/create` | 内容编辑 DTO | 人工新增 |
| `/contents/update` | 内容编辑 DTO | 保存修改 |
| `/contents/delete` | `{id}` | 删除草稿/待审核内容 |
| `/contents/status/update` | `{id,targetStatus}` | 按状态机变更 |
| `/contents/copy` | `{id}` | 返回可复制正文包 |
| `/reviews/execute` | `{contentId}` | 执行 AI 审核 |
| `/reviews/history` | `{contentId}` | 审核历史 |
| `/reviews/confirm` | `{contentId,approved}` | 人工确认或退回草稿 |

### 5.3 发布与复盘

| 路径 | 请求 | 响应/用途 |
| --- | --- | --- |
| `/publish-records/save` | 发布记录 DTO | 新增或更新发布数据并推进状态 |
| `/publish-records/page` | 分页条件 | 发布记录列表和实时指标 |
| `/publish-records/detail` | `{id}` | 发布详情 |
| `/analyses/execute` | `{publishRecordId}` | AI 数据复盘 |
| `/analyses/latest` | `{publishRecordId}` | 最新复盘 |
| `/analyses/dashboard` | `{startDate,endDate}` | 汇总数据、TOP 内容 |

健康检查使用 Spring Boot Actuator 的标准 GET，不属于 Web 业务接口。

## 6. AI 调用架构

### 6.1 抽象

```text
TopicService / ContentService / ReviewService / AnalysisService
                         │
                         ▼
                  AiModelService
                         │
                         ▼
             DefaultAiModelService（路由）
                 │                 │
                 ▼                 ▼
        MockAiProviderClient   OpenAiCompatibleProviderClient
                                      │
                        OpenAI / DeepSeek / OpenRouter / 兼容服务
```

- `AiModelService` 只暴露“结构化生成”能力，业务层不知道厂商 HTTP 格式。
- `AiProviderClient` 是 Provider SPI，通过 `supports(provider)` 路由；新增厂商只需新增实现。
- 配置项：`provider`、`base-url`、`api-key`、`model`、`temperature`、`timeout`。
- 默认 `mock` Provider 生成确定性示例数据，使项目在没有密钥时也可启动和联调；生产使用真实 Provider 时必须配置 `XHS_AI_API_KEY`。
- 使用 JSON Schema 风格提示和 Jackson 严格反序列化；兼容模型若返回 Markdown 代码块会先做安全剥离。
- Prompt 按 `topic-v1/content-v1/review-v1/analysis-v1` 版本管理，便于后续 A/B 测试。

### 6.2 提示词原则

- 固定账号人设、目标用户和内容边界。
- 禁止编造个人收入、产品体验或未提供的事实；缺少事实时要求使用“计划/尝试/经验建议”等诚实表述。
- 禁止承诺收益和制造焦虑，标题吸引力不能依赖虚假夸张。
- 明确输出字段、数量、长度、分值范围，禁止输出 JSON 以外内容。
- 审核提示与生成提示分离，形成“生成者—审稿人”双角色。

### 6.3 失败与重试

- 仅对连接超时、429、5xx 做最多 2 次指数退避重试；4xx 参数错误不重试。
- 每次业务生成对应一个 AI 任务号；失败信息截断并脱敏。
- JSON 解析失败算任务失败，不保存半成品，用户可重新生成。

## 7. V1.0 开发计划

1. **设计与骨架**：落盘技术设计，创建 Maven/Vite 工程、统一配置和 README。
2. **数据库与公共能力**：初始化 SQL、MyBatis-Plus、`Result<T>`、异常、校验、分页、审计字段。
3. **核心领域**：选题、内容、状态机、审核、发布、复盘 Entity/Mapper/Service/API。
4. **AI 能力**：Provider SPI、OpenAI-compatible 客户端、Mock、本系统四类提示词和结构化解析。
5. **定时任务**：每日选题生成、Redis 锁、数据库幂等兜底。
6. **Vue 后台**：仪表盘、选题、内容工作台、审核确认、发布数据、复盘页面。
7. **联调与质量**：后端单元/集成测试、Maven 构建、前端类型检查与构建、接口冒烟、Diff 审查。

## 8. V2.0～V4.0 扩展规划

### V2.0：运营效率与素材能力

- 素材库、个人事实库和 RAG，降低 AI 编造风险。
- 封面/卡片图片模板渲染、批量导出和内容日历。
- Prompt 版本管理、重试、生成对比和成本统计。
- 在官方能力允许时接入官方发布 API；仍保留人工确认闸门。

### V3.0：数据驱动增长

- 通过官方数据接口自动同步指标，构建账号、选题、标题多维分析。
- A/B 标题实验、发布时间建议、相似内容去重和热点趋势源。
- 向量检索历史高表现内容，自动生成更个性化的下一批选题。

### V4.0：多账号与平台化

- 用户、组织、租户、RBAC、审核流和操作审计。
- 多账号人设、品牌词库、合规策略和模型路由。
- 领域服务拆分、消息队列异步编排、任务中心和可观测性平台。
- 多平台官方渠道适配，但每个平台以独立发布网关和合规策略隔离。

## 9. 风险与决策记录

- 平台规则可能变化：V1.0 坚持人工复制发布，不接入不受支持的接口。
- AI 输出不是事实来源：系统通过提示词和审核降低风险，最终真实性由人工确认。
- 账号单租户：V1.0 为降低无效代码刻意不做登录/RBAC，公网部署前必须补认证或由网关保护。
- Redis 不是强依赖事实库：Redis 故障时数据库唯一索引仍防止同日重复选题。
- 发布数据由人工回填，可能存在误差；V2.0 再考虑官方同步。
