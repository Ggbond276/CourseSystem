# 课堂管理系统 — 团队开发作战指挥手册

> **【警告】严禁修改全局框架配置，任何报错请立即同步组长，保持代码规范（严禁复杂三元运算）。**

---

## 一、团队角色与职责定义

### 1.1 组长的权威职责

组长在本项目中承担**架构设计者与最终守门人**的双重角色：

| 职责 | 说明 |
|---|---|
| **整体架构设计** | 负责系统分层、目录结构、模块划分、API 契约制定 |
| **统筹合并** | 审查并合并所有组员的 Pull Request，拥有最终否决权 |
| **冲突仲裁** | 当多人修改同一文件产生冲突时，由组长统一裁决 |
| **规范执行** | 监督代码规范（禁止复杂三元运算、禁止生僻语法、生僻缩写） |

### 1.2 组员的行动边界

> **红线：以下文件/目录属于全局框架配置，严禁任何组员私自修改。违者取消本次提交并全组通报。**

**前端禁止修改区：**

| 文件 / 目录 | 路径 | 禁止修改原因 |
|---|---|---|
| `main.js` | `course_frontend/src/main.js` | Vue 应用入口，已注册 Router / Pinia / ElementPlus |
| `App.vue` | `course_frontend/src/App.vue` | 根组件，`<router-view>` 挂载点已就绪 |
| `vite.config.js` | `course_frontend/vite.config.js` | 跨域代理、端口、路径别名已设定，修改会导致联调失败 |
| `package.json` | `course_frontend/package.json` | 依赖版本已锁定 |
| `router/index.js` | `course_frontend/src/router/index.js` | **仅组长可修改**，新增路由须向组长申请 |

**后端禁止修改区：**

| 文件 / 目录 | 路径 | 禁止修改原因 |
|---|---|---|
| `ServerApplication.java` | `server/.../ServerApplication.java` | Spring Boot 启动类，`@MapperScan` 配置已固定 |
| `application.yml` | `server/.../resources/application.yml` | 数据库连接、端口、MyBatis-Plus 配置已设定 |
| `CorsConfig.java` | `server/.../config/CorsConfig.java` | 全局跨域配置已就绪 |
| `WebExceptionHandler.java` | `server/.../handler/WebExceptionHandler.java` | 全局异常处理器，统一返回格式已定义 |
| `CommonResult.java` | `server/.../utils/CommonResult.java` | 统一响应结构，所有接口必须通过它返回 |
| `BaseBean.java` | `server/.../pojo/BaseBean.java` | 所有实体类父类，公共字段已定义 |

---

## 二、组员开发分工表

> 每人严格按照自己负责的板块开发，不跨板块修改他人文件。如需跨板块协作，请先与对应组员沟通并告知组长。

| 组员 ID | 负责前端模块 | 负责后端板块 | 每日进度要求 |
|---|---|---|---|
| **组员 A** | `views/teacher/` 课程管理页面、`api/course.js` | `controller/CourseTeacherController`、`controller/CourseStudentController`、`service/ICourseService`、`service/IStudentCourseService`、对应的 ServiceImpl、Mapper、POJO、DTO | 每日至少完成 **1 个接口的前后端联调**，并在群里截图汇报 |
| **组员 B** | `views/teacher/` 作业管理页面、`views/student/` 作业页面、`api/homework.js`、`api/comment.js` | `controller/HomeworkTeacherController`、`controller/HomeworkStudentController`、`controller/HomeworkCommentController`、对应的 ServiceImpl、Mapper、POJO、DTO | 每日至少完成 **1 个接口的前后端联调**，并在群里截图汇报 |
| **组员 C** | `views/login/`、`store/user.js`、`api/auth.js` | `controller/AuthController`、`service/IAuthService`、`pojo/User`、`pojo/UserCourse`、`mapper/UserMapper`、`mapper/UserCourseMapper` | 优先完成登录鉴权模块，**每日与组长同步进度** |

### 2.1 各板块详细目录映射

**板块 A — 课程管理：**

