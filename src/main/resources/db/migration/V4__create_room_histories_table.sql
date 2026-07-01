CREATE TABLE room_histories (
    id UUID PRIMARY KEY,
    room_id UUID NOT NULL REFERENCES rooms(id),
    changed_at TIMESTAMP WITH TIME ZONE NOT NULL,
    changed_by UUID NOT NULL,
    reason VARCHAR(255),
    changes TEXT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_room_histories_room_id ON room_histories(room_id);
CREATE INDEX idx_room_histories_changed_at ON room_histories(changed_at);
