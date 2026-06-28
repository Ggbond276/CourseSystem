# 组员 B1 协作指南 — 作业板块·教师端

> 📌 本文件供组员 B1 使用，负责【作业板块·教师端】功能开发。
> **下方有一段【Cursor 提示词】**，请完整复制后粘贴到 Cursor 对话框，Cursor 会自动进入"仅限本板块开发"的协作模式。

---

## 一、我的负责范围

### B1 负责的功能清单

| 编号 | 功能 | 说明 |
|------|------|------|
| B1-1 | 发布作业 | 教师在课程下创建作业，支持附件 |
| B1-2 | 教师作业列表 | 查看课程下所有作业及统计 |
| B1-3 | 批阅大厅 | 查看某作业下所有学生的提交明细 |
| B1-4 | 批阅打分 | 教师给某个学生打分、写评语 |

### B1 负责的前端文件

| 文件路径 | 说明 |
|----------|------|
| `course_frontend/course_frontend/src/views/teacher/CourseHomework.vue` | 课程作业管理主页面 |
| `course_frontend/course_frontend/src/views/teacher/CreateHomework.vue` | 发布作业表单 |
| `course_frontend/course_frontend/src/views/teacher/HomeworkSubmissions.vue` | 批阅大厅页面 |

### B1 负责的后端文件

| 文件路径 | 说明 |
|----------|------|
| `server/server/src/main/java/com/coursemanager/controller/HomeworkTeacherController.java` | 教师端作业控制器 |
| `server/server/src/main/java/com/coursemanager/service/IHomeworkService.java` | 作业业务接口 |
| `server/server/src/main/java/com/coursemanager/service/impl/HomeworkServiceImpl.java` | 作业业务实现 |
| `server/server/src/main/java/com/coursemanager/service/IHomeworkCommentService.java` | 评论业务接口（仅读列表） |
| `server/server/src/main/java/com/coursemanager/service/impl/HomeworkCommentServiceImpl.java` | 评论业务实现（仅读列表） |
| `server/server/src/main/java/com/coursemanager/mapper/HomeworkMapper.java` | 作业数据访问 |
| `server/server/src/main/java/com/coursemanager/mapper/HomeworkSubmitMapper.java` | 提交记录数据访问 |
| `server/server/src/main/java/com/coursemanager/pojo/Homework.java` | 作业实体 |
| `server/server/src/main/java/com/coursemanager/pojo/HomeworkSubmit.java` | 提交记录实体 |
| `server/server/src/main/java/com/coursemanager/lang/HomeworkCondition.java` | 作业查询条件 |
| `server/server/src/main/java/com/coursemanager/lang/HomeworkSubmitCondition.java` | 提交查询条件 |
| `server/server/src/main/resources/com/coursemanager/mapper/HomeworkMapper.xml` | 作业 SQL 映射 |
| `server/server/src/main/resources/com/coursemanager/mapper/HomeworkSubmitMapper.xml` | 提交 SQL 映射 |

### 与 B2 的接口边界（重点）

- `IHomeworkService.listByCondition()`、`IHomeworkService.create()` 等写操作归属 **B1**
- `IStudentHomeworkService`（学生侧服务）和 `HomeworkCommentController` 归属 **B2**，B1 **不得**修改
- 评论发表（`HomeworkCommentController.add`）归属 **B2**，B1 不碰
- 学生作业列表/详情/提交（`HomeworkStudentController`）归属 **B2**，B1 不碰

---

## 二、每日开发流程

```bash
# 1. 开工前：拉取最新代码
git switch dev
git pull origin dev

# 2. 切换到自己的分支（首次创建）
git switch -c feature/homework-b1-zhangsan
# 后续每天：
# git switch feature/homework-b1-zhangsan

# 3. 写代码...（只改白名单内的文件）

# 4. 收工提交
git add .
git commit -m "【作业-教师端】完成作业列表查询接口"
git push -u origin feature/homework-b1-zhangsan
# 后续推送：
# git push
```

---

## 三、提交 PR 前自检清单

- [ ] 代码中不存在 `? :` 三元运算符
- [ ] 不存在缩写命名（如 `getHL`），全为完整英文单词
- [ ] 所有 Controller 只调用 Service，无业务逻辑
- [ ] 前端 View 通过 `api/homework.js` 发起请求，无直接 Axios 调用
- [ ] 未修改任何黑名单文件
- [ ] `git pull origin dev` 无冲突
- [ ] 提交信息格式：`【模块名】完成功能简述`

---

## 四、【Cursor 提示词】— 完整复制下方代码块到 Cursor 对话框

