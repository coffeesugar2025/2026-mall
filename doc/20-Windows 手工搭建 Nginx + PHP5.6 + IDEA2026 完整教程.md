

# Windows 手工搭建 Nginx + PHP5.6 + IDEA2026 完整教程

1，PHP5.6
https://downloads.php.net/~windows/releases/archives/php-5.6.40-Win32-VC11-x64.zip

2，Nginx Windows 稳定版

https://nginx.org/download/nginx-1.30.3.zip

3，Xdebug

https://xdebug.org/files/php_xdebug-2.5.4-5.6-vc11-x86_64.dll


4,
https://downloads.marketplace.jetbrains.com/files/6610/1086343/php-impl-261.26222.22.zip?updateId=1086343&pluginId=6610&family=INTELLIJ



全程纯手动，无集成面板，路径统一英文无空格，兼容 Windows10/11
## 一、前置依赖（必装，否则PHP直接报错）
PHP5.6 Windows 编译基于 **VC11 (VS2012)**，必须安装运行库
1. 下载：Visual C++ Redistributable 2012 Update 4 x64
2. 两个文件都装：vcredist_x64.exe、vcredist_x86.exe
3. 安装完成重启电脑

## 二、下载资源（全部64位）
### 1. PHP5.6（Nginx 必须选 NTS 非线程安全版）
归档地址：https://windows.php.net/downloads/releases/archives/
文件名：`php-5.6.40-Win32-VC11-x64-nts.zip`
解压路径：`D:\server\php56`

### 2. Nginx Windows 稳定版
推荐 nginx-1.20.2（新版也能用，配置通用）
解压路径：`D:\server\nginx`

### 3. Xdebug 对应版本
PHP5.6 NTS x64 VC11
下载 `php_xdebug-2.5.5-5.6-vc11-x86_64.dll`
放入 `D:\server\php56\ext\`

## 三、PHP5.6 初始化配置
1. 进入 `D:\server\php56`，复制 `php.ini-development` → 重命名 `php.ini`
2. 编辑 php.ini，修改以下内容
```ini
; 1. 扩展目录
extension_dir = "D:/server/php56/ext"

; 2. 开启常用扩展
extension=php_mbstring.dll
extension=php_mysqli.dll
extension=php_pdo_mysql.dll
extension=php_gd2.dll
extension=php_curl.dll
extension=php_openssl.dll

; 3. 时区
date.timezone = Asia/Shanghai

; 4. 开发环境显示错误
display_errors = On
error_reporting = E_ALL

; 5. 短标签（老项目兼容）
short_open_tag = On

