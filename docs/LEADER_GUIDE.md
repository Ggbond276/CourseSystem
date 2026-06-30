# 组长协作指南 — 项目架构与流程管理

> 📌 本文件供组长使用。**下方有一段【Cursor 提示词】**，请完整复制后粘贴到 Cursor 对话框，Cursor 会进入"组长全局统筹 + 自身开发"的协作模式。

---

## 一、组长定位

组长负责：
1. **架构与规范维护**：全局配置、跨板块协调、合并 PR、解决冲突
2. **自身开发**：组长通常承担一个完整板块（建议选 **Course 教师端**或 **作业批改端**），便于理解所有组员的代码
3. **代码审查**：每个组员提 PR 后，由组长 review 后合并到 `dev` 分支
4. **每日进度跟踪**：接收组员 A / B / C 的进度同步

---

## 二、我可以触碰的文件（白名单）

### ✅ 全局配置（组长专属，其他组员禁止触碰）
- `course_frontend/course_frontend/src/main.js`
- `course_frontend/course_frontend/src/App.vue`
- `course_frontend/course_frontend/vite.config.js`
- `course_frontend/course_frontend/package.json`
- `course_frontend/course_frontend/src/router/index.js`
- `course_frontend/course_frontend/src/constant/index.js`
- `server/server/pom.xml`
- `server/server/src/main/java/com/coursemanager/ServerApplication.java`
- `server/server/src/main/resources/application.yml`
- `server/server/src/main/java/com/coursemanager/config/CorsConfig.java`
- `server/server/src/main/java/com/coursemanager/handler/WebExceptionHandler.java`
- `server/server/src/main/java/com/coursemanager/utils/CommonResult.java`
- `server/server/src/main/java/com/coursemanager/utils/RandomData.java`
- `server/server/src/main/java/com/coursemanager/pojo/BaseBean.java`
- 项目根目录的 `README.md`、`DEV_GUIDE.md`

### ✅ 组员 B1 / B2 负责的作业板块（B1 归教师端，B2 归学生端）
#### B1 教师端
- `course_frontend/course_frontend/src/views/teacher/HomeworkSubmissions.vue`
- `course_frontend/course_frontend/src/views/teacher/GradeDetail.vue`
- `server/server/src/main/java/com/coursemanager/controller/HomeworkTeacherController.java`
- `server/server/src/main/java/com/coursemanager/service/IHomeworkService.java`
- `server/server/src/main/java/com/coursemanager/service/impl/HomeworkServiceImpl.java`
- `server/server/src/main/java/com/coursemanager/mapper/HomeworkMapper.java`
- `server/server/src/main/java/com/coursemanager/pojo/Homework.java`
- `server/server/src/main/resources/com/coursemanager/mapper/HomeworkMapper.xml`

#### B2 学生端 + 作业评论
- `course_frontend/course_frontend/src/views/student/StudentHomeworkDetail.vue`
- `course_frontend/course_frontend/src/api/comment.js`
- `server/server/src/main/java/com/coursemanager/controller/HomeworkStudentController.java`
- `server/server/src/main/java/com/coursemanager/controller/HomeworkCommentController.java`
- `server/server/src/main/java/com/coursemanager/service/IStudentHomeworkService.java`
- `server/server/src/main/java/com/coursemanager/service/impl/StudentHomeworkServiceImpl.java`
- `server/server/src/main/java/com/coursemanager/service/IHomeworkCommentService.java`
- `server/server/src/main/java/com/coursemanager/service/impl/HomeworkCommentServiceImpl.java`
- `server/server/src/main/java/com/coursemanager/pojo/HomeworkSubmit.java`
- `server/server/src/main/java/com/coursemanager/pojo/HomeworkComment.java`
- `server/server/src/main/resources/com/coursemanager/mapper/HomeworkSubmitMapper.xml`

#### 共享文件（B1 创建基础，B2 可复用但不得删改已有内容）
- `server/server/src/main/java/com/coursemanager/mapper/HomeworkMapper.java`
- `server/server/src/main/java/com/coursemanager/mapper/HomeworkSubmitMapper.java`
- `server/server/src/main/java/com/coursemanager/mapper/HomeworkCommentMapper.java`
- `server/server/src/main/java/com/coursemanager/pojo/Homework.java`
- `server/server/src/main/resources/com/coursemanager/mapper/HomeworkMapper.xml`

