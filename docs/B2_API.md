# 组员 B2 接口文档 — 作业板块·学生端 + 作业评论

> 本文档为组员 B2 专用，专职负责学生端作业交互接口和作业评论接口。
> 每个接口都包含 **完整 URL + Method + Headers + JSON Body 示例 + 响应示例**，复制即可测试。
> 协同开发请参考 `B2_homework.md`。

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

### 0.5 状态码字典（本模块）

| 字段 | 含义 | 取值 |
|------|------|------|
| `homeworkSubmit.status` | 提交状态 | `0` 未交、`1` 已提交待批阅、`2` 已批改、`3` 打回重做 |
| `homeworkComment.isAnonymous` | 匿名发表 | `0` 实名、`1` 匿名 |

---

## 1. 学生作业列表

| 项 | 值 |
|----|----|
| **URL** | `{{baseUrl}}/homework/student/list?courseId=1856321478523691000` |
| **Method** | `GET` |
| **鉴权** | 需要学生 token |

### 成功响应示例
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

### 字段说明
| 字段 | 类型 | 说明 |
|------|------|------|
| `status` | Integer | 该学生的提交状态：`0`未交 / `1`已提交待批阅 / `2`已批改 / `3`打回重做 |
| `score` | Integer | 该学生的得分，未批改为 null |

---

## 2. 学生作业详情

| 项 | 值 |
|----|----|
| **URL** | `{{baseUrl}}/homework/student/detail?homeworkId=1856321478523692000` |
| **Method** | `GET` |
| **鉴权** | 需要学生 token |

### 成功响应示例
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

### 字段说明
- `mySubmission` 为 `null` 表示该学生还没提交过
- `attachments` 是 JSON 字符串，需要后端解析为对象数组

---

## 3. 学生提交作业

| 项 | 值 |
|----|----|
| **URL** | `{{baseUrl}}/homework/student/submit` |
| **Method** | `POST` |
| **鉴权** | 需要学生 token |

### 请求 JSON
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

### 字段说明
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `homeworkId` | String | 是 | 作业 ID |
| `content` | String | 是 | 文本作答内容 |
| `attachments` | Array | 否 | 学生附件列表 |
| `attachments[].name` | String | 否 | 附件原始名 |
| `attachments[].url` | String | 否 | 附件 URL |

### 成功响应示例
```json
{
  "code": 200,
  "msg": "作业提交成功",
  "data": true
}
```

### 失败响应示例（已截止）
```json
{
  "code": 500,
  "msg": "作业已截止，禁止超时提交",
  "data": null
}
```

---

## 4. 作业评论列表（教师 / 学生共用）

| 项 | 值 |
|----|----|
| **URL** | `{{baseUrl}}/homework/comment/list?homeworkId=1856321478523692000` |
| **Method** | `GET` |
| **鉴权** | 需要登录 |

### 成功响应示例
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

> `isAnonymous=1` 时，前端展示为"匿名用户"，后端不返回真实姓名。

---

## 5. 发表评论（教师 / 学生共用）

| 项 | 值 |
|----|----|
| **URL** | `{{baseUrl}}/homework/comment/add` |
| **Method** | `POST` |
| **鉴权** | 需要登录 |

### 请求 JSON
```json
{
  "homeworkId": "1856321478523692000",
  "content": "请问第 5 题的第二问怎么做？",
  "isAnonymous": 1
}
```

### 字段说明
| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `homeworkId` | String | 是 | 作业 ID |
| `content` | String | 是 | 评论内容，不能为空 |
| `isAnonymous` | Integer | 否 | `0` 实名（默认），`1` 匿名 |

### 成功响应示例
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

## 6. Postman 集合导入 JSON（B2 学生端 + 评论）

```json
{
  "info": {
    "name": "CourseSystem-B2-作业学生端+评论",
    "_postman_id": "homework-b2-001",
    "description": "组员 B2 负责的学生端作业接口和作业评论接口集合",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "1. 学生-作业列表",
      "request": {
        "method": "GET",
        "url": "{{baseUrl}}/homework/student/list?courseId=1856321478523691000"
      }
    },
    {
      "name": "2. 学生-作业详情",
      "request": {
        "method": "GET",
        "url": "{{baseUrl}}/homework/student/detail?homeworkId=1856321478523692000"
      }
    },
    {
      "name": "3. 学生-提交作业",
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
      "name": "4. 评论-列表",
      "request": {
        "method": "GET",
        "url": "{{baseUrl}}/homework/comment/list?homeworkId=1856321478523692000"
      }
    },
    {
      "name": "5. 评论-发表",
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

## 7. 常见问题

**Q1：提交作业时提示 "作业已截止"？**
A：作业 `deadline` 已经过了当前时间，或者作业配置了 `forbidLate=1` 且超过了 deadline。

**Q2：评论接口哪些人能调用？**
A：教师和学生都可以调用，但必须登录。匿名评论建议学生用，实名评论教师用。
