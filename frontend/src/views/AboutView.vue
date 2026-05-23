<template>
  <main class="ve-hoai-tu">
    <!-- HEADER -->
    <section class="page-hero">
      <div class="page-hero-content">
        <h1 class="page-title">Về Hoài tự</h1>
        <p class="page-desc">
          "Hoài" 懷 trong hoài niệm, "Tự" 叙 trong tự sự
        </p>
      </div>
    </section>

    <!-- NỘI DUNG CHÍNH -->
    <section class="content-section">
      <div class="content-inner">

        <!-- Lời chào -->
        <div class="intro-block">
          <p class="greeting">Thương chào bạn,</p>
          <p>
            Tụi mình là <strong>Hoài tự</strong> — những người trẻ mượn lời kể để gom nhặt và gìn giữ những hoài niệm xưa cũ. Tụi mình là một nhóm nghiên cứu nhỏ thuộc Khoa Ngữ văn - Truyền thông, Trường Đại học Sư phạm - Đại học Đà Nẵng.
          </p>
        </div>

        <div class="divider-ornament">—— ✦ ——</div>

        <!-- Câu chuyện -->
        <div class="story-block">
          <p>
            Sau quá trình dày công tìm hiểu về đề tài <em>"Ký ức tập thể và Folklore đương đại: Nghiên cứu trường hợp truyền thuyết hậu chiến tại Quảng Nam - Đà Nẵng"</em>, nhóm nhận thấy không gian số đang mở ra một lối đi mới cho các giá trị văn hóa dân gian được gìn giữ, lưu truyền và tái tạo bằng những cách thức khác trước.
          </p>
          <p>
            Những truyền thuyết về người chết trong chiến tranh không chỉ quẩn quanh nơi sân đình, bến nước, ngõ chợ làng quê; chúng còn len vào vỉa hè, ngõ nhỏ, khu chung cư, rồi tiếp tục cắm rễ và lan truyền trên các diễn đàn trực tuyến.
          </p>
          <p>
            Trong không gian ấy, mỗi người dùng Internet đều có thể trở thành người kể chuyện — khởi xướng, truyền tải, bình luận, chia sẻ hoặc tái tạo câu chuyện từ những điều mình từng nghe, từng thấy, từng tin hoặc từng chiêm nghiệm. Văn hóa dân gian vì thế không nằm yên trong quá khứ, mà tiếp tục có những bước chuyển di từ <em>Folklore</em> sang <em>Digital Folklore</em>.
          </p>
        </div>

        <div class="highlight-box">
          <p>
            Men theo dòng chuyển động đó, <strong>Hoài tự</strong> được tạo nên như một căn nhà nhỏ để lưu giữ các truyền thuyết hậu chiến, những lời kể về người chết, linh hồn, báo mộng, hài cốt, không gian thiêng và ký ức cộng đồng sau chiến tranh.
          </p>
        </div>

        <div class="question-block">
          <p class="question">Vậy truyền thuyết hậu chiến là gì? Có chăng chỉ là chuyện ma?</p>
          <router-link to="/truyen-thuyet" class="explore-btn">Mời bạn ghé nhà Hoài tự →</router-link>
        </div>

      </div>
    </section>
  </main>
</template>

<script setup>
// Static page, no data fetching needed
</script>

<style scoped>
.ve-hoai-tu {
  padding-top: 64px;
}

.page-hero {
  min-height: 40vh;
  display: flex;
  align-items: center;
  justify-content: center;
  text-align: center;
  padding: 5rem 2rem 3rem;
  background: radial-gradient(ellipse 70% 60% at 50% 40%, rgba(139,45,45,0.1) 0%, transparent 70%);
  border-bottom: 1px solid var(--border);
}
.page-hero-content { max-width: 640px; }
.section-label {
  font-size: 0.7rem;
  letter-spacing: 0.15em;
  text-transform: uppercase;
  color: var(--accent);
  margin-bottom: 1rem;
}
.page-title {
  font-family: 'Playfair Display', serif;
  font-size: clamp(2.5rem, 6vw, 4rem);
  font-weight: 700;
  background: linear-gradient(135deg, var(--accent) 0%, #e8c882 50%, #a07840 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  margin-bottom: 1rem;
}
.page-desc {
  font-size: 1rem;
  color: var(--text-muted);
  font-style: italic;
  letter-spacing: 0.04em;
}

.content-section {
  padding: 5rem 3rem;
  max-width: 760px;
  margin: 0 auto;
}
.content-inner {
  display: flex;
  flex-direction: column;
  gap: 2.5rem;
}

.intro-block { display: flex; flex-direction: column; gap: 1.2rem; }
.greeting {
  font-family: 'Playfair Display', serif;
  font-size: 1.3rem;
  color: var(--accent);
  font-weight: 600;
}
.intro-block p, .story-block p {
  font-size: 1rem;
  line-height: 1.9;
  color: var(--text-secondary);
}
.intro-block strong, .highlight-box strong {
  color: var(--text-primary);
  font-weight: 600;
}

.divider-ornament {
  text-align: center;
  color: var(--text-muted);
  font-size: 0.85rem;
  letter-spacing: 0.2em;
}

.story-block {
  display: flex;
  flex-direction: column;
  gap: 1.4rem;
}
.story-block em { color: var(--accent); font-style: italic; }

.highlight-box {
  padding: 1.5rem 2rem;
  background: rgba(201,169,110,0.06);
  border-left: 3px solid var(--accent);
  border-radius: 0 8px 8px 0;
}
.highlight-box p {
  font-size: 1rem;
  line-height: 1.85;
  color: var(--text-secondary);
}

.question-block {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 1.5rem;
  padding-top: 1rem;
  border-top: 1px solid var(--border);
}
.question {
  font-family: 'Playfair Display', serif;
  font-size: 1.15rem;
  color: var(--text-primary);
  font-style: italic;
  line-height: 1.6;
}
.explore-btn {
  display: inline-block;
  padding: 10px 24px;
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
.explore-btn:hover { transform: translateY(-2px); box-shadow: 0 8px 24px rgba(201,169,110,0.3); }

@media (max-width: 700px) {
  .content-section { padding: 3rem 1.5rem; }
  .page-hero { padding: 4rem 1.5rem 2.5rem; }
}
</style>
