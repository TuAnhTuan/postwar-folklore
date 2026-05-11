import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import './assets/main.css'
import { useAuthStore } from '@/stores/auth'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)

// Khởi động auth listener TRƯỚC khi router xử lý navigation đầu tiên
// Đảm bảo isAdmin đã được set khi router guard chạy
const authStore = useAuthStore()
authStore.init()

app.use(router)
app.mount('#app')
