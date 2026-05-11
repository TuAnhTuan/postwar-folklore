# ════════════════════════════════════════════════════════
# STAGE 1 — BUILD
# Dùng Node alpine để cài dependencies và build Vue
# ════════════════════════════════════════════════════════
FROM node:22-alpine AS builder

WORKDIR /app

# Copy package files trước để cache layer node_modules
# (chỉ re-install khi package.json / package-lock.json thay đổi)
COPY package.json package-lock.json* ./
RUN npm ci --prefer-offline --no-audit

# Copy toàn bộ source
COPY . .

# Build production bundle
# Biến VITE_ phải được truyền vào lúc build (build-time env)
ARG VITE_API_BASE_URL=/api
ARG VITE_FIREBASE_API_KEY
ARG VITE_FIREBASE_AUTH_DOMAIN
ARG VITE_FIREBASE_PROJECT_ID
ARG VITE_FIREBASE_STORAGE_BUCKET
ARG VITE_FIREBASE_MESSAGING_SENDER_ID
ARG VITE_FIREBASE_APP_ID

ENV VITE_API_BASE_URL=$VITE_API_BASE_URL \
    VITE_FIREBASE_API_KEY=$VITE_FIREBASE_API_KEY \
    VITE_FIREBASE_AUTH_DOMAIN=$VITE_FIREBASE_AUTH_DOMAIN \
    VITE_FIREBASE_PROJECT_ID=$VITE_FIREBASE_PROJECT_ID \
    VITE_FIREBASE_STORAGE_BUCKET=$VITE_FIREBASE_STORAGE_BUCKET \
    VITE_FIREBASE_MESSAGING_SENDER_ID=$VITE_FIREBASE_MESSAGING_SENDER_ID \
    VITE_FIREBASE_APP_ID=$VITE_FIREBASE_APP_ID

RUN npm run build

# ════════════════════════════════════════════════════════
# STAGE 2 — SERVE
# Dùng Nginx alpine (cực nhẹ ~25MB) để serve static files
# ════════════════════════════════════════════════════════
FROM nginx:1.27-alpine AS runtime

# Xóa config mặc định
RUN rm /etc/nginx/conf.d/default.conf

# Copy nginx config tùy chỉnh
COPY nginx.conf /etc/nginx/conf.d/app.conf

# Copy static files từ stage builder
COPY --from=builder /app/dist /usr/share/nginx/html

# Port Nginx lắng nghe
EXPOSE 80

# Healthcheck
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
    CMD wget -qO- http://localhost:80/ | grep -q "<!DOCTYPE html>" || exit 1

CMD ["nginx", "-g", "daemon off;"]
