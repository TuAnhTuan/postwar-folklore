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
import { onMounted } from 'vue'

const authStore = useAuthStore()
onMounted(() => authStore.init())
</script>
