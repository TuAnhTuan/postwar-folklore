<template>
  <main class="admin-page">
    <div class="admin-header">
      <h1 class="admin-title">Admin Dashboard</h1>
    </div>

    <div class="admin-tabs">
      <button :class="['tab', { active: activeTab === 'posts' }]" @click="activeTab = 'posts'">Quản lý bài viết</button>
      <button :class="['tab', { active: activeTab === 'prompt' }]" @click="activeTab = 'prompt'">System Prompt AI</button>
    </div>

    <!-- TAB: Bài viết -->
    <div v-if="activeTab === 'posts'">

      <!-- Nút tạo mới -->
      <button class="btn-new" @click="openCreate">
        {{ showForm && !editingId ? '✕ Hủy' : '+ Bài viết mới' }}
      </button>

      <!-- Form tạo / chỉnh sửa -->
      <form v-if="showForm" class="post-form" @submit.prevent="submitPost">
        <div class="form-title-bar">
          <span class="form-label">{{ editingId ? '✏️ Chỉnh sửa bài viết' : '📝 Bài viết mới' }}</span>
          <button type="button" class="btn-cancel" @click="closeForm">Hủy</button>
        </div>
        <div class="form-row">
          <input v-model="form.title" class="form-input" placeholder="Tiêu đề *" required />
          <select v-model="form.type" class="form-input form-select">
            <option value="THEORY">Lý thuyết</option>
            <option value="LEGEND">Truyền thuyết</option>
          </select>
        </div>
        <input v-model="form.author" class="form-input" placeholder="Tác giả" />
        <textarea v-model="form.content" class="form-input" rows="10" placeholder="Nội dung *" required></textarea>
        <div class="file-row">
          <label class="file-label">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><rect x="3" y="3" width="18" height="18" rx="2"/><circle cx="8.5" cy="8.5" r="1.5"/><polyline points="21 15 16 10 5 21"/></svg>
            {{ form.thumbnail ? form.thumbnail.name : 'Chọn ảnh thumbnail' }}
            <input type="file" @change="form.thumbnail = $event.target.files[0]" accept="image/*" hidden />
          </label>
        </div>
        <div class="form-actions">
          <button type="submit" class="btn-submit" :disabled="submitting">
            {{ submitting ? 'Đang lưu...' : editingId ? 'Cập nhật' : 'Đăng bài' }}
          </button>
        </div>
        <p v-if="formError" class="error-msg">{{ formError }}</p>
      </form>

      <!-- Danh sách bài viết -->
      <div class="posts-header">
        <span class="posts-count">{{ posts.length }} bài viết</span>
      </div>

      <div v-if="loadingPosts" class="loading-text">Đang tải...</div>

      <div v-else-if="posts.length === 0" class="empty-state">
        Chưa có bài viết nào. Tạo bài viết đầu tiên!
      </div>

      <div v-else class="post-list">
        <div v-for="post in posts" :key="post.id" class="post-item">
          <div class="post-item-info">
            <span class="post-type-badge" :class="post.type.toLowerCase()">
              {{ post.type === 'THEORY' ? 'Lý thuyết' : 'Truyền thuyết' }}
            </span>
            <span class="post-item-title">{{ post.title }}</span>
            <span class="post-item-author" v-if="post.author">— {{ post.author }}</span>
          </div>
          <div class="post-item-actions">
            <button class="btn-edit" @click="openEdit(post)">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7"/><path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z"/></svg>
              Sửa
            </button>
            <button class="btn-delete" @click="deletePost(post)" :disabled="deletingId === post.id">
              <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><polyline points="3 6 5 6 21 6"/><path d="M19 6l-1 14H6L5 6"/><path d="M10 11v6M14 11v6"/><path d="M9 6V4h6v2"/></svg>
              {{ deletingId === post.id ? '...' : 'Xóa' }}
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- TAB: Prompt -->
    <div v-if="activeTab === 'prompt'" class="prompt-section">
      <p class="prompt-hint">System Prompt điều chỉnh cách AI kể chuyện. Thay đổi áp dụng ngay lập tức.</p>
      <textarea v-model="systemPrompt" class="form-input prompt-textarea" rows="10"></textarea>
      <button class="btn-submit" @click="savePrompt" :disabled="savingPrompt">
        {{ savingPrompt ? 'Đang lưu...' : 'Lưu Prompt' }}
      </button>
    </div>
  </main>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import api from '@/utils/axios'
import { uploadToCloudinary } from '@/utils/cloudinary'

