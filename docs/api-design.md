# dyx-blog 接口设计文档

## 1. 文档概述

### 1.1 项目定位
`dyx-blog` 是一个同时服务于个人品牌展示与求职表达的站点，前台当前聚焦以下模块：
- 首页 `/`
- 关于我 `/about`
- 简历 `/resume`
- 动态 `/moments`
- 博客 `/blog`

其中：
- “关于我”页面聚合个人信息、个人作品与荣誉时间线
- “简历”页面聚合教育经历、工作经历、项目经历与 PDF 简历下载
- 图片与附件能力不再作为独立模块对外展示，而是通过媒体资源统一支撑头像、荣誉图片、证书 PDF、项目封面与简历 PDF

### 1.2 接口风格
- 协议：HTTP / JSON
- 前台接口前缀：`/api/site`
- 后台接口前缀：`/api/admin`
- 认证接口前缀：`/api/auth`
- 时间字段格式：`YYYY-MM-DD HH:mm:ss`
- 资源字段：使用可直接访问的 URL 字符串
- 多图字段：`imageUrls` 使用 JSON 字符串数组存储，例如：`["/media/a.jpg","/media/b.jpg"]`

### 1.3 认证说明
除登录接口外，后台接口均需要携带 JWT：

```http
Authorization: Bearer <token>
```

### 1.4 通用响应结构
前端当前通过 `frontend/src/api/http.ts` 对 axios 响应做了解包，业务侧统一按以下结构取值：

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

说明：
- `code = 200` 表示成功
- `message` 为接口提示信息，成功时通常为 `success`
- `data` 为业务数据主体

---

## 2. 数据模型

### 2.1 ProfileData
用于首页、关于我、简历、留言页和后台资料管理共享的人物资料。

| 字段 | 类型 | 必填 | 说明 |
|---|---|---:|---|
| id | number | 否 | 主键 |
| siteTitle | string | 否 | 站点标题 |
| heroTitle | string | 否 | 首页主标题 / 个人定位 |
| heroSubtitle | string | 否 | 首页副标题 |
| heroConfig | string | 否 | 首页 Hero 区块配置 JSON |
| aboutContent | string | 否 | 关于我正文 |
| educationExperience | string | 否 | 教育经历文本 |
| workExperience | string | 否 | 工作经历文本 |
| email | string | 否 | 邮箱 |
| phone | string | 否 | 电话 |
| wechat | string | 否 | 微信 |
| githubUrl | string | 否 | GitHub 地址 |
| avatarUrl | string | 否 | 头像地址 |
| resumePdfUrl | string | 否 | PDF 简历地址 |
| guestbookIntro | string | 否 | 留言页介绍文案 |
| contactMethods | string | 否 | 联系方式配置 JSON |

### 2.2 ProjectData
用于简历页的项目经历与关于我页面的个人作品展示。

| 字段 | 类型 | 必填 | 说明 |
|---|---|---:|---|
| id | number | 是 | 主键 |
| name | string | 是 | 项目名称 |
| roleName | string | 否 | 角色名称 |
| description | string | 否 | 项目描述 |
| techStack | string | 否 | 技术栈 |
| projectLink | string | 否 | 项目链接 |
| coverImage | string | 否 | 项目封面图 |
| sortOrder | number | 否 | 排序值 |
| published | number | 否 | 是否发布，1=已发布，0=草稿 |
| updatedAt | string | 否 | 更新时间 |

### 2.3 HonorData
用于关于我页面中的荣誉时间线展示。

| 字段 | 类型 | 必填 | 说明 |
|---|---|---:|---|
| id | number | 是 | 主键 |
| title | string | 是 | 荣誉名称 |
| issuer | string | 否 | 授予机构 |
| description | string | 否 | 荣誉说明 |
| coverImage | string | 否 | 封面图 |
| imageUrls | string | 否 | 荣誉图集 JSON 字符串 |
| attachmentUrl | string | 否 | 证书附件 / PDF 地址 |
| awardAt | string | 否 | 获得时间 |
| sortOrder | number | 否 | 排序值 |
| published | number | 否 | 是否发布，1=已发布，0=草稿 |
| updatedAt | string | 否 | 更新时间 |

