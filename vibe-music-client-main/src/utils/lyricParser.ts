/**
 * 借鉴 SeraphAudioPlayer 的毫秒精度与时间分组理念的高性能 LRC 歌词解析器
 */

export interface ParsedLyricLine {
  id: number
  time: number // 秒 (浮点数)
  timeMs: number // 毫秒 (整数)
  text: string
  isCredit?: boolean // 制作信息/非演唱词
}

export interface LyricMetadata {
  title?: string
  artist?: string
  album?: string
  by?: string
  offset?: number // 毫秒偏移
  [key: string]: string | number | undefined
}

export interface ParsedLyricResult {
  lines: ParsedLyricLine[]
  metadata: LyricMetadata
  hasLyrics: boolean
}

// 时间标签正则匹配 [mm:ss.xx] 或 [mm:ss.xxx] 或 [mm:ss]
const TIME_TAG_REG = /\[(\d{1,2}):(\d{2})(?:\.(\d{1,3}))?\]/g
// 元数据正则匹配 [tag:value]
const META_TAG_REG = /^\[(ti|ar|al|by|offset|length):([^\]]*)\]/i

/**
 * 解析时间标签为毫秒整数
 */
function parseTimeTagToMs(minuteStr: string, secondStr: string, msStr?: string): number {
  const minutes = parseInt(minuteStr, 10) || 0
  const seconds = parseInt(secondStr, 10) || 0
  let milliseconds = 0

  if (msStr) {
    if (msStr.length === 1) milliseconds = parseInt(msStr, 10) * 100
    else if (msStr.length === 2) milliseconds = parseInt(msStr, 10) * 10
    else milliseconds = parseInt(msStr.slice(0, 3), 10)
  }

  return minutes * 60 * 1000 + seconds * 1000 + milliseconds
}

/**
 * 判断是否属于歌曲制作信息行（如：词/曲/编曲/录音/混音/制作人）
 */
function isCreditLine(text: string): boolean {
  const creditKeywords = ['作词', '作曲', '编曲', '制作人', '录音', '混音', '吉他', '贝斯', '鼓', '和声', '母带', '监制', '企划', '出品']
  return creditKeywords.some((kw) => text.startsWith(kw) || text.startsWith(`${kw}：`) || text.startsWith(`${kw}:`))
}

/**
 * 解析完整 LRC 字符串
 * @param lrcContent LRC 格式的歌词内容
 * @returns 解析后的歌词行列表与元数据
 */
export function parseLrc(lrcContent?: string | null): ParsedLyricResult {
  if (!lrcContent || typeof lrcContent !== 'string' || !lrcContent.trim()) {
    return { lines: [], metadata: {}, hasLyrics: false }
  }

  const rawLines = lrcContent.split(/\r?\n/)
  const metadata: LyricMetadata = {}
  const rawParsedLines: Array<{ timeMs: number; text: string; isCredit: boolean }> = []

  let offsetMs = 0

  for (const rawLine of rawLines) {
    const trimmedLine = rawLine.trim()
    if (!trimmedLine) continue

    // 1. 优先尝试解析元数据标签
    const metaMatch = trimmedLine.match(META_TAG_REG)
    if (metaMatch) {
      const key = metaMatch[1].toLowerCase()
      const val = metaMatch[2].trim()
      if (key === 'offset') {
        offsetMs = parseInt(val, 10) || 0
        metadata.offset = offsetMs
      } else {
        metadata[key] = val
      }
      continue
    }

    // 2. 匹配行内所有时间戳
    TIME_TAG_REG.lastIndex = 0
    const timeMatches: number[] = []
    let match: RegExpExecArray | null

    while ((match = TIME_TAG_REG.exec(trimmedLine)) !== null) {
      const timeMs = parseTimeTagToMs(match[1], match[2], match[3])
      timeMatches.push(timeMs)
    }

    // 如果包含有效时间标签
    if (timeMatches.length > 0) {
      // 剥离时间标签后剩下的纯文本内容
      const lyricText = trimmedLine.replace(TIME_TAG_REG, '').trim()
      const credit = isCreditLine(lyricText)

      for (const tMs of timeMatches) {
        rawParsedLines.push({
          timeMs: Math.max(0, tMs + offsetMs),
          text: lyricText,
          isCredit: credit,
        })
      }
    } else {
      // 无时间戳的行（通常在歌词最前段），如果是制作信息也保留
      if (isCreditLine(trimmedLine)) {
        rawParsedLines.push({
          timeMs: 0,
          text: trimmedLine,
          isCredit: true,
        })
      }
    }
  }

  // 3. 按时间升序排序
  rawParsedLines.sort((a, b) => a.timeMs - b.timeMs)

  // 4. 组装行数据，剔除开头末尾连续空行
  const lines: ParsedLyricLine[] = rawParsedLines.map((item, index) => ({
    id: index + 1,
    time: Number((item.timeMs / 1000).toFixed(3)),
    timeMs: item.timeMs,
    text: item.text,
    isCredit: item.isCredit,
  }))

  return {
    lines,
    metadata,
    hasLyrics: lines.length > 0,
  }
}

/**
 * 根据当前播放时间（毫秒），通过二分查找快速定位当前活动的歌词行索引
 * @param lines 解析好的歌词行列表
 * @param currentTimeMs 当前播放时间（毫秒）
 * @returns 当前处于活动状态的歌词行索引（未匹配到则返回 -1）
 */
export function findActiveLyricIndex(lines: ParsedLyricLine[], currentTimeMs: number): number {
  if (!lines || lines.length === 0) return -1

  // 如果当前时间早于第一句，返回第 0 句或 -1
  if (currentTimeMs < lines[0].timeMs) {
    return 0
  }

  // 二分查找最近的歌词行
  let low = 0
  let high = lines.length - 1
  let result = 0

  while (low <= high) {
    const mid = (low + high) >> 1
    if (lines[mid].timeMs <= currentTimeMs) {
      result = mid
      low = mid + 1 // 往右找更接近的
    } else {
      high = mid - 1
    }
  }

  return result
}

/**
 * 格式化毫秒为标准时间字符串 mm:ss
 */
export function formatMsToTime(ms: number): string {
  const totalSeconds = Math.max(0, Math.floor(ms / 1000))
  const minutes = Math.floor(totalSeconds / 60)
  const seconds = totalSeconds % 60
  return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
}
