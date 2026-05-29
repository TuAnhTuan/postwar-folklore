-- ═══════════════════════════════════════════════════════════════
--  supabase-init.sql  —  Khởi tạo DB mới trên Supabase
--
--  Cách dùng:
--    1. Vào Supabase Dashboard → SQL Editor
--    2. Paste toàn bộ file này và chạy
--    3. Kiểm tra tab "Table Editor" để xác nhận
--
--  File này an toàn để chạy nhiều lần (idempotent).
-- ═══════════════════════════════════════════════════════════════

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- ─────────────────────────────────────────────
--  LOCATIONS  (bảng mới — thay thế enum cũ)
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS locations (
    id      SERIAL       PRIMARY KEY,
    slug    VARCHAR(50)  UNIQUE NOT NULL,
    name    VARCHAR(100) NOT NULL,
    region  VARCHAR(100),
    country VARCHAR(10)  NOT NULL DEFAULT 'VN'
);

CREATE INDEX IF NOT EXISTS idx_locations_slug ON locations (slug);

-- Dữ liệu mặc định — thêm địa điểm mới chỉ cần INSERT vào đây
INSERT INTO locations (slug, name, region, country) VALUES
    ('quang-nam', 'Quảng Nam', 'Miền Trung', 'VN'),
    ('da-nang',   'Đà Nẵng',   'Miền Trung', 'VN')
ON CONFLICT (slug) DO NOTHING;

-- ─────────────────────────────────────────────
--  POSTS
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS posts (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    type          VARCHAR(50)  NOT NULL,              -- THEORY | LEGEND
    location_id   INT          REFERENCES locations(id) ON DELETE SET NULL,
    title         VARCHAR(255) NOT NULL,
    content       TEXT         NOT NULL,
    author        VARCHAR(100),
    thumbnail_url VARCHAR(500),
    created_at    TIMESTAMP    NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_posts_type        ON posts (type);
CREATE INDEX IF NOT EXISTS idx_posts_location_id ON posts (location_id);
CREATE INDEX IF NOT EXISTS idx_posts_type_loc    ON posts (type, location_id);
CREATE INDEX IF NOT EXISTS idx_posts_created_at  ON posts (created_at DESC);

-- ─────────────────────────────────────────────
--  COMMENTS
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS comments (
    id           BIGSERIAL                PRIMARY KEY,
    post_id      UUID                     NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
    user_uid     VARCHAR(128),
    display_name VARCHAR(100)             NOT NULL,
    content      TEXT                     NOT NULL,
    status       VARCHAR(50)              NOT NULL DEFAULT 'APPROVED',
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_comments_post_id ON comments (post_id);
CREATE INDEX IF NOT EXISTS idx_comments_status  ON comments (status);

-- ─────────────────────────────────────────────
--  SYSTEM PROMPTS
-- ─────────────────────────────────────────────
CREATE TABLE IF NOT EXISTS system_prompts (
    id             BIGSERIAL  PRIMARY KEY,
    active         BOOLEAN    NOT NULL DEFAULT false,
    prompt_content TEXT       NOT NULL,
    updated_at     TIMESTAMP  NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_system_prompts_one_active
    ON system_prompts (active)
    WHERE active = true;

-- ─────────────────────────────────────────────
--  THÊM ĐỊA ĐIỂM MỚI (ví dụ)
-- ─────────────────────────────────────────────
-- Khi muốn mở rộng, chỉ cần chạy:
--
-- INSERT INTO locations (slug, name, region, country)
-- VALUES ('hanoi', 'Hà Nội', 'Miền Bắc', 'VN')
-- ON CONFLICT (slug) DO NOTHING;
--
-- INSERT INTO locations (slug, name, region, country)
-- VALUES ('ho-chi-minh', 'TP. Hồ Chí Minh', 'Miền Nam', 'VN')
-- ON CONFLICT (slug) DO NOTHING;
