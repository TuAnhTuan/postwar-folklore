#!/bin/bash
# ============================================================
# setup-github.sh
# Script đẩy project lên GitHub và tạo repo mới.
# Chạy script này TRÊN MÁY của bạn (không phải trong sandbox).
#
# Yêu cầu: gh CLI đã cài (brew install gh) và đã đăng nhập (gh auth login)
# ============================================================

set -e

REPO_NAME="postwar-folklore"
REPO_DESC="Postwar Folklore — A post-war legends archive with AI storytelling chatbot (Vue 3 + Spring Boot)"

echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  Setup GitHub Repo: $REPO_NAME"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"

# Kiểm tra gh CLI
if ! command -v gh &> /dev/null; then
  echo "❌ gh CLI chưa được cài. Chạy: brew install gh"
  exit 1
fi

# Kiểm tra đã đăng nhập chưa
if ! gh auth status &> /dev/null; then
  echo "🔑 Chưa đăng nhập GitHub. Đang mở trình duyệt..."
  gh auth login
fi

echo ""
echo "📦 Tạo repo GitHub (private)..."
gh repo create "$REPO_NAME" \
  --private \
  --description "$REPO_DESC" \
  --source=. \
  --remote=origin \
  --push

echo ""
echo "✅ Đã tạo repo và push thành công!"
echo ""

# Hiển thị URL
GITHUB_USER=$(gh api user --jq '.login')
echo "🔗 Repo URL: https://github.com/$GITHUB_USER/$REPO_NAME"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo "  Bước tiếp theo:"
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
echo ""
echo "1. BACKEND — copy .env.example và điền API keys:"
echo "   cp backend/.env.example backend/.env"
echo ""
echo "2. FRONTEND — copy .env.example và điền Firebase config:"
echo "   cp frontend/.env.example frontend/.env"
echo ""
echo "3. Cài dependencies frontend:"
echo "   cd frontend && npm install"
echo ""
echo "4. Chạy dev local:"
echo "   # Terminal 1 (backend):"
echo "   cd backend && mvn spring-boot:run"
echo "   # Terminal 2 (frontend):"
echo "   cd frontend && npm run dev"
echo ""
echo "5. Deploy lên Render:"
echo "   - Truy cập https://render.com"
echo "   - New > Blueprint > kết nối repo GitHub này"
echo "   - Render sẽ đọc render.yaml và tạo tự động 3 services"
echo "   - Điền các Environment Variables còn thiếu (Firebase, Cloudinary, Gemini API key)"
echo ""
echo "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━"
