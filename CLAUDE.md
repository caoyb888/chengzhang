# CLAUDE.md — 宸章高等学历继续教育论文综合服务系统

> **本文件是项目唯一权威开发行为规范。**  
> 每次 AI 辅助开发会话启动时，必须首先完整读取本文件，所有代码生成、架构决策、数据库操作均须严格遵守。  
> 与本文件冲突的口头指令、临时要求一律以本文件为准，如需修改须更新本文件后再执行。

---

## 一、项目基准信息

| 项目 | 值 |
|---|---|
| 系统名称 | 宸章高等学历继续教育论文综合服务系统 |
| 建设客户 | 曲阜师范大学 |
| 交付期限 | 合同签订后 10 日内 |
| 服务期限 | 1 年，7×24 小时 |
| 注册用户规模 | ≥ 10 万 |
| 最大同时在线 | ≥ 10,000 人 |
| 接口性能目标 | 普通接口 P99 ≤ 500ms；AI 接口 P99 ≤ 10s |
| 系统可用性目标 | SLA 99.9%（年停机 ≤ 8.76h） |
| 技术方案基准文档 | `宸章论文综合服务系统_技术实现方案.md` |

---

## 二、代码仓库与工程结构

### 2.1 仓库划分

| 仓库名 | 类型 | 说明 |
|---|---|---|
| `thesis-backend` | 后端 Monorepo（Maven 多模块）| 所有微服务模块 |
| `thesis-web` | 前端 | Vue 3 单页应用，五套角色布局 |
| `thesis-ops` | 运维 | K8s YAML、Dockerfile、Flyway 脚本、CI/CD 配置 |
| `thesis-docs` | 文档 | 技术方案、接口文档、数据库设计 |

### 2.2 后端工程结构（Maven 多模块）

```
thesis-backend/
├── pom.xml                      # 父 POM，统一依赖版本管理
├── thesis-common/               # 公共模块（枚举/工具/异常/响应体）
├── thesis-gateway/              # API 网关
├── thesis-auth/                 # 统一认证授权
├── thesis-user/                 # 用户与权限中心
├── thesis-paper/                # 论文核心（流程引擎/审核/批注）
├── thesis-template/             # 模板管理
├── thesis-im/                   # 即时通讯
├── thesis-defense/              # 在线答辩
├── thesis-statistics/           # 统计报表
├── thesis-ai/                   # AI 能力服务
├── thesis-notify/               # 通知推送
├── thesis-file/                 # 文件处理与导出
└── thesis-admin/                # 运维管理后台
```

### 2.3 单个微服务内部包结构（强制遵守）

```
com.chenzhang.thesis.<module>/
├── controller/          # REST 接口层：只做参数校验 + 调用 service，禁止写业务逻辑
├── service/
│   ├── <XxxService>.java            # 接口定义
│   └── impl/<XxxServiceImpl>.java   # 实现类
├── domain/
│   ├── entity/          # 数据库实体（@TableName 注解）
│   ├── dto/             # 请求入参对象（DTO）
│   └── vo/              # 响应出参对象（VO）
├── mapper/              # MyBatis-Plus Mapper 接口
├── config/              # 配置类（@Configuration）
├── event/               # 领域事件（发布/监听）
├── exception/           # 自定义异常
└── util/                # 模块内工具类
```

规则：
- Controller 不得直接操作 Mapper，必须经过 Service
- Service 实现类不得互相直接注入，跨服务调用通过 Feign 或 MQ 事件
- entity 包中的类禁止暴露给前端，必须经 VO 转换后返回

### 2.4 前端工程结构