### 2.4 MomentData
用于动态页展示过程型记录。

| 字段 | 类型 | 必填 | 说明 |
|---|---|---:|---|
| id | number | 是 | 主键 |
| title | string | 是 | 动态标题 |
| content | string | 否 | 动态内容 |
| coverImage | string | 否 | 封面图 |
| imageUrls | string | 否 | 动态图集 JSON 字符串 |
| happenedAt | string | 否 | 发生时间 |
| sortOrder | number | 否 | 排序值 |
| published | number | 否 | 是否发布，1=已发布，0=草稿 |
| updatedAt | string | 否 | 更新时间 |

### 2.5 PostData
用于博客列表与详情。

| 字段 | 类型 | 必填 | 说明 |
|---|---|---:|---|
| id | number | 是 | 主键 |
| title | string | 是 | 标题 |
| summary | string | 否 | 摘要 |
| content | string | 否 | 正文 |
| coverImage | string | 否 | 封面图 |
| category | string | 否 | 分类 |
| tags | string | 否 | 标签 |
| published | number | 否 | 是否发布 |
| updatedAt | string | 否 | 更新时间 |
| viewCount | number | 否 | 当前文章浏览量 |

### 2.6 WorkData
用于关于我页面中的作品展示与后台作品管理。

| 字段 | 类型 | 必填 | 说明 |
|---|---|---:|---|
| id | number | 是 | 主键 |
| title | string | 是 | 作品名称 |
| summary | string | 否 | 作品说明 |
| coverImage | string | 否 | 封面图 |
| imageUrls | string | 否 | 图集 JSON 字符串 |
| videoUrl | string | 否 | 视频地址 |
| videoPoster | string | 否 | 视频封面 |
| workLink | string | 否 | 作品链接 |
| sortOrder | number | 否 | 排序值 |
| published | number | 否 | 是否发布 |
| updatedAt | string | 否 | 更新时间 |

### 2.7 MediaData
用于后台媒体库与前台资源地址复用。

| 字段 | 类型 | 必填 | 说明 |
|---|---|---:|---|
| id | number | 是 | 主键 |
| originalName | string | 否 | 原始文件名 |
| fileName | string | 否 | 存储文件名 |
| fileUrl | string | 否 | 访问地址 |
| mediaType | string | 否 | MIME 类型 |
| fileSize | number | 否 | 文件大小 |
| createdAt | string | 否 | 创建时间 |

### 2.8 FootprintData
用于首页足迹地图和后台足迹管理。

| 字段 | 类型 | 必填 | 说明 |
|---|---|---:|---|
| id | number | 否 | 主键 |
| cityName | string | 是 | 城市名，当前也兼容保存区县名 |
| countryName | string | 否 | 国家名，当前默认 `中国` |
| regionName | string | 否 | 省级行政区名称 |
| positionX | number | 否 | 地图横向保留字段 |
| positionY | number | 否 | 地图纵向保留字段 |
| visitedAt | string | 否 | 到访时间，格式 `YYYY-MM-DD HH:mm:ss` |
| description | string | 否 | 足迹说明 |
| importance | number | 否 | 高亮权重 |
| sortOrder | number | 否 | 排序值 |
| published | number | 否 | 是否发布，1=已发布，0=草稿 |
| createdAt | string | 否 | 创建时间 |
| updatedAt | string | 否 | 更新时间 |

### 2.9 HomeSystemConfigData
首页公开页面消费的系统配置子集。

