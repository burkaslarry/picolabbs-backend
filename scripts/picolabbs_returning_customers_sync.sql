-- Align returning-customer demo rows with app seed (PicoLabbReturningCustomerSeed).
-- Run on Postgres if deploy cannot reach DB from local; ids are fixed numeric strings.
-- Inserts are normally done on first boot; this file is for contact/vertical sync only.

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
