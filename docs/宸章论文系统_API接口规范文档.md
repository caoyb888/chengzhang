# 宸章高等学历继续教育论文综合服务系统
# API 接口规范文档（API Specification）

**规范版本：** V1.0  
**日期：** 2026-04-21  
**文档规范：** OpenAPI 3.0.3  
**基准文档：** CLAUDE.md V2.0 / 数据库设计文档 V1.0 / 技术实现方案 V1.0  
**维护责任：** 后端负责人  
**阅读对象：** 前端开发、后端开发、测试、项目负责人

---

## 目录

1. [全局约定](#1-全局约定)
2. [鉴权规范](#2-鉴权规范)
3. [通用 Schema 定义](#3-通用-schema-定义)
4. [错误码规范](#4-错误码规范)
5. [认证模块 `/api/v1/auth`](#5-认证模块)
6. [用户与权限模块 `/api/v1/user` `/api/v1/role`](#6-用户与权限模块)
7. [批次管理模块 `/api/v1/batch`](#7-批次管理模块)
8. [论文流程模块 `/api/v1/paper`](#8-论文流程模块)
9. [在线批注与评分模块 `/api/v1/annotation` `/api/v1/score`](#9-在线批注与评分模块)
10. [模板管理模块 `/api/v1/template`](#10-模板管理模块)
11. [即时通讯模块 `/api/v1/im`](#11-即时通讯模块)
12. [在线答辩模块 `/api/v1/defense`](#12-在线答辩模块)
13. [AI 能力模块 `/api/v1/ai`](#13-ai-能力模块)
14. [统计报表模块 `/api/v1/statistics`](#14-统计报表模块)
15. [文件服务模块 `/api/v1/file`](#15-文件服务模块)
16. [通知模块 `/api/v1/notify`](#16-通知模块)
17. [WebSocket 接口规范](#17-websocket-接口规范)
18. [接口变更日志](#18-接口变更日志)

---

## 1. 全局约定

### 1.1 基础信息

```yaml
openapi: 3.0.3
info:
  title: 宸章论文综合服务系统 API
  version: "1.0.0"
  description: 曲阜师范大学高等学历继续教育论文综合服务系统接口规范
  contact:
    name: 宸章科技后端团队

servers:
  - url: https://api.thesis.chenzhang.com
    description: 生产环境
  - url: https://staging-api.thesis.chenzhang.com
    description: 验收/演示环境
  - url: http://localhost:8080
    description: 本地开发环境（Gateway 端口）
```

### 1.2 请求规范

| 规则 | 说明 |
|---|---|
| 协议 | HTTPS（生产/演示），HTTP（仅本地开发）|
| 内容类型 | `Content-Type: application/json`（文件上传除外，使用 `multipart/form-data`）|
| 字符编码 | UTF-8 |
| 时间格式 | ISO 8601：`2024-04-21T10:00:00+08:00`（带时区）|
| 日期格式 | `2024-04-21` |
| 布尔值 | `true` / `false`（JSON 原生布尔，不使用 0/1）|
| 分页参数 | `pageNum`（从 **1** 开始）、`pageSize`（默认 20，最大 100）|
| 长整型 | 超过 53 位的 ID（雪花算法）以 **字符串** 形式返回，避免 JS 精度丢失 |

### 1.3 响应规范

所有接口统一返回以下结构，**禁止返回裸数据**：

```json
{
  "code":      200,
  "message":   "success",
  "data":      {},
  "timestamp": 1713600000000
}
```

分页响应的 `data` 结构：

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total":   1250,
    "pages":   63,
    "current": 1,
    "size":    20,
    "records": []
  },
  "timestamp": 1713600000000
}
```

### 1.4 接口路径规范

```
/api/v1/<模块>/<资源>[/<id>][/<子操作>]

示例：
  GET    /api/v1/paper/list                    列表（分页）
  POST   /api/v1/paper/submit                  新建/提交
  GET    /api/v1/paper/{id}                    详情
  PUT    /api/v1/paper/{id}/review             资源子操作
  DELETE /api/v1/batch/{id}                    删除（逻辑）
  POST   /api/v1/user/import                   异步任务触发
  GET    /api/v1/user/import/{taskId}/progress 异步任务进度
```

### 1.5 接口权限标注说明

文档中每个接口使用以下标记注明所需权限：

| 标记 | 含义 |
|---|---|
| `🔓 公开` | 无需登录即可访问（如登录接口）|
| `🔑 已登录` | 任意已登录用户均可访问 |
| `👑 SCHOOL_ADMIN` | 需要学校管理员角色 |
| `📍 POINT_ADMIN` | 需要教学点管理员角色（含 SCHOOL_ADMIN）|
| `👨‍🏫 TEACHER` | 需要指导教师角色 |
| `👩‍🏫 ASSISTANT` | 需要辅助指导教师角色 |
| `🎓 STUDENT` | 需要学生角色 |
| `🔐 perm:xxx` | 需要具体权限编码 |

---

## 2. 鉴权规范

### 2.1 认证方式

系统采用 **Bearer Token（JWT）** 认证，基于 Sa-Token 框架实现。

```
请求头格式：
Authorization: Bearer <token>

示例：
Authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...
```

### 2.2 Token 生命周期

| Token 类型 | 有效期 | 说明 |
|---|---|---|
| Access Token | 2 小时无操作失效（活跃续期）| 随每次请求自动续期 |
| Refresh Token | 7 天 | 用于无感刷新 Access Token |
| 微信签名临时 Token | 30 分钟 | 仅用于电子签名场景 |

### 2.3 Token 刷新流程

```
1. 客户端检测到响应 code=401（Token 过期）
2. 携带 Refresh Token 调用 POST /api/v1/auth/token/refresh
3. 获取新 Access Token，重试原请求
4. 若 Refresh Token 也过期（code=4010），跳转登录页
```

### 2.4 多端登录规则

```
同一账号同一 client_type 下只允许一个有效 Token（后登录踢出先登录）
client_type 枚举：WEB（PC浏览器）| WECHAT（微信小程序，用于电子签名）
```

### 2.5 数据权限说明

Token 中携带用户的学校和教学点信息，网关层自动注入，**前端无需传递**：

| 角色 | 数据范围 | 注入机制 |
|---|---|---|
| SCHOOL_ADMIN | 全校所有数据 | MyBatis-Plus 自动注入 `school_id` |
| POINT_ADMIN | 本教学点数据 | 自动注入 `school_id` + `teaching_point_id` |
| TEACHER/ASSISTANT | 自己负责的学生数据 | 业务层校验指导关系 |
| STUDENT | 仅自己的论文 | 业务层校验 `student_id` |

---

## 3. 通用 Schema 定义

### 3.1 基础响应体

```yaml
# Result<T>
ResultBase:
  type: object
  required: [code, message, timestamp]
  properties:
    code:
      type: integer
      description: 响应状态码
      example: 200
    message:
      type: string
      description: 响应消息
      example: "success"
    data:
      description: 响应数据（错误时为 null）
    timestamp:
      type: integer
      format: int64
      description: 服务器时间戳（毫秒）
      example: 1713600000000

# PageResult<T>
PageResult:
  type: object
  properties:
    total:
      type: integer
      format: int64
      description: 总记录数
      example: 1250
    pages:
      type: integer
      description: 总页数
      example: 63
    current:
      type: integer
      description: 当前页码（从1开始）
      example: 1
    size:
      type: integer
      description: 每页数量
      example: 20
    records:
      type: array
      items: {}
      description: 当前页数据列表
```

### 3.2 通用枚举值

```yaml
# 用户类型
UserType:
  type: string
  enum: [SCHOOL_ADMIN, POINT_ADMIN, TEACHER, ASSISTANT, STUDENT]
  description: |
    - SCHOOL_ADMIN: 学校管理员
    - POINT_ADMIN: 教学点管理员
    - TEACHER: 指导教师
    - ASSISTANT: 辅助指导教师
    - STUDENT: 学生

# 论文节点类型
FlowNodeType:
  type: string
  enum: [SIGN, TOPIC, OUTLINE, TOPIC_FORM, TASK_BOOK, DRAFT_SEG, DRAFT_FULL, FINAL_DRAFT, FINAL]
  description: |
    - SIGN: 电子签名
    - TOPIC: 论文选题
    - OUTLINE: 大纲编写
    - TOPIC_FORM: 选题登记表
    - TASK_BOOK: 任务书
    - DRAFT_SEG: 初稿（分段）
    - DRAFT_FULL: 初稿（整篇）
    - FINAL_DRAFT: 定稿
    - FINAL: 终稿

# 论文节点状态
NodeStatus:
  type: string
  enum: [NOT_STARTED, PENDING, SUBMITTED, REVIEWING, APPROVED, REJECTED]
  description: |
    - NOT_STARTED: 未到达该节点
    - PENDING: 待学生提交
    - SUBMITTED: 已提交待审核
    - REVIEWING: 审核中
    - APPROVED: 审核通过
    - REJECTED: 已驳回

# 论文整体状态
PaperStatus:
  type: string
  enum: [NOT_STARTED, IN_PROGRESS, COMPLETED, FAILED]

# 审核决定
ReviewDecision:
  type: string
  enum: [APPROVED, REJECTED, GUIDE_ONLY]
  description: |
    - APPROVED: 通过
    - REJECTED: 驳回（退回学生修改）
    - GUIDE_ONLY: 仅提供指导意见（不影响状态流转）
```

### 3.3 分页请求参数

```yaml
PageQuery:
  type: object
  properties:
    pageNum:
      type: integer
      minimum: 1
      default: 1
      description: 页码（从1开始）
    pageSize:
      type: integer
      minimum: 1
      maximum: 100
      default: 20
      description: 每页数量（最大100）
```

---

## 4. 错误码规范

### 4.1 HTTP 状态码

所有接口 HTTP 状态码统一返回 **200**，错误通过响应体 `code` 字段区分。

> 例外：网关层的 429（限流）、502（服务不可达）直接返回 HTTP 状态码。

### 4.2 业务错误码定义

#### 4.2.1 通用错误码（1xxx）

| code | message | 场景 |
|---|---|---|
| `200` | success | 成功 |
| `400` | 请求参数错误 | 通用参数校验失败 |
| `401` | 未认证，请先登录 | Token 缺失或无效 |
| `4010` | 登录已过期，请重新登录 | Refresh Token 过期 |
| `4011` | 账号已在其他设备登录 | 被踢出 |
| `403` | 无权限执行此操作 | 权限不足 |
| `404` | 资源不存在 | 业务资源未找到 |
| `409` | 数据冲突 | 重复提交/唯一约束冲突 |
| `429` | 操作过于频繁，请稍后重试 | 限流触发 |
| `500` | 系统繁忙，请稍后重试 | 服务器未知异常 |
| `503` | 服务暂时不可用 | 服务熔断或维护 |

#### 4.2.2 认证模块错误码（10xx）

| code | message | 场景 |
|---|---|---|
| `1001` | 账号或密码错误 | 登录失败 |
| `1002` | 账号不存在 | 用户名/手机号未注册 |
| `1003` | 账号已被锁定，请 {minutes} 分钟后重试 | 密码连续错误5次 |
| `1004` | 账号已被停用，请联系管理员 | 账号 status=INACTIVE |
| `1005` | 验证码错误或已过期 | 短信验证码验证失败 |
| `1006` | 验证码发送过于频繁 | 60秒内重复请求 |
| `1007` | 微信授权失败，请重试 | OAuth 流程异常 |
| `1008` | 该微信尚未绑定账号 | 微信扫码但未绑定 |

#### 4.2.3 论文流程错误码（20xx）

| code | message | 场景 |
|---|---|---|
| `2001` | 当前节点未在开放时间内 | 不在 start_time~end_time 范围 |
| `2002` | 前置节点尚未完成，请先完成前置步骤 | 强制顺序校验失败 |
| `2003` | 本节点已关闭，无法提交 | is_enabled=0 |
| `2004` | 已超过最大重新提交次数 | submit_count ≥ max_resubmit_count |
| `2005` | 评语不足最低字数要求（至少 {n} 字）| min_comment_length 校验失败 |
| `2006` | 该选题已被其他同学选择，请重新选择 | 选题去重冲突 |
| `2007` | 论文不存在或无权访问 | 数据权限校验失败 |
| `2008` | 当前论文不允许逾期提交 | 无有效 deadline_extension |
| `2009` | 逾期提交通道已过期 | expire_at < now() |
| `2010` | 评分已提交锁定，如需修改请联系管理员解锁 | is_locked=1 |
| `2011` | 批次状态不允许此操作 | batch.status 不匹配 |
| `2012` | 教师无权审核该论文 | 指导关系校验失败 |

#### 4.2.4 文件错误码（30xx）

| code | message | 场景 |
|---|---|---|
| `3001` | 文件类型不支持，仅允许：{types} | 文件白名单校验失败 |
| `3002` | 文件大小超出限制（最大 {size}MB）| 超出文件大小限制 |
| `3003` | 文件上传失败，请重试 | OSS 上传异常 |
| `3004` | 文件不存在或链接已过期 | 预签名 URL 过期 |
| `3005` | 论文解析失败，请检查文件格式 | POI 解析 DOCX 异常 |

#### 4.2.5 即时通讯错误码（40xx）

| code | message | 场景 |
|---|---|---|
| `4001` | 您不在该群组中 | 发送消息权限校验 |
| `4002` | 您已被禁言 | is_muted=1 |
| `4003` | 群组不存在或已解散 | status=DISSOLVED |
| `4004` | 消息发送失败，内容包含违禁词 | is_blocked=1 |
| `4005` | 消息撤回超时（仅支持2分钟内撤回）| recalled 时间校验 |

#### 4.2.6 答辩错误码（50xx）

| code | message | 场景 |
|---|---|---|
| `5001` | 答辩房间不存在或已结束 | RTC 房间状态异常 |
| `5002` | 您不在该答辩组中 | 成员校验 |
| `5003` | 答辩尚未开始 | status=PENDING |
| `5004` | 答辩视频上传失败 | 异步答辩视频上传异常 |

#### 4.2.7 AI 服务错误码（60xx）

| code | message | 场景 |
|---|---|---|
| `6001` | AI 服务繁忙，请稍后重试 | 模型接口超时/熔断 |
| `6002` | 论文内容为空，无法执行 AI 分析 | 内容校验失败 |
| `6003` | 查重任务正在处理中，请勿重复提交 | 幂等校验 |
| `6004` | 第三方查重服务暂不可用 | 外部查重 API 异常 |

### 4.3 错误响应示例

```json
// 参数校验失败
{
  "code": 400,
  "message": "评语不足最低字数要求（至少 200 字）",
  "data": null,
  "timestamp": 1713600000000
}

// 权限不足
{
  "code": 403,
  "message": "无权限执行此操作",
  "data": null,
  "timestamp": 1713600000000
}

// 业务冲突
{
  "code": 2006,
  "message": "该选题已被其他同学选择，请重新选择",
  "data": null,
  "timestamp": 1713600000000
}
```


---

## 5. 认证模块

**服务：** `thesis-auth`  
**路径前缀：** `/api/v1/auth`

---

### POST /api/v1/auth/login/password
**功能：** 账号密码登录  
**权限：** 🔓 公开

**Request Body：**
```json
{
  "username": "string",       // 登录账号（支持：账号名 / 手机号）
  "password": "string",       // 密码（明文，HTTPS传输）
  "clientType": "WEB"         // 客户端类型：WEB
}
```

**Schema：**
```yaml
LoginByPasswordRequest:
  type: object
  required: [username, password, clientType]
  properties:
    username:
      type: string
      maxLength: 64
      description: 登录账号（账号名或手机号）
    password:
      type: string
      minLength: 6
      maxLength: 32
      description: 登录密码（明文，HTTPS 加密传输）
    clientType:
      type: string
      enum: [WEB, WECHAT]
      default: WEB
```

**Response 200：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "accessToken":  "eyJhbGciOiJSUzI1NiJ9...",
    "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
    "expiresIn":    7200,
    "userInfo": {
      "userId":           "1234567890",
      "schoolId":         "100",
      "username":         "zhangsan",
      "realName":         "张三",
      "userType":         "TEACHER",
      "teachingPointId":  null,
      "avatarUrl":        "https://oss.example.com/avatars/xxx.jpg",
      "permissions":      ["paper:review", "paper:annotate", "statistics:view"]
    }
  },
  "timestamp": 1713600000000
}
```

**错误码：** `1001` `1002` `1003` `1004`

---

### POST /api/v1/auth/login/sms
**功能：** 手机号 + 短信验证码登录  
**权限：** 🔓 公开

**Request Body：**
```json
{
  "phone":      "13800138000",
  "smsCode":    "123456",
  "clientType": "WEB"
}
```

**Response 200：** 同 `/login/password`

**错误码：** `1002` `1004` `1005`

---

### POST /api/v1/auth/sms/send
**功能：** 发送短信验证码  
**权限：** 🔓 公开  
**限流：** 同手机号 60 秒内只能发送 1 次

**Request Body：**
```json
{
  "phone":   "13800138000",
  "purpose": "LOGIN"          // 用途：LOGIN | BIND_WECHAT | RESET_PWD
}
```

**Response 200：**
```json
{
  "code": 200,
  "message": "验证码已发送",
  "data": { "expireSeconds": 300 },
  "timestamp": 1713600000000
}
```

**错误码：** `1006`

---

### POST /api/v1/auth/token/refresh
**功能：** 刷新 Access Token  
**权限：** 🔓 公开（携带 Refresh Token）

**Request Body：**
```json
{
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000"
}
```

**Response 200：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "accessToken": "eyJhbGciOiJSUzI1NiJ9...",
    "expiresIn":   7200
  },
  "timestamp": 1713600000000
}
```

**错误码：** `401` `4010`

---

### POST /api/v1/auth/logout
**功能：** 退出登录，吊销当前 Token  
**权限：** 🔑 已登录

**Request Body：** 无

**Response 200：**
```json
{ "code": 200, "message": "success", "data": null, "timestamp": 1713600000000 }
```

---

### GET /api/v1/auth/wechat/qrcode
**功能：** 获取微信扫码登录 / 绑定二维码  
**权限：** 🔓 公开  
**用途：** 电子签名微信扫码场景

**Query Params：**
```
purpose: LOGIN | SIGN_BIND   // SIGN_BIND=绑定微信用于电子签名
state:   string              // 随机字符串，防 CSRF
```

**Response 200：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "qrcodeUrl": "https://mp.weixin.qq.com/connect/qrconnect?...",
    "scene":     "qr_20240421_abc123",
    "expireSeconds": 120
  },
  "timestamp": 1713600000000
}
```

---

### GET /api/v1/auth/wechat/poll
**功能：** 轮询微信扫码结果  
**权限：** 🔓 公开

**Query Params：**
```
scene: qr_20240421_abc123
```

**Response 200：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "status": "WAITING",   // WAITING | SCANNED | SUCCESS | EXPIRED | FAILED
    "accessToken": null    // status=SUCCESS 时返回 Token
  },
  "timestamp": 1713600000000
}
```

---

### PUT /api/v1/auth/password/change
**功能：** 修改密码  
**权限：** 🔑 已登录

**Request Body：**
```json
{
  "oldPassword": "string",
  "newPassword": "string",   // 6~32位，必须包含数字和字母
  "confirmPassword": "string"
}
```

**Response 200：** 标准成功响应

---

## 6. 用户与权限模块

**服务：** `thesis-user`  
**路径前缀：** `/api/v1/user` | `/api/v1/role` | `/api/v1/teaching-point`

---

### POST /api/v1/user/import
**功能：** 批量导入用户（异步）  
**权限：** 👑 SCHOOL_ADMIN | 📍 POINT_ADMIN（仅限本教学点）  
**权限编码：** `perm:user:import`

**Request：** `multipart/form-data`
```
importType: STUDENT | TEACHER | ASSISTANT | TEACHING_POINT | RELATIONSHIP
file:       <Excel文件，.xlsx，最大5MB>
batchId:    long（可选，STUDENT类型时关联批次）
```

**Response 200：**
```json
{
  "code": 200,
  "message": "导入任务已提交，请通过taskId查询进度",
  "data": {
    "taskId":      "uuid-task-id-here",
    "totalCount":  null,
    "description": "正在解析文件..."
  },
  "timestamp": 1713600000000
}
```

---

### GET /api/v1/user/import/{taskId}/progress
**功能：** 查询导入任务进度  
**权限：** 👑 SCHOOL_ADMIN | 📍 POINT_ADMIN

**Path Params：** `taskId: string`

**Response 200：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "taskId":       "uuid-task-id-here",
    "status":       "PROCESSING",
    "totalCount":   500,
    "successCount": 320,
    "failCount":    5,
    "progress":     64,
    "errorFileUrl": null,
    "startedAt":    "2024-04-21T10:00:00+08:00",
    "finishedAt":   null
  },
  "timestamp": 1713600000000
}
```

**status 枚举：** `PENDING` `PROCESSING` `SUCCESS` `PARTIAL` `FAILED`

---

### GET /api/v1/user/list
**功能：** 查询用户列表  
**权限：** 👑 SCHOOL_ADMIN | 📍 POINT_ADMIN  
**权限编码：** `perm:user:list`

**Query Params：**
```
pageNum:          int     页码（默认1）
pageSize:         int     每页数量（默认20，最大100）
userType:         string  用户类型过滤（可选）
teachingPointId:  long    教学点ID过滤（可选，POINT_ADMIN自动限制本教学点）
keyword:          string  关键词搜索（姓名/账号/学号/工号，可选）
major:            string  专业过滤（STUDENT类型有效，可选）
status:           string  账号状态过滤（ACTIVE/LOCKED/INACTIVE，可选）
```

**Response 200：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 1250,
    "pages": 63,
    "current": 1,
    "size": 20,
    "records": [
      {
        "userId":           "1234567890",
        "username":         "2021001001",
        "realName":         "张三",
        "phone":            "138****8000",
        "userType":         "STUDENT",
        "studentNo":        "2021001001",
        "major":            "计算机科学与技术",
        "teachingPointId":  "200",
        "teachingPointName":"济南教学点",
        "status":           "ACTIVE",
        "lastLoginAt":      "2024-04-20T09:30:00+08:00",
        "createdAt":        "2024-09-01T08:00:00+08:00"
      }
    ]
  },
  "timestamp": 1713600000000
}
```

---

### POST /api/v1/user
**功能：** 单独添加用户  
**权限：** 👑 SCHOOL_ADMIN | 📍 POINT_ADMIN  
**权限编码：** `perm:user:create`

**Request Body：**
```json
{
  "userType":         "STUDENT",
  "realName":         "李四",
  "username":         "2021001002",
  "phone":            "13900139000",
  "password":         "Thesis@2024",
  "studentNo":        "2021001002",
  "major":            "计算机科学与技术",
  "teachingPointId":  200,
  "batchId":          100
}
```

**Schema：**
```yaml
CreateUserRequest:
  type: object
  required: [userType, realName, username, phone]
  properties:
    userType:
      $ref: '#/components/schemas/UserType'
    realName:
      type: string
      maxLength: 64
    username:
      type: string
      maxLength: 64
      description: 登录账号，学校内唯一
    phone:
      type: string
      pattern: '^1[3-9]\d{9}$'
    password:
      type: string
      description: 初始密码，不传则系统生成并短信通知
    studentNo:
      type: string
      maxLength: 32
      description: 学号（STUDENT 类型必填）
    teacherNo:
      type: string
      maxLength: 32
      description: 工号（TEACHER/ASSISTANT 类型必填）
    major:
      type: string
      description: 专业（STUDENT 类型必填）
    department:
      type: string
      description: 院系（TEACHER 类型可选）
    teachingPointId:
      type: integer
      format: int64
      description: 教学点ID（STUDENT/ASSISTANT 类型必填）
```

**Response 200：**
```json
{
  "code": 200,
  "message": "success",
  "data": { "userId": "1234567891" },
  "timestamp": 1713600000000
}
```

---

### PUT /api/v1/user/{userId}
**功能：** 修改用户信息  
**权限：** 👑 SCHOOL_ADMIN | 📍 POINT_ADMIN（本教学点用户）  
**权限编码：** `perm:user:edit`

**Path Params：** `userId: string`

**Request Body：**（字段均为可选，只传需要修改的字段）
```json
{
  "username":         "new_username",
  "phone":            "13800138001",
  "password":         "NewPassword@2024",
  "major":            "软件工程",
  "teachingPointId":  201,
  "status":           "ACTIVE"
}
```

**Response 200：** 标准成功响应

---

### DELETE /api/v1/user/{userId}
**功能：** 删除用户（逻辑删除）  
**权限：** 👑 SCHOOL_ADMIN  
**权限编码：** `perm:user:delete`

**Response 200：** 标准成功响应

---

### GET /api/v1/user/{userId}
**功能：** 用户详情  
**权限：** 👑 SCHOOL_ADMIN | 📍 POINT_ADMIN | 🔑 自己

**Response 200：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId":           "1234567890",
    "schoolId":         "100",
    "username":         "zhangsan",
    "realName":         "张三",
    "phone":            "138****8000",
    "email":            "zhangsan@example.com",
    "userType":         "TEACHER",
    "teacherNo":        "T20210001",
    "department":       "计算机学院",
    "title":            "副教授",
    "avatarUrl":        "https://oss.example.com/avatars/xxx.jpg",
    "status":           "ACTIVE",
    "lastLoginAt":      "2024-04-20T09:30:00+08:00",
    "roles":            ["论文指导教师套餐"],
    "createdAt":        "2021-09-01T08:00:00+08:00"
  },
  "timestamp": 1713600000000
}
```

---

### GET /api/v1/user/me
**功能：** 获取当前登录用户信息  
**权限：** 🔑 已登录

**Response 200：** 同 `/user/{userId}` 的 data 结构，附加 `permissions` 权限列表

---

### GET /api/v1/role/list
**功能：** 角色列表  
**权限：** 👑 SCHOOL_ADMIN  
**权限编码：** `perm:role:list`

**Response 200：**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "roleId":      "10",
      "roleCode":    "SCHOOL_ADMIN",
      "roleName":    "学校管理员",
      "isPreset":    true,
      "description": "系统最高权限角色",
      "permCount":   45
    },
    {
      "roleId":      "15",
      "roleCode":    "CUSTOM_ROLE_001",
      "roleName":    "审核专员",
      "isPreset":    false,
      "description": "仅具备论文审核权限",
      "permCount":   8
    }
  ],
  "timestamp": 1713600000000
}
```

---

### POST /api/v1/role
**功能：** 创建自定义角色  
**权限：** 👑 SCHOOL_ADMIN  
**权限编码：** `perm:role:create`

**Request Body：**
```json
{
  "roleCode":    "CUSTOM_ROLE_001",
  "roleName":    "审核专员",
  "description": "仅具备论文审核权限",
  "permIds":     [1, 2, 5, 8, 12]
}
```

**Response 200：**
```json
{ "code": 200, "message": "success", "data": { "roleId": "20" }, "timestamp": 1713600000000 }
```

---

### PUT /api/v1/role/{roleId}/permissions
**功能：** 更新角色权限  
**权限：** 👑 SCHOOL_ADMIN  
**权限编码：** `perm:role:edit`

**Request Body：**
```json
{
  "permIds": [1, 2, 5, 8, 12, 15]
}
```

---

### POST /api/v1/role/preset/{presetRoleCode}/apply
**功能：** 一键应用角色套餐（将预置角色权限应用至指定角色）  
**权限：** 👑 SCHOOL_ADMIN  
**权限编码：** `perm:role:edit`

**Path Params：** `presetRoleCode: string`（如 TEACHER、STUDENT）

**Request Body：**
```json
{
  "targetRoleId": 20
}
```

---

### POST /api/v1/user/{userId}/roles
**功能：** 为用户分配角色  
**权限：** 👑 SCHOOL_ADMIN  
**权限编码：** `perm:user:grant-role`

**Request Body：**
```json
{
  "roleIds": [10, 15]
}
```

---

### GET /api/v1/permission/tree
**功能：** 获取权限树（用于角色权限配置页面）  
**权限：** 👑 SCHOOL_ADMIN

**Response 200：**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "permId":    "1",
      "permCode":  "paper",
      "permName":  "论文管理",
      "permType":  "MENU",
      "module":    "PAPER",
      "children": [
        {
          "permId":   "2",
          "permCode": "paper:submit:read",
          "permName": "查看提交记录",
          "permType": "BUTTON",
          "module":   "PAPER",
          "children": []
        },
        {
          "permId":   "3",
          "permCode": "paper:review",
          "permName": "审核论文",
          "permType": "BUTTON",
          "module":   "PAPER",
          "children": []
        }
      ]
    }
  ],
  "timestamp": 1713600000000
}
```

---

### GET /api/v1/teaching-point/list
**功能：** 教学点列表  
**权限：** 👑 SCHOOL_ADMIN  
**权限编码：** `perm:teaching-point:list`

**Response 200：**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "teachingPointId": "200",
      "name":            "济南教学点",
      "code":            "JN001",
      "contactName":     "王主任",
      "contactPhone":    "053188888888",
      "studentCount":    256,
      "teacherCount":    18,
      "status":          "ACTIVE"
    }
  ],
  "timestamp": 1713600000000
}
```

---

### POST /api/v1/teaching-point
**功能：** 新增教学点  
**权限：** 👑 SCHOOL_ADMIN  
**权限编码：** `perm:teaching-point:create`

**Request Body：**
```json
{
  "name":         "青岛教学点",
  "code":         "QD001",
  "contactName":  "李主任",
  "contactPhone": "053266666666",
  "dataScope":    "POINT"
}
```


---

## 7. 批次管理模块

**服务：** `thesis-paper`  
**路径前缀：** `/api/v1/batch`

---

### GET /api/v1/batch/list
**功能：** 批次列表  
**权限：** 👑 SCHOOL_ADMIN | 📍 POINT_ADMIN | 👨‍🏫 TEACHER | 🎓 STUDENT

**Query Params：**
```
pageNum:    int    页码
pageSize:   int    每页数量
status:     string 批次状态过滤（DRAFT/ACTIVE/FINISHED/ARCHIVED，可选）
paperType:  string 论文类型（GRADUATION/DEGREE，可选）
keyword:    string 批次名称关键词搜索
```

**Response 200：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 8,
    "pages": 1,
    "current": 1,
    "size": 20,
    "records": [
      {
        "batchId":        "100",
        "name":           "2024年春季毕业论文批次",
        "academicYear":   "2023-2024",
        "semester":       "SPRING",
        "paperType":      "GRADUATION",
        "startTime":      "2024-03-01T08:00:00+08:00",
        "endTime":        "2024-06-30T18:00:00+08:00",
        "status":         "ACTIVE",
        "totalStudents":  256,
        "createdAt":      "2024-02-20T10:00:00+08:00"
      }
    ]
  },
  "timestamp": 1713600000000
}
```

---

### POST /api/v1/batch
**功能：** 创建批次  
**权限：** 👑 SCHOOL_ADMIN  
**权限编码：** `perm:batch:create`

**Request Body：**
```json
{
  "name":         "2024年秋季毕业论文批次",
  "academicYear": "2024-2025",
  "semester":     "AUTUMN",
  "paperType":    "GRADUATION",
  "startTime":    "2024-09-01T08:00:00+08:00",
  "endTime":      "2025-01-15T18:00:00+08:00",
  "description":  "本批次为2024年秋季毕业论文"
}
```

**Response 200：**
```json
{ "code": 200, "message": "success", "data": { "batchId": "101" }, "timestamp": 1713600000000 }
```

---

### PUT /api/v1/batch/{batchId}
**功能：** 修改批次基本信息  
**权限：** 👑 SCHOOL_ADMIN  
**权限编码：** `perm:batch:edit`

---

### DELETE /api/v1/batch/{batchId}
**功能：** 删除批次（逻辑删除，仅 DRAFT 状态可删除）  
**权限：** 👑 SCHOOL_ADMIN  
**权限编码：** `perm:batch:delete`

**错误码：** `2011`（批次状态不允许删除）

---

### GET /api/v1/batch/{batchId}/flow-config
**功能：** 获取批次流程节点配置  
**权限：** 🔑 已登录

**Response 200：**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "configId":          "1001",
      "nodeType":          "SIGN",
      "nodeName":          "电子签名",
      "sortOrder":         1,
      "isEnabled":         true,
      "isRequired":        true,
      "needGuide":         false,
      "needReview":        true,
      "allowResubmit":     false,
      "maxResubmitCount":  99,
      "startTime":         "2024-03-01T08:00:00+08:00",
      "endTime":           "2024-03-15T23:59:59+08:00",
      "reviewDeadline":    "2024-03-20T23:59:59+08:00",
      "minCommentLength":  0,
      "description":       "请完成电子签名后方可进入论文写作阶段",
      "teacherLevels": [
        {
          "levelId":            "2001",
          "level":              1,
          "levelName":          "教学点管理员",
          "allowReject":        true,
          "reviewMode":         "ALL",
          "sampleRate":         null,
          "pushToStudent":      true,
          "isFinalLevel":       true,
          "autoApproveHours":   null
        }
      ]
    },
    {
      "configId":         "1002",
      "nodeType":         "TOPIC",
      "nodeName":         "论文选题",
      "sortOrder":        2,
      "isEnabled":        true,
      "isRequired":       true,
      "needGuide":        false,
      "needReview":       true,
      "minCommentLength": 50,
      "startTime":        "2024-03-15T08:00:00+08:00",
      "endTime":          "2024-03-31T23:59:59+08:00",
      "teacherLevels": [
        {
          "level":        1,
          "levelName":    "指导教师",
          "allowReject":  true,
          "reviewMode":   "ALL",
          "pushToStudent": true,
          "isFinalLevel": true
        }
      ]
    }
  ],
  "timestamp": 1713600000000
}
```

---

### PUT /api/v1/batch/{batchId}/flow-config
**功能：** 更新批次流程配置（整体更新，传完整配置列表）  
**权限：** 👑 SCHOOL_ADMIN  
**权限编码：** `perm:batch:flow-config`

**Request Body：**
```json
{
  "flowConfigs": [
    {
      "nodeType":         "SIGN",
      "nodeName":         "电子签名",
      "sortOrder":        1,
      "isEnabled":        true,
      "isRequired":       true,
      "needGuide":        false,
      "needReview":       true,
      "allowResubmit":    false,
      "startTime":        "2024-03-01T08:00:00+08:00",
      "endTime":          "2024-03-15T23:59:59+08:00",
      "minCommentLength": 0,
      "teacherLevels": [
        {
          "level":          1,
          "levelName":      "教学点管理员",
          "allowReject":    true,
          "reviewMode":     "ALL",
          "pushToStudent":  true,
          "isFinalLevel":   true
        }
      ]
    }
  ]
}
```

**Schema：**
```yaml
UpdateFlowConfigRequest:
  type: object
  required: [flowConfigs]
  properties:
    flowConfigs:
      type: array
      items:
        type: object
        required: [nodeType, nodeName, sortOrder, startTime, endTime]
        properties:
          nodeType:
            $ref: '#/components/schemas/FlowNodeType'
          nodeName:
            type: string
            maxLength: 64
          sortOrder:
            type: integer
            minimum: 1
          isEnabled:
            type: boolean
            default: true
          isRequired:
            type: boolean
            default: true
          needGuide:
            type: boolean
            default: false
          needReview:
            type: boolean
            default: true
          allowResubmit:
            type: boolean
            default: true
          maxResubmitCount:
            type: integer
            default: 99
          startTime:
            type: string
            format: date-time
          endTime:
            type: string
            format: date-time
          reviewDeadline:
            type: string
            format: date-time
            nullable: true
          minCommentLength:
            type: integer
            minimum: 0
            default: 0
          description:
            type: string
            maxLength: 512
          teacherLevels:
            type: array
            items:
              $ref: '#/components/schemas/TeacherLevelConfig'

TeacherLevelConfig:
  type: object
  required: [level, levelName, reviewMode, isFinalLevel]
  properties:
    level:
      type: integer
      minimum: 1
      description: 导师层级（1=第一级，数字越小越先审核）
    levelName:
      type: string
      maxLength: 64
    allowReject:
      type: boolean
      default: true
    reviewMode:
      type: string
      enum: [ALL, SAMPLE]
      default: ALL
    sampleRate:
      type: number
      format: double
      minimum: 0.01
      maximum: 1.0
      nullable: true
      description: 抽检比例（reviewMode=SAMPLE时必填）
    pushToStudent:
      type: boolean
      default: true
    isFinalLevel:
      type: boolean
    autoApproveHours:
      type: integer
      nullable: true
```

---

### POST /api/v1/batch/{batchId}/students
**功能：** 批量添加学生至批次（同步小批量，异步大批量）  
**权限：** 👑 SCHOOL_ADMIN | 📍 POINT_ADMIN  
**权限编码：** `perm:batch:add-student`

**Request Body：**
```json
{
  "studentIds": ["1234567890", "1234567891", "1234567892"]
}
```

**Response 200：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "addedCount":   3,
    "skippedCount": 0,
    "skipReasons":  []
  },
  "timestamp": 1713600000000
}
```

---

### POST /api/v1/batch/{batchId}/relationship/import
**功能：** 批量导入指导关系（异步）  
**权限：** 👑 SCHOOL_ADMIN | 📍 POINT_ADMIN  
**权限编码：** `perm:relationship:import`

**Request：** `multipart/form-data`
```
file:         <Excel文件，模板格式：学号/工号/教师类型/层级>
teacherType:  MAIN | ASSIST
```

**Response 200：** 同 `/user/import`，返回 `taskId`

---

### POST /api/v1/batch/{batchId}/relationship
**功能：** 单个配置指导关系  
**权限：** 👑 SCHOOL_ADMIN | 📍 POINT_ADMIN  
**权限编码：** `perm:relationship:create`

**Request Body：**
```json
{
  "studentId":   "1234567890",
  "teacherId":   "9876543210",
  "teacherType": "MAIN",
  "level":       1
}
```

---

## 8. 论文流程模块

**服务：** `thesis-paper`  
**路径前缀：** `/api/v1/paper`

---

### GET /api/v1/paper/list
**功能：** 论文列表（管理端/教师端，支持多维度筛选）  
**权限：** 👑 SCHOOL_ADMIN | 📍 POINT_ADMIN | 👨‍🏫 TEACHER | 👩‍🏫 ASSISTANT  
**权限编码：** `perm:paper:list`

**Query Params：**
```
pageNum:          int     页码
pageSize:         int     每页数量
batchId:          long    批次ID（必填）
teachingPointId:  long    教学点ID（可选，POINT_ADMIN自动限制）
major:            string  专业（可选）
teacherId:        long    指导教师ID（可选）
nodeType:         string  当前节点类型（可选）
nodeStatus:       string  节点状态（可选）
keyword:          string  关键词（学生姓名/学号，可选）
overallStatus:    string  整体状态（可选）
```

**Response 200：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 256,
    "pages": 13,
    "current": 1,
    "size": 20,
    "records": [
      {
        "paperId":          "5001",
        "studentId":        "1234567890",
        "studentName":      "张三",
        "studentNo":        "2021001001",
        "major":            "计算机科学与技术",
        "teachingPointName":"济南教学点",
        "currentNode":      "OUTLINE",
        "overallStatus":    "IN_PROGRESS",
        "wordCount":        3500,
        "topicTitle":       "基于深度学习的图像识别系统设计与实现",
        "totalScore":       null,
        "updatedAt":        "2024-04-20T16:30:00+08:00"
      }
    ]
  },
  "timestamp": 1713600000000
}
```

---

### GET /api/v1/paper/my
**功能：** 学生查看自己的论文（含当前节点状态）  
**权限：** 🎓 STUDENT

**Query Params：**
```
batchId: long（必填）
```

**Response 200：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "paperId":       "5001",
    "batchId":       "100",
    "batchName":     "2024年春季毕业论文批次",
    "overallStatus": "IN_PROGRESS",
    "currentNode":   "DRAFT_FULL",
    "wordCount":     12500,
    "signConfirmed": true,
    "topicTitle":    "基于深度学习的图像识别系统",
    "nodes": [
      {
        "nodeType":       "SIGN",
        "nodeName":       "电子签名",
        "status":         "APPROVED",
        "sortOrder":      1,
        "startTime":      "2024-03-01T08:00:00+08:00",
        "endTime":        "2024-03-15T23:59:59+08:00",
        "approvedAt":     "2024-03-10T14:20:00+08:00"
      },
      {
        "nodeType":  "TOPIC",
        "nodeName":  "论文选题",
        "status":    "APPROVED",
        "sortOrder": 2,
        "startTime": "2024-03-15T08:00:00+08:00",
        "endTime":   "2024-03-31T23:59:59+08:00"
      },
      {
        "nodeType":      "DRAFT_FULL",
        "nodeName":      "初稿",
        "status":        "REVIEWING",
        "sortOrder":     6,
        "submitCount":   1,
        "lastSubmitAt":  "2024-04-18T10:00:00+08:00",
        "startTime":     "2024-04-01T08:00:00+08:00",
        "endTime":       "2024-04-30T23:59:59+08:00"
      }
    ],
    "mainTeacher": {
      "teacherId":  "9876543210",
      "realName":   "李教授",
      "title":      "教授",
      "department": "计算机学院"
    },
    "assistTeacher": {
      "teacherId":  "9876543211",
      "realName":   "王老师"
    }
  },
  "timestamp": 1713600000000
}
```

