## 一、生产环境 Nginx 部署架构

### 1.1 架构图（文本版）
纯文本

```

┌─────────────────────────────────────┐
                           │           Nginx (80/443)            │
                           │                                     │
                           │  ┌─────────────┐  ┌──────────────┐ │
  HTTPS / http://ehr.xxx   │  │  location /  │  │ location     │ │
  ──────────────────────────┼─▶│  (主应用)    │  │ /vhr/       │ │
                            │  │ ehr-platform│  │ (子应用 vhr) │ │
                            │  │ dist/       │  │ vhr/dist/   │ │
                            │  └──────┬──────┘  └──────┬───────┘ │
                            │         │                │         │
                            │         ▼                ▼         │
                            │  ┌──────────────────────────────┐  │
                            │  │     try_files $uri $uri/     │  │
                            │  │     /index.html (SPA 兜底)   │  │
                            │  └──────────────────────────────┘  │
                            └─────────────────────────────────────┘

```
### 1.2 Nginx 完整配置文件
nginx

 /etc/nginx/conf.d/ehr.conf
```
upstream ehr_platform {
    server 127.0.0.1:8080;
}

upstream vhr_service {
    server 127.0.0.1:8081;
}

server {
    listen 80;
    server_name ehr.xxx.com;

    # 强制 HTTPS（可选）
    # return 301 https://$server_name$request_uri;
    
    # ===== 主应用（ehr-platform）=====
    location / {
        root /data/apps/ehr-platform/dist;
        index index.html;
        try_files $uri $uri/ /index.html;
    }
    
    # ===== 子应用 vhr（关键配置）=====
    location /vhr {
        alias /data/apps/vhr/dist;
        index index.html;
        try_files $uri $uri/ /vhr/index.html;
    }
    
    # ===== 子应用静态资源缓存 =====
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        root /data/apps;
        expires 30d;
        add_header Cache-Control "public, no-transform";
        access_log off;
    }
    
    # ===== API 反向代理（如果主子应用共用后端）=====
    location /api/ {
        proxy_pass http://backend_server/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    
    # ===== 安全头 =====
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    
    # ===== Gzip =====
    gzip on;
    gzip_min_length 1k;
    gzip_comp_level 6;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml application/xml+rss text/javascript;
    gzip_vary on;
}
```

### 1.3 子应用 Webpack 生产构建关键配置

子应用 vhr/build/webpack.prod.conf.js 中 output.publicPath 必须和 Nginx location 对应：
```
js
// vhr/build/webpack.prod.conf.js
const { merge } = require('webpack-merge');
const base = require('./webpack.base.conf');

module.exports = merge(base, {
  mode: 'production',
  output: {
    publicPath: '/vhr/',   // ← 必须和 Nginx location /vhr 对应
    filename: 'js/[name].[contenthash:8].js',
    chunkFilename: 'js/[name].[contenthash:8].chunk.js',
  },
  devtool: 'source-map',
});
```

### 1.4 主应用注册子应用 entry 改为相对路径
```
js
// ehr-platform/src/main.js（生产环境）
registerMicroApps([
  {
    name: 'vhr',
    entry: '/vhr/',          // ← 生产环境用相对路径，不再是 localhost:8081
    container: '#subapp-viewport',
    activeRule: '/vhr',
    props: { base: '/vhr' },
  },
]);
```