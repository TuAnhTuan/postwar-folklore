<template>
  <router-link :to="`/bai-viet/${post.id}`" class="post-card">
    <div class="post-thumb">
      <img v-if="post.thumbnailUrl" :src="thumbSrc" :alt="post.title" class="thumb-img" />
      <div v-else class="thumb-placeholder" :class="post.type === 'THEORY' ? 'thumb-theory' : 'thumb-legend'">
        <span>{{ post.type === 'THEORY' ? '📜' : '🌙' }}</span>
      </div>
    </div>
    <div class="post-body">
      <div class="post-tags">
        <span class="post-tag" :class="post.type === 'THEORY' ? 'tag-theory' : 'tag-legend'">
          {{ post.type === 'THEORY' ? 'Lý thuyết' : 'Truyền thuyết' }}
        </span>
        <span v-if="post.location" class="post-tag tag-location">
          📍 {{ post.location.name }}
        </span>
      </div>
      <h3 class="post-title">{{ post.title }}</h3>
      <p class="post-excerpt">{{ excerpt }}</p>
      <div class="post-meta">
        <div class="post-author">
          <div class="author-avatar">{{ (post.author || 'B')[0].toUpperCase() }}</div>
          <span>{{ post.author || 'Ban biên soạn' }}</span>
        </div>
        <div class="post-stats">
          <span>💬 {{ post.commentCount || 0 }}</span>
        </div>
      </div>
    </div>
  </router-link>
</template>

<script setup>
import { computed } from 'vue'
import { cloudinaryUrl } from '@/utils/cloudinary'

const props = defineProps({ post: { type: Object, required: true } })

const excerpt = computed(() => {
  const text = props.post.content || ''
  return text.length > 120 ? text.slice(0, 120) + '...' : text
})

const thumbSrc = computed(() =>
  cloudinaryUrl(props.post.thumbnailUrl, { w: 600, h: 360 })
)
</script>

<style scoped>
.post-card {
  display: block;
  text-decoration: none;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s;
  color: inherit;
}
.post-card:hover { border-color: var(--border-hover); transform: translateY(-4px); box-shadow: 0 12px 40px rgba(0,0,0,0.4); }
.post-thumb { height: 180px; overflow: hidden; }
.thumb-img { width: 100%; height: 100%; object-fit: cover; }
.thumb-placeholder { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; font-size: 2.5rem; }
.thumb-theory { background: linear-gradient(135deg, #1a1a2e, #16213e); }
.thumb-legend { background: linear-gradient(135deg, #1a0a0a, #2d1515); }
.post-body { padding: 1.4rem; }
.post-tags { display: flex; align-items: center; gap: 6px; flex-wrap: wrap; margin-bottom: 0.75rem; }
.post-tag { display: inline-block; padding: 3px 10px; border-radius: 4px; font-size: 0.65rem; font-weight: 600; letter-spacing: 0.1em; text-transform: uppercase; }
.tag-theory { background: rgba(201,169,110,0.1); color: var(--accent); border: 1px solid rgba(201,169,110,0.2); }
.tag-legend { background: rgba(139,45,45,0.15); color: #d97272; border: 1px solid rgba(139,45,45,0.3); }
.tag-location { background: rgba(99,179,237,0.08); color: #7ec8e3; border: 1px solid rgba(99,179,237,0.2); letter-spacing: 0.05em; text-transform: none; font-weight: 500; }
.post-title { font-family: 'Playfair Display', serif; font-size: 1.05rem; font-weight: 700; color: var(--text-primary); margin-bottom: 0.6rem; line-height: 1.4; }
.post-excerpt { font-size: 0.8rem; color: var(--text-secondary); line-height: 1.6; margin-bottom: 1.2rem; }
.post-meta { display: flex; align-items: center; justify-content: space-between; font-size: 0.72rem; color: var(--text-muted); }
.post-author { display: flex; align-items: center; gap: 6px; }
.author-avatar { width: 20px; height: 20px; border-radius: 50%; background: linear-gradient(135deg, #a07840, #8b2d2d); display: flex; align-items: center; justify-content: center; font-size: 0.6rem; font-weight: 700; color: #0a0a0f; }
</style>