```
thesis-web/
├── src/
│   ├── api/              # 接口层（按微服务模块分文件）
│   │   ├── paper.ts   ├── user.ts     ├── im.ts
│   │   ├── defense.ts ├── ai.ts       └── statistics.ts
│   ├── components/       # 公共组件
│   │   ├── RichEditor/       # 论文富文本编辑器（WangEditor 封装）
│   │   ├── AnnotationTool/   # PDF 在线批注组件
│   │   ├── ChartPanel/       # ECharts 图表封装
│   │   ├── VideoConf/        # WebRTC 答辩组件
│   │   ├── OutlineTree/      # 提纲树形编辑器（支持拖拽排序）
│   │   └── FileUpload/       # 文件上传（预签名直传 OSS）
│   ├── composables/      # Vue 3 组合式函数（use 前缀）
│   ├── layouts/          # 五套角色布局
│   │   ├── AdminLayout.vue    # 学校管理端
│   │   ├── PointLayout.vue    # 教学点管理端
│   │   ├── TeacherLayout.vue  # 指导教师端
│   │   ├── AssistLayout.vue   # 辅助指导教师端
│   │   └── StudentLayout.vue  # 学生端
│   ├── router/           # 动态路由（按角色按需加载）
│   ├── stores/           # Pinia 状态管理
│   ├── types/            # TypeScript 类型定义
│   └── views/            # 页面（按角色目录分组）
│       ├── admin/  ├── point/  ├── teacher/
│       ├── assist/ └── student/
└── vite.config.ts
```

---

## 三、技术栈版本锁定

**以下版本为强制约束，不得使用其他版本或替代框架，变更须更新本文件并经团队评审。**

### 3.1 后端技术栈

| 技术 | 版本 | 用途 |
|---|---|---|
| JDK | 21（LTS） | 运行环境，使用虚拟线程 |
| Spring Boot | 3.3.x | 核心框架 |
| Spring Cloud Alibaba | 2023.0.x | 微服务治理 |
| Spring Cloud Gateway | 4.1.x | API 网关 |
| Nacos | 2.3.x | 注册中心 + 配置中心 |
| Sentinel | 1.8.x | 限流熔断 |
| MyBatis-Plus | 3.5.x | ORM 框架 |
| ShardingSphere-JDBC | 5.4.x | 读写分离 |
| Sa-Token | 1.39.x | 认证授权 |
| MySQL | 8.0.x | 主数据库 |
| Redis | 7.2.x | 缓存 / 会话 |
| RocketMQ | 5.1.x | 消息队列 |
| Elasticsearch | 8.12.x | 全文索引 / 查重 |
| MinIO | RELEASE.2024-xx | 对象存储 |
| EasyExcel | 3.3.x | Excel 导入导出 |
| Apache POI | 5.2.x | DOCX 文件处理 |
| Flyway | 10.x | 数据库版本迁移 |
| Knife4j | 4.4.x | API 文档（OpenAPI 3）|
| Redisson | 3.27.x | 分布式锁 |
| Resilience4j | 2.x | AI 接口重试熔断 |

### 3.2 前端技术栈

| 技术 | 版本 | 用途 |
|---|---|---|
| Node.js | 20.x（LTS） | 构建环境 |
| Vue | 3.4.x | 前端框架 |
| TypeScript | 5.x | 类型安全 |
| Vite | 5.x | 构建工具 |
| Element Plus | 2.6.x | UI 组件库 |
| Pinia | 2.x | 状态管理 |
| Vue Router | 4.x | 路由管理 |
| WangEditor | 5.x | 富文本编辑器 |
| PDF.js | 4.x | PDF 渲染（批注底层）|
| ECharts | 5.x | 数据图表 |
| vue-draggable-plus | 0.x | 提纲拖拽排序 |
| axios | 1.6.x | HTTP 客户端 |

### 3.3 基础设施

| 技术 | 版本 | 用途 |
|---|---|---|
| Kubernetes | 1.28.x | 容器编排 |
| Docker | 25.x | 容器化 |
| Nginx | 1.25.x | 反向代理 / 负载均衡 |
| Prometheus + Grafana | 最新稳定 | 监控告警 |
| SkyWalking | 9.7.x | 链路追踪 |
| ELK Stack | 8.x | 日志收集分析 |

