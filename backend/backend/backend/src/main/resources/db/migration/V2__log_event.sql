CREATE TABLE log_event (
    id          BIGSERIAL PRIMARY KEY,
    service_id  BIGINT NOT NULL REFERENCES service(id),
    level       VARCHAR(10) NOT NULL,
    message     TEXT NOT NULL,
    trace_id    VARCHAR(64),
    logged_at   TIMESTAMPTZ NOT NULL,
    received_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_log_event_service_time ON log_event (service_id, logged_at DESC);
CREATE INDEX idx_log_event_level_time ON log_event (level, logged_at DESC);