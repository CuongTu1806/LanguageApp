package com.example.appNN.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Service để tìm kiếm và tải hình ảnh từ Pixabay API
 */
@Slf4j
@Service
public class ImageService {

    @Value("${pixabay.api.key}")
    private String pixabayApiKey;

    @Value("${pixabay.api.url}")
    private String pixabayApiUrl;

    @Value("${image.storage.path}")
    private String imageStoragePath;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Tìm kiếm hình ảnh từ Pixabay dựa trên từ khóa (tiếng Anh hoặc Pinyin)
     * @param keyword Từ khóa tìm kiếm (nên dùng nghĩa tiếng Anh)
     * @return URL của hình ảnh tìm được (null nếu không tìm thấy)
     */
    public String searchImage(String keyword) {
        try {
            String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            String apiUrl = String.format("%s?key=%s&q=%s&image_type=photo&per_page=3&safesearch=true",
                    pixabayApiUrl, pixabayApiKey, encodedKeyword);

            log.info("Searching image for keyword: {}", keyword);
            String response = restTemplate.getForObject(apiUrl, String.class);
            
            if (response == null) {
                log.warn("No response from Pixabay API");
                return null;
            }

            JsonNode root = objectMapper.readTree(response);
            JsonNode hits = root.get("hits");

            if (hits != null && hits.isArray() && hits.size() > 0) {
                // Lấy hình ảnh đầu tiên, ưu tiên webformatURL (kích thước vừa phải)
                String imageUrl = hits.get(0).get("webformatURL").asText();
                log.info("Found image: {}", imageUrl);
                return imageUrl;
            }

            log.warn("No images found for keyword: {}", keyword);
            return null;

        } catch (Exception e) {
            log.error("Error searching image for keyword: " + keyword, e);
            return null;
        }
    }

    /**
     * Tải hình ảnh từ URL về server
     * @param imageUrl URL của hình ảnh
     * @param fileName Tên file để lưu (ví dụ: "apple_123.jpg")
     * @return Đường dẫn tương đối để lưu vào database (ví dụ: "/images/vocab/apple_123.jpg")
     */
    public String downloadImage(String imageUrl, String fileName) {
        try {
            // Tạo thư mục nếu chưa có
            Path directory = Paths.get(imageStoragePath);
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
                log.info("Created directory: {}", directory);
            }

            // Xác định extension từ URL
            String extension = getFileExtension(imageUrl);
            if (extension.isEmpty()) {
                extension = ".jpg"; // default
            }

            // Tạo tên file an toàn
            String safeFileName = sanitizeFileName(fileName) + extension;
            Path filePath = directory.resolve(safeFileName);

            // Tải file về
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            try (InputStream in = connection.getInputStream();
                 FileOutputStream out = new FileOutputStream(filePath.toFile())) {

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }

                log.info("Downloaded image: {}", filePath);
                
                // Trả về đường dẫn tương đối để lưu vào DB và dùng trong HTML
                return "/images/vocab/" + safeFileName;

            } finally {
                connection.disconnect();
            }

        } catch (IOException e) {
            log.error("Error downloading image from: " + imageUrl, e);
            return null;
        }
    }

    /**
     * Tìm và tải hình ảnh trong một bước
     * @param keyword Từ khóa tìm kiếm
     * @param vocabId ID của từ vựng (để tạo tên file unique)
     * @return Đường dẫn tương đối của hình ảnh đã tải
     */
    public String fetchAndDownloadImage(String keyword, Long vocabId) {
        String imageUrl = searchImage(keyword);
        if (imageUrl == null) {
            return null;
        }

        String fileName = keyword.replaceAll("[^a-zA-Z0-9]", "_") + "_" + vocabId;
        return downloadImage(imageUrl, fileName);
    }

    /**
     * Lấy extension từ URL
     */
    private String getFileExtension(String url) {
        int lastDot = url.lastIndexOf('.');
        int lastQuestionMark = url.lastIndexOf('?');
        
        if (lastDot > 0 && (lastQuestionMark == -1 || lastDot < lastQuestionMark)) {
            return url.substring(lastDot, lastQuestionMark == -1 ? url.length() : lastQuestionMark);
        }
        return "";
    }

    /**
     * Làm sạch tên file để tránh ký tự đặc biệt
     */
    private String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9_-]", "_")
                       .replaceAll("_+", "_")
                       .toLowerCase();
    }

    /**
     * Kiểm tra xem file hình ảnh đã tồn tại chưa
     */
    public boolean imageExists(String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            return false;
        }
        // imagePath dạng: /images/vocab/apple_123.jpg
        // Cần convert sang path thực: src/main/resources/static/images/vocab/apple_123.jpg
        String realPath = imageStoragePath + imagePath.substring(imagePath.lastIndexOf('/'));
        return Files.exists(Paths.get(realPath));
    }
}
