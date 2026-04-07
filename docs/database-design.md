# dyx-blog 数据库设计文档

## 1. 用户表 `dyx_user`
- `id` bigint 主键
- `username` varchar(64) 用户名
- `password` varchar(255) 密码
- `display_name` varchar(128) 显示名称
- `role` varchar(32) 角色
- `enabled` tinyint 是否启用
- `created_at` datetime 创建时间
- `updated_at` datetime 更新时间

## 2. 文章表 `dyx_post`
- `id` bigint 主键
- `title` varchar(200) 标题
- `summary` varchar(500) 摘要
- `content` longtext 内容，当前用于保存经过白名单清洗的博客 HTML 正文
- `cover_image` varchar(255) 封面图
- `category` varchar(100) 分类
- `tags` varchar(255) 标签
- `published` tinyint 是否发布
- `published_at` datetime 业务发布日期，用于前台展示与排序
- `view_count` int 浏览量
- `created_at` datetime 创建时间
- `updated_at` datetime 更新时间

## 3. 动态表 `dyx_moment`
- `id` bigint 主键
- `title` varchar(200) 标题
- `content` text 内容
- `cover_image` varchar(255) 封面图
- `image_urls` text 多图地址 JSON 字符串
- `happened_at` datetime 发生时间
- `sort_order` int 排序
- `published` tinyint 是否发布
- `created_at` datetime 创建时间
- `updated_at` datetime 更新时间

## 4. 项目经历表 `dyx_project`
- `id` bigint 主键
- `name` varchar(200) 项目名称
- `role_name` varchar(100) 角色名称
- `description` text 项目说明
- `tech_stack` varchar(255) 技术栈
- `project_link` varchar(255) 项目链接
- `cover_image` varchar(255) 封面图
- `sort_order` int 排序
- `published` tinyint 是否发布
- `created_at` datetime 创建时间
- `updated_at` datetime 更新时间

## 5. 个人作品表 `dyx_work`
- `id` bigint 主键
- `title` varchar(200) 作品名称
- `summary` text 作品说明
- `cover_image` varchar(255) 封面图
- `image_urls` text 图集地址 JSON 字符串
- `video_url` varchar(255) 视频地址
- `video_poster` varchar(255) 视频封面图
- `work_link` varchar(255) 作品链接
- `award_at` datetime 获得时间
- `sort_order` int 排序
- `published` tinyint 是否发布
- `created_at` datetime 创建时间
- `updated_at` datetime 更新时间

## 6. 荣誉表 `dyx_honor`
- `id` bigint 主键
- `title` varchar(200) 荣誉名称
- `issuer` varchar(200) 授予机构
- `description` text 荣誉说明
- `cover_image` varchar(255) 封面图
- `image_urls` text 多图地址 JSON 字符串
- `attachment_url` varchar(255) 荣誉附件 / 证书 PDF 地址
- `award_at` datetime 获得时间
- `sort_order` int 排序
- `published` tinyint 是否发布
- `created_at` datetime 创建时间
- `updated_at` datetime 更新时间

## 7. 个人资料表 `dyx_profile`
- `id` bigint 主键
- `site_title` varchar(200) 站点标题
- `hero_title` varchar(200) 首页主标题
- `hero_subtitle` varchar(500) 首页副标题
- `hero_config` text 首页 Hero 区块配置 JSON
- `about_content` text 关于我
- `education_experience` text 教育经历
- `work_experience` text 工作经历
- `email` varchar(128) 邮箱
- `phone` varchar(64) 电话
- `wechat` varchar(64) 微信
- `github_url` varchar(255) GitHub 地址
- `avatar_url` varchar(255) 头像地址
- `resume_pdf_url` varchar(255) PDF 简历地址
- `guestbook_intro` text 留言页顶部介绍文案
- `contact_methods` text 联系方式配置 JSON
- `updated_at` datetime 更新时间

## 8. 留言表 `dyx_guestbook_message`
- `id` bigint 主键 / 自增
- `content` text 留言正文
- `published` tinyint 是否公开
- `ip_address` varchar(45) 提交 IP
- `created_at` datetime 创建时间
- `updated_at` datetime 更新时间

## 9. 媒体资源表 `dyx_media`
- `id` bigint 主键
- `original_name` varchar(255) 原始文件名
- `file_name` varchar(255) 存储文件名
- `file_url` varchar(255) 访问地址
- `media_type` varchar(64) 媒体类型
- `file_size` bigint 文件大小
- `created_at` datetime 创建时间

