<script setup lang="ts">
import { ref, watch, onUnmounted, nextTick } from 'vue'
import { parseLrc, findActiveLyricIndex, type ParsedLyricLine } from '@/utils/lyricParser'
import { autoFetchOnlineLyric, searchOnlineLyrics, fetchOnlineLrcById, type OnlineLyricSearchResult } from '@/api/lyrics'
import { useAudioPlayer } from '@/hooks/useAudioPlayer'
import { ElMessage } from 'element-plus'
import { Icon } from '@iconify/vue'
import LyricPosterModal from './components/LyricPosterModal.vue'

interface Props {
  rawLyric?: string | null
  songTitle?: string
  artistName?: string
  albumName?: string
  coverUrl?: string
}

const props = withDefaults(defineProps<Props>(), {
  rawLyric: '',
  songTitle: '',
  artistName: '',
  albumName: '',
  coverUrl: '',
})

const { currentTime, seek } = useAudioPlayer()

// 歌词状态
const currentLrcText = ref('')
const parsedLines = ref<ParsedLyricLine[]>([])
const activeIndex = ref(-1)
const isLargeFont = ref(false)
const lyricSourceType = ref<'local' | 'network' | 'custom'>('local')

// 海报生成弹窗
const showPosterModal = ref(false)

// 网络加载状态
const isFetchingLyric = ref(false)
const searchModalVisible = ref(false)
const searchKeyword = ref('')
const isSearching = ref(false)
const searchResults = ref<OnlineLyricSearchResult[]>([])

// 滚动控制
const scrollContainerRef = ref<HTMLDivElement | null>(null)
const lineRefs = ref<Array<HTMLButtonElement | null>>([])
const isUserScrolling = ref(false)
let scrollResumeTimer: any = null

// 解析歌词
const updateLyrics = (lrcString: string, source: 'local' | 'network' | 'custom' = 'local') => {
  currentLrcText.value = lrcString
  lyricSourceType.value = source
  const result = parseLrc(lrcString)
  parsedLines.value = result.lines
  lineRefs.value = []
}

// 监听原歌词变化
watch(
  () => [props.rawLyric, props.songTitle, props.artistName],
  async ([newLyric, title, artist]) => {
    if (newLyric && typeof newLyric === 'string' && newLyric.trim()) {
      updateLyrics(newLyric, 'local')
    } else if (title) {
      // 本地无歌词，触发网络在线匹配
      await fetchLyricFromNetwork(title, artist)
    } else {
      updateLyrics('', 'local')
    }
  },
  { immediate: true }
)

// 从网络尝试拉取歌词
async function fetchLyricFromNetwork(title: string, artist?: string) {
  isFetchingLyric.value = true
  try {
    const netLrc = await autoFetchOnlineLyric(title, artist)
    if (netLrc && netLrc.trim()) {
      updateLyrics(netLrc, 'network')
      ElMessage.success({
        message: '已从网络匹配并同步歌词',
        duration: 2000,
      })
    } else {
      updateLyrics('', 'local')
    }
  } catch (e) {
    updateLyrics('', 'local')
  } finally {
    isFetchingLyric.value = false
  }
}

// 监听当前音频播放时间，毫秒级快速查找当前活动句
watch(currentTime, (newTime) => {
  if (!parsedLines.value.length) {
    activeIndex.value = -1
    return
  }

  const currentMs = Math.floor(newTime * 1000)
  const index = findActiveLyricIndex(parsedLines.value, currentMs)

  if (index !== activeIndex.value) {
    activeIndex.value = index
    if (!isUserScrolling.value) {
      scrollToActiveLine(index)
    }
  }
})

// 视口平滑居中滚动算法
const scrollToActiveLine = (index: number) => {
  if (index < 0 || !scrollContainerRef.value) return
  nextTick(() => {
    const container = scrollContainerRef.value
    const targetLine = lineRefs.value[index]
    if (!container || !targetLine) return

    const containerHeight = container.clientHeight
    const lineTop = targetLine.offsetTop
    const lineHeight = targetLine.clientHeight

    // 目标偏移位置：将目标句精准定位在视口中心偏上位置 (38% 视口高)
    const targetScrollTop = Math.max(0, lineTop - containerHeight * 0.38 + lineHeight / 2)

    container.scrollTo({
      top: targetScrollTop,
      behavior: 'smooth',
    })
  })
}

