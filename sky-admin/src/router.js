import { ref } from 'vue'
const valid=new Set(['/admin/dashboard','/admin/orders','/admin/dish','/admin/setmeal','/admin/category','/admin/employee','/admin/reports','/user/menu','/user/cart','/user/orders','/user/addresses'])
export const normalizeRoute=hash=>valid.has(hash.replace(/^#/,''))?hash.replace(/^#/,''):'/admin/dashboard'
export const route=ref(normalizeRoute(location.hash))
export const go=path=>{location.hash=path;route.value=normalizeRoute(path)}
export function startRouter(){const sync=()=>{route.value=normalizeRoute(location.hash)};window.addEventListener('hashchange',sync);sync();return()=>window.removeEventListener('hashchange',sync)}
