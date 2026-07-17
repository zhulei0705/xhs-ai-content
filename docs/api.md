# V1.0 API 接口文档

## 1. 通用约定

- Base URL：`http://localhost:8080/api/v1`
- 业务接口统一使用 `POST`
- Content-Type：`application/json`
- 雪花 ID 以字符串返回，请勿在 JavaScript 中转为 Number
- 时间格式：ISO-8601 本地时间，例如 `2026-07-17T12:30:00`

统一响应：

```json
{
  "code": 0,
  "message": "success",
  "data": {},
  "timestamp": 1784260800000
}
```

常见错误码：

| code | 说明 |
| --- | --- |
| `40000` | 参数错误 |
| `40400` | 数据不存在 |
| `40900` | 状态冲突或重复提交 |
| `50010` | AI 配置不完整 |
| `50011` | AI 调用失败 |
| `50012` | AI 结构化结果无效 |
| `50000` | 系统异常 |

## 2. 选题接口

| 路径 | 请求体 | 说明 |
| --- | --- | --- |
| `/topics/generate` | `{"count":5,"generationDate":"2026-07-17"}` | 生成 3～10 个选题，同日 AI 结果幂等 |
| `/topics/page` | `{"pageNum":1,"pageSize":10,"status":"CANDIDATE","keyword":"AI"}` | 分页查询 |
| `/topics/detail` | `{"id":"..."}` | 详情 |
| `/topics/create` | 选题字段 | 手动新增 |
| `/topics/update` | 选题字段 + `id` | 修改未使用选题 |
| `/topics/delete` | `{"id":"..."}` | 逻辑删除未使用选题 |
| `/topics/status/update` | `{"id":"...","status":"ARCHIVED"}` | 更新状态 |

选题生成响应中的评分均为 0～100，`publishPriority` 越小越优先。

## 3. 内容接口

| 路径 | 请求体 | 说明 |
| --- | --- | --- |
| `/contents/generate` | `{"topicId":"..."}` | 生成并保存完整内容包 |
| `/contents/page` | `{"pageNum":1,"pageSize":10,"status":"DRAFT","keyword":"副业"}` | 分页查询 |
| `/contents/detail` | `{"id":"..."}` | 内容、标题、卡片和最新审核 |
| `/contents/create` | 内容编辑对象 | 手动新增草稿 |
| `/contents/update` | 内容编辑对象 + `id` | 保存未发布内容 |
| `/contents/delete` | `{"id":"..."}` | 逻辑删除草稿/待审核内容 |
| `/contents/status/update` | `{"id":"...","targetStatus":"ARCHIVED"}` | 状态修改 |
| `/contents/copy` | `{"id":"..."}` | 返回标题、带标签正文和互动语 |

内容编辑对象示例：

```json
{
  "id": "可选，更新时必填",
  "topicId": "可选",
  "selectedTitle": "下班后做AI副业，我先跑通了这3步",
  "body": "正文……",
  "coverTitle": "AI副业先跑通3步",
  "coverSubtitle": "程序员的下班后真实尝试",
  "tags": ["AI副业", "程序员副业", "个人成长"],
  "interactionGuide": "你想先解决哪个小问题？",
  "titles": [
    {"text": "标题1", "sortOrder": 1, "attractionScore": 90}
  ],
  "cards": [
    {"cardNo": 1, "title": "从小闭环开始", "body": "卡片正文"}
  ]
}
```

AI 生成结果固定包含 5 个标题和 6 张卡片；手动内容允许先保存部分标题/卡片再逐步完善。

## 4. 审核与人工确认

| 路径 | 请求体 | 说明 |
| --- | --- | --- |
| `/reviews/execute` | `{"contentId":"..."}` | 执行 AI 审核并进入待审核 |
| `/reviews/history` | `{"id":"内容ID"}` | 审核历史 |
| `/reviews/confirm` | `{"contentId":"...","approved":true}` | 人工确认或退回草稿 |

高风险（`HIGH`）审核结果不能直接确认通过。必须修改内容并重新审核。`READY_TO_PUBLISH` 只能由人工确认接口产生，不能通过通用状态接口绕过。

## 5. 发布记录

| 路径 | 请求体 | 说明 |
| --- | --- | --- |
| `/publish-records/save` | 发布记录对象 | 按 contentId 新增或幂等更新 |
| `/publish-records/page` | `{"pageNum":1,"pageSize":10}` | 发布记录分页 |
| `/publish-records/detail` | `{"id":"..."}` | 发布记录详情 |

```json
{
  "contentId": "...",
  "publishedAt": "2026-07-17T12:30:00",
  "noteUrl": "https://www.xiaohongshu.com/explore/...",
  "exposureCount": 1000,
  "likeCount": 80,
  "favoriteCount": 120,
  "commentCount": 30,
  "followerGrowth": 12
}
```

首次保存要求内容状态为待发布，并自动推进为已发布。收藏率=`收藏/曝光`，互动率=`(点赞+收藏+评论)/曝光`。

## 6. 数据复盘

| 路径 | 请求体 | 说明 |
| --- | --- | --- |
| `/analyses/execute` | `{"publishRecordId":"..."}` | 生成并保存新复盘 |
| `/analyses/latest` | `{"id":"发布记录ID"}` | 最近一次复盘 |
| `/analyses/dashboard` | `{"startDate":"2026-07-01","endDate":"2026-07-31"}` | 聚合数据和 TOP 5 |

日期为空时仪表盘默认查询最近 30 天。

## 7. 推荐联调顺序

```text
POST /topics/generate
→ POST /contents/generate
→ POST /reviews/execute
→ POST /reviews/confirm (approved=true)
→ 人工复制并在小红书发布
→ POST /publish-records/save
→ POST /analyses/execute
→ POST /analyses/dashboard
```

健康检查为 Actuator 标准接口 `GET /actuator/health`，它不是业务接口，因此不受“业务接口统一 POST”约束。
