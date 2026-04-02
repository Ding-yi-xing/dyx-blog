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
