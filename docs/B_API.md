# 组员 B 接口文档 — 作业板块（教师端 + 学生端 + 评论）

> 📌 本文档专为 Postman 后端联调设计，每个接口都包含 **完整 URL + Method + Headers + JSON Body 示例 + 响应示例**，复制即可测试。

---

## 0. 测试前必读

### 0.1 后端服务地址
```
http://localhost:8888
```

### 0.2 Postman 环境变量

在 Postman 左侧 `Environments` 新建 `CourseSystem-Local`，加两个变量：

| 变量名 | 初始值 | 说明 |
|--------|--------|------|
| `baseUrl` | `http://localhost:8888` | 后端根地址 |
| `token` | （登录后填入） | 登录后从响应里 `data.token` 复制粘贴进来 |

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
  "data": ...
}
```
- `code = 200`：成功
- `code = 500`：业务异常
- `code = 401`：未登录或 token 失效

### 0.5 状态码字典（本板块）

| 字段 | 含义 | 取值 |
|------|------|------|
| `homework.type` | 作业模式 | `1` 个人作业、`2` 小组作业 |
| `homework.forbidLate` | 截止策略 | `0` 允许超时提交、`1` 禁止超时提交 |
| `homeworkSubmit.status` | 提交状态 | `0` 未交、`1` 已提交待批阅、`2` 已批改、`3` 打回重做 |
| `homework.activityTag` | 应用环节 | `课前` / `课中` / `课后` / `期末` 等自定义字符串 |
| `homeworkComment.isAnonymous` | 匿名发表 | `0` 实名、`1` 匿名 |

---

## 1. 教师端作业接口

### 1.1 发布新作业

| 项 | 值 |
|----|----|
| **URL** | `{{baseUrl}}/homework/teacher/create` |
| **Method** | `POST` |
| **鉴权** | ✅ 需要教师 token |

#### 请求 JSON
```json
{
  "courseId": "1856321478523691000",
  "title": "第一章 课后练习",
  "description": "请完成课本第 1 章课后习题 1-10，拍照上传",
  "type": 1,
  "activityTag": "课后",
  "totalScore": 100,
  "forbidLate": 1,
  "deadline": "2024-12-31 23:59:59",
  "attachments": [
    {
      "name": "作业模板.docx",
      "url": "https://example.com/file/template.docx"
    }
  ]
}
```

#### 字段说明

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `courseId` | String | ✅ | 课程 ID（雪花算法，必须加引号） |
| `title` | String | ✅ | 作业标题 |
| `description` | String | ❌ | 作业要求描述 |
| `type` | Integer | ✅ | `1` 个人作业，`2` 小组作业 |
| `activityTag` | String | ❌ | 应用环节标签 |
| `totalScore` | Integer | ✅ | 满分值 |
| `forbidLate` | Integer | ✅ | `0` 允许超时，`1` 禁止超时 |
| `deadline` | String | ✅ | 截止时间，格式 `yyyy-MM-dd HH:mm:ss` |
| `attachments` | Array | ❌ | 老师附件列表 |
| `attachments[].name` | String | ❌ | 附件原始名 |
| `attachments[].url` | String | ❌ | 附件存储 URL |

#### 成功响应示例
```json
{
  "code": 200,
  "msg": "作业发布并分发完毕",
  "data": {
    "homeworkId": "1856321478523692000"
  }
}
```

#### 复制到 Postman 的 cURL 命令
```bash
curl -X POST 'http://localhost:8888/homework/teacher/create' \
  -H 'Content-Type: application/json' \
  -H 'Authorization: Bearer {{token}}' \
  -d '{
    "courseId": "1856321478523691000",
    "title": "第一章 课后练习",
    "description": "请完成课本第 1 章课后习题 1-10",
    "type": 1,
    "activityTag": "课后",
    "totalScore": 100,
    "forbidLate": 1,
    "deadline": "2024-12-31 23:59:59",
    "attachments": [
      {"name": "作业模板.docx", "url": "https://example.com/file/template.docx"}
    ]
  }'
