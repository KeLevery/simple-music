<script setup lang="ts">
import { ref } from 'vue'
import { useAudioPlayer } from '@/hooks/useAudioPlayer'
import DrawerMusic from '@/components/DrawerMusic/index.vue'
import { Icon } from '@iconify/vue'

const { currentTrack } = useAudioPlayer()
const showDrawerMusic = ref(false)
</script>

<template>
  <div 
    class="flex items-center gap-2 w-64 cursor-pointer select-none hover:bg-hoverMenuBg transition-colors rounded-lg p-1" 
    @click="showDrawerMusic = !showDrawerMusic"
  >
    <div class="relative min-w-12 max-w-12 h-12 rounded-lg overflow-hidden group">
      <img
        :src="currentTrack.cover + '?param=90y90'"
        :alt="currentTrack.title"
        class="w-full h-full object-cover rounded-lg"
      />
      <div class="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 flex items-center justify-center text-white text-xs transition-opacity rounded-lg">
        <Icon icon="ri:fullscreen-line" />
      </div>
    </div>
    <div class="flex-1 min-w-0">
      <div
        class="text-sm font-medium text-primary-foreground truncate mx-2"
        :title="currentTrack.title"
      >
        {{ currentTrack.title }}
      </div>
      <div class="text-xs text-muted-foreground truncate h-4 mt-0.5 mx-2">
        {{ currentTrack.artist }}
      </div>
    </div>
    <DrawerMusic v-model="showDrawerMusic" />
  </div>
</template>
