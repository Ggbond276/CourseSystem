# 组员 B2 协作指南 — 作业板块·学生端 + 作业评论

> 📌 本文件供组员 B2 使用，负责【作业板块·学生端】和【作业评论】功能开发。
> **下方有一段【Cursor 提示词】**，请完整复制后粘贴到 Cursor 对话框，Cursor 会自动进入"仅限本板块开发"的协作模式。

---

## 一、我的负责范围

### B2 负责的功能清单

| 编号 | 功能 | 说明 |
|------|------|------|
| B2-1 | 学生作业列表 | 学生查看自己加入的课程下的所有作业 |
| B2-2 | 学生作业详情 | 学生查看某作业的详细内容和自己提交状态 |
| B2-3 | 学生提交作业 | 学生作答并提交作业，支持附件 |
| B2-4 | 作业评论列表 | 师生共同查看某作业下的讨论 |
| B2-5 | 发表评论 | 师生在作业下发表讨论（支持匿名） |

### B2 负责的前端文件

| 文件路径 | 说明 |
|----------|------|
| `course_frontend/course_frontend/src/views/student/StudentHomework.vue` | 学生作业列表页面 |
| `course_frontend/course_frontend/src/views/student/StudentHomeworkDetail.vue` | 学生作业详情 + 提交页面 |
| `course_frontend/course_frontend/src/api/homework.js` | 学生端作业相关 API（`listByStudent`、`getDetail`、`submit`） |
| `course_frontend/course_frontend/src/api/comment.js` | 作业评论 API（列表 + 发表） |
| `course_frontend/course_frontend/src/api/index.js` | API 统一导出（仅当需要导出新 API 时） |

### B2 负责的后端文件

| 文件路径 | 说明 |
|----------|------|
| `server/server/src/main/java/com/coursemanager/controller/HomeworkStudentController.java` | 学生端作业控制器 |
| `server/server/src/main/java/com/coursemanager/controller/HomeworkCommentController.java` | 作业评论控制器 |
| `server/server/src/main/java/com/coursemanager/service/IStudentHomeworkService.java` | 学生端作业业务接口 |
| `server/server/src/main/java/com/coursemanager/service/impl/StudentHomeworkServiceImpl.java` | 学生端作业业务实现 |
| `server/server/src/main/java/com/coursemanager/service/IHomeworkCommentService.java` | 作业评论业务接口 |
| `server/server/src/main/java/com/coursemanager/service/impl/HomeworkCommentServiceImpl.java` | 作业评论业务实现 |
| `server/server/src/main/java/com/coursemanager/mapper/HomeworkMapper.java` | 作业数据访问（复用） |
| `server/server/src/main/java/com/coursemanager/mapper/HomeworkSubmitMapper.java` | 提交记录数据访问 |
| `server/server/src/main/java/com/coursemanager/mapper/HomeworkCommentMapper.java` | 评论数据访问 |
| `server/server/src/main/java/com/coursemanager/pojo/Homework.java` | 作业实体（复用） |
| `server/server/src/main/java/com/coursemanager/pojo/HomeworkSubmit.java` | 提交记录实体 |
| `server/server/src/main/java/com/coursemanager/pojo/HomeworkComment.java` | 评论实体 |
| `server/server/src/main/resources/com/coursemanager/mapper/HomeworkMapper.xml` | 作业 SQL 映射（复用） |
| `server/server/src/main/resources/com/coursemanager/mapper/HomeworkSubmitMapper.xml` | 提交 SQL 映射 |

### 与 B1 的接口边界（重点）

- `HomeworkMapper`、`HomeworkMapper.xml` 由 **B1 创建基础结构**，B2 可以复用查询方法，但**不得修改**已有的查询 SQL（只增不减）
- `IStudentHomeworkService` 和 `HomeworkStudentController` 归属 **B2**，B1 不碰
- `HomeworkCommentController` 和 `HomeworkCommentService` 归属 **B2**，B1 不碰（但 B1 前端页面可以调用评论接口）
- 教师发布作业、批阅打分归属 **B1**，B2 不碰

