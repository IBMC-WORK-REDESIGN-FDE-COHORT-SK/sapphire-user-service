-- ===============================================
-- TABLE CREATION SCRIPT
-- ===============================================

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    full_name VARCHAR(255) NOT NULL,
    user_tier VARCHAR(20) NOT NULL,
    physical_attributes JSONB,
    address JSONB,
    labels JSONB,
    metadata JSONB,
    annotations JSONB,
    links JSONB,
    tags JSONB,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- ===============================================
-- INSERT SCRIPTS FOR PERSONAS (Distributed: GB, US, IN)
-- ===============================================

-- 1. Sarah Chen – GB (London)
INSERT INTO users (
    email, full_name, user_tier, physical_attributes, address, labels, tags, metadata, annotations, links
) VALUES (
    'sarah.chen@sapphirewellness.com',
    'Sarah Chen',
    'PREMIUM',
    '{"gender":"female","heightCm":165,"weightKg":68}'::jsonb,
    '{"city":"London","state":"England","country":"GB","zip":"EC1A1BB"}'::jsonb,
    '{"persona":"weight_watcher"}'::jsonb,
    '["urban","marketing"]'::jsonb,
    '{}',
    '{"subscription":"premium"}',
    '[]'
);

-- 2. Marcus Johnson – US (New York)
INSERT INTO users (
    email, full_name, user_tier, physical_attributes, address, labels, tags, metadata, annotations, links
) VALUES (
    'marcus.johnson@sapphirewellness.com',
    'Marcus Johnson',
    'PREMIUM',
    '{"gender":"male","heightCm":178,"weightKg":80}'::jsonb,
    '{"city":"New York","state":"NY","country":"US","zip":"10001"}'::jsonb,
    '{"persona":"fitness_enthusiast"}'::jsonb,
    '["software_dev","athlete"]'::jsonb,
    '{}',
    '{"analytics":"advanced"}',
    '[]'
);

-- 3. Elena Rodriguez – US (Chicago)
INSERT INTO users (
    email, full_name, user_tier, physical_attributes, address, labels, tags, metadata, annotations, links
) VALUES (
    'elena.rodriguez@sapphirewellness.com',
    'Elena Rodriguez',
    'FREE',
    '{"gender":"female","heightCm":160,"weightKg":70}'::jsonb,
    '{"city":"Chicago","state":"IL","country":"US","zip":"60601"}'::jsonb,
    '{"persona":"busy_professional"}'::jsonb,
    '["finance","stress_management"]'::jsonb,
    '{}',
    '{}',
    '[]'
);

-- 4. David Kim – GB (Manchester)
INSERT INTO users (
    email, full_name, user_tier, physical_attributes, address, labels, tags, metadata, annotations, links
) VALUES (
    'david.kim@sapphirewellness.com',
    'David Kim',
    'FREE',
    '{"gender":"male","heightCm":170,"weightKg":85}'::jsonb,
    '{"city":"Manchester","state":"England","country":"GB","zip":"M11AE"}'::jsonb,
    '{"persona":"chronic_condition_manager"}'::jsonb,
    '["diabetes","hypertension"]'::jsonb,
    '{}',
    '{}',
    '[]'
);

-- 5. Maya Patel – IN (Mumbai)
INSERT INTO users (
    email, full_name, user_tier, physical_attributes, address, labels, tags, metadata, annotations, links
) VALUES (
    'maya.patel@sapphirewellness.com',
    'Maya Patel',
    'FREE',
    '{"gender":"female","heightCm":162,"weightKg":55}'::jsonb,
    '{"city":"Mumbai","state":"Maharashtra","country":"IN","zip":"400001"}'::jsonb,
    '{"persona":"wellness_explorer"}'::jsonb,
    '["yoga","fitness","experiments"]'::jsonb,
    '{}',
    '{}',
    '[]'
);

