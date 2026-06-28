# 组员 A 接口文档 — 课程板块（教师端 + 学生端）

> 📌 本文档专为 Postman 后端联调设计，每个接口都包含 **完整 URL + Method + Headers + JSON Body 示例 + 响应示例**，复制即可测试。

---

## 0. 测试前必读

### 0.1 后端服务地址
```
http://localhost:8888
```
（端口号见 `server/server/src/main/resources/application.yml` 的 `server.port`）

### 0.2 Postman 环境变量（推荐先建一个环境）

打开 Postman → 左侧 `Environments` → 新建一个环境 `CourseSystem-Local`，加两个变量：

| 变量名 | 初始值 | 说明 |
|--------|--------|------|
| `baseUrl` | `http://localhost:8888` | 后端根地址 |
| `token` | （登录后填入） | 登录后从响应里 `data.token` 复制粘贴进来 |

后续所有接口 URL 都可以写成 `{{baseUrl}}/course/teacher/list`，便于切换环境。

### 0.3 通用请求头

登录后**所有需要鉴权的接口**都要带：
```
Content-Type: application/json
Authorization: Bearer {{token}}
```

### 0.4 通用响应结构
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": ...   // 业务数据，可能为对象、数组、布尔、null
}
```
- `code = 200`：成功
- `code = 500`：业务异常（看 `msg` 提示）
- `code = 401`：未登录或 token 失效

### 0.5 状态码字典（本板块）

| 字段 | 含义 | 取值 |
|------|------|------|
| `course.status` | 课程状态 | `0` 禁用、`1` 启用 |
| `userCourse.role` | 课程角色 | `1` 任课教师、`2` 正式学生、`3` 助教 |
| `userCourse.isTop` | 是否置顶 | `0` 否、`1` 是 |
| `userCourse.sortWeight` | 拖拽权重 | 数字，越大越靠前 |

---

## 1. 教师端课程接口

### 1.1 创建课程

| 项 | 值 |
|----|----|
| **URL** | `{{baseUrl}}/course/teacher/create` |
| **Method** | `POST` |
| **鉴权** | ✅ 需要教师 token |
| **Content-Type** | `application/json` |

#### 请求 JSON
```json
{
  "courseNum": "CS101",
  "courseName": "计算机科学导论",
  "className": "2024级1班",
  "term": "2024-2025 第一学期",
  "period": 48,
  "credit": 3.0,
  "cover": "https://example.com/cover/cs101.jpg"
}
```

#### 字段说明

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `courseNum` | String | ✅ | 课程编号，建议格式：CS101 |
| `courseName` | String | ✅ | 课程名称 |
| `className` | String | ✅ | 教学班级，如 "2024级1班" |
| `term` | String | ✅ | 学年学期 |
| `period` | Integer | ❌ | 学时，默认 0 |
| `credit` | Double | ❌ | 学分，默认 0.0 |
| `cover` | String | ❌ | 课程封面 URL |

#### 成功响应示例
```json
{
  "code": 200,
  "msg": "建课成功",
  "data": {
    "courseId": "1856321478523691000",
    "joinCode": "9B3299"
  }
}
```

#### 复制到 Postman 的 cURL 命令
```bash
curl -X POST 'http://localhost:8888/course/teacher/create' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer {{token}}' \
  -d '{
    "courseNum": "CS101",
    "courseName": "计算机科学导论",
    "className": "2024级1班",
    "term": "2024-2025 第一学期",
    "period": 48,
    "credit": 3.0,
    "cover": "https://example.com/cover/cs101.jpg"
  }'
```

---

### 1.2 教师课程列表

| 项 | 值 |
|----|----|
| **URL** | `{{baseUrl}}/course/teacher/list` |
| **Method** | `GET` |
| **鉴权** | ✅ 需要教师 token |

请求：无需 Body，URL 后也不带参数。

#### 成功响应示例
```json
{
  "code": 200,
  "msg": "查询成功",
  "data": [
    {
      "term": "2024-2025 第一学期",
      "courses": [
        {
          "id": "1856321478523691000",
          "courseNum": "CS101",
          "courseName": "计算机科学导论",
          "className": "2024级1班",
          "joinCode": "9B3299",
          "cover": "https://example.com/cover/cs101.jpg",
          "isTop": 1,
          "sortWeight": 999
        }
      ]
    },
    {
      "term": "2023-2024 第二学期",
      "courses": [
        {
          "id": "1856321478523691001",
          "courseNum": "CS102",
          "courseName": "数据结构",
          "className": "2023级2班",
          "joinCode": "8A1234",
          "isTop": 0,
          "sortWeight": 100
        }
      ]
    }
  ]
}
```

#### 复制到 Postman 的 cURL 命令
```bash
curl -X GET 'http://localhost:8888/course/teacher/list' \
  -H 'Authorization: Bearer {{token}}'
