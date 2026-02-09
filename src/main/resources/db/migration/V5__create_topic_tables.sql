-- Migration tạo bảng Topic và VocabularyTopic
-- Hỗ trợ phân loại từ vựng theo chủ đề cho đa ngôn ngữ

-- Tạo bảng Topic
CREATE TABLE IF NOT EXISTS topic (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    name_vi VARCHAR(100),
    description VARCHAR(500),
    language_code VARCHAR(10) NOT NULL,
    icon VARCHAR(50),
    color VARCHAR(20),
    display_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Index để tối ưu tìm kiếm
    INDEX idx_language_code (language_code),
    INDEX idx_display_order (display_order),
    UNIQUE KEY uk_name_language (name, language_code)
);

-- Tạo bảng trung gian VocabularyTopic
CREATE TABLE IF NOT EXISTS vocabulary_topic (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vocabulary_id BIGINT NOT NULL,
    topic_id BIGINT NOT NULL,
    priority INT DEFAULT 5,
    
    -- Foreign keys
    CONSTRAINT fk_vocab_topic_vocabulary 
        FOREIGN KEY (vocabulary_id) REFERENCES vocabulary(id) ON DELETE CASCADE,
    CONSTRAINT fk_vocab_topic_topic 
        FOREIGN KEY (topic_id) REFERENCES topic(id) ON DELETE CASCADE,
    
    -- Index
    INDEX idx_vocabulary_id (vocabulary_id),
    INDEX idx_topic_id (topic_id),
    UNIQUE KEY uk_vocab_topic (vocabulary_id, topic_id)
);

-- Insert dữ liệu topics TOEIC cho tiếng Anh
INSERT INTO topic (name, name_vi, description, language_code, icon, color, display_order) VALUES
-- Business & Office (Kinh doanh & Văn phòng)
('Contracts', 'Hợp đồng', 'Agreements, terms, obligations, and contract vocabulary', 'en', '📝', '#2C3E50', 1),
('Marketing', 'Tiếp thị', 'Advertising, promotion, market research, and sales', 'en', '📊', '#E74C3C', 2),
('Warranties', 'Bảo hành', 'Product guarantees, service warranties, and repairs', 'en', '🔧', '#3498DB', 3),
('Business Planning', 'Kế hoạch kinh doanh', 'Strategy, goals, objectives, and business development', 'en', '📈', '#27AE60', 4),
('Office Technology', 'Công nghệ văn phòng', 'Office equipment, software, and technology tools', 'en', '🖥️', '#9B59B6', 5),
('Office Procedures', 'Thủ tục văn phòng', 'Workplace rules, guidelines, and office protocols', 'en', '📋', '#16A085', 6),

-- Human Resources (Nhân sự)
('Job Advertising & Recruiting', 'Tuyển dụng', 'Job postings, applications, and recruitment process', 'en', '📢', '#E67E22', 7),
('Applying & Interviewing', 'Ứng tuyển & Phỏng vấn', 'Resumes, interviews, and job application vocabulary', 'en', '💼', '#34495E', 8),
('Hiring & Training', 'Tuyển dụng & Đào tạo', 'Onboarding, training programs, and employee development', 'en', '👥', '#1ABC9C', 9),
('Salaries & Benefits', 'Lương & Phúc lợi', 'Compensation, benefits, bonuses, and payroll', 'en', '💰', '#F39C12', 10),
('Promotions & Awards', 'Thăng tiến & Giải thưởng', 'Career advancement, recognition, and achievements', 'en', '🏆', '#D35400', 11),

-- Finance & Accounting (Tài chính & Kế toán)
('Banking', 'Ngân hàng', 'Banking services, accounts, loans, and transactions', 'en', '🏦', '#2980B9', 12),
('Accounting', 'Kế toán', 'Financial records, bookkeeping, and accounting terms', 'en', '💹', '#8E44AD', 13),
('Investments', 'Đầu tư', 'Stocks, bonds, portfolios, and investment strategies', 'en', '📉', '#C0392B', 14),
('Financial Statements', 'Báo cáo tài chính', 'Balance sheets, income statements, and financial reports', 'en', '📊', '#16A085', 15),
('Taxes', 'Thuế', 'Tax returns, deductions, and taxation vocabulary', 'en', '🧾', '#7F8C8D', 16),

-- Purchasing & Logistics (Mua sắm & Logistics)
('Shopping', 'Mua sắm', 'Retail, purchasing, prices, and consumer vocabulary', 'en', '🛒', '#E91E63', 17),
('Ordering Supplies', 'Đặt hàng', 'Purchase orders, suppliers, and procurement', 'en', '📦', '#00BCD4', 18),
('Shipping', 'Vận chuyển', 'Delivery, freight, shipping methods, and logistics', 'en', '🚚', '#607D8B', 19),
('Invoices', 'Hóa đơn', 'Billing, invoices, receipts, and payment documents', 'en', '🧾', '#FF5722', 20),
('Inventory', 'Hàng tồn kho', 'Stock management, inventory control, and warehousing', 'en', '📊', '#795548', 21),

