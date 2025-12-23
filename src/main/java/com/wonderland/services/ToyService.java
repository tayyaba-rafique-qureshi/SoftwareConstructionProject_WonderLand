package com.wonderland.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.wonderland.models.QuizAnswers;
import com.wonderland.models.Toy;
import com.wonderland.models.ToyUpdateDto;

@Service
public class ToyService {

    private static final Logger logger = LoggerFactory.getLogger(ToyService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${app.upload.dir}")
    private String baseUploadDir;

    // --- BUCKET ANALYSIS / RECOMMENDATIONS ---
    
    public List<Toy> getRelatedToys(int toyId) {
        // 1. MARKET BASKET ANALYSIS (Bucket Analysis)
        // Query: "Find toys that appear in the same orders as the current toy"
        String analysisSql = 
            "SELECT t.*, COUNT(oi.order_id) as frequency " +
            "FROM toys t " +
            "JOIN order_items oi ON t.id = oi.toy_id " +
            "WHERE oi.order_id IN ( " +
            "    SELECT sub_oi.order_id " +
            "    FROM order_items sub_oi " +
            "    WHERE sub_oi.toy_id = ? " +
            ") " +
            "AND t.id != ? " + // Exclude the product itself
            "GROUP BY t.id " +
            "ORDER BY frequency DESC " +
            "LIMIT 4";

        List<Toy> related = jdbcTemplate.query(analysisSql, new ToyRowMapper(), toyId, toyId);

        // 2. FALLBACK: If data is scarce (new store), show items from the SAME CATEGORY
        if (related.isEmpty()) {
            Toy current = getToyById(toyId);
            if (current != null) {
                // MySQL specific: ORDER BY RAND(). Use ORDER BY id DESC for generic SQL.
                String fallbackSql = "SELECT * FROM toys WHERE category = ? AND id != ? ORDER BY RAND() LIMIT 4";
                related = jdbcTemplate.query(fallbackSql, new ToyRowMapper(), current.getCategory(), toyId);
            }
        }
        
        return related;
    }

    // --- EXISTING METHODS ---

    public List<Toy> getToysPaginated(int page, int size, String category, String search, String ageGroup, String audience) {
        int offset = (page - 1) * size;
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT * FROM toys WHERE 1=1");

        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND (LOWER(name) LIKE ? OR LOWER(description) LIKE ? OR LOWER(brand) LIKE ?)");
            String query = "%" + search.toLowerCase().trim() + "%";
            params.add(query);
            params.add(query);
            params.add(query);
        }

       // Inside ToyService.java -> getToysPaginated method
if (category != null && !category.isEmpty() && !category.equalsIgnoreCase("All")) {
    // Robust check: Trim and ignore case to handle %20 or spacing differences 
    sql.append(" AND LOWER(TRIM(category)) = LOWER(TRIM(?))");
    params.add(category);
}
        if (ageGroup != null && !ageGroup.isEmpty()) {
            switch (ageGroup) {
                case "0-2": sql.append(" AND min_age <= 2"); break;
                case "3-5": sql.append(" AND min_age BETWEEN 3 AND 5"); break;
                case "6-11": sql.append(" AND min_age BETWEEN 6 AND 11"); break;
                case "12+": sql.append(" AND min_age >= 12"); break;
            }
        }

        if (audience != null && !audience.isEmpty()) {
            sql.append(" AND target_audience = ?");
            params.add(audience);
        }

        sql.append(" ORDER BY id DESC LIMIT ? OFFSET ?");
        params.add(size);
        params.add(offset);

        return jdbcTemplate.query(sql.toString(), new ToyRowMapper(), params.toArray());
    }

    public int countToys(String category, String search, String ageGroup, String audience) {
        StringBuilder sql = new StringBuilder("SELECT COUNT(*) FROM toys WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (search != null && !search.trim().isEmpty()) {
            sql.append(" AND (LOWER(name) LIKE ? OR LOWER(description) LIKE ? OR LOWER(brand) LIKE ?)");
            String query = "%" + search.toLowerCase().trim() + "%";
            params.add(query);
            params.add(query);
            params.add(query);
        }

      // Inside ToyService.java -> countToys method
if (category != null && !category.isEmpty() && !category.equalsIgnoreCase("All")) {
    sql.append(" AND LOWER(TRIM(category)) = LOWER(TRIM(?))");
    params.add(category);
}

        if (ageGroup != null && !ageGroup.isEmpty()) {
            switch (ageGroup) {
                case "0-2": sql.append(" AND min_age <= 2"); break;
                case "3-5": sql.append(" AND min_age BETWEEN 3 AND 5"); break;
                case "6-11": sql.append(" AND min_age BETWEEN 6 AND 11"); break;
                case "12+": sql.append(" AND min_age >= 12"); break;
            }
        }
        
        if (audience != null && !audience.isEmpty()) {
            sql.append(" AND target_audience = ?");
            params.add(audience);
        }

        Integer count = jdbcTemplate.queryForObject(sql.toString(), Integer.class, params.toArray());
        return count != null ? count : 0;
    }
// Wonderland/src/main/java/com/wonderland/services/ToyService.java

// ... imports ...
// (Keep existing imports, ensure java.util.List is there)

