CREATE TABLE IF NOT EXISTS files (
id VARCHAR(36) PRIMARY KEY,
    project_id VARCHAR(36) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    category VARCHAR(30) NOT NULL,
    size BIGINT NOT NULL,
    object_key VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    uploaded_by VARCHAR(36) NOT NULL
    );