-- Property & Facilities (Tài sản & Cơ sở vật chất)
('Property & Departments', 'Tài sản & Phòng ban', 'Office spaces, facilities, and department organization', 'en', '🏢', '#455A64', 22),
('Renting & Leasing', 'Thuê & Cho thuê', 'Rental agreements, leases, and property rental', 'en', '🔑', '#5D4037', 23),

-- Meetings & Events (Họp & Sự kiện)
('Conferences', 'Hội nghị', 'Meetings, conferences, seminars, and business events', 'en', '🎤', '#00796B', 24),
('Board Meetings', 'Họp hội đồng', 'Corporate meetings, board decisions, and governance', 'en', '👔', '#512DA8', 25),
('Events', 'Sự kiện', 'Event planning, coordination, and event management', 'en', '🎉', '#C2185B', 26),

-- Travel & Hospitality (Du lịch & Khách sạn)
('General Travel', 'Du lịch chung', 'Travel planning, destinations, and travel vocabulary', 'en', '🌍', '#0097A7', 27),
('Airlines', 'Hàng không', 'Flights, airports, airlines, and air travel', 'en', '✈️', '#0288D1', 28),
('Trains', 'Tàu hỏa', 'Rail travel, train stations, and railway vocabulary', 'en', '🚄', '#1976D2', 29),
('Hotels', 'Khách sạn', 'Accommodations, hotel services, and hospitality', 'en', '🏨', '#303F9F', 30),
('Car Rentals', 'Thuê xe', 'Vehicle rental, car hire, and transportation', 'en', '🚗', '#5E35B1', 31),
('Restaurants', 'Nhà hàng', 'Dining, restaurants, food service, and cuisine', 'en', '🍽️', '#E53935', 32),

-- Technology & Communications (Công nghệ & Truyền thông)
('Computers', 'Máy tính', 'Computer hardware, software, and IT terminology', 'en', '💻', '#673AB7', 33),
('Electronics', 'Điện tử', 'Electronic devices, gadgets, and technology', 'en', '📱', '#3F51B5', 34),
('Correspondences', 'Thư tín', 'Letters, emails, memos, and written communication', 'en', '✉️', '#2196F3', 35),
('Media', 'Truyền thông', 'Press, journalism, broadcasting, and media industry', 'en', '📺', '#03A9F4', 36),

-- Entertainment & Leisure (Giải trí & Giải trí)
('Movies', 'Phim ảnh', 'Cinema, films, and movie industry vocabulary', 'en', '🎬', '#00BCD4', 37),
('Theater', 'Rạp hát', 'Performances, plays, and theatrical vocabulary', 'en', '🎭', '#009688', 38),
('Music', 'Âm nhạc', 'Music industry, instruments, and musical terms', 'en', '🎵', '#4CAF50', 39),
('Museums', 'Bảo tàng', 'Art, exhibitions, and museum vocabulary', 'en', '🖼️', '#8BC34A', 40),

-- Health & Medicine (Sức khỏe & Y tế)
('Dentist Office', 'Nha khoa', 'Dental care, dentistry, and oral health', 'en', '🦷', '#CDDC39', 41),
('Doctor Office', 'Phòng khám', 'Medical appointments, clinics, and healthcare', 'en', '👨‍⚕️', '#FFEB3B', 42),
('Health Insurance', 'Bảo hiểm y tế', 'Health coverage, insurance policies, and benefits', 'en', '🏥', '#FFC107', 43),
('Hospitals', 'Bệnh viện', 'Hospital services, medical facilities, and patient care', 'en', '🏥', '#FF9800', 44),
('Pharmacy', 'Hiệu thuốc', 'Medications, prescriptions, and pharmaceutical terms', 'en', '💊', '#FF5722', 45),

-- Production & Quality (Sản xuất & Chất lượng)
('Product Development', 'Phát triển sản phẩm', 'R&D, product design, and innovation', 'en', '🔬', '#9C27B0', 46),
('Quality Control', 'Kiểm soát chất lượng', 'Quality assurance, testing, and standards', 'en', '✅', '#673AB7', 47),
('Equipment', 'Thiết bị', 'Machinery, tools, and equipment vocabulary', 'en', '⚙️', '#3F51B5', 48);

-- Insert dữ liệu mẫu cho tiếng Trung (nếu cần)
INSERT INTO topic (name, name_vi, description, language_code, icon, color, display_order) VALUES
('Food & Dining', 'Ẩm thực', '食物、餐厅和烹饪相关词汇', 'cn', '🍔', '#FF6B6B', 1),
('Travel & Transportation', 'Du lịch & Phương tiện', '旅行、酒店和交通词汇', 'cn', '✈️', '#4ECDC4', 2),
('Business & Work', 'Kinh doanh & Công việc', '职场和商务专业词汇', 'cn', '💼', '#45B7D1', 3),
('Daily Life', 'Đời sống hàng ngày', '日常生活词汇', 'cn', '🏡', '#96CEB4', 4);

COMMIT;
