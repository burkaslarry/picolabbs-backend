-- Run once on existing PostgreSQL DBs (tables already created without category).
-- Safe to re-run: IF NOT EXISTS / idempotent updates.

ALTER TABLE aicrm_picolabbs_rag_products ADD COLUMN IF NOT EXISTS category VARCHAR(255);
ALTER TABLE aicrm_picolabbs_rag_services ADD COLUMN IF NOT EXISTS category VARCHAR(255);

-- Remove legacy aesthetic vertical from leads / triage
UPDATE aicrm_picolabbs_leads SET vertical = NULL WHERE vertical = 'med_spa';
UPDATE aicrm_picolabbs_ai_triage SET vertical = NULL, category = NULL WHERE vertical = 'med_spa';

-- Drop rows that are explicitly 醫美 / med spa oriented in RAG tables
DELETE FROM aicrm_picolabbs_rag_products
WHERE LOWER(COALESCE(category, '')) LIKE '%med_spa%'
   OR COALESCE(category, '') LIKE '%醫美%'
   OR COALESCE(name, '') LIKE '%醫美%'
   OR COALESCE(description, '') LIKE '%醫美%';

DELETE FROM aicrm_picolabbs_rag_services
WHERE LOWER(COALESCE(category, '')) LIKE '%med_spa%'
   OR COALESCE(category, '') LIKE '%醫美%'
   OR COALESCE(name, '') LIKE '%醫美%'
   OR COALESCE(description, '') LIKE '%醫美%';
