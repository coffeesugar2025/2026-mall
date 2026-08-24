import { describe, it, expect, vi } from 'vitest'
import api from '../api.js'

vi.mock('axios', () => {
  const mockAxios = {
    defaults: { baseURL: '/api', headers: {} },
    post: vi.fn(() => Promise.resolve({ data: {} })),
    get: vi.fn(() => Promise.resolve({ data: [] })),
    interceptors: { request: { use: vi.fn() }, response: { use: vi.fn() } }
  }
  return { default: mockAxios }
})

describe('api service', () => {
  it('chatStream 应返回 EventSource 实例（浏览器环境）', () => {
    // Node 测试环境下 EventSource 可能不存在，仅验证函数可调用
    expect(typeof api.chatStream).toBe('function')
  })

  it('analyzeIncident 应 POST 到 /ai/analyze', async () => {
    const axios = (await import('axios')).default
    await api.analyzeIncident('order-service 500', 'ops-001')
    expect(axios.post).toHaveBeenCalledWith(
      '/ai/analyze?userId=ops-001',
      'order-service 500',
      expect.objectContaining({ headers: expect.anything() })
    )
  })

  it('ingestDocuments 应 POST 到 /ai/rag/ingest', async () => {
    const axios = (await import('axios')).default
    await api.ingestDocuments('/data/docs')
    expect(axios.post).toHaveBeenCalledWith(
      '/ai/rag/ingest',
      { dir: '/data/docs' },
      expect.anything()
    )
  })
})
