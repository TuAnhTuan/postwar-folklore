<template>
  <section class="comments">
    <h2 class="comments-title">Bình luận ({{ comments.length }})</h2>

    <!-- Form -->
    <div class="comment-form">
      <input
        v-if="!authStore.isLoggedIn"
        v-model="guestName"
        class="form-input"
        placeholder="Tên hiển thị *"
      />
      <textarea
        v-model="commentText"
        class="form-input"
        rows="3"
        placeholder="Viết bình luận của bạn..."
      ></textarea>
      <button class="btn-submit" @click="submit" :disabled="submitting">
        {{ submitting ? 'Đang gửi...' : 'Gửi bình luận' }}
      </button>
      <p v-if="submitError" class="error-msg">{{ submitError }}</p>
    </div>

    <!-- List -->
    <div v-if="loading" class="loading-text">Đang tải bình luận...</div>
    <div v-else class="comment-list">
      <div v-for="c in comments" :key="c.id" class="comment-item">
        <div class="comment-header">
          <div class="comment-avatar">{{ (c.displayName || '?')[0].toUpperCase() }}</div>
          <div>
            <span class="comment-name">{{ c.displayName }}</span>
            <span v-if="c.isAuthenticated" class="verified-badge">✓</span>
          </div>
          <span class="comment-time">{{ timeAgo(c.createdAt) }}</span>
        </div>
        <p class="comment-text">{{ c.content }}</p>
      </div>
      <div v-if="!comments.length" class="empty-comments">Chưa có bình luận nào. Hãy là người đầu tiên!</div>
    </div>
  </section>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth'
import api from '@/utils/axios'

const props       = defineProps({ postId: { type: String, required: true } })
const authStore   = useAuthStore()
const comments    = ref([])
const loading     = ref(true)
const guestName   = ref('')
const commentText = ref('')
const submitting  = ref(false)
const submitError = ref('')

function timeAgo(dateStr) {
  if (!dateStr) return ''
  const diff = Date.now() - new Date(dateStr).getTime()
  if (isNaN(diff) || diff < 0) return 'Vừa xong'
  const mins = Math.floor(diff / 60000)
  if (mins < 1) return 'Vừa xong'
  if (mins < 60) return `${mins} phút trước`
  const hrs = Math.floor(mins / 60)
  if (hrs < 24) return `${hrs} giờ trước`
  return `${Math.floor(hrs / 24)} ngày trước`
}

async function fetchComments() {
  try {
    comments.value = await api.get(`/comments?postId=${props.postId}`)
  } catch (e) { console.error(e) }
  finally { loading.value = false }
}

async function submit() {
  submitError.value = ''
  if (!commentText.value.trim()) { submitError.value = 'Vui lòng nhập nội dung bình luận.'; return }
  if (!authStore.isLoggedIn && !guestName.value.trim()) { submitError.value = 'Vui lòng nhập tên hiển thị.'; return }

  submitting.value = true
  try {
    const body = { content: commentText.value.trim() }
    if (!authStore.isLoggedIn) body.displayName = guestName.value.trim()

    const newComment = await api.post(`/comments?postId=${props.postId}`, body)
    comments.value.unshift(newComment)
    commentText.value = ''
    guestName.value = ''
  } catch (e) { submitError.value = e.message }
  finally { submitting.value = false }
}

onMounted(fetchComments)
</script>

<style scoped>
.comments { margin-top: 4rem; border-top: 1px solid var(--border); padding-top: 2.5rem; }
.comments-title { font-family: 'Playfair Display', serif; font-size: 1.3rem; font-weight: 700; margin-bottom: 1.5rem; }
.comment-form { display: flex; flex-direction: column; gap: 0.75rem; margin-bottom: 2rem; }
.form-input { width: 100%; padding: 10px 14px; background: var(--bg-card); border: 1px solid var(--border); border-radius: 8px; color: var(--text-primary); font-size: 0.875rem; font-family: 'Inter', sans-serif; outline: none; transition: border-color 0.2s; }
.form-input:focus { border-color: var(--accent); }
.form-input::placeholder { color: var(--text-muted); }
textarea.form-input { resize: vertical; }
.btn-submit { align-self: flex-start; padding: 10px 24px; background: linear-gradient(135deg, var(--accent), #a07840); color: #0a0a0f; border: none; border-radius: 8px; font-size: 0.8rem; font-weight: 600; cursor: pointer; font-family: 'Inter', sans-serif; transition: all 0.2s; text-transform: uppercase; letter-spacing: 0.05em; }
.btn-submit:disabled { opacity: 0.6; cursor: not-allowed; }
.error-msg { color: #d97272; font-size: 0.8rem; }
.loading-text { color: var(--text-muted); font-size: 0.875rem; }
.comment-list { display: flex; flex-direction: column; gap: 1rem; }
.comment-item { background: var(--bg-card); border: 1px solid var(--border); border-radius: 10px; padding: 1rem 1.2rem; }
.comment-header { display: flex; align-items: center; gap: 10px; margin-bottom: 0.6rem; }
.comment-avatar { width: 30px; height: 30px; border-radius: 50%; background: linear-gradient(135deg, #a07840, #8b2d2d); display: flex; align-items: center; justify-content: center; font-size: 0.75rem; font-weight: 700; color: #0a0a0f; flex-shrink: 0; }
.comment-name { font-size: 0.82rem; font-weight: 600; color: var(--text-primary); }
.verified-badge { font-size: 0.7rem; color: var(--accent); margin-left: 4px; }
.comment-time { font-size: 0.72rem; color: var(--text-muted); margin-left: auto; }
.comment-text { font-size: 0.82rem; color: var(--text-secondary); line-height: 1.6; }
.empty-comments { text-align: center; padding: 2rem; color: var(--text-muted); font-size: 0.875rem; }
</style>