```

---

### 1.2 教师作业列表

| 项 | 值 |
|----|----|
| **URL** | `{{baseUrl}}/homework/teacher/list?courseId=1856321478523691000` |
| **Method** | `GET` |
| **鉴权** | ✅ 需要教师 token |

> ⚠️ `courseId` 是 **Query 参数**（URL 问号后面），不是 Body。

#### 成功响应示例
```json
{
  "code": 200,
  "msg": "获取成功",
  "data": [
    {
      "homeworkId": "1856321478523692000",
      "title": "第一章 课后练习",
      "activityTag": "课后",
      "deadline": "2024-12-31 23:59:59",
      "isOver": false,
      "gradedCount": 12,
      "ungradedCount": 8,
      "unsubmittedCount": 22
    },
    {
      "homeworkId": "1856321478523692001",
      "title": "实验报告 1",
      "activityTag": "课中",
      "deadline": "2024-11-30 23:59:59",
      "isOver": true,
      "gradedCount": 42,
      "ungradedCount": 0,
      "unsubmittedCount": 0
    }
  ]
}
```

#### 字段说明
| 字段 | 类型 | 说明 |
|------|------|------|
| `isOver` | Boolean | true 已截止，false 未截止 |
| `gradedCount` | Integer | 已批改数 |
| `ungradedCount` | Integer | 待批改数 |
| `unsubmittedCount` | Integer | 未提交数 |

---

### 1.3 教师批阅大厅：作业提交明细

| 项 | 值 |
|----|----|
| **URL** | `{{baseUrl}}/homework/teacher/submit-list?homeworkId=1856321478523692000&status=1&studentName=张` |
| **Method** | `GET` |
| **鉴权** | ✅ 需要教师 token |

#### Query 参数
| 参数 | 必填 | 说明 |
|------|------|------|
| `homeworkId` | ✅ | 作业 ID |
| `status` | ❌ | 筛选提交状态：`0`未交 / `1`已提交 / `2`已批改 / `3`打回重做 |
| `studentName` | ❌ | 学生姓名模糊搜索 |

> 💡 全部参数都可省略，省略时不筛选。

#### 成功响应示例
```json
{
  "code": 200,
  "msg": "获取成功",
  "data": [
    {
      "submitId": "1856321478523693000",
      "studentName": "张三",
      "studentAccount": "2024001001",
      "status": 1,
      "submitTime": "2024-12-25 14:32:18",
      "similarity": 12.5,
      "wordCount": 856,
      "score": null
    },
    {
      "submitId": "1856321478523693001",
      "studentName": "李四",
      "studentAccount": "2024001002",
      "status": 2,
      "submitTime": "2024-12-25 15:11:02",
      "similarity": 5.3,
      "wordCount": 1024,
      "score": 92
    }
  ]
}
```

---

### 1.4 教师批阅打分

| 项 | 值 |
|----|----|
| **URL** | `{{baseUrl}}/homework/teacher/grade` |
| **Method** | `POST` |
| **鉴权** | ✅ 需要教师 token |

#### 请求 JSON
```json
{
  "submitId": "1856321478523693000",
  "score": 95,
  "teacherComment": "完成度很高，建议注意第 3 题的边界条件",
  "status": 2
}
```

#### 字段说明

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `submitId` | String | ✅ | 提交记录 ID |
| `score` | Integer | ✅ | 分数（0 - totalScore） |
| `teacherComment` | String | ❌ | 评语 |
| `status` | Integer | ✅ | `2` 已批改（默认），`3` 打回重做 |

#### 成功响应示例
```json
{
  "code": 200,
  "msg": "批阅成功",
  "data": true
}
```

---

## 2. 学生端作业接口

### 2.1 学生作业列表

| 项 | 值 |
|----|----|
| **URL** | `{{baseUrl}}/homework/student/list?courseId=1856321478523691000` |
| **Method** | `GET` |
| **鉴权** | ✅ 需要学生 token |

#### 成功响应示例
```json
{
  "code": 200,
  "msg": "获取成功",
  "data": [
    {
      "homeworkId": "1856321478523692000",
      "title": "第一章 课后练习",
      "activityTag": "课后",
      "deadline": "2024-12-31 23:59:59",
      "status": 1,
      "score": null
    },
    {
      "homeworkId": "1856321478523692001",
      "title": "实验报告 1",
      "activityTag": "课中",
      "deadline": "2024-11-30 23:59:59",
      "status": 2,
      "score": 92
    }
  ]
}
```

#### 字段说明
| 字段 | 类型 | 说明 |
|------|------|------|
| `status` | Integer | 该学生的提交状态 |
| `score` | Integer | 该学生的得分，未批改为 null |

---

### 2.2 学生作业详情

| 项 | 值 |
|----|----|
| **URL** | `{{baseUrl}}/homework/student/detail?homeworkId=1856321478523692000` |
| **Method** | `GET` |
| **鉴权** | ✅ 需要学生 token |

#### 成功响应示例
```json
{
  "code": 200,
  "msg": "详情加载完毕",
  "data": {
    "homeworkId": "1856321478523692000",
    "title": "第一章 课后练习",
    "description": "请完成课本第 1 章课后习题 1-10",
    "deadline": "2024-12-31 23:59:59",
    "totalScore": 100,
    "attachments": [
      {"name": "作业模板.docx", "url": "https://example.com/file/template.docx"}
    ],
    "mySubmission": {
      "submitId": "1856321478523693000",
      "status": 1,
      "content": "我的答案...",
      "attachments": [
        {"name": "我的作业.pdf", "url": "https://example.com/file/answer.pdf"}
      ],
      "submitTime": "2024-12-25 14:32:18",
      "score": null,
      "teacherComment": null
    }
  }
}
```

#### 字段说明
- `mySubmission` 为 `null` 表示该学生还没提交过
- `attachments` 是 JSON 字符串，需要后端解析为对象数组
- `status` 含义见文档头 0.5 节

---

### 2.3 学生提交作业

| 项 | 值 |
|----|----|
| **URL** | `{{baseUrl}}/homework/student/submit` |
| **Method** | `POST` |
| **鉴权** | ✅ 需要学生 token |

#### 请求 JSON
```json
{
  "homeworkId": "1856321478523692000",
  "content": "我的作答内容如下：\n1. xxx\n2. yyy",
  "attachments": [
    {
      "name": "我的作业.pdf",
      "url": "https://example.com/file/answer.pdf"
    }
  ]
}
```

#### 字段说明
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `homeworkId` | String | ✅ | 作业 ID |
| `content` | String | ✅ | 文本作答内容 |
| `attachments` | Array | ❌ | 学生附件列表 |
| `attachments[].name` | String | ❌ | 附件原始名 |
| `attachments[].url` | String | ❌ | 附件 URL |

#### 成功响应示例
```json
{
  "code": 200,
  "msg": "作业提交成功",
  "data": true
}
```

#### 失败响应示例（已截止）
```json
{
  "code": 500,
  "msg": "作业已截止，禁止超时提交",
  "data": null
}
```

---

## 3. 作业评论接口（教师 / 学生共用）

### 3.1 作业评论列表

| 项 | 值 |
|----|----|
| **URL** | `{{baseUrl}}/homework/comment/list?homeworkId=1856321478523692000` |
| **Method** | `GET` |
| **鉴权** | ✅ 需要登录 |

#### 成功响应示例
```json
{
  "code": 200,
  "msg": "加载成功",
  "data": [
    {
      "commentId": "1856321478523694000",
      "userName": "张老师",
      "userAvatar": "https://example.com/avatar/teacher1.jpg",
      "content": "这道题有什么疑问可以在这里讨论",
      "createTime": "2024-12-25 10:00:00"
    },
    {
      "commentId": "1856321478523694001",
      "userName": "匿名用户",
      "userAvatar": null,
      "content": "请问第 5 题的第二问怎么做？",
      "createTime": "2024-12-25 10:15:32"
    }
  ]
}
```

> 💡 `is_anonymous=1` 时，前端展示为"匿名用户"，后端不返回真实姓名。

---

### 3.2 发表作业评论

| 项 | 值 |
|----|----|
| **URL** | `{{baseUrl}}/homework/comment/add` |
| **Method** | `POST` |
| **鉴权** | ✅ 需要登录 |

#### 请求 JSON
```json
{
  "homeworkId": "1856321478523692000",
  "content": "请问第 5 题的第二问怎么做？",
  "isAnonymous": 1
}
```

#### 字段说明
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `homeworkId` | String | ✅ | 作业 ID |
| `content` | String | ✅ | 评论内容，不能为空 |
| `isAnonymous` | Integer | ❌ | `0` 实名（默认），`1` 匿名 |

#### 成功响应示例
```json
{
  "code": 200,
  "msg": "发表评论成功",
  "data": {
    "commentId": "1856321478523694002",
    "createTime": "2024-12-25 16:22:10"
  }
}
```

---

## 4. 完整测试流程（推荐新手照着跑一遍）

按以下顺序调用，可以走通作业板块的全部场景：

```
步骤 1：登录
  POST {{baseUrl}}/auth/login
  → 拿 token，存到环境变量

