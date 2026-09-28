import Vue from 'vue';
import VueRouter from 'vue-router';
import * as echarts from 'echarts';
import axios from 'axios';


import App from './App.vue';
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
import './assets/css/common.css';
import './assets/css/advice.css';
import '@/assets/font/font.css';
import "@/js/rem";

import Router from './router';


// 使用 Vue 插件
Vue.use(VueRouter);
Vue.use(ElementUI);


// 全局挂载 axios 和 echarts
Vue.prototype.$axios = axios;
Vue.prototype.$echarts = echarts;

Vue.config.productionTip = false;

new Vue({
  render: h => h(App),
  router: Router, // 绑定路由
}).$mount('#app');
