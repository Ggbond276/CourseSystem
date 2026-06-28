# 组员 C 接口文档 — 鉴权板块（登录认证）

> 📌 本文档专为 Postman 后端联调设计，专门给组员 C 维护登录鉴权模块使用。

---

## 0. 测试前必读

### 0.1 后端服务地址
```
http://localhost:8888
```

### 0.2 Postman 环境变量

在 Postman 左侧 `Environments` 新建 `CourseSystem-Local`：

| 变量名 | 初始值 | 说明 |
|--------|--------|------|
| `baseUrl` | `http://localhost:8888` | 后端根地址 |
| `token` | （登录后填入） | 登录后从响应里 `data.token` 复制 |

### 0.3 通用响应结构
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": ...
}
```
- `code = 200`：成功
- `code = 500`：业务异常（账号不存在、密码错误等）
- `code = 401`：未登录或 token 失效

### 0.4 鉴权规则
- **登录成功** → 后端返回 `token` 字符串
- **后续请求** → 在 Header 加 `Authorization: Bearer <token>`
- **token 失效** → 响应 `code = 401`，前端需要跳回登录页

---

## 1. 账号密码登录

| 项 | 值 |
|----|----|
| **URL** | `{{baseUrl}}/auth/login` |
| **Method** | `POST` |
| **鉴权** | ❌ 不需要 token（这是拿 token 的入口） |
| **Content-Type** | `application/json` |

### 1.1 请求 JSON
```json
{
  "account": "2024001001",
  "password": "123456"
}
```

### 1.2 字段说明

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `account` | String | ✅ | 学号 / 工号 / 唯一登录账号（不是手机号） |
| `password` | String | ✅ | 明文密码，后端用 BCrypt 加密后比对 |

### 1.3 成功响应示例
```json
{
  "code": 200,
  "msg": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxODU2MzIxNDc4NTIzNjkwMDAwMCIsImlhdCI6MTcyMDAwMDAwMCwiZXhwIjoxNzIwMDAzNjAwfQ.xxxxx",
    "userId": "1856321478523690000",
    "name": "张老师",
    "avatar": "https://example.com/avatar/teacher1.jpg",
    "phone": "13800138000"
  }
}
```

### 1.4 响应字段说明

| 字段 | 类型 | 说明 |
|------|------|------|
| `token` | String | JWT 令牌，存到 localStorage / Pinia |
| `userId` | String | 用户 ID（雪花算法，加引号） |
| `name` | String | 真实姓名 |
| `avatar` | String | 头像 URL，可为空 |
| `phone` | String | 手机号，可为空 |

### 1.5 失败响应示例

**账号不存在：**
```json
{
  "code": 500,
  "msg": "账号不存在",
  "data": null
}
```

**密码错误：**
```json
{
  "code": 500,
  "msg": "密码错误",
  "data": null
}
```

### 1.6 复制到 Postman 的 cURL 命令
```bash
curl -X POST 'http://localhost:8888/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{
    "account": "2024001001",
    "password": "123456"
  }'
```

### 1.7 测试数据准备

登录接口需要数据库里有用户记录。如果没有测试账号，需要先用 SQL 插入：

```sql
-- 教师测试账号（密码 123456 的 BCrypt 加密结果，$2b$ 与 $2a$ 兼容）
INSERT INTO user (id, account, password, name, phone, avatar, create_time, update_time)
VALUES (
  1856321478523690000,
  'T001',
  '$2b$10$BcWbKT8Y9Yu97xujrh3gt.5G1tGv1HZumVw300xiSAX7BYpDwaegC',
  '张老师',
  '13800138000',
  'https://example.com/avatar/teacher1.jpg',
  NOW(),
  NOW()
);

-- 学生测试账号
INSERT INTO user (id, account, password, name, phone, avatar, create_time, update_time)
VALUES (
  1856321478523690001,
  '2024001001',
  '$2b$10$BcWbKT8Y9Yu97xujrh3gt.5G1tGv1HZumVw300xiSAX7BYpDwaegC',
  '张三',
  '13800138001',
  'https://example.com/avatar/student1.jpg',
  NOW(),
  NOW()
);
```

> ⚠️ 上面 BCrypt 字符串是 `123456` 的真实加密结果，**直接复制粘贴即可**。

---

## 2. 用户注册

| 项 | 值 |
|----|----|
| **URL** | `{{baseUrl}}/auth/register` |
| **Method** | `POST` |
| **鉴权** | ❌ 不需要 token（这是注册入口） |
| **Content-Type** | `application/json` |

### 2.1 请求 JSON
```json
{
  "account": "2024002001",
  "password": "123456",
  "name": "李四",
  "phone": "13800138002"
}
```

### 2.2 字段说明

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| `account` | String | ✅ | 学号 / 工号，唯一标识 |
| `password` | String | ✅ | 明文密码（后端自动用 BCrypt 加密存储） |
| `name` | String | ✅ | 真实姓名 |
| `phone` | String | ❌ | 手机号，可为空 |

### 2.3 成功响应示例
```json
{
  "code": 200,
  "msg": "注册成功",
  "data": {
    "userId": "1856321478523690100"
  }
}
```

### 2.4 失败响应示例（账号已存在）
```json
{
  "code": 500,
  "msg": "账号已存在，请换一个账号注册",
  "data": null
}
```

### 2.5 复制到 Postman 的 cURL 命令
```bash
curl -X POST 'http://localhost:8888/auth/register' \
  -H 'Content-Type: application/json' \
  -d '{
    "account": "2024002001",
    "password": "123456",
    "name": "李四",
    "phone": "13800138002"
  }'
