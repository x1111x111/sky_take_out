-- Required for the current event-only cleanup DELETE to cascade ACK rows.
-- For a NEW database created by ../sky_take_out.sql: run this once after the base schema.
-- For an EXISTING database: first inspect SHOW CREATE TABLE and check for orphan ACKs;
-- do not run if an equivalent foreign key already exists. No existing data is deleted here.
USE sky_take_out;
ALTER TABLE cache_invalidation_ack
  ADD CONSTRAINT fk_cache_ack_event
  FOREIGN KEY (event_id) REFERENCES cache_invalidation_event(id)
  ON DELETE CASCADE;
