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
    published_at DATETIME,
    view_count INT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

SET @dyx_post_has_published_at := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'dyx_post'
      AND COLUMN_NAME = 'published_at'
);
SET @dyx_post_add_published_at_sql := IF(
    @dyx_post_has_published_at = 0,
    'ALTER TABLE dyx_post ADD COLUMN published_at DATETIME NULL AFTER published',
    'SELECT 1'
);
PREPARE dyx_post_add_published_at_stmt FROM @dyx_post_add_published_at_sql;
EXECUTE dyx_post_add_published_at_stmt;
DEALLOCATE PREPARE dyx_post_add_published_at_stmt;

UPDATE dyx_post
SET published_at = COALESCE(updated_at, created_at)
WHERE published_at IS NULL;

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
    award_at DATETIME,
    sort_order INT NOT NULL DEFAULT 0,
    published TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
);

SET @dyx_work_has_award_at := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'dyx_work'
      AND COLUMN_NAME = 'award_at'
);
SET @dyx_work_add_award_at_sql := IF(
    @dyx_work_has_award_at = 0,
    'ALTER TABLE dyx_work ADD COLUMN award_at DATETIME AFTER work_link',
    'SELECT 1'
);
PREPARE dyx_work_add_award_at_stmt FROM @dyx_work_add_award_at_sql;
EXECUTE dyx_work_add_award_at_stmt;
DEALLOCATE PREPARE dyx_work_add_award_at_stmt;

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
    guestbook_intro TEXT,
    updated_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS dyx_guestbook_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    content TEXT NOT NULL,
    published TINYINT NOT NULL DEFAULT 0,
    ip_address VARCHAR(45) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_guestbook_message_published (published),
    INDEX idx_guestbook_message_created_at (created_at)
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

