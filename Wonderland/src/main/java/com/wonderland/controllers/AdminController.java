package com.wonderland.controllers;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired; // Required for grouping
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wonderland.models.Order;
import com.wonderland.models.OrderItem;
import com.wonderland.services.CouponService;
import com.wonderland.services.EmailService;
import com.wonderland.services.IOrderService;
import com.wonderland.services.IUserService;
import com.wonderland.services.ToyService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Autowired
    private IOrderService orderService;
    
    @Autowired
    private ToyService toyService; 

    @Autowired
    private IUserService userService; 

    @Autowired
    private EmailService emailService; 

    @Autowired 
    private CouponService couponService; 

    // --- MARKETING & PROMOTIONS ---

    @PostMapping("/marketing/sale")
    public ResponseEntity<?> startSale(@RequestBody Map<String, Object> payload) {
        String category = (String) payload.get("category");
        Double discount = Double.parseDouble(payload.get("discount").toString());
        
        try {
            toyService.applyCategorySale(category, discount);
            List<String> subscribers = userService.getSubscribedEmails();
            String subject = "🔥 FLASH SALE: " + discount + "% OFF on " + category + "!";
            String body = "Don't miss out! We've slashed prices on all " + category + " items.\nShop now at: http://localhost:8081/shop.html";
            emailService.broadcastSale(subject, body, subscribers);
            return ResponseEntity.ok(Map.of("message", "Sale is LIVE! " + subscribers.size() + " emails sent."));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/marketing/coupon")
    public ResponseEntity<?> createCoupon(@RequestBody Map<String, Object> payload) {
        String code = (String) payload.get("code");
        Double discount = Double.parseDouble(payload.get("discount").toString());
        String expiry = (String) payload.get("expiry"); 
        
        try {
            if (couponService.couponExists(code)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Coupon code already exists!"));
            }
            couponService.createCoupon(code, discount, expiry);
            List<String> subscribers = userService.getSubscribedEmails();
            String subject = "🎁 A Gift For You: " + discount + "% OFF!";
            String body = "Use code " + code + " at checkout to get " + discount + "% off your order.\nValid until: " + expiry + "\nShop now: http://localhost:8081/shop.html";
            emailService.broadcastSale(subject, body, subscribers);
            return ResponseEntity.ok(Map.of("message", "Coupon " + code + " created and sent to " + subscribers.size() + " users!"));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // --- ANALYTICS (UPDATED TO FETCH CHARTS & HOT SELLERS) ---

    // --- Replace the getAnalytics method with this ---

    @GetMapping("/analytics")
    public ResponseEntity<Map<String, Object>> getAnalytics() {
        Map<String, Object> analytics = new HashMap<>();
        
        try {
            List<Order> allOrders = orderService.getAllOrders();
            
            // 1. Calculate KPI Totals
            double totalRevenue = allOrders.stream()
                .filter(o -> !"CANCELLED".equals(o.getOrderStatus()))
                .mapToDouble(Order::getTotal).sum();
            
            long totalOrders = allOrders.stream()
                .filter(o -> !"CANCELLED".equals(o.getOrderStatus())).count();
            
            long pendingOrders = allOrders.stream()
                .filter(o -> "PENDING".equals(o.getOrderStatus())).count();

            long totalItemsSold = allOrders.stream()
                .filter(o -> !"CANCELLED".equals(o.getOrderStatus()))
                .filter(o -> o.getItems() != null)
                .flatMap(o -> o.getItems().stream())
                .mapToLong(OrderItem::getQuantity).sum();

            // 2. Fetch Hot Sellers (Calls the new ToyService method)
            List<com.wonderland.models.Toy> hotSellers = toyService.getTopSellingToys(5);
            
            // 3. Prepare Revenue Chart Data (Group by Date)
            Map<String, Double> revenueMap = new HashMap<>();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            for (Order o : allOrders) {
                if (!"CANCELLED".equals(o.getOrderStatus()) && o.getOrderDate() != null) {
                    String dateKey = sdf.format(o.getOrderDate());
                    revenueMap.put(dateKey, revenueMap.getOrDefault(dateKey, 0.0) + o.getTotal());
                }
            }
            
            // 4. Prepare Status Chart Data (Group by Status)
            Map<String, Long> statusMap = allOrders.stream()
                .collect(java.util.stream.Collectors.groupingBy(Order::getOrderStatus, java.util.stream.Collectors.counting()));

            // 5. Populate the Map
            analytics.put("totalRevenue", totalRevenue);
            analytics.put("totalOrders", totalOrders);
            analytics.put("pendingOrders", pendingOrders);
            analytics.put("lowStockItems", toyService.getLowStockToys(15).size());
            analytics.put("totalItemsSold", totalItemsSold);
            
            // CRITICAL: Sending the data the JS expects
            analytics.put("hotSellers", hotSellers);
            analytics.put("revenueChart", revenueMap);
            analytics.put("statusChart", statusMap);
            
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }
    // --- ORDER MANAGEMENT ---

    @GetMapping("/orders")
    public ResponseEntity<List<Order>> getAllOrders(@RequestParam(required = false) String status) {
        if (status != null && !status.isEmpty() && !"All".equalsIgnoreCase(status)) {
            return ResponseEntity.ok(orderService.getOrdersByStatus(status));
        }
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<?> getOrderById(@PathVariable int orderId) {
        try {
            Order order = orderService.getOrderById(orderId);
            if (order != null) {
                return ResponseEntity.ok(order);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Order not found"));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to fetch order: " + e.getMessage()));
        }
    }

    @GetMapping("/orders/search")
    public ResponseEntity<?> searchOrders(@RequestParam String orderId) {
        try {
            int id = Integer.parseInt(orderId);
            Order order = orderService.getOrderById(id);
            if (order != null) return ResponseEntity.ok(List.of(order));
            else return ResponseEntity.ok(List.of());
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid Order ID format"));
        }
    }

    @GetMapping("/orders/export")
    public ResponseEntity<byte[]> exportOrdersToCSV(@RequestParam(required = false) String status) {
        try {
            List<Order> orders;
            if (status != null && !status.isEmpty() && !"All".equalsIgnoreCase(status)) {
                orders = orderService.getOrdersByStatus(status);
            } else {
                orders = orderService.getAllOrders();
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            PrintWriter writer = new PrintWriter(outputStream);
            
            writer.println("Order ID,Customer Name,Email,Phone,Address,City,Country,Order Date,Status,Subtotal,Shipping,Total,Payment Method,Items Count");
            
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (Order order : orders) {
                writer.printf("%d,%s %s,%s,%s,\"%s\",%s,%s,%s,%s,%.2f,%.2f,%.2f,%s,%d%n",
                    order.getId(),
                    order.getFirstname(),
                    order.getLastname(),
                    order.getEmail(),
                    order.getPhone(),
                    order.getAddress().replace("\"", "\"\""),
                    order.getCity(),
                    order.getCountry(),
                    order.getOrderDate() != null ? dateFormat.format(order.getOrderDate()) : "N/A",
                    order.getOrderStatus(),
                    order.getSubtotal(),
                    order.getShippingCost(),
                    order.getTotal(),
                    order.getPaymentMethod(),
                    order.getItems() != null ? order.getItems().size() : 0
                );
            }
            
            writer.flush();
            writer.close();
            
            byte[] csvBytes = outputStream.toByteArray();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            String filename = "orders-" + new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date()) + ".csv";
            headers.setContentDispositionFormData("attachment", filename);
            headers.setContentLength(csvBytes.length);
            
            return new ResponseEntity<>(csvBytes, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/orders/{orderId}/status")
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable int orderId,
            @RequestBody Map<String, String> payload) {
        
        String newStatus = payload.get("status");
        if (newStatus == null || newStatus.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Status is required"));
        }
        
        boolean updated = orderService.updateOrderStatus(orderId, newStatus);
        
        if (updated) {
            return ResponseEntity.ok(Map.of("message", "Status updated successfully"));
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Order not found or invalid transition"));
        }
    }
}