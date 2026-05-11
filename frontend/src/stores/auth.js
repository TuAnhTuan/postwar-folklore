import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import {
  auth, googleProvider,
  signInWithPopup, signInWithRedirect, getRedirectResult,
  signInWithEmailAndPassword,
  signOut, onAuthStateChanged
} from '@/utils/firebase'

export const useAuthStore = defineStore('auth', () => {
  const user     = ref(null)
  const loading  = ref(true)
  const isAdmin  = ref(false)

  const isLoggedIn = computed(() => !!user.value)
  const displayName = computed(() => user.value?.displayName || user.value?.email || 'Người dùng')

  function init() {
    return new Promise(resolve => {
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
        resolve() // báo hiệu auth state đã được xác định lần đầu
      })
    })
  }

  // Dùng cho NavBar (end user) — popup, không cần Firebase Hosting
  async function loginWithGooglePopup() {
    const result = await signInWithPopup(auth, googleProvider)
    return result.user
  }

  // Dùng cho Admin login page — redirect (full page)
  async function loginWithGoogle() {
    await signInWithRedirect(auth, googleProvider)
  }

  async function handleRedirectResult() {
    try {
      const result = await getRedirectResult(auth)
      return result?.user ?? null
    } catch (e) {
      console.warn('Google redirect result error:', e.message)
      return null
    }
  }

  async function loginWithEmail(email, password) {
    loading.value = true
    const result = await signInWithEmailAndPassword(auth, email, password)
    // onAuthStateChanged sẽ tự fire và set loading = false + isAdmin
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

  // Dùng sau khi admin claim được set — force refresh token để lấy claim mới
  async function refreshClaims() {
    if (!auth.currentUser) return
    const tokenResult = await auth.currentUser.getIdTokenResult(true) // true = force refresh
    isAdmin.value = tokenResult.claims.admin === true
  }

  return {
    user, loading, isAdmin, isLoggedIn, displayName,
    init, loginWithGooglePopup, loginWithGoogle, handleRedirectResult,
    loginWithEmail, logout, getToken, refreshClaims
  }
})
