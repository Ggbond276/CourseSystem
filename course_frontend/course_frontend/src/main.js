import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import 'element-plus/dist/index.css'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
// 使用中文语言包，让 Element Plus 的内置提示/分页全部显示中文
app.use(ElementPlus, { locale: zhCn })
app.mount('#app')
