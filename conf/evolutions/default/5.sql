# --- !Ups

ALTER TABLE plans ADD num_questions INT;
ALTER TABLE plans CHANGE num_calls num_surveys INT;


INSERT INTO user_statuses (name) VALUES
("Admin"),
("Subscription");

INSERT INTO user_statuses (name) VALUES
("Active"),
("Archived");

REPLACE INTO survey_types (id, name) VALUES
(1, "Voice"),
(2, "SMS");

REPLACE INTO survey_statuses (id, name) VALUES
(1, "Pending"),
(2, "In Progress"),
(3, "Completed");

INSERT INTO plans (name, price, num_surveys, num_broadcasts, num_contacts, num_questions) VALUES
("Free", 0, 1, 0, 20, 5),
("Bronze", 29.99, 5, 1, 50, 10),
("Silver", 59.99, 5, 2, 100, 10),
("Gold", 199.99, 5, 4, 500, 10),
("Platinum", 329.99, 5, 6, 1000, 10);

