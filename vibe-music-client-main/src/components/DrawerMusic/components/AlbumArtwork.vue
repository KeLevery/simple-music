<script setup lang="ts">
import { computed } from 'vue'
import defaultCover from '@/assets/cover.png'

interface Props {
  coverUrl?: string
  songTitle?: string
  isPlaying: boolean
  isExpanded?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  coverUrl: '',
  songTitle: '',
  isPlaying: false,
  isExpanded: false,
})

const displayCover = computed(() => props.coverUrl || defaultCover)
</script>

<template>
  <div
    class="relative w-64 h-64 sm:w-72 sm:h-72 lg:w-80 lg:h-80 select-none flex items-center justify-center transition-all duration-500 ease-out origin-center"
    :class="isExpanded ? 'scale-115 sm:scale-120 lg:scale-125 my-4 sm:my-6' : 'my-2'"
  >
    <!-- 环境扩散柔光 (Ambient Cover Glow) -->
    <div
      class="absolute -inset-2 rounded-3xl filter blur-2xl opacity-60 dark:opacity-40 transition-all duration-700 pointer-events-none scale-100"
      :style="{
        backgroundImage: `radial-gradient(circle, rgba(59, 130, 246, 0.4) 0%, rgba(99, 102, 241, 0.2) 60%, transparent 80%)`
      }"
      :class="isPlaying ? 'scale-105' : 'scale-95'"
    ></div>

    <!-- 现代大图画报卡片 -->
    <div
      class="relative w-full h-full rounded-2xl overflow-hidden shadow-2xl border border-slate-200/80 dark:border-slate-800 bg-white dark:bg-slate-900 group transition-transform duration-500 ease-out hover:scale-[1.02]"
    >
      <img
        :src="displayCover"
        :alt="songTitle"
        class="w-full h-full object-cover select-none transition-transform duration-700 ease-out"
        :class="isPlaying ? 'scale-[1.02]' : 'scale-100'"
      />

      <!-- 玻璃反光层 (Glass Shine) -->
      <div class="absolute inset-0 bg-gradient-to-tr from-black/20 via-transparent to-white/10 pointer-events-none"></div>
    </div>
  </div>
</template>
