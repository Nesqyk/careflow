package edu.careflow.repository.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Prescription {
    private final int prescriptionId;
    private final int patientId;
    private final int doctorId;
    private final LocalDate issueDate;
    private final LocalDate validUntil;
    private final LocalDateTime createdAt;

    public Prescription(int prescriptionId, int patientId, int doctorId, LocalDate issueDate, LocalDate validUntil, LocalDateTime createdAt) {
        this.prescriptionId = prescriptionId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.issueDate = issueDate;
        this.validUntil = validUntil;
        this.createdAt = createdAt;
    }

    // Getters
    public int getPrescriptionId() {
        return prescriptionId;
    }

    public int getPatientId() {
        return patientId;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public LocalDate getValidUntil() {
        return validUntil;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}