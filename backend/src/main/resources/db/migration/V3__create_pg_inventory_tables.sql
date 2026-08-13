CREATE TABLE floors (
    id BIGINT NOT NULL AUTO_INCREMENT,
    property_id BIGINT NOT NULL,
    name VARCHAR(120) NOT NULL,
    floor_number INT NOT NULL,
    description TEXT,
    status VARCHAR(30) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_floors_property_floor_number UNIQUE (property_id, floor_number),
    CONSTRAINT fk_floors_property FOREIGN KEY (property_id) REFERENCES pg_properties(id)
);

CREATE INDEX idx_floors_property_id ON floors(property_id);

CREATE TABLE rooms (
    id BIGINT NOT NULL AUTO_INCREMENT,
    floor_id BIGINT NOT NULL,
    room_number VARCHAR(50) NOT NULL,
    room_name VARCHAR(120),
    sharing_type VARCHAR(40) NOT NULL,
    capacity INT NOT NULL,
    monthly_rent DECIMAL(12, 2) NOT NULL DEFAULT 0,
    security_deposit DECIMAL(12, 2) NOT NULL DEFAULT 0,
    ac_available BOOLEAN NOT NULL DEFAULT FALSE,
    attached_bathroom BOOLEAN NOT NULL DEFAULT FALSE,
    furnishing_type VARCHAR(40) NOT NULL,
    status VARCHAR(30) NOT NULL,
    description TEXT,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_rooms_floor_room_number UNIQUE (floor_id, room_number),
    CONSTRAINT fk_rooms_floor FOREIGN KEY (floor_id) REFERENCES floors(id)
);

CREATE INDEX idx_rooms_floor_id ON rooms(floor_id);
CREATE INDEX idx_rooms_status ON rooms(status);

CREATE TABLE beds (
    id BIGINT NOT NULL AUTO_INCREMENT,
    room_id BIGINT NOT NULL,
    bed_number VARCHAR(50) NOT NULL,
    bed_label VARCHAR(120),
    status VARCHAR(30) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_beds_room_bed_number UNIQUE (room_id, bed_number),
    CONSTRAINT fk_beds_room FOREIGN KEY (room_id) REFERENCES rooms(id)
);

CREATE INDEX idx_beds_room_id ON beds(room_id);
CREATE INDEX idx_beds_status ON beds(status);