| 字段 | 类型 | 必填 | 说明 |
|---|---|---:|---|
| footprintEyebrow | string | 否 | 足迹区眉标题 |
| footprintTitle | string | 否 | 足迹区主标题 |
| footprintSubtitle | string | 否 | 足迹区副标题 |
| footprintDescription | string | 否 | 足迹区说明文案 |
| copyrightText | string | 否 | 页脚版权文案 |
| techSupportText | string | 否 | 页脚技术支持文案 |

### 2.10 HomeData
首页聚合数据。

| 字段 | 类型 | 必填 | 说明 |
|---|---|---:|---|
| profile | ProfileData | 否 | 首页人物信息 |
| latestPosts | PostData[] | 否 | 最新博客 |
| latestMoments | MomentData[] | 否 | 最新动态 |
| featuredProjects | ProjectData[] | 否 | 精选项目 |
| latestHonors | HonorData[] | 否 | 最新荣誉 |
| footprints | FootprintData[] | 否 | 已发布足迹列表 |
| systemConfig | HomeSystemConfigData | 否 | 首页足迹与页脚文案配置 |

### 2.11 GuestbookMessageData
留言页与后台留言管理共用的留言数据。

| 字段 | 类型 | 必填 | 说明 |
|---|---|---:|---|
| id | number | 否 | 主键 |
| content | string | 否 | 留言正文 |
| published | number | 否 | 是否公开，1=公开，0=隐藏 |
| ipAddress | string | 否 | 提交 IP |
| createdAt | string | 否 | 创建时间 |
| updatedAt | string | 否 | 更新时间 |

### 2.12 GuestbookData
留言页聚合数据。

| 字段 | 类型 | 必填 | 说明 |
|---|---|---:|---|
| guestbookIntro | string | 否 | 留言页顶部介绍文案 |
| messages | GuestbookMessageData[] | 否 | 已公开留言列表 |

### 2.13 SystemConfigData
后台系统配置数据。

| 字段 | 类型 | 必填 | 说明 |
|---|---|---:|---|
| id | number | 否 | 主键 |
| storageType | string | 否 | 存储类型，当前支持 `local` / `oss` |
| ossEndpoint | string | 否 | OSS 接入端点 |
| ossRegion | string | 否 | OSS 区域 |
| ossBucketName | string | 否 | OSS Bucket 名称 |
| ossPublicUrlPrefix | string | 否 | OSS 公共访问前缀 |
| ossBaseDir | string | 否 | OSS 基础目录 |
| footprintEyebrow | string | 否 | 足迹区眉标题 |
| footprintTitle | string | 否 | 足迹区主标题 |
| footprintSubtitle | string | 否 | 足迹区副标题 |
| footprintDescription | string | 否 | 足迹区说明文案 |
| copyrightText | string | 否 | 页脚版权文案 |
| techSupportText | string | 否 | 页脚技术支持文案 |
| updatedAt | string | 否 | 更新时间 |

### 2.14 VisitTrendPoint
后台仪表盘访问趋势点。

| 字段 | 类型 | 说明 |
|---|---|---|
| date | string | 日期，格式 `YYYY-MM-DD` |
| label | string | 图表展示标签，格式 `MM-DD` |
| visitCount | number | 当日访问次数 |

### 2.15 DeviceTypeStat
后台仪表盘设备分布统计。

| 字段 | 类型 | 说明 |
|---|---|---|
| deviceType | string | 设备类型编码 |
| label | string | 设备类型中文名 |
| visitCount | number | 访问次数 |

### 2.16 PageVisitStat
后台仪表盘热门页面统计。

| 字段 | 类型 | 说明 |
|---|---|---|
| pageKey | string | 页面标识 |
| label | string | 页面中文名 |
| visitCount | number | 页面访问次数 |
| lastVisitAt | string | 最近访问时间 |

### 2.17 RecentVisitRecord
后台仪表盘最近访问记录。

| 字段 | 类型 | 说明 |
|---|---|---|
| id | number | 访问日志主键 |
| pageKey | string | 页面标识 |
| pageLabel | string | 页面中文名 |
| ipAddress | string | 访问 IP |
| userAgent | string | 原始 User-Agent |
| deviceType | string | 设备类型编码 |
| deviceTypeLabel | string | 设备类型中文名 |
| deviceName | string | 推导出的设备名称 |
| createdAt | string | 访问时间 |

