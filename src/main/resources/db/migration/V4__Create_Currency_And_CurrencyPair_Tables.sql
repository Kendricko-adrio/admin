-- =============================================
-- V4: Create Currency and CurrencyPair Tables
-- Electronic Trading Admin Module
-- =============================================

CREATE TABLE currencies (
    id         BIGSERIAL    PRIMARY KEY,
    code       VARCHAR(3)   NOT NULL,
    name       VARCHAR(100) NOT NULL,
    symbol     VARCHAR(10),
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_currencies_code UNIQUE (code)
);

CREATE INDEX idx_currencies_code ON currencies (code);

CREATE TABLE currency_pairs (
    id                BIGSERIAL    PRIMARY KEY,
    base_currency_id  BIGINT       NOT NULL,
    quote_currency_id BIGINT       NOT NULL,
    pair_code         VARCHAR(6)   NOT NULL,
    rate              DECIMAL(18,8),
    created_at        TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_currency_pairs_base_quote UNIQUE (base_currency_id, quote_currency_id),
    CONSTRAINT uq_currency_pairs_pair_code UNIQUE (pair_code),
    CONSTRAINT fk_currency_pairs_base_currency
        FOREIGN KEY (base_currency_id) REFERENCES currencies(id) ON DELETE CASCADE,
    CONSTRAINT fk_currency_pairs_quote_currency
        FOREIGN KEY (quote_currency_id) REFERENCES currencies(id) ON DELETE CASCADE
);

CREATE INDEX idx_currency_pairs_pair_code ON currency_pairs (pair_code);
