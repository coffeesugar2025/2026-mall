// 前端测试全局 setup
// 为 jsdom 环境补充 EventSource stub（Node 端无原生 EventSource）
if (typeof globalThis.EventSource === 'undefined') {
  globalThis.EventSource = class {
    constructor() { this.onmessage = null; this.onerror = null }
    close() {}
    addEventListener() {}
    removeEventListener() {}
  }
}

// 屏蔽 Element Plus 未注册全局组件的告警（测试中以 stub 方式挂载）
const warn = console.warn
console.warn = (...args) => {
  if (typeof args[0] === 'string' && args[0].includes('[Vue warn]')) return
  warn(...args)
}
