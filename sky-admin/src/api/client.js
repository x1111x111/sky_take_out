import { reactive } from 'vue'

// This is only a local expiry check; the backend still verifies signatures.
export function restoreSession(role) {
  const key = 'sky.' + role
  try {
    const saved = JSON.parse(localStorage.getItem(key))
    const parts = saved?.token?.split('.')
    if (parts?.length !== 3) throw new Error('Invalid cached token')
    const payload = JSON.parse(atob(parts[1].replaceAll('-', '+').replaceAll('_', '/')))
    if (!Number.isFinite(payload.exp) || payload.exp * 1000 <= Date.now()
      || payload.role !== role.toUpperCase()) throw new Error('Expired cached session')
    return saved
  } catch {
    localStorage.removeItem(key)
    return null
  }
}

export const session = reactive({ admin: restoreSession('admin'), user: restoreSession('user') })
export function clearSession(role) {
  session[role] = null
  localStorage.removeItem('sky.' + role)
}
export const money = value => '¥' + Number(value || 0).toFixed(2)
export const orderStates = ['', '待付款', '待接单', '已接单', '派送中', '已完成', '已取消']
export const payStates = ['未支付', '已支付', '已退款', '待退款']
export function queryString(params = {}) {
  const q = new URLSearchParams()
  Object.entries(params).forEach(([k, v]) => {
    if (v !== undefined && v !== null && v !== '') q.set(k, v)
  })
  return q.toString()
}

export async function api(role, method, path, data, params) {
  const controller = new AbortController()
  const timer = setTimeout(() => controller.abort(), 20000)
  const isLogin = path === '/employee/login' || path === '/user/login'
  const token = isLogin ? undefined : session[role]?.token
  let notify = true
  function expired() {
    // Concurrent failures from an old session must not clear a new login.
    if (token && session[role]?.token === token) clearSession(role)
    else notify = false
    return new Error('登录已失效，请重新登录')
  }
  try {
    const q = queryString(params), form = data instanceof FormData
    const response = await fetch((import.meta.env.VITE_API_BASE || '/api') + '/' + role + path + (q ? '?' + q : ''), {
      method, signal: controller.signal,
      headers: {
        ...(token ? { token } : {}),
        ...(!form && data !== undefined ? { 'Content-Type': 'application/json' } : {}),
      },
      body: data === undefined ? undefined : form ? data : JSON.stringify(data),
    })
    if (response.status === 401 && !isLogin) throw expired()
    if (!response.ok) throw new Error('请求失败（' + response.status + '），请检查服务和输入')
    const result = await response.json()
    if (result.code !== 1) {
      if (!isLogin && ['NOT_LOGIN', 'TOKEN_INVALID'].includes(result.msg)) throw expired()
      throw new Error(result.msg || '操作失败')
    }
    return result.data
  } catch (e) {
    const message = e.name === 'AbortError' ? '请求超时，请刷新确认结果'
      : e.message === 'Failed to fetch' ? '无法连接后端，请检查服务是否启动' : e.message
    if (notify) window.dispatchEvent(new CustomEvent('sky-error', { detail: message }))
    throw e
  } finally {
    clearTimeout(timer)
  }
}