// 用户手动滚动时，暂时暂停自动跟随 3.5 秒
const handleUserScroll = () => {
  isUserScrolling.value = true
  if (scrollResumeTimer) clearTimeout(scrollResumeTimer)
  scrollResumeTimer = setTimeout(() => {
    isUserScrolling.value = false
    scrollToActiveLine(activeIndex.value)
  }, 3500)
}

// 点击歌词行跳转播放
const handleSeekToLine = (line: ParsedLyricLine) => {
  isUserScrolling.value = false
  if (scrollResumeTimer) clearTimeout(scrollResumeTimer)
  seek(line.time)
}

// 复制歌词文本
const copyLyrics = () => {
  if (!currentLrcText.value) return
  navigator.clipboard.writeText(currentLrcText.value).then(() => {
    ElMessage.success('歌词已复制到剪贴板')
  })
}

// 本地 LRC 文件导入
const triggerFileInput = () => {
  const fileInput = document.createElement('input')
  fileInput.type = 'file'
  fileInput.accept = '.lrc,.txt'
  fileInput.onchange = (e: any) => {
    const file = e.target.files?.[0]
    if (file) {
      const reader = new FileReader()
      reader.onload = (event) => {
        const text = event.target?.result as string
        if (text) {
          updateLyrics(text, 'custom')
          ElMessage.success('本地歌词已成功导入')
        }
      }
      reader.readAsText(file, 'utf-8')
    }
  }
  fileInput.click()
}

// 打开在线匹配弹窗
const openSearchModal = () => {
  searchKeyword.value = [props.songTitle, props.artistName].filter(Boolean).join(' ')
  searchResults.value = []
  searchModalVisible.value = true
  if (searchKeyword.value) {
    handleSearchLyrics()
  }
}

// 在线搜索歌词
const handleSearchLyrics = async () => {
  if (!searchKeyword.value.trim()) return
  isSearching.value = true
  try {
    const list = await searchOnlineLyrics(searchKeyword.value)
    searchResults.value = list
    if (!list.length) {
      ElMessage.info('未搜到匹配的在线歌词')
    }
  } finally {
    isSearching.value = false
  }
}

// 选用搜索出的某条歌词
const applySearchedLyric = async (item: OnlineLyricSearchResult) => {
  try {
    const lrc = await fetchOnlineLrcById(item.id)
    if (lrc && lrc.trim()) {
      updateLyrics(lrc, 'network')
      searchModalVisible.value = false
      ElMessage.success(`已应用《${item.title}》的歌词`)
    } else {
      ElMessage.warning('该歌曲暂无有效歌词内容')
    }
  } catch (e) {
    ElMessage.error('加载歌词失败')
  }
}

onUnmounted(() => {
  if (scrollResumeTimer) clearTimeout(scrollResumeTimer)
})
</script>

