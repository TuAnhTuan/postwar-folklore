<template>
  <main class="page">
    <div class="page-header">
      <div class="section-label legend-label">Dân gian mạng</div>
      <h1 class="page-title">Truyền thuyết hậu chiến</h1>
      <p class="page-desc">Những câu chuyện được truyền đi qua lời kể, ký ức tập thể và không gian số.</p>
    </div>

    <!-- Location cards lớn -->
    <div class="location-cards" v-if="locations.length">
      <button
        v-for="loc in locations"
        :key="loc.slug"
        class="location-card"
        :class="{ active: selectedSlug === loc.slug }"
        @click="selectLocation(loc.slug)"
      >
        <span class="loc-card-name">{{ loc.name }}</span>
      </button>
    </div>

    <!-- Tab Tất cả -->
    <div class="filter-bar">
      <button
        class="tab-all"
        :class="{ active: selectedSlug === null }"
        @click="selectLocation(null)"
      >Tất cả</button>
    </div>

    <!-- Posts -->
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
  try { await fetchPosts() } catch (e) { console.error('fetchPosts error:', e) }
  finally { loading.value = false }

  try {
    locations.value = await api.get('/locations')
  } catch (e) {
    console.error('fetchLocations error:', e)
  }
})
</script>

<style scoped>
.page { padding: 8rem 3rem 6rem; max-width: 1200px; margin: 0 auto; }
.page-header { margin-bottom: 2.5rem; }
.section-label { font-size: 0.7rem; letter-spacing: 0.15em; text-transform: uppercase; margin-bottom: 0.5rem; }
.legend-label { color: #d97272; }
.page-title { font-family: 'Playfair Display', serif; font-size: clamp(2rem, 4vw, 3rem); font-weight: 700; margin-bottom: 0.75rem; }
.page-desc { font-size: 0.95rem; color: var(--text-secondary); }

/* Location cards lớn */
.location-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 1rem;
  margin-bottom: 1.25rem;
}
.location-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 1.25rem 1.5rem;
  background: #c9a96e;
  border: 2px solid #c9a96e;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
  text-align: center;
}
.location-card:hover {
  background: #b8924f;
  border-color: #b8924f;
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(201,169,110,0.3);
}
.location-card.active {
  background: #a07840;
  border-color: #a07840;
  box-shadow: 0 0 0 2px #a07840;
}
.loc-card-name {
  font-family: 'Playfair Display', serif;
  font-size: 1.4rem;
  font-weight: 700;
  color: #0a0a0f;
}

/* Tab Tất cả */
.filter-bar { margin-bottom: 2rem; }
.tab-all {
  padding: 7px 20px;
  border: 1px solid var(--border);
  border-radius: 100px;
  background: transparent;
  color: var(--text-secondary);
  cursor: pointer;
  font-size: 0.85rem;
  font-family: 'Inter', sans-serif;
  transition: all 0.2s;
}
.tab-all:hover { border-color: #d97272; color: #d97272; }
.tab-all.active { background: #d97272; border-color: #d97272; color: #fff; }

/* Grid */
.posts-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 1.5rem; }
.loading-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 1.5rem; }
.skeleton-card { height: 320px; background: var(--bg-card); border: 1px solid var(--border); border-radius: 12px; animation: shimmer 1.5s ease-in-out infinite; }
@keyframes shimmer { 0%,100%{opacity:0.5;} 50%{opacity:1;} }
.empty-state { text-align: center; padding: 4rem; color: var(--text-muted); }
.load-more { text-align: center; margin-top: 3rem; }
.btn-load { padding: 12px 32px; border: 1px solid var(--border); border-radius: 8px; background: transparent; color: var(--text-secondary); cursor: pointer; font-size: 0.875rem; font-family: 'Inter', sans-serif; transition: all 0.2s; }
.btn-load:hover:not(:disabled) { border-color: #d97272; color: #d97272; }
.btn-load:disabled { opacity: 0.5; cursor: not-allowed; }

@media (max-width: 900px) {
  .posts-grid, .loading-grid { grid-template-columns: repeat(2, 1fr); }
  .page { padding: 7rem 1.5rem 4rem; }
}
@media (max-width: 580px) {
  .posts-grid, .loading-grid { grid-template-columns: 1fr; }
  .location-cards { grid-template-columns: 1fr 1fr; }
}
</style>
