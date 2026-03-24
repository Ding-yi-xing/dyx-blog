CREATE DATABASE IF NOT EXISTS dyx_blog DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE dyx_blog;

CREATE TABLE IF NOT EXISTS dyx_user (
    id BIGINT PRIMARY KEY,
    username VARCHAR(64) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    display_name VARCHAR(128) NOT NULL,
    role VARCHAR(32) NOT NULL,
    enabled TINYINT NOT NULL DEFAULT 1,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS dyx_post (
    id BIGINT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    summary VARCHAR(500),
    content LONGTEXT,
    cover_image VARCHAR(255),
    category VARCHAR(100),
    tags VARCHAR(255),
    published TINYINT NOT NULL DEFAULT 0,
    view_count INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS dyx_moment (
    id BIGINT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT,
    cover_image VARCHAR(255),
    happened_at DATETIME,
    sort_order INT NOT NULL DEFAULT 0,
    published TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS dyx_project (
    id BIGINT PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    role_name VARCHAR(100),
    description TEXT,
    tech_stack VARCHAR(255),
    project_link VARCHAR(255),
    cover_image VARCHAR(255),
    sort_order INT NOT NULL DEFAULT 0,
    published TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS dyx_photo (
    id BIGINT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    image_url VARCHAR(255),
    description VARCHAR(500),
    shot_at DATETIME,
    sort_order INT NOT NULL DEFAULT 0,
    published TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS dyx_profile (
    id BIGINT PRIMARY KEY,
    site_title VARCHAR(200),
    hero_title VARCHAR(200),
    hero_subtitle VARCHAR(500),
    about_content TEXT,
    education_experience TEXT,
    work_experience TEXT,
    email VARCHAR(128),
    phone VARCHAR(64),
    wechat VARCHAR(64),
    github_url VARCHAR(255),
    avatar_url VARCHAR(255),
    updated_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS dyx_media (
    id BIGINT PRIMARY KEY,
    original_name VARCHAR(255),
    file_name VARCHAR(255),
    file_url VARCHAR(255),
    media_type VARCHAR(64),
    file_size BIGINT,
    created_at DATETIME NOT NULL
);

INSERT INTO dyx_user (id, username, password, display_name, role, enabled, created_at, updated_at)
VALUES (1, 'admin', 'admin123456', 'DYX 管理员', 'ADMIN', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE
    display_name = VALUES(display_name),
    role = VALUES(role),
    enabled = VALUES(enabled),
    updated_at = NOW();

INSERT INTO dyx_profile (id, site_title, hero_title, hero_subtitle, about_content, education_experience, work_experience, email, phone, wechat, github_url, avatar_url, updated_at)
VALUES (
    1,
    'dyx-blog',
    '构建一个兼具内容表达与个人展示的现代博客网站。',
    '用于承载博客文章、个人动态、项目经历、照片集以及教育与工作经历，整体视觉保持简洁、通透与高级留白。',
    '你好，我是 DYX，持续关注前端工程化、Java 后端开发与产品表达。我希望通过这个站点沉淀项目实践、技术思考和个人阶段性成长。',
    '2018 - 2022：完成计算机相关专业学习，系统积累 Web 开发、数据库与软件工程基础。\n2022 - 2024：持续通过个人项目和实战练习深化前端交互、接口设计与全栈协作能力。',
    '2024 - 至今：以个人博客与项目实战为主线，持续打磨 Vue 3、Spring Boot 3、后台管理系统与内容型网站的完整交付能力。',
    'example@example.com',
    '13800000000',
    'dyx-wechat',
    'https://github.com/example',
    '',
    NOW()
)
ON DUPLICATE KEY UPDATE
    site_title = VALUES(site_title),
    hero_title = VALUES(hero_title),
    hero_subtitle = VALUES(hero_subtitle),
    about_content = VALUES(about_content),
    education_experience = VALUES(education_experience),
    work_experience = VALUES(work_experience),
    email = VALUES(email),
    phone = VALUES(phone),
    wechat = VALUES(wechat),
    github_url = VALUES(github_url),
    avatar_url = VALUES(avatar_url),
    updated_at = NOW();

INSERT INTO dyx_post (id, title, summary, content, cover_image, category, tags, published, view_count, created_at, updated_at)
VALUES
    (
        101,
        '从零搭建 dyx-blog：Vue 3 与 Spring Boot 3 的一次完整实践',
        '记录 dyx-blog 从结构设计、路由分区到前后端联调的完整搭建过程。',
        '这篇文章梳理了 dyx-blog 的整体搭建过程：前端采用 Vue 3、TypeScript、Pinia、Vue Router 与 Tailwind CSS，后端采用 Spring Boot 3、MyBatis-Plus 与 JWT。通过统一的数据结构和后台管理界面，逐步形成一个既能承载个人展示，又能支持博客发布与内容管理的完整项目。',
        '',
        '项目复盘',
        'Vue3,SpringBoot3,全栈开发',
        1,
        128,
        NOW(),
        NOW()
    ),
    (
        102,
        '前后台共用一个 Vue 项目时，路由分区应该怎么设计',
        '分享前台站点与 /admin 管理后台共用一个前端工程时的模块拆分思路。',
        '在 dyx-blog 中，前台页面与后台页面共用同一个 Vue 工程，通过不同 layout 和路由守卫进行分区。这样既能共享设计 token、请求层和状态管理，又能保持前台展示与后台管理在目录和职责上的清晰边界。',
        '',
        '架构设计',
        'Vue Router,后台管理,工程化',
        1,
        86,
        NOW(),
        NOW()
    ),
    (
        103,
        '为什么这个个人博客后台选择 JWT 而不是 Spring Security',
        '结合当前项目规模，说明选择 JWT 的原因与取舍。',
        '对于当前阶段的 dyx-blog 来说，后台权限模型相对简单，主要诉求是快速形成登录鉴权、内容管理和接口保护的业务闭环。JWT 更轻量、接入成本更低，也便于前后端分离项目快速落地；如果后续权限体系继续复杂化，再逐步演进到 Spring Security 会更合适。',
        '',
        '技术选型',
        'JWT,Spring Security,认证授权',
        1,
        64,
        NOW(),
        NOW()
    )
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    summary = VALUES(summary),
    content = VALUES(content),
    cover_image = VALUES(cover_image),
    category = VALUES(category),
    tags = VALUES(tags),
    published = VALUES(published),
    view_count = VALUES(view_count),
    updated_at = NOW();

INSERT INTO dyx_moment (id, title, content, cover_image, happened_at, sort_order, published, created_at, updated_at)
VALUES
    (
        201,
        '完成后台博客管理真实 CRUD 接入',
        '后台博客管理页已支持新建、编辑、删除与列表刷新，前后台数据链路进一步打通。',
        '',
        NOW(),
        1,
        1,
        NOW(),
        NOW()
    ),
    (
        202,
        '补齐动态、项目、照片管理页的数据闭环',
        '三个后台模块均已接入真实接口，前台对应页面也可以同步消费发布后的内容。',
        '',
        NOW(),
        2,
        1,
        NOW(),
        NOW()
    ),
    (
        203,
        '为站点补充初始展示内容',
        '通过初始化 SQL 为各模块写入演示数据，保证首次运行时前后台都能看到完整内容。',
        '',
        NOW(),
        3,
        1,
        NOW(),
        NOW()
    )
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    content = VALUES(content),
    cover_image = VALUES(cover_image),
    happened_at = VALUES(happened_at),
    sort_order = VALUES(sort_order),
    published = VALUES(published),
    updated_at = NOW();

INSERT INTO dyx_project (id, name, role_name, description, tech_stack, project_link, cover_image, sort_order, published, created_at, updated_at)
VALUES
    (
        301,
        'dyx-blog',
        '全栈开发',
        '一个集个人展示、博客系统与后台管理于一体的全栈项目，重点关注内容表达、模块化组织与完整数据链路。',
        'Vue 3 / TypeScript / Tailwind CSS / Spring Boot 3 / MyBatis-Plus / MySQL',
        'https://github.com/example/dyx-blog',
        '',
        1,
        1,
        NOW(),
        NOW()
    ),
    (
        302,
        '个人作品集官网',
        '前端开发',
        '围绕个人介绍、案例展示和联系入口构建响应式官网，强调视觉留白、叙事结构与移动端体验。',
        'Vue 3 / Vite / Tailwind CSS',
        'https://github.com/example/portfolio-site',
        '',
        2,
        1,
        NOW(),
        NOW()
    ),
    (
        303,
        '后台管理模板实验项目',
        '前后端联调',
        '用于沉淀表格管理、表单编辑、权限校验与接口封装模式，服务于后续内容型系统快速搭建。',
        'Vue 3 / Element Plus / Spring Boot / JWT',
        'https://github.com/example/admin-lab',
        '',
        3,
        1,
        NOW(),
        NOW()
    )
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    role_name = VALUES(role_name),
    description = VALUES(description),
    tech_stack = VALUES(tech_stack),
    project_link = VALUES(project_link),
    cover_image = VALUES(cover_image),
    sort_order = VALUES(sort_order),
    published = VALUES(published),
    updated_at = NOW();

INSERT INTO dyx_photo (id, title, image_url, description, shot_at, sort_order, published, created_at, updated_at)
VALUES
    (
        401,
        '城市夜色',
        '',
        '记录夜晚城市灯光与节奏感，用来补充站点更生活化的一面。',
        NOW(),
        1,
        1,
        NOW(),
        NOW()
    ),
    (
        402,
        '工作台一角',
        '',
        '展示日常开发环境与设备陈设，传达项目实践背后的工作状态。',
        NOW(),
        2,
        1,
        NOW(),
        NOW()
    ),
    (
        403,
        '旅途片段',
        '',
        '用影像记录移动中的观察与灵感，丰富个人表达维度。',
        NOW(),
        3,
        1,
        NOW(),
        NOW()
    )
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    image_url = VALUES(image_url),
    description = VALUES(description),
    shot_at = VALUES(shot_at),
    sort_order = VALUES(sort_order),
    published = VALUES(published),
    updated_at = NOW();
