<template>
  <nav class="navbar">
    <router-link to="/" class="nav-logo">
      <img src="/logo-hoai-tu.png" alt="Hoài tự" class="logo-img" />
      <span class="logo-text">Hoài tự</span>
    </router-link>

    <ul class="nav-links">
      <li><router-link to="/">Trang chủ</router-link></li>
      <li><router-link to="/ve-hoai-tu">Về Hoài tự</router-link></li>
      <li><router-link to="/ly-thuyet">Lý thuyết</router-link></li>
      <li><router-link to="/truyen-thuyet">Truyền thuyết</router-link></li>
    </ul>

    <div class="nav-actions">
      <template v-if="authStore.isLoggedIn">
        <!-- Avatar + Dropdown -->
        <div class="avatar-wrap" @click="showDropdown = !showDropdown" v-click-outside="() => showDropdown = false">
          <div class="avatar">
            <img v-if="avatarUrl" :src="avatarUrl" :alt="authStore.displayName" />
            <span v-else>{{ initials }}</span>
            <span class="online-dot"></span>
          </div>
          <Transition name="dropdown">
            <div v-if="showDropdown" class="dropdown">
              <div class="dropdown-user">
                <div class="dropdown-avatar">
                  <img v-if="avatarUrl" :src="avatarUrl" :alt="authStore.displayName" />
                  <span v-else>{{ initials }}</span>
                  <span class="online-dot"></span>
                </div>
                <div class="dropdown-info">
                  <span class="dropdown-name">{{ authStore.displayName }}</span>
                  <span class="dropdown-role">{{ authStore.isAdmin ? 'Admin' : 'Thành viên' }}</span>
                </div>
              </div>
              <RouterLink v-if="authStore.isAdmin" to="/admin" class="dropdown-item" @click="showDropdown = false">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/></svg>
                Admin Dashboard
              </RouterLink>
              <div class="dropdown-divider"></div>
              <button class="dropdown-item" @click="handleLogout">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/></svg>
                Đăng xuất
              </button>
            </div>
          </Transition>
        </div>
      </template>
      <template v-else>
        <button class="btn-login" @click="showLogin = true">Đăng nhập</button>
      </template>
    </div>
  </nav>

  <!-- Login Modal -->
  <Teleport to="body">
    <div v-if="showLogin" class="modal-overlay" @click.self="showLogin = false">
      <div class="modal">
        <div class="modal-header">
          <h2 class="modal-title">Đăng nhập</h2>
          <button class="modal-close" @click="showLogin = false">✕</button>
        </div>
        <div class="modal-body">
          <button class="btn-google" @click="loginGoogle">
            <svg width="18" height="18" viewBox="0 0 18 18">
              <path fill="#4285F4" d="M17.64 9.2c0-.637-.057-1.251-.164-1.84H9v3.481h4.844c-.209 1.125-.843 2.078-1.796 2.717v2.258h2.908c1.702-1.567 2.684-3.875 2.684-6.615z"/>
              <path fill="#34A853" d="M9 18c2.43 0 4.467-.806 5.956-2.18l-2.908-2.259c-.806.54-1.837.86-3.048.86-2.344 0-4.328-1.584-5.036-3.711H.957v2.332C2.438 15.983 5.482 18 9 18z"/>
              <path fill="#FBBC05" d="M3.964 10.71c-.18-.54-.282-1.117-.282-1.71s.102-1.17.282-1.71V4.958H.957C.347 6.173 0 7.548 0 9s.348 2.827.957 4.042l3.007-2.332z"/>
              <path fill="#EA4335" d="M9 3.58c1.321 0 2.508.454 3.44 1.345l2.582-2.58C13.463.891 11.426 0 9 0 5.482 0 2.438 2.017.957 4.958L3.964 7.29C4.672 5.163 6.656 3.58 9 3.58z"/>
            </svg>
            Tiếp tục với Google
          </button>
          <div class="divider">hoặc đăng nhập bằng email</div>
          <input v-model="email" type="email" class="form-input" placeholder="Email" />
          <input v-model="password" type="password" class="form-input" placeholder="Mật khẩu" @keydown.enter="loginEmail" />
          <button class="btn-submit" @click="loginEmail">Đăng nhập</button>
          <p v-if="loginError" class="error-msg">{{ loginError }}</p>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'

