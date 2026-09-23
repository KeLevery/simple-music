<script setup lang="ts">
import Left from './left.vue'
import Right from './right.vue'
import KeyboardShortcutsModal from './components/KeyboardShortcutsModal.vue'
import { useDark, useToggle, useDateFormat, useNow, useLocalStorage } from '@vueuse/core'
import { getSongDetail, collectSong, cancelCollectSong } from '@/api/system'
import type { SongDetail } from '@/api/interface'
import { ref, provide, watch, onMounted, onUnmounted } from 'vue'
import { useAudioPlayer } from '@/hooks/useAudioPlayer'
import { themeStore } from '@/stores/modules/theme'
import { AudioStore } from '@/stores/modules/audio'
import { UserStore } from '@/stores/modules/user'
import { Icon } from '@iconify/vue'
import { ElMessage } from 'element-plus'

const formatted = useDateFormat(useNow(), 'HH:mm:ss')
const theme = themeStore()
const audioStore = AudioStore()
const userStore = UserStore()
const showDrawer = defineModel<boolean>()
const songDetail = ref<SongDetail | null>(null)

// 控制右侧面板显隐（持久化存储）
const isRightPanelVisible = useLocalStorage('music-drawer-right-panel-visible', true)

const toggleRightPanel = () => {
  isRightPanelVisible.value = !isRightPanelVisible.value
}

// 快捷键帮助弹窗
const showShortcutsModal = ref(false)

// 全屏状态
const isFullscreen = ref(false)

const isDark = useDark({
  selector: 'html',
  attribute: 'class',
  valueDark: 'dark',
  valueLight: 'light',
})
const toggleDark = useToggle(isDark)
const toggleMode = () => {
  theme.setDark(!isDark.value)
  toggleDark()
}

const {
  currentTrack,
  isPlaying,
  currentTime,
  duration,
  volume,
  togglePlayPause,
  seek,
  setVolume,
  nextTrack,
  prevTrack,
} = useAudioPlayer()

// 监听 currentTrack 的变化，获取歌曲详情
watch(
  () => currentTrack.value.id,
  async (newId) => {
    if (newId) {
      try {
        const res = await getSongDetail(Number(newId))
        if (res.code === 0 && res.data) {
          const songData = res.data as unknown as SongDetail
          if (
            'songId' in songData &&
            'songName' in songData &&
            'artistName' in songData &&
            'album' in songData
          ) {
            songDetail.value = songData
          }
        }
      } catch (error) {
        console.error('获取歌曲详情失败:', error)
      }
    }
  },
  { immediate: true }
)

// 全屏切换
const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen().then(() => {
      isFullscreen.value = true
    }).catch(() => {})
  } else {
    if (document.exitFullscreen) {
      document.exitFullscreen().then(() => {
        isFullscreen.value = false
      }).catch(() => {})
    }
  }
}

// 快捷喜欢/取消喜欢
const handleQuickLike = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  const track = audioStore.trackList[audioStore.currentSongIndex]
  if (!track) return
  const songId = Number(track.id)
  const currentStatus = track.likeStatus || 0
  try {
    if (currentStatus === 0) {
      const res = await collectSong(songId)
      if (res.code === 0) {
        track.likeStatus = 1
        ElMessage.success('已添加到我的喜欢')
      }
    } else {
      const res = await cancelCollectSong(songId)
      if (res.code === 0) {
        track.likeStatus = 0
        ElMessage.success('已取消喜欢')
      }
    }
  } catch (e) {}
}

// 全局播放器快捷键
const onKeydown = (e: KeyboardEvent) => {
  if (!showDrawer.value) return

  // 如果在输入框或文本域中输入，不触发播放器全局快捷键
  const target = e.target as HTMLElement
  if (target && (target.tagName === 'INPUT' || target.tagName === 'TEXTAREA' || target.isContentEditable)) {
    return
  }

  switch (e.code) {
    case 'Space':
      e.preventDefault()
      togglePlayPause()
      break
    case 'Escape':
      if (document.fullscreenElement) {
        document.exitFullscreen().catch(() => {})
        isFullscreen.value = false
      } else {
        showDrawer.value = false
      }
      break
    case 'ArrowLeft':
      e.preventDefault()
      seek(Math.max(0, currentTime.value - 5))
      break
    case 'ArrowRight':
      e.preventDefault()
      seek(Math.min(duration.value, currentTime.value + 5))
      break
    case 'ArrowUp':
      e.preventDefault()
      setVolume(Math.min(100, (volume.value || 0) + 5))
      break
    case 'ArrowDown':
      e.preventDefault()
      setVolume(Math.max(0, (volume.value || 0) - 5))
      break
    case 'KeyM':
      e.preventDefault()
      setVolume(volume.value === 0 ? 50 : 0)
      break
    case 'KeyF':
      e.preventDefault()
      toggleFullscreen()
      break
    case 'KeyL':
      e.preventDefault()
      handleQuickLike()
      break
    case 'KeyN':
      e.preventDefault()
      nextTrack()
      break
    case 'KeyP':
      e.preventDefault()
      prevTrack()
      break
    case 'KeyH':
      e.preventDefault()
      toggleRightPanel()
      break
  }
}