```
前端：
  src/views/teacher/CourseManage.vue        ← 组员 A
  src/views/teacher/CreateCourse.vue        ← 组员 A
  src/views/student/MyCourse.vue            ← 组员 A（学生端入口）
  src/api/course.js                        ← 组员 A

后端：
  controller/CourseTeacherController.java   ← 组员 A
  controller/CourseStudentController.java  ← 组员 A
  service/ICourseService.java              ← 组员 A
  service/impl/CourseServiceImpl.java     ← 组员 A
  service/IStudentCourseService.java       ← 组员 A
  service/impl/StudentCourseServiceImpl.java ← 组员 A
  mapper/UserCourseMapper.java             ← 组员 A
  pojo/UserCourse.java                     ← 组员 C（User 板块已覆盖）
  dto/（课程相关 DTO）                     ← 组员 A
```

**板块 B — 作业与提交：**

```
前端：
  src/views/teacher/CourseHomework.vue     ← 组员 B
  src/views/teacher/CreateHomework.vue     ← 组员 B
  src/views/teacher/HomeworkSubmissions.vue ← 组员 B
  src/views/student/StudentHomework.vue    ← 组员 B
  src/views/student/StudentHomeworkDetail.vue ← 组员 B
  src/api/homework.js                      ← 组员 B
  src/api/comment.js                       ← 组员 B

后端：
  controller/HomeworkTeacherController.java ← 组员 B
  controller/HomeworkStudentController.java ← 组员 B
  controller/HomeworkCommentController.java ← 组员 B
  service/IHomeworkService.java            ← 组员 B
  service/impl/HomeworkServiceImpl.java    ← 组员 B
  service/IStudentHomeworkService.java     ← 组员 B
  service/impl/StudentHomeworkServiceImpl.java ← 组员 B
  service/IHomeworkCommentService.java     ← 组员 B
  service/impl/HomeworkCommentServiceImpl.java ← 组员 B
  mapper/HomeworkMapper.java               ← 组员 B
  mapper/HomeworkSubmitMapper.java         ← 组员 B
  mapper/HomeworkCommentMapper.java        ← 组员 B
  pojo/Homework.java                       ← 组员 B
  pojo/HomeworkSubmit.java                 ← 组员 B
  pojo/HomeworkComment.java               ← 组员 B
  dto/（作业相关 DTO）                     ← 组员 B
  lang/HomeworkCondition.java              ← 组员 B
  lang/HomeworkSubmitCondition.java        ← 组员 B
```

**板块 C — 用户与鉴权：**

```
前端：
  src/views/login/LoginIndex.vue            ← 组员 C
  src/store/user.js                        ← 组员 C
  src/api/auth.js                          ← 组员 C

后端：
  controller/AuthController.java            ← 组员 C
  service/IAuthService.java               ← 组员 C
  service/impl/AuthServiceImpl.java       ← 组员 C
  mapper/UserMapper.java                   ← 组员 C
  pojo/User.java                          ← 组员 C
```

---

## 三、开发者专属"直达后门"（跳过登录开发指南）

> **⚠️ 重要警告：以下操作仅限本地开发环境使用。严禁将任何模拟登录逻辑写入正式代码库，违者取消提交资格并全组通报。**

### 3.1 适用场景

- 前端组员在开发页面 UI 和交互逻辑时，不需要每次都手动调用登录接口。
- 后端组员在 Postman / 接口测试工具中尚未完成鉴权中间件时，可直接跳过 token 校验进行接口调试。

### 3.2 前端跳过登录操作步骤

**Step 1.** 在浏览器中打开前端页面（`http://localhost:5173`），按 `F12` 打开开发者工具。

**Step 2.** 切换到 **Console**（控制台）标签页，粘贴以下代码并回车：

```javascript
localStorage.setItem('token', 'dev_mock_token');
localStorage.setItem('userId', '1');
localStorage.setItem('name', '测试用户');
localStorage.setItem('avatar', '');
localStorage.setItem('phone', '18300000000');
```

**Step 3.** 在浏览器地址栏直接输入目标路径，如：
- `http://localhost:5173/teacher/course-manage` — 教师课程管理页
- `http://localhost:5173/student/my-course` — 学生我的课程页

**Step 4.** 页面将直接加载，不会被路由守卫拦截到登录页。

### 3.3 后端跳过登录（Postman 测试）

