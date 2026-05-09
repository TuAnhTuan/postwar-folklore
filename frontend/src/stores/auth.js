import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
  auth, googleProvider,
  signInWithPopup, signInWithEmailAndPassword,
  signOut, onAuthStateChanged
} from '@/utils/firebase'

export const useAuthStore = defineStore('auth', () => {
  const user     = ref(null)
  const loading  = ref(true)
  const isAdmin  = ref(false)

  const isLoggedIn = computed(() => !!user.value)
  const displayName = computed(() => user.value?.displayName || user.value?.email || 'Người dùng')

  function init() {
    onAuthStateChanged(auth, async (firebaseUser) => {
      if (firebaseUser) {
        user.value = firebaseUser
        // Lấy custom claims để check admin
        const tokenResult = await firebaseUser.getIdTokenResult()
        isAdmin.value = tokenResult.claims.admin === true
      } else {
        user.value = null
        isAdmin.value = false
      }
      loading.value = false
    })
  }

  async function loginWithGoogle() {
    const result = await signInWithPopup(auth, googleProvider)
    return result.user
  }

  async function loginWithEmail(email, password) {
    const result = await signInWithEmailAndPassword(auth, email, password)
    return result.user
  }

  async function logout() {
    await signOut(auth)
    user.value = null
    isAdmin.value = false
  }

  async function getToken() {
    return auth.currentUser ? await auth.currentUser.getIdToken() : null
  }

  return {
    user, loading, isAdmin, isLoggedIn, displayName,
    init, loginWithGoogle, loginWithEmail, logout, getToken
  }
})