> 📝 **2026-06-28 重构后**：作业相关 UI 集中在 `CourseDetail.vue` 的「作业大厅」tab + 弹窗（详见 `docs/UI_REWORK_2026-06-28.md`）。
> B1/B2 修改作业相关 UI 时，**优先**在新文件 `HomeworkSubmissions.vue` / `GradeDetail.vue` / `StudentHomeworkDetail.vue` 内实现；
> 如必须改 `CourseDetail.vue`，请通过预留的 `<slot>` 提需求给组员 A 合并，**不要**直接修改该文件。

### ✅ 组员 A 负责的课程板块
- `course_frontend/course_frontend/src/views/course/MyCourses.vue`
- `course_frontend/course_frontend/src/views/course/CourseDetail.vue`
- `course_frontend/course_frontend/src/api/course.js`
- `server/server/src/main/java/com/coursemanager/controller/CourseTeacherController.java`
- `server/server/src/main/java/com/coursemanager/controller/CourseStudentController.java`
- `server/server/src/main/java/com/coursemanager/service/ICourseService.java`
- `server/server/src/main/java/com/coursemanager/service/IStudentCourseService.java`
- `server/server/src/main/java/com/coursemanager/mapper/CourseMapper.java`
- `server/server/src/main/java/com/coursemanager/pojo/Course.java`

> 📝 **2026-06-28 重构后**：`MyCourses.vue` 和 `CourseDetail.vue` 是教师/学生共用的容器页面，
> 组员 A 拥有所有权，B1/B2 想在里面加作业相关 UI 必须通过 `<slot>` 提交。

### ✅ 组员 C 负责的认证板块
- `course_frontend/course_frontend/src/views/login/LoginIndex.vue`
- `course_frontend/course_frontend/src/store/user.js`
- `course_frontend/course_frontend/src/api/auth.js`
- `server/server/src/main/java/com/coursemanager/controller/AuthController.java`
- `server/server/src/main/java/com/coursemanager/service/IAuthService.java`

### ✅ 跨板块权限（合并组员 PR 时需要）
- **所有** Controller / Service / Mapper / POJO / DTO / 前端 view / api 文件

> ⚠️ **重要原则**：组长只有在以下场景才会触碰其他组员的文件：
> 1. 合并 PR 后发现冲突，需要手动解决
> 2. 全局调整（如新增公共字段、修改接口签名）
> 3. 组员离职/请假，需要临时接手
>
> 普通开发任务下，**不应主动修改其他组员板块的文件**。

---

## 三、B1/B2 协调规则（组长必读）

作业板块拆分给 B1（教师端）和 B2（学生端 + 评论）两人开发，组长合并 PR 时需注意：

| 场景 | 规则 |
|------|------|
| `HomeworkMapper.java`、`HomeworkMapper.xml` | B1 创建，B2 可复用但**不得删除或修改**已有方法 |
| `HomeworkMapper.java` 新增方法 | 由组长判断归属：B1 逻辑找 B1 加，B2 逻辑找 B2 加 |
| `HomeworkSubmitMapper.java`、`HomeworkSubmitMapper.xml` | B1 和 B2 都可能读写，各自只管自己的业务场景 |
| `HomeworkCommentMapper.java` | 归 B2，B1 不碰 |
| `HomeworkTeacherController.java` | 归 B1 |
| `HomeworkStudentController.java`、`HomeworkCommentController.java` | 归 B2 |
| B1 和 B2 同时改同一文件 | 优先以先合并的 PR 为准，后合并者调整 |

---

## 四、🚫 组长也不应随意触碰的内容

虽然组长有最高权限，但下列场景**必须**在群里先通知，再开 PR：

| 文件 | 触发条件 | 通知对象 |
|------|----------|----------|
| `pojo/BaseBean.java` | 新增公共字段 | 全员 |
| `router/index.js` | 新增/删除路由 | 全员 |
| `utils/CommonResult.java` | 修改返回结构 | 全员 |
| `pom.xml` | 新增依赖 | 全员 |
| 数据库表结构 | 新增字段 | 全员 |

---

## 五、组长每日开发流程

> 参考文档：`A_course_teacher.md`（组员 A）、`B1_homework.md`（组员 B1）、`B2_homework.md`（组员 B2）、`C_auth.md`（组员 C）中的"提交 PR 前自检清单"。

```bash
# 1. 开工前：同步 dev
git switch dev
git pull origin dev

# 2. 检查组员 PR（从 GitHub / GitLab 网页查看）
# 重点查看：A_course_teacher.md、B_homework.md、C_auth.md 中的"提交 PR 前自检清单"

# 3. 处理组员进度同步
# 群里通常在 21:00 收到三位组员的【进度同步】模板

# 4. 自身开发：切换到自己的分支
git switch -c feature/course-leader
# 后续每天：
# git switch feature/course-leader

# 5. 收工：合并 dev 之前，先把自己的 feature 分支 rebase 到最新 dev
git fetch origin
git rebase origin/dev
git push -f origin feature/course-leader

# 6. 在 GitHub / GitLab 提 PR：feature/course-leader → dev
# 自己 review 自己代码，确认无遗漏后合并
```

