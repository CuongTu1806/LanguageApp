# 📚 Multi-Language Learning Application - TiengTrungApp

## 🌟 Giới thiệu

**TiengTrungApp** là một ứng dụng web học ngoại ngữ toàn diện (tiếng Anh và tiếng Trung), được xây dựng bằng Spring Boot, giúp người dùng học từ vựng một cách hiệu quả thông qua hệ thống bài học có cấu trúc, quiz đa dạng và lịch ôn tập thông minh dựa trên thuật toán Spaced Repetition.

## ✨ Tính năng chính

### 🎯 Hệ thống bài học
- **Đa ngôn ngữ**: Hỗ trợ học cả tiếng Anh và tiếng Trung với database từ vựng phong phú
- **Bài học theo cấp độ**: Phân chia bài học theo ngôn ngữ (language code) và cấp độ (level) phù hợp với trình độ người học
- **Bài học cá nhân hóa**: Người dùng có thể tạo bài học riêng (Personal Lesson) với từ vựng tùy chỉnh
- **Bài học hệ thống**: Bài học được thiết kế sẵn từ hệ thống (System Lesson) với nội dung chuẩn hóa
- **Quản lý chủ đề**: Tổ chức từ vựng theo chủ đề (Topic Management) để dễ dàng tra cứu và học tập

### 📝 Hệ thống Quiz đa dạng
Ứng dụng hỗ trợ **4 chế độ quiz** khác nhau:
1. **Word + Pronunciation → Meaning**: Cho từ và phiên âm (pinyin cho tiếng Trung, IPA cho tiếng Anh), chọn nghĩa đúng
2. **Word → Meaning**: Cho từ ngoại ngữ (tiếng Anh hoặc tiếng Trung), chọn nghĩa tiếng Việt
3. **Meaning → Word**: Cho nghĩa tiếng Việt, chọn từ ngoại ngữ đúng
4. **Fill-in-the-blank**: Điền từ vào chỗ trống trong câu

### 🔄 Hệ thống ôn tập thông minh (Spaced Repetition)
- **Review Schedule**: Lịch ôn tập tự động dựa trên mức độ ghi nhớ của người dùng
- **Review Stages**: Theo dõi giai đoạn ôn tập (0, 1, 2, 3...) và tự động lên lịch
- **Next Review Tracking**: Tính toán thời điểm ôn tập tiếp theo tối ưu
- **Learning Dashboard**: Bảng điều khiển trực quan hiển thị tiến độ học tập

### 📊 Theo dõi tiến độ
- **Vocabulary Statistics**: Thống kê chi tiết về từng từ vựng (số lần xem, điểm số, thời gian học)
- **Quiz History**: Lịch sử làm quiz với kết quả chi tiết
- **Wrong Vocabulary Tracking**: Theo dõi từ trả lời sai để ôn tập lại
- **Learning Analytics**: Phân tích dữ liệu học tập để tối ưu hóa quá trình học

### 🖼️ Tích hợp hình ảnh
- **Pixabay API Integration**: Tự động tải hình ảnh minh họa cho từ vựng từ Pixabay
- **Image Storage**: Lưu trữ hình ảnh trong thư mục local để tăng tốc độ tải

### 🌍 Hỗ trợ đa ngôn ngữ
- **Tiếng Trung**: Từ vựng với phiên âm pinyin, hán tự giản thể/phồn thể
- **Tiếng Anh**: Từ vựng với phiên âm IPA, ví dụ câu
- **Language Switch**: Dễ dàng chuyển đổi giữa các ngôn ngữ học tập

### 📚 Quản lý bài tập
- **Assignments**: Hệ thống bài tập và assignment cho người học

## 🛠️ Công nghệ sử dụng

### Backend
- **Spring Boot 4.0.0**: Framework chính
- **Spring Data JPA**: Quản lý database và ORM
- **Hibernate**: ORM framework
- **MySQL**: Database quản lý dữ liệu
- **Flyway**: Database migration và version control
- **Lombok**: Giảm boilerplate code

### Frontend
- **Thymeleaf**: Template engine cho server-side rendering
- **HTML/CSS/JavaScript**: Frontend stack cơ bản
- **Bootstrap** (implied): UI framework

### Tools & Libraries
- **Jackson**: JSON serialization/deserialization
- **Maven**: Build tool và dependency management
- **Pixabay API**: Tích hợp hình ảnh

### Java Version
- **Java 17**: LTS version

## 📁 Cấu trúc dự án

```
TiengTrungApp/
├── src/
│   ├── main/
│   │   ├── java/com/example/appNN/
│   │   │   ├── controller/         # REST Controllers và Web Controllers
│   │   │   │   ├── LessonController.java
│   │   │   │   ├── QuizController.java
│   │   │   │   ├── ReviewController.java
│   │   │   │   ├── CustomLessonController.java
│   │   │   │   └── ...
│   │   │   ├── service/            # Business Logic Layer
│   │   │   │   ├── LessonService.java
│   │   │   │   ├── QuizService.java
│   │   │   │   ├── UserVocabStatsService.java
│   │   │   │   └── ImageService.java
│   │   │   ├── repository/         # Data Access Layer
│   │   │   ├── entity/             # JPA Entities
│   │   │   │   ├── LessonEntity.java
│   │   │   │   ├── VocabularyEntity.java
│   │   │   │   ├── UserVocabularyEntity.java
│   │   │   │   └── ...
│   │   │   ├── dto/                # Data Transfer Objects
│   │   │   ├── request/            # Request models
│   │   │   ├── response/           # Response models
│   │   │   ├── model/              # Domain models
│   │   │   └── utils/              # Utility classes
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── db/migration/       # Flyway migration scripts
│   │       ├── static/             # Static resources
│   │       │   └── images/vocab/   # Vocabulary images
│   │       └── templates/          # Thymeleaf templates
│   │           ├── lesson_list.html
│   │           ├── lesson_view.html
│   │           ├── quiz_do.html
│   │           ├── learning_dashboard.html
│   │           └── ...
│   └── test/                       # Test classes
├── pom.xml                         # Maven configuration
└── mvnw, mvnw.cmd                  # Maven wrapper
```

