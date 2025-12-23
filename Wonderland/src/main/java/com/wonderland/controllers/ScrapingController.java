package com.wonderland.controllers;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class ScrapingController {

    private static final Logger logger = LoggerFactory.getLogger(ScrapingController.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${app.upload.dir}")
    private String baseUploadDir;

    // --- CONFIGURATION ---
    private static final int MAX_ITEMS_PER_CATEGORY = 200; 

    // --- TARGET STORE ---
    private static final List<StoreSource> TARGET_STORES = List.of(
        new StoreSource("mixed", "https://www.onetoystore.com")
    );

    @GetMapping("/api/import-toys")
    public String importToys(@RequestParam(value = "type", defaultValue = "action_her") String type) {
        StringBuilder browserLog = new StringBuilder();
        browserLog.append("=== STARTING IMPORT (Target: Action Figures for Her 3-5) ===\n");
        browserLog.append("New Limit per category: " + MAX_ITEMS_PER_CATEGORY + "\n");

        // 1. Load Current Database Counts
        Map<String, Integer> categoryCounts = new HashMap<>();
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("SELECT category, COUNT(*) as cnt FROM toys GROUP BY category");
        for (Map<String, Object> row : rows) {
            String cat = (String) row.get("category");
            Number cnt = (Number) row.get("cnt"); 
            if (cat != null) categoryCounts.put(cat, cnt.intValue());
        }

        HttpClient client = HttpClient.newHttpClient();
        ObjectMapper mapper = new ObjectMapper();
        
        int totalImported = 0;

        for (StoreSource store : TARGET_STORES) {
            browserLog.append("\n>> Connecting to: ").append(store.url).append("\n");
            
            int page = 1;
            // Scan 8 pages to find specific character figures
            while (page <= 8) { 
                try {
                    String url = store.url + "/products.json?limit=250&page=" + page;
                    HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).build();
                    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

                    if (response.statusCode() != 200) {
                        browserLog.append("   [!] Status " + response.statusCode() + ": Stopping.\n");
                        break; 
                    }

                    JsonNode rootNode = mapper.readTree(response.body());
                    JsonNode products = rootNode.get("products");

                    if (products == null || products.isEmpty()) break; 

                    browserLog.append("   Page ").append(page).append(": Scanning ").append(products.size()).append(" items...\n");

                    for (JsonNode p : products) {
                        try {
                            String name = p.get("title").asText().trim();
                            
                            // CHECK DUPLICATES
                            Integer dbCount = jdbcTemplate.queryForObject(
                                "SELECT COUNT(*) FROM toys WHERE LOWER(name) = LOWER(?)", 
                                Integer.class, name
                            );
                            if (dbCount != null && dbCount > 0) continue; 

                            String body = p.has("body_html") ? p.get("body_html").asText() : "";
                            String rawType = p.has("product_type") ? p.get("product_type").asText() : "";
                            
                            // --- STRICT FILTER: Action Figures (Girls 3-5) ---
                            if (!isActionFigureForHer(name, body, rawType)) continue;

                            String finalCategory = "Action Toys & Figures";
                            
                            // Check Limit
                            int currentCatCount = categoryCounts.getOrDefault(finalCategory, 0);
                            if (currentCatCount >= MAX_ITEMS_PER_CATEGORY) continue;

                            // SAVE ITEM
                            Double price = 0.0;
                            if (p.has("variants") && p.get("variants").size() > 0) {
                                price = p.get("variants").get(0).get("price").asDouble();
                            }

                            String remoteUrl = "";
                            if (p.has("images") && p.get("images").size() > 0) {
                                remoteUrl = p.get("images").get(0).get("src").asText();
                            }

                            if (!remoteUrl.isEmpty()) {
                                String localPath = downloadImage(remoteUrl, finalCategory);
                                
                                String sql = "INSERT INTO toys (name, price, brand, min_age, target_audience, category, item_type, image_url, stock_quantity, description) " +
                                             "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
                                
                                // Specific Age/Gender for this batch
                                int minAge = 3;
                                String audience = "Girls";

                                jdbcTemplate.update(sql, name, price, p.get("vendor").asText(), minAge, audience, finalCategory, "Physical", localPath, 50, body);
                                
                                categoryCounts.put(finalCategory, currentCatCount + 1);
                                totalImported++;
                            }

                        } catch (Exception e) {
                            // Skip bad items silently
                        }
                    }
                    page++;
                } catch (Exception e) {
                    browserLog.append("Error: ").append(e.getMessage());
                    break;
                }
            }
        }
        
        browserLog.append("\n=== SUCCESS: Imported " + totalImported + " items! ===\n");
        browserLog.append("Current Category Status:\n");
        categoryCounts.forEach((k, v) -> browserLog.append(" - " + k + ": " + v + "\n"));
        
        return browserLog.toString();
    }

    // --- STRICT FILTER LOGIC ---

    private boolean isActionFigureForHer(String name, String body, String type) {
        String text = (name + " " + body + " " + type).toLowerCase();

        // 1. Must NOT be a plushie, baby toy, or boy-specific item
        if (text.contains("plush") || text.contains("soft") || text.contains("stuffed") || 
            text.contains("baby") || text.contains("teether") || text.contains("rattle") || 
            text.contains("nerf") || text.contains("gun") || text.contains("monster truck")) {
            return false;
        }

        // 2. Must be an Action Figure / Playset Type
        boolean isFigure = text.contains("figure") || text.contains("playset") || 
                           text.contains("character") || text.contains("doll house") || 
                           text.contains("mini") || text.contains("collectible");

        if (!isFigure) return false;

        // 3. Must be Girl-Oriented Characters (Ages 3-5)
        return text.contains("bluey") || text.contains("peppa") || 
               text.contains("gabby") || text.contains("paw patrol") || 
               text.contains("skye") || text.contains("everest") || 
               text.contains("minnie") || text.contains("daisy") || 
               text.contains("princess") || text.contains("frozen") || 
               text.contains("elsa") || text.contains("anna") ||
               text.contains("shimmer") || text.contains("shine") ||
               text.contains("my little pony") || text.contains("mylittlepony") ||
               text.contains("unicorn") || text.contains("fairy");
    }

    private String downloadImage(String urlStr, String category) {
        try {
            String cleanDir = category.replaceAll("[^a-zA-Z0-9]", "_");
            Path dirPath = Paths.get(baseUploadDir, cleanDir);
            if (!Files.exists(dirPath)) Files.createDirectories(dirPath);

            String fileName = UUID.randomUUID().toString() + ".jpg";
            Path targetPath = dirPath.resolve(fileName);

            try (InputStream in = new URL(urlStr).openStream()) {
                Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
            return "/uploads/" + cleanDir + "/" + fileName;
        } catch (Exception e) {
            return "assets/img/logo2.png"; 
        }
    }

    static class StoreSource {
        String key;
        String url;
        StoreSource(String key, String url) {
            this.key = key;
            this.url = url;
        }
    }
}