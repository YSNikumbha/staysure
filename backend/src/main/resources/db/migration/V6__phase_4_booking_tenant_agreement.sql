-- Bookings
CREATE TABLE bookings (
    id BIGINT NOT NULL AUTO_INCREMENT,
    booking_number VARCHAR(30) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    property_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    bed_id BIGINT NOT NULL,
    move_in_date DATE NOT NULL,
    expected_move_out_date DATE NOT NULL,
    monthly_rent DECIMAL(12,2) NOT NULL,
    security_deposit_amount DECIMAL(12,2) NOT NULL,
    status VARCHAR(30) NOT NULL,
    user_remarks TEXT,
    owner_remarks TEXT,
    approved_at DATETIME(6),
    approved_by BIGINT,
    rejected_at DATETIME(6),
    rejected_by BIGINT,
    rejection_reason TEXT,
    confirmed_at DATETIME(6),
    checked_in_at DATETIME(6),
    cancelled_at DATETIME(6),
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_bookings_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_bookings_property FOREIGN KEY (property_id) REFERENCES pg_properties(id),
    CONSTRAINT fk_bookings_room FOREIGN KEY (room_id) REFERENCES rooms(id),
    CONSTRAINT fk_bookings_bed FOREIGN KEY (bed_id) REFERENCES beds(id),
    CONSTRAINT fk_bookings_approved_by FOREIGN KEY (approved_by) REFERENCES users(id),
    CONSTRAINT fk_bookings_rejected_by FOREIGN KEY (rejected_by) REFERENCES users(id)
);

CREATE INDEX idx_bookings_user_id ON bookings(user_id);
CREATE INDEX idx_bookings_property_id ON bookings(property_id);
CREATE INDEX idx_bookings_bed_id ON bookings(bed_id);
CREATE INDEX idx_bookings_status ON bookings(status);

-- Booking Status History
CREATE TABLE booking_status_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    booking_id BIGINT NOT NULL,
    previous_status VARCHAR(30),
    new_status VARCHAR(30) NOT NULL,
    remarks TEXT,
    changed_by BIGINT,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_booking_status_history_booking FOREIGN KEY (booking_id) REFERENCES bookings(id),
    CONSTRAINT fk_booking_status_history_changed_by FOREIGN KEY (changed_by) REFERENCES users(id)
);

CREATE INDEX idx_booking_status_history_booking_id ON booking_status_history(booking_id);

-- Tenant Profiles
CREATE TABLE tenant_profiles (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    booking_id BIGINT NOT NULL UNIQUE,
    property_id BIGINT NOT NULL,
    room_id BIGINT NOT NULL,
    bed_id BIGINT NOT NULL,
    status VARCHAR(30) NOT NULL,
    joining_date DATETIME(6),
    expected_checkout_date DATETIME(6),
    emergency_contact_name VARCHAR(120),
    emergency_contact_phone VARCHAR(20),
    college_or_company VARCHAR(180),
    occupation_type VARCHAR(60),
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_tenant_profiles_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_tenant_profiles_booking FOREIGN KEY (booking_id) REFERENCES bookings(id),
    CONSTRAINT fk_tenant_profiles_property FOREIGN KEY (property_id) REFERENCES pg_properties(id),
    CONSTRAINT fk_tenant_profiles_room FOREIGN KEY (room_id) REFERENCES rooms(id),
    CONSTRAINT fk_tenant_profiles_bed FOREIGN KEY (bed_id) REFERENCES beds(id)
);

CREATE INDEX idx_tenant_profiles_user_id ON tenant_profiles(user_id);
CREATE INDEX idx_tenant_profiles_property_id ON tenant_profiles(property_id);
CREATE INDEX idx_tenant_profiles_status ON tenant_profiles(status);

-- Tenant Documents
CREATE TABLE tenant_documents (
    id BIGINT NOT NULL AUTO_INCREMENT,
    booking_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    document_type VARCHAR(30) NOT NULL,
    document_number VARCHAR(60),
    document_url VARCHAR(500) NOT NULL,
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

-- Security Deposits
CREATE TABLE security_deposits (
    id BIGINT NOT NULL AUTO_INCREMENT,
    booking_id BIGINT NOT NULL,
    tenant_id BIGINT,
    property_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    required_amount DECIMAL(12,2) NOT NULL,
    paid_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    payment_method VARCHAR(30),
    payment_reference VARCHAR(120),
    paid_at DATETIME(6),
    status VARCHAR(30) NOT NULL,
    remarks TEXT,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_security_deposits_booking FOREIGN KEY (booking_id) REFERENCES bookings(id),
    CONSTRAINT fk_security_deposits_tenant FOREIGN KEY (tenant_id) REFERENCES tenant_profiles(id),
    CONSTRAINT fk_security_deposits_property FOREIGN KEY (property_id) REFERENCES pg_properties(id),
    CONSTRAINT fk_security_deposits_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_security_deposits_booking_id ON security_deposits(booking_id);

-- Rental Agreements
CREATE TABLE rental_agreements (
    id BIGINT NOT NULL AUTO_INCREMENT,
    booking_id BIGINT NOT NULL UNIQUE,
    property_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    agreement_number VARCHAR(30) NOT NULL UNIQUE,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    monthly_rent DECIMAL(12,2) NOT NULL,
    security_deposit DECIMAL(12,2) NOT NULL,
    notice_period_days INT NOT NULL,
    lock_in_months INT NOT NULL,
    terms_and_conditions TEXT,
    agreement_file_url VARCHAR(500),
    status VARCHAR(30) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_rental_agreements_booking FOREIGN KEY (booking_id) REFERENCES bookings(id),
    CONSTRAINT fk_rental_agreements_property FOREIGN KEY (property_id) REFERENCES pg_properties(id),
    CONSTRAINT fk_rental_agreements_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_rental_agreements_booking_id ON rental_agreements(booking_id);