; 底部添加Xdebug配置
[Xdebug]
zend_extension=php_xdebug-2.5.5-5.6-vc11-x86_64.dll
xdebug.remote_enable = 1
xdebug.remote_host = 127.0.0.1
xdebug.remote_port = 9000
xdebug.idekey = IDEA
xdebug.remote_autostart = 1
```
3. 配置系统环境变量
此电脑→属性→高级系统设置→环境变量→系统变量 Path 新增：
`D:\server\php56`
4. 新开 CMD 验证
```cmd
php -v
php -m | findstr xdebug
```
输出版本+出现xdebug即成功

## 四、配置 Nginx + PHP-FPM（Windows php-cgi）
Windows 无真正 php-fpm，使用 `php-cgi.exe` 进程转发，两种方式：
### 方式A：bat脚本启动php-cgi（简单，推荐开发）
新建 `D:\server\php56\start_fpm.bat`
```bat
@echo off
echo 启动PHP5.6 CGI 9000端口
D:\server\php56\php-cgi.exe -b 127.0.0.1:9000 -c D:\server\php56\php.ini
pause
```
双击运行即可启动PHP解析服务，不要关闭窗口

### 方式B：使用RunHiddenConsole后台静默运行（可选）
下载 RunHiddenConsole.exe 放入nginx目录，编写启动脚本后台运行

### 修改 Nginx 配置 nginx/conf/nginx.conf
清空原有内容，替换为开发通用配置
```nginx
worker_processes  1;
events {
    worker_connections  1024;
}
http {
    include       mime.types;
    default_type  application/octet-stream;
    sendfile        on;
    keepalive_timeout  65;

    server {
        listen       80;
        server_name  localhost;
        # 项目根目录，改成你的项目路径
        root   D:/work/php_project;
        index  index.html index.htm index.php;

        # 伪静态（ThinkPHP/CI等框架必备）
        location / {
            if (!-e $request_filename) {
                rewrite ^(.*)$ /index.php?s=$1 last;
                break;
            }
        }

        # PHP转发核心配置
        location ~ \.php$ {
            fastcgi_pass   127.0.0.1:9000;
            fastcgi_index  index.php;
            fastcgi_param  SCRIPT_FILENAME  $document_root$fastcgi_script_name;
            include        fastcgi_params;
        }

        # 禁止访问 .htaccess
        location ~ /\.ht {
            deny  all;
        }
    }
}
```

## 五、Nginx 启停脚本
新建 `D:\server\nginx\start.bat`
```bat
@echo off
cd /d D:\server\nginx
nginx.exe
echo Nginx 已启动
pause
```
新建 `stop.bat`
```bat
@echo off
cd /d D:\server\nginx
nginx.exe -s stop
echo Nginx 已停止
pause
```

### 启动顺序（必须遵守）
1. 双击 `php56/start_fpm.bat` 启动cgi（保持窗口打开）
2. 双击 `nginx/start.bat` 启动nginx
3. 新建项目目录 `D:\work/php_project`，新建 index.php
```php
<?php
phpinfo();
```
浏览器访问 http://localhost 看到phpinfo代表web环境通了

## 六、IDEA2026 完整配置PHP
### 1. 安装PHP插件
File → Settings → Plugins → Marketplace 搜索 PHP 安装，重启IDEA
> 社区版无PHP插件，必须 Ultimate 旗舰版

### 2. 配置PHP解释器
Ctrl+Alt+S → Languages & Frameworks → PHP
1. CLI Interpreter 右侧点 ... → + → Local
2. PHP executable 选择 `D:\server\php56\php.exe`
3. Language Level 选择 **PHP 5.6**
4. 下方Debugger Extension自动识别xdebug，端口9000，保存

### 3. 配置Web服务调试
1. 同页面切换到 Servers → + 新建
- Name：Local Nginx
- Host：localhost
- Port：80
- Document root：`D:\work\php_project`
- 勾选 Use path mappings，映射本地目录

### 4. 创建运行配置（断点调试）
Run → Edit Configurations → + → PHP Web Page
- Server：Local Nginx
- URL：http://localhost
- Browser：Chrome
打上断点，点击右上角虫子图标即可调试

## 七、常用操作与排坑
### 1. 重启流程
1. 关闭php-cgi黑窗口，重新运行start_fpm.bat
2. 执行nginx stop脚本，再start

### 2. 常见报错
1. php-cgi闪退
   - 检查VC11运行库是否安装、PHP是NTS版本、php.ini路径无中文
2. 访问页面下载php文件，不解析
   - php-cgi未启动，或fastcgi_pass端口不匹配
3. Xdebug断点不生效
   - 确认xdebug.dll版本匹配NTS、端口9000、xdebug.remote_autostart=1
4. Nginx 403 Forbidden
   - 检查root目录路径斜杠用 `/`，不要反斜杠 `\`，目录权限放开
5. MySQL连接报错
   - PHP5.6废弃原生mysql扩展，使用mysqli/pdo_mysql，确认扩展已开启

## 八、环境结构总览
```
D:\server
├─ php56            # PHP5.6 NTS
│  ├─ ext
│  │  └─ php_xdebug.dll
│  ├─ php.ini
│  └─ start_fpm.bat
└─ nginx
   ├─ conf/nginx.conf
   ├─ start.bat
   └─ stop.bat

D:\work\php_project  # 项目代码目录
```