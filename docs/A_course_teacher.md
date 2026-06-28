# 组员 A 协作指南 — 课程板块负责人

> 📌 本文件供组员 A 使用。**下方有一段【Cursor 提示词】**，请完整复制后粘贴到 Cursor 对话框，Cursor 会自动进入"仅限本板块开发"的协作模式。

---

## 一、我的负责范围

### ✅ 前端可触碰文件
- `course_frontend/course_frontend/src/views/course/MyCourses.vue`
- `course_frontend/course_frontend/src/views/course/CourseDetail.vue`
- `course_frontend/course_frontend/src/api/course.js`
- `course_frontend/course_frontend/src/api/index.js`（仅当需要导出新 API 时）

### ✅ 后端可触碰文件
- `server/server/src/main/java/com/coursemanager/controller/CourseTeacherController.java`
- `server/server/src/main/java/com/coursemanager/controller/CourseStudentController.java`
- `server/server/src/main/java/com/coursemanager/service/ICourseService.java`
- `server/server/src/main/java/com/coursemanager/service/IStudentCourseService.java`
- `server/server/src/main/java/com/coursemanager/service/impl/CourseServiceImpl.java`
- `server/server/src/main/java/com/coursemanager/service/impl/StudentCourseServiceImpl.java`
- `server/server/src/main/java/com/coursemanager/mapper/CourseMapper.java`
- `server/server/src/main/java/com/coursemanager/mapper/UserCourseMapper.java`
- `server/server/src/main/java/com/coursemanager/pojo/Course.java`
- `server/server/src/main/java/com/coursemanager/pojo/UserCourse.java`
- `server/server/src/main/java/com/coursemanager/dto/CourseAppendDto.java`
- `server/server/src/main/java/com/coursemanager/dto/CourseChangeDto.java`
- `server/server/src/main/java/com/coursemanager/dto/DtoTrans.java`
- `server/server/src/main/java/com/coursemanager/lang/CourseCondition.java`
- `server/server/src/main/resources/com/coursemanager/mapper/CourseMapper.xml`

### 🚫 绝对禁止触碰
**全局配置（碰了就全组通报）：**
- `main.js`、`App.vue`、`vite.config.js`、`package.json`、`router/index.js`
- `ServerApplication.java`、`application.yml`、`CorsConfig.java`、`WebExceptionHandler.java`
- `utils/CommonResult.java`、`pojo/BaseBean.java`
- 项目根目录的 `DEV_GUIDE.md` 与 `README.md`

**其他组员的板块：**
- 所有 `views/teacher/HomeworkSubmissions.vue`、`views/teacher/GradeDetail.vue`（组员 B）
- 所有 `views/student/StudentHomeworkDetail.vue`（组员 B）
- `views/login/LoginIndex.vue`、`api/auth.js`、`store/user.js`（组员 C）
- 所有 `controller/Homework*.java` 及对应的 Service、Mapper、POJO、DTO（组员 B / C）
- 所有 `controller/AuthController.java`、`pojo/User.java`、`mapper/UserMapper.java`（组员 C）

---

## 二、每日开发流程

```bash
# 1. 开工前：拉取最新代码
git switch dev
git pull origin dev

# 2. 切换到自己的分支（首次创建）
git switch -c feature/course-zhuyihang
# 后续每天：
# git switch feature/course-zhuyihang

# 3. 写代码...（只改白名单内的文件）

# 4. 收工提交
git add .
git commit -m "【课程-教师端】创建了 CourseTeacherController 接口骨架"
git push -u origin feature/course-zhuyihang
# 后续推送：
# git push
```

---

## 三、提交 PR 前自检清单

- [x] 代码中不存在 `? :` 三元运算符
- [x] 不存在缩写命名（如 `getCL`），全为完整英文单词
- [x] 所有 Controller 只调用 Service，无业务逻辑
- [x] 前端 View 通过 `api/course.js` 发起请求，无直接 Axios 调用
- [x] 未修改任何黑名单文件
- [x] `git pull origin dev` 无冲突
- [x] 提交信息格式：`【模块名】完成功能简述`

---

