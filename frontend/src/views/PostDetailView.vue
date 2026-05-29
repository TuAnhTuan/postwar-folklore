<template>
  <main class="page" v-if="post">
    <div class="post-header">
      <div class="post-tags">
        <span class="post-tag" :class="post.type === 'THEORY' ? 'tag-theory' : 'tag-legend'">
          {{ post.type === 'THEORY' ? 'Lý thuyết' : 'Truyền thuyết' }}
        </span>
        <span v-if="post.location" class="post-tag tag-location">
          📍 {{ post.location.name }}
        </span>
      </div>
      <h1 class="post-title">{{ post.title }}</h1>
      <div class="post-meta">
        <span>{{ post.author || 'Ban biên soạn' }}</span>
        <span>·</span>
        <span>{{ formatDate(post.createdAt) }}</span>
        <span>·</span>
        <span>{{ post.commentCount }} bình luận</span>
      </div>
    </div>

    <img v-if="post.thumbnailUrl" :src="heroSrc" class="post-thumbnail" :alt="post.title" />

    <div class="post-content">{{ post.content }}</div>

    <CommentSection :post-id="post.id" />
  </main>

  <div v-else-if="loading" class="loading-state">Đang tải...</div>
  <div v-else class="error-state">Không tìm thấy bài viết.</div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import CommentSection from '@/components/CommentSection.vue'
import api from '@/utils/axios'
import { cloudinaryUrl } from '@/utils/cloudinary'

const route   = useRoute()
const post    = ref(null)
const loading = ref(true)

const heroSrc = computed(() =>
  cloudinaryUrl(post.value?.thumbnailUrl, { w: 1200, h: 675 })
)

function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleDateString('vi-VN', { year: 'numeric', month: 'long', day: 'numeric' })
}

onMounted(async () => {
  try {
    post.value = await api.get(`/posts/${route.params.id}`)
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.page { padding: 8rem 3rem 6rem; max-width: 800px; margin: 0 auto; }
.post-header { margin-bottom: 2.5rem; }
.post-tags { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; margin-bottom: 1rem; }
.post-tag { display: inline-block; padding: 4px 12px; border-radius: 4px; font-size: 0.7rem; font-weight: 600; letter-spacing: 0.1em; text-transform: uppercase; }
.tag-theory { background: rgba(201,169,110,0.1); color: var(--accent); border: 1px solid rgba(201,169,110,0.2); }
.tag-legend { background: rgba(139,45,45,0.15); color: #d97272; border: 1px solid rgba(139,45,45,0.3); }
.tag-location { background: rgba(99,179,237,0.08); color: #7ec8e3; border: 1px solid rgba(99,179,237,0.2); letter-spacing: 0.05em; text-transform: none; font-weight: 500; }
.post-title { font-family: 'Playfair Display', serif; font-size: clamp(1.8rem, 4vw, 2.8rem); font-weight: 700; line-height: 1.2; margin-bottom: 1rem; }
.post-meta { display: flex; gap: 0.5rem; font-size: 0.8rem; color: var(--text-muted); flex-wrap: wrap; }
.post-thumbnail { width: 100%; border-radius: 12px; margin-bottom: 2.5rem; aspect-ratio: 16/9; object-fit: cover; }
.post-content { font-size: 1rem; line-height: 1.85; color: var(--text-secondary); white-space: pre-line; margin-bottom: 4rem; }
.loading-state, .error-state { padding: 10rem 2rem; text-align: center; color: var(--text-muted); }
@media (max-width: 700px) { .page { padding: 7rem 1.5rem 4rem; } }
</style>