```

---

### 1.3 课程拖拽排序

| 项 | 值 |
|----|----|
| **URL** | `{{baseUrl}}/course/teacher/sort` |
| **Method** | `PUT` |
| **鉴权** | ✅ 需要教师 token |

#### 请求 JSON
```json
{
  "sortedCourseIds": ["1856321478523691000", "1856321478523691001", "1856321478523691002"]
}
```

#### 字段说明
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `sortedCourseIds` | String[] | ✅ | 按新顺序排列的课程 ID 列表，第 1 个排最前 |

> 💡 注意：ID 在 JSON 中必须**加引号**，因为后端用雪花算法生成，序列化为字符串防止精度丢失。

#### 成功响应示例
```json
{
  "code": 200,
  "msg": "排序权重保存成功",
  "data": true
}
```

---

### 1.4 课程置顶/取消置顶

| 项 | 值 |
|----|----|
| **URL** | `{{baseUrl}}/course/teacher/top` |
| **Method** | `PUT` |
| **鉴权** | ✅ 需要教师 token |

#### 请求 JSON
```json
{
  "courseId": "1856321478523691000",
  "isTop": 1
}
```

#### 字段说明
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `courseId` | String | ✅ | 课程 ID |
| `isTop` | Integer | ✅ | `1` 置顶，`0` 取消置顶 |

#### 成功响应示例
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {
    "courseId": "1856321478523691000",
    "isTop": 1
  }
}
```

---

### 1.5 课程详情

| 项 | 值 |
|----|----|
| **URL** | `{{baseUrl}}/course/teacher/detail/1856321478523691000` |
| **Method** | `GET` |
| **鉴权** | ✅ 需要教师 token |

> ⚠️ ID 写在 **URL 路径中**，不是 Body、也不是 Query Param。

#### 成功响应示例
```json
{
  "code": 200,
  "msg": "获取成功",
  "data": {
    "id": "1856321478523691000",
    "courseNum": "CS101",
    "courseName": "计算机科学导论",
    "className": "2024级1班",
    "term": "2024-2025 第一学期",
    "joinCode": "9B3299",
    "cover": "https://example.com/cover/cs101.jpg",
    "memberCount": 42,
    "status": 1
  }
}
```

---

## 2. 学生端课程接口

### 2.1 凭加课码加入课程

| 项 | 值 |
|----|----|
| **URL** | `{{baseUrl}}/course/student/join` |
| **Method** | `POST` |
| **鉴权** | ✅ 需要学生 token |

#### 请求 JSON
```json
{
  "joinCode": "9B3299"
}
```

#### 字段说明
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `joinCode` | String | ✅ | 6 位加课码，区分大小写 |

#### 成功响应示例
```json
{
  "code": 200,
  "msg": "成功加入课程",
  "data": {
    "courseId": "1856321478523691000",
    "courseName": "计算机科学导论"
  }
}
```

#### 失败响应示例（加课码错误）
```json
{
  "code": 500,
  "msg": "加课码不存在或已失效",
  "data": null
}
```

---

### 2.2 学生课程列表

| 项 | 值 |
|----|----|
| **URL** | `{{baseUrl}}/course/student/list` |
| **Method** | `GET` |
| **鉴权** | ✅ 需要学生 token |

#### 成功响应示例
```json
{
  "code": 200,
  "msg": "查询成功",
  "data": [
    {
      "term": "2024-2025 第一学期",
      "courses": [
        {
          "id": "1856321478523691000",
          "courseNum": "CS101",
          "courseName": "计算机科学导论",
          "className": "2024级1班",
          "cover": "https://example.com/cover/cs101.jpg",
          "teacherName": "张老师"
        }
      ]
    }
  ]
}
```

---

## 3. 通用分页课程接口（管理员使用 / 调试用）

> 这套接口主要用于后台管理或调试，目前没在白名单内，但方便测试数据库连接。

### 3.1 分页查询课程

| 项 | 值 |
|----|----|
| **URL** | `{{baseUrl}}/course?pageNum=1&pageSize=10&courseName=CS` |
| **Method** | `GET` |
| **鉴权** | ⚠️ 当前未加拦截（视实际部署而定） |

#### Query 参数
| 参数 | 必填 | 说明 |
|------|------|------|
| `pageNum` | ❌ | 页码，默认 1 |
| `pageSize` | ❌ | 每页大小，默认 10 |
| `id` | ❌ | 课程 ID |
| `courseNum` | ❌ | 课程编号（精确） |
| `courseName` | ❌ | 课程名称（模糊） |
| `period` | ❌ | 学时 |
| `credit` | ❌ | 学分 |
| `status` | ❌ | 课程状态 |
| `startTime` | ❌ | 创建起始时间，格式 `yyyy-MM-dd HH:mm:ss` |
| `endTime` | ❌ | 创建截止时间，格式同上 |

