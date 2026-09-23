<script setup lang="ts">
import type { SongDetail } from '@/api/interface'
import { ref, inject, type Ref, computed, watch } from 'vue'
import { formatNumber, formatTime } from '@/utils'
import coverImg from '@/assets/cover.png'
import { likeComment, addSongComment, getSongDetail, deleteComment } from '@/api/system'
import { ElMessage } from 'element-plus'
import { UserStore } from '@/stores/modules/user'
import { settingStore } from '@/stores/modules/setting'
import { useAudioPlayer } from '@/hooks/useAudioPlayer'
import LyricView from './LyricView.vue'
import { Icon } from '@iconify/vue'

const songDetail = inject<Ref<SongDetail | null>>('songDetail')
const userStore = UserStore()
const setting = settingStore()
const { currentTrack, duration } = useAudioPlayer()

// Tab 切换：歌词 (lyrics) 与 评论 (comments) 与 歌曲档案 (info)
const activeTab = ref<'lyrics' | 'comments' | 'info'>('lyrics')

const emit = defineEmits<{
  (e: 'hide'): void
}>()

// 切换评论区显隐
const toggleCommentVisibility = () => {
  const nextVisible = !setting.isCommentVisible
  setting.setSettingState('isCommentVisible', nextVisible)
  if (!nextVisible && activeTab.value === 'comments') {
    activeTab.value = 'lyrics'
  }
}

// 监听评论显隐状态，评论隐藏时若当前处在评论 Tab 则立即切换至歌词 Tab
watch(
  () => setting.isCommentVisible,
  (visible) => {
    if (!visible && activeTab.value === 'comments') {
      activeTab.value = 'lyrics'
    }
  },
  { immediate: true }
)

// 获取当前用户名
const currentUsername = computed(() => userStore.userInfo?.username || '')

// 评论相关
const commentContent = ref('')
const maxLength = 180

// 对评论进行排序，最新的显示在前面
const comments = computed(() => {
  if (!songDetail.value?.comments) return []
  return [...songDetail.value.comments].sort((a, b) => b.commentId - a.commentId)
})

// 发布评论
const handleComment = async () => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }

  if (!commentContent.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }

  try {
    const songId = songDetail.value?.songId
    if (!songId) return

    const content = commentContent.value.trim()
    const res = await addSongComment({
      songId,
      content,
    })

    if (res.code === 0) {
      ElMessage.success('评论发布成功')
      commentContent.value = ''
      // 重新获取歌曲详情以更新评论列表
      const detailRes = await getSongDetail(songId)
      if (detailRes.code === 0 && detailRes.data) {
        songDetail.value = detailRes.data as unknown as SongDetail
      }
    } else {
      ElMessage.error('评论发布失败')
    }
  } catch (error) {
    ElMessage.error('评论发布失败')
  }
}

const formatDate = (date: string) => {
  if (!date) return '未知'
  return new Date(date).toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  })
}

// 处理点赞
const handleLike = async (comment: any) => {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }

  try {
    const res = await likeComment(comment.commentId)
    if (res.code === 0) {
      if (songDetail.value && songDetail.value.comments) {
        const updatedComments = songDetail.value.comments.map((item) => {
          if (item.commentId === comment.commentId) {
            return {
              ...item,
              likeCount: item.likeCount + 1,
            }
          }
          return item
        })

        songDetail.value = {
          ...songDetail.value,
          comments: updatedComments,
        }
      }
      ElMessage.success('点赞成功')
    }
  } catch (error) {
    ElMessage.error('点赞失败')
  }
}

// 删除评论
const handleDelete = async (comment: any) => {
  try {
    const res = await deleteComment(comment.commentId)
    if (res.code === 0) {
      ElMessage.success('删除成功')
      const songId = songDetail.value?.songId
      if (songId) {
        const detailRes = await getSongDetail(songId)
        if (detailRes.code === 0 && detailRes.data) {
          songDetail.value = detailRes.data as unknown as SongDetail
        }
      }
    } else {
      ElMessage.error('删除失败')
    }
  } catch (error) {
    ElMessage.error('删除失败')
  }
}
</script>

