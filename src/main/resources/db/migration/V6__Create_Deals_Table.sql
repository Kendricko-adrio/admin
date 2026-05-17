-- =============================================
-- V6: Create Deals Table
-- Electronic Trading Admin Module
-- =============================================

CREATE TABLE deals_rfq_events (
    id                           BIGSERIAL       PRIMARY KEY,
    order_id                     UUID,
    order_status                 VARCHAR(50),
    identity_type_label          VARCHAR(50),
    identity_number              VARCHAR(50),
    amount_1                     DECIMAL(18, 8),
    amount_2                     DECIMAL(18, 8),
    fx_rate                      DECIMAL(18, 8),
    transaction_type             VARCHAR(50),
    deal_purpose_id              VARCHAR(50),
    customer_name                VARCHAR(100),
    branch_code                  BIGINT,
    currency_pair                VARCHAR(10),
    document_code_id             VARCHAR(50),
    document_description         VARCHAR(255),
    npwp                         VARCHAR(50),
    counterparty_short_name      VARCHAR(100),
    typology                     VARCHAR(50),
    user_short_name              VARCHAR(100),
    trade_time                   VARCHAR(20),
    trade_date                   VARCHAR(20),
    maturity_date                VARCHAR(20),
    disclaimer                   VARCHAR(255),
    special_transaction          VARCHAR(50),
    document_flag                VARCHAR(50),
    source_account               VARCHAR(100),
    destination_account          VARCHAR(100),
    cin                          VARCHAR(50),
    settlement_option            VARCHAR(50),
    equivalent_margin_amount_idr DECIMAL(18, 8),
    equivalent_margin_currency   DECIMAL(18, 8),
    customer_pic                 VARCHAR(100),
    mrr_type                     VARCHAR(50),
    risk_currency                VARCHAR(10),
    risk_amount                  DECIMAL(18, 8),
    event_reason                 VARCHAR(255),
    fixing_parent                VARCHAR(50),
    deal_status                  VARCHAR(50),
    ref_spot                     DECIMAL(18, 8),
    net_spot                     DECIMAL(18, 8),
    forward                      DECIMAL(18, 8),
    client_swap_near_leg         DECIMAL(18, 8),
    internal_swap_near_leg       DECIMAL(18, 8),
    client_swap_far_leg          DECIMAL(18, 8),
    internal_swap_far_leg        DECIMAL(18, 8),
    capture_date                 VARCHAR(20),
    amount_1_leg_2               DECIMAL(18, 8),
    amount_2_leg_2               DECIMAL(18, 8),
    transaction_code             VARCHAR(50),
    client_id                    VARCHAR(50),
    trader_id                    VARCHAR(50),
    comments                     TEXT,
    captured_amount              INT,
    value_date                   VARCHAR(20),
    settlement_type              VARCHAR(50),
    created_at                   TIMESTAMP       NOT NULL DEFAULT NOW(),
    updated_at                   TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_deals_order_id ON deals_rfq_events (order_id);
CREATE INDEX idx_deals_customer_name ON deals_rfq_events (customer_name);
CREATE INDEX idx_deals_branch_code ON deals_rfq_events (branch_code);
CREATE INDEX idx_deals_currency_pair ON deals_rfq_events (currency_pair);
CREATE INDEX idx_deals_trade_date ON deals_rfq_events (trade_date);
CREATE INDEX idx_deals_deal_status ON deals_rfq_events (deal_status);
CREATE INDEX idx_deals_client_id ON deals_rfq_events (client_id);
CREATE INDEX idx_deals_trader_id ON deals_rfq_events (trader_id);
CREATE INDEX idx_deals_transaction_type ON deals_rfq_events (transaction_type);