---

## 四、数据库开发规范

### 4.1 分库规则（禁止跨库混放）

| 数据库实例 | 数据库名 | 归属服务 |
|---|---|---|
| db-core | `thesis_user`、`thesis_auth` | thesis-user、thesis-auth |
| db-paper | `thesis_paper`、`thesis_template` | thesis-paper、thesis-template |
| db-im | `thesis_im` | thesis-im、thesis-defense |
| db-biz | `thesis_statistics`、`thesis_notify` | thesis-statistics、thesis-notify |

### 4.2 表结构强制规范

**所有业务表必须包含以下四个基础字段，缺一禁止合并 PR：**

```sql
id          BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键（雪花算法）',
created_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
updated_at  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
                            ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
is_deleted  TINYINT(1)      NOT NULL DEFAULT 0 COMMENT '逻辑删除（0正常 1删除）',
```

**命名规范：**
- 库名：`thesis_<模块>`，全小写下划线
- 表名：全小写下划线，带业务前缀，如 `paper_submit_record`
- 字段名：全小写下划线，如 `teaching_point_id`
- 主键统一命名 `id`，类型 `BIGINT UNSIGNED`
- 状态字段：`status VARCHAR(32)`，存枚举字符串，不使用数字
- 禁止使用 `TIMESTAMP` 类型，统一使用 `DATETIME`

**禁止事项：**
- 禁止在数据库层面创建外键约束
- 禁止使用物理删除，一律逻辑删除（`is_deleted = 1`）
- 禁止在核心查询表中存储大字段（论文富文本内容独立存表 `paper_content`）
- 单表索引数量不超过 8 个

### 4.3 核心业务表速查

| 数据库 | 表名 | 核心字段 |
|---|---|---|
| thesis_user | `user` | school_id, teaching_point_id, username, phone, password_hash, user_type |
| thesis_user | `role` | school_id, role_code, role_name, is_preset |
| thesis_user | `permission` | perm_code, perm_name, perm_type(MENU/BUTTON) |
| thesis_user | `teaching_point` | school_id, name, data_scope |
| thesis_paper | `batch` | school_id, name, start_time, end_time |
| thesis_paper | `batch_flow_config` | batch_id, node_type, sort_order, is_enabled, is_required, need_guide, need_review, start_time, end_time, min_comment_length |
| thesis_paper | `node_teacher_level` | config_id, level, allow_reject, review_mode(ALL/SAMPLE), push_to_student |
| thesis_paper | `paper` | school_id, batch_id, student_id, current_node, overall_status |
| thesis_paper | `paper_node_status` | paper_id, node_type, status, submit_count |
| thesis_paper | `paper_submit_record` | paper_id, node_type, student_id, file_url, submitted_at |
| thesis_paper | `paper_review_record` | paper_id, node_type, teacher_id, teacher_level, decision, comment, score |
| thesis_paper | `paper_annotation` | paper_id, teacher_id, type, page, position_json, color, comment |
| thesis_paper | `paper_topic` | paper_id, title, topic_hash, source(SELF/AI/PRESET) |
| thesis_paper | `paper_outline` | paper_id, outline_json |
| thesis_paper | `paper_content` | paper_id, node_id, content_html, word_count, version |
| thesis_paper | `teaching_relationship` | batch_id, student_id, teacher_id, teacher_type(MAIN/ASSIST), level |
| thesis_paper | `paper_deadline_extension` | paper_id, node_type, student_id, teacher_id, extend_hours, expire_at |
| thesis_paper | `electronic_signature` | user_id, batch_id, sign_type(WECHAT/UPLOAD), image_url, review_status |
| thesis_template | `paper_template` | school_id, name, major_ids_json, styles_json, file_url |
| thesis_template | `outline_template` | school_id, major_id, name, outline_json |
| thesis_im | `im_group` | school_id, batch_id, group_type(BATCH/GUIDE/CUSTOM), name |
| thesis_im | `im_message` | group_id, sender_id, msg_type, content, is_filtered, created_at |
| thesis_im | `defense_group` | batch_id, name, defense_type(SYNC/ASYNC), defense_time |
| thesis_statistics | `stat_paper_summary` | batch_id, teaching_point_id, major_name, teacher_id, [各节点统计字段], stat_time |

