ALTER TABLE pg_properties
    ADD COLUMN submitted_for_verification_at DATETIME(6),
    ADD COLUMN verified_at DATETIME(6),
    ADD COLUMN verified_by BIGINT,
    ADD COLUMN verification_remarks TEXT,
    ADD COLUMN rejection_reason TEXT,
    ADD CONSTRAINT fk_pg_properties_verified_by FOREIGN KEY (verified_by) REFERENCES users(id);

CREATE INDEX idx_pg_properties_city ON pg_properties(city);
CREATE INDEX idx_pg_properties_area ON pg_properties(area);
CREATE INDEX idx_pg_properties_starting_rent ON pg_properties(starting_rent);
CREATE INDEX idx_pg_properties_gender_type ON pg_properties(gender_type);
CREATE INDEX idx_rooms_sharing_type ON rooms(sharing_type);

CREATE TABLE property_verification_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    property_id BIGINT NOT NULL,
    previous_status VARCHAR(30),
    new_status VARCHAR(30) NOT NULL,
    remarks TEXT,
    action_by BIGINT,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_property_verification_history_property FOREIGN KEY (property_id) REFERENCES pg_properties(id),
    CONSTRAINT fk_property_verification_history_action_by FOREIGN KEY (action_by) REFERENCES users(id)
);

CREATE INDEX idx_property_verification_history_property_id ON property_verification_history(property_id);
CREATE INDEX idx_property_verification_history_created_at ON property_verification_history(created_at);

CREATE TABLE wishlists (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    property_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_wishlists_user_property UNIQUE (user_id, property_id),
    CONSTRAINT fk_wishlists_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT fk_wishlists_property FOREIGN KEY (property_id) REFERENCES pg_properties(id)
);

CREATE INDEX idx_wishlists_user_id ON wishlists(user_id);
CREATE INDEX idx_wishlists_property_id ON wishlists(property_id);