---

### GET /api/v1/paper/{paperId}
**功能：** 论文详情（含节点状态和基本信息）  
**权限：** 🔑 已登录（数据权限校验）

---

### POST /api/v1/paper/{paperId}/sign
**功能：** 学生/教师提交电子签名  
**权限：** 🎓 STUDENT | 👨‍🏫 TEACHER

**Request：** `multipart/form-data`
```
batchId:  long    批次ID
signType: string  WECHAT | UPLOAD
imageFile: file   签名图片（UPLOAD类型，JPG/PNG，最大2MB）
```

**Response 200：**
```json
{
  "code": 200,
  "message": "签名提交成功，等待审核",
  "data": { "signatureId": "3001" },
  "timestamp": 1713600000000
}
```

---

### PUT /api/v1/paper/{paperId}/sign/review
**功能：** 审核电子签名（教学点管理员操作）  
**权限：** 📍 POINT_ADMIN | 👑 SCHOOL_ADMIN  
**权限编码：** `perm:sign:review`

**Request Body：**
```json
{
  "signatureId":  "3001",
  "decision":     "APPROVED",
  "rejectReason": null
}
```

---

### POST /api/v1/paper/{paperId}/topic
**功能：** 学生提交/更新选题  
**权限：** 🎓 STUDENT