const activeTab    = ref('posts')
const showForm     = ref(false)
const submitting   = ref(false)
const formError    = ref('')
const editingId    = ref(null)
const posts        = ref([])
const loadingPosts = ref(true)
const deletingId   = ref(null)
const systemPrompt = ref('')
const savingPrompt = ref(false)

const emptyForm = () => ({ title: '', content: '', type: 'THEORY', author: '', thumbnail: null })
const form = ref(emptyForm())

// ── POSTS ────────────────────────────────────────────

async function fetchPosts() {
  loadingPosts.value = true
  try {
    const res = await api.get('/posts?page=0&size=100')
    posts.value = res.content || []
  } catch (e) { console.error(e) }
  finally { loadingPosts.value = false }
}

function openCreate() {
  if (showForm.value && !editingId.value) { closeForm(); return }
  editingId.value = null
  form.value = emptyForm()
  showForm.value = true
  setTimeout(() => window.scrollTo({ top: 200, behavior: 'smooth' }), 50)
}

function openEdit(post) {
  editingId.value = post.id
  form.value = {
    title: post.title,
    content: post.content,
    type: post.type,
    author: post.author || '',
    thumbnail: null
  }
  showForm.value = true
  setTimeout(() => window.scrollTo({ top: 200, behavior: 'smooth' }), 50)
}

function closeForm() {
  showForm.value = false
  editingId.value = null
  form.value = emptyForm()
  formError.value = ''
}

async function submitPost() {
  submitting.value = true; formError.value = ''
  try {
    let thumbnailUrl = null
    if (form.value.thumbnail) {
      thumbnailUrl = await uploadToCloudinary(form.value.thumbnail)
    }

    const params = new URLSearchParams({
      title: form.value.title,
      content: form.value.content,
      type: form.value.type,
    })
    if (form.value.author) params.append('author', form.value.author)
    if (thumbnailUrl) params.append('thumbnailUrl', thumbnailUrl)

    if (editingId.value) {
      await api.put(`/admin/posts/${editingId.value}`, params)
    } else {
      await api.post('/admin/posts', params)
    }

    closeForm()
    await fetchPosts()
  } catch (e) { formError.value = e.message }
  finally { submitting.value = false }
}

async function deletePost(post) {
  if (!confirm(`Xóa bài "${post.title}"?`)) return
  deletingId.value = post.id
  try {
    await api.delete(`/admin/posts/${post.id}`)
    posts.value = posts.value.filter(p => p.id !== post.id)
  } catch (e) { alert('Xóa thất bại: ' + e.message) }
  finally { deletingId.value = null }
}

// ── PROMPT ───────────────────────────────────────────

async function savePrompt() {
  savingPrompt.value = true
  try { await api.put('/ai/admin/prompt', { promptContent: systemPrompt.value }) }
  catch (e) { console.error(e) }
  finally { savingPrompt.value = false }
}

onMounted(async () => {
  fetchPosts()
  try {
    const res = await api.get('/ai/prompt')
    systemPrompt.value = res.prompt || ''
  } catch (e) { /* ignore */ }
})
</script>

<style scoped>
.admin-page { padding: 8rem 3rem 6rem; max-width: 960px; margin: 0 auto; }
.admin-header { margin-bottom: 2rem; }
.admin-title { font-family: 'Playfair Display', serif; font-size: 1.8rem; font-weight: 700; }
.admin-tabs { display: flex; border-bottom: 1px solid var(--border); margin-bottom: 2rem; }
.tab { padding: 10px 20px; background: none; border: none; border-bottom: 2px solid transparent; color: var(--text-muted); cursor: pointer; font-size: 0.875rem; font-family: 'Inter', sans-serif; transition: all 0.2s; }
.tab.active { color: var(--accent); border-bottom-color: var(--accent); }

