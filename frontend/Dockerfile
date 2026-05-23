# ════════════════════════════════════════════════════════
# STAGE 1 — BUILD
# Dùng Node alpine để cài dependencies và build Vue
# ════════════════════════════════════════════════════════
FROM node:22-alpine AS builder

WORKDIR /app

# Copy package files trước để cache layer node_modules
# (chỉ re-install khi package.json / package-lock.json thay đổi)
COPY package.json package-lock.json* ./
# Dùng npm ci nếu có lockfile (nhanh hơn), fallback npm install nếu không có
RUN if [ -f package-lock.json ]; then npm ci --no-audit; else npm install --no-audit; fi

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
ARG VITE_CLOUDINARY_CLOUD_NAME
ARG VITE_CLOUDINARY_UPLOAD_PRESET

ENV VITE_API_BASE_URL=$VITE_API_BASE_URL \
    VITE_FIREBASE_API_KEY=$VITE_FIREBASE_API_KEY \
    VITE_FIREBASE_AUTH_DOMAIN=$VITE_FIREBASE_AUTH_DOMAIN \
    VITE_FIREBASE_PROJECT_ID=$VITE_FIREBASE_PROJECT_ID \
    VITE_FIREBASE_STORAGE_BUCKET=$VITE_FIREBASE_STORAGE_BUCKET \
    VITE_FIREBASE_MESSAGING_SENDER_ID=$VITE_FIREBASE_MESSAGING_SENDER_ID \
    VITE_FIREBASE_APP_ID=$VITE_FIREBASE_APP_ID \
    VITE_CLOUDINARY_CLOUD_NAME=$VITE_CLOUDINARY_CLOUD_NAME \
    VITE_CLOUDINARY_UPLOAD_PRESET=$VITE_CLOUDINARY_UPLOAD_PRESET

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

# Đảm bảo nginx có quyền đọc tất cả static files (ảnh, font, js...)
RUN chmod -R 755 /usr/share/nginx/html

# Port Nginx lắng nghe
EXPOSE 80

# Healthcheck
HEALTHCHECK --interval=30s --timeout=5s --retries=3 \
    CMD wget -qO- http://localhost:80/ | grep -q "<!DOCTYPE html>" || exit 1

CMD ["nginx", "-g", "daemon off;"]
