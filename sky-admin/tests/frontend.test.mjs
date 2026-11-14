import {test,after} from 'node:test'
import assert from 'node:assert/strict'
import {createServer} from 'vite'
import {createSSRApp} from 'vue'
import {renderToString} from 'vue/server-renderer'
import {toSavePayload} from '../src/api/contracts.js'

const storage=new Map()
globalThis.localStorage={getItem:k=>storage.get(k)||null,setItem:(k,v)=>storage.set(k,v),removeItem:k=>storage.delete(k)}
globalThis.location={hash:'#/admin/dashboard'}
globalThis.window=new EventTarget()
const server=await createServer({server:{middlewareMode:true},appType:'custom'})
after(()=>server.close())
const client=await server.ssrLoadModule('/src/api/client.js')
const router=await server.ssrLoadModule('/src/router.js')
const {default:App}=await server.ssrLoadModule('/src/App.vue')
let lastRequest
const reply=(data,code=1,msg='success')=>{globalThis.fetch=async(url,options)=>{lastRequest={url,options};return{ok:true,status:200,json:async()=>({code,data,msg})}}}

test('查询编码保留 0，忽略空值，转义特殊字符',()=>{assert.equal(client.queryString({page:1,status:0,name:'鱼 & 饭',empty:'',nil:null}), 'page=1&status=0&name=%E9%B1%BC+%26+%E9%A5%AD')})
test('管理端登录请求不使用用户 Token，响应数据正确解包',async()=>{client.session.admin=null;client.session.user={token:'user-only'};reply({token:'new-admin'});assert.deepEqual(await client.api('admin','POST','/employee/login',{username:'admin',password:'secret'}),{token:'new-admin'});assert.equal(lastRequest.options.headers.token,undefined);assert.equal(lastRequest.url,'/api/admin/employee/login');assert.equal(JSON.parse(lastRequest.options.body).username,'admin')})
test('用户 API 严格使用用户 Token 与后端参数名',async()=>{client.session.admin={token:'admin-only'};client.session.user={token:'user-only'};reply([]);await client.api('user','GET','/dish/list',undefined,{categoryId:12});assert.equal(lastRequest.options.headers.token,'user-only');assert.equal(lastRequest.url,'/api/user/dish/list?categoryId=12');assert.equal(lastRequest.options.body,undefined)})
test('管理状态切换通过查询参数传 id',async()=>{reply(null);await client.api('admin','POST','/dish/status/1',undefined,{id:7});assert.equal(lastRequest.url,'/api/admin/dish/status/1?id=7');assert.equal(lastRequest.options.headers.token,'admin-only')})
test('业务失败抛出中文提示并清除对应失效会话',async()=>{reply(null,0,'TOKEN_INVALID');await assert.rejects(client.api('user','GET','/order/historyOrders'),/登录已失效/);assert.equal(client.session.user,null);assert.equal(client.session.admin.token,'admin-only')})
test('上传 FormData 不强制设置 Content-Type',async()=>{reply('/uploads/test.png');const data=new FormData();data.append('file',new Blob(['image']),'test.png');await client.api('admin','POST','/common/upload',data);assert.equal(lastRequest.options.headers['Content-Type'],undefined);assert.equal(lastRequest.options.body,data)})
test('路由拒绝未知路径',()=>{assert.equal(router.normalizeRoute('#/user/orders'),'/user/orders');assert.equal(router.normalizeRoute('#/unknown'),'/admin/dashboard')})
test('登录页面分别渲染管理员表单与模拟微信凭证表单',async()=>{client.session.admin=null;client.session.user=null;router.route.value='/admin/dashboard';let html=await renderToString(createSSRApp(App));assert.match(html,/autocomplete="current-password"/);router.route.value='/user/menu';html=await renderToString(createSSRApp(App));assert.match(html,/登录凭证/);assert.doesNotMatch(html,/autocomplete="current-password"/)})
for(const [path,title] of [['/admin/dashboard','工作台'],['/admin/orders','订单管理'],['/admin/dish','菜品管理'],['/admin/setmeal','套餐管理'],['/admin/category','分类管理'],['/admin/employee','员工管理'],['/admin/reports','数据统计'],['/user/menu','今天，想吃点什么'],['/user/cart','购物车与结算'],['/user/orders','我的订单'],['/user/addresses','收货地址']]){
 test('页面可渲染：'+path,async()=>{client.session.admin={token:'a',name:'管理员'};client.session.user={token:'u',name:'用户'};router.route.value=path;const html=await renderToString(createSSRApp(App));assert.ok(html.includes(title));assert.doesNotMatch(html,/HelloWorld|Vite logo/)})
}
test('支付状态 3 明确显示待退款',()=>assert.equal(client.payStates[3],'待退款'))
test('管理表单严格按 DTO 发送字段，不混入其他资源属性',()=>{
 const form={id:9,name:'午餐',username:'staff',phone:'13800000000',sex:'1',idNumber:'123',categoryId:3,price:28,flavors:[],setmealDishes:[],type:1,sort:2,status:0}
 assert.deepEqual(Object.keys(toSavePayload('employee',form)),['id','username','name','phone','sex','idNumber'])
 assert.deepEqual(Object.keys(toSavePayload('category',form)),['id','name','type','sort'])
 assert.ok(!('setmealDishes' in toSavePayload('dish',form)))
 assert.ok(!('flavors' in toSavePayload('setmeal',form)))
})

function jwt(payload) { return 'header.'+Buffer.from(JSON.stringify(payload)).toString('base64url')+'.signature' }
test('恢复登录时静默移除过期、损坏及角色错误的缓存',()=>{
 for(const token of ['broken',jwt({exp:1,role:'USER'}),jwt({exp:Date.now()/1000+3600,role:'ADMIN'})]){
  localStorage.setItem('sky.user',JSON.stringify({token}))
  assert.equal(client.restoreSession('user'),null)
  assert.equal(localStorage.getItem('sky.user'),null)
 }
 const saved={token:jwt({exp:Date.now()/1000+3600,role:'USER'})}
 localStorage.setItem('sky.user',JSON.stringify(saved))
 assert.deepEqual(client.restoreSession('user'),saved)
})
test('重新登录不携带旧 Token',async()=>{
 client.session.user={token:'old-token'};reply('new-token')
 await client.api('user','POST','/user/login',{code:'existing-user'})
 assert.equal(lastRequest.options.headers.token,undefined)
})
test('并发过期请求只提示一次，旧请求不会清除新会话',async()=>{
 const pending=[],messages=[]
 const listener=e=>messages.push(e.detail)
 window.addEventListener('sky-error',listener)
 globalThis.fetch=()=>new Promise(resolve=>pending.push(resolve))
 const failure={ok:true,status:200,json:async()=>({code:0,msg:'TOKEN_INVALID'})}
 try{
  client.session.user={token:'expired'}
  const requests=Promise.allSettled([client.api('user','GET','/category/list'),client.api('user','GET','/shop/status')])
  pending.splice(0).forEach(resolve=>resolve(failure));await requests
  assert.deepEqual(messages,['登录已失效，请重新登录'])
  client.session.user={token:'older'}
  const stale=client.api('user','GET','/shop/status').catch(()=>{})
  client.session.user={token:'new-login'}
  pending.shift()(failure);await stale
  assert.equal(client.session.user.token,'new-login')
  assert.equal(messages.length,1)
 }finally{window.removeEventListener('sky-error',listener)}
})