---

## 二、每日开发流程

```bash
# 1. 开工前：拉取最新代码
git switch dev
git pull origin dev

# 2. 切换到自己的分支（首次创建）
git switch -c feature/homework-b2-lisi
# 后续每天：
# git switch feature/homework-b2-lisi

# 3. 写代码...（只改白名单内的文件）

# 4. 收工提交
git add .
git commit -m "【作业-学生端】完成作业列表查询接口"
git push -u origin feature/homework-b2-lisi
# 后续推送：
# git push
```

---

## 三、提交 PR 前自检清单

- [ ] 代码中不存在 `? :` 三元运算符
- [ ] 不存在缩写命名（如 `getHL`），全为完整英文单词
- [ ] 所有 Controller 只调用 Service，无业务逻辑
- [ ] 前端 View 通过 `api/homework.js` 或 `api/comment.js` 发起请求，无直接 Axios 调用
- [ ] 未修改任何黑名单文件
- [ ] `git pull origin dev` 无冲突
- [ ] 提交信息格式：`【模块名】完成功能简述`

---

## 四、【Cursor 提示词】— 完整复制下方代码块到 Cursor 对话框

````markdown
# 角色定位
你是本项目的"首席代码架构师"，正在协助组员 B2 开发【作业板块·学生端 + 作业评论】模块。
组员 B2 负责：学生作业列表、学生作业详情、学生提交作业、作业评论列表、发表评论。
对应的前端文件：`views/student/StudentHomework.vue`、`views/student/StudentHomeworkDetail.vue`、`api/homework.js`（学生端接口）、`api/comment.js`。
对应的后端文件：`controller/HomeworkStudentController`、`controller/HomeworkCommentController`、`service/IStudentHomeworkService`、`service/impl/StudentHomeworkServiceImpl`、`service/IHomeworkCommentService`、`service/impl/HomeworkCommentServiceImpl` 及对应 Mapper/POJO。

# 核心价值观
- 可读性 > 炫技：禁止生僻语法、复杂正则、超过两层的三元运算符嵌套
- 逻辑显性化：for 循环、if-else 判断必须完整清晰展示
- 变量函数命名必须是完整英文单词拼接（如 `getHomeworkList`），禁止缩写（如 `getHL`）
- 每一个复杂业务逻辑块、每一个 API 接口都必须带上清晰的中文注释

# 架构铁律
- Controller 只能接参和校验，严禁写业务逻辑，业务全交给 Service
- 前端 View 绝对不准直接写 Axios 请求，必须调用 `api/homework.js` 或 `api/comment.js` 目录下的函数
- 每个文件、每个函数的体积必须小巧，职责必须单一

# B2 白名单（仅限以下文件）
## 前端
- `course_frontend/course_frontend/src/views/student/StudentHomework.vue`
- `course_frontend/course_frontend/src/views/student/StudentHomeworkDetail.vue`
- `course_frontend/course_frontend/src/api/homework.js`（仅添加 B2 相关的学生端接口函数）
- `course_frontend/course_frontend/src/api/comment.js`
- `course_frontend/course_frontend/src/api/index.js`（仅当需要导出新 API 时）