### 4.4 Flyway 迁移规范

**所有 Schema 变更必须通过 Flyway 脚本，禁止直接操作生产库结构。**

```
thesis-ops/flyway/
├── thesis_paper/
│   ├── V1.0.0__init_paper_schema.sql
│   ├── V1.1.0__add_multi_teacher_level.sql
│   └── V1.2.0__add_deadline_extension.sql
└── thesis_user/
    └── V1.0.0__init_user_schema.sql

命名规则：V<大版本>.<小版本>.<序号>__<英文描述>.sql
```

---

## 五、后端开发规范

### 5.1 统一响应体（禁止自定义格式）

```java
// 定义于 thesis-common，所有服务依赖
@Data
public class Result<T> {
    private Integer code;     // 200成功 400参数错误 401未认证 403无权限 500服务错误
    private String message;
    private T data;
    private Long timestamp;

    public static <T> Result<T> ok(T data) { ... }
    public static <T> Result<T> fail(int code, String message) { ... }
}

@Data
public class PageResult<T> {
    private Long total;
    private Integer pages;
    private Integer current;
    private Integer size;
    private List<T> records;
}
```

### 5.2 接口路径规范

```
格式：/api/v1/<模块名>/<资源>

示例：
  GET    /api/v1/paper/list                      # 论文列表（分页）
  POST   /api/v1/paper/submit                    # 提交论文节点
  PUT    /api/v1/paper/{id}/review               # 审核论文
  DELETE /api/v1/batch/{id}                      # 删除批次
  GET    /api/v1/statistics/paper-process        # 论文过程统计
  POST   /api/v1/user/import                     # 批量导入（返回 taskId）
  GET    /api/v1/user/import/{taskId}/progress   # 查询导入进度

分页参数：pageNum（从 1 开始）、pageSize（默认 20，最大 100）
```

### 5.3 异常处理规范

```java
// 业务异常统一使用 BusinessException，禁止 throw new RuntimeException()
public class BusinessException extends RuntimeException {
    private final int code;
    public BusinessException(String message) { this(400, message); }
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}

// 全局异常处理器（thesis-common 中定义）
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException e) {
        log.warn("业务异常: code={}, message={}", e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public Result<Void> handleUnknown(Exception e) {
        log.error("系统异常", e);
        return Result.fail(500, "系统繁忙，请稍后重试");
    }
}

// 严禁吞异常（以下写法禁止出现）
catch (Exception e) { /* 空 catch */ }
```

### 5.4 权限校验规范

```java
// 接口级权限注解
@SaCheckPermission("paper:review")
@SaCheckPermission({"paper:review", "paper:annotate"}, mode = SaMode.OR)

// 获取当前登录用户（禁止通过参数传递 userId）
Long userId = StpUtil.getLoginIdAsLong();
LoginUser currentUser = (LoginUser) StpUtil.getSession().get("userInfo");

// 数据权限：MyBatis-Plus 拦截器自动注入 school_id / teaching_point_id 过滤
// 禁止在业务代码中手写 school_id 过滤条件
```

### 5.5 关键业务规则（强制执行）

#### 论文流程引擎

- 所有节点的启用/顺序/是否强制/是否需要审核，必须读取 `batch_flow_config` 表，**禁止硬编码**
- 强制顺序校验：提交节点前，验证 `sort_order` 更小的所有 `is_required=1` 节点均已 `APPROVED`
- 时间窗口校验：检查 `[start_time, end_time]`，或查 `paper_deadline_extension` 授权
- 状态变更必须在同一 `@Transactional` 事务内，并写操作日志

