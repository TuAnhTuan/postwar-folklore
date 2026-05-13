-- Tự động tạo bảng nếu chưa tồn tại
-- Chạy mỗi lần app start (continue-on-error: true nên không crash nếu đã tồn tại)

CREATE TABLE IF NOT EXISTS posts (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    type         VARCHAR(50)  NOT NULL,
    title        VARCHAR(255) NOT NULL,
    content      TEXT         NOT NULL,
    author       VARCHAR(100),
    thumbnail_url VARCHAR(500),
    created_at   TIMESTAMP    DEFAULT now()
);

CREATE TABLE IF NOT EXISTS comments (
    id           BIGSERIAL    PRIMARY KEY,
    post_id      UUID         NOT NULL,
    user_uid     VARCHAR(128),
    display_name VARCHAR(100) NOT NULL,
    content      TEXT         NOT NULL,
    status       VARCHAR(50)  DEFAULT 'APPROVED',
    created_at   TIMESTAMP    DEFAULT now()
);

CREATE TABLE IF NOT EXISTS system_prompts (
    id             BIGSERIAL  PRIMARY KEY,
    active         BOOLEAN    NOT NULL DEFAULT false,
    prompt_content TEXT       NOT NULL,
    updated_at     TIMESTAMP  DEFAULT now()
);
