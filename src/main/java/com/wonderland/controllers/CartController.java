// Wonderland/src/main/java/com/wonderland/controllers/CartController.java

package com.wonderland.controllers;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

import com.wonderland.models.CartItem;
import com.wonderland.models.User;
import com.wonderland.services.CartService;
import com.wonderland.services.IUserService;

import jakarta.servlet.http.HttpSession;

@Controller
public class CartController {

    private static final Logger logger = LoggerFactory.getLogger(CartController.class);

    @Autowired
    private CartService cartService;

   @Autowired
    private IUserService userService;
    // 1. Serve the Cart Page HTML
 // 1. Serve the Cart Page HTML
    @GetMapping("/cart")
    public String viewCartPage(Model model, HttpSession session) {
        String username = (String) session.getAttribute("user");
        
        if (username != null) {
            User user = userService.getUserByUsername(username);
            List<CartItem> items = cartService.listCartItems(user);
            model.addAttribute("cartItems", items);
            model.addAttribute("totalPrice", cartService.calculateTotal(items));
            return "cart"; // Shows cart.html (Thymeleaf template)
        } else {
            // FIX: Redirect to Home with a flag, NOT to missing login.html
            return "redirect:/?loginRequired=true"; 
        }
    }
    // 2. API to Add Item
    @PostMapping("/api/cart/add")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> addToCart(@RequestBody Map<String, Integer> payload, HttpSession session) {
        String username = (String) session.getAttribute("user");
        
        if (username == null) {
            return ResponseEntity.status(401).body(Map.of("error", "User not logged in"));
        }

        User user = userService.getUserByUsername(username);
        int toyId = payload.get("toyId");
        int quantity = payload.get("quantity");

        cartService.addToCart(user, toyId, quantity);
        
        int newCount = cartService.countItems(user);
        return ResponseEntity.ok(Map.of("message", "Added successfully", "itemCount", newCount));
    }

    // 3. API to Get Current Count
    @GetMapping("/api/cart/count")
    @ResponseBody
    public ResponseEntity<Map<String, Integer>> getCartCount(HttpSession session) {
        int count = 0;
        String username = (String) session.getAttribute("user");
        
        if (username != null) {
            User user = userService.getUserByUsername(username);
            count = cartService.countItems(user);
        }
        return ResponseEntity.ok(Map.of("count", count));
    }

    // 4. API to Get Cart Items (For cart.html JavaScript)
    @GetMapping("/api/cart")
    @ResponseBody
    public ResponseEntity<?> getCart(HttpSession session) {
        // Debug logging
        System.out.println("=== Cart API Called ===");
        
        String username = (String) session.getAttribute("user");
        System.out.println("Session username: " + username);
        
        if (username == null) {
            System.out.println("User not authenticated - returning 401");
            return ResponseEntity.status(401).body(Map.of("error", "User not logged in"));
        }

        try {
            System.out.println("Loading cart for user: " + username);
            
            User user = userService.getUserByUsername(username);
            if (user == null) {
                System.out.println("User not found in database: " + username);
                return ResponseEntity.status(401).body(Map.of("error", "User not found"));
            }
            
            List<CartItem> items = cartService.listCartItems(user);
            System.out.println("Found " + items.size() + " items in cart");
            
            double totalPrice = cartService.calculateTotal(items);
            System.out.println("Total price calculated: " + totalPrice); // Debug log
            
            // Return cart data with items
            return ResponseEntity.ok(Map.of(
                "items", items,
                "totalPrice", totalPrice
            ));
        } catch (Exception e) {
            logger.error("Error loading cart: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of("error", "Failed to load cart: " + e.getMessage()));
        }
    }

    // 5. API to Update Quantity
    @PutMapping("/api/cart/update")
    @ResponseBody
    public ResponseEntity<?> updateQuantity(@RequestBody Map<String, Object> payload, HttpSession session) {
        String username = (String) session.getAttribute("user");
        
        if (username == null) {
            return ResponseEntity.status(401).body(Map.of("error", "User not logged in"));
        }
        
        try {
            Long itemId = Long.valueOf(payload.get("itemId").toString());
            Integer quantity = Integer.valueOf(payload.get("quantity").toString());
            
            cartService.updateQuantity(itemId, quantity);
            return ResponseEntity.ok(Map.of("message", "Quantity updated"));
        } catch (NumberFormatException e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
  /**
     * API to Remove Single Item
     * ROBUSTNESS: Uses @PathVariable to capture the ID from the URL
     */
    @DeleteMapping("/api/cart/remove/{itemId}")
    @ResponseBody
    public ResponseEntity<?> removeItem(@PathVariable Long itemId, HttpSession session) {
        // Compatibility check for both session keys
        String username = (String) session.getAttribute("user");
        if (username == null) username = (String) session.getAttribute("username");
        
        if (username == null) {
            return ResponseEntity.status(401).body(Map.of("error", "User not logged in"));
        }

        try {
            // High Cohesion: Delegating deletion to the specialized service
            cartService.removeItem(itemId);
            return ResponseEntity.ok(Map.of("message", "Item removed successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to remove item: " + e.getMessage()));
        }
    }

    @DeleteMapping("/api/cart/clear")
    @ResponseBody
    public ResponseEntity<?> clearCart(HttpSession session) {
        String username = (String) session.getAttribute("user");
        if (username == null) username = (String) session.getAttribute("username");
        
        if (username == null) {
            return ResponseEntity.status(401).body(Map.of("error", "User not logged in"));
        }

        try {
            User user = userService.getUserByUsername(username);
            cartService.clearCart(user);
            return ResponseEntity.ok(Map.of("message", "Cart cleared successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to clear cart: " + e.getMessage()));
        }
    }
}