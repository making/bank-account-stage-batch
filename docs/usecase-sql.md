# 銀行口座ステージ月次バッチ処理のSQLスクリプト

銀行口座ステージ獲得条件の月次バッチ処理シミュレーションに必要なSQLを作成しました。以下のSQLはユースケースごとに分かれており、実行しやすい形式になっています。

## テーブル作成用SQL

まずテーブルを作成するために必要なSQLです：

```sql
-- Create enums
CREATE TYPE stage_code_enum AS ENUM ('NONE', 'SILVER', 'GOLD', 'PLATINUM');
CREATE TYPE condition_type_enum AS ENUM ('TOTAL_BALANCE', 'MONTHLY_FOREIGN_CURRENCY_PURCHASE', 'MONTHLY_INVESTMENT_TRUST_PURCHASE', 'COMBINED_BALANCE_GOLD', 'COMBINED_BALANCE_PLATINUM', 'HOUSING_LOAN', 'FX_TRADING');
CREATE TYPE condition_category_enum AS ENUM ('STAGE', 'RANK_CHANGE');

-- Create stages table
CREATE TABLE stages (
    stage_code stage_code_enum NOT NULL,
    stage_name VARCHAR(50) NOT NULL, -- Display name of the stage
    stage_order INT NOT NULL, -- Order for ranking stages (0 = lowest, ascending)
    valid_from DATE NOT NULL, -- Date from which this stage definition becomes valid
    valid_to DATE NOT NULL DEFAULT '9999-12-31', -- Date until which this stage definition remains valid
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- Record creation timestamp

    -- Primary Key
    PRIMARY KEY (stage_code, valid_from)
);

-- Create conditions table
CREATE TABLE conditions (
    id SERIAL PRIMARY KEY, -- Auto-increment ID
    condition_type condition_type_enum NOT NULL, -- Type of condition
    condition_name VARCHAR(100) NOT NULL, -- Display name of the condition
    condition_category condition_category_enum NOT NULL, -- Category of condition
    valid_from DATE NOT NULL, -- Date from which this condition becomes valid
    valid_to DATE NOT NULL DEFAULT '9999-12-31', -- Date until which this condition remains valid
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- Record creation timestamp

    -- Ensure unique combination
    CONSTRAINT uq_condition_type_valid UNIQUE (condition_type, valid_from, valid_to)
);

-- Create stage_conditions table
CREATE TABLE stage_conditions (
    id BIGINT PRIMARY KEY, -- Primary key and reference to parent conditions table
    stage_code stage_code_enum NOT NULL, -- Stage code this condition belongs to
    min_value DECIMAL(15,2) NOT NULL DEFAULT 0, -- Minimum value to satisfy condition
    max_value DECIMAL(15,2) NOT NULL DEFAULT 9999999999999.99, -- Maximum value for condition

    -- Foreign key
    CONSTRAINT fk_condition FOREIGN KEY (id) REFERENCES conditions(id)
);

-- Create rank_change_conditions table
CREATE TABLE rank_change_conditions (
    id BIGINT PRIMARY KEY, -- Primary key and reference to parent conditions table
    threshold_value DECIMAL(15,2) NOT NULL, -- Value threshold to satisfy condition
    rank_change_levels INT NOT NULL DEFAULT 1, -- Number of ranks to change when condition is met

    -- Foreign key
    CONSTRAINT fk_condition FOREIGN KEY (id) REFERENCES conditions(id)
);

-- Create customer_stage_calculations table
CREATE TABLE customer_stage_calculations (
    id SERIAL PRIMARY KEY, -- Auto-increment ID for easy reference
    customer_id VARCHAR(50) NOT NULL, -- Unique customer identifier
    calculation_date DATE NOT NULL, -- Date when calculation was performed (typically month-end)
    valid_from DATE NOT NULL, -- First date when stage is valid
    valid_to DATE NOT NULL, -- Last date when stage is valid
    current_stage_code stage_code_enum NOT NULL, -- Customer's current stage
    final_stage_code stage_code_enum NOT NULL, -- Final stage after applying rank changes
    total_balance DECIMAL(15,2) NOT NULL DEFAULT 0, -- Total account balance at month-end
    foreign_currency_balance DECIMAL(15,2) NOT NULL DEFAULT 0, -- Foreign currency deposit balance
    investment_trust_balance DECIMAL(15,2) NOT NULL DEFAULT 0, -- Investment trust balance
    monthly_foreign_currency_purchase DECIMAL(15,2) NOT NULL DEFAULT 0, -- Monthly FX purchase amount
    monthly_investment_trust_purchase DECIMAL(15,2) NOT NULL DEFAULT 0, -- Monthly investment purchase
    housing_loan_balance DECIMAL(15,2) NOT NULL DEFAULT 0, -- Housing loan balance
    monthly_fx_trading_volume INT NOT NULL DEFAULT 0, -- Monthly FX trading volume
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- Record creation timestamp

    -- Ensure unique combination
    CONSTRAINT uq_customer_valid UNIQUE (customer_id, valid_from, valid_to)
);

-- Create condition_evaluation_results table
CREATE TABLE condition_evaluation_results (
    id SERIAL PRIMARY KEY,
    calculation_id BIGINT NOT NULL, -- Reference to customer_stage_calculations
    condition_id BIGINT NOT NULL, -- Reference to conditions table
    is_met BOOLEAN NOT NULL DEFAULT FALSE, -- Whether condition was met (kept for audit purposes)
    evaluated_value DECIMAL(15,2) NOT NULL DEFAULT 0, -- Actual value evaluated for condition
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- Record creation timestamp

    -- Foreign keys
    CONSTRAINT fk_calculation FOREIGN KEY (calculation_id) REFERENCES customer_stage_calculations(id),
    CONSTRAINT fk_condition FOREIGN KEY (condition_id) REFERENCES conditions(id),

    -- Ensure unique combination
    CONSTRAINT uq_calculation_condition UNIQUE (calculation_id, condition_id)
);

-- Create stage_transitions table
CREATE TABLE stage_transitions (
    id SERIAL PRIMARY KEY, -- Auto-increment ID for easy reference
    customer_id VARCHAR(50) NOT NULL, -- Unique customer identifier
    calculation_id BIGINT NOT NULL, -- Reference to customer_stage_calculations
    previous_stage_code stage_code_enum NOT NULL, -- Previous stage code
    current_stage_code stage_code_enum NOT NULL, -- Current stage code
    transition_date DATE NOT NULL, -- Date when transition occurs
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- Record creation timestamp,

    -- Foreign keys
    CONSTRAINT fk_calculation FOREIGN KEY (calculation_id) REFERENCES customer_stage_calculations(id)
);

-- Create index for efficient lookup
CREATE INDEX idx_customer_date ON stage_transitions (customer_id, transition_date);
```

