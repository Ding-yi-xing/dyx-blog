# dyx-blog 项目部署指南 (云服务器版)

本指南适用于 Linux (如 Ubuntu 20.04+, CentOS 7+) 云服务器。

## 1. 准备环境

### 1.1 安装 JDK 17
后端基于 Spring Boot 3，必须使用 Java 17。
```bash
# 以 Ubuntu 为例
sudo apt update
sudo apt install openjdk-17-jdk -y
# 验证安装
java -version
```

### 1.2 安装 MySQL 8.0+
```bash
sudo apt install mysql-server -y
# 登录并初始化
sudo mysql
# 创建数据库
CREATE DATABASE dyx_blog DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
# 创建用户并授权（根据实际情况调整）
CREATE USER 'dyx_user'@'%' IDENTIFIED BY '123456';
GRANT ALL PRIVILEGES ON dyx_blog.* TO 'dyx_user'@'%';
FLUSH PRIVILEGES;
```
**重要**：请务必导入 `backend/src/main/resources/sql/dyx-blog-init.sql` 中的数据。

### 1.3 安装 Nginx
用于托管前端静态文件并作为反向代理。
```bash
sudo apt install nginx -y
```

---

## 2. 后端部署 (Spring Boot)

### 2.1 打包
在本地开发环境下，进入 `backend` 目录执行打包：
```bash
mvn clean package -DskipTests
```
打包成功后，在 `target` 目录下会生成 `dyx-blog-1.0.0.jar`。

### 2.2 上传并启动
将 `jar` 包上传至服务器目录（如 `/home/dyx/backend`），同时在该目录下创建 `uploads` 文件夹用于存放图片。

**推荐使用环境变量启动（更安全）：**
```bash
# 在 jar 包所在目录执行
export MYSQL_HOST=localhost
export MYSQL_PORT=3306
export MYSQL_USER=dyx_user
export MYSQL_PWD=你的数据库密码
export JWT_SECRET=生成一个至少32位的随机字符串
export DYX_ENCRYPT_KEY=生成一个32位的AES密钥
export SPRING_PROFILES_ACTIVE=prod

# Redis 连接配置（如未配置则按 application.yml 默认值连接本地 Redis）
export SPRING_DATA_REDIS_HOST=127.0.0.1
export SPRING_DATA_REDIS_PORT=6379

nohup java -jar dyx-blog-1.0.0.jar > logs/stdout.log 2>&1 &
```

如需更复杂的 Redis 集群/哨兵配置，可在外部 `application-prod.yml` 中配置 Spring Data Redis 相关参数，Spring Cache 将自动使用该连接。

### 2.3 Redis 访问统计与限流配置
后端现已将部分站点防护逻辑建立在 Redis 之上，用于降低公开接口对 MySQL 的瞬时写压力，并在多实例部署下保持限流一致性。

当前涉及两类能力：
1. **访问统计聚合**：公开访问仍会写入 `dyx_site_visit_log`，但 `dyx_site_visit_stat` 不再逐请求直接累加，而是先写入 Redis，再由定时任务批量刷回 MySQL。
2. **分布式限流**：
   - `POST /api/site/guestbook/messages` 超限时返回 `429`
   - `POST /api/site/visit/{pageKey}` 超限时静默丢弃，不影响前台页面展示

默认配置位于：
- `backend/src/main/resources/application.yml`
- `backend/src/main/resources/application-prod.yml`

关键配置项示例：
```yml
dyx:
  site:
    visit-stat:
      pending-key: dyx:site:visit:stat:pending
      flush-key-prefix: "dyx:site:visit:stat:flush:"
      flush-index-key: dyx:site:visit:stat:flush:index
      flush-interval-ms: 60000
    rate-limit:
      guestbook:
        enabled: true
        limit: 5
        window-seconds: 300
      visit:
        enabled: true
        limit: 60
        window-seconds: 60
```

注意事项：
- `flush-key-prefix` 末尾包含 `:`，YAML 中必须加引号，否则 Spring Boot 启动时会报配置解析错误
- 定时刷库任务在 `pending` key 不存在时会自动跳过，不会因为空 key 导致调度线程报错

本地已验证的行为：
- `POST /api/site/visit/{pageKey}` 可正常返回 200，并保留 `dyx_site_visit_log` 写入
- `POST /api/site/guestbook/messages` 在默认阈值下第 6 次请求返回 `429`
- 访问统计会先进入 Redis 聚合，再由定时任务刷回 `dyx_site_visit_stat`

生产环境要求：
- Redis 必须可用，否则访问统计聚合与分布式限流无法正常工作
- 建议为 Redis 设置持久化与基础监控，避免重启或故障期间丢失待刷新的统计增量
- 如部署为多实例，所有实例必须连接同一 Redis 集群或主节点

---

## 3. 前端部署 (Vue 3)

### 3.1 打包
在本地开发环境下，进入 `frontend` 目录执行打包：
```bash
npm install
npm run build
```
打包成功后，会生成 `dist` 目录。

### 3.2 上传并配置 Nginx
将 `dist` 目录内容上传至服务器（如 `/var/www/dyx-blog/html`）。

**配置 Nginx：**
1. 宝塔 / OpenResty 场景请参考根目录下的 `docs/nginx.conf`，该文件适用于 `conf.d` 站点文件，**不要再包一层 `http {}`**。
2. 如果服务器上已经有可用站点配置，优先按“合并”方式接入限流与代理规则，不要直接覆盖已有 `server_name`、`ssl_certificate`、`root`、`proxy_pass` 等环境相关配置。
3. 确保 `root` 路径、证书路径、域名与后端端口按实际环境调整。
4. 重载前务必先执行配置校验：
   ```bash
   sudo nginx -t
   sudo systemctl reload nginx
   ```

---

## 4. 安全建议 (重要)

1. **防火墙配置**：仅对外开放 80 (HTTP) 和 443 (HTTPS) 端口。后端 8080 端口应仅由 Nginx 内部访问，不要对外直接暴露。
2. **HTTPS**：强烈建议安装 SSL 证书（推荐使用 Certbot / Let's Encrypt），将站点升级为 HTTPS。
3. **敏感信息保护**：生产环境下的 `JWT_SECRET` 和 `ENCRYPT_KEY` 请务必使用强随机字符串，并妥善保管。
4. **Redis 可用性**：由于访问统计聚合和分布式限流依赖 Redis，建议启用 Redis 持久化、监控与主从/哨兵等高可用能力。
5. **数据库备份**：定期导出数据库备份。

---

## 5. 常见问题排除

- **静态资源加载失败**：检查 `uploads` 目录权限，确保运行后端 Java 服务的用户有读写权限。
- **接口 404**：检查 Nginx 代理路径是否匹配，特别是后端控制器的 RequestMapping 前缀。
- **刷新页面 404**：检查 Nginx 配置文件中是否包含 `try_files $uri $uri/ /index.html;`。
