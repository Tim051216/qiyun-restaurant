/**
 * 定位工具类
 * 提供获取位置、计算距离、地图导航等功能
 */

import restaurantConfig from '@/config/restaurant.js'

// 餐厅位置信息（从配置文件读取）
const RESTAURANT_INFO = {
	name: restaurantConfig.name,
	address: restaurantConfig.address,
	latitude: restaurantConfig.location.latitude,
	longitude: restaurantConfig.location.longitude,
	phone: restaurantConfig.phone
}

/**
 * 获取用户当前位置
 * @returns {Promise} 返回位置信息
 */
export function getUserLocation() {
	return new Promise((resolve, reject) => {
		// #ifdef MP-WEIXIN
		// 检查是否在开发工具中
		const systemInfo = uni.getSystemInfoSync()
		if (systemInfo.platform === 'devtools') {
			console.log('[Location] 开发工具环境，使用模拟位置')
			// 返回模拟位置（南京市中心）
			resolve({
				latitude: 32.0584,
				longitude: 118.7969,
				address: '江苏省南京市玄武区（模拟位置）'
			})
			return
		}
		// #endif
		
		uni.getLocation({
			type: 'gcj02', // 返回可以用于uni.openLocation的经纬度
			success: (res) => {
				console.log('[Location] 获取位置成功:', res)
				
				// 如果返回了地址信息，直接使用
				if (res.address) {
					resolve({
						latitude: res.latitude,
						longitude: res.longitude,
						address: res.address
					})
				} else {
					// 没有地址信息，尝试逆地理编码
					getAddressFromLocation(res.latitude, res.longitude).then(address => {
						resolve({
							latitude: res.latitude,
							longitude: res.longitude,
							address: address
						})
					}).catch(() => {
						// 逆地理编码失败，返回坐标
						resolve({
							latitude: res.latitude,
							longitude: res.longitude,
							address: `${res.latitude.toFixed(4)}, ${res.longitude.toFixed(4)}`
						})
					})
				}
			},
			fail: (err) => {
				console.error('[Location] 获取位置失败:', err)
				
				// 处理不同的错误情况
				if (err.errMsg.includes('auth deny')) {
					uni.showModal({
						title: '需要位置权限',
						content: '请在设置中开启位置权限，以便为您提供更好的服务',
						confirmText: '去设置',
						success: (modalRes) => {
							if (modalRes.confirm) {
								// 打开设置页面
								uni.openSetting()
							}
						}
					})
				} else {
					uni.showToast({
						title: '获取位置失败',
						icon: 'none'
					})
				}
				
				reject(err)
			}
		})
	})
}

/**
 * 逆地理编码：根据经纬度获取地址
 * @param {Number} latitude 纬度
 * @param {Number} longitude 经度
 * @returns {Promise<String>} 地址信息
 */
export function getAddressFromLocation(latitude, longitude) {
	return new Promise((resolve, reject) => {
		// 使用腾讯地图逆地理编码API
		// 注意：实际使用需要申请腾讯地图API Key
		// 这里提供一个简化版本
		
		uni.request({
			url: 'https://apis.map.qq.com/ws/geocoder/v1/',
			data: {
				location: `${latitude},${longitude}`,
				key: 'YOUR_TENCENT_MAP_KEY', // 需要替换为实际的key
				get_poi: 0
			},
			success: (res) => {
				if (res.data.status === 0) {
					const result = res.data.result
					const address = result.address || result.formatted_addresses?.recommend || '未知位置'
					resolve(address)
				} else {
					reject(new Error('逆地理编码失败'))
				}
			},
			fail: (err) => {
				reject(err)
			}
		})
	})
}

/**
 * 选择位置（打开地图选择）
 * @returns {Promise} 返回选择的位置信息
 */
export function chooseLocation() {
	return new Promise((resolve, reject) => {
		uni.chooseLocation({
			success: (res) => {
				console.log('[Location] 选择位置成功:', res)
				resolve({
					name: res.name,
					address: res.address,
					latitude: res.latitude,
					longitude: res.longitude
				})
			},
			fail: (err) => {
				console.error('[Location] 选择位置失败:', err)
				reject(err)
			}
		})
	})
}

/**
 * 计算两点之间的距离（单位：米）
 * 使用 Haversine 公式
 * @param {Number} lat1 纬度1
 * @param {Number} lon1 经度1
 * @param {Number} lat2 纬度2
 * @param {Number} lon2 经度2
 * @returns {Number} 距离（米）
 */
export function calculateDistance(lat1, lon1, lat2, lon2) {
	const R = 6371e3 // 地球半径（米）
	const φ1 = lat1 * Math.PI / 180
	const φ2 = lat2 * Math.PI / 180
	const Δφ = (lat2 - lat1) * Math.PI / 180
	const Δλ = (lon2 - lon1) * Math.PI / 180

	const a = Math.sin(Δφ / 2) * Math.sin(Δφ / 2) +
		Math.cos(φ1) * Math.cos(φ2) *
		Math.sin(Δλ / 2) * Math.sin(Δλ / 2)
	const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

	const distance = R * c
	return Math.round(distance)
}

