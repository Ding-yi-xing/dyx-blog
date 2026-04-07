# dyx-blog 接口设计文档

## 1. 文档概述

### 1.1 项目定位
`dyx-blog` 是一个个人品牌展示、求职简历展示、博客内容发布与后台管理一体化项目。

当前前台页面包括：
- 首页 `/`
- 关于我 `/about`
- 简历 `/resume`
- 留言 `/guestbook`
- 动态列表 `/moments`
- 动态详情 `/moments/:id`
- 博客列表 `/blog`
- 博客详情 `/blog/:id`

当前后台页面统一使用隐藏后的管理入口：
- 登录页 `/dyx-manager/login`
- 后台首页 `/dyx-manager`

### 1.2 接口风格
- 协议：HTTP / JSON
- 统一前端请求基础路径：`/api`，见 `frontend/src/api/http.ts:8-11`
- 前台接口前缀：`/api/site`，见 `backend/src/main/java/com/dyx/blog/controller/publics/SiteController.java:32`
- 后台接口前缀：`/api/dyx-manager`，见 `backend/src/main/java/com/dyx/blog/controller/admin/AdminController.java:42`
- 认证接口前缀：`/api/auth`，见 `backend/src/main/java/com/dyx/blog/controller/auth/AuthController.java:20`
- 后端成功响应结构：`{ code, message, data }`
- 时间字段常用格式：`yyyy-MM-dd HH:mm:ss`
- 文件资源字段：统一使用可直接访问的 URL 字符串
- 多图字段：通常使用 JSON 字符串数组，如 `imageUrls: "[\"/media/a.jpg\",\"/media/b.jpg\"]"`

### 1.3 认证说明
除登录接口外，后台接口均要求携带 JWT：

```http
Authorization: Bearer <token>
```

前端在请求拦截器中自动注入 Token，见 `frontend/src/api/http.ts:33-39`。

