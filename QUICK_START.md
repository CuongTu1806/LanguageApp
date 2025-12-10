# 🚀 Quick Start Guide - Vocabulary Enrichment

## ⚡ 3 Bước để chạy ngay

### 1️⃣ Chạy SQL (30 giây)
```sql
USE TiengTrungDB;
ALTER TABLE vocabulary ADD COLUMN image_path VARCHAR(500) NULL;
```

### 2️⃣ Lấy Pixabay API Key (2 phút)
1. Đăng ký miễn phí: https://pixabay.com/accounts/register/
2. Lấy key: https://pixabay.com/api/docs/
3. Paste vào `application.properties`:
```properties
pixabay.api.key=PASTE_YOUR_KEY_HERE
```

### 3️⃣ Chạy app và test (1 phút)
```powershell
.\mvnw.cmd spring-boot:run
```

Mở browser: http://localhost:8080/courses/zh/A/lessons/1

---

## 🎯 Lấy hình ảnh

### Option A: Test với 1 bài trước (khuyên dùng)
```bash
# Mở Postman hoặc curl
POST http://localhost:8080/admin/vocabulary/enrich-lesson/zh/A/1
```
⏱️ Mất ~30 giây cho 1 bài (giả sử 30 từ)

### Option B: Lấy toàn bộ 896 từ
```bash
POST http://localhost:8080/admin/vocabulary/enrich-all
```
⏱️ Mất ~15-20 phút, chạy background

---

## 📱 Xem kết quả

Vào bất kỳ bài học nào:
```
http://localhost:8080/courses/zh/A/lessons/1
```

**Giao diện mới**:
- ✅ 2 cột: Danh sách từ (trái) + Chi tiết (phải)
- ✅ Click từ → hiển thị hình ảnh + ví dụ
- ✅ Giống y hệt app Hanzi

---

## ⚠️ Troubleshooting nhanh

**Lỗi compile?**
```powershell
.\mvnw.cmd clean install -DskipTests
```

**Không tìm thấy hình?**
- Đảm bảo `meaning` có tiếng Anh: "táo (apple)"
- Hoặc dùng pinyin để search

**API key invalid?**
- Check file `application.properties`
- Restart app sau khi đổi config

---

## 📚 Chi tiết đầy đủ
Xem file: `VOCABULARY_ENRICHMENT_GUIDE.md`
