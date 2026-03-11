-- ==========================================
-- User Profile
-- ==========================================

CREATE TABLE IF NOT EXISTS user_profile (
    id SERIAL PRIMARY KEY,
    created_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by INTEGER,
    updated_by INTEGER,
    name TEXT,
    password TEXT,
    mfa BOOLEAN DEFAULT FALSE,
    password_change BOOLEAN DEFAULT FALSE,
    active BOOLEAN DEFAULT TRUE,
    email TEXT,
    mobile TEXT,
    secret TEXT
);

-- ==========================================
-- Vendor
-- ==========================================

CREATE TABLE IF NOT EXISTS vendor (
    id SERIAL PRIMARY KEY,
    created_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by INTEGER,
    updated_by INTEGER,
    name TEXT
);

-- ==========================================
-- Custodian
-- ==========================================

CREATE TABLE IF NOT EXISTS custodian (
    id SERIAL PRIMARY KEY,
    created_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by INTEGER,
    updated_by INTEGER,
    name TEXT
);

-- ==========================================
-- Advisor
-- ==========================================

CREATE TABLE IF NOT EXISTS advisor (
    id SERIAL PRIMARY KEY,
    created_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by INTEGER,
    updated_by INTEGER,
    name TEXT,
    client_id INTEGER
);

-- ==========================================
-- Relationship
-- ==========================================

CREATE TABLE IF NOT EXISTS relationship (
    id SERIAL PRIMARY KEY,
    created_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by INTEGER,
    updated_by INTEGER,
    name TEXT,
    type TEXT,
    active BOOLEAN DEFAULT TRUE,
    location TEXT,
    household_id INTEGER,
    advisor_id INTEGER,
    client_id INTEGER,
    version_name TEXT,
    reporting_frequency TEXT,
    relationship_manager_id INTEGER,
    implementation_flag BOOLEAN DEFAULT FALSE,
    reporting_last_date TIMESTAMP,
    managed_portfolio_id INTEGER,
    parent_id INTEGER,
    vendor_id INTEGER,
    target_id INTEGER
);

-- ==========================================
-- Account
-- ==========================================

CREATE TABLE IF NOT EXISTS account (
    id SERIAL PRIMARY KEY,
    created_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by INTEGER,
    updated_by INTEGER,
    name TEXT,
    number TEXT,
    asset BOOLEAN DEFAULT TRUE,
    tax_status TEXT,
    custodian_id INTEGER,
    relationship_id INTEGER,
    aum BOOLEAN DEFAULT FALSE,
    managed BOOLEAN DEFAULT FALSE,
    account_benchmark_id INTEGER,
    as_of_date TIMESTAMP,
    reporting_target TEXT,
    goal TEXT,
    custodial BOOLEAN DEFAULT FALSE,
    entity_id INTEGER,
    vendor_id INTEGER,
    type_id INTEGER
);

-- ==========================================
-- Holding
-- ==========================================

CREATE TABLE IF NOT EXISTS holding (
    id SERIAL PRIMARY KEY,
    created_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by INTEGER,
    updated_by INTEGER,
    account_id INTEGER,
    product_id INTEGER,
    asset_id INTEGER,
    asset_product_mapping_id INTEGER,
    value DECIMAL(19,4),
    cost_basis DECIMAL(19,4),
    short_term_realized_gain DECIMAL(19,4),
    short_term_unrealized_gain DECIMAL(19,4),
    long_term_realized_gain DECIMAL(19,4),
    long_term_unrealized_gain DECIMAL(19,4)
);

CREATE TABLE IF NOT EXISTS public.household_entity (
    -- Primary key (using SERIAL)
    id SERIAL NOT NULL,

    -- Base entity fields
    created_timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,

    -- BaseUpdatableEntity fields
    updated_timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,

    -- WfHouseholdEntity specific fields
    name VARCHAR(255) NOT NULL,
    relationship_id BIGINT,

    -- Constraints
    CONSTRAINT household_entity_pkey PRIMARY KEY (id),
    CONSTRAINT fk_household_entity_relationship FOREIGN KEY (relationship_id)
        REFERENCES public.relationship (id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
);

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_household_entity_relationship_id
    ON public.household_entity(relationship_id);

CREATE INDEX IF NOT EXISTS idx_household_entity_name
    ON public.household_entity(name);

-- ==========================================
-- Foreign Keys
-- ==========================================

ALTER TABLE advisor
ADD CONSTRAINT fk_advisor_client
FOREIGN KEY (client_id) REFERENCES user_profile(id);

ALTER TABLE relationship
ADD CONSTRAINT fk_relationship_advisor
FOREIGN KEY (advisor_id) REFERENCES advisor(id);

ALTER TABLE relationship
ADD CONSTRAINT fk_relationship_client
FOREIGN KEY (client_id) REFERENCES user_profile(id);

ALTER TABLE relationship
ADD CONSTRAINT fk_relationship_vendor
FOREIGN KEY (vendor_id) REFERENCES vendor(id);

ALTER TABLE relationship
ADD CONSTRAINT fk_relationship_parent
FOREIGN KEY (parent_id) REFERENCES relationship(id);

ALTER TABLE account
ADD CONSTRAINT fk_account_relationship
FOREIGN KEY (relationship_id) REFERENCES relationship(id);

ALTER TABLE account
ADD CONSTRAINT fk_account_custodian
FOREIGN KEY (custodian_id) REFERENCES custodian(id);

ALTER TABLE account
ADD CONSTRAINT fk_account_vendor
FOREIGN KEY (vendor_id) REFERENCES vendor(id);

ALTER TABLE holding
ADD CONSTRAINT fk_holding_account
FOREIGN KEY (account_id) REFERENCES account(id);

--INSERT INTO public.advisor (name, created_timestamp, updated_timestamp)
--SELECT 'Patwa', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
--WHERE NOT EXISTS (SELECT 1 FROM public.advisor WHERE name = 'Patwa');
--
--INSERT INTO public.advisor (name, created_timestamp, updated_timestamp)
--SELECT 'India', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
--WHERE NOT EXISTS (SELECT 1 FROM public.advisor WHERE name = 'India');
--
--INSERT INTO public.advisor (name, created_timestamp, updated_timestamp)
--SELECT 'Avestar', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
--WHERE NOT EXISTS (SELECT 1 FROM public.advisor WHERE name = 'Avestar');
--
--INSERT INTO public.advisor (name, created_timestamp, updated_timestamp)
--SELECT 'Not Assigned', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
--WHERE NOT EXISTS (SELECT 1 FROM public.advisor WHERE name = 'Not Assigned');


-- ==========================================
-- Indexes (for performance)
-- ==========================================

CREATE INDEX idx_user_profile_email ON user_profile(email);

CREATE INDEX idx_advisor_client_id
ON advisor(client_id);

CREATE INDEX idx_relationship_advisor
ON relationship(advisor_id);

CREATE INDEX idx_relationship_client
ON relationship(client_id);

CREATE INDEX idx_relationship_vendor
ON relationship(vendor_id);

CREATE INDEX idx_account_relationship
ON account(relationship_id);

CREATE INDEX idx_account_custodian
ON account(custodian_id);

CREATE INDEX idx_account_vendor
ON account(vendor_id);

CREATE INDEX idx_holding_account
ON holding(account_id);

CREATE INDEX idx_holding_asset_id ON holding(asset_id);
CREATE INDEX idx_account_number ON account(number);
CREATE INDEX idx_relationship_name ON relationship(name);

alter table account add column if not exists investment_type varchar(255);

alter table account add column if not exists advisor varchar(255);