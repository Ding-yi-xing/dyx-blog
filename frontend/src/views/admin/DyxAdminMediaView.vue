<template>
  <section class="rounded-[28px] bg-white p-6 shadow-sm">
    <div class="flex items-center justify-between">
      <div>
        <h2 class="text-xl font-semibold text-slate-900">媒体资源管理</h2>
        <p class="mt-2 text-sm text-slate-500">统一管理文章封面、照片与其他上传文件。</p>
      </div>
      <el-upload :show-file-list="false" :http-request="handleUpload">
        <el-button type="primary">选择文件</el-button>
      </el-upload>
    </div>
    <el-table class="mt-6" :data="mediaList" border>
      <el-table-column prop="originalName" label="文件名" min-width="220" />
      <el-table-column prop="mediaType" label="类型" width="160" />
      <el-table-column prop="sizeText" label="大小" width="120" />
      <el-table-column prop="createdAt" label="上传时间" width="180" />
    </el-table>
  </section>
</template>

<script setup lang="ts">
import { ElMessage, type UploadRequestOptions } from 'element-plus';
import { computed, onMounted, ref } from 'vue';
import { getAdminMedia, uploadAdminMedia } from '@/api/modules/admin';
import type { MediaData } from '@/api/modules/admin';

const mediaRawList = ref<MediaData[]>([]);

const mediaList = computed(() =>
  mediaRawList.value.map((item) => ({
    ...item,
    sizeText: item.fileSize ? `${Math.round(item.fileSize / 1024)}KB` : '0KB'
  }))
);

/**
 * 加载媒体资源列表。
 */
async function loadMediaList(): Promise<void> {
  const response = await getAdminMedia();
  mediaRawList.value = response.data ?? [];
}

/**
 * 执行媒体文件上传。
 * @param options Element Plus 上传请求参数。
 */
async function handleUpload(options: UploadRequestOptions): Promise<void> {
  try {
    await uploadAdminMedia(options.file as File);
    ElMessage.success('上传成功');
    await loadMediaList();
  } catch (error) {
    ElMessage.error('上传失败');
  }
}

onMounted(() => {
  void loadMediaList();
});
</script>
