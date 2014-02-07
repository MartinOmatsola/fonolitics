# --- First database schema

# --- !Ups


-- Begin Tables


CREATE TABLE IF NOT EXISTS user_types (
	id INT NOT NULL AUTO_INCREMENT,
	name VARCHAR(255),
	active INT(1) DEFAULT 1,
	PRIMARY KEY (id)
) ENGINE=INNODB;

CREATE TABLE IF NOT EXISTS user_statuses (
	id INT NOT NULL AUTO_INCREMENT,
	name VARCHAR(255),
	active INT(1) DEFAULT 1,
	PRIMARY KEY (id)
) ENGINE=INNODB;

CREATE TABLE IF NOT EXISTS users (
	id INT NOT NULL AUTO_INCREMENT,
	fname VARCHAR(255),
	lname VARCHAR(255),
	fullname VARCHAR(255),
	email VARCHAR(255),
	password VARCHAR(255),
	user_type_id INT,
	user_type VARCHAR(255),
	user_status_id INT,
	user_status VARCHAR(255),
	last_login_time DATETIME,
	created_at INT,
	created_by INT,
	created_email VARCHAR(255),
	modified_at INT,
	modified_by INT,
	modified_email VARCHAR(255),
	PRIMARY KEY (id)
) ENGINE=INNODB;

CREATE TABLE IF NOT EXISTS sessions (
	id INT NOT NULL AUTO_INCREMENT,
	user_id INT,
	email VARCHAR(255),
	session_key VARCHAR(255),
	login_time DATETIME,
	expiry_time DATETIME,
	ip VARCHAR(255),
	INDEX (session_key),
	PRIMARY KEY (id)
) ENGINE=INNODB;

CREATE TABLE IF NOT EXISTS survey_types (
	id INT NOT NULL AUTO_INCREMENT,
	name VARCHAR(255),
	active INT(1) DEFAULT 1,
	PRIMARY KEY (id)
) ENGINE=INNODB;

CREATE TABLE IF NOT EXISTS survey_statuses (
	id INT NOT NULL AUTO_INCREMENT,
	name VARCHAR(255),
	active INT(1) DEFAULT 1,
	PRIMARY KEY (id)
) ENGINE=INNODB;

CREATE TABLE IF NOT EXISTS surveys (
	id INT NOT NULL AUTO_INCREMENT,
	user_id INT,
	email VARCHAR(255),
	name VARCHAR(255),
	survey_type_id INT,
	survey_type VARCHAR(255),
	survey_status_id INT,
	survey_status VARCHAR(255),
	greeting TEXT,
	created_at INT,
	created_by INT,
	created_email VARCHAR(255),
	modified_at INT,
	modified_by INT,
	modified_email VARCHAR(255),
	PRIMARY KEY (id)
) ENGINE=INNODB;

CREATE TABLE IF NOT EXISTS survey_questions (
	id INT NOT NULL AUTO_INCREMENT,
	survey_id INT,
	survey VARCHAR(255),
	question TEXT,
	sequence_order INT DEFAULT 0,
	created_at INT,
	created_by INT,
	created_email VARCHAR(255),
	modified_at INT,
	modified_by INT,
	modified_email VARCHAR(255),
	PRIMARY KEY (id)
) ENGINE=INNODB;

CREATE TABLE IF NOT EXISTS survey_question_options (
	id INT NOT NULL AUTO_INCREMENT,
	survey_question_id INT,
	survey_question TEXT,
	answer VARCHAR(255),
	selected_num INT DEFAULT 0,
	sequence_order INT DEFAULT 0,
	created_at INT,
	created_by INT,
	created_email VARCHAR(255),
	modified_at INT,
	modified_by INT,
	modified_email VARCHAR(255),
	PRIMARY KEY (id)
) ENGINE=INNODB;


-- End Tables


