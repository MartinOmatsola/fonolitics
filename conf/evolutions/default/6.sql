# --- !Ups

DELETE FROM user_statuses WHERE name IN ("Admin", "Subscription");

INSERT INTO user_types (name) VALUES
("Admin"),
("Subscription");