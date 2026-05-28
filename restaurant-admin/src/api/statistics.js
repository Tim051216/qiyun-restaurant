import request from '@/utils/request'

// 获取仪表盘统计数据
export function getDashboardStats() {
  return request({
    url: '/api/statistics/dashboard',
    method: 'get'
  })
}

// 获取菜品销量排行
export function getDishTop(limit = 5) {
  return request({
    url: '/api/statistics/dish/top',
    method: 'get',
    params: { limit }
  })
}

// 获取销售趋势数据
export function getSalesTrend(days = 7) {
  return request({
    url: '/api/statistics/sales/trend',
    method: 'get',
    params: { days }
  })
}
