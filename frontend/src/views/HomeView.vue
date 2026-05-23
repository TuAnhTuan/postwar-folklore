<template>
  <main>
    <!-- HERO -->
    <section class="hero">
      <div class="hero-content">
        <div class="hero-badge">
          <span class="badge-dot"></span>
          KHO LƯU GIỮ LỜI KỂ · KÝ ỨC HẬU CHIẾN
        </div>
        <h1 class="hero-title">
          <span class="gradient-text">HOÀI TỰ</span>
          <span class="hero-tagline">Tìm về truyền thuyết hậu chiến</span>
        </h1>
        <p class="hero-subtitle">
          Nơi lưu giữ những lời kể, truyền thuyết và hoài niệm của một thế giới sau chiến tranh.
        </p>
        <div class="hero-cta">
          <router-link to="/ve-hoai-tu" class="btn-primary">Ghé nhà Hoài tự</router-link>
          <router-link to="/truyen-thuyet" class="btn-secondary">Mở kho lời kể</router-link>
        </div>
      </div>
    </section>

    <!-- ẢNH BÌA -->
    <section class="cover-section">
      <div class="cover-wrap">
        <img src="/anh-bia.png" alt="Hoài tự — Truyền thuyết hậu chiến" class="cover-img" />
        <div class="cover-overlay"></div>
      </div>
    </section>

    <!-- LỜI GIỚI THIỆU -->
    <section class="intro-section">
      <div class="intro-inner">
        <div class="section-label">Lời giới thiệu</div>
        <h2 class="intro-title">Hoài tự là gì?</h2>
        <div class="intro-body">
          <p>
            Hoài tự là một căn nhà nhỏ dành cho những lời kể hậu chiến. Ở đây, tụi mình gom nhặt và lưu giữ các truyền thuyết về người chết, linh hồn, báo mộng, hài cốt, không gian thiêng và ký ức cộng đồng tại Quảng Nam - Đà Nẵng.
          </p>
          <p>
            Từ sân đình, bến nước, ngõ chợ đến vỉa hè, khu chung cư và diễn đàn trực tuyến, truyền thuyết hậu chiến vẫn tiếp tục được kể, được tin và được tái tạo trong đời sống đương đại.
          </p>
          <p>
            Mời bạn ghé nhà Hoài tự, để cùng lắng nghe những câu chuyện còn ở lại sau chiến tranh.
          </p>
        </div>
        <router-link to="/ve-hoai-tu" class="intro-cta">Đọc thêm về Hoài tự →</router-link>
      </div>
    </section>

    <!-- RECENT POSTS -->
    <section class="section">
      <div class="section-header">
        <div>
          <div class="section-label">Mới nhất</div>
          <h2 class="section-title">Bài viết gần đây</h2>
        </div>
        <router-link to="/ly-thuyet" class="section-link">Xem tất cả →</router-link>
      </div>

      <div v-if="loading" class="loading-grid">
        <div v-for="i in 6" :key="i" class="skeleton-card"></div>
      </div>

      <div v-else class="posts-grid">
        <PostCard
          v-for="post in posts"
          :key="post.id"
          :post="post"
        />
      </div>
    </section>
  </main>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import PostCard from '@/components/PostCard.vue'
import api from '@/utils/axios'

const posts   = ref([])
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await api.get('/posts?page=0&size=6')
    posts.value = res.content || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.hero {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 6rem 2rem 4rem;
  background:
    radial-gradient(ellipse 80% 60% at 50% 40%, rgba(139,45,45,0.12) 0%, transparent 70%);
}
.hero-content { max-width: 720px; }
.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 6px 16px;
  border: 1px solid var(--border);
  border-radius: 20px;
  font-size: 0.75rem;
  letter-spacing: 0.1em;
  text-transform: uppercase;
  color: var(--accent);
  margin-bottom: 2rem;
}
.badge-dot {
  width: 5px; height: 5px;
  background: var(--accent);
  border-radius: 50%;
  animation: pulse 2s ease-in-out infinite;
}
@keyframes pulse { 0%,100%{opacity:1;} 50%{opacity:0.4;} }

