import request from '@/utils/request'

// 获取订单分页列表
export function getOrderPage(params) {
  return request({
    url: '/api/order/page',
    method: 'get',
    params
  })
}

// 获取订单详情
export function getOrderById(id) {
  return request({
    url: `/api/order/${id}`,
    method: 'get'
  })
}

// 更新订单状态
export function updateOrderStatus(id, status) {
  return request({
    url: `/api/order/status/${id}`,
    method: 'put',
    params: { status }
  })
}

// 获取实时订单
export function getRealtimeOrders(params) {
  return request({
    url: '/api/order/realtime',
    method: 'get',
    params
  })
}