## 🗄️ Database Schema

### Core Tables
- **lesson**: Quản lý bài học (system và personal)
- **vocabulary**: Từ vựng tiếng Trung với phiên âm, nghĩa, hình ảnh
- **user_vocabulary**: Từ vựng cá nhân của người dùng
- **lesson_vocabulary**: Mapping giữa bài học và từ vựng
- **app_user**: Thông tin người dùng
- **user_vocab_stats**: Thống kê học tập của từng từ vựng
- **lesson_quiz_attempt**: Lịch sử làm quiz
- **lesson_quiz_wrong_vocab**: Từ trả lời sai trong quiz
- **topic**: Quản lý chủ đề từ vựng

## ⚙️ Cài đặt và chạy

### Prerequisites
- Java 17 hoặc cao hơn
- Maven 3.6+
- MySQL 8.0+
- IDE: IntelliJ IDEA, Eclipse, hoặc VS Code

### Bước 1: Clone repository
```bash
git clone <repository-url>
cd TiengTrungApp
```

### Bước 2: Cấu hình Database
1. Tạo database MySQL:
```sql
CREATE DATABASE TiengTrungDB CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. Cấu hình `application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/TiengTrungDB
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### Bước 3: Cấu hình Pixabay API (Optional)
Đăng ký API key miễn phí tại [Pixabay API](https://pixabay.com/api/docs/) và cập nhật:
```properties
pixabay.api.key=your_api_key
```

### Bước 4: Build và Run
```bash
# Build project
./mvnw clean install

# Run application
./mvnw spring-boot:run
```

### Bước 5: Truy cập ứng dụng
Mở browser và truy cập: `http://localhost:8080`

## 🚀 API Endpoints

### Lesson Management
- `GET /lessons/{lang}/{level}/lessons` - Danh sách bài học theo ngôn ngữ và cấp độ
- `GET /lessons/{lessonId}/view` - Chi tiết bài học
- `GET /lessons/custom-lesson/create` - Tạo bài học cá nhân

### Quiz System
- `GET /lessons/{lessonId}/quiz?mode={1-4}` - Bắt đầu quiz với mode tương ứng
- `POST /lessons/{lessonId}/quiz/submit` - Submit kết quả quiz
- `GET /lessons/quiz-history` - Lịch sử làm quiz

### Review System
- `GET /reviews` - Lịch ôn tập
- `POST /reviews/{lessonId}/complete` - Hoàn thành ôn tập

### Vocabulary Management
- `GET /api/vocabularies/search` - Tìm kiếm từ vựng
- `POST /api/vocabularies/custom` - Thêm từ vựng tùy chỉnh

## 🎨 Screenshots

*(Có thể thêm screenshots của ứng dụng tại đây)*

## 📈 Thuật toán Spaced Repetition

Ứng dụng sử dụng thuật toán Spaced Repetition để tối ưu hóa quá trình ghi nhớ:
- **Stage 0**: Bài học mới, chưa ôn tập
- **Stage 1**: Ôn tập lần 1 (sau 1 ngày)
- **Stage 2**: Ôn tập lần 2 (sau 3 ngày)
- **Stage 3**: Ôn tập lần 3 (sau 7 ngày)
- **Stage 4+**: Ôn tập định kỳ (14, 30, 60 ngày...)

Khoảng thời gian giữa các lần ôn tập được tự động điều chỉnh dựa trên kết quả quiz của người dùng.

## 🧪 Testing

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=LessonServiceTest
```

## 🔜 Tính năng dự kiến

- [ ] Hệ thống xác thực người dùng (Spring Security)
- [ ] Chế độ học tập gamification với điểm số và achievements
- [ ] Mở rộng thêm ngôn ngữ khác (Nhật, Hàn, Pháp, etc.)
- [ ] Mobile app (React Native/Flutter)
- [ ] Text-to-Speech cho phát âm
- [ ] Flashcard mode
- [ ] Social features: chia sẻ bài học, thi đua với bạn bè

## 📝 License

[Thêm license của bạn tại đây]

## 👨‍💻 Author

[Tên của bạn]
- Email: [your-email@example.com]
- LinkedIn: [your-linkedin-profile]
- GitHub: [your-github-profile]

## 🤝 Contributing

Contributions, issues và feature requests đều được chào đón!

## 📞 Contact

Nếu có bất kỳ câu hỏi nào, vui lòng liên hệ qua email hoặc tạo issue trên GitHub.

---

**⭐ Nếu bạn thấy project này hữu ích, hãy cho nó một star!**
