<template>
  <!-- Toggle Button -->
  <button class="chat-toggle" @click="isOpen = !isOpen" :title="isOpen ? 'Đóng chat' : 'Nói chuyện với Dân Gian Mạng'">
    <img v-if="!isOpen" src="/logo-dan-gian-mang.png" alt="Dân Gian Mạng" class="toggle-logo" />
    <span v-else>✕</span>
  </button>

  <!-- Chat Widget -->
  <Transition name="chat">
    <div v-if="isOpen" class="chat-widget">
      <div class="chat-header">
        <div class="ai-avatar"><img src="/logo-dan-gian-mang.png" alt="Dân Gian Mạng" /></div>
        <div class="ai-info">
          <div class="ai-name">Dân Gian Mạng</div>
          <div class="ai-status"><span class="status-dot"></span>Đang hoạt động</div>
        </div>
      </div>

      <div class="chat-messages" ref="messagesEl">
        <div v-for="(msg, i) in messages" :key="i" class="chat-msg" :class="msg.role">
          <div class="msg-avatar">
            <img v-if="msg.role === 'ai'" src="/logo-dan-gian-mang.png" alt="Dân Gian Mạng" />
            <span v-else>👤</span>
          </div>
          <div class="msg-bubble">
            <p style="white-space: pre-wrap; margin: 0; word-break: break-word;">{{ msg.text }}</p>
            <RouterLink
              v-if="msg.postId"
              :to="`/bai-viet/${msg.postId}`"
              class="read-more-link"
              @click="isOpen = false"
            >Đọc bài đầy đủ →</RouterLink>
          </div>
        </div>
        <div v-if="typing" class="chat-msg ai">
          <div class="msg-avatar"><img src="/logo-dan-gian-mang.png" alt="Dân Gian Mạng" /></div>
          <div class="msg-bubble typing-bubble">
            <span></span><span></span><span></span>
          </div>
        </div>
      </div>

      <div v-if="suggestions.length" class="chat-suggestions">
        <button
          v-for="s in suggestions"
          :key="s"
          class="suggest-btn"
          @click="sendMessage(s)"
        >{{ s }}</button>
      </div>

      <div class="chat-input-area">
        <input
          v-model="inputText"
          class="chat-input"
          placeholder="Hỏi dân gian mạng..."
          @keydown.enter="sendMessage()"
        />
        <button class="chat-send" @click="sendMessage()" :disabled="typing">↑</button>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { ref, nextTick, watch } from 'vue'
import { RouterLink } from 'vue-router'
import api from '@/utils/axios'

const isOpen     = ref(false)
const inputText  = ref('')
const typing     = ref(false)
const messagesEl = ref(null)
const messages   = ref([
  { role: 'ai', text: 'Vậy là bạn vừa ghé nhà Hoài tự rồi đó! 🌙\nMình là Dân Gian Mạng, người bạn nhỏ sẽ cùng bạn lần theo những lời kể hậu chiến.\nBạn muốn bắt đầu từ đâu?', postId: null }
])
const suggestions = ref([])

// Template câu hỏi theo type bài viết
const templatesByType = {
  'LEGEND': [
    t => `Truyền thuyết "${t}" kể về điều gì?`,
    t => `Nguồn gốc của "${t}" là gì?`,
    t => `"${t}" có thật không?`,
    t => `Dân gian lưu truyền gì về "${t}"?`,
  ],
  'THEORY': [
    t => `Giả thuyết "${t}" nói về điều gì?`,
    t => `"${t}" có cơ sở không?`,
    t => `Bí ẩn đằng sau "${t}" là gì?`,
    t => `Tại sao người ta tin vào "${t}"?`,
  ],
}

const defaultTemplates = [
  t => `"${t}" — câu chuyện đằng sau là gì?`,
  t => `Bí ẩn nào ẩn sau "${t}"?`,
  t => `Hãy kể tôi nghe về "${t}"`,
  t => `"${t}" có liên quan đến chiến tranh không?`,
  t => `Dân gian lưu truyền gì về "${t}"?`,
]