### 1.4 通用响应结构
```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

说明：
- `code = 200`：请求成功
- `message`：提示信息，成功时通常为 `success`
- `data`：业务数据主体

### 1.5 鉴权与跳转行为
- 后台未登录访问受保护路由时，会跳转到 `/dyx-manager/login`，见 `frontend/src/router/index.ts:95-103`
- 接口返回 `401` 时，前端会清除登录态并重定向登录页，见 `frontend/src/api/http.ts:53-61`
- 接口返回 `403` 时，前端会提示“当前账号无权限访问该功能”
- 公开站点不再暴露显式后台入口；首页保留点击站点标题 5 次进入后台登录页的隐藏入口

---

## 2. 主要数据模型

### 2.1 ProfileData
用于首页、关于我、简历、留言页与后台资料管理。

| 字段 | 类型 | 必填 | 说明 |
|---|---|---:|---|
| id | number | 否 | 主键 |
| siteTitle | string | 否 | 站点标题 |
| heroTitle | string | 否 | 首页主标题 |
| heroSubtitle | string | 否 | 首页副标题 |
| heroConfig | string | 否 | 首页 Hero 区块配置 JSON |
| aboutContent | string | 否 | 关于我正文 |
| educationExperience | string | 否 | 教育经历 |
| workExperience | string | 否 | 工作经历 |
| email | string | 否 | 邮箱 |
| phone | string | 否 | 电话 |
| wechat | string | 否 | 微信 |
| githubUrl | string | 否 | GitHub 地址 |
| avatarUrl | string | 否 | 头像地址 |
| resumePdfUrl | string | 否 | PDF 简历地址 |
| guestbookIntro | string | 否 | 留言页介绍文案 |
| contactMethods | string | 否 | 联系方式 JSON 配置 |

### 2.2 PostData
| 字段 | 类型 | 必填 | 说明 |
|---|---|---:|---|
| id | number | 是 | 主键 |
| title | string | 是 | 标题 |
| summary | string | 否 | 摘要 |
| content | string | 否 | 正文 |
| coverImage | string | 否 | 封面图 |
| category | string | 否 | 分类 |
| tags | string | 否 | 标签 |
| published | number | 否 | 是否发布，1=已发布 |
| updatedAt | string | 否 | 更新时间 |
| viewCount | number | 否 | 阅读量 |

### 2.3 MomentData
| 字段 | 类型 | 必填 | 说明 |
|---|---|---:|---|
| id | number | 是 | 主键 |
| title | string | 是 | 标题 |
| content | string | 否 | 内容 |
| coverImage | string | 否 | 封面图 |
| imageUrls | string | 否 | 图集 JSON 字符串 |
| happenedAt | string | 否 | 发生时间 |
| sortOrder | number | 否 | 排序值 |
| published | number | 否 | 发布状态 |
| updatedAt | string | 否 | 更新时间 |

### 2.4 ProjectData
| 字段 | 类型 | 必填 | 说明 |
|---|---|---:|---|
| id | string \| number | 是 | 主键（Long 主键以字符串返回，避免前端精度丢失） |
| name | string | 是 | 项目名称 |
| roleName | string | 否 | 角色名 |
| description | string | 否 | 项目描述 |
| techStack | string | 否 | 技术栈 |
| projectLink | string | 否 | 项目链接 |
| coverImage | string | 否 | 封面图 |
| sortOrder | number | 否 | 排序值 |
| published | number | 否 | 发布状态 |
| updatedAt | string | 否 | 更新时间 |
### 2.5 WorkData
| 字段 | 类型 | 必填 | 说明 |
|---|---|---:|---|
| id | number | 是 | 主键 |
| title | string | 是 | 作品名称 |
| summary | string | 否 | 简介 |
| coverImage | string | 否 | 封面图 |
| imageUrls | string | 否 | 图集 JSON 字符串 |
| videoUrl | string | 否 | 视频地址 |
| videoPoster | string | 否 | 视频封面 |
| workLink | string | 否 | 外链 |
| sortOrder | number | 否 | 排序值 |
| published | number | 否 | 发布状态 |
| updatedAt | string | 否 | 更新时间 |

### 2.6 HonorData
| 字段 | 类型 | 必填 | 说明 |
|---|---|---:|---|
| id | number | 是 | 主键 |
| title | string | 是 | 荣誉名称 |
| issuer | string | 否 | 授予机构 |
| description | string | 否 | 荣誉说明 |
| coverImage | string | 否 | 封面图 |
| imageUrls | string | 否 | 图集 JSON 字符串 |
| attachmentUrl | string | 否 | 证书 PDF / 附件 |
| awardAt | string | 否 | 获奖时间 |
| sortOrder | number | 否 | 排序值 |
| published | number | 否 | 发布状态 |
| updatedAt | string | 否 | 更新时间 |

### 2.7 FootprintData
| 字段 | 类型 | 必填 | 说明 |
|---|---|---:|---|
| id | number | 否 | 主键 |
| cityName | string | 是 | 城市或区县名称 |
| countryName | string | 否 | 国家名 |
| regionName | string | 否 | 省级行政区名 |
| positionX | number | 否 | 地图保留字段 |
| positionY | number | 否 | 地图保留字段 |
| visitedAt | string | 否 | 到访时间 |
| description | string | 否 | 足迹说明 |
| importance | number | 否 | 高亮权重 |
| sortOrder | number | 否 | 排序值 |
| published | number | 否 | 发布状态 |
| createdAt | string | 否 | 创建时间 |
| updatedAt | string | 否 | 更新时间 |

### 2.8 GuestbookMessageData
| 字段 | 类型 | 必填 | 说明 |
|---|---|---:|---|
| id | number | 否 | 主键 |
| content | string | 否 | 留言内容 |
| published | number | 否 | 是否公开 |
| createdAt | string | 否 | 创建时间 |
| updatedAt | string | 否 | 更新时间 |

### 2.9 GuestbookData
| 字段 | 类型 | 必填 | 说明 |
|---|---|---:|---|
| guestbookIntro | string | 否 | 留言页介绍文案 |
| messages | GuestbookMessageData[] | 否 | 留言列表（已脱敏，未包含 IP 等敏感字段） |

### 2.11 DashboardSummaryData
| 字段 | 类型 | 说明 |
|---|---|---|
| postCount | number | 文章数量 |
| momentCount | number | 动态数量 |
| honorCount | number | 荣誉数量 |
| userCount | number | 用户数量 |
| totalPostViews | number | 博客总浏览量 |
| totalSiteVisits | number | 全站累计访问量 |
| dailySiteVisits | object[] | 最近访问趋势 |
| deviceTypeDistribution | object[] | 设备分布 |
| topVisitedPages | object[] | 热门页面 |

### 2.12 SystemConfigData
| 字段 | 类型 | 必填 | 说明 |
|---|---|---:|---|
| id | number | 否 | 主键 |
| storageType | string | 否 | 存储类型，`local` / `oss` |
| ossEndpoint | string | 否 | OSS Endpoint |
| ossRegion | string | 否 | OSS 区域 |
| ossBucketName | string | 否 | OSS Bucket |
| ossPublicUrlPrefix | string | 否 | OSS 公网前缀 |
| ossBaseDir | string | 否 | OSS 基础目录 |
| footprintEyebrow | string | 否 | 足迹区眉标题 |
| footprintTitle | string | 否 | 足迹区主标题 |
| footprintSubtitle | string | 否 | 足迹区副标题 |
| footprintDescription | string | 否 | 足迹区说明 |
| copyrightText | string | 否 | 版权文案 |
| techSupportText | string | 否 | 技术支持文案 |
| updatedAt | string | 否 | 更新时间 |

---

## 3. 认证接口

### 3.1 管理员登录
**POST** `/api/auth/login`

来源：`backend/src/main/java/com/dyx/blog/controller/auth/AuthController.java:32-39`

#### 请求体
| 字段 | 类型 | 必填 | 说明 |
|---|---|---:|---|
| username | string | 是 | 用户名 |
| password | string | 是 | 密码 |

#### 请求示例
```json
{
  "username": "admin",
  "password": "******"
}
```

#### 响应体 data
| 字段 | 类型 | 说明 |
|---|---|---|
| token | string | JWT |
| user.id | string \| number | 用户 ID，后端 Long 主键会按字符串序列化返回 |
| user.username | string | 用户名 |
| user.displayName | string | 显示名 |
| user.role | string | 角色 |

---

## 3.2 预留：平台注册与站点初始化（未实现，仅文档规划）

为未来支持“任意用户注册并创建自己的博客站点”，在接口层预留如下设计（当前实现中暂未提供对应 Controller 与路由，仅作为后续演进依据）：

### 3.2.1 用户注册
**POST** `/api/auth/register`

- 说明：
  - 支持通过账号密码完成注册
  - 初期不强制邮箱校验与人工审核
  - 注册成功后自动登录或返回登录所需凭证
- 请求体示例：
  - `username`: 用户名
  - `password`: 密码
  - （预留）`displayName`：显示昵称
- 响应体示例：
  - 注册成功的用户基础信息
  - 可选返回初始站点简要信息

### 3.2.2 站点初始化
**POST** `/api/site/setup`

- 说明：
  - 在用户首次登录后台时，引导完成站点初始化
  - 创建并绑定一个默认站点（例如基于用户名生成 `siteSlug`）
  - 写入基础资料：站点标题、副标题、关于我、联系方式、主题配置等
- 请求体示例：
  - `siteTitle`: 站点标题
  - `heroTitle`: 首页主标题
  - `heroSubtitle`: 首页副标题
  - `aboutContent`: 关于我
  - `contactMethods`: 联系方式配置
  - 其他站点级基础字段

以上注册与站点初始化接口为规划中的预留设计，当前阶段不会对前后端代码造成影响，仅在文档中同步需求方向，便于后续版本统一实现。

---

## 4. 前台接口

来源：`backend/src/main/java/com/dyx/blog/controller/publics/SiteController.java:42-173`

### 4.1 获取首页聚合数据
**GET** `/api/site/home`

- 返回 `HomeData`
- 用于首页展示资料、最新博客、最新动态、精选项目、最新荣誉与足迹

### 4.2 获取个人资料
**GET** `/api/site/profile`

- 返回 `ProfileData`
- 用于 About、Resume 等页面基础资料展示

### 4.3 获取留言页数据
**GET** `/api/site/guestbook`

- 返回 `GuestbookData`
- 包含留言页介绍文案与公开留言列表

### 4.4 提交留言
**POST** `/api/site/guestbook/messages`

#### 请求体
`Partial<GuestbookMessageData>`

#### 示例
```json
{
  "content": "你好，站点设计很不错。"
}
```

### 4.5 获取动态列表
**GET** `/api/site/moments`

- 返回 `MomentData[]`

### 4.6 获取动态详情
**GET** `/api/site/moments/{id}`

#### 路径参数
| 字段 | 类型 | 必填 | 说明 |
|---|---|---:|---|
| id | number | 是 | 动态主键 |

- 返回 `MomentData`

### 4.7 获取项目经历列表
**GET** `/api/site/projects`

- 返回 `ProjectData[]`

### 4.8 获取作品列表
**GET** `/api/site/works`

- 返回 `WorkData[]`

### 4.9 获取荣誉列表
**GET** `/api/site/honors`

- 返回 `HonorData[]`

### 4.10 获取足迹列表
**GET** `/api/site/footprints`

- 返回 `FootprintData[]`

### 4.11 记录页面访问
**POST** `/api/site/visit/{pageKey}`

#### 路径参数
| 字段 | 类型 | 必填 | 说明 |
|---|---|---:|---|
| pageKey | string | 是 | 页面标识，如 `home`、`about`、`resume`、`moments`、`blog`、`blog-detail` |

#### 说明
- 用于前端显式上报页面访问量
- 服务端会记录累计访问统计与访问日志
- 会结合请求头解析 IP、设备类型、User-Agent 等信息

### 4.12 获取博客列表
**GET** `/api/site/posts`

#### 查询参数
| 字段 | 类型 | 必填 | 说明 |
|---|---|---:|---|
| page | number | 否 | 页码，未传时返回默认列表 |
| pageSize | number | 否 | 每页条数 |

- 返回 `PostData[]`
- 当前公开博客列表接口已支持 `page` / `pageSize` 可选分页参数
- 旧文档中的 `/api/posts` 已不再适用，当前以后端控制器路径为准

### 4.13 获取博客详情
**GET** `/api/site/posts/{id}`

#### 路径参数
| 字段 | 类型 | 必填 | 说明 |
|---|---|---:|---|
| id | number | 是 | 文章主键 |

#### 说明
- 返回 `PostData`
- 服务端会在详情读取时累加文章浏览量

---

## 5. 后台接口

来源：`backend/src/main/java/com/dyx/blog/controller/admin/AdminController.java:53-585`

### 5.1 获取仪表盘摘要
**GET** `/api/dyx-manager/dashboard/summary`

- 返回 `DashboardSummaryData`
- 用于仪表盘展示内容统计、总浏览量、总访问量、趋势图、设备分布与热门页面

### 5.2 获取访问日志列表
**GET** `/api/dyx-manager/visit-logs`

#### 查询参数
| 字段 | 类型 | 必填 | 说明 |
|---|---|---:|---|
| startTime | string | 否 | 开始时间，格式 `yyyy-MM-dd HH:mm:ss` |
| endTime | string | 否 | 结束时间，格式 `yyyy-MM-dd HH:mm:ss` |
| pageKey | string | 否 | 页面标识 |
| deviceType | string | 否 | 设备类型 |
| ipAddress | string | 否 | IP 地址 |
| page | number | 否 | 页码，默认 1 |
| pageSize | number | 否 | 每页数量，默认 20 |

#### 响应体
- 返回分页结构 `PageResult<Map<String, Object>>`
- 包含列表数据及 `total/page/pageSize`

### 5.3 删除单条访问日志
**DELETE** `/api/dyx-manager/visit-logs/{id}`

说明：仅 `ADMIN` 角色可删除。

### 5.4 批量删除访问日志
**POST** `/api/dyx-manager/visit-logs/batch-delete`

#### 请求体
`number[]`

说明：仅 `ADMIN` 角色可执行批量删除。

### 5.5 留言管理
- **GET** `/api/dyx-manager/guestbook`：获取留言管理数据
- **PUT** `/api/dyx-manager/guestbook/intro`：更新留言介绍文案
- **PUT** `/api/dyx-manager/guestbook/messages/{id}`：更新单条留言
- **DELETE** `/api/dyx-manager/guestbook/messages/{id}`：删除单条留言

### 5.6 文章管理
- **GET** `/api/dyx-manager/posts`：获取文章列表
- **POST** `/api/dyx-manager/posts`：新增文章
- **PUT** `/api/dyx-manager/posts/{id}`：更新文章
- **DELETE** `/api/dyx-manager/posts/{id}`：删除文章

### 5.7 动态管理
- **GET** `/api/dyx-manager/moments`
- **POST** `/api/dyx-manager/moments`
- **PUT** `/api/dyx-manager/moments/{id}`
- **DELETE** `/api/dyx-manager/moments/{id}`

### 5.8 项目经历管理
- **GET** `/api/dyx-manager/projects`
- **POST** `/api/dyx-manager/projects`
- **PUT** `/api/dyx-manager/projects/{id}`
- **DELETE** `/api/dyx-manager/projects/{id}`

### 5.9 个人作品管理
- **GET** `/api/dyx-manager/works`
- **POST** `/api/dyx-manager/works`
- **PUT** `/api/dyx-manager/works/{id}`
- **DELETE** `/api/dyx-manager/works/{id}`

### 5.10 荣誉管理
- **GET** `/api/dyx-manager/honors`
- **POST** `/api/dyx-manager/honors`
- **PUT** `/api/dyx-manager/honors/{id}`
- **DELETE** `/api/dyx-manager/honors/{id}`

### 5.11 首页足迹管理
- **GET** `/api/dyx-manager/footprints`
- **POST** `/api/dyx-manager/footprints`
- **PUT** `/api/dyx-manager/footprints/{id}`
- **DELETE** `/api/dyx-manager/footprints/{id}`

### 5.12 首页 Hero 配置管理
- **GET** `/api/dyx-manager/profile/hero`
- **PUT** `/api/dyx-manager/profile/hero`

说明：用于首页首屏区块文案、图片、背景图等配置。

### 5.13 个人资料管理
- **GET** `/api/dyx-manager/profile`
- **PUT** `/api/dyx-manager/profile`

说明：用于管理 About / Resume / 联系方式 / 头像 / 简历 PDF 等共用资料。

### 5.14 系统配置管理
- **GET** `/api/dyx-manager/system-config`
- **PUT** `/api/dyx-manager/system-config`

说明：用于配置存储方式、OSS 参数、首页足迹文案与页脚文案。

### 5.15 用户管理
- **GET** `/api/dyx-manager/users`
- **POST** `/api/dyx-manager/users`
- **PUT** `/api/dyx-manager/users/{id}`
- **DELETE** `/api/dyx-manager/users/{id}`

说明：
- 仅 `ADMIN` 角色可管理用户
- 后端已补充唯一用户名校验、当前登录管理员保护、至少保留一个管理员账号等约束
- 用户 ID 在前后端交互中按 `string | number` 处理，避免 Long 精度丢失

### 5.16 媒体资源管理
- **GET** `/api/dyx-manager/media`：获取媒体列表
- **GET** `/api/dyx-manager/media/content?fileUrl=...`：同源代理读取媒体内容
- **POST** `/api/dyx-manager/media/upload`：上传文件
- **DELETE** `/api/dyx-manager/media/{id}`：删除媒体
- **POST** `/api/dyx-manager/media/import-existing`：导入 `backend/uploads` 已有文件

#### 上传接口说明
- Content-Type：`multipart/form-data`
- 表单字段：`file`

#### 使用流程说明
- 媒体库可直接选择原图/原文件用于业务表单
- 图片资源可在媒体选择弹窗中决定“直接使用”或“裁剪后使用”
- 头像、首页首屏背景图、首页右侧人物图等业务字段会在具体页面调用裁剪弹窗

#### 删除接口说明
删除前会做引用检查，若资源仍被个人资料、文章、项目、作品、动态或荣誉使用，则应拒绝删除。

---

## 6. 前端页面与接口映射

### 6.1 前台页面
| 页面 | 路由 | 主要接口 |
|---|---|---|
| 首页 | `/` | `/api/site/home`、`/api/site/visit/home` |
| 关于我 | `/about` | `/api/site/profile`、`/api/site/works`、`/api/site/honors` |
| 简历 | `/resume` | `/api/site/profile`、`/api/site/projects` |
| 留言 | `/guestbook` | `/api/site/guestbook`、`/api/site/guestbook/messages` |
| 动态列表 | `/moments` | `/api/site/moments` |
| 动态详情 | `/moments/:id` | `/api/site/moments/{id}` |
| 博客列表 | `/blog` | `/api/site/posts` |
| 博客详情 | `/blog/:id` | `/api/site/posts/{id}`、`/api/site/visit/blog-detail` |

### 6.2 后台页面
| 页面 | 路由 | 主要接口 |
|---|---|---|
| 登录 | `/dyx-manager/login` | `/api/auth/login` |
| 仪表盘 | `/dyx-manager/dashboard` | `/api/dyx-manager/dashboard/summary` |
| 访问日志 | `/dyx-manager/visit-logs` | `/api/dyx-manager/visit-logs` |
| 留言管理 | `/dyx-manager/guestbook` | `/api/dyx-manager/guestbook` |
| 博客管理 | `/dyx-manager/posts` | `/api/dyx-manager/posts` |
| 动态管理 | `/dyx-manager/moments` | `/api/dyx-manager/moments` |
| 荣誉管理 | `/dyx-manager/honors` | `/api/dyx-manager/honors` |
| 首页 Hero | `/dyx-manager/home/hero` | `/api/dyx-manager/profile/hero` |
| 首页足迹 | `/dyx-manager/home/footprints` | `/api/dyx-manager/footprints` |
| 关于我管理 | `/dyx-manager/about` | `/api/dyx-manager/profile` |
| 简历管理 | `/dyx-manager/resume` | `/api/dyx-manager/profile`、`/api/dyx-manager/projects` |
| 作品管理 | `/dyx-manager/works` | `/api/dyx-manager/works` |
| 媒体管理 | `/dyx-manager/media` | `/api/dyx-manager/media` |
| 系统配置 | `/dyx-manager/system-config` | `/api/dyx-manager/system-config` |
| 用户管理 | `/dyx-manager/users` | `/api/dyx-manager/users` |

---

## 7. 常见错误码

| code | 含义 | 说明 |
|---|---|---|
| 200 | 成功 | 请求成功 |
| 400 | 请求错误 | 参数错误、业务校验不通过、资源正在被引用等 |
| 401 | 未登录或 Token 失效 | 需要重新登录 |
| 403 | 无权限 | 当前账号无访问权限 |
| 404 | 资源不存在 | 主键对应记录不存在 |
| 500 | 服务端异常 | 后端处理失败 |

---

## 8. 本次更新说明

### 2026-03-30
- 将后台接口前缀从旧文档中的 `/api/admin` 全量修正为当前实现 `/api/dyx-manager`
- 将博客公开接口从旧文档中的 `/api/posts*` 修正为当前实现 `/api/site/posts*`
- 按当前控制器补齐留言、足迹、Hero 配置、系统配置、媒体代理、动态详情等接口说明
- 按当前前端路由补齐 `/guestbook`、`/moments/:id`、`/dyx-manager/home/*` 等页面与接口映射
- 明确前端 Axios 基础路径、401/403 行为与 Bearer Token 注入逻辑
- 删除过期接口描述，统一以当前代码实现为准

### 2026-03-31
- 复核公开站点与后台管理路由，确认当前前端管理入口仍为 `/dyx-manager` 体系
- 复核前后端接口前缀与统一返回体约定，确认 `frontend/src/api/http.ts` 与三类后端控制器路径保持一致
- 复核公开访问统计、登录鉴权与页面映射说明，确保文档与当前代码实现一致
