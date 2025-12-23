package com.wonderland.services;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import com.wonderland.models.CartItem;
import com.wonderland.models.Toy;
import com.wonderland.models.User;

@Service
public class CartService {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private ToyService toyService;
    
    // Add item to cart
    public void addToCart(User user, int toyId, int quantity) {
        // 1. Fetch the toy to check price
        String toySql = "SELECT * FROM toys WHERE id = ?";
        Toy toy = jdbcTemplate.queryForObject(toySql, new BeanPropertyRowMapper<>(Toy.class), toyId);

        // ROBUSTNESS: Ensure toy exists
        if (toy == null) {
            throw new RuntimeException("Toy not found with ID: " + toyId);
        }

        // 2. Check if item exists in cart
        String checkSql = "SELECT COUNT(*) FROM cart_items WHERE user_id = ? AND toy_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, user.getId(), toyId);

        if (count != null && count > 0) {
            // Update quantity
            String updateSql = "UPDATE cart_items SET quantity = quantity + ? WHERE user_id = ? AND toy_id = ?";
            jdbcTemplate.update(updateSql, quantity, user.getId(), toyId);
        } else {
            // Insert new item
            String insertSql = "INSERT INTO cart_items (user_id, toy_id, quantity) VALUES (?, ?, ?)";
            jdbcTemplate.update(insertSql, user.getId(), toyId, quantity);
        }
    }
    
    // Get all cart items for a user
    public List<CartItem> listCartItems(User user) {
        String sql = "SELECT ci.id, ci.user_id, ci.toy_id, ci.quantity, ci.added_at " +
                     "FROM cart_items ci WHERE ci.user_id = ?";
        
        List<CartItem> items = jdbcTemplate.query(sql, new CartItemRowMapper(), user.getId());
        
        // Load toy details for each item
        for (CartItem item : items) {
            // FIX: Changed findById -> getToyById
            Toy toy = toyService.getToyById(item.getToyId().intValue());
            
            // LOGIC: If on sale, update the price in the object so Frontend sees the sale price
            if (toy != null && toy.isOnSale() && toy.getSalePrice() != null) {
                // Temporarily override price for display/calculation purposes
                toy.setPrice(toy.getSalePrice()); 
            }
            
            item.setToy(toy);
        }
        
        return items;
    }
    
    // Count items in cart
    public int countItems(User user) {
        String sql = "SELECT COUNT(*) FROM cart_items WHERE user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, user.getId());
        return count != null ? count : 0;
    }
    
    // Calculate total price
    public double calculateTotal(List<CartItem> items) {
        return items.stream()
                .filter(item -> item.getToy() != null)
                .mapToDouble(item -> {
                    Toy t = item.getToy();
                    // LOGIC: Use Sale Price if active
                    double effectivePrice = (t.isOnSale() && t.getSalePrice() != null) 
                                            ? t.getSalePrice() 
                                            : t.getPrice();
                    return effectivePrice * item.getQuantity();
                })
                .sum();
    }
    
    // Update quantity
    public void updateQuantity(Long itemId, int quantity) {
        String sql = "UPDATE cart_items SET quantity = ? WHERE id = ?";
        jdbcTemplate.update(sql, quantity, itemId);
    }
    
    // Remove item
    public void removeItem(Long itemId) {
        String sql = "DELETE FROM cart_items WHERE id = ?";
        jdbcTemplate.update(sql, itemId);
    }
    
    public void clearCart(User user) {
        String sql = "DELETE FROM cart_items WHERE user_id = ?";
        jdbcTemplate.update(sql, user.getId());
    }
    
    // Row Mapper
    private static class CartItemRowMapper implements RowMapper<CartItem> {
        @Override
        public CartItem mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
            CartItem item = new CartItem();
            item.setId(rs.getLong("id"));
            item.setUserId(rs.getLong("user_id"));
            item.setToyId(rs.getLong("toy_id"));
            item.setQuantity(rs.getInt("quantity"));
            item.setAddedAt(rs.getTimestamp("added_at").toLocalDateTime());
            return item;
        }
    }
}