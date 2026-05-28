// 活动相关API
import request from '@/utils/request.js'

// 获取活动列表（分页）
export const getActivityList = (params) => {
	return request({
		url: '/activity/page',
		method: 'GET',
		data: params
	})
}

// 根据分类获取活动
export const getActivityByCategory = (category) => {
	return request({
		url: `/activity/category/${category}`,
		method: 'GET'
	})
}

// 参与活动
export const joinActivity = (activityId) => {
	return request({
		url: `/activity/join/${activityId}`,
		method: 'POST'
	})
}
