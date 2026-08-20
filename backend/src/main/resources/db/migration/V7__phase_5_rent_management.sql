CREATE TABLE rent_invoices (
    id BIGINT NOT NULL AUTO_INCREMENT,
    invoice_number VARCHAR(50) NOT NULL,
    tenant_profile_id BIGINT NOT NULL,
    property_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    bed_id BIGINT NOT NULL,
    billing_month INT NOT NULL,
    billing_year INT NOT NULL,
    base_rent DECIMAL(12, 2) NOT NULL DEFAULT 0,
    maintenance_charge DECIMAL(12, 2) NOT NULL DEFAULT 0,
    electricity_charge DECIMAL(12, 2) NOT NULL DEFAULT 0,
    other_charge DECIMAL(12, 2) NOT NULL DEFAULT 0,
    late_fee DECIMAL(12, 2) NOT NULL DEFAULT 0,
    total_amount DECIMAL(12, 2) NOT NULL DEFAULT 0,
    paid_amount DECIMAL(12, 2) NOT NULL DEFAULT 0,
    balance_amount DECIMAL(12, 2) NOT NULL DEFAULT 0,
    due_date DATE NOT NULL,
    status VARCHAR(30) NOT NULL,
    notes TEXT,
    generated_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_rent_invoices_invoice_number UNIQUE (invoice_number),
    CONSTRAINT uk_rent_invoice_tenant_month UNIQUE (tenant_profile_id, billing_month, billing_year),
    CONSTRAINT fk_rent_invoices_tenant_profile FOREIGN KEY (tenant_profile_id) REFERENCES tenant_profiles(id),
    CONSTRAINT fk_rent_invoices_property FOREIGN KEY (property_id) REFERENCES pg_properties(id),
    CONSTRAINT fk_rent_invoices_room FOREIGN KEY (room_id) REFERENCES rooms(id),
    CONSTRAINT fk_rent_invoices_bed FOREIGN KEY (bed_id) REFERENCES beds(id)
);

CREATE INDEX idx_rent_invoices_tenant_profile_id ON rent_invoices(tenant_profile_id);
CREATE INDEX idx_rent_invoices_property_id ON rent_invoices(property_id);
CREATE INDEX idx_rent_invoices_status ON rent_invoices(status);
CREATE INDEX idx_rent_invoices_due_date ON rent_invoices(due_date);
CREATE INDEX idx_rent_invoices_billing_period ON rent_invoices(billing_year, billing_month);

CREATE TABLE rent_payments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    payment_number VARCHAR(50) NOT NULL,
    rent_invoice_id BIGINT NOT NULL,
    tenant_profile_id BIGINT NOT NULL,
    property_id BIGINT NOT NULL,
    amount DECIMAL(12, 2) NOT NULL,
    payment_method VARCHAR(40) NOT NULL,
    payment_reference VARCHAR(160),
    payment_date DATE NOT NULL,
    remarks TEXT,
    recorded_by BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_rent_payments_payment_number UNIQUE (payment_number),
    CONSTRAINT fk_rent_payments_invoice FOREIGN KEY (rent_invoice_id) REFERENCES rent_invoices(id),
    CONSTRAINT fk_rent_payments_tenant_profile FOREIGN KEY (tenant_profile_id) REFERENCES tenant_profiles(id),
    CONSTRAINT fk_rent_payments_property FOREIGN KEY (property_id) REFERENCES pg_properties(id),
    CONSTRAINT fk_rent_payments_recorded_by FOREIGN KEY (recorded_by) REFERENCES users(id)
);

CREATE INDEX idx_rent_payments_rent_invoice_id ON rent_payments(rent_invoice_id);
CREATE INDEX idx_rent_payments_tenant_profile_id ON rent_payments(tenant_profile_id);
CREATE INDEX idx_rent_payments_payment_date ON rent_payments(payment_date);
