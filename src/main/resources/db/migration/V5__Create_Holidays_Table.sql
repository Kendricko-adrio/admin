CREATE TABLE holidays (
    id           BIGSERIAL    PRIMARY KEY,
    currency_id  BIGINT       NOT NULL,
    type         VARCHAR(10)  NOT NULL,
    holiday_date DATE,
    day_of_week  VARCHAR(10),
    created_at   TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMP    NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_holidays_currency
        FOREIGN KEY (currency_id) REFERENCES currencies(id) ON DELETE CASCADE,
    CONSTRAINT uq_holidays_currency_date
        UNIQUE (currency_id, type, holiday_date),
    CONSTRAINT uq_holidays_currency_day
        UNIQUE (currency_id, type, day_of_week)
);

CREATE INDEX idx_holidays_currency_id ON holidays (currency_id);
CREATE INDEX idx_holidays_type ON holidays (type);
