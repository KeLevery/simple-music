import axios from 'axios'

const BASE_URL = 'http://localhost:9080'

async function runTests() {
  console.log('====================================================')
  console.log('🚀 开始全面运行后端 API 接口单元与连通性测试 (端口 9080)')
  console.log('====================================================\n')

  const results = []
  let userToken = ''
  let samplePlaylistId = 2
  let sampleArtistId = 7
  let sampleSongId = 1

  async function callApi(name, method, url, data = null, headers = {}) {
    const startTime = Date.now()
    try {
      const response = await axios({
        method,
        url: `${BASE_URL}${url}`,
        data,
        headers,
        timeout: 4000,
        validateStatus: () => true
      })
      const cost = Date.now() - startTime
      const status = response.status
      const body = response.data
      const isOk = status === 200 && (body?.code === 0 || body?.code === 200)

      results.push({
        name,
        endpoint: `${method} ${url}`,
        status,
        code: body?.code,
        msg: body?.message || '',
        cost: `${cost}ms`,
        pass: isOk
      })

      const icon = isOk ? '✅ [PASS]' : (status === 404 ? '❌ [404 NOT FOUND]' : `⚠️ [STATUS ${status}]`)
      console.log(`${icon.padEnd(20)} ${name.padEnd(20)} ${method} ${url} (耗时: ${cost}ms, 状态码: ${status}, 业务code: ${body?.code})`)
      if (!isOk) {
        console.log(`   ↳ 错误详情: status=${status}, body=${JSON.stringify(body).slice(0, 150)}`)
      }
      return body
    } catch (err) {
      console.log(`❌ [REQ FAILED]     ${name.padEnd(20)} 错误: ${err.message}`)
      results.push({
        name,
        endpoint: `${method} ${url}`,
        status: 'ERROR',
        pass: false,
        msg: err.message
      })
      return null
    }
  }

  // 1. 用户登录测试 (验证密码校验与 Redis 令牌存储)
  console.log('--- [阶段 1: 用户鉴权测试] ---')
  const loginRes = await callApi('1. 用户密码登录', 'POST', '/user/login', {
    email: 'user651@example.com',
    password: '123456abc'
  })
  if (loginRes?.code === 0 && loginRes?.data) {
    userToken = loginRes.data
    console.log(`   ℹ️ 成功获取 JWT 登录令牌: ${userToken.slice(0, 25)}...`)
  }

  // 2. 轮播图与公开音乐资源
  console.log('\n--- [阶段 2: 公开音乐资源列表测试] ---')
  await callApi('2. 轮播图列表', 'GET', '/banner/getBannerList')
  
  const recPlaylistRes = await callApi('3. 首页推荐歌单', 'GET', '/playlist/getRecommendedPlaylists')
  if (recPlaylistRes?.data && recPlaylistRes.data.length > 0) {
    samplePlaylistId = recPlaylistRes.data[0].id || recPlaylistRes.data[0].playlistId || samplePlaylistId
  }

  const allPlaylistRes = await callApi('4. 歌单广场全部歌单', 'POST', '/playlist/getAllPlaylists', { pageNum: 1, pageSize: 12 })
  if (allPlaylistRes?.data?.records?.length > 0) {
    samplePlaylistId = allPlaylistRes.data.records[0].id || allPlaylistRes.data.records[0].playlistId || samplePlaylistId
  }

  await callApi(`5. 歌单详情 (ID:${samplePlaylistId})`, 'GET', `/playlist/getPlaylistDetail/${samplePlaylistId}`)

  const recSongRes = await callApi('6. 首页推荐歌曲', 'GET', '/song/getRecommendedSongs')
  if (recSongRes?.data && recSongRes.data.length > 0) {
    sampleSongId = recSongRes.data[0].id || recSongRes.data[0].songId || sampleSongId
  }

  const allSongRes = await callApi('7. 曲库全部歌曲', 'POST', '/song/getAllSongs', { pageNum: 1, pageSize: 20 })
  if (allSongRes?.data?.records?.length > 0) {
    sampleSongId = allSongRes.data.records[0].id || allSongRes.data.records[0].songId || sampleSongId
  }

  await callApi(`8. 歌曲详情 (ID:${sampleSongId})`, 'GET', `/song/getSongDetail/${sampleSongId}`)

  const allArtistRes = await callApi('9. 全部歌手列表', 'POST', '/artist/getAllArtists', { pageNum: 1, pageSize: 12 })
  if (allArtistRes?.data?.records?.length > 0) {
    sampleArtistId = allArtistRes.data.records[0].id || allArtistRes.data.records[0].artistId || sampleArtistId
  }

  await callApi(`10. 歌手详情 (ID:${sampleArtistId})`, 'GET', `/artist/getArtistDetail/${sampleArtistId}`)

  // 3. 需登录权限接口与真实收藏功能闭环测试
  console.log('\n--- [阶段 3: 收藏功能完整闭环测试 (CRUD)] ---')
  const authHeaders = userToken ? { Authorization: `Bearer ${userToken}` } : {}

  await callApi('11. 用户个人信息', 'GET', '/user/getUserInfo', null, authHeaders)
  
  // 收藏歌曲
  await callApi('12. 收藏歌曲(ID:11)', 'POST', `/favorite/collectSong?songId=11`, null, authHeaders)
  const favSongsRes = await callApi('13. 获取收藏歌曲列表', 'POST', '/favorite/getFavoriteSongs', { pageNum: 1, pageSize: 20 }, authHeaders)
  console.log(`   ↳ 收藏歌曲总数: ${favSongsRes?.data?.total}, 第一首: ${favSongsRes?.data?.items?.[0]?.songName || '无'}`)
  
  // 收藏歌单
  await callApi('14. 收藏歌单(ID:12)', 'POST', `/favorite/collectPlaylist?playlistId=12`, null, authHeaders)
  const favPlaylistsRes = await callApi('15. 获取收藏歌单列表', 'POST', '/favorite/getFavoritePlaylists', { pageNum: 1, pageSize: 20 }, authHeaders)
  console.log(`   ↳ 收藏歌单总数: ${favPlaylistsRes?.data?.total}, 第一部: ${favPlaylistsRes?.data?.items?.[0]?.title || '无'}`)

  // 取消收藏测试 (清理数据)
  await callApi('16. 取消收藏歌曲', 'DELETE', `/favorite/cancelCollectSong?songId=11`, null, authHeaders)
  await callApi('17. 取消收藏歌单', 'DELETE', `/favorite/cancelCollectPlaylist?playlistId=12`, null, authHeaders)

  console.log('\n====================================================')
  const passedCount = results.filter(r => r.pass).length
  console.log(`📊 汇总测试结果: 共测试 ${results.length} 个接口，通过: ${passedCount} 个，异常: ${results.length - passedCount} 个 (通过率: ${Math.round(passedCount / results.length * 100)}%)`)
  console.log('====================================================')
}

runTests()