    // --- NEW: Get Categories for specific Age Group ---
    public List<String> getCategoriesByAgeGroup(String ageGroup) {
        StringBuilder sql = new StringBuilder("SELECT DISTINCT category FROM toys WHERE 1=1");
        
        if (ageGroup != null) {
            switch (ageGroup) {
                case "0-2": sql.append(" AND min_age <= 2"); break;
                case "3-5": sql.append(" AND min_age BETWEEN 3 AND 5"); break;
                case "6-11": sql.append(" AND min_age BETWEEN 6 AND 11"); break;
                case "12+": sql.append(" AND min_age >= 12"); break;
            }
        }
        
        sql.append(" ORDER BY category ASC");
        return jdbcTemplate.queryForList(sql.toString(), String.class);
    }

    // --- UPDATED: Get Quiz Results using Age Group ---
    public List<Toy> getQuizResults(QuizAnswers answers) {
        StringBuilder sql = new StringBuilder("SELECT * FROM toys WHERE price <= ?");
        List<Object> params = new ArrayList<>();
        params.add(answers.getBudget());

        // 1. Age Logic
        if (answers.getAgeGroup() != null) {
            switch (answers.getAgeGroup()) {
                case "0-2": sql.append(" AND min_age <= 2"); break;
                case "3-5": sql.append(" AND min_age BETWEEN 3 AND 5"); break;
                case "6-11": sql.append(" AND min_age BETWEEN 6 AND 11"); break;
                case "12+": sql.append(" AND min_age >= 12"); break;
            }
        }

        // 2. Interest Logic (Matches Category or Name)
        // If interest is empty (e.g. Toddlers), we skip this filter
        if (answers.getInterest() != null && !answers.getInterest().isEmpty()) {
            sql.append(" AND (LOWER(category) = ? OR LOWER(name) LIKE ? OR LOWER(description) LIKE ?)");
            String keyword = answers.getInterest().toLowerCase();
            params.add(keyword);
            params.add("%" + keyword + "%");
            params.add("%" + keyword + "%");
        }

        sql.append(" ORDER BY price DESC LIMIT 4");
        
        return jdbcTemplate.query(sql.toString(), new ToyRowMapper(), params.toArray());
    }

// ... (Keep the rest of the file: getToysPaginated, countToys, RowMapper, etc.) ...
    public List<Toy> filterToys(String category, String ageGroup, String brand, String audience) {
        StringBuilder sql = new StringBuilder("SELECT * FROM toys WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (category != null && !category.isEmpty() && !category.equalsIgnoreCase("All")) {
            sql.append(" AND category = ?");
            params.add(category);
        }
        if (brand != null && !brand.isEmpty()) {
            sql.append(" AND brand = ?");
            params.add(brand);
        }
        if (audience != null && !audience.isEmpty()) {
            sql.append(" AND target_audience = ?");
            params.add(audience);
        }
        if (ageGroup != null && !ageGroup.isEmpty()) {
            switch (ageGroup) {
                case "0-2": sql.append(" AND min_age <= 2"); break;
                case "3-5": sql.append(" AND min_age BETWEEN 3 AND 5"); break;
                case "6-11": sql.append(" AND min_age BETWEEN 6 AND 11"); break;
                case "12+": sql.append(" AND min_age >= 12"); break;
            }
        }
        sql.append(" ORDER BY id DESC");
        return jdbcTemplate.query(sql.toString(), new ToyRowMapper(), params.toArray());
    }

    public List<Toy> getLowStockToys(int threshold) {
        String sql = "SELECT * FROM toys WHERE stock_quantity < ? ORDER BY stock_quantity ASC";
        return jdbcTemplate.query(sql, new ToyRowMapper(), threshold);
    }

    public List<Toy> getAllToys() {
        return jdbcTemplate.query("SELECT * FROM toys ORDER BY id DESC LIMIT 50", new ToyRowMapper());
    }

