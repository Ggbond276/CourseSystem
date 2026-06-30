# 课堂管理系统 - 组员开发规范说明

> 本项目是"课堂管理系统"教学平台，由团队协作开发。以下规范旨在**明确分工边界、防止代码冲突**，让每位组员都能在自己的区域自由发挥，同时保证公共基础设施的稳定。

---

## 一、项目整体架构

```
CourseSystem/
├── course_frontend/          前端项目（Vue 3 + Vite + Element Plus）
│   └── course_frontend/
│       └── src/
│           ├── api/          API 请求函数（各模块对应的接口调用）
│           ├── store/        Pinia 状态管理（用户登录态等全局状态）
│           ├── router/       路由配置（页面导航与权限守卫）
│           ├── views/        页面视图组件（业务代码区，按角色分子目录）
│           └── ...
│
├── server/                   后端项目（Spring Boot 2.7 + MyBatis-Plus）
│   └── server/
│       └── src/main/
│           └── java/com/coursemanager/
│               ├── controller/   控制器层（接参、校验、路由）
│               ├── service/     业务逻辑层（所有业务处理在这里）
│               ├── mapper/      数据持久层（数据库操作）
│               ├── pojo/        实体类（数据库表映射）
│               ├── dto/         数据传输对象（请求/响应专用）
│               ├── lang/        查询条件类（分页、条件构造）
│               ├── utils/       工具类
│               └── config/      全局配置
│
└── README.md
```

---

## 二、前端目录规范

### 2.1 严禁组员修改的公共配置区

| 文件 / 目录 | 严禁修改原因 |
|---|---|
| `vite.config.js` | 跨域代理配置、端口、路径别名均已设定，修改会导致联调失败 |
| `package.json` | 依赖版本已锁定，新增依赖需全体确认 |
| `src/main.js` | Vue 应用入口，注册了 Pinia/Router/ElementPlus，结构固定 |
| `src/App.vue` | 根组件，`<router-view>` 挂载点已就绪 |

### 2.2 组员各自认领的业务代码区

> 前端 `src/views/` 下的目录划分，**按角色职能分工**，每人只管自己的目录，互不干扰。
>
> 📝 **2026-06-28 重构**：前端按「用户旅程驱动 UI」重构，原来的「教师/学生独立页面」合并为「共用页面 + 角色分支 + 弹窗」模型。详细设计见 `docs/UI_REWORK_2026-06-28.md`。

| 目录 | 负责组员 | 包含页面 | 说明 |
|---|---|---|---|
| `views/login/` | 组员 C | `LoginIndex.vue` | 登录注册页，所有用户共用入口 |
| `views/dashboard/` | 组长 | `TeacherDashboard.vue`、`StudentDashboard.vue` | 教师/学生工作台 |
| `views/course/` | 组员 A | `MyCourses.vue`（课程大厅）、`CourseDetail.vue`（课程详情，含作业大厅 tab） | 教师/学生共用，组件内按 role 字段分支渲染 |
| `views/teacher/` | 组员 B | `HomeworkSubmissions.vue`（批阅大厅）、`GradeDetail.vue`（批改作业） | 仅教师作业相关 |
| `views/student/` | 组员 B | `StudentHomeworkDetail.vue`（作业详情 + 提交 + 讨论） | 仅学生作业相关 |

> **注意**：新页面的路由也必须在此文件中注册。路由路径应清晰体现层级，如 `/teacher/dashboard`、`/teacher/course/detail/:id`。
> 路由配置文件位置：`src/router/index.js`

### 2.3 API 请求层（`src/api/`）

API 文件按**后端模块对应拆分**，每个文件对应后端一个 Controller 模块。

| 文件 | 对应后端模块 | 负责人 |
|---|---|---|
| `auth.js` | 认证模块 | 认证开发组员 |
| `course.js` | 课程模块（教师端 + 学生端） | 课程开发组员 |
| `homework.js` | 作业模块（教师端 + 学生端） | 作业开发组员 |
| `comment.js` | 作业讨论区模块 | 作业开发组员 |

> **规则**：views 层禁止直接写 Axios 调用，必须 import 对应的 api 函数。每个接口函数的路径、请求参数格式必须严格参照本文档末尾的 API 契约。

### 2.4 状态管理层（`src/store/`）

| 文件 | 用途 | 负责人 |
|---|---|---|
| `user.js` | 管理登录 token、userId、name、avatar、phone | 认证开发组员 |

