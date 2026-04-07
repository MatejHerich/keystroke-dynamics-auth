CREATE DATABASE IF NOT EXISTS behavioral_auth;
USE behavioral_auth;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS biometric_samples (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    field_name VARCHAR(50),
    key_pressed VARCHAR(50),
    dwell_time DOUBLE,
    flight_time DOUBLE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_biometrics
    FOREIGN KEY (user_id)
    REFERENCES users(id)
    ON DELETE CASCADE
    ) ENGINE=InnoDB;

INSERT INTO users (username, password)
VALUES ('testuser', 'pbkdf2$600000$xzRdKDCnjj6deG6pJjD7ZQ==$aRJIjv2wYR4R3Hu8hGKSSkUVG9ZCmxhrURObMYgvqSM=')
    ON DUPLICATE KEY UPDATE password = VALUES(password);

CREATE TABLE IF NOT EXISTS accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    iban VARCHAR(34) NOT NULL UNIQUE,
    balance DECIMAL(15, 2) DEFAULT 0.00,
    currency VARCHAR(3) DEFAULT 'EUR',
    UNIQUE KEY uq_accounts_user_id (user_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_id BIGINT NOT NULL,
    recipient_iban VARCHAR(34) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    description VARCHAR(255),
    transaction_type VARCHAR(20) DEFAULT 'OUTGOING',
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE CASCADE
    ) ENGINE=InnoDB;

INSERT INTO accounts (user_id, iban, balance)
VALUES (1, 'SK7711000000001234567890', 12540.50)
    ON DUPLICATE KEY UPDATE balance = 12540.50;

CREATE TABLE IF NOT EXISTS behavior_attempts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    attempt_type VARCHAR(20) NOT NULL,
    authenticated BOOLEAN NOT NULL,
    confidence_score DOUBLE NOT NULL,
    required_threshold DOUBLE NOT NULL,
    sample_count INT NOT NULL,
    evaluator_details TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS behavioral_profiles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    average_dwell_time DOUBLE NOT NULL,
    average_flight_time DOUBLE NOT NULL,
    dwell_deviation DOUBLE NOT NULL,
    flight_deviation DOUBLE NOT NULL,
    long_pause_ratio DOUBLE NOT NULL,
    reference_attempts INT NOT NULL,
    reference_samples INT NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    ) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS verification_samples (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50),
    key_pressed VARCHAR(50),
    dwell_time DOUBLE,
    flight_time DOUBLE,
    sample_index INT,
    attempt_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_verification_attempt
        FOREIGN KEY (attempt_id) REFERENCES behavior_attempts(id) ON DELETE CASCADE
    ) ENGINE=InnoDB;

INSERT INTO users (username, password)
VALUES ('user1', 'pbkdf2$600000$m1T116rPG5EZ5jibiR1PQg==$HJN5usnCiRJ9LCDpdZgJ930Iwsgo3qZzYn/NfxrwdlU=')
    ON DUPLICATE KEY UPDATE password = VALUES(password);

INSERT INTO accounts (user_id, iban, balance)
VALUES (2, 'SK0000000000000000000000', 500.00)
    ON DUPLICATE KEY UPDATE balance = 500.00;
