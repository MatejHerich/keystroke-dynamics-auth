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