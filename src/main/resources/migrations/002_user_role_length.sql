-- Widen role for ops_front, ops_back, ops_reminder slugs (run once on existing DBs).
ALTER TABLE aicrm_picolabbs_user ALTER COLUMN role TYPE VARCHAR(32);
