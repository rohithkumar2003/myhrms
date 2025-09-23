/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,jsx,ts,tsx}",
  ],
  theme: {
    extend: {},
    colors: {
      transparent: "transparent",
      current: "currentColor",
      white: "#ffffff",
      black: "#000000",
      gray: {
        50: "#f9fafb",
        100: "#f3f4f6",
        200: "#e5e7eb",
        300: "#d1d5db",
        400: "#9ca3af",
        500: "#6b7280",
        600: "#4b5563",
        700: "#374151",
        800: "#1f2937",
        900: "#111827",
      },
      red: {
        500: "#ef4444",
        600: "#dc2626",
      },
      green: {
        500: "#22c55e",
        600: "#16a34a",
      },
      yellow: {
        400: "#facc15",
      },
      blue: {
        600: "#2563eb",
        700: "#1d4ed8",
      },
    },
  },
  plugins: [
    require('@tailwindcss/line-clamp'),
  ],
  future: {
    useOkLCH: false, // ✅ disables oklch usage
  },
};
