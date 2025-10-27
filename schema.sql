-- ===========================================================
-- DIGITAL WALLET DATABASE FULL SETUP
-- Includes: Tables, Sample Data, Indexes
-- ===========================================================

-- 1️⃣ Drop existing database and create a new one
DROP DATABASE IF EXISTS digital_wallet;
CREATE DATABASE digital_wallet;
USE digital_wallet;

-- ===========================================================
-- 2️⃣ Create USERS Table
-- ===========================================================
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

-- ===========================================================
-- 3️⃣ Create WALLETS Table
-- ===========================================================
CREATE TABLE wallets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    wallet_name VARCHAR(100) NOT NULL,
    balance DECIMAL(15,2) DEFAULT 0.00 NOT NULL,
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_wallet_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- ===========================================================
-- 4️⃣ Create TRANSACTIONS Table
-- ===========================================================
CREATE TABLE transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    wallet_id BIGINT NOT NULL,
    amount DECIMAL(15,2) NOT NULL CHECK (amount > 0),
    type ENUM('CREDIT', 'DEBIT', 'TRANSFER') NOT NULL,
    description VARCHAR(255),
    timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transaction_wallet
        FOREIGN KEY (wallet_id)
        REFERENCES wallets(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);

-- ===========================================================
-- 5️⃣ Insert Sample Data
-- ===========================================================
INSERT INTO users (username, password) VALUES
('shubham', 'password123'),
('rahul', 'password456');

INSERT INTO wallets (wallet_name, balance, user_id) VALUES
('Primary Wallet', 5000.00, 1),
('Savings Wallet', 2000.00, 2);

INSERT INTO transactions (wallet_id, amount, type, description) VALUES
(1, 1000.00, 'CREDIT', 'Initial deposit'),
(1, 200.00, 'DEBIT', 'Online purchase'),
(2, 500.00, 'CREDIT', 'Salary credited');

-- ===========================================================
-- 6️⃣ Create Indexes for Frequently Queried Columns
-- ===========================================================

-- USERS Table
CREATE INDEX idx_users_username ON users(username);

-- WALLETS Table
CREATE INDEX idx_wallets_wallet_name ON wallets(wallet_name);
CREATE INDEX idx_wallets_user_id ON wallets(user_id);

-- TRANSACTIONS Table
CREATE INDEX idx_transactions_wallet_id ON transactions(wallet_id);
CREATE INDEX idx_transactions_type ON transactions(type);
CREATE INDEX idx_transactions_timestamp ON transactions(timestamp);

-- Optional: Composite index for faster queries by wallet + type
CREATE INDEX idx_transactions_wallet_type
ON transactions(wallet_id, type);

-- ===========================================================
-- ✅ Database Setup Complete
-- ===========================================================
