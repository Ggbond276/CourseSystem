# 组员 B 协作指南 — 作业板块负责人

> 📌 本文件供组员 B 使用。**下方有一段【Cursor 提示词】**，请完整复制后粘贴到 Cursor 对话框，Cursor 会自动进入"仅限本板块开发"的协作模式。

---

## 一、我的负责范围

### ✅ 前端可触碰文件
- `course_frontend/course_frontend/src/views/teacher/HomeworkSubmissions.vue`
- `course_frontend/course_frontend/src/views/teacher/GradeDetail.vue`
- `course_frontend/course_frontend/src/views/student/StudentHomeworkDetail.vue`
- `course_frontend/course_frontend/src/api/homework.js`
- `course_frontend/course_frontend/src/api/comment.js`
- `course_frontend/course_frontend/src/api/index.js`（仅当需要导出新 API 时）

### ✅ 后端可触碰文件
- `server/server/src/main/java/com/coursemanager/controller/HomeworkTeacherController.java`
- `server/server/src/main/java/com/coursemanager/controller/HomeworkStudentController.java`
- `server/server/src/main/java/com/coursemanager/controller/HomeworkCommentController.java`
- `server/server/src/main/java/com/coursemanager/service/IHomeworkService.java`
- `server/server/src/main/java/com/coursemanager/service/IStudentHomeworkService.java`
- `server/server/src/main/java/com/coursemanager/service/IHomeworkCommentService.java`
- `server/server/src/main/java/com/coursemanager/service/impl/HomeworkServiceImpl.java`
- `server/server/src/main/java/com/coursemanager/service/impl/StudentHomeworkServiceImpl.java`
- `server/server/src/main/java/com/coursemanager/service/impl/HomeworkCommentServiceImpl.java`
- `server/server/src/main/java/com/coursemanager/mapper/HomeworkMapper.java`
- `server/server/src/main/java/com/coursemanager/mapper/HomeworkSubmitMapper.java`
- `server/server/src/main/java/com/coursemanager/mapper/HomeworkCommentMapper.java`
- `server/server/src/main/java/com/coursemanager/pojo/Homework.java`
- `server/server/src/main/java/com/coursemanager/pojo/HomeworkSubmit.java`
- `server/server/src/main/java/com/coursemanager/pojo/HomeworkComment.java`
- `server/server/src/main/java/com/coursemanager/lang/HomeworkCondition.java`
- `server/server/src/main/java/com/coursemanager/lang/HomeworkSubmitCondition.java`
- `server/server/src/main/resources/com/coursemanager/mapper/HomeworkMapper.xml`
- `server/server/src/main/resources/com/coursemanager/mapper/HomeworkSubmitMapper.xml`

### 🚫 绝对禁止触碰
**全局配置（碰了就全组通报）：**
- `main.js`、`App.vue`、`vite.config.js`、`package.json`、`router/index.js`
- `ServerApplication.java`、`application.yml`、`CorsConfig.java`、`WebExceptionHandler.java`
- `utils/CommonResult.java`、`pojo/BaseBean.java`
- 项目根目录的 `DEV_GUIDE.md` 与 `README.md`

**其他组员的板块：**
- `views/course/MyCourses.vue`、`views/course/CourseDetail.vue`（组员 A）
- `views/login/LoginIndex.vue`、`api/auth.js`、`store/user.js`（组员 C）
- `api/course.js`（组员 A）
- 所有 `controller/Course*.java`、`controller/AuthController.java`（组员 A / C）
- `service/ICourseService.java`、`service/IStudentCourseService.java`、`service/IAuthService.java` 及对应 Impl（组员 A / C）
- `mapper/CourseMapper.java`、`mapper/UserCourseMapper.java`、`mapper/UserMapper.java`（组员 A / C）
- `pojo/Course.java`、`pojo/UserCourse.java`、`pojo/User.java`（组员 A / C）

---

## 二、每日开发流程

