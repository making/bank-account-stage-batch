-- Initial data for stages table
-- Inserting the basic stage definitions with their order and names
INSERT INTO stages (stage_code, stage_name, stage_order, valid_from)
VALUES ('NONE', 'ステージなし', 0, '2020-01-01'),
    ('SILVER', 'シルバー', 100, '2020-01-01'),
    ('GOLD', 'ゴールド', 200, '2020-01-01'),
    ('PLATINUM', 'プラチナ', 300, '2020-01-01');

-- Initial data for conditions table
-- First, inserting stage conditions
INSERT INTO conditions (condition_type, condition_name, condition_category, valid_from)
VALUES ('TOTAL_BALANCE', '月末の総残高', 'STAGE', '2020-01-01'),
    ('FOREIGN_CURRENCY_PURCHASE', '外貨預金の積立購入', 'STAGE', '2020-01-01'),
    ('INVESTMENT_TRUST_PURCHASE', '投資信託 積み立てプラン', 'STAGE', '2020-01-01'),
    ('COMBINED_BALANCE_GOLD', '外貨預金と投資信託の合計残高 (ゴールド)', 'STAGE', '2020-01-01'),
    ('COMBINED_BALANCE_PLATINUM', '外貨預金と投資信託の合計残高 (プラチナ)', 'STAGE', '2020-01-01'),
    ('HOUSING_LOAN', '住宅ローン残高', 'RANK_CHANGE', '2020-01-01'),
    ('FX_TRADING', '外国為替証拠金取引（FX）', 'RANK_CHANGE', '2020-01-01');

-- Link stage conditions to specific stages with their threshold values
-- For Silver stage: any of the three conditions
INSERT INTO stage_conditions (id, stage_code, min_value)
SELECT id, 'SILVER', 3000000
FROM conditions
WHERE condition_type = 'TOTAL_BALANCE' AND condition_category = 'STAGE';

INSERT INTO stage_conditions (id, stage_code, min_value)
SELECT id, 'SILVER', 30000
FROM conditions
WHERE condition_type = 'FOREIGN_CURRENCY_PURCHASE' AND condition_category = 'STAGE';

INSERT INTO stage_conditions (id, stage_code, min_value)
SELECT id, 'SILVER', 30000
FROM conditions
WHERE condition_type = 'INVESTMENT_TRUST_PURCHASE' AND condition_category = 'STAGE';

-- For Gold stage: combined balance condition
INSERT INTO stage_conditions (id, stage_code, min_value, max_value)
SELECT id, 'GOLD', 5000000, 10000000
FROM conditions
WHERE condition_type = 'COMBINED_BALANCE_GOLD' AND condition_category = 'STAGE';

-- For Platinum stage: combined balance condition
INSERT INTO stage_conditions (id, stage_code, min_value)
SELECT id, 'PLATINUM', 10000000
FROM conditions
WHERE condition_type = 'COMBINED_BALANCE_PLATINUM' AND condition_category = 'STAGE';

-- Define the rank change rules
INSERT INTO rank_change_conditions (id, threshold_value, rank_change_levels)
SELECT id, 1, 1
FROM conditions
WHERE condition_type = 'HOUSING_LOAN' AND condition_category = 'RANK_CHANGE';

INSERT INTO rank_change_conditions (id, threshold_value, rank_change_levels)
SELECT id, 1000, 1
FROM conditions
WHERE condition_type = 'FX_TRADING' AND condition_category = 'RANK_CHANGE';
