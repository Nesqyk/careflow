package edu.careflow.repository.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class VisitNote {
    private int visitNoteId;
    private int patientId;
    private int doctorId;
    private LocalDate visitDate;
    private String primaryDiagnosis;
    private String secondaryDiagnosis;
    private String notes;
    private byte[] imageData;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String status; // "Draft", "Final", "Archived"
    private int appointmentId;

    public VisitNote(int visitNoteId, int patientId, int doctorId, LocalDate visitDate,
                    String primaryDiagnosis, String secondaryDiagnosis, String notes,
                    byte[] imageData, LocalDateTime createdAt, LocalDateTime updatedAt,
                    String status, int appointmentId) {
        this.visitNoteId = visitNoteId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.visitDate = visitDate;
        this.primaryDiagnosis = primaryDiagnosis;
        this.secondaryDiagnosis = secondaryDiagnosis;
        this.notes = notes;
        this.imageData = imageData;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.status = status;
        this.appointmentId = appointmentId;
    }

    // Getters
    public int getVisitNoteId() { return visitNoteId; }
    public int getPatientId() { return patientId; }
    public int getDoctorId() { return doctorId; }
    public LocalDate getVisitDate() { return visitDate; }
    public String getPrimaryDiagnosis() { return primaryDiagnosis; }
    public String getSecondaryDiagnosis() { return secondaryDiagnosis; }
    public String getNotes() { return notes; }
    public byte[] getImageData() { return imageData; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public String getStatus() { return status; }
    public int getAppointmentId() { return appointmentId; }

    // Setters
    public void setVisitNoteId(int visitNoteId) { this.visitNoteId = visitNoteId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }
    public void setVisitDate(LocalDate visitDate) { this.visitDate = visitDate; }
    public void setPrimaryDiagnosis(String primaryDiagnosis) { this.primaryDiagnosis = primaryDiagnosis; }
    public void setSecondaryDiagnosis(String secondaryDiagnosis) { this.secondaryDiagnosis = secondaryDiagnosis; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setImageData(byte[] imageData) { this.imageData = imageData; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setStatus(String status) { this.status = status; }
    public void setAppointmentId(int appointmentId) { this.appointmentId = appointmentId; }

    // Validation methods
    public boolean isValid() {
        return patientId > 0 && 
               doctorId > 0 && 
               visitDate != null && 
               primaryDiagnosis != null && !primaryDiagnosis.trim().isEmpty() &&
               notes != null && !notes.trim().isEmpty() &&
               status != null && !status.trim().isEmpty();
    }

    public boolean isDraft() {
        return "Draft".equals(status);
    }

    public boolean isFinal() {
        return "Final".equals(status);
    }

    public boolean isArchived() {
        return "Archived".equals(status);
    }
} 