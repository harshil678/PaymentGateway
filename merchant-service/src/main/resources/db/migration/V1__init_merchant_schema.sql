CREATE TABLE merchants (
                           id            VARCHAR(36)      NOT NULL,
                           name          VARCHAR(255)  NOT NULL,
                           email         VARCHAR(255)  NOT NULL,
                           webhook_url   VARCHAR(500),
                           status        ENUM('ACTIVE','SUSPENDED','BLOCKED') NOT NULL DEFAULT 'ACTIVE',
                           created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                           CONSTRAINT pk_merchants PRIMARY KEY (id),
                           CONSTRAINT uq_merchants_email UNIQUE (email)
);

CREATE TABLE merchant_api_keys (
                                   id            VARCHAR(36)   NOT NULL,
                                   merchant_id   VARCHAR(36)   NOT NULL,
                                   key_prefix    VARCHAR(20)   NOT NULL,
                                   key_hash      VARCHAR(255)  NOT NULL,
                                   expires_at    TIMESTAMP     NULL,
                                   revoked       BOOLEAN       NOT NULL DEFAULT FALSE,
                                   created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   CONSTRAINT pk_merchant_api_keys PRIMARY KEY (id),
                                   CONSTRAINT fk_api_keys_merchant FOREIGN KEY (merchant_id) REFERENCES merchants(id)
);

CREATE TABLE merchant_events (
                                 id            VARCHAR(36)      NOT NULL,
                                 merchant_id   VARCHAR(36)      NOT NULL,
                                 event_type    VARCHAR(100)  NOT NULL,
                                 metadata      JSON,
                                 created_at    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                 CONSTRAINT pk_merchant_events PRIMARY KEY (id),
                                 CONSTRAINT fk_events_merchant FOREIGN KEY (merchant_id) REFERENCES merchants(id)
);