-- =========================================================================
-- SmartCare seed data
--
-- Runs automatically on startup (see spring.sql.init.mode: always in
-- application.yml). Safe to leave in place across restarts: duplicate-key
-- errors on re-inserts are swallowed because spring.sql.init.continue-on-error
-- is also enabled, so this only ever inserts once per unique row.
--
-- IMPORTANT: change the admin password immediately in a real deployment.
-- The hash below corresponds to the plaintext password: Admin@12345
-- =========================================================================

INSERT INTO users (email, password, phone, role, enabled, account_non_locked, created_at, updated_at)
VALUES ('admin@smartcare.local', '$2b$10$64qRXrk1SLVzuYAvi2t94OL7/pyfKU/iI2DBZJkRNJ7nL6Qpy/pPa',
        '+1-555-010-0100', 'ROLE_ADMIN', TRUE, TRUE, NOW(), NOW());

-- ---- Departments (Module 5) ----
INSERT INTO departments (name, description, active, created_at, updated_at) VALUES
('General Medicine', 'Everyday illnesses, checkups, and referrals.', TRUE, NOW(), NOW()),
('Cardiology', 'Heart and blood-vessel conditions.', TRUE, NOW(), NOW()),
('Dermatology', 'Skin, hair, and nail conditions.', TRUE, NOW(), NOW()),
('Orthopedics', 'Bones, joints, and muscles.', TRUE, NOW(), NOW()),
('Neurology', 'Brain, spine, and nervous system.', TRUE, NOW(), NOW()),
('Pediatrics', 'Care for infants, children, and teens.', TRUE, NOW(), NOW()),
('Gynecology', 'Women\'s reproductive health.', TRUE, NOW(), NOW()),
('ENT', 'Ear, nose, and throat conditions.', TRUE, NOW(), NOW()),
('Dentistry', 'Teeth and oral health.', TRUE, NOW(), NOW());

-- ---- Symptoms (Module 6 - configurable, not hardcoded in Java) ----
INSERT INTO symptoms (name, description, active, created_at, updated_at) VALUES
('skin rash', 'Redness, bumps, or irritation on the skin.', TRUE, NOW(), NOW()),
('itching', 'Persistent urge to scratch.', TRUE, NOW(), NOW()),
('skin irritation', 'General skin discomfort or inflammation.', TRUE, NOW(), NOW()),
('chest pain', 'Pain or pressure in the chest.', TRUE, NOW(), NOW()),
('shortness of breath', 'Difficulty breathing normally.', TRUE, NOW(), NOW()),
('joint pain', 'Pain in one or more joints.', TRUE, NOW(), NOW()),
('back pain', 'Pain along the spine or lower back.', TRUE, NOW(), NOW()),
('headache', 'Pain in the head or upper neck.', TRUE, NOW(), NOW()),
('dizziness', 'Feeling lightheaded or unsteady.', TRUE, NOW(), NOW()),
('sore throat', 'Pain or scratchiness in the throat.', TRUE, NOW(), NOW()),
('ear pain', 'Pain inside or around the ear.', TRUE, NOW(), NOW()),
('toothache', 'Pain in or around a tooth.', TRUE, NOW(), NOW()),
('fever', 'Elevated body temperature.', TRUE, NOW(), NOW()),
('cough', 'Persistent coughing.', TRUE, NOW(), NOW());

-- ---- Symptom -> Department weighted mappings ----
INSERT INTO department_recommendations (symptom_id, department_id, weight, created_at, updated_at)
SELECT s.id, d.id, 3, NOW(), NOW()
FROM symptoms s, departments d
WHERE s.name = 'skin rash' AND d.name = 'Dermatology';

INSERT INTO department_recommendations (symptom_id, department_id, weight, created_at, updated_at)
SELECT s.id, d.id, 2, NOW(), NOW()
FROM symptoms s, departments d
WHERE s.name = 'itching' AND d.name = 'Dermatology';

INSERT INTO department_recommendations (symptom_id, department_id, weight, created_at, updated_at)
SELECT s.id, d.id, 2, NOW(), NOW()
FROM symptoms s, departments d
WHERE s.name = 'skin irritation' AND d.name = 'Dermatology';

INSERT INTO department_recommendations (symptom_id, department_id, weight, created_at, updated_at)
SELECT s.id, d.id, 3, NOW(), NOW()
FROM symptoms s, departments d
WHERE s.name = 'chest pain' AND d.name = 'Cardiology';

INSERT INTO department_recommendations (symptom_id, department_id, weight, created_at, updated_at)
SELECT s.id, d.id, 3, NOW(), NOW()
FROM symptoms s, departments d
WHERE s.name = 'shortness of breath' AND d.name = 'Cardiology';

INSERT INTO department_recommendations (symptom_id, department_id, weight, created_at, updated_at)
SELECT s.id, d.id, 3, NOW(), NOW()
FROM symptoms s, departments d
WHERE s.name = 'joint pain' AND d.name = 'Orthopedics';

INSERT INTO department_recommendations (symptom_id, department_id, weight, created_at, updated_at)
SELECT s.id, d.id, 2, NOW(), NOW()
FROM symptoms s, departments d
WHERE s.name = 'back pain' AND d.name = 'Orthopedics';

INSERT INTO department_recommendations (symptom_id, department_id, weight, created_at, updated_at)
SELECT s.id, d.id, 3, NOW(), NOW()
FROM symptoms s, departments d
WHERE s.name = 'headache' AND d.name = 'Neurology';

INSERT INTO department_recommendations (symptom_id, department_id, weight, created_at, updated_at)
SELECT s.id, d.id, 2, NOW(), NOW()
FROM symptoms s, departments d
WHERE s.name = 'dizziness' AND d.name = 'Neurology';

INSERT INTO department_recommendations (symptom_id, department_id, weight, created_at, updated_at)
SELECT s.id, d.id, 3, NOW(), NOW()
FROM symptoms s, departments d
WHERE s.name = 'sore throat' AND d.name = 'ENT';

INSERT INTO department_recommendations (symptom_id, department_id, weight, created_at, updated_at)
SELECT s.id, d.id, 3, NOW(), NOW()
FROM symptoms s, departments d
WHERE s.name = 'ear pain' AND d.name = 'ENT';

INSERT INTO department_recommendations (symptom_id, department_id, weight, created_at, updated_at)
SELECT s.id, d.id, 3, NOW(), NOW()
FROM symptoms s, departments d
WHERE s.name = 'toothache' AND d.name = 'Dentistry';

INSERT INTO department_recommendations (symptom_id, department_id, weight, created_at, updated_at)
SELECT s.id, d.id, 1, NOW(), NOW()
FROM symptoms s, departments d
WHERE s.name = 'fever' AND d.name = 'General Medicine';

INSERT INTO department_recommendations (symptom_id, department_id, weight, created_at, updated_at)
SELECT s.id, d.id, 1, NOW(), NOW()
FROM symptoms s, departments d
WHERE s.name = 'cough' AND d.name = 'General Medicine';
