CREATE TABLE IF NOT EXISTS upload (
    id                  BIGSERIAL PRIMARY KEY,
    external_id         VARCHAR(250) NOT NULL UNIQUE,   -- Used for aws upload id
    directory_name      VARCHAR(250) NOT NULL,          -- Used for s3 bucket name
    file_name           VARCHAR(250) NOT NULL,
    created_at          TIMESTAMP NOT NULL,
    updated_at          TIMESTAMP NOT NULL,
    version             BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS upload_part (
    id                  BIGSERIAL PRIMARY KEY,
    upload_id           BIGINT NOT NULL,
    url                 VARCHAR(2048) NOT NULL,
    part_number         BIGINT NOT NULL,
    completion_tag      VARCHAR(250),
    part_size_in_bytes  BIGINT NOT NULL,
    created_at          TIMESTAMP NOT NULL,
    updated_at          TIMESTAMP NOT NULL,
    version             BIGINT NOT NULL,
    CONSTRAINT fk_upload FOREIGN KEY (upload_id) REFERENCES upload(id) ON DELETE CASCADE
);
