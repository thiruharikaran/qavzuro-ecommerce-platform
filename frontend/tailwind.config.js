/** @type {import('tailwindcss').Config} */
export default {
  content: ["./index.html", "./src/**/*.{js,jsx}"],
  theme: {
    extend: {
      colors: {
        ink: {
          50: '#f4f6fa',
          100: '#e4e9f2',
          400: '#5b6b8c',
          600: '#2b3a5c',
          800: '#16223c',
          900: '#0d1526',
        },
        brass: {
          50: '#fbf3e6',
          200: '#eccf9a',
          400: '#cf9a4d',
          500: '#b8823a',
          600: '#98692c',
        },
        linen: '#faf8f4',
      },
      fontFamily: {
        display: ['"Fraunces"', 'Georgia', 'serif'],
        sans: ['"Inter"', 'system-ui', 'sans-serif'],
      },
    },
  },
  plugins: [],
}
