-- Drop old registrations table (development mode)
DROP TABLE IF EXISTS registrations CASCADE;

-- Create new registrations table with checked-in support
CREATE TABLE registrations (
    id UUID PRIMARY KEY,
    workshop_id UUID NOT NULL REFERENCES workshops(id),
    user_id UUID NOT NULL,
    status VARCHAR(50) NOT NULL,
    registration_time TIMESTAMP WITH TIME ZONE NOT NULL,
    checked_in BOOLEAN DEFAULT FALSE,
    checked_in_at TIMESTAMP WITH TIME ZONE,
    checked_in_by UUID,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uk_registrations_workshop_user UNIQUE (workshop_id, user_id)
);

CREATE INDEX idx_registrations_workshop_id ON registrations(workshop_id);
CREATE INDEX idx_registrations_user_id ON registrations(user_id);
CREATE INDEX idx_registrations_status ON registrations(status);
CREATE INDEX idx_registrations_checked_in ON registrations(checked_in);
