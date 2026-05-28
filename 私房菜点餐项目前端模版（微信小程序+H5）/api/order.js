import request from '../utils/request.js'

/**
 * 创建订单
 */
export function createOrder(data) {
  return request({
    url: '/order/create',
    method: 'POST',
    data
  })
}

/**
 * 获取我的订单列表
 */
export function getMyOrders(params) {
  return request({
    url: '/order/my',
    method: 'GET',
    data: params
  })
}

/**
 * 获取订单详情
 */
export function getOrderDetail(id) {
  return request({
    url: `/order/${id}`,
    method: 'GET'
  })
}
