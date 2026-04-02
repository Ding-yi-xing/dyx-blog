import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import path from 'node:path';

function resolveManualChunk(id: string): string | undefined {
  if (!id.includes('node_modules') && !id.includes('/src/utils/footprintGeo')) {
    return undefined;
  }

  if (
    id.includes('/node_modules/vue/') ||
    id.includes('/node_modules/vue-router/') ||
    id.includes('/node_modules/pinia/')
  ) {
    return 'vendor-core';
  }

  if (
    id.includes('/node_modules/element-plus/') ||
    id.includes('/node_modules/@element-plus/icons-vue/')
  ) {
    return 'vendor-ui';
  }

  if (id.includes('/node_modules/echarts/') || id.includes('/node_modules/vue-echarts/')) {
    return 'vendor-charts';
  }

  if (id.includes('/node_modules/china-map-data/') || id.includes('/src/utils/footprintGeo')) {
    return 'vendor-map';
  }

  return undefined;
}

/**
 * Vite 配置文件。
 * 用于配置 Vue 插件、路径别名以及本地开发代理。
 */
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src')
    }
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks: resolveManualChunk
      }
    }
  },
  server: {
    port: 5173,
    host: '0.0.0.0',
    proxy: {
      '/api': {
        // 本地
        // target: 'http://localhost:8080',
        // 正式
        target: 'http://localhost:8050',
        changeOrigin: true
      },
      '/media': {
        // 正式
        target: 'http://localhost:8050',
        // 本地
        // target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
});
