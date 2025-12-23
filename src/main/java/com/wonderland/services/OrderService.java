package com.wonderland.services;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.lang.NonNull; // Added for annotations
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wonderland.models.Order;
import com.wonderland.models.OrderItem;

/**
 * Order Service Implementation
 * High Cohesion: Handles only order-related business logic
 * Demonstrates Transaction Management for data integrity
 */
@Service
public class OrderService implements IOrderService {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private IEmailService emailService; // Low coupling: depends on interface

    @Override
    @Transactional // Robustness: Ensures atomicity (all or nothing)
    public Order createOrder(Order order) {
        // 1. Set initial state
        order.setOrderStatus("PENDING"); 
        order.calculateTotal(); 
        
        // 2. Insert order record
        String orderSql = "INSERT INTO orders (user_id, firstname, lastname, email, phone, address, city, " +
                         "postal_code, country, shipping_method, shipping_cost, payment_method, subtotal, " +
                         "discount, total, order_status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        KeyHolder keyHolder = new GeneratedKeyHolder();
        
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(orderSql, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, order.getUserId());
            ps.setString(2, order.getFirstname());
            ps.setString(3, order.getLastname());
            ps.setString(4, order.getEmail());
            ps.setString(5, order.getPhone());
            ps.setString(6, order.getAddress());
            ps.setString(7, order.getCity());
            ps.setString(8, order.getPostalCode());
            ps.setString(9, order.getCountry());
            ps.setString(10, order.getShippingMethod());
            ps.setDouble(11, order.getShippingCost());
            ps.setString(12, order.getPaymentMethod());
            ps.setDouble(13, order.getSubtotal());
            ps.setDouble(14, order.getDiscount());
            ps.setDouble(15, order.getTotal());
            ps.setString(16, order.getOrderStatus());
            return ps;
        }, keyHolder);
        
        // Robust Key Retrieval
        int orderId;
        Number key = keyHolder.getKey();
        if (key != null) {
            orderId = key.intValue();
            order.setId(orderId);
        } else {
            throw new RuntimeException("Failed to generate Order ID");
        }
        
        // 3. Insert order items & Update stock
        String itemSql = "INSERT INTO order_items (order_id, toy_id, quantity, price) VALUES (?, ?, ?, ?)";
        for (OrderItem item : order.getItems()) {
            jdbcTemplate.update(itemSql, orderId, item.getToyId(), item.getQuantity(), item.getPrice());
            
            // Call the private method defined below
            updateToyStock(item.getToyId(), item.getQuantity()); 
        }
        
        // 4. Load details for email
        order.setItems(getOrderItemsWithDetails(orderId));
        
        // 5. Send confirmation email
        try {
            emailService.sendOrderConfirmationEmail(order);
            System.out.println("✅ Order confirmation email sent for Order #" + orderId);
        } catch (Exception e) {
            System.err.println("⚠️ Warning: Order saved but email failed: " + e.getMessage());
        }
        
        return order;
    }

   // Wonderland/src/main/java/com/wonderland/services/OrderService.java

// ... existing imports ...

// ... inside OrderService class ...

    /**
     * Update toy inventory
     * Modularization: Separate method for stock management
     */
    private void updateToyStock(int toyId, int quantity) {
        // 1. Decrement Stock
        String updateSql = "UPDATE toys SET stock_quantity = stock_quantity - ? WHERE id = ? AND stock_quantity >= ?";
        int rowsAffected = jdbcTemplate.update(updateSql, quantity, toyId, quantity);
        
        if (rowsAffected == 0) {
            throw new RuntimeException("Insufficient stock for toy ID: " + toyId);
        }

        // 2. CHECK STOCK LEVEL (The Spicy Part 🌶️)
        try {
            String checkSql = "SELECT name, stock_quantity FROM toys WHERE id = ?";
            
            jdbcTemplate.query(checkSql, rs -> {
                String name = rs.getString("name");
                int currentStock = rs.getInt("stock_quantity");
                
                // Trigger alert if stock is <= 15
                if (currentStock <= 15) {
                    emailService.sendLowStockAlert(toyId, name, currentStock);
                }
            }, toyId);
            
        } catch (Exception e) {
            // Don't fail the order just because the alert check failed
            System.err.println("⚠️ Stock check failed: " + e.getMessage());
        }
    }

// ... rest of file ...

    @Override
    public Order getOrderById(int orderId) {
        try {
            String sql = "SELECT * FROM orders WHERE id = ?";
            Order order = jdbcTemplate.queryForObject(sql, new OrderRowMapper(), orderId);
            
            if (order != null) {
                String itemsSql = "SELECT oi.*, t.name as toy_name, t.image_url FROM order_items oi " +
                                 "JOIN toys t ON oi.toy_id = t.id WHERE oi.order_id = ?";
                List<OrderItem> items = jdbcTemplate.query(itemsSql, new OrderItemRowMapper(), orderId);
                order.setItems(items);
            }
            return order;
        } catch (org.springframework.dao.EmptyResultDataAccessException e) {
            return null; // Robustness: Handle non-existent IDs
        }
    }



  @Override
