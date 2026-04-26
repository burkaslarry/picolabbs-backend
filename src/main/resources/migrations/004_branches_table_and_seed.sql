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

ALTER TABLE aicrm_picolabbs_branches ADD COLUMN IF NOT EXISTS region VARCHAR(10) NOT NULL DEFAULT 'hk';
ALTER TABLE aicrm_picolabbs_branches ADD COLUMN IF NOT EXISTS name_zh VARCHAR(255);
ALTER TABLE aicrm_picolabbs_branches ADD COLUMN IF NOT EXISTS name_en VARCHAR(255);
ALTER TABLE aicrm_picolabbs_branches ADD COLUMN IF NOT EXISTS district_zh VARCHAR(255);
ALTER TABLE aicrm_picolabbs_branches ADD COLUMN IF NOT EXISTS district_en VARCHAR(255);
ALTER TABLE aicrm_picolabbs_branches ADD COLUMN IF NOT EXISTS address_zh VARCHAR(1000);
ALTER TABLE aicrm_picolabbs_branches ADD COLUMN IF NOT EXISTS address_en VARCHAR(1000);
ALTER TABLE aicrm_picolabbs_branches ADD COLUMN IF NOT EXISTS whatsapp VARCHAR(64);
ALTER TABLE aicrm_picolabbs_branches ADD COLUMN IF NOT EXISTS hours_zh VARCHAR(255);
ALTER TABLE aicrm_picolabbs_branches ADD COLUMN IF NOT EXISTS hours_en VARCHAR(255);
ALTER TABLE aicrm_picolabbs_branches ADD COLUMN IF NOT EXISTS sort_order INT NOT NULL DEFAULT 0;
CREATE INDEX IF NOT EXISTS idx_branches_region ON aicrm_picolabbs_branches(region);

UPDATE aicrm_picolabbs_branches
SET region = COALESCE(NULLIF(region, ''), 'hk'),
    name_zh = COALESCE(NULLIF(name_zh, ''), name),
    name_en = COALESCE(NULLIF(name_en, ''), name),
    district_zh = COALESCE(NULLIF(district_zh, ''), district),
    district_en = COALESCE(NULLIF(district_en, ''), district),
    address_zh = COALESCE(NULLIF(address_zh, ''), address),
    address_en = COALESCE(NULLIF(address_en, ''), address);

INSERT INTO aicrm_picolabbs_branches
    (id, code, name, district, address, region, name_zh, name_en, district_zh, district_en, address_zh, address_en, phone, whatsapp, hours_zh, hours_en, sort_order)
