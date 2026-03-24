import type { Config } from 'tailwindcss';

/**
 * Tailwind 配置文件。
 * 用于定义扫描路径和主题扩展，保证前后台界面风格统一。
 */
export default {
  content: ['./index.html', './src/**/*.{vue,ts,tsx}'],
  theme: {
    extend: {
      colors: {
        dyx: {
          dark: '#0f172a',
          primary: '#2563eb',
          secondary: '#64748b',
          light: '#f8fafc'
        }
      },
      boxShadow: {
        'dyx-soft': '0 24px 60px rgba(15, 23, 42, 0.12)'
      },
      borderRadius: {
        'dyx-card': '1.5rem'
      }
    }
  },
  plugins: []
} satisfies Config;