-- 6. Margaret Thompson – US (Boston)
INSERT INTO users (
    email, full_name, user_tier, physical_attributes, address, labels, tags, metadata, annotations, links
) VALUES (
    'margaret.thompson@sapphirewellness.com',
    'Margaret Thompson',
    'FREE',
    '{"gender":"female","heightCm":155,"weightKg":60}'::jsonb,
    '{"city":"Boston","state":"MA","country":"US","zip":"02108"}'::jsonb,
    '{"persona":"elder_user"}'::jsonb,
    '["caregiver_access"]'::jsonb,
    '{}',
    '{}',
    '[]'
);

-- END_OF_SCHEMA

-- ===============================================
-- USER WELLNESS SUMMARY TABLE
-- ===============================================

CREATE TABLE IF NOT EXISTS user_wellness_summary (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    profile_summary TEXT NOT NULL,
    data_summary TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE OR REPLACE FUNCTION update_user_wellness_summary_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_user_wellness_summary_updated_at
BEFORE UPDATE ON user_wellness_summary
FOR EACH ROW
EXECUTE FUNCTION update_user_wellness_summary_updated_at();

-- ===============================================
-- ALERTS TABLE CREATION
-- ===============================================

CREATE TABLE IF NOT EXISTS user_alerts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    metric_name VARCHAR(255) NOT NULL,
    metric_type VARCHAR(100) NOT NULL,
    alert_message TEXT NOT NULL,
    labels JSONB,
    metadata JSONB,
    annotations JSONB,
    links JSONB,
    tags JSONB,
    created_at TIMESTAMP DEFAULT NOW()
);

-- ===============================================
-- USER PARTNER SERVICES SUBSCRIPTIONS TABLE
-- ===============================================

CREATE TABLE IF NOT EXISTS user_partner_services_subscriptions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    partner_service_id UUID NOT NULL,
    association_context JSONB,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE OR REPLACE FUNCTION update_subscription_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_subscription_updated_at
BEFORE UPDATE ON user_partner_services_subscriptions
FOR EACH ROW
EXECUTE FUNCTION update_subscription_updated_at();

-- ===============================================
-- USER RECOMMENDATIONS TABLE
-- ===============================================

