# E-Commerce Backend API

A scalable and production-ready e-commerce backend built with **Spring Boot**, **PostgreSQL**, and **JPA/Hibernate**.  
It supports authentication, product catalog management, cart operations, order processing, and payment simulation using clean architecture and best practices.

---

## Features

- User authentication & authorization (JWT, roles)
- Product & category management
- Shopping cart system
- Order checkout & history
- Payment simulation workflow
- Role-based access control (ADMIN, USER)
- DTO mapping with ModelMapper
- Pagination & filtering
- Optimistic locking for stock control
- Soft delete support
- Global exception handling
- RESTful API design

---

## Tech Stack

- **Java 17**
- **Spring Boot**
- **Spring Security + JWT**
- **Spring Data JPA (Hibernate)**
- **PostgreSQL**
- **Maven**
- **ModelMapper**
- **Lombok**
- **Docker (optional)**

---

## Project Structure

org.example.ecommerce
├── auth
├── user
├── catalog
│ ├── product
│ └── category
├── cart
├── order
├── payment
├── common
├── config
└── EcommerceApplication.java



---

## Database Design (Core Entities)

- User ↔ Role (Many-to-Many)
- Category → Product (One-to-Many)
- User → Cart (One-to-One)
- Cart → CartItem (One-to-Many)
- User → Order (One-to-Many)
- Order → OrderItem (One-to-Many)
- Order → Payment (One-to-One)
- Payment → PaymentAttempt (One-to-Many)

---

## Requirements

- Java 17+
- Maven 3.8+
- PostgreSQL 13+
- Git

---

## Setup Instructions

### 1. Clone Repository

```bash
git clone https://github.com/your-username/your-repo-name.git
cd your-repo-name

 Create PostgreSQL Database
CREATE DATABASE ecommerce_db;
CREATE USER ecommerce_user WITH PASSWORD 'password';
GRANT ALL PRIVILEGES ON DATABASE ecommerce_db TO ecommerce_user;