## 初期データ設定用SQL

次に、初期データをテーブルに設定するSQLです：

```sql
-- Initial data for stages table
-- Inserting the basic stage definitions with their order and names
INSERT INTO stages (stage_code, stage_name, stage_order, valid_from) VALUES
('NONE', 'ステージなし', 0.00, '2020-01-01'),
('SILVER', 'シルバー', 100, '2020-01-01'),
('GOLD', 'ゴールド', 200, '2020-01-01'),
('PLATINUM', 'プラチナ', 300, '2020-01-01');

-- Initial data for conditions table
-- First, inserting stage conditions
INSERT INTO conditions (condition_type, condition_name, condition_category, valid_from) VALUES
('TOTAL_BALANCE', '月末の総残高', 'STAGE', '2020-01-01'),
('MONTHLY_FOREIGN_CURRENCY_PURCHASE', '外貨預金の積立購入', 'STAGE', '2020-01-01'),
('MONTHLY_INVESTMENT_TRUST_PURCHASE', '投資信託 積み立てプラン', 'STAGE', '2020-01-01'),
('COMBINED_BALANCE_GOLD', '外貨預金と投資信託の合計残高 (ゴールド)', 'STAGE', '2020-01-01'),
('COMBINED_BALANCE_PLATINUM', '外貨預金と投資信託の合計残高 (プラチナ)', 'STAGE', '2020-01-01'),
('HOUSING_LOAN', '住宅ローン残高', 'RANK_CHANGE', '2020-01-01'),
('FX_TRADING', '外国為替証拠金取引（FX）', 'RANK_CHANGE', '2020-01-01');

-- Link stage conditions to specific stages with their threshold values
-- For Silver stage: any of the three conditions
INSERT INTO stage_conditions (id, stage_code, min_value)
SELECT id, 'SILVER', 3000000
FROM conditions
WHERE condition_type = 'TOTAL_BALANCE'
  AND condition_category = 'STAGE';

INSERT INTO stage_conditions (id, stage_code, min_value)
SELECT id, 'SILVER', 30000
FROM conditions
WHERE condition_type = 'MONTHLY_FOREIGN_CURRENCY_PURCHASE'
  AND condition_category = 'STAGE';

INSERT INTO stage_conditions (id, stage_code, min_value)
SELECT id, 'SILVER', 30000
FROM conditions
WHERE condition_type = 'MONTHLY_INVESTMENT_TRUST_PURCHASE'
  AND condition_category = 'STAGE';

-- For Gold stage: combined balance condition
INSERT INTO stage_conditions (id, stage_code, min_value, max_value)
SELECT id, 'GOLD', 5000000, 10000000
FROM conditions
WHERE condition_type = 'COMBINED_BALANCE_GOLD'
  AND condition_category = 'STAGE';

-- For Platinum stage: combined balance condition
INSERT INTO stage_conditions (id, stage_code, min_value)
SELECT id, 'PLATINUM', 10000000
FROM conditions
WHERE condition_type = 'COMBINED_BALANCE_PLATINUM'
  AND condition_category = 'STAGE';

-- Define the rank change rules
INSERT INTO rank_change_conditions (id, threshold_value, rank_change_levels)
SELECT id, 1, 1
FROM conditions
WHERE condition_type = 'HOUSING_LOAN'
  AND condition_category = 'RANK_CHANGE';

INSERT INTO rank_change_conditions (id, threshold_value, rank_change_levels)
SELECT id, 1000, 1
FROM conditions
WHERE condition_type = 'FX_TRADING'
  AND condition_category = 'RANK_CHANGE';
```

