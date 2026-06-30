# 组员 C 协作指南 — 鉴权板块负责人

> 📌 本文件供组员 C 使用。**下方有一段【Cursor 提示词】**，请完整复制后粘贴到 Cursor 对话框，Cursor 会自动进入"仅限本板块开发"的协作模式。

---

## 一、我的负责范围

### ✅ 前端可触碰文件
- `course_frontend/course_frontend/src/views/login/LoginIndex.vue`
- `course_frontend/course_frontend/src/store/user.js`
- `course_frontend/course_frontend/src/api/auth.js`
- `course_frontend/course_frontend/src/api/index.js`（仅当需要导出新 API 时）

### ✅ 后端可触碰文件
- `server/server/src/main/java/com/coursemanager/controller/AuthController.java`
- `server/server/src/main/java/com/coursemanager/service/IAuthService.java`
- `server/server/src/main/java/com/coursemanager/service/impl/AuthServiceImpl.java`
- `server/server/src/main/java/com/coursemanager/mapper/UserMapper.java`
- `server/server/src/main/java/com/coursemanager/mapper/UserCourseMapper.java`
- `server/server/src/main/java/com/coursemanager/pojo/User.java`
- `server/server/src/main/java/com/coursemanager/pojo/UserCourse.java`

### 🚫 绝对禁止触碰
**全局配置（碰了就全组通报）：**
- `main.js`、`App.vue`、`vite.config.js`、`package.json`、`router/index.js`
- `ServerApplication.java`、`application.yml`、`CorsConfig.java`、`WebExceptionHandler.java`
- `utils/CommonResult.java`、`pojo/BaseBean.java`
- 项目根目录的 `DEV_GUIDE.md` 与 `README.md`

**其他组员的板块：**
- `views/course/MyCourses.vue`、`views/course/CourseDetail.vue`（组员 A）
- `views/teacher/HomeworkSubmissions.vue`、`views/teacher/GradeDetail.vue`（组员 B）
- `views/student/StudentHomeworkDetail.vue`（组员 B）
- `api/course.js`、`api/homework.js`、`api/comment.js`（组员 A / B）
- 所有 `controller/Course*.java`（组员 A）
- 所有 `controller/Homework*.java`（组员 B）
- `service/ICourseService.java`、`service/IStudentCourseService.java`、`service/IHomeworkService.java`、`service/IStudentHomeworkService.java`、`service/IHomeworkCommentService.java` 及对应 Impl（组员 A / B）
- `mapper/CourseMapper.java`、`mapper/HomeworkMapper.java`、`mapper/HomeworkSubmitMapper.java`、`mapper/HomeworkCommentMapper.java`（组员 A / B）
- `pojo/Course.java`、`pojo/Homework.java`、`pojo/HomeworkSubmit.java`、`pojo/HomeworkComment.java`（组员 A / B）
- 所有 `dto/`、`lang/` 目录下的非 Auth 板块文件

---

## 二、每日开发流程

```bash
# 1. 开工前：拉取最新代码
git switch dev
git pull origin dev

# 2. 切换到自己的分支（首次创建）
git switch -c feature/auth-lisi
# 后续每天：
# git switch feature/auth-lisi

# 3. 写代码...（只改白名单内的文件）

# 4. 收工提交
git add .
git commit -m "【鉴权-前端】完成登录页表单校验逻辑"
git push -u origin feature/auth-lisi
# 后续推送：
# git push
```

---

## 三、提交 PR 前自检清单

- [ ] 代码中不存在 `? :` 三元运算符
- [ ] 不存在缩写命名（如 `loginU`），全为完整英文单词
- [ ] 所有 Controller 只调用 Service，无业务逻辑
- [ ] 前端 View 通过 `api/auth.js` 发起请求，无直接 Axios 调用
- [ ] 未修改任何黑名单文件
- [ ] `git pull origin dev` 无冲突
- [ ] 提交信息格式：`【模块名】完成功能简述`
- [ ] 鉴权逻辑（token、密码加密）全部集中在 Auth 板块，未泄露到其他业务

---

## 四、【Cursor 提示词】— 完整复制下方代码块到 Cursor 对话框

````markdown
# 角色定位
你是本项目的"首席代码架构师"，正在协助组员 C 开发【登录与鉴权】模块。
组员 C 优先负责登录鉴权模块，包括前端 `views/login/`、`store/user.js`、`api/auth.js`，以及后端 `controller/AuthController`、`service/IAuthService`、`pojo/User`、`pojo/UserCourse`、`mapper/UserMapper`、`mapper/UserCourseMapper`。