**Request Body：**
```json
{
  "title":         "基于深度学习的图像识别系统设计与实现",
  "titleEn":       "Design and Implementation of Image Recognition System Based on Deep Learning",
  "keywords":      "深度学习,图像识别,卷积神经网络",
  "researchField": "人工智能",
  "source":        "SELF",
  "presetTopicId": null
}
```

**Schema：**
```yaml
SubmitTopicRequest:
  type: object
  required: [title, source]
  properties:
    title:
      type: string
      maxLength: 512
      description: 选题题目
    titleEn:
      type: string
      maxLength: 512
      nullable: true
    keywords:
      type: string
      maxLength: 512
      description: 关键词（逗号分隔，最多5个）
    researchField:
      type: string
      maxLength: 128
    source:
      type: string
      enum: [SELF, AI, PRESET]
    presetTopicId:
      type: integer
      format: int64
      nullable: true
      description: 教师预设选题ID（source=PRESET时必填）
```

**Response 200：**
```json
{
  "code": 200,
  "message": "选题提交成功",
  "data": { "topicId": "4001" },
  "timestamp": 1713600000000
}
```

**错误码：** `2001` `2002` `2006`

---

### GET /api/v1/paper/{paperId}/outline
**功能：** 获取论文大纲（当前版本）  
**权限：** 🔑 已登录（数据权限校验）

