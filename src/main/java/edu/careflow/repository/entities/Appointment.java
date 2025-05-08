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
    private final String room;
    private final String symptoms;
    private final String diagnosis;
    private final String service_type;

    public Appointment(int appointmentId, int patientId, int doctorId, int nurseId,
                       LocalDateTime appointmentDate, String status, String notes,
                       LocalDateTime createdAt, String room, String symptoms, String diagnosis, String serviceType) {
        this.appointmentId = appointmentId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.nurseId = nurseId;
        this.appointmentDate = appointmentDate;
        this.status = status;
        this.notes = notes;
        this.createdAt = createdAt;
        this.room = room;
        this.symptoms = symptoms;
        this.diagnosis = diagnosis;
        this.service_type = serviceType;
    }

    // u forgot about the getters

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

    public String getRoom() {
        return room;
    }

    public String getConditions() {
        return notes; // Assuming conditions are stored in notes
    }


    public String getSymptoms() {
        return symptoms;
    }

    public String getDiagnosis() {
        return diagnosis;
    }

    public String getServiceType() {
        return service_type;
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
                ", room='" + room + '\'' +
                ", symptoms='" + symptoms + '\'' +
                ", diagnosis='" + diagnosis + '\'' +
                '}';
    }
}
