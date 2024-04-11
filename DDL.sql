-- Member Table
CREATE TABLE Member(
	member_id SERIAL,
	first_name VARCHAR(255) NOT NULL,
	last_name VARCHAR(255) NOT NULL,
	email VARCHAR(255) NOT NULL,
	date_of_birth DATE,
	heart_rate INT,
	height NUMERIC(5,2),
	weight NUMERIC(5,2),
	password VARCHAR(255) NOT NULL,
	sex VARCHAR(255),	
		PRIMARY KEY(member_id)
);

-- Health_Goals Table
CREATE TABLE Health_Goals(
	goal_id SERIAL,
	member_id INT,
	title VARCHAR(255),
	creation_date DATE,
		PRIMARY KEY (member_id, goal_id),
		FOREIGN KEY (member_id) REFERENCES Member(member_id)
);

-- Health_Achievements Table
CREATE TABLE Health_Achievements(
	achievement_id SERIAL,
	member_id INT,
	title VARCHAR(255),
	date DATE,
	description TEXT,
		PRIMARY KEY(achievement_id, member_id),
		FOREIGN KEY (member_id) REFERENCES Member(member_id)
);

-- Fitness_Routines Table
CREATE TABLE Fitness_Routines(
	routine_id SERIAL,
	member_id INT,
	title VARCHAR(255),
	instructions TEXT,
	description TEXT,
		PRIMARY KEY(routine_id, member_id),
		FOREIGN KEY (member_id) REFERENCES Member(member_id)
);

-- Trainer Table
CREATE TABLE Trainer(
	trainer_id SERIAL,
	first_name VARCHAR(255) NOT NULL,
	last_name VARCHAR(255) NOT NULL,
	password VARCHAR(255) NOT NULL,
	sex VARCHAR(255),
	email VARCHAR(255) NOT NULL,
		PRIMARY KEY(trainer_id)
);

-- Trainer_Schedule Table
CREATE TABLE Trainer_Schedule(
	schedule_id SERIAL,
	trainer_id INT,
	date DATE,
	start_time TIME,
	end_time TIME,
	duration TIME,
		PRIMARY KEY(schedule_id, trainer_id),
		FOREIGN KEY (trainer_id) REFERENCES Trainer(trainer_id)
);

-- Admin Table
CREATE TABLE Admin(
	admin_id SERIAL,
	password VARCHAR(255) NOT NULL,
	first_name VARCHAR(255) NOT NULL,
	last_name VARCHAR(255) NOT NULL,
	email VARCHAR(255) UNIQUE NOT NULL,
		PRIMARY KEY(admin_id)
);

-- Room Table
CREATE TABLE Room(
	room_id SERIAL,
	max_capacity INT,
	room_number VARCHAR(255),
		PRIMARY KEY(room_id)
);

-- Session Table
CREATE TABLE Session(
	session_id SERIAL,
	date DATE,
	cost NUMERIC(5,2),
	start_time TIME,
	end_time TIME,
	duration TIME,
	type VARCHAR(255) NOT NULL,
	title VARCHAR(255) NOT NULL,
	description TEXT,
	trainer_id INT,
	room_id INT,
		PRIMARY KEY(session_id),
		FOREIGN KEY (trainer_id) REFERENCES Trainer(trainer_id),
		FOREIGN KEY (room_id) REFERENCES Room(room_id)
);

-- Equipment Table
CREATE TABLE Equipment(
	equip_id SERIAL,
	name VARCHAR(255),
	purpose TEXT,
	last_maintenance_date DATE,
	next_maintenance_date DATE,
		PRIMARY KEY(equip_id)
);

-- Bills Table
CREATE TABLE Bills(
	bill_id SERIAL,
	member_id INT,
	admin_id INT,
	amount NUMERIC(5,2),
	session_type VARCHAR(255),
	date DATE,
		PRIMARY KEY (bill_id),
		FOREIGN KEY (member_id) REFERENCES Member(member_id),
		FOREIGN KEY (admin_id) REFERENCES Admin(admin_id)
);

-- Register Table
CREATE TABLE Register(
	session_id INT,
	member_id INT,
		FOREIGN KEY(session_id) REFERENCES Session(session_id),
		FOREIGN KEY(member_id) REFERENCES Member(member_id)
);