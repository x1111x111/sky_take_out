<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import Admin from './views/Admin.vue'
import Customer from './views/Customer.vue'
import { api, session, clearSession } from './api/client.js'
import { route, go, startRouter } from './router.js'
const error=ref(''),busy=ref(false),username=ref(''),password=ref(''),code=ref('')
const role=computed(()=>route.value.startsWith('/user')?'user':'admin')
const logged=computed(()=>!!session[role.value]?.token)
let stop
onMounted(()=>{stop=startRouter();window.addEventListener('sky-error',showError)})
onUnmounted(()=>{stop?.();window.removeEventListener('sky-error',showError)})
function showError(e){error.value=e.detail}
async function login(){busy.value=true;error.value='';try{const data=await api(role.value,'POST',role.value==='admin'?'/employee/login':'/user/login',role.value==='admin'?{username:username.value,password:password.value}:{code:code.value});session[role.value]=role.value==='admin'?data:{token:data,name:'微信用户'};localStorage.setItem('sky.'+role.value,JSON.stringify(session[role.value]));go(role.value==='admin'?'/admin/dashboard':'/user/menu')}catch{}finally{busy.value=false}}
</script>
<template>
<div v-if="error" class="alert" role="alert">{{error}}<button @click="error=''" aria-label="关闭提示">×</button></div>
<div v-if="!logged" class="login"><section class="login-art"><span class="wordmark">◒ 苍穹外卖</span><h1>好味道，<br>即刻抵达。</h1><p>一餐一饭，用心相伴</p><div class="plate">🍜</div></section><form class="login-form" @submit.prevent="login"><span class="eyebrow">SKY TAKE OUT</span><h2>{{role==='admin'?'商家管理中心':'欢迎来到苍穹外卖'}}</h2><p class="muted">{{role==='admin'?'登录账号，开启今日营业':'使用现有后端的模拟微信凭证登录'}}</p><template v-if="role==='admin'"><label>用户名<input v-model="username" required autocomplete="username" placeholder="管理员账号"></label><label>密码<input v-model="password" type="password" required autocomplete="current-password" placeholder="请输入密码"></label></template><label v-else>登录凭证<input v-model="code" required autocomplete="username" placeholder="固定凭证可再次登录同一账户"></label><button class="primary wide" :disabled="busy">{{busy?'正在登录…':'登录'}}</button><a :href="role==='admin'?'#/user/menu':'#/admin/dashboard'">切换到{{role==='admin'?'用户点餐':'商家管理'}} →</a></form></div>
<div v-else :class="['app',role]"><aside><a class="wordmark" :href="role==='admin'?'#/admin/dashboard':'#/user/menu'">◒ 苍穹外卖</a><small>{{role==='admin'?'商家管理中心':'美味随时在线'}}</small><nav><template v-if="role==='admin'"><a v-for="n in [['dashboard','工作台'],['orders','订单管理'],['dish','菜品管理'],['setmeal','套餐管理'],['category','分类管理'],['employee','员工管理'],['reports','数据统计']]" :key="n[0]" :href="'#/admin/'+n[0]" :class="{active:route==='/admin/'+n[0]}">{{n[1]}}</a></template><template v-else><a v-for="n in [['menu','在线点餐'],['cart','购物车'],['orders','我的订单'],['addresses','收货地址']]" :key="n[0]" :href="'#/user/'+n[0]" :class="{active:route==='/user/'+n[0]}">{{n[1]}}</a></template></nav><div class="aside-bottom"><a :href="role==='admin'?'#/user/menu':'#/admin/dashboard'">切换{{role==='admin'?'用户端':'管理端'}} ↗</a><button @click="clearSession(role)">退出登录</button></div></aside><div class="workspace"><header><span>{{role==='admin'?'苍穹外卖 · 运营平台':'苍穹外卖 · 在线点餐'}}</span><span class="muted">{{session[role]?.name || session[role]?.username}} <b class="avatar">{{role==='admin'?'商':'我'}}</b> <button @click="clearSession(role)">退出</button></span></header><main><Admin v-if="role==='admin'" :key="route"/><Customer v-else :key="route"/></main><footer>苍穹外卖 · 每一餐，都值得用心</footer></div></div>
</template>
