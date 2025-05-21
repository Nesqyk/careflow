-- Add appointment_id column to vitals table
ALTER TABLE vitals ADD COLUMN appointment_id INT;
ALTER TABLE vitals ADD CONSTRAINT fk_vitals_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(appointment_id);

-- Add appointment_id column to allergies table
ALTER TABLE allergies ADD COLUMN appointment_id INT;
ALTER TABLE allergies ADD CONSTRAINT fk_allergies_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(appointment_id);

-- Add appointment_id column to conditions table
ALTER TABLE conditions ADD COLUMN appointment_id INT;
ALTER TABLE conditions ADD CONSTRAINT fk_conditions_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(appointment_id);

-- Add appointment_id column to prescriptions table
ALTER TABLE prescriptions ADD COLUMN appointment_id INT;
ALTER TABLE prescriptions ADD CONSTRAINT fk_prescriptions_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(appointment_id);

-- Add appointment_id column to prescription_details table
ALTER TABLE prescription_details ADD COLUMN appointment_id INT;
ALTER TABLE prescription_details ADD CONSTRAINT fk_prescription_details_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(appointment_id);

-- Add appointment_id column to attachments table (if it exists)
ALTER TABLE attachments ADD COLUMN appointment_id INT;
ALTER TABLE attachments ADD CONSTRAINT fk_attachments_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(appointment_id);

-- Add appointment_id column to visit_notes table (if it exists)
ALTER TABLE visit_notes ADD COLUMN appointment_id INT;
ALTER TABLE visit_notes ADD CONSTRAINT fk_visit_notes_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(appointment_id);

-- Add appointment_id column to billing table (if not already present)
ALTER TABLE billing ADD COLUMN appointment_id INT;
ALTER TABLE billing ADD CONSTRAINT fk_billing_appointment FOREIGN KEY (appointment_id) REFERENCES appointments(appointment_id);

-- Add indexes for better query performance
CREATE INDEX idx_vitals_appointment_id ON vitals(appointment_id);
CREATE INDEX idx_allergies_appointment_id ON allergies(appointment_id);
CREATE INDEX idx_conditions_appointment_id ON conditions(appointment_id);
CREATE INDEX idx_prescriptions_appointment_id ON prescriptions(appointment_id);
CREATE INDEX idx_prescription_details_appointment_id ON prescription_details(appointment_id);
CREATE INDEX idx_attachments_appointment_id ON attachments(appointment_id);
CREATE INDEX idx_visit_notes_appointment_id ON visit_notes(appointment_id);
CREATE INDEX idx_billing_appointment_id ON billing(appointment_id); 