## 入力用CSVファイル

シミュレーションに使用される入力CSVデータは以下の通りです。このデータをファイルとして保存してご利用ください：

```csv
customer_id,current_stage_code,month_end_date,total_balance,foreign_currency_balance,investment_trust_balance,monthly_foreign_currency_purchase,monthly_investment_trust_purchase,housing_loan_balance,monthly_fx_trading_volume
CUS001,NONE,2025-04-30,2500000,0,0,0,0,0,0
CUS002,NONE,2025-04-30,3500000,0,0,0,0,0,0
CUS003,SILVER,2025-04-30,8800000,3000000,3000000,0,0,0,0
CUS004,GOLD,2025-04-30,9000000,1000000,1000000,0,0,0,0
CUS005,PLATINUM,2025-04-30,13000000,4000000,4000000,0,0,0,0
CUS006,NONE,2025-04-30,1000000,0,0,40000,0,0,0
CUS007,NONE,2025-04-30,1000000,0,0,0,40000,0,0
CUS008,SILVER,2025-04-30,13000000,6000000,6000000,0,0,0,0
CUS009,NONE,2025-04-30,1000000,0,0,0,0,10000000,0
CUS010,NONE,2025-04-30,1000000,0,0,0,0,0,1200
CUS011,NONE,2025-04-30,13000000,6000000,6000000,0,0,10000000,1200
```

## 各ユースケースのINSERT SQL文

以下、各ユースケースに対応するINSERT SQL文を提供します。

### 1. 顧客CUS001のデータ処理

```sql
-- Case 1: Customer CUS001 - No stage change (does not meet basic conditions)
-- Insert into customer_stage_calculations table
INSERT INTO customer_stage_calculations (
  customer_id, calculation_date, valid_from, valid_to,
  current_stage_code, final_stage_code, total_balance,
  foreign_currency_balance, investment_trust_balance,
  monthly_foreign_currency_purchase, monthly_investment_trust_purchase,
  housing_loan_balance, monthly_fx_trading_volume, created_at
) VALUES (
  'CUS001', '2025-04-30', '2025-05-01', '2025-05-31',
  'NONE', 'NONE', 2500000.00,
  0.00, 0.00,
  0.00, 0.00,
  0.00, 0, '2025-04-30 12:00:00'
);

-- Set calculated ID for condition evaluation (usually auto-generated)
DO $$
DECLARE
  calc_id INT;
BEGIN
  SELECT id INTO calc_id FROM customer_stage_calculations WHERE customer_id = 'CUS001' AND calculation_date = '2025-04-30';

  -- Insert into condition_evaluation_results table
  INSERT INTO condition_evaluation_results (
    calculation_id, condition_id, is_met, evaluated_value, created_at
  ) VALUES
    (calc_id, 1, false, 2500000.00, '2025-04-30 12:00:00'),
    (calc_id, 2, false, 0.00, '2025-04-30 12:00:00'),
    (calc_id, 3, false, 0.00, '2025-04-30 12:00:00'),
    (calc_id, 4, false, 0.00, '2025-04-30 12:00:00'),
    (calc_id, 5, false, 0.00, '2025-04-30 12:00:00'),
    (calc_id, 6, false, 0.00, '2025-04-30 12:00:00'),
    (calc_id, 7, false, 0.00, '2025-04-30 12:00:00');

  -- No stage transition for this customer
END $$;
```

### 2. 顧客CUS002のデータ処理

