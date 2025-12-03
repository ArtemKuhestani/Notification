-- =====================================================
-- Notification Service Database Schema
-- Version: 1.0.0
-- PostgreSQL 15+
-- =====================================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =====================================================
-- Table: admins
-- Stores administrator accounts
-- =====================================================
CREATE TABLE IF NOT EXISTS admins (
    admin_id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(60) NOT NULL,
    full_name VARCHAR(200) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'VIEWER',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    failed_login_attempts INTEGER NOT NULL DEFAULT 0,
    locked_until TIMESTAMP NULL,
    last_login_at TIMESTAMP NULL,
    last_login_ip VARCHAR(45) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    
    CONSTRAINT chk_admins_role CHECK (role IN ('ADMIN', 'VIEWER'))
);

CREATE INDEX IF NOT EXISTS idx_admins_email ON admins(email);
CREATE INDEX IF NOT EXISTS idx_admins_active ON admins(is_active) WHERE is_active = TRUE;

-- =====================================================
-- Table: api_clients
-- Stores external API client information
-- =====================================================
CREATE TABLE IF NOT EXISTS api_clients (
    client_id SERIAL PRIMARY KEY,
    client_name VARCHAR(100) NOT NULL UNIQUE,
    client_description TEXT NULL,
    api_key_hash VARCHAR(64) NOT NULL,
    api_key_prefix VARCHAR(8) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    rate_limit INTEGER NOT NULL DEFAULT 100,
    allowed_channels VARCHAR[] NULL,
    allowed_ips VARCHAR[] NULL,
    callback_url_default VARCHAR(500) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by INTEGER NULL REFERENCES admins(admin_id),
    last_used_at TIMESTAMP NULL
);

CREATE INDEX IF NOT EXISTS idx_api_clients_api_key_hash ON api_clients(api_key_hash);
CREATE INDEX IF NOT EXISTS idx_api_clients_active ON api_clients(is_active) WHERE is_active = TRUE;
CREATE INDEX IF NOT EXISTS idx_api_clients_prefix ON api_clients(api_key_prefix);

-- =====================================================
-- Table: channel_configs
-- Stores channel provider configurations
-- =====================================================
CREATE TABLE IF NOT EXISTS channel_configs (
    config_id SERIAL PRIMARY KEY,
    channel_name VARCHAR(20) NOT NULL UNIQUE,
    provider_name VARCHAR(50) NOT NULL,
    credentials BYTEA NOT NULL,
    settings JSONB NULL,
    is_enabled BOOLEAN NOT NULL DEFAULT FALSE,
    priority INTEGER NOT NULL DEFAULT 0,
    daily_limit INTEGER NULL,
    daily_sent_count INTEGER NOT NULL DEFAULT 0,
    last_health_check TIMESTAMP NULL,
    health_status VARCHAR(20) DEFAULT 'UNKNOWN',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    
    CONSTRAINT chk_channel_name CHECK (channel_name IN ('EMAIL', 'TELEGRAM', 'SMS', 'WHATSAPP')),
    CONSTRAINT chk_health_status CHECK (health_status IN ('UNKNOWN', 'HEALTHY', 'UNHEALTHY', 'DEGRADED'))
);

-- =====================================================
-- Table: message_templates
-- Stores message templates
-- =====================================================
CREATE TABLE IF NOT EXISTS message_templates (
    template_id SERIAL PRIMARY KEY,
    template_code VARCHAR(50) NOT NULL UNIQUE,
    template_name VARCHAR(200) NOT NULL,
    channel_type VARCHAR(20) NOT NULL,
    subject_template VARCHAR(500) NULL,
    body_template TEXT NOT NULL,
    variables VARCHAR[] NOT NULL DEFAULT '{}',
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by INTEGER NULL REFERENCES admins(admin_id),
    
    CONSTRAINT chk_template_channel CHECK (channel_type IN ('EMAIL', 'TELEGRAM', 'SMS', 'WHATSAPP'))
);

CREATE INDEX IF NOT EXISTS idx_templates_code ON message_templates(template_code);
CREATE INDEX IF NOT EXISTS idx_templates_active ON message_templates(is_active) WHERE is_active = TRUE;

-- =====================================================
-- Table: notifications
-- Stores all notification requests and their status
-- =====================================================
CREATE TABLE IF NOT EXISTS notifications (
    notification_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    client_id INTEGER NOT NULL REFERENCES api_clients(client_id),
    channel_type VARCHAR(20) NOT NULL,
    recipient VARCHAR(255) NOT NULL,
    subject VARCHAR(500) NULL,
    message_body TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    priority VARCHAR(10) NOT NULL DEFAULT 'NORMAL',
    retry_count INTEGER NOT NULL DEFAULT 0,
    max_retries INTEGER NOT NULL DEFAULT 5,
    next_retry_at TIMESTAMP NULL,
    error_message TEXT NULL,
    error_code VARCHAR(50) NULL,
    provider_message_id VARCHAR(255) NULL,
    idempotency_key VARCHAR(255) NULL UNIQUE,
    callback_url VARCHAR(500) NULL,
    metadata JSONB NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    sent_at TIMESTAMP NULL,
    expires_at TIMESTAMP NULL,
    
    CONSTRAINT chk_notifications_channel CHECK (channel_type IN ('EMAIL', 'TELEGRAM', 'SMS', 'WHATSAPP')),
    CONSTRAINT chk_notifications_status CHECK (status IN ('PENDING', 'SENDING', 'SENT', 'DELIVERED', 'FAILED', 'EXPIRED')),
    CONSTRAINT chk_notifications_priority CHECK (priority IN ('HIGH', 'NORMAL', 'LOW'))
);

