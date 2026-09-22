<script setup lang="ts">
import { Icon } from '@iconify/vue'
import { formatTime } from '@/utils'
import { UserStore } from '@/stores/modules/user'
import { AudioStore } from '@/stores/modules/audio'
import { collectSong, cancelCollectSong } from '@/api/system'
import { ElMessage } from 'element-plus'
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useLibraryStore } from '@/stores/modules/library'
import { useArtistStore } from '@/stores/modules/artist'
import { usePlaylistStore } from '@/stores/modules/playlist'

const userStore = UserStore()
const audioStore = AudioStore()
const route = useRoute()
const libraryStore = useLibraryStore()

const {
  isPlaying,
  currentTime,
  duration,
  isLoading,
  isBuffering,
  bufferedPercent,
  networkError,
  nextTrack,
  prevTrack,
  togglePlayPause,
  seek,
} = useAudioPlayer()

// 获取当前播放歌曲的喜欢状态
const currentSongLikeStatus = computed(() => {
  const currentTrack = audioStore.trackList[audioStore.currentSongIndex]
  return currentTrack?.likeStatus || 0
})

// 更新所有相同歌曲的喜欢状态
const updateAllSongLikeStatus = (songId: number, status: number) => {
  // 更新播放列表中的状态
  audioStore.trackList.forEach(track => {
    if (Number(track.id) === songId) {
      track.likeStatus = status
    }
  })

  // 更新当前页面的歌曲列表状态
  if (audioStore.currentPageSongs) {
    audioStore.currentPageSongs.forEach(song => {
      if (song.songId === songId) {
        song.likeStatus = status
      }
    })
  }

  // 更新曲库页面的数据
  if (route.path === '/library' && libraryStore.tableData?.items) {
    const song = libraryStore.tableData.items.find(song => song.songId === songId)
    if (song) {
      song.likeStatus = status
    }
  }

  // 更新歌手详情页的数据
  if (route.path.startsWith('/artist/')) {
    const artistStore = useArtistStore()
    if (artistStore.artistInfo?.songs) {
      const song = artistStore.artistInfo.songs.find(song => song.songId === songId)
      if (song) {
        song.likeStatus = status
      }
    }
  }

  // 更新歌单详情页的数据
  if (route.path.startsWith('/playlist/')) {
    const playlistStore = usePlaylistStore()
    if (playlistStore.songs) {
      const song = playlistStore.songs.find(song => song.songId === songId)
      if (song) {
        song.likeStatus = status
      }
    }
  }
}

// 处理喜欢/取消喜欢
const handleLike = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }

  const currentTrack = audioStore.trackList[audioStore.currentSongIndex]
  if (!currentTrack) return

  try {
    const songId = Number(currentTrack.id)
    if (currentSongLikeStatus.value === 0) {
      // 收藏歌曲
      const res = await collectSong(songId)
      if (res.code === 0) {
        updateAllSongLikeStatus(songId, 1)
        ElMessage.success('已添加到我的喜欢')
      } else {
        ElMessage.error(res.message || '添加到我的喜欢失败')
      }
    } else {
      // 取消收藏
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
  <div class="flex items-center flex-1">
    <div class="flex items-center mr-2">
      <button
        @click="prevTrack"
        class="p-2 rounded-full hover:bg-hoverMenuBg transition"
        title="上一首"
      >
        <icon-solar:skip-previous-bold class="text-lg" />
      </button>

      <button
        @click="togglePlayPause"
        class="p-2 rounded-full hover:bg-hoverMenuBg transition relative"
        title="播放/暂停"
      >
        <Icon
          v-if="isBuffering"
          icon="ri:loader-2-line"
          class="text-4xl animate-spin"
          :color="'#2a68fa'"
        />
        <Icon
          v-else
          :icon="
            isPlaying ? 'ic:round-pause-circle' : 'material-symbols:play-circle'
          "
          class="text-5xl"
          :color="'#2a68fa'"
        />
      </button>

      <button
        @click="nextTrack"
        class="p-2 rounded-full hover:bg-hoverMenuBg transition"
        title="下一首"
      >
        <icon-solar:skip-previous-bold class="transform scale-x-[-1] text-lg" />
      </button>

      <button class="p-2 rounded-full hover:bg-hoverMenuBg transition" @click="handleLike">
        <icon-mdi:cards-heart-outline v-if="currentSongLikeStatus === 0" class="text-lg" />
        <icon-mdi:cards-heart v-else class="text-lg text-red-500" />
      </button>
    </div>

    <!-- 进度条区（支持双轨网络已缓冲背景条） -->
    <div class="w-full flex items-center space-x-2">
      <div class="relative w-full flex items-center">
        <!-- 灰色已缓冲底轨 -->
        <div class="absolute left-0 right-0 h-1 rounded-full bg-black/10 dark:bg-white/10 overflow-hidden pointer-events-none">
          <div
            class="h-full bg-black/20 dark:bg-white/25 rounded-full transition-all duration-300"
            :style="{ width: `${bufferedPercent}%` }"
          ></div>
        </div>

        <el-slider
          v-model="currentTime"
          :step="1"
          :show-tooltip="false"
          @change="seek"
          :max="duration"
          class="w-full relative z-10"
          size="small"
        />
      </div>

      <div class="text-xs text-muted-foreground whitespace-nowrap flex items-center font-mono gap-1">
        <span>{{ formatTime(currentTime) }}</span>
        <span>/</span>
        <span>{{ formatTime(duration) }}</span>
      </div>
    </div>
  </div>
</template>