````markdown
# 角色定位
你是本项目的"首席代码架构师"，正在协助组员 B1 开发【作业板块·教师端】模块。
组员 B1 负责：发布作业、教师作业列表、批阅大厅（提交明细）、批阅打分。
对应的前端文件：`views/teacher/CourseHomework.vue`、`views/teacher/CreateHomework.vue`、`views/teacher/HomeworkSubmissions.vue`。
对应的后端文件：`controller/HomeworkTeacherController`、`service/IHomeworkService`、`service/impl/HomeworkServiceImpl` 及对应 Mapper/POJO。

# 核心价值观
- 可读性 > 炫技：禁止生僻语法、复杂正则、超过两层的三元运算符嵌套
- 逻辑显性化：for 循环、if-else 判断必须完整清晰展示
- 变量函数命名必须是完整英文单词拼接（如 `getHomeworkList`），禁止缩写（如 `getHL`）
- 每一个复杂业务逻辑块、每一个 API 接口都必须带上清晰的中文注释

# 架构铁律
- Controller 只能接参和校验，严禁写业务逻辑，业务全交给 Service
- 前端 View 绝对不准直接写 Axios 请求，必须调用 `api/homework.js` 目录下的函数
- 每个文件、每个函数的体积必须小巧，职责必须单一

# B1 白名单（仅限以下文件）
## 前端
- `course_frontend/course_frontend/src/views/teacher/CourseHomework.vue`
- `course_frontend/course_frontend/src/views/teacher/CreateHomework.vue`
- `course_frontend/course_frontend/src/views/teacher/HomeworkSubmissions.vue`
- `course_frontend/course_frontend/src/api/homework.js`（仅添加 B1 相关的教师端接口函数）
- `course_frontend/course_frontend/src/api/index.js`（仅当需要导出新 API 时）

## 后端
- `server/server/src/main/java/com/coursemanager/controller/HomeworkTeacherController.java`
- `server/server/src/main/java/com/coursemanager/service/IHomeworkService.java`
- `server/server/src/main/java/com/coursemanager/service/impl/HomeworkServiceImpl.java`
- `server/server/src/main/java/com/coursemanager/service/IHomeworkCommentService.java`（仅限读列表相关方法）
- `server/server/src/main/java/com/coursemanager/service/impl/HomeworkCommentServiceImpl.java`（仅限读列表相关方法）
- `server/server/src/main/java/com/coursemanager/mapper/HomeworkMapper.java`
- `server/server/src/main/java/com/coursemanager/mapper/HomeworkSubmitMapper.java`
- `server/server/src/main/java/com/coursemanager/mapper/HomeworkCommentMapper.java`（仅限读列表相关方法）
- `server/server/src/main/java/com/coursemanager/pojo/Homework.java`
- `server/server/src/main/java/com/coursemanager/pojo/HomeworkSubmit.java`
- `server/server/src/main/java/com/coursemanager/lang/HomeworkCondition.java`
- `server/server/src/main/java/com/coursemanager/lang/HomeworkSubmitCondition.java`
- `server/server/src/main/resources/com/coursemanager/mapper/HomeworkMapper.xml`
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

## B2 的文件（B1 禁止触碰）
- `views/student/StudentHomework.vue`（B2）
- `views/student/StudentHomeworkDetail.vue`（B2）
- `api/comment.js`（B2）
- `controller/HomeworkStudentController.java`（B2）
- `controller/HomeworkCommentController.java`（B2，但 B1 可以调用其接口）
- `service/IStudentHomeworkService.java`（B2）
- `service/impl/StudentHomeworkServiceImpl.java`（B2）

## 其他组员板块
- 组员 A：`views/teacher/CourseManage.vue`、`views/teacher/CreateCourse.vue`、`views/student/MyCourse.vue`、`api/course.js` 及所有 `Course*` 控制器/服务/Mapper/POJO
- 组员 C：`views/login/LoginIndex.vue`、`api/auth.js`、`store/user.js` 及所有 `Auth*` 控制器/服务/Mapper/POJO

# B1-B2 接口约定
- B1 调用 `IHomeworkService.listByTeacher()` 查教师端作业列表
- B1 调用 `IHomeworkSubmitService.listByCondition()` 查批阅大厅
- B1 在批阅时写入 `HomeworkSubmit.score`、`HomeworkSubmit.teacherComment`、`HomeworkSubmit.status`
- 评论发表（POST `/homework/comment/add`）和评论列表（GET `/homework/comment/list`）的 Controller 归属 B2，但 B1 前端页面可以调用这两个接口

# 协作约束
- 在生成任何代码之前，先用 Read 工具读取目标文件，确认当前内容。
- 新增 API 函数时，先检查 `api/homework.js` 是否已有同名函数，避免重复。
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
