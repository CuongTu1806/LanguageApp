# Hướng dẫn sử dụng tính năng Vocabulary Enrichment

## 📋 Tổng quan

Tính năng này giúp tự động lấy hình ảnh minh họa cho các từ vựng từ Pixabay API và hiển thị giao diện học tập đẹp mắt giống app Hanzi.

## 🚀 Các bước cài đặt

### 1. Chạy migration SQL

Mở MySQL Workbench hoặc command line và chạy file:
```bash
mysql -u root -p TiengTrungDB < migration_add_image_path.sql
```

Hoặc chạy trực tiếp trong MySQL Workbench:
```sql
USE TiengTrungDB;
ALTER TABLE vocabulary 
ADD COLUMN image_path VARCHAR(500) NULL 
COMMENT 'Đường dẫn hình ảnh minh họa từ vựng';
```

### 2. Đăng ký Pixabay API Key (MIỄN PHÍ)

1. Truy cập: https://pixabay.com/accounts/register/
2. Đăng ký tài khoản miễn phí
3. Vào https://pixabay.com/api/docs/
4. Copy API Key của bạn

### 3. Cập nhật application.properties

Mở file `src/main/resources/application.properties` và thay thế:
```properties
pixabay.api.key=YOUR_PIXABAY_API_KEY_HERE
```

Bằng API key thực của bạn:
```properties
pixabay.api.key=123456789-abcdefghijklmnop
```

### 4. Tạo thư mục lưu hình ảnh

Chạy trong PowerShell:
```powershell
mkdir src\main\resources\static\images\vocab
```

### 5. Thêm dependency vào pom.xml (nếu chưa có)

Đảm bảo có Jackson để xử lý JSON từ Pixabay API:
```xml
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
</dependency>
```

## 🎯 Cách sử dụng

### A. Tự động lấy hình cho TẤT CẢ từ vựng

1. Khởi động ứng dụng:
```powershell
.\mvnw.cmd spring-boot:run
```

2. Gọi API (bằng Postman, curl, hoặc browser):
```
POST http://localhost:8080/admin/vocabulary/enrich-all
```

⚠️ **LƯU Ý**: Process này sẽ chạy trong background và mất khoảng 15-20 phút cho 896 từ (1 giây/từ để tránh spam API).

**Theo dõi tiến trình** bằng cách xem console log:
```
✓ [100/896] Successfully enriched: 苹果 -> /images/vocab/apple_123.jpg
✓ [101/896] Successfully enriched: 香蕉 -> /images/vocab/banana_124.jpg
...
```

### B. Lấy hình cho một từ cụ thể

```
POST http://localhost:8080/admin/vocabulary/enrich/{vocabId}
```

Ví dụ:
```
POST http://localhost:8080/admin/vocabulary/enrich/1
```

### C. Lấy hình cho một bài học cụ thể

```
POST http://localhost:8080/admin/vocabulary/enrich-lesson/{lang}/{level}/{lessonNo}
```

Ví dụ:
```
POST http://localhost:8080/admin/vocabulary/enrich-lesson/zh/A/1
```

## 🎨 Xem kết quả

1. Truy cập bất kỳ bài học nào:
```
http://localhost:8080/courses/zh/A/lessons/1
```

2. Giao diện mới sẽ hiển thị:
   - **Cột trái**: Danh sách từ vựng (click để xem chi tiết)
   - **Cột phải**: 
     - Từ Hán tự lớn
     - Phiên âm (pinyin)
     - Nghĩa tiếng Việt
     - Ví dụ câu (nếu có)
     - Hình ảnh minh họa (nếu có)
     - Thống kê học tập

## 📊 Pixabay API Limits

- **Free tier**: 
  - 100 requests/phút
  - 5,000 requests/tháng
  - Hoàn toàn miễn phí
  - Không cần credit card

- **Rate limiting trong code**: 
  - Script tự động đợi 1 giây giữa mỗi request
  - Đảm bảo không vượt quá giới hạn

## 🔧 Troubleshooting

### Vấn đề 1: "No images found for keyword"
**Nguyên nhân**: Từ vựng không có nghĩa tiếng Anh hoặc keyword không tìm thấy hình trên Pixabay.

**Giải pháp**: 
- Cập nhật cột `meaning` trong database thêm nghĩa tiếng Anh trong ngoặc:
  ```sql
  UPDATE vocabulary 
  SET meaning = 'táo (apple)' 
  WHERE word = '苹果';
  ```

### Vấn đề 2: "API key invalid"
**Nguyên nhân**: API key chưa được cấu hình đúng.

**Giải pháp**:
- Kiểm tra lại file `application.properties`
- Đảm bảo không có khoảng trắng thừa
- Restart ứng dụng sau khi đổi config

### Vấn đề 3: Hình ảnh không hiển thị
**Nguyên nhân**: Đường dẫn file không đúng hoặc thư mục chưa được tạo.

**Giải pháp**:
```powershell
# Kiểm tra thư mục đã tồn tại
Test-Path "src\main\resources\static\images\vocab"

# Tạo nếu chưa có
mkdir src\main\resources\static\images\vocab -Force
```

### Vấn đề 4: Download ảnh thất bại
**Nguyên nhân**: Connection timeout hoặc file size quá lớn.

**Giải pháp**: Code đã có xử lý timeout (5 giây). Nếu vẫn bị, có thể tăng timeout trong `ImageService.java`:
```java
connection.setConnectTimeout(10000); // Tăng từ 5s lên 10s
connection.setReadTimeout(10000);
```

## 🎯 Tối ưu hóa

### 1. Chạy enrichment trong giờ thấp điểm
```java
// Có thể thêm scheduled task
@Scheduled(cron = "0 0 2 * * ?") // Chạy lúc 2h sáng
public void scheduledEnrichment() {
    enrichmentService.enrichAllVocabulary();
}
```

### 2. Chỉ enrich những từ thường dùng
Thêm filter trong `VocabularyEnrichmentService`:
```java
// Chỉ lấy hình cho từ HSK 1-3
List<VocabularyEntity> allVocabs = vocabularyRepository
    .findByLevelIn(Arrays.asList("A", "B", "C"));
```

### 3. Cache kết quả search
Lưu mapping `keyword -> imageUrl` vào Redis hoặc database để tránh search lại.

## 📝 Các bước tiếp theo (tùy chọn)

1. **Thêm audio phát âm**: Tích hợp Google Text-to-Speech API
2. **Lấy ví dụ câu tự động**: Web scraping từ Tatoeba.org
3. **Thêm flashcard mode**: Học từ theo kiểu flashcard
4. **Xuất Anki deck**: Export từ vựng ra file Anki

## ❓ FAQ

**Q: Có cần trả tiền cho Pixabay không?**
A: Không, hoàn toàn miễn phí với 5,000 requests/tháng.

**Q: Hình ảnh có bản quyền không?**
A: Tất cả hình từ Pixabay đều là CC0 (Public Domain), sử dụng tự do cho mục đích thương mại.

**Q: Tôi có thể dùng API khác không?**
A: Có, bạn có thể thay thế bằng Unsplash, Pexels, hoặc Google Custom Search. Chỉ cần sửa `ImageService.java`.

**Q: 896 từ mất bao lâu để enrich?**
A: Khoảng 15-20 phút (1 giây/từ). Bạn có thể giảm delay xuống 500ms nếu muốn nhanh hơn.

## 📧 Support

Nếu gặp vấn đề, check console log để xem lỗi chi tiết.
