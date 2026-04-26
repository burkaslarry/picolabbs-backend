-- PostgreSQL schema for AI CRM only (TEXT instead of CLOB).
-- Safe on shared DBs (e.g. BNI Render "eventxp"): uses CREATE TABLE IF NOT EXISTS only — no DROP,
-- no changes to existing bni_anchor_* or other non-CRM tables.
CREATE TABLE IF NOT EXISTS aicrm_picolabbs_leads (
    id VARCHAR(36) PRIMARY KEY,
    channel VARCHAR(20) NOT NULL DEFAULT 'web',
    raw_message TEXT,
    name VARCHAR(500),
    contact VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    stage VARCHAR(50) NOT NULL DEFAULT 'New',
    owner_id VARCHAR(255),
    vertical VARCHAR(50),
    source VARCHAR(255),
    service_date VARCHAR(10)
);

CREATE TABLE IF NOT EXISTS aicrm_picolabbs_ai_triage (
    lead_id VARCHAR(36) PRIMARY KEY REFERENCES aicrm_picolabbs_leads(id),
    vertical VARCHAR(50),
    category VARCHAR(50),
    subcategory VARCHAR(255),
    intent VARCHAR(50),
    urgency_score INT,
    extracted_fields TEXT,
    missing_fields TEXT,
    summary TEXT,
    recommended_actions TEXT,
    safety_escalate INT DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS aicrm_picolabbs_tasks (
    id VARCHAR(36) PRIMARY KEY,
    lead_id VARCHAR(36) NOT NULL REFERENCES aicrm_picolabbs_leads(id),
    type VARCHAR(50) NOT NULL,
    title VARCHAR(500) NOT NULL,
    due_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS aicrm_picolabbs_timeline (
    id VARCHAR(36) PRIMARY KEY,
    lead_id VARCHAR(36) NOT NULL REFERENCES aicrm_picolabbs_leads(id),
    event_type VARCHAR(100) NOT NULL,
    payload TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS aicrm_picolabbs_automation_rules (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    trigger_condition TEXT NOT NULL,
    actions TEXT NOT NULL,
    enabled INT DEFAULT 1,
    sort_order INT DEFAULT 0
);

CREATE TABLE IF NOT EXISTS aicrm_picolabbs_slot_suggestions (
    id VARCHAR(36) PRIMARY KEY,
    lead_id VARCHAR(36) NOT NULL REFERENCES aicrm_picolabbs_leads(id),
    slots TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS aicrm_picolabbs_scheduled_jobs (
    id VARCHAR(36) PRIMARY KEY,
    lead_id VARCHAR(36) NOT NULL REFERENCES aicrm_picolabbs_leads(id),
    job_type VARCHAR(50) NOT NULL,
    run_at TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'pending',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_leads_stage ON aicrm_picolabbs_leads(stage);
CREATE INDEX IF NOT EXISTS idx_leads_channel ON aicrm_picolabbs_leads(channel);
CREATE INDEX IF NOT EXISTS idx_leads_created ON aicrm_picolabbs_leads(created_at);
CREATE INDEX IF NOT EXISTS idx_leads_service_date ON aicrm_picolabbs_leads(service_date);
CREATE INDEX IF NOT EXISTS idx_tasks_lead ON aicrm_picolabbs_tasks(lead_id);
CREATE INDEX IF NOT EXISTS idx_timeline_lead ON aicrm_picolabbs_timeline(lead_id);
CREATE INDEX IF NOT EXISTS idx_scheduled_jobs_run_at ON aicrm_picolabbs_scheduled_jobs(run_at);
CREATE INDEX IF NOT EXISTS idx_scheduled_jobs_status ON aicrm_picolabbs_scheduled_jobs(status);

-- RAG: services and products (CSV import, search by region HK/TW/CN)
CREATE TABLE IF NOT EXISTS aicrm_picolabbs_rag_services (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(500) NOT NULL,
    description TEXT,
    region VARCHAR(10) NOT NULL DEFAULT 'hk',
    category VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS aicrm_picolabbs_rag_products (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(500) NOT NULL,
    description TEXT,
    region VARCHAR(10) NOT NULL DEFAULT 'hk',
    category VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE TABLE IF NOT EXISTS aicrm_picolabbs_rag_category (
    code VARCHAR(255) PRIMARY KEY,
    display_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_rag_services_region ON aicrm_picolabbs_rag_services(region);
CREATE INDEX IF NOT EXISTS idx_rag_products_region ON aicrm_picolabbs_rag_products(region);
CREATE INDEX IF NOT EXISTS idx_rag_category_display_name ON aicrm_picolabbs_rag_category(display_name);

CREATE TABLE IF NOT EXISTS aicrm_picolabbs_branches (
    id VARCHAR(64) PRIMARY KEY,
    code VARCHAR(64) UNIQUE,
    name VARCHAR(255),
    district VARCHAR(255),
    address TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    region VARCHAR(10) NOT NULL DEFAULT 'hk',
    name_zh VARCHAR(255),
    name_en VARCHAR(255),
    district_zh VARCHAR(255),
    district_en VARCHAR(255),
    address_zh VARCHAR(1000),
    address_en VARCHAR(1000),
    phone VARCHAR(64),
    whatsapp VARCHAR(64),
    hours_zh VARCHAR(255),
    hours_en VARCHAR(255),
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_branches_region ON aicrm_picolabbs_branches(region);

CREATE TABLE IF NOT EXISTS aicrm_picolabbs_follow_up_cases (
    id VARCHAR(36) PRIMARY KEY,
    case_name VARCHAR(500) NOT NULL,
    contact VARCHAR(500),
    status VARCHAR(50),
    notes TEXT,
    lead_ref VARCHAR(64),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_follow_up_cases_status ON aicrm_picolabbs_follow_up_cases(status);
CREATE INDEX IF NOT EXISTS idx_follow_up_cases_created ON aicrm_picolabbs_follow_up_cases(created_at);

CREATE TABLE IF NOT EXISTS aicrm_picolabbs_rag_documents (
    id VARCHAR(36) PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    region VARCHAR(10) NOT NULL DEFAULT 'sg',
    extracted_text TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS aicrm_picolabbs_rag_document_links (
    id VARCHAR(36) PRIMARY KEY,
    document_id VARCHAR(36) NOT NULL REFERENCES aicrm_picolabbs_rag_documents(id),
    item_type VARCHAR(20) NOT NULL,
    item_id VARCHAR(36) NOT NULL,
    item_name VARCHAR(500) NOT NULL,
    score DOUBLE PRECISION NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_rag_document_links_document ON aicrm_picolabbs_rag_document_links(document_id);
CREATE INDEX IF NOT EXISTS idx_rag_document_links_item ON aicrm_picolabbs_rag_document_links(item_type, item_id);

-- Users: superadmin and operators (for confirming bookings, handling inquiries)
CREATE TABLE IF NOT EXISTS aicrm_picolabbs_user (
    id VARCHAR(36) PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255),
    role VARCHAR(32) NOT NULL DEFAULT 'ops_front',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_users_role ON aicrm_picolabbs_user(role);
