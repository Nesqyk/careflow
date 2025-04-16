package edu.careflow.repository.entities;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Patient {

    private final int patientId;
    private final String firstName;
    private final String lastName;
    private final LocalDate dateOfBirth;
    private final String gender;

    enum Gender {
        MALE,
        FEMALE,
        OTHER
    }

    private final String contact;
    private final String email;
    private final String address;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public Patient(int patientId, String firstName, String lastName, LocalDate dateOfBirth, String gender, String contact, String email, String address, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.patientId = patientId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.contact = contact;
        this.email = email;
        this.address = address;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public int getPatientId() {
        return patientId;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public String getContact() {
        return contact;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public String toString() {
        return "Patient{" +
                "patientId=" + patientId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", gender='" + gender + '\'' +
                ", contact='" + contact + '\'' +
                ", email='" + email + '\'' +
                ", address='" + address + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}