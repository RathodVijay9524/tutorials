-- Sample Quiz Data for Testing
-- This script creates a sample quiz for the tutorial with ID 1 (adjust if needed)

-- First, get the tutorial ID (assuming 'test-slug' tutorial exists)
-- SET @tutorial_id = (SELECT id FROM tutorials WHERE slug = 'test-slug');

-- Create the Quiz
INSERT INTO quizzes (tutorial_id, title, description, passing_score, time_limit_minutes, is_active, created_at, updated_at)
VALUES (1, 'Java Basics Quiz', 'Test your understanding of Java fundamentals covered in this tutorial.', 70, 10, true, NOW(), NOW());

-- Get the quiz ID (assuming it's the first quiz, ID = 1)
SET @quiz_id = LAST_INSERT_ID();

-- Question 1: Multiple Choice
INSERT INTO questions (quiz_id, question_text, question_type, explanation, display_order, points)
VALUES (@quiz_id, 'What is the correct way to declare a variable in Java?', 'MULTIPLE_CHOICE', 
        'In Java, you declare a variable by specifying the type followed by the variable name.', 1, 1);
SET @q1_id = LAST_INSERT_ID();

INSERT INTO question_options (question_id, option_text, is_correct, display_order) VALUES
(@q1_id, 'var x = 10;', false, 1),
(@q1_id, 'int x = 10;', true, 2),
(@q1_id, 'x = 10;', false, 3),
(@q1_id, 'variable x = 10;', false, 4);

-- Question 2: Multiple Choice
INSERT INTO questions (quiz_id, question_text, question_type, explanation, display_order, points)
VALUES (@quiz_id, 'Which keyword is used to define a class in Java?', 'MULTIPLE_CHOICE',
        'The "class" keyword is used to define a class in Java.', 2, 1);
SET @q2_id = LAST_INSERT_ID();

INSERT INTO question_options (question_id, option_text, is_correct, display_order) VALUES
(@q2_id, 'def', false, 1),
(@q2_id, 'function', false, 2),
(@q2_id, 'class', true, 3),
(@q2_id, 'object', false, 4);

-- Question 3: With Code Snippet
INSERT INTO questions (quiz_id, question_text, question_type, code_snippet, explanation, display_order, points)
VALUES (@quiz_id, 'What will be the output of this code?', 'MULTIPLE_CHOICE',
        'int x = 5;\nint y = 3;\nSystem.out.println(x + y);',
        'The + operator adds the two integer values, resulting in 8.', 3, 1);
SET @q3_id = LAST_INSERT_ID();

INSERT INTO question_options (question_id, option_text, is_correct, display_order) VALUES
(@q3_id, '53', false, 1),
(@q3_id, '8', true, 2),
(@q3_id, 'x + y', false, 3),
(@q3_id, 'Error', false, 4);

-- Question 4: Multiple Choice
INSERT INTO questions (quiz_id, question_text, question_type, explanation, display_order, points)
VALUES (@quiz_id, 'Which method is the entry point for a Java application?', 'MULTIPLE_CHOICE',
        'The main() method with the signature "public static void main(String[] args)" is the entry point.', 4, 1);
SET @q4_id = LAST_INSERT_ID();

INSERT INTO question_options (question_id, option_text, is_correct, display_order) VALUES
(@q4_id, 'start()', false, 1),
(@q4_id, 'run()', false, 2),
(@q4_id, 'main()', true, 3),
(@q4_id, 'init()', false, 4);

-- Question 5: Multiple Choice
INSERT INTO questions (quiz_id, question_text, question_type, explanation, display_order, points)
VALUES (@quiz_id, 'What is the file extension for Java source files?', 'MULTIPLE_CHOICE',
        'Java source files have the .java extension, and compiled files have .class extension.', 5, 1);
SET @q5_id = LAST_INSERT_ID();

INSERT INTO question_options (question_id, option_text, is_correct, display_order) VALUES
(@q5_id, '.jv', false, 1),
(@q5_id, '.class', false, 2),
(@q5_id, '.java', true, 3),
(@q5_id, '.js', false, 4);

SELECT 'Quiz created successfully with 5 questions!' AS result;
