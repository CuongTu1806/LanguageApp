# Mô tả dự án cho CV

## Phiên bản ngắn gọn (1-2 dòng)
**Multi-Language Learning Application** - Ứng dụng web học ngoại ngữ (tiếng Anh & tiếng Trung) với hệ thống quiz đa dạng, lịch ôn tập thông minh (Spaced Repetition), và dashboard theo dõi tiến độ học tập. Công nghệ: Spring Boot, JPA/Hibernate, MySQL, Thymeleaf.

---

## Phiên bản chi tiết (cho mục mô tả dự án)

### Multi-Language Learning Application (TiengTrungApp)
**Vai trò**: Full-Stack Developer  
**Thời gian**: [Thêm thời gian thực hiện]  
**Mô tả**: 
Phát triển ứng dụng web học ngoại ngữ đa ngôn ngữ (tiếng Anh và tiếng Trung) toàn diện, giúp người dùng học từ vựng hiệu quả thông qua hệ thống bài học có cấu trúc và thuật toán ôn tập thông minh.

**Tính năng chính**:
- Xây dựng hệ thống học đa ngôn ngữ (tiếng Anh và tiếng Trung) với quản lý bài học đa cấp độ và khả năng tạo bài học cá nhân hóa
- Phát triển 4 chế độ quiz đa dạng: Word→Meaning, Meaning→Word, Pronunciation→Meaning, và Fill-in-blank
- Triển khai thuật toán Spaced Repetition để tối ưu hóa lịch ôn tập dựa trên hiệu suất học tập
- Xây dựng dashboard trực quan theo dõi tiến độ với thống kê chi tiết (quiz history, wrong vocabulary tracking)
- Tích hợp Pixabay API để tự động tải hình ảnh minh họa cho từ vựng
- Thiết kế database schema với 9+ bảng và migration scripts sử dụng Flyway

**Công nghệ sử dụng**:
- **Backend**: Spring Boot 4.0, Spring Data JPA, Hibernate
- **Database**: MySQL với Flyway migration
- **Frontend**: Thymeleaf, HTML/CSS/JavaScript
- **Tools**: Maven, Lombok, Jackson, Pixabay API
- **Language**: Java 17

**Kết quả đạt được**:
- Xử lý logic phức tạp cho hệ thống ôn tập tự động với nhiều review stages
- Thiết kế RESTful API và MVC pattern rõ ràng với separation of concerns
- Quản lý state phức tạp cho quiz sessions và user statistics
- Đảm bảo data integrity với composite keys và proper relationship mapping

---

## Phiên bản cho LinkedIn/Portfolio

### 🎓 Multi-Language Learning Web Application

**Overview**  
Developed a comprehensive multi-language vocabulary learning platform (English & Chinese) featuring intelligent spaced repetition algorithms, multiple quiz modes, and personalized learning paths.

**Key Achievements**
✅ Engineered a robust Spring Boot application with clean architecture (Controller-Service-Repository pattern)  
✅ Implemented sophisticated Spaced Repetition algorithm with automatic review scheduling based on user performance  
✅ Designed and optimized MySQL database schema with 9+ interconnected tables and Flyway migrations  
✅ Built 4 distinct quiz modes to accommodate different learning styles  
✅ Integrated third-party API (Pixabay) for automatic vocabulary image enrichment  
✅ Created comprehensive learning analytics dashboard for progress tracking  

**Technical Highlights**
- **Architecture**: MVC pattern with clear separation of concerns
- **ORM**: Complex JPA relationships including composite keys and bidirectional mappings
- **State Management**: Session-based quiz state handling
- **Data Modeling**: Normalized database design for scalability
- **Performance**: Lazy loading and query optimization for efficient data retrieval

**Tech Stack**  
`Spring Boot` `Spring Data JPA` `Hibernate` `MySQL` `Thymeleaf` `Maven` `Flyway` `RESTful API` `MVC` `Java 17`

---

## Bullet points cho CV (chọn 3-5 points phù hợp)

