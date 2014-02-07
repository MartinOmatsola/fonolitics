# --- !Ups

CREATE TABLE IF NOT EXISTS plans (
	id INT NOT NULL AUTO_INCREMENT,
	name VARCHAR(255),
	price DECIMAL(10,2),
	num_calls INT,
	num_broadcasts INT,
	num_contacts INT,
	PRIMARY KEY (id)
) ENGINE=INNODB;

CREATE TABLE IF NOT EXISTS contacts (
	id INT NOT NULL AUTO_INCREMENT,
	user_id INT,
	user VARCHAR(255),
	fname VARCHAR(255),
	lname VARCHAR(255),
	email VARCHAR(255),
	phone VARCHAR(20),
	PRIMARY KEY (id)
) ENGINE=INNODB;

CREATE TABLE IF NOT EXISTS contact_lists (
	id INT NOT NULL AUTO_INCREMENT,
	user_id INT,
	user VARCHAR(255),
	name VARCHAR(255),
	num_contacts INT,
	PRIMARY KEY (id)
) ENGINE=INNODB;

CREATE TABLE IF NOT EXISTS contact_list_contacts (
	id INT NOT NULL AUTO_INCREMENT,
	user_id INT,
	user VARCHAR(255),
	contact_list_id INT,
	contact_list VARCHAR(255),
	contact_id INT,
	contact VARCHAR(20),
	PRIMARY KEY (id)
) ENGINE=INNODB;



ALTER TABLE users ADD plan_id INT;
ALTER TABLE users ADD plan VARCHAR(255);
ALTER TABLE users ADD num_surveys_left INT;
ALTER TABLE users ADD num_broadcasts_left INT;
ALTER TABLE users ADD plan_expiry_date DATE;

ALTER TABLE surveys ADD plan_id INT;
ALTER TABLE surveys ADD plan VARCHAR(255);
ALTER TABLE surveys ADD num_contacts INT;
ALTER TABLE surveys ADD num_contacts_allowed INT;
ALTER TABLE surveys ADD contact_list_id INT;
ALTER TABLE surveys ADD contact_list INT;


ALTER TABLE users
  ADD CONSTRAINT users_ibfk_3 FOREIGN KEY (plan_id) REFERENCES 
	plans (id) ON DELETE SET NULL ON UPDATE CASCADE;
 
 ALTER TABLE surveys
  ADD CONSTRAINT surveys_ibfk_6 FOREIGN KEY (plan_id) REFERENCES 
	plans (id) ON DELETE SET NULL ON UPDATE CASCADE,
  ADD CONSTRAINT surveys_ibfk_7 FOREIGN KEY (contact_list_id) REFERENCES 
	contact_lists (id) ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE contacts
  ADD CONSTRAINT contacts_ibfk_1 FOREIGN KEY (user_id) REFERENCES 
	users (id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE contact_lists
  ADD CONSTRAINT contact_lists_ibfk_1 FOREIGN KEY (user_id) REFERENCES 
	users (id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE contact_list_contacts
  ADD CONSTRAINT contact_list_contacts_ibfk_1 FOREIGN KEY (user_id) REFERENCES 
	users (id) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT contact_list_contacts_ibfk_2 FOREIGN KEY (contact_list_id) REFERENCES 
	contact_lists (id) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT contact_list_contacts_ibfk_3 FOREIGN KEY (contact_id) REFERENCES 
	contacts (id) ON DELETE CASCADE ON UPDATE CASCADE;


CREATE TRIGGER plans_au_trg
AFTER UPDATE ON plans
FOR EACH ROW
BEGIN
	UPDATE users SET plan = NEW.name 
		WHERE plan_id = NEW.id;;
END;

CREATE TRIGGER users_au_trg
AFTER UPDATE ON users
FOR EACH ROW
BEGIN
	UPDATE surveys SET email = NEW.email
		WHERE user_type_id = NEW.id;;
	UPDATE contacts SET user = NEW.email
		WHERE user_id = NEW.id;;
	UPDATE contact_lists SET user = NEW.email
		WHERE user_id = NEW.id;;
	UPDATE contact_list_contacts SET user = NEW.email
		WHERE user_id = NEW.id;;	
END;

CREATE TRIGGER contacts_au_trg
AFTER UPDATE ON contacts
FOR EACH ROW
BEGIN
	UPDATE contact_list_contacts SET contact = NEW.phone
		WHERE contact_id = NEW.id;;	
END;

CREATE TRIGGER contact_lists_au_trg
AFTER UPDATE ON contact_lists
FOR EACH ROW
BEGIN
	UPDATE contact_list_contacts SET contact_list = NEW.name
		WHERE contact_list_id = NEW.id;;	
END;



# --- !Downs

ALTER TABLE users
  DROP FOREIGN KEY users_ibfk_3;

ALTER TABLE surveys
  DROP FOREIGN KEY surveys_ibfk_6,
  DROP FOREIGN KEY surveys_ibfk_7;

ALTER TABLE contacts
  DROP FOREIGN KEY contacts_ibfk_1;

ALTER TABLE contact_lists
  DROP FOREIGN KEY contact_lists_ibfk_1;

ALTER TABLE contact_list_contacts
  DROP FOREIGN KEY contact_list_contacts_ibfk_1,
  DROP FOREIGN KEY contact_list_contacts_ibfk_2,
  DROP FOREIGN KEY contact_list_contacts_ibfk_3;


DROP TRIGGER plans_au_trg;
DROP TRIGGER users_au_trg;
DROP TRIGGER contacts_au_trg;
DROP TRIGGER contact_lists_au_trg;


SET FOREIGN_KEY_CHECKS = 0;


DROP TABLE IF EXISTS plans;
DROP TABLE IF EXISTS contacts;
DROP TABLE IF EXISTS contact_lists;
DROP TABLE IF EXISTS contact_list_contacts;


SET FOREIGN_KEY_CHECKS = 1;
