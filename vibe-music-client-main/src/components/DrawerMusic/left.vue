<script setup lang="ts">
import { formatTime } from '@/utils'
import { Icon } from '@iconify/vue'
import type { SongDetail } from '@/api/interface'
import { ref, inject, computed, type Ref, watch } from 'vue'
import { useAudioPlayer } from '@/hooks/useAudioPlayer'
import Recently from '../../layout/components/footer/components/recently.vue'
import VinylPlayer from './components/VinylPlayer.vue'
import AlbumArtwork from './components/AlbumArtwork.vue'
import { UserStore } from '@/stores/modules/user'
import { AudioStore } from '@/stores/modules/audio'
import { collectSong, cancelCollectSong } from '@/api/system'
import { ElMessage } from 'element-plus'

interface Props {
  isExpanded?: boolean
}

withDefaults(defineProps<Props>(), {
  isExpanded: false,
})

const {
  currentTrack,
  isPlaying,
  currentTime,
  duration,
  volume,
  isLoading,
  isBuffering,
  bufferedPercent,
  networkError,
  audioElement,
  reloadAudio,
  nextTrack,
  prevTrack,
  togglePlayPause,
  seek,
  setVolume,
  setPlayMode,
} = useAudioPlayer()

const songDetail = inject<Ref<SongDetail | null>>('songDetail')
const userStore = UserStore()
const audioStore = AudioStore()

// 视觉呈现模式：'vinyl' (黑胶唱机) 或 'artwork' (大画报)
const visualMode = ref<'vinyl' | 'artwork'>('vinyl')

// 倍速播放
const playbackRates = [0.5, 0.75, 1.0, 1.25, 1.5, 2.0]
const currentPlaybackRate = ref(1.0)

const setPlaybackRate = (rate: number) => {
  currentPlaybackRate.value = rate
  if (audioElement.value) {
    audioElement.value.playbackRate = rate
  }
}

// 监听歌曲切换重置或同步倍速
watch(
  () => currentTrack.value.id,
  () => {
    if (audioElement.value && currentPlaybackRate.value !== 1.0) {
      audioElement.value.playbackRate = currentPlaybackRate.value
    }
  }
)

// 音量控制与静音
const previousVolume = ref(50)
const isMuted = computed(() => volume.value === 0)

const toggleMute = () => {
  if (isMuted.value) {
    setVolume(previousVolume.value || 50)
  } else {
    previousVolume.value = volume.value || 50
    setVolume(0)
  }
}

// 播放模式
const playModes = {
  order: {
    icon: 'solar:repeat-linear',
    next: 'shuffle',
    tooltip: '顺序播放',
  },
  shuffle: {
    icon: 'solar:shuffle-linear',
    next: 'loop',
    tooltip: '随机播放',
  },
  loop: {
    icon: 'solar:restart-linear',
    next: 'single',
    tooltip: '列表循环',
  },
  single: {
    icon: 'solar:repeat-one-linear',
    next: 'order',
    tooltip: '单曲循环',
  },
}

const currentMode = ref<'order' | 'shuffle' | 'loop' | 'single'>('order')

const togglePlayMode = () => {
  const nextMode = playModes[currentMode.value].next as 'order' | 'shuffle' | 'loop' | 'single'
  currentMode.value = nextMode
  setPlayMode(nextMode)
}

// 喜欢状态
const currentSongLikeStatus = computed(() => {
  const track = audioStore.trackList[audioStore.currentSongIndex]
  return track?.likeStatus || 0
})

const updateAllSongLikeStatus = (songId: number, status: number) => {
  audioStore.trackList.forEach((track) => {
    if (Number(track.id) === songId) {
      track.likeStatus = status
    }
  })
  if (audioStore.currentPageSongs) {
    audioStore.currentPageSongs.forEach((song) => {
      if (song.songId === songId) {
        song.likeStatus = status
      }
    })
  }
}

const handleLike = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }

  const track = audioStore.trackList[audioStore.currentSongIndex]
  if (!track) return

  try {
    const songId = Number(track.id)
    if (currentSongLikeStatus.value === 0) {
      const res = await collectSong(songId)
      if (res.code === 0) {
        updateAllSongLikeStatus(songId, 1)
        ElMessage.success('已添加到我的喜欢')
      } else {
        ElMessage.error(res.message || '添加到我的喜欢失败')
      }
    } else {
      const res = await cancelCollectSong(songId)
      if (res.code === 0) {
        updateAllSongLikeStatus(songId, 0)
        ElMessage.success('已取消喜欢')
      } else {
        ElMessage.error(res.message || '取消喜欢失败')
      }
    }
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}
</script>

