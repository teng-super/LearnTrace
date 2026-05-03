# LearnTrace

LearnTrace 是一个面向程序员学习和音视频求职准备的全栈学习闭环系统：学习目标、任务打卡、错误记录、间隔复习、数据统计和笔记沉淀都放在同一个工作台里。

## 功能
- 用户注册、登录、JWT 鉴权、个人资料和密码修改。
- 目标 CRUD、状态切换、进度统计、阶段计划和模板导入。
- 八周学习计划、学校三科、三条音视频求职赛道模板。
- 任务 CRUD、今日任务、未完成任务、完成/取消完成。
- 错误记录 CRUD、Markdown、代码块、标签、筛选和间隔复习。
- 统计面板：目标、任务、错误、复习、趋势、错误类型、错误标签。
- 笔记模块：导入 Markdown/PDF/Word，Markdown 编辑，PDF 预览批注和摘录，DOCX 通过 ONLYOFFICE 原格式编辑。
- 4 套主题：作战夜航、专业日光、护眼纸张、代码终端。

## 技术栈
- 前端：Vue 3、Vite、TypeScript、Pinia、Vue Router、Element Plus、Axios、ECharts、md-editor-v3、PDF 预览。
- 后端：Spring Boot 3、Java 17、MyBatis Plus、Spring Security、JWT、Validation、MySQL 8。
- 文档：ONLYOFFICE Document Server。
- 部署：Docker Compose。

## 项目结构
```text
D:\LearnTrace
  backend/      Spring Boot 后端
  frontend/     Vue 3 前端
  db/           MySQL 建表和模板初始化脚本
  data/         上传文件、导出文件、MySQL 数据卷
```

## 默认账号
```text
用户名：demo
密码：learntrace123
```

## 本地数据库
1. 创建 MySQL 8 数据库 `learntrace`。
2. 执行 `db/01_schema.sql`。
3. 执行 `db/02_seed_templates.sql`。
4. 后端启动后会自动创建 demo 账号，并导入默认计划和示例错误记录。

## 后端启动
如果本机安装了 Maven：
```powershell
cd D:\LearnTrace\backend
mvn spring-boot:run
```

本机未安装 Maven 时，推荐用 Docker Compose，或安装 Maven 后再启动。配置项在 `backend/src/main/resources/application.yml`，默认连接：
```text
jdbc:mysql://localhost:3306/learntrace
用户：learntrace
密码：learntrace123
```

## 前端启动
```powershell
cd D:\LearnTrace\frontend
npm.cmd install
npm.cmd run dev
```

访问：
```text
http://localhost:5173
```

## Docker Compose
安装 Docker Desktop 后：
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

## 后续优化
- 增加 Docx 到 Markdown 的正文提取质量。
- PDF 批注增加坐标选择和页面缩略图。
- 加入计划向导，根据目标日期自动排周计划。
- 增加更多面试题和项目复盘模板。
- 接入真实邮件提醒或桌面通知。
