-- 熟客示範資料（Database 為準）：兩個電話 × 各 2 筆 lead（+852 6111 2222、+852 9999 8888）
-- 喺 Render / 本機 Postgres 執行一次，例如：
--   psql "$DATABASE_URL" -v ON_ERROR_STOP=1 -f src/main/resources/migrations/005_returning_customer_demo.sql
-- 唔再靠 Spring ApplicationRunner 開機 seed。

BEGIN;

INSERT INTO aicrm_picolabbs_leads (id, channel, raw_message, name, contact, stage, vertical, source, service_date, created_at, updated_at)
VALUES (
  '88001001', 'whatsapp',
  $$想問銅鑼灣時代廣場店母親節 iRelief 禮盒仲有冇現貨？可唔可以留貨週末取。$$,
  '陳小姐', '+852 6111 2222', 'New', 'picolabbs_wellness', 'picolabbs_returning_seed', NULL,
  NOW() - INTERVAL '14 days', NOW() - INTERVAL '14 days'
)
ON CONFLICT (id) DO UPDATE SET
  channel = EXCLUDED.channel,
  raw_message = EXCLUDED.raw_message,
  name = EXCLUDED.name,
  contact = EXCLUDED.contact,
  stage = EXCLUDED.stage,
  vertical = EXCLUDED.vertical,
  source = EXCLUDED.source,
  service_date = EXCLUDED.service_date,
  created_at = EXCLUDED.created_at,
  updated_at = EXCLUDED.updated_at;

INSERT INTO aicrm_picolabbs_leads (id, channel, raw_message, name, contact, stage, vertical, source, service_date, created_at, updated_at)
VALUES (
  '88001002', 'whatsapp',
  $$跟進返上次 iRelief 查詢：想改去尖沙咀門市取貨，請問要點改？$$,
  '陳小姐', '+852 6111 2222', 'New', 'picolabbs_wellness', 'picolabbs_returning_seed', NULL,
  NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'
)
ON CONFLICT (id) DO UPDATE SET
  channel = EXCLUDED.channel,
  raw_message = EXCLUDED.raw_message,
  name = EXCLUDED.name,
  contact = EXCLUDED.contact,
  stage = EXCLUDED.stage,
  vertical = EXCLUDED.vertical,
  source = EXCLUDED.source,
  service_date = EXCLUDED.service_date,
  created_at = EXCLUDED.created_at,
  updated_at = EXCLUDED.updated_at;

INSERT INTO aicrm_picolabbs_leads (id, channel, raw_message, name, contact, stage, vertical, source, service_date, created_at, updated_at)
VALUES (
  '88001003', 'whatsapp',
  $$媽媽膝頭痛，想買 iKnee 俾佢，中環店可唔可以試用？$$,
  '周先生', '+852 9999 8888', 'New', 'picolabbs_hardware_pain', 'picolabbs_returning_seed', NULL,
  NOW() - INTERVAL '14 days', NOW() - INTERVAL '14 days'
)
ON CONFLICT (id) DO UPDATE SET
  channel = EXCLUDED.channel,
  raw_message = EXCLUDED.raw_message,
  name = EXCLUDED.name,
  contact = EXCLUDED.contact,
  stage = EXCLUDED.stage,
  vertical = EXCLUDED.vertical,
  source = EXCLUDED.source,
  service_date = EXCLUDED.service_date,
  created_at = EXCLUDED.created_at,
  updated_at = EXCLUDED.updated_at;

INSERT INTO aicrm_picolabbs_leads (id, channel, raw_message, name, contact, stage, vertical, source, service_date, created_at, updated_at)
VALUES (
  '88001004', 'whatsapp',
  $$上週問過 iKnee，想加多一部 iWand Pro 一齊寄屋企，有冇套裝價？$$,
  '周先生', '+852 9999 8888', 'New', 'picolabbs_hardware_pain', 'picolabbs_returning_seed', NULL,
  NOW() - INTERVAL '2 days', NOW() - INTERVAL '2 days'
)
ON CONFLICT (id) DO UPDATE SET
  channel = EXCLUDED.channel,
  raw_message = EXCLUDED.raw_message,
  name = EXCLUDED.name,
  contact = EXCLUDED.contact,
  stage = EXCLUDED.stage,
  vertical = EXCLUDED.vertical,
  source = EXCLUDED.source,
  service_date = EXCLUDED.service_date,
  created_at = EXCLUDED.created_at,
  updated_at = EXCLUDED.updated_at;

INSERT INTO aicrm_picolabbs_timeline (id, lead_id, event_type, payload, created_at) VALUES
  ('891000001', '88001001', 'whatsapp_paste', '{}', NOW() - INTERVAL '14 days'),
  ('891000002', '88001002', 'whatsapp_paste', '{}', NOW() - INTERVAL '2 days'),
  ('891000003', '88001003', 'whatsapp_paste', '{}', NOW() - INTERVAL '14 days'),
  ('891000004', '88001004', 'whatsapp_paste', '{}', NOW() - INTERVAL '2 days')
ON CONFLICT (id) DO NOTHING;

INSERT INTO aicrm_picolabbs_ai_triage (
  lead_id, vertical, category, subcategory, intent, urgency_score,
  extracted_fields, missing_fields, summary, recommended_actions, safety_escalate
) VALUES
  ('88001001', 'picolabbs_wellness', 'picolabbs_wellness', NULL, 'info', 40, '{}', '[]', '示範：iRelief 門市查詢', '[]', 0),
  ('88001002', 'picolabbs_wellness', 'picolabbs_wellness', NULL, 'info', 40, '{}', '[]', '示範：取貨門市跟進', '[]', 0),
  ('88001003', 'picolabbs_hardware_pain', 'picolabbs_hardware_pain', NULL, 'info', 40, '{}', '[]', '示範：iKnee 試用', '[]', 0),
  ('88001004', 'picolabbs_hardware_pain', 'picolabbs_hardware_pain', NULL, 'info', 40, '{}', '[]', '示範：套裝加購', '[]', 0)
ON CONFLICT (lead_id) DO UPDATE SET
  vertical = EXCLUDED.vertical,
  category = EXCLUDED.category,
  subcategory = EXCLUDED.subcategory,
  intent = EXCLUDED.intent,
  urgency_score = EXCLUDED.urgency_score,
  extracted_fields = EXCLUDED.extracted_fields,
  missing_fields = EXCLUDED.missing_fields,
  summary = EXCLUDED.summary,
  recommended_actions = EXCLUDED.recommended_actions,
  safety_escalate = EXCLUDED.safety_escalate;

COMMIT;