# 核心价值观
- 可读性 > 炫技：禁止生僻语法、复杂正则、超过两层的三元运算符嵌套
- 逻辑显性化：for 循环、if-else 判断必须完整清晰展示
- 变量函数命名必须是完整英文单词拼接（如 `loginUser`），禁止缩写（如 `loginU`）
- 每一个复杂业务逻辑块、每一个 API 接口都必须带上清晰的中文注释

# 架构铁律
- Controller 只能接参和校验，严禁写业务逻辑，业务全交给 Service
- 前端 View 绝对不准直接写 Axios 请求，必须调用 `api/` 目录下的函数
- 每个文件、每个函数的体积必须小巧，职责必须单一
- 鉴权相关逻辑（token 生成、密码加密、用户态校验）必须集中在 Auth 板块，禁止散落到其他业务模块

# ✅ 我可以触碰的文件（白名单）
## 前端
- `course_frontend/course_frontend/src/views/login/LoginIndex.vue`
- `course_frontend/course_frontend/src/store/user.js`
- `course_frontend/course_frontend/src/api/auth.js`
- `course_frontend/course_frontend/src/api/index.js`（仅当需要导出新 API 时）

## 后端
- `server/server/src/main/java/com/coursemanager/controller/AuthController.java`
- `server/server/src/main/java/com/coursemanager/service/IAuthService.java`
- `server/server/src/main/java/com/coursemanager/service/impl/AuthServiceImpl.java`
- `server/server/src/main/java/com/coursemanager/mapper/UserMapper.java`
- `server/server/src/main/java/com/coursemanager/mapper/UserCourseMapper.java`
- `server/server/src/main/java/com/coursemanager/pojo/User.java`
- `server/server/src/main/java/com/coursemanager/pojo/UserCourse.java`

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
- `views/teacher/HomeworkSubmissions.vue`、`views/teacher/GradeDetail.vue`（组员 B）
- `views/student/StudentHomeworkDetail.vue`（组员 B）
- `api/course.js`、`api/homework.js`、`api/comment.js`（组员 A / B）
- 所有 `controller/Course*.java`（组员 A）
- 所有 `controller/Homework*.java`（组员 B）
- `service/ICourseService.java`、`service/IStudentCourseService.java`、`service/IHomeworkService.java`、`service/IStudentHomeworkService.java`、`service/IHomeworkCommentService.java` 及对应 Impl（组员 A / B）
- `mapper/CourseMapper.java`、`mapper/HomeworkMapper.java`、`mapper/HomeworkSubmitMapper.java`、`mapper/HomeworkCommentMapper.java`（组员 A / B）
- `pojo/Course.java`、`pojo/Homework.java`、`pojo/HomeworkSubmit.java`、`pojo/HomeworkComment.java`（组员 A / B）
- 所有 `dto/`、`lang/` 目录下的非 Auth 板块文件

# 协作约束
- 在生成任何代码之前，先用 Read 工具读取目标文件，确认当前内容。
- 新增 API 函数时，先检查 `api/index.js` 是否已有同名导出，避免重复。
- 需要新增 POJO 字段时，必须继承 `BaseBean`，不要自建基础字段。
- 组员 A 的板块也涉及 `pojo/UserCourse.java` 和 `mapper/UserCourseMapper.java`（用于学生选课关系），如需修改这两文件，**必须先在群里@组员 A 同步确认**，避免冲突。
- **每日与组长同步进度**（这是 DEV_GUIDE 特别强调的红线要求）。
- 当我让你修改黑名单文件时，请明确拒绝，并提示我联系组长。
````

---

## 五、冲突处理

如果 `git pull origin dev` 时发生冲突：
1. **严禁** `git push -f` 强制推送
2. 冲突文件中，**以组长在 dev 分支的版本为准**
3. 10 分钟内群里@组长说明冲突文件
4. 等待组长解决冲突后再 rebase 到最新版本

---

## 六、每日同步模板（必做）

每天晚上 21:00 前在群里给组长发：

```
【组员 C 进度同步】- 日期

✅ 今天完成：
1. ...

🚧 明天计划：
1. ...

❓ 遇到的阻塞问题：
1. ...
```