# Vibe Music API 接口文档

## 基础信息

- **Base URL**: `http://localhost:9080`
- **请求超时**: 20000ms (20秒)
- **认证方式**: Bearer Token (JWT)
- **请求头**:
  - `Accept`: `application/json, text/plain, */*`
  - `Content-Type`: `application/json`
  - `X-Requested-With`: `XMLHttpRequest`

---

## 通用响应格式

### 成功响应

```typescript
interface Result<T> {
  code: number        // 业务状态码：0-成功，1-失败
  message: string     // 提示信息
  data?: T           // 响应数据
}
```

### 分页响应

```typescript
interface PageResult<T> {
  total: number           // 总条目数
  items: Array<T>         // 列表数据
}
```

**注意**：分页接口返回 `Result<PageResult<T>>` 格式

---

## 用户模块

### 1. 用户登录

**接口**: `POST /user/login`

**描述**: 用户登录认证

**请求参数**:
```json
{
  "email": "string",
  "password": "string"
}
```

**响应数据**:
```json
{
  "code": 0,
  "message": "登录成功",
  "data": {
    "token": "string",
    "userInfo": {
      "userId": "number",
      "username": "string",
      "phone": "string",
      "email": "string",
      "userAvatar": "string",
      "introduction": "string"
    }
  }
}
```

---

### 2. 用户登出

**接口**: `POST /user/logout`

**描述**: 用户退出登录

**需要认证**: 是

**响应数据**:
```json
{
  "code": 200,
  "message": "登出成功"
}
```

---

### 3. 发送邮箱验证码

**接口**: `GET /user/sendVerificationCode`

**描述**: 发送邮箱验证码（用于注册或重置密码）

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| email | string | 是 | 用户邮箱 |

**响应数据**:
```json
{
  "code": 200,
  "message": "验证码已发送"
}
```

---

### 4. 用户注册

**接口**: `POST /user/register`

**描述**: 用户注册新账号

**请求参数**:
```json
{
  "email": "string",
  "password": "string",
  "verificationCode": "string",
  "username": "string"
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "注册成功"
}
```

---

### 5. 重置密码

**接口**: `PATCH /user/resetUserPassword`

**描述**: 重置用户密码

**需要认证**: 否

**请求参数**:
```json
{
  "email": "string",
  "verificationCode": "string",
  "newPassword": "string"
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "密码重置成功"
}
```

---

### 6. 获取用户信息

**接口**: `GET /user/getUserInfo`

**描述**: 获取当前登录用户的详细信息

**需要认证**: 是

**响应数据**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "userId": "number",
    "username": "string",
    "email": "string",
    "avatar": "string",
    "createTime": "string"
  }
}
```

---

### 7. 更新用户信息

**接口**: `PUT /user/updateUserInfo`

**描述**: 更新当前用户的基本信息

**需要认证**: 是

**请求参数**:
```json
{
  "username": "string",
  "email": "string"
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "更新成功"
}
```

---

### 8. 更新用户头像

**接口**: `PATCH /user/updateUserAvatar`

**描述**: 更新用户头像

**需要认证**: 是

**请求类型**: `multipart/form-data`

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| file | File | 是 | 头像图片文件 |

**响应数据**:
```json
{
  "code": 200,
  "message": "头像更新成功",
  "data": {
    "avatarUrl": "string"
  }
}
```

---

### 9. 注销账号

**接口**: `DELETE /user/deleteAccount`

**描述**: 删除当前用户账号

**需要认证**: 是

**响应数据**:
```json
{
  "code": 0,
  "message": "账号已注销"
}
```

---

## 音乐模块

### 10. 获取轮播图

**接口**: `GET /banner/getBannerList`

**描述**: 获取首页轮播图列表

**响应数据**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "bannerId": "number",
      "imageUrl": "string",
      "targetUrl": "string",
      "title": "string"
    }
  ]
}
```

---

### 11. 获取推荐歌单

**接口**: `GET /playlist/getRecommendedPlaylists`

**描述**: 获取推荐歌单列表

**响应数据**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "playlistId": "number",
      "title": "string",
      "coverUrl": "string",
      "playCount": "number"
    }
  ]
}
```

---

### 12. 获取推荐歌曲

**接口**: `GET /song/getRecommendedSongs`

**描述**: 获取推荐歌曲列表

**响应数据**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": [
    {
      "songId": "number",
      "songName": "string",
      "artistName": "string",
      "coverUrl": "string",
      "duration": "string"
    }
  ]
}
```

---

### 13. 获取所有歌曲

**接口**: `POST /song/getAllSongs`

**描述**: 分页获取所有歌曲列表

**请求参数**:
```json
{
  "currentPage": "number",
  "pageSize": "number",
  "keyword": "string"  // 可选，搜索关键词
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "items": [
      {
        "songId": "number",
        "songName": "string",
        "artistName": "string",
        "album": "string",
        "duration": "string",
        "coverUrl": "string",
        "audioUrl": "string",
        "likeStatus": "number",
        "releaseTime": "string"
      }
    ],
    "total": "number",
    "pageSize": "number",
    "currentPage": "number"
  }
}
```

