-- 基础用户表
CREATE TABLE IF NOT EXISTS dyx_user (
    id BIGINT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    nickname VARCHAR(50),
    avatar_url VARCHAR(255),
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 文章表
CREATE TABLE IF NOT EXISTS dyx_post (
    id BIGINT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    summary TEXT,
    content LONGTEXT,
    cover_image VARCHAR(255),
    view_count INT NOT NULL DEFAULT 0,
    published TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_post_published (published)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 动态表
CREATE TABLE IF NOT EXISTS dyx_moment (
    id BIGINT PRIMARY KEY,
    content TEXT,
    cover_image VARCHAR(255),
    image_urls TEXT,
    happened_at DATETIME,
    published TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_moment_published (published),
    INDEX idx_moment_happened_at (happened_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 项目经历表
CREATE TABLE IF NOT EXISTS dyx_project (
    id BIGINT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    role VARCHAR(100),
    technologies VARCHAR(255),
    link VARCHAR(255),
    sort_order INT NOT NULL DEFAULT 0,
    published TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_project_published (published),
    INDEX idx_project_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 个人作品表
CREATE TABLE IF NOT EXISTS dyx_work (
    id BIGINT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    cover_image VARCHAR(255),
    link VARCHAR(255),
    sort_order INT NOT NULL DEFAULT 0,
    published TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_work_published (published),
    INDEX idx_work_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 荣誉表
CREATE TABLE IF NOT EXISTS dyx_honor (
    id BIGINT PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    issuer VARCHAR(100),
    award_at DATETIME,
    sort_order INT NOT NULL DEFAULT 0,
    published TINYINT NOT NULL DEFAULT 0,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_honor_published (published),
    INDEX idx_honor_award_at (award_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 首页足迹表
CREATE TABLE IF NOT EXISTS dyx_footprint (
    id BIGINT PRIMARY KEY,
    city_name VARCHAR(100) NOT NULL,
    country_name VARCHAR(100),
    region_name VARCHAR(100),
    position_x DECIMAL(10,6) NOT NULL DEFAULT 0,
    position_y DECIMAL(10,6) NOT NULL DEFAULT 0,
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 留言表
CREATE TABLE IF NOT EXISTS dyx_guestbook_message (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    content TEXT NOT NULL,
    published TINYINT NOT NULL DEFAULT 0,
    ip_address VARCHAR(45) NOT NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    INDEX idx_guestbook_message_published (published),
    INDEX idx_guestbook_message_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 访问统计表
CREATE TABLE IF NOT EXISTS dyx_site_visit_stat (
    page_key VARCHAR(64) PRIMARY KEY,
    visit_count BIGINT NOT NULL DEFAULT 0,
    updated_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 访问日志表
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 个人资料与配置表
CREATE TABLE IF NOT EXISTS dyx_profile (
    id BIGINT PRIMARY KEY,
    site_title VARCHAR(100),
    hero_title VARCHAR(100),
    hero_subtitle VARCHAR(200),
    hero_config TEXT,
    github_url VARCHAR(255),
    contact_methods LONGTEXT,
    resume_pdf_url VARCHAR(255),
    guestbook_intro TEXT,
    updated_at DATETIME NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 系统配置表
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
