import axios from 'axios'

const BASE_URL = '/api'

// 创建订单
export const createOrder = (data) => {
  return axios.post(`${BASE_URL}/pay/create-order`, data)
}

// 发起支付
export const payOrder = (orderNo, payType) => {
  return axios.post(`${BASE_URL}/pay/pay/${orderNo}?payType=${payType}`)
}

// 查询订单
export const queryOrder = (orderNo) => {
  return axios.get(`${BASE_URL}/pay/order/${orderNo}`)
}

// 关闭订单
export const closeOrder = (orderNo) => {
  return axios.post(`${BASE_URL}/pay/order/${orderNo}/close`)
}

// 获取订单列表
export const getOrders = (page = 1, size = 10, status = '') => {
  return axios.get(`${BASE_URL}/orders`, { params: { page, size, status } })
}

// 手动触发微信 Mock 支付成功
export const mockWechatSuccess = (orderNo) => {
  return axios.post(`${BASE_URL}/pay/wechat/mock-success/${orderNo}`)
}

// 获取支付宝配置
export const getAlipayConfig = () => {
  return axios.get(`${BASE_URL}/pay/alipay/config`)
}
