CREATE TABLE pg_properties (
    id BIGINT NOT NULL AUTO_INCREMENT,
    owner_id BIGINT NOT NULL,
    name VARCHAR(180) NOT NULL,
    slug VARCHAR(220) NOT NULL,
    description TEXT,
    gender_type VARCHAR(30) NOT NULL,
    property_type VARCHAR(40) NOT NULL,
    address_line1 VARCHAR(255) NOT NULL,
    address_line2 VARCHAR(255),
    area VARCHAR(120) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    pincode VARCHAR(12) NOT NULL,
    latitude DECIMAL(10, 7),
    longitude DECIMAL(10, 7),
    starting_rent DECIMAL(12, 2) NOT NULL DEFAULT 0,
    security_deposit DECIMAL(12, 2) NOT NULL DEFAULT 0,
    notice_period_days INT NOT NULL DEFAULT 0,
    lock_in_months INT NOT NULL DEFAULT 0,
    entry_time TIME,
    food_available BOOLEAN NOT NULL DEFAULT FALSE,
    status VARCHAR(30) NOT NULL,
    verification_status VARCHAR(30) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_pg_properties_slug UNIQUE (slug),
    CONSTRAINT fk_pg_properties_owner FOREIGN KEY (owner_id) REFERENCES owner_profiles(id)
);

CREATE INDEX idx_pg_properties_owner_id ON pg_properties(owner_id);
CREATE INDEX idx_pg_properties_status ON pg_properties(status);
CREATE INDEX idx_pg_properties_verification_status ON pg_properties(verification_status);

CREATE TABLE property_rules (
    id BIGINT NOT NULL AUTO_INCREMENT,
    property_id BIGINT NOT NULL,
    visitor_allowed BOOLEAN NOT NULL DEFAULT FALSE,
    smoking_allowed BOOLEAN NOT NULL DEFAULT FALSE,
    alcohol_allowed BOOLEAN NOT NULL DEFAULT FALSE,
    cooking_allowed BOOLEAN NOT NULL DEFAULT FALSE,
    gate_closing_time TIME,
    late_entry_allowed BOOLEAN NOT NULL DEFAULT FALSE,
    notice_period_days INT NOT NULL DEFAULT 0,
    additional_rules TEXT,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_property_rules_property_id UNIQUE (property_id),
    CONSTRAINT fk_property_rules_property FOREIGN KEY (property_id) REFERENCES pg_properties(id)
);

CREATE INDEX idx_property_rules_property_id ON property_rules(property_id);
