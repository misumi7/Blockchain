import { defineConfig , loadEnv } from 'vite'
import react from '@vitejs/plugin-react'

// https://vite.dev/config/
export default defineConfig(({mode}) => {
  const env = loadEnv(mode, process.cwd());
  const backendPort = env.VITE_BACKEND_PORT;

  return {
    plugins: [react()],
    server: {
      proxy: {
        '/api': `http://localhost:${backendPort}`
      }
    },
    resolve: {
      alias: [
        {
          find: /^@nectary\/components\/(.*)$/,
          replacement: "@nectary/components/$1/index",
        },
      ],
    },
  };
});
