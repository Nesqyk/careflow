-- Vitals.java
ALTER TABLE vitals ADD COLUMN appointment_id INT;

-- Allergy.java
ALTER TABLE allergies ADD COLUMN appointment_id INT;

-- Condition.java
ALTER TABLE conditions ADD COLUMN appointment_id INT;

-- Prescription.java
ALTER TABLE prescriptions ADD COLUMN appointment_id INT;

-- PrescriptionDetails.java
ALTER TABLE prescription_details ADD COLUMN appointment_id INT;

-- Attachment.java (if it exists)
ALTER TABLE attachments ADD COLUMN appointment_id INT;

-- VisitNote.java (if it exists)
ALTER TABLE visit_notes ADD COLUMN appointment_id INT;

-- Java code for Vitals.java
/*
package edu.careflow.repository.entities;

import java.time.LocalDateTime;

public class Vitals {
    // ... existing fields ...
    private int appointmentId;
    
    // Update constructor
    public Vitals(int patientId, int vitalsId, int nurseId, String bloodPressure, int heartRate, 
                 int respiratoryRate, double weightKg, double heightCm, double temperature, 
                 double oxygenSaturation, LocalDateTime recordedAt, LocalDateTime updatedAt, int appointmentId) {
        // ... existing assignments ...
        this.appointmentId = appointmentId;
    }
    
    // Add getter and setter
    public int getAppointmentId() {
        return appointmentId;
    }
    
    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }
}
*/

-- Java code for Allergy.java
/*
package edu.careflow.repository.entities;

import java.util.ArrayList;
import java.util.List;

public class Allergy {
    // ... existing fields ...
    private int appointmentId;
    
    // Update constructor
    public Allergy(int allergyId, int patientId, String allergen, String severity, String comment, int appointmentId) {
        // ... existing assignments ...
        this.appointmentId = appointmentId;
    }
    
    // Add getter and setter
    public int getAppointmentId() {
        return appointmentId;
    }
    
    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }
}
*/

-- Java code for Condition.java
/*
package edu.careflow.repository.entities;

import java.time.LocalDate;

public class Condition {
    // ... existing fields ...
    private int appointmentId;
    
    // Update constructor
    public Condition(int conditionId, int patientId, String conditionName, String description, 
                    LocalDate onSetDate, String status, int appointmentId) {
        // ... existing assignments ...
        this.appointmentId = appointmentId;
    }
    
    // Add getter and setter
    public int getAppointmentId() {
        return appointmentId;
    }
    
    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }
}
*/

-- Java code for Prescription.java
/*
package edu.careflow.repository.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Prescription {
    // ... existing fields ...
    private int appointmentId;
    
    // Update constructor
    public Prescription(int prescriptionId, int patientId, int doctorId, LocalDate issueDate, 
                       LocalDate validUntil, LocalDateTime createdAt, int appointmentId) {
        // ... existing assignments ...
        this.appointmentId = appointmentId;
    }
    
    // Add getter and setter
    public int getAppointmentId() {
        return appointmentId;
    }
    
    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }
}
*/ 