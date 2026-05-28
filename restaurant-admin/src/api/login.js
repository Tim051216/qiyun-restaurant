import request from '@/utils/request'

// 管理员登录
export function login(data) {
  return request({
    url: '/api/admin/login',  // 添加 /api 前缀
    method: 'post',
    data
  })
}

// 退出登录
export function logout() {
  return request({
    url: '/api/admin/logout',  // 添加 /api 前缀
    method: 'post'
  })
}
