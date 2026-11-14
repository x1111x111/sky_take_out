<script setup>defineProps(['page','total','size']);defineEmits(['change'])</script>
<template><div class="pager"><span class="muted">共 {{total||0}} 条</span><button :disabled="page<=1" @click="$emit('change',page-1)">上一页</button><span>{{page}} / {{Math.max(1,Math.ceil((total||0)/size))}}</span><button :disabled="page*size>=total" @click="$emit('change',page+1)">下一页</button></div></template>