<template>
  <div
    class="w-full flex flex-col items-center justify-center select-none py-1 transition-all duration-500 ease-in-out"
    :class="isExpanded ? 'max-w-lg lg:max-w-xl' : 'max-w-md'"
  >
    <!-- 视觉呈现模式切换控制条 -->
    <div class="flex items-center gap-1.5 mb-2 bg-slate-100 dark:bg-slate-800/80 p-1 rounded-xl border border-slate-200/80 dark:border-slate-800">
      <button
        class="px-3 py-1 rounded-lg text-xs font-medium transition-all flex items-center gap-1.5 cursor-pointer"
        :class="
          visualMode === 'vinyl'
            ? 'bg-white dark:bg-slate-700 text-primary font-semibold shadow-sm'
            : 'text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white'
        "
        @click="visualMode = 'vinyl'"
        title="黑胶唱机模式"
      >
        <Icon icon="solar:vinyl-record-linear" class="text-sm" />
        <span>黑胶唱机</span>
      </button>

      <button
        class="px-3 py-1 rounded-lg text-xs font-medium transition-all flex items-center gap-1.5 cursor-pointer"
        :class="
          visualMode === 'artwork'
            ? 'bg-white dark:bg-slate-700 text-primary font-semibold shadow-sm'
            : 'text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-white'
        "
        @click="visualMode = 'artwork'"
        title="专辑画报模式"
      >
        <Icon icon="solar:gallery-wide-linear" class="text-sm" />
        <span>专辑画报</span>
      </button>
    </div>

    <!-- 核心视觉组件呈现 (黑胶唱盘 or 封面画报) -->
    <div
      class="w-full flex items-center justify-center transition-all duration-500 ease-out"
      :class="isExpanded ? 'min-h-[350px] sm:min-h-[390px] lg:min-h-[430px]' : 'min-h-[300px] sm:min-h-[340px]'"
    >
      <Transition name="fade-scale" mode="out-in">
        <VinylPlayer
          v-if="visualMode === 'vinyl'"
          :key="'vinyl-' + (currentTrack.id || '0')"
          :cover-url="songDetail?.coverUrl || currentTrack.cover"
          :song-title="songDetail?.songName || currentTrack.title"
          :is-playing="isPlaying"
          :is-expanded="isExpanded"
        />
        <AlbumArtwork
          v-else
          :key="'artwork-' + (currentTrack.id || '0')"
          :cover-url="songDetail?.coverUrl || currentTrack.cover"
          :song-title="songDetail?.songName || currentTrack.title"
          :is-playing="isPlaying"
          :is-expanded="isExpanded"
        />
      </Transition>
    </div>

    <!-- 歌曲标题与歌手 -->
    <div class="flex flex-col items-center text-center mt-3 w-full px-4 transition-all duration-300">
      <h1
        class="font-bold text-slate-900 dark:text-white truncate max-w-full tracking-tight transition-all duration-300"
        :class="isExpanded ? 'text-2xl sm:text-3xl lg:text-4xl' : 'text-2xl lg:text-3xl'"
      >
        {{ songDetail?.songName || currentTrack.title }}
      </h1>
      <p
        class="text-slate-500 dark:text-slate-400 mt-1 truncate max-w-full font-medium transition-all duration-300"
        :class="isExpanded ? 'text-base sm:text-lg' : 'text-sm lg:text-base'"
      >
        {{ songDetail?.artistName || currentTrack.artist }}
        <span v-if="songDetail?.album || currentTrack.album" class="text-slate-300 dark:text-slate-600 mx-1.5">·</span>
        <span v-if="songDetail?.album || currentTrack.album">{{ songDetail?.album || currentTrack.album }}</span>
      </p>
    </div>

    <!-- 异常提示 -->
    <div v-if="networkError" class="mt-2 px-3 py-1 rounded-lg bg-red-50 dark:bg-red-950/40 text-red-600 dark:text-red-400 text-xs flex items-center gap-1.5 border border-red-200 dark:border-red-800">
      <Icon icon="solar:danger-triangle-linear" class="text-sm" />
      <span>{{ networkError }}</span>
      <button class="underline ml-1 font-semibold cursor-pointer" @click="reloadAudio">重试</button>
    </div>

    <!-- 进度条与时间 -->
    <div class="w-full mt-5 space-y-1">
      <div class="relative w-full flex items-center">
        <span class="text-xs font-mono text-slate-400 dark:text-slate-500 w-11 text-right pr-2">
          {{ formatTime(currentTime) }}
        </span>

        <div class="relative flex-1 mx-2 flex items-center">
          <!-- 缓冲进度条底轨 -->
          <div class="absolute left-0 right-0 h-1 rounded-full bg-slate-200 dark:bg-slate-800 overflow-hidden pointer-events-none">
            <div
              class="h-full bg-slate-300 dark:bg-slate-700 rounded-full transition-all duration-300"
              :style="{ width: `${bufferedPercent}%` }"
            ></div>
          </div>

          <el-slider
            v-model="currentTime"
            :show-tooltip="false"
            @change="seek"
            :max="duration"
            class="w-full relative z-10 clean-player-slider"
            size="small"
          />
        </div>

        <span class="text-xs font-mono text-slate-400 dark:text-slate-500 w-11 text-left pl-2">
          {{ formatTime(duration) }}
        </span>
      </div>
    </div>

    <!-- 底部主要控制按钮栏 -->
    <div class="flex items-center justify-center gap-4 sm:gap-6 w-full mt-3">
      <!-- 播放模式 -->
      <el-tooltip :content="playModes[currentMode].tooltip" placement="top" effect="dark">
        <button
          class="p-2.5 rounded-full text-slate-500 dark:text-slate-400 hover:text-slate-800 dark:hover:text-white hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors cursor-pointer"
          @click="togglePlayMode"
        >
          <Icon :icon="playModes[currentMode].icon" class="text-xl" />
        </button>
      </el-tooltip>

      <!-- 上一首 -->
      <button
        class="p-2.5 rounded-full text-slate-700 dark:text-slate-300 hover:text-primary hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors cursor-pointer"
        @click="prevTrack"
        title="上一首 (P)"
      >
        <Icon icon="solar:skip-previous-bold" class="text-2xl" />
      </button>

      <!-- 主播放/暂停按钮 -->
      <button
        class="w-14 h-14 rounded-full bg-primary hover:bg-blue-600 active:scale-95 text-white flex items-center justify-center shadow-lg shadow-blue-500/30 transition-all cursor-pointer"
        @click="togglePlayPause"
        title="播放/暂停 (Space)"
      >
        <Icon
          v-if="isBuffering || isLoading"
          icon="solar:refresh-circle-linear"
          class="animate-spin text-2xl"
        />
        <Icon
          v-else
          :icon="isPlaying ? 'solar:pause-bold' : 'solar:play-bold'"
          class="text-2xl"
          :class="!isPlaying ? 'ml-0.5' : ''"
        />
      </button>

      <!-- 下一首 -->
      <button
        class="p-2.5 rounded-full text-slate-700 dark:text-slate-300 hover:text-primary hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors cursor-pointer"
        @click="nextTrack"
        title="下一首 (N)"
      >
        <Icon icon="solar:skip-next-bold" class="text-2xl" />
      </button>

      <!-- 喜欢 -->
      <button
        class="p-2.5 rounded-full text-slate-500 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors cursor-pointer"
        @click="handleLike"
        title="喜欢 (L)"
      >
        <Icon
          v-if="currentSongLikeStatus === 0"
          icon="solar:heart-linear"
          class="text-xl hover:text-red-500 transition-colors"
        />
        <Icon
          v-else
          icon="solar:heart-bold"
          class="text-xl text-red-500 animate-pulse"
        />
      </button>

      <!-- 播放列表 -->
      <div class="scale-105">
        <Recently />
      </div>
    </div>

    <!-- 进阶控制栏 (倍速选择器 + 音量滑块) -->
    <div class="flex items-center justify-between w-full mt-4 px-2 py-2 rounded-xl bg-slate-100/70 dark:bg-slate-800/40 border border-slate-200/60 dark:border-slate-800/60">
      <!-- 倍速控制 -->
      <el-dropdown trigger="click" @command="setPlaybackRate">
        <button class="flex items-center gap-1 px-2.5 py-1 rounded-lg text-xs font-medium text-slate-600 dark:text-slate-300 hover:bg-white dark:hover:bg-slate-700 transition-colors cursor-pointer border border-transparent hover:border-slate-200 dark:hover:border-slate-600">
          <Icon icon="solar:speedometer-middle-linear" class="text-sm text-primary" />
          <span class="font-mono">{{ currentPlaybackRate }}x 倍速</span>
          <Icon icon="solar:alt-arrow-down-linear" class="text-xs opacity-60" />
        </button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item
              v-for="rate in playbackRates"
              :key="rate"
              :command="rate"
              :class="{ 'text-primary font-bold': currentPlaybackRate === rate }"
            >
              {{ rate }}x {{ rate === 1.0 ? '(正常)' : '' }}
            </el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>

      <!-- 音量控制 -->
      <div class="flex items-center gap-2">
        <button
          class="p-1 rounded-lg text-slate-500 hover:text-slate-800 dark:text-slate-400 dark:hover:text-white transition-colors cursor-pointer"
          @click="toggleMute"
          :title="isMuted ? '取消静音 (M)' : '静音 (M)'"
        >
          <Icon :icon="isMuted ? 'solar:muted-linear' : 'solar:volume-loud-linear'" class="text-base" />
        </button>
        <el-slider
          v-model="volume"
          :show-tooltip="false"
          @input="setVolume"
          :max="100"
          class="!w-20 clean-volume-slider"
          size="small"
        />
        <span class="text-[11px] font-mono text-slate-400 w-6 text-right">{{ volume }}%</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
:deep(.clean-player-slider) {
  --el-slider-main-bg-color: #2a68fa;
  --el-slider-runway-bg-color: transparent;
  --el-slider-button-size: 13px;
}

:deep(.clean-volume-slider) {
  --el-slider-main-bg-color: #2a68fa;
  --el-slider-runway-bg-color: rgba(148, 163, 184, 0.3);
  --el-slider-button-size: 10px;
}

.fade-scale-enter-active,
.fade-scale-leave-active {
  transition: all 0.35s ease;
}

.fade-scale-enter-from {
  opacity: 0;
  transform: scale(0.92);
}

.fade-scale-leave-to {
  opacity: 0;
  transform: scale(1.04);
}
</style>
