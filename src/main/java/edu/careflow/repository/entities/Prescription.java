package edu.careflow.repository.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Prescription {
    private int prescriptionId;
    private int patientId;
    private int doctorId;
    private LocalDate issueDate;
    private LocalDate validUntil;
    private LocalDateTime createdAt;
    private int appointmentId;

    public Prescription(int prescriptionId, int patientId, int doctorId, LocalDate issueDate, 
                       LocalDate validUntil, LocalDateTime createdAt, int appointmentId) {
        this.prescriptionId = prescriptionId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.issueDate = issueDate;
        this.validUntil = validUntil;
        this.createdAt = createdAt;
        this.appointmentId = appointmentId;
    }

    // Getters and Setters
    public int getPrescriptionId() { return prescriptionId; }
    public void setPrescriptionId(int prescriptionId) { this.prescriptionId = prescriptionId; }

    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }

    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }

    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }

    public LocalDate getValidUntil() { return validUntil; }
    public void setValidUntil(LocalDate validUntil) { this.validUntil = validUntil; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public int getAppointmentId() { return appointmentId; }
    public void setAppointmentId(int appointmentId) { this.appointmentId = appointmentId; }
}