CREATE TABLE IF NOT EXISTS user_recommendations (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    partner_service_id UUID NOT NULL,
    relevance_score INTEGER,
    labels JSONB,
    tags JSONB,
    annotations JSONB,
    spec JSONB,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

CREATE OR REPLACE FUNCTION update_user_recommendations_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_user_recommendations_updated_at
BEFORE UPDATE ON user_recommendations
FOR EACH ROW
EXECUTE FUNCTION update_user_recommendations_updated_at();

-- ===============================================
-- WELLNESS SUMMARY DATA INSERTS
-- ===============================================

-- Sarah Chen
INSERT INTO user_wellness_summary (user_id, profile_summary, data_summary, created_at, updated_at)
SELECT id, 
       'Overall, the profile indicates a highly active, normal individual.',
       'The user shows a active lifestyle with good daily movement with approximately 10398 steps per day and regular walking and exercise. Blood oxygen at 96.9 percent indicates normal oxygen saturation. An average blood pressure of 119/79 indicates optimal blood pressure. Fasting glucose of 85 and random glucose of 114 place the user in the normal range. A pulse rate around 79 suggests a normal resting heart rate.',
       NOW(),
       NOW()
FROM users WHERE email = 'sarah.chen@sapphirewellness.com';

-- Marcus Johnson
INSERT INTO user_wellness_summary (user_id, profile_summary, data_summary, created_at, updated_at)
SELECT id,
       'Overall, the profile indicates a highly active, normal individual.',
       'The user shows a highly active lifestyle with significant daily movement with approximately 15205 steps per day and running and regular exercise. Blood oxygen at 97.4 percent indicates excellent oxygen saturation. An average blood pressure of 118/77 indicates optimal blood pressure. Fasting glucose of 96 and random glucose of 136 place the user in the normal range. A pulse rate around 84 suggests a moderately elevated resting heart rate.',
       NOW(),
       NOW()
FROM users WHERE email = 'marcus.johnson@sapphirewellness.com';

-- Elena Rodriguez
INSERT INTO user_wellness_summary (user_id, profile_summary, data_summary, created_at, updated_at)
SELECT id,
       'Overall, the profile indicates a moderately active, normal individual.',
       'The user shows a moderately active lifestyle with approximately 7728 steps per day and moderate daily walking. Blood oxygen at 96.7 percent indicates normal oxygen saturation. An average blood pressure of 118/77 indicates optimal blood pressure. Fasting glucose of 96 and random glucose of 129 place the user in the normal range. A pulse rate around 75 suggests a normal resting heart rate.',
       NOW(),
       NOW()
FROM users WHERE email = 'elena.rodriguez@sapphirewellness.com';

-- David Kim
INSERT INTO user_wellness_summary (user_id, profile_summary, data_summary, created_at, updated_at)
SELECT id,
       'Overall, the profile indicates a minimally active, prediabetic, with mildly low oxygen saturation individual.',
       'The user shows a somewhat sedentary lifestyle with limited activity with approximately 6652 steps per day and light walking. Blood oxygen at 94.1 percent indicates slightly lower than ideal oxygen saturation. An average blood pressure of 119/77 indicates optimal blood pressure. Fasting glucose of 104 and random glucose of 140 place the user in the prediabetic range. A pulse rate around 75 suggests a normal resting heart rate.',
       NOW(),
       NOW()
FROM users WHERE email = 'david.kim@sapphirewellness.com';

-- Maya Patel
INSERT INTO user_wellness_summary (user_id, profile_summary, data_summary, created_at, updated_at)
SELECT id,
       'Overall, the profile indicates a highly active, normal individual.',
       'The user shows a active lifestyle with good daily movement with approximately 11505 steps per day and regular walking and exercise. Blood oxygen at 95.1 percent indicates normal oxygen saturation. An average blood pressure of 120/77 indicates normal to slightly elevated blood pressure. Fasting glucose of 98 and random glucose of 137 place the user in the normal range. A pulse rate around 79 suggests a normal resting heart rate.',
       NOW(),
       NOW()
FROM users WHERE email = 'maya.patel@sapphirewellness.com';

-- Margaret Thompson
INSERT INTO user_wellness_summary (user_id, profile_summary, data_summary, created_at, updated_at)
SELECT id,
       'Overall, the profile indicates a minimally active, prediabetic individual.',
       'The user shows a somewhat sedentary lifestyle with limited activity with approximately 5200 steps per day and light walking. Blood oxygen at 95.6 percent indicates normal oxygen saturation. An average blood pressure of 114/79 indicates optimal blood pressure. Fasting glucose of 104 and random glucose of 142 place the user in the prediabetic range. A pulse rate around 74 suggests a normal resting heart rate.',
       NOW(),
       NOW()
FROM users WHERE email = 'margaret.thompson@sapphirewellness.com';

-- ===============================================
-- ADF-9: PROMOTIONS TABLE
-- ===============================================

CREATE TABLE IF NOT EXISTS promotions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(80) NOT NULL,
    badge_label VARCHAR(20),
    body_text TEXT,
    cta_label VARCHAR(30) NOT NULL,
    cta_url VARCHAR(2048) NOT NULL,
    background_colour CHAR(7) NOT NULL DEFAULT '#FFFFFF',
    text_colour CHAR(7) NOT NULL DEFAULT '#000000',
    target_tier VARCHAR(20) NOT NULL DEFAULT 'FREE',
    starts_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    impression_count BIGINT NOT NULL DEFAULT 0,
    click_count BIGINT NOT NULL DEFAULT 0,
    dismiss_count BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_promotion_window CHECK (expires_at > starts_at)
);

-- Only one active promotion per tier at a time
CREATE UNIQUE INDEX IF NOT EXISTS uidx_promotions_active_tier
    ON promotions (target_tier)
    WHERE is_active = TRUE;

CREATE OR REPLACE FUNCTION update_promotions_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_promotions_updated_at
BEFORE UPDATE ON promotions
FOR EACH ROW
EXECUTE FUNCTION update_promotions_updated_at();