**Response 200：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "outlineId":  "6001",
    "paperId":    "5001",
    "source":     "AI",
    "templateId": null,
    "version":    3,
    "nodeCount":  12,
    "outlineJson": [
      {
        "id":     "node_001",
        "level":  1,
        "title":  "第一章 绪论",
        "remark": "本章应包含研究背景、目的、方法和主要内容",
        "sort":   1,
        "children": [
          {
            "id":     "node_002",
            "level":  2,
            "title":  "1.1 研究背景",
            "remark": "",
            "sort":   1,
            "children": []
          }
        ]
      }
    ],
    "updatedAt": "2024-04-15T09:00:00+08:00"
  },
  "timestamp": 1713600000000
}
```

---

### PUT /api/v1/paper/{paperId}/outline
**功能：** 保存/更新论文大纲（支持自动保存）  
**权限：** 🎓 STUDENT

**Request Body：**
```json
{
  "outlineJson": [
    {
      "id":     "node_001",
      "level":  1,
      "title":  "第一章 绪论",
      "remark": "",
      "sort":   1,
      "children": []
    }
  ],
  "isAutoSave": true
}
```

**Response 200：**
```json
{
  "code": 200,
  "message": "success",
  "data": { "version": 4, "savedAt": "2024-04-21T10:30:00+08:00" },
  "timestamp": 1713600000000
}
```

---

### GET /api/v1/paper/{paperId}/content/{nodeId}
**功能：** 获取论文章节内容  
**权限：** 🔑 已登录（数据权限校验）

**Path Params：** `paperId: string`、`nodeId: string`（大纲节点ID）

**Response 200：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "contentId":    "7001",
    "nodeId":       "node_001",
    "nodeTitle":    "第一章 绪论",
    "contentHtml":  "<p>随着人工智能技术的快速发展...</p>",
    "wordCount":    1250,
    "version":      5,
    "autoSavedAt":  "2024-04-21T10:29:00+08:00"
  },
  "timestamp": 1713600000000
}
```

---

### PUT /api/v1/paper/{paperId}/content/{nodeId}
**功能：** 保存章节内容（支持自动保存，60秒一次）  
**权限：** 🎓 STUDENT

**Request Body：**
```json
{
  "contentHtml": "<p>随着人工智能技术的快速发展...</p>",
  "wordCount":   1250,
  "isAutoSave":  true
}
```

**Response 200：**
```json
{
  "code": 200,
  "message": "success",
  "data": { "version": 6, "savedAt": "2024-04-21T10:30:00+08:00" },
  "timestamp": 1713600000000
}
```

---

### POST /api/v1/paper/{paperId}/submit
**功能：** 提交论文节点（整篇或分段）  
**权限：** 🎓 STUDENT

**Request Body：**
```json
{
  "nodeType":    "DRAFT_FULL",
  "submitType":  "FULL",
  "segmentNodeId": null,
  "remark":      "初稿完成，请老师审阅"
}
```

**Schema：**
```yaml
SubmitPaperRequest:
  type: object
  required: [nodeType, submitType]
  properties:
    nodeType:
      $ref: '#/components/schemas/FlowNodeType'
    submitType:
      type: string
      enum: [FULL, SEGMENT]
      description: FULL-整篇提交 SEGMENT-分段提交
    segmentNodeId:
      type: string
      nullable: true
      description: 分段提交时的大纲节点ID（submitType=SEGMENT时必填）
    remark:
      type: string
      maxLength: 512
      nullable: true
      description: 提交备注（可选）
```

**Response 200：**
```json
{
  "code": 200,
  "message": "论文提交成功，等待教师审核",
  "data": { "submitRecordId": "8001", "nodeStatus": "SUBMITTED" },
  "timestamp": 1713600000000
}
```

**错误码：** `2001` `2002` `2003` `2004` `2007` `2008`

---

### POST /api/v1/paper/{paperId}/submit/file
**功能：** 文件类节点上传提交（选题登记表、任务书）  
**权限：** 🎓 STUDENT

**Request：** `multipart/form-data`
```
nodeType:  string   TOPIC_FORM | TASK_BOOK
file:      file     DOCX/PDF，最大20MB
remark:    string   备注（可选）
```

---

### POST /api/v1/paper/{paperId}/withdraw
**功能：** 撤回上次提交（仅状态为SUBMITTED时可撤回）  
**权限：** 🎓 STUDENT

**Request Body：**
```json
{
  "nodeType": "DRAFT_FULL"
}
```

---

### PUT /api/v1/paper/{paperId}/review
**功能：** 教师审核论文节点  
**权限：** 👨‍🏫 TEACHER | 👩‍🏫 ASSISTANT  
**权限编码：** `perm:paper:review`

**Request Body：**
```json
{
  "nodeType":   "DRAFT_FULL",
  "decision":   "REJECTED",
  "comment":    "论文第三章内容较为单薄，建议补充实验数据和对比分析，参考文献格式需按学校规范调整。摘要部分需要包含研究结论。",
  "score":      null
}
```

**Schema：**
```yaml
ReviewPaperRequest:
  type: object
  required: [nodeType, decision, comment]
  properties:
    nodeType:
      $ref: '#/components/schemas/FlowNodeType'
    decision:
      $ref: '#/components/schemas/ReviewDecision'
    comment:
      type: string
      minLength: 0
      maxLength: 5000
      description: 审核意见/指导建议（长度需满足 min_comment_length 要求）
    score:
      type: number
      format: double
      nullable: true
      description: 评分（仅终稿节点有评分方案时填写）
```

**Response 200：**
```json
{
  "code": 200,
  "message": "审核完成",
  "data": {
    "reviewRecordId": "9001",
    "nodeStatus":     "REJECTED",
    "nextAction":     "等待学生修改后重新提交"
  },
  "timestamp": 1713600000000
}
```

**错误码：** `2005` `2007` `2012`

---

### POST /api/v1/paper/{paperId}/deadline-extension
**功能：** 开启逾期提交通道（指导教师操作）  
**权限：** 👨‍🏫 TEACHER  
**权限编码：** `perm:paper:deadline-ext`

**Request Body：**
```json
{
  "nodeType":    "DRAFT_FULL",
  "extendHours": 48,
  "reason":      "该同学因家庭原因无法按时提交，特批延期48小时"
}
```

**Response 200：**
```json
{
  "code": 200,
  "message": "逾期通道已开启",
  "data": {
    "extensionId": "11001",
    "expireAt":    "2024-05-02T10:00:00+08:00"
  },
  "timestamp": 1713600000000
}
```

---

### POST /api/v1/paper/import/docx
**功能：** 整篇论文一键导入（DOCX 解析）  
**权限：** 🎓 STUDENT  
**权限编码：** `perm:paper:import`

**Request：** `multipart/form-data`
```
paperId:   long    论文ID
file:      file    DOCX文件，最大50MB
```

**Response 200：**
```json
{
  "code": 200,
  "message": "论文解析完成",
  "data": {
    "taskId":       "import-task-001",
    "chapterCount": 5,
    "wordCount":    15000,
    "imageCount":   8,
    "tableCount":   3,
    "warningMsg":   null
  },
  "timestamp": 1713600000000
}
```

**错误码：** `3005`

---

### GET /api/v1/paper/{paperId}/export
**功能：** 导出标准格式论文（一键导出）  
**权限：** 🎓 STUDENT  
**权限编码：** `perm:paper:export`

**Query Params：**
```
templateId: long    论文格式模板ID
format:     string  DOCX | PDF（默认DOCX）
```

**Response 200：**
```json
{
  "code": 200,
  "message": "导出成功",
  "data": {
    "downloadUrl":  "https://oss.example.com/exports/paper_5001_20240421.docx?token=xxx",
    "expireAt":     "2024-04-21T10:30:00+08:00",
    "fileName":     "张三_基于深度学习的图像识别系统_2024届.docx",
    "fileSize":     2048576
  },
  "timestamp": 1713600000000
}
```

**错误码：** `3005`

---

### GET /api/v1/paper/{paperId}/history
**功能：** 查看论文操作时间线  
**权限：** 🔑 已登录（数据权限校验）

**Query Params：**
```
nodeType: string  按节点过滤（可选）
```

**Response 200：**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "logId":        "12001",
      "opType":       "SUBMIT",
      "nodeType":     "DRAFT_FULL",
      "operatorName": "张三",
      "operatorType": "STUDENT",
      "beforeStatus": "PENDING",
      "afterStatus":  "SUBMITTED",
      "opDetail":     { "submitType": "FULL", "wordCount": 12500 },
      "createdAt":    "2024-04-18T10:00:00+08:00"
    },
    {
      "logId":        "12002",
      "opType":       "REVIEW_REJECT",
      "nodeType":     "DRAFT_FULL",
      "operatorName": "李教授",
      "operatorType": "TEACHER",
      "beforeStatus": "REVIEWING",
      "afterStatus":  "REJECTED",
      "opDetail":     { "decision": "REJECTED", "commentLength": 150 },
      "createdAt":    "2024-04-20T14:30:00+08:00"
    }
  ],
  "timestamp": 1713600000000
}
```

---

### GET /api/v1/paper/{paperId}/review-records
**功能：** 查看论文审核记录（含历次评语）  
**权限：** 🔑 已登录（STUDENT 只能看对自己推送的评语）

**Query Params：**
```
nodeType: string  节点类型过滤（可选）
```

**Response 200：**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "reviewId":            "9001",
      "nodeType":            "DRAFT_FULL",
      "teacherName":         "李教授",
      "teacherLevel":        1,
      "decision":            "REJECTED",
      "comment":             "论文第三章内容较为单薄...",
      "isVisibleToStudent":  true,
      "reviewedAt":          "2024-04-20T14:30:00+08:00"
    }
  ],
  "timestamp": 1713600000000
}
```


---

## 9. 在线批注与评分模块

**服务：** `thesis-paper`  
**路径前缀：** `/api/v1/annotation` | `/api/v1/score`

---

### GET /api/v1/annotation/list
**功能：** 获取论文批注列表（PDF 渲染时调用）  
**权限：** 👨‍🏫 TEACHER | 👩‍🏫 ASSISTANT | 🎓 STUDENT（仅可见的批注）

