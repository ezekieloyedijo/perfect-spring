-- Creates the "register sheet" (table) for the topic03 Student Records project.
CREATE TABLE student (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,   -- the database gives each row a number
    name        VARCHAR(100) NOT NULL,
    class_name  VARCHAR(10)  NOT NULL,
    score       INT          NOT NULL
);
