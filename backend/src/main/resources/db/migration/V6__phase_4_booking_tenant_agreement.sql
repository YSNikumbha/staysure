CREATE TABLE bookings (
    id BIGINT NOT NULL AUTO_INCREMENT,
    booking_number VARCHAR(40) NOT NULL,
    user_id BIGINT NOT NULL,
    property_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    bed_id BIGINT NOT NULL,
    status VARCHAR(40) NOT NULL,
    move_in_date DATE NOT NULL,
    expected_move_out_date DATE,
    monthly_rent DECIMAL(12, 2) NOT NULL DEFAULT 0,
    security_deposit DECIMAL(12, 2) NOT NULL DEFAULT 0,
    requested_at DATETIME(6) NOT NULL,
    approved_at DATETIME(6),
    rejected_at DATETIME(6),
    cancelled_at DATETIME(6),
    confirmed_at DATETIME(6),
    checked_in_at DATETIME(6),
    rejection_reason TEXT,
    cancellation_reason TEXT,
    remarks TEXT,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_bookings_booking_number UNIQUE (booking_number),
    CONSTRAINT fk_bookings_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_bookings_property FOREIGN KEY (property_id) REFERENCES pg_properties(id),
    CONSTRAINT fk_bookings_room FOREIGN KEY (room_id) REFERENCES rooms(id),
    CONSTRAINT fk_bookings_bed FOREIGN KEY (bed_id) REFERENCES beds(id)
);

CREATE INDEX idx_bookings_user_id ON bookings(user_id);
CREATE INDEX idx_bookings_property_id ON bookings(property_id);
CREATE INDEX idx_bookings_room_id ON bookings(room_id);
CREATE INDEX idx_bookings_bed_id ON bookings(bed_id);
CREATE INDEX idx_bookings_status ON bookings(status);

CREATE TABLE booking_status_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    booking_id BIGINT NOT NULL,
    previous_status VARCHAR(40),
    new_status VARCHAR(40) NOT NULL,
    remarks TEXT,
    action_by BIGINT,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_booking_status_history_booking FOREIGN KEY (booking_id) REFERENCES bookings(id),
    CONSTRAINT fk_booking_status_history_action_by FOREIGN KEY (action_by) REFERENCES users(id)
);

CREATE INDEX idx_booking_status_history_booking_id ON booking_status_history(booking_id);
CREATE INDEX idx_booking_status_history_created_at ON booking_status_history(created_at);

CREATE TABLE tenant_documents (
    id BIGINT NOT NULL AUTO_INCREMENT,
    booking_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    document_type VARCHAR(50) NOT NULL,
    document_number VARCHAR(120),
    document_url VARCHAR(600) NOT NULL,
    original_file_name VARCHAR(255),
    content_type VARCHAR(100),
    size_bytes BIGINT,
    verification_status VARCHAR(30) NOT NULL,
    rejection_reason TEXT,
    verified_by BIGINT,
    verified_at DATETIME(6),
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_tenant_documents_booking FOREIGN KEY (booking_id) REFERENCES bookings(id),
    CONSTRAINT fk_tenant_documents_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_tenant_documents_verified_by FOREIGN KEY (verified_by) REFERENCES users(id)
);

CREATE INDEX idx_tenant_documents_booking_id ON tenant_documents(booking_id);
CREATE INDEX idx_tenant_documents_user_id ON tenant_documents(user_id);
CREATE INDEX idx_tenant_documents_verification_status ON tenant_documents(verification_status);
CREATE INDEX idx_tenant_documents_document_type ON tenant_documents(document_type);

CREATE TABLE security_deposits (
    id BIGINT NOT NULL AUTO_INCREMENT,
    booking_id BIGINT NOT NULL,
    required_amount DECIMAL(12, 2) NOT NULL DEFAULT 0,
    paid_amount DECIMAL(12, 2) NOT NULL DEFAULT 0,
    status VARCHAR(30) NOT NULL,
    last_payment_method VARCHAR(40),
    last_payment_reference VARCHAR(160),
    remarks TEXT,
    paid_at DATETIME(6),
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_security_deposits_booking_id UNIQUE (booking_id),
    CONSTRAINT fk_security_deposits_booking FOREIGN KEY (booking_id) REFERENCES bookings(id)
);

CREATE INDEX idx_security_deposits_status ON security_deposits(status);

CREATE TABLE rental_agreements (
    id BIGINT NOT NULL AUTO_INCREMENT,
    booking_id BIGINT NOT NULL,
    agreement_number VARCHAR(40) NOT NULL,
    status VARCHAR(30) NOT NULL,
    document_url VARCHAR(600),
    original_file_name VARCHAR(255),
    content_type VARCHAR(100),
    size_bytes BIGINT,
    terms TEXT,
    start_date DATE NOT NULL,
    end_date DATE,
    monthly_rent DECIMAL(12, 2) NOT NULL DEFAULT 0,
    security_deposit DECIMAL(12, 2) NOT NULL DEFAULT 0,
    notice_period_days INT NOT NULL DEFAULT 0,
    lock_in_months INT NOT NULL DEFAULT 0,
    issued_at DATETIME(6) NOT NULL,
    accepted_at DATETIME(6),
    created_by BIGINT,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_rental_agreements_booking_id UNIQUE (booking_id),
    CONSTRAINT uk_rental_agreements_agreement_number UNIQUE (agreement_number),
    CONSTRAINT fk_rental_agreements_booking FOREIGN KEY (booking_id) REFERENCES bookings(id),
    CONSTRAINT fk_rental_agreements_created_by FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE INDEX idx_rental_agreements_status ON rental_agreements(status);

CREATE TABLE tenant_profiles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    booking_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    property_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    bed_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    joining_date DATETIME(6),
    expected_checkout_date DATE,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_tenant_profiles_booking_id UNIQUE (booking_id),
    CONSTRAINT fk_tenant_profiles_booking FOREIGN KEY (booking_id) REFERENCES bookings(id),
    CONSTRAINT fk_tenant_profiles_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_tenant_profiles_property FOREIGN KEY (property_id) REFERENCES pg_properties(id),
    CONSTRAINT fk_tenant_profiles_room FOREIGN KEY (room_id) REFERENCES rooms(id),
    CONSTRAINT fk_tenant_profiles_bed FOREIGN KEY (bed_id) REFERENCES beds(id)
);

CREATE INDEX idx_tenant_profiles_user_id ON tenant_profiles(user_id);
CREATE INDEX idx_tenant_profiles_property_id ON tenant_profiles(property_id);
CREATE INDEX idx_tenant_profiles_status ON tenant_profiles(status);