VALUES
    ('causeway-bay', 'causeway-bay', '銅鑼灣店', '港島區', '香港銅鑼灣勿地臣街1號時代廣場9樓925號舖', 'hk', '銅鑼灣店', 'Causeway Bay', '港島區', 'Hong Kong Island', '香港銅鑼灣勿地臣街1號時代廣場9樓925號舖', 'Shop 925, 9/F, Times Square, 1 Matheson Street, Causeway Bay, Hong Kong', '+852 2152 3968', '85293588333', '上午11:00 - 晚上8:00', '11:00am - 8:00pm', 10),
    ('quarry-bay', 'quarry-bay', '鰂魚涌店', '港島區', '香港鰂魚涌康怡廣場1樓F9A及F9B號舖', 'hk', '鰂魚涌店', 'Quarry Bay', '港島區', 'Hong Kong Island', '香港鰂魚涌康怡廣場1樓F9A及F9B號舖', 'Shop F9A & F9B, 1/F, KornHill Plaza, 1 KornHill Road, Quarry Bay, Hong Kong', '+852 3568 2783', '85292183497', '上午11:00 - 晚上8:00', '11:00am - 8:00pm', 20),
    ('diamond-hill', 'diamond-hill', '鑽石山店', '九龍區', '九龍鑽石山龍蟠街3號荷里活廣場2樓211及295號舖', 'hk', '鑽石山店', 'Diamond Hill', '九龍區', 'Kowloon', '九龍鑽石山龍蟠街3號荷里活廣場2樓211及295號舖', 'Shop 211 and 295, 2/F, Plaza Hollywood, 3 Lung Poon Street, Diamond Hill, Kowloon', '+852 2117 8380', '85269030753', '上午11:00 - 晚上8:00', '11:00am - 8:00pm', 30),
    ('kowloon-bay', 'kowloon-bay', '九龍灣店', '九龍區', '九龍灣德福廣場1期地下G86號舖', 'hk', '九龍灣店', 'Kowloon Bay', '九龍區', 'Kowloon', '九龍灣德福廣場1期地下G86號舖', 'Shop G86, Telford Plaza 1, 33 Wai Yip Street, Kowloon Bay, Kowloon', '+852 9210 4662', '85292104662', '上午11:00 - 晚上8:00', '11:00am - 8:00pm', 40),
    ('mong-kok', 'mong-kok', '旺角店', '九龍區', '旺角MOKO新世紀廣場MTR層M59號舖', 'hk', '旺角店', 'Mong Kok', '九龍區', 'Kowloon', '旺角MOKO新世紀廣場MTR層M59號舖', 'Shop M59, MTR/F, Moko, Mong Kok', '+852 2116 0665', '85294309171', '上午11:00 - 晚上8:00', '11:00am - 8:00pm', 50),
    ('tsuen-wan', 'tsuen-wan', '荃灣店', '新界區', '新界荃灣荃灣廣場4樓407號舖', 'hk', '荃灣店', 'Tsuen Wan', '新界區', 'New Territories', '新界荃灣荃灣廣場4樓407號舖', 'Shop 407, 4/F, Tsuen Wan Plaza, Tsuen Wan, New Territories', '+852 2123 9450', '85293160157', '上午11:00 - 晚上8:00', '11:00am - 8:00pm', 60),
    ('tuen-mun', 'tuen-mun', '屯門店', '新界區', '新界屯門屯順街1號屯門市廣場1期1002-1006號舖', 'hk', '屯門店', 'Tuen Mun', '新界區', 'New Territories', '新界屯門屯順街1號屯門市廣場1期1002-1006號舖', 'Shop 1002-1006, Tuen Mun Town Plaza Phase 1, 1 Tuen Shun Street, Tuen Mun, New Territories', '+852 2123 9342', '85293158757', '上午11:00 - 晚上8:00', '11:00am - 8:00pm', 70),
    ('sha-tin', 'sha-tin', '沙田店', '新界區', '新界沙田新城市廣場1期6樓601號舖', 'hk', '沙田店', 'Sha Tin', '新界區', 'New Territories', '新界沙田新城市廣場1期6樓601號舖', 'Shop 601, 6/F, New Town Plaza Phase 1, Shatin, New Territories', '+852 3188 1081', '85292519546', '上午11:00 - 晚上8:30', '11:00am - 8:30pm', 80),
    ('tseung-kwan-o', 'tseung-kwan-o', '將軍澳店', '新界區', '新界將軍澳唐賢街9號Popcorn商場F04號舖', 'hk', '將軍澳店', 'Tseung Kwan O', '新界區', 'New Territories', '新界將軍澳唐賢街9號Popcorn商場F04號舖', 'Shop F04, Popcorn, 9 Tong Yin Street, Tseung Kwan O', '+852 2368 3300', '85255637365', '上午11:00 - 晚上8:00', '11:00am - 8:00pm', 90)
ON CONFLICT (code) DO UPDATE SET
    id = EXCLUDED.id,
    name = EXCLUDED.name,
    district = EXCLUDED.district,
    address = EXCLUDED.address,
    region = EXCLUDED.region,
    name_zh = EXCLUDED.name_zh,
    name_en = EXCLUDED.name_en,
    district_zh = EXCLUDED.district_zh,
    district_en = EXCLUDED.district_en,
    address_zh = EXCLUDED.address_zh,
    address_en = EXCLUDED.address_en,
    phone = EXCLUDED.phone,
    whatsapp = EXCLUDED.whatsapp,
    hours_zh = EXCLUDED.hours_zh,
    hours_en = EXCLUDED.hours_en,
    sort_order = EXCLUDED.sort_order;