**Query Params：**
```
submitRecordId: long   提交记录ID（必填）
pageNo:         int    页码过滤（可选，按页加载）
```

**Response 200：**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "annotationId":       "13001",
      "teacherName":        "李教授",
      "annotType":          "TEXT_HIGHLIGHT",
      "pageNo":             3,
      "positionJson":       { "x1": 100, "y1": 200, "x2": 400, "y2": 220 },
      "selectedText":       "本文提出了一种新的...",
      "highlightColor":     "#FFEB3B",
      "comment":            "此处论述逻辑不清晰，建议补充实验数据支撑",
      "isResolved":         false,
      "isVisibleToStudent": true,
      "createdAt":          "2024-04-20T15:00:00+08:00"
    }
  ],
  "timestamp": 1713600000000
}
```

---

### POST /api/v1/annotation
**功能：** 添加批注  
**权限：** 👨‍🏫 TEACHER | 👩‍🏫 ASSISTANT  
**权限编码：** `perm:paper:annotate`

**Request Body：**
```json
{
  "submitRecordId":    "8001",
  "annotType":         "TEXT_HIGHLIGHT",
  "pageNo":            3,
  "positionJson":      { "x1": 100, "y1": 200, "x2": 400, "y2": 220 },
  "selectedText":      "本文提出了一种新的...",
  "highlightColor":    "#FFEB3B",
  "comment":           "此处论述逻辑不清晰，建议补充实验数据支撑",
  "isVisibleToStudent": true
}
```

**Schema：**
```yaml
CreateAnnotationRequest:
  type: object
  required: [submitRecordId, annotType, pageNo, positionJson, comment]
  properties:
    submitRecordId:
      type: integer
      format: int64
    annotType:
      type: string
      enum: [TEXT_HIGHLIGHT, AREA_BOX, FREE_TEXT]
    pageNo:
      type: integer
      minimum: 1
    positionJson:
      type: object
      required: [x1, y1, x2, y2]
      properties:
        x1: { type: number }
        y1: { type: number }
        x2: { type: number }
        y2: { type: number }
      description: PDF 坐标系（pt 单位）
    selectedText:
      type: string
      nullable: true
      maxLength: 2000
    highlightColor:
      type: string
      pattern: '^#[0-9A-Fa-f]{6}$'
      default: "#FFEB3B"
    comment:
      type: string
      maxLength: 2000
    isVisibleToStudent:
      type: boolean
      default: true
```

**Response 200：**
```json
{
  "code": 200,
  "message": "success",
  "data": { "annotationId": "13002" },
  "timestamp": 1713600000000
}
```

---

### DELETE /api/v1/annotation/{annotationId}
**功能：** 删除批注（撤回，仅批注人可删除）  
**权限：** 👨‍🏫 TEACHER | 👩‍🏫 ASSISTANT

---

### PUT /api/v1/annotation/{annotationId}/resolve
**功能：** 标记批注为已处理（学生操作）  
**权限：** 🎓 STUDENT

**Request Body：**
```json
{ "isResolved": true }
```

---

### GET /api/v1/score/scheme/list
**功能：** 获取评分方案列表  
**权限：** 👑 SCHOOL_ADMIN | 👨‍🏫 TEACHER

**Response 200：**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "schemeId":       "1",
      "name":           "2024届毕业论文评分标准",
      "paperType":      "GRADUATION",
      "totalScore":     100.0,
      "passScore":      60.0,
      "isActive":       true,
      "items": [
        {
          "itemId":      "1",
          "itemName":    "选题合理性",
          "maxScore":    20.0,
          "description": "选题是否符合专业培养目标，是否具有实际意义",
          "isRequired":  true,
          "sortOrder":   1
        },
        {
          "itemId":      "2",
          "itemName":    "内容质量",
          "maxScore":    40.0,
          "description": "论文内容是否完整、论证是否充分、数据是否准确",
          "isRequired":  true,
          "sortOrder":   2
        },
        {
          "itemId":      "3",
          "itemName":    "格式规范性",
          "maxScore":    20.0,
          "description": "格式是否符合学校模板，参考文献是否规范",
          "isRequired":  true,
          "sortOrder":   3
        },
        {
          "itemId":      "4",
          "itemName":    "创新性",
          "maxScore":    20.0,
          "description": "是否有创新点或新颖的研究视角",
          "isRequired":  true,
          "sortOrder":   4
        }
      ]
    }
  ],
  "timestamp": 1713600000000
}
```

---

### POST /api/v1/score
**功能：** 教师提交论文评分  
**权限：** 👨‍🏫 TEACHER  
**权限编码：** `perm:paper:score`

**Request Body：**
```json
{
  "paperId":    "5001",
  "schemeId":   "1",
  "nodeType":   "FINAL",
  "itemScores": [
    { "itemId": "1", "score": 18.0 },
    { "itemId": "2", "score": 35.0 },
    { "itemId": "3", "score": 17.0 },
    { "itemId": "4", "score": 15.0 }
  ],
  "overallComment": "该论文选题合适，内容充实，实验数据完整，分析较为深入。格式基本规范，参考文献引用正确。建议后续在创新性方面进一步加强。综合评定良好。"
}
```

**Response 200：**
```json
{
  "code": 200,
  "message": "评分提交成功",
  "data": {
    "scoreId":    "14001",
    "totalScore": 85.0,
    "scoreLevel": "GOOD",
    "isLocked":   true
  },
  "timestamp": 1713600000000
}
```

**错误码：** `2005`（评语不足最低字数）`2010`（已锁定）

---

### GET /api/v1/score/{paperId}
**功能：** 查看论文评分详情  
**权限：** 🔑 已登录（数据权限校验）

---

### PUT /api/v1/score/{scoreId}/unlock
**功能：** 管理员解锁评分（允许重新评分）  
**权限：** 👑 SCHOOL_ADMIN  
**权限编码：** `perm:score:unlock`

---

## 10. 模板管理模块

**服务：** `thesis-template`  
**路径前缀：** `/api/v1/template`

---

### GET /api/v1/template/paper/list
**功能：** 论文格式模板列表  
**权限：** 🔑 已登录

**Query Params：**
```
paperType:      string  GRADUATION | DEGREE | ALL
educationLevel: string  UNDERGRADUATE | JUNIOR_COLLEGE | ALL
```

**Response 200：**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "templateId":     "1",
      "name":           "本科毕业论文标准模板2024版",
      "paperType":      "GRADUATION",
      "educationLevel": "UNDERGRADUATE",
      "isDefault":      true,
      "previewUrl":     "https://oss.example.com/templates/preview_1.png"
    }
  ],
  "timestamp": 1713600000000
}
```

---

### POST /api/v1/template/paper
**功能：** 上传论文格式模板  
**权限：** 👑 SCHOOL_ADMIN  
**权限编码：** `perm:template:paper:upload`

**Request：** `multipart/form-data`
```
name:           string  模板名称
paperType:      string  GRADUATION | DEGREE | ALL
educationLevel: string  适用学历层次
majorIds:       string  专业ID列表（逗号分隔，空=所有专业）
isDefault:      bool    是否设为默认模板
file:           file    DOCX模板文件，最大10MB
```

---

### GET /api/v1/template/outline/list
**功能：** 获取大纲模板列表（按专业）  
**权限：** 🔑 已登录

**Query Params：**
```
major: string  专业名称（必填）
```

**Response 200：**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "templateId":  "10",
      "major":       "计算机科学与技术",
      "name":        "计算机专业标准大纲模板A",
      "description": "适用于软件开发类选题",
      "nodeCount":   12,
      "outlineJson": [
        {
          "id":     "node_001",
          "level":  1,
          "title":  "第一章 绪论",
          "remark": "本章应包含研究背景（500字以上）、研究目的、研究方法和论文结构",
          "sort":   1,
          "children": []
        }
      ]
    }
  ],
  "timestamp": 1713600000000
}
```

---

### GET /api/v1/template/misc/{templateType}
**功能：** 获取选题登记表/任务书模板下载链接  
**权限：** 🔑 已登录

**Path Params：** `templateType: TOPIC_FORM | TASK_BOOK`

**Response 200：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "templateId":  "5",
    "name":        "曲阜师范大学选题登记表2024版",
    "downloadUrl": "https://oss.example.com/templates/topic_form_2024.docx?token=xxx",
    "expireAt":    "2024-04-21T11:00:00+08:00",
    "version":     "2024.1"
  },
  "timestamp": 1713600000000
}
```

---

### GET /api/v1/template/score-scheme
**功能：** 获取当前启用的评分方案详情  
**权限：** 🔑 已登录

---

### POST /api/v1/template/score-scheme
**功能：** 创建/更新评分方案  
**权限：** 👑 SCHOOL_ADMIN  
**权限编码：** `perm:template:score:manage`

**Request Body：**
```json
{
  "name":         "2024届毕业论文评分标准",
  "paperType":    "GRADUATION",
  "passScore":    60.0,
  "goodScore":    75.0,
  "excellentScore": 90.0,
  "items": [
    {
      "itemName":    "选题合理性",
      "maxScore":    20.0,
      "description": "选题是否符合专业培养目标",
      "isRequired":  true,
      "sortOrder":   1
    }
  ]
}
```

---

## 11. 即时通讯模块

**服务：** `thesis-im`  
**路径前缀：** `/api/v1/im`  
> 实时消息收发通过 WebSocket 实现，见 §17。本节仅包含 HTTP 管理接口。

---

### GET /api/v1/im/group/list
**功能：** 获取当前用户的群组列表  
**权限：** 🔑 已登录

**Query Params：**
```
batchId: long  批次ID过滤（可选）
```

**Response 200：**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "groupId":       "2001",
      "name":          "2024春季-张三-李教授 指导群",
      "groupType":     "GUIDE",
      "memberCount":   3,
      "unreadCount":   5,
      "lastMessage": {
        "msgType":    "TEXT",
        "content":    "好的，我明白了，我会尽快修改",
        "senderName": "张三",
        "sentAt":     "2024-04-21T09:55:00+08:00"
      }
    },
    {
      "groupId":    "2002",
      "name":       "2024年春季毕业论文-批次大群",
      "groupType":  "BATCH",
      "memberCount":256,
      "unreadCount": 12
    }
  ],
  "timestamp": 1713600000000
}
```

---

### POST /api/v1/im/group
**功能：** 创建自定义群组  
**权限：** 👑 SCHOOL_ADMIN | 👨‍🏫 TEACHER  
**权限编码：** `perm:im:group:create`

**Request Body：**
```json
{
  "name":      "计算机专业答辩交流群",
  "memberIds": ["1234567890", "9876543210", "1111111111"]
}
```

---

### GET /api/v1/im/group/{groupId}/messages
**功能：** 获取群组历史消息（游标翻页）  
**权限：** 🔑 已登录（需是群成员）  
**权限编码：** 管理员可查任意群组（`perm:im:message:view-all`）

**Query Params：**
```
cursor:   string  游标（最后一条消息的 created_at，首次不传）
size:     int     每次加载数量（默认50，最大100）
direction: string UP（加载更早）| DOWN（加载更新，默认UP）
```

**Response 200：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "hasMore": true,
    "nextCursor": "2024-04-21T09:00:00.000+08:00",
    "messages": [
      {
        "msgId":      "3001",
        "senderId":   "1234567890",
        "senderName": "张三",
        "senderType": "STUDENT",
        "msgType":    "TEXT",
        "content":    "老师，我已经修改好了第三章，请您再看一下",
        "isFiltered": false,
        "isRecalled": false,
        "sentAt":     "2024-04-21T09:30:00+08:00"
      },
      {
        "msgId":      "3002",
        "senderId":   "9876543210",
        "senderName": "李教授",
        "senderType": "TEACHER",
        "msgType":    "FILE",
        "content":    "",
        "fileUrl":    "https://oss.example.com/im/files/review_guide.pdf",
        "fileName":   "论文修改指导说明.pdf",
        "fileSize":   524288,
        "isFiltered": false,
        "sentAt":     "2024-04-21T09:45:00+08:00"
      }
    ]
  },
  "timestamp": 1713600000000
}
```

---

### DELETE /api/v1/im/message/{msgId}
**功能：** 撤回消息（发送后2分钟内）  
**权限：** 🔑 已登录（仅撤回自己的消息）

**错误码：** `4005`

---

### GET /api/v1/im/group/{groupId}/members
**功能：** 获取群成员列表  
**权限：** 🔑 已登录（需是群成员）

---

### PUT /api/v1/im/group/{groupId}/mute
**功能：** 设置群禁言状态（全群/单人）  
**权限：** 👑 SCHOOL_ADMIN | 群主

**Request Body：**
```json
{
  "isMuted":  true,
  "userId":   null,   // null=全群禁言，指定userId=单人禁言
  "reason":   "答辩进行中，请保持安静"
}
```

---

### GET /api/v1/im/sensitive-word/list
**功能：** 敏感词库列表  
**权限：** 👑 SCHOOL_ADMIN  
**权限编码：** `perm:im:sensitive-word:manage`

---

### POST /api/v1/im/sensitive-word
**功能：** 添加敏感词  
**权限：** 👑 SCHOOL_ADMIN  
**权限编码：** `perm:im:sensitive-word:manage`

**Request Body：**
```json
{
  "word":     "示例敏感词",
  "category": "CUSTOM",
  "level":    1
}
```

---

## 12. 在线答辩模块

**服务：** `thesis-defense`  
**路径前缀：** `/api/v1/defense`

---

### GET /api/v1/defense/group/list
**功能：** 答辩组列表  
**权限：** 👑 SCHOOL_ADMIN | 📍 POINT_ADMIN | 👨‍🏫 TEACHER | 🎓 STUDENT

**Query Params：**
```
batchId:      long    批次ID（必填）
defenseType:  string  SYNC | ASYNC（可选）
status:       string  PENDING | IN_PROGRESS | FINISHED（可选）
```

**Response 200：**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "groupId":          "20001",
      "name":             "计算机专业第一答辩组",
      "defenseType":      "SYNC",
      "defenseTime":      "2024-05-15T09:00:00+08:00",
      "durationMinutes":  180,
      "hostName":         "王主持",
      "judgeCount":       3,
      "studentCount":     8,
      "status":           "PENDING",
      "isRecording":      true,
      "viewMode":         "GRID",
      "recordingUrl":     null
    }
  ],
  "timestamp": 1713600000000
}
```