#### 成功响应示例
```json
{
  "code": 200,
  "msg": "查询成功",
  "data": {
    "total": 25,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 3,
    "list": [
      {
        "id": "1856321478523691000",
        "courseNum": "CS101",
        "courseName": "计算机科学导论",
        "period": 48,
        "credit": 3.0,
        "status": 1,
        "className": "2024级1班",
        "term": "2024-2025 第一学期",
        "joinCode": "9B3299",
        "cover": "https://example.com/cover/cs101.jpg",
        "creatorId": "1856321478523690000"
      }
    ]
  }
}
```

### 3.2 新增课程（DTO 方式）

| 项 | 值 |
|----|----|
| **URL** | `{{baseUrl}}/course` |
| **Method** | `POST` |

#### 请求 JSON
```json
{
  "courseName": "算法分析",
  "period": 64,
  "credit": 4.0,
  "status": 1
}
```

> 注：`courseNum` 由后端自动生成，不用传。

### 3.3 修改课程

| 项 | 值 |
|----|----|
| **URL** | `{{baseUrl}}/course` |
| **Method** | `PUT` |

#### 请求 JSON
```json
{
  "id": "1856321478523691000",
  "courseName": "算法分析（改）",
  "period": 64,
  "credit": 4.0,
  "status": 1
}
```

### 3.4 删除课程

| 项 | 值 |
|----|----|
| **URL** | `{{baseUrl}}/course/1856321478523691000` |
| **Method** | `DELETE` |

---

## 4. Postman 集合导入提示

如果你想一次性导入所有接口到 Postman：

1. 打开 Postman → 左上 `Import`
2. 选择 `Raw text`
3. 把下方 JSON 粘贴进去：

```json
{
  "info": {
    "name": "CourseSystem-A-课程板块",
    "_postman_id": "course-a-001",
    "description": "组员 A 负责的课程板块接口集合",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "1.1 教师-创建课程",
      "request": {
        "method": "POST",
        "header": [{"key": "Content-Type", "value": "application/json"}],
        "url": "{{baseUrl}}/course/teacher/create",
        "body": {
          "mode": "raw",
          "raw": "{\n  \"courseNum\": \"CS101\",\n  \"courseName\": \"计算机科学导论\",\n  \"className\": \"2024级1班\",\n  \"term\": \"2024-2025 第一学期\",\n  \"period\": 48,\n  \"credit\": 3.0,\n  \"cover\": \"https://example.com/cover/cs101.jpg\"\n}"
        }
      }
    },
    {
      "name": "1.2 教师-课程列表",
      "request": {
        "method": "GET",
        "url": "{{baseUrl}}/course/teacher/list"
      }
    },
    {
      "name": "1.3 教师-课程排序",
      "request": {
        "method": "PUT",
        "header": [{"key": "Content-Type", "value": "application/json"}],
        "url": "{{baseUrl}}/course/teacher/sort",
        "body": {
          "mode": "raw",
          "raw": "{\n  \"sortedCourseIds\": [\"1856321478523691000\", \"1856321478523691001\"]\n}"
        }
      }
    },
    {
      "name": "1.4 教师-课程置顶",
      "request": {
        "method": "PUT",
        "header": [{"key": "Content-Type", "value": "application/json"}],
        "url": "{{baseUrl}}/course/teacher/top",
        "body": {
          "mode": "raw",
          "raw": "{\n  \"courseId\": \"1856321478523691000\",\n  \"isTop\": 1\n}"
        }
      }
    },
    {
      "name": "1.5 教师-课程详情",
      "request": {
        "method": "GET",
        "url": "{{baseUrl}}/course/teacher/detail/1856321478523691000"
      }
    },
    {
      "name": "2.1 学生-加课码加入",
      "request": {
        "method": "POST",
        "header": [{"key": "Content-Type", "value": "application/json"}],
        "url": "{{baseUrl}}/course/student/join",
        "body": {
          "mode": "raw",
          "raw": "{\n  \"joinCode\": \"9B3299\"\n}"
        }
      }
    },
    {
      "name": "2.2 学生-课程列表",
      "request": {
        "method": "GET",
        "url": "{{baseUrl}}/course/student/list"
      }
    }
  ]
}
```

---

## 5. 常见问题

**Q1：响应里 `code: 401` 怎么办？**
A：token 失效或缺失。重新调用 `/auth/login` 拿新 token，粘贴到环境变量 `token`。

**Q2：ID 字段到底要不要加引号？**
A：**必须加引号**。后端用雪花算法（19 位），超过 JS 数字精度会丢位，所以序列化为 String。

**Q3：日期格式怎么传？**
A：标准格式 `yyyy-MM-dd HH:mm:ss`，例如 `2024-12-31 23:59:59`。

**Q4：调用时提示 "无法访问此网站"？**
A：检查后端是否启动：终端跑 `mvn spring-boot:run` 或在 IDE 跑 `ServerApplication.main()`，控制台看到 `Started ServerApplication in x seconds` 才算启动成功。