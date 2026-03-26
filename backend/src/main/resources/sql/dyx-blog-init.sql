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
    image_urls TEXT,
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

CREATE TABLE IF NOT EXISTS dyx_work (
    id BIGINT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    summary TEXT,
    cover_image VARCHAR(255),
    image_urls TEXT,
    video_url VARCHAR(255),
    video_poster VARCHAR(255),
    work_link VARCHAR(255),
    sort_order INT NOT NULL DEFAULT 0,
    published TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS dyx_honor (
    id BIGINT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    issuer VARCHAR(200),
    description TEXT,
    cover_image VARCHAR(255),
    image_urls TEXT,
    attachment_url VARCHAR(255),
    award_at DATETIME,
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
    hero_config LONGTEXT,
    about_content TEXT,
    education_experience TEXT,
    work_experience TEXT,
    email VARCHAR(128),
    phone VARCHAR(64),
    wechat VARCHAR(64),
    github_url VARCHAR(255),
    contact_methods LONGTEXT,
    avatar_url VARCHAR(255),
    resume_pdf_url VARCHAR(255),
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

CREATE TABLE IF NOT EXISTS dyx_site_visit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    page_key VARCHAR(64) NOT NULL,
    ip_address VARCHAR(45) NOT NULL,
    user_agent VARCHAR(512),
    device_type VARCHAR(32) NOT NULL,
    device_name VARCHAR(128),
    created_at DATETIME NOT NULL,
    INDEX idx_site_visit_log_created_at (created_at),
    INDEX idx_site_visit_log_page_key (page_key),
    INDEX idx_site_visit_log_device_type (device_type)
);

INSERT INTO dyx_media (id, original_name, file_name, file_url, media_type, file_size, created_at)
VALUES
    (601, '42263dcc-1292-434c-8aa8-878f5b8ba6bc.jpg', '42263dcc-1292-434c-8aa8-878f5b8ba6bc.jpg', '/media/42263dcc-1292-434c-8aa8-878f5b8ba6bc.jpg', 'image/jpeg', 0, NOW()),
    (602, '2024福建省职业技能大赛.jpeg', '2024福建省职业技能大赛.jpeg', '/media/2024福建省职业技能大赛.jpeg', 'image/jpeg', 0, NOW()),
    (603, '20220323203851-0001.jpg', '20220323203851-0001.jpg', '/media/20220323203851-0001.jpg', 'image/jpeg', 0, NOW()),
    (604, '金砖.jpg', '金砖.jpg', '/media/金砖.jpg', 'image/jpeg', 0, NOW()),
    (605, '文件_福建船政交通职业学院-微视频结果(4).pdf', '文件_福建船政交通职业学院-微视频结果(4).pdf', '/media/文件_福建船政交通职业学院-微视频结果(4).pdf', 'application/pdf', 0, NOW()),
    (606, '直聘简历-未命名.pdf', '直聘简历-未命名.pdf', '/media/直聘简历-未命名.pdf', 'application/pdf', 0, NOW()),
    (607, '丁益星(350784200310120035).pdf', '丁益星(350784200310120035).pdf', '/media/丁益星(350784200310120035).pdf', 'application/pdf', 0, NOW()),
    (608, '丁益星(350784200310120035)_20250821_022043.pdf', '丁益星(350784200310120035)_20250821_022043.pdf', '/media/丁益星(350784200310120035)_20250821_022043.pdf', 'application/pdf', 0, NOW())
ON DUPLICATE KEY UPDATE
    original_name = VALUES(original_name),
    file_name = VALUES(file_name),
    file_url = VALUES(file_url),
    media_type = VALUES(media_type),
    file_size = VALUES(file_size);

INSERT INTO dyx_user (id, username, password, display_name, role, enabled, created_at, updated_at)
VALUES (1, 'admin', 'admin123456', 'DYX 管理员', 'ADMIN', 1, NOW(), NOW())
ON DUPLICATE KEY UPDATE
    display_name = VALUES(display_name),
    role = VALUES(role),
    enabled = VALUES(enabled),
    updated_at = NOW();

INSERT INTO dyx_profile (id, site_title, hero_title, hero_subtitle, hero_config, about_content, education_experience, work_experience, email, phone, wechat, github_url, contact_methods, avatar_url, resume_pdf_url, updated_at)
VALUES (
    1,
    'dyx-blog',
    '构建一个兼具内容表达与个人展示的现代博客网站。',
    '用于承载博客文章、个人动态、荣誉成果以及教育与工作经历，整体视觉保持简洁、通透与高级留白。',
    '{"version":1,"blocks":[{"id":"eyebrow-default","type":"eyebrow","column":"left","text":"dyx-blog"},{"id":"title-default","type":"title","column":"left","text":"构建一个兼具内容表达与个人展示的现代博客网站。"},{"id":"subtitle-default","type":"subtitle","column":"left","text":"用于承载博客文章、个人动态、荣誉成果以及教育与工作经历，整体视觉保持简洁、通透与高级留白。"},{"id":"tags-default","type":"tags","column":"left","items":["后端 · Java","安全 / 基础设施","随笔与长文"]},{"id":"image-default","type":"image","column":"right","imageUrl":"/media/42263dcc-1292-434c-8aa8-878f5b8ba6bc.jpg","alt":"avatar"}]}',
    '你好，我是 DYX，持续关注前端工程化、Java 后端开发与产品表达。我希望通过这个站点沉淀项目实践、技术思考和个人阶段性成长。',
    '2018 - 2022：完成计算机相关专业学习，系统积累 Web 开发、数据库与软件工程基础。\n2022 - 2024：持续通过个人项目和实战练习深化前端交互、接口设计与全栈协作能力。',
    '2024 - 至今：以个人博客与项目实战为主线，持续打磨 Vue 3、Spring Boot 3、后台管理系统与内容型网站的完整交付能力。',
    'example@example.com',
    '13800000000',
    'dyx-wechat',
    'https://github.com/example',
    '[{"type":"email","label":"邮箱","value":"example@example.com"},{"type":"phone","label":"电话","value":"13800000000"},{"type":"wechat","label":"微信","value":"dyx-wechat"},{"type":"github","label":"GitHub","value":"https://github.com/example"}]',
    '/media/42263dcc-1292-434c-8aa8-878f5b8ba6bc.jpg',
    '/media/直聘简历-未命名.pdf',
    NOW()
)
ON DUPLICATE KEY UPDATE
    site_title = VALUES(site_title),
    hero_title = VALUES(hero_title),
    hero_subtitle = VALUES(hero_subtitle),
    hero_config = VALUES(hero_config),
    about_content = VALUES(about_content),
    education_experience = VALUES(education_experience),
    work_experience = VALUES(work_experience),
    email = VALUES(email),
    phone = VALUES(phone),
    wechat = VALUES(wechat),
    github_url = VALUES(github_url),
    contact_methods = VALUES(contact_methods),
    avatar_url = VALUES(avatar_url),
    resume_pdf_url = VALUES(resume_pdf_url),
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

INSERT INTO dyx_moment (id, title, content, cover_image, image_urls, happened_at, sort_order, published, created_at, updated_at)
VALUES
    (
        201,
        '完成后台博客管理真实 CRUD 接入',
        '后台博客管理页已支持新建、编辑、删除与列表刷新，前后台数据链路进一步打通。',
        '',
        '',
        NOW(),
        1,
        1,
        NOW(),
        NOW()
    ),
    (
        202,
        '补齐动态、项目与荣誉管理的数据闭环',
        '三个后台模块均已接入真实接口，前台对应页面也可以同步消费发布后的内容。',
        '',
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
    image_urls = VALUES(image_urls),
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
    ),
    (
        304,
        '校园微视频作品实践',
        '策划 / 剪辑 / 视觉表达',
        '围绕校园主题完成脚本整理、素材剪辑与成片输出，结果证明文件已纳入媒体库，可作为个人作品与内容表达能力的补充案例。',
        '视频脚本 / 剪辑表达 / 视觉设计 / 内容策划',
        '/media/文件_福建船政交通职业学院-微视频结果(4).pdf',
        '',
        4,
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

INSERT INTO dyx_work (id, title, summary, cover_image, image_urls, video_url, video_poster, work_link, sort_order, published, created_at, updated_at)
VALUES
    (
        401,
        '校园微视频作品实践',
        '围绕校园主题完成脚本整理、素材剪辑与成片输出。',
        '/media/2024福建省职业技能大赛.jpeg',
        '["/media/2024福建省职业技能大赛.jpeg"]',
        '',
        '',
        '/media/文件_福建船政交通职业学院-微视频结果(4).pdf',
        1,
        1,
        NOW(),
        NOW()
    ),
    (
        402,
        '图像处理与证书展示',
        '整理个人证书与图像处理成果，用于补充作品表达。',
        '/media/20220323203851-0001.jpg',
        '["/media/20220323203851-0001.jpg","/media/金砖.jpg"]',
        '',
        '',
        '',
        2,
        1,
        NOW(),
        NOW()
    )
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    summary = VALUES(summary),
    cover_image = VALUES(cover_image),
    image_urls = VALUES(image_urls),
    video_url = VALUES(video_url),
    video_poster = VALUES(video_poster),
    work_link = VALUES(work_link),
    sort_order = VALUES(sort_order),
    published = VALUES(published),
    updated_at = NOW();

INSERT INTO dyx_honor (id, title, issuer, description, cover_image, image_urls, attachment_url, award_at, sort_order, published, created_at, updated_at)
VALUES
    (
        501,
        '福建省职业技能大赛奖状',
        '福建建设职业技术学院代表队',
        '奖状中记录了 2024 年福建省职业技能大赛相关参赛与结果信息，包含队员姓名与指导教师信息，可作为阶段性赛事经历证明。',
        '/media/2024福建省职业技能大赛.jpeg',
        '["/media/2024福建省职业技能大赛.jpeg"]',
        '',
        '2024-01-01 10:00:00',
        1,
        1,
        NOW(),
        NOW()
    ),
    (
        502,
        'Photoshop 图形图像处理专项职业能力',
        '职业技能鉴定（指导）中心',
        '2021-01-23 参加 Photoshop 图形图像处理专项职业能力考核，成绩 97.0，发证日期为 2021-05-11，可作为图像处理与视觉表达能力证明。',
        '/media/20220323203851-0001.jpg',
        '["/media/20220323203851-0001.jpg"]',
        '/media/丁益星(350784200310120035).pdf',
        '2021-05-11 10:00:00',
        2,
        1,
        NOW(),
        NOW()
    ),
    (
        503,
        '全栈项目实践结项优秀',
        '个人项目训练营',
        '围绕 Vue 3 与 Spring Boot 3 的整站项目实践获得优秀结项评价。',
        '',
        '',
        '',
        '2024-08-01 10:00:00',
        3,
        1,
        NOW(),
        NOW()
    )
ON DUPLICATE KEY UPDATE
    title = VALUES(title),
    issuer = VALUES(issuer),
    description = VALUES(description),
    cover_image = VALUES(cover_image),
    image_urls = VALUES(image_urls),
    attachment_url = VALUES(attachment_url),
    award_at = VALUES(award_at),
    sort_order = VALUES(sort_order),
    published = VALUES(published),
    updated_at = NOW();

INSERT INTO dyx_site_visit_stat (page_key, visit_count, updated_at)
VALUES
    ('home', 0, NOW()),
    ('profile', 0, NOW()),
    ('resume', 0, NOW()),
    ('moments', 0, NOW()),
    ('blog', 0, NOW()),
    ('blog-detail', 0, NOW())
ON DUPLICATE KEY UPDATE
    updated_at = NOW();

INSERT INTO dyx_site_visit_log (page_key, ip_address, user_agent, device_type, device_name, created_at)
VALUES
    ('home', '127.0.0.1', 'Mozilla/5.0 (Windows NT 10.0; Win64; x64)', 'DESKTOP', 'Windows', NOW()),
    ('blog', '127.0.0.1', 'Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X)', 'MOBILE', 'iPhone', NOW());