```sql
-- Case 2: Customer CUS002 - Acquiring Silver Stage based on total balance
-- Insert into customer_stage_calculations table
INSERT INTO customer_stage_calculations (
  customer_id, calculation_date, valid_from, valid_to,
  current_stage_code, final_stage_code, total_balance,
  foreign_currency_balance, investment_trust_balance,
  monthly_foreign_currency_purchase, monthly_investment_trust_purchase,
  housing_loan_balance, monthly_fx_trading_volume, created_at
) VALUES (
  'CUS002', '2025-04-30', '2025-05-01', '2025-05-31',
  'NONE', 'SILVER', 3500000.00,
  0.00, 0.00,
  0.00, 0.00,
  0.00, 0, '2025-04-30 12:00:00'
);

-- Set calculated ID for condition evaluation
DO $$
DECLARE
  calc_id INT;
BEGIN
  SELECT id INTO calc_id FROM customer_stage_calculations WHERE customer_id = 'CUS002' AND calculation_date = '2025-04-30';

  -- Insert into condition_evaluation_results table
  INSERT INTO condition_evaluation_results (
    calculation_id, condition_id, is_met, evaluated_value, created_at
  ) VALUES
    (calc_id, 1, true, 3500000.00, '2025-04-30 12:00:00'),
    (calc_id, 2, false, 0.00, '2025-04-30 12:00:00'),
    (calc_id, 3, false, 0.00, '2025-04-30 12:00:00'),
    (calc_id, 4, false, 0.00, '2025-04-30 12:00:00'),
    (calc_id, 5, false, 0.00, '2025-04-30 12:00:00'),
    (calc_id, 6, false, 0.00, '2025-04-30 12:00:00'),
    (calc_id, 7, false, 0.00, '2025-04-30 12:00:00');

  -- Insert stage transition
  INSERT INTO stage_transitions (
    customer_id, calculation_id, previous_stage_code, current_stage_code,
    transition_date, created_at
  ) VALUES (
    'CUS002', calc_id, 'NONE', 'SILVER',
    '2025-05-01', '2025-04-30 12:00:00'
  );
END $$;
```

### 3. 顧客CUS003のデータ処理

```sql
-- Case 3: Customer CUS003 - Upgrading from Silver to Gold based on combined investment balance
-- Insert into customer_stage_calculations table
INSERT INTO customer_stage_calculations (
  customer_id, calculation_date, valid_from, valid_to,
  current_stage_code, final_stage_code, total_balance,
  foreign_currency_balance, investment_trust_balance,
  monthly_foreign_currency_purchase, monthly_investment_trust_purchase,
  housing_loan_balance, monthly_fx_trading_volume, created_at
) VALUES (
  'CUS003', '2025-04-30', '2025-05-01', '2025-05-31',
  'SILVER', 'GOLD', 8800000.00,
  3000000.00, 3000000.00,
  0.00, 0.00,
  0.00, 0, '2025-04-30 12:00:00'
);

-- Set calculated ID for condition evaluation
DO $$
DECLARE
  calc_id INT;
BEGIN
  SELECT id INTO calc_id FROM customer_stage_calculations WHERE customer_id = 'CUS003' AND calculation_date = '2025-04-30';

  -- Insert into condition_evaluation_results table
  INSERT INTO condition_evaluation_results (
    calculation_id, condition_id, is_met, evaluated_value, created_at
  ) VALUES
    (calc_id, 1, true, 8800000.00, '2025-04-30 12:00:00'),
    (calc_id, 2, false, 0.00, '2025-04-30 12:00:00'),
    (calc_id, 3, false, 0.00, '2025-04-30 12:00:00'),
    (calc_id, 4, true, 6000000.00, '2025-04-30 12:00:00'),
    (calc_id, 5, false, 6000000.00, '2025-04-30 12:00:00'),
    (calc_id, 6, false, 0.00, '2025-04-30 12:00:00'),
    (calc_id, 7, false, 0.00, '2025-04-30 12:00:00');

  -- Insert stage transition
  INSERT INTO stage_transitions (
    customer_id, calculation_id, previous_stage_code, current_stage_code,
    transition_date, created_at
  ) VALUES (
    'CUS003', calc_id, 'SILVER', 'GOLD',
    '2025-05-01', '2025-04-30 12:00:00'
  );
END $$;
```

### 4. 顧客CUS004のデータ処理