#### 选题去重（双重保障）

```java
// 必须同时使用 Redisson 分布式锁 + 数据库唯一索引，缺一不可
String lockKey = "topic:lock:" + batchId + ":" + topicHash;
RLock lock = redissonClient.getLock(lockKey);
boolean locked = lock.tryLock(0, 30, TimeUnit.SECONDS);
if (!locked) throw new BusinessException("该选题已被选择，请重新选择");
// ...双重校验 + 写库
```

#### 多导师审核顺序

- 低 level 教师 `APPROVED` 后，自动生成高 level 教师待审核任务
- 末级 `APPROVED` 触发节点完成，流转下一节点
- 任意 level `REJECTED` 时，按 `allow_reject` 配置决定是否退回学生

#### 评语最低字数

- 教师提交审核时，后端 Service 层校验评语字数 ≥ `batch_flow_config.min_comment_length`
- 前端校验仅为辅助提示，不可替代后端校验

#### 批量导入异步化

- 单次 > 50 条走异步（RocketMQ），立即返回 `taskId`
- 进度存 Redis：Key `import:task:{taskId}`，TTL 24h
- 单批写库 500 条：`saveBatch(list, 500)`

### 5.6 缓存 Key 命名规范（强制遵守）

```
auth:token:{userId}              # 用户 Token，TTL 7d
auth:perm:{userId}               # 权限列表，TTL 1h
paper:status:{paperId}           # 论文状态，TTL 30min
stat:batch:{batchId}             # 批次统计，TTL 5min
sms:{phone}                      # 短信验证码，TTL 5min
import:task:{taskId}             # 导入任务进度，TTL 24h
topic:lock:{batchId}:{hash}      # 选题分布式锁

缓存操作原则：
  - 写操作：先更新 DB，再删除缓存（Cache-Aside）
  - 统计数据：定时任务刷新，不做实时更新
  - 禁止缓存 null 值（防穿透用布隆过滤器）
  - 所有缓存操作必须设置 TTL，禁止永不过期（选题锁除外）
```

### 5.7 消息队列 Topic 规范

```
Topic 命名（thesis-<动作>）：
  thesis-paper-submit         # 论文提交事件
  thesis-paper-review         # 论文审核事件
  thesis-user-import          # 用户批量导入任务
  thesis-notify-send          # 通知发送任务
  thesis-ai-evaluate          # AI 评议任务
  thesis-ai-ideology-scan     # 意识形态扫描任务
  thesis-stat-refresh         # 统计刷新任务

消费者组命名（<服务名>-<topic简称>-group）：
  thesis-notify-send-group
  thesis-ai-evaluate-group

规则：
  1. 消费者必须实现幂等（消息可能重复投递）
  2. 消费失败自动重试 3 次，超过进入死信队列
  3. 死信队列必须有告警机制
```

### 5.8 AI 接口调用规范

```java
// 所有 AI 功能必须通过 AiServiceFacade，禁止在业务服务中直接调用大模型 SDK

// 同步调用（选题推荐、摘要生成）
String result = aiServiceFacade.generateText(systemPrompt, userPrompt);

// 流式调用（大纲推荐实时展示）
Flux<String> stream = aiServiceFacade.generateTextStream(systemPrompt, userPrompt);

// 异步调用（批量评议、意识形态扫描）→ 发 MQ，异步消费，前端轮询结果
rocketMQTemplate.send("thesis-ai-evaluate", EvaluateMessage.of(paperId, batchId));

// Facade 内已封装：超时 30s，重试 3 次（2s 指数退避），熔断 60s
```

### 5.9 日志规范

