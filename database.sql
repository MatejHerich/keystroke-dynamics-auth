DROP DATABASE IF EXISTS keystroke_auth;
CREATE DATABASE keystroke_auth
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
USE keystroke_auth;

-- ---------- Banking part ----------
CREATE TABLE users (
    id             INT          NOT NULL AUTO_INCREMENT,
    username       VARCHAR(50)  NOT NULL,
    password_hash  VARCHAR(255) NOT NULL,
    profile_status VARCHAR(20)  NOT NULL DEFAULT 'TRAINING',
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uq_users_username UNIQUE (username),
    CONSTRAINT chk_users_profile_status
        CHECK (profile_status IN ('TRAINING', 'ACTIVE'))
) ENGINE = InnoDB;

CREATE TABLE accounts (
    id       INT           NOT NULL AUTO_INCREMENT,
    users_id INT           NOT NULL,
    iban     VARCHAR(34)   NOT NULL,
    balance  DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(5)    NOT NULL DEFAULT 'EUR',
    PRIMARY KEY (id),
    CONSTRAINT uq_accounts_users_id UNIQUE (users_id),
    CONSTRAINT uq_accounts_iban UNIQUE (iban),
    CONSTRAINT fk_accounts_users
        FOREIGN KEY (users_id) REFERENCES users (id)
) ENGINE = InnoDB;

-- ---------- Biometric part ----------
CREATE TABLE biometric_attempts (
    id            INT         NOT NULL AUTO_INCREMENT,
    users_id      INT         NOT NULL,
    attempt_type  VARCHAR(20) NOT NULL,
    context       VARCHAR(20) NOT NULL,
    final_score   DOUBLE      NULL,
    threshold     DOUBLE      NULL,
    authenticated TINYINT(1)  NULL,
    created_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_biometric_attempts_users
        FOREIGN KEY (users_id) REFERENCES users (id),
    CONSTRAINT chk_biometric_attempts_type
        CHECK (attempt_type IN ('TRAINING', 'GENUINE', 'IMPOSTOR')),
    CONSTRAINT chk_biometric_attempts_context
        CHECK (context IN ('REGISTRATION', 'PAYMENT'))
) ENGINE = InnoDB;

CREATE TABLE key_presses (
    id         INT    NOT NULL AUTO_INCREMENT,
    attempt_id INT    NOT NULL,
    key_index  INT    NOT NULL,
    down_ms    DOUBLE NOT NULL,
    up_ms      DOUBLE NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_key_presses_attempt_key UNIQUE (attempt_id, key_index),
    CONSTRAINT fk_key_presses_attempts
        FOREIGN KEY (attempt_id) REFERENCES biometric_attempts (id)
        ON DELETE CASCADE,
    CONSTRAINT chk_key_presses_times CHECK (up_ms >= down_ms)
) ENGINE = InnoDB;

CREATE TABLE evaluator_results (
    id             INT         NOT NULL AUTO_INCREMENT,
    attempt_id     INT         NOT NULL,
    evaluator_name VARCHAR(50) NOT NULL,
    score          DOUBLE      NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_evaluator_results_attempt_evaluator
        UNIQUE (attempt_id, evaluator_name),
    CONSTRAINT fk_evaluator_results_attempts
        FOREIGN KEY (attempt_id) REFERENCES biometric_attempts (id)
        ON DELETE CASCADE,
    CONSTRAINT chk_evaluator_results_score CHECK (score BETWEEN 0 AND 1)
) ENGINE = InnoDB;

CREATE TABLE biometric_profiles (
    users_id       INT       NOT NULL,
    training_count INT       NOT NULL,
    feature_stats  JSON      NOT NULL,
    updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (users_id),
    CONSTRAINT fk_biometric_profiles_users
        FOREIGN KEY (users_id) REFERENCES users (id)
) ENGINE = InnoDB;

-- ---------- Payments ----------
CREATE TABLE transactions (
    id             INT           NOT NULL AUTO_INCREMENT,
    account_id     INT           NOT NULL,
    attempt_id     INT           NOT NULL,
    recipient_iban VARCHAR(34)   NOT NULL,
    amount         DECIMAL(15,2) NOT NULL,
    description    VARCHAR(255)  NULL,
    status         VARCHAR(20)   NOT NULL,
    created_at     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uq_transactions_attempt UNIQUE (attempt_id),
    CONSTRAINT fk_transactions_accounts
        FOREIGN KEY (account_id) REFERENCES accounts (id),
    CONSTRAINT fk_transactions_attempts
        FOREIGN KEY (attempt_id) REFERENCES biometric_attempts (id),
    CONSTRAINT chk_transactions_amount CHECK (amount > 0),
    CONSTRAINT chk_transactions_status
        CHECK (status IN ('COMPLETED', 'REJECTED'))
) ENGINE = InnoDB;