/**
 * 格式化距离显示
 * @param {Number} distance 距离（米）
 * @returns {String} 格式化后的距离
 */
export function formatDistance(distance) {
	if (distance < 1000) {
		return `${distance}米`
	} else {
		return `${(distance / 1000).toFixed(1)}公里`
	}
}

/**
 * 计算用户到餐厅的距离
 * @param {Number} userLat 用户纬度
 * @param {Number} userLon 用户经度
 * @returns {Object} 包含距离和格式化距离的对象
 */
export function getDistanceToRestaurant(userLat, userLon) {
	const distance = calculateDistance(
		userLat,
		userLon,
		RESTAURANT_INFO.latitude,
		RESTAURANT_INFO.longitude
	)
	
	return {
		distance,
		formatted: formatDistance(distance)
	}
}

/**
 * 打开地图导航到餐厅
 * @param {String} type 导航类型 'default'|'wechat'|'amap'|'baidu'
 */
export function navigateToRestaurant(type = 'default') {
	// #ifdef MP-WEIXIN
	// 检查是否在开发工具中
	const systemInfo = uni.getSystemInfoSync()
	if (systemInfo.platform === 'devtools') {
		uni.showModal({
			title: '提示',
			content: '地图导航功能需要在真机上测试，开发工具中无法使用',
			showCancel: false
		})
		return
	}
	// #endif
	
	if (type === 'default') {
		// 使用系统默认地图
		uni.openLocation({
			latitude: RESTAURANT_INFO.latitude,
			longitude: RESTAURANT_INFO.longitude,
			name: RESTAURANT_INFO.name,
			address: RESTAURANT_INFO.address,
			scale: 15,
			success: () => {
				console.log('[Location] 打开地图成功')
			},
			fail: (err) => {
				console.error('[Location] 打开地图失败:', err)
				uni.showModal({
					title: '打开地图失败',
					content: '请在真机上测试地图功能，或检查小程序权限配置',
					showCancel: false
				})
			}
		})
	} else {
		// 第三方地图导航
		const lat = RESTAURANT_INFO.latitude
		const lon = RESTAURANT_INFO.longitude
		const name = encodeURIComponent(RESTAURANT_INFO.name)
		const address = encodeURIComponent(RESTAURANT_INFO.address)
		
		let url = ''
		
		switch (type) {
			case 'wechat':
				// 微信内置地图
				url = `https://apis.map.qq.com/uri/v1/marker?marker=coord:${lat},${lon};title:${name};addr:${address}&referer=myapp`
				break
			case 'amap':
				// 高德地图
				url = `https://uri.amap.com/marker?position=${lon},${lat}&name=${name}&src=myapp&coordinate=gaode&callnative=1`
				break
			case 'baidu':
				// 百度地图
				url = `https://api.map.baidu.com/marker?location=${lat},${lon}&title=${name}&content=${address}&output=html&src=myapp`
				break
		}
		
		if (url) {
			// #ifdef H5
			window.location.href = url
			// #endif
			
			// #ifdef MP-WEIXIN
			// 小程序中复制链接，提示用户在浏览器打开
			uni.setClipboardData({
				data: url,
				success: () => {
					uni.showModal({
						title: '提示',
						content: '导航链接已复制，请在浏览器中打开',
						showCancel: false
					})
				}
			})
			// #endif
		}
	}
}

/**
 * 获取餐厅信息
 * @returns {Object} 餐厅信息
 */
export function getRestaurantInfo() {
	return { ...RESTAURANT_INFO }
}

/**
 * 更新餐厅信息（用于配置）
 * @param {Object} info 餐厅信息
 */
export function updateRestaurantInfo(info) {
	Object.assign(RESTAURANT_INFO, info)
}

/**
 * 检查是否授权位置权限
 * @returns {Promise<Boolean>} 是否已授权
 */
export function checkLocationAuth() {
	return new Promise((resolve) => {
		uni.getSetting({
			success: (res) => {
				const authStatus = res.authSetting['scope.userLocation']
				console.log('[Location] 位置权限状态:', authStatus)
				resolve(authStatus === true)
			},
			fail: () => {
				resolve(false)
			}
		})
	})
}

/**
 * 请求位置权限
 * @returns {Promise<Boolean>} 是否授权成功
 */
export function requestLocationAuth() {
	return new Promise((resolve) => {
		uni.authorize({
			scope: 'scope.userLocation',
			success: () => {
				console.log('[Location] 位置权限授权成功')
				resolve(true)
			},
			fail: () => {
				console.log('[Location] 位置权限授权失败')
				uni.showModal({
					title: '需要位置权限',
					content: '请在设置中开启位置权限，以便为您提供更好的服务',
					confirmText: '去设置',
					success: (res) => {
						if (res.confirm) {
							uni.openSetting()
						}
					}
				})
				resolve(false)
			}
		})
	})
}

export default {
	getUserLocation,
	chooseLocation,
	calculateDistance,
	formatDistance,
	getDistanceToRestaurant,
	navigateToRestaurant,
	getRestaurantInfo,
	updateRestaurantInfo,
	checkLocationAuth,
	requestLocationAuth,
	getAddressFromLocation
}