    public Toy getToyById(int id) {
        String sql = "SELECT * FROM toys WHERE id = ?";
        List<Toy> toys = jdbcTemplate.query(sql, new ToyRowMapper(), id);
        return toys.isEmpty() ? null : toys.get(0);
    }

    public void saveNewToy(String name, String brand, String category, Double price, 
                           int stockQuantity, int minAge, String targetAudience, 
                           String itemType, String description, MultipartFile imageFile) throws IOException {
        String dbImageUrl = handleFileUpload(category, imageFile);
        String sql = "INSERT INTO toys (name, price, brand, min_age, target_audience, category, item_type, image_url, stock_quantity, description) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, name, price, brand, minAge, targetAudience, category, itemType, dbImageUrl, stockQuantity, description);
    }

public void updateToy(int id, ToyUpdateDto dto) {
        // Updated SQL to include stock_quantity
        String sql = "UPDATE toys SET name = ?, brand = ?, category = ?, price = ?, min_age = ?, target_audience = ?, description = ?, stock_quantity = ? WHERE id = ?";
        
        jdbcTemplate.update(sql, 
            dto.getName(), 
            dto.getBrand(), 
            dto.getCategory(), 
            dto.getPrice(), 
            dto.getMinAge(), 
            dto.getTargetAudience(), 
            dto.getDescription(), 
            dto.getStockQuantity(), // Pass the stock quantity here
            id
        );
    }

    public void updateStock(int id, int quantity) {
        String sql = "UPDATE toys SET stock_quantity = GREATEST(0, stock_quantity + ?) WHERE id = ?";
        jdbcTemplate.update(sql, quantity, id);
    }

    public void deleteToy(int id) {
        jdbcTemplate.update("DELETE FROM toys WHERE id = ?", id);
    }

    public void applyCategorySale(String category, double discountPercent) {
        double multiplier = 1.0 - (discountPercent / 100.0);
        String sql = "UPDATE toys SET sale_price = price * ?, is_on_sale = TRUE";
        if (!"All".equalsIgnoreCase(category)) {
            sql += " WHERE category = ?";
            jdbcTemplate.update(sql, multiplier, category);
        } else {
            jdbcTemplate.update(sql, multiplier);
        }
    }

    public void endSale() {
        jdbcTemplate.update("UPDATE toys SET is_on_sale = FALSE, sale_price = NULL");
    }

    public List<String> getUniqueBrands() {
        return jdbcTemplate.queryForList("SELECT DISTINCT brand FROM toys ORDER BY brand ASC", String.class);
    }

    private String handleFileUpload(String category, MultipartFile imageFile) throws IOException {
        if (imageFile == null || imageFile.isEmpty()) return "assets/img/logo2.png";
        String cleanCategory = category.replaceAll("[^a-zA-Z0-9]", "_");
        Path categoryPath = Paths.get(baseUploadDir, cleanCategory);
        if (!Files.exists(categoryPath)) Files.createDirectories(categoryPath);
        String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename().replaceAll("\\s+", "_");
        Files.copy(imageFile.getInputStream(), categoryPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
        return "/uploads/" + cleanCategory + "/" + fileName;
    }

    // --- ROW MAPPER ---
    private static class ToyRowMapper implements RowMapper<Toy> {
        @Override
        public Toy mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
            Toy toy = new Toy();
            toy.setId(rs.getInt("id"));
            toy.setName(rs.getString("name"));
            toy.setPrice(rs.getDouble("price"));
            toy.setBrand(rs.getString("brand"));
            toy.setMinAge(rs.getInt("min_age"));
            toy.setTargetAudience(rs.getString("target_audience"));
            toy.setCategory(rs.getString("category"));
            toy.setItemType(rs.getString("item_type"));
            toy.setImageUrl(rs.getString("image_url"));
            toy.setStockQuantity(rs.getInt("stock_quantity"));
            toy.setDescription(rs.getString("description"));
            try {
                boolean onSale = rs.getBoolean("is_on_sale");
                toy.setOnSale(onSale);
                double salePrice = rs.getDouble("sale_price");
                if (!rs.wasNull()) toy.setSalePrice(salePrice);
            } catch (SQLException e) {
                toy.setOnSale(false);
            }
            return toy;
        }
    }


    // --- Add this to ToyService.java ---

    public List<Toy> getTopSellingToys(int limit) {
        String sql = 
            "SELECT t.*, SUM(oi.quantity) as total_sold " +
            "FROM toys t " +
            "JOIN order_items oi ON t.id = oi.toy_id " +
            "GROUP BY t.id " +
            "ORDER BY total_sold DESC " +
            "LIMIT ?";
        
        return jdbcTemplate.query(sql, new ToyRowMapper(), limit);
    }
    
}