---

## 五、合并组员 PR 的标准流程

```bash
# 1. 收到组员的 PR 通知后，先在本地切换到 dev
git switch dev
git pull origin dev

# 2. 拉取组员的 feature 分支做本地 review
git fetch origin
git switch feature/course-zhuyihang
git pull origin feature/course-zhuyihang

# 3. 代码 review 检查清单（务必逐项核对）
#    - [ ] 是否只修改了白名单文件
#    - [ ] 是否存在三元运算符、缩写命名
#    - [ ] Controller 是否只接参和校验
#    - [ ] 前端 View 是否通过 api/ 发起请求
#    - [ ] 是否继承 BaseBean
#    - [ ] 是否在 application.yml / pom.xml 中加了无关依赖

# 4. 切换到 dev，做合并（推荐 squash 模式）
git switch dev
git merge --squash feature/course-zhuyihang
# 或直接在 GitHub / GitLab 网页点 Squash and merge

# 5. 推送 dev
git push origin dev

# 6. 删除远程 feature 分支（可选）
git push origin --delete feature/course-zhuyihang

# 7. 群里通知组员："PR #N 已合并，可继续下一阶段开发"
```

---

## 六、处理冲突的标准流程

```bash
# 1. 切换到组员的 feature 分支
git switch feature/course-zhuyihang
git pull origin feature/course-zhuyihang

# 2. Rebase 到最新 dev
git fetch origin
git rebase origin/dev

# 3. 如果出现冲突：
#    a. 打开冲突文件，找到 <<<<<<< HEAD 标记
#    b. 与组员沟通，确认保留哪一部分
#    c. 手动编辑后 git add <file>
#    d. git rebase --continue

# 4. 强制推送（feature 分支允许 -f）
git push -f origin feature/course-zhuyihang

# 5. 在群里告知组员冲突已解决
```

---

## 七、PR 审查 Checklist（合并前必看）

### A. 文件范围合规性
- [ ] 修改的文件 100% 在该组员的白名单内
- [ ] 未修改任何黑名单文件（特别是全局配置）
- [ ] 未修改其他组员的板块

### B. 代码规范
- [ ] 无三元运算符 `? :`
- [ ] 无缩写命名（`getCL`、`loginU` 等）
- [ ] 每个函数有中文注释说明"干什么的"
- [ ] 复杂逻辑展开为多行 if-else / for，不是一行表达式

### C. 架构铁律
- [ ] Controller 只接参和校验，无业务逻辑
- [ ] Service 不直接操作 Mapper 之外的依赖
- [ ] 前端 View 不直接调用 Axios，全部走 `api/` 函数

### D. 提交规范
- [ ] commit message 格式：`【模块名】功能描述`
- [ ] 单次 PR 改动不超过 5 个文件（避免巨型 PR）
- [ ] 大型 PR 拆分为多个小 PR

---

## 七、【Cursor 提示词】— 完整复制下方代码块到 Cursor 对话框

````markdown
# 角色定位
你是本项目的"首席代码架构师"，正在协助组长进行项目架构维护与自身开发。
组长同时负责：全局配置维护、跨板块协调、合并 PR、解决冲突，以及自身承担的一个完整业务板块（建议 Course 教师端）。

# 核心价值观
- 可读性 > 炫技：禁止生僻语法、复杂正则、超过两层的三元运算符嵌套
- 逻辑显性化：for 循环、if-else 判断必须完整清晰展示
- 变量函数命名必须是完整英文单词拼接（如 `getCourseList`），禁止缩写
- 每一个复杂业务逻辑块、每一个 API 接口都必须带上清晰的中文注释

# 架构铁律
- Controller 只能接参和校验，严禁写业务逻辑，业务全交给 Service
- 前端 View 绝对不准直接写 Axios 请求，必须调用 `api/` 目录下的函数
- 每个文件、每个函数的体积必须小巧，职责必须单一

