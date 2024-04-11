-- Populate Member Table
INSERT INTO Member (first_name,	last_name, email, date_of_birth, heart_rate, height,	weight,	password, sex)
VALUES 
('John', 'Doe', 'john.doe@example.com', '2003-09-01', 88, 175.8, 145.56, 'password1', 'M'),
('Jane', 'Smith', 'jane.smith@example.com', '2004-09-01', 90, 160.7, 100.2, 'password2', 'F'),
('Jim', 'Beam', 'jim.beam@example.com', '1999-09-02', 79, 178.3, 150.2, 'password3', 'M');


-- Populate Admin Table
INSERT INTO Admin (first_name, last_name, password, email)
VALUES 
    ('John', 'Smith', 'admin123', 'john.smith@example.com'),
    ('Emma', 'Johnson', 'secret', 'emma.johnson@example.com'),
    ('Michael', 'Williams', 'password123', 'michael.williams@example.com');

-- Populate Trainer table
INSERT INTO Trainer (first_name, last_name, password, sex, email)
VALUES 
    ('Sarah', 'Brown', 'trainer1pass', 'F', 'sarah.brown@example.com'),
    ('David', 'Clark', 'trainer2pass', 'M', 'david.clark@example.com'),
    ('Laura', 'Wilson', 'trainer3pass', 'F', 'laura.wilson@example.com');


-- Populate Health_Goals table
INSERT INTO Health_Goals (member_id, title, creation_date) VALUES
    (1, 'Lose 10 pounds', '2024-01-01'),
    (1, 'Run a marathon', '2024-02-15'),
    (2, 'Gain muscle mass', '2024-01-10'),
    (3, 'Improve flexibility', '2024-03-01');

-- Populate Health_Achievements table
INSERT INTO Health_Achievements (member_id, title, date, description) VALUES
    (1, 'Lost 5 pounds', '2024-01-30', 'Achieved by following a balanced diet and regular exercise routine.'),
    (1, 'Completed half marathon', '2024-02-29', 'Successfully completed a half marathon after months of training.'),
    (2, 'Increased bench press max', '2024-02-20', 'Reached a new personal record in bench press.'),
    (3, 'Mastered yoga pose', '2024-03-15', 'Achieved full expression of a challenging yoga pose.');

-- Populate Fitness_Routines table
INSERT INTO Fitness_Routines (member_id, title, instructions, description) VALUES
    (1, 'Cardio Circuit', '1. Start with a 5-minute warm-up jog. 2. Perform 3 rounds of: - 30 seconds of jumping jacks - 30 seconds of high knees - 30 seconds of mountain climbers - 30 seconds of burpees. 3. Cool down with a 5-minute jog or walk.', 'High-intensity cardio workout to burn calories and improve cardiovascular health.'),
    (2, 'Strength Training', '1. Warm up with 5 minutes of light cardio. 2. Perform 3 sets of: - Squats (10 reps) - Deadlifts (8 reps) - Bench press (8 reps) - Pull-ups (6 reps). 3. Cool down with stretching exercises.', 'Full-body strength training routine to build muscle mass and strength.');


-- Populate Rooms
INSERT INTO Room (max_capacity, room_number)
VALUES
    (10, 'R101'),
    (15, 'R102'),
    (20, 'R103'),
    (12, 'R104'),
    (18, 'R105');

