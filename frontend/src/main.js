import { createApp } from 'vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import './style.css'
import App from './App.vue'
import '@fortawesome/fontawesome-free/css/all.min.css'
import router from './router/index'

createApp(App)
  .use(router)
  .use(ElementPlus)
  .mount('#app')
