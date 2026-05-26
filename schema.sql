-- ================================================================
-- NOVA CORE DATABASE SCHEMA
-- PostgreSQL 16+
-- ================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- USERS
CREATE TABLE IF NOT EXISTS users (
    id              UUID        DEFAULT uuid_generate_v4() PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    display_name    VARCHAR(100) NOT NULL,
    photo_url       TEXT,
    user_id         VARCHAR(80)  NOT NULL UNIQUE,   -- faiz@48372
    referral_code   VARCHAR(10)  NOT NULL UNIQUE,   -- 5-digit
    referred_by_id  UUID         REFERENCES users(id) ON DELETE SET NULL,
    nova_balance    NUMERIC(18,6) NOT NULL DEFAULT 0,
    total_earned    NUMERIC(18,6) NOT NULL DEFAULT 0,
    total_claims    INT NOT NULL DEFAULT 0,
    referral_count  INT NOT NULL DEFAULT 0,
    mining_speed    NUMERIC(8,4) NOT NULL DEFAULT 0.1,
    current_streak  INT NOT NULL DEFAULT 0,
    last_claim_at   TIMESTAMP,
    is_active       BOOLEAN NOT NULL DEFAULT TRUE,
    is_banned       BOOLEAN NOT NULL DEFAULT FALSE,
    role            VARCHAR(30) NOT NULL DEFAULT 'ROLE_USER',
    created_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_users_email         ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_user_id       ON users(user_id);
CREATE INDEX IF NOT EXISTS idx_users_referral_code ON users(referral_code);

-- MINING HISTORY
CREATE TABLE IF NOT EXISTS mining_history (
    id            UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    user_id       UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    slot_time     TIMESTAMP NOT NULL,  -- normalised slot e.g. 2024-05-23T06:00
    points_earned NUMERIC(18,6) NOT NULL,
    ip_address    VARCHAR(45),
    claimed_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    UNIQUE (user_id, slot_time)       -- prevent double-claim per slot
);

CREATE INDEX IF NOT EXISTS idx_mh_user      ON mining_history(user_id);
CREATE INDEX IF NOT EXISTS idx_mh_slot      ON mining_history(slot_time);
CREATE INDEX IF NOT EXISTS idx_mh_claimed   ON mining_history(claimed_at);

-- REFERRAL TRACKING
CREATE TABLE IF NOT EXISTS referral_tracking (
    id                       UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    referrer_id              UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    referred_id              UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    register_bonus_paid      BOOLEAN NOT NULL DEFAULT FALSE,
    mining_bonus_paid        BOOLEAN NOT NULL DEFAULT FALSE,
    total_per_claim_bonus    INT NOT NULL DEFAULT 0,
    total_earned_from_referral NUMERIC(18,6) NOT NULL DEFAULT 0,
    created_at               TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_rt_referrer ON referral_tracking(referrer_id);
CREATE INDEX IF NOT EXISTS idx_rt_referred ON referral_tracking(referred_id);

-- WALLET TRANSACTIONS
CREATE TABLE IF NOT EXISTS wallet_transactions (
    id            UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    user_id       UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    type          VARCHAR(40) NOT NULL,
    amount        NUMERIC(18,6) NOT NULL,
    balance_after NUMERIC(18,6) NOT NULL,
    description   TEXT,
    created_at    TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_wt_user    ON wallet_transactions(user_id);
CREATE INDEX IF NOT EXISTS idx_wt_type    ON wallet_transactions(type);
CREATE INDEX IF NOT EXISTS idx_wt_created ON wallet_transactions(created_at);

-- REFRESH TOKENS
CREATE TABLE IF NOT EXISTS refresh_tokens (
    id         UUID DEFAULT uuid_generate_v4() PRIMARY KEY,
    token      TEXT NOT NULL UNIQUE,
    user_id    UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    expires_at TIMESTAMP NOT NULL,
    revoked    BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_rft_token ON refresh_tokens(token);
CREATE INDEX IF NOT EXISTS idx_rft_user  ON refresh_tokens(user_id);

-- VIEWS
CREATE OR REPLACE VIEW v_platform_stats AS
SELECT
    (SELECT COUNT(*) FROM users)                                      AS total_users,
    (SELECT COUNT(*) FROM users WHERE is_active = TRUE)               AS active_users,
    (SELECT COALESCE(SUM(nova_balance),0) FROM users)                 AS total_circulating,
    (SELECT COALESCE(SUM(total_earned),0) FROM users)                 AS total_ever_earned,
    (SELECT COALESCE(SUM(points_earned),0) FROM mining_history)       AS total_points_distributed,
    (SELECT COUNT(*) FROM mining_history WHERE claimed_at > NOW() - INTERVAL '24h') AS claims_last_24h,
    (SELECT COUNT(*) FROM referral_tracking)                          AS total_referrals;

-- Auto updated_at trigger
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$ BEGIN NEW.updated_at = NOW(); RETURN NEW; END; $$ LANGUAGE plpgsql;
