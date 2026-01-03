import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      // Frontend → /api/search/... proxas till search-service NodePort
      "/api/search": {
        target: "http://localhost:30085",
        changeOrigin: true,
        secure: false,
      },
    },
  },
});
