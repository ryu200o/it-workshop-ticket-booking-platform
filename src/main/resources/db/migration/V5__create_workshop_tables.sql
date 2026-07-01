-- Rename workshop -> workshops (plural naming convention)
ALTER TABLE IF EXISTS workshop RENAME TO workshops;

-- Expand state column for future-proofing
ALTER TABLE workshops ALTER COLUMN state TYPE VARCHAR(50);

-- Create workshop_histories table (business audit log)
CREATE TABLE workshop_histories (
    id UUID PRIMARY KEY,
    workshop_id UUID NOT NULL REFERENCES workshops(id),
    event_type VARCHAR(50) NOT NULL,
    event_data TEXT NOT NULL,
    reason VARCHAR(255),
    changed_by UUID NOT NULL,
    occurred_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_workshop_histories_workshop_id ON workshop_histories(workshop_id);
CREATE INDEX idx_workshop_histories_occurred_at ON workshop_histories(occurred_at);
CREATE INDEX idx_workshop_histories_event_type ON workshop_histories(event_type);
-- Create workshop_snapshots table (immutable report on COMPLETED)
CREATE TABLE workshop_snapshots (
    id UUID PRIMARY KEY,
    workshop_id UUID NOT NULL REFERENCES workshops(id),
    room_name VARCHAR(255) NOT NULL,
    room_location VARCHAR(255) NOT NULL,
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE NOT NULL,
    capacity INT NOT NULL,
    actual_attendance INT DEFAULT 0,
    feedback_score DECIMAL(3,2),
    completed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE UNIQUE INDEX uk_workshop_snapshots_workshop ON workshop_snapshots(workshop_id);
CREATE INDEX idx_workshop_snapshots_completed_at ON workshop_snapshots(completed_at);

-- Rename old index
ALTER INDEX IF EXISTS idx_workshop_room_id RENAME TO idx_workshops_room_id;
