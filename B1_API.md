# 组员 B1 接口文档 — 作业板块·教师端

> 本文档为组员 B1 专用，专职负责教师端作业管理接口。
> 每个接口都包含 **完整 URL + Method + Headers + JSON Body 示例 + 响应示例**，复制即可测试。
> 协同开发请参考 `B1_homework.md`。

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
| `homework.type` | 作业模式 | `1` 个人作业、`2` 小组作业 |
| `homework.forbidLate` | 截止策略 | `0` 允许超时提交、`1` 禁止超时提交 |
| `homeworkSubmit.status` | 提交状态 | `0` 未交、`1` 已提交待批阅、`2` 已批改、`3` 打回重做 |
| `homework.activityTag` | 应用环节 | `课前` / `课中` / `课后` / `期末` 等自定义字符串 |

---

## 1. 教师发布作业

| 项 | 值 |
|----|----|
| **URL** | `{{baseUrl}}/homework/teacher/create` |
| **Method** | `POST` |
| **鉴权** | 需要教师 token |

### 请求 JSON
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

### 字段说明

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `courseId` | String | 是 | 课程 ID（雪花算法，必须加引号） |
| `title` | String | 是 | 作业标题 |
| `description` | String | 否 | 作业要求描述 |
| `type` | Integer | 是 | `1` 个人作业，`2` 小组作业 |
| `activityTag` | String | 否 | 应用环节标签 |
| `totalScore` | Integer | 是 | 满分值 |
| `forbidLate` | Integer | 是 | `0` 允许超时，`1` 禁止超时 |
| `deadline` | String | 是 | 截止时间，格式 `yyyy-MM-dd HH:mm:ss` |
| `attachments` | Array | 否 | 老师附件列表 |
| `attachments[].name` | String | 否 | 附件原始名 |
| `attachments[].url` | String | 否 | 附件存储 URL |

### 成功响应示例
```json
{
  "code": 200,
  "msg": "作业发布并分发完毕",
  "data": {
    "homeworkId": "1856321478523692000"
  }
}
```

### cURL 命令
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

## 2. 教师作业列表

| 项 | 值 |
|----|----|
| **URL** | `{{baseUrl}}/homework/teacher/list?courseId=1856321478523691000` |
| **Method** | `GET` |
| **鉴权** | 需要教师 token |

> `courseId` 是 **Query 参数**（URL 问号后面），不是 Body。

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

### 字段说明
| 字段 | 类型 | 说明 |
|------|------|------|
| `isOver` | Boolean | true 已截止，false 未截止 |
| `gradedCount` | Integer | 已批改数 |
| `ungradedCount` | Integer | 待批改数 |
| `unsubmittedCount` | Integer | 未提交数 |

---

## 3. 教师批阅大厅：作业提交明细

| 项 | 值 |
|----|----|
| **URL** | `{{baseUrl}}/homework/teacher/submit-list?homeworkId=1856321478523692000&status=1&studentName=张` |
| **Method** | `GET` |
| **鉴权** | 需要教师 token |

### Query 参数
| 参数 | 必填 | 说明 |
|------|------|------|
| `homeworkId` | 是 | 作业 ID |
| `status` | 否 | 筛选提交状态：`0`未交 / `1`已提交 / `2`已批改 / `3`打回重做 |
| `studentName` | 否 | 学生姓名模糊搜索 |

> 全部参数都可省略，省略时不筛选。

### 成功响应示例
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

## 4. 教师批阅打分

| 项 | 值 |
|----|----|
| **URL** | `{{baseUrl}}/homework/teacher/grade` |
| **Method** | `POST` |
| **鉴权** | 需要教师 token |

### 请求 JSON
```json
{
  "submitId": "1856321478523693000",
  "score": 95,
  "teacherComment": "完成度很高，建议注意第 3 题的边界条件",
  "status": 2
}
```

### 字段说明

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `submitId` | String | 是 | 提交记录 ID |
| `score` | Integer | 是 | 分数（0 - totalScore） |
| `teacherComment` | String | 否 | 评语 |
| `status` | Integer | 是 | `2` 已批改（默认），`3` 打回重做 |

### 成功响应示例
```json
{
  "code": 200,
  "msg": "批阅成功",
  "data": true
}
```

---

## 5. Postman 集合导入 JSON（B1 教师端）

```json
{
  "info": {
    "name": "CourseSystem-B1-作业教师端",
    "_postman_id": "homework-b1-001",
    "description": "组员 B1 负责的作业教师端接口集合",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "1. 教师-发布作业",
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
      "name": "2. 教师-作业列表",
      "request": {
        "method": "GET",
        "url": "{{baseUrl}}/homework/teacher/list?courseId=1856321478523691000"
      }
    },
    {
      "name": "3. 教师-批阅大厅",
      "request": {
        "method": "GET",
        "url": "{{baseUrl}}/homework/teacher/submit-list?homeworkId=1856321478523692000&status=1"
      }
    },
    {
      "name": "4. 教师-批阅打分",
      "request": {
        "method": "POST",
        "header": [{"key": "Content-Type", "value": "application/json"}],
        "url": "{{baseUrl}}/homework/teacher/grade",
        "body": {
          "mode": "raw",
          "raw": "{\n  \"submitId\": \"1856321478523693000\",\n  \"score\": 95,\n  \"teacherComment\": \"完成度很高\",\n  \"status\": 2\n}"
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

**Q2：批阅打分后学生看不到分数？**
A：批阅接口 `status` 字段必须传 `2`（已批改），传错状态会让学生端仍然显示"待批阅"。
