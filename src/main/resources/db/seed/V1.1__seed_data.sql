INSERT INTO users (id, email, password_hash, first_name, last_name, role, created_at, updated_at) VALUES
('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'admin@example.com', '$2a$10$vG0I2L9eS2L5rQ9v6Dk4eeZpH9p7S/6gO9I0a6v.kYOnp6019a.fO', 'Admin', 'User', 'ADMIN', '2026-06-22 10:00:00+00', '2026-06-22 10:00:00+00'),
('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a12', 'organizer@example.com', '$2a$10$vG0I2L9eS2L5rQ9v6Dk4eeZpH9p7S/6gO9I0a6v.kYOnp6019a.fO', 'Organizer', 'User', 'ORGANIZER', '2026-06-22 10:00:00+00', '2026-06-22 10:00:00+00'),
('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'attendee1@example.com', '$2a$10$vG0I2L9eS2L5rQ9v6Dk4eeZpH9p7S/6gO9I0a6v.kYOnp6019a.fO', 'Attendee', 'One', 'ATTENDEE', '2026-06-22 10:00:00+00', '2026-06-22 10:00:00+00'),
('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 'attendee2@example.com', '$2a$10$vG0I2L9eS2L5rQ9v6Dk4eeZpH9p7S/6gO9I0a6v.kYOnp6019a.fO', 'Attendee', 'Two', 'ATTENDEE', '2026-06-22 10:00:00+00', '2026-06-22 10:00:00+00');

INSERT INTO rooms (id, name, capacity, location, created_at, updated_at) VALUES
('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', 'Auditorium A', 100, 'Main Building', '2026-06-22 10:00:00+00', '2026-06-22 10:00:00+00'),
('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a16', 'Informatics Lab 202', 30, 'Computer Science Building', '2026-06-22 10:00:00+00', '2026-06-22 10:00:00+00');

INSERT INTO workshops (id, title, description, speaker_name, start_time, end_time, status, created_at, updated_at, room_id, room_name, room_capacity, room_location) VALUES
('11eebc99-9c0b-4ef8-bb6d-6bb9bd380a17', 'Introduction to Spring Boot', 'A beginner-friendly workshop on Spring Boot.', 'Dr. Jane Doe', '2026-07-15 09:00:00+00', '2026-07-15 12:00:00+00', 'SCHEDULED', '2026-06-22 10:00:00+00', '2026-06-22 10:00:00+00', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a15', 'Auditorium A', 100, 'Main Building'),
('22eebc99-9c0b-4ef8-bb6d-6bb9bd380a18', 'Advanced Java Concurrency', 'Deep dive into concurrent programming in Java.', 'Prof. John Smith', '2026-08-01 13:00:00+00', '2026-08-01 17:00:00+00', 'SCHEDULED', '2026-06-22 10:00:00+00', '2026-06-22 10:00:00+00', 'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a16', 'Informatics Lab 202', 30, 'Computer Science Building');

INSERT INTO registrations (id, workshop_id, user_id, status, registration_time, created_at, updated_at, attended, attendance_marked_at, attendance_marked_by) VALUES
('33eebc99-9c0b-4ef8-bb6d-6bb9bd380a19', '11eebc99-9c0b-4ef8-bb6d-6bb9bd380a17', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a13', 'CONFIRMED', '2026-06-22 10:00:00+00', '2026-06-22 10:00:00+00', '2026-06-22 10:00:00+00', FALSE, NULL, NULL),
('44eebc99-9c0b-4ef8-bb6d-6bb9bd380a20', '22eebc99-9c0b-4ef8-bb6d-6bb9bd380a18', 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380a14', 'CONFIRMED', '2026-06-22 10:00:00+00', '2026-06-22 10:00:00+00', '2026-06-22 10:00:00+00', FALSE, NULL, NULL);