```java
// 使用 @Slf4j 注解，禁止 System.out.println
// INFO：关键业务节点（提交/审核/状态变更/登录）
// WARN：业务预期内的失败（参数错误/权限不足）
// ERROR：不可预期的系统异常
// DEBUG：开发调试，生产环境必须关闭

// 日志必须包含关键业务标识符
log.info("[论文提交] paperId={}, nodeType={}, studentId={}", paperId, nodeType, studentId);

// 禁止在日志中打印：密码、Token、身份证号等敏感信息
```

### 5.10 代码质量红线（以下问题禁止合并）

- [ ] 循环内执行数据库查询（N+1 问题）
- [ ] 直接使用 `new RuntimeException()` 抛业务异常
- [ ] Controller 中包含业务逻辑
- [ ] 敏感信息硬编码（密码/AK/SK/密钥）
- [ ] 缓存操作未设置 TTL
- [ ] AI 接口未通过 Facade 调用
- [ ] Schema 变更未提供 Flyway 脚本
- [ ] 单个方法超过 100 行
- [ ] 缺少接口入参校验注解
- [ ] 并发场景缺少分布式锁保护

---

## 六、前端开发规范

### 6.1 TypeScript 规范

```typescript
// 禁止使用 any，所有数据必须有类型定义
// 枚举统一在 src/types/enums.ts 中定义

export enum PaperStatus {
  PENDING = 'PENDING',
  SUBMITTED = 'SUBMITTED',
  REVIEWING = 'REVIEWING',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED'
}

export enum FlowNodeType {
  SIGN = 'SIGN',         TOPIC = 'TOPIC',
  OUTLINE = 'OUTLINE',   TOPIC_FORM = 'TOPIC_FORM',
  TASK_BOOK = 'TASK_BOOK', DRAFT_SEG = 'DRAFT_SEG',
  DRAFT_FULL = 'DRAFT_FULL', FINAL_DRAFT = 'FINAL_DRAFT',
  FINAL = 'FINAL'
}
```

### 6.2 组件规范

```vue
<!-- 统一使用 <script setup>，禁止 Options API -->
<script setup lang="ts">
interface Props {
  paperId: number
  readonly?: boolean
}
const props = withDefaults(defineProps<Props>(), { readonly: false })
const emit = defineEmits<{
  submitted: [paperId: number]
  cancelled: []
}>()
</script>

<!-- 组件文件名：PascalCase，如 PaperSubmitForm.vue -->
<!-- 组合式函数：use 前缀，如 usePaperFlow.ts -->
```

### 6.3 API 请求规范

```typescript
// 禁止在视图组件中直接使用 axios，统一通过 src/api/ 目录调用
export const paperApi = {
  submitNode: (paperId: number, nodeType: string, data: FormData) =>
    request.post<void>(`/paper/${paperId}/submit`, data),
  list: (params: PaperListParams) =>
    request.get<PageResult<PaperVO>>('/paper/list', { params })
}
```

### 6.4 文件上传规范（预签名直传）

```typescript
// 所有文件上传必须使用预签名 URL 直传 OSS，禁止通过业务服务器中转
const { uploadUrl, fileKey } = await fileApi.getPresignedUrl({ fileName, fileType, bizType })
await axios.put(uploadUrl, file, { headers: { 'Content-Type': file.type } })
await fileApi.confirmUpload({ fileKey, paperId, nodeType })
```

### 6.5 图表规范

```typescript
// ECharts 统一封装在 src/components/ChartPanel/index.vue
// 状态颜色规范（统一不得随意更改）：
// 已通过：#67C23A  审核中：#E6A23C  已驳回：#F56C6C  未提交：#909399  主题色：#409EFF
```

---

## 七、Git 工作流规范

### 7.1 分支策略

```
main       # 生产，只接受 release/* 的 PR，合并后打 Tag
develop    # 集成测试，功能完成后合并至此
feature/*  # 功能开发，从 develop 拉出
           # 命名：feature/<模块>-<描述>
           # 示例：feature/paper-flow-engine
           #       feature/im-sensitive-word
           #       feature/ai-abstract-generation
hotfix/*   # 生产紧急修复，修复后同时合并至 main 和 develop
release/*  # 发版准备，从 develop 拉出
```

