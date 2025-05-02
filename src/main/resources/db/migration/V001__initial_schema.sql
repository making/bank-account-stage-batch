-- Create enums
CREATE TYPE stage_code_enum AS ENUM ('NONE', 'SILVER', 'GOLD', 'PLATINUM');
CREATE TYPE condition_type_enum AS ENUM ('TOTAL_BALANCE', 'FOREIGN_CURRENCY_PURCHASE', 'INVESTMENT_TRUST_PURCHASE', 'COMBINED_BALANCE_GOLD', 'COMBINED_BALANCE_PLATINUM', 'HOUSING_LOAN', 'FX_TRADING');
CREATE TYPE condition_category_enum AS ENUM ('STAGE', 'RANK_CHANGE');

-- Create stages table
CREATE TABLE stages (
    stage_code stage_code_enum NOT NULL,
    stage_name VARCHAR(50) NOT NULL,                         -- Display name of the stage
    stage_order INT NOT NULL,                                -- Order for ranking stages (0 = lowest, ascending)
    valid_from DATE NOT NULL,                                -- Date from which this stage definition becomes valid
    valid_to DATE NOT NULL DEFAULT '9999-12-31',             -- Date until which this stage definition remains valid
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- Record creation timestamp

    -- Primary Key
    PRIMARY KEY (stage_code, valid_from)
);

-- Create conditions table
CREATE TABLE conditions (
    id SERIAL PRIMARY KEY,                                   -- Auto-increment ID
    condition_type condition_type_enum NOT NULL,             -- Type of condition
    condition_name VARCHAR(100) NOT NULL,                    -- Display name of the condition
    condition_category condition_category_enum NOT NULL,     -- Category of condition
    valid_from DATE NOT NULL,                                -- Date from which this condition becomes valid
    valid_to DATE NOT NULL DEFAULT '9999-12-31',             -- Date until which this condition remains valid
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- Record creation timestamp

    -- Ensure unique combination
    CONSTRAINT uq_condition_type_valid UNIQUE (condition_type, valid_from, valid_to)
);

-- Create stage_conditions table
CREATE TABLE stage_conditions (
    id BIGINT PRIMARY KEY,                                      -- Primary key and reference to parent conditions table
    stage_code stage_code_enum NOT NULL,                        -- Stage code this condition belongs to
    min_value DECIMAL(15, 2) NOT NULL DEFAULT 0,                -- Minimum value to satisfy condition
    max_value DECIMAL(15, 2) NOT NULL DEFAULT 9999999999999.99, -- Maximum value for condition

    -- Foreign key
    CONSTRAINT fk_condition FOREIGN KEY (id) REFERENCES conditions(id)
);

-- Create rank_change_conditions table
CREATE TABLE rank_change_conditions (
    id BIGINT PRIMARY KEY,                     -- Primary key and reference to parent conditions table
    threshold_value DECIMAL(15, 2) NOT NULL,   -- Value threshold to satisfy condition
    rank_change_levels INT NOT NULL DEFAULT 1, -- Number of ranks to change when condition is met

    -- Foreign key
    CONSTRAINT fk_condition FOREIGN KEY (id) REFERENCES conditions(id)
);

-- Create customer_stage_calculations table
CREATE TABLE customer_stage_calculations (
    id SERIAL PRIMARY KEY,                                               -- Auto-increment ID for easy reference
    customer_id VARCHAR(50) NOT NULL,                                    -- Unique customer identifier
    calculation_date DATE NOT NULL,                                      -- Date when calculation was performed (typically month-end)
    valid_from DATE NOT NULL,                                            -- First date when stage is valid
    valid_to DATE NOT NULL,                                              -- Last date when stage is valid
    current_stage_code stage_code_enum NOT NULL,                         -- Customer's current stage
    final_stage_code stage_code_enum NOT NULL,                           -- Final stage after applying rank changes
    total_balance DECIMAL(15, 2) NOT NULL DEFAULT 0,                     -- Total account balance at month-end
    foreign_currency_balance DECIMAL(15, 2) NOT NULL DEFAULT 0,          -- Foreign currency deposit balance
    investment_trust_balance DECIMAL(15, 2) NOT NULL DEFAULT 0,          -- Investment trust balance
    monthly_foreign_currency_purchase DECIMAL(15, 2) NOT NULL DEFAULT 0, -- Monthly FX purchase amount
    monthly_investment_trust_purchase DECIMAL(15, 2) NOT NULL DEFAULT 0, -- Monthly investment purchase
    housing_loan_balance DECIMAL(15, 2) NOT NULL DEFAULT 0,              -- Housing loan balance
    monthly_fx_trading_volume INT NOT NULL DEFAULT 0,                    -- Monthly FX trading volume
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,             -- Record creation timestamp

    -- Ensure unique combination
    CONSTRAINT uq_customer_valid UNIQUE (customer_id, valid_from, valid_to)
);

-- Create condition_evaluation_results table
CREATE TABLE condition_evaluation_results (
    id SERIAL PRIMARY KEY,
    calculation_id BIGINT NOT NULL,                          -- Reference to customer_stage_calculations
    condition_id BIGINT NOT NULL,                            -- Reference to conditions table
    is_met BOOLEAN NOT NULL DEFAULT FALSE,                   -- Whether condition was met (kept for audit purposes)
    evaluated_value DECIMAL(15, 2) NOT NULL DEFAULT 0,       -- Actual value evaluated for condition
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- Record creation timestamp

    -- Foreign keys
    CONSTRAINT fk_calculation FOREIGN KEY (calculation_id) REFERENCES customer_stage_calculations(id),
    CONSTRAINT fk_condition FOREIGN KEY (condition_id) REFERENCES conditions(id),

    -- Ensure unique combination
    CONSTRAINT uq_calculation_condition UNIQUE (calculation_id, condition_id)
);

-- Create stage_transitions table
CREATE TABLE stage_transitions (
    id SERIAL PRIMARY KEY,                                   -- Auto-increment ID for easy reference
    customer_id VARCHAR(50) NOT NULL,                        -- Unique customer identifier
    calculation_id BIGINT NOT NULL,                          -- Reference to customer_stage_calculations
    previous_stage_code stage_code_enum NOT NULL,            -- Previous stage code
    current_stage_code stage_code_enum NOT NULL,             -- Current stage code
    transition_date DATE NOT NULL,                           -- Date when transition occurs
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, -- Record creation timestamp,

    -- Foreign keys
    CONSTRAINT fk_calculation FOREIGN KEY (calculation_id) REFERENCES customer_stage_calculations(id)
);

-- Create index for efficient lookup
CREATE INDEX idx_customer_date ON stage_transitions(customer_id, transition_date);

