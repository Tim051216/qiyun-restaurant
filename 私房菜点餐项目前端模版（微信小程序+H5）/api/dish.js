// 菜品相关API
import request from '@/utils/request.js'

// 获取菜品分类列表
export const getDishCategories = () => {
	return request({
		url: '/dish-category/list',
		method: 'GET'
	})
}

// 获取菜品列表（分页）
export const getDishList = (params) => {
	return request({
		url: '/dish/page',
		method: 'GET',
		data: params
	})
}

// 根据分类ID获取菜品
export const getDishByCategory = (categoryId) => {
	return request({
		url: `/dish/category/${categoryId}`,
		method: 'GET'
	})
}

// 获取所有菜品（按分类分组）
export const getDishesGrouped = () => {
	return request({
		url: '/dish/grouped',
		method: 'GET'
	})
}
