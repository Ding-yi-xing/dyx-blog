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
1. 参考根目录下的 `nginx.conf` 修改 `/etc/nginx/sites-available/default` 或新建配置文件。
2. 确保 `root` 路径指向您的 `dist` 目录。
3. 重启 Nginx：
   ```bash
   sudo nginx -t
   sudo systemctl restart nginx
   ```

---

## 4. 安全建议 (重要)

1. **防火墙配置**：仅对外开放 80 (HTTP) 和 443 (HTTPS) 端口。后端 8080 端口应仅由 Nginx 内部访问，不要对外直接暴露。
2. **HTTPS**：强烈建议安装 SSL 证书（推荐使用 Certbot / Let's Encrypt），将站点升级为 HTTPS。
3. **敏感信息保护**：生产环境下的 `JWT_SECRET` 和 `ENCRYPT_KEY` 请务必使用强随机字符串，并妥善保管。
4. **数据库备份**：定期导出数据库备份。

---

## 5. 常见问题排除

- **静态资源加载失败**：检查 `uploads` 目录权限，确保运行后端 Java 服务的用户有读写权限。
- **接口 404**：检查 Nginx 代理路径是否匹配，特别是后端控制器的 RequestMapping 前缀。
- **刷新页面 404**：检查 Nginx 配置文件中是否包含 `try_files $uri $uri/ /index.html;`。
