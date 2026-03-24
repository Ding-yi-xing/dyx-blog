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
- `content` longtext 内容
- `cover_image` varchar(255) 封面图
- `category` varchar(100) 分类
- `tags` varchar(255) 标签
- `published` tinyint 是否发布
- `view_count` int 浏览量
- `created_at` datetime 创建时间
- `updated_at` datetime 更新时间

## 3. 动态表 `dyx_moment`
- `id` bigint 主键
- `title` varchar(200) 标题
- `content` text 内容
- `cover_image` varchar(255) 封面图
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

## 5. 照片表 `dyx_photo`
- `id` bigint 主键
- `title` varchar(200) 标题
- `image_url` varchar(255) 图片地址
- `description` varchar(500) 描述
- `shot_at` datetime 拍摄时间
- `sort_order` int 排序
- `published` tinyint 是否发布
- `created_at` datetime 创建时间
- `updated_at` datetime 更新时间

## 6. 个人资料表 `dyx_profile`
- `id` bigint 主键
- `site_title` varchar(200) 站点标题
- `hero_title` varchar(200) 首页主标题
- `hero_subtitle` varchar(500) 首页副标题
- `about_content` text 关于我
- `education_experience` text 教育经历
- `work_experience` text 工作经历
- `email` varchar(128) 邮箱
- `phone` varchar(64) 电话
- `wechat` varchar(64) 微信
- `github_url` varchar(255) GitHub 地址
- `avatar_url` varchar(255) 头像地址
- `updated_at` datetime 更新时间

## 7. 媒体资源表 `dyx_media`
- `id` bigint 主键
- `original_name` varchar(255) 原始文件名
- `file_name` varchar(255) 存储文件名
- `file_url` varchar(255) 访问地址
- `media_type` varchar(64) 媒体类型
- `file_size` bigint 文件大小
- `created_at` datetime 创建时间
