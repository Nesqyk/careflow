package edu.careflow.repository.entities;

import java.time.LocalDateTime;

public class Doctor {

    private final int doctorId;
    private final String firstName;
    private final String lastName;
    private final String specialization;
    private final String licenseNumber;
    private final String contactNumber;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    private boolean availability;

    public Doctor(int doctorId, String firstName, String lastName, String specialization, String licenseNumber, String contactNumber, LocalDateTime createdAt, LocalDateTime updatedAt, boolean availability) {
        this.doctorId = doctorId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.specialization = specialization;
        this.licenseNumber = licenseNumber;
        this.contactNumber = contactNumber;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.availability = availability;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getSpecialization() {
        return specialization;
    }

    public String getLicenseNumber() {
        return licenseNumber;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public boolean isAvailable() {
        return availability;
    }

    public void setAvailability(boolean availability) {
        this.availability = availability;
    }

    @Override
    public String toString() {
        return "Doctor{" +
                "doctorId=" + doctorId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", specialization='" + specialization + '\'' +
                ", licenseNumber='" + licenseNumber + '\'' +
                ", contactNumber='" + contactNumber + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", availability=" + availability +
                '}';
    }
}