-- Begin Constraints

 
ALTER TABLE users
  ADD CONSTRAINT users_ibfk_1 FOREIGN KEY (user_type_id) REFERENCES 
	user_types (id) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT users_ibfk_2 FOREIGN KEY (user_status_id) REFERENCES 
	user_statuses (id) ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE sessions
  ADD CONSTRAINT sessions_ibfk_1 FOREIGN KEY (user_id) REFERENCES 
	users (id) ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE surveys
  ADD CONSTRAINT surveys_ibfk_1 FOREIGN KEY (user_id) REFERENCES 
	users (id) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT surveys_ibfk_2 FOREIGN KEY (survey_type_id) REFERENCES 
	survey_types (id) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT surveys_ibfk_3 FOREIGN KEY (survey_status_id) REFERENCES 
	survey_statuses (id) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT surveys_ibfk_4 FOREIGN KEY (created_by) REFERENCES 
	users (id) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT surveys_ibfk_5 FOREIGN KEY (modified_by) REFERENCES 
	users (id) ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE survey_questions
  ADD CONSTRAINT survey_questions_ibfk_1 FOREIGN KEY (survey_id) REFERENCES 
	surveys (id) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT survey_questions_ibfk_2 FOREIGN KEY (created_by) REFERENCES 
	users (id) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT survey_questions_ibfk_3 FOREIGN KEY (modified_by) REFERENCES 
	users (id) ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE survey_question_options
  ADD CONSTRAINT survey_question_options_ibfk_1 FOREIGN KEY (survey_question_id) REFERENCES 
	survey_questions (id) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT survey_question_options_ibfk_2 FOREIGN KEY (created_by) REFERENCES 
	users (id) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT survey_question_options_ibfk_3 FOREIGN KEY (modified_by) REFERENCES 
	users (id) ON DELETE SET NULL ON UPDATE CASCADE;	

-- End Constraints


-- Begin Triggers



CREATE TRIGGER user_types_au_trg
AFTER UPDATE ON user_types
FOR EACH ROW
BEGIN
	UPDATE users SET user_type = NEW.name 
		WHERE user_type_id = NEW.id;;
END;

CREATE TRIGGER user_statuses_au_trg
AFTER UPDATE ON user_statuses
FOR EACH ROW
BEGIN
	UPDATE users SET user_status = NEW.name 
		WHERE user_status_id = NEW.id;;
END;

CREATE TRIGGER survey_types_au_trg
AFTER UPDATE ON survey_types
FOR EACH ROW
BEGIN
	UPDATE surveys SET survey_type = NEW.name 
		WHERE survey_type_id = NEW.id;;
END;

CREATE TRIGGER survey_statuses_au_trg
AFTER UPDATE ON survey_statuses
FOR EACH ROW
BEGIN
	UPDATE surveys SET survey_status = NEW.name 
		WHERE survey_status_id = NEW.id;;
END;

CREATE TRIGGER surveys_au_trg
AFTER UPDATE ON surveys
FOR EACH ROW
BEGIN
	UPDATE survey_questions SET survey = NEW.name 
		WHERE survey_id = NEW.id;;
END;

CREATE TRIGGER survey_questions_au_trg
AFTER UPDATE ON survey_questions
FOR EACH ROW
BEGIN
	UPDATE survey_question_options SET survey_question = NEW.question 
		WHERE survey_question_id = NEW.id;;
END;



-- End Triggers


# --- !Downs


-- Begin Constraints


ALTER TABLE users
  DROP FOREIGN KEY users_ibfk_1,
  DROP FOREIGN KEY users_ibfk_2;

ALTER TABLE sessions
  DROP FOREIGN KEY sessions_ibfk_1;

ALTER TABLE surveys
  DROP FOREIGN KEY surveys_ibfk_1,
  DROP FOREIGN KEY surveys_ibfk_2,
  DROP FOREIGN KEY surveys_ibfk_3,
  DROP FOREIGN KEY surveys_ibfk_4,
  DROP FOREIGN KEY surveys_ibfk_5;

ALTER TABLE survey_questions
  DROP FOREIGN KEY survey_questions_ibfk_1,
  DROP FOREIGN KEY survey_questions_ibfk_2,
  DROP FOREIGN KEY survey_questions_ibfk_3;

ALTER TABLE survey_question_options
  DROP FOREIGN KEY survey_question_options_ibfk_1,
  DROP FOREIGN KEY survey_question_options_ibfk_2,
  DROP FOREIGN KEY survey_question_options_ibfk_3;


-- End Constraints


-- Begin Triggers


DROP TRIGGER user_types_au_trg;
DROP TRIGGER user_statuses_au_trg;
DROP TRIGGER survey_types_au_trg;
DROP TRIGGER survey_statuses_au_trg;
DROP TRIGGER surveys_au_trg;
DROP TRIGGER survey_questions_au_trg;


-- End Triggers


-- Begin Tables


SET FOREIGN_KEY_CHECKS = 0;


DROP TABLE IF EXISTS user_types;
DROP TABLE IF EXISTS user_statuses;
DROP TABLE IF EXISTS users;
DROP TABLE IF EXISTS sessions;
DROP TABLE IF EXISTS survey_types;
DROP TABLE IF EXISTS survey_statuses;
DROP TABLE IF EXISTS surveys;
DROP TABLE IF EXISTS survey_questions;
DROP TABLE IF EXISTS survey_question_options;


SET FOREIGN_KEY_CHECKS = 1;


-- End Tables
