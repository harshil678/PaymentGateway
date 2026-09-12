
ALTER TABLE processor_logs
    ADD CONSTRAINT uq_processor_transaction UNIQUE (transaction_id);