### 7.2 Commit Message 规范

```
<type>(<scope>): <subject>

type：feat | fix | refactor | perf | test | docs | chore | style
scope：paper | user | auth | im | defense | ai | statistics | template | file | notify | gateway

示例：
  feat(paper): 实现论文流程引擎，支持自定义节点顺序和强制顺序
  fix(im): 修复敏感词 AC 自动机热更新后并发空指针问题
  perf(statistics): 改为预计算方式，消除首页统计接口超时
  feat(ai): 新增意识形态扫描，支持涉政/敏感词/价值观多维度检测
```

### 7.3 PR 规范（缺项拒绝合并）

PR 描述必须包含：
1. 改动摘要：做了什么、为什么
2. 测试说明：验证方式（截图/测试结果）
3. 数据库变更：是否有 Schema 改动，Flyway 脚本是否已提交
4. 接口变更：接口新增/修改，文档是否更新
5. 影响范围：是否影响其他模块

Review 标准：至少 1 人通过；涉及论文核心流程需 2 人通过；CI 全部通过。

---

## 八、AI 辅助开发行为约束

### 8.1 会话启动检查（每次必做）

1. 确认已读取本文件（CLAUDE.md）当前版本
2. 确认当前 Git 分支和所在模块
3. 明确本次任务目标

### 8.2 代码生成约束

| 场景 | 强制要求 |
|---|---|
| 生成实体类 | 必须包含 id/created_at/updated_at/is_deleted 四个基础字段 |
| 生成接口 | 返回值必须用 `Result<T>` 或 `Result<PageResult<T>>` 包装 |
| 生成 Service | 接口 + 实现类分离，禁止 Controller 写业务逻辑 |
| 论文状态变更 | 必须在同一 `@Transactional` 内，并记录操作日志 |
| 选题相关 | 必须加 Redisson 分布式锁，不得省略 |
| 文件上传 | 必须使用预签名 URL 直传方案 |
| 生成 SQL | 必须说明索引覆盖情况 |
| 跨服务调用 | 使用 Feign 接口或 MQ 事件，不得直接 HTTP 调用 |
| AI 调用 | 必须通过 AiServiceFacade，不得直接调用大模型 SDK |
| 缓存操作 | 必须设置 TTL，Key 必须符合命名规范 |

### 8.3 数据库操作约束

- 禁止生成直接操作生产库的裸 DDL/DML
- Schema 变更必须同步生成 Flyway 迁移文件
- 批量写入必须分批（≤ 500 条/批）
- 所有删除操作必须是逻辑删除（`UPDATE ... SET is_deleted=1`）

### 8.4 安全约束

- 禁止硬编码任何密码、Token、AK/SK，使用 `${config.xxx}` 占位
- 禁止生成任何绕过 `@SaCheckPermission` 的代码路径
- 禁止生成"临时关闭鉴权以便调试"的代码提交至非 local 分支

### 8.5 生成代码后的人工核查清单

- [ ] 包路径符合 §2.3 规定结构
- [ ] 无 N+1 查询（循环内 DB 调用）
- [ ] 事务边界合理（`@Transactional` 范围）
- [ ] 异常使用 `BusinessException`
- [ ] 关键节点有 INFO 日志
- [ ] 接口有权限注解
- [ ] 数据库查询利用了索引
- [ ] 并发场景有分布式锁

---

## 九、演示功能优先级（P0 最高）

以下功能在招标文件中要求**在山东省政府采购电子交易系统线上演示**，开发和测试优先级最高：

