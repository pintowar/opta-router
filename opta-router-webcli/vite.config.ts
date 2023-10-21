import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import svgLoader from "vite-svg-loader";

// https://vitejs.dev/config/
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
  plugins: [vue(), svgLoader()],
});