<template>
  <div class="h-full flex flex-col relative overflow-hidden select-none">
    <!-- 顶部轻量工具条 -->
    <div class="flex items-center justify-between px-6 py-2.5 shrink-0 border-b border-slate-100 dark:border-slate-800/80">
      <div class="flex items-center gap-2">
        <span class="text-xs text-slate-400 dark:text-slate-500 font-medium">
          实时歌词
        </span>
        <span
          v-if="lyricSourceType === 'network'"
          class="px-2 py-0.5 text-[10px] rounded-full bg-blue-50 text-blue-600 dark:bg-blue-950/50 dark:text-blue-400 font-medium"
        >
          网络匹配
        </span>
        <span
          v-else-if="lyricSourceType === 'custom'"
          class="px-2 py-0.5 text-[10px] rounded-full bg-amber-50 text-amber-600 dark:bg-amber-950/50 dark:text-amber-400 font-medium"
        >
          本地导入
        </span>
      </div>

      <div class="flex items-center gap-1.5">
        <!-- 切换字号 -->
        <button
          class="clean-tool-btn"
          :class="{ 'clean-tool-btn-active': isLargeFont }"
          @click="isLargeFont = !isLargeFont"
          :title="isLargeFont ? '恢复标准字号' : '放大歌词字号'"
        >
          <Icon icon="solar:text-square-linear" class="text-sm" />
          <span>{{ isLargeFont ? '标准' : '大字' }}</span>
        </button>

        <!-- 生成歌词海报 -->
        <button
          v-if="parsedLines.length > 0"
          class="clean-tool-btn hover:text-primary"
          @click="showPosterModal = true"
          title="生成歌词分享海报"
        >
          <Icon icon="solar:card-2-linear" class="text-sm text-primary" />
          <span>海报</span>
        </button>

        <!-- 网络在线匹配 -->
        <button
          class="clean-tool-btn"
          @click="openSearchModal"
          title="在线搜索匹配歌词"
        >
          <Icon icon="solar:magnifer-linear" class="text-sm" />
          <span>匹配</span>
        </button>

        <!-- 本地导入 -->
        <button
          class="clean-tool-btn"
          @click="triggerFileInput"
          title="从本地导入 .lrc 歌词文件"
        >
          <Icon icon="solar:upload-track-2-linear" class="text-sm" />
          <span>导入</span>
        </button>

        <!-- 复制歌词 -->
        <button
          v-if="parsedLines.length > 0"
          class="clean-tool-btn"
          @click="copyLyrics"
          title="复制全部歌词"
        >
          <Icon icon="solar:copy-linear" class="text-sm" />
        </button>
      </div>
    </div>

    <!-- 歌词展示主体区 -->
    <div class="flex-1 min-h-0 relative overflow-hidden">
      <!-- 歌词加载动画 -->
      <div v-if="isFetchingLyric" class="absolute inset-0 flex flex-col items-center justify-center gap-3 text-slate-400 dark:text-slate-500">
        <Icon icon="solar:refresh-circle-linear" class="animate-spin text-3xl text-primary" />
        <p class="text-xs">正在搜索匹配歌词…</p>
      </div>

      <!-- 歌词滚动流 -->
      <div
        v-else-if="parsedLines.length > 0"
        ref="scrollContainerRef"
        class="clean-lyrics-viewport h-full overflow-y-auto px-6 lg:px-10 py-8"
        @wheel="handleUserScroll"
        @touchstart="handleUserScroll"
      >
        <!-- 顶部占位垫高 -->
        <div class="h-24"></div>

        <div class="flex flex-col space-y-3.5">
          <template v-for="(line, index) in parsedLines" :key="line.id">
            <!-- 制作信息行 -->
            <div
              v-if="line.isCredit"
              :ref="(el) => (lineRefs[index] = el as any)"
              class="py-1 text-xs text-slate-400 dark:text-slate-600 transition-colors"
              :class="{ '!text-primary font-bold': index === activeIndex }"
            >
              {{ line.text }}
            </div>

            <!-- 主歌词行 -->
            <button
              v-else
              :ref="(el) => (lineRefs[index] = el as any)"
              class="lyric-row text-left transition-all duration-300 relative group cursor-pointer w-full py-2 outline-none border-0 bg-transparent"
              :class="[
                isLargeFont ? 'text-2xl lg:text-3xl' : 'text-lg lg:text-xl',
                index === activeIndex ? 'lyric-row-active' : 'lyric-row-inactive',
              ]"
              @click="handleSeekToLine(line)"
            >
              <!-- 悬浮播放图标 -->
              <span class="absolute -left-6 top-1/2 -translate-y-1/2 opacity-0 group-hover:opacity-100 text-primary text-xs transition-opacity flex items-center">
                <Icon icon="solar:play-bold" />
              </span>

              <!-- 活跃句指示条 -->
              <span
                v-if="index === activeIndex"
                class="absolute -left-3 top-2 bottom-2 w-1.5 rounded-full bg-primary shadow-[0_0_8px_#3b82f6]"
              ></span>

              <span class="block tracking-wide leading-relaxed font-sans transition-all">
                {{ line.text }}
              </span>
            </button>
          </template>
        </div>

        <!-- 底部占位垫高 -->
        <div class="h-44"></div>
      </div>

      <!-- 空状态（暂无歌词） -->
      <div v-else class="h-full flex flex-col items-center justify-center text-center p-8 gap-3">
        <div class="w-14 h-14 rounded-full bg-slate-100 dark:bg-slate-800 text-slate-400 dark:text-slate-500 flex items-center justify-center text-2xl">
          <Icon icon="solar:music-note-2-linear" />
        </div>
        <div class="space-y-1">
          <h4 class="text-sm font-semibold text-slate-700 dark:text-slate-300">暂无歌词</h4>
          <p class="text-xs text-slate-400 dark:text-slate-500">享受此刻纯粹的音乐旋律</p>
        </div>
        <div class="flex items-center gap-2.5 mt-2">
          <button
            class="px-3.5 py-1.5 rounded-lg bg-primary hover:bg-blue-600 text-white text-xs font-medium transition-colors flex items-center gap-1.5 cursor-pointer shadow-sm"
            @click="openSearchModal"
          >
            <Icon icon="solar:magnifer-linear" />
            <span>网络匹配歌词</span>
          </button>
          <button
            class="px-3.5 py-1.5 rounded-lg bg-slate-100 hover:bg-slate-200 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-700 dark:text-slate-300 text-xs font-medium transition-colors flex items-center gap-1.5 cursor-pointer border border-slate-200 dark:border-slate-700"
            @click="triggerFileInput"
          >
            <Icon icon="solar:upload-track-2-linear" />
            <span>导入本地LRC</span>
          </button>
        </div>
      </div>
    </div>

    <!-- 网络歌词匹配弹窗 -->
    <el-dialog
      v-model="searchModalVisible"
      title="匹配在线歌词"
      width="460px"
      append-to-body
      destroy-on-close
      class="rounded-2xl"
    >
      <div class="space-y-4">
        <div class="flex gap-2">
          <el-input
            v-model="searchKeyword"
            placeholder="输入歌曲名 / 歌手名搜索歌词"
            clearable
            @keyup.enter="handleSearchLyrics"
          >
            <template #prefix>
              <Icon icon="solar:magnifer-linear" class="text-slate-400" />
            </template>
          </el-input>
          <el-button type="primary" :loading="isSearching" @click="handleSearchLyrics">
            搜索
          </el-button>
        </div>

        <div v-loading="isSearching" class="max-h-64 overflow-y-auto divide-y divide-slate-100 dark:divide-slate-800 pr-1">
          <div
            v-for="item in searchResults"
            :key="item.id"
            class="p-2.5 hover:bg-slate-100 dark:hover:bg-slate-800 rounded-lg flex items-center justify-between cursor-pointer transition-colors"
            @click="applySearchedLyric(item)"
          >
            <div class="min-w-0 pr-3">
              <div class="font-medium text-sm text-slate-800 dark:text-slate-200 truncate">{{ item.title }}</div>
              <div class="text-xs text-slate-400 truncate mt-0.5">
                {{ item.artist }} · {{ item.album || '单曲' }}
              </div>
            </div>
            <el-button size="small" type="primary" plain>应用</el-button>
          </div>
          <div v-if="!isSearching && !searchResults.length" class="text-center py-6 text-xs text-slate-400">
            暂无搜索结果，请输入关键词重新搜索
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 歌词海报分享生成弹窗 -->
    <LyricPosterModal
      v-model="showPosterModal"
      :lines="parsedLines"
      :song-title="props.songTitle"
      :artist-name="props.artistName"
      :album-name="props.albumName"
      :cover-url="props.coverUrl"
    />
  </div>