```sql
-- Case 4: Customer CUS004 - Downgrading from Gold to Silver due to decreased investment balance
-- Insert into customer_stage_calculations table
INSERT INTO customer_stage_calculations (
  customer_id, calculation_date, valid_from, valid_to,
  current_stage_code, final_stage_code, total_balance,
  foreign_currency_balance, investment_trust_balance,
  monthly_foreign_currency_purchase, monthly_investment_trust_purchase,
  housing_loan_balance, monthly_fx_trading_volume, created_at
) VALUES (
  'CUS004', '2025-04-30', '2025-05-01', '2025-05-31',
  'GOLD', 'SILVER', 9000000.00,
  1000000.00, 1000000.00,
  0.00, 0.00,
  0.00, 0, '2025-04-30 12:00:00'
);

-- Set calculated ID for condition evaluation
DO $$
DECLARE
  calc_id INT;
BEGIN
  SELECT id INTO calc_id FROM customer_stage_calculations WHERE customer_id = 'CUS004' AND calculation_date = '2025-04-30';

  -- Insert into condition_evaluation_results table
  INSERT INTO condition_evaluation_results (
    calculation_id, condition_id, is_met, evaluated_value, created_at
  ) VALUES
    (calc_id, 1, true, 9000000.00, '2025-04-30 12:00:00'),
    (calc_id, 2, false, 0.00, '2025-04-30 12:00:00'),
    (calc_id, 3, false, 0.00, '2025-04-30 12:00:00'),
    (calc_id, 4, false, 2000000.00, '2025-04-30 12:00:00'),
    (calc_id, 5, false, 2000000.00, '2025-04-30 12:00:00'),
    (calc_id, 6, false, 0.00, '2025-04-30 12:00:00'),
    (calc_id, 7, false, 0.00, '2025-04-30 12:00:00');

  -- Insert stage transition
  INSERT INTO stage_transitions (
    customer_id, calculation_id, previous_stage_code, current_stage_code,
    transition_date, created_at
  ) VALUES (
    'CUS004', calc_id, 'GOLD', 'SILVER',
    '2025-05-01', '2025-04-30 12:00:00'
  );
END $$;
```

### 5. 顧客CUS005のデータ処理

```sql
-- Case 5: Customer CUS005 - Downgrading from Platinum to Gold due to decreased investment balance
-- Insert into customer_stage_calculations table
INSERT INTO customer_stage_calculations (
  customer_id, calculation_date, valid_from, valid_to,
  current_stage_code, final_stage_code, total_balance,
  foreign_currency_balance, investment_trust_balance,
  monthly_foreign_currency_purchase, monthly_investment_trust_purchase,
  housing_loan_balance, monthly_fx_trading_volume, created_at
) VALUES (
  'CUS005', '2025-04-30', '2025-05-01', '2025-05-31',
  'PLATINUM', 'GOLD', 13000000.00,
  4000000.00, 4000000.00,
  0.00, 0.00,
  0.00, 0, '2025-04-30 12:00:00'
);

-- Set calculated ID for condition evaluation
DO $$
DECLARE
  calc_id INT;
BEGIN
  SELECT id INTO calc_id FROM customer_stage_calculations WHERE customer_id = 'CUS005' AND calculation_date = '2025-04-30';

  -- Insert into condition_evaluation_results table
  INSERT INTO condition_evaluation_results (
    calculation_id, condition_id, is_met, evaluated_value, created_at
  ) VALUES
    (calc_id, 1, true, 13000000.00, '2025-04-30 12:00:00'),
    (calc_id, 2, false, 0.00, '2025-04-30 12:00:00'),
    (calc_id, 3, false, 0.00, '2025-04-30 12:00:00'),
    (calc_id, 4, true, 8000000.00, '2025-04-30 12:00:00'),
    (calc_id, 5, false, 8000000.00, '2025-04-30 12:00:00'),
    (calc_id, 6, false, 0.00, '2025-04-30 12:00:00'),
    (calc_id, 7, false, 0.00, '2025-04-30 12:00:00');

  -- Insert stage transition
  INSERT INTO stage_transitions (
    customer_id, calculation_id, previous_stage_code, current_stage_code,
    transition_date, created_at
  ) VALUES (
    'CUS005', calc_id, 'PLATINUM', 'GOLD',
    '2025-05-01', '2025-04-30 12:00:00'
  );
END $$;
```

### 6. 顧客CUS006のデータ処理

```sql
-- Case 6: Customer CUS006 - Acquiring Silver Stage based on monthly foreign currency purchase
-- Insert into customer_stage_calculations table
INSERT INTO customer_stage_calculations (
  customer_id, calculation_date, valid_from, valid_to,
  current_stage_code, final_stage_code, total_balance,
  foreign_currency_balance, investment_trust_balance,
  monthly_foreign_currency_purchase, monthly_investment_trust_purchase,
  housing_loan_balance, monthly_fx_trading_volume, created_at
) VALUES (
  'CUS006', '2025-04-30', '2025-05-01', '2025-05-31',
  'NONE', 'SILVER', 1000000.00,
  0.00, 0.00,
  40000.00, 0.00,
  0.00, 0, '2025-04-30 12:00:00'
);

-- Set calculated ID for condition evaluation
DO $$
DECLARE
  calc_id INT;
BEGIN
  SELECT id INTO calc_id FROM customer_stage_calculations WHERE customer_id = 'CUS006' AND calculation_date = '2025-04-30';

  -- Insert into condition_evaluation_results table
  INSERT INTO condition_evaluation_results (
    calculation_id, condition_id, is_met, evaluated_value, created_at
  ) VALUES
    (calc_id, 1, false, 1000000.00, '2025-04-30 12:00:00'),
    (calc_id, 2, true, 40000.00, '2025-04-30 12:00:00'),
    (calc_id, 3, false, 0.00, '2025-04-30 12:00:00'),
    (calc_id, 4, false, 0.00, '2025-04-30 12:00:00'),
    (calc_id, 5, false, 0.00, '2025-04-30 12:00:00'),
    (calc_id, 6, false, 0.00, '2025-04-30 12:00:00'),
    (calc_id, 7, false, 0.00, '2025-04-30 12:00:00');

  -- Insert stage transition
  INSERT INTO stage_transitions (
    customer_id, calculation_id, previous_stage_code, current_stage_code,
    transition_date, created_at
  ) VALUES (
    'CUS006', calc_id, 'NONE', 'SILVER',
    '2025-05-01', '2025-04-30 12:00:00'
  );
END $$;
```

