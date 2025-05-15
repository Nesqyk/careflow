package edu.careflow.repository.entities;

import java.time.LocalDateTime;

public class Appointment {
    private int appointmentId;
    private int patientId;
    private int doctorId;
    private int nurseId;
    private LocalDateTime appointmentDate;
    private String status;
    private String notes;
    private LocalDateTime createdAt;
    private String room;
    private String symptoms;
    private String diagnosis;
    private String service_type;
    private String appointmentType;
    private String meetingLink;
    private String bookedBy;
    private String preferredContact;
    private LocalDateTime bookingTime;

    public Appointment(int appointmentId, int patientId, int doctorId, int nurseId,
                       LocalDateTime appointmentDate, String status, String notes,
                       LocalDateTime createdAt, String room, String symptoms, String diagnosis, 
                       String serviceType, String appointmentType, String meetingLink,
                       String bookedBy, String preferredContact, LocalDateTime bookingTime) {
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
        this.appointmentType = appointmentType;
        this.meetingLink = meetingLink;
        this.bookedBy = bookedBy;
        this.preferredContact = preferredContact;
        this.bookingTime = bookingTime;
    }

    public void setAppointmentStatus(String status) {
        this.status = status;
    }

    public void setAppointmentNotes(String notes) {
        this.notes = notes;

    }

    public String setAppointmentRoom(String room) {
        return room;
    }

    public String setAppointmentSymptoms(String symptoms) {
        return symptoms;
    }

    public String setAppointmentDiagnosis(String diagnosis) {
        return diagnosis;
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

    public String getAppointmentType() {
        return appointmentType;
    }

    public String getMeetingLink() {
        return meetingLink;
    }

    public String getBookedBy() {
        return bookedBy;
    }

    public String getPreferredContact() {
        return preferredContact;
    }

    public LocalDateTime getBookingTime() {
        return bookingTime;
    }

    public void setAppointmentId(int appointmentId) { this.appointmentId = appointmentId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }
    public void setNurseId(int nurseId) { this.nurseId = nurseId; }
    public void setAppointmentDate(LocalDateTime appointmentDate) { this.appointmentDate = appointmentDate; }
    public void setStatus(String status) { this.status = status; }
    public void setNotes(String notes) { this.notes = notes; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setRoom(String room) { this.room = room; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }
    public void setServiceType(String service_type) { this.service_type = service_type; }
    public void setAppointmentType(String appointmentType) { this.appointmentType = appointmentType; }
    public void setMeetingLink(String meetingLink) { this.meetingLink = meetingLink; }
    public void setBookedBy(String bookedBy) { this.bookedBy = bookedBy; }
    public void setPreferredContact(String preferredContact) { this.preferredContact = preferredContact; }
    public void setBookingTime(LocalDateTime bookingTime) { this.bookingTime = bookingTime; }

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
                ", appointmentType='" + appointmentType + '\'' +
                ", meetingLink='" + meetingLink + '\'' +
                ", bookedBy='" + bookedBy + '\'' +
                ", preferredContact='" + preferredContact + '\'' +
                ", bookingTime=" + bookingTime +
                '}';
    }
}
