-- Add canonical category table for code + display name
CREATE TABLE IF NOT EXISTS aicrm_picolabbs_rag_category (
    code VARCHAR(255) PRIMARY KEY,
    display_name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_rag_category_display_name
    ON aicrm_picolabbs_rag_category(display_name);

ALTER TABLE aicrm_picolabbs_rag_products ADD COLUMN IF NOT EXISTS category VARCHAR(255);
ALTER TABLE aicrm_picolabbs_rag_services ADD COLUMN IF NOT EXISTS category VARCHAR(255);

-- Backfill existing category codes from products/services.
INSERT INTO aicrm_picolabbs_rag_category (code, display_name)
SELECT DISTINCT x.code, x.code
FROM (
    SELECT NULLIF(TRIM(category), '') AS code FROM aicrm_picolabbs_rag_products
    UNION
    SELECT NULLIF(TRIM(category), '') AS code FROM aicrm_picolabbs_rag_services
) x
WHERE x.code IS NOT NULL
ON CONFLICT (code) DO NOTHING;
