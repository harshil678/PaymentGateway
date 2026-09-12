CREATE TABLE settlements (
    id                  VARCHAR(36)     NOT NULL,
    merchant_id         VARCHAR(36)     NOT NULL,
    settlement_date     DATE            NOT NULL,
    total_amount        DECIMAL(19,4)   NOT NULL,
    transaction_count   INT             NOT NULL DEFAULT 0,
    currency            VARCHAR(3)         NOT NULL,
    s3_report_key       VARCHAR(500),
    status              ENUM('PENDING','EXPORTED') NOT NULL DEFAULT 'PENDING',
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT pk_settlements PRIMARY KEY (id),
    CONSTRAINT uq_merchant_date UNIQUE (merchant_id, settlement_date, currency)
);

CREATE TABLE settlement_transactions (
    id                  VARCHAR(36)     NOT NULL,
    settlement_id       VARCHAR(36)     NOT NULL,
    transaction_id      VARCHAR(36)     NOT NULL,
    amount              DECIMAL(19,4)   NOT NULL,
    currency            VARCHAR(3)         NOT NULL,
    processor_ref       VARCHAR(255),
    created_at          TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_settlement_transactions PRIMARY KEY (id),
    CONSTRAINT fk_settlement_tx FOREIGN KEY (settlement_id) REFERENCES settlements(id)
);