---

### POST /api/v1/defense/group
**功能：** 创建答辩组  
**权限：** 👑 SCHOOL_ADMIN  
**权限编码：** `perm:defense:group:create`

**Request Body：**
```json
{
  "batchId":         "100",
  "name":            "计算机专业第一答辩组",
  "defenseType":     "SYNC",
  "defenseTime":     "2024-05-15T09:00:00+08:00",
  "durationMinutes": 180,
  "hostUserId":      "9876543210",
  "isRecording":     true,
  "viewMode":        "GRID",
  "members": [
    { "userId": "1111111111", "memberRole": "JUDGE" },
    { "userId": "2222222222", "memberRole": "JUDGE" },
    { "userId": "3333333333", "memberRole": "JUDGE" },
    { "userId": "1234567890", "memberRole": "STUDENT", "paperId": "5001", "defenseOrder": 1 },
    { "userId": "1234567891", "memberRole": "STUDENT", "paperId": "5002", "defenseOrder": 2 }
  ]
}
```

---

### POST /api/v1/defense/group/{groupId}/join
**功能：** 加入答辩房间（获取 RTC Token）  
**权限：** 🔑 已登录（需是答辩组成员）

**Response 200：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "roomId":     "rtc-room-20001",
    "rtcToken":   "xxxxxxxxxxxxxxxx",
    "userId":     "1234567890",
    "memberRole": "STUDENT",
    "expireAt":   "2024-05-15T14:00:00+08:00",
    "rtcConfig": {
      "provider":  "TRTC",
      "sdkAppId":  "1400000001",
      "region":    "cn-shanghai"
    }
  },
  "timestamp": 1713600000000
}
```

---

### PUT /api/v1/defense/group/{groupId}/view-mode
**功能：** 切换视图布局模式  
**权限：** 主持人

**Request Body：**
```json
{
  "viewMode": "RIGHT_LIST"
}
```

---

### PUT /api/v1/defense/group/{groupId}/recording
**功能：** 控制录像（暂停/恢复）  
**权限：** 主持人  
**权限编码：** `perm:defense:recording:control`

**Request Body：**
```json
{
  "action": "PAUSE"
}
```

**action 枚举：** `START` `PAUSE` `RESUME` `STOP`

---

### POST /api/v1/defense/group/{groupId}/member/{userId}/action
**功能：** 主持人对成员执行操作（禁言/踢出等）  
**权限：** 主持人

**Path Params：** `groupId: string`、`userId: string`

**Request Body：**
```json
{
  "action":  "MUTE",
  "reason":  "请保持安静"
}
```

**action 枚举：**
| 值 | 说明 |
|---|---|
| `MUTE` | 禁言 |
| `UNMUTE` | 解除禁言 |
| `MUTE_VIDEO` | 禁止视频 |
| `UNMUTE_VIDEO` | 恢复视频 |
| `INVITE_SPEAK` | 邀请上台（允许发言）|
| `KICK` | 踢出房间 |

---

### POST /api/v1/defense/group/{groupId}/score
**功能：** 评委提交答辩评分  
**权限：** 评委教师

**Request Body：**
```json
{
  "studentId": "1234567890",
  "score":     85.5,
  "comment":   "答辩表达清晰，对问题回答较为充分，建议在创新点阐述上更加突出"
}
```

---

### POST /api/v1/defense/group/{groupId}/async-video
**功能：** 学生上传异步答辩视频（获取上传凭证）  
**权限：** 🎓 STUDENT

**Response 200：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "uploadUrl":  "https://oss.example.com/defense/videos/?token=xxx",
    "fileKey":    "defense/videos/group_20001_student_5001.mp4",
    "expireAt":   "2024-05-10T23:59:59+08:00",
    "maxSizeMB":  2048
  },
  "timestamp": 1713600000000
}
```

---

### POST /api/v1/defense/group/{groupId}/async-video/confirm
**功能：** 确认异步答辩视频上传完成  
**权限：** 🎓 STUDENT

**Request Body：**
```json
{
  "fileKey": "defense/videos/group_20001_student_5001.mp4",
  "fileSize": 524288000
}
```

---

### POST /api/v1/defense/group/{groupId}/scores/import
**功能：** 批量导入答辩成绩（Excel）  
**权限：** 👑 SCHOOL_ADMIN  
**权限编码：** `perm:defense:score:import`

**Request：** `multipart/form-data`
```
file: file  Excel文件，格式：学号/成绩/评语
```

---

## 13. AI 能力模块

**服务：** `thesis-ai`  
**路径前缀：** `/api/v1/ai`  
> **注意：** AI 接口响应时间 P99 ≤ 10s，长耗时操作返回 `taskId` 前端轮询结果。

---

### POST /api/v1/ai/topic/recommend
**功能：** 论文选题推荐（AI 模式）  
**权限：** 🎓 STUDENT  
**权限编码：** `perm:paper:topic`

**Request Body：**
```json
{
  "batchId":       "100",
  "major":         "计算机科学与技术",
  "keywords":      "深度学习,图像处理",
  "researchField": "人工智能",
  "count":         10
}
```

**Response 200：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "recommendations": [
      {
        "title":         "基于 Transformer 的医学图像分割方法研究",
        "isAvailable":   true,
        "description":   "本课题研究..."
      },
      {
        "title":         "卷积神经网络在遥感图像目标检测中的应用",
        "isAvailable":   false,
        "reason":        "该选题已被其他同学选择"
      }
    ]
  },
  "timestamp": 1713600000000
}
```

---

### GET /api/v1/ai/topic/preset/list
**功能：** 获取教师预设选题列表  
**权限：** 🎓 STUDENT

**Query Params：**
```
batchId:     long    批次ID（必填）
major:       string  专业过滤（可选）
teacherId:   long    教师ID过滤（可选）
keyword:     string  标题关键词（可选）
pageNum:     int
pageSize:    int
```

**Response 200：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 45,
    "records": [
      {
        "presetTopicId":  "1",
        "title":          "基于 Vue3 的在线考试系统设计与实现",
        "teacherName":    "李教授",
        "description":    "本课题要求...",
        "remark":         "扫码加入讨论群：[二维码URL]",
        "majorLimit":     null,
        "maxSelectCount": 1,
        "selectedCount":  0,
        "isAvailable":    true
      }
    ]
  },
  "timestamp": 1713600000000
}
```

---

### POST /api/v1/ai/outline/recommend
**功能：** AI 推荐大纲（流式响应）  
**权限：** 🎓 STUDENT  
**注意：** 此接口返回 `text/event-stream`（SSE 流式响应）

**Request Body：**
```json
{
  "paperId":    "5001",
  "topicTitle": "基于深度学习的图像识别系统设计与实现",
  "major":      "计算机科学与技术",
  "paperType":  "GRADUATION"
}
```

**Response（SSE 流）：**
```
Content-Type: text/event-stream

data: {"type":"chunk","content":"{\n  \"outline\": [\n    {\n      \"id\": \"node_001\",\n      \"level\": 1,"}

data: {"type":"chunk","content":"      \"title\": \"第一章 绪论\",\n      \"remark\": \""}

data: {"type":"done","outlineJson":[...完整JSON...]}
```

---

### POST /api/v1/ai/abstract/generate
**功能：** 生成论文摘要  
**权限：** 🎓 STUDENT  
**权限编码：** `perm:paper:ai-assist`

**Request Body：**
```json
{
  "paperId":      "5001",
  "language":     "ZH",
  "withTranslate": true
}
```

**Response 200：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "abstractZh":  "随着深度学习技术的快速发展，图像识别在各领域得到广泛应用...",
    "abstractEn":  "With the rapid development of deep learning technology, image recognition has been widely applied in various fields...",
    "wordCountZh": 312,
    "wordCountEn": 198
  },
  "timestamp": 1713600000000
}
```

**错误码：** `6001` `6002`

---

### POST /api/v1/ai/reference/recommend
**功能：** 参考文献推荐  
**权限：** 🎓 STUDENT

**Request Body：**
```json
{
  "paperId":  "5001",
  "count":    15
}
```

**Response 200：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "references": [
      {
        "title":     "Deep Learning for Computer Vision",
        "authors":   ["Goodfellow I", "Bengio Y"],
        "year":      2016,
        "publisher": "MIT Press",
        "doi":       "10.7551/mitpress/9780262035613.001.0001",
        "formatted": "[1] Goodfellow I, Bengio Y. Deep Learning for Computer Vision[M]. MIT Press, 2016.",
        "citationStyle": "GB_T_7714"
      }
    ]
  },
  "timestamp": 1713600000000
}
```

---

### POST /api/v1/ai/proofread
**功能：** 论文校对  
**权限：** 🎓 STUDENT  
**权限编码：** `perm:paper:ai-assist`

**Request Body：**
```json
{
  "paperId":      "5001",
  "checkTypes":   ["TYPO", "GRAMMAR", "PUNCTUATION", "SENSITIVE_WORD", "NUMBER_FORMAT"]
}
```

**checkTypes 枚举：**
| 值 | 说明 |
|---|---|
| `TYPO` | 别字/别词纠错 |
| `GRAMMAR` | 语法错误（冗余/缺失/乱序）|
| `PUNCTUATION` | 标点符号规范 |
| `SENSITIVE_WORD` | 敏感词/黑名单 |
| `NUMBER_FORMAT` | 数字格式规范 |
| `INSTITUTION_NAME` | 机构名/地名/职称校验 |

**Response 200（异步任务）：**
```json
{
  "code": 200,
  "message": "校对任务已提交",
  "data": { "taskId": "proofread-task-001" },
  "timestamp": 1713600000000
}
```

---

### GET /api/v1/ai/proofread/{taskId}/result
**功能：** 获取校对结果  
**权限：** 🎓 STUDENT

**Response 200：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "taskId":  "proofread-task-001",
    "status":  "SUCCESS",
    "issues": [
      {
        "type":         "TYPO",
        "nodeId":       "node_003",
        "nodeTitle":    "1.2 研究目的",
        "position":     { "paragraphIndex": 2, "charOffset": 15, "length": 2 },
        "original":     "意义",
        "suggestion":   "意义（上文已使用"重要意义"，此处建议改为"价值"避免重复）",
        "severity":     "WARNING"
      },
      {
        "type":       "PUNCTUATION",
        "nodeId":     "node_005",
        "nodeTitle":  "2.1 理论基础",
        "position":   { "paragraphIndex": 5, "charOffset": 80, "length": 1 },
        "original":   "，",
        "suggestion": "。（此处应为句末标点，建议改为句号）",
        "severity":   "ERROR"
      }
    ],
    "summary": {
      "totalIssues":  15,
      "errorCount":   3,
      "warningCount": 12
    }
  },
  "timestamp": 1713600000000
}
```

---

### POST /api/v1/ai/analyze
**功能：** 论文与大纲契合度分析  
**权限：** 🎓 STUDENT | 👨‍🏫 TEACHER

**Request Body：**
```json
{
  "paperId": "5001"
}
```

**Response 200：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "overallMatch":  0.82,
    "riskLevel":     "LOW",
    "nodeAnalyses": [
      {
        "nodeId":      "node_003",
        "nodeTitle":   "第三章 系统设计",
        "matchScore":  0.65,
        "riskLevel":   "MEDIUM",
        "suggestion":  "该章节内容与大纲标题偏差较大，建议补充系统架构设计相关内容"
      }
    ]
  },
  "timestamp": 1713600000000
}
```

---

### POST /api/v1/ai/plagiarism/check
**功能：** 发起查重（同届或第三方）  
**权限：** 🎓 STUDENT | 👑 SCHOOL_ADMIN  
**权限编码：** `perm:paper:plagiarism`

**Request Body：**
```json
{
  "paperId":       "5001",
  "checkType":     "THIRD_PARTY",
  "thirdPartyName":"CNKI",
  "isFinal":       true
}
```

**Response 200：**
```json
{
  "code": 200,
  "message": "查重任务已提交，预计3-10分钟内完成",
  "data": { "checkRecordId": "50001" },
  "timestamp": 1713600000000
}
```

**错误码：** `6003` `6004`

---

### GET /api/v1/ai/plagiarism/{checkRecordId}/result
**功能：** 获取查重结果  
**权限：** 🔑 已登录（数据权限校验）

