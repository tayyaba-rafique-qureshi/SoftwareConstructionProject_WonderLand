<div align="center">

# 🧸 WonderLand Toystore

### A Full-Stack E-Commerce Platform for Toys & Games

[![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=java)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen?style=flat-square&logo=springboot)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.x-blue?style=flat-square&logo=mysql)](https://www.mysql.com/)
[![Maven](https://img.shields.io/badge/Maven-3.x-red?style=flat-square&logo=apachemaven)](https://maven.apache.org/)
[![Selenium](https://img.shields.io/badge/Selenium-4.21.0-43B02A?style=flat-square&logo=selenium)](https://www.selenium.dev/)
[![TestNG](https://img.shields.io/badge/TestNG-7.10.2-red?style=flat-square)](https://testng.org/)
[![JaCoCo](https://img.shields.io/badge/Coverage-JaCoCo-yellow?style=flat-square)](https://www.jacoco.org/)
[![License](https://img.shields.io/badge/License-MIT-lightgrey?style=flat-square)](LICENSE)

</div>

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Key Features](#-key-features)
- [Architecture](#-architecture)
- [Technology Stack](#-technology-stack)
- [Project Structure](#-project-structure)
- [Prerequisites](#-prerequisites)
- [Setup & Installation](#-setup--installation)
- [Running the Application](#-running-the-application)
- [API Reference](#-api-reference)
- [Automated Testing with Selenium WebDriver](#-automated-testing-with-selenium-webdriver)
- [Code Coverage](#-code-coverage)
- [Contributing](#-contributing)
- [Authors](#-authors)
- [License](#-license)

---

## 🌟 Overview

**WonderLand** is a feature-rich, full-stack e-commerce web application built for a toy and games store. It offers a complete shopping experience — from browsing a product catalog with smart filters, to placing and tracking orders — backed by a robust Spring Boot REST API and a MySQL relational database.

The project was developed as part of a **Software Construction** course, applying industry best practices including:

- **Low Coupling / High Cohesion** via service interfaces
- **RESTful API design** using Spring Boot
- **Role-Based Access Control** (Admin / Customer)
- **MM-Path based integration testing** with Selenium WebDriver

---

## ✨ Key Features

### 🛍️ Customer-Facing
| Feature | Description |
|---|---|
| **Product Catalog** | Paginated toy listings with search, category, age-group, and target-audience filters |
| **Product Detail Page** | Full product info with a **Market Basket Analysis** recommendation engine (shows "frequently bought together" toys) |
| **Shopping Cart** | Add, update quantity, remove items, and clear cart — persisted server-side per user session |
| **Toy Quiz** | Interactive quiz that recommends toys based on age, interest, and budget |
| **Checkout** | Full checkout flow: shipping address, shipping method, payment method (Cash on Delivery / Card) |
| **Coupon Discounts** | Apply discount coupons at checkout; server-side validation prevents client-side tampering |
| **Order History** | Customers can view all their past orders and statuses |
| **User Registration** | Account creation with username, email, password, and newsletter opt-in |
| **User Authentication** | Session-based login/logout |
| **Email Notifications** | Welcome emails, order confirmation, order status updates, and password-change alerts |

### 🔧 Admin Panel
| Feature | Description |
|---|---|
| **Analytics Dashboard** | Real-time KPIs: total revenue, total orders, pending orders, items sold |
| **Revenue Chart** | Line/bar chart of daily revenue (powered by Chart.js) |
| **Order Status Chart** | Doughnut chart of orders grouped by status |
| **Hot Sellers** | Top 5 best-selling toys ranked by order frequency |
| **Order Management** | View, search, filter, and update order statuses (PENDING → SHIPPED → DELIVERED / CANCELLED) |
| **CSV Export** | Export orders to a downloadable CSV file |
| **Toy Management** | Add, edit, delete toys; upload product images |
| **Inventory & Restock** | View low-stock toys; adjust stock quantities |
| **Flash Sales** | Apply percentage discounts to entire categories; broadcast to all subscribed users via email |
| **Coupon Management** | Create coupon codes with discount percentage and expiry dates; auto-broadcast to subscribers |
| **Data Import** | Scrape and import toy listings from external online stores via a built-in HTTP client |
| **Route Guard** | Admin pages are protected — non-admin users are automatically redirected |

---

## 🏗️ Architecture

WonderLand follows a **layered MVC architecture** built on Spring Boot:

```
┌─────────────────────────────────────────────────┐
│                  Frontend Layer                  │
│  HTML5 · Vanilla JavaScript · CSS3 · Chart.js    │
│  (Static files served by Spring Boot)            │
└────────────────────┬────────────────────────────┘
                     │ HTTP / REST API calls
┌────────────────────▼────────────────────────────┐
│              Controller Layer (REST)             │
│  AdminController · CartController               │
│  CheckoutController · InventoryController       │
│  LoginController · RegisterController           │
│  QuizController · BrandController               │
│  ScrapingController                             │
└────────────────────┬────────────────────────────┘
                     │ Service interfaces (Low Coupling)
┌────────────────────▼────────────────────────────┐
│                 Service Layer                    │
│  ToyService · CartService · OrderService        │
│  UserService · CouponService · EmailService     │
└────────────────────┬────────────────────────────┘
                     │ JdbcTemplate (Spring JDBC)
┌────────────────────▼────────────────────────────┐
│                 Data Layer                       │
│           MySQL 8.x  (wonderland_db)            │
│  toys · users · orders · order_items           │
│  cart_items · coupons                           │
└─────────────────────────────────────────────────┘
```

**Key Design Decisions:**
- **Low Coupling**: Controllers depend on service *interfaces* (`IOrderService`, `IUserService`, `IEmailService`), not concrete implementations.
- **High Cohesion**: Each service class is responsible for exactly one domain (toys, cart, orders, users, email, coupons).
- **Session-based Auth**: `HttpSession` stores the logged-in username; protected endpoints return `401 Unauthorized` if the session is empty.
- **Server-side Discount Validation**: Coupon discounts are recalculated on the server to prevent client-side tampering.

---

## 🛠️ Technology Stack

### Backend
| Technology | Version | Purpose |
|---|---|---|
| **Java** | 21 | Primary programming language |
| **Spring Boot** | 3.2.0 | Application framework (web, auto-config, devtools) |
| **Spring MVC** | (via Spring Boot) | RESTful controller layer |
| **Spring JDBC** | (via Spring Boot) | Database access using `JdbcTemplate` |
| **Spring Mail** | (via Spring Boot) | Email notifications via SMTP |
| **MySQL Connector/J** | (managed) | JDBC driver for MySQL |
| **Jackson** | (managed) | JSON serialisation/deserialisation |
| **SLF4J / Logback** | (managed) | Application logging |

### Frontend
| Technology | Purpose |
|---|---|
| **HTML5** | Page structure |
| **CSS3** | Styling with custom properties (CSS variables) |
| **Vanilla JavaScript (ES6+)** | Dynamic DOM manipulation and REST API calls (`fetch`) |
| **Google Fonts (Fredoka)** | Custom brand typography |
| **Chart.js** | Interactive revenue and order-status charts in the admin panel |

### Testing
| Technology | Version | Purpose |
|---|---|---|
| **Selenium WebDriver** | 4.21.0 | Browser automation for UI integration tests |
| **WebDriverManager** | 5.8.0 | Automatic ChromeDriver setup — no manual driver installation needed |
| **TestNG** | 7.10.2 | Test execution framework with priority ordering |
| **JaCoCo** | 0.8.11 | Code coverage instrumentation and HTML/XML report generation |
| **Maven Surefire Plugin** | 3.2.5 | Runs TestNG suite as part of the Maven build lifecycle |

### Build & DevOps
| Tool | Purpose |
|---|---|
| **Apache Maven** | Dependency management and build lifecycle |
| **Spring Boot DevTools** | Hot reload during development |

---

## 📁 Project Structure

```
SoftwareConstructionProject_WonderLand/
├── pom.xml                                  # Maven build configuration
└── src/
    ├── main/
    │   ├── java/com/wonderland/
    │   │   ├── WonderlandApplication.java   # Spring Boot entry point
    │   │   ├── config/
    │   │   │   └── WebConfig.java           # CORS / MVC configuration
    │   │   ├── controllers/
    │   │   │   ├── AdminController.java     # Admin REST API
    │   │   │   ├── BrandController.java     # Brand listings API
    │   │   │   ├── CartController.java      # Cart CRUD REST API
    │   │   │   ├── CheckoutController.java  # Order placement & coupon validation
    │   │   │   ├── InventoryController.java # Toy inventory REST API
    │   │   │   ├── LoginController.java     # Session-based login
    │   │   │   ├── QuizController.java      # Toy recommendation quiz
    │   │   │   ├── RegisterController.java  # User registration
    │   │   │   └── ScrapingController.java  # External toy data import
    │   │   ├── models/
    │   │   │   ├── CartItem.java
    │   │   │   ├── Coupon.java
    │   │   │   ├── Order.java
    │   │   │   ├── OrderItem.java
    │   │   │   ├── QuizAnswers.java
    │   │   │   ├── Toy.java
    │   │   │   ├── ToyUpdateDto.java
    │   │   │   ├── User.java
    │   │   │   └── VideoGame.java
    │   │   └── services/
    │   │       ├── CartService.java
    │   │       ├── CouponService.java
    │   │       ├── EmailService.java        # JavaMail implementation
    │   │       ├── IEmailService.java       # Email interface
    │   │       ├── IOrderService.java       # Order interface
    │   │       ├── IUserService.java        # User interface
    │   │       ├── OrderService.java
    │   │       ├── ToyService.java          # Market Basket Analysis & filtering
    │   │       └── UserService.java
    │   └── resources/
    │       ├── application.properties       # App & DB configuration
    │       └── static/                      # Frontend (served as static files)
    │           ├── index.html               # Home page
    │           ├── shop.html                # Product catalog
    │           ├── product.html             # Product detail + recommendations
    │           ├── cart.html                # Shopping cart
    │           ├── checkout.html            # Checkout & order placement
    │           ├── orders.html              # Order history
    │           ├── quiz.html                # Toy recommendation quiz
    │           ├── register.html            # User registration
    │           ├── admin.html               # Admin dashboard
    │           └── assets/
    │               ├── css/                 # Page-specific stylesheets
    │               ├── js/                  # Page-specific JavaScript modules
    │               └── img/                 # Banners, logos, product images
    └── test/
        ├── java/com/wonderland/selenium/
        │   ├── WonderlandSeleniumTests.java # All Selenium test cases
        │   └── TestConfig.java             # Centralised test configuration
        └── resources/
            └── testng.xml                  # TestNG suite configuration
```

---

## 📦 Prerequisites

Before you begin, ensure the following are installed on your system:

| Requirement | Version | Notes |
|---|---|---|
| **Java JDK** | 21 | [Download](https://www.oracle.com/java/technologies/downloads/) |
| **Apache Maven** | 3.8+ | [Download](https://maven.apache.org/download.cgi) |
| **MySQL Server** | 8.x | [Download](https://dev.mysql.com/downloads/mysql/) |
| **Google Chrome** | Latest | Required for Selenium tests (WebDriverManager handles the driver automatically) |
| **Git** | Any | For cloning the repository |

---

## ⚙️ Setup & Installation

### 1. Clone the Repository

```bash
git clone https://github.com/tayyaba-rafique-qureshi/SoftwareConstructionProject_WonderLand.git
cd SoftwareConstructionProject_WonderLand
```

### 2. Create the MySQL Database

Connect to your MySQL instance and run:

```sql
CREATE DATABASE wonderland_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE wonderland_db;

CREATE TABLE users (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(100) UNIQUE NOT NULL,
    firstname   VARCHAR(100),
    lastname    VARCHAR(100),
    email       VARCHAR(200) UNIQUE NOT NULL,
    password    VARCHAR(255) NOT NULL,
    role        VARCHAR(20) DEFAULT 'CUSTOMER',
    subscribed  BOOLEAN DEFAULT FALSE
);

CREATE TABLE toys (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(255) NOT NULL,
    price           DOUBLE NOT NULL,
    brand           VARCHAR(100),
    min_age         INT DEFAULT 0,
    target_audience VARCHAR(50),
    category        VARCHAR(100),
    item_type       VARCHAR(50),
    image_url       VARCHAR(500),
    stock_quantity  INT DEFAULT 0,
    description     TEXT,
    is_on_sale      BOOLEAN DEFAULT FALSE,
    sale_price      DOUBLE
);

CREATE TABLE cart_items (
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id  INT NOT NULL,
    toy_id   INT NOT NULL,
    quantity INT DEFAULT 1,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (toy_id)  REFERENCES toys(id)
);

CREATE TABLE orders (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    user_id         INT NOT NULL,
    firstname       VARCHAR(100),
    lastname        VARCHAR(100),
    email           VARCHAR(200),
    phone           VARCHAR(30),
    address         VARCHAR(500),
    city            VARCHAR(100),
    postal_code     VARCHAR(20),
    country         VARCHAR(100),
    shipping_method VARCHAR(50),
    payment_method  VARCHAR(50),
    subtotal        DOUBLE,
    shipping_cost   DOUBLE,
    discount        DOUBLE DEFAULT 0,
    total           DOUBLE,
    order_status    VARCHAR(50) DEFAULT 'PENDING',
    order_date      DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE order_items (
    id        INT AUTO_INCREMENT PRIMARY KEY,
    order_id  INT NOT NULL,
    toy_id    INT NOT NULL,
    quantity  INT NOT NULL,
    price     DOUBLE NOT NULL,
    FOREIGN KEY (order_id) REFERENCES orders(id),
    FOREIGN KEY (toy_id)   REFERENCES toys(id)
);

CREATE TABLE coupons (
    id               INT AUTO_INCREMENT PRIMARY KEY,
    code             VARCHAR(50) UNIQUE NOT NULL,
    discount_percent DOUBLE NOT NULL,
    expiry_date      DATE,
    is_active        BOOLEAN DEFAULT TRUE
);

-- Seed a default admin user (password: admin123 — change in production!)
INSERT INTO users (username, firstname, lastname, email, password, role, subscribed)
VALUES ('admin', 'Admin', 'User', 'admin@wonderland.com', 'admin123', 'ADMIN', FALSE);

-- Seed a sample coupon
INSERT INTO coupons (code, discount_percent, expiry_date, is_active)
VALUES ('SUMMER20', 20.0, '2026-12-31', TRUE);
```

### 3. Configure `application.properties`

Edit `src/main/resources/application.properties` to match your environment:

```properties
# ── Database ──────────────────────────────────────────────────────────────────
spring.datasource.url=jdbc:mysql://localhost:3306/wonderland_db
spring.datasource.username=YOUR_MYSQL_USERNAME
spring.datasource.password=YOUR_MYSQL_PASSWORD
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# ── Email (Gmail SMTP) ────────────────────────────────────────────────────────
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=YOUR_GMAIL_ADDRESS
spring.mail.password=YOUR_APP_PASSWORD          # Use a Gmail App Password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# ── Server ────────────────────────────────────────────────────────────────────
server.port=8080

# ── File Uploads ──────────────────────────────────────────────────────────────
# Path where product images are saved (must exist on your filesystem)
app.upload.dir=/path/to/your/uploads/

# ── DevTools ──────────────────────────────────────────────────────────────────
spring.devtools.restart.enabled=true
```

> **Gmail App Password**: If you use 2-Step Verification on your Google account, generate an [App Password](https://myaccount.google.com/apppasswords) and use it for `spring.mail.password`.

### 4. Create the Upload Directory

```bash
# Linux / macOS
mkdir -p /path/to/your/uploads

# Windows PowerShell
New-Item -ItemType Directory -Force -Path "C:\uploads"
```

### 5. Install Dependencies

```bash
mvn clean install -DskipTests
```

---

## ▶️ Running the Application

```bash
mvn spring-boot:run
```

The application starts on **http://localhost:8080**.

| Page | URL |
|---|---|
| Home | http://localhost:8080/index.html |
| Shop | http://localhost:8080/shop.html |
| Quiz | http://localhost:8080/quiz.html |
| Cart | http://localhost:8080/cart.html |
| Checkout | http://localhost:8080/checkout.html |
| Orders | http://localhost:8080/orders.html |
| Register | http://localhost:8080/register.html |
| Admin Panel | http://localhost:8080/admin.html |

---

## 📡 API Reference

All REST endpoints are prefixed with the application base URL (`http://localhost:8080`).

### Authentication
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/LoginServlet` | Login with `email` + `password` form fields |
| `GET` | `/logout` | Invalidate the current session |
| `POST` | `/api/register` | Register a new user account |

### Inventory / Toys
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/inventory` | Get paginated toys (`page`, `size`, `category`, `search`, `ageGroup`, `audience`) |
| `GET` | `/api/inventory/{id}` | Get a single toy by ID |
| `POST` | `/api/inventory` | Add a new toy (multipart form, admin only) |
| `PUT` | `/api/inventory/{id}` | Update toy details |
| `DELETE` | `/api/inventory/{id}` | Delete a toy |
| `PUT` | `/api/inventory/{id}/restock` | Adjust stock quantity |

### Shopping Cart
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/cart` | Get the current user's cart items |
| `GET` | `/api/cart/count` | Get the number of items in the cart |
| `POST` | `/api/cart/add` | Add an item (`toyId`, `quantity`) |
| `PUT` | `/api/cart/update` | Update item quantity (`itemId`, `quantity`) |
| `DELETE` | `/api/cart/remove/{itemId}` | Remove a single item |
| `DELETE` | `/api/cart/clear` | Clear the entire cart |

### Orders & Checkout
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/orders` | Place an order (checkout) |
| `GET` | `/api/orders/user` | Get the current user's order history |
| `GET` | `/api/orders/{orderId}` | Get a specific order |
| `POST` | `/api/coupons/validate` | Validate and apply a coupon code |

### Quiz
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/quiz/categories` | Get categories for a given `ageGroup` |
| `POST` | `/api/quiz/submit` | Submit quiz answers to get toy recommendations |

### Admin
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/admin/analytics` | Dashboard KPIs, hot sellers, chart data |
| `GET` | `/api/admin/orders` | All orders (optional `status` filter) |
| `GET` | `/api/admin/orders/{orderId}` | Order details |
| `GET` | `/api/admin/orders/search` | Search order by ID |
| `GET` | `/api/admin/orders/export` | Download orders as CSV |
| `PUT` | `/api/admin/orders/{orderId}/status` | Update an order's status |
| `POST` | `/api/admin/marketing/sale` | Start a flash sale and broadcast emails |
| `POST` | `/api/admin/marketing/coupon` | Create a coupon and broadcast to subscribers |

---

## 🤖 Automated Testing with Selenium WebDriver

WonderLand includes a complete **MM-Path (Multiple-condition / Multiple-path) based integration test suite** using **Selenium WebDriver 4** with **TestNG** to exercise the full application stack — from the browser UI down through the REST API to the database.

### Test Strategy

Each test case maps to a **MM-Path** tracing the flow from the browser through Spring controllers and services to the MySQL database:

| Test Case | MM-Path |
|---|---|
| TC-01 | UI → `RegisterController` → `UserService.registerUser()` → `users` table |
| TC-03 | UI → `LoginController.login()` → `UserService.authenticateUser()` → `HttpSession` |
| TC-06 | UI → `CartController.addToCart()` → `CartService.addToCart()` → `cart_items` table |
| TC-08 | UI → `CheckoutController.validateCoupon()` → `CouponService.getValidCoupon()` → `coupons` table |
| TC-09 | UI → `CheckoutController.processCheckout()` → `OrderService.createOrder()` → `orders` + `order_items` tables |

### Test Files

| File | Description |
|---|---|
| `src/test/java/com/wonderland/selenium/WonderlandSeleniumTests.java` | All 5 test cases |
| `src/test/java/com/wonderland/selenium/TestConfig.java` | Centralised constants: URLs, credentials, timeouts, element locators |
| `src/test/resources/testng.xml` | TestNG suite definition with priority-ordered execution |

### Prerequisites for Running Tests

1. **The application must be running** on `http://localhost:8080` before executing tests.
2. **Google Chrome** must be installed. WebDriverManager downloads the matching `chromedriver` automatically — no manual setup required.
3. The **test user** (`farhatsamreen8@gmail.com` / `simmi1`) must exist in the database. You can seed it with:
   ```sql
   INSERT INTO users (username, firstname, lastname, email, password, role, subscribed)
   VALUES ('farhat', 'Farhat', 'Samreen', 'farhatsamreen8@gmail.com', 'simmi1', 'CUSTOMER', FALSE);
   ```
4. At least one toy must exist in the `toys` table, and the coupon `SUMMER20` must be active.

### Running the Tests

#### Run the full Selenium test suite

```bash
# Start the application first (in a separate terminal or background)
mvn spring-boot:run &

# Then run the tests
mvn test
```

#### Run only the Selenium suite explicitly

```bash
mvn test -DsuiteXmlFile=src/test/resources/testng.xml
```

#### Run a single test method

```bash
mvn test -Dtest=WonderlandSeleniumTests#testUserLogin
```

#### Run in headless mode (for CI/CD)

Open `TestConfig.java` and set:

```java
public static final boolean HEADLESS_MODE = true;
```

Or pass a system property at runtime:

```bash
mvn test -Dheadless=true
```

### Test Suite Configuration (`testng.xml`)

```xml
<suite name="Wonderland Toystore Selenium Test Suite" verbose="1">
    <test name="MM-Path Integration Tests" preserve-order="true">
        <classes>
            <class name="com.wonderland.selenium.WonderlandSeleniumTests">
                <methods>
                    <include name="testUserRegistration"/>   <!-- TC-01 priority 1 -->
                    <include name="testUserLogin"/>          <!-- TC-03 priority 2 -->
                    <include name="testAddItemToCart"/>      <!-- TC-06 priority 3 -->
                    <include name="testApplyCoupon"/>        <!-- TC-08 priority 4 -->
                    <include name="testCheckoutProcess"/>    <!-- TC-09 priority 5 -->
                </methods>
            </class>
        </classes>
    </test>
</suite>
```

Tests run **in priority order** because later tests (e.g., checkout) depend on data created by earlier tests (e.g., add to cart).

### WebDriver Setup Details

```java
// WebDriverManager automatically downloads the correct ChromeDriver version
WebDriverManager.chromedriver().setup();

driver = new ChromeDriver();
driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(15));
wait = new WebDriverWait(driver, Duration.ofSeconds(15));
driver.manage().window().maximize();
```

Key settings in `TestConfig.java`:

| Setting | Default | Description |
|---|---|---|
| `BASE_URL` | `http://localhost:8080` | Application URL under test |
| `IMPLICIT_WAIT_TIMEOUT` | `10` s | Global implicit wait |
| `EXPLICIT_WAIT_TIMEOUT` | `10` s | `WebDriverWait` timeout |
| `PAGE_LOAD_TIMEOUT` | `30` s | Page load timeout |
| `HEADLESS_MODE` | `false` | Set to `true` for CI pipelines |
| `MAXIMIZE_WINDOW` | `true` | Maximize browser window on start |

### CI/CD Considerations

When running in a CI/CD environment (e.g., GitHub Actions, Jenkins):

1. Set `HEADLESS_MODE = true` in `TestConfig.java` or use a system property.
2. Ensure Chrome is installed on the CI agent (`apt-get install -y google-chrome-stable`).
3. The application server must be started before the test phase. Use a Maven lifecycle plugin or a separate service step.
4. `TestConfig.isCIEnvironment()` automatically detects the `CI` or `JENKINS_HOME` environment variables.

---

## 📊 Code Coverage

WonderLand uses **JaCoCo** for code coverage analysis, integrated into the Maven build lifecycle.

### Generate the Coverage Report

```bash
mvn verify
```

This produces an HTML report at:

```
target/site/jacoco/index.html
```

Open it in your browser for a line-by-line coverage breakdown per class.

### Coverage Configuration (`pom.xml`)

```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.11</version>
    <executions>
        <!-- Attach agent before tests -->
        <execution>
            <id>prepare-agent</id>
            <goals><goal>prepare-agent</goal></goals>
        </execution>
        <!-- Generate report after verify phase -->
        <execution>
            <id>report</id>
            <phase>verify</phase>
            <goals><goal>report</goal></goals>
        </execution>
    </executions>
</plugin>
```

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. **Fork** the repository on GitHub.
2. **Clone** your fork locally:
   ```bash
   git clone https://github.com/YOUR_USERNAME/SoftwareConstructionProject_WonderLand.git
   ```
3. **Create a feature branch** from `main`:
   ```bash
   git checkout -b feature/your-feature-name
   ```
4. **Make your changes**, following the existing code style:
   - Use service interfaces for all cross-layer dependencies.
   - Keep controllers thin — business logic belongs in services.
   - Add or update Selenium tests for any new UI flows.
5. **Build and test** before committing:
   ```bash
   mvn clean install
   mvn test
   ```
6. **Commit** with a clear, descriptive message:
   ```bash
   git commit -m "feat: add wishlist feature with persistence"
   ```
7. **Push** to your fork and **open a Pull Request** against the `main` branch.

### Code Style Guidelines

- Follow standard Java naming conventions (camelCase for variables/methods, PascalCase for classes).
- Add Javadoc comments to public methods.
- Log errors with SLF4J (`Logger logger = LoggerFactory.getLogger(...)`); do not use raw `System.out.println` in production code.
- Validate all user inputs server-side before processing.

---

## 👩‍💻 Authors

| Name | Role |
|---|---|
| **Tayyaba Rafique Qureshi** | Backend, Frontend, Testing |
| **Zikra Khan** | Testing (Selenium WebDriver) |

---

## 📄 License

This project is licensed under the **MIT License**.

```
MIT License

Copyright (c) 2025 Tayyaba Rafique Qureshi

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
```

---

<div align="center">

Made with ❤️ by the WonderLand team · [Report an issue](https://github.com/tayyaba-rafique-qureshi/SoftwareConstructionProject_WonderLand/issues)

</div>
