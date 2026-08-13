CREATE TABLE amenities (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    code VARCHAR(80) NOT NULL,
    icon VARCHAR(80),
    description VARCHAR(500),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_amenities_code UNIQUE (code)
);

CREATE TABLE property_amenities (
    property_id BIGINT NOT NULL,
    amenity_id BIGINT NOT NULL,
    PRIMARY KEY (property_id, amenity_id),
    CONSTRAINT uk_property_amenities_property_amenity UNIQUE (property_id, amenity_id),
    CONSTRAINT fk_property_amenities_property FOREIGN KEY (property_id) REFERENCES pg_properties(id),
    CONSTRAINT fk_property_amenities_amenity FOREIGN KEY (amenity_id) REFERENCES amenities(id)
);

CREATE TABLE pg_images (
    id BIGINT NOT NULL AUTO_INCREMENT,
    property_id BIGINT NOT NULL,
    image_url VARCHAR(600) NOT NULL,
    category VARCHAR(40) NOT NULL,
    is_cover_image BOOLEAN NOT NULL DEFAULT FALSE,
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_pg_images_property FOREIGN KEY (property_id) REFERENCES pg_properties(id)
);

CREATE INDEX idx_pg_images_property_id ON pg_images(property_id);

INSERT INTO amenities (name, code, icon, description, active)
VALUES
    ('Wi-Fi', 'WIFI', 'wifi', 'Wireless internet access', TRUE),
    ('AC', 'AC', 'snowflake', 'Air conditioning', TRUE),
    ('CCTV', 'CCTV', 'camera', 'Common area security camera coverage', TRUE),
    ('Laundry', 'LAUNDRY', 'shirt', 'Laundry service', TRUE),
    ('Washing Machine', 'WASHING_MACHINE', 'washing-machine', 'Shared washing machine access', TRUE),
    ('Parking', 'PARKING', 'parking-circle', 'Vehicle parking', TRUE),
    ('Hot Water', 'HOT_WATER', 'waves', 'Hot water availability', TRUE),
    ('Power Backup', 'POWER_BACKUP', 'battery-charging', 'Power backup', TRUE),
    ('Housekeeping', 'HOUSEKEEPING', 'sparkles', 'Housekeeping service', TRUE),
    ('Lift', 'LIFT', 'arrow-up-down', 'Lift access', TRUE),
    ('Gym', 'GYM', 'dumbbell', 'Fitness area', TRUE),
    ('RO Water', 'RO_WATER', 'droplets', 'RO drinking water', TRUE),
    ('Refrigerator', 'REFRIGERATOR', 'refrigerator', 'Shared refrigerator', TRUE),
    ('Study Table', 'STUDY_TABLE', 'table', 'Study table availability', TRUE),
    ('Cupboard', 'CUPBOARD', 'archive', 'Cupboard or wardrobe', TRUE),
    ('Common Kitchen', 'COMMON_KITCHEN', 'cooking-pot', 'Shared kitchen access', TRUE);
