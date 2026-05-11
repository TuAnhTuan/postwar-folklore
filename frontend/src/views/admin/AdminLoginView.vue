<template>
  <main class="login-page">
    <div class="login-box">
      <div class="login-logo">⚔</div>
      <h1 class="login-title">Admin Portal</h1>
      <p class="login-subtitle">Truyền Thuyết Hậu Chiến</p>

      <div class="form-group">
        <button class="btn-google" @click="loginGoogle" :disabled="loading">
          <svg width="18" height="18" viewBox="0 0 18 18">
            <path fill="#4285F4" d="M17.64 9.2c0-.637-.057-1.251-.164-1.84H9v3.481h4.844c-.209 1.125-.843 2.078-1.796 2.717v2.258h2.908c1.702-1.567 2.684-3.875 2.684-6.615z"/>
            <path fill="#34A853" d="M9 18c2.43 0 4.467-.806 5.956-2.18l-2.908-2.259c-.806.54-1.837.86-3.048.86-2.344 0-4.328-1.584-5.036-3.711H.957v2.332C2.438 15.983 5.482 18 9 18z"/>
            <path fill="#FBBC05" d="M3.964 10.71c-.18-.54-.282-1.117-.282-1.71s.102-1.17.282-1.71V4.958H.957C.347 6.173 0 7.548 0 9s.348 2.827.957 4.042l3.007-2.332z"/>
            <path fill="#EA4335" d="M9 3.58c1.321 0 2.508.454 3.44 1.345l2.582-2.58C13.463.891 11.426 0 9 0 5.482 0 2.438 2.017.957 4.958L3.964 7.29C4.672 5.163 6.656 3.58 9 3.58z"/>
          </svg>
          Đăng nhập với Google
        </button>
      </div>

      <div class="divider">hoặc</div>

      <div class="form-group">
        <input v-model="email" type="email" class="form-input" placeholder="Email" />
      </div>
      <div class="form-group">
        <input v-model="password" type="password" class="form-input" placeholder="Mật khẩu"
               @keydown.enter="loginEmail" />
      </div>
      <button class="btn-submit" @click="loginEmail" :disabled="loading">
        {{ loading ? 'Đang đăng nhập...' : 'Đăng nhập' }}
      </button>

      <p v-if="error" class="error-msg">{{ error }}</p>
    </div>
  </main>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router    = useRouter()
const authStore = useAuthStore()
const email     = ref('')
const password  = ref('')
const loading   = ref(false)
const error     = ref('')

// Redirect result được xử lý toàn cục trong App.vue
// AdminLoginView chỉ cần lưu returnTo trước khi redirect

async function loginGoogle() {
  loading.value = true; error.value = ''
  // Lưu returnTo để App.vue biết redirect vào /admin sau khi đăng nhập xong
  sessionStorage.setItem('returnTo', '/admin/login')
  await authStore.loginWithGoogle()
  // Browser redirect → không có code nào chạy tiếp
}

async function loginEmail() {
  if (!email.value || !password.value) { error.value = 'Vui lòng nhập đầy đủ thông tin.'; return }
  loading.value = true; error.value = ''
  try {
    await authStore.loginWithEmail(email.value, password.value)

    // Chờ onAuthStateChanged cập nhật isAdmin — check ngay nếu đã xong
    await new Promise(resolve => {
      if (!authStore.loading) { resolve(); return }
      const stop = authStore.$subscribe(() => {
        if (!authStore.loading) { stop(); resolve() }
      })
      setTimeout(resolve, 5000)
    })

    if (authStore.isAdmin) router.push('/admin')
    else error.value = 'Tài khoản này không có quyền Admin.'
  } catch (e) { error.value = 'Email hoặc mật khẩu không đúng.' }
  finally { loading.value = false }
}
</script>

<style scoped>
.login-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; padding: 2rem; }
.login-box { background: var(--bg-secondary); border: 1px solid var(--border); border-radius: 16px; padding: 3rem; width: 100%; max-width: 400px; }
.login-logo { font-size: 2.5rem; text-align: center; margin-bottom: 1rem; }
.login-title { font-family: 'Playfair Display', serif; font-size: 1.5rem; font-weight: 700; text-align: center; margin-bottom: 0.25rem; }
.login-subtitle { font-size: 0.8rem; color: var(--text-muted); text-align: center; margin-bottom: 2rem; }
.form-group { margin-bottom: 0.75rem; }
.form-input { width: 100%; padding: 10px 14px; background: var(--bg-card); border: 1px solid var(--border); border-radius: 8px; color: var(--text-primary); font-size: 0.875rem; font-family: 'Inter', sans-serif; outline: none; transition: border-color 0.2s; }
.form-input:focus { border-color: var(--accent); }
.form-input::placeholder { color: var(--text-muted); }
.btn-google { width: 100%; display: flex; align-items: center; justify-content: center; gap: 10px; padding: 12px; border: 1px solid var(--border); border-radius: 8px; background: var(--bg-card); color: var(--text-primary); font-size: 0.875rem; cursor: pointer; font-family: 'Inter', sans-serif; transition: all 0.2s; }
.btn-google:hover:not(:disabled) { border-color: var(--accent); }
.divider { display: flex; align-items: center; gap: 1rem; color: var(--text-muted); font-size: 0.75rem; margin: 1rem 0; }
.divider::before, .divider::after { content: ''; flex: 1; height: 1px; background: var(--border); }
.btn-submit { width: 100%; padding: 12px; background: linear-gradient(135deg, var(--accent), #a07840); color: #0a0a0f; border: none; border-radius: 8px; font-size: 0.875rem; font-weight: 600; cursor: pointer; font-family: 'Inter', sans-serif; margin-top: 0.5rem; transition: all 0.2s; }
.btn-submit:hover:not(:disabled) { transform: translateY(-1px); }
.btn-submit:disabled { opacity: 0.6; cursor: not-allowed; }
.error-msg { margin-top: 1rem; font-size: 0.8rem; color: #d97272; text-align: center; }
</style>
