CREATE TABLE IF NOT EXISTS billing_services (
    billing_id INTEGER NOT NULL,
    service_rate_id INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (billing_id, service_rate_id),
    FOREIGN KEY (billing_id) REFERENCES billing(billing_id) ON DELETE CASCADE,
    FOREIGN KEY (service_rate_id) REFERENCES service_rate(rate_id) ON DELETE CASCADE
); 