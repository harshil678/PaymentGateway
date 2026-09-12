CREATE TABLE webhook_deliveries (
    id                  VARCHAR(36)     NOT NULL,
    transaction_id      VARCHAR(36)     NOT NULL,
    merchant_id         VARCHAR(36)     NOT NULL,
    webhook_url         VARCHAR(500)    NOT NULL,
    payload             JSON            NOT NULL,
    status              ENUM('PENDING','SUCCESS','FAILED') NOT NULL DEFAULT 'PENDING',
    attempt_count       INT             NOT NULL DEFAULT 0,
    last_attempt_at     TIMESTAMP,
    next_retry_at       TIMESTAMP,
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_webhook_deliveries PRIMARY KEY (id)
);