## 10. 站点访问统计表 `dyx_site_visit_stat`
- `page_key` varchar(64) 页面标识主键
- `visit_count` bigint 累计访问次数
- `updated_at` datetime 最近更新时间

## 11. 站点访问日志表 `dyx_site_visit_log`
- `id` bigint 自增主键
- `page_key` varchar(64) 页面标识
- `ip_address` varchar(45) 访问 IP
- `user_agent` varchar(512) 原始访问端 User-Agent
- `device_type` varchar(32) 设备类型，当前支持 `DESKTOP` / `MOBILE` / `TABLET` / `BOT` / `UNKNOWN`
- `device_name` varchar(128) 推导出的设备名称，例如 `iPhone`、`Windows`、`Android`
- `created_at` datetime 访问发生时间

## 12. 足迹表 `dyx_footprint`
- `id` bigint 主键
- `city_name` varchar(100) 城市名或当前保存的区县名
- `country_name` varchar(100) 国家名
- `region_name` varchar(100) 省级行政区名称
- `position_x` decimal(5,2) 地图横向保留字段
- `position_y` decimal(5,2) 地图纵向保留字段
- `visited_at` datetime 到访时间
- `description` text 足迹说明
- `importance` int 高亮权重
- `sort_order` int 排序值
- `published` tinyint 是否发布
- `created_at` datetime 创建时间
- `updated_at` datetime 更新时间

## 13. 系统配置表 `dyx_system_config`
- `id` bigint 主键
- `storage_type` varchar(32) 存储类型，支持 `local` / `oss`
- `oss_endpoint` varchar(255) OSS 接入端点
- `oss_region` varchar(100) OSS 区域
- `oss_bucket_name` varchar(255) OSS Bucket
- `oss_public_url_prefix` varchar(255) OSS 公共访问前缀
- `oss_base_dir` varchar(255) OSS 基础目录
- `footprint_eyebrow` varchar(120) 首页足迹眉标题
- `footprint_title` varchar(200) 首页足迹主标题
- `footprint_subtitle` varchar(255) 首页足迹副标题
- `footprint_description` varchar(500) 首页足迹说明文案
- `copyright_text` varchar(255) 页脚版权文案
- `tech_support_text` varchar(255) 页脚技术支持文案
- `updated_at` datetime 更新时间

## 14. 当前内容结构说明
- 前台主模块为：首页、关于我、简历、动态、博客
- “关于我”页面同时消费 `dyx_profile`、`dyx_work`、`dyx_honor`，展示个人资料、个人作品与横向荣誉时间线
- 简历页通过 `dyx_profile.resume_pdf_url` 提供 PDF 下载入口，并结合 `dyx_project` 展示项目经历
- 荣誉与动态均支持多图扩展，因此保留 `image_urls` 字段
- 荣誉的图片展示与证书下载分离处理，附件通过 `attachment_url` 保存
- 文章详情会累加 `dyx_post.view_count`，后台仪表盘聚合展示博客总浏览量
- 博客列表与详情页优先使用 `dyx_post.published_at` 作为业务发布日期展示；若历史数据缺失则回退到 `updated_at`
- 页面访问量通过 `dyx_site_visit_stat` 按页面 key 累计，当前覆盖 `home`、`profile`、`resume`、`moments`、`blog`、`blog-detail`
- 页面访问明细通过 `dyx_site_visit_log` 保留，可用于设备分布、热门页面与独立访问日志模块展示
- 仪表盘仅展示访问概览型统计，访问日志明细独立放在后台“访问日志”模块中
- 资源能力由 `dyx_media` 提供统一支持，不再保留独立照片模块
- `dyx_media` 既可记录后台上传文件，也可记录 `backend/uploads` 中导入的已有文件

## 15. 媒体引用与删除约束
当前媒体资源不是通过外键关联业务表，而是以 URL 字符串形式直接保存到各业务字段中，因此删除媒体时不能只删 `dyx_media` 记录，还必须先做业务引用校验。

### 15.1 当前引用字段
- `dyx_profile.avatar_url`
- `dyx_profile.resume_pdf_url`
- `dyx_post.cover_image`
- `dyx_project.cover_image`
- `dyx_work.cover_image`
- `dyx_work.image_urls`
- `dyx_work.video_poster`
- `dyx_moment.cover_image`
- `dyx_moment.image_urls`
- `dyx_honor.cover_image`
- `dyx_honor.image_urls`
- `dyx_honor.attachment_url`

