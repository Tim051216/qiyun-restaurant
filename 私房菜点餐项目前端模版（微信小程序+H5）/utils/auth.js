/**
 * 认证工具类
 */

const TOKEN_KEY = 'token'
const MEMBER_KEY = 'member'

/**
 * 获取token
 */
export function getToken() {
  return uni.getStorageSync(TOKEN_KEY)
}

/**
 * 设置token
 */
export function setToken(token) {
  return uni.setStorageSync(TOKEN_KEY, token)
}

/**
 * 移除token
 */
export function removeToken() {
  return uni.removeStorageSync(TOKEN_KEY)
}

/**
 * 获取会员信息
 */
export function getMember() {
  const memberStr = uni.getStorageSync(MEMBER_KEY)
  return memberStr ? JSON.parse(memberStr) : null
}

/**
 * 设置会员信息
 */
export function setMember(member) {
  return uni.setStorageSync(MEMBER_KEY, JSON.stringify(member))
}

/**
 * 移除会员信息
 */
export function removeMember() {
  return uni.removeStorageSync(MEMBER_KEY)
}

/**
 * 检查是否已登录
 */
export function isLoggedIn() {
  return !!getToken()
}

/**
 * 清除所有认证信息
 */
export function clearAuth() {
  removeToken()
  removeMember()
}