### 7. 顧客CUS007のデータ処理

```sql
-- Case 7: Customer CUS007 - Acquiring Silver Stage based on monthly investment trust purchase
-- Insert into customer_stage_calculations table
INSERT INTO customer_stage_calculations (
  customer_id, calculation_date, valid_from, valid_to,
  current_stage_code, final_stage_code, total_balance,
  foreign_currency_balance, investment_trust_balance,
  monthly_foreign_currency_purchase, monthly_investment_trust_purchase,
  housing_loan_balance, monthly_fx_trading_volume, created_at
) VALUES (
  'CUS007', '2025-04-30', '2025-05-01', '2025-05-31',
  'NONE', 'SILVER', 1000000.00,
  0.00, 0.00,
  0.00, 40000.00,
  0.00, 0, '2025-04-30 12:00:00'
);

-- Set calculated ID for condition evaluation
DO $$
DECLARE
  calc_id INT;
BEGIN
  SELECT id INTO calc_id FROM customer_stage_calculations WHERE customer_id = 'CUS007' AND calculation_date = '2025-04-30';

  -- Insert into condition_evaluation_results table
  INSERT INTO condition_evaluation_results (
    calculation_id, condition_id, is_met, evaluated_value, created_at
  ) VALUES
    (calc_id, 1, false, 1000000.00, '2025-04-30 12:00:00'),
    (calc_id, 2, false, 0.00, '2025-04-30 12:00:00'),
    (calc_id, 3, true, 40000.00, '2025-04-30 12:00:00'),
    (calc_id, 4, false, 0.00, '2025-04-30 12:00:00'),
    (calc_id, 5, false, 0.00, '2025-04-30 12:00:00'),
    (calc_id, 6, false, 0.00, '2025-04-30 12:00:00'),
    (calc_id, 7, false, 0.00, '2025-04-30 12:00:00');

  -- Insert stage transition
  INSERT INTO stage_transitions (
    customer_id, calculation_id, previous_stage_code, current_stage_code,
    transition_date, created_at
  ) VALUES (
    'CUS007', calc_id, 'NONE', 'SILVER',
    '2025-05-01', '2025-04-30 12:00:00'
  );
END $$;
```

### 8. 顧客CUS008のデータ処理

```sql
-- Case 8: Customer CUS008 - Upgrading from Silver to Platinum based on high combined investment balance
-- Insert into customer_stage_calculations table
INSERT INTO customer_stage_calculations (
  customer_id, calculation_date, valid_from, valid_to,
  current_stage_code, final_stage_code, total_balance,
  foreign_currency_balance, investment_trust_balance,
  monthly_foreign_currency_purchase, monthly_investment_trust_purchase,
  housing_loan_balance, monthly_fx_trading_volume, created_at
) VALUES (
  'CUS008', '2025-04-30', '2025-05-01', '2025-05-31',
  'SILVER', 'PLATINUM', 13000000.00,
  6000000.00, 6000000.00,
  0.00, 0.00,
  0.00, 0, '2025-04-30 12:00:00'
);

-- Set calculated ID for condition evaluation
DO $$
DECLARE
  calc_id INT;
BEGIN
  SELECT id INTO calc_id FROM customer_stage_calculations WHERE customer_id = 'CUS008' AND calculation_date = '2025-04-30';

  -- Insert into condition_evaluation_results table
  INSERT INTO condition_evaluation_results (
    calculation_id, condition_id, is_met, evaluated_value, created_at
  ) VALUES
    (calc_id, 1, true, 13000000.00, '2025-04-30 12:00:00'),
    (calc_id, 2, false, 0.00, '2025-04-30 12:00:00'),
    (calc_id, 3, false, 0.00, '2025-04-30 12:00:00'),
    (calc_id, 4, false, 12000000.00, '2025-04-30 12:00:00'),
    (calc_id, 5, true, 12000000.00, '2025-04-30 12:00:00'),
    (calc_id, 6, false, 0.00, '2025-04-30 12:00:00'),
    (calc_id, 7, false, 0.00, '2025-04-30 12:00:00');

  -- Insert stage transition
  INSERT INTO stage_transitions (
    customer_id, calculation_id, previous_stage_code, current_stage_code,
    transition_date, created_at
  ) VALUES (
    'CUS008', calc_id, 'SILVER', 'PLATINUM',
    '2025-05-01', '2025-04-30 12:00:00'
  );
END $$;
```

