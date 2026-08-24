import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import IncidentView from '../IncidentView.vue'

vi.mock('axios', () => ({
  default: {
    post: vi.fn(() =>
      Promise.resolve({
        data: {
          rootCause: 'DB 连接池耗尽',
          impact: '订单服务不可用',
          suggestion: '扩容连接池',
          ticketId: 'TICKET-test',
          riskLevel: 'HIGH'
        }
      })
    )
  }
}))

describe('IncidentView.vue', () => {
  it('提交事件描述后应渲染结构化报告', async () => {
    const wrapper = mount(IncidentView, {
      global: { stubs: ['el-card', 'el-input', 'el-button', 'el-tag', 'el-alert', 'el-descriptions', 'el-descriptions-item'] }
    })
    wrapper.vm.incident = 'order-service 连续 500 错误'
    await wrapper.vm.handleAnalyze()
    await wrapper.vm.$nextTick()

    expect(wrapper.vm.report).not.toBeNull()
    expect(wrapper.vm.report.rootCause).toBe('DB 连接池耗尽')
    expect(wrapper.vm.report.ticketId).toBe('TICKET-test')
  })

  it('空描述不应调用分析接口', async () => {
    const axios = await import('axios')
    const wrapper = mount(IncidentView, {
      global: { stubs: ['el-card', 'el-input', 'el-button'] }
    })
    wrapper.vm.incident = '   '
    await wrapper.vm.handleAnalyze()
    expect(axios.default.post).not.toHaveBeenCalled()
  })
})
