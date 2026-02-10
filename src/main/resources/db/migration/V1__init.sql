-- =========================
-- Enable UUID generation
-- =========================
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =========================
-- ROLES
-- =========================
CREATE TABLE roles (
                       id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                       name VARCHAR(50) NOT NULL,
                       created_at TIMESTAMP,
                       updated_at TIMESTAMP,
                       CONSTRAINT uk_roles_name UNIQUE (name)
);

-- =========================
-- USERS
-- =========================
CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                       full_name VARCHAR(120) NOT NULL,
                       email VARCHAR(180) NOT NULL,
                       password_hash TEXT NOT NULL,
                       enabled BOOLEAN NOT NULL,
                       created_at TIMESTAMP,
                       updated_at TIMESTAMP,
                       CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE INDEX idx_users_email ON users(email);

-- =========================
-- USER ROLES (M:N)
-- =========================
CREATE TABLE user_roles (
                            user_id UUID NOT NULL,
                            role_id UUID NOT NULL,
                            CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role_id),
                            CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id),
                            CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id),
                            CONSTRAINT uk_user_roles_user_role UNIQUE (user_id, role_id)
);

-- =========================
-- CATEGORIES
-- =========================
CREATE TABLE categories (
                            id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                            name VARCHAR(120) NOT NULL,
                            slug VARCHAR(160) NOT NULL,
                            description VARCHAR(500),
                            created_at TIMESTAMP,
                            updated_at TIMESTAMP,
                            CONSTRAINT uk_categories_slug UNIQUE (slug)
);

CREATE INDEX idx_categories_slug ON categories(slug);

-- =========================
-- PRODUCTS
-- =========================
CREATE TABLE products (
                          id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                          name VARCHAR(180) NOT NULL,
                          sku VARCHAR(80) NOT NULL,
                          description VARCHAR(2000),
                          price NUMERIC(19,2) NOT NULL,
                          stock_quantity INTEGER NOT NULL,
                          deleted_at TIMESTAMP,
                          version BIGINT NOT NULL,
                          active BOOLEAN NOT NULL,
                          category_id UUID NOT NULL,
                          created_at TIMESTAMP,
                          updated_at TIMESTAMP,
                          CONSTRAINT uk_products_sku UNIQUE (sku),
                          CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories(id)
);

CREATE INDEX idx_products_name ON products(name);
CREATE INDEX idx_products_category ON products(category_id);

-- =========================
-- CARTS
-- =========================
CREATE TABLE carts (
                       id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                       user_id UUID NOT NULL,
                       created_at TIMESTAMP,
                       updated_at TIMESTAMP,
                       CONSTRAINT uk_carts_user UNIQUE (user_id),
                       CONSTRAINT fk_carts_user FOREIGN KEY (user_id) REFERENCES users(id)
);

-- =========================
-- CART ITEMS
-- =========================
CREATE TABLE cart_items (
                            id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                            cart_id UUID NOT NULL,
                            product_id UUID NOT NULL,
                            quantity INTEGER NOT NULL,
                            created_at TIMESTAMP,
                            updated_at TIMESTAMP,
                            CONSTRAINT uk_cart_items_cart_product UNIQUE (cart_id, product_id),
                            CONSTRAINT fk_cart_items_cart FOREIGN KEY (cart_id) REFERENCES carts(id),
                            CONSTRAINT fk_cart_items_product FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE INDEX idx_cart_items_cart ON cart_items(cart_id);
CREATE INDEX idx_cart_items_product ON cart_items(product_id);

-- =========================
-- ORDERS
-- =========================
CREATE TABLE orders (
                        id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                        user_id UUID NOT NULL,
                        status VARCHAR(30) NOT NULL,
                        total_amount NUMERIC(19,2) NOT NULL,

    -- Embedded OrderAddress
                        shipping_address_full_name VARCHAR(255),
                        shipping_address_phone VARCHAR(50),
                        shipping_address_address_line1 VARCHAR(255),
                        shipping_address_address_line2 VARCHAR(255),
                        shipping_address_city VARCHAR(100),
                        shipping_address_state VARCHAR(100),
                        shipping_address_country VARCHAR(100),
                        shipping_address_postal_code VARCHAR(50),

                        created_at TIMESTAMP,
                        updated_at TIMESTAMP,

                        CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_orders_user ON orders(user_id);
CREATE INDEX idx_orders_status ON orders(status);

-- =========================
-- ORDER ITEMS
-- =========================
CREATE TABLE order_items (
                             id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                             order_id UUID NOT NULL,
                             product_id UUID NOT NULL,
                             quantity INTEGER NOT NULL,
                             product_name_snapshot VARCHAR(180) NOT NULL,
                             sku_snapshot VARCHAR(80) NOT NULL,
                             unit_price NUMERIC(19,2) NOT NULL,
                             line_total NUMERIC(19,2) NOT NULL,
                             created_at TIMESTAMP,
                             updated_at TIMESTAMP,
                             CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders(id),
                             CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE INDEX idx_order_items_order ON order_items(order_id);
CREATE INDEX idx_order_items_product ON order_items(product_id);

-- =========================
-- PAYMENTS
-- =========================
CREATE TABLE payments (
                          id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                          order_id UUID NOT NULL,
                          provider VARCHAR(20) NOT NULL,
                          status VARCHAR(20) NOT NULL,
                          reference VARCHAR(80) NOT NULL,
                          amount NUMERIC(19,2) NOT NULL,
                          failure_reason VARCHAR(500),
                          created_at TIMESTAMP,
                          updated_at TIMESTAMP,
                          CONSTRAINT uk_payments_reference UNIQUE (reference),
                          CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES orders(id)
);

CREATE INDEX idx_payments_reference ON payments(reference);

-- =========================
-- PAYMENT ATTEMPTS
-- =========================
CREATE TABLE payment_attempts (
                                  id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                  payment_id UUID NOT NULL,
                                  reference VARCHAR(80) NOT NULL,
                                  status VARCHAR(20) NOT NULL,
                                  message VARCHAR(500),
                                  created_at TIMESTAMP,
                                  updated_at TIMESTAMP,
                                  CONSTRAINT fk_payment_attempts_payment FOREIGN KEY (payment_id) REFERENCES payments(id)
);

CREATE INDEX idx_payment_attempts_payment ON payment_attempts(payment_id);
CREATE INDEX idx_payment_attempts_reference ON payment_attempts(reference);
