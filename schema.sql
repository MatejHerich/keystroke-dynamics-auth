CREATE DATABASE IF NOT EXISTS behavioral_auth;
USE behavioral_auth;

-- Entity User.java
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;

-- Entity BiometricSample.java
CREATE TABLE IF NOT EXISTS biometric_samples (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    field_name VARCHAR(50),  -- 'username' or 'password'
    key_pressed VARCHAR(50), -- keyCode
    dwell_time DOUBLE,       -- ms
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_biometrics
    FOREIGN KEY (user_id)
    REFERENCES users(id)
    ON DELETE CASCADE
    ) ENGINE=InnoDB;

INSERT INTO users (username, password) VALUES ('testuser', 'heslo123');

CREATE TABLE IF NOT EXISTS accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    iban VARCHAR(34) NOT NULL UNIQUE,
    balance DECIMAL(15, 2) DEFAULT 0.00,
    currency VARCHAR(3) DEFAULT 'EUR',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_id BIGINT NOT NULL,
    recipient_iban VARCHAR(34) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    description VARCHAR(255),
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE
    ) ENGINE=InnoDB;

INSERT INTO accounts (user_id, iban, balance)

VALUES (1, 'SK7711000000001234567890', 12540.50)
    ON DUPLICATE KEY UPDATE balance = 12540.50;