import request from '@/utils/request'

// 获取优惠券列表
export function getCouponList() {
  return request({
    url: '/api/coupon/list',
    method: 'get'
  })
}

// 获取优惠券详情
export function getCouponById(id) {
  return request({
    url: `/api/coupon/${id}`,
    method: 'get'
  })
}

// 添加优惠券
export function addCoupon(data) {
  return request({
    url: '/api/coupon',
    method: 'post',
    data
  })
}

// 更新优惠券
export function updateCoupon(data) {
  return request({
    url: '/api/coupon',
    method: 'put',
    data
  })
}

// 删除优惠券
export function deleteCoupon(id) {
  return request({
    url: `/api/coupon/${id}`,
    method: 'delete'
  })
}
