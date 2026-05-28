import request from '@/utils/request'

// 获取会员分页列表
export function getMemberPage(params) {
  return request({
    url: '/api/member/page',
    method: 'get',
    params
  })
}

// 获取会员详情
export function getMemberById(id) {
  return request({
    url: `/api/member/${id}`,
    method: 'get'
  })
}

// 更新会员信息
export function updateMember(data) {
  return request({
    url: '/api/member',
    method: 'put',
    data
  })
}
