-- V1.3: Refactor workshop table schema to match Workshop entity
-- @Table(name = "workshop") with columns: id, title(VARCHAR200 NOT NULL), description(VARCHAR2000),
-- room_id(UUID NULLABLE), room_display_name_snapshot(VARCHAR200), start_time, end_time,
-- capacity(INT NOT NULL), state(VARCHAR20 NOT NULL), created_at, updated_at

-- Rename table to match entity @Table(name = "workshop")
ALTER TABLE workshops RENAME TO workshop;

-- Drop columns not present in entity
ALTER TABLE workshop DROP COLUMN speaker_name;
ALTER TABLE workshop DROP COLUMN room_capacity;
ALTER TABLE workshop DROP COLUMN room_location;

-- Rename status -> state and resize to VARCHAR(20)
ALTER TABLE workshop RENAME COLUMN status TO state;
ALTER TABLE workshop ALTER COLUMN state TYPE VARCHAR(20);

-- Rename room_name -> room_display_name_snapshot and resize to VARCHAR(200)
ALTER TABLE workshop RENAME COLUMN room_name TO room_display_name_snapshot;
ALTER TABLE workshop ALTER COLUMN room_display_name_snapshot TYPE VARCHAR(200);

-- Change description from TEXT to VARCHAR(2000)
ALTER TABLE workshop ALTER COLUMN description TYPE VARCHAR(2000);

-- Change title from VARCHAR(255) to VARCHAR(200) (NOT NULL preserved)
ALTER TABLE workshop ALTER COLUMN title TYPE VARCHAR(200);

-- Make room_id nullable (was NOT NULL)
ALTER TABLE workshop ALTER COLUMN room_id DROP NOT NULL;

-- Add capacity column with default value for existing rows
ALTER TABLE workshop ADD COLUMN capacity INT NOT NULL DEFAULT 0;

-- Rename index to match new table name
ALTER INDEX IF EXISTS idx_workshops_room_id RENAME TO idx_workshop_room_id;
