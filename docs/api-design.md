# dyx-blog 接口设计文档

## 1. 认证接口
### POST `/api/auth/login`
- 功能：管理员登录
- 请求体：`username`、`password`
- 返回：JWT、用户信息

## 2. 前台接口
### GET `/api/site/home`
- 功能：获取首页聚合数据

### GET `/api/site/profile`
- 功能：获取个人资料

### GET `/api/site/moments`
- 功能：获取动态列表

### GET `/api/site/projects`
- 功能：获取项目经历列表

### GET `/api/site/photos`
- 功能：获取照片列表

### GET `/api/posts`
- 功能：获取博客列表

### GET `/api/posts/{id}`
- 功能：获取博客详情

## 3. 后台接口
### GET `/api/admin/dashboard/summary`
- 功能：获取后台仪表盘摘要

### GET `/api/admin/posts`
- 功能：获取文章列表

### POST `/api/admin/posts`
- 功能：新增文章

### PUT `/api/admin/posts/{id}`
- 功能：修改文章

### DELETE `/api/admin/posts/{id}`
- 功能：删除文章

### GET `/api/admin/profile`
- 功能：获取个人资料

### PUT `/api/admin/profile`
- 功能：更新个人资料

### GET `/api/admin/moments`
- 功能：获取动态列表

### POST `/api/admin/moments`
- 功能：新增动态

### PUT `/api/admin/moments/{id}`
- 功能：修改动态

### DELETE `/api/admin/moments/{id}`
- 功能：删除动态

### GET `/api/admin/projects`
- 功能：获取项目经历列表

### POST `/api/admin/projects`
- 功能：新增项目经历

### PUT `/api/admin/projects/{id}`
- 功能：修改项目经历

### DELETE `/api/admin/projects/{id}`
- 功能：删除项目经历

### GET `/api/admin/photos`
- 功能：获取照片列表

### POST `/api/admin/photos`
- 功能：新增照片

### PUT `/api/admin/photos/{id}`
- 功能：修改照片

### DELETE `/api/admin/photos/{id}`
- 功能：删除照片

### GET `/api/admin/media`
- 功能：获取媒体资源列表

### POST `/api/admin/media/upload`
- 功能：上传媒体资源

### GET `/api/admin/users`
- 功能：获取用户列表
