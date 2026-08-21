CREATE TABLE complaints (
    id BIGINT NOT NULL AUTO_INCREMENT,
    complaint_number VARCHAR(50) NOT NULL,
    tenant_profile_id BIGINT NOT NULL,
    property_id BIGINT NOT NULL,
    room_id BIGINT,
    category VARCHAR(40) NOT NULL,
    title VARCHAR(180) NOT NULL,
    description TEXT NOT NULL,
    priority VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    assigned_to BIGINT,
    resolved_at DATETIME(6),
    closed_at DATETIME(6),
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_complaints_complaint_number UNIQUE (complaint_number),
    CONSTRAINT fk_complaints_tenant_profile FOREIGN KEY (tenant_profile_id) REFERENCES tenant_profiles(id),
    CONSTRAINT fk_complaints_property FOREIGN KEY (property_id) REFERENCES pg_properties(id),
    CONSTRAINT fk_complaints_room FOREIGN KEY (room_id) REFERENCES rooms(id),
    CONSTRAINT fk_complaints_assigned_to FOREIGN KEY (assigned_to) REFERENCES users(id)
);

CREATE INDEX idx_complaints_tenant_profile_id ON complaints(tenant_profile_id);
CREATE INDEX idx_complaints_property_id ON complaints(property_id);
CREATE INDEX idx_complaints_status ON complaints(status);
CREATE INDEX idx_complaints_created_at ON complaints(created_at);

CREATE TABLE complaint_comments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    complaint_id BIGINT NOT NULL,
    author_user_id BIGINT NOT NULL,
    comment TEXT NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_complaint_comments_complaint FOREIGN KEY (complaint_id) REFERENCES complaints(id),
    CONSTRAINT fk_complaint_comments_author FOREIGN KEY (author_user_id) REFERENCES users(id)
);

CREATE INDEX idx_complaint_comments_complaint_id ON complaint_comments(complaint_id);

CREATE TABLE complaint_status_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    complaint_id BIGINT NOT NULL,
    previous_status VARCHAR(30),
    new_status VARCHAR(30) NOT NULL,
    remarks TEXT,
    changed_by BIGINT,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_complaint_history_complaint FOREIGN KEY (complaint_id) REFERENCES complaints(id),
    CONSTRAINT fk_complaint_history_changed_by FOREIGN KEY (changed_by) REFERENCES users(id)
);

CREATE INDEX idx_complaint_history_complaint_id ON complaint_status_history(complaint_id);

CREATE TABLE maintenance_tasks (
    id BIGINT NOT NULL AUTO_INCREMENT,
    task_number VARCHAR(50) NOT NULL,
    complaint_id BIGINT,
    property_id BIGINT NOT NULL,
    room_id BIGINT,
    title VARCHAR(180) NOT NULL,
    description TEXT NOT NULL,
    priority VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    assigned_to_text VARCHAR(160),
    scheduled_date DATE,
    completed_at DATETIME(6),
    remarks TEXT,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_maintenance_tasks_task_number UNIQUE (task_number),
    CONSTRAINT fk_maintenance_tasks_complaint FOREIGN KEY (complaint_id) REFERENCES complaints(id),
    CONSTRAINT fk_maintenance_tasks_property FOREIGN KEY (property_id) REFERENCES pg_properties(id),
    CONSTRAINT fk_maintenance_tasks_room FOREIGN KEY (room_id) REFERENCES rooms(id)
);

CREATE INDEX idx_maintenance_tasks_property_id ON maintenance_tasks(property_id);
CREATE INDEX idx_maintenance_tasks_status ON maintenance_tasks(status);

