CREATE TABLE IF NOT EXISTS urls (
    id BIGSERIAL PRIMARY KEY,
    short_code VARCHAR(20) NOT NULL,
    original_url TEXT NOT NULL,
    custom_alias VARCHAR(50),
    expiration_date TIMESTAMP,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_urls_short_code UNIQUE (short_code)
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_urls_short_code ON urls (short_code);
CREATE INDEX IF NOT EXISTS idx_urls_created_at ON urls (created_at);
CREATE INDEX IF NOT EXISTS idx_urls_is_active ON urls (is_active);

CREATE TABLE IF NOT EXISTS url_analytics (
    id BIGSERIAL PRIMARY KEY,
    url_id BIGINT NOT NULL,
    clicked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ip_address VARCHAR(45),
    country VARCHAR(100),
    browser VARCHAR(100),
    os VARCHAR(100),
    device_type VARCHAR(50),
    referrer TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_url_analytics_url FOREIGN KEY (url_id) REFERENCES urls (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_url_analytics_url_id ON url_analytics (url_id);
CREATE INDEX IF NOT EXISTS idx_url_analytics_clicked_at ON url_analytics (clicked_at);
CREATE INDEX IF NOT EXISTS idx_url_analytics_url_id_clicked_at ON url_analytics (url_id, clicked_at);

CREATE TABLE IF NOT EXISTS url_stats (
    id BIGSERIAL PRIMARY KEY,
    url_id BIGINT NOT NULL,
    total_clicks BIGINT NOT NULL DEFAULT 0,
    first_visit TIMESTAMP,
    last_visit TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_url_stats_url_id UNIQUE (url_id),
    CONSTRAINT fk_url_stats_url FOREIGN KEY (url_id) REFERENCES urls (id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_url_stats_url_id ON url_stats (url_id);