步骤 2：教师建课
  POST {{baseUrl}}/course/teacher/create
  → 拿到 courseId 和 joinCode

步骤 3：学生用 joinCode 加入
  POST {{baseUrl}}/course/student/join
  Body: { "joinCode": "<步骤2拿到的>" }

步骤 4：教师发布作业
  POST {{baseUrl}}/homework/teacher/create
  → 拿到 homeworkId

步骤 5：学生提交作业
  POST {{baseUrl}}/homework/student/submit
  → status 自动变为 1

步骤 6：教师批阅打分
  POST {{baseUrl}}/homework/teacher/grade
  → status 变为 2

步骤 7：学生查看作业详情
  GET {{baseUrl}}/homework/student/detail?homeworkId=...
  → 看到 score 已经有值

步骤 8：学生发评论 / 教师发评论
  POST {{baseUrl}}/homework/comment/add

步骤 9：所有人查看评论列表
  GET {{baseUrl}}/homework/comment/list?homeworkId=...
```

---

## 5. Postman 集合导入 JSON

```json
{
  "info": {
    "name": "CourseSystem-B-作业板块",
    "_postman_id": "homework-b-001",
    "description": "组员 B 负责的作业板块接口集合",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "1.1 教师-发布作业",
      "request": {
        "method": "POST",
        "header": [{"key": "Content-Type", "value": "application/json"}],
        "url": "{{baseUrl}}/homework/teacher/create",
        "body": {
          "mode": "raw",
          "raw": "{\n  \"courseId\": \"1856321478523691000\",\n  \"title\": \"第一章 课后练习\",\n  \"description\": \"请完成课本第 1 章课后习题 1-10\",\n  \"type\": 1,\n  \"activityTag\": \"课后\",\n  \"totalScore\": 100,\n  \"forbidLate\": 1,\n  \"deadline\": \"2024-12-31 23:59:59\",\n  \"attachments\": [{\"name\": \"作业模板.docx\", \"url\": \"https://example.com/file/template.docx\"}]\n}"
        }
      }
    },
    {
      "name": "1.2 教师-作业列表",
      "request": {
        "method": "GET",
        "url": "{{baseUrl}}/homework/teacher/list?courseId=1856321478523691000"
      }
    },
    {
      "name": "1.3 教师-批阅大厅",
      "request": {
        "method": "GET",
        "url": "{{baseUrl}}/homework/teacher/submit-list?homeworkId=1856321478523692000&status=1"
      }
    },
    {
      "name": "1.4 教师-批阅打分",
      "request": {
        "method": "POST",
        "header": [{"key": "Content-Type", "value": "application/json"}],
        "url": "{{baseUrl}}/homework/teacher/grade",
        "body": {
          "mode": "raw",
          "raw": "{\n  \"submitId\": \"1856321478523693000\",\n  \"score\": 95,\n  \"teacherComment\": \"完成度很高\",\n  \"status\": 2\n}"
        }
      }
    },
    {
      "name": "2.1 学生-作业列表",
      "request": {
        "method": "GET",
        "url": "{{baseUrl}}/homework/student/list?courseId=1856321478523691000"
      }
    },
    {
      "name": "2.2 学生-作业详情",
      "request": {
        "method": "GET",
        "url": "{{baseUrl}}/homework/student/detail?homeworkId=1856321478523692000"
      }
    },
    {
      "name": "2.3 学生-提交作业",
      "request": {
        "method": "POST",
        "header": [{"key": "Content-Type", "value": "application/json"}],
        "url": "{{baseUrl}}/homework/student/submit",
        "body": {
          "mode": "raw",
          "raw": "{\n  \"homeworkId\": \"1856321478523692000\",\n  \"content\": \"我的作答内容\",\n  \"attachments\": [{\"name\": \"我的作业.pdf\", \"url\": \"https://example.com/file/answer.pdf\"}]\n}"
        }
      }
    },
    {
      "name": "3.1 评论-列表",
      "request": {
        "method": "GET",
        "url": "{{baseUrl}}/homework/comment/list?homeworkId=1856321478523692000"
      }
    },
    {
      "name": "3.2 评论-发表",
      "request": {
        "method": "POST",
        "header": [{"key": "Content-Type", "value": "application/json"}],
        "url": "{{baseUrl}}/homework/comment/add",
        "body": {
          "mode": "raw",
          "raw": "{\n  \"homeworkId\": \"1856321478523692000\",\n  \"content\": \"请问第 5 题怎么做？\",\n  \"isAnonymous\": 1\n}"
        }
      }
    }
  ]
}
```

---

## 6. 常见问题

**Q1：作业列表返回空？**
A：检查 `courseId` 是否正确；检查该课程下是否真的发布过作业（`courseId` 是字符串，雪花 ID 必须加引号）。

**Q2：提交作业时提示 "作业已截止"？**
A：作业 `deadline` 已经过了当前时间，或者 `forbidLate=1` 且超过了 deadline。

**Q3：教师批阅打分后学生看不到分数？**
A：批阅接口 `status` 字段必须传 `2`（已批改），传错状态会让学生端仍然显示"待批阅"。

**Q4：评论接口哪些人能调用？**
A：教师和学生都可以，但必须登录。匿名评论建议学生用，实名评论教师用。