# LearnTrace

LearnTrace 是一个面向程序员学习和音视频求职准备的全栈学习闭环系统：目标规划、任务打卡、错误记录、间隔复习、数据统计和笔记沉淀都放在同一个工作台里。

## 功能

- 用户注册、登录、JWT 鉴权、个人资料和密码修改。
- 目标 CRUD、状态切换、进度统计、阶段计划、阶段复盘和模板导入。
- 八周学习计划、学校三科、客户端播放器/SDK、流媒体服务端、RTC/WebRTC 求职赛道模板。
- 任务 CRUD、今日任务、未完成任务、按周筛选、完成/取消完成。
- 错误记录 CRUD、Markdown、代码高亮、标签、搜索筛选和间隔复习。
- 学习驾驶舱：今日任务、待复习错误、主线目标、薄弱标签、趋势图和日历提醒。
- 笔记模块：导入 Markdown/PDF/Word，Markdown 编辑保存，PDF 预览批注和摘录，Word 预留 ONLYOFFICE 在线编辑。
- 4 套主题：作战夜航、专业日光、护眼纸张、代码终端。

## 技术栈

- 前端：Vue 3、Vite、TypeScript、Pinia、Vue Router、Element Plus、Axios、ECharts、md-editor-v3、PDF.js。
- 后端：Spring Boot 3、Java 17、MyBatis Plus、Spring Security、JWT、Validation、MySQL 8。
- 数据库：MySQL 8，提供 SQL 建表和模板初始化脚本。
- 文档服务：ONLYOFFICE 通过 `ONLYOFFICE_URL` 预留远程服务地址，本地首阶段不强制安装。

## 项目结构

```text
D:\LearnTrace
  backend\      Spring Boot 后端
  frontend\     Vue 3 前端
  db\           MySQL 建表和模板初始化脚本
  scripts\      本地免 Docker 启停脚本
  data\         MySQL 数据、上传文件、导出文件
  logs\         本地服务日志
  .tools\       D 盘便携工具目录
```

## 默认账号

```text
用户名：demo
密码：learntrace123
```

## 本地免 Docker 运行

这个方式不会安装 Docker Desktop，也不会用 winget。便携 MySQL、下载缓存、数据目录和日志都放在 `D:\LearnTrace` 内。

第一次配置：

```powershell
cd D:\LearnTrace
.\scripts\setup-mysql.ps1
.\scripts\start-mysql.ps1
.\scripts\start-backend.ps1
.\scripts\start-frontend.ps1
```

打开页面：

```text
http://localhost:5173
```

停止本地服务：

```powershell
cd D:\LearnTrace
.\scripts\stop-local.ps1
```

本地目录约定：

```text
MySQL 程序：优先使用 LEARNTRACE_MYSQL_HOME，也会读取 D:\LearnTrace\.tools\mysql-home.txt
MySQL 数据：D:\LearnTrace\data\mysql
上传文件：D:\LearnTrace\data\uploads
导出文件：D:\LearnTrace\data\exports
日志文件：D:\LearnTrace\logs
```

如果你的 MySQL 程序目录后续移动了，只要在 PowerShell 里先设置：

```powershell
$env:LEARNTRACE_MYSQL_HOME="D:\你的MySQL目录"
```

目录下需要有 `bin\mysqld.exe`。

ONLYOFFICE 首阶段不在本机安装。Word 文件可以导入、归档、关联目标/任务/错误；如果后续有远程 ONLYOFFICE Docs 地址，在 `.env.local` 写入即可：

```text
ONLYOFFICE_URL=https://your-onlyoffice-server
```

## 常规本地启动

如果你已经自己安装了 MySQL 8、Maven 和 Node，也可以手动启动：

```powershell
cd D:\LearnTrace\backend
mvn spring-boot:run
```

```powershell
cd D:\LearnTrace\frontend
npm.cmd install
npm.cmd run dev
```

默认数据库连接：

```text
jdbc:mysql://localhost:3306/learntrace
用户：learntrace
密码：learntrace123
```

## Docker Compose

当前优先使用免 Docker 本地运行。后续如果需要 Docker Desktop，可以执行：

```powershell
cd D:\LearnTrace
docker compose up --build
```

服务端口：

- 前端：http://localhost:5173
- 后端：http://localhost:8080
- MySQL：localhost:3306
- ONLYOFFICE：http://localhost:8088

## API 摘要

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`
- `GET/POST/PUT/DELETE /api/goals`
- `GET /api/goal-templates`
- `POST /api/goals/import-template`
- `POST /api/tasks/import-default-plan`
- `GET/POST/PUT/DELETE /api/tasks`
- `GET/POST/PUT/DELETE /api/errors`
- `GET /api/reviews/today`
- `POST /api/reviews/{errorId}`
- `GET/POST/DELETE /api/tags`
- `GET /api/statistics/overview`
- `GET/POST/PUT/DELETE /api/notes`
- `POST /api/notes/import`
- `POST /api/files/upload`
- `GET /api/documents/{id}/editor-config`
- `GET/POST /api/pdfs/{id}/annotations`

## 后续推送 GitHub

本地环境确认可用之后再推送。推送前确认 `.gitignore` 已排除：

- `frontend/node_modules`
- `frontend/dist`
- `backend/target`
- `.tools`
- `data/mysql`
- `data/uploads`
- `data/exports`
- 日志文件

## 后续优化

- 提升 DOCX 到 Markdown 的正文提取质量。
- PDF 批注增加坐标选择、页面缩略图和批量摘录。
- 加入计划向导，根据目标日期自动排周计划。
- 增加更多音视频面试题和项目复盘模板。
- 接入真实邮件提醒或桌面通知。
