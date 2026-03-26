import type { Config } from 'tailwindcss';

export default {
  darkMode: 'class',
  content: ['./index.html', './src/**/*.{vue,ts,tsx}'],
  theme: {
    extend: {
      colors: {
        dyx: {
          page: 'rgb(var(--dyx-bg-page-rgb) / <alpha-value>)',
          'page-soft': 'rgb(var(--dyx-bg-page-soft-rgb) / <alpha-value>)',
          surface: 'rgb(var(--dyx-bg-surface-rgb) / <alpha-value>)',
          'surface-muted': 'rgb(var(--dyx-bg-surface-muted-rgb) / <alpha-value>)',
          text: 'rgb(var(--dyx-text-main-rgb) / <alpha-value>)',
          muted: 'rgb(var(--dyx-text-muted-rgb) / <alpha-value>)',
          meta: 'rgb(var(--dyx-text-meta-rgb) / <alpha-value>)',
          accent: 'rgb(var(--dyx-accent-rgb) / <alpha-value>)',
          border: 'rgb(var(--dyx-border-subtle-rgb) / <alpha-value>)'
        }
      },
      boxShadow: {
        'dyx-soft': 'var(--dyx-shadow-soft)',
        'dyx-elevated': 'var(--dyx-shadow-elevated)',
        'dyx-window': 'var(--dyx-shadow-window)'
      },
      borderRadius: {
        'dyx-card': 'var(--dyx-radius-card)',
        'dyx-window': 'var(--dyx-radius-window)',
        'dyx-pill': '999px'
      }
    }
  },
  plugins: []
} satisfies Config;
