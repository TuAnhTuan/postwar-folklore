<template>
  <main class="page">
    <div class="page-header">
      <div class="section-label legend-label">Dân gian mạng</div>
      <h1 class="page-title">Truyền thuyết hậu chiến</h1>
      <p class="page-desc">Những câu chuyện được truyền đi qua lời kể, ký ức tập thể và không gian số.</p>
    </div>

    <div class="location-tabs" v-if="locations.length">
      <button
        class="tab-btn"
        :class="{ active: selectedSlug === null }"
        @click="selectLocation(null)"
      >Tất cả</button>
      <button
        v-for="loc in locations"
        :key="loc.slug"
        class="tab-btn"
        :class="{ active: selectedSlug === loc.slug }"
        @click="selectLocation(loc.slug)"
      >{{ loc.name }}</button>
    </div>

    <div class="content-area">
      <div v-if="loading" class="loading-grid">
        <div v-for="i in 6" :key="i" class="skeleton-card"></div>
      </div>
      <div v-else-if="posts.length" class="posts-grid">
        <PostCard v-for="post in posts" :key="post.id" :post="post" />
      </div>
      <div v-else class="empty-state">Chưa có truyền thuyết nào.</div>

      <div v-if="!loading && hasMore" class="load-more">
        <button class="btn-load" @click="loadMore" :disabled="loadingMore">
          {{ loadingMore ? 'Đang tải...' : 'Xem thêm' }}
        </button>
      </div>
    </div>
  </main>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import PostCard from '@/components/PostCard.vue'
import api from '@/utils/axios'

const posts        = ref([])
const locations    = ref([])
const loading      = ref(true)
const loadingMore  = ref(false)
const page         = ref(0)
const hasMore      = ref(false)
const selectedSlug = ref(null)

function buildUrl() {
  let url = `/posts/type/LEGEND?page=${page.value}&size=9`
  if (selectedSlug.value) url += `&location=${selectedSlug.value}`
  return url
}

async function fetchPosts() {
  const res = await api.get(buildUrl())
  posts.value.push(...(res.content || []))
  hasMore.value = !res.last
}

async function selectLocation(slug) {
  selectedSlug.value = slug
  posts.value = []
  page.value = 0
  hasMore.value = false
  loading.value = true
  try { await fetchPosts() } catch (e) { console.error(e) }
  finally { loading.value = false }
}

async function loadMore() {
  loadingMore.value = true
  page.value++
  await fetchPosts()
  loadingMore.value = false
}

onMounted(async () => {
  try {
    const [, locs] = await Promise.all([
      fetchPosts(),
      api.get('/locations')
    ])
    locations.value = locs
  } catch (e) { console.error(e) }
  finally { loading.value = false }
})
</script>

<style scoped>
.page { padding: 8rem 3rem 6rem; max-width: 1200px; margin: 0 auto; }
.page-header { margin-bottom: 2rem; }
.section-label { font-size: 0.7rem; letter-spacing: 0.15em; text-transform: uppercase; margin-bottom: 0.5rem; }
.legend-label { color: #d97272; }
.page-title { font-family: 'Playfair Display', serif; font-size: clamp(2rem, 4vw, 3rem); font-weight: 700; margin-bottom: 0.75rem; }
.page-desc { font-size: 0.95rem; color: var(--text-secondary); }

.location-tabs {
  display: flex;
  gap: 0.5rem;
  margin-bottom: 2.5rem;
  flex-wrap: wrap;
}
.tab-btn {
  padding: 8px 20px;
  border: 1px solid var(--border);
  border-radius: 100px;
  background: transparent;
  color: var(--text-secondary);
  cursor: pointer;
  font-size: 0.85rem;
  font-family: 'Inter', sans-serif;
  transition: all 0.2s;
}
.tab-btn:hover { border-color: #d97272; color: #d97272; }
.tab-btn.active { background: #d97272; border-color: #d97272; color: #fff; }

.posts-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 1.5rem; }
.loading-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 1.5rem; }
.skeleton-card { height: 320px; background: var(--bg-card); border: 1px solid var(--border); border-radius: 12px; animation: shimmer 1.5s ease-in-out infinite; }
@keyframes shimmer { 0%,100%{opacity:0.5;} 50%{opacity:1;} }
.empty-state { text-align: center; padding: 4rem; color: var(--text-muted); }
.load-more { text-align: center; margin-top: 3rem; }
.btn-load { padding: 12px 32px; border: 1px solid var(--border); border-radius: 8px; background: transparent; color: var(--text-secondary); cursor: pointer; font-size: 0.875rem; font-family: 'Inter', sans-serif; transition: all 0.2s; }
.btn-load:hover:not(:disabled) { border-color: #d97272; color: #d97272; }
.btn-load:disabled { opacity: 0.5; cursor: not-allowed; }
@media (max-width: 900px) { .posts-grid, .loading-grid { grid-template-columns: repeat(2, 1fr); } .page { padding: 7rem 1.5rem 4rem; } }
@media (max-width: 580px) { .posts-grid, .loading-grid { grid-template-columns: 1fr; } }
</style>
