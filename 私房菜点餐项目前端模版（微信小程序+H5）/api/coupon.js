// 优惠券相关API
import request from '@/utils/request.js'

// 获取优惠券列表（分页）
export const getCouponList = (params) => {
	return request({
		url: '/coupon/page',
		method: 'GET',
		data: params
	})
}

// 领取优惠券
export const receiveCoupon = (couponId) => {
	return request({
		url: `/coupon/receive/${couponId}`,
		method: 'POST'
	})
}

// 获取用户优惠券
export const getUserCoupons = (status) => {
	return request({
		url: '/coupon/user',
		method: 'GET',
		data: { status }
	})
}
