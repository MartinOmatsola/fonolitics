# --- First database schema

# --- !Ups

ALTER TABLE users MODIFY created_at DATETIME;
ALTER TABLE users MODIFY modified_at DATETIME;

ALTER TABLE surveys MODIFY created_at DATETIME;
ALTER TABLE surveys MODIFY modified_at DATETIME;

ALTER TABLE survey_questions MODIFY created_at DATETIME;
ALTER TABLE survey_questions MODIFY modified_at DATETIME;

ALTER TABLE survey_question_options MODIFY created_at DATETIME;
ALTER TABLE survey_question_options MODIFY modified_at DATETIME;


# --- !Downs

ALTER TABLE users MODIFY created_at INT;
ALTER TABLE users MODIFY modified_at INT;

ALTER TABLE surveys MODIFY created_at INT;
ALTER TABLE surveys MODIFY modified_at INT;

ALTER TABLE survey_questions MODIFY created_at INT;
ALTER TABLE survey_questions MODIFY modified_at INT;

ALTER TABLE survey_question_options MODIFY created_at INT;
ALTER TABLE survey_question_options MODIFY modified_at INT;