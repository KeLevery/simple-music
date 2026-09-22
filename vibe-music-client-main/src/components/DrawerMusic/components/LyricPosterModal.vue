<script setup lang="ts">
import { ref, computed } from 'vue'
import { Icon } from '@iconify/vue'
import { ElMessage } from 'element-plus'
import type { ParsedLyricLine } from '@/utils/lyricParser'
import defaultCover from '@/assets/cover.png'

interface Props {
  modelValue: boolean
  lines: ParsedLyricLine[]
  songTitle?: string
  artistName?: string
  albumName?: string
  coverUrl?: string
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: false,
  lines: () => [],
  songTitle: '未知歌曲',
  artistName: '未知歌手',
  albumName: '单曲专辑',
  coverUrl: '',
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

// 选中的歌词行文本
const selectedLineIndices = ref<number[]>([])

// 预设主题风格
const themes = [
  {
    id: 'aurora',
    name: '极光蓝',
    bgClass: 'bg-gradient-to-br from-blue-900 via-indigo-950 to-slate-950 text-white border-blue-500/30',
    accentText: 'text-blue-400',
    tagBg: 'bg-blue-500/20 text-blue-300',
  },
  {
    id: 'midnight',
    name: '暗夜黑',
    bgClass: 'bg-gradient-to-br from-slate-900 via-black to-zinc-900 text-slate-100 border-zinc-700/40',
    accentText: 'text-amber-400',
    tagBg: 'bg-amber-500/20 text-amber-300',
  },
  {
    id: 'sunset',
    name: '暮光紫',
    bgClass: 'bg-gradient-to-br from-purple-900 via-rose-950 to-slate-950 text-white border-rose-500/30',
    accentText: 'text-rose-400',
    tagBg: 'bg-rose-500/20 text-rose-300',
  },
  {
    id: 'clean',
    name: '雅白质感',
    bgClass: 'bg-gradient-to-br from-slate-50 via-white to-slate-100 text-slate-900 border-slate-200 shadow-xl',
    accentText: 'text-blue-600',
    tagBg: 'bg-blue-100 text-blue-700',
  },
]

const currentThemeId = ref('aurora')
const currentTheme = computed(() => themes.find((t) => t.id === currentThemeId.value) || themes[0])

// 选中的歌词内容列表
const selectedLyricsText = computed(() => {
  if (!selectedLineIndices.value.length) {
    // 默认选取前 2~3 句有效歌词
    const validLines = props.lines.filter((l) => !l.isCredit && l.text.trim())
    return validLines.slice(0, 3).map((l) => l.text)
  }
  return selectedLineIndices.value
    .sort((a, b) => a - b)
    .map((idx) => props.lines[idx]?.text)
    .filter(Boolean)
})

// 切换选择单行歌词
const toggleLine = (index: number) => {
  const pos = selectedLineIndices.value.indexOf(index)
  if (pos > -1) {
    selectedLineIndices.value.splice(pos, 1)
  } else {
    if (selectedLineIndices.value.length >= 6) {
      ElMessage.warning('最多选取 6 句歌词')
      return
    }
    selectedLineIndices.value.push(index)
  }
}

// 复制歌词卡片文案
const copyCardText = () => {
  const quote = selectedLyricsText.value.join('\n')
  const content = `「${quote}」\n\n—— 《${props.songTitle}》· ${props.artistName}\n来自 Vibe Music 音乐播放器`
  navigator.clipboard.writeText(content).then(() => {
    ElMessage.success('歌词金句文案已复制到剪贴板')
  })
}

// 关闭弹窗
const closeModal = () => {
  emit('update:modelValue', false)
}
</script>

<template>
  <el-dialog
    :model-value="modelValue"
    @update:model-value="emit('update:modelValue', $event)"
    title="生成歌词分享海报"
    width="680px"
    destroy-on-close
    append-to-body
    class="rounded-2xl overflow-hidden"
  >
    <div class="grid grid-cols-1 md:grid-cols-12 gap-6 p-1">
      <!-- 左侧：选择歌词与主题控制 -->
      <div class="md:col-span-6 flex flex-col space-y-4">
        <div>
          <label class="block text-xs font-semibold text-slate-700 dark:text-slate-300 mb-2">
            1. 选择主题风格
          </label>
          <div class="grid grid-cols-2 gap-2">
            <button
              v-for="theme in themes"
              :key="theme.id"
              class="px-3 py-2 rounded-xl text-xs font-medium border text-left transition-all flex items-center justify-between"
              :class="
                currentThemeId === theme.id
                  ? 'border-primary ring-2 ring-primary/20 bg-primary/5 text-primary font-semibold'
                  : 'border-slate-200 dark:border-slate-700 text-slate-600 dark:text-slate-400 hover:border-slate-300'
              "
              @click="currentThemeId = theme.id"
            >
              <span>{{ theme.name }}</span>
              <Icon
                v-if="currentThemeId === theme.id"
                icon="solar:check-circle-bold"
                class="text-primary text-sm"
              />
            </button>
          </div>
        </div>

