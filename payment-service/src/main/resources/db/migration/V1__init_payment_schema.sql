CREATE TABLE transactions (
    id                VARCHAR(36)     NOT NULL,
    merchant_id       VARCHAR(36)     NOT NULL,
    idempotency_key   VARCHAR(255)    NOT NULL,
    amount            DECIMAL(19,4)   NOT NULL,
    currency          VARCHAR(3)      NOT NULL,
    status            ENUM('INITIATED','PROCESSING','SUCCESS','FAILED','TIMED_OUT','EXPIRED')
                                      NOT NULL DEFAULT 'INITIATED',
    processor_ref     VARCHAR(255),
    failure_reason    VARCHAR(500),
    metadata          JSON,
    expires_at        TIMESTAMP       NOT NULL,
    created_at        TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_transactions PRIMARY KEY (id),
    CONSTRAINT uq_idempotency UNIQUE (merchant_id, idempotency_key)
);

CREATE TABLE transaction_events (
    id              VARCHAR(36)     NOT NULL,
    transaction_id  VARCHAR(36)     NOT NULL,
    from_status     VARCHAR(50)     NOT NULL,
    to_status       VARCHAR(50)     NOT NULL,
    triggered_by    VARCHAR(100)    NOT NULL,
    metadata        JSON,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_transaction_events PRIMARY KEY (id),
    CONSTRAINT fk_tx_events_transaction FOREIGN KEY (transaction_id) REFERENCES transactions(id)
);