const authStore    = useAuthStore()
const router       = useRouter()
const showLogin    = ref(false)
const showDropdown = ref(false)
const email        = ref('')
const password     = ref('')
const loginError   = ref('')

const avatarUrl = computed(() => authStore.user?.photoURL || null)
const initials  = computed(() => {
  const name = authStore.displayName || ''
  return name.split(' ').map(w => w[0]).join('').toUpperCase().slice(0, 2)
})

// Directive click-outside
const vClickOutside = {
  mounted(el, binding) {
    el._clickOutside = (e) => { if (!el.contains(e.target)) binding.value(e) }
    document.addEventListener('click', el._clickOutside)
  },
  unmounted(el) { document.removeEventListener('click', el._clickOutside) }
}

async function handleLogout() {
  showDropdown.value = false
  await authStore.logout()
  router.push('/')
}

async function loginGoogle() {
  loginError.value = ''
  try {
    await authStore.loginWithGooglePopup()
    showLogin.value = false  // đóng modal khi đăng nhập xong
  } catch (e) {
    if (e.code !== 'auth/popup-closed-by-user') {
      loginError.value = 'Đăng nhập Google thất bại: ' + e.message
    }
  }
}

async function loginEmail() {
  loginError.value = ''
  try { await authStore.loginWithEmail(email.value, password.value); showLogin.value = false }
  catch (e) { loginError.value = 'Email hoặc mật khẩu không đúng.' }
}
</script>

<style scoped>
.navbar {
  position: fixed; top: 0; left: 0; right: 0; z-index: 100;
  display: flex; align-items: center; justify-content: space-between;
  padding: 0 3rem; height: 64px;
  background: rgba(10,10,15,0.85);
  backdrop-filter: blur(16px);
  border-bottom: 1px solid var(--border);
}
.nav-logo { display: flex; align-items: center; gap: 10px; text-decoration: none; flex-shrink: 0; }
.logo-img { width: 34px; height: 34px; border-radius: 6px; object-fit: cover; }
.logo-text { font-family: 'Playfair Display', serif; font-size: 1.05rem; font-weight: 700; color: var(--accent); white-space: nowrap; }
.nav-links { display: flex; gap: 2rem; list-style: none; }
.nav-links a { text-decoration: none; color: var(--text-secondary); font-size: 0.875rem; font-weight: 500; letter-spacing: 0.05em; text-transform: uppercase; transition: color 0.2s; position: relative; padding-bottom: 4px; }
.nav-links a::after { content: ''; position: absolute; bottom: 0; left: 0; right: 0; height: 1px; background: var(--accent); transform: scaleX(0); transition: transform 0.2s; }
.nav-links a:hover, .nav-links a.router-link-active { color: var(--accent); }
.nav-links a:hover::after, .nav-links a.router-link-active::after { transform: scaleX(1); }
.nav-actions { display: flex; align-items: center; gap: 1rem; }
.btn-login { padding: 7px 20px; border: 1px solid var(--border); border-radius: 6px; background: transparent; color: var(--text-secondary); font-size: 0.8rem; font-weight: 500; cursor: pointer; transition: all 0.2s; font-family: 'Inter', sans-serif; letter-spacing: 0.04em; text-transform: uppercase; }
.btn-login:hover { border-color: var(--accent); color: var(--accent); }

