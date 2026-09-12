CREATE TABLE processor_logs (
    id                VARCHAR(36)     NOT NULL,
    transaction_id    VARCHAR(36)     NOT NULL,
    merchant_id       VARCHAR(36)     NOT NULL,
    processor_type    VARCHAR(100)    NOT NULL,
    success           BOOLEAN         NOT NULL,
    processor_ref     VARCHAR(255),
    failure_reason    VARCHAR(500),
    latency_ms        BIGINT,
    created_at        TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_processor_logs PRIMARY KEY (id)
);
