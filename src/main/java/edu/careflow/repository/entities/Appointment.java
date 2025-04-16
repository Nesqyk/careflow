package edu.careflow.repository.entities;

import java.time.LocalDateTime;

public class Appointment {

    private final int appointmentId;
    private final int patientId;
    private final int doctorId;
    private final int nurseId;
    private final LocalDateTime appointmentDate;
    private final String status;
    private final String notes;
    private final LocalDateTime createdAt;

    public Appointment(int appointmentId, int patientId, int doctorId, int nurseId, LocalDateTime appointmentDate, String status, String notes, LocalDateTime createdAt) {
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.nurseId = nurseId;
        this.appointmentDate = appointmentDate;
        this.status = status;
        this.notes = notes;
        this.createdAt = createdAt;
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public int getPatientId() {
        return patientId;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public int getNurseId() {
        return nurseId;
    }

    public LocalDateTime getAppointmentDate() {
        return appointmentDate;
    }

    public String getStatus() {
        return status;
    }

    public String getNotes() {
        return notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "Appointment{" +
                "appointmentId=" + appointmentId +
                ", patientId=" + patientId +
                ", doctorId=" + doctorId +
                ", nurseId=" + nurseId +
                ", appointmentDate=" + appointmentDate +
                ", status='" + status + '\'' +
                ", notes='" + notes + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}