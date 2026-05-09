<template>
  <main class="admin-page">
    <div class="admin-header">
      <h1 class="admin-title">Admin Dashboard</h1>
      <button class="btn-logout" @click="authStore.logout()">Đăng xuất</button>
    </div>

    <div class="admin-tabs">
      <button :class="['tab', { active: activeTab === 'posts' }]" @click="activeTab = 'posts'">Quản lý bài viết</button>
      <button :class="['tab', { active: activeTab === 'prompt' }]" @click="activeTab = 'prompt'">System Prompt AI</button>
    </div>

    <!-- TAB: Bài viết -->
    <div v-if="activeTab === 'posts'">
      <button class="btn-new" @click="showForm = !showForm">
        {{ showForm ? '✕ Hủy' : '+ Bài viết mới' }}
      </button>

      <form v-if="showForm" class="post-form" @submit.prevent="submitPost">
        <div class="form-row">
          <input v-model="form.title" class="form-input" placeholder="Tiêu đề *" required />
          <select v-model="form.type" class="form-input">
            <option value="THEORY">Lý thuyết</option>
            <option value="LEGEND">Truyền thuyết</option>
          </select>
        </div>
        <input v-model="form.author" class="form-input" placeholder="Tác giả" />
        <textarea v-model="form.content" class="form-input" rows="8" placeholder="Nội dung *" required></textarea>
        <input type="file" @change="form.thumbnail = $event.target.files[0]" accept="image/*" class="form-input" />
        <button type="submit" class="btn-submit" :disabled="submitting">
          {{ submitting ? 'Đang lưu...' : 'Đăng bài' }}
        </button>
        <p v-if="formError" class="error-msg">{{ formError }}</p>
      </form>
    </div>

    <!-- TAB: Prompt -->
    <div v-if="activeTab === 'prompt'" class="prompt-section">
      <p class="prompt-hint">System Prompt điều chỉnh cách AI kể chuyện. Thay đổi sẽ áp dụng ngay lập tức.</p>
      <textarea v-model="systemPrompt" class="form-input prompt-textarea" rows="10"></textarea>
      <button class="btn-submit" @click="savePrompt" :disabled="savingPrompt">
        {{ savingPrompt ? 'Đang lưu...' : 'Lưu Prompt' }}
      </button>
    </div>
  </main>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import api from '@/utils/axios'

const authStore     = useAuthStore()
const activeTab     = ref('posts')
const showForm      = ref(false)
const submitting    = ref(false)
const formError     = ref('')
const systemPrompt  = ref('')
const savingPrompt  = ref(false)

const form = ref({ title: '', content: '', type: 'THEORY', author: '', thumbnail: null })

async function submitPost() {
  submitting.value = true; formError.value = ''
  try {
    const fd = new FormData()
    fd.append('title', form.value.title)
    fd.append('content', form.value.content)
    fd.append('type', form.value.type)
    if (form.value.author) fd.append('author', form.value.author)
    if (form.value.thumbnail) fd.append('thumbnail', form.value.thumbnail)

    await api.post('/admin/posts', fd, { headers: { 'Content-Type': 'multipart/form-data' } })
    showForm.value = false
    form.value = { title: '', content: '', type: 'THEORY', author: '', thumbnail: null }
  } catch (e) { formError.value = e.message }
  finally { submitting.value = false }
}

async function savePrompt() {
  savingPrompt.value = true
  try { await api.put('/ai/admin/prompt', { promptContent: systemPrompt.value }) }
  catch (e) { console.error(e) }
  finally { savingPrompt.value = false }
}

onMounted(async () => {
  try {
    const res = await api.get('/ai/prompt')
    systemPrompt.value = res.prompt || ''
  } catch (e) { /* ignore */ }
})
</script>

<style scoped>
.admin-page { padding: 8rem 3rem 6rem; max-width: 900px; margin: 0 auto; }
.admin-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 2rem; }
.admin-title { font-family: 'Playfair Display', serif; font-size: 1.8rem; font-weight: 700; }
.btn-logout { padding: 8px 20px; border: 1px solid var(--border); border-radius: 6px; background: transparent; color: var(--text-secondary); cursor: pointer; font-size: 0.8rem; font-family: 'Inter', sans-serif; transition: all 0.2s; }
.btn-logout:hover { border-color: #d97272; color: #d97272; }
.admin-tabs { display: flex; gap: 0; border-bottom: 1px solid var(--border); margin-bottom: 2rem; }
.tab { padding: 10px 20px; background: none; border: none; border-bottom: 2px solid transparent; color: var(--text-muted); cursor: pointer; font-size: 0.875rem; font-family: 'Inter', sans-serif; transition: all 0.2s; }
.tab.active { color: var(--accent); border-bottom-color: var(--accent); }
.btn-new { padding: 10px 20px; background: linear-gradient(135deg, var(--accent), #a07840); color: #0a0a0f; border: none; border-radius: 8px; cursor: pointer; font-size: 0.875rem; font-weight: 600; font-family: 'Inter', sans-serif; margin-bottom: 1.5rem; }
.post-form { display: flex; flex-direction: column; gap: 0.75rem; }
.form-row { display: grid; grid-template-columns: 1fr auto; gap: 0.75rem; }
.form-input { width: 100%; padding: 10px 14px; background: var(--bg-card); border: 1px solid var(--border); border-radius: 8px; color: var(--text-primary); font-size: 0.875rem; font-family: 'Inter', sans-serif; outline: none; transition: border-color 0.2s; }
.form-input:focus { border-color: var(--accent); }
.form-input::placeholder { color: var(--text-muted); }
textarea.form-input { resize: vertical; }
.btn-submit { padding: 12px 28px; background: linear-gradient(135deg, var(--accent), #a07840); color: #0a0a0f; border: none; border-radius: 8px; font-size: 0.875rem; font-weight: 600; cursor: pointer; font-family: 'Inter', sans-serif; align-self: flex-start; }
.error-msg { color: #d97272; font-size: 0.8rem; }
.prompt-section { display: flex; flex-direction: column; gap: 1rem; }
.prompt-hint { font-size: 0.85rem; color: var(--text-muted); }
.prompt-textarea { min-height: 250px; }
@media (max-width: 700px) { .admin-page { padding: 7rem 1.5rem 4rem; } }
</style>