> 其他业务状态（如课程列表缓存、作业草稿等），各组员可在自己的 views 目录下自行管理，无需上报。

---

## 三、后端目录规范

### 3.1 严禁组员修改的公共配置区

| 文件 / 目录 | 严禁修改原因 |
|---|---|
| `ServerApplication.java` | Spring Boot 启动类，`@MapperScan` 已配置好 |
| `application.yml` | 数据库连接池、端口、MyBatis-Plus 配置已设定 |
| `config/CorsConfig.java` | 全局跨域配置，所有端口已放行 |
| `handler/WebExceptionHandler.java` | 全局异常处理器，统一返回格式已定义 |
| `utils/CommonResult.java` | 统一响应结构，所有接口必须通过它返回 |
| `pojo/BaseBean.java` | 所有实体类的父类，包含 create_time/update_time |

> **如有配置需求变更**（如增加新的数据库连接、修改端口），必须全员讨论后再动。

### 3.2 组员各自认领的业务代码区

> 后端按**模块分包**，每个模块包含 Controller、Service（接口+实现）、Mapper（接口+XML）三大件。每个人认领自己模块的文件，**不在自己模块内的文件写入操作需谨慎**。

| 后端包路径 | 负责组员 | 包含文件 |
|---|---|---|
| `controller/AuthController.java` | 认证开发组员 | 登录接口 |
| `controller/CourseTeacherController.java` | 教师端开发组员 | 教师创建课程、列表、排序、置顶、详情 |
| `controller/CourseStudentController.java` | 学生端开发组员 | 学生加课、课程列表 |
| `controller/HomeworkTeacherController.java` | 教师端开发组员 | 发布作业、作业列表、批阅大厅、打分 |
| `controller/HomeworkStudentController.java` | 学生端开发组员 | 作业列表、详情、提交 |
| `controller/HomeworkCommentController.java` | 作业开发组员 | 评论列表、发表评论 |

对应的 Service、Mapper、POJO、DTD 由各模块负责人**同步创建**，XML 文件放在 `resources/com/coursemanager/mapper/` 目录下。

---

## 四、API 接口契约（唯一法定合同）

> **所有接口前缀统一为 `/api`**，后端响应格式统一为：
> ```json
> { "code": 200, "msg": "操作成功", "data": {} }
> ```

### 认证模块
- `POST /api/auth/login` - 账号密码登录

### 课程模块（教师端）
- `POST /api/course/teacher/create` - 创建课程
- `GET /api/course/teacher/list` - 获取教师课程列表
- `PUT /api/course/teacher/sort` - 课程排序保存
- `PUT /api/course/teacher/top` - 切换置顶状态
- `GET /api/course/teacher/detail/{courseId}` - 获取课程详情

### 课程模块（学生端）
- `POST /api/course/student/join` - 凭加课码加入课程
- `GET /api/course/student/list` - 获取学生课程列表

### 作业模块（教师端）
- `POST /api/homework/teacher/create` - 发布作业
- `GET /api/homework/teacher/list?courseId=` - 获取作业总览
- `GET /api/homework/teacher/submit-list?homeworkId=&status=&studentName=` - 批阅大厅
- `POST /api/homework/teacher/grade` - 批阅打分

### 作业模块（学生端）
- `GET /api/homework/student/list?courseId=` - 获取作业列表
- `GET /api/homework/student/detail?homeworkId=` - 作业详情
- `POST /api/homework/student/submit` - 提交作业

### 讨论区模块
- `GET /api/homework/comment/list?homeworkId=` - 评论列表
- `POST /api/homework/comment/add` - 发表讨论

---

## 五、Git 协作约定

1. **每人从 main 分支拉取自己的功能分支**，命名规范：`feature/<模块>-<姓名>`，如 `feature/course-teacher-zhuyihang`
2. **严禁向 main 直接推送**，必须通过 Pull Request 合并
3. **禁止提交敏感信息**：`.yml` 中的数据库密码、`node_modules/` 等不要上传
4. **提交前先 `git pull`**：确保本地代码为最新版本后再开发，减少冲突

---

## 六、开发顺序建议

1. **认证模块**：先完成登录（token 生成、鉴权拦截），其他模块才能联调
2. **课程模块**：课程是其他所有功能的前提
3. **作业模块**：作业依赖课程，课程建好再建作业
4. **讨论区**：可与作业同步开发，是作业详情的附属功能

---

*本规范文件由首席架构师制定，如有疑问请在群里讨论。*