## 后端
- `server/server/src/main/java/com/coursemanager/controller/HomeworkStudentController.java`
- `server/server/src/main/java/com/coursemanager/controller/HomeworkCommentController.java`
- `server/server/src/main/java/com/coursemanager/service/IStudentHomeworkService.java`
- `server/server/src/main/java/com/coursemanager/service/impl/StudentHomeworkServiceImpl.java`
- `server/server/src/main/java/com/coursemanager/service/IHomeworkCommentService.java`
- `server/server/src/main/java/com/coursemanager/service/impl/HomeworkCommentServiceImpl.java`
- `server/server/src/main/java/com/coursemanager/mapper/HomeworkMapper.java`（复用 B1 的，不得删除或修改已有的查询方法）
- `server/server/src/main/java/com/coursemanager/mapper/HomeworkSubmitMapper.java`
- `server/server/src/main/java/com/coursemanager/mapper/HomeworkCommentMapper.java`
- `server/server/src/main/java/com/coursemanager/pojo/Homework.java`（复用 B1 的，不得删除已有字段）
- `server/server/src/main/java/com/coursemanager/pojo/HomeworkSubmit.java`
- `server/server/src/main/java/com/coursemanager/pojo/HomeworkComment.java`
- `server/server/src/main/resources/com/coursemanager/mapper/HomeworkMapper.xml`（复用 B1 的，不得删除或修改已有的 SQL）
- `server/server/src/main/resources/com/coursemanager/mapper/HomeworkSubmitMapper.xml`

# 黑名单（绝对禁止触碰）
## 全局配置
- `course_frontend/course_frontend/src/main.js`
- `course_frontend/course_frontend/src/App.vue`
- `course_frontend/course_frontend/vite.config.js`
- `course_frontend/course_frontend/package.json`
- `course_frontend/course_frontend/src/router/index.js`（仅组长可改）
- `server/server/src/main/java/com/coursemanager/ServerApplication.java`
- `server/server/src/main/resources/application.yml`
- `server/server/src/main/java/com/coursemanager/config/CorsConfig.java`
- `server/server/src/main/java/com/coursemanager/handler/WebExceptionHandler.java`
- `server/server/src/main/java/com/coursemanager/utils/CommonResult.java`
- `server/server/src/main/java/com/coursemanager/pojo/BaseBean.java`
- 项目根目录的 `DEV_GUIDE.md` 与 `README.md`

## B1 的文件（B2 禁止触碰）
- `views/teacher/CourseHomework.vue`（B1）
- `views/teacher/CreateHomework.vue`（B1）
- `views/teacher/HomeworkSubmissions.vue`（B1）
- `controller/HomeworkTeacherController.java`（B1）
- `service/IHomeworkService.java`（B1）
- `service/impl/HomeworkServiceImpl.java`（B1）
- `lang/HomeworkCondition.java`（B1）
- `lang/HomeworkSubmitCondition.java`（B1）

## 其他组员板块
- 组员 A：`views/teacher/CourseManage.vue`、`views/teacher/CreateCourse.vue`、`views/student/MyCourse.vue`、`api/course.js` 及所有 `Course*` 控制器/服务/Mapper/POJO
- 组员 C：`views/login/LoginIndex.vue`、`api/auth.js`、`store/user.js` 及所有 `Auth*` 控制器/服务/Mapper/POJO

# B1-B2 接口约定
- B2 复用 `HomeworkMapper` 的查询方法，调用时不得改动已有 SQL
- B2 在 `StudentHomeworkServiceImpl` 中调用 `HomeworkSubmitMapper` 进行学生提交操作（新增/更新）
- B2 的 `HomeworkCommentServiceImpl` 负责评论发表（insert）和列表查询（select）
- 匿名评论时，后端不返回真实 userName，前端展示"匿名用户"

# 协作约束
- 在生成任何代码之前，先用 Read 工具读取目标文件，确认当前内容。
- 新增 API 函数时，先检查 `api/homework.js` 和 `api/comment.js` 是否已有同名函数，避免重复。
- 需要新增 POJO 字段时，必须继承 `BaseBean`，不要自建基础字段。
- 新增 Controller 接口路径时，必须先确认 `router/index.js` 中已配置对应前端路由（无路由则向组长申请）。
- 当我让你修改黑名单文件时，请明确拒绝，并提示我联系组长。
````

---

## 五、冲突处理

如果 `git pull origin dev` 时发生冲突：
1. **严禁** `git push -f` 强制推送
2. 冲突文件中，**以组长在 dev 分支的版本为准**
3. 10 分钟内群里@组长说明冲突文件
4. 等待组长解决冲突后再 rebase 到最新版本