**Response 200：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "checkRecordId":  "50001",
    "status":         "SUCCESS",
    "checkType":      "THIRD_PARTY",
    "thirdPartyName": "CNKI",
    "similarityRate": 8.5,
    "reportUrl":      "https://oss.example.com/reports/check_50001.pdf",
    "teacherReviewed": false,
    "completedAt":    "2024-04-21T10:15:00+08:00",
    "sources": [
      {
        "sourceTitle": "深度学习在图像识别中的应用综述",
        "matchRate":   3.2,
        "matchedText": "（相似片段摘要）"
      }
    ]
  },
  "timestamp": 1713600000000
}
```

---

### POST /api/v1/ai/ideology/scan
**功能：** 意识形态扫描（管理员触发）  
**权限：** 👑 SCHOOL_ADMIN  
**权限编码：** `perm:paper:ideology-scan`

**Request Body：**
```json
{
  "paperIds":   ["5001", "5002"],
  "triggerType": "AUTO_BATCH"
}
```

**Response 200：**
```json
{
  "code": 200,
  "message": "扫描任务已提交",
  "data": {
    "taskIds": ["ideology-task-001", "ideology-task-002"]
  },
  "timestamp": 1713600000000
}
```

---

### GET /api/v1/ai/ideology/{taskId}/result
**功能：** 获取意识形态扫描结果  
**权限：** 👑 SCHOOL_ADMIN

**Response 200：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "taskId":       "ideology-task-001",
    "paperId":      "5001",
    "status":       "SUCCESS",
    "overallRisk":  "LOW",
    "riskItemCount": 0,
    "details":      [],
    "completedAt":  "2024-04-21T10:10:00+08:00"
  },
  "timestamp": 1713600000000
}
```

---

### POST /api/v1/ai/preliminary-review/trigger
**功能：** 触发论文初步评议  
**权限：** 👑 SCHOOL_ADMIN  
**权限编码：** `perm:paper:ai-review`

**Request Body：**
```json
{
  "batchId":       "100",
  "paperIds":      ["5001", "5002"],
  "reviewStandard": "山东省教委毕业论文评议标准2024版",
  "triggerType":    "MANUAL"
}
```

---

### GET /api/v1/ai/preliminary-review/{paperId}/latest
**功能：** 获取论文最新评议报告  
**权限：** 👑 SCHOOL_ADMIN | 📍 POINT_ADMIN

**Response 200：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "reviewId":      "60001",
    "status":        "SUCCESS",
    "overallRating": "GOOD",
    "reviewStandard":"山东省教委毕业论文评议标准2024版",
    "dimensionScores": {
      "选题合理性":   85,
      "大纲完整性":   78,
      "内容质量":     82,
      "格式规范性":   90
    },
    "reviewReport":  "## 论文评议报告\n\n### 选题合理性\n该选题结合...",
    "completedAt":   "2024-04-21T10:05:00+08:00"
  },
  "timestamp": 1713600000000
}
```


---

## 14. 统计报表模块

**服务：** `thesis-statistics`  
**路径前缀：** `/api/v1/statistics`  
> 所有统计接口数据来源于预计算宽表 `stat_paper_summary`，不实时聚合，TTL 5 分钟。

---

### GET /api/v1/statistics/overview
**功能：** 批次论文整体概况统计  
**权限：** 👑 SCHOOL_ADMIN | 📍 POINT_ADMIN

**Query Params：**
```
batchId:          long    批次ID（必填）
teachingPointId:  long    教学点ID过滤（POINT_ADMIN自动限制，可选）
```

**Response 200：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "batchId":        "100",
    "batchName":      "2024年春季毕业论文批次",
    "totalStudents":  256,
    "overallProgress": {
      "notStarted":   5,
      "inProgress":   220,
      "completed":    31
    },
    "guideStats": {
      "totalGuided":    231,
      "guideCompleted": 31,
      "guideInProgress":200,
      "guidePending":   25
    },
    "checkStats": {
      "totalChecked":  180,
      "avgSimilarity": 7.8
    },
    "statTime": "2024-04-21T10:00:00+08:00"
  },
  "timestamp": 1713600000000
}
```

---

### GET /api/v1/statistics/paper-process
**功能：** 论文各环节过程统计（按维度分组）  
**权限：** 👑 SCHOOL_ADMIN | 📍 POINT_ADMIN  
**权限编码：** `perm:statistics:paper-process`

**Query Params：**
```
batchId:          long    批次ID（必填）
dimType:          string  统计维度（SCHOOL/POINT/TEACHER/MAJOR，默认SCHOOL）
teachingPointId:  long    教学点过滤（dimType=TEACHER/MAJOR时配合使用，可选）
major:            string  专业过滤（可选）
nodeType:         string  节点类型过滤（可选，不传=返回所有节点）
```

**Response 200：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "dimType":   "POINT",
    "statTime":  "2024-04-21T10:00:00+08:00",
    "columns":   ["not_started", "submitted", "reviewing", "approved", "rejected"],
    "records": [
      {
        "dimensionId":   "200",
        "dimensionName": "济南教学点",
        "totalStudents": 120,
        "nodes": {
          "SIGN": {
            "notStarted": 0,
            "submitted":  2,
            "reviewing":  5,
            "approved":   112,
            "rejected":   1
          },
          "TOPIC": {
            "notStarted": 1,
            "submitted":  3,
            "reviewing":  8,
            "approved":   105,
            "rejected":   3
          },
          "DRAFT_FULL": {
            "notStarted": 10,
            "submitted":  20,
            "reviewing":  35,
            "approved":   48,
            "rejected":   7
          }
        }
      },
      {
        "dimensionId":   "201",
        "dimensionName": "青岛教学点",
        "totalStudents": 136,
        "nodes": {}
      }
    ]
  },
  "timestamp": 1713600000000
}
```

---

### GET /api/v1/statistics/guide-progress
**功能：** 教师指导进度统计  
**权限：** 👑 SCHOOL_ADMIN | 📍 POINT_ADMIN | 👨‍🏫 TEACHER（仅自己）

**Query Params：**
```
batchId:          long    批次ID（必填）
teacherId:        long    教师ID（TEACHER角色自动限制为自己，可选）
teachingPointId:  long    教学点过滤（可选）
```

**Response 200：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "summary": {
      "totalGuide":    256,
      "guideCompleted":  80,
      "guideInProgress": 140,
      "guidePending":    36
    },
    "byTeacher": [
      {
        "teacherId":       "9876543210",
        "teacherName":     "李教授",
        "department":      "计算机学院",
        "totalStudents":   15,
        "completed":       5,
        "inProgress":      8,
        "pending":         2,
        "overdueStudents": 1
      }
    ],
    "chartData": {
      "type":   "donut",
      "series": [
        { "name": "已完成", "value": 80,  "color": "#67C23A" },
        { "name": "指导中", "value": 140, "color": "#E6A23C" },
        { "name": "待指导", "value": 36,  "color": "#909399" }
      ]
    }
  },
  "timestamp": 1713600000000
}
```

---

### GET /api/v1/statistics/major-distribution
**功能：** 各专业学生人数统计（柱状图）  
**权限：** 👑 SCHOOL_ADMIN | 📍 POINT_ADMIN

**Query Params：**
```
batchId:         long    批次ID（必填）
teachingPointId: long    教学点过滤（可选）
```

**Response 200：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "chartType": "bar",
    "data": [
      { "major": "计算机科学与技术", "studentCount": 85, "completedCount": 30 },
      { "major": "软件工程",         "studentCount": 72, "completedCount": 25 },
      { "major": "信息管理",         "studentCount": 56, "completedCount": 18 }
    ]
  },
  "timestamp": 1713600000000
}
```

---

### GET /api/v1/statistics/teacher-workload
**功能：** 教师审核工作量统计  
**权限：** 👑 SCHOOL_ADMIN | 📍 POINT_ADMIN

**Query Params：**
```
batchId:          long    批次ID（必填）
teachingPointId:  long    教学点过滤（可选）
nodeType:         string  节点类型（可选）
pageNum:          int
pageSize:         int
```

**Response 200：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total": 18,
    "records": [
      {
        "teacherId":     "9876543210",
        "teacherName":   "李教授",
        "totalReview":   45,
        "approvedCount": 38,
        "rejectedCount": 7,
        "pendingCount":  10,
        "avgReviewDays": 2.3,
        "overdueCount":  1
      }
    ]
  },
  "timestamp": 1713600000000
}
```

---

### GET /api/v1/statistics/export
**功能：** 导出统计报表（Excel）  
**权限：** 👑 SCHOOL_ADMIN | 📍 POINT_ADMIN  
**权限编码：** `perm:statistics:export`

**Query Params：**
```
batchId:    long    批次ID（必填）
reportType: string  OVERVIEW | PAPER_PROCESS | GUIDE_PROGRESS | MAJOR_DIST | TEACHER_WORKLOAD
dimType:    string  统计维度（可选）
```

**Response 200：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "downloadUrl": "https://oss.example.com/exports/stat_report_20240421.xlsx?token=xxx",
    "expireAt":    "2024-04-21T11:00:00+08:00",
    "fileName":    "2024春季批次-论文过程统计-20240421.xlsx"
  },
  "timestamp": 1713600000000
}
```

---

### GET /api/v1/statistics/teacher/my
**功能：** 教师自己的工作台统计（待处理数量）  
**权限：** 👨‍🏫 TEACHER | 👩‍🏫 ASSISTANT

**Query Params：**
```
batchId: long（必填）
```

**Response 200：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "totalStudents":   15,
    "pendingReview":   8,
    "inReview":        3,
    "completed":       4,
    "overdueStudents": 1,
    "pendingByNode": {
      "DRAFT_FULL":   5,
      "FINAL_DRAFT":  2,
      "FINAL":        1
    }
  },
  "timestamp": 1713600000000
}
```

---

## 15. 文件服务模块

**服务：** `thesis-file`  
**路径前缀：** `/api/v1/file`

---

### POST /api/v1/file/presigned-url
**功能：** 获取文件上传预签名 URL（前端直传 OSS）  
**权限：** 🔑 已登录

**Request Body：**
```json
{
  "fileName": "论文初稿.docx",
  "fileType": "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
  "fileSize": 2048576,
  "bizType":  "PAPER_SUBMIT"
}
```

**bizType 枚举：**
| 值 | 允许类型 | 大小限制 |
|---|---|---|
| `PAPER_SUBMIT` | docx/pdf | 50 MB |
| `TOPIC_FORM` | docx/pdf | 20 MB |
| `TASK_BOOK` | docx/pdf | 20 MB |
| `SIGNATURE_IMAGE` | jpg/png | 2 MB |
| `IM_IMAGE` | jpg/png/gif | 10 MB |
| `IM_FILE` | 任意 | 50 MB |
| `TEMPLATE` | docx | 10 MB |
| `AVATAR` | jpg/png | 2 MB |
| `DEFENSE_VIDEO` | mp4/mov | 2048 MB |

**Schema：**
```yaml
PresignedUrlRequest:
  type: object
  required: [fileName, fileType, bizType]
  properties:
    fileName:
      type: string
      maxLength: 256
    fileType:
      type: string
      description: MIME 类型
    fileSize:
      type: integer
      format: int64
      description: 文件大小（字节），用于校验限制
    bizType:
      type: string
      enum: [PAPER_SUBMIT, TOPIC_FORM, TASK_BOOK, SIGNATURE_IMAGE, IM_IMAGE, IM_FILE, TEMPLATE, AVATAR, DEFENSE_VIDEO]
```

**Response 200：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "uploadUrl":  "https://bucket.oss-cn-beijing.aliyuncs.com/papers/5001/...",
    "fileKey":    "papers/5001/2024042110300001.docx",
    "expireAt":   "2024-04-21T10:30:00+08:00",
    "headers": {
      "Content-Type": "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    }
  },
  "timestamp": 1713600000000
}
```

---

### POST /api/v1/file/confirm
**功能：** 确认文件上传完成（通知后端记录文件信息）  
**权限：** 🔑 已登录

**Request Body：**
```json
{
  "fileKey":    "papers/5001/2024042110300001.docx",
  "bizType":    "PAPER_SUBMIT",
  "refId":      "5001",
  "actualSize": 2035712
}
```

**Response 200：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "fileId":    "70001",
    "accessUrl": "https://oss.example.com/papers/5001/2024042110300001.docx"
  },
  "timestamp": 1713600000000
}
```

---

### GET /api/v1/file/download-url
**功能：** 获取文件下载预签名 URL（有效期 30 分钟）  
**权限：** 🔑 已登录（数据权限校验）

**Query Params：**
```
fileKey: string  文件Key（必填）
```

**Response 200：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "downloadUrl": "https://oss.example.com/...?token=xxx&expires=1713602400",
    "expireAt":    "2024-04-21T11:00:00+08:00",
    "fileName":    "论文初稿.docx"
  },
  "timestamp": 1713600000000
}
```

---

## 16. 通知模块

**服务：** `thesis-notify`  
**路径前缀：** `/api/v1/notify`

---

### GET /api/v1/notify/list
**功能：** 站内通知列表  
**权限：** 🔑 已登录

**Query Params：**
```
pageNum:    int
pageSize:   int
readStatus: string  UNREAD | READ（可选，不传=全部）
notifyType: string  通知类型过滤（可选）
```

**Response 200：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "total":       15,
    "unreadCount": 5,
    "records": [
      {
        "notifyId":   "80001",
        "notifyType": "PAPER_REJECTED",
        "title":      "论文审核不通过",
        "content":    "您提交的初稿已被李教授驳回，请查看审核意见后修改重新提交。",
        "linkUrl":    "/student/paper/5001/draft",
        "readStatus": "UNREAD",
        "createdAt":  "2024-04-20T14:35:00+08:00"
      }
    ]
  },
  "timestamp": 1713600000000
}
```

---

### PUT /api/v1/notify/{notifyId}/read
**功能：** 标记通知为已读  
**权限：** 🔑 已登录

---

### PUT /api/v1/notify/read-all
**功能：** 全部标记为已读  
**权限：** 🔑 已登录

---

### GET /api/v1/notify/unread-count
**功能：** 获取未读通知数量（首页徽章使用）  
**权限：** 🔑 已登录

