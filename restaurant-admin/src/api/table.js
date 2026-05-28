import request from '@/utils/request'

// 获取桌台列表
export function getTableList() {
  return request({
    url: '/api/table/list',
    method: 'get'
  })
}

// 获取桌台详情
export function getTableById(id) {
  return request({
    url: `/api/table/${id}`,
    method: 'get'
  })
}

// 添加桌台
export function addTable(data) {
  return request({
    url: '/api/table',
    method: 'post',
    data
  })
}

// 更新桌台
export function updateTable(data) {
  return request({
    url: '/api/table',
    method: 'put',
    data
  })
}

// 删除桌台
export function deleteTable(id) {
  return request({
    url: `/api/table/${id}`,
    method: 'delete'
  })
}

// 更新桌台状态
export function updateTableStatus(id, status) {
  return request({
    url: `/api/table/status/${id}`,
    method: 'put',
    params: { status }
  })
}