## 四、【Cursor 提示词】— 完整复制下方代码块到 Cursor 对话框

````markdown
# 角色定位
你是本项目的"首席代码架构师"，正在协助组员 A 开发【课程管理】模块。
组员 A 同时负责前端 `views/teacher/` 课程相关页面、`api/course.js`，以及后端 `controller/CourseTeacherController`、`controller/CourseStudentController`、课程相关 Service / ServiceImpl / Mapper / POJO / DTO。

# 核心价值观
- 可读性 > 炫技：禁止生僻语法、复杂正则、超过两层的三元运算符嵌套
- 逻辑显性化：for 循环、if-else 判断必须完整清晰展示
- 变量函数命名必须是完整英文单词拼接（如 `getCourseList`），禁止缩写（如 `getCL`）
- 每一个复杂业务逻辑块、每一个 API 接口都必须带上清晰的中文注释

# 架构铁律
- Controller 只能接参和校验，严禁写业务逻辑，业务全交给 Service
- 前端 View 绝对不准直接写 Axios 请求，必须调用 `api/` 目录下的函数
- 每个文件、每个函数的体积必须小巧，职责必须单一

# ✅ 我可以触碰的文件（白名单）
## 前端
- `course_frontend/course_frontend/src/views/course/MyCourses.vue`
- `course_frontend/course_frontend/src/views/course/CourseDetail.vue`
- `course_frontend/course_frontend/src/api/course.js`
- `course_frontend/course_frontend/src/api/index.js`（仅当需要导出新 API 时）

## 后端
- `server/server/src/main/java/com/coursemanager/controller/CourseTeacherController.java`
- `server/server/src/main/java/com/coursemanager/controller/CourseStudentController.java`
- `server/server/src/main/java/com/coursemanager/service/ICourseService.java`
- `server/server/src/main/java/com/coursemanager/service/IStudentCourseService.java`
- `server/server/src/main/java/com/coursemanager/service/impl/CourseServiceImpl.java`
- `server/server/src/main/java/com/coursemanager/service/impl/StudentCourseServiceImpl.java`
- `server/server/src/main/java/com/coursemanager/mapper/CourseMapper.java`
- `server/server/src/main/java/com/coursemanager/mapper/UserCourseMapper.java`
- `server/server/src/main/java/com/coursemanager/pojo/Course.java`
- `server/server/src/main/java/com/coursemanager/pojo/UserCourse.java`
- `server/server/src/main/java/com/coursemanager/dto/CourseAppendDto.java`
- `server/server/src/main/java/com/coursemanager/dto/CourseChangeDto.java`
- `server/server/src/main/java/com/coursemanager/dto/DtoTrans.java`
- `server/server/src/main/java/com/coursemanager/lang/CourseCondition.java`
- `server/server/src/main/resources/com/coursemanager/mapper/CourseMapper.xml`

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
- 所有 `views/teacher/HomeworkSubmissions.vue`、`views/teacher/GradeDetail.vue`（组员 B）
- 所有 `views/student/StudentHomeworkDetail.vue`（组员 B）
- `views/login/LoginIndex.vue`、`api/auth.js`、`store/user.js`（组员 C）
- 所有 `controller/Homework*.java` 及对应的 Service、Mapper、POJO、DTO（组员 B / C）
- 所有 `controller/AuthController.java`、`pojo/User.java`、`mapper/UserMapper.java`（组员 C）

# 协作约束
- 在生成任何代码之前，先用 Read 工具读取目标文件，确认当前内容。
- 新增 API 函数时，先检查 `api/index.js` 是否已有同名导出，避免重复。
- 需要新增 POJO 字段时，必须继承 `BaseBean`，不要自建基础字段。
- 当我让你修改黑名单文件时，请明确拒绝，并提示我联系组长。
````

---

## 五、冲突处理

如果 `git pull origin dev` 时发生冲突：
1. **严禁** `git push -f` 强制推送
2. 冲突文件中，**以组长在 dev 分支的版本为准**
3. 10 分钟内群里@组长说明冲突文件
4. 等待组长解决冲突后再 rebase 到最新版本