CREATE INDEX IF NOT EXISTS idx_notifications_status ON notifications(status);
CREATE INDEX IF NOT EXISTS idx_notifications_client_id ON notifications(client_id);
CREATE INDEX IF NOT EXISTS idx_notifications_created_at ON notifications(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_notifications_next_retry ON notifications(next_retry_at) WHERE status = 'PENDING';
CREATE INDEX IF NOT EXISTS idx_notifications_idempotency ON notifications(idempotency_key) WHERE idempotency_key IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_notifications_channel ON notifications(channel_type);

-- =====================================================
-- Table: retry_queue
-- Stores messages pending retry
-- =====================================================
CREATE TABLE IF NOT EXISTS retry_queue (
    queue_id BIGSERIAL PRIMARY KEY,
    notification_id UUID NOT NULL REFERENCES notifications(notification_id),
    retry_attempt INTEGER NOT NULL DEFAULT 1,
    scheduled_at TIMESTAMP NOT NULL,
    processed_at TIMESTAMP NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    
    CONSTRAINT chk_retry_status CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED'))
);

CREATE INDEX IF NOT EXISTS idx_retry_queue_scheduled ON retry_queue(scheduled_at) WHERE status = 'PENDING';
CREATE INDEX IF NOT EXISTS idx_retry_queue_notification ON retry_queue(notification_id);

-- =====================================================
-- Table: audit_log
-- Stores admin action history
-- =====================================================
CREATE TABLE IF NOT EXISTS audit_log (
    log_id BIGSERIAL PRIMARY KEY,
    admin_id INTEGER NULL REFERENCES admins(admin_id),
    action_type VARCHAR(50) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id VARCHAR(50) NULL,
    old_value JSONB NULL,
    new_value JSONB NULL,
    ip_address VARCHAR(45) NOT NULL,
    user_agent TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_audit_log_admin ON audit_log(admin_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_created ON audit_log(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_audit_log_action ON audit_log(action_type);
CREATE INDEX IF NOT EXISTS idx_audit_log_entity ON audit_log(entity_type, entity_id);

-- =====================================================
-- Table: notification_stats_hourly
-- Aggregated hourly statistics for dashboard
-- =====================================================
CREATE TABLE IF NOT EXISTS notification_stats_hourly (
    stat_id BIGSERIAL PRIMARY KEY,
    stat_hour TIMESTAMP NOT NULL,
    channel_type VARCHAR(20) NOT NULL,
    total_sent INTEGER NOT NULL DEFAULT 0,
    total_failed INTEGER NOT NULL DEFAULT 0,
    total_pending INTEGER NOT NULL DEFAULT 0,
    avg_delivery_time_ms BIGINT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    
    CONSTRAINT uq_stats_hour_channel UNIQUE (stat_hour, channel_type)
);

CREATE INDEX IF NOT EXISTS idx_stats_hour ON notification_stats_hourly(stat_hour DESC);

-- =====================================================
-- Function: Update updated_at timestamp
-- =====================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Apply trigger to tables with updated_at column
DROP TRIGGER IF EXISTS update_admins_updated_at ON admins;
CREATE TRIGGER update_admins_updated_at
    BEFORE UPDATE ON admins
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_api_clients_updated_at ON api_clients;
CREATE TRIGGER update_api_clients_updated_at
    BEFORE UPDATE ON api_clients
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_channel_configs_updated_at ON channel_configs;
CREATE TRIGGER update_channel_configs_updated_at
    BEFORE UPDATE ON channel_configs
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_message_templates_updated_at ON message_templates;
CREATE TRIGGER update_message_templates_updated_at
    BEFORE UPDATE ON message_templates
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

DROP TRIGGER IF EXISTS update_notifications_updated_at ON notifications;
CREATE TRIGGER update_notifications_updated_at
    BEFORE UPDATE ON notifications
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- Insert default email channel config (disabled)
-- =====================================================
INSERT INTO channel_configs (channel_name, provider_name, credentials, settings, is_enabled, priority)
VALUES 
    ('EMAIL', 'SMTP', E'\\x', '{"host": "smtp.gmail.com", "port": 587, "use_tls": true}'::jsonb, false, 1),
    ('TELEGRAM', 'Telegram Bot API', E'\\x', '{"parse_mode": "HTML"}'::jsonb, false, 2),
    ('SMS', 'Default SMS Provider', E'\\x', '{}'::jsonb, false, 3),
    ('WHATSAPP', 'WhatsApp Business API', E'\\x', '{}'::jsonb, false, 4)
ON CONFLICT (channel_name) DO NOTHING;

-- =====================================================
-- Insert default API client for testing
-- API Key: ns_test_api_key_12345678 (SHA-256 hash stored)
-- =====================================================
INSERT INTO api_clients (client_name, client_description, api_key_hash, api_key_prefix, is_active, rate_limit)
VALUES (
    'Test Client',
    'Default test client for development',
    'a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3',  -- SHA-256 of '123'
    'ns_test_',
    true,
    1000
)
ON CONFLICT (client_name) DO NOTHING;
