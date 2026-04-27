-- Align returning-customer demo rows (+852 6111 2222, +852 9999 8888; ids 88001001–88001004).
-- Prefer HTTP (no SQL): curl -sS -X POST "$API/api/demo/seed-returning-customers" \
--   -H "Content-Type: application/json" \
--   -d '{"key":"picolabbs-prototype-returning-seed-v1"}'
-- Or set PICOLABBS_DEMO_SEED_KEY on the server and use that value as "key".

UPDATE aicrm_picolabbs_leads
SET contact = '+852 6111 2222', name = '陳小姐', vertical = 'picolabbs_wellness', updated_at = CURRENT_TIMESTAMP
WHERE id = '88001001';

UPDATE aicrm_picolabbs_leads
SET contact = '+852 6111 2222', name = '陳小姐', vertical = 'picolabbs_wellness', updated_at = CURRENT_TIMESTAMP
WHERE id = '88001002';

UPDATE aicrm_picolabbs_leads
SET contact = '+852 9999 8888', name = '周先生', vertical = 'picolabbs_hardware_pain', updated_at = CURRENT_TIMESTAMP
WHERE id = '88001003';

UPDATE aicrm_picolabbs_leads
SET contact = '+852 9999 8888', name = '周先生', vertical = 'picolabbs_hardware_pain', updated_at = CURRENT_TIMESTAMP
WHERE id = '88001004';
