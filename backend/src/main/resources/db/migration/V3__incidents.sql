CREATE TABLE incident (
    id                 BIGSERIAL PRIMARY KEY,
    service_id         BIGINT NOT NULL REFERENCES service(id),
    title              VARCHAR(200) NOT NULL,
    status             VARCHAR(20) NOT NULL,
    severity           VARCHAR(10) NOT NULL,
    started_at         TIMESTAMPTZ NOT NULL,
    resolved_at        TIMESTAMPTZ,
    peak_errors_per_min BIGINT NOT NULL DEFAULT 0,
    summary            TEXT
);

CREATE TABLE incident_event (
    id           BIGSERIAL PRIMARY KEY,
    incident_id  BIGINT NOT NULL REFERENCES incident(id) ON DELETE CASCADE,
    occurred_at  TIMESTAMPTZ NOT NULL,
    type         VARCHAR(30) NOT NULL,
    message      TEXT NOT NULL
);

CREATE INDEX idx_incident_status ON incident (status, started_at DESC);
CREATE INDEX idx_incident_event_incident ON incident_event (incident_id, occurred_at);