CREATE TABLE users (
    id            INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE login_attempts (
    id           INT AUTO_INCREMENT PRIMARY KEY,
    user_id      INT       NOT NULL,
    attempt_type VARCHAR(25) NOT NULL,
    attempted_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    success      BOOLEAN   NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE key_presses (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    attempt_id INT    NOT NULL,
    position   INT    NOT NULL,
    down_ms    DOUBLE NOT NULL,
    up_ms      DOUBLE NOT NULL,
    FOREIGN KEY (attempt_id) REFERENCES login_attempts(id) ON DELETE CASCADE
);
