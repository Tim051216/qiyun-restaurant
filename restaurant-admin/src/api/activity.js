import request from '@/utils/request'

// 获取活动列表
export function getActivityList() {
  return request({
    url: '/api/activity/list',
    method: 'get'
  })
}

// 获取活动详情
export function getActivityById(id) {
  return request({
    url: `/api/activity/${id}`,
    method: 'get'
  })
}

// 添加活动
export function addActivity(data) {
  return request({
    url: '/api/activity',
    method: 'post',
    data
  })
}

// 更新活动
export function updateActivity(data) {
  return request({
    url: '/api/activity',
    method: 'put',
    data
  })
}

// 删除活动
export function deleteActivity(id) {
  return request({
    url: `/api/activity/${id}`,
    method: 'delete'
  })
}