CREATE TABLE IF NOT EXISTS dyx_site_visit_stat (
    page_key VARCHAR(64) PRIMARY KEY,
    visit_count BIGINT NOT NULL DEFAULT 0,
    updated_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS dyx_footprint (
    id BIGINT PRIMARY KEY,
    city_name VARCHAR(100) NOT NULL,
    country_name VARCHAR(100),
    region_name VARCHAR(100),
    position_x DECIMAL(5,2) NOT NULL DEFAULT 0,
    position_y DECIMAL(5,2) NOT NULL DEFAULT 0,
    visited_at DATETIME,
    description TEXT,
    importance INT NOT NULL DEFAULT 1,
    sort_order INT NOT NULL DEFAULT 0,
    published TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_footprint_published (published),
    INDEX idx_footprint_sort_order (sort_order),
    INDEX idx_footprint_visited_at (visited_at)
);

CREATE TABLE IF NOT EXISTS dyx_system_config (
    id BIGINT PRIMARY KEY,
    storage_type VARCHAR(32) NOT NULL DEFAULT 'local',
    oss_endpoint VARCHAR(255),
    oss_region VARCHAR(100),
    oss_bucket_name VARCHAR(255),
    oss_public_url_prefix VARCHAR(255),
    oss_base_dir VARCHAR(255),
    footprint_eyebrow VARCHAR(120),
    footprint_title VARCHAR(200),
    footprint_subtitle VARCHAR(255),
    footprint_description VARCHAR(500),
    copyright_text VARCHAR(255),
    tech_support_text VARCHAR(255),
    updated_at DATETIME NOT NULL
);

INSERT INTO dyx_post (id, title, summary, content, cover_image, category, tags, published, published_at, view_count, created_at, updated_at)
SELECT 1930000000000000001, 'Java 基础学习笔记整理', '从 Java 入门语法、循环与方法、面向对象、JVM 基础到 JDBC 操作的一次课堂式笔记整理。',
'<p>这篇内容整理自我的 Java 学习笔记，保留原始知识点推进顺序，并按博客阅读节奏重组为更容易复盘的结构。</p>
<h2>适合谁阅读</h2>
<p>适合正在回顾 Java 入门语法、流程控制、方法机制、面向对象与 JDBC 基础的同学，尤其适合做阶段性复习。</p>
<h2>本篇主线</h2>
<ul>
  <li>Java 基础语法与程序结构</li>
  <li>循环、方法与常见语言特性</li>
  <li>封装、继承、多态与 static</li>
  <li>JVM / GC 的初步认识</li>
  <li>IDEA 与 JDBC 入门实践</li>
</ul>
<h2>重点整理</h2>
<p>Java 学习通常从开发环境、注释、标识符、变量、常量和基本数据类型开始，先建立完整的语言骨架认知。</p>
<p>流程控制部分的重点在于 <code>if</code>、<code>switch</code>、<code>for</code>、<code>while</code>、<code>do...while</code> 以及 <code>break</code> / <code>continue</code> 的适用场景。</p>
<p>方法章节需要理解定义方式、参数传递、返回值、重载，以及 <code>main</code> 作为程序入口的角色。</p>
<p>进入面向对象后，学习重点会切换到类与对象、构造方法、<code>this</code>、访问修饰符、继承体系、多态行为与 <code>static</code> 成员。</p>
<p>JVM 部分先建立栈、堆、类加载与垃圾回收的基本印象，再为后续性能与内存问题排查打下基础。</p>
<p>数据库入门通常会结合 JDBC 连接、<code>PreparedStatement</code>、结果集遍历与资源释放，让语法知识真正落到可运行的代码上。</p>
<blockquote><p>这篇更适合用来快速复盘知识框架；如果后续继续补充，我会把课堂式提纲逐步补成更完整的代码示例与实践总结。</p></blockquote>',
NULL, 'Java', 'Java,基础语法,JVM,面向对象,JDBC', 1, '2023-03-25 00:00:00', 0, '2023-03-25 00:00:00', '2023-03-25 00:00:00'
WHERE NOT EXISTS (SELECT 1 FROM dyx_post WHERE title = 'Java 基础学习笔记整理');

INSERT INTO dyx_post (id, title, summary, content, cover_image, category, tags, published, published_at, view_count, created_at, updated_at)
SELECT 1930000000000000002, 'MyBatis 入门与动态 SQL 笔记整理', '围绕 MyBatis 基础概念、Mapper 映射、动态 SQL 与 MyBatis-Plus 入门的学习笔记整理。',
'<p>这篇内容来自我的 MyBatis 课堂笔记，重点围绕“如何把 SQL 与 Java 代码解耦”这一主线重新整理。</p>
<p><img src="/blog-note-images/mybatis-page-1.png" alt="MyBatis 笔记原始页面截图" /></p>
<h2>适合谁阅读</h2>
<p>适合已经会写基础 JDBC、希望进一步理解 Mapper 映射、参数传递和动态 SQL 的后端初学者。</p>
<h2>本篇主线</h2>
<ul>
  <li>MyBatis 基础认知与使用场景</li>
  <li>SqlSession、Mapper、XML 映射之间的关系</li>
  <li><code>@Param</code> 与多参数传递</li>
  <li><code>if</code>、<code>where</code>、<code>set</code>、<code>foreach</code> 等动态 SQL 标签</li>
  <li>事务提交与 MyBatis-Plus 入门</li>
</ul>
<h2>重点整理</h2>
<p>MyBatis 的核心价值在于把 SQL 保留在更容易维护的位置，让 Java 代码专注于业务组织与参数传递。</p>
<p>入门时最重要的是先理解 <code>SqlSession</code>、Mapper 接口和 XML 映射文件三者如何形成一条完整调用链。</p>
<p>参数传递常见形式包括单参数、对象参数、Map 参数，以及通过 <code>@Param</code> 为多参数显式命名。</p>
<p>动态 SQL 则重点解决“条件不固定”的场景：例如查询筛选条件可选、更新字段不完整、批量 ID 处理等问题。</p>
<p>继续往后走时，可以借助 MyBatis-Plus 的通用 CRUD 与 Wrapper 条件构造器，减少重复模板代码。</p>
<blockquote><p>笔记里的原始页面保留了配置、依赖与映射示例，适合在复盘概念时对照查看。</p></blockquote>',
NULL, 'Java 后端', 'MyBatis,Mapper,动态SQL,MyBatis-Plus', 1, '2023-04-24 00:00:00', 0, '2023-04-24 00:00:00', '2023-04-24 00:00:00'
WHERE NOT EXISTS (SELECT 1 FROM dyx_post WHERE title = 'MyBatis 入门与动态 SQL 笔记整理');

INSERT INTO dyx_post (id, title, summary, content, cover_image, category, tags, published, published_at, view_count, created_at, updated_at)
SELECT 1930000000000000003, 'Spring Boot 核心概念学习笔记整理', '从 Spring Boot 自动配置、starter、IOC 到 Web 入门的核心概念学习笔记整理。',
'<p>这篇文章把 Spring Boot 相关笔记按“为什么出现、怎么启动、如何落地 Web 项目”重新串联，适合做概念复盘。</p>
<h2>适合谁阅读</h2>
<p>适合刚从传统 Spring 或纯 Servlet/JSP 开始转向 Spring Boot 的同学，用来快速建立整体认知。</p>
<h2>本篇主线</h2>
<ul>
  <li>Spring Boot 解决了什么问题</li>
  <li>starter 与依赖管理机制</li>
  <li>启动类与自动配置</li>
  <li>Web 项目快速搭建</li>
  <li>IOC、配置类与 Bean 管理基础</li>
</ul>
<h2>重点整理</h2>
<p>Spring Boot 的价值不只是“启动更快”，而是把项目初始化、依赖管理和常见中间件整合流程标准化。</p>
<p><code>starter</code> 机制把常见技术栈封装为一致的接入入口，大幅降低了手工拼装依赖的复杂度。</p>
<p>启动类上的 <code>@SpringBootApplication</code> 是一个组合注解，它把配置类、自动配置和组件扫描整合到了一起。</p>
<p>在 Web 场景里，学习重点通常围绕 Controller、请求映射、参数接收、JSON 返回和配置文件组织展开。</p>
<p>继续深入时，则需要逐步建立 IOC 容器、依赖注入、<code>@Configuration</code> 与 <code>@Bean</code> 的基础理解。</p>
<blockquote><p>这部分内容非常适合作为“Spring Boot 第一次系统回顾”的提纲索引。</p></blockquote>',
NULL, 'Java 后端', 'Spring Boot,Spring MVC,IOC,自动配置,Maven', 1, '2023-04-17 00:00:00', 0, '2023-04-17 00:00:00', '2023-04-17 00:00:00'
WHERE NOT EXISTS (SELECT 1 FROM dyx_post WHERE title = 'Spring Boot 核心概念学习笔记整理');

INSERT INTO dyx_post (id, title, summary, content, cover_image, category, tags, published, published_at, view_count, created_at, updated_at)
SELECT 1930000000000000004, 'MySQL 基础与常用 SQL 学习笔记整理', '从表结构设计、增删改查、约束、范式到事务基础的一次 MySQL 学习笔记整理。',
'<p>这篇 MySQL 笔记按数据库学习最常见的路径重新组织：先认 SQL 分类，再回到表结构、约束与事务。</p>
<p><img src="/blog-note-images/mysql-page-1.png" alt="MySQL 笔记原始页面截图" /></p>
<h2>适合谁阅读</h2>
<p>适合刚接触数据库设计与 SQL 语句、想快速建立“表结构 + 增删改查 + 事务”整体框架的同学。</p>
<h2>本篇主线</h2>
<ul>
  <li>SQL 分类与常见数据类型</li>
  <li>基础 CRUD 语句</li>
  <li>表结构修改与约束</li>
  <li>范式与关系设计</li>
  <li>事务与连接查询</li>
</ul>
<h2>重点整理</h2>
<p>入门阶段首先要把 DDL、DML、DQL、DCL 这些 SQL 分类区分清楚，避免把“定义结构”和“操作数据”混在一起。</p>
<p>CRUD 基础包括数据库与数据表的创建、插入、更新、删除、条件查询、排序与分页，这些是后续所有业务开发的基础。</p>
<p>表结构部分要掌握 <code>ALTER TABLE</code>、主键、唯一约束、非空约束和默认值等能力，因为这些直接决定数据质量。</p>
<p>关系设计中通常会接触一对一、一对多、多对多和三大范式，用来理解为什么表需要拆分、如何减少冗余。</p>
<p>事务部分则重点围绕 ACID、提交与回滚、隔离级别，以及连接查询和子查询的典型写法展开。</p>
<blockquote><p>如果你正在复习数据库基础，这篇更适合搭配实际建表与查询练习一起看。</p></blockquote>',
NULL, '数据库', 'MySQL,SQL,DDL,DML,DQL,约束,事务', 1, '2023-03-28 00:00:00', 0, '2023-03-28 00:00:00', '2023-03-28 00:00:00'
WHERE NOT EXISTS (SELECT 1 FROM dyx_post WHERE title = 'MySQL 基础与常用 SQL 学习笔记整理');

INSERT INTO dyx_post (id, title, summary, content, cover_image, category, tags, published, published_at, view_count, created_at, updated_at)
SELECT 1930000000000000005, 'Redis 入门到 Spring Data Redis 笔记整理', '覆盖 Redis 核心数据结构、Jedis 客户端与 Spring Data Redis 使用方式的学习笔记整理。',
'<p>这篇 Redis 学习笔记按“基础概念 → 数据结构 → Java 接入 → Spring 整合”的顺序重新梳理，更适合系统复盘。</p>
<p><img src="/blog-note-images/redis-page-1.png" alt="Redis 笔记原始页面截图" /></p>
<h2>适合谁阅读</h2>
<p>适合已经知道 Redis 是缓存数据库，但还没有系统梳理过核心数据结构、Jedis 与 Spring Data Redis 接入方式的同学。</p>
<h2>本篇主线</h2>
<ul>
  <li>Redis 与 NoSQL 基础</li>
  <li>Key 设计与常见命令</li>
  <li>五大核心数据结构</li>
  <li>Jedis 操作示例</li>
  <li>Spring Data Redis 与序列化</li>
</ul>
<h2>重点整理</h2>
<p>Redis 是典型的内存型 NoSQL 数据库，常用于缓存、计数器、排行榜和一些高频读写场景。</p>
<p>学习时需要先掌握字符串、哈希、列表、集合和有序集合等核心数据结构，因为它们决定了 Redis 的建模方式。</p>
<p>Key 设计不仅是“取个名字”，还涉及命名规范、过期时间控制、批量操作和删除策略。</p>
<p>Jedis 更适合作为基础客户端入门，而 Spring Data Redis 则更方便与 Spring 项目整合，适合进入真实业务场景。</p>
<p>继续深入时，还需要理解序列化策略、缓存穿透、缓存一致性等开发中常见的问题。</p>
<blockquote><p>如果你是第一次把 Redis 接入 Java 项目，这篇笔记适合作为总览式导航。</p></blockquote>',
NULL, '数据库', 'Redis,NoSQL,Jedis,Spring Data Redis,缓存', 1, '2025-08-18 00:00:00', 0, '2025-08-18 00:00:00', '2025-08-18 00:00:00'
WHERE NOT EXISTS (SELECT 1 FROM dyx_post WHERE title = 'Redis 入门到 Spring Data Redis 笔记整理');

INSERT INTO dyx_post (id, title, summary, content, cover_image, category, tags, published, published_at, view_count, created_at, updated_at)
SELECT 1930000000000000006, '前端基础（HTML + CSS）学习笔记整理', '从 HTML 标签、表单、媒体元素到 CSS 选择器、文本样式与基础布局的前端入门笔记整理。',
'<p>这篇前端基础笔记围绕 HTML 与 CSS 入门知识点重排，适合做一次“从标签到布局”的快速回顾。</p>
<h2>适合谁阅读</h2>
<p>适合刚开始接触网页开发、想系统复习 HTML 常用标签、表单控件和 CSS 基础样式能力的同学。</p>
<h2>本篇主线</h2>
<ul>
  <li>HTML 文档结构</li>
  <li>常用标签与表单控件</li>
  <li>表格、列表与媒体元素</li>
  <li>CSS 选择器与优先级</li>
  <li>文本样式与基础布局</li>
</ul>
<h2>重点整理</h2>
<p>前端入门通常从 HTML 页面结构开始，先理解 <code>html</code>、<code>head</code>、<code>body</code> 等核心标签的职责。</p>
<p>表单部分需要熟悉 <code>input</code>、<code>select</code>、<code>textarea</code>、<code>button</code> 等控件及其常见属性。</p>
<p>页面内容组织还包括表格、列表、图片、音视频等媒体元素的基本使用方式。</p>
<p>CSS 学习重点在于选择器、优先级、盒模型、文本样式、背景、边框和布局基础。</p>
<p>当这些内容建立起来后，后续才能更自然地进入响应式布局、组件化开发和现代前端框架。</p>
<blockquote><p>如果你正在补前端基础，这篇很适合作为“HTML + CSS 第一轮复习提纲”。</p></blockquote>',
NULL, '前端', 'HTML,CSS,表单,布局,选择器', 1, '2023-07-10 00:00:00', 0, '2023-07-10 00:00:00', '2023-07-10 00:00:00'
WHERE NOT EXISTS (SELECT 1 FROM dyx_post WHERE title = '前端基础（HTML + CSS）学习笔记整理');

INSERT INTO dyx_post (id, title, summary, content, cover_image, category, tags, published, published_at, view_count, created_at, updated_at)
SELECT 1930000000000000007, 'uni-app 常用开发知识点整理', '围绕 uni-app 页面开发、常用组件、样式适配、tabBar 与打包配置的课堂式笔记整理。',
'<p>这篇 uni-app 笔记按“页面结构、常用组件、配置文件、打包发布”重新串联，更适合作为移动端多端开发复盘。</p>
<h2>适合谁阅读</h2>
<p>适合已经接触过 Vue 或小程序开发，希望快速了解 uni-app 常见页面开发和工程配置的同学。</p>
<h2>本篇主线</h2>
<ul>
  <li>uni-app 页面与尺寸单位</li>
  <li><code>scroll-view</code> / <code>swiper</code> 等常用组件</li>
  <li>tabBar 与 <code>pages.json</code> 配置</li>
  <li>表单与交互细节</li>
  <li>打包签名与 token 场景</li>
</ul>
<h2>重点整理</h2>
<p>uni-app 的核心优势是多端复用，因此首先要理解页面结构、目录约定和配置文件的组织方式。</p>
<p>常用组件包括 <code>scroll-view</code>、<code>swiper</code>、<code>image</code>、<code>button</code>、<code>input</code> 等，适合快速搭建移动端页面。</p>
<p>样式部分要熟悉 <code>rpx</code> 等尺寸单位及其在不同设备上的适配方式。</p>
<p>项目开发中还会频繁接触 tabBar、<code>pages.json</code>、页面跳转、生命周期和表单交互处理。</p>
<p>上线前则需要重点关注打包签名、环境配置、接口鉴权与 token 管理等工程化问题。</p>
<blockquote><p>如果你准备做一个小型 uni-app 项目，这篇内容适合作为开工前的知识清单。</p></blockquote>',
NULL, '前端', 'uni-app,uView,scroll-view,swiper,打包', 1, '2023-05-12 00:00:00', 0, '2023-05-12 00:00:00', '2023-05-12 00:00:00'
WHERE NOT EXISTS (SELECT 1 FROM dyx_post WHERE title = 'uni-app 常用开发知识点整理');

INSERT INTO dyx_post (id, title, summary, content, cover_image, category, tags, published, published_at, view_count, created_at, updated_at)
SELECT 1930000000000000008, 'Flutter 基础组件与生命周期笔记整理', '围绕 Flutter Widget 体系、State 生命周期、列表组件与弹窗能力的基础学习笔记整理。',
'<p>这篇 Flutter 笔记围绕组件体系、状态生命周期与常见列表/弹窗能力重排，适合快速建立 Flutter 入门框架。</p>
<h2>适合谁阅读</h2>
<p>适合刚接触 Flutter、想从 Widget 思维切入，系统理解页面骨架、状态更新和常见交互组件的同学。</p>
<h2>本篇主线</h2>
<ul>
  <li>Scaffold / Color / 布局基础</li>
  <li>StatelessWidget 与 StatefulWidget</li>
  <li>State 生命周期与 BuildContext</li>
  <li>ListView 系列组件</li>
  <li>Dialog 与常见交互模式</li>
</ul>
<h2>重点整理</h2>
<p>Flutter 以 Widget 作为 UI 构建核心，因此入门时首先要接受“一切皆组件”的开发思想。</p>
<p><code>Scaffold</code> 提供页面骨架，通常与 <code>AppBar</code>、Body、FloatingActionButton 等结构一起使用。</p>
<p>状态管理初步会接触 <code>StatelessWidget</code>、<code>StatefulWidget</code>、<code>setState</code> 与 State 生命周期。</p>
<p>列表展示通常从 <code>ListView</code>、<code>ListTile</code>、builder 构造方式入手，再逐步扩展到更复杂的滚动场景。</p>
<p>交互部分则需要掌握 Dialog、SnackBar、路由跳转与 <code>BuildContext</code> 的使用方式。</p>
<blockquote><p>如果你正从前端或 Android 原生转向 Flutter，这篇内容很适合作为第一轮框架梳理。</p></blockquote>',
NULL, '移动端', 'Flutter,Widget,State,生命周期,ListView,Dialog', 1, '2024-04-21 00:00:00', 0, '2024-04-21 00:00:00', '2024-04-21 00:00:00'
WHERE NOT EXISTS (SELECT 1 FROM dyx_post WHERE title = 'Flutter 基础组件与生命周期笔记整理');

-- 覆盖已存在的旧文章正文：将 8 篇笔记博客统一升级为更完整的长文版富文本内容（不再使用图片）
UPDATE dyx_post
SET content = '<p>这篇文章基于我的 Java 课堂笔记重新整理，不再停留在零散知识点罗列，而是按“语法基础 → 程序结构 → 面向对象 → JVM → JDBC”这条学习路径重组成一篇适合连续阅读的复盘长文。</p>
<h2>为什么 Java 入门总是先学语法与程序结构</h2>
<p>Java 的学习通常从开发环境、源文件、编译产物和运行流程开始。一个 <code>.java</code> 文件经过 <code>javac</code> 编译会生成 <code>.class</code> 字节码文件，再由 JVM 负责加载和执行。理解这条链路很重要，因为它决定了 Java 与“脚本直接执行”类语言的差异：Java 先编译、后运行，跨平台能力来自字节码和 JVM，而不是源代码本身。</p>
<p>在最开始的语法阶段，需要先建立几个最基础的认知：变量与常量如何声明、基本数据类型分别负责什么、表达式和语句怎样组合、方法为什么能把重复逻辑封装起来。很多初学者会把这些内容看成零散规则，但其实它们共同构成了后续一切编程能力的地基。</p>
<h2>流程控制与方法：从“会写”走向“能组织代码”</h2>
<p>当基础语法能写通以后，真正开始训练编程思维的是流程控制。<code>if</code>、<code>switch</code>、<code>for</code>、<code>while</code>、<code>do...while</code> 不是简单记忆格式，而是在训练你如何描述“条件分支”和“重复执行”。例如数组求最大值、遍历统计、区间判断等，都是流程控制最经典的练习场景。</p>
<p>方法则进一步解决“代码复用”和“职责拆分”问题。学习方法时，重点不只是定义格式，更要理解参数传递、返回值、重载以及 <code>main</code> 作为程序入口的意义。一个好的方法应该让调用者只关心输入和输出，而不必重复关心内部步骤，这也是后面理解封装思想的前置条件。</p>
<h2>面向对象真正要掌握的不是语法，而是建模方式</h2>
<p>进入面向对象阶段后，学习重点会自然转移到类、对象、构造方法、<code>this</code>、访问修饰符、继承、多态和 <code>static</code> 成员。很多人第一次学 OOP 时容易陷在“语法点很多”的感觉里，但真正关键的是：你是否开始用“对象负责什么行为、暴露什么状态”的方式去组织程序。</p>
<p>封装解决的是边界问题——哪些属性应该被保护，哪些方法应该对外开放；继承解决的是复用和扩展问题——公共能力能否上提到父类；多态解决的是调用一致性问题——同样一个父类引用，为什么能表现出不同子类行为。理解这些后，再看 <code>extends</code>、<code>override</code>、<code>super</code>、<code>final</code>，就不会只是死记硬背语法。</p>
<h2>JVM 与 GC：先建立正确的“运行时直觉”</h2>
<p>JVM 部分对于初学者来说往往有点抽象，但其实第一阶段不需要追求非常底层，只要先建立基本运行时印象即可：类会被加载，方法调用会进栈，对象多数分配在堆中，而垃圾回收则负责回收不再被引用的对象。只要把“栈、堆、类加载、GC Root、Young/Old 区”这些概念先串起来，后续排查内存、性能和 Full GC 问题时就不会完全陌生。</p>
<p>笔记里还涉及 Eden、Survivor、Tenured、Minor GC、Major GC、Full GC 等内容。对于入门学习来说，更重要的是知道这些机制为何存在：它们不是为了增加复杂度，而是为了让 JVM 能在不同对象生命周期模式下更高效地管理内存。</p>
<h2>JDBC：Java 语法第一次真正连接到业务开发</h2>
<p>JDBC 通常是很多人第一次感受到“Java 终于在做真实事情”的阶段。通过驱动、连接、SQL、<code>Statement</code>、<code>PreparedStatement</code>、结果集和事务控制，原本停留在语言层面的知识开始接入数据库操作。</p>
<p>这里最值得重视的有三点：第一，知道连接数据库的完整流程；第二，理解 <code>PreparedStatement</code> 相比字符串拼接 SQL 的安全性和可维护性；第三，理解事务提交、回滚和自动提交开关为什么会直接影响数据一致性。很多后端开发中的基本功，其实就是从 JDBC 阶段奠定下来的。</p>
<h2>这篇笔记更适合怎样使用</h2>
<p>如果你正处在 Java 入门到后端开发的过渡阶段，这篇内容更适合拿来做“知识骨架复盘”。你不一定要逐句记住，但可以借它重新建立对 Java 学习路径的整体地图：先写语法，再写结构，再理解对象，再理解运行时，最后连接数据库。这条路线一旦清楚，很多零散知识点都会自动归位。</p>
<blockquote><p>对我来说，这份 Java 笔记最有价值的地方不是覆盖了多少点，而是它把“语言基础”和“后端实践起点”放进了同一条连续学习路径中。</p></blockquote>'
WHERE title = 'Java 基础学习笔记整理';

UPDATE dyx_post
SET content = '<p>这篇文章整理自我的 MyBatis 学习笔记，重点不只是记住标签和配置项，而是搞清楚 MyBatis 到底解决了什么问题：它如何把 SQL 与 Java 代码分离，又如何让数据库访问从生硬的 JDBC 模板代码转向更清晰的映射式开发。</p>
<h2>为什么会有 MyBatis</h2>
<p>如果只用 JDBC 写数据库访问，开发者需要自己处理连接、SQL 拼接、参数绑定、结果集映射以及资源释放，重复劳动很多，而且 SQL 与 Java 业务代码高度耦合。MyBatis 的价值就在于把 SQL 保留在更容易维护的位置，让 Java 代码专注于业务组织、参数传递和结果承接。</p>
<p>因此，学习 MyBatis 的第一步不是背 XML，而是先理解它在工程中的定位：它不是替你“不要 SQL”，而是帮助你更有组织地使用 SQL。</p>
<h2>核心运行链路：SqlSession、Mapper 与 XML 映射</h2>
<p>MyBatis 的主线可以概括为：读取配置、创建 <code>SqlSessionFactory</code>、打开 <code>SqlSession</code>、通过 Mapper 接口执行数据库操作。在这个过程中，Mapper 接口承担“面向业务的方法定义”，XML 映射文件承担“SQL 语句和结果映射定义”，二者通过命名空间和语句 id 建立连接。</p>
<p>当这套链路理解清楚后，很多初学者困惑的问题都会变简单：为什么接口里只有方法声明却能执行 SQL？为什么同一个方法名能找到对应 SQL？为什么返回结果能自动映射到 POJO？本质上都是这一套映射机制在工作。</p>
<h2>参数传递：别把“能传进去”当成“已经理解”</h2>
<p>参数传递是 MyBatis 使用中最容易低估、但又最影响实际开发体验的部分。单参数场景通常很简单，而多参数、对象参数、Map 参数以及 <code>@Param</code> 注解命名则直接关系到 SQL 中占位符怎么写、表达式怎么取值。</p>
<p>尤其在多参数场景下，显式使用 <code>@Param</code> 往往能让 SQL 可读性更高，也更不容易因为默认命名规则导致歧义。对于后续动态 SQL 编写来说，清晰的参数命名几乎是基础中的基础。</p>
<h2>动态 SQL 才是 MyBatis 最有存在感的部分</h2>
<p>真正让 MyBatis 从“会用”进阶到“好用”的关键，是动态 SQL。项目中的查询条件往往不是固定的，更新字段也往往不是每次全量提交，这时 <code>if</code>、<code>choose</code>、<code>where</code>、<code>set</code>、<code>trim</code>、<code>foreach</code> 等标签就会发挥价值。</p>
<p>例如可选筛选条件查询、批量 ID 查询、按传入字段动态更新、列表批量插入等，都是动态 SQL 的典型用法。学习这些标签时，不要只盯着语法形式，更要理解它们本质上是在帮你安全地拼接“条件可变”的 SQL 结构。</p>
<h2>事务与执行：别忘了数据库操作最终还是要提交</h2>
<p>MyBatis 只是把数据库访问流程包装得更清晰，但事务规则本身并不会消失。笔记中提到自动提交和手动提交的区别，也说明了为什么某些操作执行后数据库里看不到变化：不是 SQL 没跑，而是事务还没提交。</p>
<p>因此，从 JDBC 过渡到 MyBatis 时，一个容易忽略的问题是：框架简化了调用方式，但你仍然需要理解数据库连接、事务边界和提交时机。否则排查问题时容易把执行结果和提交结果混为一谈。</p>
<h2>从 MyBatis 到 MyBatis-Plus：减少模板代码，但别丢掉 SQL 思维</h2>
<p>笔记后半部分还触及 MyBatis-Plus，这很适合作为入门扩展。MyBatis-Plus 的优势在于通用 CRUD、Wrapper 条件构造器和大量样板代码收敛，能显著提升中后台开发效率。</p>
<p>但它并不意味着可以完全丢掉 SQL 思维。恰恰相反，只有先理解 MyBatis 如何映射 SQL、参数和结果，再去用 MyBatis-Plus，才不会陷入“会调用 API 但不知道底层做了什么”的状态。</p>
<h2>这篇笔记适合怎样复盘</h2>
<p>如果你已经掌握 JDBC，正准备进入更正式的持久层开发，这篇内容最适合作为过渡笔记。它能帮助你理解：Mapper 不是魔法，动态 SQL 不是技巧炫耀，参数命名也不是小事；这些内容组合起来，才构成了一个真正可维护的数据库访问层。</p>
<blockquote><p>MyBatis 最打动我的地方，不是它让 SQL 消失了，而是它让我第一次感受到“SQL 可以被组织得很清晰”。</p></blockquote>'
WHERE title = 'MyBatis 入门与动态 SQL 笔记整理';

UPDATE dyx_post
SET content = '<p>这篇文章把 Spring Boot 相关笔记按“为什么出现、怎么启动、如何自动装配、如何配置 Web 项目”重新串联，目的是把原本分散在注解、依赖、配置文件和自动配置原理中的知识点，整理成一条更适合系统理解的学习主线。</p>
<h2>Spring Boot 解决的到底是什么问题</h2>
<p>如果从传统 Spring 或 Servlet/JSP 项目切换过来，很容易立刻感受到 Spring Boot 的差异：依赖选择更集中、配置更统一、内嵌容器更直接、启动方式更简单。Spring Boot 并不是替代 Spring，而是把 Spring 生态里那些高频、重复、易错的项目初始化与整合步骤标准化了。</p>
<p>也正因为如此，学习 Spring Boot 时最重要的不是只会跑一个 Demo，而是理解它为什么能让项目搭建变得更快：背后依靠的是 starter、自动配置和约定优于配置。</p>
<h2>starter 与依赖管理：项目骨架为什么能这么快搭起来</h2>
<p>Spring Boot 的 starter 机制把一整类技术栈的常用依赖封装成统一入口。比如 <code>spring-boot-starter-web</code> 并不是单个库，而是把 Web 开发常用的一组依赖按兼容版本组织好，让你不需要手动拼出一大串坐标。</p>
<p>这背后依赖的是父工程和依赖管理体系。理解 <code>spring-boot-starter-parent</code>、<code>spring-boot-dependencies</code> 与 Maven 属性版本控制后，就能明白为什么 Spring Boot 项目在大多数情况下“不手动写版本也能正常工作”。</p>
<h2>启动类不是形式主义，而是自动配置入口</h2>
<p>很多教程会告诉你“写一个 <code>@SpringBootApplication</code> 启动类就行”，但这并不意味着这个注解只是一个固定模板。它本质上是多个核心能力的组合：配置类声明、组件扫描和自动配置启用。也就是说，项目为什么能在最少配置下跑起来，很大程度上就是从这个入口展开的。</p>
<p>当你理解了它整合了 <code>@Configuration</code>、<code>@EnableAutoConfiguration</code> 和组件扫描后，再去看自动配置流程、扫描范围、Bean 注册时机，就会更顺畅。</p>
<h2>自动配置的关键不是“神奇”，而是“按条件生效”</h2>
<p>Spring Boot 的自动配置之所以好用，是因为它并不是无脑装配，而是大量基于条件判断的装配：类路径里有没有某个依赖、容器里有没有某个 Bean、配置文件里有没有某类属性、当前是不是某种运行环境。满足条件就注册，不满足就跳过。</p>
<p>因此，学习自动配置最值得掌握的思路是：先看某个功能需要哪些依赖和条件，再看对应的 <code>xxxAutoConfiguration</code> 和 <code>xxxProperties</code> 怎样配合。这样以后排查“为什么这个 Bean 没生效”“为什么配置没被读取”时，就不会完全依赖猜测。</p>
<h2>配置类、Bean 与配置绑定：从 XML 思维过渡出来</h2>
<p>如果有传统 Spring 基础，接触 Spring Boot 时往往会经历一个从 XML 配置转向注解和配置绑定的过程。<code>@Configuration</code>、<code>@Bean</code>、<code>@Component</code>、<code>@Import</code>、<code>@ConfigurationProperties</code> 这些能力组合在一起，构成了今天 Spring Boot 最常见的配置方式。</p>
<p>其中最值得重视的是配置绑定。相比零散的 <code>@Value</code>，<code>@ConfigurationProperties</code> 更适合承载一组结构化配置，也更符合真实项目里“一个模块对应一组配置”的管理方式。</p>
<h2>Web、静态资源与基础工程能力</h2>
<p>Spring Boot 的 Web 入门通常会从控制器、请求映射、JSON 返回、静态资源目录、配置文件和打包插件展开。表面上看这些内容都很“基础”，但它们恰恰构成了真实项目最常见的基础设施：接口怎么暴露、静态资源怎么访问、配置怎么修改、Jar 怎么打包运行。</p>
<p>当你把控制器、<code>application.properties / yml</code>、内嵌 Tomcat、静态资源处理和 Maven 打包这几块串起来，Spring Boot 才真正从“会跑一个 hello”变成“能搭一个 Web 项目”。</p>
<h2>从入门到实战：为什么这篇笔记值得保留</h2>
<p>这份 Spring Boot 笔记后续还延伸到了 Security、Validation、JWT、日志、文件上传、Bean 装配等主题。它的价值不只是在于列了很多知识点，而在于它把“框架使用”和“框架理解”放在了一起：既告诉你怎么写，也提示你背后的自动配置、组件注册和条件装配机制。</p>
<p>如果你正在从 Java 基础、JDBC、MyBatis 走向完整后端开发，这篇内容正好适合作为中间那一层桥梁。它会让你知道：Spring Boot 真正减少的不是学习深度，而是项目起步时的重复劳动。</p>
<blockquote><p>对我来说，Spring Boot 最重要的收获不是“启动更快”，而是开始理解一个现代 Java Web 项目为什么能被搭得这么整齐。</p></blockquote>'
WHERE title = 'Spring Boot 核心概念学习笔记整理';

UPDATE dyx_post
SET content = '<p>这篇文章基于 MySQL 学习笔记重新整理，主线围绕“表结构怎么设计、数据怎么增删改查、约束为什么重要、事务和连接查询怎么理解”展开。相比把知识点孤立地记成术语，我更希望把这些内容组织成一套适合初学者建立数据库整体认知的阅读顺序。</p>
<h2>先理解 SQL 分类，才能知道自己在做什么</h2>
<p>学习 MySQL 时，最容易被忽略但最值得先厘清的，就是 SQL 的分类。DDL 负责定义结构，DML 负责操作数据，DQL 负责查询，DCL 负责权限与控制。很多初学者之所以在写数据库语句时容易混乱，根源就在于没有先把“改表结构”和“改表数据”分开理解。</p>
<p>当你把这四类语句区分清楚后，再去看建表、插入、更新、删除、查询和权限相关命令，整个数据库学习会顺很多。</p>
<h2>数据类型与建表：数据库设计从一开始就在影响后面一切</h2>
<p>MySQL 的入门学习并不只是“会 create table”，更重要的是知道不同字段类型和约束到底意味着什么。整数、字符、可变长字符串、文本、日期时间、浮点和定点数，不只是语法名称，它们决定了数据的存储方式、范围和业务语义。</p>
<p>与此同时，主键、非空、唯一、自增、默认值这些约束也不是附属配置，而是数据质量的第一道保护线。一个字段是否允许为空、是否必须唯一、是否需要索引支持，往往会直接决定后续代码复杂度和数据可控程度。</p>
<h2>CRUD 是数据库最基本也最值得反复练的能力</h2>
<p>增删改查通常被简称为 CRUD，但在实际学习过程中，它们不是几条模板语句那么简单。插入语句会涉及列顺序、默认值和批量插入；更新语句会涉及条件筛选和多字段修改；删除语句必须意识到 <code>where</code> 的重要性；查询则是后续所有数据分析和业务展示的基础。</p>
<p>也正因为如此，初学数据库时最值得反复练习的不是复杂语法，而是把最基础的 CRUD 写得稳、写得清楚、写得知道后果。尤其是更新和删除，一旦没有条件约束，影响范围往往会立刻失控。</p>
<h2>表结构调整与约束设计：数据库不是建完就结束</h2>
<p>随着业务变化，表结构往往需要演进，因此 <code>ALTER TABLE</code> 相关能力非常重要。增加字段、删除字段、修改字段类型、调整约束、重命名列名、增加主键与唯一索引，这些操作构成了数据库演进的日常。</p>
<p>而外键与级联规则则进一步把“关系型数据库”这个概念落到实处。更新联动、删除联动、是否允许空引用，这些规则表面是表结构选项，实质上是在定义业务数据之间的关系边界。</p>
<h2>范式与关系设计：为什么表不能乱堆字段</h2>
<p>数据库设计中常见的一对一、一对多、多对多关系，以及第一范式、第二范式、第三范式这些概念，经常让人觉得抽象。但它们要解决的问题其实很具体：减少冗余、避免更新异常、保证结构清晰。</p>
<p>范式并不是为了考试而存在，而是在提醒你：同一份信息不应该无序地散落在多个地方；当一张表承担了过多不属于自己的职责，后续维护成本一定会上升。因此，理解范式的价值，不在于背定义，而在于学会判断“这个字段是不是放错地方了”。</p>
<h2>事务、连接查询与后续数据库能力的入口</h2>
<p>MySQL 的学习不会停留在单表操作。事务让你理解数据一致性，提交与回滚让你知道操作不是一执行就不可逆；连接查询让你看到多个表之间的信息是如何被组合出来的。到了这一层，数据库已经不再只是“存数据的地方”，而开始变成业务逻辑的重要组成部分。</p>
<p>笔记里还涉及自动提交、连接查询、条件控制、流程语句和索引等内容。这些部分共同构成了从“数据库入门”走向“能支撑实际后端开发”的路径。</p>
<h2>这篇内容适合怎样使用</h2>
<p>如果你正在学习后端，MySQL 是一门非常值得系统复盘的基础课。它既是数据存储能力的起点，也是后面 JDBC、MyBatis、事务管理和性能优化的前置知识。把这篇内容当作一张数据库能力地图去看，会比把它当成零散语法清单更有价值。</p>
<blockquote><p>我越来越觉得，数据库学习真正难的不是语句本身，而是你是否开始用“结构、约束和关系”去思考数据。</p></blockquote>'
WHERE title = 'MySQL 基础与常用 SQL 学习笔记整理';

UPDATE dyx_post
SET content = '<p>这篇文章基于 Redis 笔记整理，主线围绕“Redis 是什么、适合解决什么问题、核心数据结构怎么理解、Jedis 与 Spring Data Redis 如何接入、进一步还能延伸到哪些真实业务场景”展开。相比把 Redis 只当成一个缓存工具，我更希望把它放回到 NoSQL 与高频读写场景的大背景里理解。</p>
<h2>先别急着背命令，先知道 Redis 为什么会出现</h2>
<p>Redis 属于典型的 NoSQL 数据库，最核心的特点是内存存储、读写快、数据结构丰富。和传统关系型数据库相比，它更擅长处理高频访问、计数、缓存、临时状态、排行榜、会话数据等场景。也正因为此，学习 Redis 时最先要建立的认知不是“命令有哪些”，而是“哪些问题更适合 Redis 来做”。</p>
<p>当你先理解了 SQL 和 NoSQL 的差异，再去看 Redis 的设计，就会更容易明白它为什么大量围绕 key-value 和特定数据结构来组织操作。</p>
<h2>Key 设计不是小事，而是 Redis 建模的起点</h2>
<p>笔记里单独把 key 拎出来，其实很有代表性。Redis 虽然灵活，但这种灵活如果没有命名规范，很快就会变成混乱。一个合理的 key 命名方案，通常需要体现业务前缀、对象类型、主键标识，有时还要带上时间维度或状态维度。</p>
<p>因此，Redis 的建模并不只是“把数据塞进去”，而是从 key 设计那一刻就开始了。命名是否可读、是否可批量管理、是否容易设置过期、是否便于后续排查，这些都会直接影响系统维护成本。</p>
<h2>五大核心数据结构决定了 Redis 的使用方式</h2>
<p>真正理解 Redis，一定离不开字符串、哈希、列表、集合和有序集合。字符串适合简单缓存和计数器；哈希适合对象属性聚合；列表适合消息队列和时间序列式数据；集合适合去重与成员判断；有序集合则非常适合排行榜、权重排序等场景。</p>
<p>很多时候业务建模难点并不在“Redis 能不能做”，而在“当前问题更适合哪种数据结构”。一旦数据结构选对，很多操作天然就会变得更直接、更高效。</p>
<h2>Jedis：第一次直接操作 Redis 的入口</h2>
<p>在 Java 侧接入 Redis 时，Jedis 往往是最容易理解的起点。通过连接、认证、选择库以及直接调用字符串、哈希等命令 API，可以非常直观地感受到 Redis 是如何工作的。笔记中的示例也正体现了这一点：先连上 Redis，再执行 set/get、hset/hgetAll 之类的操作，把概念直接落到代码上。</p>
<p>Jedis 的价值在于入门清晰、操作直接，但当项目开始走向更标准的 Spring 体系时，往往会进一步过渡到 Spring Data Redis。</p>
<h2>Spring Data Redis：从“能连”到“能进入真实项目”</h2>
<p>Spring Data Redis 的优势在于更好地融入 Spring Boot 工程体系，尤其是连接配置、模板封装、序列化策略和统一访问方式。这里最值得重视的点是 <code>RedisTemplate</code>：它并不只是一个工具类，而是 Redis 在 Spring 体系中的主要访问入口。</p>
<p>与此同时，序列化策略也非常关键。笔记中提到 JDK 序列化、JSON 序列化以及 <code>GenericJackson2JsonRedisSerializer</code> 等方式，这直接关系到 Redis 中的数据是否可读、是否便于调试、是否适合跨服务协作。</p>
<h2>从缓存到分布式：Redis 的能力远不止“存一下数据”</h2>
<p>笔记后续还延伸到缓存问题、ID 方案、Lua、分布式锁、Redisson、Pub/Sub、Streams、GEO、Bitmap、HyperLogLog 等内容。这说明 Redis 在真实业务里并不只是“拿来缓存数据库查询结果”这么简单，它还会参与到高并发、去重、消息、地理位置、统计和分布式协作等更复杂的场景中。</p>
<p>当然，这也意味着使用 Redis 不能只停留在命令层面。什么时候会有缓存一致性问题、什么时候需要防止缓存穿透、什么时候要考虑锁和原子性，这些都属于后续真正值得深入的主题。</p>
<h2>这篇笔记更适合谁</h2>
<p>如果你已经有 Java 和数据库基础，正准备把项目性能、缓存和状态管理做得更像一个真实后端系统，这篇内容很适合作为 Redis 的第一轮系统复盘。它既能帮你建立对 Redis 数据结构的直觉，也能让你知道从 Jedis 到 Spring Data Redis，再到分布式场景，学习路径应该怎么展开。</p>
<blockquote><p>Redis 真正让我感兴趣的地方，不是“它很快”，而是它把数据结构和业务场景直接拉到了同一层思考里。</p></blockquote>'
WHERE title = 'Redis 入门到 Spring Data Redis 笔记整理';

UPDATE dyx_post
SET content = '<p>这篇文章整理自前端基础笔记，主题并不是展示多少标签，而是试着把 HTML 与 CSS 入门最容易碎片化的部分重新编排成一条更适合初学者复习的路线：先知道网页骨架是什么，再理解表单、媒体和链接，最后进入 CSS 选择器、文本样式和基础布局。</p>
<h2>HTML 首先解决的是“页面结构”问题</h2>
<p>刚接触前端时，最容易把 HTML 理解成“很多标签的集合”。但从学习角度看，HTML 更像是在描述页面结构：<code>html</code> 代表文档根节点，<code>head</code> 放元信息，<code>body</code> 放真正展示给用户的内容。只有先把这种结构意识建立起来，后续写标题、段落、图片、表单和列表时才不会觉得全是零散标签。</p>
<p>也正因为如此，HTML 入门的关键不是记住标签数量，而是知道一个页面为什么会有头部信息、正文区域、资源引用和语义结构。</p>
<h2>文本、链接与图片：网页内容组织的最基础单元</h2>
<p>网页的核心内容往往由标题、段落、换行、超链接和图片构成。学习 <code>h1-h6</code>、<code>p</code>、<code>br</code>、<code>a</code>、<code>img</code> 这些标签时，重要的不只是会写，而是理解它们各自的语义和使用场景。</p>
<p>例如超链接不仅仅是跳转，还涉及内部锚点、外部地址、资源引用和目标打开方式；图片标签除了路径，还涉及替代文本、尺寸、间距和展示含义。把这些基础点掌握好，网页内容才能既能看，又能用。</p>
<h2>表单是前端第一次真正接触“用户输入”</h2>
<p>表单部分是前端入门中非常重要的一块，因为从这里开始，页面不再只是展示内容，而开始接收用户输入。<code>form</code>、<code>input</code>、<code>textarea</code>、<code>select</code>、<code>button</code> 等元素组合在一起，构成了最基本的人机交互界面。</p>
<p>在这部分学习中，除了认识不同类型的输入控件外，也要逐步理解 <code>name</code>、<code>value</code>、<code>placeholder</code>、<code>required</code>、<code>maxlength</code>、<code>checked</code>、<code>multiple</code> 等属性的作用，因为这些属性决定了表单行为和提交数据的形态。</p>
<h2>CSS 真正让页面从“有内容”变成“能看”</h2>
<p>如果说 HTML 负责搭结构，那么 CSS 就负责让页面有样子。学习 CSS 时最重要的几件事通常是：选择器怎么选中元素、优先级怎么决定样式覆盖、盒模型怎样影响尺寸与间距、文本样式和背景边框如何塑造视觉效果。</p>
<p>初学阶段常见的痛点其实并不是“属性太多”，而是没有建立样式系统感。什么时候应该改字体，什么时候应该调间距，什么时候需要边框和背景，什么时候布局应该由容器控制，这些都需要在基础练习里慢慢建立。</p>
<h2>从静态页面到现代前端，基础并没有过时</h2>
<p>即使今天大多数项目都在使用 Vue、React 等框架，HTML 与 CSS 基础仍然没有过时。组件化开发只是把页面拆得更细，但最终仍然要落回标签语义、表单交互、图片展示、文本结构和样式布局。很多“框架问题”追到最后，其实仍然是基础结构或基础样式没有理解透。</p>
<p>因此，这份笔记的意义不是让人停留在传统静态页面阶段，而是帮助自己在进入组件化、响应式与工程化之前，把最根本的页面表达能力打牢。</p>
<h2>这篇内容适合作为怎样的复盘材料</h2>
<p>如果你正在从零开始学前端，或者已经开始接触 Vue 但仍觉得 HTML/CSS 基础不够稳，这篇内容很适合作为第一轮回顾资料。它能帮助你把原本零散的标签和样式知识，重新串成一条“结构 → 内容 → 输入 → 样式 → 布局”的学习路径。</p>
<blockquote><p>我越来越觉得，前端基础真正重要的不是记住多少标签，而是能不能把一个页面的结构和视觉表达讲清楚。</p></blockquote>'
WHERE title = '前端基础（HTML + CSS）学习笔记整理';

UPDATE dyx_post
SET content = '<p>这篇文章整理自 uni-app 学习笔记，重点不是单纯罗列组件名称，而是把 uni-app 在多端开发中的常见关注点按“页面结构、常用组件、样式适配、配置文件、交互细节和打包发布”重新组织成一篇更适合复盘的长文。</p>
<h2>uni-app 的价值首先在于“多端复用”</h2>
<p>接触 uni-app 时，最先建立的认知应该是它为什么存在：它试图让开发者用一套更接近 Vue 的方式，同时覆盖多个端的页面开发。这意味着学习 uni-app 不能只盯着语法写法，更要理解它在小程序、H5、App 之间做了哪些抽象。</p>
<p>也正因为如此，页面目录结构、配置文件组织和常用组件能力，都会比单一平台开发时更重要。</p>
<h2>页面与组件：从结构开始理解 uni-app</h2>
<p>uni-app 的页面开发通常围绕视图结构、滚动容器、轮播容器、图片、按钮、输入组件等展开。像 <code>scroll-view</code>、<code>swiper</code> 这样的组件之所以高频出现，是因为移动端页面天然就更依赖滚动和滑动交互。</p>
<p>因此，学习 uni-app 的组件时，不仅要知道组件名，更要知道它们在移动端页面中的职责：哪些适合承载长列表，哪些适合承载轮播区域，哪些适合构建表单和交互反馈。</p>
<h2>尺寸单位与样式适配，是多端开发绕不过去的一课</h2>
<p>笔记中提到 <code>rpx</code>、<code>upx</code> 等尺寸单位，这部分非常关键。多端开发最现实的问题之一，就是同一份页面代码如何在不同设备上尽量保持合理的视觉比例。理解这些尺寸单位的意义后，才能更自然地处理间距、字号、卡片尺寸和响应式布局。</p>
<p>同时，像 <code>box-sizing</code>、阴影、边框、滚动定位等样式能力，也在 uni-app 里承担着“把移动端界面做得可用又顺眼”的职责。</p>
<h2>配置文件、tabBar 与页面路由，决定工程是否清晰</h2>
<p>uni-app 的工程化认知很大一部分来自配置文件，尤其是 <code>pages.json</code>。页面注册、导航栏样式、tabBar 配置、图标路径和页面切换关系，都依赖这一层来统一描述。对于入门者来说，这一步非常像“项目地图”：只有先知道页面怎么注册、怎么跳转、怎么显示在 tabBar 中，后续业务功能才能顺利接起来。</p>
<p>因此，理解 tabBar 和页面配置，并不只是“会配 JSON”，而是在学习一个前端工程如何被组织。</p>
<h2>表单、token 与打包：开始接近真实项目场景</h2>
<p>笔记后半部分开始触及 uni-forms、表单绑定、token、安卓签名、<code>keytool</code>、打包配置等内容，这意味着学习已经从“页面怎么写”延伸到了“项目怎么交付”。这一步很重要，因为很多人会写页面，但一到环境配置、签名和发布就会卡住。</p>
<p>真正能落地的小程序或 App 项目，往往不仅要求页面可见，还要求登录状态、接口鉴权、表单校验、发布配置和图标资源都能配齐。这部分内容虽然琐碎，但恰恰构成了工程能力。</p>
<h2>如何利用这篇笔记</h2>
<p>如果你已经会一点 Vue，希望更快上手 uni-app，这篇内容适合作为“第一轮工程化复盘”。它不能替代完整项目实践，但能帮你先把关键模块的位置看清：页面结构、组件能力、尺寸适配、配置文件、交互行为和打包发布分别在系统里扮演什么角色。</p>
<blockquote><p>uni-app 最值得学的不只是“能多端”，而是它会逼着你更早开始用工程视角看待一个前端项目。</p></blockquote>'
WHERE title = 'uni-app 常用开发知识点整理';

UPDATE dyx_post
SET content = '<p>这篇文章整理自 Flutter 基础笔记，核心主线是：如何理解 Widget 思维、为什么状态会成为 Flutter 学习中的关键、常用列表和弹窗组件分别解决什么问题，以及这些基础组件如何共同构成一个可交互的页面。</p>
<h2>Flutter 的第一道门槛，其实是接受“一切皆 Widget”</h2>
<p>很多刚接触 Flutter 的同学都会有一种不适应感，因为它与传统前端、Android 原生或其他 UI 框架的思维方式不完全一致。在 Flutter 中，页面骨架、按钮、文本、布局、间距甚至状态展示，都被组织为 Widget。理解这一点后，很多看似分散的组件才会开始形成统一逻辑。</p>
<p>因此，学习 Flutter 的第一步并不是背 API，而是接受：UI 是通过一棵 Widget 树被描述出来的。</p>
<h2>Scaffold、Container、Row、Column：页面骨架为什么重要</h2>
<p>笔记中最前面的内容集中在 <code>Scaffold</code>、颜色、边距、容器、行列布局等组件上。这些看起来很基础，但它们实际上构成了页面结构搭建的核心。<code>Scaffold</code> 提供典型的 Material 页面骨架，<code>Container</code> 负责尺寸与装饰，<code>Row</code> 和 <code>Column</code> 负责线性布局，<code>EdgeInsets</code> 控制内外边距。</p>
<p>当这些组件关系理顺后，Flutter 页面就不再像一堆拼起来的部件，而更像一套可以层层嵌套、职责清晰的结构系统。</p>
<h2>状态管理入门：StatelessWidget 与 StatefulWidget 为什么如此关键</h2>
<p>Flutter 的学习真正开始变难，往往就是从状态开始。<code>StatelessWidget</code> 适合描述不依赖可变状态的界面，而 <code>StatefulWidget</code> 与对应的 <code>State</code> 则负责那些需要随着用户交互或数据变化而刷新的界面。</p>
<p>这部分最值得掌握的不只是类名区别，而是状态与 UI 刷新的关系：为什么调用 <code>setState()</code> 会触发重建，为什么生命周期函数会在不同阶段运行，为什么 <code>BuildContext</code> 在某些时机可用、某些时机又需要谨慎使用。只有这些问题想通，Flutter 的交互式页面开发才真正入门。</p>
<h2>生命周期不是为了记忆，而是为了知道“什么时候做什么事”</h2>
<p>笔记中对 <code>initState</code>、<code>didChangeDependencies</code>、<code>didUpdateWidget</code>、<code>deactivate</code>、<code>dispose</code> 等生命周期都有涉及。这些函数的价值不在于背顺序，而在于帮助你把初始化、依赖变化、组件更新和资源释放放在正确时机处理。</p>
<p>例如初始化控制器、监听器和异步请求通常适合在生命周期早期处理，而释放资源必须在销毁阶段完成。生命周期学明白后，很多“为什么页面重复构建”“为什么资源没释放”“为什么上下文取不到”的问题都会容易定位得多。</p>
<h2>列表、弹窗与交互：开始进入真实界面开发</h2>
<p>Flutter 的常见界面开发通常很快就会碰到列表和弹窗。<code>ListView.builder</code>、<code>ListView.separated</code>、<code>AlertDialog</code>、<code>SimpleDialog</code>、<code>Dialog</code>、<code>showDialog</code> 这些内容之所以重要，是因为它们几乎覆盖了最常见的信息展示与用户反馈场景。</p>
<p>与此同时，<code>GestureDetector</code>、按钮组件、文本输入框、表单、图片和头像组件，则共同组成了页面互动能力。也就是说，到了这一阶段，Flutter 已经不再只是“会布局”，而是开始具备真正的 UI 交互表达力。</p>
<h2>为什么这份笔记适合保留</h2>
<p>这份 Flutter 笔记最有价值的地方，在于它把组件、布局、状态、生命周期和交互放到了同一条学习线上。对于初学者来说，这比零散记 API 更重要，因为 Flutter 最难的往往不是某个组件本身，而是你能不能把页面结构、状态变化和用户操作放到同一个模型里理解。</p>
<p>如果你正从前端或原生移动开发转向 Flutter，这篇内容很适合作为第一轮系统梳理。它不能替代完整项目实践，但足够帮助你建立后续深入学习所需要的概念骨架。</p>
<blockquote><p>Flutter 最让我印象深的，不是组件数量，而是它把“界面”和“状态”之间的关系表达得非常直接。</p></blockquote>'
WHERE title = 'Flutter 基础组件与生命周期笔记整理';