# ✅ 我可以触碰的文件（白名单）
## 全局配置（组长专属）
- `course_frontend/course_frontend/src/main.js`
- `course_frontend/course_frontend/src/App.vue`
- `course_frontend/course_frontend/vite.config.js`
- `course_frontend/course_frontend/package.json`
- `course_frontend/course_frontend/src/router/index.js`
- `course_frontend/course_frontend/src/constant/index.js`
- `server/server/pom.xml`
- `server/server/src/main/java/com/coursemanager/ServerApplication.java`
- `server/server/src/main/resources/application.yml`
- `server/server/src/main/java/com/coursemanager/config/CorsConfig.java`
- `server/server/src/main/java/com/coursemanager/handler/WebExceptionHandler.java`
- `server/server/src/main/java/com/coursemanager/utils/CommonResult.java`
- `server/server/src/main/java/com/coursemanager/utils/RandomData.java`
- `server/server/src/main/java/com/coursemanager/pojo/BaseBean.java`
- 项目根目录的 `README.md`、`DEV_GUIDE.md`

## 公共视图
- `course_frontend/course_frontend/src/views/home/index.vue`
- `course_frontend/course_frontend/src/views/home/components/EditForm.vue`
- `course_frontend/course_frontend/src/views/home/components/MainTable.vue`
- `course_frontend/course_frontend/src/views/home/config/index.js`
- `course_frontend/course_frontend/src/views/home/config/rules.js`

## 跨板块（合并 PR / 解决冲突时使用）
- **所有** Controller / Service / Mapper / POJO / DTO / 前端 view / api 文件

# ⚠️ 必须先通知再触碰的文件
修改下列文件之前，必须先在群里通知相关组员：
- `pojo/BaseBean.java`：新增公共字段时通知全员
- `router/index.js`：新增/删除路由时通知全员
- `utils/CommonResult.java`：修改返回结构时通知全员
- `pom.xml`：新增依赖时通知全员
- 数据库表结构：通知全员

# 🚫 普通开发任务不应触碰的文件
## 组员 B1 / B2 的作业板块（除非合并冲突，否则不碰）
- B1：`views/teacher/HomeworkSubmissions.vue`、`views/teacher/GradeDetail.vue` 及 `HomeworkTeacherController.java`、`IHomeworkService.java` 等
- B2：`views/student/StudentHomeworkDetail.vue`、`api/comment.js`、`HomeworkStudentController.java`、`HomeworkCommentController.java` 等

## 组员 A 的课程板块
- `views/course/MyCourses.vue`、`views/course/CourseDetail.vue`、`api/course.js` 及所有 `Course*` 控制器/服务/Mapper
- ⚠️ `MyCourses.vue` / `CourseDetail.vue` 是教师/学生共用页面，B1/B2 想在里面加作业相关 UI 必须通过 `<slot>` 提交需求

## 组员 C 的认证板块
- `views/login/LoginIndex.vue`、`store/user.js`、`api/auth.js` 及所有 `Auth*` 控制器/服务/Mapper

## 组员的 Controller / Service / Mapper / POJO 文件
- 三个组员的业务代码（仅在合并冲突时按规则处理）

# 协作约束
- 修改全局配置前，先用 Read 工具确认当前内容。
- 修改 `BaseBean`、`CommonResult` 等公共类时，先在群里发预告，再开始编辑。
- 每次合并 PR 后，**自动在群里通知相关组员**。
- 自身开发时，与其他组员一样遵守"只能改白名单"原则，避免自己修改自己板块外的文件。
- 当我让你修改全局配置时，先提示我评估影响范围，再决定是否继续。
````

---

## 九、每周站会模板（组长主持）

每周一早上 10:00 群里发：

```
【第 X 周站会 - 组长】

📊 本周目标：
1. 完成 xxx 模块
2. 解决 xxx 技术难点

👥 分工确认：
- 组员 A：xxx
- 组员 B1 / B2：xxx（作业板块已拆分）
- 组员 C：xxx

⚠️ 风险预警：
1. xxx

❓ 需要协调的资源：
1. xxx
```

---

## 十、紧急情况处理

| 情况 | 处理方式 |
|------|----------|
| 组员 PR 阻塞超过 24 小时 | 主动联系组员，必要时临时接手 |
| `dev` 分支无法构建 | 立即回滚到上一个稳定版本 |
| 多人改同一文件 | 群里@全员，强制指定一人合并 |
| 数据库结构变更 | 先在群里讨论，再出迁移脚本 |
| 发现全组通用 Bug | 自己修，并写一份 `BUG_FIX_NOTES.md` |

---

## 十一、组长每日工作清单

- [ ] 9:00 同步 dev 分支
- [ ] 9:30 检查组员 PR 通知（重点关注 B1 / B2 是否有冲突）
- [ ] 10:00 每周一开站会（仅周一）
- [ ] 12:00 处理组员中午提出的 PR
- [ ] 15:00 自己开发 2 小时（组长专属板块）
- [ ] 18:00 处理组员下午提出的 PR（B1 和 B2 的 PR 需分开 review）
- [ ] 21:00 接收组员进度同步，更新 `dev/progress.md`