<template>
  <div class="h-full flex flex-col overflow-hidden bg-white/95 dark:bg-slate-900/95 backdrop-blur-md rounded-2xl border border-slate-200/80 dark:border-slate-800 shadow-sm">
    <!-- 顶部选项卡切换 -->
    <div class="flex items-center justify-between px-6 py-2.5 border-b border-slate-100 dark:border-slate-800 shrink-0">
      <div class="flex items-center gap-2">
        <div class="flex items-center gap-1 p-1 rounded-xl bg-slate-100 dark:bg-slate-800">
          <!-- 歌词 Tab -->
          <button
            class="clean-nav-tab"
            :class="{ 'clean-nav-tab-active': activeTab === 'lyrics' }"
            @click="activeTab = 'lyrics'"
          >
            <Icon icon="solar:music-note-linear" class="text-base" />
            <span>歌词</span>
          </button>

          <!-- 评论 Tab -->
          <button
            v-if="setting.isCommentVisible"
            class="clean-nav-tab"
            :class="{ 'clean-nav-tab-active': activeTab === 'comments' }"
            @click="activeTab = 'comments'"
          >
            <Icon icon="solar:chat-round-linear" class="text-base" />
            <span>评论</span>
            <span
              v-if="songDetail?.comments?.length"
              class="text-[11px] px-1.5 py-0.2 rounded-full bg-slate-200 dark:bg-slate-700 font-mono"
            >
              {{ songDetail.comments.length }}
            </span>
          </button>

          <!-- 详情 Tab -->
          <button
            class="clean-nav-tab"
            :class="{ 'clean-nav-tab-active': activeTab === 'info' }"
            @click="activeTab = 'info'"
          >
            <Icon icon="solar:info-circle-linear" class="text-base" />
            <span>详情</span>
          </button>
        </div>

        <!-- 评论显隐切换按钮 -->
        <el-tooltip
          :content="setting.isCommentVisible ? '隐藏评论区' : '显示评论区'"
          placement="top"
          effect="dark"
        >
          <button
            class="p-2 rounded-xl bg-slate-100 hover:bg-slate-200 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-500 hover:text-slate-800 dark:text-slate-400 dark:hover:text-slate-200 transition-colors border border-transparent hover:border-slate-200/80 dark:hover:border-slate-700 cursor-pointer flex items-center justify-center"
            :title="setting.isCommentVisible ? '隐藏评论区' : '显示评论区'"
            @click="toggleCommentVisibility"
          >
            <Icon
              :icon="setting.isCommentVisible ? 'solar:eye-closed-linear' : 'solar:chat-round-linear'"
              class="text-base"
            />
          </button>
        </el-tooltip>
      </div>

      <div class="flex items-center gap-2.5">
        <!-- 歌曲专辑信息简标 -->
        <div class="hidden md:flex items-center gap-1.5 text-xs text-slate-400 dark:text-slate-500 truncate max-w-[160px]">
          <Icon icon="solar:album-linear" class="text-sm shrink-0" />
          <span class="truncate">{{ songDetail?.album || currentTrack?.album || '单曲专辑' }}</span>
        </div>

        <!-- 隐藏面板按钮 -->
        <el-tooltip content="隐藏右侧面板 (唱片铺满全屏)" placement="top" effect="dark">
          <button
            class="flex items-center gap-1 px-2.5 py-1.5 rounded-xl bg-slate-100 hover:bg-slate-200 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-600 dark:text-slate-300 hover:text-slate-900 dark:hover:text-white transition-all border border-slate-200/80 dark:border-slate-700 text-xs font-medium cursor-pointer"
            @click="emit('hide')"
            title="隐藏右侧面板"
          >
            <Icon icon="solar:sidebar-minimalistic-linear" class="text-sm" />
            <span class="hidden sm:inline">隐藏</span>
            <Icon icon="solar:alt-arrow-right-linear" class="text-xs" />
          </button>
        </el-tooltip>
      </div>
    </div>

    <!-- 主体区域切换 -->
    <div class="flex-1 min-h-0 relative overflow-hidden">
      <!-- Tab 1: 歌词视口 -->
      <div v-show="activeTab === 'lyrics'" class="h-full">
        <LyricView
          :raw-lyric="songDetail?.lyric"
          :song-title="songDetail?.songName || currentTrack.title"
          :artist-name="songDetail?.artistName || currentTrack.artist"
          :album-name="songDetail?.album || currentTrack.album"
          :cover-url="songDetail?.coverUrl || currentTrack.cover"
        />
      </div>

      <!-- Tab 2: 评论视口 -->
      <div v-if="setting.isCommentVisible" v-show="activeTab === 'comments'" class="h-full p-6 overflow-y-auto space-y-6">
        <div class="space-y-6">
          <!-- 评论输入卡片 -->
          <div class="p-4 rounded-xl bg-slate-50 dark:bg-slate-800/50 border border-slate-200/80 dark:border-slate-800 space-y-3">
            <el-input
              v-model="commentContent"
              type="textarea"
              :rows="3"
              :maxlength="maxLength"
              placeholder="分享你此刻听歌的心情与故事…"
              resize="none"
              show-word-limit
            />
            <div class="flex items-center justify-between pt-1">
              <span class="text-xs text-slate-400">畅所欲言，文明互动</span>
              <button
                @click="handleComment"
                :disabled="!commentContent.trim()"
                class="px-5 py-1.5 bg-primary hover:bg-blue-600 active:scale-95 text-white rounded-lg text-xs font-medium disabled:opacity-40 disabled:cursor-not-allowed transition-all shadow-sm flex items-center gap-1.5 cursor-pointer"
              >
                <Icon icon="solar:plain-3-linear" class="text-sm" />
                <span>发布评论</span>
              </button>
            </div>
          </div>

          <!-- 全部评论列表 -->
          <div class="space-y-4">
            <div class="flex items-center justify-between px-1">
              <h3 class="text-sm font-semibold text-slate-800 dark:text-slate-200 flex items-center gap-2">
                <span>全部评论</span>
                <span class="text-xs font-normal text-slate-400">({{ formatNumber(songDetail?.comments?.length || 0) }})</span>
              </h3>
            </div>

            <!-- 评论列表项 -->
            <div v-if="comments.length > 0" class="space-y-3">
              <template v-for="comment in comments" :key="comment.commentId">
                <div class="flex gap-3.5 p-3.5 rounded-xl hover:bg-slate-50 dark:hover:bg-slate-800/60 border border-transparent hover:border-slate-200/60 dark:hover:border-slate-800 transition-all group">
                  <div class="w-9 h-9 rounded-full overflow-hidden flex-shrink-0 border border-slate-200 dark:border-slate-700">
                    <img :src="comment.userAvatar || coverImg" alt="avatar" class="w-full h-full object-cover" />
                  </div>
                  <div class="flex-1 min-w-0">
                    <div class="flex items-center justify-between">
                      <span class="text-xs font-semibold text-slate-800 dark:text-slate-200">{{ comment.username }}</span>
                      <span class="text-[11px] text-slate-400 font-mono">{{ comment.createTime }}</span>
                    </div>
                    <p class="text-xs text-slate-600 dark:text-slate-300 mt-1 mb-2 leading-relaxed break-words">{{ comment.content }}</p>
                    <div class="flex items-center justify-end gap-3 text-xs text-slate-400">
                      <!-- 当前用户本人的评论显示删除按钮 -->
                      <button
                        v-if="comment.username === currentUsername"
                        class="flex items-center gap-1 hover:text-red-500 opacity-0 group-hover:opacity-100 transition-opacity cursor-pointer"
                        @click="handleDelete(comment)"
                      >
                        <Icon icon="solar:trash-bin-trash-linear" class="text-sm" />
                        <span>删除</span>
                      </button>
                      <button
                        class="flex items-center gap-1 hover:text-primary transition-colors cursor-pointer"
                        @click="handleLike(comment)"
                      >
                        <Icon icon="solar:like-linear" class="text-sm" />
                        <span>{{ formatNumber(comment.likeCount) }}</span>
                      </button>
                    </div>
                  </div>
                </div>
              </template>
            </div>
            <div v-else class="text-center py-12 text-slate-400 text-xs space-y-2">
              <Icon icon="solar:chat-round-linear" class="text-3xl mx-auto text-slate-300 dark:text-slate-600" />
              <p>暂无评论，留下第一条足迹吧~</p>
            </div>
          </div>
        </div>
      </div>

      <!-- Tab 3: 歌曲详情 -->
      <div v-show="activeTab === 'info'" class="h-full p-6 overflow-y-auto space-y-6">
        <div class="space-y-4">
          <h3 class="text-sm font-semibold text-slate-800 dark:text-slate-200">歌曲详细信息</h3>
          
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div class="p-4 rounded-xl bg-slate-50 dark:bg-slate-800/50 border border-slate-200/80 dark:border-slate-800 space-y-1">
              <span class="text-xs text-slate-400">歌曲名</span>
              <p class="text-sm font-medium text-slate-800 dark:text-slate-200 truncate">{{ songDetail?.songName || currentTrack.title }}</p>
            </div>

            <div class="p-4 rounded-xl bg-slate-50 dark:bg-slate-800/50 border border-slate-200/80 dark:border-slate-800 space-y-1">
              <span class="text-xs text-slate-400">歌手</span>
              <p class="text-sm font-medium text-slate-800 dark:text-slate-200 truncate">{{ songDetail?.artistName || currentTrack.artist }}</p>
            </div>

            <div class="p-4 rounded-xl bg-slate-50 dark:bg-slate-800/50 border border-slate-200/80 dark:border-slate-800 space-y-1">
              <span class="text-xs text-slate-400">所属专辑</span>
              <p class="text-sm font-medium text-slate-800 dark:text-slate-200 truncate">{{ songDetail?.album || currentTrack.album || '单曲专辑' }}</p>
            </div>

            <div class="p-4 rounded-xl bg-slate-50 dark:bg-slate-800/50 border border-slate-200/80 dark:border-slate-800 space-y-1">
              <span class="text-xs text-slate-400">发行时间</span>
              <p class="text-sm font-medium text-slate-800 dark:text-slate-200">{{ formatDate(songDetail?.releaseTime || '') }}</p>
            </div>

            <div class="p-4 rounded-xl bg-slate-50 dark:bg-slate-800/50 border border-slate-200/80 dark:border-slate-800 space-y-1">
              <span class="text-xs text-slate-400">歌曲时长</span>
              <p class="text-sm font-medium text-slate-800 dark:text-slate-200 font-mono">{{ formatTime(duration || currentTrack.duration) }}</p>
            </div>

            <div class="p-4 rounded-xl bg-slate-50 dark:bg-slate-800/50 border border-slate-200/80 dark:border-slate-800 space-y-1">
              <span class="text-xs text-slate-400">音质</span>
              <p class="text-sm font-medium text-primary font-medium">标准 / 无损高清音频</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.clean-nav-tab {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  color: #64748b;
  background: transparent;
  border: none;
  cursor: pointer;
  transition: all 0.2s ease;
}

:global(.dark) .clean-nav-tab {
  color: #94a3b8;
}

.clean-nav-tab:hover {
  color: #0f172a;
}

:global(.dark) .clean-nav-tab:hover {
  color: #ffffff;
}

.clean-nav-tab-active {
  background: #ffffff !important;
  color: #2a68fa !important;
  font-weight: 600;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
}

:global(.dark) .clean-nav-tab-active {
  background: #1e293b !important;
  color: #60a5fa !important;
}
</style>
