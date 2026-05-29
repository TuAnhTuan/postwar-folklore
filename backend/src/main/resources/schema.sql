-- ─────────────────────────────────────────────
--  schema.sql  —  chạy tự động mỗi lần app start
--  continue-on-error: true → không crash nếu đã tồn tại
-- ─────────────────────────────────────────────

-- ── LOCATIONS ──
CREATE TABLE IF NOT EXISTS locations (
    id      SERIAL       PRIMARY KEY,
    slug    VARCHAR(50)  UNIQUE NOT NULL,   -- 'quang-nam', 'da-nang', 'hanoi'
    name    VARCHAR(100) NOT NULL,          -- 'Quảng Nam', 'Đà Nẵng'
    region  VARCHAR(100),                   -- 'Miền Trung'
    country VARCHAR(10)  DEFAULT 'VN'
);

-- ── POSTS ──
CREATE TABLE IF NOT EXISTS posts (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    type          VARCHAR(50)  NOT NULL,
    location_id   INT          REFERENCES locations(id) ON DELETE SET NULL,
    title         VARCHAR(255) NOT NULL,
    content       TEXT         NOT NULL,
    author        VARCHAR(100),
    thumbnail_url VARCHAR(500),
    created_at    TIMESTAMP    DEFAULT now()
);

-- Migration: xóa cột location cũ (enum string) nếu vẫn còn
ALTER TABLE posts DROP COLUMN IF EXISTS location;

-- ── COMMENTS ──
CREATE TABLE IF NOT EXISTS comments (
    id           BIGSERIAL                PRIMARY KEY,
    post_id      UUID                     NOT NULL,
    user_uid     VARCHAR(128),
    display_name VARCHAR(100)             NOT NULL,
    content      TEXT                     NOT NULL,
    status       VARCHAR(50)              DEFAULT 'APPROVED',
    created_at   TIMESTAMP WITH TIME ZONE DEFAULT now()
);

-- ── SYSTEM PROMPTS ──
CREATE TABLE IF NOT EXISTS system_prompts (
    id             BIGSERIAL  PRIMARY KEY,
    active         BOOLEAN    NOT NULL DEFAULT false,
    prompt_content TEXT       NOT NULL,
    updated_at     TIMESTAMP  DEFAULT now()
);

-- ── SEED: địa điểm mặc định (idempotent) ──
INSERT INTO locations (slug, name, region, country)
VALUES
    ('quang-nam', 'Quảng Nam', 'Miền Trung', 'VN'),
    ('da-nang',   'Đà Nẵng',   'Miền Trung', 'VN')
ON CONFLICT (slug) DO NOTHING;