```

---

## 3. 验证登录态（受保护接口示例）

为了验证 `token` 是否生效，可以调用任意需要鉴权的接口测试，例如：

### 3.1 验证用接口：教师课程列表

| 项 | 值 |
|----|----|
| **URL** | `{{baseUrl}}/course/teacher/list` |
| **Method** | `GET` |
| **鉴权** | ✅ 需要 token |

请求 Header：
```
Authorization: Bearer {{token}}
```

#### 带 token 的成功响应
```json
{
  "code": 200,
  "msg": "查询成功",
  "data": [...]
}
```

#### 不带 token 的失败响应
```json
{
  "code": 401,
  "msg": "未登录或 token 已失效",
  "data": null
}
```

#### 复制到 Postman 的 cURL 命令
```bash
curl -X GET 'http://localhost:8888/course/teacher/list' \
  -H 'Authorization: Bearer {{token}}'
```

---

## 4. 完整测试流程（推荐新手照着跑一遍）

```
步骤 1：注册一个账号（可选，不想注册就用步骤 2 的测试账号）
  POST {{baseUrl}}/auth/register
  Body: { "account": "2024002001", "password": "123456", "name": "李四", "phone": "13800138002" }

步骤 2：调用登录接口（直接用测试账号）
  POST {{baseUrl}}/auth/login
  Body: { "account": "T001", "password": "123456" }

步骤 3：拿到 token
  复制响应 data.token 的值

步骤 4：把 token 存到环境变量
  左侧 Environments → CourseSystem-Local → token 字段粘贴

步骤 5：调用任意受保护接口
  GET {{baseUrl}}/course/teacher/list
  Header: Authorization: Bearer {{token}}
  → 返回 200 + 业务数据 = 登录态有效

步骤 6：故意改坏 token 测试
  把 token 末尾改一个字符
  → 返回 401 = 鉴权中间件正常工作
```

---

## 5. Postman 集合导入 JSON

```json
{
  "info": {
    "name": "CourseSystem-C-鉴权板块",
    "_postman_id": "auth-c-001",
    "description": "组员 C 负责的鉴权板块接口集合",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "1. 账号密码登录",
      "request": {
        "method": "POST",
        "header": [{"key": "Content-Type", "value": "application/json"}],
        "url": "{{baseUrl}}/auth/login",
        "body": {
          "mode": "raw",
          "raw": "{\n  \"account\": \"T001\",\n  \"password\": \"123456\"\n}"
        }
      },
      "event": [
        {
          "listen": "test",
          "script": {
            "type": "text/javascript",
            "exec": [
              "// 登录成功后自动把 token 存到环境变量",
              "var json = pm.response.json();",
              "if (json.code === 200 && json.data && json.data.token) {",
              "  pm.environment.set('token', json.data.token);",
              "  console.log('token 已自动保存到环境变量');",
              "} else {",
              "  console.log('登录失败，请检查账号密码');",
              "}"
            ]
          }
        }
      ]
    },
    {
      "name": "2. 用户注册",
      "request": {
        "method": "POST",
        "header": [{"key": "Content-Type", "value": "application/json"}],
        "url": "{{baseUrl}}/auth/register",
        "body": {
          "mode": "raw",
          "raw": "{\n  \"account\": \"2024002001\",\n  \"password\": \"123456\",\n  \"name\": \"李四\",\n  \"phone\": \"13800138002\"\n}"
        }
      }
    },
    {
      "name": "3. 验证登录态（受保护接口）",
      "request": {
        "method": "GET",
        "header": [{"key": "Authorization", "value": "Bearer {{token}}"}],
        "url": "{{baseUrl}}/course/teacher/list"
      }
    }
  ]
}
```

> 💡 **贴心小技巧**：登录接口的 `Tests` 标签页里加了 JS 脚本，登录成功后会自动把 token 存到环境变量，下次请求就不用手动复制粘贴了。

---

## 6. 常见问题

**Q1：登录一直返回 500 "账号不存在"？**
A：检查数据库里 `user` 表是否有对应 `account` 字段的记录；用 `schema.sql` 末尾的 INSERT 语句插入测试数据后重试。

**Q2：登录一直返回 500 "密码错误"？**
A：密码字段是 BCrypt 加密后的字符串，不是明文。直接用 `schema.sql` 里的 SQL，不要自己 md5 / sha1 加密。

**Q3：响应里的 token 怎么用？**
A：复制到 Postman 环境变量 `token`，然后在请求 Header 加 `Authorization: Bearer <token>`。

**Q4：什么时候会 401？**
A：① 没传 token；② token 字符串写错；③ token 过期（默认 1 小时）；④ token 被篡改。**任意一种**都会返回 401。

**Q5：登录成功后，前端应该把 token 存在哪里？**
A：建议存 `localStorage.token` 或 Pinia store，关闭浏览器不丢失；如果对安全要求高，可改存 sessionStorage（关闭浏览器即失效）。

**Q6：注册接口报 "账号已存在"？**
A：该账号在数据库里已经有人注册过了，换一个 `account` 再试。