✦ Phát triển ứng dụng web học ngoại ngữ đa ngôn ngữ (tiếng Anh & tiếng Trung) sử dụng Spring Boot, JPA/Hibernate, MySQL với hơn 10 controller và service classes

✦ Triển khai thuật toán Spaced Repetition để tối ưu lịch ôn tập, tăng hiệu quả ghi nhớ từ vựng cho người học

✦ Thiết kế và xây dựng database schema với 9+ bảng, sử dụng Flyway migration để quản lý version control

✦ Xây dựng hệ thống quiz đa dạng (4 chế độ) với tracking chi tiết về vocabulary statistics và wrong answers

✦ Tích hợp Pixabay API để tự động fetch và lưu trữ hình ảnh minh họa, cải thiện trải nghiệm học tập

✦ Áp dụng MVC pattern và RESTful API design principles để đảm bảo code maintainability và scalability

✦ Xử lý complex JPA relationships (OneToMany, ManyToOne, composite keys) và lazy loading optimization

---

## Câu hỏi phỏng vấn có thể gặp và cách trả lời

### Q: Mô tả kiến trúc tổng quan của dự án?
**A**: "Dự án sử dụng kiến trúc MVC với Spring Boot. Controller layer xử lý HTTP requests, Service layer chứa business logic như thuật toán Spaced Repetition và quiz generation, Repository layer tương tác với MySQL thông qua Spring Data JPA. Frontend sử dụng Thymeleaf để server-side rendering."

### Q: Giải thích thuật toán Spaced Repetition bạn implement?
**A**: "Tôi implement thuật toán dựa trên review stages (0-4+). Mỗi lần user hoàn thành quiz, hệ thống sẽ tính toán next review date dựa trên performance. Stage 0 là bài mới, các stage tiếp theo có khoảng cách 1, 3, 7, 14, 30 ngày. Next review date được lưu trong database và hệ thống tự động hiển thị các bài cần ôn trong Review Schedule."

### Q: Challenge lớn nhất khi phát triển dự án?
**A**: "Challenge lớn nhất là thiết kế database schema để support cả system lessons và personal lessons của user, đồng thời tracking vocabulary statistics một cách hiệu quả. Tôi đã giải quyết bằng cách tạo composite table lesson_vocabulary và user_vocab_stats để track state riêng biệt cho từng user và từng từ vựng."

### Q: Làm thế nào để optimize database queries?
**A**: "Tôi sử dụng Lazy Loading cho các relationships để tránh N+1 problem. Với các trường hợp cần fetch nhiều data như quiz generation, tôi dùng JOIN FETCH để eager load một lần. Thêm indexes trên các foreign keys và frequently queried columns như user_id, lesson_id, next_review_at."

### Q: Tại sao chọn Thymeleaf thay vì SPA framework như React?
**A**: "Với scope của dự án là learning application không cần real-time updates phức tạp, Thymeleaf cho phép tôi focus vào backend logic và deliver faster với Spring Boot integration sẵn có. Server-side rendering cũng tốt hơn cho SEO. Trong tương lai có thể chuyển sang SPA nếu cần mobile app hoặc real-time features."

---

## Tags/Keywords để thêm vào CV

`Spring Boot` `Spring MVC` `Spring Data JPA` `Hibernate` `MySQL` `RESTful API` `Maven` `Flyway Migration` `Thymeleaf` `Lombok` `ORM` `Database Design` `API Integration` `MVC Architecture` `Java 17` `Web Development` `Full-Stack Development` `Backend Development`

---

## SKILLS (Format cho CV)

**SKILLS**

**Languages:** Java, SQL, JavaScript, HTML/CSS

**Frameworks & Technologies:** Spring Boot, Spring MVC, Spring Data JPA, Hibernate, Thymeleaf, Maven, Flyway, Lombok, Jackson

**Databases:** MySQL, Database Design, Query Optimization

**Tools & Practices:** Git, RESTful API, MVC Architecture, ORM, API Integration, Server-side Rendering

**Project Management:** AGILE, Git/GitHub, Version Control

**Soft Skills:** Problem-solving, Analytical Thinking, Attention to Detail, System Design, Algorithm Implementation
