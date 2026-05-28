// API请求工具
import { getToken } from './auth.js'

const BASE_URL = 'http://localhost:9080'

const request = (options) => {
	return new Promise((resolve, reject) => {
		// 添加token到请求头
		const token = getToken()
		const header = {
			'Content-Type': 'application/json',
			...options.header
		}
		if (token) {
			header['token'] = token
		}
		
		uni.request({
			url: BASE_URL + options.url,
			method: options.method || 'GET',
			data: options.data || {},
			header: header,
			success: (res) => {
				if (res.statusCode === 200) {
					if (res.data.code === 1 || res.data.code === 200) {
						resolve(res.data)
					} else {
						uni.showToast({
							title: res.data.msg || '请求失败',
							icon: 'none'
						})
						reject(res.data)
					}
				} else {
					uni.showToast({
						title: '网络请求失败',
						icon: 'none'
					})
					reject(res)
				}
			},
			fail: (err) => {
				uni.showToast({
					title: '网络连接失败',
					icon: 'none'
				})
				reject(err)
			}
		})
	})
}

export default request
