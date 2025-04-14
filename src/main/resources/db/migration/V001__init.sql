CREATE TABLE IF NOT EXISTS voting_sessions (
  id            UUID                NOT NULL,
  name          TEXT                NOT NULL,
  candidates    TEXT[]              NOT NULL,
  status        TEXT                NOT NULL,
  version       BIGINT              NOT NULL,
  votes         JSONB               NOT NULL DEFAULT '{}'::JSONB,
  created_at    TIMESTAMPTZ         NOT NULL DEFAULT clock_timestamp(),
  closed_at     TIMESTAMPTZ         NULL,
  CONSTRAINT    pk_voting_sessions  PRIMARY KEY (id)
);