.hero-title {
  font-family: 'Playfair Display', serif;
  font-size: clamp(3rem, 8vw, 5.5rem);
  font-weight: 700;
  line-height: 1.05;
  margin-bottom: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.4rem;
}
.gradient-text {
  background: linear-gradient(135deg, var(--accent) 0%, #e8c882 50%, #a07840 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  letter-spacing: 0.05em;
}
.hero-tagline {
  font-size: clamp(1rem, 2.5vw, 1.5rem);
  font-weight: 400;
  color: var(--text-secondary);
  font-style: italic;
  letter-spacing: 0.02em;
}
.hero-subtitle {
  font-size: 1rem;
  color: var(--text-secondary);
  line-height: 1.7;
  margin: 0.5rem auto 2.5rem;
  max-width: 520px;
}
.hero-cta { display: flex; gap: 1rem; justify-content: center; flex-wrap: wrap; }
.btn-primary {
  padding: 12px 28px;
  background: linear-gradient(135deg, var(--accent), #a07840);
  color: #0a0a0f;
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 600;
  text-decoration: none;
  transition: all 0.25s;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}
.btn-primary:hover { transform: translateY(-2px); box-shadow: 0 8px 24px rgba(201,169,110,0.3); }
.btn-secondary {
  padding: 12px 28px;
  background: transparent;
  color: var(--text-secondary);
  border: 1px solid var(--border);
  border-radius: 8px;
  font-size: 0.875rem;
  font-weight: 500;
  text-decoration: none;
  transition: all 0.25s;
  text-transform: uppercase;
  letter-spacing: 0.04em;
}
.btn-secondary:hover { border-color: var(--accent); color: var(--accent); }

/* ẢNH BÌA */
.cover-section {
  width: 100%;
}
.cover-wrap {
  position: relative;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 3rem;
}
.cover-img {
  width: 100%;
  height: auto;
  display: block;
  border-radius: 12px;
  filter: sepia(0.1) brightness(0.92);
}
.cover-overlay {
  position: absolute;
  inset: 0;
  border-radius: 12px;
  background: linear-gradient(
    to bottom,
    rgba(10,10,15,0.15) 0%,
    transparent 20%,
    transparent 80%,
    rgba(10,10,15,0.4) 100%
  );
  pointer-events: none;
}

/* LỜI GIỚI THIỆU */
.intro-section {
  padding: 6rem 3rem;
  max-width: 820px;
  margin: 0 auto;
  text-align: center;
}
.intro-title {
  font-family: 'Playfair Display', serif;
  font-size: clamp(1.8rem, 3vw, 2.5rem);
  font-weight: 700;
  margin: 0.75rem 0 2rem;
}
.intro-body {
  display: flex;
  flex-direction: column;
  gap: 1.2rem;
  text-align: left;
}
.intro-body p {
  font-size: 1rem;
  line-height: 1.85;
  color: var(--text-secondary);
}
.intro-cta {
  display: inline-block;
  margin-top: 2rem;
  font-size: 0.875rem;
  color: var(--accent);
  text-decoration: none;
  letter-spacing: 0.05em;
  border-bottom: 1px solid transparent;
  transition: border-color 0.2s;
}
.intro-cta:hover { border-color: var(--accent); }

/* SECTION */
.section { padding: 6rem 3rem; max-width: 1200px; margin: 0 auto; }
.section-header { display: flex; align-items: flex-end; justify-content: space-between; margin-bottom: 3rem; }
.section-label { font-size: 0.7rem; letter-spacing: 0.15em; text-transform: uppercase; color: var(--accent); margin-bottom: 0.5rem; }
.section-title { font-family: 'Playfair Display', serif; font-size: clamp(1.6rem, 3vw, 2.2rem); font-weight: 700; }
.section-link { font-size: 0.8rem; color: var(--text-muted); text-decoration: none; letter-spacing: 0.05em; text-transform: uppercase; }
.section-link:hover { color: var(--accent); }

.posts-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 1.5rem; }
.loading-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 1.5rem; }
.skeleton-card {
  height: 320px;
  background: var(--bg-card);
  border: 1px solid var(--border);
  border-radius: 12px;
  animation: shimmer 1.5s ease-in-out infinite;
}
@keyframes shimmer { 0%,100%{opacity:0.5;} 50%{opacity:1;} }

@media (max-width: 900px) {
  .posts-grid, .loading-grid { grid-template-columns: repeat(2, 1fr); }
  .section { padding: 4rem 1.5rem; }
  .intro-section { padding: 4rem 1.5rem; }
  .cover-wrap { padding: 0 1.5rem; }
}
@media (max-width: 580px) {
  .posts-grid, .loading-grid { grid-template-columns: 1fr; }
  .cover-wrap { padding: 0 1rem; }
}
</style>