| 优先级 | 功能 | 所在服务 | 演示要点 |
|---|---|---|---|
| P0 | 多导师管理 | thesis-paper | 多级导师配置、独立权限、流转过程 |
| P0 | 在线群组即时通讯 | thesis-im | 群组创建、收发消息、敏感词拦截 |
| P0 | 在线答辩 | thesis-defense | 音视频房间、主持人权限、录像 |
| P0 | 在线批注及评分 | thesis-paper | PDF 批注、评分表、评语字数校验 |
| P0 | 大纲推荐（三种模式）| thesis-ai | AI推荐/专业模板/自行编写演示 |

**P0 功能改动必须在 staging 环境完整验证后方可合并至 main。**

---

## 十、非功能性开发检查

### 10.1 性能检查（提交前验证）

| 代码类型 | 验证方式 | 通过标准 |
|---|---|---|
| 列表查询接口 | 数据量 > 10 万条压测 | P99 ≤ 500ms |
| 批量导入（5000 条）| 计时功能测试 | 异步完成 ≤ 60s |
| 统计接口（非缓存）| 直接调用测试 | P99 ≤ 1s |
| WebSocket 并发 | JMeter 500 连接 | 无连接丢失 |

### 10.2 安全发版检查

- [ ] OWASP Dependency Check：无高危漏洞
- [ ] SonarQube：无 Critical/Blocker
- [ ] 所有接口有权限注解或明确标注为公开接口
- [ ] 文件上传白名单：仅允许 pdf/doc/docx/jpg/png/mp4/zip
- [ ] 敏感词库更新时间 ≤ 30 天

---

## 十一、环境与配置管理

### 11.1 环境分层

| 环境 | 分支 | 触发方式 | 用途 |
|---|---|---|---|
| local | 任意 | 手动（Docker Compose）| 本地开发 |
| dev | develop | push 自动触发 | 功能联调 |
| staging | release/* | 手动触发 | 验收演示 |
| prod | main（Tag）| 人工审批 + 蓝绿发布 | 生产 |

### 11.2 本地开发快速启动

```bash
# 启动中间件（MySQL/Redis/Nacos/RocketMQ/MinIO/ES）
cd thesis-ops/local && docker-compose up -d

# 初始化数据库（仅首次）
cd thesis-ops/flyway && ./migrate.sh local

# IDEA 启动服务：Profile 选 local，先启动 thesis-gateway

# 前端
cd thesis-web && npm install && npm run dev
# 访问 http://localhost:5173
```

### 11.3 配置管理规则

```yaml
# 允许提交至 Git（占位符形式）
spring.datasource.password: ${MYSQL_PASSWORD}

# 禁止提交至 Git（通过 Nacos 配置中心或环境变量注入）
# 数据库密码、Redis 密码、大模型 API Key、JWT 签名密钥、OSS AK/SK

# 本地开发：创建 ~/.thesis/local.env（已加入 .gitignore）
```

---

## 十二、发版交付物检查清单

| 交付物 | 检查方式 |
|---|---|
| 后端源码（含 Git 完整历史）| `git log --oneline` 确认提交完整 |
| 前端源码（含 Git 完整历史）| 同上 |
| Flyway 全量迁移脚本 | 空库 `flyway migrate` 验证通过 |
| K8s 部署配置 | `kubectl apply --dry-run=client` 验证 |
| Knife4j 接口文档（OpenAPI JSON）| 导出并验证格式正确 |
| 管理员操作手册（PDF）| 覆盖学校管理端和教学点管理端全部功能 |
| 用户使用手册（PDF）| 覆盖教师端和学生端全部功能 |
| 功能测试报告 | 覆盖全部 F-001 ~ F-053 功能项 |
| 性能测试报告 | 10,000 并发压测通过 |
| 数据备份方案文档 | 含恢复步骤和首次演练记录 |

---

*版本：V2.0 | 日期：2026-04-21 | 基于《宸章论文综合服务系统_技术实现方案 V1.0》*  
*修改本文件须经团队评审，并更新版本号和日期。*