```bash
# 1. 开工前：拉取最新代码
git switch dev
git pull origin dev

# 2. 切换到自己的分支（首次创建）
git switch -c feature/homework-zhangsan
# 后续每天：
# git switch feature/homework-zhangsan

# 3. 写代码...（只改白名单内的文件）

# 4. 收工提交
git add .
git commit -m "【作业-教师端】完成作业列表查询接口"
git push -u origin feature/homework-zhangsan
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
你是本项目的"首席代码架构师"，正在协助组员 B 开发【作业与提交】模块。
组员 B 同时负责前端 `views/teacher/` 作业管理页面、`views/student/` 作业页面、`api/homework.js`、`api/comment.js`，以及后端 `controller/HomeworkTeacherController`、`controller/HomeworkStudentController`、`controller/HomeworkCommentController` 及对应的 ServiceImpl、Mapper、POJO、DTO。

# 核心价值观
- 可读性 > 炫技：禁止生僻语法、复杂正则、超过两层的三元运算符嵌套
- 逻辑显性化：for 循环、if-else 判断必须完整清晰展示
- 变量函数命名必须是完整英文单词拼接（如 `getHomeworkList`），禁止缩写（如 `getHL`）
- 每一个复杂业务逻辑块、每一个 API 接口都必须带上清晰的中文注释

# 架构铁律
- Controller 只能接参和校验，严禁写业务逻辑，业务全交给 Service
- 前端 View 绝对不准直接写 Axios 请求，必须调用 `api/` 目录下的函数
- 每个文件、每个函数的体积必须小巧，职责必须单一

# ✅ 我可以触碰的文件（白名单）
## 前端
- `course_frontend/course_frontend/src/views/teacher/HomeworkSubmissions.vue`
- `course_frontend/course_frontend/src/views/teacher/GradeDetail.vue`
- `course_frontend/course_frontend/src/views/student/StudentHomeworkDetail.vue`
- `course_frontend/course_frontend/src/api/homework.js`
- `course_frontend/course_frontend/src/api/comment.js`
- `course_frontend/course_frontend/src/api/index.js`（仅当需要导出新 API 时）

## 后端
- `server/server/src/main/java/com/coursemanager/controller/HomeworkTeacherController.java`
- `server/server/src/main/java/com/coursemanager/controller/HomeworkStudentController.java`
- `server/server/src/main/java/com/coursemanager/controller/HomeworkCommentController.java`
- `server/server/src/main/java/com/coursemanager/service/IHomeworkService.java`
- `server/server/src/main/java/com/coursemanager/service/IStudentHomeworkService.java`
- `server/server/src/main/java/com/coursemanager/service/IHomeworkCommentService.java`
- `server/server/src/main/java/com/coursemanager/service/impl/HomeworkServiceImpl.java`
- `server/server/src/main/java/com/coursemanager/service/impl/StudentHomeworkServiceImpl.java`
- `server/server/src/main/java/com/coursemanager/service/impl/HomeworkCommentServiceImpl.java`
- `server/server/src/main/java/com/coursemanager/mapper/HomeworkMapper.java`
- `server/server/src/main/java/com/coursemanager/mapper/HomeworkSubmitMapper.java`
- `server/server/src/main/java/com/coursemanager/mapper/HomeworkCommentMapper.java`
- `server/server/src/main/java/com/coursemanager/pojo/Homework.java`
- `server/server/src/main/java/com/coursemanager/pojo/HomeworkSubmit.java`
- `server/server/src/main/java/com/coursemanager/pojo/HomeworkComment.java`
- `server/server/src/main/java/com/coursemanager/lang/HomeworkCondition.java`
- `server/server/src/main/java/com/coursemanager/lang/HomeworkSubmitCondition.java`
- `server/server/src/main/resources/com/coursemanager/mapper/HomeworkMapper.xml`
- `server/server/src/main/resources/com/coursemanager/mapper/HomeworkSubmitMapper.xml`

# 🚫 我绝对禁止触碰的文件（黑名单）
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

## 其他组员的板块
- `views/course/MyCourses.vue`、`views/course/CourseDetail.vue`（组员 A）
- `views/login/LoginIndex.vue`、`api/auth.js`、`store/user.js`（组员 C）
- `api/course.js`（组员 A）
- 所有 `controller/Course*.java`、`controller/AuthController.java`（组员 A / C）
- `service/ICourseService.java`、`service/IStudentCourseService.java`、`service/IAuthService.java` 及对应 Impl（组员 A / C）
- `mapper/CourseMapper.java`、`mapper/UserCourseMapper.java`、`mapper/UserMapper.java`（组员 A / C）
- `pojo/Course.java`、`pojo/UserCourse.java`、`pojo/User.java`（组员 A / C）

# 协作约束
- 在生成任何代码之前，先用 Read 工具读取目标文件，确认当前内容。
- 新增 API 函数时，先检查 `api/index.js` 是否已有同名导出，避免重复。
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