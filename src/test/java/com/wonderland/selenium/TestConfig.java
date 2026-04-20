package com.wonderland.selenium;

/**
 * Test Configuration Class
 *
 * Centralized configuration for Selenium WebDriver tests
 * Modify these constants to match your environment
 *
 * @author Tayyaba Rafique, Zikra Khan
 */
public class TestConfig {

    // ==================== APPLICATION URLS ====================

    /**
     * Base URL of the application under test
     * Change this if your application runs on a different port
     */
    public static final String BASE_URL = "http://localhost:8080";

    /**
     * Individual page URLs
     */
    public static final String LOGIN_URL = BASE_URL + "/login.html";
    public static final String REGISTER_URL = BASE_URL + "/register.html";
    public static final String SHOP_URL = BASE_URL + "/shop.html";
    public static final String CART_URL = BASE_URL + "/cart.html";
    public static final String CHECKOUT_URL = BASE_URL + "/checkout.html";
    public static final String ORDERS_URL = BASE_URL + "/orders.html";

    // ==================== TEST CREDENTIALS ====================

    /**
     * Test user credentials
     * Ensure this user exists in your database before running tests
     */
    public static final String TEST_USER_EMAIL = "tayyabarafique204@gmail.com";
    public static final String TEST_USER_PASSWORD = "06afa17c";

    /**
     * New user registration data (for TC-01)
     * Username will be appended with timestamp for uniqueness
     */
    public static final String NEW_USER_PREFIX = "testuser_";
    public static final String NEW_USER_EMAIL_DOMAIN = "@test.com";
    public static final String NEW_USER_PASSWORD = "TestPass123!";

    // ==================== TEST DATA ====================

    /**
     * Coupon codes for testing
     */
    public static final String VALID_COUPON_CODE = "SUMMER20";
    public static final int EXPECTED_DISCOUNT_PERCENTAGE = 20;

    /**
     * Shipping information for checkout
     */
    public static final String TEST_CUSTOMER_NAME = "Test Customer";
    public static final String TEST_CUSTOMER_ADDRESS = "123 Test Street";
    public static final String TEST_CUSTOMER_CITY = "Test City";
    public static final String TEST_CUSTOMER_ZIP = "12345";
    public static final String TEST_CUSTOMER_PHONE = "1234567890";

    /**
     * Payment method
     */
    public static final String PAYMENT_METHOD_CASH = "cash";
    public static final String PAYMENT_METHOD_CARD = "card";

    // ==================== WEBDRIVER SETTINGS ====================

    /**
     * Timeout settings (in seconds)
     */
    public static final int IMPLICIT_WAIT_TIMEOUT = 10;
    public static final int EXPLICIT_WAIT_TIMEOUT = 10;
    public static final int PAGE_LOAD_TIMEOUT = 30;

    /**
     * Browser settings
     */
    public static final boolean MAXIMIZE_WINDOW = true;
    public static final boolean HEADLESS_MODE = false; // Set to true for CI/CD

    // ==================== ELEMENT LOCATORS ====================

    /**
     * Login page locators
     */
    public static class LoginPage {
        public static final String EMAIL_FIELD = "email";
        public static final String PASSWORD_FIELD = "password";
        public static final String LOGIN_BUTTON = "loginBtn";
    }

    /**
     * Registration page locators
     */
    public static class RegisterPage {
        public static final String USERNAME_FIELD = "username";
        public static final String EMAIL_FIELD = "email";
        public static final String PASSWORD_FIELD = "password";
        public static final String CONFIRM_PASSWORD_FIELD = "confirmPassword";
        public static final String REGISTER_BUTTON = "registerBtn";
    }

    /**
     * Shop page locators
     */
    public static class ShopPage {
        public static final String PRODUCT_CARD = "product-card";
        public static final String PRODUCT_NAME = "product-name";
        public static final String ADD_TO_CART_BUTTON = ".add-to-cart-btn";
        public static final String CART_ICON = "cartIcon";
    }

    /**
     * Cart page locators
     */
    public static class CartPage {
        public static final String CART_ITEMS_CONTAINER = "cartItemsContainer";
        public static final String CART_ITEM = "cart-item";
        public static final String CART_SUCCESS = "cart-success";
    }

    /**
     * Checkout page locators
     */
    public static class CheckoutPage {
        public static final String CHECKOUT_FORM = "checkoutForm";
        public static final String FULL_NAME_FIELD = "fullName";
        public static final String ADDRESS_FIELD = "address";
        public static final String CITY_FIELD = "city";
        public static final String ZIP_CODE_FIELD = "zipCode";
        public static final String PHONE_FIELD = "phone";
        public static final String PAYMENT_METHOD_DROPDOWN = "paymentMethod";
        public static final String COUPON_CODE_FIELD = "couponCode";
        public static final String APPLY_COUPON_BUTTON = "applyCouponBtn";
        public static final String ORDER_SUMMARY = "orderSummary";
        public static final String TOTAL_AMOUNT = "totalAmount";
        public static final String DISCOUNT_MESSAGE = "discountMessage";
        public static final String DISCOUNT_ROW = "discountRow";
        public static final String PLACE_ORDER_BUTTON = "placeOrderBtn";
    }

    /**
     * Orders page locators
     */
    public static class OrdersPage {
        public static final String ORDER_ID = "orderId";
        public static final String ORDER_CONFIRMATION = "order-confirmation";
    }

    // ==================== HELPER METHODS ====================

    /**
     * Generate unique username for registration tests
     * @return Unique username with timestamp
     */
    public static String generateUniqueUsername() {
        return NEW_USER_PREFIX + System.currentTimeMillis();
    }

    /**
     * Generate unique email for registration tests
     * @return Unique email address
     */
    public static String generateUniqueEmail() {
        return NEW_USER_PREFIX + System.currentTimeMillis() + NEW_USER_EMAIL_DOMAIN;
    }

    /**
     * Check if running in CI/CD environment
     * @return true if CI environment detected
     */
    public static boolean isCIEnvironment() {
        return System.getenv("CI") != null || System.getenv("JENKINS_HOME") != null;
    }
}
