# 🎠 WonderLand Store

> A full-stack e-commerce web application for toys and video games, built with **Spring Boot** (Java) and a static HTML/CSS/JavaScript frontend.

---

## 📖 Overview

WonderLand Store is a software construction project that implements a complete online shop experience. Customers can browse toys and video games, add items to a cart, apply coupons, complete checkout, and receive email order confirmations. An admin panel supports inventory and order management, and a web-scraping module keeps product data up to date.

The backend is a **Spring Boot 3** REST/MVC application backed by a **MySQL** database. The frontend consists of plain HTML pages styled with CSS and powered by vanilla JavaScript.

---

## 🌐 Language Composition

| Language   | Share  |
|------------|--------|
| Java       | 41.1 % |
| HTML       | 28.8 % |
| JavaScript | 22.4 % |
| CSS        |  7.7 % |

---

## ✨ Features

- 🛒 **Shopping cart** – add, update, and remove items
- 🏷️ **Coupon / discount codes**
- 💳 **Checkout & order management**
- 📦 **Inventory management** (admin)
- 👤 **User registration & login**
- 📧 **Email notifications** via Gmail SMTP
- 🧩 **Interactive product quiz** to recommend items
- 🕷️ **Web-scraping controller** to fetch live product data
- 🧪 **Automated UI tests** with Selenium + TestNG
- 📊 **Code-coverage reports** via JaCoCo

---

## 🗂️ Project Structure

```
SoftwareConstructionProject_WonderLand/
├── pom.xml                          # Maven build descriptor (Spring Boot 3.2, Java 21)
└── src/
    ├── main/
    │   ├── java/com/wonderland/
    │   │   ├── WonderlandApplication.java   # Spring Boot entry point
    │   │   ├── config/                      # Security / app configuration
    │   │   ├── controllers/                 # MVC / REST controllers
    │   │   │   ├── AdminController.java
    │   │   │   ├── BrandController.java
    │   │   │   ├── CartController.java
    │   │   │   ├── CheckoutController.java
    │   │   │   ├── InventoryController.java
    │   │   │   ├── LoginController.java
    │   │   │   ├── QuizController.java
    │   │   │   ├── RegisterController.java
    │   │   │   └── ScrapingController.java
    │   │   ├── models/                      # Domain / DTO classes
    │   │   │   ├── CartItem.java
    │   │   │   ├── Coupon.java
    │   │   │   ├── Order.java  /  OrderItem.java
    │   │   │   ├── Toy.java  /  VideoGame.java
    │   │   │   ├── ToyUpdateDto.java
    │   │   │   └── User.java
    │   │   └── services/                    # Business logic
    │   │       ├── CartService.java
    │   │       ├── CouponService.java
    │   │       ├── EmailService.java        # Implements IEmailService
    │   │       ├── OrderService.java        # Implements IOrderService
    │   │       ├── ToyService.java
    │   │       └── UserService.java         # Implements IUserService
    │   └── resources/
    │       ├── application.properties       # DB, mail, server config
    │       └── static/                      # Frontend assets served by Spring
    │           ├── index.html
    │           ├── shop.html
    │           ├── product.html
    │           ├── cart.html
    │           ├── checkout.html
    │           ├── orders.html
    │           ├── quiz.html
    │           ├── register.html
    │           ├── admin.html
    │           └── assets/
    │               ├── css/
    │               ├── js/
    │               └── img/
    └── test/                                # Selenium / TestNG automated tests
        └── resources/testng.xml
```

---

## ⚙️ Prerequisites

| Tool | Version |
|------|---------|
| Java JDK | 21 |
| Apache Maven | 3.8+ |
| MySQL | 8.0+ |
| Git | any recent |
| A modern web browser | Chrome / Firefox recommended |

---

## 🚀 Getting Started

### 1 – Clone the repository

```bash
git clone https://github.com/tayyaba-rafique-qureshi/SoftwareConstructionProject_WonderLand.git
cd SoftwareConstructionProject_WonderLand
```

### 2 – Set up the database

1. Start your MySQL server.
2. Create the database:
   ```sql
   CREATE DATABASE wonderland_db;
   ```
3. (Optional) Import a seed SQL file if one is provided in the repository.

### 3 – Configure the application

Edit `src/main/resources/application.properties` and update the values for your environment:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/wonderland_db
spring.datasource.username=<your_mysql_user>
spring.datasource.password=<your_mysql_password>

spring.mail.username=<your_gmail_address>
spring.mail.password=<your_gmail_app_password>

app.upload.dir=<absolute_path_to_an_uploads_folder>
```

> ⚠️ **Never commit real credentials.** Use environment variables or a local override file (e.g. `application-local.properties`) that is listed in `.gitignore`.

### 4 – Build & run

```bash
# Compile, run tests, and package
mvn clean package

# Start the application (default port 8080)
mvn spring-boot:run
```

Open your browser at **http://localhost:8080** to view the store.

### 5 – Run the automated tests

```bash
mvn test
```

TestNG test results land in `target/surefire-reports/`.  
JaCoCo coverage reports are generated at `target/site/jacoco/index.html` after:

```bash
mvn verify
```

---

## 🖼️ Screenshots

> _Screenshots or a short demo video can be added here once the application is deployed or running locally._

---

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. **Fork** this repository.
2. **Create a feature branch:**
   ```bash
   git checkout -b feature/your-feature-name
   ```
3. **Make your changes** and add or update tests where relevant.
4. **Commit** with a clear message:
   ```bash
   git commit -m "feat: add your feature description"
   ```
5. **Push** to your fork and open a **Pull Request** against `main`.
6. Ensure all CI checks pass before requesting a review.

### Code style

- Follow standard Java conventions (camelCase for variables/methods, PascalCase for classes).
- Keep controller methods thin; put business logic in the service layer.
- Write at least one Selenium/TestNG test for each new user-facing feature.

---

## 📄 License

> _No license has been specified yet. Add one by creating a `LICENSE` file in the root of the repository (e.g., [MIT](https://choosealicense.com/licenses/mit/), [Apache-2.0](https://choosealicense.com/licenses/apache-2.0/), or [GPL-3.0](https://choosealicense.com/licenses/gpl-3.0/))._

---

## 📬 Contact

Maintained by **Tayyaba Rafique Qureshi**.  
Feel free to open an issue for bug reports, feature requests, or questions.