### 9. 顧客CUS009のデータ処理

```sql
-- Case 9: Customer CUS009 - Rank-up due to housing loan
-- Insert into customer_stage_calculations table
INSERT INTO customer_stage_calculations (
  customer_id, calculation_date, valid_from, valid_to,
  current_stage_code, final_stage_code, total_balance,
  foreign_currency_balance, investment_trust_balance,
  monthly_foreign_currency_purchase, monthly_investment_trust_purchase,
  housing_loan_balance, monthly_fx_trading_volume, created_at
) VALUES (
  'CUS009', '2025-04-30', '2025-05-01', '2025-05-31',
  'NONE', 'SILVER', 1000000.00,
  0.00, 0.00,
  0.00, 0.00,
  10000000.00, 0, '2025-04-30 12:00:00'
);

-- Set calculated ID for condition evaluation
DO $$
DECLARE
  calc_id INT;
BEGIN
  SELECT id INTO calc_id FROM customer_stage_calculations WHERE customer_id = 'CUS009' AND calculation_date = '2025-04-30';

  -- Insert into condition_evaluation_results table
  INSERT INTO condition_evaluation_results (
    calculation_id, condition_id, is_met, evaluated_value, created_at
  ) VALUES
    (calc_id, 1, false, 1000000.00, '2025-04-30 12:00:00'),
    (calc_id, 2, false, 0.00, '2025-04-30 12:00:00'),
    (calc_id, 3, false, 0.00, '2025-04-30 12:00:00'),
    (calc_id, 4, false, 0.00, '2025-04-30 12:00:00'),
    (calc_id, 5, false, 0.00, '2025-04-30 12:00:00'),
    (calc_id, 6, true, 10000000.00, '2025-04-30 12:00:00'),
    (calc_id, 7, false, 0.00, '2025-04-30 12:00:00');

  -- Insert stage transition
  INSERT INTO stage_transitions (
    customer_id, calculation_id, previous_stage_code, current_stage_code,
    transition_date, created_at
  ) VALUES (
    'CUS009', calc_id, 'NONE', 'SILVER',
    '2025-05-01', '2025-04-30 12:00:00'
  );
END $$;
```

### 10. 顧客CUS010のデータ処理

```sql
-- Case 10: Customer CUS010 - Rank-up due to FX trading volume
-- Insert into customer_stage_calculations table
INSERT INTO customer_stage_calculations (
  customer_id, calculation_date, valid_from, valid_to,
  current_stage_code, final_stage_code, total_balance,
  foreign_currency_balance, investment_trust_balance,
  monthly_foreign_currency_purchase, monthly_investment_trust_purchase,
  housing_loan_balance, monthly_fx_trading_volume, created_at
) VALUES (
  'CUS010', '2025-04-30', '2025-05-01', '2025-05-31',
  'NONE', 'SILVER', 1000000.00,
  0.00, 0.00,
  0.00, 0.00,
  0.00, 1200, '2025-04-30 12:00:00'
);

-- Set calculated ID for condition evaluation
DO $$
DECLARE
  calc_id INT;
BEGIN
  SELECT id INTO calc_id FROM customer_stage_calculations WHERE customer_id = 'CUS010' AND calculation_date = '2025-04-30';

  -- Insert into condition_evaluation_results table
  INSERT INTO condition_evaluation_results (
    calculation_id, condition_id, is_met, evaluated_value, created_at
  ) VALUES
    (calc_id, 1, false, 1000000.00, '2025-04-30 12:00:00'),
    (calc_id, 2, false, 0.00, '2025-04-30 12:00:00'),
    (calc_id, 3, false, 0.00, '2025-04-30 12:00:00'),
    (calc_id, 4, false, 0.00, '2025-04-30 12:00:00'),
    (calc_id, 5, false, 0.00, '2025-04-30 12:00:00'),
    (calc_id, 6, false, 0.00, '2025-04-30 12:00:00'),
    (calc_id, 7, true, 1200, '2025-04-30 12:00:00');

  -- Insert stage transition
  INSERT INTO stage_transitions (
    customer_id, calculation_id, previous_stage_code, current_stage_code,
    transition_date, created_at
  ) VALUES (
    'CUS010', calc_id, 'NONE', 'SILVER',
    '2025-05-01', '2025-04-30 12:00:00'
  );
END $$;
```

