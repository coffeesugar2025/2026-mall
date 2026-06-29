import vue from 'rollup-plugin-vue'
import peerDepsExternal from 'rollup-plugin-peer-deps-external'
import terser from '@rollup/plugin-terser';
import scss from 'rollup-plugin-scss'

export default [{
    input: 'src/index.js',
    output: [
    {
        format: 'esm',
        file: 'dist/vue3-advanced-query-builder.mjs'
    },
    {
        format: 'cjs',
        file: 'dist/vue3-advanced-query-builder.js'
    }
    ],
    external: ['vuedraggable'],
    plugins: [
        vue(), 
        peerDepsExternal(), 
        terser(),
        scss({
            // output: 'dist/vue3-advanced-query-builder.css',
            fileName: 'styles.css'
        })
        
    ]
}]