---

## 3. 认证接口

### 3.1 管理员登录
**POST** `/api/auth/login`

#### 请求体
| 字段 | 类型 | 必填 | 说明 |
|---|---|---:|---|
| username | string | 是 | 用户名 |
| password | string | 是 | 密码 |

#### 请求示例
```json
{
  "username": "admin",
  "password": "123456"
}
```

#### 响应体 data
| 字段 | 类型 | 说明 |
|---|---|---|
| token | string | JWT |
| user.id | number | 用户 ID |
| user.username | string | 用户名 |
| user.displayName | string | 显示名称 |
| user.role | string | 角色 |

---

## 4. 前台接口

### 4.1 获取首页聚合数据
**GET** `/api/site/home`

#### 功能说明
用于首页展示人物主信息、精选项目、最新动态、最新荣誉和最新博客。

#### 响应示例
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "profile": {
      "siteTitle": "dyx-blog",
      "heroTitle": "全栈开发 / 内容创作 / 项目实践",
      "heroSubtitle": "欢迎来到我的个人品牌主页",
      "avatarUrl": "/media/avatar.jpg",
      "resumePdfUrl": "/media/resume.pdf",
      "email": "dyx@example.com"
    },
    "latestPosts": [],
    "latestMoments": [],
    "featuredProjects": [],
    "latestHonors": []
  }
}
```

### 4.2 获取关于我资料
**GET** `/api/site/profile`

#### 功能说明
用于“关于我”和“简历”页面获取人物基础信息，包括头像、联系方式与 PDF 简历地址。

#### 响应体
`data` 为 `ProfileData`

### 4.3 获取动态列表
**GET** `/api/site/moments`

#### 功能说明
用于动态页面获取已发布的动态列表。

#### 响应体
`data` 为 `MomentData[]`

### 4.4 获取项目经历列表
**GET** `/api/site/projects`

#### 功能说明
用于简历页面获取已发布的项目列表。

#### 响应体
`data` 为 `ProjectData[]`

### 4.5 获取作品列表
**GET** `/api/site/works`

#### 功能说明
用于“关于我”页面获取已发布的作品列表。

#### 响应体
`data` 为 `WorkData[]`

### 4.6 获取荣誉列表
**GET** `/api/site/honors`

#### 功能说明
用于关于我页面中的荣誉时间线展示，支持图片与证书附件链接。

#### 响应体
`data` 为 `HonorData[]`

#### 响应示例
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 502,
      "title": "Photoshop 图形图像处理专项职业能力",
      "issuer": "职业技能鉴定（指导）中心",
      "description": "2021-01-23 参加 Photoshop 图形图像处理专项职业能力考核，成绩 97.0。",
      "coverImage": "/media/20220323203851-0001.jpg",
      "imageUrls": "[\"/media/20220323203851-0001.jpg\"]",
      "attachmentUrl": "/media/丁益星(350784200310120035).pdf",
      "awardAt": "2021-05-11 10:00:00",
      "sortOrder": 2,
      "published": 1,
      "updatedAt": "2026-03-24 09:30:00"
    }
  ]
}
```

### 4.7 获取留言页数据
**GET** `/api/site/guestbook`

#### 功能说明
用于公开留言页拉取顶部介绍文案与已公开留言列表。

#### 响应体
`data` 为 `GuestbookData`

### 4.8 提交留言
**POST** `/api/site/guestbook/messages`

#### 功能说明
访客提交留言。服务端会结合请求头与代理头记录来源 IP，新留言默认是否公开由服务端保存逻辑决定。

#### 请求体
`Partial<GuestbookMessageData>`

#### 请求示例
```json
{
  "content": "你好，作品和博客都很喜欢。"
}
```

