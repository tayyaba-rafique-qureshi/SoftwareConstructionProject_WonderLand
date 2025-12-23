package com.wonderland.services;

import java.util.List;

import com.wonderland.models.Order;

/**
 * Order Service Interface
 * Low Coupling: Controllers depend on interface, not implementation
 */
public interface IOrderService {
    /**
     * Create a new order
     * @param order Order object with customer and payment details
     * @return Created order with generated ID
     */
    Order createOrder(Order order);
    
    /**
     * Get order by ID
     * @param orderId Order ID
     * @return Order object
     */
    Order getOrderById(int orderId);
    
    /**
     * Get all orders for a user
     * @param userId User ID
     * @return List of orders
     */
    List<Order> getOrdersByUserId(int userId);
    
    /**
     * Update order status
     * @param orderId Order ID
     * @param status New status
     * @return true if updated successfully
     */
    boolean updateOrderStatus(int orderId, String status);
    
    /**
     * Get all orders (for admin)
     * @return List of all orders
     */
    List<Order> getAllOrders();
    
    /**
     * Get orders by status
     * @param status Order status
     * @return List of orders with specified status
     */
    List<Order> getOrdersByStatus(String status);
}
