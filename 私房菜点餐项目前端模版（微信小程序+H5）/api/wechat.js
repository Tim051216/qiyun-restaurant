import request from '../utils/request.js'

/**
 * 微信登录
 * @param {String} code - 微信登录凭证
 * @param {Object} userInfo - 用户信息（可选）
 */
export function wechatLogin(code, userInfo = null) {
  const data = { code }
  
  // 如果有用户信息，添加到请求数据中
  if (userInfo) {
    data.userInfo = userInfo
  }
  
  return request({
    url: '/wechat/login',
    method: 'POST',
    data
  })
}
