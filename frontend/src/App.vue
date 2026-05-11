<template>
  <NavBar />
  <router-view v-slot="{ Component }">
    <transition name="page" mode="out-in">
      <component :is="Component" />
    </transition>
  </router-view>
  <AiChatWidget />
</template>

<script setup>
import NavBar from '@/components/NavBar.vue'
import AiChatWidget from '@/components/AiChatWidget.vue'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'
import { onMounted } from 'vue'

const authStore = useAuthStore()
const router    = useRouter()

onMounted(async () => {
  // init() đã được gọi sớm trong main.js
  // Chỉ cần xử lý Google redirect result ở đây
  const user = await authStore.handleRedirectResult()
  if (user) {
    // Chờ onAuthStateChanged cập nhật isAdmin
    await new Promise(resolve => {
      if (!authStore.loading) { resolve(); return }
      const stop = authStore.$subscribe(() => {
        if (!authStore.loading) { stop(); resolve() }
      })
      setTimeout(resolve, 5000)
    })

    const returnTo = sessionStorage.getItem('returnTo')
    sessionStorage.removeItem('returnTo')

    if (authStore.isAdmin && returnTo === '/admin/login') {
      router.push('/admin')
    } else if (returnTo) {
      router.push(returnTo)
    }
  }
})
</script>
