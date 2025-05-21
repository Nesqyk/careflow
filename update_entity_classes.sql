-- Update Vitals entity class
ALTER TABLE vitals ADD COLUMN appointment_id INT;
UPDATE vitals SET appointment_id = (
    SELECT a.appointment_id 
    FROM appointments a 
    WHERE a.patient_id = vitals.patient_id 
    ORDER BY a.appointment_date DESC 
    LIMIT 1
) WHERE appointment_id IS NULL;

-- Update Allergy entity class
ALTER TABLE allergies ADD COLUMN appointment_id INT;
UPDATE allergies SET appointment_id = (
    SELECT a.appointment_id 
    FROM appointments a 
    WHERE a.patient_id = allergies.patient_id 
    ORDER BY a.appointment_date DESC 
    LIMIT 1
) WHERE appointment_id IS NULL;

-- Update Condition entity class
ALTER TABLE conditions ADD COLUMN appointment_id INT;
UPDATE conditions SET appointment_id = (
    SELECT a.appointment_id 
    FROM appointments a 
    WHERE a.patient_id = conditions.patient_id 
    ORDER BY a.appointment_date DESC 
    LIMIT 1
) WHERE appointment_id IS NULL;

-- Update Prescription entity class
ALTER TABLE prescriptions ADD COLUMN appointment_id INT;
UPDATE prescriptions SET appointment_id = (
    SELECT a.appointment_id 
    FROM appointments a 
    WHERE a.patient_id = prescriptions.patient_id 
    ORDER BY a.appointment_date DESC 
    LIMIT 1
) WHERE appointment_id IS NULL;

-- Update PrescriptionDetails entity class
ALTER TABLE prescription_details ADD COLUMN appointment_id INT;
UPDATE prescription_details SET appointment_id = (
    SELECT p.appointment_id 
    FROM prescriptions p 
    WHERE p.prescription_id = prescription_details.prescription_id
) WHERE appointment_id IS NULL;

-- Update Attachment entity class (if it exists)
ALTER TABLE attachments ADD COLUMN appointment_id INT;
UPDATE attachments SET appointment_id = (
    SELECT a.appointment_id 
    FROM appointments a 
    WHERE a.patient_id = attachments.patient_id 
    ORDER BY a.appointment_date DESC 
    LIMIT 1
) WHERE appointment_id IS NULL;

-- Update VisitNote entity class (if it exists)
ALTER TABLE visit_notes ADD COLUMN appointment_id INT;
UPDATE visit_notes SET appointment_id = (
    SELECT a.appointment_id 
    FROM appointments a 
    WHERE a.patient_id = visit_notes.patient_id 
    ORDER BY a.appointment_date DESC 
    LIMIT 1
) WHERE appointment_id IS NULL; 