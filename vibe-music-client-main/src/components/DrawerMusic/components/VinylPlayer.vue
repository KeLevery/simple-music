<script setup lang="ts">
import { computed } from 'vue'
import defaultCover from '@/assets/cover.png'

interface Props {
  coverUrl?: string
  songTitle?: string
  isPlaying: boolean
}

const props = withDefaults(defineProps<Props>(), {
  coverUrl: '',
  songTitle: '',
  isPlaying: false,
})

const displayCover = computed(() => props.coverUrl || defaultCover)
</script>

<template>
  <div class="relative w-full max-w-[340px] aspect-square flex items-center justify-center select-none my-2">
    <!-- 背景柔光光晕 (Ambient Disc Glow) -->
    <div
      class="absolute inset-4 rounded-full filter blur-2xl opacity-40 dark:opacity-30 transition-all duration-700 pointer-events-none scale-105"
      :style="{
        backgroundImage: `radial-gradient(circle, rgba(59, 130, 246, 0.45) 0%, rgba(147, 51, 234, 0.25) 50%, transparent 70%)`
      }"
    ></div>

    <!-- 唱针基座与唱臂 (Tonearm) -->
    <div class="absolute -top-3 right-8 sm:right-10 z-30 pointer-events-none transition-transform duration-700 ease-out origin-[20px_16px]"
      :class="isPlaying ? 'rotate-0' : '-rotate-[32deg]'"
    >
      <!-- 基座轴承 -->
      <div class="relative w-10 h-10 rounded-full bg-gradient-to-b from-slate-200 to-slate-400 dark:from-slate-700 dark:to-slate-900 border-2 border-slate-300 dark:border-slate-600 shadow-md flex items-center justify-center">
        <div class="w-4 h-4 rounded-full bg-slate-400 dark:bg-slate-600 border border-slate-100 dark:border-slate-800"></div>
      </div>
      <!-- 唱臂金属杆 -->
      <div class="absolute top-5 left-4 w-2 h-28 bg-gradient-to-r from-slate-300 via-slate-100 to-slate-400 dark:from-slate-600 dark:via-slate-400 dark:to-slate-700 rounded-full shadow-lg origin-top rotate-[-12deg]">
        <!-- 唱头/唱针 -->
        <div class="absolute -bottom-3 -left-1.5 w-5 h-7 rounded-sm bg-gradient-to-br from-slate-700 to-slate-950 border border-slate-500 shadow-md">
          <div class="w-1 h-3 bg-amber-400 mx-auto mt-1 rounded-full shadow-[0_0_4px_#fbbf24]"></div>
        </div>
      </div>
    </div>

    <!-- 黑胶唱盘 (Vinyl Disc) -->
    <div
      class="relative w-full h-full rounded-full bg-[#111318] p-4 shadow-2xl border-4 border-slate-800/80 dark:border-slate-700/60 flex items-center justify-center transition-transform"
      :class="isPlaying ? 'vinyl-spin' : 'vinyl-spin-paused'"
    >
      <!-- 唱片同心微槽反光纹理 (Grooves Texture) -->
      <div class="absolute inset-2 rounded-full border border-white/5 pointer-events-none"></div>
      <div class="absolute inset-4 rounded-full border border-white/5 pointer-events-none"></div>
      <div class="absolute inset-7 rounded-full border border-white/5 pointer-events-none"></div>
      <div class="absolute inset-10 rounded-full border border-white/5 pointer-events-none"></div>
      <div class="absolute inset-14 rounded-full border border-white/5 pointer-events-none"></div>

      <!-- 唱片表面金属光泽高光遮罩 -->
      <div class="absolute inset-0 rounded-full bg-gradient-to-tr from-transparent via-white/[0.04] to-transparent pointer-events-none"></div>

      <!-- 唱片中心封面 (Album Artwork Center) -->
      <div class="relative w-[52%] h-[52%] rounded-full overflow-hidden border-4 border-[#1f242d] shadow-inner bg-slate-900 flex items-center justify-center">
        <img
          :src="displayCover"
          :alt="songTitle"
          class="w-full h-full object-cover select-none"
        />
        <!-- 唱片中心金属轴孔 -->
        <div class="absolute w-6 h-6 rounded-full bg-slate-900 border-2 border-white/40 shadow-inner flex items-center justify-center">
          <div class="w-2 h-2 rounded-full bg-white/80 shadow"></div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
@keyframes spinContinuous {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.vinyl-spin {
  animation: spinContinuous 22s linear infinite;
}

.vinyl-spin-paused {
  animation: spinContinuous 22s linear infinite;
  animation-play-state: paused;
}
</style>