在完成 JWT 鉴权中间件之前，所有接口可以直接不带 `token` 参数进行测试。完成鉴权中间件后，请按以下方式在 Postman 中添加 Header：

```
Header Key:   Authorization
Header Value: Bearer dev_mock_token
```

### 3.4 后门清理规范

> **每次正式提 PR 之前，必须检查以下两项：**

1. **前端代码中不得出现** `localStorage.setItem('token'` 或任何模拟 token 的硬编码字符串。
2. **后端代码中不得出现** 跳过 token 校验的"快捷逻辑"（如 `if (token == null) return success;`）。
3. 任何人均可在群里@对应组员要求代码审查，发现违规代码立即回退。

---

## 四、Git 代码合并规范

### 4.1 分支管理策略

| 分支类型 | 命名规范 | 权限 |
|---|---|---|
| 主干分支 | `main` | 仅组长可推送 |
| 开发分支 | `dev` | 组长统一维护 |
| 功能分支 | `feature/<板块>-<姓名>` | 组员自建自用 |

**功能分支命名示例：**
```
feature/course-zhuyihang    ← 组员 A（课程板块）
feature/homework-zhangsan   ← 组员 B（作业板块）
feature/auth-lisi           ← 组员 C（鉴权板块）
```

### 4.2 每日开发流程

```
1. 每天开工前：git switch dev && git pull origin dev（确保基于最新代码开发）
2. 创建/切换功能分支：git switch -c feature/<板块>-<姓名>
3. 在认领的模块目录内开发：views/<板块>、service/<板块>、mapper/<板块>
4. 每日收工前：git add . && git commit -m "【模块名】+【完成功能简述】"
5. 推送：git push -u origin feature/<板块>-<姓名>
6. 在群里通知组长申请合并
```

### 4.3 提交信息格式规范

> 每次 commit 必须使用以下格式，格式不符的提交组长有权拒绝合并。

```
【模块名】完成功能简述
```

**正确示例：**

```
【课程-教师端】创建了 CourseTeacherController 接口骨架
【作业-学生端】实现了 getStudentHomeworkList 方法
【作业-讨论区】补全了 HomeworkComment 相关 POJO 和 Mapper
【登录】完成了 AuthController 登录接口实现
```

**错误示例（禁止）：**

```
fix bug
update
merge
完成了
wip
```

### 4.4 Pull Request 合并规范

1. **自检清单**（合表前必须全部通过）：
   - [ ] 所有 `TODO` 占位方法已替换为正式实现，或确认保留 TODO 并注明预计完成时间
   - [ ] 代码中不存在 `? :` 三元运算符
   - [ ] 不存在 `getCL` 等缩写命名，全为完整英文单词
   - [ ] 所有 Controller 方法均无业务逻辑，只调用 Service
   - [ ] 所有前端 View 均通过 `api/` 目录下的函数发起请求，无直接 Axios 调用
   - [ ] 不涉及任何禁止修改区文件
   - [ ] `git pull origin dev` 无冲突

2. **PR 标题格式**：`【合并】<板块名>-<姓名>：<本次完成功能>`

3. **组长审查维度**：
   - 代码规范（无三元运算、无缩写命名）
   - 架构合规（Controller 无业务逻辑、View 无直接 Axios）
   - 接口契约一致性（响应格式符合 API 文档）
   - 无全局配置泄露

### 4.5 冲突处理规范

若 `git pull origin dev` 时发生文件冲突：

1. **禁止强制覆盖**：严禁使用 `git push -f` 强制推送
2. **保留组长代码**：冲突文件中，以组长在 `dev` 分支的版本为准
3. **立即上报**：冲突发生后 10 分钟内，在群里@组长说明冲突文件
4. **组长仲裁**：组长在 `dev` 分支解决冲突后，组员再 rebase 到最新版本

---

## 五、每日站会汇报模板

> 每位组员每日 20:00 前在群里按以下格式汇报：

```
【日进度】<姓名>
昨日完成：[功能点 A]、[功能点 B]
今日计划：[功能点 C]、[功能点 D]
阻塞问题：[有/无] — 若有请说明具体情况
```

---

*本手册由组长制定并解释，修改须经组长批准后执行。*
