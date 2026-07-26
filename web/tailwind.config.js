/** @type {import('tailwindcss').Config} */
export default {
  darkMode: 'class',
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      fontFamily: {
        sans: ['Inter', 'sans-serif'],
        mono: ['JetBrains Mono', 'monospace'],
      },
      colors: {
        brand: {
          dark: '#0B0F19',
          light: '#FFFFFF',
          cardDark: '#1F2937',
          cardLight: '#FFFFFF',
          accent: '#00E5FF',
          textDark: '#9CA3AF',
          textLight: '#4B5563',
          headingDark: '#F9FAFB',
          headingLight: '#1E1E24'
        }
      }
    },
  },
  plugins: [],
}