public boolean updateOrderStatus(int orderId, String newStatus) {
    // 1. Fetch current order to check its state
    Order currentOrder = getOrderById(orderId);
    if (currentOrder == null) return false;

    String currentStatus = currentOrder.getOrderStatus();

    // 2. APPLY STATE TRANSITION LOGIC (One-way Street)
    // If order is SHIPPED or DELIVERED, it cannot go back to PENDING, PROCESSING, or be CANCELED
    if ("SHIPPED".equalsIgnoreCase(currentStatus) || "DELIVERED".equalsIgnoreCase(currentStatus)) {
        if ("PENDING".equalsIgnoreCase(newStatus) || 
            "PROCESSING".equalsIgnoreCase(newStatus) || 
            "CANCELED".equalsIgnoreCase(newStatus)) {
            
            System.err.println("❌ Invalid Transition: Order #" + orderId + 
                               " is already " + currentStatus + " and cannot be moved to " + newStatus);
            return false; // Blocks the update
        }
    }
    
    // 3. Prevent changes once DELIVERED
    if ("DELIVERED".equalsIgnoreCase(currentStatus)) {
        System.err.println("❌ Invalid Transition: Delivered orders are final.");
        return false; 
    }

    // 4. If valid, proceed with update
    String sql = "UPDATE orders SET order_status = ? WHERE id = ?";
    boolean updated = jdbcTemplate.update(sql, newStatus, orderId) > 0;

    if (updated) {
        try {
            emailService.sendOrderStatusUpdateEmail(currentOrder, newStatus);
        } catch (Exception e) {
            System.err.println("⚠️ Status updated in DB but notification failed.");
        }
    }
    return updated;
}

    /**
     * Get order items with toy details
     * Modularization: Reusable helper method for fetching order items
     * High Cohesion: Encapsulates item-fetching logic
     */
    private List<OrderItem> getOrderItemsWithDetails(int orderId) {
        String sql = "SELECT oi.*, t.name as toy_name, t.image_url FROM order_items oi " +
                     "JOIN toys t ON oi.toy_id = t.id WHERE oi.order_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            OrderItem item = new OrderItem();
            item.setId(rs.getInt("id"));
            item.setOrderId(rs.getInt("order_id"));
            item.setToyId(rs.getInt("toy_id"));
            item.setQuantity(rs.getInt("quantity"));
            item.setPrice(rs.getDouble("price"));
            item.setToyName(rs.getString("toy_name"));
            item.setToyImage(rs.getString("image_url"));
            return item;
        }, orderId);
    }
// Wonderland/src/main/java/com/wonderland/services/OrderService.java

@Override
public List<Order> getAllOrders() {
    String sql = "SELECT * FROM orders ORDER BY order_date DESC";
    List<Order> orders = jdbcTemplate.query(sql, new OrderRowMapper());
    
    // CRITICAL FIX: Populate items for each order so size() is not 0
    for (Order order : orders) {
        order.setItems(getOrderItemsWithDetails(order.getId()));
    }
    return orders;
}

@Override
public List<Order> getOrdersByStatus(String status) {
    String sql = "SELECT * FROM orders WHERE order_status = ? ORDER BY order_date DESC";
    List<Order> orders = jdbcTemplate.query(sql, new OrderRowMapper(), status);
    
    // CRITICAL FIX: Populate items for each order
    for (Order order : orders) {
        order.setItems(getOrderItemsWithDetails(order.getId()));
    }
    return orders;
}

@Override
public List<Order> getOrdersByUserId(int userId) {
    String sql = "SELECT * FROM orders WHERE user_id = ? ORDER BY order_date DESC";
    List<Order> orders = jdbcTemplate.query(sql, new OrderRowMapper(), userId);
    
    // CRITICAL FIX: Populate items for each order
    for (Order order : orders) {
        order.setItems(getOrderItemsWithDetails(order.getId()));
    }
    return orders;
}
    // --- RowMappers with @NonNull to satisfy compiler ---

    private static class OrderRowMapper implements RowMapper<Order> {
        @Override
        public Order mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
            Order order = new Order();
            order.setId(rs.getInt("id"));
            order.setUserId(rs.getInt("user_id"));
            order.setFirstname(rs.getString("firstname"));
            order.setLastname(rs.getString("lastname"));
            order.setEmail(rs.getString("email"));
            order.setPhone(rs.getString("phone"));
            order.setAddress(rs.getString("address"));
            order.setCity(rs.getString("city"));
            order.setPostalCode(rs.getString("postal_code"));
            order.setCountry(rs.getString("country"));
            order.setShippingMethod(rs.getString("shipping_method"));
            order.setShippingCost(rs.getDouble("shipping_cost"));
            order.setPaymentMethod(rs.getString("payment_method"));
            order.setSubtotal(rs.getDouble("subtotal"));
            order.setDiscount(rs.getDouble("discount"));
            order.setTotal(rs.getDouble("total"));
            order.setOrderStatus(rs.getString("order_status"));
            order.setOrderDate(rs.getTimestamp("order_date"));
            return order;
        }
    }

    private static class OrderItemRowMapper implements RowMapper<OrderItem> {
        @Override
        public OrderItem mapRow(@NonNull ResultSet rs, int rowNum) throws SQLException {
            OrderItem item = new OrderItem();
            item.setId(rs.getInt("id"));
            item.setOrderId(rs.getInt("order_id"));
            item.setToyId(rs.getInt("toy_id"));
            item.setQuantity(rs.getInt("quantity"));
            item.setPrice(rs.getDouble("price"));
            item.setToyName(rs.getString("toy_name"));
            item.setToyImage(rs.getString("image_url"));
            return item;
        }
    }
}