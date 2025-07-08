import "vitest/config";

import vue from "@vitejs/plugin-vue";
import { defineConfig } from "vitest/config";

export default defineConfig({
  plugins: [vue()],
  test: {
    globals: true,
    environment: "jsdom",
    setupFiles: ["./vitest.setup.ts"],
    coverage: {
      exclude: ["*.config.ts", "**/generated/**", "**/index.ts", "**/main.ts", "**/App.vue"],
      reporter: ["lcov", "html", "clover"],
    },
  },
});