### 11. 顧客CUS011のデータ処理

```sql
-- Case 11: Customer CUS011 - Multiple conditions leading to double rank-up
-- Insert into customer_stage_calculations table
INSERT INTO customer_stage_calculations (
  customer_id, calculation_date, valid_from, valid_to,
  current_stage_code, final_stage_code, total_balance,
  foreign_currency_balance, investment_trust_balance,
  monthly_foreign_currency_purchase, monthly_investment_trust_purchase,
  housing_loan_balance, monthly_fx_trading_volume, created_at
) VALUES (
  'CUS011', '2025-04-30', '2025-05-01', '2025-05-31',
  'NONE', 'PLATINUM', 13000000.00,
  6000000.00, 6000000.00,
  0.00, 0.00,
  10000000.00, 1200, '2025-04-30 12:00:00'
);

-- Set calculated ID for condition evaluation
DO $$
DECLARE
  calc_id INT;
BEGIN
  SELECT id INTO calc_id FROM customer_stage_calculations WHERE customer_id = 'CUS011' AND calculation_date = '2025-04-30';

  -- Insert into condition_evaluation_results table
  INSERT INTO condition_evaluation_results (
    calculation_id, condition_id, is_met, evaluated_value, created_at
  ) VALUES
    (calc_id, 1, true, 13000000.00, '2025-04-30 12:00:00'),
    (calc_id, 2, false, 0.00, '2025-04-30 12:00:00'),
    (calc_id, 3, false, 0.00, '2025-04-30 12:00:00'),
    (calc_id, 4, false, 12000000.00, '2025-04-30 12:00:00'),
    (calc_id, 5, true, 12000000.00, '2025-04-30 12:00:00'),
    (calc_id, 6, true, 10000000.00, '2025-04-30 12:00:00'),
    (calc_id, 7, true, 1200, '2025-04-30 12:00:00');

  -- Insert stage transition
  INSERT INTO stage_transitions (
    customer_id, calculation_id, previous_stage_code, current_stage_code,
    transition_date, created_at
  ) VALUES (
    'CUS011', calc_id, 'NONE', 'PLATINUM',
    '2025-05-01', '2025-04-30 12:00:00'
  );
END $$;
```

## 全ケースをまとめて実行するSQL

以下のSQLを使用して、すべてのユースケースをまとめて実行することもできます：

```sql
-- First drop existing tables if they exist
DROP TABLE IF EXISTS stage_transitions;
DROP TABLE IF EXISTS condition_evaluation_results;
DROP TABLE IF EXISTS customer_stage_calculations;
DROP TABLE IF EXISTS rank_change_conditions;
DROP TABLE IF EXISTS stage_conditions;
DROP TABLE IF EXISTS conditions;
DROP TABLE IF EXISTS stages;
DROP TYPE IF EXISTS condition_category_enum;
DROP TYPE IF EXISTS condition_type_enum;
DROP TYPE IF EXISTS stage_code_enum;

-- Create types and tables with initial data
-- ... (テーブル作成とマスターデータのSQLをここに挿入)

-- Now execute all customer cases in sequence
BEGIN;

-- ... (すべてのユースケースのINSERT SQLをここに挿入)

COMMIT;
```

## 結果確認用SQL

処理結果を確認するためのSQL例：

```sql
-- View all customer stage calculations
SELECT
  c.customer_id,
  c.current_stage_code,
  c.final_stage_code,
  c.total_balance,
  c.foreign_currency_balance,
  c.investment_trust_balance,
  c.monthly_foreign_currency_purchase,
  c.monthly_investment_trust_purchase,
  c.housing_loan_balance,
  c.monthly_fx_trading_volume
FROM
  customer_stage_calculations c
ORDER BY
  c.customer_id;

-- View all stage transitions
SELECT
  t.customer_id,
  t.previous_stage_code,
  t.current_stage_code,
  t.transition_date
FROM
  stage_transitions t
ORDER BY
  t.customer_id;

-- View all condition evaluations for a specific customer
SELECT
  cond.condition_name,
  er.is_met,
  er.evaluated_value
FROM
  condition_evaluation_results er
JOIN
  conditions cond ON er.condition_id = cond.id
JOIN
  customer_stage_calculations csc ON er.calculation_id = csc.id
WHERE
  csc.customer_id = 'CUS011'  -- Replace with desired customer_id
ORDER BY
  cond.condition_type;
```

以上のSQLを使用して、銀行口座ステージ獲得条件の月次バッチ処理シミュレーションを実行し、結果を確認することができます。それぞれのユースケースに対応するSQLは個別に実行可能な形式になっています。
