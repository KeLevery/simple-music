# Simple Music Admin API 接口文档

## 概述

| 项目 | 说明 |
| --- | --- |
| **服务名称** | Vibe Music Admin API |
| **版本** | v1.0.0 |
| **Base URL** | `http://localhost:9080` |
| **协议** | HTTP/HTTPS |
| **数据格式** | JSON |
| **字符编码** | UTF-8 |

---

## 认证方式

所有需要认证的接口均使用 **JWT Bearer Token** 认证。

### 请求头

```
Authorization: Bearer <token>
```

### Token 获取

通过 [登录接口](#111-登录) 获取 Token。

---

## 通用说明

### 响应格式

所有接口统一返回以下格式：

```json
{
  "code": 0,
  "message": "操作成功",
  "data": {}
}
```

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `code` | number | 状态码，`0` 表示成功，非 `0` 表示失败 |
| `message` | string | 响应消息 |
| `data` | any | 响应数据，失败时可能为 `null` |

### 错误响应示例

```json
{
  "code": 401,
  "message": "未授权，请先登录",
  "data": null
}
```

### 分页参数

分页查询接口统一使用以下参数：

| 参数 | 类型 | 必填 | 默认值 | 说明 |
| --- | --- | --- | --- | --- |
| `page` | number | 否 | 1 | 当前页码，从 1 开始 |
| `pageSize` | number | 否 | 10 | 每页条数 |

### 分页响应格式

```json
{
  "code": 0,
  "message": "获取成功",
  "data": {
    "items": [],
    "total": 100,
    "pageSize": 10,
    "currentPage": 1
  }
}
```

### HTTP 状态码

| 状态码 | 说明 |
| --- | --- |
| 200 | 请求成功 |
| 400 | 请求参数错误 |
| 401 | 未授权（未登录或 Token 失效） |
| 403 | 无权限访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

### 业务状态码

| 状态码 | 说明 |
| --- | --- |
| 0 | 操作成功 |
| 1001 | 用户名或密码错误 |
| 1002 | 用户已存在 |
| 1003 | Token 无效或已过期 |
| 2001 | 资源不存在 |
| 3001 | 参数校验失败 |

---

## 接口列表

### 1. 认证模块

#### 1.1 登录

用户登录获取 Token。

**请求信息**

| 项目 | 说明 |
| --- | --- |
| **URL** | `/admin/login` |
| **Method** | `POST` |
| **认证** | 否 |

**请求参数（Body - application/json）**

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `username` | string | 是 | 用户名 |
| `password` | string | 是 | 密码 |

**请求示例**

```json
{
  "username": "admin",
  "password": "123456"
}
```

**响应示例**

成功（200）：

```json
{
  "code": 0,
  "message": "登录成功",
  "data": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

失败（401）：

```json
{
  "code": 1001,
  "message": "用户名或密码错误",
  "data": null
}
```

---

#### 1.2 登出

用户登出，注销当前 Token。

**请求信息**

| 项目 | 说明 |
| --- | --- |
| **URL** | `/admin/logout` |
| **Method** | `POST` |
| **认证** | 是 |

**请求头**

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `Authorization` | string | 是 | `Bearer <token>` |

**响应示例**

```json
{
  "code": 0,
  "message": "登出成功",
  "data": null
}
```

---

### 2. 数据统计模块

#### 2.1 获取用户总数

**请求信息**

| 项目 | 说明 |
| --- | --- |
| **URL** | `/admin/getAllUsersCount` |
| **Method** | `GET` |
| **认证** | 是 |

**响应示例**

```json
{
  "code": 0,
  "message": "获取成功",
  "data": 100
}
```

---

#### 2.2 获取歌手总数

**请求信息**

| 项目 | 说明 |
| --- | --- |
| **URL** | `/admin/getAllArtistsCount` |
| **Method** | `GET` |
| **认证** | 是 |

**查询参数**

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `gender` | number | 否 | 性别筛选：`0`-男歌手，`1`-女歌手，`2`-组合/乐队 |
| `area` | string | 否 | 地区筛选 |

**响应示例**

```json
{
  "code": 0,
  "message": "获取成功",
  "data": 50
}
```

---

#### 2.3 获取歌曲总数

**请求信息**

| 项目 | 说明 |
| --- | --- |
| **URL** | `/admin/getAllSongsCount` |
| **Method** | `GET` |
| **认证** | 是 |

**查询参数**

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `style` | string | 否 | 风格筛选 |

**可选风格值**

| 值 | 说明 |
| --- | --- |
| `欧美流行` | Western Pop |
| `华语流行` | Chinese Pop |
| `粤语流行` | Cantonese Pop |
| `韩国流行` | K-Pop |
| `古典` | Classical |
| `嘻哈说唱` | Hip-Hop |
| `摇滚` | Rock |
| `电子` | Electronic |
| `节奏布鲁斯` | R&B |
| `轻音乐` | Light Music |

**响应示例**

```json
{
  "code": 0,
  "message": "获取成功",
  "data": 200
}
```

---

#### 2.4 获取歌单总数

**请求信息**

| 项目 | 说明 |
| --- | --- |
| **URL** | `/admin/getAllPlaylistsCount` |
| **Method** | `GET` |
| **认证** | 是 |

**查询参数**

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `style` | string | 否 | 风格筛选 |

**响应示例**

```json
{
  "code": 0,
  "message": "获取成功",
  "data": 80
}
```

---

### 3. 用户管理模块

#### 3.1 获取用户列表

**请求信息**

| 项目 | 说明 |
| --- | --- |
| **URL** | `/admin/getAllUsers` |
| **Method** | `POST` |
| **认证** | 是 |

**请求参数（Body - application/json）**

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `page` | number | 否 | 页码，默认 1 |
| `pageSize` | number | 否 | 每页条数，默认 10 |
| `username` | string | 否 | 用户名模糊搜索 |
| `email` | string | 否 | 邮箱模糊搜索 |

**请求示例**

```json
{
  "page": 1,
  "pageSize": 10,
  "username": "",
  "email": ""
}
```

**响应示例**

```json
{
  "code": 0,
  "message": "获取成功",
  "data": {
    "items": [
      {
        "userId": 138,
        "username": "test_user_7",
        "phone": "13212345698",
        "email": "vibe_music_service_7@qq.com",
        "introduction": "用户简介",
        "userAvatar": null,
        "userStatus": "ENABLE",
        "createTime": "2025-03-07T18:06:24",
        "updateTime": "2025-04-02T16:45:43"
      }
    ],
    "total": 100,
    "pageSize": 10,
    "currentPage": 1
  }
}
```

**数据结构 - User**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `userId` | number | 用户 ID |
| `username` | string | 用户名 |
| `phone` | string | 手机号码 |
| `email` | string | 邮箱 |
| `introduction` | string | 个人简介 |
| `userAvatar` | string \| null | 用户头像 URL |
| `userStatus` | string | 状态：`ENABLE`-启用，`DISABLE`-禁用 |
| `createTime` | string | 创建时间（ISO 8601 格式） |
| `updateTime` | string | 更新时间（ISO 8601 格式） |

---

#### 3.2 新增用户

**请求信息**

| 项目 | 说明 |
| --- | --- |
| **URL** | `/admin/addUser` |
| **Method** | `POST` |
| **认证** | 是 |

**请求参数（Body - application/json）**

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `username` | string | 是 | 用户名 |
| `email` | string | 是 | 邮箱 |
| `password` | string | 是 | 密码 |
| `phone` | string | 否 | 手机号码 |
| `introduction` | string | 否 | 个人简介 |
| `userStatus` | string | 否 | 状态：`ENABLE`-启用，`DISABLE`-禁用，默认 `ENABLE` |

**请求示例**

```json
{
  "username": "test",
  "email": "test@example.com",
  "password": "123456",
  "phone": "13800138000",
  "introduction": "这是测试用户",	
  "userStatus": "ENABLE"
}
```

**响应示例**

```json
{
  "code": 0,
  "message": "添加成功",
  "data": null
}
```

---

#### 3.3 编辑用户

**请求信息**

| 项目 | 说明 |
| --- | --- |
| **URL** | `/admin/updateUser` |
| **Method** | `PUT` |
| **认证** | 是 |

**请求参数（Body - application/json）**

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `userId` | number | 是 | 用户 ID |
| `username` | string | 否 | 用户名 |
| `email` | string | 否 | 邮箱 |
| `phone` | string | 否 | 手机号码 |
| `introduction` | string | 否 | 个人简介 |
| `userStatus` | string | 否 | 状态：`ENABLE`-启用，`DISABLE`-禁用 |

**请求示例**

```json
{
  "userId": 138,
  "username": "test_user_7",
  "email": "test@example.com",
  "phone": "13800138000",
  "introduction": "这是测试用户",
  "userStatus": "ENABLE"
}
```

**响应示例**

```json
{
  "code": 0,
  "message": "更新成功",
  "data": null
}
```

---

#### 3.4 更新用户状态

**请求信息**

| 项目 | 说明 |
| --- | --- |
| **URL** | `/admin/updateUserStatus/{id}/{status}` |
| **Method** | `PATCH` |
| **认证** | 是 |

**路径参数**

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| `id` | number | 用户 ID |
| `status` | string | 状态：`ENABLE`-启用，`DISABLE`-禁用 |

**响应示例**

```json
{
  "code": 0,
  "message": "更新成功",
  "data": null
}
```

---

#### 3.5 删除用户

**请求信息**

| 项目 | 说明 |
| --- | --- |
| **URL** | `/admin/deleteUser/{id}` |
| **Method** | `DELETE` |
| **认证** | 是 |

**路径参数**

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| `id` | number | 用户 ID |

**响应示例**

```json
{
  "code": 0,
  "message": "删除成功",
  "data": null
}
```

---

#### 3.6 批量删除用户

**请求信息**

| 项目 | 说明 |
| --- | --- |
| **URL** | `/admin/deleteUsers` |
| **Method** | `DELETE` |
| **认证** | 是 |

**请求参数（Body - application/json）**

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| - | array\<number\> | 是 | 用户 ID 数组 |

**请求示例**

```json
[1, 2, 3]
```

**响应示例**

```json
{
  "code": 0,
  "message": "删除成功",
  "data": null
}
```

---

### 4. 歌手管理模块

#### 4.1 获取歌手列表

**请求信息**

| 项目 | 说明 |
| --- | --- |
| **URL** | `/admin/getAllArtists` |
| **Method** | `POST` |
| **认证** | 是 |

**请求参数（Body - application/json）**

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `pageNum` | number | 否 | 页码，默认 1 |
| `pageSize` | number | 否 | 每页条数，默认 10 |
| `artistName` | string | 否 | 歌手名模糊搜索 |
| `gender` | number | 否 | 性别：`0`-男歌手，`1`-女歌手，`2`-组合/乐队 |
| `area` | string | 否 | 地区筛选 |

**请求示例**

```json
{
  "page": 1,
  "pageSize": 10,
  "name": "",
  "gender": 0,
  "area": ""
}
```

**响应示例**

```json
{
  "code": 0,
  "message": "获取成功",
  "data": {
    "items": [
      {
        "artistId": 1,
        "artistName": "周杰伦",
        "gender": 0,
        "birth": "1979-01-18",
        "area": "中国台湾",
        "introduction": "华语流行音乐天王",
        "avatar": "https://example.com/avatar.jpg",
        "createTime": "2024-01-01T00:00:00"
      }
    ],
    "total": 50,
    "pageSize": 10,
    "currentPage": 1
  }
}
```

**数据结构 - Artist**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `artistId` | number | 歌手 ID |
| `artistName` | string | 歌手名 |
| `gender` | number | 性别：`0`-男歌手，`1`-女歌手，`2`-组合/乐队 |
| `birth` | string | 出生日期 |
| `area` | string | 国籍/地区 |
| `introduction` | string | 歌手简介 |
| `avatar` | string \| null | 头像 URL |
| `createTime` | string | 创建时间（ISO 8601 格式） |

---

#### 4.2 新增歌手

**请求信息**

| 项目 | 说明 |
| --- | --- |
| **URL** | `/admin/addArtist` |
| **Method** | `POST` |
| **认证** | 是 |

**请求参数（Body - application/json）**

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `artistName` | string | 是 | 歌手名 |
| `gender` | number | 否 | 性别：`0`-男歌手，`1`-女歌手，`2`-组合/乐队，默认 0 |
| `birth` | string | 否 | 出生日期（格式：YYYY-MM-DD） |
| `area` | string | 否 | 国籍/地区 |
| `introduction` | string | 否 | 歌手简介 |

**请求示例**

```json
{
  "artistName": "周杰伦",
  "gender": 0,
  "birth": "1979-01-18",
  "area": "中国台湾",
  "introduction": "华语流行音乐天王"
}
```

**响应示例**

```json
{
  "code": 0,
  "message": "添加成功",
  "data": null
}
```

---

#### 4.3 编辑歌手

**请求信息**

| 项目 | 说明 |
| --- | --- |
| **URL** | `/admin/updateArtist` |
| **Method** | `PUT` |
| **认证** | 是 |

**请求参数（Body - application/json）**

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `artistId` | number | 是 | 歌手 ID |
| `artistName` | string | 否 | 歌手名 |
| `gender` | number | 否 | 性别：`0`-男歌手，`1`-女歌手，`2`-组合/乐队 |
| `birth` | string | 否 | 出生日期（格式：YYYY-MM-DD） |
| `area` | string | 否 | 国籍/地区 |
| `introduction` | string | 否 | 歌手简介 |

**请求示例**

```json
{
  "artistId": 1,
  "artistName": "周杰伦",
  "gender": 0,
  "birth": "1979-01-18",
  "area": "中国台湾",
  "introduction": "华语流行音乐天王"
}
```

**响应示例**

```json
{
  "code": 0,
  "message": "更新成功",
  "data": null
}
```

---

#### 4.4 上传歌手头像

**请求信息**

| 项目 | 说明 |
| --- | --- |
| **URL** | `/admin/updateArtistAvatar/{id}` |
| **Method** | `PATCH` |
| **认证** | 是 |
| **Content-Type** | `multipart/form-data` |

**路径参数**

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| `id` | number | 歌手 ID |

**表单参数**

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `avatar` | file | 是 | 头像文件（支持 jpg/png/webp） |

**响应示例**

```json
{
  "code": 0,
  "message": "更新成功",
  "data": "https://example.com/avatar.jpg"
}
```

---

#### 4.5 删除歌手

**请求信息**

| 项目 | 说明 |
| --- | --- |
| **URL** | `/admin/deleteArtist/{id}` |
| **Method** | `DELETE` |
| **认证** | 是 |

**路径参数**

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| `id` | number | 歌手 ID |

**响应示例**

```json
{
  "code": 0,
  "message": "删除成功",
  "data": null
}
```

---

#### 4.6 批量删除歌手

**请求信息**

| 项目 | 说明 |
| --- | --- |
| **URL** | `/admin/deleteArtists` |
| **Method** | `DELETE` |
| **认证** | 是 |

**请求参数（Body - application/json）**

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| - | array\<number\> | 是 | 歌手 ID 数组 |

**请求示例**

```json
[1, 2, 3]
```

**响应示例**

```json
{
  "code": 0,
  "message": "删除成功",
  "data": null
}
```

---

### 5. 歌曲管理模块

#### 5.1 获取所有歌手名称列表

用于歌曲编辑时的歌手下拉选择。

**请求信息**

| 项目 | 说明 |
| --- | --- |
| **URL** | `/admin/getAllArtistNames` |
| **Method** | `GET` |
| **认证** | 是 |

**响应示例**

```json
{
  "code": 0,
  "message": "获取成功",
  "data": [
    {
      "id": 1,
      "name": "周杰伦"
    },
    {
      "id": 2,
      "name": "林俊杰"
    }
  ]
}
```

---

#### 5.2 获取歌曲列表

**请求信息**

| 项目 | 说明 |
| --- | --- |
| **URL** | `/admin/getAllSongsByArtist` |
| **Method** | `POST` |
| **认证** | 是 |

**请求参数（Body - application/json）**

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `page` | number | 否 | 页码，默认 1 |
| `pageSize` | number | 否 | 每页条数，默认 10 |
| `artistId` | number | 否 | 歌手 ID 筛选，`0` 表示全部 |
| `songName` | string | 否 | 歌曲名模糊搜索 |
| `album` | string | 否 | 专辑名模糊搜索 |
| `style` | string | 否 | 风格筛选 |

**请求示例**

```json
{
  "page": 1,
  "pageSize": 10,
  "artistId": 0,
  "name": "",
  "style": ""
}
```

**响应示例**

```json
{
  "code": 0,
  "message": "获取成功",
  "data": {
    "items": [
      {
        "songId": 1,
        "songName": "七里香",
        "artistId": 1,
        "artistName": "周杰伦",
        "album": "七里香",
        "style": ["华语流行"],
        "releaseTime": "2004-08-03",
        "cover": "https://example.com/cover.jpg",
        "audio": "https://example.com/audio.mp3",
        "lyrics": "窗外的麻雀...",
        "createTime": "2024-01-01T00:00:00"
      }
    ],
    "total": 200,
    "pageSize": 10,
    "currentPage": 1
  }
}
```

**数据结构 - Song**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `songId` | number | 歌曲 ID |
| `songName` | string | 歌曲名 |
| `artistId` | number | 歌手 ID |
| `artistName` | string | 歌手名 |
| `album` | string | 专辑名称 |
| `style` | array\<string\> | 风格标签数组 |
| `releaseTime` | string | 发行时间（格式：YYYY-MM-DD） |
| `cover` | string \| null | 封面 URL |
| `audio` | string \| null | 音频 URL |
| `lyrics` | string \| null | 歌词 |
| `createTime` | string | 创建时间（ISO 8601 格式） |

---

#### 5.3 新增歌曲

**请求信息**

| 项目 | 说明 |
| --- | --- |
| **URL** | `/admin/addSong` |
| **Method** | `POST` |
| **认证** | 是 |

**请求参数（Body - application/json）**

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `songName` | string | 是 | 歌曲名 |
| `artistId` | number | 是 | 歌手 ID |
| `album` | string | 否 | 专辑名称 |
| `style` | array\<string\> | 否 | 风格标签数组 |
| `releaseTime` | string | 否 | 发行时间（格式：YYYY-MM-DD） |
| `lyrics` | string | 否 | 歌词 |

**请求示例**

```json
{
  "songName": "七里香",
  "artistId": 1,
  "album": "七里香",
  "style": ["华语流行"],
  "releaseTime": "2004-08-03",
  "lyrics": "窗外的麻雀..."
}
```

**响应示例**

```json
{
  "code": 0,
  "message": "添加成功",
  "data": null
}
```

---

#### 5.4 编辑歌曲

**请求信息**

| 项目 | 说明 |
| --- | --- |
| **URL** | `/admin/updateSong` |
| **Method** | `PUT` |
| **认证** | 是 |

**请求参数（Body - application/json）**

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `songId` | number | 是 | 歌曲 ID |
| `songName` | string | 否 | 歌曲名 |
| `artistId` | number | 否 | 歌手 ID |
| `album` | string | 否 | 专辑名称 |
| `style` | array\<string\> | 否 | 风格标签数组 |
| `releaseTime` | string | 否 | 发行时间（格式：YYYY-MM-DD） |
| `lyrics` | string | 否 | 歌词 |

**请求示例**

```json
{
  "songId": 1,
  "songName": "七里香",
  "artistId": 1,
  "album": "七里香",
  "style": ["华语流行"],
  "releaseTime": "2004-08-03",
  "lyrics": "窗外的麻雀..."
}
```

**响应示例**

```json
{
  "code": 0,
  "message": "更新成功",
  "data": null
}
```

---

#### 5.5 上传歌曲封面

**请求信息**

| 项目 | 说明 |
| --- | --- |
| **URL** | `/admin/updateSongCover/{id}` |
| **Method** | `PATCH` |
| **认证** | 是 |
| **Content-Type** | `multipart/form-data` |

**路径参数**

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| `id` | number | 歌曲 ID |

**表单参数**

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `cover` | file | 是 | 封面文件（支持 jpg/png/webp） |

**响应示例**

```json
{
  "code": 0,
  "message": "更新成功",
  "data": "https://example.com/cover.jpg"
}
```

---

#### 5.6 上传歌曲音频

**请求信息**

| 项目 | 说明 |
| --- | --- |
| **URL** | `/admin/updateSongAudio/{id}` |
| **Method** | `PATCH` |
| **认证** | 是 |
| **Content-Type** | `multipart/form-data` |

**路径参数**

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| `id` | number | 歌曲 ID |

**表单参数**

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `audio` | file | 是 | 音频文件（支持 mp3/wav/flac） |

**响应示例**

```json
{
  "code": 0,
  "message": "更新成功",
  "data": "https://example.com/audio.mp3"
}
```

---

#### 5.7 删除歌曲

**请求信息**

| 项目 | 说明 |
| --- | --- |
| **URL** | `/admin/deleteSong/{id}` |
| **Method** | `DELETE` |
| **认证** | 是 |

**路径参数**

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| `id` | number | 歌曲 ID |

**响应示例**

```json
{
  "code": 0,
  "message": "删除成功",
  "data": null
}
```

---

#### 5.8 批量删除歌曲

**请求信息**

| 项目 | 说明 |
| --- | --- |
| **URL** | `/admin/deleteSongs` |
| **Method** | `DELETE` |
| **认证** | 是 |

**请求参数（Body - application/json）**

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| - | array\<number\> | 是 | 歌曲 ID 数组 |

**请求示例**

```json
[1, 2, 3]
```

**响应示例**

```json
{
  "code": 0,
  "message": "删除成功",
  "data": null
}
```

---

### 6. 歌单管理模块

#### 6.1 获取歌单列表

**请求信息**

| 项目 | 说明 |
| --- | --- |
| **URL** | `/admin/getAllPlaylists` |
| **Method** | `POST` |
| **认证** | 是 |

**请求参数（Body - application/json）**

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `page` | number | 否 | 页码，默认 1 |
| `pageSize` | number | 否 | 每页条数，默认 10 |
| `title` | string | 否 | 歌单名模糊搜索 |
| `style` | string | 否 | 风格筛选 |

**请求示例**

```json
{
  "page": 1,
  "pageSize": 10,
  "title": "",
  "style": ""
}
```

**响应示例**

```json
{
  "code": 0,
  "message": "获取成功",
  "data": {
    "items": [
      {
        "playlistId": 1,
        "title": "流行金曲",
        "introduction": "流行音乐合集",
        "style": "华语流行",
        "cover": "https://example.com/cover.jpg",
        "createTime": "2024-01-01T00:00:00"
      }
    ],
    "total": 80,
    "pageSize": 10,
    "currentPage": 1
  }
}
```

**数据结构 - Playlist**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `playlistId` | number | 歌单 ID |
| `title` | string | 歌单名 |
| `introduction` | string | 歌单简介 |
| `style` | string | 风格 |
| `cover` | string \| null | 封面 URL |
| `createTime` | string | 创建时间（ISO 8601 格式） |

---

#### 6.2 新增歌单

**请求信息**

| 项目 | 说明 |
| --- | --- |
| **URL** | `/admin/addPlaylist` |
| **Method** | `POST` |
| **认证** | 是 |

**请求参数（Body - application/json）**

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `title` | string | 是 | 歌单名 |
| `introduction` | string | 否 | 歌单简介 |
| `style` | string | 否 | 风格 |

**请求示例**

```json
{
  "title": "流行金曲",
  "introduction": "流行音乐合集",
  "style": "华语流行"
}
```

**响应示例**

```json
{
  "code": 0,
  "message": "添加成功",
  "data": null
}
```

---

#### 6.3 编辑歌单

**请求信息**

| 项目 | 说明 |
| --- | --- |
| **URL** | `/admin/updatePlaylist` |
| **Method** | `PUT` |
| **认证** | 是 |

**请求参数（Body - application/json）**

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `playlistId` | number | 是 | 歌单 ID |
| `title` | string | 否 | 歌单名 |
| `introduction` | string | 否 | 歌单简介 |
| `style` | string | 否 | 风格 |

**请求示例**

```json
{
  "playlistId": 1,
  "title": "流行金曲",
  "introduction": "流行音乐合集",
  "style": "华语流行"
}
```

**响应示例**

```json
{
  "code": 0,
  "message": "更新成功",
  "data": null
}
```

---

#### 6.4 上传歌单封面

**请求信息**

| 项目 | 说明 |
| --- | --- |
| **URL** | `/admin/updatePlaylistCover/{id}` |
| **Method** | `PATCH` |
| **认证** | 是 |
| **Content-Type** | `multipart/form-data` |

**路径参数**

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| `id` | number | 歌单 ID |

**表单参数**

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `cover` | file | 是 | 封面文件（支持 jpg/png/webp） |

**响应示例**

```json
{
  "code": 0,
  "message": "更新成功",
  "data": "https://example.com/cover.jpg"
}
```

---

#### 6.5 删除歌单

**请求信息**

| 项目 | 说明 |
| --- | --- |
| **URL** | `/admin/deletePlaylist/{id}` |
| **Method** | `DELETE` |
| **认证** | 是 |

**路径参数**

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| `id` | number | 歌单 ID |

**响应示例**

```json
{
  "code": 0,
  "message": "删除成功",
  "data": null
}
```

---

#### 6.6 批量删除歌单

**请求信息**

| 项目 | 说明 |
| --- | --- |
| **URL** | `/admin/deletePlaylists` |
| **Method** | `DELETE` |
| **认证** | 是 |

**请求参数（Body - application/json）**

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| - | array\<number\> | 是 | 歌单 ID 数组 |

**请求示例**

```json
[1, 2, 3]
```

**响应示例**

```json
{
  "code": 0,
  "message": "删除成功",
  "data": null
}
```

---

### 7. 反馈管理模块

#### 7.1 获取反馈列表

**请求信息**

| 项目 | 说明 |
| --- | --- |
| **URL** | `/admin/getAllFeedbacks` |
| **Method** | `POST` |
| **认证** | 是 |

**请求参数（Body - application/json）**

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `page` | number | 否 | 页码，默认 1 |
| `pageSize` | number | 否 | 每页条数，默认 10 |
| `keyword` | string | 否 | 反馈内容关键词搜索 |

**请求示例**

```json
{
  "page": 1,
  "pageSize": 10,
  "keyword": ""
}
```

**响应示例**

```json
{
  "code": 0,
  "message": "获取成功",
  "data": {
    "items": [
      {
        "feedbackId": 1,
        "userId": 1,
        "username": "user1",
        "content": "建议增加更多歌曲",
        "createTime": "2024-01-01T00:00:00"
      }
    ],
    "total": 30,
    "pageSize": 10,
    "currentPage": 1
  }
}
```

**数据结构 - Feedback**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `feedbackId` | number | 反馈 ID |
| `userId` | number | 用户 ID |
| `username` | string | 用户名 |
| `content` | string | 反馈内容 |
| `createTime` | string | 创建时间（ISO 8601 格式） |

---

#### 7.2 删除反馈

**请求信息**

| 项目 | 说明 |
| --- | --- |
| **URL** | `/admin/deleteFeedback/{id}` |
| **Method** | `DELETE` |
| **认证** | 是 |

**路径参数**

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| `id` | number | 反馈 ID |

**响应示例**

```json
{
  "code": 0,
  "message": "删除成功",
  "data": null
}
```

---

#### 7.3 批量删除反馈

**请求信息**

| 项目 | 说明 |
| --- | --- |
| **URL** | `/admin/deleteFeedbacks` |
| **Method** | `DELETE` |
| **认证** | 是 |

**请求参数（Body - application/json）**

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| - | array\<number\> | 是 | 反馈 ID 数组 |

**请求示例**

```json
[1, 2, 3]
```

**响应示例**

```json
{
  "code": 0,
  "message": "删除成功",
  "data": null
}
```

---

### 8. 轮播图管理模块

#### 8.1 获取轮播图列表

**请求信息**

| 项目 | 说明 |
| --- | --- |
| **URL** | `/admin/getAllBanners` |
| **Method** | `POST` |
| **认证** | 是 |

**请求参数（Body - application/json）**

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `page` | number | 否 | 页码，默认 1 |
| `pageSize` | number | 否 | 每页条数，默认 10 |

**请求示例**

```json
{
  "page": 1,
  "pageSize": 10
}
```

**响应示例**

```json
{
  "code": 0,
  "message": "获取成功",
  "data": {
    "items": [
      {
        "id": 1,
        "image": "https://example.com/banner.jpg",
        "url": "https://example.com/song/1",
        "status": 1,
        "createdAt": "2024-01-01 00:00:00"
      }
    ],
    "total": 10,
    "pageSize": 10,
    "currentPage": 1
  }
}
```

**数据结构 - Banner**

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | number | 轮播图 ID |
| `image` | string | 图片 URL |
| `url` | string | 跳转链接 |
| `status` | number | 状态：`1`-启用，`0`-禁用 |
| `createdAt` | string | 创建时间 |

---

#### 8.2 新增轮播图

**请求信息**

| 项目 | 说明 |
| --- | --- |
| **URL** | `/admin/addBanner` |
| **Method** | `POST` |
| **认证** | 是 |
| **Content-Type** | `multipart/form-data` |

**表单参数**

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `image` | file | 是 | 轮播图文件（支持 jpg/png/webp） |
| `url` | string | 是 | 跳转链接 |
| `status` | number | 是 | 状态：`1`-启用，`0`-禁用 |

**响应示例**

```json
{
  "code": 0,
  "message": "添加成功",
  "data": null
}
```

---

#### 8.3 编辑轮播图

**请求信息**

| 项目 | 说明 |
| --- | --- |
| **URL** | `/admin/updateBanner/{id}` |
| **Method** | `PATCH` |
| **认证** | 是 |
| **Content-Type** | `multipart/form-data` |

**路径参数**

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| `id` | number | 轮播图 ID |

**表单参数**

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `image` | file | 否 | 轮播图文件（支持 jpg/png/webp） |
| `url` | string | 否 | 跳转链接 |
| `status` | number | 否 | 状态：`1`-启用，`0`-禁用 |

**响应示例**

```json
{
  "code": 0,
  "message": "更新成功",
  "data": null
}
```

---

#### 8.4 更新轮播图状态

**请求信息**

| 项目 | 说明 |
| --- | --- |
| **URL** | `/admin/updateBannerStatus/{id}` |
| **Method** | `PATCH` |
| **认证** | 是 |

**路径参数**

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| `id` | number | 轮播图 ID |

**请求参数（Body - application/json）**

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `status` | number | 是 | 状态：`1`-启用，`0`-禁用 |

**请求示例**

```json
{
  "status": 1
}
```

**响应示例**

```json
{
  "code": 0,
  "message": "更新成功",
  "data": null
}
```

---

#### 8.5 删除轮播图

**请求信息**

| 项目 | 说明 |
| --- | --- |
| **URL** | `/admin/deleteBanner/{id}` |
| **Method** | `DELETE` |
| **认证** | 是 |

**路径参数**

| 参数 | 类型 | 说明 |
| --- | --- | --- |
| `id` | number | 轮播图 ID |

**响应示例**

```json
{
  "code": 0,
  "message": "删除成功",
  "data": null
}
```

---

#### 8.6 批量删除轮播图

**请求信息**

| 项目 | 说明 |
| --- | --- |
| **URL** | `/admin/deleteBanners` |
| **Method** | `DELETE` |
| **认证** | 是 |

**请求参数（Body - application/json）**

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| - | array\<number\> | 是 | 轮播图 ID 数组 |

**请求示例**

```json
[1, 2, 3]
```

**响应示例**

```json
{
  "code": 0,
  "message": "删除成功",
  "data": null
}
```

---

## 附录

### 枚举值

#### 用户状态（UserStatus）

| 值 | 说明 |
| --- | --- |
| `ENABLE` | 启用 |
| `DISABLE` | 禁用 |

#### 歌手性别（Gender）

| 值 | 说明 |
| --- | --- |
| `0` | 男歌手 |
| `1` | 女歌手 |
| `2` | 组合/乐队 |

#### 歌曲风格（Style）

| 值 | 说明 |
| --- | --- |
| `欧美流行` | Western Pop |
| `华语流行` | Chinese Pop |
| `粤语流行` | Cantonese Pop |
| `国风流行` | Chinese Style Pop |
| `韩语流行` | K-Pop |
| `日本流行` | J-Pop |
| `嘻哈说唱` | Hip-Hop |
| `非洲节拍` | Afrobeats |
| `原声带` | OST |
| `轻音乐` | Light Music |
| `摇滚` | Rock |
| `朋克` | Punk |
| `电子` | Electronic |
| `国风` | Chinese Style |
| `乡村` | Country |
| `古典` | Classical |

---

### 更新日志

| 版本 | 日期 | 更新内容 |
| --- | --- | --- |
| v1.0.0 | 2024-01-01 | 初始版本 |

---

> **文档维护**: 如有问题请联系 `vibe_music_service@163.com`
