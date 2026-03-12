
--Black Diamond Account Table
CREATE TABLE black_diamond_account (

    id BIGSERIAL PRIMARY KEY,

    account_is_open BOOLEAN,
    relationship_id VARCHAR(50),
    relationship_name VARCHAR(255),

    portfolio_id VARCHAR(50),
    portfolio_name VARCHAR(255),

    account_id VARCHAR(50),
    account_number VARCHAR(100),
    account_name VARCHAR(255),

    custodian VARCHAR(100),
    goal VARCHAR(100),

    aum VARCHAR(20),

    tax_status VARCHAR(50),
    account_reporting_target VARCHAR(100),
    account_benchmark VARCHAR(100),

    asset_id BIGINT,
    asset_name VARCHAR(255),

    class VARCHAR(100),

    market_value NUMERIC(18,2),

    as_of_date DATE,

    advisor VARCHAR(100),

    isSupervised BOOLEAN
);


--ADDEPAR ACCOUNT TABLE
CREATE TABLE addepar_account (

    id BIGSERIAL PRIMARY KEY,

    holding_account VARCHAR(500),

    entity_id BIGINT,

    model_type VARCHAR(100),

    direct_owner_id BIGINT,

    top_level_owner_id BIGINT,

    top_level_owner VARCHAR(500),

    ac_account_number VARCHAR(100),

    ac_goal VARCHAR(255),

    ac_custodian VARCHAR(100),

    ac_avestar BOOLEAN,

    ac_aum BOOLEAN,

    ac_reporting_target VARCHAR(200),

    ac_asset_class VARCHAR(100),

    ac_sub_asset_class VARCHAR(100),

    adjusted_value_usd NUMERIC(18,2),

    ac_last_activity_date DATE,

    isSupervised BOOLEAN
);

--delete last row from addepar after import