import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import KnowledgeView from '../KnowledgeView.vue'

// 模拟 axios
vi.mock('axios', () => ({
  default: {
    post: vi.fn(() => Promise.resolve({ data: { ingested: 3 } })),
    get: vi.fn(() => Promise.resolve({ data: [] }))
  }
}))

describe('KnowledgeView.vue', () => {
  let wrapper

  beforeEach(() => {
    wrapper = mount(KnowledgeView, {
      global: {
        stubs: ['el-upload', 'el-button', 'el-input', 'el-table', 'el-table-column', 'el-card', 'el-tag', 'el-message']
      }
    })
  })

  it('初始状态下文档列表为空', () => {
    expect(wrapper.vm.documents).toEqual([])
  })

  it('目录路径为空时不应触发摄取', async () => {
    wrapper.vm.ingestDir = ''
    const axios = await import('axios')
    await wrapper.vm.handleIngest()
    expect(axios.default.post).not.toHaveBeenCalled()
  })

  it('填写目录后应能调用摄取接口', async () => {
    const axios = await import('axios')
    wrapper.vm.ingestDir = '/data/docs'
    await wrapper.vm.handleIngest()
    expect(axios.default.post).toHaveBeenCalledWith(
      expect.stringContaining('/ai/rag/ingest'),
      expect.anything(),
      expect.anything()
    )
  })

  it('提交检索后应更新结果列表', async () => {
    const axios = await import('axios')
    axios.default.get.mockResolvedValueOnce({
      data: [{ text: 'DB 连接超时排查', score: 0.92, source: 'ops.pdf' }]
    })
    wrapper.vm.query = '连接池'
    await wrapper.vm.handleSearch()
    expect(wrapper.vm.results.length).toBe(1)
    expect(wrapper.vm.results[0].text).toContain('DB 连接超时')
  })
})
