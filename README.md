# dyx-blog

一个现代化的个人品牌站点，集个人主页、博客内容、动态分享与后台管理于一体的全栈项目。

![Vue 3](https://img.shields.io/badge/Vue-3.5.13-42b883?logo=vue.js)
![TypeScript](https://img.shields.io/badge/TypeScript-5.8.3-3178c6?logo=typescript)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.1-6db33f?logo=spring-boot)
![Java](https://img.shields.io/badge/Java-17-orange?logo=openjdk)

## 项目特色

- **前台展示**：首页、关于我、简历、博客、动态、留言
- **后台管理**：内容管理、媒体资源、访问统计、系统配置
- **响应式设计**：适配桌面端与移动端
- **主题切换**：支持深浅色主题切换
- **安全加固**：JWT 认证、角色权限控制、XSS 防护、敏感信息加密
- **富文本编辑**：支持博客富文本内容编辑与展示
- **图片放大预览**：支持图片点击放大查看
- **足迹地图**：首页第二屏足迹地图展示

## 技术栈

### 前端
- **框架**：Vue 3 + TypeScript + Pinia + Vue Router
- **UI**：Element Plus + Tailwind CSS
- **图表**：ECharts
- **构建**：Vite
- **富文本**：VueQuill
- **图片裁剪**：vue-cropper

### 后端
- **框架**：Spring Boot 3.3.1
- **ORM**：MyBatis-Plus
- **数据库**：MySQL 8.0+
- **认证**：JWT (jjwt 0.12.6)
- **安全**：BCrypt 密码加密、Jsoup XSS 清洗
- **存储**：本地存储 / 阿里云 OSS

## 快速开始

### 环境要求
- JDK 17
- Maven 3.9+
- Node.js 18+
- npm 9+
- MySQL 8.0+

### 1. 数据库初始化

```bash
# 创建数据库
CREATE DATABASE dyx_blog DEFAULT CHARACTER SET utf8mb4;

# 导入初始化脚本
mysql -u root -p dyx_blog < backend/src/main/resources/sql/dyx-blog-init.sql
```

### 2. 启动后端

```bash
cd backend

# 配置环境变量（示例）
export MYSQL_HOST=localhost
export MYSQL_USER=root
export MYSQL_PWD=your_password
export JWT_SECRET=your_secret
export DYX_ENCRYPT_KEY=your_32_char_key

# 运行
mvn spring-boot:run

# 或打包后运行
mvn clean package
java -jar target/dyx-blog-1.0.0.jar
```

### 3. 启动前端

```bash
cd frontend

# 安装依赖
npm install

# 开发模式
npm run dev

# 生产构建
npm run build
```

前端环境变量说明：
- `frontend/.env.local`：本地开发使用的高德地图 Key、安全密钥与代理目标
- `frontend/.env.production`：生产构建使用的高德地图 Key、安全密钥与代理目标
- 仓库中的 `.env` 文件仅保留占位值，构建前请替换为你自己的配置

## 访问入口

### 前台页面
| 页面 | 路由 |
|------|------|
| 首页 | `/` |
| 关于我 | `/about` |
| 简历 | `/resume` |
| 动态 | `/moments` |
| 博客列表 | `/blog` |
| 留言 | `/guestbook` |

### 后台管理
- 登录页：`/dyx-manager/login`
- 管理后台：`/dyx-manager`

## 目录结构

```
dyx-blog/
├── backend/           # Spring Boot 后端
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   ├── resources/
│   │   │   │   ├── sql/          # 数据库初始化脚本
│   │   │   │   └── application.yml
│   │   │   └── test/
│   │   └── test/
│   └── pom.xml
├── frontend/          # Vue 3 前端
│   ├── src/
│   │   ├── api/        # API 模块
│   │   ├── assets/     # 静态资源
│   │   ├── components/ # 公共组件
│   │   ├── constants/  # 常量配置
│   │   ├── router/     # 路由配置
│   │   ├── stores/     # Pinia 状态管理
│   │   ├── types/      # TypeScript 类型
│   │   ├── utils/      # 工具函数
│   │   ├── views/      # 页面组件
│   │   │   ├── admin/    # 后台页面
│   │   │   └── web/      # 前台页面
│   │   ├── App.vue
│   │   └── main.ts
│   ├── public/
│   └── package.json
└── docs/              # 项目文档
```

## 核心功能

### 前台功能
- **首页**：Hero 首屏、足迹地图、第三屏精选内容，手机端支持左右滑动浏览精选卡片
- **关于我**：个人资料、作品展示、荣誉时间线
- **简历**：教育/工作/项目经历、PDF 下载、打印支持，长项目描述支持展开/收起且项目链接常驻可见
- **博客**：文章列表、详情阅读、富文本展示
- **动态**：时间流式内容展示与详情查看
- **留言**：访客留言互动

### 后台功能
- **仪表盘**：数据统计、访问趋势、设备分布
- **访问日志**：页面访问记录、筛选查询、批量管理
- **内容管理**：博客、动态、荣誉、作品、留言
- **媒体资源**：文件上传、导入、预览、安全删除
- **首页配置**：Hero 配置、足迹管理、第三屏聚合规则
- **用户管理**：用户 CRUD、角色权限管理

## SEO 优化

项目已内置 SEO 优化：
- `robots.txt`：声明公开页面抓取策略
- `sitemap.xml`：站点地图收录
- 动态 meta 标签：页面标题、描述、Open Graph、Twitter 标签
- 结构化数据：BlogPosting Schema

## 安全特性

| 特性 | 说明 |
|------|------|
| JWT 认证 | Token 鉴权，支持后台登录态 |
| 角色权限 | 后台登录仅允许 ADMIN 角色，系统配置 / 用户管理 / 访问日志删除需超级管理员权限 |
| XSS 防护 | Jsoup 白名单清洗 |
| 密码加密 | BCrypt 强哈希存储 |
| 敏感信息加密 | AES 加密存储 OSS 配置等 |
| 安全头部 | CSP、HSTS、X-Frame-Options 等 |

## 许可证

MIT License

---

**最后更新**: 2026-04-09