---

### 14. 获取歌曲详情

**接口**: `GET /song/getSongDetail/{id}`

**描述**: 根据歌曲ID获取详细信息

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | number | 是 | 歌曲ID |

**响应数据**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "songId": "number",
    "songName": "string",
    "artistName": "string",
    "album": "string",
    "lyric": "string | null",
    "duration": "string",
    "coverUrl": "string",
    "audioUrl": "string",
    "releaseTime": "string",
    "likeStatus": "boolean | null",
    "comments": [
      {
        "commentId": "number",
        "username": "string",
        "userAvatar": "string",
        "content": "string",
        "createTime": "string",
        "likeCount": "number"
      }
    ]
  }
}
```

---

### 15. 获取音乐播放链接

**接口**: `GET /song/url/v1`

**描述**: 获取指定音质的音乐播放链接

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | number/string | 是 | 歌曲ID |
| level | string | 是 | 音质等级 |

**响应数据**:
```json
{
  "data": [
    {
      "url": "string"
    }
  ]
}
```

---

## 歌手模块

### 16. 获取所有歌手

**接口**: `POST /artist/getAllArtists`

**描述**: 分页获取所有歌手列表

**请求参数**:
```json
{
  "currentPage": "number",
  "pageSize": "number",
  "keyword": "string"  // 可选，搜索关键词
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "items": [
      {
        "artistId": "number",
        "artistName": "string",
        "avatar": "string",
        "songCount": "number"
      }
    ],
    "total": "number",
    "pageSize": "number",
    "currentPage": "number"
  }
}
```

---

### 17. 获取歌手详情

**接口**: `GET /artist/getArtistDetail/{id}`

**描述**: 根据歌手ID获取详细信息

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | number | 是 | 歌手ID |

**响应数据**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "artistId": "number",
    "artistName": "string",
    "avatar": "string",
    "description": "string",
    "songCount": "number",
    "songs": [
      {
        "songId": "number",
        "songName": "string",
        "album": "string",
        "duration": "string"
      }
    ]
  }
}
```

---

## 歌单模块

### 18. 获取所有歌单

**接口**: `POST /playlist/getAllPlaylists`

**描述**: 分页获取所有歌单列表

**请求参数**:
```json
{
  "currentPage": "number",
  "pageSize": "number",
  "keyword": "string"  // 可选，搜索关键词
}
```

**响应数据**:
```json
{
  "code": 0,
  "message": "获取成功",
  "data": {
    "total": "number",
    "items": [
      {
        "playlistId": "number",
        "title": "string",
        "coverUrl": "string"
      }
    ]
  }
}
```

---

### 19. 获取歌单详情

**接口**: `GET /playlist/getPlaylistDetail/{id}`

**描述**: 根据歌单ID获取详细信息

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | number | 是 | 歌单ID |

