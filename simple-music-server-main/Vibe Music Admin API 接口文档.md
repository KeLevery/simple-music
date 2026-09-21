# Vibe Music Admin API 接口文档

## 基础信息

- **API Base URL**: `http://localhost:9080`
- **认证方式**: JWT Token (在请求头中添加 `Authorization: <token>`)
- **响应格式**: JSON

## 通用说明

### 分页参数

所有分页接口统一使用以下参数：

| 参数名   | 类型    | 必需 | 描述     |
| -------- | ------- | ---- | -------- |
| pageNum  | Integer | 是   | 页码     |
| pageSize | Integer | 是   | 每页数量 |

### 响应格式

所有接口统一返回以下格式：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {}
}
```

分页数据格式：

```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "items": [],
    "total": 100,
    "pageSize": 10,
    "currentPage": 1
  }
}
```

## 1. 登录页面

### 1.1 登录

- **请求方法**: POST

- **URL**: `/admin/login`

- **请求参数**:

  | 参数名   | 类型   | 必需 | 描述   |
  | -------- | ------ | ---- | ------ |
  | username | string | 是   | 用户名 |
  | password | string | 是   | 密码   |

- **响应格式**:

  ```json
  {
    "code": 200,
    "message": "登录成功",
    "data": "<JWT Token>"
  }
  ```

### 1.2 登出

- **请求方法**: POST

- **URL**: `/admin/logout`

- **请求头**: `Authorization: <token>`

- **响应格式**:

  ```json
  {
    "code": 200,
    "message": "登出成功",
    "data": null
  }
  ```

## 2. 首页（数据统计）

### 2.1 获取用户数量

- **请求方法**: GET

- **URL**: `/admin/getAllUsersCount`

- **请求头**: `Authorization: <token>`

- **响应格式**:

  ```json
  {
    "code": 200,
    "message": "获取成功",
    "data": 100
  }
  ```

### 2.2 获取歌手数量

- **请求方法**: GET

- **URL**: `/admin/getAllArtistsCount`

- **请求参数**:

  | 参数名 | 类型    | 必需 | 描述                          |
  | ------ | ------- | ---- | ----------------------------- |
  | gender | Integer | 否   | 性别（0: 男, 1: 女, 2: 组合） |
  | area   | String  | 否   | 地区                          |

- **请求头**: `Authorization: <token>`

- **响应格式**:

  ```json
  {
    "code": 200,
    "message": "获取成功",
    "data": 50
  }
  ```

### 2.3 获取歌曲数量

- **请求方法**: GET

- **URL**: `/admin/getAllSongsCount`

- **请求参数**:

  | 参数名 | 类型   | 必需 | 描述 |
  | ------ | ------ | ---- | ---- |
  | style  | String | 否   | 风格 |

- **请求头**: `Authorization: <token>`

- **响应格式**:

  ```json
  {
    "code": 200,
    "message": "获取成功",
    "data": 200
  }
  ```

### 2.4 获取歌单数量

- **请求方法**: GET

- **URL**: `/admin/getAllPlaylistsCount`

- **请求参数**:

  | 参数名 | 类型   | 必需 | 描述 |
  | ------ | ------ | ---- | ---- |
  | style  | String | 否   | 风格 |

- **请求头**: `Authorization: <token>`

- **响应格式**:

  ```json
  {
    "code": 200,
    "message": "获取成功",
    "data": 80
  }
  ```

## 3. 用户管理

### 3.1 获取用户列表

- **请求方法**: POST

- **URL**: `/admin/getAllUsers`

- **请求参数**:

  ```json
  {
    "pageNum": 1,
    "pageSize": 10,
    "username": "",
    "phone": "",
    "userStatus": 0
  }
  ```

- **参数说明**:

  | 参数名     | 类型    | 必需 | 描述                          |
  | ---------- | ------- | ---- | ----------------------------- |
  | pageNum    | Integer | 是   | 页码                          |
  | pageSize   | Integer | 是   | 每页数量                      |
  | username   | String  | 否   | 用户名（模糊搜索）            |
  | phone      | String  | 否   | 手机号                        |
  | userStatus | Integer | 否   | 用户状态（0: 启用, 1: 禁用） |

- **请求头**: `Authorization: <token>`

- **响应格式**:

  ```json
  {
    "code": 200,
    "message": "获取成功",
    "data": {
      "items": [
        {
          "userId": 1,
          "username": "admin",
          "phone": "13800138000",
          "email": "admin@example.com",
          "introduction": "管理员",
          "userStatus": 0,
          "createdAt": "2024-01-01 00:00:00"
        }
      ],
      "total": 100,
      "pageSize": 10,
      "currentPage": 1
    }
  }
  ```

### 3.2 新增用户

- **请求方法**: POST

- **URL**: `/admin/addUser`

- **请求参数**:

  ```json
  {
    "username": "test",
    "password": "Test123456",
    "phone": "13800138001",
    "email": "test@example.com",
    "introduction": "测试用户",
    "userStatus": 0
  }
  ```

- **参数说明**:

  | 参数名       | 类型    | 必需 | 描述                                      |
  | ------------ | ------- | ---- | ----------------------------------------- |
  | username     | String  | 是   | 用户名（4-16位字母、数字、下划线、连字符）|
  | password     | String  | 是   | 密码（8-18位数字、字母、符号的任意两种组合）|
  | phone        | String  | 否   | 手机号（1开头，11位数字）                 |
  | email        | String  | 是   | 邮箱                                      |
  | introduction | String  | 否   | 简介（100字以内）                         |
  | userStatus   | Integer | 否   | 用户状态（0: 启用, 1: 禁用）             |

- **请求头**: `Authorization: <token>`

- **响应格式**:

  ```json
  {
    "code": 200,
    "message": "添加成功",
    "data": null
  }
  ```

### 3.3 编辑用户

- **请求方法**: PUT

- **URL**: `/admin/updateUser`

- **请求参数**:

  ```json
  {
    "userId": 1,
    "username": "admin",
    "phone": "13800138000",
    "email": "admin@example.com",
    "introduction": "管理员"
  }
  ```

- **参数说明**:

  | 参数名       | 类型    | 必需 | 描述                                   |
  | ------------ | ------- | ---- | -------------------------------------- |
  | userId       | Long    | 是   | 用户ID                                 |
  | username     | String  | 是   | 用户名（4-16位字母、数字、下划线、连字符）|
  | phone        | String  | 否   | 手机号（1开头，11位数字）              |
  | email        | String  | 是   | 邮箱                                   |
  | introduction | String  | 否   | 简介（100字以内）                      |

- **请求头**: `Authorization: <token>`

- **响应格式**:

  ```json
  {
    "code": 200,
    "message": "更新成功",
    "data": null
  }
  ```

### 3.4 更新用户状态

- **请求方法**: PATCH

- **URL**: `/admin/updateUserStatus/{id}/{status}`

- **路径参数**:

  | 参数名 | 类型    | 描述                     |
  | ------ | ------- | ------------------------ |
  | id     | Long    | 用户ID                   |
  | status | Integer | 状态（0: 启用, 1: 禁用） |

- **请求头**: `Authorization: <token>`

- **响应格式**:

  ```json
  {
    "code": 200,
    "message": "更新成功",
    "data": null
  }
  ```

### 3.5 删除用户

- **请求方法**: DELETE

- **URL**: `/admin/deleteUser/{id}`

- **路径参数**:

  | 参数名 | 类型 | 描述   |
  | ------ | ---- | ------ |
  | id     | Long | 用户ID |

- **请求头**: `Authorization: <token>`

- **响应格式**:

  ```json
  {
    "code": 200,
    "message": "删除成功",
    "data": null
  }
  ```

### 3.6 批量删除用户

- **请求方法**: DELETE

- **URL**: `/admin/deleteUsers`

- **请求参数**:

  ```json
  [1, 2, 3]
  ```

- **请求头**: `Authorization: <token>`

- **响应格式**:

  ```json
  {
    "code": 200,
    "message": "删除成功",
    "data": null
  }
  ```

## 4. 歌手管理

### 4.1 获取歌手列表

- **请求方法**: POST

- **URL**: `/admin/getAllArtists`

- **请求参数**:

  ```json
  {
    "pageNum": 1,
    "pageSize": 10,
    "artistName": "",
    "gender": 0,
    "area": ""
  }
  ```

- **参数说明**:

  | 参数名     | 类型    | 必需 | 描述                          |
  | ---------- | ------- | ---- | ----------------------------- |
  | pageNum    | Integer | 是   | 页码                          |
  | pageSize   | Integer | 是   | 每页数量                      |
  | artistName | String  | 否   | 歌手名（模糊搜索）            |
  | gender     | Integer | 否   | 性别（0: 男, 1: 女, 2: 组合） |
  | area       | String  | 否   | 地区                          |

- **请求头**: `Authorization: <token>`

- **响应格式**:

  ```json
  {
    "code": 200,
    "message": "获取成功",
    "data": {
      "items": [
        {
          "artistId": 1,
          "artistName": "周杰伦",
          "gender": 0,
          "birth": "1979-01-18",
          "area": "中国台湾",
          "introduction": "华语流行音乐歌手",
          "avatar": "https://example.com/avatar.jpg",
          "createdAt": "2024-01-01 00:00:00"
        }
      ],
      "total": 50,
      "pageSize": 10,
      "currentPage": 1
    }
  }
  ```

### 4.2 新增歌手

- **请求方法**: POST

- **URL**: `/admin/addArtist`

- **请求参数**:

  ```json
  {
    "artistName": "周杰伦",
    "gender": 0,
    "birth": "1979-01-18",
    "area": "中国台湾",
    "introduction": "华语流行音乐歌手"
  }
  ```

- **参数说明**:

  | 参数名       | 类型    | 必需 | 描述                          |
  | ------------ | ------- | ---- | ----------------------------- |
  | artistName   | String  | 否   | 歌手名                        |
  | gender       | Integer | 否   | 性别（0: 男, 1: 女, 2: 组合） |
  | birth        | String  | 否   | 出生日期（格式: YYYY-MM-DD）  |
  | area         | String  | 否   | 国籍/地区                     |
  | introduction | String  | 否   | 歌手简介                      |

- **请求头**: `Authorization: <token>`

- **响应格式**:

  ```json
  {
    "code": 200,
    "message": "添加成功",
    "data": null
  }
  ```

### 4.3 编辑歌手

- **请求方法**: PUT

- **URL**: `/admin/updateArtist`

- **请求参数**:

  ```json
  {
    "artistId": 1,
    "artistName": "周杰伦",
    "gender": 0,
    "birth": "1979-01-18",
    "area": "中国台湾",
    "introduction": "华语流行音乐歌手"
  }
  ```

- **参数说明**:

  | 参数名       | 类型    | 必需 | 描述                          |
  | ------------ | ------- | ---- | ----------------------------- |
  | artistId     | Long    | 是   | 歌手ID                        |
  | artistName   | String  | 否   | 歌手名                        |
  | gender       | Integer | 否   | 性别（0: 男, 1: 女, 2: 组合） |
  | birth        | String  | 否   | 出生日期（格式: YYYY-MM-DD）  |
  | area         | String  | 否   | 国籍/地区                     |
  | introduction | String  | 否   | 歌手简介                      |

- **请求头**: `Authorization: <token>`

- **响应格式**:

  ```json
  {
    "code": 200,
    "message": "更新成功",
    "data": null
  }
  ```

### 4.4 更新歌手头像

- **请求方法**: PATCH

- **URL**: `/admin/updateArtistAvatar/{id}`

- **路径参数**:

  | 参数名 | 类型 | 描述   |
  | ------ | ---- | ------ |
  | id     | Long | 歌手ID |

- **请求参数**: `multipart/form-data`

  | 参数名 | 类型 | 必需 | 描述     |
  | ------ | ---- | ---- | -------- |
  | avatar | file | 是   | 头像文件 |

- **请求头**: `Authorization: <token>`

- **响应格式**:

  ```json
  {
    "code": 200,
    "message": "更新成功",
    "data": "https://example.com/avatar.jpg"
  }
  ```

### 4.5 删除歌手

- **请求方法**: DELETE

- **URL**: `/admin/deleteArtist/{id}`

- **路径参数**:

  | 参数名 | 类型 | 描述   |
  | ------ | ---- | ------ |
  | id     | Long | 歌手ID |

- **请求头**: `Authorization: <token>`

- **响应格式**:

  ```json
  {
    "code": 200,
    "message": "删除成功",
    "data": null
  }
  ```

### 4.6 批量删除歌手

- **请求方法**: DELETE

- **URL**: `/admin/deleteArtists`

- **请求参数**:

  ```json
  [1, 2, 3]
  ```

- **请求头**: `Authorization: <token>`

- **响应格式**:

  ```json
  {
    "code": 200,
    "message": "删除成功",
    "data": null
  }
  ```

## 5. 歌曲管理

### 5.1 获取所有歌手名称列表

- **请求方法**: GET

- **URL**: `/admin/getAllArtistNames`

- **请求头**: `Authorization: <token>`

- **响应格式**:

  ```json
  {
    "code": 200,
    "message": "获取成功",
    "data": [
      {
        "artistId": 1,
        "artistName": "周杰伦"
      }
    ]
  }
  ```

### 5.2 获取歌曲列表

- **请求方法**: POST

- **URL**: `/admin/getAllSongsByArtist`

- **请求参数**:

  ```json
  {
    "pageNum": 1,
    "pageSize": 10,
    "artistId": 1,
    "songName": "",
    "album": ""
  }
  ```

- **参数说明**:

  | 参数名   | 类型    | 必需 | 描述           |
  | -------- | ------- | ---- | -------------- |
  | pageNum  | Integer | 是   | 页码           |
  | pageSize | Integer | 是   | 每页数量       |
  | artistId | Long    | 否   | 歌手ID         |
  | songName | String  | 否   | 歌曲名（模糊搜索）|
  | album    | String  | 否   | 专辑名（模糊搜索）|

- **请求头**: `Authorization: <token>`

- **响应格式**:

  ```json
  {
    "code": 200,
    "message": "获取成功",
    "data": {
      "items": [
        {
          "songId": 1,
          "songName": "七里香",
          "artistId": 1,
          "artistName": "周杰伦",
          "album": "七里香",
          "style": "流行",
          "cover": "https://example.com/cover.jpg",
          "audio": "https://example.com/audio.mp3",
          "lyrics": "窗外的麻雀...",
          "releaseTime": "2004-08-03",
          "createdAt": "2024-01-01 00:00:00"
        }
      ],
      "total": 200,
      "pageSize": 10,
      "currentPage": 1
    }
  }
  ```

### 5.3 新增歌曲

- **请求方法**: POST

- **URL**: `/admin/addSong`

- **请求参数**:

  ```json
  {
    "artistId": 1,
    "songName": "七里香",
    "album": "七里香",
    "style": "流行",
    "releaseTime": "2004-08-03"
  }
  ```

- **参数说明**:

  | 参数名      | 类型    | 必需 | 描述                         |
  | ----------- | ------- | ---- | ---------------------------- |
  | artistId    | Long    | 否   | 歌手ID                       |
  | songName    | String  | 否   | 歌曲名                       |
  | album       | String  | 否   | 专辑名                       |
  | style       | String  | 否   | 歌曲风格                     |
  | releaseTime | String  | 否   | 发行时间（格式: YYYY-MM-DD） |

- **请求头**: `Authorization: <token>`

- **响应格式**:

  ```json
  {
    "code": 200,
    "message": "添加成功",
    "data": null
  }
  ```

### 5.4 编辑歌曲

- **请求方法**: PUT

- **URL**: `/admin/updateSong`

- **请求参数**:

  ```json
  {
    "songId": 1,
    "artistId": 1,
    "songName": "七里香",
    "album": "七里香",
    "style": "流行",
    "releaseTime": "2004-08-03"
  }
  ```

- **参数说明**:

  | 参数名      | 类型    | 必需 | 描述                         |
  | ----------- | ------- | ---- | ---------------------------- |
  | songId      | Long    | 是   | 歌曲ID                       |
  | artistId    | Long    | 否   | 歌手ID                       |
  | songName    | String  | 否   | 歌曲名                       |
  | album       | String  | 否   | 专辑名                       |
  | style       | String  | 否   | 歌曲风格                     |
  | releaseTime | String  | 否   | 发行时间（格式: YYYY-MM-DD） |

- **请求头**: `Authorization: <token>`

- **响应格式**:

  ```json
  {
    "code": 200,
    "message": "更新成功",
    "data": null
  }
  ```

### 5.5 更新歌曲封面

- **请求方法**: PATCH

- **URL**: `/admin/updateSongCover/{id}`

- **路径参数**:

  | 参数名 | 类型 | 描述   |
  | ------ | ---- | ------ |
  | id     | Long | 歌曲ID |

- **请求参数**: `multipart/form-data`

  | 参数名 | 类型 | 必需 | 描述     |
  | ------ | ---- | ---- | -------- |
  | cover  | file | 是   | 封面文件 |

- **请求头**: `Authorization: <token>`

- **响应格式**:

  ```json
  {
    "code": 200,
    "message": "更新成功",
    "data": "https://example.com/cover.jpg"
  }
  ```

### 5.6 更新歌曲音频

- **请求方法**: PATCH

- **URL**: `/admin/updateSongAudio/{id}`

- **路径参数**:

  | 参数名 | 类型 | 描述   |
  | ------ | ---- | ------ |
  | id     | Long | 歌曲ID |

- **请求参数**: `multipart/form-data`

  | 参数名 | 类型 | 必需 | 描述     |
  | ------ | ---- | ---- | -------- |
  | audio  | file | 是   | 音频文件 |

- **请求头**: `Authorization: <token>`

- **响应格式**:

  ```json
  {
    "code": 200,
    "message": "更新成功",
    "data": "https://example.com/audio.mp3"
  }
  ```

### 5.7 删除歌曲

- **请求方法**: DELETE

- **URL**: `/admin/deleteSong/{id}`

- **路径参数**:

  | 参数名 | 类型 | 描述   |
  | ------ | ---- | ------ |
  | id     | Long | 歌曲ID |

- **请求头**: `Authorization: <token>`

- **响应格式**:

  ```json
  {
    "code": 200,
    "message": "删除成功",
    "data": null
  }
  ```

### 5.8 批量删除歌曲

- **请求方法**: DELETE

- **URL**: `/admin/deleteSongs`

- **请求参数**:

  ```json
  [1, 2, 3]
  ```

- **请求头**: `Authorization: <token>`

- **响应格式**:

  ```json
  {
    "code": 200,
    "message": "删除成功",
    "data": null
  }
  ```

## 6. 歌单管理

### 6.1 获取歌单列表

- **请求方法**: POST

- **URL**: `/admin/getAllPlaylists`

- **请求参数**:

  ```json
  {
    "pageNum": 1,
    "pageSize": 10,
    "title": "",
    "style": ""
  }
  ```

- **参数说明**:

  | 参数名   | 类型    | 必需 | 描述               |
  | -------- | ------- | ---- | ------------------ |
  | pageNum  | Integer | 是   | 页码               |
  | pageSize | Integer | 是   | 每页数量           |
  | title    | String  | 否   | 歌单标题（模糊搜索）|
  | style    | String  | 否   | 歌单风格           |

- **请求头**: `Authorization: <token>`

- **响应格式**:

  ```json
  {
    "code": 200,
    "message": "获取成功",
    "data": {
      "items": [
        {
          "playlistId": 1,
          "title": "流行金曲",
          "style": "流行",
          "cover": "https://example.com/cover.jpg",
          "introduction": "流行音乐合集",
          "createdAt": "2024-01-01 00:00:00"
        }
      ],
      "total": 80,
      "pageSize": 10,
      "currentPage": 1
    }
  }
  ```

### 6.2 新增歌单

- **请求方法**: POST

- **URL**: `/admin/addPlaylist`

- **请求参数**:

  ```json
  {
    "title": "流行金曲",
    "style": "流行",
    "introduction": "流行音乐合集"
  }
  ```

- **参数说明**:

  | 参数名       | 类型   | 必需 | 描述     |
  | ------------ | ------ | ---- | -------- |
  | title        | String | 否   | 歌单标题 |
  | style        | String | 否   | 歌单风格 |
  | introduction | String | 否   | 歌单简介 |

- **请求头**: `Authorization: <token>`

- **响应格式**:

  ```json
  {
    "code": 200,
    "message": "添加成功",
    "data": null
  }
  ```

### 6.3 编辑歌单

- **请求方法**: PUT

- **URL**: `/admin/updatePlaylist`

- **请求参数**:

  ```json
  {
    "playlistId": 1,
    "title": "流行金曲",
    "style": "流行",
    "introduction": "流行音乐合集"
  }
  ```

- **参数说明**:

  | 参数名       | 类型   | 必需 | 描述     |
  | ------------ | ------ | ---- | -------- |
  | playlistId   | Long   | 是   | 歌单ID   |
  | title        | String | 否   | 歌单标题 |
  | style        | String | 否   | 歌单风格 |
  | introduction | String | 否   | 歌单简介 |

- **请求头**: `Authorization: <token>`

- **响应格式**:

  ```json
  {
    "code": 200,
    "message": "更新成功",
    "data": null
  }
  ```

### 6.4 更新歌单封面

- **请求方法**: PATCH

- **URL**: `/admin/updatePlaylistCover/{id}`

- **路径参数**:

  | 参数名 | 类型 | 描述   |
  | ------ | ---- | ------ |
  | id     | Long | 歌单ID |

- **请求参数**: `multipart/form-data`

  | 参数名 | 类型 | 必需 | 描述     |
  | ------ | ---- | ---- | -------- |
  | cover  | file | 是   | 封面文件 |

- **请求头**: `Authorization: <token>`

- **响应格式**:

  ```json
  {
    "code": 200,
    "message": "更新成功",
    "data": "https://example.com/cover.jpg"
  }
  ```

### 6.5 删除歌单

- **请求方法**: DELETE

- **URL**: `/admin/deletePlaylist/{id}`

- **路径参数**:

  | 参数名 | 类型 | 描述   |
  | ------ | ---- | ------ |
  | id     | Long | 歌单ID |

- **请求头**: `Authorization: <token>`

- **响应格式**:

  ```json
  {
    "code": 200,
    "message": "删除成功",
    "data": null
  }
  ```

### 6.6 批量删除歌单

- **请求方法**: DELETE

- **URL**: `/admin/deletePlaylists`

- **请求参数**:

  ```json
  [1, 2, 3]
  ```

- **请求头**: `Authorization: <token>`

- **响应格式**:

  ```json
  {
    "code": 200,
    "message": "删除成功",
    "data": null
  }
  ```

## 7. 反馈管理

### 7.1 获取反馈列表

- **请求方法**: POST

- **URL**: `/admin/getAllFeedbacks`

- **请求参数**:

  ```json
  {
    "pageNum": 1,
    "pageSize": 10,
    "keyword": ""
  }
  ```

- **参数说明**:

  | 参数名   | 类型    | 必需 | 描述                 |
  | -------- | ------- | ---- | -------------------- |
  | pageNum  | Integer | 是   | 页码                 |
  | pageSize | Integer | 是   | 每页数量             |
  | keyword  | String  | 否   | 反馈内容关键字（模糊搜索）|

- **请求头**: `Authorization: <token>`

- **响应格式**:

  ```json
  {
    "code": 200,
    "message": "获取成功",
    "data": {
      "items": [
        {
          "feedbackId": 1,
          "userId": 1,
          "username": "user1",
          "content": "建议增加更多歌曲",
          "createdAt": "2024-01-01 00:00:00"
        }
      ],
      "total": 30,
      "pageSize": 10,
      "currentPage": 1
    }
  }
  ```

### 7.2 删除反馈

- **请求方法**: DELETE

- **URL**: `/admin/deleteFeedback/{id}`

- **路径参数**:

  | 参数名 | 类型 | 描述   |
  | ------ | ---- | ------ |
  | id     | Long | 反馈ID |

- **请求头**: `Authorization: <token>`

- **响应格式**:

  ```json
  {
    "code": 200,
    "message": "删除成功",
    "data": null
  }
  ```

### 7.3 批量删除反馈

- **请求方法**: DELETE

- **URL**: `/admin/deleteFeedbacks`

- **请求参数**:

  ```json
  [1, 2, 3]
  ```

- **请求头**: `Authorization: <token>`

- **响应格式**:

  ```json
  {
    "code": 200,
    "message": "删除成功",
    "data": null
  }
  ```

## 8. 轮播图管理

### 8.1 获取轮播图列表

- **请求方法**: POST

- **URL**: `/admin/getAllBanners`

- **请求参数**:

  ```json
  {
    "pageNum": 1,
    "pageSize": 10,
    "bannerStatus": 0
  }
  ```

- **参数说明**:

  | 参数名       | 类型    | 必需 | 描述                     |
  | ------------ | ------- | ---- | ------------------------ |
  | pageNum      | Integer | 是   | 页码                     |
  | pageSize     | Integer | 是   | 每页数量                 |
  | bannerStatus | Integer | 否   | 轮播图状态（0: 启用, 1: 禁用）|

- **请求头**: `Authorization: <token>`

- **响应格式**:

  ```json
  {
    "code": 200,
    "message": "获取成功",
    "data": {
      "items": [
        {
          "bannerId": 1,
          "image": "https://example.com/banner.jpg",
          "url": "https://example.com/song/1",
          "bannerStatus": 0,
          "createdAt": "2024-01-01 00:00:00"
        }
      ],
      "total": 10,
      "pageSize": 10,
      "currentPage": 1
    }
  }
  ```

### 8.2 新增轮播图

- **请求方法**: POST

- **URL**: `/admin/addBanner`

- **请求参数**: `multipart/form-data`

  | 参数名 | 类型 | 必需 | 描述         |
  | ------ | ---- | ---- | ------------ |
  | banner | file | 是   | 轮播图文件   |

- **请求头**: `Authorization: <token>`

- **响应格式**:

  ```json
  {
    "code": 200,
    "message": "添加成功",
    "data": null
  }
  ```

### 8.3 编辑轮播图

- **请求方法**: POST

- **URL**: `/admin/updateBanner/{id}`

- **路径参数**:

  | 参数名 | 类型 | 描述     |
  | ------ | ---- | -------- |
  | id     | Long | 轮播图ID |

- **请求参数**: `multipart/form-data`

  | 参数名 | 类型 | 必需 | 描述       |
  | ------ | ---- | ---- | ---------- |
  | banner | file | 是   | 轮播图文件 |

- **请求头**: `Authorization: <token>`

- **响应格式**:

  ```json
  {
    "code": 200,
    "message": "更新成功",
    "data": null
  }
  ```

### 8.4 更新轮播图状态

- **请求方法**: PATCH

- **URL**: `/admin/updateBannerStatus/{id}`

- **路径参数**:

  | 参数名 | 类型 | 描述     |
  | ------ | ---- | -------- |
  | id     | Long | 轮播图ID |

- **请求参数**:

  | 参数名 | 类型    | 必需 | 描述                     |
  | ------ | ------- | ---- | ------------------------ |
  | status | Integer | 是   | 状态（0: 启用, 1: 禁用） |

- **请求头**: `Authorization: <token>`

- **响应格式**:

  ```json
  {
    "code": 200,
    "message": "更新成功",
    "data": null
  }
  ```

### 8.5 删除轮播图

- **请求方法**: DELETE

- **URL**: `/admin/deleteBanner/{id}`

- **路径参数**:

  | 参数名 | 类型 | 描述     |
  | ------ | ---- | -------- |
  | id     | Long | 轮播图ID |

- **请求头**: `Authorization: <token>`

- **响应格式**:

  ```json
  {
    "code": 200,
    "message": "删除成功",
    "data": null
  }
  ```

### 8.6 批量删除轮播图

- **请求方法**: DELETE

- **URL**: `/admin/deleteBanners`

- **请求参数**:

  ```json
  [1, 2, 3]
  ```

- **请求头**: `Authorization: <token>`

- **响应格式**:

  ```json
  {
    "code": 200,
    "message": "删除成功",
    "data": null
  }
  ```

## 响应状态码

| 状态码 | 描述           |
| ------ | -------------- |
| 200    | 操作成功       |
| 400    | 请求参数错误   |
| 401    | 未授权         |
| 403    | 无权限         |
| 404    | 资源不存在     |
| 500    | 服务器内部错误 |

## 修改记录

### 2026-03-31
- 修复代码错误：AdminController中getAllFeedbacks方法使用了错误的HTTP方法和实现
- 修复代码错误：deleteArtist方法参数注解错误
- 修复代码错误：updatePlaylistCover方法参数注解错误
- 修复代码错误：deleteUsers方法缺少@RequestBody注解
- 统一分页参数：所有分页接口使用pageNum和pageSize
- 更新用户管理接口参数：新增phone、introduction字段
- 更新歌手管理接口参数：使用artistName、birth、introduction等字段
- 更新歌曲管理接口参数：使用songName、album、releaseTime等字段
- 更新歌单管理接口参数：使用title、introduction等字段
- 更新轮播图管理接口：修正addBanner和updateBanner参数
- 更新反馈管理接口：使用keyword字段进行搜索
