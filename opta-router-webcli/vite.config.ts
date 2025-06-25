import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import tailwindcss from "@tailwindcss/vite";

// https://vite.dev/config/
export default defineConfig({
  server: {
    port: 3000,
    proxy: {
      "/api/": {
        target: "http://localhost:8080/",
        changeOrigin: true,
        secure: false,
      },
      "/ws/": {
        target: "ws://localhost:8080/",
        changeOrigin: true,
        secure: false,
        ws: true,
      },
    },
  },
  plugins: [vue(), tailwindcss()],
  optimizeDeps: {
    exclude: ["oh-vue-icons/icons"],
  },
});
