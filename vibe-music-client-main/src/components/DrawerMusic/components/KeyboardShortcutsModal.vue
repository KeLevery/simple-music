<script setup lang="ts">
import { Icon } from '@iconify/vue'

interface Props {
  modelValue: boolean
}

defineProps<Props>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const shortcuts = [
  { key: 'Space', desc: '播放 / 暂停' },
  { key: 'ESC', desc: '收起 / 退出全屏' },
  { key: '← / →', desc: '快退 / 快进 5 秒' },
  { key: '↑ / ↓', desc: '增加 / 减小音量 5%' },
  { key: 'M', desc: '一键静音 / 取消静音' },
  { key: 'F', desc: '切换全屏沉浸模式' },
  { key: 'H', desc: '隐藏 / 显示右侧面板 (全屏唱片)' },
  { key: 'L', desc: '将当前歌曲加入/取消喜欢' },
  { key: 'N', desc: '切换至下一首歌曲' },
  { key: 'P', desc: '切换至上一首歌曲' },
]
</script>

<template>
  <el-dialog
    :model-value="modelValue"
    @update:model-value="emit('update:modelValue', $event)"
    title="播放器键盘快捷键指南"
    width="440px"
    destroy-on-close
    append-to-body
    class="rounded-2xl"
  >
    <div class="space-y-3 py-1">
      <div
        v-for="item in shortcuts"
        :key="item.key"
        class="flex items-center justify-between py-2 px-3 rounded-xl hover:bg-slate-100 dark:hover:bg-slate-800/80 transition-colors"
      >
        <span class="text-xs text-slate-600 dark:text-slate-300 font-medium">{{ item.desc }}</span>
        <kbd class="px-2.5 py-1 text-xs font-mono font-semibold text-slate-800 dark:text-slate-200 bg-slate-200 dark:bg-slate-700 rounded-lg shadow-sm border border-slate-300 dark:border-slate-600">
          {{ item.key }}
        </kbd>
      </div>

      <div class="mt-4 pt-3 border-t border-slate-100 dark:border-slate-800 text-center">
        <p class="text-[11px] text-slate-400">提示：在输入框内打字时快捷键会自动停用以防误触</p>
      </div>
    </div>
  </el-dialog>
</template>