**响应数据**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "playlistId": "number",
    "title": "string",
    "coverUrl": "string",
    "introduction": "string",
    "songs": [
      {
        "songId": "number",
        "songName": "string",
        "artistName": "string",
        "album": "string",
        "duration": "string",
        "coverUrl": "string",
        "audioUrl": "string",
        "likeStatus": "number",
        "releaseTime": "string"
      }
    ],
    "likeStatus": "number",
    "comments": [
      {
        "commentId": "number",
        "username": "string",
        "userAvatar": "string",
        "content": "string",
        "createTime": "string",
        "likeCount": "number"
      }
    ],
    "isCollected": "boolean"
  }
}
```

---

## 收藏模块

### 20. 获取用户收藏的歌曲

**接口**: `POST /favorite/getFavoriteSongs`

**描述**: 获取当前用户收藏的歌曲列表

**需要认证**: 是

**请求参数**:
```json
{
  "currentPage": "number",
  "pageSize": "number"
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "items": [
      {
        "songId": "number",
        "songName": "string",
        "artistName": "string",
        "album": "string",
        "coverUrl": "string"
      }
    ],
    "total": "number"
  }
}
```

---

### 21. 收藏歌曲

**接口**: `POST /favorite/collectSong`

**描述**: 收藏指定歌曲

**需要认证**: 是

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| songId | number | 是 | 歌曲ID |

**响应数据**:
```json
{
  "code": 200,
  "message": "收藏成功"
}
```

---

### 22. 取消收藏歌曲

**接口**: `DELETE /favorite/cancelCollectSong`

**描述**: 取消收藏指定歌曲

**需要认证**: 是

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| songId | number | 是 | 歌曲ID |

**响应数据**:
```json
{
  "code": 200,
  "message": "取消收藏成功"
}
```

---

### 23. 获取用户收藏的歌单

**接口**: `POST /favorite/getFavoritePlaylists`

**描述**: 获取当前用户收藏的歌单列表

**需要认证**: 是

**请求参数**:
```json
{
  "currentPage": "number",
  "pageSize": "number"
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "获取成功",
  "data": {
    "items": [
      {
        "playlistId": "number",
        "title": "string",
        "coverUrl": "string",
        "songCount": "number"
      }
    ],
    "total": "number"
  }
}
```

---

### 24. 收藏歌单

**接口**: `POST /favorite/collectPlaylist`

**描述**: 收藏指定歌单

**需要认证**: 是

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| playlistId | number | 是 | 歌单ID |

**响应数据**:
```json
{
  "code": 0,
  "message": "收藏成功"
}
```

---

### 25. 取消收藏歌单

**接口**: `DELETE /favorite/cancelCollectPlaylist`

**描述**: 取消收藏指定歌单

**需要认证**: 是

**请求参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| playlistId | number | 是 | 歌单ID |

**响应数据**:
```json
{
  "code": 200,
  "message": "取消收藏成功"
}
```

---

## 评论模块

### 26. 新增歌曲评论

**接口**: `POST /comment/addSongComment`

**描述**: 为歌曲添加评论

**需要认证**: 是

**请求参数**:
```json
{
  "songId": "number",
  "content": "string"
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "评论成功"
}
```

---

### 27. 新增歌单评论

**接口**: `POST /comment/addPlaylistComment`

**描述**: 为歌单添加评论

**需要认证**: 是

**请求参数**:
```json
{
  "playlistId": "number",
  "content": "string"
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "评论成功"
}
```

---

### 28. 点赞评论

**接口**: `PATCH /comment/likeComment/{commentId}`

**描述**: 点赞指定评论

**需要认证**: 是

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| commentId | number | 是 | 评论ID |

**响应数据**:
```json
{
  "code": 200,
  "message": "点赞成功"
}
```

---

### 29. 取消点赞评论

**接口**: `PATCH /comment/cancelLikeComment/{commentId}`

**描述**: 取消点赞指定评论

**需要认证**: 是

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| commentId | number | 是 | 评论ID |

**响应数据**:
```json
{
  "code": 0,
  "message": "取消点赞成功"
}
```

---

### 30. 删除评论

**接口**: `DELETE /comment/deleteComment/{commentId}`

**描述**: 删除指定评论

**需要认证**: 是

**路径参数**:
| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| commentId | number | 是 | 评论ID |

**响应数据**:
```json
{
  "code": 200,
  "message": "删除成功"
}
```

---

## 反馈模块

### 31. 新增反馈

**接口**: `POST /feedback/addFeedback`

**描述**: 提交用户反馈

**需要认证**: 是

**请求参数**:
```json
{
  "content": "string"
}
```

**响应数据**:
```json
{
  "code": 200,
  "message": "反馈提交成功"
}
```

---

## 错误码说明

| 状态码 | 说明 |
|--------|------|
| 0 | 请求成功 |
| 1 | 请求失败 |
| 401 | 未授权，需要登录或Token过期 |
| 403 | 没有权限访问该资源 |
| 404 | 请求的资源不存在 |
| 500 | 服务器内部错误 |

---

## 认证说明

### Token 使用方式

除登录接口外，其他需要认证的接口必须在请求头中携带 Token：

```
Authorization: <token>
```

### Token 获取

通过登录接口 `/user/login` 获取，返回的 `data.token` 即为认证 Token。

### Token 过期处理

当 Token 过期或无效时，接口会返回 401 状态码，前端应清除本地存储的用户信息并跳转到登录页面。

---

## 数据类型定义

### Song (歌曲)

```typescript
interface Song {
  songId: number
  songName: string
  artistName: string
  album: string
  duration: string
  coverUrl: string
  audioUrl: string
  likeStatus: number
  releaseTime: string
}
```

### PlaylistSong (歌单歌曲)

```typescript
interface PlaylistSong {
  songId: number
  songName: string
  artistName: string
  album: string
  duration: string
  coverUrl: string | null
  audioUrl: string
  likeStatus: number
  releaseTime: string | null
}
```

### Comment (评论)

```typescript
interface Comment {
  commentId: number
  username: string
  userAvatar: string | null
  content: string
  createTime: string
  likeCount: number
}
```

### PlaylistDetail (歌单详情)

```typescript
interface PlaylistDetail {
  playlistId: number
  title: string
  coverUrl: string | null
  introduction: string
  songs: PlaylistSong[]
  likeStatus: number
  comments: PlaylistComment[]
  isCollected: boolean
}
```

### SongDetail (歌曲详情)

```typescript
interface SongDetail {
  songId: number
  songName: string
  artistName: string
  album: string
  lyric: string | null
  duration: string
  coverUrl: string
  audioUrl: string
  releaseTime: string
  likeStatus: boolean | null
  comments: Comment[]
}
```

---

## 注意事项

1. 所有需要认证的接口，如果未携带 Token 或 Token 过期，将返回 401 错误
2. 分页查询接口统一使用 POST 方法，请求体包含分页参数
3. 文件上传接口使用 `multipart/form-data` 格式
4. 所有时间字段格式为 `yyyy-MM-dd`
5. 响应数据中的 `code` 字段为 0 表示请求成功，1 表示请求失败
6. 错误信息通过 `message` 字段返回
7. `likeStatus` 字段：0 表示未收藏/未点赞，1 表示已收藏/已点赞

---

**文档版本**: v1.0  
**更新日期**: 2026-03-31  
**项目**: Vibe Music Client
