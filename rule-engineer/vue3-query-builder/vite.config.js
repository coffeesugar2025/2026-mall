import { defineConfig } from 'vite'
import { resolve } from 'path'
// const path = require('path')
import { fileURLToPath, URL } from 'node:url'
import vue from '@vitejs/plugin-vue'


// https://vitejs.dev/config/
export default defineConfig({
    plugins: [vue()],
    resolve: {
        alias: {
            '/@': resolve('./node_modules/vue')
            // '@': fileURLToPath(new URL('./src', import.meta.url)),
            // vue: resolve('./node_modules/vue'),
        },
        dedupe: ['vue'], // this line 
        preserveSymlinks: false,
    },
    build:{
        lib: {
            alias: {
                '@': fileURLToPath(new URL('./src', import.meta.url)),
            },
            entry: resolve(__dirname, 'src/index.js'),
            name: "AdvancedQueryBuilder",
            fileName: (format) => `vue3-advanced-query-builder.${format}.js`
        }
    },
    rollupOptions: {
        // make sure to externalize deps that shouldn't be bundled
        // into your library
        external: ["vue"],
        output: {
            // Provide global variables to use in the UMD build
            // for externalized deps
            globals: {
                vue: 'Vue',
            },
        },
    },
    
  })