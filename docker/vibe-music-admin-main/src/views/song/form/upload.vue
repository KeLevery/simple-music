<script setup lang="ts">
import { ref, defineProps, defineEmits, watch } from "vue";
import { updateSongAudio } from "@/api/system";
import { message } from "@/utils/message";
import UploadIcon from "@iconify-icons/ri/upload-2-line";

const props = defineProps({ songId: Number, visible: Boolean });
const emit = defineEmits(["update:visible", "success"]);

const fileList = ref([]);
const isVisible = ref(props.visible);
const audioUrl = ref(""); // 存储音频预览URL

watch(
  () => props.visible,
  newVal => {
    isVisible.value = newVal;
  }
);

const handleChange = file => {
  if (!file.raw.name.endsWith(".mp3")) {
    message("只能上传.mp3格式的音频文件", { type: "warning" });
    fileList.value = [];
    return;
  }
  fileList.value = [file.raw]; // 只允许上传一个文件，替换掉之前的

  // 创建音频预览 URL
  if (audioUrl.value) {
    URL.revokeObjectURL(audioUrl.value);
  }
  audioUrl.value = URL.createObjectURL(file.raw);
};

const submitForm = async () => {
  if (!fileList.value.length) {
    message("请先选择一个音频文件", { type: "warning" });
    return;
  }

  const formData = new FormData();
  formData.append("audio", fileList.value[0]);

  try {
    const res = await updateSongAudio(props.songId, formData);
    if (res.code === 0) {
      message("上传成功", { type: "success" });
      emit("update:visible", false);
      emit("success");
    } else {
      message("上传失败", { type: "error" });
    }
  } catch (error) {
    console.error("上传失败:", error);
    message("上传失败，请重试", { type: "error" });
  }
};
</script>

<template>
  <el-dialog
    v-model="isVisible"
    title="上传音频"
    @close="emit('update:visible', false)"
  >
    <el-upload
      :file-list="fileList"
      :auto-upload="false"
      :limit="1"
      action="#"
      drag
      accept=".mp3"
      @change="handleChange"
    >
      <div class="el-upload__text">
        <IconifyIconOffline :icon="UploadIcon" width="26" class="m-auto mb-2" />
        点击或拖拽上传 (仅支持.mp3格式)
      </div>
    </el-upload>
    <audio v-if="audioUrl" :src="audioUrl" controls class="mt-3" />
    <template #footer>
      <el-button @click="emit('update:visible', false)">取消</el-button>
      <el-button type="primary" @click="submitForm">提交</el-button>
    </template>
  </el-dialog>
</template>

<style scoped></style>