### 4.9 获取已发布足迹列表
**GET** `/api/site/footprints`

#### 功能说明
用于首页足迹地图获取已发布足迹点位与排序后的展示数据。

#### 响应体
`data` 为 `FootprintData[]`

### 4.10 记录页面访问
**POST** `/api/site/visit/{pageKey}`

#### 功能说明
用于前端页面挂载时显式上报访问，避免单页内多个接口请求被重复计数。服务端会同时维护：
- `dyx_site_visit_stat` 的累计访问量
- `dyx_site_visit_log` 的明细访问日志

#### 路径参数
| 字段 | 类型 | 必填 | 说明 |
|---|---|---:|---|
| pageKey | string | 是 | 页面标识，例如 `home`、`profile`、`blog-detail` |

#### 明细日志写入内容
- 访问 IP
- 原始 User-Agent
- 设备类型（`DESKTOP` / `MOBILE` / `TABLET` / `BOT` / `UNKNOWN`）
- 设备名称
- 访问时间

### 4.11 获取博客列表
**GET** `/api/posts`

#### 功能说明
用于博客列表页展示文章概览。

#### 响应体
`data` 为 `PostData[]`

### 4.12 获取博客详情
**GET** `/api/posts/{id}`

#### 路径参数
| 字段 | 类型 | 必填 | 说明 |
|---|---|---:|---|
| id | number | 是 | 文章主键 |

#### 功能说明
用于博客详情页获取文章正文，并在服务端累加 `dyx_post.view_count`。

#### 响应体
`data` 为 `PostData`

---

## 5. 后台接口

### 5.1 获取仪表盘摘要
**GET** `/api/admin/dashboard/summary`

#### 功能说明
用于后台仪表盘展示模块摘要统计、访问趋势、设备分布与热门页面，不再承载访问日志明细列表。

#### 响应体 data
| 字段 | 类型 | 说明 |
|---|---|---|
| postCount | number | 文章数量 |
| momentCount | number | 动态数量 |
| honorCount | number | 荣誉数量 |
| userCount | number | 用户数量 |
| totalPostViews | number | 博客总浏览量 |
| totalSiteVisits | number | 全站累计访问量 |
| dailySiteVisits | VisitTrendPoint[] | 最近 7 天访问趋势 |
| deviceTypeDistribution | DeviceTypeStat[] | 设备分布 |
| topVisitedPages | PageVisitStat[] | 热门访问页面排行 |

