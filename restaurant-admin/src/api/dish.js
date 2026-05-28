import request from '@/utils/request'

// 获取菜品分页列表
export function getDishPage(params) {
  return request({
    url: '/api/dish/page',
    method: 'get',
    params
  })
}

// 获取菜品详情
export function getDishById(id) {
  return request({
    url: `/api/dish/${id}`,
    method: 'get'
  })
}

// 添加菜品
export function addDish(data) {
  return request({
    url: '/api/dish',
    method: 'post',
    data
  })
}

// 更新菜品
export function updateDish(data) {
  return request({
    url: '/api/dish',
    method: 'put',
    data
  })
}

// 删除菜品
export function deleteDish(id) {
  return request({
    url: `/api/dish/${id}`,
    method: 'delete'
  })
}

// 批量删除菜品
export function batchDeleteDish(ids) {
  return request({
    url: '/api/dish/batch',
    method: 'delete',
    data: ids
  })
}

// 更新菜品状态
export function updateDishStatus(id, status) {
  return request({
    url: `/api/dish/status/${id}`,
    method: 'put',
    params: { status }
  })
}

// 获取菜品分类列表
export function getCategoryList() {
  return request({
    url: '/api/dish/category/list',
    method: 'get'
  })
}

// 添加分类
export function addCategory(data) {
  return request({
    url: '/api/dish/category',
    method: 'post',
    data
  })
}

// 更新分类
export function updateCategory(data) {
  return request({
    url: '/api/dish/category',
    method: 'put',
    data
  })
}

// 删除分类
export function deleteCategory(id) {
  return request({
    url: `/api/dish/category/${id}`,
    method: 'delete'
  })
}