</template>

<style scoped>
.clean-lyrics-viewport {
  scrollbar-width: none;
  -ms-overflow-style: none;
  mask-image: linear-gradient(
    to bottom,
    transparent 0%,
    rgba(0, 0, 0, 1) 8%,
    rgba(0, 0, 0, 1) 90%,
    transparent 100%
  );
  -webkit-mask-image: linear-gradient(
    to bottom,
    transparent 0%,
    rgba(0, 0, 0, 1) 8%,
    rgba(0, 0, 0, 1) 90%,
    transparent 100%
  );
}

.clean-lyrics-viewport::-webkit-scrollbar {
  display: none;
}

.clean-tool-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 500;
  color: #64748b;
  background: #f1f5f9;
  border: 1px solid #e2e8f0;
  transition: all 0.2s ease;
  cursor: pointer;
}

:global(.dark) .clean-tool-btn {
  color: #94a3b8;
  background: #1e293b;
  border-color: #334155;
}

.clean-tool-btn:hover {
  color: #0f172a;
  background: #e2e8f0;
}

:global(.dark) .clean-tool-btn:hover {
  color: #f8fafc;
  background: #334155;
}

.clean-tool-btn-active {
  background: #2a68fa !important;
  color: #ffffff !important;
  border-color: #2a68fa !important;
}

.lyric-row-inactive {
  color: #94a3b8;
  font-weight: 400;
  opacity: 0.65;
}

:global(.dark) .lyric-row-inactive {
  color: #64748b;
  opacity: 0.65;
}

.lyric-row-inactive:hover {
  color: #1e293b;
  opacity: 1;
}

:global(.dark) .lyric-row-inactive:hover {
  color: #f1f5f9;
  opacity: 1;
}

.lyric-row-active {
  color: #2a68fa !important;
  font-weight: 700;
  transform: scale(1.03);
  transform-origin: left center;
  opacity: 1 !important;
  text-shadow: 0 0 12px rgba(42, 104, 250, 0.25);
}

:global(.dark) .lyric-row-active {
  color: #60a5fa !important;
  text-shadow: 0 0 14px rgba(96, 165, 250, 0.35);
}
</style>