#### 响应示例
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "postCount": 3,
    "momentCount": 3,
    "honorCount": 3,
    "userCount": 1,
    "totalPostViews": 278,
    "totalSiteVisits": 42,
    "dailySiteVisits": [
      {
        "date": "2026-03-19",
        "label": "03-19",
        "visitCount": 3
      }
    ],
    "deviceTypeDistribution": [
      {
        "deviceType": "DESKTOP",
        "label": "桌面端",
        "visitCount": 18
      }
    ],
    "topVisitedPages": [
      {
        "pageKey": "blog",
        "label": "博客",
        "visitCount": 12,
        "lastVisitAt": "2026-03-25 21:18:00"
      }
    ]
  }
}
```

### 5.2 获取访问日志列表
**GET** `/api/admin/visit-logs`

#### 功能说明
用于后台独立“访问日志”模块按时间倒序查看访问明细；无筛选条件时默认返回最近 8 条日志，传入筛选条件时返回全部匹配结果。

#### 查询参数
| 字段 | 类型 | 必填 | 说明 |
|---|---|---:|---|
| startTime | string | 否 | 开始时间，格式 `YYYY-MM-DD HH:mm:ss` |
| endTime | string | 否 | 结束时间，格式 `YYYY-MM-DD HH:mm:ss` |
| pageKey | string | 否 | 页面标识，支持 `home` / `profile` / `resume` / `moments` / `blog` / `blog-detail` |
| deviceType | string | 否 | 设备类型，支持 `DESKTOP` / `MOBILE` / `TABLET` / `BOT` / `UNKNOWN` |
| ipAddress | string | 否 | 访问 IP 精确筛选 |

#### 响应体
`data` 为 `RecentVisitRecord[]`

### 5.3 删除单条访问日志
**DELETE** `/api/admin/visit-logs/{id}`

#### 路径参数
| 字段 | 类型 | 必填 | 说明 |
|---|---|---:|---|
| id | number | 是 | 访问日志主键 |

### 5.4 批量删除访问日志
**POST** `/api/admin/visit-logs/batch-delete`

#### 请求体
`number[]`

#### 功能说明
用于后台访问日志页面批量删除选中的日志记录。

### 5.5 获取后台文章列表
**GET** `/api/admin/posts`

#### 响应体
`data` 为 `PostData[]`

### 5.4 新增文章
**POST** `/api/admin/posts`

#### 请求体
`Partial<PostData>`

### 5.5 更新文章
**PUT** `/api/admin/posts/{id}`

#### 请求体
`Partial<PostData>`

### 5.6 删除文章
**DELETE** `/api/admin/posts/{id}`

### 5.7 获取动态列表
**GET** `/api/admin/moments`

#### 响应体
`data` 为 `MomentData[]`

### 5.8 新增动态
**POST** `/api/admin/moments`

#### 请求体
`Partial<MomentData>`

### 5.9 更新动态
**PUT** `/api/admin/moments/{id}`

#### 请求体
`Partial<MomentData>`

### 5.10 删除动态
**DELETE** `/api/admin/moments/{id}`

### 5.11 获取项目经历列表
**GET** `/api/admin/projects`

#### 响应体
`data` 为 `ProjectData[]`

### 5.12 新增项目经历
**POST** `/api/admin/projects`

#### 请求体
`Partial<ProjectData>`

### 5.13 更新项目经历
**PUT** `/api/admin/projects/{id}`

#### 请求体
`Partial<ProjectData>`

### 5.14 删除项目经历
**DELETE** `/api/admin/projects/{id}`

### 5.15 获取作品列表
**GET** `/api/admin/works`

#### 响应体
`data` 为 `WorkData[]`

### 5.16 新增作品
**POST** `/api/admin/works`

#### 请求体
`Partial<WorkData>`

### 5.17 更新作品
**PUT** `/api/admin/works/{id}`

#### 请求体
`Partial<WorkData>`

### 5.18 删除作品
**DELETE** `/api/admin/works/{id}`

### 5.19 获取荣誉列表
**GET** `/api/admin/honors`

#### 响应体
`data` 为 `HonorData[]`

### 5.20 新增荣誉
**POST** `/api/admin/honors`

#### 请求体字段
| 字段 | 类型 | 必填 | 说明 |
|---|---|---:|---|
| title | string | 是 | 荣誉名称 |
| issuer | string | 否 | 授予机构 |
| description | string | 否 | 荣誉说明 |
| coverImage | string | 否 | 封面图 |
| imageUrls | string | 否 | 图片数组 JSON 字符串 |
| attachmentUrl | string | 否 | 证书附件 / PDF 地址 |
| awardAt | string | 否 | 获得时间 |
| sortOrder | number | 否 | 排序值 |
| published | number | 否 | 发布状态 |

### 5.21 更新荣誉
**PUT** `/api/admin/honors/{id}`

#### 请求体
`Partial<HonorData>`

### 5.22 删除荣誉
**DELETE** `/api/admin/honors/{id}`

### 5.23 获取后台留言管理数据
**GET** `/api/admin/guestbook`

#### 功能说明
用于后台留言管理页获取留言介绍文案与全部留言列表。

#### 响应体
`data` 为 `GuestbookData`

### 5.24 更新留言页介绍
**PUT** `/api/admin/guestbook/intro`

#### 请求体字段
| 字段 | 类型 | 必填 | 说明 |
|---|---|---:|---|
| guestbookIntro | string | 否 | 留言页顶部介绍文案 |

### 5.25 更新单条留言
**PUT** `/api/admin/guestbook/messages/{id}`

#### 路径参数
| 字段 | 类型 | 必填 | 说明 |
|---|---|---:|---|
| id | number | 是 | 留言主键 |

#### 请求体
`Partial<GuestbookMessageData>`

### 5.26 删除单条留言
**DELETE** `/api/admin/guestbook/messages/{id}`

### 5.27 获取足迹列表
**GET** `/api/admin/footprints`

#### 功能说明
用于后台足迹管理页获取全部足迹记录。

#### 响应体
`data` 为 `FootprintData[]`

### 5.28 新增足迹
**POST** `/api/admin/footprints`

#### 请求体
`Partial<FootprintData>`

#### 说明
- `visitedAt` 使用 `YYYY-MM-DD HH:mm:ss` 字符串格式
- 当前管理端选择器已支持完整省 / 市 / 区县联动，但保存模型仍以 `regionName` + `cityName` 为主

### 5.29 更新足迹
**PUT** `/api/admin/footprints/{id}`

#### 请求体
`Partial<FootprintData>`

### 5.30 删除足迹
**DELETE** `/api/admin/footprints/{id}`

### 5.31 获取后台个人资料
**GET** `/api/admin/profile`

#### 功能说明
后台获取人物资料，供“关于我管理”和“简历管理”共享。

#### 响应体
`data` 为 `ProfileData`

### 5.32 更新后台个人资料
**PUT** `/api/admin/profile`

#### 请求体
`ProfileData`

#### 功能说明
更新站点标题、关于我、联系方式、教育经历、工作经历、头像与 PDF 简历地址等共享资料。

### 5.33 获取系统配置
**GET** `/api/admin/system-config`

#### 功能说明
用于后台读取站点系统配置，包括存储配置、首页足迹文案和页脚文案。

#### 响应体
`data` 为 `SystemConfigData`

### 5.34 更新系统配置
**PUT** `/api/admin/system-config`

#### 请求体
`SystemConfigData`

### 5.35 获取媒体资源列表
**GET** `/api/admin/media`

#### 功能说明
提供所有图片与文件资源，供关于我、动态、荣誉、简历、项目等模块选择使用。

#### 响应体 data
| 字段 | 类型 | 说明 |
|---|---|---|
| id | number | 主键 |
| originalName | string | 原始文件名 |
| fileName | string | 存储文件名 |
| fileUrl | string | 访问地址 |
| mediaType | string | 媒体类型 |
| fileSize | number | 文件大小 |
| createdAt | string | 创建时间 |

### 5.36 上传媒体资源
**POST** `/api/admin/media/upload`

#### 请求类型
`multipart/form-data`

#### 表单字段
| 字段 | 类型 | 必填 | 说明 |
|---|---|---:|---|
| file | file | 是 | 上传文件 |

### 5.37 删除媒体资源
**DELETE** `/api/admin/media/{id}`

#### 功能说明
删除媒体库中的单个资源。删除前会校验该资源是否仍被个人资料、文章、项目、作品、动态或荣誉字段引用；若仍被引用，则返回失败提示并拒绝删除，避免前台出现坏链。

#### 路径参数
| 字段 | 类型 | 必填 | 说明 |
|---|---|---:|---|
| id | number | 是 | 媒体主键 |

### 5.38 导入 uploads 已有文件
**POST** `/api/admin/media/import-existing`

#### 功能说明
扫描 `backend/uploads` 目录，将图片、PDF 等已存在文件导入 `dyx_media`，避免后台重复手工上传。

#### 响应体
`data` 为 `number`，表示本次成功导入的文件数量。

### 5.39 获取用户列表
**GET** `/api/admin/users`

#### 功能说明
用于后台用户管理页获取用户列表。

#### 响应体
`data` 为 `AdminListUserData[]`

### 5.40 新增用户
**POST** `/api/admin/users`

#### 请求体
`Partial<AdminListUserData>`

### 5.41 更新用户
**PUT** `/api/admin/users/{id}`

#### 请求体
`Partial<AdminListUserData>`

### 5.42 删除用户
**DELETE** `/api/admin/users/{id}`

---

## 6. 常见错误码建议

| code | 含义 | 说明 |
|---|---|---|
| 200 | 成功 | 请求成功 |
| 400 | 请求参数错误 | 字段缺失、格式不正确，或媒体删除时仍存在业务引用 |
| 401 | 未登录或 token 失效 | 需要重新登录 |
| 403 | 无权限 | 当前用户无权访问该接口 |
| 404 | 资源不存在 | 主键对应的数据不存在 |
| 500 | 服务器内部错误 | 服务端执行异常 |

---

## 7. 文档变更说明

### 2026-03-29
- 为 `ProfileData` 补充 `heroConfig`、`guestbookIntro`、`contactMethods` 字段说明
- 新增 `FootprintData`、`HomeSystemConfigData`、`GuestbookMessageData`、`GuestbookData`、`SystemConfigData` 数据模型说明
- 补齐前台留言页接口：`/api/site/guestbook`、`/api/site/guestbook/messages`
- 补齐前台足迹接口：`/api/site/footprints`
- 补齐后台留言管理接口：`/api/admin/guestbook`、`/api/admin/guestbook/intro`、`/api/admin/guestbook/messages/{id}`
- 补齐后台足迹 CRUD 接口：`/api/admin/footprints`
- 补齐后台系统配置接口：`/api/admin/system-config`
- 同步足迹时间字段格式为 `YYYY-MM-DD HH:mm:ss`

### 2026-03-25
- 修正访问日志设计，明确服务端会优先解析 `X-Forwarded-For` / `X-Real-IP`，并将 `::1`、`0:0:0:0:0:0:0:1`、`::ffff:127.0.0.1` 统一归一化为 `127.0.0.1`
- 说明真实访客 IP 依赖反向代理 / 网关正确透传 `X-Forwarded-For` 或 `X-Real-IP`，本地直连开发环境通常只能记录 `127.0.0.1` 或局域网地址
- 调整后台仪表盘摘要说明，移除 `recentVisits` 字段，仅保留概览型访问分析数据
- 新增 `/api/admin/visit-logs` 独立访问日志接口说明
- 补充后台访问日志单条删除与批量删除接口说明
- 为访问日志返回模型补充 `id` 字段，供后台精确删除记录
- 补充 `/api/admin/visit-logs` 的 `startTime`、`endTime`、`pageKey`、`deviceType`、`ipAddress` 可选筛选参数说明
- 补充 `/api/site/works` 作品接口说明
- 补充 `/api/site/visit/{pageKey}` 页面访问上报接口说明
- 明确站点访问会同时维护累计统计与访问日志明细
- 扩展后台仪表盘摘要结构，补入趋势图、设备分布、热门页面和最近访问记录字段
- 补充 `VisitTrendPoint`、`DeviceTypeStat`、`PageVisitStat`、`RecentVisitRecord` 数据模型说明
- 补齐后台作品 CRUD 与用户 CRUD 接口说明

### 2026-03-24
- 明确前台模块调整为：首页、关于我、简历、动态、博客
- 明确“关于我”页面聚合个人信息、个人作品与荣誉时间线
- 删除独立照片模块相关接口说明，资源能力统一由媒体库支撑
- 为 `ProfileData` 补充 `resumePdfUrl`
- 为 `HonorData` 补充 `attachmentUrl`
- 将成功响应示例同步为后端当前实际返回：`code = 200`、`message = success`
- 补齐 `/api/admin/media/{id}` 删除接口说明，并补充媒体引用校验约束
- 补齐 `/api/admin/media/import-existing` 接口说明
- 补齐简历 PDF 与证书附件的管理说明