function buildSuggestion(post) {
  const pool = templatesByType[post.type] ?? defaultTemplates
  const fn = pool[Math.floor(Math.random() * pool.length)]
  return fn(post.title)
}

// Khi mở chat, lấy 3 bài mới nhất làm gợi ý
watch(isOpen, async (opened) => {
  if (!opened || suggestions.value.length > 0) return
  try {
    const res = await api.get('/posts', { params: { page: 0, size: 3, sort: 'createdAt,desc' } })
    const posts = res.content ?? res
    suggestions.value = posts.map(p => buildSuggestion(p))
  } catch {
    suggestions.value = ['Truyền thuyết hậu chiến là gì?', 'Bí ẩn nào còn sót lại sau chiến tranh?']
  }
})

async function sendMessage(text) {
  const msg = text || inputText.value.trim()
  if (!msg || typing.value) return

  suggestions.value = []
  inputText.value = ''
  messages.value.push({ role: 'user', text: msg })
  await scrollToBottom()

  typing.value = true
  try {
    const res = await api.post('/ai/chat', { message: msg })
    messages.value.push({
      role: 'ai',
      text: res.reply || 'Ta không nhận được phản hồi.',
      postId: res.found ? res.postId : null,
      postTitle: res.found ? res.postTitle : null
    })
  } catch (e) {
    const status = e?.response?.status
    const errMsg = status === 429
      ? 'Ngươi đã hỏi quá nhiều. Hãy chờ một lúc rồi hỏi lại.'
      : 'Ta đang gặp khó khăn trong việc kết nối với kho ký ức. Hãy thử lại sau.'
    messages.value.push({ role: 'ai', text: errMsg, postId: null })
  } finally {
    typing.value = false
    await scrollToBottom()
  }
}

async function scrollToBottom() {
  await nextTick()
  if (messagesEl.value) {
    messagesEl.value.scrollTop = messagesEl.value.scrollHeight
  }
}
</script>

<style scoped>
.chat-toggle {
  position: fixed; bottom: 2rem; right: 2rem; z-index: 150;
  width: 56px; height: 56px;
  background: var(--bg-secondary);
  border: 1px solid var(--border); border-radius: 50%; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  font-size: 1.2rem;
  box-shadow: 0 8px 24px rgba(0,0,0,0.4);
  transition: all 0.3s;
  color: var(--text-secondary);
  overflow: hidden;
  padding: 0;
}
.chat-toggle:hover { transform: scale(1.1); box-shadow: 0 12px 32px rgba(201,169,110,0.3); border-color: var(--accent); }
.toggle-logo { width: 100%; height: 100%; object-fit: cover; border-radius: 50%; }

.chat-widget {
  position: fixed; bottom: 6rem; right: 2rem; z-index: 150;
  width: 380px; height: 520px;
  background: var(--bg-secondary);
  border: 1px solid var(--border);
  border-radius: 16px;
  display: flex; flex-direction: column;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0,0,0,0.5);
}

.chat-enter-active, .chat-leave-active { transition: all 0.3s cubic-bezier(0.34,1.56,0.64,1); }
.chat-enter-from, .chat-leave-to { opacity: 0; transform: translateY(20px) scale(0.95); }

.chat-header { padding: 1rem 1.2rem; background: var(--bg-card); border-bottom: 1px solid var(--border); display: flex; align-items: center; gap: 10px; }
.ai-avatar { width: 36px; height: 36px; border-radius: 50%; background: var(--bg-secondary); border: 1px solid var(--border); display: flex; align-items: center; justify-content: center; flex-shrink: 0; overflow: hidden; }
.ai-avatar img { width: 100%; height: 100%; object-fit: cover; }
.ai-info { flex: 1; }
.ai-name { font-size: 0.875rem; font-weight: 600; color: var(--text-primary); }
.ai-status { font-size: 0.7rem; color: var(--accent); display: flex; align-items: center; gap: 4px; }
.status-dot { width: 5px; height: 5px; border-radius: 50%; background: var(--accent); animation: pulse 2s ease-in-out infinite; }
@keyframes pulse { 0%,100%{opacity:1;} 50%{opacity:0.4;} }