CREATE TABLE notices (
    id BIGINT NOT NULL AUTO_INCREMENT,
    property_id BIGINT NOT NULL,
    title VARCHAR(180) NOT NULL,
    content TEXT NOT NULL,
    notice_type VARCHAR(40) NOT NULL,
    priority VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,
    published_at DATETIME(6),
    expires_at DATE,
    created_by BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_notices_property FOREIGN KEY (property_id) REFERENCES pg_properties(id),
    CONSTRAINT fk_notices_created_by FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE INDEX idx_notices_property_id ON notices(property_id);
CREATE INDEX idx_notices_status ON notices(status);
CREATE INDEX idx_notices_published_at ON notices(published_at);

CREATE TABLE food_menus (
    id BIGINT NOT NULL AUTO_INCREMENT,
    property_id BIGINT NOT NULL,
    menu_date DATE NOT NULL,
    meal_type VARCHAR(30) NOT NULL,
    items TEXT NOT NULL,
    notes TEXT,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_food_menus_property_date_meal UNIQUE (property_id, menu_date, meal_type),
    CONSTRAINT fk_food_menus_property FOREIGN KEY (property_id) REFERENCES pg_properties(id)
);

CREATE INDEX idx_food_menus_property_id ON food_menus(property_id);
CREATE INDEX idx_food_menus_menu_date ON food_menus(menu_date);

CREATE TABLE food_feedback (
    id BIGINT NOT NULL AUTO_INCREMENT,
    tenant_profile_id BIGINT NOT NULL,
    property_id BIGINT NOT NULL,
    menu_date DATE NOT NULL,
    meal_type VARCHAR(30) NOT NULL,
    rating INT NOT NULL,
    comment TEXT,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_food_feedback_tenant_date_meal UNIQUE (tenant_profile_id, menu_date, meal_type),
    CONSTRAINT fk_food_feedback_tenant_profile FOREIGN KEY (tenant_profile_id) REFERENCES tenant_profiles(id),
    CONSTRAINT fk_food_feedback_property FOREIGN KEY (property_id) REFERENCES pg_properties(id)
);

CREATE INDEX idx_food_feedback_property_id ON food_feedback(property_id);
CREATE INDEX idx_food_feedback_menu_date ON food_feedback(menu_date);

CREATE TABLE visitor_entries (
    id BIGINT NOT NULL AUTO_INCREMENT,
    visitor_number VARCHAR(50) NOT NULL,
    tenant_profile_id BIGINT NOT NULL,
    property_id BIGINT NOT NULL,
    visitor_name VARCHAR(140) NOT NULL,
    visitor_phone VARCHAR(30) NOT NULL,
    relationship VARCHAR(80) NOT NULL,
    visit_date DATE NOT NULL,
    expected_arrival_time TIME NOT NULL,
    expected_departure_time TIME NOT NULL,
    actual_arrival_time DATETIME(6),
    actual_departure_time DATETIME(6),
    purpose VARCHAR(300) NOT NULL,
    status VARCHAR(30) NOT NULL,
    approved_by BIGINT,
    approved_at DATETIME(6),
    rejection_reason TEXT,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_visitor_entries_visitor_number UNIQUE (visitor_number),
    CONSTRAINT fk_visitor_entries_tenant_profile FOREIGN KEY (tenant_profile_id) REFERENCES tenant_profiles(id),
    CONSTRAINT fk_visitor_entries_property FOREIGN KEY (property_id) REFERENCES pg_properties(id),
    CONSTRAINT fk_visitor_entries_approved_by FOREIGN KEY (approved_by) REFERENCES users(id)
);

CREATE INDEX idx_visitor_entries_property_id ON visitor_entries(property_id);
CREATE INDEX idx_visitor_entries_tenant_profile_id ON visitor_entries(tenant_profile_id);
CREATE INDEX idx_visitor_entries_visit_date ON visitor_entries(visit_date);
CREATE INDEX idx_visitor_entries_status ON visitor_entries(status);

CREATE TABLE notifications (
    id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    type VARCHAR(60) NOT NULL,
    title VARCHAR(180) NOT NULL,
    message VARCHAR(500) NOT NULL,
    reference_type VARCHAR(80),
    reference_id BIGINT,
    read_at DATETIME(6),
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_read_at ON notifications(read_at);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);
CREATE INDEX idx_notifications_reference ON notifications(reference_type, reference_id);