-- Populate Session
INSERT INTO Session (date, cost, start_time, end_time, duration, type, title, description, trainer_id, room_id)
VALUES
    ('2024-04-10', 50.00, '10:00', '11:00', '01:00', 'PRIVATE', 'Session 1', 'Description of Session 1', 1, 1),
    ('2024-04-10', 50.00, '12:00', '13:00', '01:00', 'PRIVATE', 'Session 1', 'Description of Session 1', 1, 1),
    ('2024-04-13', 80.00, '13:00', '14:00', '01:00', 'GROUP', 'Session 4', 'Description of Session 4', 1, 1),
    ('2024-04-19', 140.00, '19:00', '20:00', '01:00', 'GROUP', 'Session 10', 'Description of Session 10', 1, 1),
    ('2024-04-16', 110.00, '16:00', '17:00', '01:00', 'PRIVATE', 'Session 7', 'Description of Session 7', 1, 1),
    ('2024-04-11', 60.00, '11:00', '12:00', '01:00', 'GROUP', 'Session 2', 'Description of Session 2', 2, 2),
    ('2024-04-17', 120.00, '17:00', '18:00', '01:00', 'GROUP', 'Session 8', 'Description of Session 8', 2, 2),    
    ('2024-04-14', 90.00, '14:00', '15:00', '01:00', 'PRIVATE', 'Session 5', 'Description of Session 5', 2, 2),
    ('2024-04-12', 70.00, '12:00', '13:00', '01:00', 'PRIVATE', 'Session 3', 'Description of Session 3', 3, 3),
    ('2024-04-15', 100.00, '15:00', '16:00', '01:00', 'GROUP', 'Session 6', 'Description of Session 6', 3, 3),
    ('2024-04-18', 130.00, '18:00', '19:00', '01:00', 'PRIVATE', 'Session 9', 'Description of Session 9', 3, 3);


-- Populate trainer schedule
INSERT INTO Trainer_Schedule (trainer_id, date, start_time, end_time, duration)
VALUES
    (1, '2024-04-10', '09:00:00', '14:00:00', '05:00:00'),
    (1, '2024-04-11', '10:00:00', '16:00:00', '06:00:00'),
    (1, '2024-04-13', '10:00:00', '16:00:00', '06:00:00'),
    (1, '2024-04-16', '10:00:00', '16:00:00', '06:00:00'),
    (1, '2024-04-19', '09:00:00', '14:00:00', '05:00:00'),
    (2, '2024-04-11', '11:00:00', '13:00:00', '02:00:00'),
    (2, '2024-04-14', '12:00:00', '15:00:00', '03:00:00'),
    (2, '2024-04-17', '12:00:00', '18:00:00', '06:00:00'),
    (3, '2024-04-12', '12:00:00', '15:00:00', '03:00:00'),
    (3, '2024-04-15', '14:00:00', '16:00:00', '02:00:00'),
    (3, '2024-04-18', '14:00:00', '19:00:00', '05:00:00');

-- Populate Equipment
INSERT INTO Equipment (name, purpose, last_maintenance_date, next_maintenance_date)
VALUES
('Treadmill', 'Cardiovascular exercise', '2023-12-15', '2024-12-15'),
('Dumbbell Set', 'Strength training', '2024-01-20', '2024-07-20'),
('Yoga Mat', 'Yoga and stretching exercises', '2024-02-10', '2024-08-10'),
('Stationary Bike', 'Cardiovascular exercise', '2023-11-05', '2024-11-05'),
('Kettlebell', 'Strength training', '2024-03-08', '2024-09-08'),
('Rowing Machine', 'Cardiovascular exercise', '2024-01-30', '2024-07-30'),
('Medicine Ball', 'Functional training and core exercises', '2024-02-25', '2024-08-25'),
('Resistance Bands', 'Strength training and flexibility exercises', '2024-03-10', '2024-09-10'),
('Bench Press', 'Strength training', '2024-01-15', '2024-07-15');

-- Populate Register table
INSERT INTO Register (session_id, member_id)
VALUES
    (1, 1), (2, 2), (3, 3),(4, 1), (5, 2),
    (6, 3),(7, 1), (8, 2), (9, 3), (10, 1);

-- Populate Bills
INSERT INTO Bills (member_id, amount, date, session_type) VALUES 
(1, 50.00, '2024-04-10', 'PRIVATE'),
(2, 60.00, '2024-04-11', 'GROUP'),
(3, 80.00, '2024-04-13', 'GROUP'),
(1, 90.00, '2024-04-14', 'PRIVATE'),
(2, 120.00, '2024-04-17', 'GROUP'),
(3, 100.00, '2024-04-15', 'GROUP'),
(1, 140.00, '2024-04-19', 'GROUP'),
(1, 70.00, '2024-04-12', 'PRIVATE'),
(3, 130.00, '2024-04-18', 'PRIVATE'),
(2, 50.00, '2024-04-10', 'PRIVATE');