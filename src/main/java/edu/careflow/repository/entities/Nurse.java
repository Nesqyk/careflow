package edu.careflow.repository.entities;

import java.time.LocalDate;

public class Nurse {
    private int nurseId;
    private String firstName;
    private String lastName;
    private String licenseNumber;
    private String phone;
    private LocalDate createdAt;

    public Nurse() {}

    public Nurse(int nurseId, String firstName, String lastName, String licenseNumber, String phone, LocalDate createdAt) {
        this.nurseId = nurseId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.licenseNumber = licenseNumber;
        this.phone = phone;
        this.createdAt = createdAt;
    }

    public int getNurseId() { return nurseId; }
    public void setNurseId(int nurseId) { this.nurseId = nurseId; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public LocalDate getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDate createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return firstName + " " + lastName;
    }
} 