.chat-messages { flex: 1; overflow-y: auto; padding: 1rem; display: flex; flex-direction: column; gap: 0.75rem; }
.chat-messages::-webkit-scrollbar { width: 4px; }
.chat-messages::-webkit-scrollbar-thumb { background: var(--border); border-radius: 2px; }

.chat-msg { display: flex; gap: 8px; align-items: flex-end; }
.chat-msg.user { flex-direction: row-reverse; }
.msg-avatar { width: 26px; height: 26px; border-radius: 50%; background: var(--bg-card); border: 1px solid var(--border); display: flex; align-items: center; justify-content: center; font-size: 0.7rem; flex-shrink: 0; overflow: hidden; }
.msg-avatar img { width: 100%; height: 100%; object-fit: cover; }
.chat-msg.ai .msg-avatar { background: var(--bg-secondary); border-color: var(--border); }
.msg-bubble { max-width: 75%; padding: 10px 13px; border-radius: 12px; font-size: 0.8rem; line-height: 1.55; }
.msg-text { white-space: pre-wrap; margin: 0; }
.chat-msg.ai .msg-bubble { background: var(--bg-card); border: 1px solid var(--border); color: var(--text-secondary); border-bottom-left-radius: 3px; display: flex; flex-direction: column; gap: 8px; }
.chat-msg.user .msg-bubble { background: rgba(201,169,110,0.15); border: 1px solid rgba(201,169,110,0.2); color: var(--text-primary); border-bottom-right-radius: 3px; }
.read-more-link { display: inline-block; margin-top: 2px; color: var(--accent); font-size: 0.75rem; font-weight: 600; text-decoration: none; border-top: 1px solid rgba(201,169,110,0.2); padding-top: 6px; transition: opacity 0.2s; letter-spacing: 0.02em; }
.read-more-link:hover { opacity: 0.75; }
.typing-bubble { display: flex; gap: 4px; align-items: center; padding: 14px 16px; }
.typing-bubble span { width: 6px; height: 6px; background: var(--text-muted); border-radius: 50%; animation: bounce 1.2s ease-in-out infinite; }
.typing-bubble span:nth-child(2) { animation-delay: 0.2s; }
.typing-bubble span:nth-child(3) { animation-delay: 0.4s; }
@keyframes bounce { 0%,60%,100%{transform:translateY(0);} 30%{transform:translateY(-5px);} }

.chat-suggestions { display: flex; flex-wrap: wrap; gap: 6px; padding: 0 1rem 0.5rem; }
.suggest-btn { padding: 5px 11px; background: transparent; border: 1px solid var(--border); border-radius: 20px; color: var(--text-muted); font-size: 0.72rem; cursor: pointer; font-family: 'Inter', sans-serif; transition: all 0.2s; }
.suggest-btn:hover { border-color: var(--accent); color: var(--accent); }

.chat-input-area { padding: 0.75rem 1rem; border-top: 1px solid var(--border); display: flex; gap: 8px; }
.chat-input { flex: 1; background: var(--bg-card); border: 1px solid var(--border); border-radius: 8px; padding: 8px 12px; color: var(--text-primary); font-size: 0.8rem; font-family: 'Inter', sans-serif; outline: none; transition: border-color 0.2s; }
.chat-input:focus { border-color: var(--accent); }
.chat-input::placeholder { color: var(--text-muted); }
.chat-send { width: 36px; height: 36px; background: linear-gradient(135deg, var(--accent), #a07840); border: none; border-radius: 8px; color: #0a0a0f; cursor: pointer; display: flex; align-items: center; justify-content: center; font-size: 1rem; transition: all 0.2s; flex-shrink: 0; }
.chat-send:hover:not(:disabled) { transform: scale(1.05); }
.chat-send:disabled { opacity: 0.5; cursor: not-allowed; }

@media (max-width: 480px) { .chat-widget { width: calc(100vw - 2rem); right: 1rem; bottom: 5.5rem; } }
</style>
