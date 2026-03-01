-- Payment Service Database Schema
-- MySQL 8.0+

-- Create Database
CREATE DATABASE IF NOT EXISTS payment_db;
USE payment_db;

-- Payments Table
CREATE TABLE IF NOT EXISTS payments (
  id BINARY(16) PRIMARY KEY,
  order_id VARCHAR(255) NOT NULL UNIQUE,
  user_id VARCHAR(255) NOT NULL,
  amount DECIMAL(19, 2) NOT NULL,
  currency VARCHAR(3) DEFAULT 'USD',
  method VARCHAR(50) NOT NULL,
  status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
  provider VARCHAR(50) NOT NULL,
  transaction_id VARCHAR(255) NOT NULL UNIQUE,
  metadata LONGTEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  INDEX idx_order_id (order_id),
  INDEX idx_user_id (user_id),
  INDEX idx_transaction_id (transaction_id),
  INDEX idx_status (status),
  INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Payment Transactions Table
CREATE TABLE IF NOT EXISTS payment_transactions (
  id BINARY(16) PRIMARY KEY,
  payment_id BINARY(16) NOT NULL,
  transaction_code VARCHAR(255) NOT NULL,
  status VARCHAR(50) NOT NULL,
  amount DECIMAL(19, 2) NOT NULL,
  description LONGTEXT,
  response LONGTEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  INDEX idx_payment_id (payment_id),
  INDEX idx_transaction_code (transaction_code),
  INDEX idx_status (status),
  INDEX idx_created_at (created_at),

  CONSTRAINT fk_payment_transaction FOREIGN KEY (payment_id) REFERENCES payments (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Sample Data (Optional)
-- INSERT INTO payments VALUES
-- (UNHEX(REPLACE(UUID(), '-', '')), 'ORD-001', 'USR-001', 99.99, 'USD', 'CREDIT_CARD', 'PENDING', 'STRIPE', 'pi_123456', '{}', NOW(), NOW());