const onFullscreenChange = () => {
  isFullscreen.value = !!document.fullscreenElement
}

onMounted(() => {
  window.addEventListener('keydown', onKeydown)
  document.addEventListener('fullscreenchange', onFullscreenChange)
})

onUnmounted(() => {
  window.removeEventListener('keydown', onKeydown)
  document.removeEventListener('fullscreenchange', onFullscreenChange)
})

// 提供 songDetail 给子组件
provide('songDetail', songDetail)
</script>

<template>
  <el-drawer
    v-model="showDrawer"
    direction="btt"
    size="100%"
    :modal="false"
    :show-close="false"
    :with-header="false"
    class="drawer-clean-theme-wrapper"
  >
    <!-- 主体视听容器 -->
    <div class="w-full h-full flex flex-col overflow-hidden select-none relative bg-slate-50 dark:bg-slate-950 text-slate-900 dark:text-slate-100">
      <!-- 动态自适应环境柔光背景层 (Ambient Blur Background) -->
      <div
        class="absolute inset-0 pointer-events-none opacity-25 dark:opacity-15 filter blur-[90px] scale-125 transition-all duration-1000 bg-center bg-no-repeat bg-cover"
        :style="{
          backgroundImage: `url(${songDetail?.coverUrl || currentTrack.cover})`
        }"
      ></div>

      <!-- 顶栏导航 -->
      <header class="flex items-center justify-between px-6 lg:px-10 py-3 shrink-0 border-b border-slate-200/70 dark:border-slate-800/80 bg-white/80 dark:bg-slate-900/80 backdrop-blur-md z-20">
        <!-- 左侧：收起按钮与正在播放简标 -->
        <div class="flex items-center gap-3">
          <button
            class="flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-slate-100 hover:bg-slate-200 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-700 dark:text-slate-200 text-xs font-medium transition-colors border border-slate-200 dark:border-slate-700 cursor-pointer"
            @click="showDrawer = false"
            title="收起播放页面 (ESC)"
          >
            <Icon icon="solar:arrow-down-linear" class="text-sm" />
            <span>收起</span>
            <kbd class="hidden sm:inline-block text-[10px] font-mono px-1 py-0.2 rounded bg-slate-200 dark:bg-slate-700 text-slate-500 dark:text-slate-400">ESC</kbd>
          </button>

          <div class="hidden sm:flex items-center gap-2 text-xs text-slate-400 dark:text-slate-500 border-l border-slate-200 dark:border-slate-800 pl-3">
            <span class="font-medium text-slate-700 dark:text-slate-300">正在播放</span>
            <span>·</span>
            <span class="truncate max-w-[240px] text-slate-500 dark:text-slate-400">{{ songDetail?.songName || currentTrack.title }}</span>
          </div>
        </div>

        <!-- 右侧：快捷键指南、右栏显隐、沉浸全屏、时钟、主题切换与关闭 -->
        <div class="flex items-center gap-2.5">
          <!-- 快捷键指南 -->
          <button
            @click="showShortcutsModal = true"
            class="p-2 rounded-lg bg-slate-100 hover:bg-slate-200 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-600 dark:text-slate-300 transition-colors border border-slate-200 dark:border-slate-700 cursor-pointer"
            title="快捷键指南"
          >
            <Icon icon="solar:keyboard-linear" class="text-base" />
          </button>

          <!-- 侧边栏/右侧面板显隐切换 -->
          <button
            @click="toggleRightPanel"
            class="p-2 rounded-lg bg-slate-100 hover:bg-slate-200 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-600 dark:text-slate-300 transition-colors border border-slate-200 dark:border-slate-700 cursor-pointer"
            :class="{ '!bg-blue-50 dark:!bg-blue-950/40 text-primary border-primary/30': !isRightPanelVisible }"
            :title="isRightPanelVisible ? '隐藏右侧面板 / 铺满全屏 (H)' : '展开右侧面板 (H)'"
          >
            <Icon :icon="isRightPanelVisible ? 'solar:sidebar-minimalistic-linear' : 'solar:sidebar-minimalistic-bold'" class="text-base" />
          </button>

          <!-- 全屏沉浸切换 -->
          <button
            @click="toggleFullscreen"
            class="p-2 rounded-lg bg-slate-100 hover:bg-slate-200 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-600 dark:text-slate-300 transition-colors border border-slate-200 dark:border-slate-700 cursor-pointer"
            :title="isFullscreen ? '退出全屏 (F)' : '全屏沉浸 (F)'"
          >
            <Icon :icon="isFullscreen ? 'solar:quit-full-screen-linear' : 'solar:full-screen-linear'" class="text-base" />
          </button>

          <!-- 实时时钟 -->
          <div class="hidden md:flex items-center gap-1.5 text-xs font-mono text-slate-500 dark:text-slate-400 bg-slate-100 dark:bg-slate-800 px-3 py-1 rounded-lg border border-slate-200 dark:border-slate-700">
            <Icon icon="solar:clock-circle-linear" class="text-sm text-primary" />
            <span>{{ formatted }}</span>
          </div>

          <!-- 主题切换 -->
          <button
            @click="toggleMode"
            class="p-2 rounded-lg bg-slate-100 hover:bg-slate-200 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-600 dark:text-slate-300 transition-colors border border-slate-200 dark:border-slate-700 cursor-pointer"
            :title="isDark ? '切换至浅色模式' : '切换至暗黑模式'"
          >
            <Icon :icon="isDark ? 'solar:moon-linear' : 'solar:sun-2-linear'" class="text-base" />
          </button>

          <!-- 关闭 -->
          <button
            @click="showDrawer = false"
            class="p-2 rounded-lg bg-slate-100 hover:bg-red-50 hover:text-red-600 dark:bg-slate-800 dark:hover:bg-red-950/40 dark:hover:text-red-400 text-slate-600 dark:text-slate-300 transition-colors border border-slate-200 dark:border-slate-700 cursor-pointer"
            title="关闭"
          >
            <Icon icon="solar:close-circle-linear" class="text-base" />
          </button>
        </div>
      </header>

      <!-- 主体双栏布局 -->
      <main class="flex-1 min-h-0 w-full flex flex-col lg:flex-row p-4 lg:p-6 gap-6 overflow-hidden relative z-10">
        <!-- 左侧：黑胶/画报与控制区 -->
        <section
          class="h-full flex flex-col justify-center items-center overflow-y-auto lg:overflow-visible transition-all duration-500 ease-in-out"
          :class="isRightPanelVisible ? 'w-full lg:w-[46%]' : 'w-full flex-1 max-w-4xl mx-auto'"
        >
          <Left :is-expanded="!isRightPanelVisible" />
        </section>

        <!-- 右侧：歌词与评论卡片 -->
        <Transition name="panel-slide">
          <section
            v-if="isRightPanelVisible"
            class="w-full lg:w-[54%] h-full flex flex-col min-h-0 transition-all duration-500 ease-in-out"
          >
            <Right @hide="isRightPanelVisible = false" />
          </section>
        </Transition>
      </main>

      <!-- 右侧面板展开浮动按钮 (当右侧面板隐藏时浮现在右边缘) -->
      <Transition name="fade-slide">
        <button
          v-if="!isRightPanelVisible"
          class="fixed right-0 top-1/2 -translate-y-1/2 z-30 flex flex-col items-center gap-2 py-4 px-2 rounded-l-2xl bg-white/90 dark:bg-slate-900/90 hover:bg-white dark:hover:bg-slate-800 text-slate-600 dark:text-slate-300 hover:text-primary shadow-xl border border-r-0 border-slate-200/80 dark:border-slate-800 backdrop-blur-md transition-all duration-300 hover:pl-3 group cursor-pointer"
          @click="isRightPanelVisible = true"
          title="展开歌词与评论面板 (H)"
        >
          <Icon icon="solar:alt-arrow-left-linear" class="text-base group-hover:-translate-x-0.5 transition-transform" />
          <div class="flex flex-col items-center gap-0.5 text-[11px] font-medium [writing-mode:vertical-lr] tracking-widest text-slate-500 dark:text-slate-400 group-hover:text-primary">
            <span>歌词与评论</span>
          </div>
        </button>
      </Transition>
    </div>

    <!-- 快捷键指南弹窗 -->
    <KeyboardShortcutsModal v-model="showShortcutsModal" />
  </el-drawer>
</template>

<style>
.drawer-clean-theme-wrapper.el-drawer {
  background: #f8fafc !important;
  box-shadow: none !important;
}

.dark .drawer-clean-theme-wrapper.el-drawer {
  background: #020617 !important;
}

.drawer-clean-theme-wrapper .el-drawer__body {
  padding: 0 !important;
  overflow: hidden !important;
}

.panel-slide-enter-active,
.panel-slide-leave-active {
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.panel-slide-enter-from,
.panel-slide-leave-to {
  opacity: 0;
  transform: translateX(60px);
  width: 0 !important;
  margin: 0 !important;
  padding: 0 !important;
  overflow: hidden;
}

.fade-slide-enter-active,
.fade-slide-leave-active {
  transition: all 0.3s ease;
}

.fade-slide-enter-from,
.fade-slide-leave-to {
  opacity: 0;
  transform: translate(100%, -50%);
}
</style>
