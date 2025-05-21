package edu.careflow.repository.entities;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class BillRate {
    private int rateId;
    private int doctorId;
    private String serviceType;
    private BigDecimal rateAmount;
    private String description;
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public BillRate(int rateId, int doctorId, String serviceType, BigDecimal rateAmount, 
                   String description, boolean isActive, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.rateId = rateId;
        this.doctorId = doctorId;
        this.serviceType = serviceType;
        this.rateAmount = rateAmount;
        this.description = description;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters
    public int getRateId() { return rateId; }
    public int getDoctorId() { return doctorId; }
    public String getServiceType() { return serviceType; }
    public BigDecimal getRateAmount() { return rateAmount; }
    public String getDescription() { return description; }
    public boolean isActive() { return isActive; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters
    public void setRateId(int rateId) { this.rateId = rateId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }
    public void setRateAmount(BigDecimal rateAmount) { this.rateAmount = rateAmount; }
    public void setDescription(String description) { this.description = description; }
    public void setActive(boolean active) { isActive = active; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
} 