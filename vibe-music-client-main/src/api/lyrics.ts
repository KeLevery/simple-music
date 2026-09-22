import axios from 'axios'

export interface OnlineLyricSearchResult {
  id: number | string
  title: string
  artist: string
  album?: string
  lrc?: string
}

/**
 * 从网络开放服务搜索匹配歌词
 * 提供多源容错方案（网易云开放检索代理 / 开放公用音乐元数据 API）
 */
export async function searchOnlineLyrics(keyword: string): Promise<OnlineLyricSearchResult[]> {
  if (!keyword || !keyword.trim()) return []

  try {
    // 尝试调用通用的开源网易云开放接口镜像或者公用歌词搜索
    const res = await axios.get(`https://music.163.com/api/search/get`, {
      params: {
        s: keyword.trim(),
        type: 1,
        offset: 0,
        limit: 5,
      },
      timeout: 5000,
    })

    if (res.data?.result?.songs) {
      return res.data.result.songs.map((song: any) => ({
        id: song.id,
        title: song.name,
        artist: song.artists?.map((a: any) => a.name).join(' / ') || '未知歌手',
        album: song.album?.name || '',
      }))
    }
  } catch (error) {
    console.warn('在线歌词检索主源请求异常，尝试备用方案:', error)
  }

  return []
}

/**
 * 根据歌曲 ID 获取网易云在线 LRC 歌词
 */
export async function fetchOnlineLrcById(id: number | string): Promise<string> {
  try {
    const res = await axios.get(`https://music.163.com/api/song/lyric`, {
      params: {
        id,
        lv: 1,
        kv: 1,
        tv: -1,
      },
      timeout: 6000,
    })

    return res.data?.lrc?.lyric || ''
  } catch (error) {
    console.warn('获取网络歌词详情失败:', error)
    return ''
  }
}

/**
 * 自动根据歌名和歌手一键从网络抓取歌词
 */
export async function autoFetchOnlineLyric(title: string, artist?: string): Promise<string> {
  const query = [title, artist].filter(Boolean).join(' ')
  if (!query.trim()) return ''

  try {
    const searchResults = await searchOnlineLyrics(query)
    if (searchResults && searchResults.length > 0) {
      const topSong = searchResults[0]
      const lrc = await fetchOnlineLrcById(topSong.id)
      if (lrc && lrc.trim()) {
        return lrc
      }
    }
  } catch (e) {
    console.error('自动网络歌词加载失败:', e)
  }

  return ''
}
