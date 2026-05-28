/**
 * 餐厅配置信息
 * 请根据实际情况修改以下信息
 */

export default {
	// 餐厅基本信息
	name: '南京林业大学私房菜餐厅',
	address: '江苏省南京市玄武区龙蟠路159号南京林业大学',
	phone: '025-85427777',
	
	// 餐厅位置坐标（必须填写）
	// 南京林业大学坐标
	location: {
		latitude: 32.0833,   // 纬度
		longitude: 118.8167  // 经度
	},
	
	// 营业时间
	businessHours: {
		weekday: '10:00-22:00',
		weekend: '09:00-23:00'
	},
	
	// 配送范围（公里）
	deliveryRange: 5,
	
	// 起送价格（元）
	minDeliveryPrice: 30,
	
	// 配送费（元）
	deliveryFee: 5
}