**Response 200：**
```json
{
  "code": 200,
  "message": "success",
  "data": { "unreadCount": 5 },
  "timestamp": 1713600000000
}
```

---

## 17. WebSocket 接口规范

### 17.1 连接建立

```
协议：    WebSocket（wss://）
端点：    wss://api.thesis.chenzhang.com/ws
认证：    连接时通过 URL 参数传递 Token（不支持 Header）
连接 URL：wss://api.thesis.chenzhang.com/ws?token=<accessToken>

STOMP 协议：Spring WebSocket + STOMP
```

### 17.2 即时消息频道

#### 订阅频道

| 订阅地址 | 说明 |
|---|---|
| `/user/queue/messages` | 私信通知（推送给指定用户）|
| `/topic/group/{groupId}` | 群组消息（所有群成员）|
| `/user/queue/errors` | 错误反馈（消息发送失败等）|
| `/user/queue/notify` | 实时通知推送（审核结果等）|

#### 发送消息

**目标地址：** `/app/group/{groupId}/send`

**消息格式：**
```json
{
  "msgType": "TEXT",
  "content": "老师，我已经修改好了",
  "replyMsgId": null
}
```

**发送文件/图片（先上传 OSS 获取 fileKey，再发送）：**
```json
{
  "msgType":  "FILE",
  "content":  "",
  "fileKey":  "im/files/group_2001/review_guide.pdf",
  "fileName": "论文修改指导说明.pdf",
  "fileSize": 524288
}
```

#### 服务端推送格式（订阅 `/topic/group/{groupId}` 收到）

```json
{
  "msgId":      "3005",
  "groupId":    "2001",
  "senderId":   "9876543210",
  "senderName": "李教授",
  "senderType": "TEACHER",
  "senderAvatar": "https://oss.example.com/avatars/teacher.jpg",
  "msgType":    "TEXT",
  "content":    "好的，我已经看到了，总体上写的不错",
  "isFiltered": false,
  "sentAt":     "2024-04-21T10:35:00+08:00"
}
```

#### 撤回消息

**目标地址：** `/app/group/{groupId}/recall`

```json
{ "msgId": "3005" }
```

#### 系统通知（订阅 `/user/queue/notify` 收到）

```json
{
  "notifyType": "PAPER_APPROVED",
  "title":      "论文审核通过",
  "content":    "您提交的选题已通过审核，可以进入下一阶段",
  "linkUrl":    "/student/paper/5001",
  "paperId":    "5001",
  "nodeType":   "TOPIC",
  "timestamp":  1713600000000
}
```

### 17.3 在线答辩信令

> 答辩视频流通过云端 RTC SDK 处理，WebSocket 仅用于主持人控制信令。

**目标地址：** `/app/defense/{groupId}/control`

**主持人发送控制指令：**
```json
{
  "action":   "MUTE",
  "targetId": "1234567890",
  "param":    null
}
```

**服务端广播至所有成员（订阅 `/topic/defense/{groupId}`）：**
```json
{
  "eventType": "MEMBER_MUTED",
  "targetId":  "1234567890",
  "operator":  "9876543210",
  "timestamp": 1713600000000
}
```

**eventType 枚举：**
| 事件 | 说明 |
|---|---|
| `MEMBER_JOINED` | 成员进入答辩间 |
| `MEMBER_LEFT` | 成员离开 |
| `MEMBER_MUTED` | 成员被禁言 |
| `MEMBER_KICKED` | 成员被踢出 |
| `RECORDING_PAUSED` | 录像已暂停 |
| `RECORDING_RESUMED` | 录像已恢复 |
| `VIEW_MODE_CHANGED` | 视图模式切换 |
| `DEFENSE_STARTED` | 答辩开始 |
| `DEFENSE_ENDED` | 答辩结束 |

### 17.4 心跳保活

```
客户端每 30 秒发送 STOMP HEARTBEAT 帧
服务端 45 秒未收到心跳则关闭连接
重连策略：指数退避，最大 30 秒间隔，最多重连 5 次
```

---

## 18. 接口变更日志

### V1.0.0（2026-04-21）

- 初始版本，覆盖全部系统模块接口
- 包含：认证/用户/批次/论文/批注/评分/模板/即时通讯/答辩/AI/统计/文件/通知共 13 个模块
- WebSocket 接口：即时消息 + 答辩信令

---

## 附录 A：完整枚举值汇总

### 用户相关枚举

```
UserType:         SCHOOL_ADMIN | POINT_ADMIN | TEACHER | ASSISTANT | STUDENT
UserStatus:       ACTIVE | LOCKED | INACTIVE
LoginType:        PASSWORD | SMS | WECHAT
ClientType:       WEB | WECHAT
```

### 论文流程相关枚举

```
FlowNodeType:     SIGN | TOPIC | OUTLINE | TOPIC_FORM | TASK_BOOK | DRAFT_SEG | DRAFT_FULL | FINAL_DRAFT | FINAL
NodeStatus:       NOT_STARTED | PENDING | SUBMITTED | REVIEWING | APPROVED | REJECTED
PaperStatus:      NOT_STARTED | IN_PROGRESS | COMPLETED | FAILED
BatchStatus:      DRAFT | ACTIVE | FINISHED | ARCHIVED
ReviewDecision:   APPROVED | REJECTED | GUIDE_ONLY
SubmitType:       FULL | SEGMENT
TopicSource:      SELF | AI | PRESET
TeacherType:      MAIN | ASSIST
ReviewMode:       ALL | SAMPLE
```

### AI 相关枚举

```
CheckType:        INTRA_BATCH | THIRD_PARTY
ThirdPartyName:   CNKI | VIP | WANFANG
ProofreadType:    TYPO | GRAMMAR | PUNCTUATION | SENSITIVE_WORD | NUMBER_FORMAT | INSTITUTION_NAME
RiskLevel:        LOW | MEDIUM | HIGH
OverallRating:    EXCELLENT | GOOD | NEEDS_IMPROVEMENT | POOR
```

### 答辩相关枚举

```
DefenseType:      SYNC | ASYNC
DefenseStatus:    PENDING | IN_PROGRESS | PAUSED | FINISHED
MemberRole:       HOST | JUDGE | STUDENT
ViewMode:         GRID | RIGHT_LIST | TOP_LIST
VideoStatus:      UPLOADED | TRANSCODING | READY
```

### 通知相关枚举

```
NotifyType:       PAPER_SUBMITTED | PAPER_APPROVED | PAPER_REJECTED | DEFENSE_REMIND | DEADLINE_REMIND | SYSTEM
SendChannel:      SMS | INSITE | WECHAT
ReadStatus:       UNREAD | READ
```

---

## 附录 B：接口速查索引

### 按角色分类

#### 🎓 学生端核心接口

| 接口 | 方法 | 路径 |
|---|---|---|
| 查看我的论文 | GET | `/api/v1/paper/my` |
| 提交选题 | POST | `/api/v1/paper/{id}/topic` |
| 获取/保存大纲 | GET/PUT | `/api/v1/paper/{id}/outline` |
| 保存章节内容 | PUT | `/api/v1/paper/{id}/content/{nodeId}` |
| 提交论文节点 | POST | `/api/v1/paper/{id}/submit` |
| 撤回提交 | POST | `/api/v1/paper/{id}/withdraw` |
| 导出标准论文 | GET | `/api/v1/paper/{id}/export` |
| 一键导入DOCX | POST | `/api/v1/paper/import/docx` |
| AI选题推荐 | POST | `/api/v1/ai/topic/recommend` |
| AI大纲推荐 | POST | `/api/v1/ai/outline/recommend` |
| 生成摘要 | POST | `/api/v1/ai/abstract/generate` |
| 推荐参考文献 | POST | `/api/v1/ai/reference/recommend` |
| 论文校对 | POST | `/api/v1/ai/proofread` |
| 发起查重 | POST | `/api/v1/ai/plagiarism/check` |

#### 👨‍🏫 教师端核心接口

| 接口 | 方法 | 路径 |
|---|---|---|
| 论文列表 | GET | `/api/v1/paper/list` |
| 审核论文 | PUT | `/api/v1/paper/{id}/review` |
| 添加批注 | POST | `/api/v1/annotation` |
| 提交评分 | POST | `/api/v1/score` |
| 开逾期通道 | POST | `/api/v1/paper/{id}/deadline-extension` |
| 工作台统计 | GET | `/api/v1/statistics/teacher/my` |

#### 👑 管理端核心接口

| 接口 | 方法 | 路径 |
|---|---|---|
| 批量导入用户 | POST | `/api/v1/user/import` |
| 创建批次 | POST | `/api/v1/batch` |
| 配置流程节点 | PUT | `/api/v1/batch/{id}/flow-config` |
| 论文过程统计 | GET | `/api/v1/statistics/paper-process` |
| 触发AI评议 | POST | `/api/v1/ai/preliminary-review/trigger` |
| 意识形态扫描 | POST | `/api/v1/ai/ideology/scan` |
| 导出统计报表 | GET | `/api/v1/statistics/export` |

---

## 附录 C：OpenAPI 3.0 完整 YAML 文件位置

完整的 OpenAPI 3.0 YAML 规范文件（可导入 Swagger UI / Apifox / Postman）：

```
thesis-docs/
└── api/
    ├── openapi.yaml              # 完整规范（由 Knife4j 自动生成）
    ├── openapi-auth.yaml         # 认证模块分割文件
    ├── openapi-paper.yaml        # 论文模块分割文件
    ├── openapi-ai.yaml           # AI模块分割文件
    └── postman_collection.json   # Postman 导入集合
```

**Knife4j 在线文档地址：**
- 生产环境：`https://api.thesis.chenzhang.com/doc.html`（仅内网访问）
- 演示环境：`https://staging-api.thesis.chenzhang.com/doc.html`

---

## 附录 D：前端接口调用示例（TypeScript）

```typescript
// src/utils/request.ts — axios 封装
import axios, { AxiosInstance } from 'axios'
import { useAuthStore } from '@/stores/auth'

const request: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 30000,
  headers: { 'Content-Type': 'application/json' }
})

// 请求拦截：自动注入 Token
request.interceptors.request.use(config => {
  const auth = useAuthStore()
  if (auth.accessToken) {
    config.headers.Authorization = `Bearer ${auth.accessToken}`
  }
  return config
})

// 响应拦截：统一错误处理
request.interceptors.response.use(
  response => {
    const { code, message, data } = response.data
    if (code === 200) return data
    if (code === 401 || code === 4010) {
      useAuthStore().logout()
      return Promise.reject(new Error(message))
    }
    ElMessage.error(message)
    return Promise.reject(new Error(message))
  },
  error => {
    if (error.response?.status === 429) {
      ElMessage.warning('操作过于频繁，请稍后重试')
    }
    return Promise.reject(error)
  }
)

export default request
```

```typescript
// src/api/paper.ts — 论文模块接口
import request from '@/utils/request'
import type { PaperDetail, PageResult, SubmitTopicRequest } from '@/types'

export const paperApi = {
  // 获取论文列表
  list: (params: PaperListParams) =>
    request.get<PageResult<PaperVO>>('/api/v1/paper/list', { params }),

  // 获取我的论文
  my: (batchId: number) =>
    request.get<PaperDetail>('/api/v1/paper/my', { params: { batchId } }),

  // 提交选题
  submitTopic: (paperId: string, data: SubmitTopicRequest) =>
    request.post<{ topicId: string }>(`/api/v1/paper/${paperId}/topic`, data),

  // 提交论文节点
  submit: (paperId: string, data: SubmitPaperRequest) =>
    request.post<{ submitRecordId: string }>(`/api/v1/paper/${paperId}/submit`, data),

  // 审核论文（教师）
  review: (paperId: string, data: ReviewPaperRequest) =>
    request.put<{ reviewRecordId: string }>(`/api/v1/paper/${paperId}/review`, data),

  // 导出论文
  export: (paperId: string, templateId: number, format: 'DOCX' | 'PDF' = 'DOCX') =>
    request.get<{ downloadUrl: string }>(`/api/v1/paper/${paperId}/export`, {
      params: { templateId, format }
    })
}
```

```typescript
// WebSocket 连接示例（STOMP）
import { Client, IMessage } from '@stomp/stompjs'
import { useAuthStore } from '@/stores/auth'

let stompClient: Client | null = null

export function connectWebSocket() {
  const auth = useAuthStore()
  stompClient = new Client({
    brokerURL: `${import.meta.env.VITE_WS_URL}?token=${auth.accessToken}`,
    reconnectDelay: 5000,
    heartbeatIncoming: 45000,
    heartbeatOutgoing: 30000,
    onConnect: () => {
      // 订阅群组消息
      stompClient!.subscribe(`/topic/group/${groupId}`, (msg: IMessage) => {
        const message = JSON.parse(msg.body)
        handleNewMessage(message)
      })
      // 订阅个人通知
      stompClient!.subscribe('/user/queue/notify', (msg: IMessage) => {
        const notify = JSON.parse(msg.body)
        handleNotification(notify)
      })
    }
  })
  stompClient.activate()
}

// 发送消息
export function sendMessage(groupId: string, content: string) {
  stompClient?.publish({
    destination: `/app/group/${groupId}/send`,
    body: JSON.stringify({ msgType: 'TEXT', content })
  })
}
```

---

*版本：V1.0 | 日期：2026-04-21 | 基于《CLAUDE.md V2.0》《数据库设计文档 V1.0》《技术实现方案 V1.0》*  
*接口变更须同步更新本文档，并通知前端团队。所有 Breaking Change 须在上线前至少 3 天告知。*
