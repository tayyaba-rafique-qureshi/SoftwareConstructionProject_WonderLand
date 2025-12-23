// Wonderland/src/main/java/com/wonderland/controllers/InventoryController.java

package com.wonderland.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.wonderland.models.Toy;
import com.wonderland.models.ToyUpdateDto;
import com.wonderland.services.ToyService;

@RestController
@RequestMapping("/api")
public class InventoryController {

    private static final Logger logger = LoggerFactory.getLogger(InventoryController.class);

    @Autowired
    private ToyService toyService;

    // --- GET INVENTORY (Paginated) ---
    @GetMapping("/inventory")
    public ResponseEntity<Map<String, Object>> getInventory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "15") int limit, 
            @RequestParam(defaultValue = "All") String category,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String age,
            @RequestParam(required = false) String audience) { 
        
        try {
            if ("undefined".equals(age) || "null".equals(age)) age = "";
            if ("undefined".equals(audience) || "null".equals(audience)) audience = "";

            List<Toy> toys = toyService.getToysPaginated(page, limit, category, search, age, audience);
            int totalItems = toyService.countToys(category, search, age, audience);
            int totalPages = (int) Math.ceil((double) totalItems / limit);

            Map<String, Object> response = new HashMap<>();
            response.put("toys", toys);
            response.put("currentPage", page);
            response.put("totalPages", totalPages);
            response.put("totalItems", totalItems);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error fetching inventory: ", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    // --- GET SINGLE TOY ---
    @GetMapping("/toys/{id}")
    public ResponseEntity<Toy> getToy(@PathVariable int id) {
        Toy toy = toyService.getToyById(id);
        return toy != null ? ResponseEntity.ok(toy) : ResponseEntity.notFound().build();
    }

    // --- GET RELATED TOYS (Bucket Analysis) ---
    @GetMapping("/toys/related/{id}")
    public ResponseEntity<List<Toy>> getRelatedToys(@PathVariable int id) {
        try {
            List<Toy> related = toyService.getRelatedToys(id);
            return ResponseEntity.ok(related);
        } catch (Exception e) {
            logger.error("Error fetching related toys: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // --- OTHER ENDPOINTS (Restock, Add, Delete, Update) ---
    
    @GetMapping("/toys/filter")
    public ResponseEntity<List<Toy>> filterToys(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String ageGroup,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String audience) {
        return ResponseEntity.ok(toyService.filterToys(category, ageGroup, brand, audience));
    }

    @PostMapping("/restock")
    public ResponseEntity<Map<String, Object>> restockToy(@RequestBody Map<String, Object> payload) {
        Map<String, Object> response = new HashMap<>();
        try {
            int id = Integer.parseInt(payload.get("id").toString());
            int quantity = Integer.parseInt(payload.get("quantity").toString());
            toyService.updateStock(id, quantity);
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (NumberFormatException e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/toys/add")
    public ResponseEntity<Map<String, Object>> addToy(
            @RequestParam String name, @RequestParam String brand, @RequestParam String category,
            @RequestParam Double price, @RequestParam int stock_quantity, @RequestParam int min_age,
            @RequestParam String target_audience, @RequestParam String item_type, @RequestParam String description,
            @RequestParam(value = "image_file", required = false) MultipartFile imageFile) {

        Map<String, Object> response = new HashMap<>();
        if (name == null || name.trim().isEmpty() || price < 0) {
            response.put("status", "error");
            response.put("message", "Invalid Product Data");
            return ResponseEntity.badRequest().body(response);
        }
        try {
            toyService.saveNewToy(name, brand, category, price, stock_quantity, 
                                 min_age, target_audience, item_type, description, imageFile);
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (IOException | IllegalArgumentException e) {
            logger.error("Failed to add toy: ", e);
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    @DeleteMapping("/toys/delete/{id}")
    public ResponseEntity<Map<String, Object>> deleteToy(@PathVariable int id) {
        Map<String, Object> response = new HashMap<>();
        try {
            toyService.deleteToy(id);
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("status", "error");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PutMapping("/toys/update/{id}")
    public ResponseEntity<Map<String, Object>> updateToy(@PathVariable int id, @RequestBody ToyUpdateDto updateDto) {
        Map<String, Object> response = new HashMap<>();
        try {
            toyService.updateToy(id, updateDto);
            response.put("status", "success");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Failed to update toy: ", e);
            response.put("status", "error");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}