        <div class="flex-1 min-h-0 flex flex-col">
          <div class="flex items-center justify-between mb-2">
            <label class="text-xs font-semibold text-slate-700 dark:text-slate-300">
              2. 点击挑选歌词 (最多6句)
            </label>
            <span class="text-[11px] text-slate-400">已选 {{ selectedLyricsText.length }} 句</span>
          </div>

          <div class="flex-1 max-h-56 overflow-y-auto rounded-xl border border-slate-200 dark:border-slate-800 p-2 space-y-1 divide-y divide-slate-100 dark:divide-slate-800/60 bg-slate-50/50 dark:bg-slate-900/50">
            <template v-for="(line, idx) in lines" :key="line.id">
              <div
                v-if="!line.isCredit && line.text.trim()"
                class="p-2 rounded-lg text-xs cursor-pointer transition-colors flex items-center justify-between gap-2"
                :class="
                  selectedLineIndices.includes(idx)
                    ? 'bg-primary/10 text-primary font-medium'
                    : 'text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800'
                "
                @click="toggleLine(idx)"
              >
                <span class="truncate">{{ line.text }}</span>
                <Icon
                  :icon="selectedLineIndices.includes(idx) ? 'solar:checkbox-square-bold' : 'solar:square-linear'"
                  class="text-sm shrink-0"
                />
              </div>
            </template>
          </div>
        </div>
      </div>

      <!-- 右侧：海报实时渲染卡片预览 -->
      <div class="md:col-span-6 flex flex-col items-center justify-center">
        <label class="w-full text-xs font-semibold text-slate-700 dark:text-slate-300 mb-2">
          海报效果预览
        </label>

        <!-- 海报卡片容器 -->
        <div
          class="w-full rounded-2xl p-5 border flex flex-col justify-between shadow-xl transition-all duration-300 relative overflow-hidden"
          :class="currentTheme.bgClass"
          style="min-height: 280px;"
        >
          <!-- 背景装饰光晕 -->
          <div class="absolute -right-8 -top-8 w-28 h-28 rounded-full bg-white/10 blur-xl pointer-events-none"></div>

          <!-- 卡片顶部：歌曲信息 -->
          <div class="flex items-center gap-3 relative z-10">
            <div class="w-10 h-10 rounded-lg overflow-hidden shrink-0 shadow-md border border-white/20">
              <img :src="coverUrl || defaultCover" :alt="songTitle" class="w-full h-full object-cover" />
            </div>
            <div class="min-w-0">
              <h4 class="text-xs font-bold truncate leading-tight">{{ songTitle }}</h4>
              <p class="text-[11px] opacity-75 truncate mt-0.5">{{ artistName }} · {{ albumName }}</p>
            </div>
          </div>

          <!-- 卡片中部：歌词金句内容 -->
          <div class="my-4 relative z-10 space-y-1.5 py-1">
            <div :class="currentTheme.accentText" class="text-lg font-serif leading-none">“</div>
            <div
              v-for="(text, index) in selectedLyricsText"
              :key="index"
              class="text-xs sm:text-sm font-medium leading-relaxed tracking-wide"
            >
              {{ text }}
            </div>
            <div :class="currentTheme.accentText" class="text-lg font-serif leading-none text-right">”</div>
          </div>

          <!-- 卡片底部：品牌签名 -->
          <div class="flex items-center justify-between pt-3 border-t border-current/10 text-[10px] opacity-70 relative z-10">
            <span class="flex items-center gap-1 font-semibold tracking-wider uppercase">
              <Icon icon="solar:music-library-2-bold" class="text-xs" />
              <span>Vibe Music</span>
            </span>
            <span class="font-mono">NOW PLAYING</span>
          </div>
        </div>

        <!-- 底部快捷操作 -->
        <div class="flex items-center gap-2.5 w-full mt-4">
          <button
            class="flex-1 py-2 px-3 rounded-xl bg-primary hover:bg-blue-600 text-white text-xs font-medium transition-all shadow-sm flex items-center justify-center gap-1.5 cursor-pointer"
            @click="copyCardText"
          >
            <Icon icon="solar:copy-linear" class="text-sm" />
            <span>复制金句文案</span>
          </button>
          <button
            class="py-2 px-3 rounded-xl bg-slate-100 hover:bg-slate-200 dark:bg-slate-800 dark:hover:bg-slate-700 text-slate-700 dark:text-slate-300 text-xs font-medium transition-colors border border-slate-200 dark:border-slate-700 cursor-pointer"
            @click="closeModal"
          >
            关闭
          </button>
        </div>
      </div>
    </div>
  </el-dialog>
</template>
