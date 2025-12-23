// Wonderland/src/main/java/com/wonderland/services/CouponService.java

package com.wonderland.services;

import java.sql.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.wonderland.models.Coupon;

@Service
public class CouponService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void createCoupon(String code, double discount, String expiryDate) {
        String sql = "INSERT INTO coupons (code, discount_percent, expiry_date, is_active) VALUES (?, ?, ?, TRUE)";
        jdbcTemplate.update(sql, code, discount, Date.valueOf(expiryDate));
    }
    
    public boolean couponExists(String code) {
        String sql = "SELECT COUNT(*) FROM coupons WHERE code = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, code);
        return count != null && count > 0;
    }

    /**
     * Retrieves a valid coupon by code.
     * Robustness: Checks for existence, active status, and expiry date.
     */
    public Coupon getValidCoupon(String code) {
        try {
            String sql = "SELECT * FROM coupons WHERE code = ? AND is_active = TRUE AND expiry_date >= CURDATE()";
            return jdbcTemplate.queryForObject(sql, new BeanPropertyRowMapper<>(Coupon.class), code);
        } catch (EmptyResultDataAccessException e) {
            return null; // Coupon not found or invalid
        }
    }
}