/* Avatar */
.avatar-wrap { position: relative; cursor: pointer; }
.avatar { width: 38px; height: 38px; border-radius: 50%; background: linear-gradient(135deg, var(--accent), #8b2d2d); display: flex; align-items: center; justify-content: center; font-size: 0.75rem; font-weight: 700; color: #0a0a0f; overflow: hidden; position: relative; border: 2px solid var(--border); transition: border-color 0.2s; }
.avatar:hover { border-color: var(--accent); }
.avatar img { width: 100%; height: 100%; object-fit: cover; }
.online-dot { position: absolute; bottom: 1px; right: 1px; width: 9px; height: 9px; background: #4ade80; border-radius: 50%; border: 2px solid var(--bg-secondary); }

/* Dropdown */
.dropdown { position: absolute; top: calc(100% + 10px); right: 0; min-width: 220px; background: var(--bg-secondary); border: 1px solid var(--border); border-radius: 12px; box-shadow: 0 16px 48px rgba(0,0,0,0.5); z-index: 200; overflow: hidden; }
.dropdown-user { display: flex; align-items: center; gap: 12px; padding: 1rem 1.1rem; }
.dropdown-avatar { width: 44px; height: 44px; border-radius: 50%; background: linear-gradient(135deg, var(--accent), #8b2d2d); display: flex; align-items: center; justify-content: center; font-size: 0.85rem; font-weight: 700; color: #0a0a0f; overflow: hidden; position: relative; flex-shrink: 0; }
.dropdown-avatar img { width: 100%; height: 100%; object-fit: cover; }
.dropdown-avatar .online-dot { bottom: 0; right: 0; }
.dropdown-info { display: flex; flex-direction: column; gap: 2px; min-width: 0; }
.dropdown-name { font-size: 0.875rem; font-weight: 600; color: var(--text-primary); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.dropdown-role { font-size: 0.72rem; color: var(--accent); font-weight: 500; }
.dropdown-divider { height: 1px; background: var(--border); }
.dropdown-item { width: 100%; display: flex; align-items: center; gap: 10px; padding: 0.75rem 1.1rem; background: none; border: none; color: var(--text-secondary); font-size: 0.82rem; font-family: 'Inter', sans-serif; cursor: pointer; transition: all 0.15s; text-align: left; }
.dropdown-item:hover { background: rgba(255,255,255,0.04); color: #d97272; }

/* Dropdown animation */
.dropdown-enter-active, .dropdown-leave-active { transition: all 0.15s ease; }
.dropdown-enter-from, .dropdown-leave-to { opacity: 0; transform: translateY(-6px) scale(0.98); }

/* Modal */
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.8); backdrop-filter: blur(8px); z-index: 200; display: flex; align-items: center; justify-content: center; padding: 2rem; }
.modal { background: var(--bg-secondary); border: 1px solid var(--border); border-radius: 16px; max-width: 420px; width: 100%; padding: 0; overflow: hidden; }
.modal-header { padding: 1.5rem 2rem 1rem; border-bottom: 1px solid var(--border); display: flex; justify-content: space-between; align-items: center; }
.modal-title { font-family: 'Playfair Display', serif; font-size: 1.2rem; font-weight: 700; }
.modal-close { background: none; border: 1px solid var(--border); border-radius: 6px; color: var(--text-muted); cursor: pointer; padding: 4px 10px; font-size: 1rem; transition: all 0.2s; }
.modal-close:hover { border-color: var(--accent); color: var(--accent); }
.modal-body { padding: 1.5rem 2rem; display: flex; flex-direction: column; gap: 0.75rem; }
.btn-google { display: flex; align-items: center; justify-content: center; gap: 10px; padding: 12px; border: 1px solid var(--border); border-radius: 8px; background: var(--bg-card); color: var(--text-primary); font-size: 0.875rem; cursor: pointer; font-family: 'Inter', sans-serif; transition: all 0.2s; }
.btn-google:hover { border-color: var(--accent); }
.divider { display: flex; align-items: center; gap: 1rem; color: var(--text-muted); font-size: 0.75rem; }
.divider::before, .divider::after { content: ''; flex: 1; height: 1px; background: var(--border); }
.form-input { width: 100%; padding: 10px 14px; background: var(--bg-card); border: 1px solid var(--border); border-radius: 8px; color: var(--text-primary); font-size: 0.875rem; font-family: 'Inter', sans-serif; outline: none; transition: border-color 0.2s; }
.form-input:focus { border-color: var(--accent); }
.form-input::placeholder { color: var(--text-muted); }
.btn-submit { width: 100%; padding: 12px; background: linear-gradient(135deg, var(--accent), #a07840); color: #0a0a0f; border: none; border-radius: 8px; font-size: 0.875rem; font-weight: 600; cursor: pointer; font-family: 'Inter', sans-serif; }
.error-msg { color: #d97272; font-size: 0.8rem; text-align: center; }

@media (max-width: 700px) {
  .navbar { padding: 0 1.5rem; }
  .nav-links { display: none; }
}
</style>