### 15.2 删除规则
- 后台删除媒体时，先根据 `dyx_media.file_url` 扫描上述业务字段
- 若仍被个人资料、文章、项目、作品、动态或荣誉引用，则拒绝删除
- 仅当未被任何业务字段引用时，才允许同时删除物理文件与 `dyx_media` 记录
- 当前不做级联清空业务字段，也不提供强制删除模式，以避免前台图片或附件出现坏链

## 16. 旧库兼容说明
- 应用启动时会自动补齐历史库缺失的 `dyx_profile.resume_pdf_url` 字段
- 初始化 SQL 已包含 `dyx_post.published_at` 字段定义；旧库升级时需手动补齐该字段，并为历史文章按 `updated_at / created_at` 回填业务发布日期
- 应用启动时会自动补齐历史库缺失的 `dyx_profile.guestbook_intro` 与 `dyx_profile.contact_methods` 字段
- 应用启动时会自动确保 `dyx_work`、`dyx_site_visit_stat`、`dyx_site_visit_log`、`dyx_guestbook_message`、`dyx_footprint` 与 `dyx_system_config` 表存在
- 若旧库中的 `dyx_site_visit_log` 尚未包含 `device_name` 字段，启动时会自动补齐
- 访问日志写入时会优先从 `X-Forwarded-For`、`X-Real-IP` 解析客户端 IP，并将 `::1`、`0:0:0:0:0:0:0:1`、`::ffff:127.0.0.1` 归一化为 `127.0.0.1`
- 后端应用默认时区统一为 `Asia/Shanghai`，访问日志 `created_at` 与接口返回时间均按北京时间处理，避免服务器默认时区差异导致时间偏移
- 初始化 SQL 会补入站点访问统计初始记录、访问日志示例记录、留言示例、足迹示例与系统配置默认记录，便于后台模块直接展示数据

## 17. 当前接口与前端交互约定
- 表中主键仍以 `bigint` 存储，但面向前端返回时，用户、荣誉、作品等 Long 主键会按字符串安全序列化，避免 JavaScript `number` 精度丢失
- `dyx_user.role` 当前区分 `ADMIN` 与 `USER`，其中后台登录与用户管理仅允许 `ADMIN` 使用
- 访问日志的删除能力受角色限制，仅超级管理员可执行删除与批量删除
- 媒体资源在数据库中只保存原始文件记录，头像、首页背景图、首页人物图等业务裁剪结果仍以业务字段中的 URL 字符串形式引用

## 18. 后续多租户扩展规划（暂不实现）

为支持未来“一个平台托管多个个人博客站点”的多租户能力，数据库层面补充以下规划说明：
- 新增站点表（暂定命名为 `dyx_site`），用于记录个人站点 / 博客的元信息：
  - `id` bigint 主键
  - `owner_user_id` bigint 站点拥有者用户 ID（关联 `dyx_user.id`）
  - `site_slug` varchar(64) 站点路径标识，例如 `dyx`、`alice`，用于 `/u/{siteSlug}` 路由
  - `site_title` varchar(200) 站点标题（可与 `dyx_profile.site_title` 配合使用或后续迁移）
  - `enabled` tinyint 是否启用
  - `created_at` datetime 创建时间
  - `updated_at` datetime 更新时间
- 现有业务表在保持当前实现不变的前提下，后续版本规划通过补充 `site_id` 字段实现数据按站点隔离：
  - `dyx_profile.site_id`
  - `dyx_post.site_id`
  - `dyx_moment.site_id`
  - `dyx_project.site_id`
  - `dyx_work.site_id`
  - `dyx_honor.site_id`
  - `dyx_guestbook_message.site_id`
  - `dyx_footprint.site_id`
  - `dyx_media.site_id`
  - `dyx_site_visit_stat.site_id`
  - `dyx_site_visit_log.site_id`
  - `dyx_system_config.site_id`
- 现有 `dyx_user` 表仍作为平台级账号表，后续可按需扩展 `role` 语义：
  - 平台级角色示例：`PLATFORM_ADMIN`（平台管理员）、`PLATFORM_USER`（普通用户）
  - 站点级角色示例：`SITE_OWNER`（站点拥有者）、`SITE_EDITOR`（协同编辑，按站点关联）
- 上述规划仅作为多租户改造的预留设计，当前版本暂不进行表结构调整与数据迁移