/* Buttons */
.btn-new { padding: 10px 20px; background: linear-gradient(135deg, var(--accent), #a07840); color: #0a0a0f; border: none; border-radius: 8px; cursor: pointer; font-size: 0.875rem; font-weight: 600; font-family: 'Inter', sans-serif; margin-bottom: 1.5rem; }
.btn-submit { padding: 12px 28px; background: linear-gradient(135deg, var(--accent), #a07840); color: #0a0a0f; border: none; border-radius: 8px; font-size: 0.875rem; font-weight: 600; cursor: pointer; font-family: 'Inter', sans-serif; }
.btn-submit:disabled { opacity: 0.6; cursor: not-allowed; }
.btn-cancel { padding: 6px 14px; background: transparent; border: 1px solid var(--border); border-radius: 6px; color: var(--text-muted); font-size: 0.8rem; font-family: 'Inter', sans-serif; cursor: pointer; }
.btn-cancel:hover { border-color: var(--accent); color: var(--accent); }
.btn-edit { display: flex; align-items: center; gap: 5px; padding: 6px 12px; background: transparent; border: 1px solid var(--border); border-radius: 6px; color: var(--text-secondary); font-size: 0.78rem; font-family: 'Inter', sans-serif; cursor: pointer; transition: all 0.15s; }
.btn-edit:hover { border-color: var(--accent); color: var(--accent); }
.btn-delete { display: flex; align-items: center; gap: 5px; padding: 6px 12px; background: transparent; border: 1px solid var(--border); border-radius: 6px; color: var(--text-muted); font-size: 0.78rem; font-family: 'Inter', sans-serif; cursor: pointer; transition: all 0.15s; }
.btn-delete:hover:not(:disabled) { border-color: #d97272; color: #d97272; }
.btn-delete:disabled { opacity: 0.5; cursor: not-allowed; }

/* Form */
.post-form { background: var(--bg-card); border: 1px solid var(--border); border-radius: 12px; padding: 1.5rem; display: flex; flex-direction: column; gap: 0.75rem; margin-bottom: 2rem; }
.form-title-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 0.5rem; }
.form-label { font-size: 0.875rem; font-weight: 600; color: var(--text-primary); }
.form-row { display: grid; grid-template-columns: 1fr 160px; gap: 0.75rem; }
.form-select { padding: 10px 14px; }
.form-input { width: 100%; padding: 10px 14px; background: var(--bg-secondary); border: 1px solid var(--border); border-radius: 8px; color: var(--text-primary); font-size: 0.875rem; font-family: 'Inter', sans-serif; outline: none; transition: border-color 0.2s; box-sizing: border-box; }
.form-input:focus { border-color: var(--accent); }
.form-input::placeholder { color: var(--text-muted); }
textarea.form-input { resize: vertical; }
.file-row { display: flex; }
.file-label { display: flex; align-items: center; gap: 8px; padding: 9px 14px; background: var(--bg-secondary); border: 1px dashed var(--border); border-radius: 8px; color: var(--text-muted); font-size: 0.82rem; font-family: 'Inter', sans-serif; cursor: pointer; transition: all 0.2s; }
.file-label:hover { border-color: var(--accent); color: var(--accent); }
.form-actions { display: flex; gap: 0.75rem; }
.error-msg { color: #d97272; font-size: 0.8rem; }

/* Post list */
.posts-header { display: flex; align-items: center; margin-bottom: 1rem; }
.posts-count { font-size: 0.8rem; color: var(--text-muted); }
.loading-text { color: var(--text-muted); font-size: 0.875rem; padding: 2rem 0; }
.empty-state { text-align: center; padding: 3rem; color: var(--text-muted); font-size: 0.875rem; border: 1px dashed var(--border); border-radius: 12px; }
.post-list { display: flex; flex-direction: column; gap: 0.5rem; }
.post-item { display: flex; align-items: center; justify-content: space-between; padding: 0.9rem 1.2rem; background: var(--bg-card); border: 1px solid var(--border); border-radius: 10px; gap: 1rem; transition: border-color 0.15s; }
.post-item:hover { border-color: rgba(201,169,110,0.3); }
.post-item-info { display: flex; align-items: center; gap: 10px; min-width: 0; flex: 1; }
.post-type-badge { font-size: 0.68rem; font-weight: 600; padding: 3px 8px; border-radius: 20px; text-transform: uppercase; letter-spacing: 0.05em; flex-shrink: 0; }
.post-type-badge.theory { background: rgba(99,179,237,0.15); color: #63b3ed; }
.post-type-badge.legend { background: rgba(201,169,110,0.15); color: var(--accent); }
.post-item-title { font-size: 0.875rem; font-weight: 500; color: var(--text-primary); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.post-item-author { font-size: 0.78rem; color: var(--text-muted); flex-shrink: 0; }
.post-item-actions { display: flex; gap: 0.5rem; flex-shrink: 0; }

/* Prompt */
.prompt-section { display: flex; flex-direction: column; gap: 1rem; }
.prompt-hint { font-size: 0.85rem; color: var(--text-muted); }
.prompt-textarea { min-height: 250px; }

@media (max-width: 700px) {
  .admin-page { padding: 7rem 1.5rem 4rem; }
  .form-row { grid-template-columns: 1fr; }
  .post-item { flex-direction: column; align-items: flex-start; }
  .post-item-actions { width: 100%; }
}
</style>
